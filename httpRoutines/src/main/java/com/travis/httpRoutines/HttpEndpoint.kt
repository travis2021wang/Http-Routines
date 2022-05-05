package com.travis.httpRoutines


import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
abstract class HttpEndpoint {
    abstract val baseUrl: String

    var httpClient = OkHttpClient.Builder().build()

    suspend fun GET(
        uri: String,
        config: Request.Builder.() -> Unit
    ): HttpResponse {
        return http {
            url("$baseUrl/$uri")
            config()
            get()
        }
    }

    suspend fun DELETE(
        uri: String,
        payload: Any? = null,
        config: Request.Builder.() -> Unit
    ): HttpResponse {
        return http {
            url("$baseUrl/$uri")
            config()
            delete(Gson().toJson(payload).toRequestBody())
        }
    }

    suspend fun POST(
        uri: String,
        payload: Any? = null,
        config: Request.Builder.() -> Unit
    ): HttpResponse {
        return http {
            url("$baseUrl/$uri")
            config()
            post(Gson().toJson(payload).toRequestBody())
        }
    }

    suspend fun PUT(
        uri: String,
        payload: Any? = null,
        config: Request.Builder.() -> Unit
    ): HttpResponse {
        return http {
            url("$baseUrl/$uri")
            config()
            put(Gson().toJson(payload).toRequestBody())
        }
    }

    private suspend fun http(config: Request.Builder.() -> Unit): HttpResponse {
        val request = Request.Builder().apply {
            config()
        }.build()

        return http(request).toHttpResponse()
    }

    private suspend fun http(request: Request): Response {
        val call = httpClient.newCall(request)

        return suspendCancellableCoroutine { continuation ->
            call.enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    continuation.resume(response) { continuation.resumeWithException(it) }
                }

                override fun onFailure(call: Call, e: IOException) {
                    // Don't bother with resuming the continuation if it is already cancelled.
                    if (!continuation.isCancelled) continuation.resumeWithException(e)
                }
            })

            continuation.invokeOnCancellation {
                try {
                    call.cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
            }
        }
    }
}

data class HttpResponse(
    val statusCode: Int = 200,
    val headers: Headers? = null,
    val payload: String? = null
) {
    inline fun <reified T> body(): T? {
        return Gson().fromJson(payload, T::class.java)
    }

    inline fun <reified T> onSuccess(handler: (Int?, Headers?, T?) -> Unit): HttpResponse {
        if (statusCode in 200..299)
            handler(statusCode, headers, body<T>())
        return this
    }

    fun onPlainSuccess(handler: (Int?, Headers?, String?) -> Unit): HttpResponse {
        if (statusCode in 200..299)
            handler(statusCode, headers, payload)
        return this
    }

    fun onError(handler: (Int?, Headers?) -> Unit): HttpResponse {
        if (statusCode !in 200..299)
            handler(statusCode, headers)
        return this
    }
}

fun Response.toHttpResponse() =
    if (isSuccessful) HttpResponse(code, headers, body?.string())
    else HttpResponse(code, headers)