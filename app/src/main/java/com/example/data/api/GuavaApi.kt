package com.example.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

// API Models
data class WifiRequest(
    val ssid: String,
    val password: String
)

data class ModeRequest(
    val mode: String
)

data class SettingsRequest(
    val language: String,
    val voiceEnabled: Boolean,
    val notesEnabled: Boolean,
    val assistantName: String
)

data class StatusResponse(
    val wifiStatus: String = "unknown",
    val deviceIp: String = "unknown",
    val ssid: String = "",
    val mode: String = "penguin",
    val language: String = "english",
    val voiceEnabled: Boolean = true,
    val notesEnabled: Boolean = true,
    val assistantName: String = "Penguin",
    val voiceEngine: String = "piper",
    val listenWhileProcessing: Boolean = true,
    val queueLimit: Int = 20,
    val voiceName: String = "Ryan low",
    val voiceGender: String = "male",
    val piperModel: String = "",
    val silenceSeconds: Double = 0.85,
    val minUtteranceSeconds: Double = 0.35,
    val maxUtteranceSeconds: Double = 18.0
)

data class SystemChild(
    val name: String = "",
    val path: String = ""
)

data class SystemFolder(
    val name: String = "",
    val path: String = "",
    val description: String = "",
    val children: List<SystemChild> = emptyList()
)

data class SystemModeDetails(
    val role: String = "",
    val memory: String = "",
    val prompt: String = ""
)

data class SystemOverviewResponse(
    val root: String = "",
    val folders: List<SystemFolder> = emptyList(),
    val modes: Map<String, SystemModeDetails> = emptyMap()
)

// Retrofit Interface API
interface GuavaApi {
    @GET("api/status")
    suspend fun getStatus(): StatusResponse

    @POST("api/wifi")
    suspend fun saveWifi(@Body request: WifiRequest): StatusResponse

    @POST("api/mode")
    suspend fun setMode(@Body request: ModeRequest): StatusResponse

    @POST("api/settings")
    suspend fun saveSettings(@Body request: SettingsRequest): StatusResponse

    @POST("api/restart")
    suspend fun restartDevice(): StatusResponse

    @GET("api/system/overview")
    suspend fun getSystemOverview(): SystemOverviewResponse
}

// Client Factory
object GuavaApiClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun createService(baseUrl: String): GuavaApi {
        val formattedUrl = when {
            baseUrl.startsWith("http://") || baseUrl.startsWith("https://") -> baseUrl
            else -> "http://$baseUrl"
        }.trimEnd('/') + "/"

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(4, TimeUnit.SECONDS)
            .writeTimeout(4, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(formattedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GuavaApi::class.java)
    }
}
