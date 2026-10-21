package cl.lacasajojo.app

import android.app.Application
import cl.lacasajojo.app.data.AppContainer

class CasaJoJoApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
