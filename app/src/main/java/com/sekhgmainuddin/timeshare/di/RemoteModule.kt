package com.sekhgmainuddin.timeshare.di

import com.sekhgmainuddin.timeshare.data.remote.NotificationSend
import com.sekhgmainuddin.timeshare.utils.Keys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    fun providesNotificationAPI(client: OkHttpClient) = Retrofit.Builder()
        .client(client)
        .baseUrl("https://fcm.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NotificationSend::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor() = OkHttpClient.Builder().addInterceptor(Interceptor { chain ->
        val newRequest: Request = chain.request().newBuilder()
            .addHeader("Content-Type","application/json")
            .addHeader("Authorization", "key=${Keys.getFCMKey()}")
            .addHeader("project_id", Keys.getProjectNumber())
            .build()
        chain.proceed(newRequest)
    }).build()

}