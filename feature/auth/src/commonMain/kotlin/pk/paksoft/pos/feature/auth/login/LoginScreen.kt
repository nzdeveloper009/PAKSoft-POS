package pk.paksoft.pos.feature.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import pk.paksoft.pos.core.designsystem.component.ErrorBanner
import pk.paksoft.pos.core.designsystem.component.PosButton
import pk.paksoft.pos.core.designsystem.component.PosTextField
import pk.paksoft.pos.core.designsystem.theme.PosSpacing
import pk.paksoft.pos.core.designsystem.theme.PosTheme
import pk.paksoft.pos.core.ui.UiText
import pk.paksoft.pos.core.ui.asString
import pk.paksoft.pos.feature.auth.resources.Res
import pk.paksoft.pos.feature.auth.resources.login_button
import pk.paksoft.pos.feature.auth.resources.login_change_shop
import pk.paksoft.pos.feature.auth.resources.login_error_password_required
import pk.paksoft.pos.feature.auth.resources.login_field_login
import pk.paksoft.pos.feature.auth.resources.login_field_password
import pk.paksoft.pos.feature.auth.resources.login_hide_password
import pk.paksoft.pos.feature.auth.resources.login_shop_label
import pk.paksoft.pos.feature.auth.resources.login_show_password
import pk.paksoft.pos.feature.auth.resources.login_title

@Composable
fun LoginScreen(state: LoginUiState, onAction: (LoginAction) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.verticalScroll(rememberScrollState())) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth().padding(PosSpacing.l),
                verticalArrangement = Arrangement.spacedBy(PosSpacing.m),
            ) {
                Text(stringResource(Res.string.login_title), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(Res.string.login_shop_label, state.shopName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.banner?.let { ErrorBanner(message = it.asString()) }
                PosTextField(
                    value = state.login,
                    onValueChange = { onAction(LoginAction.LoginChanged(it)) },
                    label = stringResource(Res.string.login_field_login),
                    error = state.loginError?.asString(),
                    enabled = !state.isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
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
                        TextButton(onClick = { onAction(LoginAction.TogglePasswordVisibility) }) {
                            Text(
                                stringResource(
                                    if (state.passwordVisible) Res.string.login_hide_password else Res.string.login_show_password,
                                ),
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAction(LoginAction.Submit) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                PosButton(
                    text = stringResource(Res.string.login_button),
                    onClick = { onAction(LoginAction.Submit) },
                    loading = state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(
                    onClick = { onAction(LoginAction.ChangeShop) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(stringResource(Res.string.login_change_shop))
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    PosTheme {
        LoginScreen(state = LoginUiState(shopName = "Super Madina Solar Fan", login = "cashier1"), onAction = {})
    }
}

@Preview
@Composable
private fun LoginScreenErrorPreview() {
    PosTheme {
        LoginScreen(
            state = LoginUiState(
                shopName = "Super Madina Solar Fan",
                login = "cashier1",
                passwordError = UiText.Res(Res.string.login_error_password_required),
                banner = UiText.Raw("The username/email or password is incorrect."),
            ),
            onAction = {},
        )
    }
}
