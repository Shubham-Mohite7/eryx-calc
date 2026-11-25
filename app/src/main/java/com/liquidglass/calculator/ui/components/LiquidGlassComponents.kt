package com.liquidglass.calculator.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.liquidglass.calculator.data.CalculatorMode
import com.liquidglass.calculator.ui.theme.LocalGlassPalette

/**
 * Ambient frosted backdrop using purely solid translucent blurred shapes (no gradients).
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalGlassPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(p.backgroundBase)
    ) {
        if (p.blurEnabled) {
            val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Modifier.blur(76.dp)
            } else {
                Modifier
            }
            Box(modifier = Modifier.fillMaxSize().then(blurModifier)) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = (-60).dp, y = 40.dp)
                        .clip(CircleShape)
                        .background(p.backdropBlobA)
                )
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 70.dp, y = 180.dp)
                        .clip(CircleShape)
                        .background(p.backdropBlobB)
                )
                Box(
                    modifier = Modifier
                        .size(340.dp)
                        .align(Alignment.BottomStart)
                        .offset(x = (-40).dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(p.backdropBlobC)
                )
            }
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalGlassPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "cardScale"
    )

    val clickMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor ?: p.cardSurface)
            .border(width = 0.9.dp, color = borderColor ?: p.glassBorder, shape = shape)
            .then(clickMod),
        content = content
    )
}

@Composable
fun GlassCircleButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    containerColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalGlassPalette.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 550f),
        label = "circleBtnScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(containerColor ?: p.cardSurface)
            .border(0.9.dp, p.glassBorder, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun GlassKeyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    pressedColor: Color? = null,
    borderColor: Color? = null,
    contentDescription: String? = null,
    shape: Shape? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val p = LocalGlassPalette.current
    val keyShape = shape ?: if (p.isCircularKeys) RoundedCornerShape(999.dp) else RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 600f),
        label = "keyScale"
    )
    val bg = if (pressed) (pressedColor ?: p.keyPressedSurface) else containerColor

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(keyShape)
            .background(bg)
            .border(0.9.dp, borderColor ?: p.glassBorder, keyShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun GlassSegmentedModeBar(
    selectedMode: CalculatorMode,
    onSelectMode: (CalculatorMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    val modes = CalculatorMode.entries
    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(p.pillContainerSurface)
            .border(0.85.dp, p.glassBorder, RoundedCornerShape(999.dp))
            .padding(3.dp)
    ) {
        val segmentWidth = maxWidth / modes.size
        val indicatorOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(dampingRatio = 0.80f, stiffness = 420f),
            label = "segOffset"
        )

        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(p.activePillSurface)
                .border(0.6.dp, p.glassBorder, RoundedCornerShape(999.dp))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            modes.forEach { mode ->
                val active = mode == selectedMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectMode(mode) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = mode.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            color = if (active) p.textPrimary else p.textSecondary,
                            fontSize = (12.5f * p.textScale).coerceAtMost(15f).sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSegmentedTextBar(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 36.dp
) {
    val p = LocalGlassPalette.current
    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(p.pillContainerSurface)
            .border(0.85.dp, p.glassBorder, RoundedCornerShape(999.dp))
            .padding(3.dp)
    ) {
        val segW = maxWidth / options.size.coerceAtLeast(1)
        val offset by animateDpAsState(
            targetValue = segW * selectedIndex.coerceIn(0, (options.size - 1).coerceAtLeast(0)),
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
            label = "textSeg"
        )
        Box(
            modifier = Modifier
                .offset(x = offset)
                .width(segW)
                .fillMaxHeight()
                .clip(RoundedCornerShape(999.dp))
                .background(p.activePillSurface)
                .border(0.5.dp, p.glassBorder, RoundedCornerShape(999.dp))
        )
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { idx, label ->
                val active = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(idx) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BasicText(
                        text = label,
                        maxLines = 1,
                        style = TextStyle(
                            color = if (active) p.textPrimary else p.textSecondary,
                            fontSize = (12.5f * p.textScale).coerceAtMost(15f).sp,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = LocalGlassPalette.current
    val trackColor by animateColorAsState(
        targetValue = if (checked) p.switchTrackOn else p.switchTrackOff,
        label = "switchTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(26.dp)
                .clip(CircleShape)
                .background(p.switchThumb)
        )
    }
}

@Composable
fun GlassSubpageTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    val p = LocalGlassPalette.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        GlassCircleButton(
            onClick = onBack,
            contentDescription = "Back",
            size = 40.dp,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            IconChevronLeft(p.textPrimary, 20.dp)
        }
        BasicText(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                color = p.textPrimary,
                fontSize = (18f * p.textScale).sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 96.dp)
        )
        if (trailing != null) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailing()
            }
        }
    }
}

@Composable
fun GlassModalCardDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val p = LocalGlassPalette.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.28f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(p.modalSurface)
                    .border(1.dp, p.glassBorder, RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(18.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun GlassText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    fontSizeSp: Float = 15f,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign = TextAlign.Unspecified
) {
    val p = LocalGlassPalette.current
    BasicText(
        text = text,
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            color = color ?: p.textPrimary,
            fontSize = (fontSizeSp * p.textScale).sp,
            fontWeight = fontWeight,
            textAlign = textAlign
        )
    )
}
