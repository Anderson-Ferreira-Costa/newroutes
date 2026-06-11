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
 * Configura: OkHttpClient com logging e interceptor User-Agent,
 * Retrofit com Moshi, NominatimApi e NominatimRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
object GeocodingModule {

    /**
     * Provê o OkHttpClient configurado para a API Nominatim.
     *
     * - Logging: BODY em debug, NONE em release (BuildConfig.DEBUG)
     * - Header fixo: User-Agent: NewRoutes/1.0
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
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "NewRoutes/1.0 (contato: newroutes@example.com)")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provê a instância do Retrofit configurada para a API Nominatim.
     *
     * - Base URL: https://nominatim.openstreetmap.org/
     * - Converter: MosConverterFactory
     * - Client: OkHttpClient fornecido por provideOkHttpClient()
     */
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
            ))
            .client(okHttpClient)
            .build()
    }

    /**
     * Provê a instância de NominatimApi a partir do Retrofit.
     */
    @Provides
    fun provideNominatimApi(retrofit: Retrofit): NominatimApi {
        return retrofit.create(NominatimApi::class.java)
    }

    /**
     * Provê o NominatimRepository com a dependência NominatimApi injetada.
     */
    @Provides
    fun provideNominatimRepository(api: NominatimApi): NominatimRepository {
        return NominatimRepository(api)
    }
}
