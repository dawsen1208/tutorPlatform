package com.example.teacher.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ParentEntity::class,
        TeacherEntity::class,
        DemandEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class,
        ApplicationEntity::class,
        PaymentEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        ProductOrderEntity::class,
        ProductOrderItemEntity::class,
        ProductPaymentEntity::class,
        ReportEntity::class,
        NotificationEntity::class,
    ],
    version = 11,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun parentDao(): ParentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun demandDao(): DemandDao
    abstract fun chatDao(): ChatDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun paymentDao(): PaymentDao
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun productOrderDao(): ProductOrderDao
    abstract fun productOrderItemDao(): ProductOrderItemDao
    abstract fun productPaymentDao(): ProductPaymentDao
    abstract fun reportDao(): ReportDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        private fun ensureSchemaV8(db: SupportSQLiteDatabase) {
            fun hasColumn(table: String, column: String): Boolean {
                val cursor = db.query("PRAGMA table_info(`$table`)")
                cursor.use {
                    val nameIndex = it.getColumnIndex("name")
                    while (it.moveToNext()) {
                        if (nameIndex >= 0 && it.getString(nameIndex) == column) return true
                    }
                }
                return false
            }

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS parents (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  phone TEXT NOT NULL,
                  password TEXT NOT NULL,
                  address TEXT NOT NULL,
                  studentName TEXT NOT NULL,
                  studentGender TEXT NOT NULL,
                  studentGrade TEXT NOT NULL,
                  weakSubjects TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_parents_phone ON parents(phone)")
            if (!hasColumn("parents", "address")) db.execSQL("ALTER TABLE parents ADD COLUMN address TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("parents", "studentName")) db.execSQL("ALTER TABLE parents ADD COLUMN studentName TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("parents", "studentGender")) db.execSQL("ALTER TABLE parents ADD COLUMN studentGender TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("parents", "studentGrade")) db.execSQL("ALTER TABLE parents ADD COLUMN studentGrade TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("parents", "weakSubjects")) db.execSQL("ALTER TABLE parents ADD COLUMN weakSubjects TEXT NOT NULL DEFAULT ''")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS teachers (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  phone TEXT NOT NULL,
                  password TEXT NOT NULL,
                  avatarPath TEXT NOT NULL,
                  wechat TEXT,
                  teachingExperience TEXT NOT NULL,
                  employmentStatus TEXT NOT NULL,
                  subjects TEXT NOT NULL,
                  grades TEXT NOT NULL,
                  pricePerHour REAL NOT NULL,
                  address TEXT NOT NULL,
                  status TEXT NOT NULL DEFAULT 'PENDING'
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_teachers_phone ON teachers(phone)")
            if (!hasColumn("teachers", "avatarPath")) db.execSQL("ALTER TABLE teachers ADD COLUMN avatarPath TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "wechat")) db.execSQL("ALTER TABLE teachers ADD COLUMN wechat TEXT")
            if (!hasColumn("teachers", "teachingExperience")) db.execSQL("ALTER TABLE teachers ADD COLUMN teachingExperience TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "employmentStatus")) db.execSQL("ALTER TABLE teachers ADD COLUMN employmentStatus TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "subjects")) db.execSQL("ALTER TABLE teachers ADD COLUMN subjects TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "grades")) db.execSQL("ALTER TABLE teachers ADD COLUMN grades TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "pricePerHour")) db.execSQL("ALTER TABLE teachers ADD COLUMN pricePerHour REAL NOT NULL DEFAULT 0")
            if (!hasColumn("teachers", "address")) db.execSQL("ALTER TABLE teachers ADD COLUMN address TEXT NOT NULL DEFAULT ''")
            if (!hasColumn("teachers", "status")) db.execSQL("ALTER TABLE teachers ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS applications (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  parentId INTEGER NOT NULL,
                  teacherId INTEGER NOT NULL,
                  studentName TEXT NOT NULL,
                  studentGrade TEXT NOT NULL,
                  parentPhone TEXT NOT NULL,
                  teacherPhone TEXT NOT NULL,
                  status TEXT NOT NULL DEFAULT 'PENDING',
                  paymentStatus TEXT NOT NULL DEFAULT 'UNPAID',
                  createdAt INTEGER NOT NULL DEFAULT 0,
                  scheduledAt INTEGER
                )
                """.trimIndent(),
            )
            if (!hasColumn("applications", "status")) db.execSQL("ALTER TABLE applications ADD COLUMN status TEXT NOT NULL DEFAULT 'PENDING'")
            if (!hasColumn("applications", "paymentStatus")) db.execSQL("ALTER TABLE applications ADD COLUMN paymentStatus TEXT NOT NULL DEFAULT 'UNPAID'")
            if (!hasColumn("applications", "createdAt")) db.execSQL("ALTER TABLE applications ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn("applications", "scheduledAt")) db.execSQL("ALTER TABLE applications ADD COLUMN scheduledAt INTEGER")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS payments (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  applicationId INTEGER NOT NULL,
                  parentPhone TEXT NOT NULL,
                  teacherPhone TEXT NOT NULL,
                  amount REAL NOT NULL,
                  status TEXT NOT NULL DEFAULT 'PAID',
                  paidAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            if (!hasColumn("payments", "status")) db.execSQL("ALTER TABLE payments ADD COLUMN status TEXT NOT NULL DEFAULT 'PAID'")
            if (!hasColumn("payments", "paidAt")) db.execSQL("ALTER TABLE payments ADD COLUMN paidAt INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS products (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  type TEXT NOT NULL,
                  name TEXT NOT NULL,
                  price REAL NOT NULL,
                  provider TEXT NOT NULL,
                  details TEXT NOT NULL,
                  imageUri TEXT,
                  createdAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            if (!hasColumn("products", "imageUri")) db.execSQL("ALTER TABLE products ADD COLUMN imageUri TEXT")
            if (!hasColumn("products", "createdAt")) db.execSQL("ALTER TABLE products ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS cart_items (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  parentId INTEGER NOT NULL,
                  productId INTEGER NOT NULL,
                  quantity INTEGER NOT NULL,
                  addedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_cart_items_parentId_productId ON cart_items(parentId, productId)")
            if (!hasColumn("cart_items", "addedAt")) db.execSQL("ALTER TABLE cart_items ADD COLUMN addedAt INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS product_orders (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  parentId INTEGER NOT NULL,
                  parentPhone TEXT NOT NULL,
                  totalAmount REAL NOT NULL,
                  status TEXT NOT NULL DEFAULT 'CREATED',
                  createdAt INTEGER NOT NULL DEFAULT 0,
                  paidAt INTEGER
                )
                """.trimIndent(),
            )
            if (!hasColumn("product_orders", "status")) db.execSQL("ALTER TABLE product_orders ADD COLUMN status TEXT NOT NULL DEFAULT 'CREATED'")
            if (!hasColumn("product_orders", "createdAt")) db.execSQL("ALTER TABLE product_orders ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn("product_orders", "paidAt")) db.execSQL("ALTER TABLE product_orders ADD COLUMN paidAt INTEGER")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS product_order_items (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  orderId INTEGER NOT NULL,
                  productId INTEGER NOT NULL,
                  type TEXT NOT NULL,
                  name TEXT NOT NULL,
                  price REAL NOT NULL,
                  provider TEXT NOT NULL,
                  details TEXT NOT NULL,
                  imageUri TEXT,
                  quantity INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            if (!hasColumn("product_order_items", "imageUri")) db.execSQL("ALTER TABLE product_order_items ADD COLUMN imageUri TEXT")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS product_payments (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  orderId INTEGER NOT NULL,
                  parentPhone TEXT NOT NULL,
                  amount REAL NOT NULL,
                  status TEXT NOT NULL DEFAULT 'PAID',
                  paidAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            if (!hasColumn("product_payments", "status")) db.execSQL("ALTER TABLE product_payments ADD COLUMN status TEXT NOT NULL DEFAULT 'PAID'")
            if (!hasColumn("product_payments", "paidAt")) db.execSQL("ALTER TABLE product_payments ADD COLUMN paidAt INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reports (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  reporterRole TEXT NOT NULL,
                  reporterId INTEGER,
                  reporterPhone TEXT,
                  category TEXT NOT NULL,
                  subject TEXT NOT NULL,
                  content TEXT NOT NULL,
                  status TEXT NOT NULL,
                  adminNote TEXT,
                  createdAt INTEGER NOT NULL DEFAULT 0,
                  updatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent(),
            )
            if (!hasColumn("reports", "adminNote")) db.execSQL("ALTER TABLE reports ADD COLUMN adminNote TEXT")
            if (!hasColumn("reports", "createdAt")) db.execSQL("ALTER TABLE reports ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn("reports", "updatedAt")) db.execSQL("ALTER TABLE reports ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notifications (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  userRole TEXT NOT NULL,
                  userId INTEGER NOT NULL,
                  category TEXT NOT NULL,
                  title TEXT NOT NULL,
                  content TEXT NOT NULL,
                  refType TEXT,
                  refId INTEGER,
                  isRead INTEGER NOT NULL DEFAULT 0,
                  createdAt INTEGER NOT NULL DEFAULT 0,
                  readAt INTEGER
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userRole_userId ON notifications(userRole, userId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userRole_userId_isRead ON notifications(userRole, userId, isRead)")
            if (!hasColumn("notifications", "isRead")) db.execSQL("ALTER TABLE notifications ADD COLUMN isRead INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn("notifications", "createdAt")) db.execSQL("ALTER TABLE notifications ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            if (!hasColumn("notifications", "readAt")) db.execSQL("ALTER TABLE notifications ADD COLUMN readAt INTEGER")
        }

        private val MIGRATION_1_8 =
            object : Migration(1, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    ensureSchemaV8(db)
                }
            }

        private val MIGRATION_2_8 =
            object : Migration(2, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    ensureSchemaV8(db)
                }
            }

        private val MIGRATION_3_8 =
            object : Migration(3, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    ensureSchemaV8(db)
                }
            }

        private val MIGRATION_4_8 =
            object : Migration(4, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    ensureSchemaV8(db)
                }
            }

        private val MIGRATION_5_6 =
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS reports (
                          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          reporterRole TEXT NOT NULL,
                          reporterId INTEGER,
                          reporterPhone TEXT,
                          category TEXT NOT NULL,
                          subject TEXT NOT NULL,
                          content TEXT NOT NULL,
                          status TEXT NOT NULL,
                          adminNote TEXT,
                          createdAt INTEGER NOT NULL,
                          updatedAt INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )
                }
            }

        private val MIGRATION_6_7 =
            object : Migration(6, 7) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS notifications (
                          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          userRole TEXT NOT NULL,
                          userId INTEGER NOT NULL,
                          category TEXT NOT NULL,
                          title TEXT NOT NULL,
                          content TEXT NOT NULL,
                          refType TEXT,
                          refId INTEGER,
                          isRead INTEGER NOT NULL DEFAULT 0,
                          createdAt INTEGER NOT NULL,
                          readAt INTEGER
                        )
                        """.trimIndent(),
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userRole_userId ON notifications(userRole, userId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_notifications_userRole_userId_isRead ON notifications(userRole, userId, isRead)")
                }
            }

        private val MIGRATION_7_8 =
            object : Migration(7, 8) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE applications ADD COLUMN scheduledAt INTEGER")
                }
            }

        private val MIGRATION_8_9 =
            object : Migration(8, 9) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE parents ADD COLUMN latitude REAL")
                    db.execSQL("ALTER TABLE parents ADD COLUMN longitude REAL")
                    db.execSQL("ALTER TABLE parents ADD COLUMN poiName TEXT")
                    db.execSQL("ALTER TABLE parents ADD COLUMN poiAddress TEXT")
                    db.execSQL("ALTER TABLE parents ADD COLUMN addressDetail TEXT NOT NULL DEFAULT ''")

                    db.execSQL("ALTER TABLE teachers ADD COLUMN latitude REAL")
                    db.execSQL("ALTER TABLE teachers ADD COLUMN longitude REAL")
                    db.execSQL("ALTER TABLE teachers ADD COLUMN poiName TEXT")
                    db.execSQL("ALTER TABLE teachers ADD COLUMN poiAddress TEXT")
                    db.execSQL("ALTER TABLE teachers ADD COLUMN addressDetail TEXT NOT NULL DEFAULT ''")
                }
            }

        private val MIGRATION_9_10 =
            object : Migration(9, 10) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE teachers ADD COLUMN gender TEXT NOT NULL DEFAULT ''")
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS demands (
                          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          parentId INTEGER NOT NULL,
                          parentPhone TEXT NOT NULL,
                          parentAddress TEXT NOT NULL,
                          studentName TEXT NOT NULL,
                          studentGrade TEXT NOT NULL,
                          subject TEXT NOT NULL,
                          timeStartAt INTEGER NOT NULL,
                          timeEndAt INTEGER NOT NULL,
                          teacherGenderPreference TEXT,
                          minPrice REAL NOT NULL,
                          maxPrice REAL NOT NULL,
                          status TEXT NOT NULL DEFAULT 'OPEN',
                          claimedTeacherId INTEGER,
                          createdAt INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent(),
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_demands_status_createdAt ON demands(status, createdAt)")
                }
            }

        private val MIGRATION_10_11 =
            object : Migration(10, 11) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS chat_threads (
                          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          refType TEXT NOT NULL,
                          refId INTEGER NOT NULL,
                          parentId INTEGER NOT NULL,
                          teacherId INTEGER NOT NULL,
                          createdAt INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent(),
                    )
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_chat_threads_refType_refId ON chat_threads(refType, refId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_threads_parentId ON chat_threads(parentId)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_threads_teacherId ON chat_threads(teacherId)")

                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS chat_messages (
                          id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                          threadId INTEGER NOT NULL,
                          senderRole TEXT NOT NULL,
                          senderId INTEGER NOT NULL,
                          content TEXT NOT NULL,
                          createdAt INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent(),
                    )
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_threadId_createdAt ON chat_messages(threadId, createdAt)")
                }
            }

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutor_platform.db",
                )
                    .addMigrations(
                        MIGRATION_1_8,
                        MIGRATION_2_8,
                        MIGRATION_3_8,
                        MIGRATION_4_8,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                    )
                    .build()
                    .also { instance = it }
            }
        }
    }
}
