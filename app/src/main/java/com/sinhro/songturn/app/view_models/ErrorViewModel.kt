package com.sinhro.songturn.app.view_models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sinhro.songturn.rest.core.CommonError

class ErrorViewModel : ViewModel() {
    val commonErrorMutableLiveData = MutableLiveData<CommonError>()

    fun error(commonError: CommonError) {
        commonErrorMutableLiveData.postValue(commonError)
    }

    companion object {
        private var globalInstance: ErrorViewModel? = null

        fun initGlobal(instance: ErrorViewModel) {
            globalInstance = instance
        }

        fun getGlobalInstance(): ErrorViewModel? {
            return globalInstance
        }
    }
}