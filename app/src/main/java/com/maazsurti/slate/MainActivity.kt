package com.maazsurti.slate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.maazsurti.slate.ui.SlateApp
import com.maazsurti.slate.ui.theme.SlateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlateTheme {
                SlateApp()
            }
        }
    }
}
