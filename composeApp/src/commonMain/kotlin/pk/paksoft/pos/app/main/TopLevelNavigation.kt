package pk.paksoft.pos.app.main

import androidx.navigation3.runtime.NavKey

/**
 * Dashboard is the fixed start destination: every other tab sits on top of it, so Back from a tab returns
 * to Dashboard and Back on Dashboard leaves the app.
 */
fun backStackFor(destination: TopLevelDestination): List<NavKey> =
    if (destination == TopLevelDestination.DASHBOARD) {
        listOf(TopLevelDestination.DASHBOARD.key)
    } else {
        listOf(TopLevelDestination.DASHBOARD.key, destination.key)
    }

/** The tab to highlight: the one owning the last top-level key on the stack (screens above it keep it selected). */
fun selectedDestination(stack: List<NavKey>): TopLevelDestination? =
    stack.asReversed().firstNotNullOfOrNull { key -> TopLevelDestination.entries.firstOrNull { it.key == key } }
