const express = require("express");
const { z } = require("zod");
const { prisma } = require("../prisma");
const { getOrCreateThreadForApplication } = require("./applications");

function buildDemandsRouter() {
  const router = express.Router();

  router.post("/", async (req, res) => {
    if (!req.user || req.user.role !== "PARENT") return res.status(403).json({ error: "FORBIDDEN" });

    const schema =
      z.object({
        subject: z.string().min(1).max(32),
        studentGrade: z.string().min(1).max(32),
        timeStartAt: z.number().int().positive(),
        timeEndAt: z.number().int().positive(),
        teacherGenderPreference: z.string().max(16).optional(),
        minPrice: z.number().nonnegative(),
        maxPrice: z.number().positive(),
      });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });

    const { subject, studentGrade, timeStartAt, timeEndAt, teacherGenderPreference, minPrice, maxPrice } = parsed.data;
    if (timeEndAt <= timeStartAt) return res.status(400).json({ error: "INVALID_INPUT" });
    if (maxPrice < minPrice) return res.status(400).json({ error: "INVALID_INPUT" });

    const demand = await prisma.demand.create({
      data: {
        parentId: req.user.id,
        subject: subject.trim(),
        studentGrade: studentGrade.trim(),
        timeStartAt: new Date(timeStartAt),
        timeEndAt: new Date(timeEndAt),
        teacherGenderPreference: teacherGenderPreference?.trim()?.slice(0, 16) || null,
        minPrice,
        maxPrice,
        status: "OPEN",
      },
      select: {
        id: true,
        parentId: true,
        subject: true,
        studentGrade: true,
        timeStartAt: true,
        timeEndAt: true,
        teacherGenderPreference: true,
        minPrice: true,
        maxPrice: true,
        status: true,
        createdAt: true,
      },
    });

    return res.json({ demand });
  });

  router.get("/open", async (req, res) => {
    if (!req.user || req.user.role !== "TEACHER") return res.status(403).json({ error: "FORBIDDEN" });

    const limit = Math.min(Math.max(Number(req.query?.limit || "30"), 1), 100);
    const cursorId = req.query?.cursor ? Number(req.query.cursor) : null;
    const where = cursorId ? { status: "OPEN", id: { lt: cursorId } } : { status: "OPEN" };

    const rows = await prisma.demand.findMany({
      where,
      orderBy: { id: "desc" },
      take: limit,
      select: {
        id: true,
        parentId: true,
        subject: true,
        studentGrade: true,
        timeStartAt: true,
        timeEndAt: true,
        teacherGenderPreference: true,
        minPrice: true,
        maxPrice: true,
        status: true,
        createdAt: true,
      },
    });
    const items = rows.reverse();
    const nextCursor = rows.length === limit ? String(rows[rows.length - 1].id) : null;
    return res.json({ items, nextCursor });
  });

  router.post("/:id/claim", async (req, res) => {
    if (!req.user || req.user.role !== "TEACHER") return res.status(403).json({ error: "FORBIDDEN" });
    const demandId = Number(req.params.id);
    if (!Number.isFinite(demandId) || demandId <= 0) return res.status(400).json({ error: "INVALID_INPUT" });

    const teacherId = req.user.id;

    try {
      const result = await prisma.$transaction(async (tx) => {
        const demand = await tx.demand.findUnique({
          where: { id: demandId },
          select: { id: true, parentId: true, status: true },
        });
        if (!demand) return { kind: "NOT_FOUND" };
        if (demand.status !== "OPEN") return { kind: "ALREADY_CLAIMED" };

        const updatedDemand = await tx.demand.update({
          where: { id: demandId },
          data: { status: "CLAIMED", claimedTeacherId: teacherId },
          select: {
            id: true,
            parentId: true,
            subject: true,
            studentGrade: true,
            timeStartAt: true,
            timeEndAt: true,
            teacherGenderPreference: true,
            minPrice: true,
            maxPrice: true,
            status: true,
            createdAt: true,
          },
        });

        const application = await tx.application.create({
          data: { parentId: updatedDemand.parentId, teacherId, status: "PENDING" },
          select: { id: true, parentId: true, teacherId: true, status: true, createdAt: true },
        });

        return { kind: "OK", demand: updatedDemand, application };
      });

      if (result.kind === "NOT_FOUND") return res.status(404).json({ error: "DEMAND_NOT_FOUND" });
      if (result.kind === "ALREADY_CLAIMED") return res.status(409).json({ error: "DEMAND_NOT_OPEN" });

      const thread = await getOrCreateThreadForApplication(result.application.id);
      return res.json({ demand: result.demand, application: result.application, threadId: thread.id });
    } catch {
      return res.status(500).json({ error: "INTERNAL_ERROR" });
    }
  });

  return router;
}

module.exports = { buildDemandsRouter };

