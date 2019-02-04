package com.vsevolod.data.internal.di

import com.vsevolod.data.internal.Api
import com.vsevolod.data.internal.HeaderHolder
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
class ApiModule {

    @Provides
    fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

    @Provides
    fun provideRetrofitBuilder(client: OkHttpClient): Retrofit =
            Retrofit.Builder()
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(API)
                    .build()

    @Provides
    fun provideOkHttpClient(logger: HttpLoggingInterceptor): OkHttpClient =
            OkHttpClient.Builder()
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor { chain -> chain.withHeaders() }
                    .addInterceptor(logger)
                    .build()

    private fun Interceptor.Chain.withHeaders(): Response {

        val builder = request().newBuilder()

        HeaderHolder.values().forEach {
            builder.addHeader(it.name, it.header)
        }

        val request = builder.build()
        return proceed(request)
    }

    @Provides
    fun provideLoggingInterceptor() =
            HttpLoggingInterceptor()
                    .apply { level = HttpLoggingInterceptor.Level.BODY }

    companion object {

        const val TIMEOUT = 25L

        const val API = "https://example.com/"
    }
}
