package cl.lacasajojo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.lacasajojo.app.ui.navigation.AppNavHost
import cl.lacasajojo.app.ui.theme.CasaJoJoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CasaJoJoApplication).container
        setContent {
            CasaJoJoTheme {
                AppNavHost(container = container)
            }
        }
    }
}
