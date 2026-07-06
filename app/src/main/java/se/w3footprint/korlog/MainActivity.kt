package se.w3footprint.korlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import se.w3footprint.korlog.presentation.common.theme.KorLogTheme
import se.w3footprint.korlog.presentation.navigation.KorLogNavGraph
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val navigateTo = intent.getStringExtra("navigate_to")
        setContent {
            KorLogTheme {
                Surface {
                    KorLogNavGraph(
                        isLoggedIn = auth.currentUser != null,
                        navigateTo = navigateTo
                    )
                }
            }
        }
    }
}
