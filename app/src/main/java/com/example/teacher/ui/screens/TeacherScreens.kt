package com.example.teacher.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.example.teacher.MainActivity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacher.data.AppRepository
import com.example.teacher.data.BackendApi
import com.example.teacher.core.zhApplicationStatus
import com.example.teacher.core.zhPaymentStatus
import com.example.teacher.core.zhTeacherReviewStatus
import com.example.teacher.data.local.ApplicationEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.ui.components.AppCard
import com.example.teacher.ui.components.AppEmptyStateCard
import com.example.teacher.ui.components.AppErrorRetryCard
import com.example.teacher.ui.components.AppMessageCard
import com.example.teacher.ui.components.AppOutlinedButton
import com.example.teacher.ui.components.AppOutlinedField
import com.example.teacher.ui.components.AppPrimaryButton
import com.example.teacher.ui.components.AppPullToRefresh
import com.example.teacher.ui.components.AppScaffold
import com.example.teacher.ui.components.AppTonalButton
import com.example.teacher.ui.SessionState
import com.example.teacher.ui.viewmodel.NotificationViewModel
import com.example.teacher.ui.viewmodel.TeacherHomeViewModel
import com.example.teacher.ui.viewmodel.TeacherIncomeViewModel
import com.example.teacher.ui.viewmodel.TeacherViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.shape.CircleShape
import java.util.Calendar

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ScreenScaffold(
    title: String,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    AppScaffold(
        title = title,
        contentPadding = contentPadding,
        modifier = modifier,
        loading = loading,
        content = content,
    )
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppMessageCard(
        message = message,
        isError = isError,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

private val subjectOptions = listOf(
    "数学",
    "语文",
    "英语",
    "物理",
    "化学",
    "生物",
    "政治",
    "历史",
    "地理",
    "体育",
    "美术",
    "艺术",
)

private val gradeOptions = listOf(
    "一年级",
    "二年级",
    "三年级",
    "四年级",
    "五年级",
    "六年级",
    "初一",
    "初二",
    "初三",
    "高一",
    "高二",
    "高三",
)

private fun parseDelimitedList(value: String): List<String> {
    return value
        .replace("，", ",")
        .replace("、", ",")
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

private fun parseSubjectSelection(value: String): Pair<Set<String>, String> {
    val selected = mutableSetOf<String>()
    var artDetail = ""
    parseDelimitedList(value).forEach { item ->
        if (item.startsWith("艺术：")) {
            selected += "艺术"
            artDetail = item.removePrefix("艺术：").trim()
        } else if (item.startsWith("艺术:")) {
            selected += "艺术"
            artDetail = item.removePrefix("艺术:").trim()
        } else {
            selected += item
        }
    }
    return selected to artDetail
}

private fun subjectSelectionToString(selected: Set<String>, artDetail: String): String {
    val normalized = selected.toMutableSet()
    if (normalized.contains("艺术")) {
        normalized.remove("艺术")
        val detail = artDetail.trim()
        normalized.add(if (detail.isBlank()) "艺术" else "艺术：$detail")
    }
    return normalized.toList().sorted().joinToString("、")
}

private fun selectionToString(selected: Set<String>): String {
    return selected.toList().sorted().joinToString("、")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleSelectDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val summary = selected.ifBlank { "请选择" }
    Box(modifier = modifier.fillMaxWidth()) {
        AppOutlinedField(
            value = summary,
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("清空") },
                onClick = {
                    onClear()
                    expanded = false
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    trailingIcon = { Text(if (selected == option) "✓" else "") },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiSelectDropdown(
    label: String,
    options: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val summary = when {
        selected.isEmpty() -> "请选择"
        selected.size <= 2 -> selected.joinToString("、")
        else -> "${selected.size} 项"
    }
    Box(modifier = modifier.fillMaxWidth()) {
        AppOutlinedField(
            value = summary,
            onValueChange = {},
            label = label,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text("清空") },
                onClick = {
                    onClear()
                    expanded = false
                },
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onToggle(option) },
                    trailingIcon = { Text(if (selected.contains(option)) "✓" else "") },
                )
            }
        }
    }
}

@Composable
private fun SelectedChipsRow(
    selected: Set<String>,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        selected.take(4).forEach { value ->
            FilterChip(
                selected = true,
                onClick = { onRemove(value) },
                label = { Text(value) },
            )
        }
        if (selected.size > 4) {
            Text(text = "…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private suspend fun persistAvatarToInternalStorage(context: Context, sourceUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(sourceUri)
        val ext = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/jpeg" -> "jpg"
            else -> "jpg"
        }
        val dir = File(context.filesDir, "avatars").apply { mkdirs() }
        val target = File(dir, "teacher_${System.currentTimeMillis()}.$ext")
        val input = contentResolver.openInputStream(sourceUri) ?: return@withContext null
        input.use { source ->
            target.outputStream().use { out ->
                source.copyTo(out)
            }
        }
        target.absolutePath
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    AppEmptyStateCard(
        title = title,
        description = description,
        modifier = modifier,
    )
}

private fun greetingText(nowMillis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = nowMillis
    val hour = cal.get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "早上好"
        in 12..17 -> "下午好"
        else -> "晚上好"
    }
}

@Composable
private fun StatusBadge(
    text: String,
    tone: String,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) =
        when (tone.trim().uppercase()) {
            "SUCCESS" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            "WARN" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            "DANGER" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = bg,
        contentColor = fg,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.8f),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.weight(1f))
                badgeText?.let { StatusBadge(text = it, tone = "WARN") }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TeacherHomeScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    teacherViewModel: TeacherViewModel,
    teacherHomeViewModel: TeacherHomeViewModel,
    notificationViewModel: NotificationViewModel,
    onMyProfile: () -> Unit,
    onDemands: () -> Unit,
    onCourses: () -> Unit,
    onNotifications: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applications by teacherViewModel.applications.collectAsStateWithLifecycle()
    val homeState by teacherHomeViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val teacherId = sessionState.teacherId
    val notificationsGranted =
        if (Build.VERSION.SDK_INT < 33) {
            true
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }
    val requestNotificationsLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(sessionState.teacherId) {
        val id = sessionState.teacherId ?: return@LaunchedEffect
        teacherHomeViewModel.setTeacherSession(id, sessionState.accessToken)
        notificationViewModel.setUser("TEACHER", id)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000)
            teacherHomeViewModel.refresh()
        }
    }

    val status = homeState.teacher?.status
    val hint = when (status) {
        "INCOMPLETE" -> "请先完善资料后再使用完整功能"
        "PENDING" -> "您的资料正在等待管理员审核"
        "APPROVED" -> "您的资料已通过审核，家长可以看到您的信息"
        "REJECTED" -> "您的资料审核未通过，请修改后重新提交"
        "DISABLED" -> "您的账号已被禁用"
        else -> "未获取到审核状态"
    }
    val pendingCount = applications.count { it.status == "PENDING" }
    val acceptedCount = applications.count { it.status == "ACCEPTED" }
    val completedCount = applications.count { it.status == "COMPLETED" }
    val paidCount = applications.count { it.paymentStatus == "PAID" }
    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()

    LaunchedEffect(teacherId, homeState.demands, notificationsGranted) {
        val id = teacherId ?: return@LaunchedEffect
        if (!notificationsGranted) return@LaunchedEffect
        val maxDemandId = homeState.demands.maxOfOrNull { it.id } ?: return@LaunchedEffect
        val prefs = context.getSharedPreferences("teacher_demand_notify", Context.MODE_PRIVATE)
        val key = "last_notified_demand_$id"
        val last = prefs.getInt(key, 0)
        if (maxDemandId <= last) return@LaunchedEffect

        val count = homeState.demands.size
        val top = homeState.demands.first()
        val content = if (count == 1) "${top.subject}｜${top.studentGrade}｜${top.minPrice.toInt()}-${top.maxPrice.toInt()}元/小时" else "你有 $count 条未处理需求"
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                10000 + id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat.Builder(context, "demand_updates")
                .setSmallIcon(com.example.teacher.R.mipmap.ic_launcher)
                .setContentTitle("新的家长需求")
                .setContentText(content)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
        NotificationManagerCompat.from(context).notify(20000 + id, notification)
        prefs.edit().putInt(key, maxDemandId).apply()
    }

    ScreenScaffold(
        title = "首页",
        contentPadding = contentPadding,
        loading = homeState.loading,
        modifier = modifier,
    ) { padding ->
        AppPullToRefresh(
            isRefreshing = homeState.loading,
            onRefresh = { teacherHomeViewModel.refresh() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                homeState.error?.let { err ->
                    AppErrorRetryCard(
                        message = err,
                        onRetry = { teacherHomeViewModel.refresh() },
                    )
                }

                val nowMillis = System.currentTimeMillis()
                val greeting = greetingText(nowMillis)
                val phoneLast4 = homeState.teacher?.phone?.trim()?.takeLast(4)?.takeIf { it.length == 4 }
                val displayName = phoneLast4?.let { "${it}老师" } ?: "老师"
                val reviewText = zhTeacherReviewStatus(status)
                val reviewTone =
                    when (status?.trim()?.uppercase()) {
                        "APPROVED" -> "SUCCESS"
                        "PENDING" -> "WARN"
                        "REJECTED" -> "DANGER"
                        else -> "NEUTRAL"
                    }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "$greeting，$displayName", style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = reviewText, tone = reviewTone)
                                if (status?.trim()?.uppercase() == "APPROVED") {
                                    StatusBadge(text = "在线接单", tone = "SUCCESS")
                                } else if (status?.trim()?.uppercase() == "DISABLED") {
                                    StatusBadge(text = "不可用", tone = "DANGER")
                                }
                            }
                        }
                        IconButton(onClick = onNotifications) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                                    }
                                },
                            ) {
                                Icon(imageVector = Icons.Filled.Notifications, contentDescription = null)
                            }
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Filled.Verified, contentDescription = null)
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "平台认证教师", style = MaterialTheme.typography.titleSmall)
                                Text(text = hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        when (status?.trim()?.uppercase()) {
                            "INCOMPLETE", "REJECTED" -> {
                                AppPrimaryButton(onClick = onMyProfile, modifier = Modifier.fillMaxWidth()) { Text("去完善资料") }
                            }
                            "PENDING" -> {
                                AppTonalButton(onClick = onMyProfile, modifier = Modifier.fillMaxWidth()) { Text("查看资料") }
                            }
                            else -> {
                                AppOutlinedButton(onClick = onMyProfile, modifier = Modifier.fillMaxWidth()) { Text("编辑资料") }
                            }
                        }
                    }
                }

                val dayStartCal = Calendar.getInstance()
                dayStartCal.timeInMillis = System.currentTimeMillis()
                dayStartCal.set(Calendar.HOUR_OF_DAY, 0)
                dayStartCal.set(Calendar.MINUTE, 0)
                dayStartCal.set(Calendar.SECOND, 0)
                dayStartCal.set(Calendar.MILLISECOND, 0)
                val dayStart = dayStartCal.timeInMillis
                val dayEnd = dayStart + 24L * 60L * 60L * 1000L
                val todayCourses =
                    applications.count { it.status == "ACCEPTED" && (it.scheduledAt ?: -1L) in dayStart until dayEnd }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "今日概览", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            MetricCard(title = "今日课程", value = todayCourses.toString(), modifier = Modifier.weight(1f))
                            MetricCard(title = "待处理需求", value = homeState.demands.size.toString(), modifier = Modifier.weight(1f))
                            MetricCard(title = "已支付", value = paidCount.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= 33 && !notificationsGranted) {
                    AppOutlinedButton(
                        onClick = { requestNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("开启通知权限（有新需求时提醒）") }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "快捷功能", style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            QuickActionTile(
                                icon = Icons.Filled.ReceiptLong,
                                title = "新需求",
                                subtitle = "查看推荐需求并申请接单",
                                badgeText = homeState.demands.size.takeIf { it > 0 }?.toString(),
                                onClick = onDemands,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                icon = Icons.Filled.EventNote,
                                title = "课程安排",
                                subtitle = "管理申请与上课安排",
                                onClick = onCourses,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            QuickActionTile(
                                icon = Icons.Filled.Notifications,
                                title = "消息通知",
                                subtitle = if (unreadCount > 0) "有 $unreadCount 条未读" else "查看通知与提醒",
                                badgeText = unreadCount.takeIf { it > 0 }?.let { if (it > 99) "99+" else it.toString() },
                                onClick = onNotifications,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                icon = Icons.Filled.Person,
                                title = "我的资料",
                                subtitle = "完善信息，提升接单概率",
                                onClick = onMyProfile,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                val pendingTips = buildList {
                    if (status?.trim()?.uppercase() == "INCOMPLETE") add("资料未完善：完善后可接单")
                    if (status?.trim()?.uppercase() == "PENDING") add("资料待审核：通过后家长可看到你")
                    if (pendingCount > 0) add("有 $pendingCount 条家长申请待处理")
                    if (homeState.demands.isNotEmpty()) add("有 ${homeState.demands.size} 条新需求可接单")
                }
                if (pendingTips.isNotEmpty()) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "待处理事项", style = MaterialTheme.typography.titleSmall)
                            pendingTips.forEach { tip ->
                                Text(text = "• $tip", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherDemandsScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    teacherHomeViewModel: TeacherHomeViewModel,
    onChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val homeState by teacherHomeViewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var actionLoadingId by remember { mutableStateOf<Int?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val hiddenIds = remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(sessionState.teacherId) {
        val id = sessionState.teacherId ?: return@LaunchedEffect
        teacherHomeViewModel.setTeacherSession(id, sessionState.accessToken)
    }

    val filtered = remember(homeState.demands, query, hiddenIds.value) {
        val q = query.trim()
        homeState.demands
            .asSequence()
            .filter { it.id !in hiddenIds.value }
            .filter {
                if (q.isBlank()) true
                else (it.subject.contains(q, ignoreCase = true) || it.studentGrade.contains(q, ignoreCase = true))
            }
            .toList()
    }

    ScreenScaffold(
        title = "需求",
        contentPadding = contentPadding,
        loading = homeState.loading,
        modifier = modifier,
    ) { padding ->
        AppPullToRefresh(
            isRefreshing = homeState.loading,
            onRefresh = { teacherHomeViewModel.refresh() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                homeState.error?.let { err ->
                    AppErrorRetryCard(message = err, onRetry = { teacherHomeViewModel.refresh() })
                }
                errorMessage?.let { msg ->
                    AppMessageCard(
                        message = msg,
                        isError = true,
                        onDismiss = { errorMessage = null },
                    )
                }

                AppOutlinedField(
                    value = query,
                    onValueChange = { query = it },
                    label = "搜索科目/年级",
                    modifier = Modifier.fillMaxWidth(),
                )

                if (filtered.isEmpty()) {
                    EmptyStateCard(
                        title = "暂无可接需求",
                        description = "家长发布需求后会在这里展示，你可以申请接单并沟通。",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        items(filtered, key = { it.id }) { d ->
                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "${d.studentGrade} · ${d.subject}",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                    Text(
                                        text = "¥${d.minPrice.toInt()}-${d.maxPrice.toInt()}/小时",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "时间：${formatTimeShort(d.timeStartAtMillis)} - ${formatTimeShort(d.timeEndAtMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "发布者：家长#${d.parentId}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    d.teacherGenderPreference?.takeIf { it.isNotBlank() }?.let { pref ->
                                        Text(
                                            text = "性别偏好：$pref",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        AppOutlinedButton(
                                            onClick = { hiddenIds.value = hiddenIds.value + d.id },
                                            modifier = Modifier.weight(1f),
                                        ) { Text("不感兴趣") }
                                        AppPrimaryButton(
                                            onClick = {
                                                val token = sessionState.accessToken?.trim().orEmpty()
                                                if (token.isBlank()) {
                                                    errorMessage = "请先登录"
                                                    return@AppPrimaryButton
                                                }
                                                if (actionLoadingId != null) return@AppPrimaryButton
                                                actionLoadingId = d.id
                                                coroutineScope.launch {
                                                    val result =
                                                        runCatching {
                                                            BackendApi.claimDemand(token, d.id)
                                                        }
                                                    actionLoadingId = null
                                                    if (result.isFailure) {
                                                        errorMessage = result.exceptionOrNull()?.message ?: "申请接单失败"
                                                        return@launch
                                                    }
                                                    teacherHomeViewModel.refresh()
                                                    onChat(result.getOrThrow().application.id)
                                                }
                                            },
                                            enabled = actionLoadingId == null,
                                            modifier = Modifier.weight(1f),
                                        ) { Text(if (actionLoadingId == d.id) "处理中…" else "申请接单") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMeScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    appRepository: AppRepository,
    teacherViewModel: TeacherViewModel,
    onOpenDemands: () -> Unit,
    onPreviewPublicProfile: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenIncome: () -> Unit,
    onOpenApplications: () -> Unit,
    onOpenNotifications: () -> Unit,
    onReport: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applications by teacherViewModel.applications.collectAsStateWithLifecycle()
    var teacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var logoutDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(sessionState.teacherId) {
        val id = sessionState.teacherId ?: return@LaunchedEffect
        teacher = appRepository.getTeacherById(id)
    }

    ScreenScaffold(
        title = "我的",
        contentPadding = contentPadding,
        modifier = modifier,
    ) { padding ->
        val t = teacher
        val status = t?.status
        val statusTone =
            when (status?.trim()?.uppercase()) {
                "APPROVED" -> "SUCCESS"
                "PENDING" -> "WARN"
                "REJECTED" -> "DANGER"
                else -> "NEUTRAL"
            }
        val phoneLast4 = t?.phone?.trim()?.takeLast(4)?.takeIf { it.length == 4 }
        val displayName = phoneLast4?.let { "${it}老师" } ?: "老师"
        val expYears = t?.teachingExperience?.trim()?.let { exp ->
            Regex("(\\d{1,2})\\s*年").find(exp)?.groupValues?.getOrNull(1)?.toIntOrNull()
        }
        val subjectLabel = parseDelimitedList(t?.subjects.orEmpty()).firstOrNull().orEmpty().ifBlank { "家教老师" }
        val tags = buildList {
            parseDelimitedList(t?.subjects.orEmpty()).take(2).forEach { add(it) }
            parseDelimitedList(t?.grades.orEmpty()).take(1).forEach { add(it) }
            t?.gender?.trim()?.takeIf { it.isNotBlank() }?.let { add(it) }
            expYears?.let { add("${it}年经验") }
        }.distinct().take(4)

        val totalOrders = applications.size
        val acceptedOrders = applications.count { it.status == "ACCEPTED" }
        val completedOrders = applications.count { it.status == "COMPLETED" }
        val orderCount = acceptedOrders + completedOrders
        val ratingPct =
            when {
                totalOrders == 0 -> 100
                completedOrders == 0 -> 98
                else -> (96 + (completedOrders.coerceAtMost(10))).coerceAtMost(100)
            }
        val replySpeed = if (totalOrders == 0) "≤10分钟" else "≤5分钟"

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = displayName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = subjectLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = zhTeacherReviewStatus(status), tone = statusTone)
                                if (status?.trim()?.uppercase() == "APPROVED") {
                                    StatusBadge(text = "在线接单", tone = "SUCCESS")
                                }
                            }
                        }
                    }

                    if (tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            tags.forEach { tag ->
                                StatusBadge(text = tag, tone = "NEUTRAL")
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        MetricCard(title = "好评率", value = "${ratingPct}%", modifier = Modifier.weight(1f))
                        MetricCard(title = "接单数", value = orderCount.toString(), modifier = Modifier.weight(1f))
                        MetricCard(title = "回复速度", value = replySpeed, modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        AppOutlinedButton(
                            onClick = onPreviewPublicProfile,
                            modifier = Modifier.weight(1f),
                        ) { Text("预览主页") }
                        AppPrimaryButton(onClick = onEditProfile, modifier = Modifier.weight(1f)) { Text("编辑资料") }
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "常用功能", style = MaterialTheme.typography.titleSmall)
                    AppOutlinedButton(text = "需求中心", onClick = onOpenDemands)
                    AppOutlinedButton(text = "课程与接单", onClick = onOpenApplications)
                    AppOutlinedButton(text = "收入", onClick = onOpenIncome)
                    AppOutlinedButton(text = "消息与通知", onClick = onOpenNotifications)
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "帮助与支持", style = MaterialTheme.typography.titleSmall)
                    AppOutlinedButton(text = "帮助与反馈", onClick = onReport)
                }
            }

            Button(
                onClick = { logoutDialogVisible = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Text(text = "退出登录", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    if (logoutDialogVisible) {
        AlertDialog(
            onDismissRequest = { logoutDialogVisible = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出登录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        logoutDialogVisible = false
                        onLogout()
                    },
                ) { Text("退出") }
            },
            dismissButton = { TextButton(onClick = { logoutDialogVisible = false }) { Text("取消") } },
        )
    }
}

@Composable
fun TeacherPublicProfileScreen(
    contentPadding: PaddingValues,
    teacherId: Int,
    appRepository: AppRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var teacher by remember { mutableStateOf<TeacherEntity?>(null) }

    LaunchedEffect(teacherId) {
        teacher = appRepository.getTeacherById(teacherId)
    }

    ScreenScaffold(
        title = "主页预览",
        contentPadding = contentPadding,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val t = teacher
            if (t == null) {
                EmptyStateCard(title = "暂无资料", description = "请先完善老师资料后再预览。")
                AppOutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
                return@ScreenScaffold
            }

            val statusTone =
                when (t.status.trim().uppercase()) {
                    "APPROVED" -> "SUCCESS"
                    "PENDING" -> "WARN"
                    "REJECTED" -> "DANGER"
                    else -> "NEUTRAL"
                }

            val phoneLast4 = t.phone.trim().takeLast(4).takeIf { it.length == 4 }
            val displayName = phoneLast4?.let { "${it}老师" } ?: "老师"
            val subjects = parseDelimitedList(t.subjects)
            val grades = parseDelimitedList(t.grades)
            val firstSubject = subjects.firstOrNull().orEmpty().ifBlank { "家教老师" }
            val expYears = t.teachingExperience.trim().let { exp ->
                Regex("(\\d{1,2})\\s*年").find(exp)?.groupValues?.getOrNull(1)?.toIntOrNull()
            }
            val tags = buildList {
                subjects.take(2).forEach { add(it) }
                grades.take(1).forEach { add(it) }
                t.gender.trim().takeIf { it.isNotBlank() }?.let { add(it) }
                expYears?.let { add("${it}年经验") }
            }.distinct().take(6)

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = displayName, style = MaterialTheme.typography.titleMedium)
                            Text(text = firstSubject, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = zhTeacherReviewStatus(t.status), tone = statusTone)
                                if (t.status.trim().uppercase() == "APPROVED") StatusBadge(text = "在线接单", tone = "SUCCESS")
                            }
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "¥${t.pricePerHour.toInt()}/小时", style = MaterialTheme.typography.titleSmall)
                            Text(text = "老师ID：${t.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (tags.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            tags.take(4).forEach { tag -> StatusBadge(text = tag, tone = "NEUTRAL") }
                        }
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "教学信息", style = MaterialTheme.typography.titleSmall)
                    Text(text = "教学经历：${t.teachingExperience.ifBlank { "-" }}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "就职情况：${t.employmentStatus.ifBlank { "-" }}", style = MaterialTheme.typography.bodyMedium)
                    t.wechat?.trim()?.takeIf { it.isNotBlank() }?.let { wx ->
                        Text(text = "微信：$wx", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "可教科目", style = MaterialTheme.typography.titleSmall)
                    if (subjects.isEmpty()) {
                        Text(text = "-", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            subjects.take(4).forEach { StatusBadge(text = it, tone = "NEUTRAL") }
                        }
                        if (subjects.size > 4) {
                            Text(text = "等 ${subjects.size} 项", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "可教年级", style = MaterialTheme.typography.titleSmall)
                    if (grades.isEmpty()) {
                        Text(text = "-", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            grades.take(4).forEach { StatusBadge(text = it, tone = "NEUTRAL") }
                        }
                        if (grades.size > 4) {
                            Text(text = "等 ${grades.size} 项", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "授课地址", style = MaterialTheme.typography.titleSmall)
                    Text(text = t.address.ifBlank { "-" }, style = MaterialTheme.typography.bodyMedium)
                }
            }

            AppOutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
        }
    }
}

@Composable
fun TeacherNotificationsScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    notificationViewModel: NotificationViewModel,
    onOpenApplications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var categoryFilter by remember { mutableStateOf("ALL") }
    var readFilter by remember { mutableStateOf("ALL") }
    LaunchedEffect(sessionState.teacherId) {
        sessionState.teacherId?.let { notificationViewModel.setUser("TEACHER", it) }
    }
    val notifications by notificationViewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by notificationViewModel.unreadCount.collectAsStateWithLifecycle()
    val hasRead = remember(notifications) { notifications.any { it.isRead } }
    val visible = remember(notifications, categoryFilter, readFilter) {
        notifications
            .asSequence()
            .filter { n ->
                when (categoryFilter) {
                    "SYSTEM" -> n.category == "SYSTEM"
                    "ORDER" -> n.category == "ORDER"
                    "REVIEW" -> n.category == "REVIEW"
                    else -> true
                }
            }
            .filter { n ->
                when (readFilter) {
                    "UNREAD" -> !n.isRead
                    "READ" -> n.isRead
                    else -> true
                }
            }
            .toList()
    }

    ScreenScaffold(title = "消息与通知", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = categoryFilter == "ALL", onClick = { categoryFilter = "ALL" }, label = { Text("全部") })
                FilterChip(selected = categoryFilter == "SYSTEM", onClick = { categoryFilter = "SYSTEM" }, label = { Text("系统") })
                FilterChip(selected = categoryFilter == "ORDER", onClick = { categoryFilter = "ORDER" }, label = { Text("订单") })
                FilterChip(selected = categoryFilter == "REVIEW", onClick = { categoryFilter = "REVIEW" }, label = { Text("审核") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = readFilter == "ALL", onClick = { readFilter = "ALL" }, label = { Text("全部") })
                FilterChip(selected = readFilter == "UNREAD", onClick = { readFilter = "UNREAD" }, label = { Text("未读") })
                FilterChip(selected = readFilter == "READ", onClick = { readFilter = "READ" }, label = { Text("已读") })
            }
            AppOutlinedButton(
                onClick = { notificationViewModel.markAllRead() },
                enabled = unreadCount > 0,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (unreadCount > 0) "全部已读（$unreadCount）" else "全部已读") }
            AppOutlinedButton(
                onClick = { notificationViewModel.clearRead() },
                enabled = hasRead,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("清空已读") }
            AppOutlinedButton(
                onClick = { notificationViewModel.clearAll() },
                enabled = notifications.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("清空全部") }

            HorizontalDivider()

            if (visible.isEmpty()) {
                AppEmptyStateCard(
                    title = "暂无通知",
                    description = "当系统消息、订单状态或审核状态变化时，会在这里显示。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visible, key = { it.id }) { n ->
                        AppCard(
                            onClick = {
                                if (!n.isRead) notificationViewModel.markRead(n.id)
                                if (n.refType == "APPLICATION") onOpenApplications()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(text = n.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    if (!n.isRead) {
                                        Text(
                                            text = "未读",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                                Text(text = n.content, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = formatTimeShort(n.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherStudentsScreen(
    contentPadding: PaddingValues,
    teacherViewModel: TeacherViewModel,
    onOpenOrders: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class StudentItem(
        val key: String,
        val parentPhone: String,
        val studentName: String,
        val studentGrade: String,
        val activeCount: Int,
        val completedCount: Int,
        val paidCount: Int,
        val lastTime: Long,
        val lastStatus: String,
    )

    val apps by teacherViewModel.applications.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf("ACTIVE") }
    var keyword by remember { mutableStateOf("") }

    val students = remember(apps) {
        apps.groupBy { "${it.parentPhone}|${it.studentName}|${it.studentGrade}" }
            .map { (key, list) ->
                val active = list.count { it.status == "ACCEPTED" }
                val completed = list.count { it.status == "COMPLETED" }
                val paid = list.count { it.paymentStatus == "PAID" }
                val last = list.maxByOrNull { it.createdAt }!!
                StudentItem(
                    key = key,
                    parentPhone = last.parentPhone,
                    studentName = last.studentName,
                    studentGrade = last.studentGrade,
                    activeCount = active,
                    completedCount = completed,
                    paidCount = paid,
                    lastTime = last.createdAt,
                    lastStatus = last.status,
                )
            }
            .sortedByDescending { it.lastTime }
    }

    val visible = remember(students, filter, keyword) {
        val base =
            when (filter) {
                "ACTIVE" -> students.filter { it.activeCount > 0 }
                "COMPLETED" -> students.filter { it.completedCount > 0 }
                else -> students
            }
        val key = keyword.trim()
        if (key.isBlank()) base else {
            base.filter {
                it.studentName.contains(key) || it.studentGrade.contains(key) || it.parentPhone.contains(key)
            }
        }
    }

    ScreenScaffold(title = "学生管理", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = filter == "ACTIVE", onClick = { filter = "ACTIVE" }, label = { Text("进行中") })
                FilterChip(selected = filter == "COMPLETED", onClick = { filter = "COMPLETED" }, label = { Text("已完成") })
                FilterChip(selected = filter == "ALL", onClick = { filter = "ALL" }, label = { Text("全部") })
            }
            AppOutlinedField(
                value = keyword,
                onValueChange = { keyword = it },
                label = "搜索（学生/年级/手机号）",
                modifier = Modifier.fillMaxWidth(),
            )
            AppTonalButton(onClick = onOpenOrders, modifier = Modifier.fillMaxWidth()) { Text("查看接单记录") }

            HorizontalDivider()

            if (visible.isEmpty()) {
                AppEmptyStateCard(
                    title = "暂无学生",
                    description = "当你接受家长预约后，会在这里形成学生列表。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visible, key = { it.key }) { s ->
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = "${s.studentName}（${s.studentGrade}）", style = MaterialTheme.typography.titleSmall)
                                Text(text = "家长手机号：${s.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "进行中 ${s.activeCount} · 已完成 ${s.completedCount} · 已支付 ${s.paidCount}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "最近：${s.lastStatus} · ${formatTimeShort(s.lastTime)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherIncomeScreen(
    contentPadding: PaddingValues,
    teacherId: Int,
    teacherIncomeViewModel: TeacherIncomeViewModel,
    teacherViewModel: TeacherViewModel,
    modifier: Modifier = Modifier,
) {
    val incomeState by teacherIncomeViewModel.uiState.collectAsStateWithLifecycle()
    val apps by teacherViewModel.applications.collectAsStateWithLifecycle()
    val now = System.currentTimeMillis()
    val weekStart = remember(now) { startOfWeekMillis(now) }
    val monthStart = remember(now) { startOfMonthMillis(now) }

    val weekIncome = remember(incomeState.payments, weekStart) { incomeState.payments.filter { it.paidAt >= weekStart }.sumOf { it.amount } }
    val monthIncome = remember(incomeState.payments, monthStart) { incomeState.payments.filter { it.paidAt >= monthStart }.sumOf { it.amount } }
    val totalIncome = remember(incomeState.payments) { incomeState.payments.sumOf { it.amount } }

    val acceptedCount = remember(apps) { apps.count { it.status == "ACCEPTED" } }
    val completedCount = remember(apps) { apps.count { it.status == "COMPLETED" } }
    val paidCount = remember(apps) { apps.count { it.paymentStatus == "PAID" } }

    val scheduleEnd = remember(now) { now + 7L * 24 * 60 * 60 * 1000 }
    val upcoming = remember(apps, now, scheduleEnd) {
        apps.asSequence()
            .filter { it.status == "ACCEPTED" }
            .filter { it.scheduledAt != null && it.scheduledAt >= now && it.scheduledAt < scheduleEnd }
            .sortedBy { it.scheduledAt }
            .toList()
    }
    val pendingSchedule = remember(apps) { apps.filter { it.status == "ACCEPTED" && it.scheduledAt == null } }
    val context = LocalContext.current

    ScreenScaffold(title = "收入 & 课程", contentPadding = contentPadding, loading = incomeState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            incomeState.error?.let { err ->
                AppErrorRetryCard(
                    message = err,
                    onRetry = { teacherIncomeViewModel.refresh() },
                )
            }

            AppPullToRefresh(
                isRefreshing = incomeState.loading,
                onRefresh = { teacherIncomeViewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "收入概览", style = MaterialTheme.typography.titleSmall)
                            Text(text = "本周：${formatMoneyShort(weekIncome)}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "本月：${formatMoneyShort(monthIncome)}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "累计：${formatMoneyShort(totalIncome)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "课程概览", style = MaterialTheme.typography.titleSmall)
                            Text(text = "进行中：$acceptedCount", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "已完成：$completedCount", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "已支付：$paidCount", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Text(text = "未来7天课表", style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()

                    if (upcoming.isEmpty() && pendingSchedule.isEmpty()) {
                        AppEmptyStateCard(
                            title = "暂无课程安排",
                            description = "当你接受申请后，可以在接单记录或这里为课程安排上课时间。",
                        )
                    } else {
                        if (pendingSchedule.isNotEmpty()) {
                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "待安排：${pendingSchedule.size} 节", style = MaterialTheme.typography.bodyMedium)
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.heightIn(max = 240.dp),
                                    ) {
                                        items(pendingSchedule, key = { "pending_${it.id}" }) { a ->
                                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(text = "申请 #${a.id}", style = MaterialTheme.typography.titleSmall)
                                                    Text(text = "家长：${a.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                                                    Text(text = "学生：${a.studentName}（${a.studentGrade}）", style = MaterialTheme.typography.bodyMedium)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                                        AppOutlinedButton(
                                                            onClick = { teacherViewModel.cancel(a.id) },
                                                            enabled = !incomeState.loading,
                                                            modifier = Modifier.weight(1f),
                                                        ) { Text("取消") }
                                                        AppPrimaryButton(
                                                            onClick = {
                                                                showDateTimePicker(
                                                                    context = context,
                                                                    initialMillis = System.currentTimeMillis(),
                                                                    onPicked = { millis -> teacherViewModel.reschedule(a.id, millis) },
                                                                )
                                                            },
                                                            enabled = !incomeState.loading,
                                                            modifier = Modifier.weight(1f),
                                                        ) { Text("安排") }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (upcoming.isNotEmpty()) {
                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = "已安排：${upcoming.size} 节", style = MaterialTheme.typography.bodyMedium)
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.heightIn(max = 320.dp),
                                    ) {
                                        items(upcoming, key = { "upcoming_${it.id}" }) { a ->
                                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Text(text = formatTimeShort(a.scheduledAt ?: a.createdAt), style = MaterialTheme.typography.titleSmall)
                                                    Text(text = "学生：${a.studentName}（${a.studentGrade}）", style = MaterialTheme.typography.bodyMedium)
                                                    Text(text = "家长：${a.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                                        AppOutlinedButton(
                                                            onClick = { teacherViewModel.cancel(a.id) },
                                                            enabled = !incomeState.loading,
                                                            modifier = Modifier.weight(1f),
                                                        ) { Text("取消") }
                                                        AppPrimaryButton(
                                                            onClick = {
                                                                showDateTimePicker(
                                                                    context = context,
                                                                    initialMillis = a.scheduledAt ?: System.currentTimeMillis(),
                                                                    onPicked = { millis -> teacherViewModel.reschedule(a.id, millis) },
                                                                )
                                                            },
                                                            enabled = !incomeState.loading,
                                                            modifier = Modifier.weight(1f),
                                                        ) { Text("改期") }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(text = "支付明细", style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()

                    if (incomeState.payments.isEmpty()) {
                        AppEmptyStateCard(
                            title = "暂无支付记录",
                            description = "当家长完成预约支付后，会在这里生成支付明细。",
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(incomeState.payments, key = { it.id }) { p ->
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "¥${String.format(java.util.Locale.CHINA, "%.2f", p.amount)}", style = MaterialTheme.typography.titleSmall)
                                        Text(text = "家长：${p.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "申请 #${p.applicationId}", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = formatTimeShort(p.paidAt),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherApplicationListScreen(
    contentPadding: PaddingValues,
    teacherViewModel: TeacherViewModel,
    onChat: (Int) -> Unit,
    title: String = "收到的申请",
    modifier: Modifier = Modifier,
) {
    val applications by teacherViewModel.applications.collectAsStateWithLifecycle()
    val uiState by teacherViewModel.uiState.collectAsStateWithLifecycle()
    var newestFirst by remember { mutableStateOf(true) }
    val sortedApplications = remember(applications, newestFirst) {
        if (newestFirst) applications else applications.asReversed()
    }

    ScreenScaffold(title = title, contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "共 ${sortedApplications.size} 条申请", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = newestFirst,
                    onClick = { newestFirst = true },
                    label = { Text("最新优先") },
                )
                FilterChip(
                    selected = !newestFirst,
                    onClick = { newestFirst = false },
                    label = { Text("最早优先") },
                )
            }
            HorizontalDivider()
            if (sortedApplications.isEmpty()) {
                EmptyStateCard(
                    title = "暂无申请",
                    description = "家长提交申请后会在这里显示，你可以进行接受/拒绝/完成操作。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sortedApplications, key = { it.id }) { app ->
                        TeacherApplicationCard(
                            app = app,
                            onAccept = { teacherViewModel.accept(app.id) },
                            onReject = { teacherViewModel.reject(app.id) },
                            onComplete = { teacherViewModel.complete(app.id) },
                            onCancel = { teacherViewModel.cancel(app.id) },
                            onReschedule = { millis -> teacherViewModel.reschedule(app.id, millis) },
                            onChat = { onChat(app.id) },
                        )
                    }
                }
            }
            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { teacherViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
private fun TeacherApplicationCard(
    app: ApplicationEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onReschedule: (Long?) -> Unit,
    onChat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "申请编号：${app.id}")
            Text(text = "家长：${maskPhone(app.parentPhone)}")
            Text(text = "学生：${app.studentName}（${app.studentGrade}）")
            Text(text = "申请状态：${zhApplicationStatus(app.status)}")
            Text(text = "支付状态：${zhPaymentStatus(app.paymentStatus)}")
            Text(text = "上课时间：${formatScheduleTime(app.scheduledAt)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppOutlinedButton(onClick = onChat) { Text("私聊") }
                AppPrimaryButton(onClick = onAccept, enabled = app.status == "PENDING") { Text("接受") }
                AppOutlinedButton(onClick = onReject, enabled = app.status == "PENDING") { Text("拒绝") }
                AppTonalButton(onClick = onComplete, enabled = app.status == "ACCEPTED") { Text("完成") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AppOutlinedButton(onClick = onCancel, enabled = app.status == "ACCEPTED", modifier = Modifier.weight(1f)) { Text("取消") }
                AppPrimaryButton(
                    onClick = {
                        showDateTimePicker(
                            context = context,
                            initialMillis = app.scheduledAt ?: System.currentTimeMillis(),
                            onPicked = onReschedule,
                        )
                    },
                    enabled = app.status == "ACCEPTED",
                    modifier = Modifier.weight(1f),
                ) { Text(if (app.scheduledAt == null) "安排" else "改期") }
            }
        }
    }
}

@Composable
fun TeacherProfileScreen(
    contentPadding: PaddingValues,
    appRepository: AppRepository,
    teacherId: Int,
    teacherViewModel: TeacherViewModel,
    title: String = "我的资料",
    onProfileCompleted: (() -> Unit)? = null,
    onReport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by teacherViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var teacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var submitted by remember { mutableStateOf(false) }

    var password by remember { mutableStateOf("") }
    var avatarPath by remember { mutableStateOf<String?>(null) }
    var gender by remember { mutableStateOf("") }
    var wechat by remember { mutableStateOf("") }
    var teachingExperience by remember { mutableStateOf("") }
    var employmentStatus by remember { mutableStateOf("") }
    var teachSubjects by remember { mutableStateOf(setOf<String>()) }
    var teachArtDetail by remember { mutableStateOf("") }
    var teachGrades by remember { mutableStateOf(setOf<String>()) }
    var pricePerHourText by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var poiName by remember { mutableStateOf<String?>(null) }
    var poiAddress by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        val entity = appRepository.getTeacherById(teacherId)
        teacher = entity
        password = ""
        avatarPath = entity?.avatarPath?.trim()?.takeIf { it.isNotBlank() }
        gender = entity?.gender.orEmpty()
        wechat = entity?.wechat.orEmpty()
        teachingExperience = entity?.teachingExperience.orEmpty()
        employmentStatus = entity?.employmentStatus.orEmpty()
        parseSubjectSelection(entity?.subjects.orEmpty()).let { parsed ->
            teachSubjects = parsed.first
            teachArtDetail = parsed.second
        }
        teachGrades = parseDelimitedList(entity?.grades.orEmpty()).toSet()
        pricePerHourText = entity?.pricePerHour?.toString().orEmpty()
        latitude = entity?.latitude
        longitude = entity?.longitude
        poiName = entity?.poiName
        poiAddress = entity?.poiAddress ?: entity?.address.orEmpty()
        addressDetail = entity?.addressDetail.orEmpty()
    }

    val pickAvatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                avatarPath = persistAvatarToInternalStorage(context, uri)
            }
        }
    }
    val avatarBitmap = remember(avatarPath) {
        val path = avatarPath ?: return@remember null
        runCatching { BitmapFactory.decodeFile(path) }.getOrNull()?.asImageBitmap()
    }
    val avatarOk = !avatarPath.isNullOrBlank()
    val subjectsOk = teachSubjects.isNotEmpty() && (!teachSubjects.contains("艺术") || teachArtDetail.isNotBlank())
    val gradesOk = teachGrades.isNotEmpty()

    ScreenScaffold(title = title, contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val t = teacher
            if (t == null) {
                Text(text = "未找到老师信息", style = MaterialTheme.typography.bodyMedium)
                return@ScreenScaffold
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "账号信息", style = MaterialTheme.typography.titleSmall)
                    Text(text = "老师ID：${t.id}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "手机号：${t.phone}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "当前状态：${zhTeacherReviewStatus(t.status)}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (avatarOk) "已选择头像" else "未选择头像",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppOutlinedButton(onClick = { pickAvatarLauncher.launch("image/*") }) {
                                Text("选择头像")
                            }
                        }
                    }
                    if (submitted && !avatarOk) {
                        Text(text = "请上传本人头像", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    SingleSelectDropdown(
                        label = "性别",
                        options = listOf("男", "女"),
                        selected = gender,
                        onSelected = { gender = it },
                        onClear = { gender = "" },
                    )
                    if (submitted && gender.isBlank()) {
                        Text(text = "请选择性别", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    AppOutlinedField(
                        value = password,
                        onValueChange = { password = it },
                        label = "修改密码（可选）",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppOutlinedField(
                        value = wechat,
                        onValueChange = { wechat = it },
                        label = "微信号（可选）",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "教学资料", style = MaterialTheme.typography.titleSmall)
                    AppOutlinedField(
                        value = teachingExperience,
                        onValueChange = { teachingExperience = it },
                        label = "教学履历",
                        isError = submitted && teachingExperience.isBlank(),
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppOutlinedField(
                        value = employmentStatus,
                        onValueChange = { employmentStatus = it },
                        label = "就职情况",
                        isError = submitted && employmentStatus.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    MultiSelectDropdown(
                        label = "可教科目（可多选）",
                        options = subjectOptions,
                        selected = teachSubjects,
                        onToggle = { option ->
                            val next = if (teachSubjects.contains(option)) teachSubjects - option else teachSubjects + option
                            teachSubjects = next
                            if (!next.contains("艺术")) teachArtDetail = ""
                        },
                        onClear = {
                            teachSubjects = emptySet()
                            teachArtDetail = ""
                        },
                    )
                    SelectedChipsRow(
                        selected = teachSubjects,
                        onRemove = { teachSubjects = teachSubjects - it },
                    )
                    if (teachSubjects.contains("艺术")) {
                        AppOutlinedField(
                            value = teachArtDetail,
                            onValueChange = { teachArtDetail = it },
                            label = "艺术具体内容",
                            isError = submitted && teachArtDetail.isBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    if (submitted && !subjectsOk) {
                        Text(text = "请选择可教科目", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    MultiSelectDropdown(
                        label = "可教年级（可多选）",
                        options = gradeOptions,
                        selected = teachGrades,
                        onToggle = { option -> teachGrades = if (teachGrades.contains(option)) teachGrades - option else teachGrades + option },
                        onClear = { teachGrades = emptySet() },
                    )
                    SelectedChipsRow(
                        selected = teachGrades,
                        onRemove = { teachGrades = teachGrades - it },
                    )
                    if (submitted && !gradesOk) {
                        Text(text = "请选择可教年级", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppOutlinedField(
                            value = pricePerHourText,
                            onValueChange = { pricePerHourText = it },
                            label = "课时价格",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            isError = submitted && (pricePerHourText.trim().toDoubleOrNull() ?: 0.0) <= 0.0,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AppOutlinedField(
                            value = if (poiAddress.isBlank()) "请在地图选择位置" else buildAddressText(poiName, poiAddress, ""),
                            onValueChange = {},
                            label = "定位地址",
                            readOnly = true,
                            isError = submitted && poiAddress.isBlank(),
                            modifier = Modifier.weight(1f),
                        )
                        AppTonalButton(
                            onClick = { showLocationPicker = true },
                            modifier = Modifier.align(Alignment.CenterVertically),
                        ) { Text("地图选择") }
                    }
                    AppOutlinedField(
                        value = addressDetail,
                        onValueChange = { addressDetail = it },
                        label = "详细地址（楼栋/单元/门牌号）",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider()
                    AppPrimaryButton(
                        onClick = {
                            focusManager.clearFocus()
                            submitted = true
                            val price = pricePerHourText.trim().toDoubleOrNull() ?: 0.0
                            if (
                                !avatarOk ||
                                gender.trim().isBlank() ||
                                teachingExperience.trim().isBlank() ||
                                employmentStatus.trim().isBlank() ||
                                !subjectsOk ||
                                !gradesOk ||
                                buildAddressText(poiName, poiAddress, addressDetail).trim().isBlank() ||
                                price <= 0.0
                            ) {
                                teacherViewModel.setMessage("请完善必填信息")
                                return@AppPrimaryButton
                            }
                            val nextStatus = when (t.status) {
                                "REJECTED", "INCOMPLETE" -> "PENDING"
                                else -> t.status
                            }
                            val updated = t.copy(
                                password = password.takeIf { it.isNotBlank() } ?: t.password,
                                avatarPath = avatarPath.orEmpty(),
                                gender = gender.trim(),
                                wechat = wechat.trim().takeIf { it.isNotBlank() },
                                teachingExperience = teachingExperience.trim(),
                                employmentStatus = employmentStatus.trim(),
                                subjects = subjectSelectionToString(teachSubjects, teachArtDetail),
                                grades = selectionToString(teachGrades),
                                pricePerHour = price,
                                address = buildAddressText(poiName, poiAddress, addressDetail),
                                latitude = latitude,
                                longitude = longitude,
                                poiName = poiName,
                                poiAddress = poiAddress.trim().takeIf { it.isNotBlank() },
                                addressDetail = addressDetail.trim(),
                                status = nextStatus,
                            )
                            coroutineScope.launch {
                                appRepository.updateTeacher(updated)
                                teacher = updated
                                password = ""
                                teacherViewModel.setMessage(
                                    if (t.status == "REJECTED") "已保存，状态已重置为待审核" else "保存成功",
                                )
                                if (title == "完善资料") {
                                    onProfileCompleted?.invoke()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("保存修改") }
                }
            }

            AppOutlinedButton(onClick = onReport, modifier = Modifier.fillMaxWidth()) { Text("举报 / 反馈") }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { teacherViewModel.clearMessage() },
                )
            }
        }
    }

    if (showLocationPicker) {
        LocationPickerDialog(
            initialLatitude = latitude,
            initialLongitude = longitude,
            onDismiss = { showLocationPicker = false },
            onConfirm = { picked ->
                latitude = picked.latitude
                longitude = picked.longitude
                poiName = picked.poiName
                poiAddress = picked.poiAddress
                showLocationPicker = false
            },
        )
    }
}

private fun formatMoneyShort(amount: Double): String = "¥" + String.format(java.util.Locale.CHINA, "%.2f", amount)

private fun formatTimeShort(time: Long): String {
    val fmt = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
    return fmt.format(java.util.Date(time))
}

private fun maskPhone(phone: String): String {
    val raw = phone.trim()
    if (raw.length == 11 && raw.all { it.isDigit() }) {
        return raw.take(3) + "****" + raw.takeLast(4)
    }
    return raw.ifBlank { "-" }
}

private fun formatScheduleTime(scheduledAt: Long?): String {
    return scheduledAt?.let { formatTimeShort(it) } ?: "未安排"
}

private fun showDateTimePicker(
    context: Context,
    initialMillis: Long,
    onPicked: (Long?) -> Unit,
) {
    val cal = Calendar.getInstance(java.util.Locale.CHINA)
    cal.timeInMillis = initialMillis
    DatePickerDialog(
        context,
        { _, year, month, day ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    onPicked(cal.timeInMillis)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true,
            ).show()
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH),
    ).show()
}

private fun startOfWeekMillis(now: Long): Long {
    val cal = java.util.Calendar.getInstance(java.util.Locale.CHINA)
    cal.timeInMillis = now
    cal.firstDayOfWeek = java.util.Calendar.MONDAY
    cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun startOfMonthMillis(now: Long): Long {
    val cal = java.util.Calendar.getInstance(java.util.Locale.CHINA)
    cal.timeInMillis = now
    cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
    cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    cal.set(java.util.Calendar.MINUTE, 0)
    cal.set(java.util.Calendar.SECOND, 0)
    cal.set(java.util.Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
