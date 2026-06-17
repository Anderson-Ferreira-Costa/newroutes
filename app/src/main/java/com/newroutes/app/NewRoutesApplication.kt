package com.newroutes.app

import android.app.Application
import com.newroutes.app.data.tolls.TollPlazaSeeder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NewRoutesApplication : Application() {

    @Inject
    lateinit var tollPlazaSeeder: TollPlazaSeeder

    companion object {
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override fun onCreate() {
        super.onCreate()

        // Seed assíncrono — não bloqueia o startup
        applicationScope.launch {
            tollPlazaSeeder.seedIfNeeded()
        }
    }
}
