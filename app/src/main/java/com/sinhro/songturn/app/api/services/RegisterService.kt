package com.sinhro.songturn.app.api.services


import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import com.sinhro.songturn.rest.request_response.RegisterDemoReqData
import com.sinhro.songturn.rest.request_response.RegisterDemoRespBody
import com.sinhro.songturn.rest.request_response.RegisterReqData
import com.sinhro.songturn.rest.request_response.RegisterRespBody
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class RegisterService {

    interface RegisterAPI {
        @POST("/register")
        fun register(
            @Body registerReqData: RegisterReqData
        ): Observable<Response<RegisterRespBody>>
    }

    private val registerApi =
        AppRetrofitProvider.SimpleInstance()
            .create(RegisterAPI::class.java)

    val register = AppNetService.Builder<RegisterReqData, RegisterRespBody>()
        .observableFrom { registerApi.register(it) }
        .build()

    interface RegisterDemoAPI {
        @POST("/registerDemo")
        fun registerDemo(
            @Body registerDemoReqData: RegisterDemoReqData
        ): Observable<Response<RegisterDemoRespBody>>
    }

    private val registerDemoApi =
        AppRetrofitProvider.SimpleInstance()
            .create(RegisterDemoAPI::class.java)

    val registerDemo = AppNetService.Builder<RegisterDemoReqData, RegisterDemoRespBody>()
        .observableFrom { registerDemoApi.registerDemo(it) }
        .build()
}