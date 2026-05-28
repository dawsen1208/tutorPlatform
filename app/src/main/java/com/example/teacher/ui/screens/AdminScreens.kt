package com.example.teacher.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacher.core.zhApplicationStatus
import com.example.teacher.core.zhPaymentStatus
import com.example.teacher.core.zhProductOrderStatus
import com.example.teacher.core.zhReportStatus
import com.example.teacher.core.zhTeacherReviewStatus
import com.example.teacher.core.zhUserRole
import com.example.teacher.data.local.ApplicationEntity
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.PaymentEntity
import com.example.teacher.data.local.ProductEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.ui.components.AppCard
import com.example.teacher.ui.components.AppEmptyStateCard
import com.example.teacher.ui.components.AppMessageCard
import com.example.teacher.ui.components.AppOutlinedButton
import com.example.teacher.ui.components.AppOutlinedField
import com.example.teacher.ui.components.AppPrimaryButton
import com.example.teacher.ui.components.AppScaffold
import com.example.teacher.ui.components.AppTextButton
import com.example.teacher.ui.components.AppTonalButton
import com.example.teacher.ui.viewmodel.AdminViewModel
import com.example.teacher.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

@Composable
fun AdminDashboardScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    onParents: () -> Unit,
    onTeachers: () -> Unit,
    onApplications: () -> Unit,
    onPayments: () -> Unit,
    onProducts: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val parents by adminViewModel.parents.collectAsStateWithLifecycle()
    val teachers by adminViewModel.teachers.collectAsStateWithLifecycle()
    val pendingTeachers by adminViewModel.pendingTeachers.collectAsStateWithLifecycle()
    val applications by adminViewModel.applications.collectAsStateWithLifecycle()
    val payments by adminViewModel.payments.collectAsStateWithLifecycle()

    AppScaffold(title = "管理员后台", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppEmptyStateCard(
                title = "数据概览",
                description =
                    "家长：${parents.size}  ·  老师：${teachers.size}  ·  待审核：${pendingTeachers.size}\n申请：${applications.size}  ·  已支付：${payments.count { it.status == "PAID" }}",
            )
            AppPrimaryButton(onClick = onParents, modifier = Modifier.fillMaxWidth()) { Text("家长列表") }
            AppPrimaryButton(onClick = onTeachers, modifier = Modifier.fillMaxWidth()) { Text("老师列表 / 审核") }
            AppPrimaryButton(onClick = onApplications, modifier = Modifier.fillMaxWidth()) { Text("申请记录") }
            AppPrimaryButton(onClick = onPayments, modifier = Modifier.fillMaxWidth()) { Text("支付记录") }
            AppPrimaryButton(onClick = onProducts, modifier = Modifier.fillMaxWidth()) { Text("商品管理") }
            AppPrimaryButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("退出登录") }
        }
    }
}

@Composable
fun AdminUsersScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val parents by adminViewModel.parents.collectAsStateWithLifecycle()
    val teachers by adminViewModel.teachers.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("PARENTS") }

    AppScaffold(title = "用户管理", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = tab == "PARENTS", onClick = { tab = "PARENTS" }, label = { Text("家长") })
                FilterChip(selected = tab == "TEACHERS", onClick = { tab = "TEACHERS" }, label = { Text("老师") })
            }
            HorizontalDivider()
            if (tab == "PARENTS") {
                if (parents.isEmpty()) {
                    EmptyStateCard(title = "暂无家长用户", description = "当有家长注册后会在这里显示。")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(parents, key = { it.id }) { parent ->
                            ParentRow(parent = parent, onDelete = { adminViewModel.deleteParent(parent.id) })
                        }
                    }
                }
            } else {
                if (teachers.isEmpty()) {
                    EmptyStateCard(title = "暂无老师用户", description = "当有老师注册后会在这里显示。")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(teachers, key = { it.id }) { teacher ->
                            TeacherRow(
                                teacher = teacher,
                                onApprove = { adminViewModel.approveTeacher(teacher.id) },
                                onReject = { adminViewModel.rejectTeacher(teacher.id) },
                                onDisable = { adminViewModel.disableTeacher(teacher.id) },
                                onDelete = { adminViewModel.deleteTeacher(teacher.id) },
                            )
                        }
                    }
                }
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun AdminTeacherReviewScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val pending by adminViewModel.pendingTeachers.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()

    AppScaffold(title = "教师审核", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "待审核：${pending.size}", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            if (pending.isEmpty()) {
                EmptyStateCard(title = "暂无待审核老师", description = "当有老师注册并提交资料后，会出现在这里。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pending, key = { it.id }) { teacher ->
                        TeacherRow(
                            teacher = teacher,
                            onApprove = { adminViewModel.approveTeacher(teacher.id) },
                            onReject = { adminViewModel.rejectTeacher(teacher.id) },
                            onDisable = { adminViewModel.disableTeacher(teacher.id) },
                            onDelete = { adminViewModel.deleteTeacher(teacher.id) },
                        )
                    }
                }
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun AdminOrdersScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val applications by adminViewModel.applications.collectAsStateWithLifecycle()
    val payments by adminViewModel.payments.collectAsStateWithLifecycle()
    val productOrders by adminViewModel.productOrders.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("APPLICATIONS") }
    val sortedProductOrders = remember(productOrders) { productOrders.sortedByDescending { it.createdAt } }

    AppScaffold(title = "订单管理", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = tab == "APPLICATIONS", onClick = { tab = "APPLICATIONS" }, label = { Text("预约") })
                FilterChip(selected = tab == "PAYMENTS", onClick = { tab = "PAYMENTS" }, label = { Text("支付") })
                FilterChip(selected = tab == "PRODUCT_ORDERS", onClick = { tab = "PRODUCT_ORDERS" }, label = { Text("商品订单") })
            }
            HorizontalDivider()
            when (tab) {
                "APPLICATIONS" -> {
                    if (applications.isEmpty()) {
                        EmptyStateCard(title = "暂无预约记录", description = "家长提交预约后会在这里显示。")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(applications, key = { it.id }) { app ->
                                AdminApplicationRow(
                                    app = app,
                                    onSetStatus = { status -> adminViewModel.setApplicationStatus(app.id, status) },
                                )
                            }
                        }
                    }
                }

                "PAYMENTS" -> {
                    if (payments.isEmpty()) {
                        EmptyStateCard(title = "暂无支付记录", description = "家长完成支付后会在这里生成支付记录。")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(payments, key = { it.id }) { payment ->
                                PaymentRow(payment = payment)
                            }
                        }
                    }
                }

                else -> {
                    if (sortedProductOrders.isEmpty()) {
                        EmptyStateCard(title = "暂无商品订单", description = "家长下单后会在这里显示商品订单。")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(sortedProductOrders, key = { it.id }) { o ->
                                AppCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "订单 #${o.id}", style = MaterialTheme.typography.titleSmall)
                                        Text(text = "家长：${o.parentPhone}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "金额：${o.totalAmount}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "状态：${zhProductOrderStatus(o.status)}", style = MaterialTheme.typography.bodyMedium)
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            AppOutlinedButton(
                                                onClick = { adminViewModel.cancelProductOrder(o.id) },
                                                enabled = !uiState.loading && o.status == "CREATED",
                                                modifier = Modifier.weight(1f),
                                            ) { Text("取消") }
                                            AppPrimaryButton(
                                                onClick = { adminViewModel.refundProductOrder(o.id) },
                                                enabled = !uiState.loading && o.status == "PAID",
                                                modifier = Modifier.weight(1f),
                                            ) { Text("退款") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun AdminReportsScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val reports by adminViewModel.reports.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()
    var statusFilter by remember { mutableStateOf("OPEN") }
    var categoryFilter by remember { mutableStateOf("ALL") }
    var keyword by remember { mutableStateOf("") }

    var dialogReportId by remember { mutableStateOf<Int?>(null) }
    var dialogStatus by remember { mutableStateOf("OPEN") }
    var dialogNote by remember { mutableStateOf("") }

    val visible = remember(reports, statusFilter, categoryFilter, keyword) {
        val key = keyword.trim()
        reports
            .filter { r ->
                (statusFilter == "ALL" || r.status == statusFilter) &&
                    (categoryFilter == "ALL" || r.category == categoryFilter) &&
                    (key.isBlank() || r.subject.contains(key) || r.content.contains(key) || (r.reporterPhone?.contains(key) == true))
            }
    }

    if (dialogReportId != null) {
        AlertDialog(
            onDismissRequest = { dialogReportId = null },
            title = { Text("处理举报/反馈") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = dialogStatus == "OPEN", onClick = { dialogStatus = "OPEN" }, label = { Text("未处理") })
                        FilterChip(selected = dialogStatus == "PROCESSING", onClick = { dialogStatus = "PROCESSING" }, label = { Text("处理中") })
                        FilterChip(selected = dialogStatus == "RESOLVED", onClick = { dialogStatus = "RESOLVED" }, label = { Text("已解决") })
                    }
                    AppOutlinedField(
                        value = dialogNote,
                        onValueChange = { dialogNote = it },
                        label = "处理备注",
                        singleLine = false,
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                AppPrimaryButton(
                    onClick = {
                        val id = dialogReportId ?: return@AppPrimaryButton
                        adminViewModel.updateReportStatus(id, dialogStatus, dialogNote)
                        dialogReportId = null
                    },
                ) { Text("保存") }
            },
            dismissButton = {
                AppTextButton(onClick = { dialogReportId = null }) { Text("取消") }
            },
        )
    }

    AppScaffold(title = "举报反馈", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = statusFilter == "OPEN", onClick = { statusFilter = "OPEN" }, label = { Text("未处理") })
                FilterChip(selected = statusFilter == "PROCESSING", onClick = { statusFilter = "PROCESSING" }, label = { Text("处理中") })
                FilterChip(selected = statusFilter == "RESOLVED", onClick = { statusFilter = "RESOLVED" }, label = { Text("已解决") })
                FilterChip(selected = statusFilter == "ALL", onClick = { statusFilter = "ALL" }, label = { Text("全部") })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = categoryFilter == "ALL", onClick = { categoryFilter = "ALL" }, label = { Text("全部类型") })
                FilterChip(selected = categoryFilter == "FEEDBACK", onClick = { categoryFilter = "FEEDBACK" }, label = { Text("反馈") })
                FilterChip(selected = categoryFilter == "REPORT", onClick = { categoryFilter = "REPORT" }, label = { Text("举报") })
            }
            AppOutlinedField(
                value = keyword,
                onValueChange = { keyword = it },
                label = "搜索（标题/内容/手机号）",
                modifier = Modifier.fillMaxWidth(),
            )
            HorizontalDivider()

            if (visible.isEmpty()) {
                AppEmptyStateCard(
                    title = "暂无数据",
                    description = "当用户提交举报/反馈后，会在这里显示并可处理。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visible, key = { it.id }) { r ->
                        AppCard(modifier = Modifier.fillMaxWidth(), onClick = {
                            dialogReportId = r.id
                            dialogStatus = r.status
                            dialogNote = r.adminNote.orEmpty()
                        }) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = r.subject, style = MaterialTheme.typography.titleSmall)
                                Text(text = r.content, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "类型：${if (r.category == "REPORT") "举报" else "反馈"} · 状态：${zhReportStatus(r.status)} · ${formatTimeShort(r.createdAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = "来源：${zhUserRole(r.reporterRole)}${r.reporterPhone?.let { " · $it" }.orEmpty()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                r.adminNote?.takeIf { it.isNotBlank() }?.let { note ->
                                    Text(text = "备注：$note", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    AppTonalButton(onClick = {
                                        dialogReportId = r.id
                                        dialogStatus = "PROCESSING"
                                        dialogNote = r.adminNote.orEmpty()
                                    }) { Text("处理") }
                                    AppPrimaryButton(onClick = {
                                        dialogReportId = r.id
                                        dialogStatus = "RESOLVED"
                                        dialogNote = r.adminNote.orEmpty()
                                    }) { Text("关闭") }
                                }
                            }
                        }
                    }
                }
            }

            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun AdminStatsScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kpis by adminViewModel.kpis.collectAsStateWithLifecycle()

    AppScaffold(title = "系统统计", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "本周订单数", style = MaterialTheme.typography.titleSmall)
                    Text(text = kpis.weekOrderCount.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "本周成交额", style = MaterialTheme.typography.titleSmall)
                    Text(text = formatMoneyShort(kpis.weekRevenue), style = MaterialTheme.typography.titleLarge)
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "活跃老师数", style = MaterialTheme.typography.titleSmall)
                    Text(text = kpis.activeTeacherCount.toString(), style = MaterialTheme.typography.titleLarge)
                }
            }
            AppPrimaryButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) { Text("退出登录") }
        }
    }
}

private const val PRODUCT_TYPE_COURSE = "COURSE"
private const val PRODUCT_TYPE_MATERIAL = "MATERIAL"

private fun formatTimeShort(time: Long): String {
    val fmt = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
    return fmt.format(java.util.Date(time))
}

private fun formatMoneyShort(amount: Double): String {
    return "¥" + String.format(java.util.Locale.CHINA, "%.2f", amount)
}

@Composable
private fun ProductImage(
    imageUri: String?,
    modifier: Modifier = Modifier,
) {
    if (imageUri.isNullOrBlank()) {
        Box(modifier = modifier)
        return
    }
    val context = LocalContext.current
    val bitmapState: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, key1 = imageUri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(imageUri)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    if (bitmapState != null) {
        Image(
            bitmap = bitmapState!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    } else {
        Box(modifier = modifier)
    }
}

@Composable
fun AdminProductManageScreen(
    contentPadding: PaddingValues,
    productViewModel: ProductViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val products by productViewModel.products.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        productViewModel.setFilters(type = null, keyword = "")
    }

    var showAdd by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf(PRODUCT_TYPE_COURSE) }
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var provider by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        imageUri = uri?.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "商品管理")

        AppPrimaryButton(onClick = { showAdd = !showAdd }, modifier = Modifier.fillMaxWidth()) {
            Text(if (showAdd) "收起新增表单" else "新增商品")
        }

        if (showAdd) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == PRODUCT_TYPE_COURSE, onClick = { type = PRODUCT_TYPE_COURSE }, label = { Text("课程") })
                        FilterChip(
                            selected = type == PRODUCT_TYPE_MATERIAL,
                            onClick = { type = PRODUCT_TYPE_MATERIAL },
                            label = { Text("课辅资料") },
                        )
                    }
                    AppOutlinedField(
                        value = name,
                        onValueChange = { name = it },
                        label = "名称",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppOutlinedField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = "价格",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppOutlinedField(
                        value = provider,
                        onValueChange = { provider = it },
                        label = "提供方",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    AppOutlinedField(
                        value = details,
                        onValueChange = { details = it },
                        label = "详情",
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        singleLine = false,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        AppOutlinedButton(
                            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f),
                        ) { Text("选择图片") }
                        AppTonalButton(onClick = { imageUri = null }, modifier = Modifier.weight(1f)) { Text("清除图片") }
                    }
                    if (!imageUri.isNullOrBlank()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            ProductImage(imageUri = imageUri, modifier = Modifier.width(96.dp).height(72.dp))
                            Text(text = imageUri ?: "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }

                    AppPrimaryButton(
                        onClick = {
                            val price = priceText.trim().toDoubleOrNull() ?: 0.0
                            productViewModel.addProduct(
                                type = type,
                                name = name,
                                price = price,
                                provider = provider,
                                details = details,
                                imageUri = imageUri,
                                onSuccess = {
                                    showAdd = false
                                    name = ""
                                    priceText = ""
                                    provider = ""
                                    details = ""
                                    imageUri = null
                                    type = PRODUCT_TYPE_COURSE
                                },
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("保存商品") }
                }
            }
        }

        HorizontalDivider()

        if (products.isEmpty()) {
            EmptyStateCard(title = "暂无商品", description = "点击上方“新增商品”添加课程或课辅资料。")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(products, key = { it.id }) { p: ProductEntity ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = p.name, style = MaterialTheme.typography.titleSmall)
                            Text(text = "分类：${if (p.type == PRODUCT_TYPE_COURSE) "课程" else "课辅资料"}")
                            Text(text = "提供方：${p.provider}")
                            Text(text = "价格：${p.price}")
                            if (!p.imageUri.isNullOrBlank()) {
                                ProductImage(imageUri = p.imageUri, modifier = Modifier.fillMaxWidth().height(120.dp))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AppTonalButton(
                                    onClick = { productViewModel.deleteProduct(p.id) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("删除") }
                            }
                        }
                    }
                }
            }
        }

        uiState.message?.let { msg ->
            MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { productViewModel.clearMessage() })
        }
    }
}

@Composable
fun AdminParentListScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val parents by adminViewModel.parents.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "家长用户列表")
        HorizontalDivider()
        if (parents.isEmpty()) {
            EmptyStateCard(
                title = "暂无家长用户",
                description = "当有家长注册后会在这里显示。",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(parents, key = { it.id }) { parent ->
                    ParentRow(parent = parent, onDelete = { adminViewModel.deleteParent(parent.id) })
                }
            }
        }
        uiState.message?.let { msg ->
            MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
        }
    }
}

@Composable
private fun ParentRow(
    parent: ParentEntity,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "手机号：${parent.phone}")
            Text(text = "地址：${parent.address}")
            Text(text = "学生：${parent.studentName}（${parent.studentGender}，${parent.studentGrade}）")
            AppOutlinedButton(onClick = onDelete) { Text("删除用户") }
        }
    }
}

@Composable
fun AdminTeacherListScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val teachers by adminViewModel.teachers.collectAsStateWithLifecycle()
    val pending by adminViewModel.pendingTeachers.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "老师用户列表")
        Text(text = "待审核：${pending.size}")
        HorizontalDivider()
        if (teachers.isEmpty()) {
            EmptyStateCard(
                title = "暂无老师用户",
                description = "当有老师注册后会在这里显示，并可进行审核与管理。",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(teachers, key = { it.id }) { teacher ->
                    TeacherRow(
                        teacher = teacher,
                        onApprove = { adminViewModel.approveTeacher(teacher.id) },
                        onReject = { adminViewModel.rejectTeacher(teacher.id) },
                        onDisable = { adminViewModel.disableTeacher(teacher.id) },
                        onDelete = { adminViewModel.deleteTeacher(teacher.id) },
                    )
                }
            }
        }
        uiState.message?.let { msg ->
            MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
        }
    }
}

@Composable
private fun TeacherRow(
    teacher: TeacherEntity,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onDisable: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "手机号：${teacher.phone}")
            Text(text = "微信：${teacher.wechat ?: "未填写"}")
            Text(text = "履历：${teacher.teachingExperience}")
            Text(text = "就职：${teacher.employmentStatus}")
            Text(text = "科目：${teacher.subjects}")
            Text(text = "年级：${teacher.grades}")
            Text(text = "价格：${teacher.pricePerHour}")
            Text(text = "地址：${teacher.address}")
            Text(text = "状态：${zhTeacherReviewStatus(teacher.status)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppPrimaryButton(onClick = onApprove, enabled = teacher.status == "PENDING" || teacher.status == "REJECTED") { Text("通过") }
                AppOutlinedButton(onClick = onReject, enabled = teacher.status == "PENDING") { Text("拒绝") }
                AppTonalButton(onClick = onDisable, enabled = teacher.status == "APPROVED") { Text("禁用") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppOutlinedButton(onClick = onDelete) { Text("删除") }
            }
        }
    }
}

@Composable
fun AdminApplicationListScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val applications by adminViewModel.applications.collectAsStateWithLifecycle()
    val uiState by adminViewModel.uiState.collectAsStateWithLifecycle()
    var newestFirst by remember { mutableStateOf(true) }
    val sortedApplications = remember(applications, newestFirst) {
        if (newestFirst) applications else applications.asReversed()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "申请记录管理")
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
                title = "暂无申请记录",
                description = "家长提交申请后会在这里显示，可由管理员统一管理状态。",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sortedApplications, key = { it.id }) { app ->
                    AdminApplicationRow(
                        app = app,
                        onSetStatus = { status -> adminViewModel.setApplicationStatus(app.id, status) },
                    )
                }
            }
        }
        uiState.message?.let { msg ->
            MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { adminViewModel.clearMessage() })
        }
    }
}

@Composable
private fun AdminApplicationRow(
    app: ApplicationEntity,
    onSetStatus: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "申请编号：${app.id}")
            Text(text = "家长：${app.parentPhone}")
            Text(text = "老师：${app.teacherPhone}")
            Text(text = "学生：${app.studentName}（${app.studentGrade}）")
            Text(text = "状态：${zhApplicationStatus(app.status)}")
            Text(text = "支付：${zhPaymentStatus(app.paymentStatus)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTonalButton(onClick = { onSetStatus("PENDING") }, enabled = app.status != "PENDING") { Text("待处理") }
                AppPrimaryButton(onClick = { onSetStatus("ACCEPTED") }, enabled = app.status != "ACCEPTED") { Text("已接受") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppOutlinedButton(onClick = { onSetStatus("REJECTED") }, enabled = app.status != "REJECTED") { Text("已拒绝") }
                AppPrimaryButton(onClick = { onSetStatus("COMPLETED") }, enabled = app.status != "COMPLETED") { Text("已完成") }
            }
        }
    }
}

@Composable
fun AdminPaymentRecordScreen(
    contentPadding: PaddingValues,
    adminViewModel: AdminViewModel,
    modifier: Modifier = Modifier,
) {
    val payments by adminViewModel.payments.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "支付记录")
        HorizontalDivider()
        if (payments.isEmpty()) {
            EmptyStateCard(
                title = "暂无支付记录",
                description = "家长完成支付后会在这里生成支付记录。",
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(payments, key = { it.id }) { payment ->
                    PaymentRow(payment = payment)
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    payment: PaymentEntity,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "支付编号：${payment.id}")
            Text(text = "申请编号：${payment.applicationId}")
            Text(text = "家长：${payment.parentPhone}")
            Text(text = "老师：${payment.teacherPhone}")
            Text(text = "金额：${payment.amount}")
            Text(text = "状态：${zhPaymentStatus(payment.status)}")
        }
    }
}
