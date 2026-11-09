package pk.paksoft.pos.core.testing

import pk.paksoft.pos.core.data.LocalDataCleaner

class FakeLocalDataCleaner : LocalDataCleaner {
    var clearCalls: Int = 0
        private set

    override suspend fun clearAll() {
        clearCalls++
    }
}
