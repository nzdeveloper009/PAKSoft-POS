package pk.paksoft.pos.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** Entry point for the iOS shell: `MainViewControllerKt.MainViewController()` in Swift. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
