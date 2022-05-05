package com.travis.httpRoutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.internal.http2.Header

@ExperimentalCoroutinesApi
class DemoEndpoint : HttpEndpoint() {
    override var baseUrl = "http://fakeApi"

    suspend fun myApi(): String {
        var res = ""
        GET("") {
            Header("", "")
            Header("", "")
        }
            .onError { i, headers -> res = ""}
            .onSuccess<String> { i, headers, t -> res = t.orEmpty() }

        POST("", "") {
            Header("", "")
        }
            .onSuccess<String> { i, headers, t -> res = t.orEmpty() }
            .onError { i, headers -> res = ""}

        return ""
    }
}