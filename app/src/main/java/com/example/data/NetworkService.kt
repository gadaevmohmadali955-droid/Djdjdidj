package com.example.data

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dns
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object NetworkService {
    private const val TAG = "BloxNetwork"
    private const val BASE_URL = "https://ntfy.sh"

    // OkHttpClient configured with IPv4 priority to prevent IPv6 unreachable errors in Android emulators
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(6, TimeUnit.SECONDS)
        .dns(object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                return try {
                    val addresses = Dns.SYSTEM.lookup(hostname)
                    val ipv4 = addresses.filterIsInstance<Inet4Address>()
                    if (ipv4.isNotEmpty()) ipv4 else addresses
                } catch (e: Exception) {
                    emptyList()
                }
            }
        })
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun publish(topic: String, payload: JSONObject): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$BASE_URL/$topic")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.d(TAG, "Network publish skipped for $topic: ${e.message}")
            false
        }
    }

    suspend fun pollHistory(topic: String, since: String = "24h"): List<JSONObject> = withContext(Dispatchers.IO) {
        val results = mutableListOf<JSONObject>()
        try {
            val url = "$BASE_URL/$topic/json?poll=1&since=$since"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val lines = body.lines()
                    for (line in lines) {
                        if (line.isBlank()) continue
                        try {
                            val eventObj = JSONObject(line)
                            if (eventObj.optString("event") == "message") {
                                val rawMessage = eventObj.optString("message")
                                if (rawMessage.startsWith("{")) {
                                    results.add(JSONObject(rawMessage))
                                }
                            }
                        } catch (e: Exception) {
                            // ignore malformed lines
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Network poll skipped for $topic: ${e.message}")
        }
        results
    }

    fun startListening(
        scope: CoroutineScope,
        topic: String,
        onMessage: (JSONObject) -> Unit
    ): Job {
        return scope.launch(Dispatchers.IO) {
            var retryDelay = 5000L
            while (isActive) {
                try {
                    val url = "$BASE_URL/$topic/json"
                    val request = Request.Builder().url(url).get().build()
                    val response = client.newCall(request).execute()
                    val inputStream = response.body?.byteStream()
                    if (inputStream != null) {
                        retryDelay = 5000L
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        var line: String? = null
                        while (isActive && reader.readLine().also { line = it } != null) {
                            val curLine = line ?: continue
                            if (curLine.isBlank()) continue
                            try {
                                val eventObj = JSONObject(curLine)
                                if (eventObj.optString("event") == "message") {
                                    val rawMessage = eventObj.optString("message")
                                    if (rawMessage.startsWith("{")) {
                                        val payload = JSONObject(rawMessage)
                                        withContext(Dispatchers.Main) {
                                            onMessage(payload)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // ignore
                            }
                        }
                    } else {
                        delay(retryDelay)
                        retryDelay = (retryDelay * 2).coerceAtMost(30000L)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "Stream offline/reconnecting for $topic: ${e.message}")
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(30000L)
                }
            }
        }
    }
}
