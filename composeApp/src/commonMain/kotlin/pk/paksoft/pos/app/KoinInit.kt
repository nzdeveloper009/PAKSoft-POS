package pk.paksoft.pos.app

import org.koin.core.annotation.KoinApplication
import org.koin.dsl.KoinAppDeclaration
import org.koin.plugin.module.dsl.startKoin
import pk.paksoft.pos.core.model.AppConfig

/** The Koin entry point: the compiler plugin discovers every `@Configuration` module and checks the graph here. */
@KoinApplication
object PosKoinApp

/** Starts Koin once, from Android's `PosApplication` or iOS's `initKoinIos()`. */
fun initKoin(config: AppConfig, platform: KoinAppDeclaration? = null) {
    startKoin<PosKoinApp> {
        koin.declare(config)
        platform?.invoke(this)
    }
}
