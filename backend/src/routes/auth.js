const express = require("express");
const { z } = require("zod");
const { prisma } = require("../prisma");
const { hashPhone, hashPassword, verifyPassword } = require("../util/crypto");
const { signAccessToken } = require("../util/jwt");

function buildAuthRouter({ jwtSecret, phoneHashSecret }) {
  const router = express.Router();

  router.post("/register", async (req, res) => {
    const schema =
      z.object({
        role: z.enum(["PARENT", "TEACHER"]),
        phone: z.string().min(5).max(32),
        password: z.string().min(4).max(64),
        nickname: z.union([z.string().min(1).max(32), z.null()]).optional(),
      });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });
    const { role, phone, password, nickname } = parsed.data;

    const phoneHash = hashPhone(phone, phoneHashSecret);
    const existing = await prisma.user.findUnique({ where: { phoneHash } });
    if (existing) return res.status(409).json({ error: "PHONE_EXISTS" });

    const passwordHash = await hashPassword(password);
    const user = await prisma.user.create({
      data: {
        role,
        phoneHash,
        passwordHash,
        nickname: (nickname || "").trim() || (role === "PARENT" ? "家长用户" : "老师用户"),
      },
      select: { id: true, role: true, nickname: true, avatarUrl: true },
    });

    const accessToken = signAccessToken(user, jwtSecret);
    return res.json({ accessToken, user });
  });

  router.post("/login", async (req, res) => {
    const schema = z.object({
      role: z.enum(["PARENT", "TEACHER"]),
      phone: z.string().min(5).max(32),
      password: z.string().min(4).max(64),
    });
    const parsed = schema.safeParse(req.body);
    if (!parsed.success) return res.status(400).json({ error: "INVALID_INPUT" });
    const { role, phone, password } = parsed.data;

    const phoneHash = hashPhone(phone, phoneHashSecret);
    const user = await prisma.user.findUnique({ where: { phoneHash } });
    if (!user || user.role !== role) return res.status(401).json({ error: "INVALID_CREDENTIALS" });

    const ok = await verifyPassword(password, user.passwordHash);
    if (!ok) return res.status(401).json({ error: "INVALID_CREDENTIALS" });

    const safeUser = { id: user.id, role: user.role, nickname: user.nickname, avatarUrl: user.avatarUrl };
    const accessToken = signAccessToken(safeUser, jwtSecret);
    return res.json({ accessToken, user: safeUser });
  });

  return router;
}

module.exports = { buildAuthRouter };
