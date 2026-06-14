package com.example.teacher.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacher.data.AppRepository
import com.example.teacher.core.zhApplicationStatus
import com.example.teacher.core.zhPaymentStatus
import com.example.teacher.core.zhProductOrderStatus
import com.example.teacher.core.zhTeacherReviewStatus
import com.example.teacher.ui.components.AppCard
import com.example.teacher.ui.components.AppEmptyStateCard
import com.example.teacher.ui.components.AppErrorRetryCard
import com.example.teacher.ui.components.AppMessageCard
import com.example.teacher.ui.components.AppOutlinedField
import com.example.teacher.ui.components.AppOutlinedButton
import com.example.teacher.ui.components.AppPrimaryButton
import com.example.teacher.ui.components.AppPullToRefresh
import com.example.teacher.ui.components.AppScaffold
import com.example.teacher.ui.components.AppTonalButton
import com.example.teacher.data.local.ApplicationEntity
import com.example.teacher.data.local.CartItemWithProduct
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.ProductEntity
import com.example.teacher.data.local.ProductOrderEntity
import com.example.teacher.data.local.ProductOrderItemEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.ui.SessionState
import com.example.teacher.ui.Role
import com.example.teacher.ui.viewmodel.ParentHomeViewModel
import com.example.teacher.ui.viewmodel.CommerceViewModel
import com.example.teacher.ui.viewmodel.NotificationViewModel
import com.example.teacher.ui.viewmodel.ParentViewModel
import com.example.teacher.ui.viewmodel.ProductViewModel
import com.example.teacher.ui.viewmodel.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@Composable
private fun SectionTitle(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionText) }
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

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

@Composable
private fun RequireLoginDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onGoLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要登录") },
        text = { Text("当前为游客模式。登录后才能继续此操作。") },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onGoLogin()
                },
            ) { Text("去登录") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        modifier = modifier,
    )
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
        selected.isEmpty() -> "不限"
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
                text = { Text("清空（不限）") },
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
        selected.take(3).forEach { value ->
            FilterChip(
                selected = true,
                onClick = { onRemove(value) },
                label = { Text(value) },
            )
        }
        if (selected.size > 3) {
            Text(text = "…", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ParentHomeScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    parentViewModel: ParentViewModel,
    parentHomeViewModel: ParentHomeViewModel,
    onGoSearchTeachers: () -> Unit,
    onGoMyApplications: () -> Unit,
    onGoCart: () -> Unit,
    onGoOrders: () -> Unit,
    onTeacherClick: (Int) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applications by parentViewModel.applications.collectAsStateWithLifecycle()
    val homeState by parentHomeViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sessionState.parentId) {
        val id = sessionState.parentId ?: return@LaunchedEffect
        parentHomeViewModel.setParentId(id)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(60_000)
            parentHomeViewModel.refreshHotTeachers()
        }
    }

    val pendingCount = applications.count { it.status == "PENDING" }
    val acceptedCount = applications.count { it.status == "ACCEPTED" }
    val completedCount = applications.count { it.status == "COMPLETED" }
    val paidCount = applications.count { it.paymentStatus == "PAID" }

    ScreenScaffold(
        title = "家长中心",
        contentPadding = contentPadding,
        loading = homeState.loading,
        modifier = modifier,
    ) { padding ->
        AppPullToRefresh(
            isRefreshing = homeState.loading,
            onRefresh = { parentHomeViewModel.refreshHotTeachers() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "下午好，家长用户", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "帮孩子找到更合适的老师",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = homeState.parentPhone?.let { "手机号：$it" } ?: "家长ID：${sessionState.parentId ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onGoSearchTeachers,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "搜索老师 / 科目 / 区域", style = MaterialTheme.typography.bodyLarge)
                                Text(text = "快速筛选并提交申请", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    SectionTitle(title = "快捷入口")
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            QuickActionTile(
                                title = "找老师",
                                subtitle = "搜索与筛选",
                                icon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                                onClick = onGoSearchTeachers,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                title = "我的申请",
                                subtitle = "查看进度",
                                icon = { Icon(imageVector = Icons.Filled.EventNote, contentDescription = null) },
                                onClick = onGoMyApplications,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            QuickActionTile(
                                title = "购物车",
                                subtitle = "待付款商品",
                                icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) },
                                onClick = onGoCart,
                                modifier = Modifier.weight(1f),
                            )
                            QuickActionTile(
                                title = "我的订单",
                                subtitle = "购买记录",
                                icon = { Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null) },
                                onClick = onGoOrders,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                item {
                    homeState.error?.let { err ->
                        AppErrorRetryCard(
                            message = err,
                            onRetry = { parentHomeViewModel.refreshHotTeachers() },
                        )
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "数据概览")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "待处理", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = pendingCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "已接受", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = acceptedCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "已完成", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = completedCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "已支付", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = paidCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "热门老师推荐", actionText = "查看全部", onAction = onGoSearchTeachers)
                            if (homeState.hotTeachers.isEmpty()) {
                                Text(text = "上周暂无接单数据。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    homeState.hotTeachers.take(3).forEach { item ->
                                        TeacherCard(
                                            teacher = item.teacher,
                                            onClick = { onTeacherClick(item.teacher.id) },
                                            trailingTag = "上周已支付 ${item.orderCount} 单",
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                    ) {
                        Text("退出登录")
                    }
                }

                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

@Composable
fun GuestHomeScreen(
    contentPadding: PaddingValues,
    onBrowseTeachers: () -> Unit,
    onSearchTeachers: () -> Unit,
    onProducts: () -> Unit,
    onSearchProducts: () -> Unit,
    onGoLogin: () -> Unit,
    onExitGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "主页", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = "游",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "游客模式", style = MaterialTheme.typography.titleMedium)
                            Text(text = "可浏览与搜索内容；购买/申请等操作需登录", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "找老师", style = MaterialTheme.typography.titleSmall)
                    Text(text = "你可以先浏览与搜索老师。需要提交申请/下单时再登录即可。", style = MaterialTheme.typography.bodyMedium)
                    AppPrimaryButton(onClick = onSearchTeachers, modifier = Modifier.fillMaxWidth()) { Text("搜索老师") }
                    AppTonalButton(onClick = onBrowseTeachers, modifier = Modifier.fillMaxWidth()) { Text("浏览老师列表") }
                    AppOutlinedButton(onClick = onProducts, modifier = Modifier.fillMaxWidth()) { Text("浏览课程 / 课辅资料") }
                    Text(
                        text = "也可以使用底部导航进入「搜索 / 商品」。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AppPrimaryButton(onClick = onGoLogin, modifier = Modifier.fillMaxWidth()) { Text("登录 / 注册") }
            AppTonalButton(onClick = onExitGuest, modifier = Modifier.fillMaxWidth()) { Text("退出游客模式") }
        }
    }
}

@Composable
fun ParentMeScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    appRepository: AppRepository,
    parentViewModel: ParentViewModel,
    profileComplete: Boolean,
    onEditProfile: () -> Unit,
    onMyApplications: () -> Unit,
    onCart: () -> Unit,
    onOrders: () -> Unit,
    onReport: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by parentViewModel.uiState.collectAsStateWithLifecycle()
    val applications by parentViewModel.applications.collectAsStateWithLifecycle()
    var parent by remember { mutableStateOf<ParentEntity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var cartCount by remember { mutableStateOf(0) }
    var orderCount by remember { mutableStateOf(0) }

    LaunchedEffect(sessionState.parentId) {
        val id = sessionState.parentId
        if (id == null) {
            parent = null
            loading = false
            return@LaunchedEffect
        }
        parentViewModel.setParentId(id)
        loading = true
        runCatching { appRepository.getParentById(id) }
            .onSuccess { parent = it }
            .onFailure { parentViewModel.setError(it.message ?: "读取个人信息失败") }
        runCatching {
            withContext(Dispatchers.IO) {
                cartCount = appRepository.getCart(id).first().sumOf { it.quantity }
                orderCount = appRepository.getOrders(id).first().size
            }
        }
        loading = false
    }

    ScreenScaffold(
        title = "个人主页",
        contentPadding = contentPadding,
        loading = loading || uiState.loading,
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }
            val p = parent
            if (p == null) {
                item {
                    EmptyStateCard(
                        title = "未获取到个人信息",
                        description = "请退出后重新登录。",
                    )
                }
            } else {
                if (!profileComplete) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = "完善资料以使用全部功能", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "完善地址、学生信息与薄弱科目后，可提交家教申请并获得更准确的匹配。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                AppPrimaryButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) { Text("去完善资料") }
                            }
                        }
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier =
                                        Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = "家", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                    Text(text = "家长用户", style = MaterialTheme.typography.titleSmall)
                                    Text(text = "手机号：${p.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(text = "地址：${p.address}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "学生：${p.studentName}｜${p.studentGender}｜${p.studentGrade}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionTitle(title = "我的数据")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "我的申请", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = applications.size.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "购物车", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = cartCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                AppCard(modifier = Modifier.weight(1f)) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "订单", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = orderCount.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                item { SectionTitle(title = "常用功能") }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickActionTile(
                            title = "编辑资料",
                            subtitle = "完善地址与学生信息",
                            icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                            onClick = onEditProfile,
                        )
                        QuickActionTile(
                            title = "我的申请",
                            subtitle = "查看申请进度",
                            icon = { Icon(imageVector = Icons.Filled.EventNote, contentDescription = null) },
                            onClick = onMyApplications,
                        )
                        QuickActionTile(
                            title = "购物车",
                            subtitle = "待付款商品",
                            icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) },
                            onClick = onCart,
                        )
                        QuickActionTile(
                            title = "我的订单",
                            subtitle = "购买记录",
                            icon = { Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null) },
                            onClick = onOrders,
                        )
                        QuickActionTile(
                            title = "举报 / 反馈",
                            subtitle = "问题反馈与建议",
                            icon = { Icon(imageVector = Icons.Filled.Report, contentDescription = null) },
                            onClick = onReport,
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                ) {
                    Text("退出登录")
                }
            }

            item {
                uiState.message?.let { msg ->
                    MessageCard(
                        message = msg,
                        isError = uiState.messageIsError,
                        onDismiss = { parentViewModel.clearMessage() },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

private fun formatMoney(amount: Double): String = "¥" + String.format(java.util.Locale.CHINA, "%.2f", amount)

private fun formatOrderStatus(status: String): String = zhProductOrderStatus(status)

private fun formatTime(time: Long): String {
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

private const val PRODUCT_TYPE_COURSE = "COURSE"
private const val PRODUCT_TYPE_MATERIAL = "MATERIAL"

private fun formatTime(millis: Long?): String {
    if (millis == null) return "-"
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}

@Composable
private fun ProductImage(
    imageUri: String?,
    modifier: Modifier = Modifier,
) {
    if (imageUri.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
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

    if (bitmapState == null) {
        Box(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    } else {
        Image(
            bitmap = bitmapState!!,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(MaterialTheme.shapes.medium),
        )
    }
}

@Composable
fun ParentMessagesScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    notificationViewModel: NotificationViewModel,
    onOpenAppointments: () -> Unit,
    onOpenOrder: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var categoryFilter by remember { mutableStateOf("ALL") }
    var readFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(sessionState.parentId) {
        sessionState.parentId?.let { notificationViewModel.setUser("PARENT", it) }
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
                        AppCard(onClick = {
                            if (!n.isRead) notificationViewModel.markRead(n.id)
                            when (n.refType) {
                                "APPLICATION" -> onOpenAppointments()
                                "PRODUCT_ORDER" -> n.refId?.let(onOpenOrder)
                            }
                        }, modifier = Modifier.fillMaxWidth()) {
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
                                    text = formatTime(n.createdAt),
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
fun ReportSubmitScreen(
    contentPadding: PaddingValues,
    title: String,
    reporterRole: String,
    reporterId: Int?,
    reporterPhone: String?,
    reportViewModel: ReportViewModel,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by reportViewModel.uiState.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf("FEEDBACK") }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    ScreenScaffold(title = title, contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = category == "FEEDBACK", onClick = { category = "FEEDBACK" }, label = { Text("意见反馈") })
                FilterChip(selected = category == "REPORT", onClick = { category = "REPORT" }, label = { Text("举报") })
            }
            AppOutlinedField(
                value = subject,
                onValueChange = { subject = it },
                label = "标题",
                modifier = Modifier.fillMaxWidth(),
            )
            AppOutlinedField(
                value = content,
                onValueChange = { content = it },
                label = "内容",
                singleLine = false,
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
            AppPrimaryButton(
                onClick = {
                    reportViewModel.submit(
                        reporterRole = reporterRole,
                        reporterId = reporterId,
                        reporterPhone = reporterPhone,
                        category = category,
                        subject = subject,
                        content = content,
                        onSuccess = onDone,
                    )
                },
                enabled = subject.isNotBlank() && content.isNotBlank() && !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("提交") }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { reportViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
fun ProductListScreen(
    contentPadding: PaddingValues,
    productViewModel: ProductViewModel,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val products by productViewModel.products.collectAsStateWithLifecycle()

    var type by remember { mutableStateOf<String?>(null) }
    var sortPriceAsc by remember { mutableStateOf(false) }

    LaunchedEffect(type) {
        productViewModel.setFilters(type = type, keyword = "")
    }

    val visibleProducts = remember(products, sortPriceAsc) {
        if (sortPriceAsc) products.sortedBy { it.price } else products
    }

    ScreenScaffold(title = "商品", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = type == null, onClick = { type = null }, label = { Text("全部") })
                FilterChip(selected = type == PRODUCT_TYPE_COURSE, onClick = { type = PRODUCT_TYPE_COURSE }, label = { Text("课程") })
                FilterChip(
                    selected = type == PRODUCT_TYPE_MATERIAL,
                    onClick = { type = PRODUCT_TYPE_MATERIAL },
                    label = { Text("课辅资料") },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = sortPriceAsc,
                    onClick = { sortPriceAsc = true },
                    label = { Text("价格从低到高") },
                )
                FilterChip(
                    selected = !sortPriceAsc,
                    onClick = { sortPriceAsc = false },
                    label = { Text("默认") },
                )
            }

            HorizontalDivider()

            if (visibleProducts.isEmpty()) {
                EmptyStateCard(
                    title = "暂无商品",
                    description = "可使用管理员账号进入后台添加课程或课辅资料。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visibleProducts, key = { it.id }) { product ->
                        AppCard(
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProductImage(
                                    imageUri = product.imageUri,
                                    modifier = Modifier
                                        .height(72.dp)
                                        .weight(0.35f),
                                )
                                Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(text = product.name, style = MaterialTheme.typography.titleSmall)
                                    Text(text = "提供方：${product.provider}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "价格：${product.price}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { productViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
fun ProductSearchScreen(
    contentPadding: PaddingValues,
    productViewModel: ProductViewModel,
    onProductClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by productViewModel.uiState.collectAsStateWithLifecycle()
    val products by productViewModel.visibleProducts.collectAsStateWithLifecycle()
    val canLoadMore by productViewModel.canLoadMore.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var keyword by remember { mutableStateOf("") }
    var type by remember { mutableStateOf<String?>(null) }

    ScreenScaffold(title = "搜索", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        val listState = rememberLazyListState()
        LaunchedEffect(listState, products.size, canLoadMore) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                .distinctUntilChanged()
                .filter { canLoadMore && products.isNotEmpty() && it >= (products.size - 4).coerceAtLeast(0) }
                .collect { productViewModel.loadMore() }
        }

        AppPullToRefresh(
            isRefreshing = uiState.loading,
            onRefresh = {
                focusManager.clearFocus()
                productViewModel.setFilters(type = type, keyword = keyword)
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            AppOutlinedField(
                value = keyword,
                onValueChange = { keyword = it },
                label = "关键词（名称/提供方/详情）",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(selected = type == null, onClick = { type = null }, label = { Text("全部") })
                FilterChip(selected = type == PRODUCT_TYPE_COURSE, onClick = { type = PRODUCT_TYPE_COURSE }, label = { Text("课程") })
                FilterChip(
                    selected = type == PRODUCT_TYPE_MATERIAL,
                    onClick = { type = PRODUCT_TYPE_MATERIAL },
                    label = { Text("课辅资料") },
                )
            }
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    productViewModel.setFilters(type = type, keyword = keyword)
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("搜索") }

            HorizontalDivider()

            if (products.isEmpty()) {
                EmptyStateCard(title = "没有搜索结果", description = "尝试更换关键词或切换分类。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), state = listState) {
                    items(products, key = { it.id }) { product ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(product.id) },
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(text = product.name, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "分类：${if (product.type == PRODUCT_TYPE_COURSE) "课程" else "课辅资料"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(text = "提供方：${product.provider}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "价格：${product.price}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    if (canLoadMore) {
                        item {
                            AppTonalButton(onClick = { productViewModel.loadMore() }, modifier = Modifier.fillMaxWidth()) {
                                Text("加载更多")
                            }
                        }
                    }
                }
            }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { productViewModel.clearMessage() },
                )
            }
            }
        }
    }
}

@Composable
fun ProductDetailScreen(
    contentPadding: PaddingValues,
    productId: Int,
    appRepository: AppRepository,
    commerceViewModel: CommerceViewModel,
    canPurchase: Boolean,
    onRequireLogin: () -> Unit,
    onGoToCart: () -> Unit,
    onGoToOrder: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var product by remember { mutableStateOf<ProductEntity?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    val uiState by commerceViewModel.uiState.collectAsStateWithLifecycle()
    var requireLogin by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        loading = true
        error = null
        runCatching { appRepository.getProductById(productId) }
            .onSuccess { product = it }
            .onFailure { error = it.message ?: "加载失败" }
        loading = false
    }

    ScreenScaffold(
        title = "商品详情",
        contentPadding = contentPadding,
        loading = loading || uiState.loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            error?.let { EmptyStateCard(title = "加载失败", description = it) }
            val p = product
            if (p == null && error == null) {
                EmptyStateCard(title = "加载中", description = "正在获取商品信息…")
                return@ScreenScaffold
            }
            if (p != null) {
                ProductImage(imageUri = p.imageUri, modifier = Modifier.fillMaxWidth().height(180.dp))
                Text(text = p.name, style = MaterialTheme.typography.titleLarge)
                Text(text = "分类：${if (p.type == PRODUCT_TYPE_COURSE) "课程" else "课辅资料"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "提供方：${p.provider}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "价格：${p.price}", style = MaterialTheme.typography.bodyMedium)
                HorizontalDivider()
                Text(text = p.details, style = MaterialTheme.typography.bodyMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AppTonalButton(
                        onClick = {
                            if (!canPurchase) {
                                requireLogin = true
                            } else {
                                commerceViewModel.addToCart(p.id)
                                onGoToCart()
                            }
                        },
                        enabled = !uiState.loading,
                        modifier = Modifier.weight(1f),
                    ) { Text("加入购物车") }
                    AppPrimaryButton(
                        onClick = {
                            if (!canPurchase) {
                                requireLogin = true
                            } else {
                                commerceViewModel.buyNow(p.id) { orderId -> onGoToOrder(orderId) }
                            }
                        },
                        enabled = !uiState.loading,
                        modifier = Modifier.weight(1f),
                    ) { Text("立即购买") }
                }

                uiState.message?.let { msg ->
                    MessageCard(
                        message = msg,
                        isError = uiState.messageIsError,
                        onDismiss = { commerceViewModel.clearMessage() },
                    )
                }
            }

            RequireLoginDialog(
                visible = requireLogin,
                onDismiss = { requireLogin = false },
                onGoLogin = onRequireLogin,
            )
        }
    }
}

@Composable
fun CartScreen(
    contentPadding: PaddingValues,
    commerceViewModel: CommerceViewModel,
    onOrderCreated: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by commerceViewModel.uiState.collectAsStateWithLifecycle()
    val cart by commerceViewModel.cart.collectAsStateWithLifecycle()

    val total = remember(cart) { cart.sumOf { it.price * it.quantity } }

    ScreenScaffold(
        title = "购物车",
        contentPadding = contentPadding,
        loading = uiState.loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (cart.isEmpty()) {
                EmptyStateCard(title = "购物车为空", description = "去商品页挑选课程或课辅资料后再来结算。")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(cart, key = { it.cartItemId }) { item ->
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ProductImage(
                                    imageUri = item.imageUri,
                                    modifier = Modifier
                                        .height(72.dp)
                                        .weight(0.35f),
                                )
                                Column(
                                    modifier = Modifier.weight(0.65f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(text = item.name, style = MaterialTheme.typography.titleSmall)
                                    Text(text = "提供方：${item.provider}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "单价：${formatMoney(item.price)}", style = MaterialTheme.typography.bodyMedium)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            IconButton(
                                                onClick = {
                                                    val next = item.quantity - 1
                                                    if (next <= 0) {
                                                        commerceViewModel.removeCartItem(item.cartItemId)
                                                    } else {
                                                        commerceViewModel.updateCartQuantity(item.cartItemId, next)
                                                    }
                                                },
                                            ) {
                                                Icon(imageVector = Icons.Filled.Remove, contentDescription = null)
                                            }
                                            Text(text = item.quantity.toString(), style = MaterialTheme.typography.titleSmall)
                                            IconButton(
                                                onClick = {
                                                    commerceViewModel.updateCartQuantity(item.cartItemId, item.quantity + 1)
                                                },
                                            ) {
                                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                                            }
                                        }
                                        IconButton(onClick = { commerceViewModel.removeCartItem(item.cartItemId) }) {
                                            Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                                        }
                                    }
                                    Text(
                                        text = "小计：${formatMoney(item.price * item.quantity)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "合计：${formatMoney(total)}", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            AppOutlinedButton(
                                onClick = { commerceViewModel.clearCart() },
                                enabled = cart.isNotEmpty() && !uiState.loading,
                                modifier = Modifier.weight(1f),
                            ) { Text("清空") }
                            AppPrimaryButton(
                                onClick = { commerceViewModel.checkoutCart { id -> onOrderCreated(id) } },
                                enabled = cart.isNotEmpty() && !uiState.loading,
                                modifier = Modifier.weight(1f),
                            ) { Text("去结算") }
                        }
                    }
                }
            }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { commerceViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
fun OrdersScreen(
    contentPadding: PaddingValues,
    commerceViewModel: CommerceViewModel,
    onOrderClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by commerceViewModel.uiState.collectAsStateWithLifecycle()
    val orders by commerceViewModel.orders.collectAsStateWithLifecycle()

    val sortedOrders = remember(orders) { orders.sortedByDescending { it.createdAt } }

    ScreenScaffold(
        title = "我的订单",
        contentPadding = contentPadding,
        loading = uiState.loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (sortedOrders.isEmpty()) {
                EmptyStateCard(title = "暂无订单", description = "可在商品详情页点击“立即购买”或在购物车结算后生成订单。")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(sortedOrders, key = { it.id }) { order ->
                        AppCard(
                            onClick = { onOrderClick(order.id) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(text = "订单 #${order.id}", style = MaterialTheme.typography.titleSmall)
                                    Text(text = formatOrderStatus(order.status), style = MaterialTheme.typography.bodyMedium)
                                }
                                Text(text = "金额：${formatMoney(order.totalAmount)}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "下单时间：${formatTime(order.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                                order.paidAt?.let {
                                    Text(text = "支付时间：${formatTime(it)}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    AppTonalButton(onClick = { onOrderClick(order.id) }, modifier = Modifier.weight(1f)) {
                                        Text("查看详情")
                                    }
                                    if (order.status == "CREATED") {
                                        AppPrimaryButton(
                                            onClick = { commerceViewModel.payOrder(order.id) },
                                            enabled = !uiState.loading,
                                            modifier = Modifier.weight(1f),
                                        ) { Text("支付") }
                                    } else {
                                        AppOutlinedButton(
                                            onClick = { onOrderClick(order.id) },
                                            enabled = false,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text("已支付")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { commerceViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
fun OrderDetailScreen(
    contentPadding: PaddingValues,
    orderId: Int,
    commerceViewModel: CommerceViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by commerceViewModel.uiState.collectAsStateWithLifecycle()
    var order by remember { mutableStateOf<ProductOrderEntity?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val itemsFlow = remember(orderId) { commerceViewModel.orderItems(orderId) }
    val items by itemsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(orderId, uiState.message) {
        loading = true
        error = null
        runCatching { commerceViewModel.getOrder(orderId) }
            .onSuccess {
                order = it
                if (it == null) error = "订单不存在"
            }
            .onFailure { error = it.message ?: "加载失败" }
        loading = false
    }

    val itemsTotal = remember(items) { items.sumOf { it.price * it.quantity } }

    ScreenScaffold(
        title = "订单详情",
        contentPadding = contentPadding,
        loading = loading || uiState.loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            error?.let { EmptyStateCard(title = "加载失败", description = it) }
            val o = order
            if (o == null && error == null) {
                EmptyStateCard(title = "加载中", description = "正在获取订单信息…")
                return@ScreenScaffold
            }

            if (o != null) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "订单 #${o.id}", style = MaterialTheme.typography.titleMedium)
                        Text(text = "状态：${formatOrderStatus(o.status)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "金额：${formatMoney(o.totalAmount)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "下单时间：${formatTime(o.createdAt)}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "支付时间：${formatTime(o.paidAt)}", style = MaterialTheme.typography.bodyMedium)
                        if (o.status == "CREATED") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AppOutlinedButton(
                                    onClick = { commerceViewModel.cancelOrder(o.id) },
                                    enabled = !uiState.loading,
                                    modifier = Modifier.weight(1f),
                                ) { Text("取消订单") }
                                AppPrimaryButton(
                                    onClick = { commerceViewModel.payOrder(o.id) },
                                    enabled = !uiState.loading,
                                    modifier = Modifier.weight(1f),
                                ) { Text("立即支付") }
                            }
                        }
                    }
                }

                Text(text = "订单明细", style = MaterialTheme.typography.titleSmall)
                HorizontalDivider()
                if (items.isEmpty()) {
                    EmptyStateCard(title = "无订单项", description = "该订单没有商品明细。")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        items(items, key = { it.id }) { item ->
                            AppCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    ProductImage(
                                        imageUri = item.imageUri,
                                        modifier = Modifier
                                            .height(72.dp)
                                            .weight(0.35f),
                                    )
                                    Column(modifier = Modifier.weight(0.65f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = item.name, style = MaterialTheme.typography.titleSmall)
                                        Text(text = "提供方：${item.provider}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "单价：${formatMoney(item.price)}", style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "数量：${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "小计：${formatMoney(item.price * item.quantity)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "明细合计：${formatMoney(itemsTotal)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                uiState.message?.let { msg ->
                    MessageCard(
                        message = msg,
                        isError = uiState.messageIsError,
                        onDismiss = { commerceViewModel.clearMessage() },
                    )
                }
            }
        }
    }
}

@Composable
fun ParentPublishApplicationScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    appRepository: AppRepository,
    parentId: Int,
    onGoSearchTeachers: () -> Unit,
    onGoMyApplications: () -> Unit,
    onGoEditProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var parent by remember { mutableStateOf<ParentEntity?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var subject by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var genderPreference by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf(80f..160f) }
    var timeStartAt by remember { mutableStateOf<Long?>(null) }
    var timeEndAt by remember { mutableStateOf<Long?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var successDialogVisible by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    var createdDemandId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(parentId, reloadKey) {
        loading = true
        error = null
        runCatching { appRepository.getParentById(parentId) }
            .onSuccess { parent = it }
            .onFailure { error = it.message ?: "加载失败" }
        loading = false
    }

    fun pickDateTime(initialMillis: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        cal.timeInMillis = initialMillis
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance()
                picked.set(Calendar.YEAR, year)
                picked.set(Calendar.MONTH, month)
                picked.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        picked.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        picked.set(Calendar.MINUTE, minute)
                        picked.set(Calendar.SECOND, 0)
                        picked.set(Calendar.MILLISECOND, 0)
                        onPicked(picked.timeInMillis)
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

    val timeFmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.CHINA) }
    val timeStartText = timeStartAt?.let { timeFmt.format(Date(it)) } ?: "请选择开始时间"
    val timeEndText = timeEndAt?.let { timeFmt.format(Date(it)) } ?: "请选择结束时间"

    ScreenScaffold(
        title = "发布需求",
        contentPadding = contentPadding,
        loading = loading || submitting,
        modifier = modifier,
    ) { padding ->
        val p = parent
        if (p == null) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                error?.let {
                    AppErrorRetryCard(
                        message = it,
                        onRetry = { reloadKey += 1 },
                    )
                }
                if (!loading && error == null) {
                    EmptyStateCard(
                        title = "未获取到资料",
                        description = "请重试或重新登录后再试。",
                    )
                }
            }
            return@ScreenScaffold
        }
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                error?.let {
                    AppErrorRetryCard(
                        message = it,
                        onRetry = { reloadKey += 1 },
                    )
                }
            }

            if (grade.isBlank()) {
                grade = p.studentGrade.ifBlank { "" }
            }
            if (subject.isBlank()) {
                subject = p.weakSubjects.split('、', ',', '，', ';', '；', '|').map { it.trim() }.firstOrNull { it.isNotBlank() }.orEmpty()
            }
            if (timeStartAt == null || timeEndAt == null) {
                val now = System.currentTimeMillis()
                val cal = Calendar.getInstance()
                cal.timeInMillis = now
                cal.add(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 19)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val end = start + 2L * 60 * 60 * 1000
                timeStartAt = start
                timeEndAt = end
            }

            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "发布家教需求", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "填写需求后，会在老师端工作台展示，老师可主动接单。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionTitle(title = "需求信息")
                        SingleSelectDropdown(
                            label = "对应年级",
                            options = listOf("清空", "小学", "初一", "初二", "初三", "高一", "高二", "高三"),
                            value = grade,
                            onSelect = { picked -> grade = if (picked == "清空") "" else picked },
                        )
                        if (submitted && grade.isBlank()) {
                            Text(text = "请选择年级", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        SingleSelectDropdown(
                            label = "对应科目",
                            options = listOf("清空", "语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理", "艺术"),
                            value = subject,
                            onSelect = { picked -> subject = if (picked == "清空") "" else picked },
                        )
                        if (submitted && subject.isBlank()) {
                            Text(text = "请选择科目", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        AppOutlinedField(
                            value = timeStartText,
                            onValueChange = {},
                            label = "所需开始时间",
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val initial = timeStartAt ?: System.currentTimeMillis()
                                        pickDateTime(initial) { picked ->
                                            timeStartAt = picked
                                            if ((timeEndAt ?: 0L) <= picked) timeEndAt = picked + 2L * 60 * 60 * 1000
                                        }
                                    },
                                ) { Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null) }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val initial = timeStartAt ?: System.currentTimeMillis()
                                        pickDateTime(initial) { picked ->
                                            timeStartAt = picked
                                            if ((timeEndAt ?: 0L) <= picked) timeEndAt = picked + 2L * 60 * 60 * 1000
                                        }
                                    },
                        )
                        AppOutlinedField(
                            value = timeEndText,
                            onValueChange = {},
                            label = "所需结束时间",
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val initial = timeEndAt ?: (timeStartAt ?: System.currentTimeMillis())
                                        pickDateTime(initial) { picked -> timeEndAt = picked }
                                    },
                                ) { Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null) }
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val initial = timeEndAt ?: (timeStartAt ?: System.currentTimeMillis())
                                        pickDateTime(initial) { picked -> timeEndAt = picked }
                                    },
                        )
                        if (submitted && (timeStartAt == null || timeEndAt == null || (timeEndAt ?: 0L) <= (timeStartAt ?: 0L))) {
                            Text(text = "请选择正确的时间段", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        SingleSelectDropdown(
                            label = "老师性别偏好（可不选）",
                            options = listOf("不限", "男", "女"),
                            value = genderPreference.ifBlank { "不限" },
                            onSelect = { picked -> genderPreference = if (picked == "不限") "" else picked },
                        )

                        HorizontalDivider()
                        Text(
                            text = "费用区间：${priceRange.start.toInt()} - ${priceRange.endInclusive.toInt()} 元/小时",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        RangeSlider(
                            value = priceRange,
                            onValueChange = { priceRange = it },
                            valueRange = 0f..500f,
                        )
                    }
                }
            }

            item {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(title = "联系信息")
                        Text(text = "家长手机号：${p.phone.ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "地址：${buildAddressText(p.poiName, p.poiAddress ?: p.address, p.addressDetail).ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "学生：${p.studentName}｜${p.studentGender}｜${p.studentGrade}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AppTonalButton(onClick = onGoEditProfile, modifier = Modifier.weight(1f)) { Text("修改资料") }
                    AppOutlinedButton(onClick = onGoSearchTeachers, modifier = Modifier.weight(1f)) { Text("去搜索老师") }
                }
            }

            item {
                AppPrimaryButton(
                    onClick = {
                        focusManager.clearFocus()
                        submitted = true
                        val start = timeStartAt
                        val end = timeEndAt
                        val token = sessionState.accessToken?.trim().orEmpty()
                        if (grade.isBlank() || subject.isBlank() || start == null || end == null || end <= start) return@AppPrimaryButton
                        if (token.isBlank()) {
                            error = "请先登录获取 Token（后端鉴权必需）"
                            return@AppPrimaryButton
                        }
                        val minPrice = priceRange.start.toDouble()
                        val maxPrice = priceRange.endInclusive.toDouble()
                        submitting = true
                        coroutineScope.launch {
                            val result =
                                runCatching {
                                    com.example.teacher.data.BackendApi.createDemand(
                                        accessToken = token,
                                        subject = subject,
                                        studentGrade = grade,
                                        timeStartAt = start,
                                        timeEndAt = end,
                                        teacherGenderPreference = genderPreference.takeIf { it.isNotBlank() },
                                        minPrice = minPrice,
                                        maxPrice = maxPrice,
                                    )
                                }
                            submitting = false
                            if (result.isSuccess) {
                                createdDemandId = result.getOrThrow().demand.id
                                successDialogVisible = true
                            } else {
                                error = result.exceptionOrNull()?.message ?: "发布失败"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("发布申请") }
            }

            item {
                AppTonalButton(onClick = onGoMyApplications, modifier = Modifier.fillMaxWidth()) { Text("查看我的申请") }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }

    if (successDialogVisible) {
        val demandId = createdDemandId
        AlertDialog(
            onDismissRequest = { successDialogVisible = false },
            title = { Text("发布成功") },
            text = { Text("需求已发布${demandId?.let { "（ID：$it）" }.orEmpty()}，将面向所有老师展示。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        successDialogVisible = false
                        onGoMyApplications()
                    },
                ) { Text("知道了") }
            },
            dismissButton = { TextButton(onClick = { successDialogVisible = false }) { Text("关闭") } },
        )
    }
}

@Composable
fun TeacherListScreen(
    contentPadding: PaddingValues,
    parentId: Int?,
    parentViewModel: ParentViewModel,
    onSearch: () -> Unit,
    onTeacherClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(parentId) {
        val id = parentId ?: return@LaunchedEffect
        parentViewModel.setParentId(id)
    }

    val sortOption by parentViewModel.teacherListSort.collectAsStateWithLifecycle()
    val visibleTeachers by parentViewModel.teacherListVisibleTeachers.collectAsStateWithLifecycle()
    val canLoadMore by parentViewModel.teacherListCanLoadMore.collectAsStateWithLifecycle()

    ScreenScaffold(title = "老师列表", contentPadding = contentPadding, modifier = modifier) { padding ->
        val listState = rememberLazyListState()

        LaunchedEffect(listState, visibleTeachers.size, canLoadMore) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                .distinctUntilChanged()
                .filter { canLoadMore && visibleTeachers.isNotEmpty() && it >= (visibleTeachers.size - 4).coerceAtLeast(0) }
                .collect { parentViewModel.loadMoreTeacherList() }
        }

        AppPullToRefresh(
            isRefreshing = false,
            onRefresh = { parentViewModel.resetTeacherListPaging() },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "仅展示已审核通过的老师", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        AppPrimaryButton(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("去筛选") }
                        AppTonalButton(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("高级搜索") }
                    }
                    MultiSelectDropdown(
                        label = "排序方式",
                        options = listOf("默认", "价格从低到高", "区域优先"),
                        selected = setOf(
                            when (sortOption) {
                                ParentViewModel.TeacherSortOption.Default -> "默认"
                                ParentViewModel.TeacherSortOption.PriceAsc -> "价格从低到高"
                                ParentViewModel.TeacherSortOption.AreaFirst -> "区域优先"
                            },
                        ),
                        onToggle = { option ->
                            parentViewModel.setTeacherListSort(
                                when (option) {
                                    "价格从低到高" -> ParentViewModel.TeacherSortOption.PriceAsc
                                    "区域优先" -> ParentViewModel.TeacherSortOption.AreaFirst
                                    else -> ParentViewModel.TeacherSortOption.Default
                                },
                            )
                        },
                        onClear = { parentViewModel.setTeacherListSort(ParentViewModel.TeacherSortOption.Default) },
                    )
                }
            }
            Text(text = "已加载 ${visibleTeachers.size} 位老师", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            if (visibleTeachers.isEmpty()) {
                EmptyStateCard(
                    title = "暂无老师",
                    description = "当前没有已审核通过的老师。可让老师先注册，再由管理员审核通过。",
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), state = listState) {
                    items(visibleTeachers, key = { it.id }) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onClick = { onTeacherClick(teacher.id) },
                        )
                    }
                    if (canLoadMore) {
                        item {
                            AppTonalButton(
                                onClick = { parentViewModel.loadMoreTeacherList() },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("加载更多") }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
fun TeacherSearchScreen(
    contentPadding: PaddingValues,
    parentId: Int?,
    parentViewModel: ParentViewModel,
    onTeacherClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialFilters = remember { parentViewModel.getTeacherSearchFilters() }
    var subjects by remember { mutableStateOf(initialFilters.subjects) }
    var grades by remember { mutableStateOf(initialFilters.grades) }
    var employmentStatuses by remember { mutableStateOf(initialFilters.employmentStatuses) }
    var addressKeyword by remember { mutableStateOf(initialFilters.addressKeyword) }
    var priceRange by remember {
        mutableStateOf(
            (initialFilters.minPrice?.toFloat() ?: 0f)..(initialFilters.maxPrice?.toFloat() ?: 500f),
        )
    }
    val focusManager = LocalFocusManager.current
    val sortOption by parentViewModel.teacherSearchSort.collectAsStateWithLifecycle()
    val sortedResults by parentViewModel.teacherSearchSortedResults.collectAsStateWithLifecycle()
    val visibleResults by parentViewModel.teacherSearchVisibleResults.collectAsStateWithLifecycle()
    val canLoadMore by parentViewModel.teacherSearchCanLoadMore.collectAsStateWithLifecycle()

    LaunchedEffect(parentId) {
        val id = parentId ?: return@LaunchedEffect
        parentViewModel.setParentId(id)
    }

    var showMoreFilters by remember { mutableStateOf(false) }

    ScreenScaffold(title = "搜索", contentPadding = contentPadding, modifier = modifier) { padding ->
        val subjectOptions = listOf("语文", "数学", "英语", "物理", "化学", "生物", "政治", "历史", "地理")
        val gradeOptions = listOf("小学", "初一", "初二", "初三", "高一", "高二", "高三")
        val employmentOptions = listOf("在职", "全职", "大学生", "自由职业")
        val listState = rememberLazyListState()

        LaunchedEffect(listState, visibleResults.size, canLoadMore) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                .distinctUntilChanged()
                .filter { canLoadMore && visibleResults.isNotEmpty() && it >= (visibleResults.size - 4).coerceAtLeast(0) }
                .collect { parentViewModel.loadMoreTeacherSearchResults() }
        }

        AppPullToRefresh(
            isRefreshing = false,
            onRefresh = { parentViewModel.resetTeacherSearchPaging() },
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                state = listState,
            ) {
                item { Spacer(modifier = Modifier.height(20.dp)) }

                item {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppOutlinedField(
                                value = addressKeyword,
                                onValueChange = { addressKeyword = it },
                                label = "搜索老师 / 区域",
                                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showMoreFilters = !showMoreFilters }) {
                                        Icon(imageVector = Icons.Filled.Tune, contentDescription = null)
                                    }
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                subjectOptions.take(5).forEach { option ->
                                    FilterChip(
                                        selected = subjects.contains(option),
                                        onClick = { subjects = if (subjects.contains(option)) subjects - option else subjects + option },
                                        label = { Text(option) },
                                    )
                                }
                                FilterChip(
                                    selected = grades.isNotEmpty(),
                                    onClick = { showMoreFilters = true },
                                    label = { Text(if (grades.isEmpty()) "年级" else "年级(${grades.size})") },
                                )
                                FilterChip(
                                    selected = employmentStatuses.isNotEmpty(),
                                    onClick = { showMoreFilters = true },
                                    label = { Text(if (employmentStatuses.isEmpty()) "更多筛选" else "更多(${employmentStatuses.size})") },
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        focusManager.clearFocus()
                                        subjects = emptySet()
                                        grades = emptySet()
                                        employmentStatuses = emptySet()
                                        addressKeyword = ""
                                        priceRange = 0f..500f
                                        parentViewModel.resetTeacherSearchFilters()
                                    },
                                    label = { Text("清空") },
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AppPrimaryButton(
                                    onClick = {
                                        focusManager.clearFocus()
                                        val min = priceRange.start.toDouble()
                                        val max = priceRange.endInclusive.toDouble()
                                        val usePriceFilter = min > 0.0 || max < 500.0
                                        parentViewModel.setTeacherSearchFilters(
                                            ParentViewModel.TeacherSearchFilters(
                                                subjects = subjects,
                                                grades = grades,
                                                addressKeyword = addressKeyword,
                                                minPrice = if (usePriceFilter) min else null,
                                                maxPrice = if (usePriceFilter) max else null,
                                                employmentStatuses = employmentStatuses,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("搜索") }
                                AppTonalButton(
                                    onClick = { showMoreFilters = !showMoreFilters },
                                    modifier = Modifier.weight(1f),
                                ) { Text(if (showMoreFilters) "收起筛选" else "更多筛选") }
                            }
                        }
                    }
                }

                if (showMoreFilters) {
                    item {
                        AppCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionTitle(title = "筛选条件")
                                MultiSelectDropdown(
                                    label = "科目（可多选）",
                                    options = subjectOptions,
                                    selected = subjects,
                                    onToggle = { option -> subjects = if (subjects.contains(option)) subjects - option else subjects + option },
                                    onClear = { subjects = emptySet() },
                                )
                                SelectedChipsRow(selected = subjects, onRemove = { subjects -= it })

                                MultiSelectDropdown(
                                    label = "年级（可多选）",
                                    options = gradeOptions,
                                    selected = grades,
                                    onToggle = { option -> grades = if (grades.contains(option)) grades - option else grades + option },
                                    onClear = { grades = emptySet() },
                                )
                                SelectedChipsRow(selected = grades, onRemove = { grades -= it })

                                MultiSelectDropdown(
                                    label = "就职情况（可多选）",
                                    options = employmentOptions,
                                    selected = employmentStatuses,
                                    onToggle = { option ->
                                        employmentStatuses =
                                            if (employmentStatuses.contains(option)) employmentStatuses - option else employmentStatuses + option
                                    },
                                    onClear = { employmentStatuses = emptySet() },
                                )
                                SelectedChipsRow(selected = employmentStatuses, onRemove = { employmentStatuses -= it })

                                HorizontalDivider()
                                Text(
                                    text = "价格区间：${priceRange.start.toInt()} - ${priceRange.endInclusive.toInt()} 元/小时",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                RangeSlider(
                                    value = priceRange,
                                    onValueChange = { priceRange = it },
                                    valueRange = 0f..500f,
                                )

                                MultiSelectDropdown(
                                    label = "排序方式",
                                    options = listOf("默认", "价格从低到高", "区域优先"),
                                    selected = setOf(
                                        when (sortOption) {
                                            ParentViewModel.TeacherSortOption.Default -> "默认"
                                            ParentViewModel.TeacherSortOption.PriceAsc -> "价格从低到高"
                                            ParentViewModel.TeacherSortOption.AreaFirst -> "区域优先"
                                        },
                                    ),
                                    onToggle = { option ->
                                        parentViewModel.setTeacherSearchSort(
                                            when (option) {
                                                "价格从低到高" -> ParentViewModel.TeacherSortOption.PriceAsc
                                                "区域优先" -> ParentViewModel.TeacherSortOption.AreaFirst
                                                else -> ParentViewModel.TeacherSortOption.Default
                                            },
                                        )
                                    },
                                    onClear = { parentViewModel.setTeacherSearchSort(ParentViewModel.TeacherSortOption.Default) },
                                )
                            }
                        }
                    }
                }

                item {
                    SectionTitle(title = "老师列表（${sortedResults.size}）")
                    HorizontalDivider()
                }

                if (sortedResults.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "暂时没有匹配的老师",
                            description = "可以尝试放宽价格区间或减少筛选条件。",
                        )
                    }
                } else {
                    items(visibleResults, key = { it.id }) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            onClick = { onTeacherClick(teacher.id) },
                        )
                    }
                    if (canLoadMore) {
                        item {
                            AppTonalButton(
                                onClick = { parentViewModel.loadMoreTeacherSearchResults() },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("加载更多") }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

private fun isValidParentProfile(
    address: String,
    studentName: String,
    studentGender: String,
    studentGrade: String,
    weakSubjects: String,
): Boolean {
    return address.isNotBlank() &&
        studentName.isNotBlank() &&
        studentGender.isNotBlank() &&
        studentGrade.isNotBlank() &&
        weakSubjects.isNotBlank()
}

private val profileSubjectOptions = listOf(
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

private val profileGradeOptions = listOf(
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

private val profileGenderOptions = listOf("男", "女")

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleSelectDropdown(
    label: String,
    options: List<String>,
    value: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val summary = value.takeIf { it.isNotBlank() } ?: "请选择"
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = { Text(if (option == value) "✓" else "") },
                )
            }
        }
    }
}

@Composable
private fun TeacherCard(
    teacher: TeacherEntity,
    onClick: () -> Unit,
    trailingTag: String? = null,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = teacher.phone, style = MaterialTheme.typography.titleSmall)
                if (trailingTag != null) {
                    Box(
                        modifier =
                            Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = trailingTag,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
            Text(text = "科目：${teacher.subjects}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "年级：${teacher.grades}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "就职：${teacher.employmentStatus}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "价格：${teacher.pricePerHour} /小时", style = MaterialTheme.typography.bodyMedium)
            Text(text = "地址：${teacher.address}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TeacherDetailScreen(
    contentPadding: PaddingValues,
    teacherId: Int,
    appRepository: AppRepository,
    parentViewModel: ParentViewModel,
    canSubmitApplication: Boolean,
    onRequireLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by parentViewModel.uiState.collectAsStateWithLifecycle()
    var teacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var requireLogin by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        loading = true
        runCatching { appRepository.getTeacherById(teacherId) }
            .onSuccess { teacher = it }
            .onFailure { loadError = it.message ?: "加载失败" }
        loading = false
    }

    ScreenScaffold(
        title = "老师详情",
        contentPadding = contentPadding,
        loading = uiState.loading || loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            loadError?.let {
                EmptyStateCard(title = "加载失败", description = it)
            }
            val t = teacher
            if (t == null && loadError == null) {
                EmptyStateCard(title = "加载中", description = "正在获取老师信息…")
            }
            if (t != null) {
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "手机号：${t.phone}")
                        Text(text = "微信：${t.wechat ?: "未填写"}")
                        Text(text = "教学履历：${t.teachingExperience}")
                        Text(text = "就职情况：${t.employmentStatus}")
                        Text(text = "可教科目：${t.subjects}")
                        Text(text = "可教年级：${t.grades}")
                        Text(text = "课时价格：${t.pricePerHour} /小时")
                        Text(text = "地址：${t.address}")
                        Text(text = "审核状态：${zhTeacherReviewStatus(t.status)}")
                    }
                }
                AppPrimaryButton(
                    onClick = {
                        if (!canSubmitApplication) {
                            requireLogin = true
                        } else {
                            parentViewModel.submitApplication(t.id)
                        }
                    },
                    enabled = !uiState.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.loading) "提交中..." else "选择该老师 / 提交申请")
                }
            }
            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { parentViewModel.clearMessage() },
                )
            }

            RequireLoginDialog(
                visible = requireLogin,
                onDismiss = { requireLogin = false },
                onGoLogin = onRequireLogin,
            )
        }
    }
}

@Composable
fun ParentApplicationListScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    onPay: (Int) -> Unit,
    onChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    var newestFirst by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remoteDemands by remember { mutableStateOf<List<com.example.teacher.data.BackendDemandDto>>(emptyList()) }
    var remoteApplications by remember { mutableStateOf<List<com.example.teacher.data.BackendApplicationDto>>(emptyList()) }
    val token = sessionState.accessToken?.trim().orEmpty()

    suspend fun refreshRemoteData() {
        if (token.isBlank()) {
            remoteDemands = emptyList()
            remoteApplications = emptyList()
            loading = false
            errorMessage = "缺少 Token，请重新登录"
            return
        }
        loading = true
        errorMessage = null
        runCatching {
            val demands = com.example.teacher.data.BackendApi.myDemands(token, limit = 50).items
            val applications = com.example.teacher.data.BackendApi.myApplications(token, limit = 50).items
            remoteDemands = if (newestFirst) demands.asReversed() else demands
            remoteApplications = if (newestFirst) applications.asReversed() else applications
        }.onFailure {
            errorMessage = it.message ?: "加载失败"
        }
        loading = false
    }

    LaunchedEffect(token, newestFirst) {
        refreshRemoteData()
    }

    ScreenScaffold(title = "我的需求与申请", contentPadding = contentPadding, loading = loading, modifier = modifier) { padding ->
        AppPullToRefresh(
            isRefreshing = loading,
            onRefresh = {
                if (!loading) coroutineScope.launch { refreshRemoteData() }
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "需求 ${remoteDemands.size} 条 · 申请 ${remoteApplications.size} 条",
                    style = MaterialTheme.typography.bodyMedium,
                )
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
                    AppOutlinedButton(
                        onClick = {
                            coroutineScope.launch { refreshRemoteData() }
                        },
                    ) { Text("刷新") }
                }
                errorMessage?.let {
                    AppErrorRetryCard(
                        message = it,
                        onRetry = { coroutineScope.launch { refreshRemoteData() } },
                    )
                }
                HorizontalDivider()
                Text(text = "我发布的需求", style = MaterialTheme.typography.titleSmall)
                if (remoteDemands.isEmpty()) {
                    EmptyStateCard(
                        title = "暂无需求",
                        description = "发布成功后会立即显示在这里。",
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f, fill = false),
                    ) {
                        items(remoteDemands, key = { it.id }) { demand ->
                            RemoteDemandCard(demand = demand)
                        }
                    }
                }
                HorizontalDivider()
                Text(text = "老师申请记录", style = MaterialTheme.typography.titleSmall)
                if (remoteApplications.isEmpty()) {
                    EmptyStateCard(
                        title = "暂无申请记录",
                        description = "老师接单后，会在这里显示申请状态并可进入私聊。",
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(remoteApplications, key = { it.id }) { app ->
                            RemoteApplicationCard(app = app, onPay = onPay, onChat = onChat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteDemandCard(
    demand: com.example.teacher.data.BackendDemandDto,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "${demand.studentGrade} · ${demand.subject}", style = MaterialTheme.typography.titleSmall)
            Text(text = "需求编号：${demand.id}")
            Text(text = "价格区间：¥${demand.minPrice.toInt()}-${demand.maxPrice.toInt()}/小时")
            Text(text = "状态：${zhDemandStatus(demand.status)}")
            Text(text = "时间：${formatRemoteIsoTime(demand.timeStartAt)} - ${formatRemoteIsoTime(demand.timeEndAt)}")
            Text(text = "发布时间：${formatRemoteIsoTime(demand.createdAt)}")
        }
    }
}

@Composable
private fun RemoteApplicationCard(
    app: com.example.teacher.data.BackendApplicationDto,
    onPay: (Int) -> Unit,
    onChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "申请编号：${app.id}", style = MaterialTheme.typography.titleSmall)
            Text(text = "老师 ID：${app.teacherId}")
            Text(text = "状态：${zhApplicationStatus(app.status)}")
            Text(text = "申请时间：${formatRemoteIsoTime(app.createdAt)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppOutlinedButton(onClick = { onChat(app.id) }) { Text("私聊") }
                if (app.status == "ACCEPTED") {
                    AppPrimaryButton(onClick = { onPay(app.id) }) { Text("去支付") }
                }
            }
        }
    }
}

private fun zhDemandStatus(status: String): String =
    when (status.trim().uppercase()) {
        "OPEN" -> "待接单"
        "CLAIMED" -> "已接单"
        "CLOSED" -> "已关闭"
        else -> status
    }

private fun formatRemoteIsoTime(value: String): String {
    val millis = runCatching { java.time.Instant.parse(value).toEpochMilli() }.getOrNull() ?: return value
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
}

@Composable
fun ChatScreen(
    contentPadding: PaddingValues,
    sessionState: SessionState,
    applicationId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class UiChatMessage(
        val id: Int,
        val senderRole: String,
        val senderId: Int,
        val content: String,
    )

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var threadId by remember { mutableStateOf<Int?>(null) }
    var input by remember { mutableStateOf("") }
    var ws by remember { mutableStateOf<okhttp3.WebSocket?>(null) }
    var messages by remember { mutableStateOf<List<UiChatMessage>>(emptyList()) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    val role = sessionState.role
    val senderRole =
        when (role) {
            Role.Parent -> "PARENT"
            Role.Teacher -> "TEACHER"
            else -> null
        }
    val senderId =
        when (role) {
            Role.Parent -> sessionState.parentId
            Role.Teacher -> sessionState.teacherId
            else -> null
        }
    val token = sessionState.accessToken?.trim().orEmpty()

    LaunchedEffect(applicationId, token) {
        loading = true
        error = null
        threadId = null
        messages = emptyList()
        if (token.isBlank()) {
            error = "缺少 Token，请重新登录"
            loading = false
            return@LaunchedEffect
        }
        runCatching {
            val thread = com.example.teacher.data.BackendApi.threadByApplication(accessToken = token, applicationId = applicationId)
            threadId = thread.threadId
            val history = com.example.teacher.data.BackendApi.messages(accessToken = token, threadId = thread.threadId, limit = 50)
            messages =
                history.items.map { m ->
                    UiChatMessage(id = m.id, senderRole = m.senderRole, senderId = m.senderId, content = m.content)
                }
        }.onFailure {
            error = it.message ?: "加载失败"
        }
        loading = false
    }

    DisposableEffect(threadId, token) {
        val tid = threadId
        if (tid == null || token.isBlank()) return@DisposableEffect onDispose { }

        val socket =
            com.example.teacher.data.BackendApi.openWebSocket(
                token = token,
                onText = { text ->
                    val obj = com.example.teacher.data.BackendApi.parseJsonObject(text) ?: return@openWebSocket
                    val type = obj.get("type")?.asString ?: return@openWebSocket
                    if (type == "message") {
                        val dto = com.example.teacher.data.BackendApi.toMessageDto(obj) ?: return@openWebSocket
                        if (dto.threadId != tid) return@openWebSocket
                        val item = UiChatMessage(id = dto.id, senderRole = dto.senderRole, senderId = dto.senderId, content = dto.content)
                        mainHandler.post {
                            messages =
                                messages.let { current ->
                                    if (current.any { it.id == item.id }) current else (current + item)
                                }
                        }
                    }
                },
                onFailure = { t ->
                    mainHandler.post {
                        error = t.message ?: "WebSocket 连接失败"
                    }
                },
            )
        ws = socket
        socket.send("""{"type":"subscribe","threadId":$tid}""")

        onDispose {
            ws = null
            socket.close(1000, "bye")
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    ScreenScaffold(
        title = "私聊",
        contentPadding = contentPadding,
        loading = loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AppOutlinedButton(onClick = onBack) { Text("返回") }
            }

            error?.let {
                AppEmptyStateCard(title = "加载失败", description = it)
                return@ScreenScaffold
            }

            if (senderRole == null || senderId == null) {
                AppEmptyStateCard(title = "无法进入私聊", description = "请使用家长或老师账号登录后再试。")
                return@ScreenScaffold
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.senderRole == senderRole && msg.senderId == senderId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(
                                        if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = msg.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            val tid = threadId
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AppOutlinedField(
                    value = input,
                    onValueChange = { input = it },
                    label = "输入消息",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                )
                AppPrimaryButton(
                    onClick = {
                        val currentThreadId = tid ?: return@AppPrimaryButton
                        val text = input
                        focusManager.clearFocus()
                        if (text.isBlank()) return@AppPrimaryButton
                        coroutineScope.launch {
                            val ok = ws?.send("""{"type":"message","threadId":$currentThreadId,"content":${com.google.gson.Gson().toJson(text)}}""") ?: false
                            if (ok) input = "" else error = "发送失败"
                        }
                    },
                    enabled = tid != null,
                ) { Text("发送") }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ApplicationCard(
    app: ApplicationEntity,
    onPay: (Int) -> Unit,
    onChat: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = "申请编号：${app.id}")
            Text(text = "老师：${maskPhone(app.teacherPhone)}")
            Text(text = "学生：${app.studentName}（${app.studentGrade}）")
            Text(text = "申请状态：${zhApplicationStatus(app.status)}")
            Text(text = "支付状态：${zhPaymentStatus(app.paymentStatus)}")
            Text(text = "上课时间：${formatTime(app.scheduledAt)}")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppOutlinedButton(onClick = { onChat(app.id) }) { Text("私聊") }
                if (app.paymentStatus == "UNPAID") {
                    AppPrimaryButton(onClick = { onPay(app.id) }) { Text("去支付") }
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(
    contentPadding: PaddingValues,
    applicationId: Int,
    appRepository: AppRepository,
    parentViewModel: ParentViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by parentViewModel.uiState.collectAsStateWithLifecycle()
    var application by remember { mutableStateOf<ApplicationEntity?>(null) }
    var teacher by remember { mutableStateOf<TeacherEntity?>(null) }
    var paymentStatusText by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(applicationId, uiState.message) {
        loading = true
        loadError = null
        runCatching {
            application = appRepository.getApplicationById(applicationId)
            val app = application
            if (app != null) {
                teacher = appRepository.getTeacherById(app.teacherId)
                val payment = appRepository.getPaymentByApplicationId(app.id)
                paymentStatusText = payment?.status
            }
        }.onFailure { loadError = it.message ?: "加载失败" }
        loading = false
    }

    ScreenScaffold(
        title = "模拟支付",
        contentPadding = contentPadding,
        loading = uiState.loading || loading,
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            loadError?.let { EmptyStateCard(title = "加载失败", description = it) }
            val app = application
            if (app == null) {
                if (loadError == null) EmptyStateCard(title = "申请不存在", description = "请返回上一页重新选择。")
                return@ScreenScaffold
            }
            val t = teacher
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "申请编号：${app.id}")
                    Text(text = "家长手机号：${app.parentPhone}")
                    Text(text = "老师手机号：${app.teacherPhone}")
                    Text(text = "老师信息：${t?.subjects ?: "-"} / ${t?.grades ?: "-"}")
                    Text(text = "支付金额：${t?.pricePerHour ?: 0.0}")
                    Text(
                        text = "支付状态：${
                            zhPaymentStatus(app.paymentStatus)
                        }${
                            paymentStatusText?.let { "（记录：${zhPaymentStatus(it)}）" } ?: ""
                        }",
                    )
                }
            }

            AppPrimaryButton(
                onClick = { parentViewModel.pay(app.id) },
                enabled = !uiState.loading && app.paymentStatus != "PAID",
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.loading) "支付中..." else "确认支付")
            }

            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { parentViewModel.clearMessage() },
                )
            }
        }
    }
}

@Composable
fun ParentProfileScreen(
    contentPadding: PaddingValues,
    appRepository: AppRepository,
    parentId: Int,
    parentViewModel: ParentViewModel,
    title: String = "我的信息",
    onProfileCompleted: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by parentViewModel.uiState.collectAsStateWithLifecycle()
    var parent by remember { mutableStateOf<ParentEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()

    var password by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var poiName by remember { mutableStateOf<String?>(null) }
    var poiAddress by remember { mutableStateOf("") }
    var addressDetail by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }
    var studentGender by remember { mutableStateOf("") }
    var studentGrade by remember { mutableStateOf("") }
    var weakSubjectsSelected by remember { mutableStateOf(setOf<String>()) }
    var weakArtDetail by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(parentId) {
        val entity = appRepository.getParentById(parentId)
        parent = entity
        password = ""
        latitude = entity?.latitude
        longitude = entity?.longitude
        poiName = entity?.poiName
        poiAddress = entity?.poiAddress ?: entity?.address.orEmpty()
        addressDetail = entity?.addressDetail.orEmpty()
        studentName = entity?.studentName.orEmpty()
        studentGender = entity?.studentGender.orEmpty()
        studentGrade = entity?.studentGrade.orEmpty()
        val parsed = parseSubjectSelection(entity?.weakSubjects.orEmpty())
        weakSubjectsSelected = parsed.first
        weakArtDetail = parsed.second
    }

    val focusManager = LocalFocusManager.current
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
            val p = parent
            if (p == null) {
                Text(text = "未找到家长信息", style = MaterialTheme.typography.bodyMedium)
                return@ScreenScaffold
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "账号信息", style = MaterialTheme.typography.titleSmall)
                    Text(text = "手机号：${p.phone}", style = MaterialTheme.typography.bodyMedium)
                    AppOutlinedField(
                        value = password,
                        onValueChange = { password = it },
                        label = "修改密码（可选）",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "学生信息", style = MaterialTheme.typography.titleSmall)
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider()
                    AppOutlinedField(
                        value = studentName,
                        onValueChange = { studentName = it },
                        label = "学生姓名",
                        isError = submitted && studentName.isBlank(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    SingleSelectDropdown(
                        label = "学生性别",
                        options = profileGenderOptions,
                        value = studentGender,
                        onSelect = { studentGender = it },
                    )
                    SingleSelectDropdown(
                        label = "学生年级",
                        options = profileGradeOptions,
                        value = studentGrade,
                        onSelect = { studentGrade = it },
                    )
                    HorizontalDivider()
                    MultiSelectDropdown(
                        label = "薄弱科目（可多选）",
                        options = profileSubjectOptions,
                        selected = weakSubjectsSelected,
                        onToggle = { option ->
                            val next =
                                if (weakSubjectsSelected.contains(option)) weakSubjectsSelected - option else weakSubjectsSelected + option
                            weakSubjectsSelected = next
                            if (!next.contains("艺术")) weakArtDetail = ""
                        },
                        onClear = {
                            weakSubjectsSelected = emptySet()
                            weakArtDetail = ""
                        },
                    )
                    SelectedChipsRow(
                        selected = weakSubjectsSelected,
                        onRemove = { weakSubjectsSelected = weakSubjectsSelected - it },
                    )
                    if (weakSubjectsSelected.contains("艺术")) {
                        AppOutlinedField(
                            value = weakArtDetail,
                            onValueChange = { weakArtDetail = it },
                            label = "艺术具体内容",
                            isError = submitted && weakArtDetail.isBlank(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    val weakOk = weakSubjectsSelected.isNotEmpty() && (!weakSubjectsSelected.contains("艺术") || weakArtDetail.isNotBlank())
                    val weakSubjectsValue = if (weakOk) subjectSelectionToString(weakSubjectsSelected, weakArtDetail) else ""
                    if (!isValidParentProfile(
                            address = buildAddressText(poiName, poiAddress, addressDetail),
                            studentName = studentName.trim(),
                            studentGender = studentGender.trim(),
                            studentGrade = studentGrade.trim(),
                            weakSubjects = weakSubjectsValue,
                        )
                    ) {
                        parentViewModel.setMessage("请完善必填信息")
                        return@AppPrimaryButton
                    }
                    val updated = p.copy(
                        password = password.takeIf { it.isNotBlank() } ?: p.password,
                        address = buildAddressText(poiName, poiAddress, addressDetail),
                        latitude = latitude,
                        longitude = longitude,
                        poiName = poiName,
                        poiAddress = poiAddress.trim().takeIf { it.isNotBlank() },
                        addressDetail = addressDetail.trim(),
                        studentName = studentName.trim(),
                        studentGender = studentGender.trim(),
                        studentGrade = studentGrade.trim(),
                        weakSubjects = weakSubjectsValue,
                    )
                    coroutineScope.launch {
                        appRepository.updateParent(updated)
                        parent = updated
                        password = ""
                        parentViewModel.setMessage("保存成功")
                        if (title == "完善资料") {
                            onProfileCompleted?.invoke()
                        }
                    }
                },
                enabled = run {
                    val weakOk = weakSubjectsSelected.isNotEmpty() && (!weakSubjectsSelected.contains("艺术") || weakArtDetail.isNotBlank())
                    val weakSubjectsValue = if (weakOk) subjectSelectionToString(weakSubjectsSelected, weakArtDetail) else ""
                    isValidParentProfile(buildAddressText(poiName, poiAddress, addressDetail), studentName.trim(), studentGender.trim(), studentGrade.trim(), weakSubjectsValue)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("保存修改")
            }
            uiState.message?.let { msg ->
                MessageCard(
                    message = msg,
                    isError = uiState.messageIsError,
                    onDismiss = { parentViewModel.clearMessage() },
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
