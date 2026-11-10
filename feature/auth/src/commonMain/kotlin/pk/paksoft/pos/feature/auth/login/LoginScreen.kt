package pk.paksoft.pos.feature.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
import pk.paksoft.pos.core.designsystem.component.PosButton
import pk.paksoft.pos.core.designsystem.component.PosTextField
import pk.paksoft.pos.core.designsystem.component.ShopAvatar
import pk.paksoft.pos.core.designsystem.component.enterStagger
import pk.paksoft.pos.core.designsystem.component.pressScale
import pk.paksoft.pos.core.designsystem.icon.PosIcons
import pk.paksoft.pos.core.designsystem.motion.PosSharedResize
import pk.paksoft.pos.core.designsystem.motion.posSharedBounds
import pk.paksoft.pos.core.designsystem.motion.posSharedElement
import pk.paksoft.pos.core.designsystem.motion.shopAvatarKey
import pk.paksoft.pos.core.designsystem.motion.shopCardKey
import pk.paksoft.pos.core.designsystem.motion.shopNameKey
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.designsystem.theme.ShopBrand
import pk.paksoft.pos.core.designsystem.theme.colors
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.asString
import pk.paksoft.pos.feature.auth.resources.Res
import pk.paksoft.pos.feature.auth.resources.login_button
import pk.paksoft.pos.feature.auth.resources.login_demo_hint
import pk.paksoft.pos.feature.auth.resources.login_error_password_required
import pk.paksoft.pos.feature.auth.resources.login_field_login
import pk.paksoft.pos.feature.auth.resources.login_field_password
import pk.paksoft.pos.feature.auth.resources.login_hide_password
import pk.paksoft.pos.feature.auth.resources.login_show_password
import pk.paksoft.pos.feature.auth.resources.login_switch_shop

/**
 * Log in to one shop: its identity in a header on the shop's container colour (the picker card grows into it), the
 * form below rising in section by section.
 *
 * The header runs under the status bar, so this screen handles the window insets itself: the header pads its content
 * below the status bar, and the scrolling column stays above the navigation bar and the keyboard.
 */
@Composable
fun LoginScreen(state: LoginUiState, onAction: (LoginAction) -> Unit) {
    val brand = remember(state.shopId, state.shopName) { ShopBrand.forShop(state.shopId, state.shopName) }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ShopHeader(state = state, brand = brand, onSwitchShop = { onAction(LoginAction.ChangeShop) })
            LoginForm(
                state = state,
                onAction = onAction,
                modifier = Modifier
                    .widthIn(max = MaxContentWidth)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    .padding(horizontal = PosSpacing.l, vertical = PosSpacing.xl),
            )
        }
    }
}

@Composable
private fun ShopHeader(state: LoginUiState, brand: ShopBrand, onSwitchShop: () -> Unit) {
    val colors = brand.colors()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .posSharedBounds(shopCardKey(state.shopId), PosSharedResize.Remeasure)
            .clip(HeaderShape)
            .background(colors.container)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
            .padding(start = PosSpacing.xs, end = PosSpacing.xs, top = PosSpacing.xs, bottom = PosSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Off while a login runs or has just succeeded: switching then would end the session being created.
            IconAction(
                icon = PosIcons.Back,
                contentDescription = stringResource(Res.string.login_switch_shop),
                tint = colors.onContainer,
                enabled = !state.isSubmitting,
                onClick = onSwitchShop,
            )
        }
        ShopAvatar(brand, HeaderAvatarSize, Modifier.posSharedElement(shopAvatarKey(state.shopId)))
        Spacer(Modifier.height(PosSpacing.l))
        Text(
            text = state.shopName,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.onContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = PosSpacing.xl)
                .posSharedBounds(shopNameKey(state.shopId), PosSharedResize.Scale),
        )
        Spacer(Modifier.height(PosSpacing.xs))
        Text(
            text = state.shopHost,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = PosSpacing.xl),
        )
    }
}

@Composable
private fun LoginForm(state: LoginUiState, onAction: (LoginAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(PosSpacing.m)) {
        state.banner?.let { ErrorBanner(message = it.asString(), modifier = Modifier.enterStagger(0)) }
        PosTextField(
            value = state.login,
            onValueChange = { onAction(LoginAction.LoginChanged(it)) },
            label = stringResource(Res.string.login_field_login),
            error = state.loginError?.asString(),
            enabled = !state.isSubmitting,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.enterStagger(1).fillMaxWidth(),
        )
        PosTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
            label = stringResource(Res.string.login_field_password),
            error = state.passwordError?.asString(),
            isPassword = true,
            passwordVisible = state.passwordVisible,
            enabled = !state.isSubmitting,
            trailing = {
                IconAction(
                    icon = if (state.passwordVisible) PosIcons.VisibilityOff else PosIcons.Visibility,
                    contentDescription = stringResource(
                        if (state.passwordVisible) Res.string.login_hide_password else Res.string.login_show_password,
                    ),
                    tint = LocalContentColor.current,
                    enabled = !state.isSubmitting,
                    onClick = { onAction(LoginAction.TogglePasswordVisibility) },
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(LoginAction.Submit) }),
            modifier = Modifier.enterStagger(2).fillMaxWidth(),
        )
        PosButton(
            text = stringResource(Res.string.login_button),
            onClick = { onAction(LoginAction.Submit) },
            large = true,
            loading = state.isSubmitting,
            modifier = Modifier.enterStagger(3).padding(top = PosSpacing.s).fillMaxWidth(),
        )
        if (state.isDemoShop) {
            Text(
                text = stringResource(Res.string.login_demo_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.enterStagger(4).fillMaxWidth(),
            )
        }
    }
}

/**
 * A Material icon button (48 dp touch target, ripple) in [tint] with the icon-button squeeze. Disabled, it ignores
 * taps and dims its icon the Material way.
 */
@Composable
private fun IconAction(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    IconButton(
        onClick = onClick,
        modifier = Modifier.pressScale(interactionSource, ICON_PRESSED_SCALE),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
        interactionSource = interactionSource,
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

private val MaxContentWidth = 420.dp
private val HeaderAvatarSize = 84.dp
private val HeaderShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
private const val ICON_PRESSED_SCALE = 0.88f

private val SmfState = LoginUiState(
    shopId = "smf",
    shopName = "Super Madina Solar Fan",
    shopHost = "pbsf.org.pk/smf",
    login = "cashier1",
)

private val DemoErrorState = LoginUiState(
    shopId = "demo",
    shopName = "Demo shop",
    shopHost = "demo.paksoft.invalid",
    isDemoShop = true,
    login = "cashier1",
    passwordError = UiText.Res(Res.string.login_error_password_required),
    banner = UiText.Raw("The username/email or password is incorrect."),
)

@Preview
@Composable
private fun LoginScreenLightPreview() {
    PosTheme(darkTheme = false) { LoginScreen(state = SmfState, onAction = {}) }
}

@Preview
@Composable
private fun LoginScreenDarkPreview() {
    PosTheme(darkTheme = true) { LoginScreen(state = SmfState, onAction = {}) }
}

@Preview
@Composable
private fun LoginScreenDemoErrorLightPreview() {
    PosTheme(darkTheme = false) { LoginScreen(state = DemoErrorState, onAction = {}) }
}

@Preview
@Composable
private fun LoginScreenDemoErrorDarkPreview() {
    PosTheme(darkTheme = true) { LoginScreen(state = DemoErrorState, onAction = {}) }
}
