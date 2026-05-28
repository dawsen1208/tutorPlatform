package com.example.teacher

import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer
import com.example.teacher.data.AppRepository
import com.example.teacher.data.BackendApi
import com.example.teacher.data.local.AppDatabase
import com.example.teacher.ui.JiaonilaileApp
import com.example.teacher.ui.theme.JiaonilaileTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        val prefs = getSharedPreferences("startup_debug_prefs", Context.MODE_PRIVATE)
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            prefs.edit().putString("last_crash", stackTraceString(e)).apply()
            previousHandler?.uncaughtException(t, e)
        }

        runCatching {
            MapsInitializer.updatePrivacyShow(this, true, true)
            MapsInitializer.updatePrivacyAgree(this, true)
            AMapLocationClient.updatePrivacyShow(this, true, true)
            AMapLocationClient.updatePrivacyAgree(this, true)
        }

        enableEdgeToEdge()
        setContent {
            JiaonilaileTheme {
                AppBootstrap()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(
                "demand_updates",
                "家教需求通知",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        manager.createNotificationChannel(channel)
    }
}

private sealed class StartupState {
    data object Loading : StartupState()
    data class Ready(val repository: AppRepository) : StartupState()
    data class Error(val message: String) : StartupState()
}

@Composable
private fun AppBootstrap() {
    val context = LocalContext.current
    remember(context) { BackendApi.init(context) }
    val crashPrefs = remember { context.getSharedPreferences("startup_debug_prefs", Context.MODE_PRIVATE) }
    var lastCrash by remember { mutableStateOf(crashPrefs.getString("last_crash", null)) }
    var state by remember { mutableStateOf<StartupState>(StartupState.Loading) }
    var retryKey by remember { mutableStateOf(0) }

    if (lastCrash != null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(text = "检测到上次崩溃", style = MaterialTheme.typography.titleMedium)
                Text(text = lastCrash.orEmpty(), style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = {
                        crashPrefs.edit().remove("last_crash").apply()
                        lastCrash = null
                        retryKey += 1
                    },
                ) { Text("清除并继续启动") }
            }
        }
        return
    }

    androidx.compose.runtime.LaunchedEffect(retryKey) {
        state = StartupState.Loading
        state =
            try {
                val repository =
                    withContext(Dispatchers.IO) {
                        val database = AppDatabase.getInstance(context)
                        AppRepository(database)
                    }
                StartupState.Ready(repository)
            } catch (t: Throwable) {
                StartupState.Error(t.javaClass.simpleName + "：" + (t.message ?: "初始化失败"))
            }
    }

    when (val s = state) {
        is StartupState.Ready -> {
            JiaonilaileApp(appRepository = s.repository, applyTheme = false)
        }

        StartupState.Loading -> {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                    Text(text = "正在初始化…", modifier = Modifier.padding(top = 16.dp))
                }
            }
        }

        is StartupState.Error -> {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "启动失败", style = MaterialTheme.typography.titleMedium)
                    Text(text = s.message, modifier = Modifier.padding(top = 10.dp))
                    Column(
                        modifier = Modifier.padding(top = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            onClick = {
                                retryKey += 1
                            },
                        ) { Text("重试") }
                        Button(
                            onClick = {
                                context.deleteDatabase("tutor_platform.db")
                                context.getSharedPreferences("laoshilaile_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                                retryKey += 1
                            },
                        ) { Text("清除本地数据") }
                    }
                }
            }
        }
    }
}

private fun stackTraceString(t: Throwable): String {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    t.printStackTrace(pw)
    pw.flush()
    return sw.toString().take(4000)
}
