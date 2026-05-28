package com.example.teacher.core

const val ADMIN_PASSWORD = "admin123456"

fun zhUserRole(role: String?): String {
    return when (role?.trim()?.uppercase()) {
        "PARENT" -> "家长"
        "TEACHER" -> "老师"
        "ADMIN" -> "管理员"
        "GUEST" -> "游客"
        else -> role?.trim().orEmpty().ifBlank { "-" }
    }
}

fun zhTeacherReviewStatus(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "INCOMPLETE" -> "资料未完善"
        "PENDING" -> "待审核"
        "APPROVED" -> "已通过"
        "REJECTED" -> "未通过"
        "DISABLED" -> "已禁用"
        else -> status?.trim().orEmpty().ifBlank { "-" }
    }
}

fun zhApplicationStatus(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "PENDING" -> "待处理"
        "ACCEPTED" -> "已接受"
        "REJECTED" -> "已拒绝"
        "COMPLETED" -> "已完成"
        "CANCELLED" -> "已取消"
        else -> status?.trim().orEmpty().ifBlank { "-" }
    }
}

fun zhPaymentStatus(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "UNPAID" -> "未支付"
        "PAID" -> "已支付"
        "REFUNDED" -> "已退款"
        "CANCELLED" -> "已取消"
        else -> status?.trim().orEmpty().ifBlank { "-" }
    }
}

fun zhProductOrderStatus(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "CREATED" -> "待支付"
        "PAID" -> "已支付"
        "CANCELLED" -> "已取消"
        "REFUNDED" -> "已退款"
        else -> status?.trim().orEmpty().ifBlank { "-" }
    }
}

fun zhReportStatus(status: String?): String {
    return when (status?.trim()?.uppercase()) {
        "OPEN" -> "未处理"
        "PROCESSING" -> "处理中"
        "RESOLVED" -> "已解决"
        else -> status?.trim().orEmpty().ifBlank { "-" }
    }
}
