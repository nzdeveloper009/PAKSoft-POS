package pk.paksoft.pos.core.designsystem.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pk.paksoft.pos.core.designsystem.component.PosPreview
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/**
 * Every icon the app shows. Material Icons Rounded from `material-icons-core` where the core set has them;
 * the rest are built here from Material's own path data (Apache 2.0), Rounded style.
 * Features take icons only from here.
 */
object PosIcons {
    val Home: ImageVector get() = Icons.Rounded.Home
    val Pos: ImageVector get() = Icons.Rounded.ShoppingCart
    val Sales: ImageVector by lazy { receiptLong() }
    val Ledger: ImageVector by lazy { menuBook() }
    val More: ImageVector get() = Icons.Rounded.Menu
    val Back: ImageVector get() = Icons.AutoMirrored.Rounded.ArrowBack
    val Add: ImageVector get() = Icons.Rounded.Add
    val Check: ImageVector get() = Icons.Rounded.Check
    val Search: ImageVector get() = Icons.Rounded.Search
    val Scan: ImageVector by lazy { barcodeScan() }
    val Refresh: ImageVector get() = Icons.Rounded.Refresh
    val Person: ImageVector get() = Icons.Rounded.Person
    val Visibility: ImageVector by lazy { visibility() }
    val VisibilityOff: ImageVector by lazy { visibilityOff() }
    val Share: ImageVector get() = Icons.Rounded.Share
    val Logout: ImageVector by lazy { logout() }
    val Store: ImageVector by lazy { store() }
    val ChevronRight: ImageVector get() = Icons.AutoMirrored.Rounded.KeyboardArrowRight
    val Error: ImageVector by lazy { errorCircle() }
    val CloudOff: ImageVector by lazy { cloudOff() }
}

private inline fun icon(name: String, autoMirror: Boolean = false, paths: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        autoMirror = autoMirror,
    ).apply(paths).build()

/** A filled path; the colour is only a mask, `Icon` tints it. */
private inline fun ImageVector.Builder.filledPath(block: PathBuilder.() -> Unit) {
    path(fill = SolidColor(Color.Black), pathBuilder = block)
}

// Material: AutoMirrored.Rounded.ReceiptLong
private fun receiptLong() = icon("PosIcons.Sales", autoMirror = true) {
    filledPath {
        moveTo(14.0f, 9.0f)
        horizontalLineToRelative(-4.0f)
        curveTo(9.45f, 9.0f, 9.0f, 8.55f, 9.0f, 8.0f)
        verticalLineToRelative(0.0f)
        curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
        horizontalLineToRelative(4.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
        verticalLineToRelative(0.0f)
        curveTo(15.0f, 8.55f, 14.55f, 9.0f, 14.0f, 9.0f)
        close()
    }
    filledPath {
        moveTo(14.0f, 12.0f)
        horizontalLineToRelative(-4.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
        verticalLineToRelative(0.0f)
        curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
        horizontalLineToRelative(4.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
        verticalLineToRelative(0.0f)
        curveTo(15.0f, 11.55f, 14.55f, 12.0f, 14.0f, 12.0f)
        close()
    }
    filledPath {
        moveTo(19.5f, 3.5f)
        lineTo(18.0f, 2.0f)
        lineToRelative(-1.5f, 1.5f)
        lineTo(15.0f, 2.0f)
        lineToRelative(-1.5f, 1.5f)
        lineTo(12.0f, 2.0f)
        lineToRelative(-1.5f, 1.5f)
        lineTo(9.0f, 2.0f)
        lineTo(7.5f, 3.5f)
        lineTo(6.0f, 2.0f)
        verticalLineToRelative(14.0f)
        horizontalLineTo(4.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        verticalLineToRelative(2.0f)
        curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
        horizontalLineToRelative(12.0f)
        curveToRelative(1.66f, 0.0f, 3.0f, -1.34f, 3.0f, -3.0f)
        verticalLineTo(2.0f)
        lineTo(19.5f, 3.5f)
        close()
        moveTo(15.0f, 20.0f)
        horizontalLineTo(6.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
        verticalLineToRelative(-1.0f)
        horizontalLineToRelative(3.0f)
        horizontalLineToRelative(4.0f)
        horizontalLineToRelative(3.0f)
        verticalLineTo(20.0f)
        close()
        moveTo(19.0f, 19.0f)
        curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
        reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        horizontalLineToRelative(-2.0f)
        horizontalLineToRelative(-2.0f)
        horizontalLineTo(8.0f)
        verticalLineTo(5.0f)
        horizontalLineToRelative(11.0f)
        verticalLineTo(19.0f)
        close()
    }
    filledPath {
        moveTo(17.0f, 8.0f)
        moveToRelative(-1.0f, 0.0f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
    }
    filledPath {
        moveTo(17.0f, 11.0f)
        moveToRelative(-1.0f, 0.0f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, 2.0f, 0.0f)
        arcToRelative(1.0f, 1.0f, 0.0f, true, true, -2.0f, 0.0f)
    }
}

// Material: AutoMirrored.Rounded.MenuBook
private fun menuBook() = icon("PosIcons.Ledger", autoMirror = true) {
    filledPath {
        moveTo(17.5f, 4.5f)
        curveToRelative(-1.95f, 0.0f, -4.05f, 0.4f, -5.5f, 1.5f)
        curveToRelative(-1.45f, -1.1f, -3.55f, -1.5f, -5.5f, -1.5f)
        curveToRelative(-1.45f, 0.0f, -2.99f, 0.22f, -4.28f, 0.79f)
        curveTo(1.49f, 5.62f, 1.0f, 6.33f, 1.0f, 7.14f)
        lineToRelative(0.0f, 11.28f)
        curveToRelative(0.0f, 1.3f, 1.22f, 2.26f, 2.48f, 1.94f)
        curveTo(4.46f, 20.11f, 5.5f, 20.0f, 6.5f, 20.0f)
        curveToRelative(1.56f, 0.0f, 3.22f, 0.26f, 4.56f, 0.92f)
        curveToRelative(0.6f, 0.3f, 1.28f, 0.3f, 1.87f, 0.0f)
        curveToRelative(1.34f, -0.67f, 3.0f, -0.92f, 4.56f, -0.92f)
        curveToRelative(1.0f, 0.0f, 2.04f, 0.11f, 3.02f, 0.36f)
        curveToRelative(1.26f, 0.33f, 2.48f, -0.63f, 2.48f, -1.94f)
        lineToRelative(0.0f, -11.28f)
        curveToRelative(0.0f, -0.81f, -0.49f, -1.52f, -1.22f, -1.85f)
        curveTo(20.49f, 4.72f, 18.95f, 4.5f, 17.5f, 4.5f)
        close()
        moveTo(21.0f, 17.23f)
        curveToRelative(0.0f, 0.63f, -0.58f, 1.09f, -1.2f, 0.98f)
        curveToRelative(-0.75f, -0.14f, -1.53f, -0.2f, -2.3f, -0.2f)
        curveToRelative(-1.7f, 0.0f, -4.15f, 0.65f, -5.5f, 1.5f)
        verticalLineTo(8.0f)
        curveToRelative(1.35f, -0.85f, 3.8f, -1.5f, 5.5f, -1.5f)
        curveToRelative(0.92f, 0.0f, 1.83f, 0.09f, 2.7f, 0.28f)
        curveToRelative(0.46f, 0.1f, 0.8f, 0.51f, 0.8f, 0.98f)
        verticalLineTo(17.23f)
        close()
    }
    filledPath {
        moveTo(13.98f, 11.01f)
        curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
        curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
        curveToRelative(1.54f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
        curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
        curveToRelative(-0.05f, 0.41f, -0.42f, 0.71f, -0.83f, 0.66f)
        curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
        curveTo(14.13f, 10.99f, 14.05f, 11.01f, 13.98f, 11.01f)
        close()
    }
    filledPath {
        moveTo(13.98f, 13.67f)
        curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
        curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
        curveToRelative(1.53f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
        curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
        curveToRelative(-0.05f, 0.41f, -0.42f, 0.71f, -0.83f, 0.66f)
        curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
        curveTo(14.13f, 13.66f, 14.05f, 13.67f, 13.98f, 13.67f)
        close()
    }
    filledPath {
        moveTo(13.98f, 16.33f)
        curveToRelative(-0.32f, 0.0f, -0.61f, -0.2f, -0.71f, -0.52f)
        curveToRelative(-0.13f, -0.39f, 0.09f, -0.82f, 0.48f, -0.94f)
        curveToRelative(1.53f, -0.5f, 3.53f, -0.66f, 5.36f, -0.45f)
        curveToRelative(0.41f, 0.05f, 0.71f, 0.42f, 0.66f, 0.83f)
        curveToRelative(-0.05f, 0.41f, -0.42f, 0.7f, -0.83f, 0.66f)
        curveToRelative(-1.62f, -0.19f, -3.39f, -0.04f, -4.73f, 0.39f)
        curveTo(14.13f, 16.32f, 14.05f, 16.33f, 13.98f, 16.33f)
        close()
    }
}

// Material: Rounded.CropFree frame (the viewfinder) with four barcode bars inside.
// Material Icons has no barcode scanner; the approved preview uses this mark.
private fun barcodeScan() = icon("PosIcons.Scan") {
    filledPath {
        moveTo(3.0f, 5.0f)
        verticalLineToRelative(3.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
        lineTo(5.0f, 6.0f)
        curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
        horizontalLineToRelative(2.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
        lineTo(5.0f, 3.0f)
        curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
        close()
        moveTo(4.0f, 15.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        verticalLineToRelative(3.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(3.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
        lineTo(6.0f, 19.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
        verticalLineToRelative(-2.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        close()
        moveTo(19.0f, 18.0f)
        curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
        horizontalLineToRelative(-2.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(3.0f)
        curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
        verticalLineToRelative(-3.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
        verticalLineToRelative(2.0f)
        close()
        moveTo(19.0f, 3.0f)
        horizontalLineToRelative(-3.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(2.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
        verticalLineToRelative(2.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
        lineTo(21.0f, 5.0f)
        curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
        close()
    }
    filledPath {
        moveTo(7.0f, 8.0f)
        horizontalLineToRelative(1.5f)
        verticalLineToRelative(8.0f)
        horizontalLineTo(7.0f)
        close()
        moveTo(10.0f, 8.0f)
        horizontalLineToRelative(1.0f)
        verticalLineToRelative(8.0f)
        horizontalLineToRelative(-1.0f)
        close()
        moveTo(12.5f, 8.0f)
        horizontalLineTo(15.0f)
        verticalLineToRelative(8.0f)
        horizontalLineToRelative(-2.5f)
        close()
        moveTo(16.0f, 8.0f)
        horizontalLineToRelative(1.0f)
        verticalLineToRelative(8.0f)
        horizontalLineToRelative(-1.0f)
        close()
    }
}

// Material: Rounded.Visibility
private fun visibility() = icon("PosIcons.Visibility") {
    filledPath {
        moveTo(12.0f, 4.0f)
        curveTo(7.0f, 4.0f, 2.73f, 7.11f, 1.0f, 11.5f)
        curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
        reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
        curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
        close()
        moveTo(12.0f, 16.5f)
        curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
        reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
        reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
        reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
        close()
        moveTo(12.0f, 8.5f)
        curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
        reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
        reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
        reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
        close()
    }
}

// Material: Rounded.VisibilityOff
private fun visibilityOff() = icon("PosIcons.VisibilityOff") {
    filledPath {
        moveTo(12.0f, 6.5f)
        curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
        curveToRelative(0.0f, 0.51f, -0.1f, 1.0f, -0.24f, 1.46f)
        lineToRelative(3.06f, 3.06f)
        curveToRelative(1.39f, -1.23f, 2.49f, -2.77f, 3.18f, -4.53f)
        curveTo(21.27f, 7.11f, 17.0f, 4.0f, 12.0f, 4.0f)
        curveToRelative(-1.27f, 0.0f, -2.49f, 0.2f, -3.64f, 0.57f)
        lineToRelative(2.17f, 2.17f)
        curveToRelative(0.47f, -0.14f, 0.96f, -0.24f, 1.47f, -0.24f)
        close()
        moveTo(2.71f, 3.16f)
        curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
        lineToRelative(1.97f, 1.97f)
        curveTo(3.06f, 7.83f, 1.77f, 9.53f, 1.0f, 11.5f)
        curveTo(2.73f, 15.89f, 7.0f, 19.0f, 12.0f, 19.0f)
        curveToRelative(1.52f, 0.0f, 2.97f, -0.3f, 4.31f, -0.82f)
        lineToRelative(2.72f, 2.72f)
        curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
        lineTo(4.13f, 3.16f)
        curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.42f, 0.0f)
        close()
        moveTo(12.0f, 16.5f)
        curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
        curveToRelative(0.0f, -0.77f, 0.18f, -1.5f, 0.49f, -2.14f)
        lineToRelative(1.57f, 1.57f)
        curveToRelative(-0.03f, 0.18f, -0.06f, 0.37f, -0.06f, 0.57f)
        curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
        curveToRelative(0.2f, 0.0f, 0.38f, -0.03f, 0.57f, -0.07f)
        lineTo(14.14f, 16.0f)
        curveToRelative(-0.65f, 0.32f, -1.37f, 0.5f, -2.14f, 0.5f)
        close()
        moveTo(14.97f, 11.17f)
        curveToRelative(-0.15f, -1.4f, -1.25f, -2.49f, -2.64f, -2.64f)
        lineToRelative(2.64f, 2.64f)
        close()
    }
}

// Material: AutoMirrored.Rounded.Logout
private fun logout() = icon("PosIcons.Logout", autoMirror = true) {
    filledPath {
        moveTo(5.0f, 5.0f)
        horizontalLineToRelative(6.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(0.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        horizontalLineTo(5.0f)
        curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
        verticalLineToRelative(14.0f)
        curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
        horizontalLineToRelative(6.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(0.0f)
        curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
        horizontalLineTo(5.0f)
        verticalLineTo(5.0f)
        close()
    }
    filledPath {
        moveTo(20.65f, 11.65f)
        lineToRelative(-2.79f, -2.79f)
        curveTo(17.54f, 8.54f, 17.0f, 8.76f, 17.0f, 9.21f)
        verticalLineTo(11.0f)
        horizontalLineToRelative(-7.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        verticalLineToRelative(0.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(7.0f)
        verticalLineToRelative(1.79f)
        curveToRelative(0.0f, 0.45f, 0.54f, 0.67f, 0.85f, 0.35f)
        lineToRelative(2.79f, -2.79f)
        curveTo(20.84f, 12.16f, 20.84f, 11.84f, 20.65f, 11.65f)
        close()
    }
}

// Material: Rounded.Store
private fun store() = icon("PosIcons.Store") {
    filledPath {
        moveTo(5.0f, 6.0f)
        horizontalLineToRelative(14.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
        lineTo(5.0f, 4.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
        reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
        close()
        moveTo(20.16f, 7.8f)
        curveToRelative(-0.09f, -0.46f, -0.5f, -0.8f, -0.98f, -0.8f)
        lineTo(4.82f, 7.0f)
        curveToRelative(-0.48f, 0.0f, -0.89f, 0.34f, -0.98f, 0.8f)
        lineToRelative(-1.0f, 5.0f)
        curveToRelative(-0.12f, 0.62f, 0.35f, 1.2f, 0.98f, 1.2f)
        lineTo(4.0f, 14.0f)
        verticalLineToRelative(5.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(8.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(-5.0f)
        horizontalLineToRelative(4.0f)
        verticalLineToRelative(5.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(-5.0f)
        horizontalLineToRelative(0.18f)
        curveToRelative(0.63f, 0.0f, 1.1f, -0.58f, 0.98f, -1.2f)
        lineToRelative(-1.0f, -5.0f)
        close()
        moveTo(12.0f, 18.0f)
        lineTo(6.0f, 18.0f)
        verticalLineToRelative(-4.0f)
        horizontalLineToRelative(6.0f)
        verticalLineToRelative(4.0f)
        close()
    }
}

// Material: Rounded.Error
private fun errorCircle() = icon("PosIcons.Error") {
    filledPath {
        moveTo(12.0f, 2.0f)
        curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
        reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
        reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
        reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
        close()
        moveTo(12.0f, 13.0f)
        curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
        lineTo(11.0f, 8.0f)
        curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
        reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
        verticalLineToRelative(4.0f)
        curveToRelative(0.0f, 0.55f, -0.45f, 1.0f, -1.0f, 1.0f)
        close()
        moveTo(13.0f, 17.0f)
        horizontalLineToRelative(-2.0f)
        verticalLineToRelative(-2.0f)
        horizontalLineToRelative(2.0f)
        verticalLineToRelative(2.0f)
        close()
    }
}

// Material: Rounded.CloudOff
private fun cloudOff() = icon("PosIcons.CloudOff") {
    filledPath {
        moveTo(24.0f, 15.0f)
        curveToRelative(0.0f, -2.64f, -2.05f, -4.78f, -4.65f, -4.96f)
        curveTo(18.67f, 6.59f, 15.64f, 4.0f, 12.0f, 4.0f)
        curveToRelative(-1.33f, 0.0f, -2.57f, 0.36f, -3.65f, 0.97f)
        lineToRelative(1.49f, 1.49f)
        curveTo(10.51f, 6.17f, 11.23f, 6.0f, 12.0f, 6.0f)
        curveToRelative(3.04f, 0.0f, 5.5f, 2.46f, 5.5f, 5.5f)
        verticalLineToRelative(0.5f)
        horizontalLineTo(19.0f)
        curveToRelative(1.66f, 0.0f, 3.0f, 1.34f, 3.0f, 3.0f)
        curveToRelative(0.0f, 0.99f, -0.48f, 1.85f, -1.21f, 2.4f)
        lineToRelative(1.41f, 1.41f)
        curveToRelative(1.09f, -0.92f, 1.8f, -2.27f, 1.8f, -3.81f)
        close()
        moveTo(3.71f, 4.56f)
        curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0.0f, 1.41f)
        lineToRelative(2.06f, 2.06f)
        horizontalLineToRelative(-0.42f)
        curveToRelative(-3.28f, 0.35f, -5.76f, 3.34f, -5.29f, 6.79f)
        curveTo(0.46f, 17.84f, 3.19f, 20.0f, 6.22f, 20.0f)
        horizontalLineToRelative(11.51f)
        lineToRelative(1.29f, 1.29f)
        curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0.0f)
        curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0.0f, -1.41f)
        lineTo(5.12f, 4.56f)
        curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0.0f)
        close()
        moveTo(6.0f, 18.0f)
        curveToRelative(-2.21f, 0.0f, -4.0f, -1.79f, -4.0f, -4.0f)
        reflectiveCurveToRelative(1.79f, -4.0f, 4.0f, -4.0f)
        horizontalLineToRelative(1.73f)
        lineToRelative(8.0f, 8.0f)
        horizontalLineTo(6.0f)
        close()
    }
}

private val AllIcons: List<ImageVector>
    get() = with(PosIcons) {
        listOf(
            Home, Pos, Sales, Ledger, More, Back, Add, Check, Search, Scan,
            Refresh, Person, Visibility, VisibilityOff, Share, Logout, Store, ChevronRight, Error, CloudOff,
        )
    }

@Composable
private fun PosIconsSample() {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(PosSpacing.l),
        verticalArrangement = Arrangement.spacedBy(PosSpacing.l),
    ) {
        AllIcons.forEach { Icon(it, contentDescription = null) }
    }
}

@Preview
@Composable
private fun PosIconsLightPreview() {
    PosPreview(darkTheme = false) { PosIconsSample() }
}

@Preview
@Composable
private fun PosIconsDarkPreview() {
    PosPreview(darkTheme = true) { PosIconsSample() }
}
