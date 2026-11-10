package pk.paksoft.pos.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pk.paksoft.pos.core.designsystem.theme.LocalPosMotion
import pk.paksoft.pos.core.designsystem.theme.PosSpacing

/**
 * How far a tappable surface squeezes while pressed ([pressScale], [posClickable]): the smaller the target, the
 * deeper the squeeze, so the movement reads the same at every size.
 */
object PosPressScale {
    /** Large cards (a shop card, an action card). */
    val card = 0.97f

    /** Buttons, chips and other mid-sized controls. */
    val control = 0.95f

    /** Icon buttons ([PosIconButton]). */
    val icon = 0.88f
}

/**
 * Shrinks the element to [pressedScale] while [interactionSource] reports a press (`fastEffects`) and springs
 * back on release (`fastSpatial`). Does nothing when the user asked for reduced motion.
 *
 * Put it before the element's clip and background so the whole surface scales.
 */
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = PosPressScale.control,
): Modifier = this then PressScaleElement(interactionSource, pressedScale)

/**
 * A ripple plus [pressScale] sharing one interaction source: the press feel for any custom tappable surface.
 * Pass the surface's [shape]: it is clipped inside the scale, so the shape, its background and the ripple shrink
 * together. Put the background after this modifier: `Modifier.posClickable(shape = shape) { }.background(color)`.
 * Large cards keep the default [PosPressScale.card]; smaller targets pass [PosPressScale.control]. An icon button
 * is [PosIconButton].
 */
@Composable
fun Modifier.posClickable(
    enabled: Boolean = true,
    pressedScale: Float = PosPressScale.card,
    shape: Shape = RectangleShape,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return pressScale(interactionSource, pressedScale)
        .then(if (shape == RectangleShape) Modifier else Modifier.clip(shape))
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}

private data class PressScaleElement(
    val interactionSource: InteractionSource,
    val pressedScale: Float,
) : ModifierNodeElement<PressScaleNode>() {
    override fun create() = PressScaleNode(interactionSource, pressedScale)

    override fun update(node: PressScaleNode) = node.update(interactionSource, pressedScale)

    override fun InspectorInfo.inspectableProperties() {
        name = "pressScale"
        properties["interactionSource"] = interactionSource
        properties["pressedScale"] = pressedScale
    }
}

private class PressScaleNode(
    private var interactionSource: InteractionSource,
    private var pressedScale: Float,
) : Modifier.Node(), LayoutModifierNode, CompositionLocalConsumerModifierNode {

    private var scale = Animatable(1f)
    private var pressed = false
    private var collectJob: Job? = null
    private val layerBlock: GraphicsLayerScope.() -> Unit = {
        val s = scale.value
        scaleX = s
        scaleY = s
    }

    override fun onAttach() = collectPresses()

    override fun onDetach() {
        // A node reused by a lazy list must not come back shrunk.
        scale = Animatable(1f)
        pressed = false
        collectJob = null
    }

    fun update(source: InteractionSource, scaleWhenPressed: Float) {
        if (source != interactionSource) {
            interactionSource = source
            collectJob?.cancel()
            pressed = false
            animateToState()
            collectPresses()
        }
        if (scaleWhenPressed != pressedScale) {
            pressedScale = scaleWhenPressed
            if (pressed) animateToState()
        }
    }

    private fun collectPresses() {
        if (!isAttached) return
        collectJob = coroutineScope.launch {
            val presses = mutableListOf<PressInteraction.Press>()
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> presses += interaction
                    is PressInteraction.Release -> presses -= interaction.press
                    is PressInteraction.Cancel -> presses -= interaction.press
                    else -> return@collect
                }
                val nowPressed = presses.isNotEmpty()
                if (nowPressed != pressed) {
                    pressed = nowPressed
                    animateToState()
                }
            }
        }
    }

    private fun animateToState() {
        if (!isAttached) return
        val motion = currentValueOf(LocalPosMotion)
        coroutineScope.launch {
            when {
                motion.reduced -> scale.snapTo(1f)
                pressed -> scale.animateTo(pressedScale, motion.fastEffects())
                else -> scale.animateTo(1f, motion.fastSpatial())
            }
        }
    }

    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeWithLayer(0, 0, layerBlock = layerBlock)
        }
    }
}

@Composable
private fun PosClickableSample() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .posClickable(shape = MaterialTheme.shapes.extraLarge, onClick = {})
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(PosSpacing.xl),
        ) {
            Text("Press and hold", style = MaterialTheme.typography.titleMedium)
        }
        Box(
            modifier = Modifier
                .padding(top = PosSpacing.l)
                .posClickable(
                    pressedScale = PosPressScale.control,
                    shape = MaterialTheme.shapes.medium,
                    role = Role.Button,
                    onClick = {},
                )
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = PosSpacing.l, vertical = PosSpacing.m),
        ) {
            Text(
                "Smaller target",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Preview
@Composable
private fun PosClickableLightPreview() {
    PosPreview(darkTheme = false) { PosClickableSample() }
}

@Preview
@Composable
private fun PosClickableDarkPreview() {
    PosPreview(darkTheme = true) { PosClickableSample() }
}
