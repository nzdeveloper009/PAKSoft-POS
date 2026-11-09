package pk.paksoft.pos.core.datastore.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [PlatformDataStoreModule::class])
@Configuration
@ComponentScan("pk.paksoft.pos.core.datastore")
class DataStoreModule
