package pk.paksoft.pos.app

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.PolymorphicSerializer
import pk.paksoft.pos.app.main.TopLevelDestination
import pk.paksoft.pos.app.navigation.navSavedStateConfig

/** iOS saves the back stack with kotlinx.serialization, so an unregistered key crashes on state restore. */
class NavSavedStateTest {
    @Test fun `every top level key survives a save and restore`() {
        val serializer = PolymorphicSerializer(NavKey::class)
        TopLevelDestination.entries.forEach { destination ->
            val saved = encodeToSavedState(serializer, destination.key, navSavedStateConfig)
            assertEquals(destination.key, decodeFromSavedState(serializer, saved, navSavedStateConfig))
        }
    }
}
