package com.nikunjagarwala.dummyshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.nikunjagarwala.dummyshop.ui.navigation.DummyShopNavHost
import com.nikunjagarwala.dummyshop.ui.theme.DummyShopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DummyShopTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DummyShopNavHost(app = application as DummyShopApp)
                }
            }
        }
    }
}
