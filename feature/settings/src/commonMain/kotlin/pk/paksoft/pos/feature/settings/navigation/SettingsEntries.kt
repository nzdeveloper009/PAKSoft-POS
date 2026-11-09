package pk.paksoft.pos.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import pk.paksoft.pos.feature.settings.MoreRoute

fun EntryProviderScope<NavKey>.settingsEntries() {
    entry<MoreKey> { MoreRoute() }
}
