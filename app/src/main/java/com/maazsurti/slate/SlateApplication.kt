package com.maazsurti.slate

import android.app.Application
import com.maazsurti.slate.core.data.SlateContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SlateApplication : Application() {

    lateinit var container: SlateContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = SlateContainer(this)
        appScope.launch { container.seedDemoData.seedIfEmpty(System.currentTimeMillis()) }
    }
}
