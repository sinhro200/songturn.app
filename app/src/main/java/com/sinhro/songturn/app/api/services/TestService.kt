package com.sinhro.songturn.app.api.services

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class TestService {

    interface TestAPI {
        @POST("/test/1")
        fun test(
            @Body registerReqData: Int
        ): Observable<Response<Int>>
    }

    private val testApi =
        AppRetrofitProvider.SimpleInstance()
            .create(TestAPI::class.java)

    val test = AppNetService.Builder<Int, Int>()
        .observableFrom { testApi.test(it) }
        .build()
}