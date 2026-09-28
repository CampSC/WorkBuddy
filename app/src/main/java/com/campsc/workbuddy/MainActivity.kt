package com.campsc.workbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.campsc.workbuddy.ui.WorkBuddyNavHost
import com.campsc.workbuddy.ui.theme.WorkBuddyTheme

/**
 * A única Activity da app. Com Compose não precisamos de uma Activity por
 * ecrã: a navegação entre ecrãs é feita pelo WorkBuddyNavHost.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WorkBuddyTheme {
                WorkBuddyNavHost()
            }
        }
    }
}
