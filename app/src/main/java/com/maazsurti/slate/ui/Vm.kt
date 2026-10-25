package com.maazsurti.slate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.maazsurti.slate.SlateApplication
import com.maazsurti.slate.core.data.SlateContainer

@Composable
fun slateContainer(): SlateContainer {
    val app = LocalContext.current.applicationContext as SlateApplication
    return app.container
}

/** Small factory helper so screens can build their view model without a DI framework. */
inline fun <reified T : ViewModel> vmFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
    }
