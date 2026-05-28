package com.example.teacher.data

import android.content.Context
import com.example.teacher.BuildConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.net.URI
import java.util.concurrent.TimeUnit

data class BackendUserDto(
    val id: Int,
    val role: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
)

data class BackendAuthResponse(
    val accessToken: String,
    val user: BackendUserDto,
)

data class BackendMeResponse(
    val user: BackendUserDto,
)

data class BackendRegisterRequest(
    val role: String,
    val phone: String,
    val password: String,
    val nickname: String? = null,
)

data class BackendLoginRequest(
    val role: String,
    val phone: String,
    val password: String,
)

data class BackendCreateApplicationRequest(
    val teacherId: Int,
)

data class BackendCreateDemandRequest(
    val subject: String,
    val studentGrade: String,
    val timeStartAt: Long,
    val timeEndAt: Long,
    val teacherGenderPreference: String? = null,
    val minPrice: Double,
    val maxPrice: Double,
)

data class BackendDemandDto(
    val id: Int,
    val parentId: Int,
    val subject: String,
    val studentGrade: String,
    val timeStartAt: String,
    val timeEndAt: String,
    val teacherGenderPreference: String? = null,
    val minPrice: Double,
    val maxPrice: Double,
    val status: String,
    val createdAt: String,
)

data class BackendCreateDemandResponse(
    val demand: BackendDemandDto,
)

data class BackendDemandListResponse(
    val items: List<BackendDemandDto>,
    val nextCursor: String? = null,
)

data class BackendClaimDemandResponse(
    val demand: BackendDemandDto,
    val application: BackendApplicationDto,
    val threadId: Int,
)

data class BackendApplicationDto(
    val id: Int,
    val parentId: Int,
    val teacherId: Int,
    val status: String,
    val createdAt: String,
)

data class BackendCreateApplicationResponse(
    val application: BackendApplicationDto,
    val threadId: Int,
)

data class BackendThreadResponse(
    val threadId: Int,
)

data class BackendChatMessageDto(
    val id: Int,
    val threadId: Int,
    val senderId: Int,
    val senderRole: String,
    val content: String,
    val createdAt: String,
)

data class BackendMessagesResponse(
    val items: List<BackendChatMessageDto>,
    val nextCursor: String? = null,
)

data class BackendApplicationListResponse(
    val items: List<BackendApplicationDto>,
    val nextCursor: String? = null,
)

private interface BackendService {
    @GET("/health")
    suspend fun health(): JsonObject

    @POST("/api/auth/register")
    suspend fun register(@Body body: BackendRegisterRequest): BackendAuthResponse

    @POST("/api/auth/login")
    suspend fun login(@Body body: BackendLoginRequest): BackendAuthResponse

    @GET("/api/me")
    suspend fun me(@Header("Authorization") authorization: String): BackendMeResponse

    @POST("/api/applications")
    suspend fun createApplication(
        @Header("Authorization") authorization: String,
        @Body body: BackendCreateApplicationRequest,
    ): BackendCreateApplicationResponse

    @GET("/api/applications/mine")
    suspend fun myApplications(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null,
    ): BackendApplicationListResponse

    @POST("/api/demands")
    suspend fun createDemand(
        @Header("Authorization") authorization: String,
        @Body body: BackendCreateDemandRequest,
    ): BackendCreateDemandResponse

    @GET("/api/demands/mine")
    suspend fun myDemands(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null,
    ): BackendDemandListResponse

    @GET("/api/demands/open")
    suspend fun openDemands(
        @Header("Authorization") authorization: String,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null,
    ): BackendDemandListResponse

    @POST("/api/demands/{id}/claim")
    suspend fun claimDemand(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body body: JsonObject = JsonObject(),
    ): BackendClaimDemandResponse

    @POST("/api/chat/thread/by-application/{applicationId}")
    suspend fun threadByApplication(
        @Header("Authorization") authorization: String,
        @Path("applicationId") applicationId: Int,
        @Body body: JsonObject = JsonObject(),
    ): BackendThreadResponse

    @GET("/api/chat/threads/{threadId}/messages")
    suspend fun messages(
        @Header("Authorization") authorization: String,
        @Path("threadId") threadId: Int,
        @Query("limit") limit: Int = 30,
        @Query("cursor") cursor: String? = null,
    ): BackendMessagesResponse
}

object BackendApi {
    private const val prefsName = "backend_api_prefs"
    private const val keyBaseUrlOverride = "base_url_override"
    private val gson = Gson()
    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var baseUrl: String = normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL)
    private val httpClient =
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    @Volatile
    private var service: BackendService = buildService(baseUrl)

    private fun buildService(url: String): BackendService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BackendService::class.java)
    }

    private fun normalizeBaseUrl(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        if (trimmed.isBlank()) return normalizeBaseUrl(BuildConfig.BACKEND_BASE_URL)

        val withScheme =
            when {
                trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
                else -> "http://$trimmed"
            }

        return runCatching {
            val uri = URI(withScheme)
            val scheme = uri.scheme?.trim().orEmpty().ifBlank { "http" }
            val host = uri.host?.trim().orEmpty()
            if (host.isBlank()) {
                if (withScheme.endsWith("/")) withScheme else "$withScheme/"
            } else {
                val portPart = if (uri.port >= 0) ":${uri.port}" else ""
                "$scheme://$host$portPart/"
            }
        }.getOrElse {
            if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        }
    }

    @Synchronized
    private fun setBaseUrlInternal(url: String) {
        val normalized = normalizeBaseUrl(url)
        if (normalized == baseUrl) return
        baseUrl = normalized
        service = buildService(baseUrl)
    }

    fun init(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val prefs = ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val override = prefs.getString(keyBaseUrlOverride, null)?.trim().orEmpty()
        val effective = if (override.isBlank()) BuildConfig.BACKEND_BASE_URL else override
        setBaseUrlInternal(effective)
    }

    fun getBaseUrl(): String = baseUrl

    fun setBaseUrlOverride(context: Context, url: String?) {
        val ctx = context.applicationContext
        val trimmed = url?.trim().orEmpty()
        val normalized = if (trimmed.isBlank()) "" else normalizeBaseUrl(trimmed)
        ctx.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .edit()
            .putString(keyBaseUrlOverride, normalized)
            .apply()
        init(ctx)
    }

    fun wsUrl(token: String): String {
        val wsBase = baseUrl.replace("http://", "ws://").replace("https://", "wss://")
        val encoded = java.net.URLEncoder.encode(token, "UTF-8")
        return wsBase + "ws?token=" + encoded
    }

    suspend fun health(): JsonObject {
        return service.health()
    }

    suspend fun register(role: String, phone: String, password: String, nickname: String?): BackendAuthResponse {
        return service.register(BackendRegisterRequest(role = role, phone = phone, password = password, nickname = nickname))
    }

    suspend fun login(role: String, phone: String, password: String): BackendAuthResponse {
        return service.login(BackendLoginRequest(role = role, phone = phone, password = password))
    }

    suspend fun me(accessToken: String): BackendMeResponse {
        return service.me("Bearer $accessToken")
    }

    suspend fun createApplication(accessToken: String, teacherId: Int): BackendCreateApplicationResponse {
        return service.createApplication("Bearer $accessToken", BackendCreateApplicationRequest(teacherId = teacherId))
    }

    suspend fun myApplications(accessToken: String, limit: Int = 30, cursor: String? = null): BackendApplicationListResponse {
        return service.myApplications("Bearer $accessToken", limit, cursor)
    }

    suspend fun createDemand(
        accessToken: String,
        subject: String,
        studentGrade: String,
        timeStartAt: Long,
        timeEndAt: Long,
        teacherGenderPreference: String?,
        minPrice: Double,
        maxPrice: Double,
    ): BackendCreateDemandResponse {
        return service.createDemand(
            "Bearer $accessToken",
            BackendCreateDemandRequest(
                subject = subject,
                studentGrade = studentGrade,
                timeStartAt = timeStartAt,
                timeEndAt = timeEndAt,
                teacherGenderPreference = teacherGenderPreference,
                minPrice = minPrice,
                maxPrice = maxPrice,
            ),
        )
    }

    suspend fun openDemands(accessToken: String, limit: Int = 30, cursor: String? = null): BackendDemandListResponse {
        return service.openDemands("Bearer $accessToken", limit, cursor)
    }

    suspend fun myDemands(accessToken: String, limit: Int = 30, cursor: String? = null): BackendDemandListResponse {
        return service.myDemands("Bearer $accessToken", limit, cursor)
    }

    suspend fun claimDemand(accessToken: String, demandId: Int): BackendClaimDemandResponse {
        return service.claimDemand("Bearer $accessToken", demandId)
    }

    suspend fun threadByApplication(accessToken: String, applicationId: Int): BackendThreadResponse {
        return service.threadByApplication("Bearer $accessToken", applicationId)
    }

    suspend fun messages(accessToken: String, threadId: Int, limit: Int = 30, cursor: String? = null): BackendMessagesResponse {
        return service.messages("Bearer $accessToken", threadId, limit, cursor)
    }

    fun openWebSocket(
        token: String,
        onText: (String) -> Unit,
        onFailure: (Throwable) -> Unit,
    ): WebSocket {
        val request = Request.Builder().url(wsUrl(token)).build()
        return httpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    onText(text)
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    onFailure(t)
                }
            },
        )
    }

    fun parseJsonObject(text: String): JsonObject? {
        return runCatching { gson.fromJson(text, JsonObject::class.java) }.getOrNull()
    }

    fun toMessageDto(obj: JsonObject): BackendChatMessageDto? {
        return runCatching {
            val msg = obj.getAsJsonObject("message") ?: return@runCatching null
            gson.fromJson(msg, BackendChatMessageDto::class.java)
        }.getOrNull()
    }
}
