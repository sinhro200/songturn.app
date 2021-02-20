package com.sinhro.songturn.app.repositories

import com.sinhro.songturn.app.api.AppNetService
import com.sinhro.songturn.app.model.ErrorCollectorProvider
import com.sinhro.songturn.rest.core.CommonError

class AppRequestBuilder<ReqData, RespBody>(
    netService: AppNetService<ReqData, RespBody>,
    reqData: ReqData
) {
    private val request: () -> Unit = fun() {
        onStartLoad?.invoke()
        netService.run(
            reqData,
            {
                onSuccessSaveDataCallback?.invoke(it)
                onSuccessCallback?.invoke(it)
                onEndLoad?.invoke()
            },
            {
                errorCollector?.invoke(it)
                onErrorCallback?.invoke(it)
                onEndLoad?.invoke()
            },
            onCompleteCallback
        )
    }

    private var onErrorCallback: ((CommonError) -> Unit)? = null
    private var errorCollector: ((CommonError) -> Unit)? = null
    private var onSuccessCallback: ((RespBody) -> Unit)? = null
    private var onSuccessSaveDataCallback: ((RespBody) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null
    private var onStartLoad: (() -> Unit)? = null
    private var onEndLoad: (() -> Unit)? = null

    fun withOnComplete(onComplete: () -> Unit)
            : AppRequestBuilder<ReqData, RespBody> {
        onCompleteCallback = onComplete
        return this
    }

    fun withOnErrorCallback(onError: ((CommonError) -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.onErrorCallback = onError
        return this
    }

    fun withErrorCollector(errorCollector: ((CommonError) -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.errorCollector = errorCollector
        return this
    }

    fun withErrorCollectorViewModel(): AppRequestBuilder<ReqData, RespBody> {
        errorCollector = ErrorCollectorProvider.collectorForErrorViewModel
        return this
    }

    fun withOnSuccessCallback(onSuccess: ((RespBody) -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.onSuccessCallback = onSuccess
        return this
    }

    fun withOnSuccessSaveDataCallback(onSuccess: ((RespBody) -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.onSuccessSaveDataCallback = onSuccess
        return this
    }

    fun withOnStartLoad(onStartLoad: (() -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.onStartLoad = onStartLoad
        return this
    }

    fun withOnEndLoad(onEndLoad: (() -> Unit)? = null): AppRequestBuilder<ReqData, RespBody> {
        this.onEndLoad = onEndLoad
        return this
    }

    fun build(): () -> Unit = request

    fun run() = build().invoke()

}