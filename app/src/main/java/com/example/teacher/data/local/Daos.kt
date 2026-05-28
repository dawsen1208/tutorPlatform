package com.example.teacher.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(parent: ParentEntity): Long

    @Query("SELECT * FROM parents WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): ParentEntity?

    @Query("SELECT * FROM parents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ParentEntity?

    @Query("SELECT * FROM parents WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun login(phone: String, password: String): ParentEntity?

    @Query("SELECT * FROM parents ORDER BY id DESC")
    fun getAll(): Flow<List<ParentEntity>>

    @Update
    suspend fun update(parent: ParentEntity)

    @Query("DELETE FROM parents WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(teacher: TeacherEntity): Long

    @Query("SELECT * FROM teachers WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun login(phone: String, password: String): TeacherEntity?

    @Query("SELECT * FROM teachers ORDER BY id DESC")
    fun getAll(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE status = 'APPROVED' ORDER BY id DESC")
    fun getApproved(): Flow<List<TeacherEntity>>

    @Query(
        """
        SELECT * FROM teachers
        WHERE status = 'APPROVED'
        AND (:subject IS NULL OR subjects LIKE '%' || :subject || '%')
        AND (:grade IS NULL OR grades LIKE '%' || :grade || '%')
        AND (:addressKeyword IS NULL OR address LIKE '%' || :addressKeyword || '%')
        AND (:employmentStatus IS NULL OR employmentStatus LIKE '%' || :employmentStatus || '%')
        AND (:minPrice IS NULL OR pricePerHour >= :minPrice)
        AND (:maxPrice IS NULL OR pricePerHour <= :maxPrice)
        ORDER BY id DESC
        """,
    )
    fun searchApproved(
        subject: String?,
        grade: String?,
        addressKeyword: String?,
        employmentStatus: String?,
        minPrice: Double?,
        maxPrice: Double?,
    ): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE status = 'PENDING' ORDER BY id DESC")
    fun getPending(): Flow<List<TeacherEntity>>

    @Update
    suspend fun update(teacher: TeacherEntity)

    @Query("UPDATE teachers SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface DemandDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(demand: DemandEntity): Long

    @Query("SELECT * FROM demands WHERE status = 'OPEN' ORDER BY createdAt DESC")
    fun getOpenDemands(): Flow<List<DemandEntity>>

    @Query("SELECT * FROM demands WHERE status = 'OPEN' ORDER BY createdAt DESC")
    suspend fun getOpenDemandsOnce(): List<DemandEntity>

    @Query("SELECT * FROM demands WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): DemandEntity?

    @Query("UPDATE demands SET status = :status, claimedTeacherId = :claimedTeacherId WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, claimedTeacherId: Int?)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_threads WHERE refType = :refType AND refId = :refId LIMIT 1")
    suspend fun getThreadByRef(refType: String, refId: Int): ChatThreadEntity?

    @Query("SELECT * FROM chat_threads WHERE id = :id LIMIT 1")
    suspend fun getThreadById(id: Int): ChatThreadEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertThread(thread: ChatThreadEntity): Long

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY createdAt ASC")
    fun getMessages(threadId: Int): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMessage(message: ChatMessageEntity): Long
}

@Dao
interface ApplicationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(application: ApplicationEntity): Long

    @Query("SELECT * FROM applications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE parentId = :parentId ORDER BY createdAt DESC")
    fun getByParentId(parentId: Int): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getByTeacherId(teacherId: Int): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ApplicationEntity>>

    @Query(
        """
        SELECT teacherId AS teacherId, COUNT(*) AS orderCount
        FROM applications
        INNER JOIN teachers ON teachers.id = applications.teacherId
        WHERE createdAt >= :startAt
        AND createdAt < :endAtExclusive
        AND teachers.status = 'APPROVED'
        AND applications.paymentStatus = 'PAID'
        AND applications.status IN ('ACCEPTED', 'COMPLETED')
        GROUP BY teacherId
        ORDER BY orderCount DESC
        LIMIT :limit
        """,
    )
    suspend fun getTopTeacherIdsByOrders(
        startAt: Long,
        endAtExclusive: Long,
        limit: Int,
    ): List<TeacherOrderCount>

    @Query("UPDATE applications SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE applications SET paymentStatus = :paymentStatus WHERE id = :id")
    suspend fun updatePaymentStatus(id: Int, paymentStatus: String)

    @Query("UPDATE applications SET scheduledAt = :scheduledAt WHERE id = :id")
    suspend fun updateScheduledAt(id: Int, scheduledAt: Long?)
}

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments ORDER BY paidAt DESC")
    fun getAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE parentPhone = :parentPhone ORDER BY paidAt DESC")
    fun getByParentPhone(parentPhone: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE teacherPhone = :teacherPhone ORDER BY paidAt DESC")
    fun getByTeacherPhone(teacherPhone: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE applicationId = :applicationId LIMIT 1")
    suspend fun getByApplicationId(applicationId: Int): PaymentEntity?
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE type = :type ORDER BY createdAt DESC")
    fun getByType(type: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductEntity?

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)
}

data class CartItemWithProduct(
    val cartItemId: Int,
    val parentId: Int,
    val productId: Int,
    val quantity: Int,
    val name: String,
    val price: Double,
    val provider: String,
    val details: String,
    val type: String,
    val imageUri: String?,
)

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = quantity + :delta WHERE parentId = :parentId AND productId = :productId")
    suspend fun incrementQuantity(parentId: Int, productId: Int, delta: Int): Int

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM cart_items WHERE parentId = :parentId")
    suspend fun clearByParentId(parentId: Int)

    @Transaction
    @Query(
        """
        SELECT
            c.id AS cartItemId,
            c.parentId AS parentId,
            c.productId AS productId,
            c.quantity AS quantity,
            p.name AS name,
            p.price AS price,
            p.provider AS provider,
            p.details AS details,
            p.type AS type,
            p.imageUri AS imageUri
        FROM cart_items c
        INNER JOIN products p ON p.id = c.productId
        WHERE c.parentId = :parentId
        ORDER BY c.addedAt DESC
        """,
    )
    fun getCartWithProducts(parentId: Int): Flow<List<CartItemWithProduct>>
}

@Dao
interface ProductOrderDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(order: ProductOrderEntity): Long

    @Query("SELECT * FROM product_orders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ProductOrderEntity?

    @Query("SELECT * FROM product_orders WHERE parentId = :parentId ORDER BY createdAt DESC")
    fun getByParentId(parentId: Int): Flow<List<ProductOrderEntity>>

    @Query("SELECT * FROM product_orders ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ProductOrderEntity>>

    @Query("UPDATE product_orders SET status = :status, paidAt = :paidAt WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, paidAt: Long?)
}

@Dao
interface ProductOrderItemDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(items: List<ProductOrderItemEntity>)

    @Query("SELECT * FROM product_order_items WHERE orderId = :orderId ORDER BY id ASC")
    fun getByOrderId(orderId: Int): Flow<List<ProductOrderItemEntity>>
}

@Dao
interface ProductPaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: ProductPaymentEntity): Long

    @Query("SELECT * FROM product_payments WHERE orderId = :orderId LIMIT 1")
    suspend fun getByOrderId(orderId: Int): ProductPaymentEntity?

    @Query("SELECT * FROM product_payments ORDER BY paidAt DESC")
    fun getAll(): Flow<List<ProductPaymentEntity>>

    @Query("SELECT * FROM product_payments WHERE parentPhone = :parentPhone ORDER BY paidAt DESC")
    fun getByParentPhone(parentPhone: String): Flow<List<ProductPaymentEntity>>

    @Query("UPDATE product_payments SET status = :status WHERE orderId = :orderId")
    suspend fun updateStatusByOrderId(orderId: Int, status: String)
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE reporterRole = :reporterRole AND reporterId = :reporterId ORDER BY createdAt DESC")
    fun getByReporter(reporterRole: String, reporterId: Int): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReportEntity?

    @Query("UPDATE reports SET status = :status, adminNote = :adminNote, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatusAndNote(id: Int, status: String, adminNote: String?, updatedAt: Long)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userRole = :userRole AND userId = :userId ORDER BY createdAt DESC")
    fun getByUser(userRole: String, userId: Int): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userRole = :userRole AND userId = :userId")
    suspend fun countByUser(userRole: String, userId: Int): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE userRole = :userRole AND userId = :userId AND isRead = 0")
    fun unreadCount(userRole: String, userId: Int): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE id = :id")
    suspend fun markRead(id: Int, readAt: Long)

    @Query("UPDATE notifications SET isRead = 1, readAt = :readAt WHERE userRole = :userRole AND userId = :userId AND isRead = 0")
    suspend fun markAllRead(userRole: String, userId: Int, readAt: Long)

    @Query("DELETE FROM notifications WHERE userRole = :userRole AND userId = :userId AND isRead = 1")
    suspend fun deleteReadByUser(userRole: String, userId: Int)

    @Query("DELETE FROM notifications WHERE userRole = :userRole AND userId = :userId")
    suspend fun deleteAllByUser(userRole: String, userId: Int)
}
