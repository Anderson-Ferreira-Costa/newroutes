package com.newroutes.app

import android.app.Application
import com.newroutes.app.data.tolls.TollPlazaSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class NewRoutesApplication : Application() {

    @Inject
    lateinit var tollPlazaSeeder: TollPlazaSeeder

    override fun onCreate() {
        super.onCreate()

        // Seed assíncrono — não bloqueia o startup
        GlobalScope.launch(Dispatchers.IO) {
            tollPlazaSeeder.seedIfNeeded()
        }
    }
}
