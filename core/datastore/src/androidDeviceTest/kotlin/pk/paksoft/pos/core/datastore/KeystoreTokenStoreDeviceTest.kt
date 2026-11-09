package pk.paksoft.pos.core.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.security.KeyStore
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KeystoreTokenStoreDeviceTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    // One DataStore file per test: DataStore forbids two active instances on the same file.
    private fun store() = KeystoreTokenStore(context, "test-tokens-${UUID.randomUUID()}.preferences_pb")

    @Test fun roundTripForTwoShops() = runBlocking {
        val s = store()
        s.write("smf", "t1"); s.write("bsf", "t2")
        assertEquals("t1", s.read("smf"))
        assertEquals("t2", s.read("bsf"))
    }

    @Test fun writeReplaces() = runBlocking {
        val s = store()
        s.write("smf", "a"); s.write("smf", "b")
        assertEquals("b", s.read("smf"))
    }

    @Test fun clearRemovesOnlyThatShop() = runBlocking {
        val s = store()
        s.write("smf", "t1"); s.write("bsf", "t2")
        s.clear("smf")
        assertNull(s.read("smf"))
        assertEquals("t2", s.read("bsf"))
    }

    @Test fun storedValueShapeAndFreshIv() = runBlocking {
        val s = store()
        s.write("smf", "same-token")
        val first = s.rawValue("smf")!!
        s.write("smf", "same-token")
        val second = s.rawValue("smf")!!
        for (v in listOf(first, second)) {
            val parts = v.split(":")
            assertEquals(2, parts.size)
            assertEquals(12, java.util.Base64.getDecoder().decode(parts[0]).size)
            assertTrue(java.util.Base64.getDecoder().decode(parts[1]).isNotEmpty())
            assertTrue(!v.contains("same-token"))
        }
        assertNotEquals(first, second)
        assertNotEquals(first.substringBefore(":"), second.substringBefore(":"))
    }

    @Test fun corruptedValueReadsNullAndIsRemoved() = runBlocking {
        val s = store()
        s.write("smf", "ok")
        s.putRaw("smf", "not-a-valid-entry")
        assertNull(s.read("smf"))
        assertNull(s.rawValue("smf"))

        s.write("bsf", "ok")
        val good = s.rawValue("bsf")!!
        val tampered = good.substringBefore(":") + ":" + java.util.Base64.getEncoder().encodeToString(ByteArray(32))
        s.putRaw("bsf", tampered)
        assertNull(s.read("bsf"))
        assertNull(s.rawValue("bsf"))
    }

    @Test fun concurrentFirstWritesAllReadable() = runBlocking {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry("pos_token_key")
        val s = store()
        (1..8).map { i -> async(Dispatchers.Default) { s.write("shop$i", "token$i") } }.awaitAll()
        for (i in 1..8) assertEquals("token$i", s.read("shop$i"))
    }
}
