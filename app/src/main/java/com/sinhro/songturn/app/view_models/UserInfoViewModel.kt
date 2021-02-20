package com.sinhro.songturn.app.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sinhro.songturn.app.model.ApplicationData
import com.sinhro.songturn.app.repositories.AppRequestBuilder
import com.sinhro.songturn.app.repositories.UserRepository
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.PublicUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import okhttp3.ResponseBody

class UserInfoViewModel : ViewModel() {

    private val userRepository: UserRepository by lazy {
        UserRepository(ApplicationData.access_token)
    }

    val userLiveData: MutableLiveData<FullUserInfo> by lazy {
        MutableLiveData<FullUserInfo>().also {
            updateUser().run()
        }
    }

    fun updateUser(): AppRequestBuilder<Nothing?, FullUserInfo> {
        return userRepository.currentUserData()
            .withOnSuccessSaveDataCallback { userLiveData.postValue(it) }
    }

    fun changeUser(
        registerUserInfo: RegisterUserInfo
    ): AppRequestBuilder<RegisterUserInfo, FullUserInfo> {
        return userRepository.changeUserData(registerUserInfo)
            .withOnSuccessSaveDataCallback { userLiveData.postValue(it) }
    }

    fun getUserById(id: Int): AppRequestBuilder<Int, PublicUserInfo> {
        return userRepository.userDataById(id)
    }

    fun logoutUser(): AppRequestBuilder<Nothing?, ResponseBody> {
        return userRepository.logoutUser()
    }
}
