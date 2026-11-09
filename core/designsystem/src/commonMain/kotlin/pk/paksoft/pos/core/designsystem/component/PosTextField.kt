package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.theme.PosTheme

@Composable
fun PosTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { message -> { Text(message) } },
        trailingIcon = trailing,
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
    )
}

@Preview
@Composable
private fun PosTextFieldPreview() {
    PosTheme {
        PosTextField(value = "", onValueChange = {}, label = "Username or email", error = "Enter your username or email.")
    }
}
