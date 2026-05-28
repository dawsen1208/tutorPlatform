package com.example.teacher.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.teacher.data.AppRepository
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
)

private const val SESSION_PREFS = "laoshilaile_session"
private const val SESSION_ROLE = "role"
private const val SESSION_PARENT_ID = "parentId"
private const val SESSION_TEACHER_ID = "teacherId"
private const val SESSION_ACCESS_TOKEN = "accessToken"

private fun loadSession(context: Context): SessionState {
    val prefs = context.getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE)
    val token = prefs.getString(SESSION_ACCESS_TOKEN, null)?.takeIf { it.isNotBlank() }
    return when (prefs.getString(SESSION_ROLE, null)) {
        "PARENT" ->
            SessionState(
                role = Role.Parent,
                parentId = prefs.getInt(SESSION_PARENT_ID, 0).takeIf { it > 0 },
                accessToken = token,
            )
        "TEACHER" ->
            SessionState(
                role = Role.Teacher,
                teacherId = prefs.getInt(SESSION_TEACHER_ID, 0).takeIf { it > 0 },
                accessToken = token,
            )
        "ADMIN" -> SessionState(role = Role.Admin, accessToken = token)
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
            } else {
                editor.clear()
            }
        }

        Role.Admin -> {
            editor.putString(SESSION_ROLE, "ADMIN")
            editor.remove(SESSION_PARENT_ID)
            editor.remove(SESSION_TEACHER_ID)
            editor.putString(SESSION_ACCESS_TOKEN, session.accessToken)
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

    val content = @Composable {
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
