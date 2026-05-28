package com.example.teacher.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parents",
    indices = [
        Index(value = ["phone"], unique = true),
    ],
)
data class ParentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phone: String,
    val password: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val addressDetail: String = "",
    val studentName: String,
    val studentGender: String,
    val studentGrade: String,
    val weakSubjects: String,
)

@Entity(
    tableName = "teachers",
    indices = [
        Index(value = ["phone"], unique = true),
    ],
)
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phone: String,
    val password: String,
    val avatarPath: String,
    val gender: String = "",
    val wechat: String?,
    val teachingExperience: String,
    val employmentStatus: String,
    val subjects: String,
    val grades: String,
    val pricePerHour: Double,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val poiName: String? = null,
    val poiAddress: String? = null,
    val addressDetail: String = "",
    val status: String = "PENDING",
)

@Entity(tableName = "demands")
data class DemandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentId: Int,
    val parentPhone: String,
    val parentAddress: String,
    val studentName: String,
    val studentGrade: String,
    val subject: String,
    val timeStartAt: Long,
    val timeEndAt: Long,
    val teacherGenderPreference: String? = null,
    val minPrice: Double,
    val maxPrice: Double,
    val status: String = "OPEN",
    val claimedTeacherId: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "chat_threads",
    indices = [
        Index(value = ["refType", "refId"], unique = true),
        Index(value = ["parentId"]),
        Index(value = ["teacherId"]),
    ],
)
data class ChatThreadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val refType: String,
    val refId: Int,
    val parentId: Int,
    val teacherId: Int,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["threadId", "createdAt"]),
    ],
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val threadId: Int,
    val senderRole: String,
    val senderId: Int,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentId: Int,
    val teacherId: Int,
    val studentName: String,
    val studentGrade: String,
    val parentPhone: String,
    val teacherPhone: String,
    val status: String = "PENDING",
    val paymentStatus: String = "UNPAID",
    val createdAt: Long = System.currentTimeMillis(),
    val scheduledAt: Long? = null,
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val applicationId: Int,
    val parentPhone: String,
    val teacherPhone: String,
    val amount: Double,
    val status: String = "PAID",
    val paidAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val name: String,
    val price: Double,
    val provider: String,
    val details: String,
    val imageUri: String?,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "cart_items",
    indices = [
        Index(value = ["parentId", "productId"], unique = true),
    ],
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentId: Int,
    val productId: Int,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "product_orders")
data class ProductOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val parentId: Int,
    val parentPhone: String,
    val totalAmount: Double,
    val status: String = "CREATED",
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long?,
)

@Entity(tableName = "product_order_items")
data class ProductOrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val type: String,
    val name: String,
    val price: Double,
    val provider: String,
    val details: String,
    val imageUri: String?,
    val quantity: Int,
)

@Entity(tableName = "product_payments")
data class ProductPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,
    val parentPhone: String,
    val amount: Double,
    val status: String = "PAID",
    val paidAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val reporterRole: String,
    val reporterId: Int?,
    val reporterPhone: String?,
    val category: String,
    val subject: String,
    val content: String,
    val status: String = "OPEN",
    val adminNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["userRole", "userId"]),
        Index(value = ["userRole", "userId", "isRead"]),
    ],
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userRole: String,
    val userId: Int,
    val category: String,
    val title: String,
    val content: String,
    val refType: String? = null,
    val refId: Int? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
)

data class TeacherOrderCount(
    val teacherId: Int,
    val orderCount: Int,
)
