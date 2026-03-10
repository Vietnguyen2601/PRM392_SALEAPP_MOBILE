package com.example.saleapp.di

import com.example.saleapp.core.network.ApiConfig
import com.example.saleapp.core.network.ApiClient
import com.example.saleapp.core.network.ApiService
import com.example.saleapp.core.network.AuthInterceptor
import com.example.saleapp.core.network.ChatApiService
import com.example.saleapp.core.realtime.ChatHubManager
import com.example.saleapp.core.utils.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient =
        ApiClient.createOkHttpClient(authInterceptor)

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        ApiClient.createRetrofit(okHttpClient)

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService =
        retrofit.create(ChatApiService::class.java)

    @Provides
    @Singleton
    fun provideChatHubManager(preferenceManager: PreferenceManager): ChatHubManager {
        val baseUrl = ApiConfig.BASE_URL.replace("/api/", "").removeSuffix("/")
        val token = preferenceManager.getAuthToken() ?: ""
        return ChatHubManager(baseUrl, token).apply {
            initialize()
        }
    }
}


