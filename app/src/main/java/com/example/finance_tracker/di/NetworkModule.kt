package com.example.finance_tracker.di

import com.example.finance_tracker.data.remote.NbrbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Сеть: Retrofit-клиент к API НБ РБ (официальные курсы).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
            )
            .build()

    @Provides
    @Singleton
    fun provideNbrbApi(client: OkHttpClient): NbrbApi =
        Retrofit.Builder()
            .baseUrl("https://api.nbrb.by/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NbrbApi::class.java)
}
