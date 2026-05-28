const express = require("express");
const { z } = require("zod");
const { prisma } = require("../prisma");

async function getOrCreateThreadForApplication(applicationId) {
  const existing = await prisma.chatThread.findUnique({
    where: { refType_refId: { refType: "APPLICATION", refId: applicationId } },
    select: { id: true, parentId: true, teacherId: true },
  });
  if (existing) return existing;

  const app = await prisma.application.findUnique({
    where: { id: applicationId },
    select: { id: true, parentId: true, teacherId: true },
  });
  if (!app) throw new Error("APPLICATION_NOT_FOUND");

  const thread = await prisma.chatThread.create({
    data: {
      refType: "APPLICATION",
      refId: app.id,
      parentId: app.parentId,
      teacherId: app.teacherId,
    },
    select: { id: true, parentId: true, teacherId: true },
  });
  return thread;
}

function buildApplicationsRouter() {
  const router = express.Router();

  router.get("/mine", async (req, res) => {
    const user = req.user;
    if (!user) return res.status(401).json({ error: "UNAUTHORIZED" });
    if (user.role !== "PARENT" && user.role !== "TEACHER") return res.status(403).json({ error: "FORBIDDEN" });

    const limit = Math.min(Math.max(Number(req.query?.limit || "30"), 1), 100);
    const cursorId = req.query?.cursor ? Number(req.query.cursor) : null;

    const whereBase = user.role === "PARENT" ? { parentId: user.id } : { teacherId: user.id };
    const where = cursorId ? { ...whereBase, id: { lt: cursorId } } : whereBase;

    const rows = await prisma.application.findMany({
      where,
      orderBy: { id: "desc" },
      take: limit,
      select: { id: true, parentId: true, teacherId: true, status: true, createdAt: true },
    });

    const items = rows.reverse();
    const nextCursor = rows.length === limit ? String(rows[rows.length - 1].id) : null;
    return res.json({ items, nextCursor });
  });

  router.post("/", async (req, res) => {
    const schema = z.object({ teacherId: z.number().int().positive() });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });
    const { teacherId } = parsed.data;

    if (!req.user || req.user.role !== "PARENT") return res.status(403).json({ error: "FORBIDDEN" });
    const parentId = req.user.id;

    const teacher = await prisma.user.findUnique({ where: { id: teacherId }, select: { id: true, role: true } });
    if (!teacher || teacher.role !== "TEACHER") return res.status(404).json({ error: "TEACHER_NOT_FOUND" });

    const app = await prisma.application.create({
      data: { parentId, teacherId, status: "PENDING" },
      select: { id: true, parentId: true, teacherId: true, status: true, createdAt: true },
    });
    const thread = await getOrCreateThreadForApplication(app.id);
    return res.json({ application: app, threadId: thread.id });
  });

  router.patch("/:id/status", async (req, res) => {
    const applicationId = Number(req.params.id);
    if (!Number.isFinite(applicationId) || applicationId <= 0) return res.status(400).json({ error: "INVALID_INPUT" });

    const schema = z.object({ status: z.enum(["ACCEPTED", "REJECTED", "COMPLETED", "CANCELLED"]) });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });
    const { status } = parsed.data;

    const app = await prisma.application.findUnique({
      where: { id: applicationId },
      select: { id: true, parentId: true, teacherId: true },
    });
    if (!app) return res.status(404).json({ error: "APPLICATION_NOT_FOUND" });

    const user = req.user;
    if (!user) return res.status(401).json({ error: "UNAUTHORIZED" });
    const allowed =
      (user.role === "PARENT" && user.id === app.parentId) ||
      (user.role === "TEACHER" && user.id === app.teacherId);
    if (!allowed) return res.status(403).json({ error: "FORBIDDEN" });

    const updated = await prisma.application.update({
      where: { id: applicationId },
      data: { status },
      select: { id: true, status: true },
    });
    await getOrCreateThreadForApplication(applicationId);
    return res.json({ application: updated });
  });

  return router;
}

module.exports = { buildApplicationsRouter, getOrCreateThreadForApplication };
