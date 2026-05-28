const express = require("express");
const { z } = require("zod");
const { prisma } = require("../prisma");
const { getOrCreateThreadForApplication } = require("./applications");

function canAccessThread(user, thread) {
  if (!user) return false;
  if (user.role === "PARENT") return user.id === thread.parentId;
  if (user.role === "TEACHER") return user.id === thread.teacherId;
  return false;
}

function buildChatRouter() {
  const router = express.Router();

  router.post("/thread/by-application/:applicationId", async (req, res) => {
    const applicationId = Number(req.params.applicationId);
    if (!Number.isFinite(applicationId) || applicationId <= 0) return res.status(400).json({ error: "INVALID_INPUT" });

    const app = await prisma.application.findUnique({
      where: { id: applicationId },
      select: { id: true, parentId: true, teacherId: true },
    });
    if (!app) return res.status(404).json({ error: "APPLICATION_NOT_FOUND" });

    const user = req.user;
    const allowed =
      (user?.role === "PARENT" && user.id === app.parentId) ||
      (user?.role === "TEACHER" && user.id === app.teacherId);
    if (!allowed) return res.status(403).json({ error: "FORBIDDEN" });

    const thread = await getOrCreateThreadForApplication(applicationId);
    return res.json({ threadId: thread.id });
  });

  router.get("/threads/:threadId/messages", async (req, res) => {
    const threadId = Number(req.params.threadId);
    if (!Number.isFinite(threadId) || threadId <= 0) return res.status(400).json({ error: "INVALID_INPUT" });

    const schema =
      z.object({
        limit: z.string().optional(),
        cursor: z.string().optional(),
      });
    const parsed = schema.safeParse(req.query);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });

    const limit = Math.min(Math.max(Number(parsed.data.limit || "30"), 1), 100);
    const cursorId = parsed.data.cursor ? Number(parsed.data.cursor) : null;

    const thread = await prisma.chatThread.findUnique({
      where: { id: threadId },
      select: { id: true, parentId: true, teacherId: true },
    });
    if (!thread) return res.status(404).json({ error: "THREAD_NOT_FOUND" });
    if (!canAccessThread(req.user, thread)) return res.status(403).json({ error: "FORBIDDEN" });

    const where = cursorId ? { threadId, id: { lt: cursorId } } : { threadId };
    const rows = await prisma.chatMessage.findMany({
      where,
      orderBy: { id: "desc" },
      take: limit,
      select: { id: true, threadId: true, senderId: true, senderRole: true, content: true, createdAt: true },
    });
    const items = rows.reverse();
    const nextCursor = rows.length === limit ? String(rows[rows.length - 1].id) : null;
    return res.json({ items, nextCursor });
  });

  router.post("/threads/:threadId/messages", async (req, res) => {
    const threadId = Number(req.params.threadId);
    if (!Number.isFinite(threadId) || threadId <= 0) return res.status(400).json({ error: "INVALID_INPUT" });

    const schema = z.object({ content: z.string().min(1).max(2000) });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });

    const thread = await prisma.chatThread.findUnique({
      where: { id: threadId },
      select: { id: true, parentId: true, teacherId: true },
    });
    if (!thread) return res.status(404).json({ error: "THREAD_NOT_FOUND" });
    if (!canAccessThread(req.user, thread)) return res.status(403).json({ error: "FORBIDDEN" });

    const msg = await prisma.chatMessage.create({
      data: {
        threadId,
        senderId: req.user.id,
        senderRole: req.user.role,
        content: parsed.data.content.trim(),
      },
      select: { id: true, threadId: true, senderId: true, senderRole: true, content: true, createdAt: true },
    });
    return res.json({ message: msg });
  });

  return router;
}

module.exports = { buildChatRouter, canAccessThread };

