package pk.paksoft.pos.core.datastore

import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// The simulator test runner (a bare process spawned by simctl) has no keychain: SecItemAdd returns
// errSecNotAvailable (-25291), the same family as errSecMissingEntitlement (-34018). Covered by the
// manual run in Task 11 inside the real app, where the Keychain works.
@Ignore
class KeychainTokenStoreTest {
    private val store = KeychainTokenStore(service = "pk.paksoft.pos.token.test")

    @Test fun `write read clear per shop`() = runTest {
        store.write("smf", "t1"); store.write("bsf", "t2")
        assertEquals("t1", store.read("smf")); assertEquals("t2", store.read("bsf"))
        store.clear("smf"); assertNull(store.read("smf")); assertEquals("t2", store.read("bsf")); store.clear("bsf")
    }

    @Test fun `write replaces existing token`() = runTest {
        store.write("smf", "a"); store.write("smf", "b")
        assertEquals("b", store.read("smf")); store.clear("smf")
    }
}
