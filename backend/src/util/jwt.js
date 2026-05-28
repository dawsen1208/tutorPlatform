const jwt = require("jsonwebtoken");

function signAccessToken(payload, secret) {
  return jwt.sign(payload, secret, { expiresIn: "7d" });
}

function verifyAccessToken(token, secret) {
  return jwt.verify(token, secret);
}

function readBearerToken(req) {
  const header = req.headers.authorization || "";
  const parts = header.split(" ");
  if (parts.length === 2 && parts[0] === "Bearer") return parts[1];
  return null;
}

module.exports = { signAccessToken, verifyAccessToken, readBearerToken };
