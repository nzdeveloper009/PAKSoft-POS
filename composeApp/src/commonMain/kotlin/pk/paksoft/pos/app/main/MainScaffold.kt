package pk.paksoft.pos.app.main

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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pk.paksoft.pos.app.navigation.DashboardKey
import pk.paksoft.pos.app.navigation.LedgerKey
import pk.paksoft.pos.app.navigation.PosKey
import pk.paksoft.pos.app.navigation.SalesKey
import pk.paksoft.pos.app.navigation.navSavedStateConfig
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
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
    val destinations = visibleDestinations(session.permissions, state.settings?.features?.ledger ?: false)
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
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<DashboardKey> { ComingSoonScreen(stringResource(TopLevelDestination.DASHBOARD.label)) }
                entry<PosKey> { ComingSoonScreen(stringResource(TopLevelDestination.POS.label)) }
                entry<SalesKey> { ComingSoonScreen(stringResource(TopLevelDestination.SALES.label)) }
                entry<LedgerKey> { ComingSoonScreen(stringResource(TopLevelDestination.LEDGER.label)) }
                settingsEntries()
            },
        )
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
                NavigationRail {
                    destinations.forEach { destination ->
                        NavigationRailItem(
                            selected = destination == selected,
                            onClick = { onSelect(destination) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
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
                        NavigationBar {
                            destinations.forEach { destination ->
                                NavigationBarItem(
                                    selected = destination == selected,
                                    onClick = { onSelect(destination) },
                                    icon = { Icon(destination.icon, contentDescription = null) },
                                    label = { Text(stringResource(destination.label)) },
                                )
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
            ComingSoonScreen(title = stringResource(TopLevelDestination.DASHBOARD.label))
        }
    }
}
