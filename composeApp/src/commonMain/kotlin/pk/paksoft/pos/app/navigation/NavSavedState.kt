package pk.paksoft.pos.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import pk.paksoft.pos.feature.settings.navigation.MoreKey

/**
 * Saves the back stack with kotlinx.serialization. Every [NavKey] in the app must be registered here,
 * or restoring the back stack crashes on iOS.
 */
val navSavedStateConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DashboardKey::class)
            subclass(PosKey::class)
            subclass(SalesKey::class)
            subclass(LedgerKey::class)
            subclass(MoreKey::class)
        }
    }
}
