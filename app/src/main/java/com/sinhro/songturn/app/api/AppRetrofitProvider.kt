package com.sinhro.songturn.app.api

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class AppRetrofitProvider {

    companion object {
        private fun createLoggerInterceptor(): HttpLoggingInterceptor {
            return HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        }

        private fun createClient(vararg interceptors: Interceptor): OkHttpClient {
            return OkHttpClient.Builder()
                .apply {
                    interceptors.forEach {
                        this.addInterceptor(it)
                    }
                }.build()
        }

        private fun createRetrofit(client: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
//                .addConverterFactory(GsonConvertingFactoryProvider.gsonCFactory)
                .addConverterFactory(JacksonConvertingFactoryProvider.convertingFactory)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
        }

        fun AuthorizedInstance(bearerToken: String): Retrofit {
            return createRetrofit(
                createClient(
                    createLoggerInterceptor(),
                    AuthorizationInterceptor(bearerToken)
                )
            )
        }

        fun SimpleInstance(): Retrofit {
            return createRetrofit(
                createClient(
                    createLoggerInterceptor()
                )
            )
        }
    }

    class AuthorizationInterceptor(private val bearerToken: String) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val newReq = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $bearerToken")
                .build()
            return chain.proceed(newReq)
        }

    }
}