const WebSocket = require("ws");
const { prisma } = require("./prisma");
const { verifyAccessToken } = require("./util/jwt");
const { canAccessThread } = require("./routes/chat");

function parseJsonSafe(text) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function installWsServer({ httpServer, jwtSecret }) {
  const wss = new WebSocket.Server({ server: httpServer, path: "/ws" });

  function broadcastToThread(threadId, payload) {
    const msg = JSON.stringify(payload);
    wss.clients.forEach((client) => {
      if (client.readyState !== WebSocket.OPEN) return;
      if (!client.subscriptions || !client.subscriptions.has(threadId)) return;
      client.send(msg);
    });
  }

  wss.on("connection", async (ws, req) => {
    const url = new URL(req.url, "http://localhost");
    const token = url.searchParams.get("token");
    if (!token) {
      ws.close(4001, "UNAUTHORIZED");
      return;
    }

    let user;
    try {
      user = verifyAccessToken(token, jwtSecret);
    } catch {
      ws.close(4001, "UNAUTHORIZED");
      return;
    }

    ws.user = user;
    ws.subscriptions = new Set();
    ws.send(JSON.stringify({ type: "hello", user: { id: user.id, role: user.role, nickname: user.nickname } }));

    ws.on("message", async (data) => {
      const incoming = parseJsonSafe(String(data || ""));
      if (!incoming || typeof incoming.type !== "string") {
        ws.send(JSON.stringify({ type: "error", error: "INVALID_MESSAGE" }));
        return;
      }

      if (incoming.type === "subscribe") {
        const threadId = Number(incoming.threadId);
        if (!Number.isFinite(threadId) || threadId <= 0) {
          ws.send(JSON.stringify({ type: "error", error: "INVALID_THREAD" }));
          return;
        }
        const thread = await prisma.chatThread.findUnique({
          where: { id: threadId },
          select: { id: true, parentId: true, teacherId: true },
        });
        if (!thread || !canAccessThread(user, thread)) {
          ws.send(JSON.stringify({ type: "error", error: "FORBIDDEN" }));
          return;
        }
        ws.subscriptions.add(threadId);
        ws.send(JSON.stringify({ type: "subscribed", threadId }));
        return;
      }

      if (incoming.type === "message") {
        const threadId = Number(incoming.threadId);
        const content = String(incoming.content || "").trim();
        if (!Number.isFinite(threadId) || threadId <= 0 || !content) {
          ws.send(JSON.stringify({ type: "error", error: "INVALID_INPUT" }));
          return;
        }
        const thread = await prisma.chatThread.findUnique({
          where: { id: threadId },
          select: { id: true, parentId: true, teacherId: true },
        });
        if (!thread || !canAccessThread(user, thread)) {
          ws.send(JSON.stringify({ type: "error", error: "FORBIDDEN" }));
          return;
        }
        const msg = await prisma.chatMessage.create({
          data: {
            threadId,
            senderId: user.id,
            senderRole: user.role,
            content,
          },
          select: { id: true, threadId: true, senderId: true, senderRole: true, content: true, createdAt: true },
        });
        broadcastToThread(threadId, { type: "message", threadId, message: msg });
        return;
      }

      ws.send(JSON.stringify({ type: "error", error: "UNKNOWN_TYPE" }));
    });
  });

  return { wss };
}

module.exports = { installWsServer };

