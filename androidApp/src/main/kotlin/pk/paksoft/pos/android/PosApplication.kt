package pk.paksoft.pos.android

import android.app.Application
import android.os.Build
import org.koin.android.ext.koin.androidContext
import pk.paksoft.pos.app.initKoin
import pk.paksoft.pos.core.model.AppConfig

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            AppConfig(
                isDebug = BuildConfig.DEBUG,
                platformName = "Android",
                osVersion = Build.VERSION.RELEASE,
                appVersion = BuildConfig.VERSION_NAME,
                // The emulator reaches the host machine's localhost at 10.0.2.2.
                localApiBaseUrl = "http://10.0.2.2:8010/api/v1",
            ),
        ) { androidContext(this@PosApplication) }
    }
}
