package pk.paksoft.pos.app.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.NavigationBackHandler
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.scene.rememberNavigationEventState
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventTransitionState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pk.paksoft.pos.app.navigation.DashboardKey
import pk.paksoft.pos.app.navigation.LedgerKey
import pk.paksoft.pos.app.navigation.PosKey
import pk.paksoft.pos.app.navigation.SalesKey
import pk.paksoft.pos.app.navigation.navSavedStateConfig
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
import pk.paksoft.pos.core.designsystem.component.selectionBump
import pk.paksoft.pos.core.designsystem.motion.LocalPosAnimatedVisibilityScope
import pk.paksoft.pos.core.designsystem.motion.PosTransitions
import pk.paksoft.pos.core.designsystem.motion.predictiveBackCorners
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.model.UserSession
import pk.paksoft.pos.core.ui.asString
import pk.paksoft.pos.core.ui.resources.Res as UiRes
import pk.paksoft.pos.core.ui.resources.error_offline
import pk.paksoft.pos.feature.settings.navigation.settingsEntries

/** Material's compact-width breakpoint: a bottom bar below it, a navigation rail from it up. */
private val RailMinWidth = 600.dp

/**
 * The signed-in home: the tabs the user may see, one back stack with Dashboard as the fixed start
 * ([backStackFor]), and the shop-data refresh banner.
 */
@Composable
fun MainScaffold(session: UserSession, vm: MainViewModel = koinViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    // Logging out clears the shop data before the session ends, while this screen is still animating out: keep the
    // last settings so the bar doesn't lose its Ledger tab mid-exit.
    val settings = rememberLastNonNull(state.settings)
    val destinations = visibleDestinations(session.permissions, settings?.features?.ledger ?: false)
    val backStack = rememberNavBackStack(navSavedStateConfig, DashboardKey)
    val selected = selectedDestination(backStack)

    fun showTab(destination: TopLevelDestination) {
        backStack.clear()
        backStack.addAll(backStackFor(destination))
    }

    // A restored or open tab the user may no longer see (permissions or the shop's features changed).
    LaunchedEffect(destinations, selected) {
        if (selected !in destinations) showTab(TopLevelDestination.DASHBOARD)
    }

    MainLayout(
        destinations = destinations,
        selected = selected,
        onSelect = ::showTab,
        banner = state.banner?.asString(),
        onRetry = { vm.onAction(MainAction.RetryRefresh) },
    ) {
        MainNavDisplay(backStack)
    }
}

/** [value], or the last non-null value it had while this call stays composed. */
@Composable
private fun <T : Any> rememberLastNonNull(value: T?): T? {
    val last = remember { LastValue<T>() }
    SideEffect { if (value != null) last.value = value }
    return value ?: last.value
}

private class LastValue<T : Any> {
    var value: T? = null
}

/**
 * The home's [NavDisplay]: pushes and pops move on the shared X axis, tabs fade through, and a predictive back
 * gesture shrinks the leaving screen and rounds its corners. Its state is hoisted (scene state, back handler) only
 * so entries can tell a predictive back gesture from an ordinary pop.
 */
@Composable
private fun MainNavDisplay(backStack: NavBackStack<NavKey>) {
    val motion = PosTheme.motion
    val density = LocalDensity.current
    val tabTransitions = rememberTabTransitions()
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    // Remembered: this function re-runs whenever the hoisted state invalidates it, and new lists here would make
    // rememberSceneState build new scenes, so NavDisplay would retarget a running transition (a pop after a cancelled
    // predictive back stalled, then cut).
    val saveableStateDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
    val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
    val entryDecorators = remember(saveableStateDecorator, viewModelStoreDecorator) {
        listOf(saveableStateDecorator, viewModelStoreDecorator, posEntryDecorator())
    }
    val sceneStrategies = remember { listOf(SinglePaneSceneStrategy<NavKey>()) }
    val provider = entryProvider {
        entry<DashboardKey> { ComingSoonScreen(TopLevelDestination.DASHBOARD) }
        entry<PosKey> { ComingSoonScreen(TopLevelDestination.POS) }
        entry<SalesKey> { ComingSoonScreen(TopLevelDestination.SALES) }
        entry<LedgerKey> { ComingSoonScreen(TopLevelDestination.LEDGER) }
        settingsEntries()
    }
    val entries = rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = entryDecorators,
        entryProvider = { key -> provider(key).withTabTransitions(key, tabTransitions) },
    )
    val sceneState = rememberSceneState(entries, sceneStrategies, onBack = onBack)
    val navigationEventState = rememberNavigationEventState(sceneState)
    NavigationBackHandler(sceneState, navigationEventState, onBackCompleted = onBack)
    // Handed down as a State and read only inside the entries, so a gesture doesn't recompose this function per event.
    val inPredictiveBack = remember(navigationEventState) {
        derivedStateOf { navigationEventState.transitionState is NavigationEventTransitionState.InProgress }
    }

    CompositionLocalProvider(LocalInPredictiveBack provides inPredictiveBack) {
        NavDisplay(
            sceneState = sceneState,
            navigationEventState = navigationEventState,
            transitionSpec = { PosTransitions.sharedAxisForward(motion, density) },
            popTransitionSpec = { PosTransitions.sharedAxisBack(motion, density) },
            predictivePopTransitionSpec = { PosTransitions.predictiveBack(motion, density) },
        )
    }
}

/** Whether a predictive back gesture runs on the home's [NavDisplay]; null outside it. */
private val LocalInPredictiveBack = staticCompositionLocalOf<State<Boolean>?> { null }

/**
 * Entry metadata that makes a tab fade through both ways. Switching tabs replaces the back stack
 * (`[Dashboard]` ↔ `[Dashboard, tab]`), which [NavDisplay] would otherwise slide as a push or pop. A predictive back
 * gesture from a tab keeps the shrink. The specs run outside composition, so they read the latest motion.
 */
@Composable
private fun rememberTabTransitions(): Map<String, Any> {
    val motion by rememberUpdatedState(PosTheme.motion)
    return remember {
        metadata {
            put(NavDisplay.TransitionKey) { PosTransitions.fadeThrough(motion) }
            put(NavDisplay.PopTransitionKey) { PosTransitions.fadeThrough(motion) }
        }
    }
}

/** A tab's entry (from this module or a feature) with [tabTransitions] added; other entries unchanged. */
private fun NavEntry<NavKey>.withTabTransitions(key: NavKey, tabTransitions: Map<String, Any>): NavEntry<NavKey> {
    if (!isTopLevelKey(key)) return this
    val entry = this
    return NavEntry(key, contentKey = entry.contentKey, metadata = entry.metadata + tabTransitions) { entry.Content() }
}

/**
 * Gives every entry [NavDisplay]'s animated scope (for shared elements inside the home) and the predictive back
 * corners.
 */
private fun posEntryDecorator(): NavEntryDecorator<NavKey> = NavEntryDecorator { entry ->
    val scope = LocalNavAnimatedContentScope.current
    CompositionLocalProvider(LocalPosAnimatedVisibilityScope provides scope) {
        Box(
            modifier = Modifier.predictiveBackCorners(scope, inGesture = LocalInPredictiveBack.current?.value ?: false),
            propagateMinConstraints = true,
        ) {
            entry.Content()
        }
    }
}

@Composable
private fun MainLayout(
    destinations: List<TopLevelDestination>,
    selected: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    banner: String?,
    onRetry: () -> Unit,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= RailMinWidth
        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    destinations.forEach { destination ->
                        // Keyed, so an item keeps its indicator and bump state when a tab before it hides.
                        key(destination) {
                            val isSelected = destination == selected
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { onSelect(destination) },
                                icon = { TabIcon(destination, isSelected) },
                                label = { TabLabel(destination) },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                            )
                        }
                    }
                }
            }
            Scaffold(
                // The rail already pads for the start edge (system bar or camera cutout); the tabs must not again.
                modifier = if (useRail) {
                    Modifier.weight(1f).consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                } else {
                    Modifier.weight(1f)
                },
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = if (useRail) {
                    ScaffoldDefaults.contentWindowInsets.only(
                        WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom,
                    )
                } else {
                    ScaffoldDefaults.contentWindowInsets
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                            destinations.forEach { destination ->
                                key(destination) {
                                    val isSelected = destination == selected
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { onSelect(destination) },
                                        icon = { TabIcon(destination, isSelected) },
                                        label = { TabLabel(destination) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                },
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                    if (banner != null) {
                        ErrorBanner(message = banner, onRetry = onRetry, modifier = Modifier.padding(PosSpacing.s))
                    }
                    Column(modifier = Modifier.weight(1f)) { content() }
                }
            }
        }
    }
}

/** The tab's icon; it bumps when the tab becomes selected while the M3 indicator grows behind it. */
@Composable
private fun TabIcon(destination: TopLevelDestination, selected: Boolean) {
    Icon(destination.icon, contentDescription = null, modifier = Modifier.selectionBump(selected))
}

@Composable
private fun TabLabel(destination: TopLevelDestination) {
    Text(stringResource(destination.label), style = MaterialTheme.typography.labelMedium)
}

@Preview
@Composable
private fun MainLayoutPreview() {
    PosTheme {
        MainLayout(
            destinations = TopLevelDestination.entries,
            selected = TopLevelDestination.DASHBOARD,
            onSelect = {},
            banner = stringResource(UiRes.string.error_offline),
            onRetry = {},
        ) {
            ComingSoonScreen(TopLevelDestination.DASHBOARD)
        }
    }
}
