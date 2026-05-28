package com.example.teacher.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.ContextCompat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.teacher.BuildConfig
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.data.BackendApi
import com.example.teacher.ui.components.AppCard
import com.example.teacher.ui.components.AppMessageCard
import com.example.teacher.ui.components.AppOutlinedButton
import com.example.teacher.ui.components.AppOutlinedField
import com.example.teacher.ui.components.AppPrimaryButton
import com.example.teacher.ui.components.AppScaffold
import com.example.teacher.ui.components.AppTonalButton
import com.example.teacher.ui.components.AppTextButton
import com.example.teacher.ui.viewmodel.AuthViewModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.geocoder.GeocodeSearch
import com.amap.api.services.geocoder.RegeocodeQuery
import com.amap.api.services.geocoder.RegeocodeResult
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.core.PoiItem
import com.amap.api.services.poisearch.PoiResult

@Composable
private fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = modifier)
}

private fun isValidPhone(phone: String): Boolean = Regex("^1\\d{10}$").matches(phone.trim())

private fun isValidPassword(password: String): Boolean = password.length >= 6

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

private val genderOptions = listOf("男", "女")

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
private fun AppIcon(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val drawable: Drawable? = remember {
        runCatching { context.packageManager.getApplicationIcon(context.packageName) }.getOrNull()
    }
    val bitmap = remember(drawable) {
        when (drawable) {
            is BitmapDrawable -> drawable.bitmap
            null -> null
            else -> runCatching {
                drawable.toBitmap(width = 256, height = 256, config = Bitmap.Config.ARGB_8888)
            }.getOrElse {
                runCatching {
                    Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888).also { b ->
                        val canvas = Canvas(b)
                        drawable.setBounds(0, 0, 256, 256)
                        drawable.draw(canvas)
                    }
                }.getOrNull()
            }
        }
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
            )
        } else {
            Box(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun BrandHeader(
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppIcon(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(text = headline, style = MaterialTheme.typography.titleLarge)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LoginRequiredScreen(
    contentPadding: PaddingValues,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    onContinueBrowsing: (() -> Unit)? = null,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(contentPadding),
    ) {
        AlertDialog(
            onDismissRequest = { onContinueBrowsing?.invoke() },
            title = { Text("需要登录") },
            text = {
                Text(
                    text = "当前为游客模式。你可以继续浏览主页与搜索内容，但购买/加入购物车/提交申请等操作需要先登录。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                AppPrimaryButton(onClick = onGoToLogin) { Text("去登录 / 注册") }
            },
            dismissButton = {
                if (onContinueBrowsing != null) {
                    AppOutlinedButton(onClick = onContinueBrowsing) { Text("继续浏览") }
                }
            },
        )
    }
}

@Composable
fun ProfileRequiredScreen(
    contentPadding: PaddingValues,
    onGoToProfile: () -> Unit,
    onContinueBrowsing: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(contentPadding),
    ) {
        AlertDialog(
            onDismissRequest = onContinueBrowsing,
            title = { Text("需要完善资料") },
            text = {
                Text(
                    text = "当前为受限模式（仍保持登录）。你可以继续浏览老师与商品，但提交申请、下单购买、查看申请/订单等功能需要先完善资料。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                AppPrimaryButton(onClick = onGoToProfile) { Text("去完善资料") }
            },
            dismissButton = {
                AppOutlinedButton(onClick = onContinueBrowsing) { Text("继续浏览") }
            },
        )
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
fun WelcomeScreen(
    contentPadding: PaddingValues,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "老师来了", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AppIcon(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                            Text(text = "老师来了", style = MaterialTheme.typography.titleLarge)
                            Text(text = "家教平台 · 找老师 · 选课程 · 买课辅资料", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    HorizontalDivider()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "服务内容", style = MaterialTheme.typography.titleSmall)
                        Text(text = "· 老师浏览与搜索筛选", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "· 商品（课程/课辅资料）浏览与购买", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "· 订单/购物车管理", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AppPrimaryButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text("注册") }
            AppTonalButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("登录") }
            AppOutlinedButton(onClick = onGuest, modifier = Modifier.fillMaxWidth()) { Text("游客访问") }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    contentPadding: PaddingValues,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("laoshilaile_prefs", Context.MODE_PRIVATE) }
    val done = prefs.getBoolean("onboarding_done", false)
    if (done) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    @Composable
    fun FeatureRow(text: String) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }

    @Composable
    fun GuidePage(
        icon: ImageVector,
        title: String,
        subtitle: String,
        items: List<String>,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        Icon(imageVector = icon, contentDescription = null)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { FeatureRow(it) }
            }
        }
    }

    fun markDone(action: () -> Unit) {
        prefs.edit().putBoolean("onboarding_done", true).apply()
        action()
    }

    var introStart by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val introProgress by animateFloatAsState(
        targetValue = if (introStart) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "introProgress",
    )

    LaunchedEffect(Unit) {
        delay(120)
        introStart = true
        delay(920)
        showContent = true
    }

    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(showContent) {
        if (showContent) {
            pagerState.scrollToPage(0)
        }
    }

    val bg = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
        ),
    )

    Scaffold(modifier = modifier, containerColor = Color.Transparent) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(innerPadding)
                .background(bg),
        ) {
            val compact = maxHeight < 720.dp
            val scale = 1f - (0.18f * introProgress)
            val cardMaxHeight = maxHeight * (if (compact) 0.48f else 0.50f)
            val headerTopPadding = (if (compact) 72.dp else 110.dp) * (1f - introProgress)

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 70.dp, y = (-70).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)),
                )
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (-60).dp, y = 40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)),
                )

                val header: @Composable (Modifier) -> Unit = { headerModifier ->
                    Column(
                        modifier = headerModifier,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
                    ) {
                        AppIcon(
                            modifier = Modifier
                                .size(if (compact) 104.dp else 132.dp)
                                .clip(CircleShape),
                        )
                        Text(
                            text = "老师来了",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = if (compact) 30.sp else 34.sp,
                                lineHeight = if (compact) 34.sp else 38.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 3.sp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                ),
                                shadow = Shadow(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                                    offset = Offset(0f, 6f),
                                    blurRadius = 14f,
                                ),
                            ),
                        )
                        Text(
                            text = "珍惜每一份教育资源，\n让每一颗求知的心被看见。\n拾阶前行，我们一起！",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = if (compact) 16.sp else 18.sp,
                                lineHeight = if (compact) 22.sp else 26.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                            textAlign = TextAlign.Center,
                            maxLines = if (compact) 2 else 3,
                        )
                    }
                }

                if (!showContent) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        header(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
                    }
                } else {
                    val scrollModifier =
                        if (compact) {
                            Modifier.verticalScroll(rememberScrollState())
                        } else {
                            Modifier
                        }

                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp).then(scrollModifier),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        header(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = headerTopPadding)
                                .graphicsLayer(scaleX = scale, scaleY = scale),
                        )

                        if (!compact) {
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = if (compact) 240.dp else 260.dp, max = cardMaxHeight),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                ) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f, fill = true),
                                        userScrollEnabled = true,
                                    ) { page ->
                                        when (page) {
                                            0 -> GuidePage(
                                                icon = Icons.Filled.Person,
                                                title = "家长用户指南",
                                                subtitle = "快速找到适合孩子的家教老师",
                                                items = listOf(
                                                    "查看家教老师信息",
                                                    "按位置、科目、年级等条件筛选老师",
                                                    "查看老师教学经历和联系方式",
                                                    "提交家教需求，更方便找到合适老师",
                                                ),
                                            )
                                            1 -> GuidePage(
                                                icon = Icons.Filled.Badge,
                                                title = "老师用户指南",
                                                subtitle = "展示教学能力，连接更多家长需求",
                                                items = listOf(
                                                    "注册并完善个人教学资料",
                                                    "填写教学经历、就职情况、微信号等信息",
                                                    "展示自己的教学优势",
                                                    "等待管理员审核，被家长搜索选择",
                                                ),
                                            )
                                            2 -> GuidePage(
                                                icon = Icons.Filled.AdminPanelSettings,
                                                title = "管理员指南",
                                                subtitle = "高效维护平台用户与申请信息",
                                                items = listOf(
                                                    "查看用户和老师信息",
                                                    "管理老师申请记录",
                                                    "审核老师资料",
                                                    "维护平台基础数据与秩序",
                                                ),
                                            )
                                            else -> GuidePage(
                                                icon = Icons.Filled.CheckCircle,
                                                title = "开始使用老师来了",
                                                subtitle = "根据你的身份选择入口，快速进入平台",
                                                items = listOf(
                                                    "登录：进入完整功能",
                                                    "注册：创建账号并完善资料",
                                                    "游客：先浏览内容，操作时再登录",
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                val current = pagerState.currentPage
                                repeat(4) { index ->
                                    val selected = index == current
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (selected) 10.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            ),
                                    )
                                }
                            }

                            if (pagerState.currentPage == 3) {
                                AppPrimaryButton(onClick = { markDone(onRegister) }, modifier = Modifier.fillMaxWidth()) { Text("注册") }
                                AppTonalButton(onClick = { markDone(onLogin) }, modifier = Modifier.fillMaxWidth()) { Text("登录") }
                                AppOutlinedButton(onClick = { markDone(onGuest) }, modifier = Modifier.fillMaxWidth()) { Text("以游客身份访问") }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    AppOutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0))
                                            }
                                        },
                                        enabled = pagerState.currentPage > 0,
                                        modifier = Modifier.weight(1f),
                                    ) { Text("上一页") }
                                    AppTonalButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(3))
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    ) { Text("下一页") }
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
fun RoleSelectionScreen(
    contentPadding: PaddingValues,
    title: String,
    subtitle: String,
    parentActionText: String,
    teacherActionText: String,
    adminActionText: String,
    onParent: () -> Unit,
    onTeacher: () -> Unit,
    onAdmin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(title = "选择身份", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandHeader(headline = title, subtitle = subtitle)
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "家长", style = MaterialTheme.typography.titleSmall)
                    Text(text = "浏览、筛选老师并提交申请", style = MaterialTheme.typography.bodyMedium)
                    AppPrimaryButton(onClick = onParent, modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.Person, contentDescription = null)
                            Text(parentActionText)
                        }
                    }
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "老师", style = MaterialTheme.typography.titleSmall)
                    Text(text = "填写资料、处理家长申请", style = MaterialTheme.typography.bodyMedium)
                    AppPrimaryButton(onClick = onTeacher, modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.Badge, contentDescription = null)
                            Text(teacherActionText)
                        }
                    }
                }
            }
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "管理员", style = MaterialTheme.typography.titleSmall)
                    Text(text = "审核老师、管理申请与支付", style = MaterialTheme.typography.bodyMedium)
                    AppPrimaryButton(onClick = onAdmin, modifier = Modifier.fillMaxWidth()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Filled.AdminPanelSettings, contentDescription = null)
                            Text(adminActionText)
                        }
                    }
                }
            }
            AppOutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("返回") }
        }
    }
}

@Composable
fun LoginTabsScreen(
    contentPadding: PaddingValues,
    authViewModel: AuthViewModel,
    adminPassword: String,
    initialRole: String,
    onParentSuccess: (ParentEntity, String) -> Unit,
    onTeacherSuccess: (TeacherEntity, String) -> Unit,
    onAdminSuccess: () -> Unit,
    onGoRegister: () -> Unit,
    onGuest: () -> Unit,
    onBackToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var serverUrl by remember { mutableStateOf("") }
    var serverSavedToast by remember { mutableStateOf(false) }
    var serverTestResult by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        BackendApi.init(context)
        serverUrl = BackendApi.getBaseUrl()
    }

    var selectedTab by remember(initialRole) {
        mutableStateOf(
            when (initialRole.lowercase()) {
                "teacher" -> 1
                "admin" -> 2
                else -> 0
            },
        )
    }

    var parentPhone by remember { mutableStateOf("") }
    var parentPassword by remember { mutableStateOf("") }
    var parentPwdVisible by remember { mutableStateOf(false) }
    var parentSubmitted by remember { mutableStateOf(false) }

    var teacherPhone by remember { mutableStateOf("") }
    var teacherPassword by remember { mutableStateOf("") }
    var teacherPwdVisible by remember { mutableStateOf(false) }
    var teacherSubmitted by remember { mutableStateOf(false) }

    var adminInput by remember { mutableStateOf("") }
    var adminPwdVisible by remember { mutableStateOf(false) }
    var adminMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTab) {
        authViewModel.clearMessage()
        adminMessage = null
    }

    ScreenScaffold(title = "登录", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        val bg = Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(bg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                BrandHeader(headline = "老师来了", subtitle = "欢迎回来 · 请选择身份并登录")

                if (BuildConfig.DEBUG) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(text = "调试服务器地址", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "真机不能使用 10.0.2.2，请填电脑局域网 IP（例：http://192.168.1.23:8081/）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppOutlinedField(
                                value = serverUrl,
                                onValueChange = { serverUrl = it },
                                label = "BACKEND_BASE_URL",
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                AppPrimaryButton(
                                    onClick = {
                                        BackendApi.setBaseUrlOverride(context, serverUrl)
                                        serverUrl = BackendApi.getBaseUrl()
                                        serverSavedToast = true
                                        serverTestResult = null
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("保存") }
                                AppOutlinedButton(
                                    onClick = {
                                        BackendApi.setBaseUrlOverride(context, "")
                                        serverUrl = BackendApi.getBaseUrl()
                                        serverSavedToast = true
                                        serverTestResult = null
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("恢复默认") }
                            }
                            AppOutlinedButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    serverTestResult = "测试中…" to false
                                    coroutineScope.launch {
                                        val result =
                                            runCatching { BackendApi.health() }
                                                .fold(
                                                    onSuccess = { "连接成功：${BackendApi.getBaseUrl()}" to false },
                                                    onFailure = { t ->
                                                        val msg = t.message?.takeIf { it.isNotBlank() } ?: t.javaClass.simpleName
                                                        "连接失败：$msg\n${BackendApi.getBaseUrl()}" to true
                                                    },
                                                )
                                        serverTestResult = result
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("测试连接（/health）") }
                            if (serverSavedToast) {
                                MessageCard(
                                    message = "已切换为：${BackendApi.getBaseUrl()}",
                                    isError = false,
                                    onDismiss = { serverSavedToast = false },
                                )
                            }
                            serverTestResult?.let { (msg, isError) ->
                                MessageCard(message = msg, isError = isError, onDismiss = { serverTestResult = null })
                            }
                        }
                    }
                }

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("家长") })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("老师") })
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("管理员") })
                    }

                    when (selectedTab) {
                        0 -> {
                            val phoneOk = isValidPhone(parentPhone)
                            val passwordOk = parentPassword.isNotBlank()
                            AppOutlinedField(
                                value = parentPhone,
                                onValueChange = { parentPhone = it },
                                label = "手机号",
                                isError = parentSubmitted && !phoneOk,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (parentSubmitted && !phoneOk) ErrorText(text = "请输入11位手机号")
                            AppOutlinedField(
                                value = parentPassword,
                                onValueChange = { parentPassword = it },
                                label = "密码",
                                isError = parentSubmitted && !passwordOk,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = if (parentPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { parentPwdVisible = !parentPwdVisible }) {
                                        Icon(
                                            imageVector = if (parentPwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (parentSubmitted && !passwordOk) ErrorText(text = "请输入密码")
                            AppPrimaryButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    parentSubmitted = true
                                    authViewModel.parentLogin(parentPhone, parentPassword, onParentSuccess)
                                },
                                enabled = !uiState.loading && phoneOk && passwordOk,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(if (uiState.loading) "登录中..." else "登录") }
                        }
                        1 -> {
                            val phoneOk = isValidPhone(teacherPhone)
                            val passwordOk = teacherPassword.isNotBlank()
                            AppOutlinedField(
                                value = teacherPhone,
                                onValueChange = { teacherPhone = it },
                                label = "手机号",
                                isError = teacherSubmitted && !phoneOk,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (teacherSubmitted && !phoneOk) ErrorText(text = "请输入11位手机号")
                            AppOutlinedField(
                                value = teacherPassword,
                                onValueChange = { teacherPassword = it },
                                label = "密码",
                                isError = teacherSubmitted && !passwordOk,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = if (teacherPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { teacherPwdVisible = !teacherPwdVisible }) {
                                        Icon(
                                            imageVector = if (teacherPwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (teacherSubmitted && !passwordOk) ErrorText(text = "请输入密码")
                            AppPrimaryButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    teacherSubmitted = true
                                    authViewModel.teacherLogin(teacherPhone, teacherPassword, onTeacherSuccess)
                                },
                                enabled = !uiState.loading && phoneOk && passwordOk,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(if (uiState.loading) "登录中..." else "登录") }
                        }
                        else -> {
                            val ok = adminInput.isNotBlank()
                            AppOutlinedField(
                                value = adminInput,
                                onValueChange = { adminInput = it },
                                label = "管理员密码",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                visualTransformation = if (adminPwdVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { adminPwdVisible = !adminPwdVisible }) {
                                        Icon(
                                            imageVector = if (adminPwdVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            AppPrimaryButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (adminInput == adminPassword) {
                                        onAdminSuccess()
                                    } else {
                                        adminMessage = "管理员密码错误"
                                    }
                                },
                                enabled = ok,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("登录") }
                            adminMessage?.let { msg ->
                                MessageCard(message = msg, isError = true, onDismiss = { adminMessage = null })
                            }
                        }
                    }

                    uiState.message?.let { msg ->
                        MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { authViewModel.clearMessage() })
                    }
                }
            }

                AppTonalButton(onClick = onGoRegister, modifier = Modifier.fillMaxWidth()) { Text("没有账号？去注册") }
                AppOutlinedButton(onClick = onGuest, modifier = Modifier.fillMaxWidth()) { Text("以游客身份访问") }
                AppOutlinedButton(onClick = onBackToOnboarding, modifier = Modifier.fillMaxWidth()) { Text("返回引导页") }
                Text(
                    text = "家长、老师和管理员的家教服务管理平台",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ParentLoginScreen(
    contentPadding: PaddingValues,
    authViewModel: AuthViewModel,
    onLoginSuccess: (ParentEntity, String) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var submitted by remember { mutableStateOf(false) }
    val phoneOk = isValidPhone(phone)
    val passwordOk = password.isNotBlank()

    ScreenScaffold(title = "家长登录", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandHeader(headline = "家长登录", subtitle = "登录后可提交申请、购买商品、查看订单")
            AppOutlinedField(
                value = phone,
                onValueChange = { phone = it },
                label = "手机号",
                isError = submitted && !phoneOk,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            if (submitted && !phoneOk) ErrorText(text = "请输入11位手机号")
            AppOutlinedField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                isError = submitted && !passwordOk,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (submitted && !passwordOk) ErrorText(text = "请输入密码")
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    authViewModel.parentLogin(phone, password, onLoginSuccess)
                },
                enabled = !uiState.loading && phoneOk && passwordOk,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.loading) "登录中..." else "登录")
            }
            AppTonalButton(
                onClick = onRegister,
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("没有账号？去注册")
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { authViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun ParentRegisterScreen(
    contentPadding: PaddingValues,
    authViewModel: AuthViewModel,
    onRegisterSuccess: (ParentEntity, String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var submitted by remember { mutableStateOf(false) }
    val phoneOk = isValidPhone(phone)
    val passwordOk = isValidPassword(password)
    val requiredOk = true

    ScreenScaffold(title = "家长注册", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "创建账号后需先完善资料，才能使用完整功能", style = MaterialTheme.typography.titleMedium)
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "账号信息", style = MaterialTheme.typography.titleSmall)
                    AppOutlinedField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "手机号",
                        isError = submitted && !phoneOk,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (submitted && !phoneOk) ErrorText(text = "请输入11位手机号")
                    AppOutlinedField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        isError = submitted && !passwordOk,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (submitted && !passwordOk) ErrorText(text = "密码至少6位")
                }
            }
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    authViewModel.parentRegister(
                        phone = phone,
                        password = password,
                        onSuccess = onRegisterSuccess,
                    )
                },
                enabled = !uiState.loading && phoneOk && passwordOk && requiredOk,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.loading) "注册中..." else "注册并进入")
            }
            AppTonalButton(
                onClick = onBackToLogin,
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("已有账号？返回登录")
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { authViewModel.clearMessage() })
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TeacherLoginScreen(
    contentPadding: PaddingValues,
    authViewModel: AuthViewModel,
    onLoginSuccess: (TeacherEntity, String) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var submitted by remember { mutableStateOf(false) }
    val phoneOk = isValidPhone(phone)
    val passwordOk = password.isNotBlank()

    ScreenScaffold(title = "老师登录", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandHeader(headline = "老师登录", subtitle = "登录后可处理申请并维护资料")
            AppOutlinedField(
                value = phone,
                onValueChange = { phone = it },
                label = "手机号",
                isError = submitted && !phoneOk,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            if (submitted && !phoneOk) ErrorText(text = "请输入11位手机号")
            AppOutlinedField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                isError = submitted && !passwordOk,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (submitted && !passwordOk) ErrorText(text = "请输入密码")
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    authViewModel.teacherLogin(phone, password, onLoginSuccess)
                },
                enabled = !uiState.loading && phoneOk && passwordOk,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.loading) "登录中..." else "登录")
            }
            AppTonalButton(
                onClick = onRegister,
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("没有账号？去注册")
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { authViewModel.clearMessage() })
            }
        }
    }
}

@Composable
fun TeacherRegisterScreen(
    contentPadding: PaddingValues,
    authViewModel: AuthViewModel,
    onRegisterSuccess: (TeacherEntity, String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var submitted by remember { mutableStateOf(false) }
    val phoneOk = isValidPhone(phone)
    val passwordOk = isValidPassword(password)
    val requiredOk = true

    ScreenScaffold(title = "老师注册", contentPadding = contentPadding, loading = uiState.loading, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "创建账号后需先完善资料，才能使用完整功能", style = MaterialTheme.typography.titleMedium)
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "账号信息", style = MaterialTheme.typography.titleSmall)
                    AppOutlinedField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "手机号",
                        isError = submitted && !phoneOk,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (submitted && !phoneOk) ErrorText(text = "请输入11位手机号")
                    AppOutlinedField(
                        value = password,
                        onValueChange = { password = it },
                        label = "密码",
                        isError = submitted && !passwordOk,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (submitted && !passwordOk) ErrorText(text = "密码至少6位")
                }
            }
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    submitted = true
                    authViewModel.teacherRegister(
                        phone = phone,
                        password = password,
                        onSuccess = onRegisterSuccess,
                    )
                },
                enabled = !uiState.loading && phoneOk && passwordOk && requiredOk,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.loading) "注册中..." else "注册并提交审核")
            }
            AppTonalButton(
                onClick = onBackToLogin,
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("已有账号？返回登录")
            }
            uiState.message?.let { msg ->
                MessageCard(message = msg, isError = uiState.messageIsError, onDismiss = { authViewModel.clearMessage() })
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AdminLoginScreen(
    contentPadding: PaddingValues,
    adminPassword: String,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    ScreenScaffold(title = "管理员登录", contentPadding = contentPadding, modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BrandHeader(headline = "管理员登录", subtitle = "后台管理：审核老师 / 管理商品与申请")
            AppOutlinedField(
                value = input,
                onValueChange = { input = it },
                label = "管理员密码",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            AppPrimaryButton(
                onClick = {
                    focusManager.clearFocus()
                    if (input == adminPassword) onSuccess() else message = "管理员密码错误"
                },
                enabled = input.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("登录")
            }
            message?.let { msg ->
                MessageCard(message = msg, isError = true, onDismiss = { message = null })
            }
        }
    }
}

data class PickedLocation(
    val latitude: Double,
    val longitude: Double,
    val poiName: String? = null,
    val poiAddress: String,
)

fun buildAddressText(
    poiName: String?,
    poiAddress: String?,
    detail: String,
): String {
    val base =
        listOfNotNull(
            poiName?.trim()?.takeIf { it.isNotBlank() && it != poiAddress?.trim() },
            poiAddress?.trim()?.takeIf { it.isNotBlank() },
        ).joinToString(" ")
    val detailTrimmed = detail.trim()
    return listOfNotNull(base.takeIf { it.isNotBlank() }, detailTrimmed.takeIf { it.isNotBlank() }).joinToString(" ")
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onDismiss: () -> Unit,
    onConfirm: (PickedLocation) -> Unit,
) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    remember {
        runCatching {
            MapsInitializer.updatePrivacyShow(context, true, true)
            MapsInitializer.updatePrivacyAgree(context, true)
            AMapLocationClient.updatePrivacyShow(context, true, true)
            AMapLocationClient.updatePrivacyAgree(context, true)
        }
        Unit
    }

    var center by remember {
        mutableStateOf(
            LatLng(
                initialLatitude ?: 39.9087,
                initialLongitude ?: 116.3975,
            ),
        )
    }
    var poiAddress by remember { mutableStateOf("") }
    var poiName by remember { mutableStateOf<String?>(null) }
    var canConfirm by remember { mutableStateOf(false) }
    var keyword by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<PoiItem>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    var geocodeError by remember { mutableStateOf<String?>(null) }
    var aMapRef by remember { mutableStateOf<com.amap.api.maps.AMap?>(null) }

    val geocodeSearch = remember { GeocodeSearch(context) }
    var geocodeVersion by remember { mutableLongStateOf(0L) }

    DisposableEffect(Unit) {
        val listener =
            object : GeocodeSearch.OnGeocodeSearchListener {
                override fun onGeocodeSearched(result: com.amap.api.services.geocoder.GeocodeResult?, rCode: Int) = Unit

                override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
                    if (rCode != 1000) {
                        mainHandler.post { geocodeError = "逆地理编码失败（$rCode）" }
                        return
                    }
                    val addr = result?.regeocodeAddress?.formatAddress?.trim().orEmpty()
                    mainHandler.post {
                        geocodeError = null
                        if (addr.isNotBlank()) {
                            poiAddress = addr
                            canConfirm = true
                        } else {
                            poiAddress = ""
                            canConfirm = false
                        }
                    }
                }
            }
        geocodeSearch.setOnGeocodeSearchListener(listener)
        onDispose {
            geocodeSearch.setOnGeocodeSearchListener(null)
        }
    }

    LaunchedEffect(center, geocodeVersion) {
        delay(350)
        val query = RegeocodeQuery(LatLonPoint(center.latitude, center.longitude), 200f, GeocodeSearch.AMAP)
        runCatching { geocodeSearch.getFromLocationAsyn(query) }
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun moveCameraTo(latLng: LatLng) {
        runCatching { aMapRef?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f)) }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(RequestMultiplePermissions()) { granted ->
            if (granted.values.any { it }) {
                locating = true
                locationError = null
                startOnceLocation(
                    context = context.applicationContext,
                    mainHandler = mainHandler,
                    onSuccess = { latLng ->
                        center = latLng
                        geocodeVersion = System.currentTimeMillis()
                        locating = false
                        locationError = null
                        moveCameraTo(latLng)
                    },
                    onError = { msg ->
                        locating = false
                        locationError = msg
                    },
                )
            } else {
                locating = false
                locationError = "未授予定位权限"
            }
        }

    fun doSearch() {
        val q = keyword.trim()
        if (q.isBlank()) {
            searchResults = emptyList()
            searchError = null
            return
        }
        searching = true
        searchError = null
        val query = PoiSearch.Query(q, "", "")
        query.pageNum = 0
        query.pageSize = 20
        val search = PoiSearch(context, query)
        search.bound = PoiSearch.SearchBound(LatLonPoint(center.latitude, center.longitude), 3000, true)
        search.setOnPoiSearchListener(
            object : PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                    mainHandler.post {
                        searching = false
                        if (rCode != 1000) {
                            searchResults = emptyList()
                            searchError = "搜索失败（$rCode）"
                            return@post
                        }
                        searchResults = result?.pois ?: emptyList()
                        if (searchResults.isEmpty()) searchError = "没有找到结果"
                    }
                }

                override fun onPoiItemSearched(item: PoiItem?, rCode: Int) = Unit
            },
        )
        runCatching { search.searchPOIAsyn() }.onFailure {
            mainHandler.post {
                searching = false
                searchResults = emptyList()
                searchError = it.message ?: "搜索失败"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                CenterAlignedTopAppBar(
                    title = { Text("选择位置") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(imageVector = Icons.Filled.Close, contentDescription = null) }
                    },
                )

                AppOutlinedField(
                    value = keyword,
                    onValueChange = {
                        keyword = it
                        if (it.trim().isBlank()) {
                            searchResults = emptyList()
                            searchError = null
                        }
                    },
                    label = "搜索小区 / 地标 / 学校",
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        AppTextButton(
                            text = if (searching) "搜索中" else "搜索",
                            onClick = { doSearch() },
                            enabled = !searching,
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                )

                val mapViewResult = remember { runCatching { MapView(context) } }
                val mapView = mapViewResult.getOrNull()
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

                if (mapView == null) {
                    AppCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "地图初始化失败", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = mapViewResult.exceptionOrNull()?.message ?: "请检查高德 Key、包名与 SHA1 是否配置正确。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            AppPrimaryButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("返回") }
                        }
                    }
                    return@Column
                }

                DisposableEffect(mapView, lifecycleOwner) {
                    mapView.onCreate(null)
                    val observer =
                        LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                                else -> Unit
                            }
                        }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        runCatching { mapView.onPause() }
                        runCatching { mapView.onDestroy() }
                    }
                }

                val aMap = remember(mapView) { mapView.map }

                LaunchedEffect(aMap) {
                    runCatching {
                        aMap.uiSettings.isZoomControlsEnabled = false
                        aMap.uiSettings.isMyLocationButtonEnabled = false
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 16f))
                        aMapRef = aMap
                        aMap.setOnCameraChangeListener(
                            object : com.amap.api.maps.AMap.OnCameraChangeListener {
                                override fun onCameraChange(position: com.amap.api.maps.model.CameraPosition?) = Unit

                                override fun onCameraChangeFinish(position: com.amap.api.maps.model.CameraPosition?) {
                                    val target = position?.target ?: return
                                    center = target
                                    geocodeVersion = System.currentTimeMillis()
                                }
                            },
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
                    if (searchError != null || searchResults.isNotEmpty()) {
                        AppCard(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .align(Alignment.TopCenter),
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (searchError != null) {
                                    Text(text = searchError.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                                } else {
                                    LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                                        items(searchResults.take(12)) { item ->
                                            Column(
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            val p = item.latLonPoint
                                                            val next = LatLng(p.latitude, p.longitude)
                                                            poiName = item.title?.trim()?.takeIf { it.isNotBlank() }
                                                            val snippet = item.snippet?.trim().orEmpty()
                                                            if (snippet.isNotBlank()) {
                                                                poiAddress = snippet
                                                                canConfirm = true
                                                            }
                                                            center = next
                                                            geocodeVersion = System.currentTimeMillis()
                                                            keyword = item.title ?: keyword
                                                            searchResults = emptyList()
                                                            searchError = null
                                                            runCatching { aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(next, 16f)) }
                                                        }
                                                        .padding(vertical = 8.dp),
                                            ) {
                                                Text(text = item.title.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                                                val desc = item.snippet?.takeIf { it.isNotBlank() } ?: item.adName ?: ""
                                                if (desc.isNotBlank()) {
                                                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            HorizontalDivider()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(bottom = 28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(34.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "已选位置", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = poiAddress.ifBlank { "拖动地图选择位置" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (geocodeError != null) {
                                Text(text = geocodeError.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            if (locationError != null) {
                                Text(text = locationError.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        AppOutlinedButton(
                            onClick = {
                                if (!hasLocationPermission()) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                        ),
                                    )
                                } else {
                                    locating = true
                                    locationError = null
                                    startOnceLocation(
                                        context = context.applicationContext,
                                        mainHandler = mainHandler,
                                        onSuccess = { latLng ->
                                            center = latLng
                                            geocodeVersion = System.currentTimeMillis()
                                            locating = false
                                            locationError = null
                                            moveCameraTo(latLng)
                                        },
                                        onError = { msg ->
                                            locating = false
                                            locationError = msg
                                        },
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null)
                                Text(if (locating) "定位中..." else "定位到当前位置")
                            }
                        }
                        AppPrimaryButton(
                            onClick = {
                                onConfirm(
                                    PickedLocation(
                                        latitude = center.latitude,
                                        longitude = center.longitude,
                                        poiName = poiName,
                                        poiAddress = poiAddress,
                                    ),
                                )
                            },
                            enabled = canConfirm,
                            modifier = Modifier.weight(1f),
                        ) { Text("确认位置") }
                    }
                }
            }
        }
    }
}

private fun startOnceLocation(
    context: Context,
    mainHandler: Handler,
    onSuccess: (LatLng) -> Unit,
    onError: (String) -> Unit,
) {
    runCatching {
        val client = AMapLocationClient(context)
        val option =
            AMapLocationClientOption().apply {
                locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                isOnceLocation = true
                isOnceLocationLatest = true
                isNeedAddress = false
                isMockEnable = true
                httpTimeOut = 10_000
            }
        client.setLocationOption(option)
        client.setLocationListener(
            AMapLocationListener { loc ->
                mainHandler.post {
                    if (loc == null) {
                        onError("定位失败：无返回结果")
                    } else if (loc.errorCode == 0) {
                        onSuccess(LatLng(loc.latitude, loc.longitude))
                    } else {
                        val info = loc.errorInfo?.takeIf { it.isNotBlank() } ?: "未知错误"
                        onError("定位失败（${loc.errorCode}）：$info")
                    }
                }
                client.stopLocation()
                client.onDestroy()
            },
        )
        client.startLocation()
    }.onFailure { e ->
        mainHandler.post { onError("定位异常：" + (e.message ?: e.javaClass.simpleName)) }
    }
}
