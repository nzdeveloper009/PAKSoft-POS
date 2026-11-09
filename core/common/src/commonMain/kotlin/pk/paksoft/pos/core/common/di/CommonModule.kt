package pk.paksoft.pos.core.common.di

import pk.paksoft.pos.core.common.AppDispatchers
import kotlinx.coroutines.Dispatchers
import pk.paksoft.pos.core.common.ioDispatcher
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("pk.paksoft.pos.core.common")
class CommonModule {
    @Singleton
    fun dispatchers(): AppDispatchers = AppDispatchers(ioDispatcher(), Dispatchers.Default, Dispatchers.Main)
}
