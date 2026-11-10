package pk.paksoft.pos.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/** Single-line outlined field with 16 dp corners; [error] replaces the supporting text and turns it red. */
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
        shape = MaterialTheme.shapes.medium,
    )
}

@Composable
private fun PosTextFieldSample() {
    Column(verticalArrangement = Arrangement.spacedBy(PosSpacing.m)) {
        PosTextField(value = "cashier1", onValueChange = {}, label = "Username or email", modifier = Modifier.fillMaxWidth())
        PosTextField(
            value = "",
            onValueChange = {},
            label = "Password",
            error = "Enter your password.",
            isPassword = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun PosTextFieldLightPreview() {
    PosPreview(darkTheme = false) { PosTextFieldSample() }
}

@Preview
@Composable
private fun PosTextFieldDarkPreview() {
    PosPreview(darkTheme = true) { PosTextFieldSample() }
}
