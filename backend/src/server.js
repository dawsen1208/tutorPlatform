require("dotenv").config();

const http = require("http");
const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const { prisma } = require("./prisma");
const { authRequired } = require("./middleware/auth");
const { buildAuthRouter } = require("./routes/auth");
const { buildApplicationsRouter } = require("./routes/applications");
const { buildDemandsRouter } = require("./routes/demands");
const { buildChatRouter } = require("./routes/chat");
const { installWsServer } = require("./ws");

const port = Number(process.env.PORT || "8080");
const host = String(process.env.HOST || "0.0.0.0").trim() || "0.0.0.0";
const jwtSecret = String(process.env.JWT_SECRET || "");
const phoneHashSecret = String(process.env.PHONE_HASH_SECRET || "");

if (!jwtSecret || !phoneHashSecret) {
  throw new Error("缺少环境变量：JWT_SECRET / PHONE_HASH_SECRET");
}

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "1mb" }));
app.use(morgan("combined"));

app.get("/", (_req, res) => {
  res
    .status(200)
    .type("html")
    .send(
      [
        "<!doctype html>",
        "<html>",
        "<head><meta charset='utf-8'><meta name='viewport' content='width=device-width, initial-scale=1'></head>",
        "<body style='font-family: system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial; padding: 24px'>",
        "<h2>tutor-platform-backend</h2>",
        "<p>服务已启动。</p>",
        "<ul>",
        "<li><a href='/health'>GET /health</a></li>",
        "<li>POST /api/auth/register</li>",
        "<li>POST /api/auth/login</li>",
        "<li>WS /ws?token=JWT</li>",
        "</ul>",
        "</body>",
        "</html>",
      ].join("")
    );
});

app.get("/health", (_req, res) => res.json({ ok: true }));

app.use("/api/auth", buildAuthRouter({ jwtSecret, phoneHashSecret }));

app.use("/api", authRequired({ jwtSecret }));

app.get("/api/me", async (req, res) => {
  const userId = req.user.id;
  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: { id: true, role: true, nickname: true, avatarUrl: true, createdAt: true },
  });
  return res.json({ user });
});

app.post("/api/devices/token", async (req, res) => {
  const token = String(req.body?.token || "").trim();
  const platform = String(req.body?.platform || "android").trim();
  if (!token) return res.status(400).json({ error: "INVALID_INPUT" });
  await prisma.deviceToken.upsert({
    where: { token },
    update: { userId: req.user.id, platform },
    create: { userId: req.user.id, token, platform },
  });
  return res.json({ ok: true });
});

app.use("/api/applications", buildApplicationsRouter());
app.use("/api/demands", buildDemandsRouter());
app.use("/api/chat", buildChatRouter());

const portEnv = process.env.PORT;
const allowPortFallback = !portEnv || String(portEnv).trim() === "" || String(portEnv).trim() === "8080";

app.use((err, _req, res, _next) => {
  return res.status(500).json({ error: "INTERNAL_ERROR" });
});

const server = http.createServer(app);
const { wss } = installWsServer({ httpServer: server, jwtSecret });
wss.on("error", (err) => {
  if (err?.code === "EADDRINUSE" && allowPortFallback) return;
  console.error(err);
});

function listenOnce(targetPort) {
  return new Promise((resolve, reject) => {
    const onError = (err) => {
      cleanup();
      reject(err);
    };
    const onListening = () => {
      cleanup();
      resolve(targetPort);
    };
    const cleanup = () => {
      server.off("error", onError);
      server.off("listening", onListening);
    };

    server.once("error", onError);
    server.once("listening", onListening);
    try {
      server.listen({ port: targetPort, host });
    } catch (err) {
      onError(err);
    }
  });
}

async function startServer() {
  let currentPort = port;
  const maxRetries = 10;

  for (let attempt = 0; attempt < maxRetries; attempt += 1) {
    try {
      const boundPort = await listenOnce(currentPort);
      console.log(`API listening on http://${host}:${boundPort}`);
      console.log(`Health: http://localhost:${boundPort}/health`);
      return;
    } catch (err) {
      if (err?.code === "EADDRINUSE" && allowPortFallback) {
        currentPort += 1;
        continue;
      }
      throw err;
    }
  }

  throw new Error(`端口占用：${port} - ${port + maxRetries - 1}`);
}

startServer().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});
