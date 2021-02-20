package com.sinhro.songturn.app.api.services

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import com.sinhro.songturn.rest.request_response.AuthReqData
import com.sinhro.songturn.rest.request_response.AuthRespBody
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class LoginService {

    interface AuthAPI {
        @POST("login")
        fun login(
            @Body authReqData: AuthReqData
        ): Observable<Response<AuthRespBody>>
    }

    private val authApi =
        AppRetrofitProvider.SimpleInstance()
            .create(AuthAPI::class.java)

    val login = AppNetService.Builder<AuthReqData, AuthRespBody>()
        .observableFrom { authApi.login(it) }
        .build()
}