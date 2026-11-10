package pk.paksoft.pos.core.designsystem.theme

import androidx.compose.runtime.Composable

/** True when the user asked the OS to reduce motion. Re-read when the app resumes. */
@Composable
expect fun rememberReduceMotion(): Boolean
