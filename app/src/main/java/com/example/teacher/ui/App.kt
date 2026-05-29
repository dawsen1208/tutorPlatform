package com.example.teacher.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import com.example.teacher.data.AppRepository
import com.example.teacher.data.BackendApi
import com.example.teacher.ui.navigation.AppNavGraph
import com.example.teacher.ui.theme.JiaonilaileTheme

enum class Role {
    Parent,
    Teacher,
    Admin,
    Guest,
}

data class SessionState(
    val role: Role? = null,
    val parentId: Int? = null,
    val teacherId: Int? = null,
    val accessToken: String? = null,
    val remoteUserId: Int? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
)

private const val SESSION_PREFS = "laoshilaile_session"
private const val SESSION_ROLE = "role"
private const val SESSION_PARENT_ID = "parentId"
private const val SESSION_TEACHER_ID = "teacherId"
private const val SESSION_ACCESS_TOKEN = "accessToken"
private const val SESSION_REMOTE_USER_ID = "remoteUserId"
private const val SESSION_NICKNAME = "nickname"
private const val SESSION_AVATAR_URL = "avatarUrl"

private fun loadSession(context: Context): SessionState {
    val prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
    val token = prefs.getString(SESSION_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }
    val remoteUserId = prefs.getInt(SESSION_REMOTE_USER_ID, 0).takeIf { it > 0 }
    val nickname = prefs.getString(SESSION_NICKNAME, null)?.takeIf { it.isNotBlank() }
    val avatarUrl = prefs.getString(SESSION_AVATAR_URL, null)?.takeIf { it.isNotBlank() }
    return when (prefs.getString(SESSION_ROLE, null)) {
        "PARENT" ->
            SessionState(
                role = Role.Parent,
                parentId = prefs.getInt(SESSION_PARENT_ID, 0).takeIf { it > 0 },
                accessToken = token,
                remoteUserId = remoteUserId,
                nickname = nickname,
                avatarUrl = avatarUrl,
            )
        "TEACHER" ->
            SessionState(
                role = Role.Teacher,
                teacherId = prefs.getInt(SESSION_TEACHER_ID, 0).takeIf { it > 0 },
                accessToken = token,
                remoteUserId = remoteUserId,
                nickname = nickname,
                avatarUrl = avatarUrl,
            )
        "ADMIN" -> SessionState(role = Role.Admin, accessToken = token, remoteUserId = remoteUserId, nickname = nickname, avatarUrl = avatarUrl)
        else -> SessionState()
    }
}

private fun persistSession(context: Context, session: SessionState) {
    val prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
    val editor = prefs.edit()
    when (session.role) {
        Role.Parent -> {
            val id = session.parentId
            if (id != null && id > 0) {
                editor.putString(SESSION_ROLE, "PARENT")
                editor.putInt(SESSION_PARENT_ID, id)
                editor.remove(SESSION_TEACHER_ID)
                editor.putString(SESSION_ACCESS_TOKEN, session.accessToken)
                editor.putInt(SESSION_REMOTE_USER_ID, session.remoteUserId ?: 0)
                editor.putString(SESSION_NICKNAME, session.nickname)
                editor.putString(SESSION_AVATAR_URL, session.avatarUrl)
            } else {
                editor.clear()
            }
        }

        Role.Teacher -> {
            val id = session.teacherId
            if (id != null && id > 0) {
                editor.putString(SESSION_ROLE, "TEACHER")
                editor.putInt(SESSION_TEACHER_ID, id)
                editor.remove(SESSION_PARENT_ID)
                editor.putString(SESSION_ACCESS_TOKEN, session.accessToken)
                editor.putInt(SESSION_REMOTE_USER_ID, session.remoteUserId ?: 0)
                editor.putString(SESSION_NICKNAME, session.nickname)
                editor.putString(SESSION_AVATAR_URL, session.avatarUrl)
            } else {
                editor.clear()
            }
        }

        Role.Admin -> {
            editor.putString(SESSION_ROLE, "ADMIN")
            editor.remove(SESSION_PARENT_ID)
            editor.remove(SESSION_TEACHER_ID)
            editor.putString(SESSION_ACCESS_TOKEN, session.accessToken)
            editor.putInt(SESSION_REMOTE_USER_ID, session.remoteUserId ?: 0)
            editor.putString(SESSION_NICKNAME, session.nickname)
            editor.putString(SESSION_AVATAR_URL, session.avatarUrl)
        }

        else -> editor.clear()
    }
    editor.apply()
}

@Composable
fun JiaonilaileApp(
    appRepository: AppRepository,
    modifier: Modifier = Modifier,
    applyTheme: Boolean = true,
) {
    val context = LocalContext.current
    val session = remember { mutableStateOf(loadSession(context)) }
    val onSessionChanged: (SessionState) -> Unit = { next ->
        session.value = next
        persistSession(context, next)
    }

    var checkingToken by remember { mutableStateOf(false) }
    LaunchedEffect(session.value.role, session.value.accessToken) {
        val role = session.value.role
        val token = session.value.accessToken?.trim().orEmpty()
        val shouldCheck = (role == Role.Parent || role == Role.Teacher) && token.isNotBlank()
        if (!shouldCheck) {
            checkingToken = false
            return@LaunchedEffect
        }

        checkingToken = true
        val me =
            runCatching { BackendApi.me(token) }
                .getOrNull()
                ?.user
        if (me == null) {
            onSessionChanged(SessionState())
            checkingToken = false
            return@LaunchedEffect
        }

        val backendRole =
            when (me.role.trim().uppercase()) {
                "PARENT" -> Role.Parent
                "TEACHER" -> Role.Teacher
                else -> null
            }
        if (backendRole == null || backendRole != role) {
            onSessionChanged(SessionState())
            checkingToken = false
            return@LaunchedEffect
        }

        val next =
            session.value.copy(
                remoteUserId = me.id,
                nickname = me.nickname,
                avatarUrl = me.avatarUrl,
            )
        if (next != session.value) onSessionChanged(next)
        checkingToken = false
    }

    val content: @Composable () -> Unit = content@{
        if (checkingToken) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@content
        }
        AppNavGraph(
            appRepository = appRepository,
            sessionState = session.value,
            onSessionChanged = onSessionChanged,
            modifier = modifier,
        )
    }

    if (applyTheme) {
        JiaonilaileTheme { content() }
    } else {
        content()
    }
}
