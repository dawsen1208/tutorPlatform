package com.example.teacher.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.teacher.core.zhApplicationStatus
import com.example.teacher.core.zhReportStatus
import com.example.teacher.data.AppRepository
import com.example.teacher.data.BackendApi
import com.example.teacher.data.TeacherOrderRanking
import com.example.teacher.data.local.ApplicationEntity
import com.example.teacher.data.local.CartItemWithProduct
import com.example.teacher.data.local.ProductEntity
import com.example.teacher.data.local.ProductOrderEntity
import com.example.teacher.data.local.ProductOrderItemEntity
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.PaymentEntity
import com.example.teacher.data.local.ProductPaymentEntity
import com.example.teacher.data.local.NotificationEntity
import com.example.teacher.data.local.ReportEntity
import com.example.teacher.data.local.TeacherEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import retrofit2.HttpException

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
            modelClass.isAssignableFrom(ParentViewModel::class.java) -> ParentViewModel(repository) as T
            modelClass.isAssignableFrom(TeacherViewModel::class.java) -> TeacherViewModel(repository) as T
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> AdminViewModel(repository) as T
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> ProductViewModel(repository) as T
            modelClass.isAssignableFrom(CommerceViewModel::class.java) -> CommerceViewModel(repository) as T
            modelClass.isAssignableFrom(ParentHomeViewModel::class.java) -> ParentHomeViewModel(repository) as T
            modelClass.isAssignableFrom(TeacherHomeViewModel::class.java) -> TeacherHomeViewModel(repository) as T
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> ReportViewModel(repository) as T
            modelClass.isAssignableFrom(TeacherIncomeViewModel::class.java) -> TeacherIncomeViewModel(repository) as T
            modelClass.isAssignableFrom(NotificationViewModel::class.java) -> NotificationViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}

data class UiState(
    val loading: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
)

class AuthViewModel(private val repository: AppRepository) : ViewModel() {
    val uiState = MutableStateFlow(UiState())

    private fun isNetworkUnreachable(t: Throwable): Boolean {
        return t is java.net.UnknownHostException ||
            t is java.net.ConnectException ||
            t is java.net.SocketTimeoutException ||
            t is java.io.IOException ||
            t is IllegalArgumentException
    }

    private fun toUserMessage(t: Throwable): String {
        return when (t) {
            is HttpException -> {
                when (t.code()) {
                    400 -> "请求参数错误"
                    401 -> "手机号或密码错误，或账号身份不匹配"
                    403 -> "没有权限执行此操作"
                    404 -> "服务地址错误（404）"
                    409 -> "该手机号已注册"
                    in 500..599 -> "服务器错误：${t.code()}"
                    else -> "网络错误：${t.code()}"
                }
            }
            is java.net.UnknownHostException -> "无法解析服务器地址：${BackendApi.getBaseUrl()}"
            is java.net.ConnectException -> "无法连接到服务器：${BackendApi.getBaseUrl()}"
            is java.net.SocketTimeoutException -> "连接超时：${BackendApi.getBaseUrl()}"
            is IllegalArgumentException -> "服务地址无效：${BackendApi.getBaseUrl()}"
            is java.io.IOException -> "网络异常：${BackendApi.getBaseUrl()}"
            else -> t.message ?: "操作失败"
        }
    }

    private suspend fun ensureAccessToken(role: String, phone: String, password: String): String {
        return runCatching {
            BackendApi.login(role = role, phone = phone.trim(), password = password).accessToken
        }.recoverCatching { t ->
            if (t is HttpException && t.code() == 401) {
                runCatching {
                    BackendApi.register(role = role, phone = phone.trim(), password = password, nickname = null).accessToken
                }.recoverCatching { t2 ->
                    if (t2 is HttpException && t2.code() == 409) {
                        BackendApi.login(role = role, phone = phone.trim(), password = password).accessToken
                    } else {
                        throw t2
                    }
                }.getOrThrow()
            } else {
                throw t
            }
        }.getOrThrow()
    }

    fun parentLogin(phone: String, password: String, onSuccess: (ParentEntity, String) -> Unit) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val tokenResult = runCatching { ensureAccessToken(role = "PARENT", phone = phone, password = password) }
            val token = tokenResult.getOrNull().orEmpty()
            val parent =
                runCatching {
                    val existing = repository.getParentByPhone(phone)
                    if (existing == null) {
                        repository.registerParent(phone = phone, password = password).getOrThrow()
                    } else {
                        if (existing.password != password) {
                            val updated = existing.copy(password = password)
                            repository.updateParent(updated)
                            updated
                        } else {
                            existing
                        }
                    }
                }.getOrElse { t ->
                    uiState.value = UiState(loading = false, message = t.message ?: "本地保存失败", messageIsError = true)
                    return@launch
                }
            val tokenError = tokenResult.exceptionOrNull()
            uiState.value =
                if (tokenError != null && isNetworkUnreachable(tokenError)) {
                    UiState(loading = false, message = "已离线登录（服务器不可达），部分功能暂不可用", messageIsError = false)
                } else if (tokenError != null) {
                    UiState(loading = false, message = toUserMessage(tokenError), messageIsError = true)
                } else {
                    UiState(loading = false)
                }
            if (tokenError != null && !isNetworkUnreachable(tokenError)) return@launch
            onSuccess(parent, token)
        }
    }

    fun parentRegister(
        phone: String,
        password: String,
        address: String = "",
        studentName: String = "",
        studentGender: String = "",
        studentGrade: String = "",
        weakSubjects: String = "",
        onSuccess: (ParentEntity, String) -> Unit,
    ) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.registerParent(
                phone = phone,
                password = password,
                address = address,
                studentName = studentName,
                studentGender = studentGender,
                studentGrade = studentGrade,
                weakSubjects = weakSubjects,
            )
            if (result.isFailure) {
                uiState.value = UiState(loading = false, message = result.exceptionOrNull()?.message, messageIsError = true)
                return@launch
            }
            val parent = result.getOrThrow()
            val remoteResult =
                runCatching { BackendApi.register(role = "PARENT", phone = phone.trim(), password = password, nickname = null) }
                    .recoverCatching { t ->
                        if (t is HttpException && t.code() == 409) {
                            BackendApi.login(role = "PARENT", phone = phone.trim(), password = password)
                        } else {
                            throw t
                        }
                    }

            val remote = remoteResult.getOrNull()
            val remoteError = remoteResult.exceptionOrNull()
            uiState.value =
                if (remoteError != null && isNetworkUnreachable(remoteError)) {
                    UiState(loading = false, message = "已离线注册（服务器不可达），部分功能暂不可用", messageIsError = false)
                } else if (remoteError != null) {
                    UiState(loading = false, message = toUserMessage(remoteError), messageIsError = true)
                } else {
                    UiState(loading = false)
                }
            if (remoteError != null && !isNetworkUnreachable(remoteError)) return@launch
            onSuccess(parent, remote?.accessToken.orEmpty())
        }
    }

    fun teacherLogin(phone: String, password: String, onSuccess: (TeacherEntity, String) -> Unit) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val tokenResult = runCatching { ensureAccessToken(role = "TEACHER", phone = phone, password = password) }
            val token = tokenResult.getOrNull().orEmpty()
            val teacher =
                runCatching {
                    val existing = repository.getTeacherByPhone(phone)
                    if (existing == null) {
                        repository.registerTeacher(phone = phone, password = password).getOrThrow()
                    } else {
                        if (existing.password != password) {
                            val updated = existing.copy(password = password)
                            repository.updateTeacher(updated)
                            updated
                        } else {
                            existing
                        }
                    }
                }.getOrElse { t ->
                    uiState.value = UiState(loading = false, message = t.message ?: "本地保存失败", messageIsError = true)
                    return@launch
                }
            val tokenError = tokenResult.exceptionOrNull()
            uiState.value =
                if (tokenError != null && isNetworkUnreachable(tokenError)) {
                    UiState(loading = false, message = "已离线登录（服务器不可达），部分功能暂不可用", messageIsError = false)
                } else if (tokenError != null) {
                    UiState(loading = false, message = toUserMessage(tokenError), messageIsError = true)
                } else {
                    UiState(loading = false)
                }
            if (tokenError != null && !isNetworkUnreachable(tokenError)) return@launch
            onSuccess(teacher, token)
        }
    }

    fun teacherRegister(
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
        onSuccess: (TeacherEntity, String) -> Unit,
    ) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.registerTeacher(
                phone = phone,
                password = password,
                avatarPath = avatarPath,
                wechat = wechat,
                teachingExperience = teachingExperience,
                employmentStatus = employmentStatus,
                subjects = subjects,
                grades = grades,
                pricePerHour = pricePerHour,
                address = address,
            )
            if (result.isFailure) {
                uiState.value = UiState(loading = false, message = result.exceptionOrNull()?.message, messageIsError = true)
                return@launch
            }
            val teacher = result.getOrThrow()
            val remoteResult =
                runCatching { BackendApi.register(role = "TEACHER", phone = phone.trim(), password = password, nickname = null) }
                    .recoverCatching { t ->
                        if (t is HttpException && t.code() == 409) {
                            BackendApi.login(role = "TEACHER", phone = phone.trim(), password = password)
                        } else {
                            throw t
                        }
                    }

            val remote = remoteResult.getOrNull()
            val remoteError = remoteResult.exceptionOrNull()
            uiState.value =
                if (remoteError != null && isNetworkUnreachable(remoteError)) {
                    UiState(loading = false, message = "已离线注册（服务器不可达），部分功能暂不可用", messageIsError = false)
                } else if (remoteError != null) {
                    UiState(loading = false, message = toUserMessage(remoteError), messageIsError = true)
                } else {
                    UiState(loading = false)
                }
            if (remoteError != null && !isNetworkUnreachable(remoteError)) return@launch
            onSuccess(teacher, remote?.accessToken.orEmpty())
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }
}

class ParentViewModel(private val repository: AppRepository) : ViewModel() {
    enum class TeacherSortOption {
        Default,
        PriceAsc,
        AreaFirst,
    }

    private val parentId = MutableStateFlow<Int?>(null)
    private val parentAddress = MutableStateFlow<String?>(null)

    val approvedTeachers: StateFlow<List<TeacherEntity>> =
        repository.getApprovedTeachers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val teacherListSortOption = MutableStateFlow(TeacherSortOption.Default)
    private val teacherListPage = MutableStateFlow(1)
    private val teacherListPageSize = 20

    val teacherListSortedTeachers: StateFlow<List<TeacherEntity>> =
        combine(approvedTeachers, teacherListSortOption, parentAddress) { teachers, sort, address ->
            when (sort) {
                TeacherSortOption.Default -> teachers
                TeacherSortOption.PriceAsc -> teachers.sortedBy { it.pricePerHour }
                TeacherSortOption.AreaFirst -> {
                    val key = address?.trim().orEmpty()
                    if (key.isBlank()) teachers else {
                        teachers.sortedWith(
                            compareByDescending<TeacherEntity> { it.address.contains(key) }
                                .thenBy { it.pricePerHour },
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teacherListVisibleTeachers: StateFlow<List<TeacherEntity>> =
        combine(teacherListSortedTeachers, teacherListPage) { all, page ->
            val size = (page * teacherListPageSize).coerceAtLeast(teacherListPageSize)
            if (all.size <= size) all else all.take(size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teacherListCanLoadMore: StateFlow<Boolean> =
        combine(teacherListSortedTeachers, teacherListVisibleTeachers) { all, visible ->
            visible.size < all.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val teacherListSort: StateFlow<TeacherSortOption> = teacherListSortOption

    data class TeacherSearchFilters(
        val subjects: Set<String> = emptySet(),
        val grades: Set<String> = emptySet(),
        val addressKeyword: String = "",
        val minPrice: Double? = null,
        val maxPrice: Double? = null,
        val employmentStatuses: Set<String> = emptySet(),
    )

    private val teacherSearchFilters = MutableStateFlow(TeacherSearchFilters())
    private val teacherSearchPage = MutableStateFlow(1)
    private val teacherSearchPageSize = 20
    private val teacherSearchSortOption = MutableStateFlow(TeacherSortOption.Default)

    val teacherSearchResults: StateFlow<List<TeacherEntity>> =
        teacherSearchFilters
            .map { filters ->
                repository.searchApprovedTeachers(
                    subject = null,
                    grade = null,
                    addressKeyword = filters.addressKeyword.trim().takeIf { it.isNotBlank() },
                    employmentStatus = null,
                    minPrice = filters.minPrice,
                    maxPrice = filters.maxPrice,
                ).map { teachers ->
                    teachers.filter { teacher ->
                        val subjectOk = filters.subjects.isEmpty() || filters.subjects.any { teacher.subjects.contains(it) }
                        val gradeOk = filters.grades.isEmpty() || filters.grades.any { teacher.grades.contains(it) }
                        val employmentOk =
                            filters.employmentStatuses.isEmpty() ||
                                filters.employmentStatuses.any { teacher.employmentStatus.contains(it) }
                        subjectOk && gradeOk && employmentOk
                    }
                }
            }
            .flatMapLatest { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teacherSearchSortedResults: StateFlow<List<TeacherEntity>> =
        combine(teacherSearchResults, teacherSearchSortOption, teacherSearchFilters, parentAddress) { results, sort, filters, address ->
            when (sort) {
                TeacherSortOption.Default -> results
                TeacherSortOption.PriceAsc -> results.sortedBy { it.pricePerHour }
                TeacherSortOption.AreaFirst -> {
                    val key = filters.addressKeyword.trim().ifBlank { address?.trim().orEmpty() }
                    if (key.isBlank()) results else {
                        results.sortedWith(
                            compareByDescending<TeacherEntity> { it.address.contains(key) }
                                .thenBy { it.pricePerHour },
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teacherSearchVisibleResults: StateFlow<List<TeacherEntity>> =
        combine(teacherSearchSortedResults, teacherSearchPage) { all, page ->
            val size = (page * teacherSearchPageSize).coerceAtLeast(teacherSearchPageSize)
            if (all.size <= size) all else all.take(size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teacherSearchCanLoadMore: StateFlow<Boolean> =
        combine(teacherSearchSortedResults, teacherSearchVisibleResults) { all, visible ->
            visible.size < all.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val teacherSearchSort: StateFlow<TeacherSortOption> = teacherSearchSortOption

    val applications: StateFlow<List<ApplicationEntity>> =
        parentId.filterNotNull()
            .flatMapLatest { repository.getParentApplications(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = MutableStateFlow(UiState())

    fun setParentId(id: Int) {
        parentId.value = id
        viewModelScope.launch {
            parentAddress.value = withContext(Dispatchers.IO) { repository.getParentById(id)?.address }
        }
    }

    fun setTeacherListSort(option: TeacherSortOption) {
        teacherListSortOption.value = option
        teacherListPage.value = 1
    }

    fun resetTeacherListPaging() {
        teacherListPage.value = 1
    }

    fun loadMoreTeacherList() {
        if (!teacherListCanLoadMore.value) return
        teacherListPage.value = teacherListPage.value + 1
    }

    fun setTeacherSearchFilters(filters: TeacherSearchFilters) {
        teacherSearchFilters.value = filters
        teacherSearchPage.value = 1
    }

    fun setTeacherSearchSort(option: TeacherSortOption) {
        teacherSearchSortOption.value = option
        teacherSearchPage.value = 1
    }

    fun resetTeacherSearchPaging() {
        teacherSearchPage.value = 1
    }

    fun getTeacherSearchFilters(): TeacherSearchFilters = teacherSearchFilters.value

    fun resetTeacherSearchFilters() {
        teacherSearchFilters.value = TeacherSearchFilters()
        teacherSearchPage.value = 1
    }

    fun loadMoreTeacherSearchResults() {
        if (!teacherSearchCanLoadMore.value) return
        teacherSearchPage.value = teacherSearchPage.value + 1
    }

    fun submitApplication(teacherId: Int) {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.submitApplication(parentId = pid, teacherId = teacherId)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "申请提交成功",
                messageIsError = result.isFailure,
            )
        }
    }

    fun pay(applicationId: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.payForApplication(applicationId)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "支付成功",
                messageIsError = result.isFailure,
            )
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }

    fun setMessage(message: String) {
        uiState.value = uiState.value.copy(message = message)
    }

    fun setError(message: String) {
        uiState.value = uiState.value.copy(message = message, messageIsError = true)
    }
}

class TeacherViewModel(private val repository: AppRepository) : ViewModel() {
    private val teacherId = MutableStateFlow<Int?>(null)

    val applications: StateFlow<List<ApplicationEntity>> =
        teacherId.filterNotNull()
            .flatMapLatest { repository.getTeacherApplications(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = MutableStateFlow(UiState())

    fun setTeacherId(id: Int) {
        teacherId.value = id
    }

    fun accept(applicationId: Int) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "ACCEPTED")
            uiState.value = UiState(message = "已接受申请", messageIsError = false)
        }
    }

    fun reject(applicationId: Int) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "REJECTED")
            uiState.value = UiState(message = "已拒绝申请", messageIsError = false)
        }
    }

    fun complete(applicationId: Int) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "COMPLETED")
            uiState.value = UiState(message = "已完成申请", messageIsError = false)
        }
    }

    fun cancel(applicationId: Int) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, "CANCELLED")
            uiState.value = UiState(message = "已取消预约", messageIsError = false)
        }
    }

    fun reschedule(applicationId: Int, scheduledAt: Long?) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = withContext(Dispatchers.IO) { repository.updateApplicationSchedule(applicationId, scheduledAt) }
            uiState.value =
                UiState(
                    loading = false,
                    message = result.exceptionOrNull()?.message ?: "上课时间已更新",
                    messageIsError = result.isFailure,
                )
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }

    fun setMessage(message: String) {
        uiState.value = uiState.value.copy(message = message)
    }

    fun setError(message: String) {
        uiState.value = uiState.value.copy(message = message, messageIsError = true)
    }
}

class AdminViewModel(private val repository: AppRepository) : ViewModel() {
    val parents: StateFlow<List<ParentEntity>> =
        repository.getAllParents().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val teachers: StateFlow<List<TeacherEntity>> =
        repository.getAllTeachers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendingTeachers: StateFlow<List<TeacherEntity>> =
        repository.getPendingTeachers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val applications: StateFlow<List<ApplicationEntity>> =
        repository.getAllApplications().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> =
        repository.getAllPayments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val productPayments: StateFlow<List<ProductPaymentEntity>> =
        repository.getAllProductPayments().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val productOrders: StateFlow<List<ProductOrderEntity>> =
        repository.getAllProductOrders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reports: StateFlow<List<ReportEntity>> =
        repository.getAllReports().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    data class KpiState(
        val weekOrderCount: Int = 0,
        val weekRevenue: Double = 0.0,
        val activeTeacherCount: Int = 0,
    )

    val kpis: StateFlow<KpiState> =
        combine(payments, productPayments) { payList, productPayList ->
            val weekStart = startOfWeekMillis(System.currentTimeMillis())
            val weekPaid = payList.filter { it.status == "PAID" && it.paidAt >= weekStart }
            val weekProductPaid = productPayList.filter { it.status == "PAID" && it.paidAt >= weekStart }
            val weekOrderCount = weekPaid.size + weekProductPaid.size
            val weekRevenue = weekPaid.sumOf { it.amount } + weekProductPaid.sumOf { it.amount }
            val activeTeacherCount = weekPaid.map { it.teacherPhone }.distinct().size
            KpiState(
                weekOrderCount = weekOrderCount,
                weekRevenue = weekRevenue,
                activeTeacherCount = activeTeacherCount,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KpiState())

    private fun startOfWeekMillis(nowMillis: Long): Long {
        val cal =
            Calendar.getInstance().apply {
                timeInMillis = nowMillis
                firstDayOfWeek = Calendar.MONDAY
                minimalDaysInFirstWeek = 4
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        return cal.timeInMillis
    }

    val uiState = MutableStateFlow(UiState())

    fun approveTeacher(teacherId: Int) {
        viewModelScope.launch {
            repository.setTeacherStatus(teacherId, "APPROVED")
            uiState.value = UiState(message = "审核通过", messageIsError = false)
        }
    }

    fun rejectTeacher(teacherId: Int) {
        viewModelScope.launch {
            repository.setTeacherStatus(teacherId, "REJECTED")
            uiState.value = UiState(message = "已拒绝", messageIsError = false)
        }
    }

    fun disableTeacher(teacherId: Int) {
        viewModelScope.launch {
            repository.setTeacherStatus(teacherId, "DISABLED")
            uiState.value = UiState(message = "已禁用", messageIsError = false)
        }
    }

    fun deleteTeacher(teacherId: Int) {
        viewModelScope.launch {
            repository.deleteTeacher(teacherId)
            uiState.value = UiState(message = "已删除老师", messageIsError = false)
        }
    }

    fun deleteParent(parentId: Int) {
        viewModelScope.launch {
            repository.deleteParent(parentId)
            uiState.value = UiState(message = "已删除家长", messageIsError = false)
        }
    }

    fun setApplicationStatus(applicationId: Int, status: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, status)
            uiState.value = UiState(message = "申请状态已更新：${zhApplicationStatus(status)}", messageIsError = false)
        }
    }

    fun updateReportStatus(reportId: Int, status: String, adminNote: String?) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.updateReportStatusAndNote(reportId, status, adminNote)
            uiState.value =
                UiState(
                    loading = false,
                    message = result.exceptionOrNull()?.message ?: "已更新：${zhReportStatus(status)}",
                    messageIsError = result.isFailure,
                )
        }
    }

    fun cancelProductOrder(orderId: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result =
                runCatching {
                    val order = withContext(Dispatchers.IO) { repository.getOrderById(orderId) } ?: error("订单不存在")
                    withContext(Dispatchers.IO) { repository.cancelProductOrder(order.parentId, orderId).getOrThrow() }
                }
            uiState.value =
                UiState(
                    loading = false,
                    message = result.exceptionOrNull()?.message ?: "订单已取消",
                    messageIsError = result.isFailure,
                )
        }
    }

    fun refundProductOrder(orderId: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = withContext(Dispatchers.IO) { repository.refundProductOrder(orderId) }
            uiState.value =
                UiState(
                    loading = false,
                    message = result.exceptionOrNull()?.message ?: "订单已退款",
                    messageIsError = result.isFailure,
                )
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }

    fun setError(message: String) {
        uiState.value = uiState.value.copy(message = message, messageIsError = true)
    }
}

class ProductViewModel(private val repository: AppRepository) : ViewModel() {
    data class ProductFilters(
        val type: String? = null,
        val keyword: String = "",
    )

    val uiState = MutableStateFlow(UiState())
    private val filters = MutableStateFlow(ProductFilters())
    private val page = MutableStateFlow(1)
    private val pageSize = 20

    val products: StateFlow<List<ProductEntity>> =
        filters
            .map { f ->
                val flow =
                    if (f.type.isNullOrBlank()) {
                        repository.getAllProducts()
                    } else {
                        repository.getProductsByType(f.type)
                    }
                flow.map { list ->
                    val kw = f.keyword.trim()
                    if (kw.isBlank()) list
                    else list.filter {
                        it.name.contains(kw) || it.provider.contains(kw) || it.details.contains(kw)
                    }
                }
            }
            .flatMapLatest { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val visibleProducts: StateFlow<List<ProductEntity>> =
        combine(products, page) { all, p ->
            val size = (p * pageSize).coerceAtLeast(pageSize)
            if (all.size <= size) all else all.take(size)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val canLoadMore: StateFlow<Boolean> =
        combine(products, visibleProducts) { all, visible ->
            visible.size < all.size
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setFilters(type: String?, keyword: String) {
        filters.value = ProductFilters(type = type, keyword = keyword)
        page.value = 1
    }

    fun loadMore() {
        if (!canLoadMore.value) return
        page.value = page.value + 1
    }

    fun addProduct(
        type: String,
        name: String,
        price: Double,
        provider: String,
        details: String,
        imageUri: String?,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.addProduct(type, name, price, provider, details, imageUri)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "已添加商品",
                messageIsError = result.isFailure,
            )
            if (result.isSuccess) onSuccess()
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            runCatching { repository.deleteProduct(productId) }
                .onSuccess {
                    uiState.value = UiState(message = "已删除商品")
                }
                .onFailure {
                    uiState.value = UiState(message = it.message ?: "删除失败", messageIsError = true)
                }
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }
}

class CommerceViewModel(private val repository: AppRepository) : ViewModel() {
    private val parentId = MutableStateFlow<Int?>(null)

    val uiState = MutableStateFlow(UiState())

    val cart: StateFlow<List<CartItemWithProduct>> =
        parentId.filterNotNull()
            .flatMapLatest { repository.getCart(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<ProductOrderEntity>> =
        parentId.filterNotNull()
            .flatMapLatest { repository.getOrders(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setParentId(id: Int) {
        parentId.value = id
    }

    fun addToCart(productId: Int) {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.addToCart(pid, productId, 1)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "已加入购物车",
                messageIsError = result.isFailure,
            )
        }
    }

    fun updateCartQuantity(cartItemId: Int, quantity: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.updateCartQuantity(cartItemId, quantity)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "已更新数量",
                messageIsError = result.isFailure,
            )
        }
    }

    fun removeCartItem(cartItemId: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.removeCartItem(cartItemId)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "已移除",
                messageIsError = result.isFailure,
            )
        }
    }

    fun clearCart() {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.clearCart(pid)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "已清空购物车",
                messageIsError = result.isFailure,
            )
        }
    }

    fun checkoutCart(onOrderCreated: (Int) -> Unit) {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.checkoutCart(pid)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "订单已创建",
                messageIsError = result.isFailure,
            )
            result.getOrNull()?.let { onOrderCreated(it.id) }
        }
    }

    fun buyNow(productId: Int, onOrderCreated: (Int) -> Unit) {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.buyNow(pid, productId, 1)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "订单已创建",
                messageIsError = result.isFailure,
            )
            result.getOrNull()?.let { onOrderCreated(it.id) }
        }
    }

    fun payOrder(orderId: Int) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.payOrder(orderId)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "支付成功",
                messageIsError = result.isFailure,
            )
        }
    }

    fun cancelOrder(orderId: Int) {
        val pid = parentId.value ?: return
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result = repository.cancelProductOrder(pid, orderId)
            uiState.value = UiState(
                loading = false,
                message = result.exceptionOrNull()?.message ?: "订单已取消",
                messageIsError = result.isFailure,
            )
        }
    }

    fun orderItems(orderId: Int): StateFlow<List<ProductOrderItemEntity>> =
        repository.getOrderItems(orderId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun getOrder(orderId: Int): ProductOrderEntity? = repository.getOrderById(orderId)

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }
}

class ReportViewModel(private val repository: AppRepository) : ViewModel() {
    val uiState = MutableStateFlow(UiState())

    fun submit(
        reporterRole: String,
        reporterId: Int?,
        reporterPhone: String?,
        category: String,
        subject: String,
        content: String,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            uiState.value = UiState(loading = true)
            val result =
                withContext(Dispatchers.IO) {
                    repository.submitReport(
                        reporterRole = reporterRole,
                        reporterId = reporterId,
                        reporterPhone = reporterPhone,
                        category = category,
                        subject = subject,
                        content = content,
                    )
                }
            uiState.value =
                UiState(
                    loading = false,
                    message = result.exceptionOrNull()?.message ?: "提交成功",
                    messageIsError = result.isFailure,
                )
            if (result.isSuccess) onSuccess()
        }
    }

    fun clearMessage() {
        uiState.value = uiState.value.copy(message = null)
    }
}

class NotificationViewModel(private val repository: AppRepository) : ViewModel() {
    private val user = MutableStateFlow<Pair<String, Int>?>(null)
    val uiState = MutableStateFlow(UiState())

    val notifications: StateFlow<List<NotificationEntity>> =
        user.filterNotNull()
            .flatMapLatest { (role, id) -> repository.getNotifications(role, id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> =
        user.filterNotNull()
            .flatMapLatest { (role, id) -> repository.getUnreadNotificationCount(role, id) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setUser(role: String, id: Int) {
        val next = role to id
        if (user.value == next) return
        user.value = next
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.ensureDefaultNotifications(role, id) }
        }
    }

    fun markRead(notificationId: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.markNotificationRead(notificationId) }
        }
    }

    fun markAllRead() {
        val u = user.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.markAllNotificationsRead(u.first, u.second) }
        }
    }

    fun clearRead() {
        val u = user.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.clearReadNotifications(u.first, u.second) }
        }
    }

    fun clearAll() {
        val u = user.value ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.clearAllNotifications(u.first, u.second) }
        }
    }
}

data class TeacherIncomeUiState(
    val loading: Boolean = false,
    val teacherPhone: String? = null,
    val payments: List<PaymentEntity> = emptyList(),
    val error: String? = null,
)

class TeacherIncomeViewModel(private val repository: AppRepository) : ViewModel() {
    private val teacherId = MutableStateFlow<Int?>(null)
    private val refreshTick = MutableStateFlow(0)
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    private val teacherPhone: StateFlow<String?> =
        combine(teacherId.filterNotNull(), refreshTick) { id, _ -> id }
            .map { id ->
                loading.value = true
                error.value = null
                val phone = withContext(Dispatchers.IO) { repository.getTeacherById(id)?.phone }
                if (phone.isNullOrBlank()) {
                    error.value = "老师信息不存在"
                }
                loading.value = false
                phone
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val payments: StateFlow<List<PaymentEntity>> =
        teacherPhone
            .filterNotNull()
            .flatMapLatest { repository.getPaymentsByTeacherPhone(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<TeacherIncomeUiState> =
        combine(loading, teacherPhone, payments, error) { isLoading, phone, list, err ->
            TeacherIncomeUiState(
                loading = isLoading,
                teacherPhone = phone,
                payments = list,
                error = err,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TeacherIncomeUiState())

    fun setTeacherId(id: Int) {
        teacherId.value = id
    }

    fun refresh() {
        refreshTick.value = refreshTick.value + 1
    }
}

data class ParentHomeUiState(
    val loading: Boolean = false,
    val parentPhone: String? = null,
    val hotTeachers: List<TeacherOrderRanking> = emptyList(),
    val error: String? = null,
)

class ParentHomeViewModel(private val repository: AppRepository) : ViewModel() {
    val uiState = MutableStateFlow(ParentHomeUiState())
    private var currentParentId: Int? = null

    fun setParentId(id: Int) {
        if (currentParentId == id) return
        currentParentId = id
        loadParentPhone(id)
        refreshHotTeachers()
    }

    fun refreshHotTeachers() {
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null)
            val result =
                runCatching {
                    val (startAt, endAtExclusive) = previousWeekRangeMillis()
                    withContext(Dispatchers.IO) {
                        repository.getTopTeachersByOrders(
                            startAt = startAt,
                            endAtExclusive = endAtExclusive,
                            limit = 5,
                        )
                    }
                }
            uiState.value =
                uiState.value.copy(
                    loading = false,
                    hotTeachers = result.getOrElse { emptyList() },
                    error = result.exceptionOrNull()?.message,
                )
        }
    }

    private fun loadParentPhone(parentId: Int) {
        viewModelScope.launch {
            val phone = withContext(Dispatchers.IO) { repository.getParentById(parentId)?.phone }
            uiState.value = uiState.value.copy(parentPhone = phone)
        }
    }

    private fun previousWeekRangeMillis(): Pair<Long, Long> {
        val now = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
        }
        val startOfThisWeek = (now.clone() as Calendar).apply {
            firstDayOfWeek = Calendar.MONDAY
            minimalDaysInFirstWeek = 4
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endExclusive = startOfThisWeek.timeInMillis
        val startOfPreviousWeek = (startOfThisWeek.clone() as Calendar).apply {
            add(Calendar.WEEK_OF_YEAR, -1)
        }.timeInMillis
        return startOfPreviousWeek to endExclusive
    }
}

data class TeacherHomeUiState(
    val loading: Boolean = false,
    val teacher: TeacherEntity? = null,
    val demands: List<TeacherDemandItem> = emptyList(),
    val error: String? = null,
)

data class TeacherDemandItem(
    val id: Int,
    val parentId: Int,
    val subject: String,
    val studentGrade: String,
    val timeStartAtMillis: Long,
    val timeEndAtMillis: Long,
    val teacherGenderPreference: String?,
    val minPrice: Double,
    val maxPrice: Double,
)

class TeacherHomeViewModel(private val repository: AppRepository) : ViewModel() {
    val uiState = MutableStateFlow(TeacherHomeUiState())
    private var currentTeacherId: Int? = null
    private var currentAccessToken: String? = null

    private fun splitTokens(raw: String): Set<String> {
        return raw
            .split('、', ',', '，', ';', '；', '|', '/', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    fun setTeacherId(id: Int) {
        if (currentTeacherId == id) return
        currentTeacherId = id
        refresh()
    }

    fun setTeacherSession(id: Int, accessToken: String?) {
        val token = accessToken?.trim().orEmpty().ifBlank { null }
        val changed = currentTeacherId != id || currentAccessToken != token
        currentTeacherId = id
        currentAccessToken = token
        if (changed) refresh()
    }

    fun refresh() {
        val id = currentTeacherId ?: return
        val token = currentAccessToken
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null)
            val result =
                runCatching {
                    withContext(Dispatchers.IO) {
                        val teacher = repository.getTeacherById(id)
                        val openDemands =
                            if (token.isNullOrBlank() || teacher == null) {
                                emptyList()
                            } else {
                                val rows = BackendApi.openDemands(token, limit = 50).items
                                rows.mapNotNull { d ->
                                    val startAt = runCatching { java.time.Instant.parse(d.timeStartAt).toEpochMilli() }.getOrNull() ?: return@mapNotNull null
                                    val endAt = runCatching { java.time.Instant.parse(d.timeEndAt).toEpochMilli() }.getOrNull() ?: return@mapNotNull null
                                    TeacherDemandItem(
                                        id = d.id,
                                        parentId = d.parentId,
                                        subject = d.subject,
                                        studentGrade = d.studentGrade,
                                        timeStartAtMillis = startAt,
                                        timeEndAtMillis = endAt,
                                        teacherGenderPreference = d.teacherGenderPreference,
                                        minPrice = d.minPrice,
                                        maxPrice = d.maxPrice,
                                    )
                                }
                            }
                        val filtered =
                            if (teacher == null) {
                                openDemands
                            } else {
                                val subjects = splitTokens(teacher.subjects)
                                val grades = splitTokens(teacher.grades)
                                openDemands.filter { d ->
                                    val genderOk =
                                        d.teacherGenderPreference.isNullOrBlank() ||
                                            teacher.gender.isBlank() ||
                                            d.teacherGenderPreference == teacher.gender
                                    genderOk &&
                                        (subjects.isEmpty() || subjects.contains(d.subject)) &&
                                        (grades.isEmpty() || grades.contains(d.studentGrade))
                                }
                            }
                        teacher to filtered
                    }
                }
            val (teacher, demands) = result.getOrNull() ?: (null to emptyList())
            uiState.value =
                uiState.value.copy(
                    loading = false,
                    teacher = teacher,
                    demands = demands,
                    error = result.exceptionOrNull()?.message,
                )
        }
    }

    fun claimDemand(demandId: Int) {
        val teacherId = currentTeacherId ?: return
        val token = currentAccessToken?.trim().orEmpty()
        viewModelScope.launch {
            uiState.value = uiState.value.copy(loading = true, error = null)
            if (token.isBlank()) {
                uiState.value = uiState.value.copy(loading = false, error = "缺少 Token，请重新登录")
                return@launch
            }
            val result =
                runCatching {
                    withContext(Dispatchers.IO) {
                        BackendApi.claimDemand(token, demandId)
                    }
                }
            if (result.isFailure) {
                uiState.value = uiState.value.copy(loading = false, error = result.exceptionOrNull()?.message ?: "接单失败")
            } else {
                refresh()
            }
        }
    }
}
