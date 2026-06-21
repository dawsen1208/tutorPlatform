require("dotenv").config();

const fs = require("fs");
const http = require("http");
const path = require("path");
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
const publicDir = path.resolve(__dirname, "..", "public");
const downloadPagePath = path.join(publicDir, "index.html");
const apkFileName = "jiaonilaile-android-v1.0.apk";
const apkPath = path.join(publicDir, "downloads", apkFileName);

if (!jwtSecret || !phoneHashSecret) {
  throw new Error("缺少环境变量：JWT_SECRET / PHONE_HASH_SECRET");
}

const app = express();

app.disable("x-powered-by");
app.set("trust proxy", 1);

app.use(
  helmet({
    crossOriginResourcePolicy: { policy: "same-origin" },
  }),
);
app.use(cors());
app.use(express.json({ limit: "1mb" }));
app.use(morgan("combined"));

app.use(
  express.static(publicDir, {
    etag: true,
    lastModified: true,
    maxAge: "7d",
    setHeaders: (res, filePath) => {
      if (filePath.endsWith(".html")) {
        res.setHeader("Cache-Control", "no-store");
      }
      if (filePath.endsWith(".apk")) {
        res.setHeader("Cache-Control", "private, no-store");
        res.setHeader("Content-Type", "application/vnd.android.package-archive");
      }
    },
  }),
);

app.get("/", (_req, res) => {
  return res.redirect(302, "/download");
});

app.get("/download", (_req, res) => {
  if (!fs.existsSync(downloadPagePath)) {
    return res.status(404).json({ ok: false, error: "DOWNLOAD_PAGE_NOT_FOUND" });
  }
  res.setHeader("Cache-Control", "no-store");
  return res.sendFile(downloadPagePath);
});

app.get("/download/meta", (_req, res) => {
  const exists = fs.existsSync(apkPath);
  const stat = exists ? fs.statSync(apkPath) : null;
  return res.json({
    ok: true,
    app: "老师来了",
    platform: "android",
    version: "1.0.0",
    minAndroid: "7.0",
    fileName: apkFileName,
    downloadUrl: "/apk/latest",
    apkFound: exists,
    sizeBytes: stat?.size ?? null,
    displaySize: "30.5 MB",
  });
});

app.get("/apk/latest", (_req, res) => {
  if (!fs.existsSync(apkPath)) {
    return res.status(404).json({ ok: false, error: "APK_NOT_FOUND" });
  }
  res.setHeader("Cache-Control", "private, no-store");
  res.setHeader("Content-Type", "application/vnd.android.package-archive");
  return res.download(apkPath, apkFileName);
});

app.head("/apk/latest", (_req, res) => {
  if (!fs.existsSync(apkPath)) {
    return res.sendStatus(404);
  }
  const stat = fs.statSync(apkPath);
  res.setHeader("Cache-Control", "private, no-store");
  res.setHeader("Content-Type", "application/vnd.android.package-archive");
  res.setHeader("Content-Disposition", `attachment; filename=\"${apkFileName}\"`);
  res.setHeader("Content-Length", String(stat.size));
  return res.sendStatus(200);
});

app.get("/health", (_req, res) => res.json({ ok: true }));
app.get("/health/db", async (_req, res) => {
  try {
    await prisma.$queryRaw`SELECT 1`;
    await Promise.all([
      prisma.user.findFirst({ select: { id: true } }),
      prisma.application.findFirst({ select: { id: true } }),
      prisma.demand.findFirst({ select: { id: true } }),
      prisma.chatThread.findFirst({ select: { id: true } }),
    ]);
    return res.json({ ok: true, db: true });
  } catch (err) {
    const msg = err?.message || String(err);
    return res.status(500).json({ ok: false, db: false, error: msg });
  }
});

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
  const classifyReason = (e) => {
    const msg = String(e?.message || "").toLowerCase();
    if (msg.includes("database_url")) return "DB_CONFIG_MISSING";
    if (msg.includes("p1001") || msg.includes("can't reach database server") || msg.includes("connect")) return "DB_CONNECT_FAILED";
    if (msg.includes("doesn't exist") || msg.includes("unknown table") || msg.includes("no such table")) return "DB_SCHEMA_MISSING";
    return "UNKNOWN";
  };
  const isProd = String(process.env.NODE_ENV || "").trim() === "production";
  const message = err?.message || String(err);
  const reason = classifyReason(err);
  if (!isProd) console.error(err);
  return res.status(500).json({ error: "INTERNAL_ERROR", reason, message: isProd ? undefined : message });
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
      console.log(`Download: http://localhost:${boundPort}/download`);
      console.log(`APK: http://localhost:${boundPort}/apk/latest`);
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
