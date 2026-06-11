package com.newroutes.app.data.routing

import com.newroutes.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Módulo Hilt para prover as dependências da camada de roteamento (OSRM).
 *
 * Configura OkHttpClient e Retrofit separados da camada de geocoding (Nominatim),
 * usando @Named("osrm") para evitar colisão de providers.
 */
@Module
@InstallIn(SingletonComponent::class)
object RoutingModule {

    /**
     * Provê o OkHttpClient configurado para o OSRM.
     *
     * - Logging: BODY em debug, NONE em release (BuildConfig.DEBUG)
     * - Sem header User-Agent customizado (diferente do Nominatim)
     * - Timeout: 30s para connect, read e write (rotas longas podem demorar)
     */
    @Provides
    @Named("osrm")
    fun provideOsrmOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Provê a instância do Retrofit configurada para o OSRM.
     *
     * - Base URL: https://router.project-osrm.org/
     * - Converter: MosConverterFactory
     * - Client: OkHttpClient @Named("osrm")
     */
    @Provides
    @Named("osrm")
    fun provideOsrmRetrofit(@Named("osrm") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .addConverterFactory(MoshiConverterFactory.create(
                Moshi.Builder()
                    .addLast(KotlinJsonAdapterFactory())
                    .build()
            ))
            .client(okHttpClient)
            .build()
    }

    /**
     * Provê a instância de OsrmApi a partir do Retrofit.
     */
    @Provides
    fun provideOsrmApi(@Named("osrm") retrofit: Retrofit): OsrmApi {
        return retrofit.create(OsrmApi::class.java)
    }

    /**
     * Provê o OsrmRepository com a dependência OsrmApi injetada.
     */
    @Provides
    fun provideOsrmRepository(api: OsrmApi): OsrmRepository {
        return OsrmRepository(api)
    }
}
