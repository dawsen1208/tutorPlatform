const { verifyAccessToken, readBearerToken } = require("../util/jwt");

function authRequired({ jwtSecret }) {
  return function (req, res, next) {
    const token = readBearerToken(req);
    if (!token) return res.status(401).json({ error: "UNAUTHORIZED" });
    try {
      const decoded = verifyAccessToken(token, jwtSecret);
      req.user = decoded;
      next();
    } catch {
      return res.status(401).json({ error: "UNAUTHORIZED" });
    }
  };
}

function requireRole(role) {
  return function (req, res, next) {
    if (!req.user) return res.status(401).json({ error: "UNAUTHORIZED" });
    if (req.user.role !== role) return res.status(403).json({ error: "FORBIDDEN" });
    next();
  };
}

module.exports = { authRequired, requireRole };
