package com.sinhro.songturn.app.api

import com.fasterxml.jackson.module.kotlin.readValue
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.concurrent.TimeUnit

//ToDO заменить RxJava на kotlin.coroutine
//Когда-нибудь я этим займусь
class AppNetService<ReqData, RespBody> private constructor(
    private val observable: (ReqData) -> Observable<Response<RespBody>>,
    private val timeoutMillis: Long
) {

    private var disposable: Disposable? = null

    fun run(
        reqData: ReqData,
        onSuccess: (RespBody) -> Unit,
        onError: (CommonError) -> Unit,
        onCompleteCallback: (() -> Unit)? = null
    ) {
        if (disposable != null && !disposable!!.isDisposed) {
//            notRunsCallback?.invoke()
            return
        }
        RxJavaPlugins.setErrorHandler {
            onError(
                CommonError(
                    ErrorCodes.NET_CONNECTION_ERR, it.message, it.cause.toString(), it.stackTrace
                )
            )
        }

        disposable = observable.invoke(reqData).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(timeoutMillis, TimeUnit.MILLISECONDS,
                {
                    disposable?.dispose()
                    onError(CommonError(ErrorCodes.NETWORK_TIMEOUT))
                }
            )
            .subscribe(
                { response ->
                    if (response.isSuccessful) {
                        response.body()?.let { respBody -> onSuccess(respBody) }
                    } else {
                        response.errorBody()?.let { errorBody ->
                            handleErrorBody(errorBody, onError)
                        }
                    }
                },
                {
                    onError(
                        CommonError(
                            ErrorCodes.NET_CONNECTION_ERR,
                            it.message.toString(),
                            it.localizedMessage,
                            it.stackTraceToString()
                        )
                    )
                },
                {
                    disposable?.dispose()
                    onCompleteCallback?.invoke()
                }
            )
    }

    private fun handleErrorBody(
        body: ResponseBody,
        onError: (CommonError) -> Unit
    ) {
        val om = JacksonObjectMapperProvider.objectMapper

        try {
            val str = body.string()
            val commonError = om.readValue<CommonError>(str)
            RestErrorHandler.handle(commonError)
            onError(commonError)
        } catch (ex: Exception) {
            onError(
                CommonError(
                    ErrorCodes.SMTH_WENT_WRONG,
                    "Cant deserialize error JSON body from server",
                    ex.message, body.string()
                )
            )
        }
    }

    public class Builder<ReqData, RespBody>() {
        private lateinit var observable: (ReqData) -> Observable<Response<RespBody>>
        private var timeoutMillis: Long = 5000

        fun observableFrom(obs: (ReqData) -> Observable<Response<RespBody>>)
                : Builder<ReqData, RespBody> {
            observable = obs
            return this
        }

        fun timeoutMillis(t: Long)
                : Builder<ReqData, RespBody> {
            timeoutMillis = t
            return this
        }

        fun build(): AppNetService<ReqData, RespBody> {
            return AppNetService(observable, timeoutMillis)
        }
    }

    companion object
}