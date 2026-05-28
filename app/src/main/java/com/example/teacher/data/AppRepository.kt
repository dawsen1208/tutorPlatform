package com.example.teacher.data

import androidx.room.withTransaction
import com.example.teacher.core.zhApplicationStatus
import com.example.teacher.core.zhReportStatus
import com.example.teacher.core.zhTeacherReviewStatus
import com.example.teacher.core.zhProductOrderStatus
import com.example.teacher.data.local.AppDatabase
import com.example.teacher.data.local.ApplicationEntity
import com.example.teacher.data.local.CartItemEntity
import com.example.teacher.data.local.CartItemWithProduct
import com.example.teacher.data.local.ChatMessageEntity
import com.example.teacher.data.local.ChatThreadEntity
import com.example.teacher.data.local.ProductEntity
import com.example.teacher.data.local.ProductOrderEntity
import com.example.teacher.data.local.ProductOrderItemEntity
import com.example.teacher.data.local.ProductPaymentEntity
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.PaymentEntity
import com.example.teacher.data.local.NotificationEntity
import com.example.teacher.data.local.ReportEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.data.local.TeacherOrderCount
import com.example.teacher.data.local.DemandEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

data class TeacherOrderRanking(
    val teacher: TeacherEntity,
    val orderCount: Int,
)

class AppRepository(private val db: AppDatabase) {
    private val parentDao = db.parentDao()
    private val teacherDao = db.teacherDao()
    private val demandDao = db.demandDao()
    private val chatDao = db.chatDao()
    private val applicationDao = db.applicationDao()
    private val paymentDao = db.paymentDao()
    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val productOrderDao = db.productOrderDao()
    private val productOrderItemDao = db.productOrderItemDao()
    private val productPaymentDao = db.productPaymentDao()
    private val reportDao = db.reportDao()
    private val notificationDao = db.notificationDao()

    private fun splitTokens(raw: String): Set<String> {
        return raw
            .split('、', ',', '，', ';', '；', '|', '/', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun getAllParents(): Flow<List<ParentEntity>> = parentDao.getAll()

    suspend fun getParentById(id: Int): ParentEntity? = parentDao.getById(id)

    suspend fun getParentByPhone(phone: String): ParentEntity? = parentDao.getByPhone(phone.trim())

    suspend fun registerParent(
        phone: String,
        password: String,
        address: String = "",
        studentName: String = "",
        studentGender: String = "",
        studentGrade: String = "",
        weakSubjects: String = "",
    ): Result<ParentEntity> {
        if (phone.isBlank() || password.isBlank()) return Result.failure(IllegalArgumentException("手机号和密码不能为空"))
        val existing = parentDao.getByPhone(phone)
        if (existing != null) return Result.failure(IllegalStateException("该手机号已注册"))
        val entity = ParentEntity(
            phone = phone.trim(),
            password = password,
            address = address.trim(),
            studentName = studentName.trim(),
            studentGender = studentGender.trim(),
            studentGrade = studentGrade.trim(),
            weakSubjects = weakSubjects.trim(),
        )
        val id = parentDao.insert(entity).toInt()
        return Result.success(entity.copy(id = id))
    }

    suspend fun loginParent(phone: String, password: String): Result<ParentEntity> {
        val parent = parentDao.login(phone.trim(), password) ?: return Result.failure(IllegalStateException("手机号或密码错误"))
        return Result.success(parent)
    }

    suspend fun updateParent(parent: ParentEntity) {
        parentDao.update(parent)
    }

    fun getAllTeachers(): Flow<List<TeacherEntity>> = teacherDao.getAll()

    fun getApprovedTeachers(): Flow<List<TeacherEntity>> = teacherDao.getApproved()

    fun searchApprovedTeachers(
        subject: String?,
        grade: String?,
        addressKeyword: String?,
        employmentStatus: String?,
        minPrice: Double?,
        maxPrice: Double?,
    ): Flow<List<TeacherEntity>> = teacherDao.searchApproved(
        subject = subject,
        grade = grade,
        addressKeyword = addressKeyword,
        employmentStatus = employmentStatus,
        minPrice = minPrice,
        maxPrice = maxPrice,
    )

    fun getPendingTeachers(): Flow<List<TeacherEntity>> = teacherDao.getPending()

    suspend fun getTeacherById(id: Int): TeacherEntity? = teacherDao.getById(id)

    suspend fun getTeacherByPhone(phone: String): TeacherEntity? = teacherDao.getByPhone(phone.trim())

    suspend fun getTopTeachersByOrders(
        startAt: Long,
        endAtExclusive: Long,
        limit: Int = 5,
    ): List<TeacherOrderRanking> {
        val counts: List<TeacherOrderCount> =
            applicationDao.getTopTeacherIdsByOrders(
                startAt = startAt,
                endAtExclusive = endAtExclusive,
                limit = limit,
            )

        return counts.mapNotNull { item ->
            teacherDao.getById(item.teacherId)?.let { teacher ->
                TeacherOrderRanking(
                    teacher = teacher,
                    orderCount = item.orderCount,
                )
            }
        }
    }

    suspend fun registerTeacher(
        phone: String,
        password: String,
        avatarPath: String = "",
        wechat: String? = null,
        teachingExperience: String = "",
        employmentStatus: String = "",
        subjects: String = "",
        grades: String = "",
        pricePerHour: Double = 0.0,
        address: String = "",
    ): Result<TeacherEntity> {
        if (phone.isBlank() || password.isBlank()) return Result.failure(IllegalArgumentException("手机号和密码不能为空"))
        val existing = teacherDao.getByPhone(phone)
        if (existing != null) return Result.failure(IllegalStateException("该手机号已注册"))
        val entity = TeacherEntity(
            phone = phone.trim(),
            password = password,
            avatarPath = avatarPath,
            wechat = wechat?.trim()?.takeIf { it.isNotBlank() },
            teachingExperience = teachingExperience.trim(),
            employmentStatus = employmentStatus.trim(),
            subjects = subjects.trim(),
            grades = grades.trim(),
            pricePerHour = pricePerHour,
            address = address.trim(),
            status = if (teachingExperience.isBlank() && employmentStatus.isBlank() && subjects.isBlank() && grades.isBlank() && address.isBlank() && pricePerHour <= 0.0) {
                "INCOMPLETE"
            } else {
                "PENDING"
            },
        )
        val id = teacherDao.insert(entity).toInt()
        return Result.success(entity.copy(id = id))
    }

    suspend fun loginTeacher(phone: String, password: String): Result<TeacherEntity> {
        val teacher = teacherDao.login(phone.trim(), password) ?: return Result.failure(IllegalStateException("手机号或密码错误"))
        return Result.success(teacher)
    }

    suspend fun updateTeacher(teacher: TeacherEntity) {
        teacherDao.update(teacher)
    }

    suspend fun setTeacherStatus(teacherId: Int, status: String) {
        teacherDao.updateStatus(teacherId, status)
        val title = "审核状态更新"
        val content =
            when (status) {
                "APPROVED" -> "你的资料已通过审核，可以开始接单。"
                "REJECTED" -> "你的资料审核未通过，请修改后重新提交。"
                "DISABLED" -> "你的账号已被禁用，如有疑问请联系管理员。"
                "PENDING" -> "你的资料已提交，正在等待管理员审核。"
                else -> "你的审核状态已更新：${zhTeacherReviewStatus(status)}"
            }
        addNotification(
            userRole = "TEACHER",
            userId = teacherId,
            category = "REVIEW",
            title = title,
            content = content,
        )
    }

    suspend fun deleteTeacher(teacherId: Int) {
        teacherDao.deleteById(teacherId)
    }

    suspend fun deleteParent(parentId: Int) {
        parentDao.deleteById(parentId)
    }

    fun getParentApplications(parentId: Int): Flow<List<ApplicationEntity>> = applicationDao.getByParentId(parentId)

    fun getOpenDemands(): Flow<List<DemandEntity>> = demandDao.getOpenDemands()

    suspend fun getOpenDemandsOnce(): List<DemandEntity> = demandDao.getOpenDemandsOnce()

    suspend fun createDemand(
        parentId: Int,
        subject: String,
        studentGrade: String,
        timeStartAt: Long,
        timeEndAt: Long,
        teacherGenderPreference: String?,
        minPrice: Double,
        maxPrice: Double,
    ): Result<Int> {
        if (subject.isBlank()) return Result.failure(IllegalArgumentException("请选择科目"))
        if (studentGrade.isBlank()) return Result.failure(IllegalArgumentException("请选择年级"))
        if (timeEndAt <= timeStartAt) return Result.failure(IllegalArgumentException("请选择正确的时间段"))
        if (minPrice < 0 || maxPrice <= 0 || maxPrice < minPrice) return Result.failure(IllegalArgumentException("请输入正确的费用区间"))
        return runCatching {
            val parent = parentDao.getById(parentId) ?: error("家长不存在")
            val address = parent.address.ifBlank { parent.poiAddress ?: "" }.ifBlank { parent.poiName ?: "" } + parent.addressDetail
            val demandId =
                db.withTransaction {
                    val id =
                        demandDao.insert(
                            DemandEntity(
                                parentId = parentId,
                                parentPhone = parent.phone,
                                parentAddress = address.trim().ifBlank { "-" },
                                studentName = parent.studentName.ifBlank { "-" },
                                studentGrade = studentGrade,
                                subject = subject,
                                timeStartAt = timeStartAt,
                                timeEndAt = timeEndAt,
                                teacherGenderPreference = teacherGenderPreference?.takeIf { it.isNotBlank() },
                                minPrice = minPrice,
                                maxPrice = maxPrice,
                            ),
                        ).toInt()

                    val approvedTeachers = teacherDao.getApproved().first()
                    val matched =
                        approvedTeachers.filter { t ->
                            val subjects = splitTokens(t.subjects)
                            val grades = splitTokens(t.grades)
                            val genderOk =
                                teacherGenderPreference.isNullOrBlank() || (t.gender.isNotBlank() && t.gender == teacherGenderPreference)
                            genderOk && (subjects.isEmpty() || subjects.contains(subject)) && (grades.isEmpty() || grades.contains(studentGrade))
                        }

                    val fmt = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
                    val timeText = "${fmt.format(java.util.Date(timeStartAt))} - ${fmt.format(java.util.Date(timeEndAt))}"
                    matched.forEach { t ->
                        addNotification(
                            userRole = "TEACHER",
                            userId = t.id,
                            category = "ORDER",
                            title = "新的家长需求",
                            content = "科目：$subject · 年级：$studentGrade · 时间：$timeText · 费用：${minPrice.toInt()}-${maxPrice.toInt()}元/小时",
                            refType = "DEMAND",
                            refId = id,
                        )
                    }
                    id
                }
            demandId
        }
    }

    suspend fun claimDemand(demandId: Int, teacherId: Int): Result<Int> {
        return runCatching {
            db.withTransaction {
                val demand = demandDao.getById(demandId) ?: error("需求不存在")
                if (demand.status != "OPEN") error("该需求已被处理")
                val teacher = teacherDao.getById(teacherId) ?: error("老师不存在")
                val applicationId =
                    applicationDao.insert(
                        ApplicationEntity(
                            parentId = demand.parentId,
                            teacherId = teacherId,
                            studentName = demand.studentName,
                            studentGrade = demand.studentGrade,
                            parentPhone = demand.parentPhone,
                            teacherPhone = teacher.phone,
                            status = "ACCEPTED",
                            paymentStatus = "UNPAID",
                            scheduledAt = demand.timeStartAt,
                        ),
                    ).toInt()
                getOrCreateChatThread(refType = "APPLICATION", refId = applicationId, parentId = demand.parentId, teacherId = teacherId)
                demandDao.updateStatus(demandId, status = "CLAIMED", claimedTeacherId = teacherId)
                addNotification(
                    userRole = "PARENT",
                    userId = demand.parentId,
                    category = "ORDER",
                    title = "老师已接单",
                    content = "老师：${teacher.phone} · 科目：${demand.subject} · 年级：${demand.studentGrade}",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
                addNotification(
                    userRole = "TEACHER",
                    userId = teacherId,
                    category = "ORDER",
                    title = "接单成功",
                    content = "家长：${demand.parentPhone} · 科目：${demand.subject} · 年级：${demand.studentGrade}",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
                applicationId
            }
        }
    }

    fun getTeacherApplications(teacherId: Int): Flow<List<ApplicationEntity>> = applicationDao.getByTeacherId(teacherId)

    fun getAllApplications(): Flow<List<ApplicationEntity>> = applicationDao.getAll()

    suspend fun getApplicationById(id: Int): ApplicationEntity? = applicationDao.getById(id)

    suspend fun getPaymentByApplicationId(applicationId: Int): PaymentEntity? =
        paymentDao.getByApplicationId(applicationId)

    fun getChatMessages(threadId: Int): Flow<List<ChatMessageEntity>> = chatDao.getMessages(threadId)

    suspend fun getOrCreateChatThreadForApplication(applicationId: Int): Result<ChatThreadEntity> {
        return runCatching {
            db.withTransaction {
                val app = applicationDao.getById(applicationId) ?: error("申请不存在")
                getOrCreateChatThread(refType = "APPLICATION", refId = applicationId, parentId = app.parentId, teacherId = app.teacherId)
            }
        }
    }

    suspend fun sendChatMessage(threadId: Int, senderRole: String, senderId: Int, content: String): Result<Unit> {
        val text = content.trim()
        if (text.isBlank()) return Result.failure(IllegalArgumentException("请输入消息内容"))
        return runCatching {
            db.withTransaction {
                val thread = chatDao.getThreadById(threadId) ?: error("会话不存在")
                val allowed =
                    when (senderRole) {
                        "PARENT" -> senderId == thread.parentId
                        "TEACHER" -> senderId == thread.teacherId
                        else -> false
                    }
                if (!allowed) error("无权限发送消息")
                chatDao.insertMessage(
                    ChatMessageEntity(
                        threadId = threadId,
                        senderRole = senderRole,
                        senderId = senderId,
                        content = text,
                    ),
                )
            }
        }
    }

    private suspend fun getOrCreateChatThread(refType: String, refId: Int, parentId: Int, teacherId: Int): ChatThreadEntity {
        val existing = chatDao.getThreadByRef(refType, refId)
        if (existing != null) return existing
        val id =
            chatDao.insertThread(
                ChatThreadEntity(
                    refType = refType,
                    refId = refId,
                    parentId = parentId,
                    teacherId = teacherId,
                ),
            ).toInt()
        return ChatThreadEntity(id = id, refType = refType, refId = refId, parentId = parentId, teacherId = teacherId)
    }

    suspend fun submitApplication(parentId: Int, teacherId: Int): Result<ApplicationEntity> {
        val parent = parentDao.getById(parentId) ?: return Result.failure(IllegalStateException("家长信息不存在"))
        val teacher = teacherDao.getById(teacherId) ?: return Result.failure(IllegalStateException("老师信息不存在"))
        if (teacher.status != "APPROVED") return Result.failure(IllegalStateException("该老师尚未通过审核"))
        val entity = ApplicationEntity(
            parentId = parent.id,
            teacherId = teacher.id,
            studentName = parent.studentName,
            studentGrade = parent.studentGrade,
            parentPhone = parent.phone,
            teacherPhone = teacher.phone,
            status = "PENDING",
            paymentStatus = "UNPAID",
        )
        return runCatching {
            db.withTransaction {
                val id = applicationDao.insert(entity).toInt()
                getOrCreateChatThread(refType = "APPLICATION", refId = id, parentId = parentId, teacherId = teacherId)
                addNotification(
                    userRole = "PARENT",
                    userId = parentId,
                    category = "ORDER",
                    title = "预约申请已提交",
                    content = "老师：${teacher.phone} · 学生：${parent.studentName}（${parent.studentGrade}）",
                    refType = "APPLICATION",
                    refId = id,
                )
                addNotification(
                    userRole = "TEACHER",
                    userId = teacherId,
                    category = "ORDER",
                    title = "收到新的预约申请",
                    content = "学生：${parent.studentName}（${parent.studentGrade}） · 可进入私聊沟通时间",
                    refType = "APPLICATION",
                    refId = id,
                )
                entity.copy(id = id)
            }
        }
    }

    suspend fun updateApplicationStatus(applicationId: Int, status: String) {
        val application = applicationDao.getById(applicationId) ?: return
        applicationDao.updateStatus(applicationId, status)
        val title =
            when (status) {
                "ACCEPTED" -> "预约已被接受"
                "REJECTED" -> "预约被拒绝"
                "COMPLETED" -> "预约已完成"
                "CANCELLED" -> "预约已取消"
                else -> "预约状态更新"
            }
        addNotification(
            userRole = "PARENT",
            userId = application.parentId,
            category = "ORDER",
            title = title,
            content = "老师：${application.teacherPhone} · 学生：${application.studentName}（${application.studentGrade}） · 状态：${zhApplicationStatus(status)}",
            refType = "APPLICATION",
            refId = applicationId,
        )
    }

    suspend fun updateApplicationSchedule(applicationId: Int, scheduledAt: Long?): Result<Unit> {
        val application = applicationDao.getById(applicationId) ?: return Result.failure(IllegalStateException("申请不存在"))
        if (application.status != "ACCEPTED") return Result.failure(IllegalStateException("当前状态不可改期"))
        return runCatching {
            db.withTransaction {
                applicationDao.updateScheduledAt(applicationId, scheduledAt)
                val timeText =
                    if (scheduledAt == null) {
                        "未安排"
                    } else {
                        val fmt = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
                        fmt.format(java.util.Date(scheduledAt))
                    }
                addNotification(
                    userRole = "PARENT",
                    userId = application.parentId,
                    category = "ORDER",
                    title = "上课时间已更新",
                    content = "老师：${application.teacherPhone} · 时间：$timeText",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
                addNotification(
                    userRole = "TEACHER",
                    userId = application.teacherId,
                    category = "ORDER",
                    title = "上课时间已更新",
                    content = "家长：${application.parentPhone} · 时间：$timeText",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
            }
        }
    }

    suspend fun payForApplication(applicationId: Int): Result<PaymentEntity> {
        val application = applicationDao.getById(applicationId) ?: return Result.failure(IllegalStateException("申请不存在"))
        if (application.paymentStatus == "PAID") return Result.failure(IllegalStateException("该申请已支付"))
        val teacher = teacherDao.getById(application.teacherId) ?: return Result.failure(IllegalStateException("老师信息不存在"))

        return runCatching {
            db.withTransaction {
                applicationDao.updatePaymentStatus(applicationId, "PAID")
                val payment = PaymentEntity(
                    applicationId = application.id,
                    parentPhone = application.parentPhone,
                    teacherPhone = application.teacherPhone,
                    amount = teacher.pricePerHour,
                    status = "PAID",
                )
                val id = paymentDao.insert(payment).toInt()
                addNotification(
                    userRole = "PARENT",
                    userId = application.parentId,
                    category = "ORDER",
                    title = "预约支付成功",
                    content = "老师：${application.teacherPhone} · 金额：¥${String.format(java.util.Locale.CHINA, "%.2f", teacher.pricePerHour)}",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
                addNotification(
                    userRole = "TEACHER",
                    userId = application.teacherId,
                    category = "ORDER",
                    title = "收到预约支付",
                    content = "家长：${application.parentPhone} · 金额：¥${String.format(java.util.Locale.CHINA, "%.2f", teacher.pricePerHour)}",
                    refType = "APPLICATION",
                    refId = applicationId,
                )
                payment.copy(id = id)
            }
        }
    }

    fun getAllPayments(): Flow<List<PaymentEntity>> = paymentDao.getAll()

    fun getPaymentsByParentPhone(parentPhone: String): Flow<List<PaymentEntity>> = paymentDao.getByParentPhone(parentPhone)

    fun getPaymentsByTeacherPhone(teacherPhone: String): Flow<List<PaymentEntity>> = paymentDao.getByTeacherPhone(teacherPhone)

    fun getAllProductPayments(): Flow<List<ProductPaymentEntity>> = productPaymentDao.getAll()

    fun getProductPaymentsByParentPhone(parentPhone: String): Flow<List<ProductPaymentEntity>> = productPaymentDao.getByParentPhone(parentPhone)

    fun getAllReports(): Flow<List<ReportEntity>> = reportDao.getAll()

    fun getReportsByReporter(reporterRole: String, reporterId: Int): Flow<List<ReportEntity>> =
        reportDao.getByReporter(reporterRole, reporterId)

    suspend fun submitReport(
        reporterRole: String,
        reporterId: Int?,
        reporterPhone: String?,
        category: String,
        subject: String,
        content: String,
    ): Result<ReportEntity> {
        if (subject.isBlank()) return Result.failure(IllegalArgumentException("标题不能为空"))
        if (content.isBlank()) return Result.failure(IllegalArgumentException("内容不能为空"))
        val entity =
            ReportEntity(
                reporterRole = reporterRole,
                reporterId = reporterId,
                reporterPhone = reporterPhone?.trim()?.takeIf { it.isNotBlank() },
                category = category.trim(),
                subject = subject.trim(),
                content = content.trim(),
                status = "OPEN",
            )
        val id = reportDao.insert(entity).toInt()
        return Result.success(entity.copy(id = id))
    }

    suspend fun updateReportStatusAndNote(id: Int, status: String, adminNote: String?): Result<Unit> {
        return runCatching {
            val trimmedNote = adminNote?.trim()?.takeIf { it.isNotBlank() }
            reportDao.updateStatusAndNote(
                id = id,
                status = status,
                adminNote = trimmedNote,
                updatedAt = System.currentTimeMillis(),
            )

            val report = reportDao.getById(id) ?: return@runCatching
            val reporterId = report.reporterId ?: return@runCatching
            val role = report.reporterRole.trim().uppercase(java.util.Locale.ROOT)
            if (role != "PARENT" && role != "TEACHER") return@runCatching

            val title =
                when (status) {
                    "PROCESSING" -> "举报 / 反馈处理中"
                    "RESOLVED" -> "举报 / 反馈已处理"
                    "OPEN" -> "举报 / 反馈已重新打开"
                    else -> "举报 / 反馈状态更新"
                }
            val content =
                buildString {
                    append("主题：")
                    append(report.subject)
                    append(" · 状态：")
                    append(zhReportStatus(status))
                    if (!trimmedNote.isNullOrBlank()) {
                        append("\n处理备注：")
                        append(trimmedNote)
                    }
                }
            addNotification(
                userRole = role,
                userId = reporterId,
                category = "SYSTEM",
                title = title,
                content = content,
                refType = "REPORT",
                refId = id,
            )
        }
    }

    fun getNotifications(userRole: String, userId: Int): Flow<List<NotificationEntity>> =
        notificationDao.getByUser(userRole, userId)

    fun getUnreadNotificationCount(userRole: String, userId: Int): Flow<Int> =
        notificationDao.unreadCount(userRole, userId)

    suspend fun ensureDefaultNotifications(userRole: String, userId: Int) {
        if (notificationDao.countByUser(userRole, userId) > 0) return
        notificationDao.insert(
            NotificationEntity(
                userRole = userRole,
                userId = userId,
                category = "SYSTEM",
                title = "欢迎使用",
                content = "这里会显示系统消息、订单状态与审核状态变更。",
            ),
        )
    }

    suspend fun addNotification(
        userRole: String,
        userId: Int,
        category: String,
        title: String,
        content: String,
        refType: String? = null,
        refId: Int? = null,
    ): Result<NotificationEntity> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("标题不能为空"))
        if (content.isBlank()) return Result.failure(IllegalArgumentException("内容不能为空"))
        val entity =
            NotificationEntity(
                userRole = userRole,
                userId = userId,
                category = category.trim(),
                title = title.trim(),
                content = content.trim(),
                refType = refType,
                refId = refId,
            )
        val id = notificationDao.insert(entity).toInt()
        return Result.success(entity.copy(id = id))
    }

    suspend fun markNotificationRead(id: Int) {
        notificationDao.markRead(id = id, readAt = System.currentTimeMillis())
    }

    suspend fun markAllNotificationsRead(userRole: String, userId: Int) {
        notificationDao.markAllRead(
            userRole = userRole,
            userId = userId,
            readAt = System.currentTimeMillis(),
        )
    }

    suspend fun clearReadNotifications(userRole: String, userId: Int) {
        notificationDao.deleteReadByUser(userRole = userRole, userId = userId)
    }

    suspend fun clearAllNotifications(userRole: String, userId: Int) {
        notificationDao.deleteAllByUser(userRole = userRole, userId = userId)
    }

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAll()

    fun getProductsByType(type: String): Flow<List<ProductEntity>> = productDao.getByType(type)

    suspend fun getProductById(id: Int): ProductEntity? = productDao.getById(id)

    suspend fun addProduct(
        type: String,
        name: String,
        price: Double,
        provider: String,
        details: String,
        imageUri: String?,
    ): Result<ProductEntity> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("名称不能为空"))
        if (provider.isBlank()) return Result.failure(IllegalArgumentException("提供方不能为空"))
        if (details.isBlank()) return Result.failure(IllegalArgumentException("详情不能为空"))
        if (price < 0) return Result.failure(IllegalArgumentException("价格不能为负数"))
        val entity = ProductEntity(
            type = type,
            name = name.trim(),
            price = price,
            provider = provider.trim(),
            details = details.trim(),
            imageUri = imageUri?.trim()?.takeIf { it.isNotBlank() },
        )
        val id = productDao.insert(entity).toInt()
        return Result.success(entity.copy(id = id))
    }

    suspend fun deleteProduct(productId: Int) {
        productDao.deleteById(productId)
    }

    fun getCart(parentId: Int): Flow<List<CartItemWithProduct>> = cartDao.getCartWithProducts(parentId)

    suspend fun addToCart(parentId: Int, productId: Int, quantityDelta: Int = 1): Result<Unit> {
        if (quantityDelta <= 0) return Result.failure(IllegalArgumentException("数量不合法"))
        val product = productDao.getById(productId) ?: return Result.failure(IllegalStateException("商品不存在"))
        if (product.price < 0) return Result.failure(IllegalStateException("商品价格异常"))

        return runCatching {
            val updated = cartDao.incrementQuantity(parentId, productId, quantityDelta)
            if (updated == 0) {
                cartDao.insert(
                    CartItemEntity(
                        parentId = parentId,
                        productId = productId,
                        quantity = quantityDelta,
                    ),
                )
            }
        }
    }

    suspend fun updateCartQuantity(cartItemId: Int, quantity: Int): Result<Unit> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("数量必须大于 0"))
        return runCatching { cartDao.updateQuantity(cartItemId, quantity) }
    }

    suspend fun removeCartItem(cartItemId: Int): Result<Unit> = runCatching { cartDao.deleteById(cartItemId) }

    suspend fun clearCart(parentId: Int): Result<Unit> = runCatching { cartDao.clearByParentId(parentId) }

    fun getOrders(parentId: Int): Flow<List<ProductOrderEntity>> = productOrderDao.getByParentId(parentId)

    fun getAllProductOrders(): Flow<List<ProductOrderEntity>> = productOrderDao.getAll()

    suspend fun getOrderById(orderId: Int): ProductOrderEntity? = productOrderDao.getById(orderId)

    fun getOrderItems(orderId: Int): Flow<List<ProductOrderItemEntity>> = productOrderItemDao.getByOrderId(orderId)

    suspend fun checkoutCart(parentId: Int): Result<ProductOrderEntity> {
        val parent = parentDao.getById(parentId) ?: return Result.failure(IllegalStateException("家长不存在"))

        return runCatching {
            val snapshot = cartDao.getCartWithProducts(parentId).first()
            if (snapshot.isEmpty()) throw IllegalStateException("购物车为空")
            db.withTransaction {
                val total = snapshot.sumOf { it.price * it.quantity }
                val order = ProductOrderEntity(
                    parentId = parentId,
                    parentPhone = parent.phone,
                    totalAmount = total,
                    status = "CREATED",
                    paidAt = null,
                )
                val orderId = productOrderDao.insert(order).toInt()
                val orderItems =
                    snapshot.map {
                        ProductOrderItemEntity(
                            orderId = orderId,
                            productId = it.productId,
                            type = it.type,
                            name = it.name,
                            price = it.price,
                            provider = it.provider,
                            details = it.details,
                            imageUri = it.imageUri,
                            quantity = it.quantity,
                        )
                    }
                productOrderItemDao.insertAll(orderItems)
                cartDao.clearByParentId(parentId)
                addNotification(
                    userRole = "PARENT",
                    userId = parentId,
                    category = "ORDER",
                    title = "商品订单已创建",
                    content = "订单 #$orderId · 金额：¥${String.format(java.util.Locale.CHINA, "%.2f", total)}",
                    refType = "PRODUCT_ORDER",
                    refId = orderId,
                )
                order.copy(id = orderId)
            }
        }
    }

    suspend fun buyNow(parentId: Int, productId: Int, quantity: Int = 1): Result<ProductOrderEntity> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("数量不合法"))
        val parent = parentDao.getById(parentId) ?: return Result.failure(IllegalStateException("家长不存在"))
        val product = productDao.getById(productId) ?: return Result.failure(IllegalStateException("商品不存在"))

        return runCatching {
            db.withTransaction {
                val total = product.price * quantity
                val order = ProductOrderEntity(
                    parentId = parentId,
                    parentPhone = parent.phone,
                    totalAmount = total,
                    status = "CREATED",
                    paidAt = null,
                )
                val orderId = productOrderDao.insert(order).toInt()
                val item =
                    ProductOrderItemEntity(
                        orderId = orderId,
                        productId = product.id,
                        type = product.type,
                        name = product.name,
                        price = product.price,
                        provider = product.provider,
                        details = product.details,
                        imageUri = product.imageUri,
                        quantity = quantity,
                    )
                productOrderItemDao.insertAll(listOf(item))
                addNotification(
                    userRole = "PARENT",
                    userId = parentId,
                    category = "ORDER",
                    title = "商品订单已创建",
                    content = "订单 #$orderId · 金额：¥${String.format(java.util.Locale.CHINA, "%.2f", total)}",
                    refType = "PRODUCT_ORDER",
                    refId = orderId,
                )
                order.copy(id = orderId)
            }
        }
    }

    suspend fun payOrder(orderId: Int): Result<ProductPaymentEntity> {
        val order = productOrderDao.getById(orderId) ?: return Result.failure(IllegalStateException("订单不存在"))
        if (order.status == "PAID") return Result.failure(IllegalStateException("订单已支付"))
        if (order.status != "CREATED") return Result.failure(IllegalStateException("订单状态不可支付"))

        return runCatching {
            db.withTransaction {
                val paidAt = System.currentTimeMillis()
                productOrderDao.updateStatus(orderId, "PAID", paidAt)
                val payment = ProductPaymentEntity(
                    orderId = orderId,
                    parentPhone = order.parentPhone,
                    amount = order.totalAmount,
                    status = "PAID",
                    paidAt = paidAt,
                )
                val id = productPaymentDao.insert(payment).toInt()
                val parent = parentDao.getByPhone(order.parentPhone)
                parent?.let {
                    addNotification(
                        userRole = "PARENT",
                        userId = it.id,
                        category = "ORDER",
                        title = "商品订单支付成功",
                        content = "订单 #$orderId · 金额：¥${String.format(java.util.Locale.CHINA, "%.2f", order.totalAmount)}",
                        refType = "PRODUCT_ORDER",
                        refId = orderId,
                    )
                }
                payment.copy(id = id)
            }
        }
    }

    suspend fun cancelProductOrder(parentId: Int, orderId: Int): Result<Unit> {
        val order = productOrderDao.getById(orderId) ?: return Result.failure(IllegalStateException("订单不存在"))
        if (order.parentId != parentId) return Result.failure(IllegalStateException("无权限操作该订单"))
        if (order.status != "CREATED") return Result.failure(IllegalStateException("当前状态不可取消"))
        return runCatching {
            db.withTransaction {
                productOrderDao.updateStatus(orderId, "CANCELLED", paidAt = null)
                addNotification(
                    userRole = "PARENT",
                    userId = parentId,
                    category = "ORDER",
                    title = "订单已取消",
                    content = "订单 #$orderId · 状态：${zhProductOrderStatus("CANCELLED")}",
                    refType = "PRODUCT_ORDER",
                    refId = orderId,
                )
            }
        }
    }

    suspend fun refundProductOrder(orderId: Int): Result<Unit> {
        val order = productOrderDao.getById(orderId) ?: return Result.failure(IllegalStateException("订单不存在"))
        if (order.status != "PAID") return Result.failure(IllegalStateException("当前状态不可退款"))
        return runCatching {
            db.withTransaction {
                productOrderDao.updateStatus(orderId, "REFUNDED", paidAt = order.paidAt)
                productPaymentDao.updateStatusByOrderId(orderId = orderId, status = "REFUNDED")
                addNotification(
                    userRole = "PARENT",
                    userId = order.parentId,
                    category = "ORDER",
                    title = "订单已退款",
                    content = "订单 #$orderId · 状态：${zhProductOrderStatus("REFUNDED")}",
                    refType = "PRODUCT_ORDER",
                    refId = orderId,
                )
            }
        }
    }
}
