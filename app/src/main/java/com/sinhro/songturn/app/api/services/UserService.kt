package com.sinhro.songturn.app.api.services

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.api.AppRetrofitProvider
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.PublicUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

class UserService(private val bearerToken: String) {
    interface UserApi {
        @GET("/user/me")
        fun getUser(): Observable<Response<FullUserInfo>>

        @POST("/user/changeme")
        fun changeMe(
            @Body newUserInfo: RegisterUserInfo
        ): Observable<Response<FullUserInfo>>

        @GET("/user/{id}")
        fun getUserById(
            @Path("id") id: Int
        ): Observable<Response<PublicUserInfo>>

        @GET("/user/logout")
        fun logout(): Observable<Response<ResponseBody>>
    }

    private val userMeApi =
        AppRetrofitProvider.AuthorizedInstance(bearerToken)
            .create(UserApi::class.java)

    val getUser = AppNetService.Builder<Nothing?, FullUserInfo>()
        .observableFrom { userMeApi.getUser() }
        .build()

    val changeUser = AppNetService.Builder<RegisterUserInfo, FullUserInfo>()
        .observableFrom { userMeApi.changeMe(it) }
        .build()

    val getUserById = AppNetService.Builder<Int, PublicUserInfo>()
        .observableFrom { userMeApi.getUserById(it) }
        .build()

    val logoutUser = AppNetService.Builder<Nothing?,ResponseBody>()
        .observableFrom { userMeApi.logout() }
        .build()
}