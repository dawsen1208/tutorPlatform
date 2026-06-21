require("dotenv").config();

const { prisma } = require("../src/prisma");
const { hashPhone, hashPassword } = require("../src/util/crypto");

const phoneHashSecret = String(process.env.PHONE_HASH_SECRET || "");

if (!phoneHashSecret) {
  throw new Error("缺少环境变量：PHONE_HASH_SECRET");
}

function daysFromNow(days, hour) {
  const value = new Date();
  value.setDate(value.getDate() + days);
  value.setHours(hour, 0, 0, 0);
  return value;
}

const demoUsers = [
  { key: "parentA", role: "PARENT", phone: "13800000001", password: "123456", nickname: "演示家长-王女士" },
  { key: "parentB", role: "PARENT", phone: "13800000002", password: "123456", nickname: "演示家长-李先生" },
  { key: "parentC", role: "PARENT", phone: "13800000003", password: "123456", nickname: "演示家长-赵女士" },
  { key: "teacherA", role: "TEACHER", phone: "13900000001", password: "123456", nickname: "演示老师-张老师" },
  { key: "teacherB", role: "TEACHER", phone: "13900000002", password: "123456", nickname: "演示老师-陈老师" },
  { key: "teacherC", role: "TEACHER", phone: "13900000003", password: "123456", nickname: "演示老师-刘老师" },
  { key: "teacherD", role: "TEACHER", phone: "13900000004", password: "123456", nickname: "演示老师-周老师" },
  { key: "adminA", role: "ADMIN", phone: "13700000099", password: "123456", nickname: "系统管理员-演示账号" },
];

const demandConfigs = [
  {
    parentKey: "parentA",
    subject: "数学",
    studentGrade: "初二",
    timeStartAt: daysFromNow(1, 19),
    timeEndAt: daysFromNow(1, 21),
    teacherGenderPreference: "不限",
    minPrice: 180,
    maxPrice: 260,
    status: "OPEN",
  },
  {
    parentKey: "parentA",
    subject: "英语",
    studentGrade: "高一",
    timeStartAt: daysFromNow(2, 18),
    timeEndAt: daysFromNow(2, 20),
    teacherGenderPreference: "女",
    minPrice: 220,
    maxPrice: 320,
    status: "CLAIMED",
    claimedTeacherKey: "teacherA",
  },
  {
    parentKey: "parentA",
    subject: "语文",
    studentGrade: "小学五年级",
    timeStartAt: daysFromNow(3, 17),
    timeEndAt: daysFromNow(3, 19),
    teacherGenderPreference: "不限",
    minPrice: 150,
    maxPrice: 220,
    status: "OPEN",
  },
  {
    parentKey: "parentB",
    subject: "物理",
    studentGrade: "高二",
    timeStartAt: daysFromNow(3, 19),
    timeEndAt: daysFromNow(3, 21),
    teacherGenderPreference: "不限",
    minPrice: 240,
    maxPrice: 360,
    status: "OPEN",
  },
  {
    parentKey: "parentB",
    subject: "化学",
    studentGrade: "高三",
    timeStartAt: daysFromNow(4, 17),
    timeEndAt: daysFromNow(4, 19),
    teacherGenderPreference: "男",
    minPrice: 260,
    maxPrice: 400,
    status: "CLAIMED",
    claimedTeacherKey: "teacherB",
  },
  {
    parentKey: "parentB",
    subject: "生物",
    studentGrade: "初三",
    timeStartAt: daysFromNow(5, 18),
    timeEndAt: daysFromNow(5, 20),
    teacherGenderPreference: "女",
    minPrice: 180,
    maxPrice: 280,
    status: "CLOSED",
  },
  {
    parentKey: "parentC",
    subject: "历史",
    studentGrade: "高一",
    timeStartAt: daysFromNow(2, 19),
    timeEndAt: daysFromNow(2, 21),
    teacherGenderPreference: "不限",
    minPrice: 160,
    maxPrice: 240,
    status: "CLAIMED",
    claimedTeacherKey: "teacherC",
  },
  {
    parentKey: "parentC",
    subject: "地理",
    studentGrade: "初一",
    timeStartAt: daysFromNow(6, 16),
    timeEndAt: daysFromNow(6, 18),
    teacherGenderPreference: "不限",
    minPrice: 140,
    maxPrice: 210,
    status: "OPEN",
  },
  {
    parentKey: "parentC",
    subject: "英语口语",
    studentGrade: "小学六年级",
    timeStartAt: daysFromNow(7, 18),
    timeEndAt: daysFromNow(7, 19),
    teacherGenderPreference: "不限",
    minPrice: 130,
    maxPrice: 200,
    status: "OPEN",
  },
];

const applicationConfigs = [
  { parentKey: "parentA", teacherKey: "teacherA", status: "ACCEPTED" },
  { parentKey: "parentA", teacherKey: "teacherB", status: "COMPLETED" },
  { parentKey: "parentA", teacherKey: "teacherD", status: "PENDING" },
  { parentKey: "parentB", teacherKey: "teacherB", status: "PENDING" },
  { parentKey: "parentB", teacherKey: "teacherC", status: "REJECTED" },
  { parentKey: "parentC", teacherKey: "teacherC", status: "ACCEPTED" },
  { parentKey: "parentC", teacherKey: "teacherA", status: "CANCELLED" },
];

const threadMessageConfigs = [
  {
    applicationIndex: 0,
    messages: [
      { senderKey: "parentA", senderRole: "PARENT", content: "老师您好，孩子这周想先补一下英语阅读和语法。" },
      { senderKey: "teacherA", senderRole: "TEACHER", content: "可以的，我明晚 7 点后有空，我们先在线沟通一下学习情况。" },
      { senderKey: "parentA", senderRole: "PARENT", content: "好的，我把孩子最近的错题先整理一下发给您。" },
    ],
  },
  {
    applicationIndex: 1,
    messages: [
      { senderKey: "teacherB", senderRole: "TEACHER", content: "上次辅导结束后，孩子对函数和导数题型掌握已经明显提升。" },
      { senderKey: "parentA", senderRole: "PARENT", content: "是的，最近学校小测成绩也提高了，谢谢老师。" },
    ],
  },
  {
    applicationIndex: 3,
    messages: [
      { senderKey: "parentB", senderRole: "PARENT", content: "希望能重点提升孩子的化学实验题和综合题。" },
      { senderKey: "teacherB", senderRole: "TEACHER", content: "已收到，我先看一下需求详情，稍后给您安排试讲时间。" },
      { senderKey: "parentB", senderRole: "PARENT", content: "好的，周末晚上我们都方便沟通。" },
    ],
  },
  {
    applicationIndex: 5,
    messages: [
      { senderKey: "teacherC", senderRole: "TEACHER", content: "历史这块我会先从时间线和答题结构帮孩子梳理。" },
      { senderKey: "parentC", senderRole: "PARENT", content: "太好了，孩子现在主要是材料分析题丢分比较多。" },
      { senderKey: "teacherC", senderRole: "TEACHER", content: "没问题，我会先做一节针对性试讲，再给出后续计划。" },
      { senderKey: "parentC", senderRole: "PARENT", content: "收到，那我们先约明天晚上 8 点线上沟通。" },
    ],
  },
];

async function upsertDemoUser({ role, phone, password, nickname, avatarUrl = null }) {
  const phoneHash = hashPhone(phone, phoneHashSecret);
  const passwordHash = await hashPassword(password);

  return prisma.user.upsert({
    where: { phoneHash },
    update: { role, nickname, avatarUrl, passwordHash },
    create: { role, phoneHash, passwordHash, nickname, avatarUrl },
    select: { id: true, role: true, nickname: true },
  });
}

async function main() {
  const createdUsers = {};
  for (const item of demoUsers) {
    createdUsers[item.key] = await upsertDemoUser(item);
  }

  const demoUserIds = Object.values(createdUsers).map((item) => item.id);

  const summary = await prisma.$transaction(async (tx) => {
    const threads = await tx.chatThread.findMany({
      where: {
        OR: [{ parentId: { in: demoUserIds } }, { teacherId: { in: demoUserIds } }],
      },
      select: { id: true },
    });
    const threadIds = threads.map((item) => item.id);

    if (threadIds.length > 0) {
      await tx.chatMessage.deleteMany({ where: { threadId: { in: threadIds } } });
      await tx.chatThread.deleteMany({ where: { id: { in: threadIds } } });
    }

    await tx.application.deleteMany({
      where: {
        OR: [{ parentId: { in: demoUserIds } }, { teacherId: { in: demoUserIds } }],
      },
    });

    await tx.demand.deleteMany({
      where: {
        OR: [{ parentId: { in: demoUserIds } }, { claimedTeacherId: { in: demoUserIds } }],
      },
    });

    const demands = [];
    for (const item of demandConfigs) {
      demands.push(
        await tx.demand.create({
          data: {
            parentId: createdUsers[item.parentKey].id,
            subject: item.subject,
            studentGrade: item.studentGrade,
            timeStartAt: item.timeStartAt,
            timeEndAt: item.timeEndAt,
            teacherGenderPreference: item.teacherGenderPreference,
            minPrice: item.minPrice,
            maxPrice: item.maxPrice,
            status: item.status,
            claimedTeacherId: item.claimedTeacherKey ? createdUsers[item.claimedTeacherKey].id : null,
          },
        })
      );
    }

    const applications = [];
    for (const item of applicationConfigs) {
      applications.push(
        await tx.application.create({
          data: {
            parentId: createdUsers[item.parentKey].id,
            teacherId: createdUsers[item.teacherKey].id,
            status: item.status,
          },
        })
      );
    }

    const threadsCreated = [];
    let messageCount = 0;
    for (const item of threadMessageConfigs) {
      const application = applications[item.applicationIndex];
      const thread = await tx.chatThread.create({
        data: {
          refType: "APPLICATION",
          refId: application.id,
          parentId: application.parentId,
          teacherId: application.teacherId,
        },
      });
      threadsCreated.push(thread);

      await tx.chatMessage.createMany({
        data: item.messages.map((message) => ({
          threadId: thread.id,
          senderId: createdUsers[message.senderKey].id,
          senderRole: message.senderRole,
          content: message.content,
        })),
      });
      messageCount += item.messages.length;
    }

    return {
      userCount: demoUsers.length,
      demandCount: demands.length,
      applicationCount: applications.length,
      threadCount: threadsCreated.length,
      messageCount,
    };
  });

  console.log("演示数据已写入完成。");
  console.log(
    `统计：用户 ${summary.userCount} 个，需求 ${summary.demandCount} 条，申请 ${summary.applicationCount} 条，会话 ${summary.threadCount} 个，消息 ${summary.messageCount} 条。`
  );
  console.log("登录账号：");
  for (const item of demoUsers) {
    console.log(`- ${item.role} | ${item.nickname} | 手机号 ${item.phone} | 密码 ${item.password}`);
  }
}

main()
  .catch((error) => {
    console.error(error);
    process.exitCode = 1;
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
