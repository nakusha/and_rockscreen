package com.example.rockscreen

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException

object ApiClient {
    private const val TAG = "ApiClient"
    private val client: OkHttpClient

    init {
        // 로깅 인터셉터 추가
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    fun postExampleApi(callback: (String?) -> Unit) {
        val url = "https://jsonplaceholder.typicode.com/posts"

        // JSON 데이터 생성
        val json = JSONObject().apply {
            put("title", "foo")
            put("body", "bar")
            put("userId", 1)
        }

        // 확장 함수를 사용하여 MediaType 설정
        val mediaType = "application/json".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "API 호출 실패", e)
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "API 호출 실패: $response")
                        callback(null)
                    } else {
                        val responseData = response.body?.string()
                        Log.d(TAG, "API 응답: $responseData")
                        callback(responseData)
                    }
                }
            }
        })
    }
}
