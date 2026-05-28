const crypto = require("crypto");
const bcrypt = require("bcrypt");

function hashPhone(phone, secret) {
  const normalized = String(phone || "").trim();
  return crypto.createHmac("sha256", secret).update(normalized).digest("hex");
}

async function hashPassword(password) {
  const raw = String(password || "");
  if (!raw) throw new Error("密码不能为空");
  return bcrypt.hash(raw, 10);
}

async function verifyPassword(password, passwordHash) {
  return bcrypt.compare(String(password || ""), String(passwordHash || ""));
}

module.exports = { hashPhone, hashPassword, verifyPassword };
