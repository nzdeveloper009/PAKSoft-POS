package pk.paksoft.pos.app

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import pk.paksoft.pos.core.model.AppConfig
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

/** Called from Swift as `KoinInitIosKt.doInitKoinIos()` in `iOSApp.init()`. */
@OptIn(ExperimentalNativeApi::class)
fun initKoinIos() {
    initKoin(
        AppConfig(
            isDebug = Platform.isDebugBinary,
            platformName = "iOS",
            osVersion = UIDevice.currentDevice.systemVersion,
            appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "",
            localApiBaseUrl = "http://127.0.0.1:8010/api/v1",
        ),
    )
}
