package com.newroutes.app.data.geocoding

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.newroutes.app.BuildConfig

/**
 * Módulo Hilt para prover as dependências da camada de geocoding.
 *
 * Configura: OkHttpClient com logging, Retrofit com Moshi, PhotonApi e PhotonRepository.
 * Usa Photon (Komoot) ao invés do Nominatim — sem rate limit restritivo.
 */
@Module
@InstallIn(SingletonComponent::class)
object GeocodingModule {

    /**
     * Provê o OkHttpClient configurado para a API Photon.
     *
     * - Logging: BODY em debug, NONE em release (BuildConfig.DEBUG)
     * - Header fixo: User-Agent com email, Accept-Language: pt-BR
     * - Timeout: 15s para connect, read e write
     */
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "NewRoutes/1.0 (newroutes@example.com)")
                    .header("Accept-Language", "pt-BR,pt;q=0.9")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provê a instância do Retrofit configurada para a API Photon.
     *
     * - Base URL: https://photon.komoot.io/
     * - Converter: MosConverterFactory
     * - Client: OkHttpClient fornecido por provideOkHttpClient()
     */
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://photon.komoot.io/")
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
            ))
            .client(okHttpClient)
            .build()
    }

    /**
     * Provê a instância de PhotonApi a partir do Retrofit.
     */
    @Provides
    fun providePhotonApi(retrofit: Retrofit): PhotonApi {
        return retrofit.create(PhotonApi::class.java)
    }

    /**
     * Provê o PhotonRepository com a dependência PhotonApi injetada.
     */
    @Provides
    fun providePhotonRepository(api: PhotonApi): PhotonRepository {
        return PhotonRepository(api)
    }
}
