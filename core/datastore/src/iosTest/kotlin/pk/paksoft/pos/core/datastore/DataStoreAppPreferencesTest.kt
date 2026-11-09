package pk.paksoft.pos.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okio.Path.Companion.toPath
import pk.paksoft.pos.core.common.newUuid
import platform.Foundation.NSTemporaryDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStoreAppPreferencesTest {
    private fun prefs() = DataStoreAppPreferences(
        PreferenceDataStoreFactory.createWithPath { (NSTemporaryDirectory() + "prefs-${newUuid()}.preferences_pb").toPath() },
    )

    @Test fun `active shop round trip`() = runTest {
        val p = prefs()
        assertNull(p.activeShopId.first())
        p.setActiveShopId("smf")
        assertEquals("smf", p.activeShopId.first())
        p.setActiveShopId(null)
        assertNull(p.activeShopId.first())
    }

    @Test fun `device id is stable`() = runTest {
        val p = prefs()
        val a = p.deviceId()
        assertEquals(a, p.deviceId())
        assertEquals(36, a.length)
    }

    @Test fun `cached session round trip`() = runTest {
        val p = prefs()
        assertNull(p.cachedSessionJson.first())
        p.setCachedSessionJson("{}")
        assertEquals("{}", p.cachedSessionJson.first())
        p.setCachedSessionJson(null)
        assertNull(p.cachedSessionJson.first())
    }
}
