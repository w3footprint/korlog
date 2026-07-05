package se.w3footprint.korlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import se.w3footprint.korlog.presentation.common.theme.KorLogTheme
import se.w3footprint.korlog.presentation.navigation.KorLogNavGraph

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KorLogTheme {
                Surface {
                    KorLogNavGraph(isLoggedIn = false)
                }
            }
        }
    }
}
