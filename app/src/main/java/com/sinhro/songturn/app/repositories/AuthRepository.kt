package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.api.services.LoginService
import com.sinhro.songturn.app.api.services.RegisterService
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.rest.model.RegisterDemoUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.*

class AuthRepository {
    private val rs = RegisterService()
    private val ls = LoginService()

    fun registerUser(registerInfo: RegisterUserInfo)
            : AppRequestBuilder<RegisterReqData, RegisterRespBody> =
        AppRequestBuilder(rs.register, RegisterReqData(registerInfo))


    fun registerDemoUser(registerDemoInfo: RegisterDemoUserInfo)
            : AppRequestBuilder<RegisterDemoReqData, RegisterDemoRespBody> =
        AppRequestBuilder(rs.registerDemo, RegisterDemoReqData(registerDemoInfo))
            .withOnSuccessSaveDataCallback {
                ApplicationData.access_token = it.accessToken
            }


    fun authUser(authReqData: AuthReqData)
            : AppRequestBuilder<AuthReqData, AuthRespBody> =
        AppRequestBuilder(ls.login, authReqData)
            .withOnSuccessSaveDataCallback {
                ApplicationData.access_token = it.accessToken
            }

}