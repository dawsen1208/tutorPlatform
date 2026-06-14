package com.example.teacher.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Scaffold
import com.example.teacher.core.ADMIN_PASSWORD
import com.example.teacher.data.AppRepository
import com.example.teacher.data.local.ParentEntity
import com.example.teacher.data.local.TeacherEntity
import com.example.teacher.ui.Role
import com.example.teacher.ui.SessionState
import com.example.teacher.ui.screens.AdminApplicationListScreen
import com.example.teacher.ui.screens.AdminDashboardScreen
import com.example.teacher.ui.screens.AdminLoginScreen
import com.example.teacher.ui.screens.AdminOrdersScreen
import com.example.teacher.ui.screens.AdminParentListScreen
import com.example.teacher.ui.screens.AdminPaymentRecordScreen
import com.example.teacher.ui.screens.AdminProductManageScreen
import com.example.teacher.ui.screens.AdminReportsScreen
import com.example.teacher.ui.screens.AdminStatsScreen
import com.example.teacher.ui.screens.AdminTeacherListScreen
import com.example.teacher.ui.screens.AdminTeacherReviewScreen
import com.example.teacher.ui.screens.AdminUsersScreen
import com.example.teacher.ui.screens.CartScreen
import com.example.teacher.ui.screens.GuestHomeScreen
import com.example.teacher.ui.screens.LoginRequiredScreen
import com.example.teacher.ui.screens.ProfileRequiredScreen
import com.example.teacher.ui.screens.OrderDetailScreen
import com.example.teacher.ui.screens.OrdersScreen
import com.example.teacher.ui.screens.ParentApplicationListScreen
import com.example.teacher.ui.screens.ParentHomeScreen
import com.example.teacher.ui.screens.ParentLoginScreen
import com.example.teacher.ui.screens.ParentMessagesScreen
import com.example.teacher.ui.screens.ParentMeScreen
import com.example.teacher.ui.screens.ParentProfileScreen
import com.example.teacher.ui.screens.ParentPublishApplicationScreen
import com.example.teacher.ui.screens.ParentRegisterScreen
import com.example.teacher.ui.screens.PaymentScreen
import com.example.teacher.ui.screens.ChatScreen
import com.example.teacher.ui.screens.ProductDetailScreen
import com.example.teacher.ui.screens.ProductListScreen
import com.example.teacher.ui.screens.ProductSearchScreen
import com.example.teacher.ui.screens.ReportSubmitScreen
import com.example.teacher.ui.screens.RoleSelectionScreen
import com.example.teacher.ui.screens.TeacherApplicationListScreen
import com.example.teacher.ui.screens.TeacherDemandsScreen
import com.example.teacher.ui.screens.TeacherDetailScreen
import com.example.teacher.ui.screens.TeacherHomeScreen
import com.example.teacher.ui.screens.TeacherIncomeScreen
import com.example.teacher.ui.screens.TeacherListScreen
import com.example.teacher.ui.screens.TeacherMeScreen
import com.example.teacher.ui.screens.TeacherNotificationsScreen
import com.example.teacher.ui.screens.TeacherProfileScreen
import com.example.teacher.ui.screens.TeacherPublicProfileScreen
import com.example.teacher.ui.screens.TeacherSearchScreen
import com.example.teacher.ui.screens.TeacherStudentsScreen
import com.example.teacher.ui.screens.TeacherLoginScreen
import com.example.teacher.ui.screens.TeacherRegisterScreen
import com.example.teacher.ui.screens.WelcomeScreen
import com.example.teacher.ui.screens.OnboardingScreen
import com.example.teacher.ui.screens.LoginTabsScreen
import com.example.teacher.ui.viewmodel.AdminViewModel
import com.example.teacher.ui.viewmodel.AppViewModelFactory
import com.example.teacher.ui.viewmodel.AuthViewModel
import com.example.teacher.ui.viewmodel.CommerceViewModel
import com.example.teacher.ui.viewmodel.ParentHomeViewModel
import com.example.teacher.ui.viewmodel.ParentViewModel
import com.example.teacher.ui.viewmodel.ProductViewModel
import com.example.teacher.ui.viewmodel.NotificationViewModel
import com.example.teacher.ui.viewmodel.ReportViewModel
import com.example.teacher.ui.viewmodel.TeacherHomeViewModel
import com.example.teacher.ui.viewmodel.TeacherIncomeViewModel
import com.example.teacher.ui.viewmodel.TeacherViewModel

private fun isParentProfileComplete(parent: ParentEntity): Boolean {
    return parent.address.isNotBlank() &&
        parent.studentName.isNotBlank() &&
        parent.studentGender.isNotBlank() &&
        parent.studentGrade.isNotBlank() &&
        parent.weakSubjects.isNotBlank()
}

private fun isTeacherProfileComplete(teacher: TeacherEntity): Boolean {
    return teacher.avatarPath.isNotBlank() &&
        teacher.gender.isNotBlank() &&
        teacher.teachingExperience.isNotBlank() &&
        teacher.employmentStatus.isNotBlank() &&
        teacher.subjects.isNotBlank() &&
        teacher.grades.isNotBlank() &&
        teacher.pricePerHour > 0.0 &&
        teacher.address.isNotBlank()
}

private enum class AccessRequirement {
    None,
    ParentLogin,
    ParentProfile,
    TeacherLogin,
    TeacherProfile,
    AdminLogin,
}

private object AccessPolicy {
    fun requirement(routeBase: String): AccessRequirement {
        return when (routeBase) {
            Routes.ParentHome,
            Routes.ParentMessages,
            Routes.ParentMe,
            Routes.ParentProfile,
            -> AccessRequirement.ParentLogin

            Routes.ParentApplications,
            Routes.Payment,
            Routes.Cart,
            Routes.Orders,
            Routes.OrderDetail,
            Routes.ParentPublish,
            -> AccessRequirement.ParentProfile

            Routes.TeacherHome,
            Routes.TeacherDemands,
            Routes.TeacherMe,
            Routes.TeacherPublicProfile,
            Routes.TeacherNotifications,
            Routes.TeacherProfile,
            -> AccessRequirement.TeacherLogin

            Routes.TeacherStudents,
            Routes.TeacherApplications,
            Routes.TeacherIncome,
            Routes.TeacherCourses,
            -> AccessRequirement.TeacherProfile

            Routes.AdminUsers,
            Routes.AdminTeacherReview,
            Routes.AdminOrders,
            Routes.AdminReports,
            Routes.AdminStats,
            Routes.AdminDashboard,
            Routes.AdminParents,
            Routes.AdminTeachers,
            Routes.AdminApplications,
            Routes.AdminPayments,
            Routes.AdminProducts,
            -> AccessRequirement.AdminLogin

            else -> AccessRequirement.None
        }
    }

    fun canSubmitApplication(sessionState: SessionState, parentProfileComplete: Boolean): Boolean {
        return sessionState.role == Role.Parent && sessionState.parentId != null && parentProfileComplete
    }

    fun onRequireParentProfileOrLogin(
        sessionState: SessionState,
        parentProfileComplete: Boolean,
        onGoToParentProfile: () -> Unit,
        onGoToWelcome: () -> Unit,
    ) {
        if (sessionState.role == Role.Parent && sessionState.parentId != null && !parentProfileComplete) onGoToParentProfile()
        else onGoToWelcome()
    }
}

@Composable
private fun RequireAccess(
    requirement: AccessRequirement,
    contentPadding: PaddingValues,
    sessionState: SessionState,
    parentProfileComplete: Boolean,
    teacherProfileComplete: Boolean,
    onSessionChanged: (SessionState) -> Unit,
    onGoToWelcome: () -> Unit,
    onGoToGuestHome: () -> Unit,
    onGoToParentHome: () -> Unit,
    onGoToTeacherHome: () -> Unit,
    onGoToParentProfile: () -> Unit,
    onGoToTeacherProfile: () -> Unit,
    content: @Composable () -> Unit,
) {
    val logout = {
        onSessionChanged(SessionState())
        onGoToWelcome()
    }

    when (requirement) {
        AccessRequirement.None -> content()

        AccessRequirement.ParentLogin -> {
            if (sessionState.role == Role.Parent && sessionState.parentId != null) content()
            else {
                LoginRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToLogin = onGoToWelcome,
                    onContinueBrowsing = onGoToGuestHome,
                )
            }
        }

        AccessRequirement.ParentProfile -> {
            if (sessionState.role == Role.Parent && sessionState.parentId != null && parentProfileComplete) content()
            else if (sessionState.role == Role.Parent && sessionState.parentId != null && !parentProfileComplete) {
                ProfileRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToProfile = onGoToParentProfile,
                    onContinueBrowsing = onGoToParentHome,
                    onLogout = logout,
                )
            } else {
                LoginRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToLogin = onGoToWelcome,
                    onContinueBrowsing = onGoToGuestHome,
                )
            }
        }

        AccessRequirement.TeacherLogin -> {
            if (sessionState.role == Role.Teacher && sessionState.teacherId != null) content()
            else {
                LoginRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToLogin = onGoToWelcome,
                    onContinueBrowsing = onGoToGuestHome,
                )
            }
        }

        AccessRequirement.TeacherProfile -> {
            if (sessionState.role == Role.Teacher && sessionState.teacherId != null && teacherProfileComplete) content()
            else if (sessionState.role == Role.Teacher && sessionState.teacherId != null && !teacherProfileComplete) {
                ProfileRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToProfile = onGoToTeacherProfile,
                    onContinueBrowsing = onGoToTeacherHome,
                    onLogout = logout,
                )
            } else {
                LoginRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToLogin = onGoToWelcome,
                    onContinueBrowsing = onGoToGuestHome,
                )
            }
        }

        AccessRequirement.AdminLogin -> {
            if (sessionState.role == Role.Admin) content()
            else {
                LoginRequiredScreen(
                    contentPadding = contentPadding,
                    onGoToLogin = onGoToWelcome,
                    onContinueBrowsing = onGoToGuestHome,
                )
            }
        }
    }
}

@Composable
fun AppNavGraph(
    appRepository: AppRepository,
    sessionState: SessionState,
    onSessionChanged: (SessionState) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val factory = remember(appRepository) { AppViewModelFactory(appRepository) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val parentViewModel: ParentViewModel = viewModel(factory = factory)
    val teacherViewModel: TeacherViewModel = viewModel(factory = factory)

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route?.substringBefore("/")

    val goToWelcome = {
        navController.navigate(Routes.Welcome) {
            popUpTo(Routes.Welcome) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goToGuestHome = {
        navController.navigate(Routes.GuestHome) {
            popUpTo(Routes.GuestHome) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goToParentHome = {
        navController.navigate(Routes.ParentHome) {
            popUpTo(Routes.ParentHome) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goToTeacherHome = {
        navController.navigate(Routes.TeacherHome) {
            popUpTo(Routes.TeacherHome) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goToParentProfile = { navController.navigate(Routes.ParentProfile) { launchSingleTop = true } }
    val goToTeacherProfile = { navController.navigate(Routes.TeacherProfile) { launchSingleTop = true } }

    val parentProfileComplete = produceState(
        initialValue = true,
        key1 = sessionState.role,
        key2 = sessionState.parentId,
        key3 = currentRoute,
    ) {
        value = true
        if (sessionState.role == Role.Parent) {
            val id = sessionState.parentId
            value =
                if (id == null) {
                    false
                } else {
                    appRepository.getParentById(id)?.let(::isParentProfileComplete) == true
                }
        }
    }.value

    val teacherProfileComplete = produceState(
        initialValue = true,
        key1 = sessionState.role,
        key2 = sessionState.teacherId,
        key3 = currentRoute,
    ) {
        value = true
        if (sessionState.role == Role.Teacher) {
            val id = sessionState.teacherId
            value =
                if (id == null) {
                    false
                } else {
                    appRepository.getTeacherById(id)?.let(::isTeacherProfileComplete) == true
                }
        }
    }.value

    val parentRoutesWithBottomBar = setOf(
        Routes.ParentHome,
        Routes.TeacherSearch,
        Routes.TeacherList,
        Routes.TeacherDetail,
        Routes.ParentApplications,
        Routes.Payment,
        Routes.ParentMessages,
        Routes.ParentMe,
        Routes.ParentProfile,
        Routes.ParentPublish,
        Routes.Cart,
        Routes.Orders,
        Routes.OrderDetail,
        Routes.Products,
        Routes.ProductSearch,
        Routes.ProductDetail,
    )

    val teacherRoutesWithBottomBar = setOf(
        Routes.TeacherHome,
        Routes.TeacherNotifications,
        Routes.TeacherStudents,
        Routes.TeacherApplications,
        Routes.TeacherIncome,
        Routes.TeacherProfile,
    )

    val adminRoutesWithBottomBar = setOf(
        Routes.AdminUsers,
        Routes.AdminTeacherReview,
        Routes.AdminOrders,
        Routes.AdminReports,
        Routes.AdminStats,
        Routes.AdminDashboard,
        Routes.AdminParents,
        Routes.AdminTeachers,
        Routes.AdminApplications,
        Routes.AdminPayments,
        Routes.AdminProducts,
    )

    val guestTabRoutes = setOf(
        Routes.GuestHome,
        Routes.ProductSearch,
        Routes.Products,
    )

    val showBottomBar =
        (sessionState.role == Role.Parent && currentRoute in parentRoutesWithBottomBar) ||
            (sessionState.role == Role.Teacher && currentRoute in teacherRoutesWithBottomBar) ||
            (sessionState.role == Role.Admin && currentRoute in adminRoutesWithBottomBar) ||
            (sessionState.role == Role.Guest && currentRoute in guestTabRoutes)

    val startDestination =
        when (sessionState.role) {
            Role.Parent -> if (sessionState.parentId != null) Routes.ParentHome else Routes.Onboarding
            Role.Teacher -> if (sessionState.teacherId != null) Routes.TeacherHome else Routes.Onboarding
            Role.Admin -> Routes.AdminUsers
            Role.Guest -> Routes.GuestHome
            else -> Routes.Onboarding
        }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar && sessionState.role == Role.Parent) {
                val searchSelected = currentRoute in setOf(Routes.TeacherSearch, Routes.TeacherList, Routes.TeacherDetail)
                val productsSelected = currentRoute in setOf(Routes.Products, Routes.ProductSearch, Routes.ProductDetail)
                val publishSelected = currentRoute == Routes.ParentPublish
                val meSelected =
                    currentRoute in setOf(
                        Routes.ParentMe,
                        Routes.ParentProfile,
                        Routes.ParentMessages,
                        Routes.ParentApplications,
                        Routes.Payment,
                        Routes.Cart,
                        Routes.Orders,
                        Routes.OrderDetail,
                    )
                val itemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.ParentHome,
                        onClick = {
                            navController.navigate(Routes.ParentHome) {
                                popUpTo(Routes.ParentHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = null) },
                        label = { Text("首页", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = searchSelected,
                        onClick = {
                            navController.navigate(Routes.TeacherSearch) {
                                popUpTo(Routes.ParentHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                        label = { Text("搜索", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = publishSelected,
                        onClick = {
                            if (!publishSelected) {
                                navController.navigate(Routes.ParentPublish) {
                                    popUpTo(Routes.ParentHome) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            val container =
                                if (publishSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary
                            val content =
                                if (publishSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
                            Box(
                                modifier =
                                    Modifier
                                        .size(46.dp)
                                        .offset(y = (-4).dp)
                                        .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
                                        .background(container, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = "发布申请", tint = content)
                            }
                        },
                        label = { Text("发布需求", style = MaterialTheme.typography.labelSmall) },
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent,
                            ),
                        alwaysShowLabel = true,
                    )
                    NavigationBarItem(
                        selected = productsSelected,
                        onClick = {
                            navController.navigate(Routes.Products) {
                                popUpTo(Routes.ParentHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) },
                        label = { Text("商品", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = meSelected,
                        onClick = {
                            navController.navigate(Routes.ParentMe) {
                                popUpTo(Routes.ParentHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                        label = { Text("我的", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                }
            }
            if (showBottomBar && sessionState.role == Role.Teacher) {
                val coursesSelected = currentRoute in setOf(Routes.TeacherCourses, Routes.TeacherApplications)
                val meSelected = currentRoute in setOf(Routes.TeacherMe, Routes.TeacherProfile, Routes.TeacherIncome, Routes.TeacherNotifications)
                val itemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.TeacherHome,
                        onClick = {
                            navController.navigate(Routes.TeacherHome) {
                                popUpTo(Routes.TeacherHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = null) },
                        label = { Text("首页", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.TeacherDemands,
                        onClick = {
                            navController.navigate(Routes.TeacherDemands) {
                                popUpTo(Routes.TeacherHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text("需求", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.TeacherStudents,
                        onClick = {
                            navController.navigate(Routes.TeacherStudents) {
                                popUpTo(Routes.TeacherHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.People, contentDescription = null) },
                        label = { Text("学员", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = coursesSelected,
                        onClick = {
                            navController.navigate(Routes.TeacherCourses) {
                                popUpTo(Routes.TeacherHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.EventNote, contentDescription = null) },
                        label = { Text("课程", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = meSelected,
                        onClick = {
                            navController.navigate(Routes.TeacherMe) {
                                popUpTo(Routes.TeacherHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                        label = { Text("我的", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                }
            }
            if (showBottomBar && sessionState.role == Role.Admin) {
                val usersSelected = currentRoute in setOf(Routes.AdminUsers, Routes.AdminParents, Routes.AdminTeachers)
                val ordersSelected = currentRoute in setOf(Routes.AdminOrders, Routes.AdminApplications, Routes.AdminPayments)
                val statsSelected = currentRoute in setOf(Routes.AdminStats, Routes.AdminDashboard)
                val itemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    NavigationBarItem(
                        selected = usersSelected,
                        onClick = {
                            navController.navigate(Routes.AdminUsers) {
                                popUpTo(Routes.AdminUsers) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Group, contentDescription = null) },
                        label = { Text("用户", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.AdminTeacherReview,
                        onClick = {
                            navController.navigate(Routes.AdminTeacherReview) {
                                popUpTo(Routes.AdminUsers) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Verified, contentDescription = null) },
                        label = { Text("审核", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = ordersSelected,
                        onClick = {
                            navController.navigate(Routes.AdminOrders) {
                                popUpTo(Routes.AdminUsers) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.ReceiptLong, contentDescription = null) },
                        label = { Text("订单", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.AdminReports,
                        onClick = {
                            navController.navigate(Routes.AdminReports) {
                                popUpTo(Routes.AdminUsers) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Report, contentDescription = null) },
                        label = { Text("反馈", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = statsSelected,
                        onClick = {
                            navController.navigate(Routes.AdminStats) {
                                popUpTo(Routes.AdminUsers) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.BarChart, contentDescription = null) },
                        label = { Text("统计", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                }
            }
            if (showBottomBar && sessionState.role == Role.Guest) {
                val itemColors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = WindowInsets.navigationBars,
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Routes.GuestHome,
                        onClick = {
                            navController.navigate(Routes.GuestHome) {
                                popUpTo(Routes.GuestHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = null) },
                        label = { Text("主页", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.ProductSearch,
                        onClick = {
                            navController.navigate(Routes.ProductSearch) {
                                popUpTo(Routes.GuestHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                        label = { Text("搜索", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.Products,
                        onClick = {
                            navController.navigate(Routes.Products) {
                                popUpTo(Routes.GuestHome) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = null) },
                        label = { Text("商品", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.Welcome) },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = null) },
                        label = { Text("登录", style = MaterialTheme.typography.labelSmall) },
                        colors = itemColors,
                    )
                }
            }
        },
    ) { innerPadding: PaddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
        composable(Routes.Onboarding) {
            OnboardingScreen(
                contentPadding = innerPadding,
                onLogin = { navController.navigate(Routes.LoginRoleSelection) },
                onRegister = { navController.navigate(Routes.RegisterRoleSelection) },
                onGuest = {
                    onSessionChanged(SessionState(role = Role.Guest))
                    navController.navigate(Routes.GuestHome) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
                onFinished = {
                    navController.navigate(Routes.Welcome) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.Welcome) {
            WelcomeScreen(
                contentPadding = innerPadding,
                onLogin = { navController.navigate(Routes.LoginRoleSelection) },
                onRegister = { navController.navigate(Routes.RegisterRoleSelection) },
                onGuest = {
                    onSessionChanged(SessionState(role = Role.Guest))
                    navController.navigate(Routes.GuestHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.LoginRoleSelection) {
            RoleSelectionScreen(
                contentPadding = innerPadding,
                title = "选择登录身份",
                subtitle = "选择你要登录的角色",
                parentActionText = "家长登录",
                teacherActionText = "老师登录",
                adminActionText = "管理员登录",
                onParent = { navController.navigate(Routes.loginTabs("parent")) },
                onTeacher = { navController.navigate(Routes.loginTabs("teacher")) },
                onAdmin = { navController.navigate(Routes.loginTabs("admin")) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.RegisterRoleSelection) {
            RoleSelectionScreen(
                contentPadding = innerPadding,
                title = "选择注册身份",
                subtitle = "选择你要注册的角色（管理员仅支持登录）",
                parentActionText = "家长注册",
                teacherActionText = "老师注册",
                adminActionText = "管理员登录",
                onParent = { navController.navigate(Routes.ParentRegister) },
                onTeacher = { navController.navigate(Routes.TeacherRegister) },
                onAdmin = { navController.navigate(Routes.AdminLogin) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = "${Routes.LoginTabs}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) { entry ->
            val role = entry.arguments?.getString("role") ?: "parent"
            LoginTabsScreen(
                contentPadding = innerPadding,
                authViewModel = authViewModel,
                adminPassword = ADMIN_PASSWORD,
                initialRole = role,
                onParentSuccess = { parent, accessToken ->
                    onSessionChanged(SessionState(role = Role.Parent, parentId = parent.id, accessToken = accessToken))
                    parentViewModel.setParentId(parent.id)
                    navController.navigate(Routes.ParentHome) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
                onTeacherSuccess = { teacher, accessToken ->
                    onSessionChanged(SessionState(role = Role.Teacher, teacherId = teacher.id, accessToken = accessToken))
                    teacherViewModel.setTeacherId(teacher.id)
                    navController.navigate(Routes.TeacherHome) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
                onAdminSuccess = {
                    onSessionChanged(SessionState(role = Role.Admin))
                    navController.navigate(Routes.AdminUsers) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Routes.RegisterRoleSelection) },
                onGuest = {
                    onSessionChanged(SessionState(role = Role.Guest))
                    navController.navigate(Routes.GuestHome) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                },
                onBackToOnboarding = { navController.navigate(Routes.Onboarding) },
            )
        }

        composable(Routes.RoleSelection) {
            WelcomeScreen(
                contentPadding = innerPadding,
                onLogin = { navController.navigate(Routes.LoginRoleSelection) },
                onRegister = { navController.navigate(Routes.RegisterRoleSelection) },
                onGuest = {
                    onSessionChanged(SessionState(role = Role.Guest))
                    navController.navigate(Routes.GuestHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.GuestHome) {
            GuestHomeScreen(
                contentPadding = innerPadding,
                onBrowseTeachers = { navController.navigate(Routes.TeacherList) },
                onSearchTeachers = { navController.navigate(Routes.TeacherSearch) },
                onProducts = { navController.navigate(Routes.Products) },
                onSearchProducts = { navController.navigate(Routes.ProductSearch) },
                onGoLogin = { navController.navigate(Routes.Welcome) },
                onExitGuest = {
                    onSessionChanged(SessionState())
                    navController.navigate(Routes.Welcome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.ParentLogin) {
            ParentLoginScreen(
                contentPadding = innerPadding,
                authViewModel = authViewModel,
                onLoginSuccess = { parent, accessToken ->
                    onSessionChanged(SessionState(role = Role.Parent, parentId = parent.id, accessToken = accessToken))
                    parentViewModel.setParentId(parent.id)
                    navController.navigate(Routes.ParentHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.ParentRegister) },
            )
        }
        composable(Routes.ParentRegister) {
            ParentRegisterScreen(
                contentPadding = innerPadding,
                authViewModel = authViewModel,
                onRegisterSuccess = { parent, accessToken ->
                    onSessionChanged(SessionState(role = Role.Parent, parentId = parent.id, accessToken = accessToken))
                    parentViewModel.setParentId(parent.id)
                    navController.navigate(Routes.ParentHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.ParentHome) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentHome),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val parentHomeViewModel: ParentHomeViewModel = viewModel(factory = factory)
                ParentHomeScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    parentViewModel = parentViewModel,
                    parentHomeViewModel = parentHomeViewModel,
                    onGoSearchTeachers = { navController.navigate(Routes.TeacherSearch) },
                    onGoMyApplications = { navController.navigate(Routes.ParentApplications) },
                    onGoCart = { navController.navigate(Routes.Cart) },
                    onGoOrders = { navController.navigate(Routes.Orders) },
                    onTeacherClick = { teacherId -> navController.navigate(Routes.teacherDetail(teacherId)) },
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(Routes.ParentMessages) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentMessages),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val notificationViewModel: NotificationViewModel = viewModel(factory = factory)
                ParentMessagesScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    notificationViewModel = notificationViewModel,
                    onOpenAppointments = { navController.navigate(Routes.ParentApplications) },
                    onOpenOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                )
            }
        }
        composable(Routes.ParentPublish) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentPublish),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val parentId = sessionState.parentId ?: return@RequireAccess
                ParentPublishApplicationScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    appRepository = appRepository,
                    parentId = parentId,
                    onGoSearchTeachers = { navController.navigate(Routes.TeacherSearch) },
                    onGoMyApplications = { navController.navigate(Routes.ParentApplications) },
                    onGoEditProfile = { navController.navigate(Routes.ParentProfile) },
                )
            }
        }
        composable(Routes.ParentMe) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentMe),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                ParentMeScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    appRepository = appRepository,
                    parentViewModel = parentViewModel,
                    profileComplete = parentProfileComplete,
                    onEditProfile = { navController.navigate(Routes.ParentProfile) },
                    onMyApplications = { navController.navigate(Routes.ParentApplications) },
                    onCart = { navController.navigate(Routes.Cart) },
                    onOrders = { navController.navigate(Routes.Orders) },
                    onReport = { navController.navigate(Routes.reportSubmit("parent")) },
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(
            route = "${Routes.ReportSubmit}/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType }),
        ) { entry ->
            val role = entry.arguments?.getString("role") ?: "guest"
            val requirement =
                when (role) {
                    "parent" -> AccessRequirement.ParentLogin
                    "teacher" -> AccessRequirement.TeacherLogin
                    else -> AccessRequirement.None
                }
            if (requirement == AccessRequirement.None) {
                LoginRequiredScreen(
                    contentPadding = innerPadding,
                    onGoToLogin = goToWelcome,
                    onContinueBrowsing = goToGuestHome,
                )
            } else {
                RequireAccess(
                    requirement = requirement,
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    parentProfileComplete = parentProfileComplete,
                    teacherProfileComplete = teacherProfileComplete,
                    onSessionChanged = onSessionChanged,
                    onGoToWelcome = goToWelcome,
                    onGoToGuestHome = goToGuestHome,
                    onGoToParentHome = goToParentHome,
                    onGoToTeacherHome = goToTeacherHome,
                    onGoToParentProfile = goToParentProfile,
                    onGoToTeacherProfile = goToTeacherProfile,
                ) {
                    val reportViewModel: ReportViewModel = viewModel(factory = factory)
                    val reporterRole = if (role == "parent") "PARENT" else "TEACHER"
                    val reporterId = if (role == "parent") sessionState.parentId else sessionState.teacherId
                    val reporterPhone = produceState<String?>(initialValue = null, key1 = reporterRole, key2 = reporterId) {
                        value =
                            when (role) {
                                "parent" -> reporterId?.let { appRepository.getParentById(it)?.phone }
                                else -> reporterId?.let { appRepository.getTeacherById(it)?.phone }
                            }
                    }.value
                    ReportSubmitScreen(
                        contentPadding = innerPadding,
                        title = "举报 / 反馈",
                        reporterRole = reporterRole,
                        reporterId = reporterId,
                        reporterPhone = reporterPhone,
                        reportViewModel = reportViewModel,
                        onDone = { navController.popBackStack() },
                    )
                }
            }
        }
        composable(Routes.ParentProfile) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentProfile),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val parentId = sessionState.parentId ?: return@RequireAccess
                ParentProfileScreen(
                    contentPadding = innerPadding,
                    appRepository = appRepository,
                    parentId = parentId,
                    parentViewModel = parentViewModel,
                    title = if (parentProfileComplete) "我的信息" else "完善资料",
                    onProfileCompleted = if (parentProfileComplete) null else {
                        {
                            navController.navigate(Routes.ParentHome) {
                                popUpTo(Routes.ParentProfile) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                )
            }
        }
        composable(Routes.TeacherList) {
            TeacherListScreen(
                contentPadding = innerPadding,
                parentId = sessionState.parentId,
                parentViewModel = parentViewModel,
                onSearch = { navController.navigate(Routes.TeacherSearch) },
                onTeacherClick = { teacherId -> navController.navigate(Routes.teacherDetail(teacherId)) },
            )
        }
        composable(Routes.TeacherSearch) {
            TeacherSearchScreen(
                contentPadding = innerPadding,
                parentId = sessionState.parentId,
                parentViewModel = parentViewModel,
                onTeacherClick = { teacherId -> navController.navigate(Routes.teacherDetail(teacherId)) },
            )
        }

        composable(Routes.Products) {
            val productViewModel: ProductViewModel = viewModel(factory = factory)
            ProductListScreen(
                contentPadding = innerPadding,
                productViewModel = productViewModel,
                onProductClick = { productId -> navController.navigate(Routes.productDetail(productId)) },
            )
        }
        composable(Routes.ProductSearch) {
            val productViewModel: ProductViewModel = viewModel(factory = factory)
            ProductSearchScreen(
                contentPadding = innerPadding,
                productViewModel = productViewModel,
                onProductClick = { productId -> navController.navigate(Routes.productDetail(productId)) },
            )
        }
        composable(
            route = "${Routes.ProductDetail}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType }),
        ) { entry ->
            val productId = entry.arguments?.getInt("productId") ?: return@composable
            val canPurchase = AccessPolicy.canSubmitApplication(sessionState, parentProfileComplete)
            val commerceViewModel: CommerceViewModel = viewModel(factory = factory)
            LaunchedEffect(sessionState.parentId) {
                sessionState.parentId?.let { commerceViewModel.setParentId(it) }
            }
            ProductDetailScreen(
                contentPadding = innerPadding,
                productId = productId,
                appRepository = appRepository,
                commerceViewModel = commerceViewModel,
                canPurchase = canPurchase,
                onRequireLogin = {
                    AccessPolicy.onRequireParentProfileOrLogin(
                        sessionState = sessionState,
                        parentProfileComplete = parentProfileComplete,
                        onGoToParentProfile = goToParentProfile,
                        onGoToWelcome = goToWelcome,
                    )
                },
                onGoToCart = { navController.navigate(Routes.Cart) },
                onGoToOrder = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
            )
        }

        composable(Routes.Cart) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.Cart),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val commerceViewModel: CommerceViewModel = viewModel(factory = factory)
                LaunchedEffect(sessionState.parentId) {
                    sessionState.parentId?.let { commerceViewModel.setParentId(it) }
                }
                CartScreen(
                    contentPadding = innerPadding,
                    commerceViewModel = commerceViewModel,
                    onOrderCreated = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                )
            }
        }
        composable(Routes.Orders) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.Orders),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val commerceViewModel: CommerceViewModel = viewModel(factory = factory)
                LaunchedEffect(sessionState.parentId) {
                    sessionState.parentId?.let { commerceViewModel.setParentId(it) }
                }
                OrdersScreen(
                    contentPadding = innerPadding,
                    commerceViewModel = commerceViewModel,
                    onOrderClick = { orderId -> navController.navigate(Routes.orderDetail(orderId)) },
                )
            }
        }
        composable(
            route = "${Routes.OrderDetail}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType }),
        ) { entry ->
            val orderId = entry.arguments?.getInt("orderId") ?: return@composable
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.OrderDetail),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val commerceViewModel: CommerceViewModel = viewModel(factory = factory)
                LaunchedEffect(sessionState.parentId) {
                    sessionState.parentId?.let { commerceViewModel.setParentId(it) }
                }
                OrderDetailScreen(
                    contentPadding = innerPadding,
                    orderId = orderId,
                    commerceViewModel = commerceViewModel,
                )
            }
        }
        composable(
            route = "${Routes.TeacherDetail}/{teacherId}",
            arguments = listOf(navArgument("teacherId") { type = NavType.IntType }),
        ) { entry ->
            val teacherId = entry.arguments?.getInt("teacherId") ?: return@composable
            val canSubmitApplication = AccessPolicy.canSubmitApplication(sessionState, parentProfileComplete)
            TeacherDetailScreen(
                contentPadding = innerPadding,
                teacherId = teacherId,
                appRepository = appRepository,
                parentViewModel = parentViewModel,
                canSubmitApplication = canSubmitApplication,
                onRequireLogin = {
                    AccessPolicy.onRequireParentProfileOrLogin(
                        sessionState = sessionState,
                        parentProfileComplete = parentProfileComplete,
                        onGoToParentProfile = goToParentProfile,
                        onGoToWelcome = goToWelcome,
                    )
                },
            )
        }
        composable(Routes.ParentApplications) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.ParentApplications),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                ParentApplicationListScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    onPay = { applicationId -> navController.navigate(Routes.payment(applicationId)) },
                    onChat = { applicationId -> navController.navigate(Routes.chat(applicationId)) },
                )
            }
        }
        composable(
            route = "${Routes.Payment}/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.IntType }),
        ) { entry ->
            val applicationId = entry.arguments?.getInt("applicationId") ?: return@composable
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.Payment),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                PaymentScreen(
                    contentPadding = innerPadding,
                    applicationId = applicationId,
                    appRepository = appRepository,
                    parentViewModel = parentViewModel,
                )
            }
        }

        composable(Routes.TeacherLogin) {
            TeacherLoginScreen(
                contentPadding = innerPadding,
                authViewModel = authViewModel,
                onLoginSuccess = { teacher, accessToken ->
                    onSessionChanged(SessionState(role = Role.Teacher, teacherId = teacher.id, accessToken = accessToken))
                    teacherViewModel.setTeacherId(teacher.id)
                    navController.navigate(Routes.TeacherHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
                onRegister = { navController.navigate(Routes.TeacherRegister) },
            )
        }
        composable(Routes.TeacherRegister) {
            TeacherRegisterScreen(
                contentPadding = innerPadding,
                authViewModel = authViewModel,
                onRegisterSuccess = { teacher, accessToken ->
                    onSessionChanged(SessionState(role = Role.Teacher, teacherId = teacher.id, accessToken = accessToken))
                    teacherViewModel.setTeacherId(teacher.id)
                    navController.navigate(Routes.TeacherHome) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() },
            )
        }
        composable(Routes.TeacherHome) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherHome),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val teacherHomeViewModel: TeacherHomeViewModel = viewModel(factory = factory)
                val notificationViewModel: NotificationViewModel = viewModel(factory = factory)
                TeacherHomeScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    teacherViewModel = teacherViewModel,
                    teacherHomeViewModel = teacherHomeViewModel,
                    notificationViewModel = notificationViewModel,
                    onMyProfile = { navController.navigate(Routes.TeacherProfile) },
                    onDemands = { navController.navigate(Routes.TeacherDemands) },
                    onCourses = { navController.navigate(Routes.TeacherCourses) },
                    onNotifications = { navController.navigate(Routes.TeacherNotifications) },
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(Routes.TeacherDemands) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherDemands),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val teacherHomeViewModel: TeacherHomeViewModel = viewModel(factory = factory)
                TeacherDemandsScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    teacherHomeViewModel = teacherHomeViewModel,
                    onChat = { applicationId -> navController.navigate(Routes.chat(applicationId)) },
                )
            }
        }
        composable(Routes.TeacherNotifications) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherNotifications),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val notificationViewModel: NotificationViewModel = viewModel(factory = factory)
                TeacherNotificationsScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    notificationViewModel = notificationViewModel,
                    onOpenApplications = { navController.navigate(Routes.TeacherApplications) },
                )
            }
        }
        composable(Routes.TeacherStudents) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherStudents),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                TeacherStudentsScreen(
                    contentPadding = innerPadding,
                    teacherViewModel = teacherViewModel,
                    onOpenOrders = { navController.navigate(Routes.TeacherApplications) },
                )
            }
        }
        composable(Routes.TeacherCourses) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherCourses),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                TeacherApplicationListScreen(
                    contentPadding = innerPadding,
                    teacherViewModel = teacherViewModel,
                    onChat = { applicationId -> navController.navigate(Routes.chat(applicationId)) },
                    title = "课程",
                )
            }
        }
        composable(Routes.TeacherMe) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherMe),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                TeacherMeScreen(
                    contentPadding = innerPadding,
                    sessionState = sessionState,
                    appRepository = appRepository,
                    teacherViewModel = teacherViewModel,
                    onOpenDemands = { navController.navigate(Routes.TeacherDemands) },
                    onPreviewPublicProfile = { navController.navigate(Routes.TeacherPublicProfile) },
                    onEditProfile = { navController.navigate(Routes.TeacherProfile) },
                    onOpenIncome = { navController.navigate(Routes.TeacherIncome) },
                    onOpenApplications = { navController.navigate(Routes.TeacherCourses) },
                    onOpenNotifications = { navController.navigate(Routes.TeacherNotifications) },
                    onReport = { navController.navigate(Routes.reportSubmit("teacher")) },
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(Routes.TeacherPublicProfile) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherPublicProfile),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val teacherId = sessionState.teacherId ?: return@RequireAccess
                TeacherPublicProfileScreen(
                    contentPadding = innerPadding,
                    teacherId = teacherId,
                    appRepository = appRepository,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Routes.TeacherIncome) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherIncome),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val teacherId = sessionState.teacherId ?: return@RequireAccess
                val teacherIncomeViewModel: TeacherIncomeViewModel = viewModel(factory = factory)
                LaunchedEffect(teacherId) {
                    teacherIncomeViewModel.setTeacherId(teacherId)
                }
                TeacherIncomeScreen(
                    contentPadding = innerPadding,
                    teacherId = teacherId,
                    teacherIncomeViewModel = teacherIncomeViewModel,
                    teacherViewModel = teacherViewModel,
                )
            }
        }
        composable(Routes.TeacherProfile) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherProfile),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val teacherId = sessionState.teacherId ?: return@RequireAccess
                TeacherProfileScreen(
                    contentPadding = innerPadding,
                    appRepository = appRepository,
                    teacherId = teacherId,
                    teacherViewModel = teacherViewModel,
                    title = if (teacherProfileComplete) "我的资料" else "完善资料",
                    onProfileCompleted = if (teacherProfileComplete) null else {
                        {
                            navController.navigate(Routes.TeacherHome) {
                                popUpTo(Routes.TeacherProfile) { inclusive = true }
                            }
                        }
                    },
                    onReport = { navController.navigate(Routes.reportSubmit("teacher")) },
                )
            }
        }
        composable(Routes.TeacherApplications) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.TeacherApplications),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                TeacherApplicationListScreen(
                    contentPadding = innerPadding,
                    teacherViewModel = teacherViewModel,
                    onChat = { applicationId -> navController.navigate(Routes.chat(applicationId)) },
                )
            }
        }

        composable(
            route = "${Routes.Chat}/{applicationId}",
            arguments = listOf(navArgument("applicationId") { type = NavType.IntType }),
        ) { entry ->
            val applicationId = entry.arguments?.getInt("applicationId") ?: return@composable
            ChatScreen(
                contentPadding = innerPadding,
                sessionState = sessionState,
                applicationId = applicationId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.AdminLogin) {
            AdminLoginScreen(
                contentPadding = innerPadding,
                adminPassword = ADMIN_PASSWORD,
                onSuccess = {
                    onSessionChanged(SessionState(role = Role.Admin))
                    navController.navigate(Routes.AdminUsers) {
                        popUpTo(Routes.Welcome) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.AdminUsers) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminUsers),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminUsersScreen(contentPadding = innerPadding, adminViewModel = adminViewModel)
            }
        }
        composable(Routes.AdminTeacherReview) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminTeacherReview),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminTeacherReviewScreen(contentPadding = innerPadding, adminViewModel = adminViewModel)
            }
        }
        composable(Routes.AdminOrders) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminOrders),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminOrdersScreen(contentPadding = innerPadding, adminViewModel = adminViewModel)
            }
        }
        composable(Routes.AdminReports) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminReports),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminReportsScreen(contentPadding = innerPadding, adminViewModel = adminViewModel)
            }
        }
        composable(Routes.AdminStats) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminStats),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminStatsScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(Routes.AdminDashboard) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminDashboard),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminStatsScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                    onLogout = {
                        onSessionChanged(SessionState())
                        goToWelcome()
                    },
                )
            }
        }
        composable(Routes.AdminParents) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminParents),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminParentListScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                )
            }
        }
        composable(Routes.AdminTeachers) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminTeachers),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminTeacherListScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                )
            }
        }
        composable(Routes.AdminApplications) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminApplications),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminApplicationListScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                )
            }
        }
        composable(Routes.AdminPayments) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminPayments),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val adminViewModel: AdminViewModel = viewModel(factory = factory)
                AdminPaymentRecordScreen(
                    contentPadding = innerPadding,
                    adminViewModel = adminViewModel,
                )
            }
        }
        composable(Routes.AdminProducts) {
            RequireAccess(
                requirement = AccessPolicy.requirement(Routes.AdminProducts),
                contentPadding = innerPadding,
                sessionState = sessionState,
                parentProfileComplete = parentProfileComplete,
                teacherProfileComplete = teacherProfileComplete,
                onSessionChanged = onSessionChanged,
                onGoToWelcome = goToWelcome,
                onGoToGuestHome = goToGuestHome,
                onGoToParentHome = goToParentHome,
                onGoToTeacherHome = goToTeacherHome,
                onGoToParentProfile = goToParentProfile,
                onGoToTeacherProfile = goToTeacherProfile,
            ) {
                val productViewModel: ProductViewModel = viewModel(factory = factory)
                AdminProductManageScreen(
                    contentPadding = innerPadding,
                    productViewModel = productViewModel,
                )
            }
        }
    }
    }
}
