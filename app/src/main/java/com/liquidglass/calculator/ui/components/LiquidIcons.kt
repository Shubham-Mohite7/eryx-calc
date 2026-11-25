package com.liquidglass.calculator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IconGridMode(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val stroke = Stroke(width = s * 0.085f)
        val box = s * 0.30f
        val gap = s * 0.14f
        val start = (s - (box * 2 + gap)) / 2f
        val r = CornerRadius(s * 0.07f, s * 0.07f)
        for (row in 0..1) {
            for (col in 0..1) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(start + col * (box + gap), start + row * (box + gap)),
                    size = Size(box, box),
                    cornerRadius = r,
                    style = stroke
                )
            }
        }
    }
}

@Composable
fun IconHistoryClock(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        val radius = s * 0.36f
        val c = Offset(s / 2f, s / 2f)
        drawCircle(color = tint, radius = radius, center = c, style = Stroke(width = sw))
        drawLine(
            color = tint,
            start = c,
            end = Offset(c.x, c.y - radius * 0.55f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = c,
            end = Offset(c.x + radius * 0.48f, c.y + radius * 0.22f),
            strokeWidth = sw,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun IconMoreDots(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val r = s * 0.075f
        val cy = s / 2f
        drawCircle(tint, r, Offset(s * 0.24f, cy))
        drawCircle(tint, r, Offset(s * 0.50f, cy))
        drawCircle(tint, r, Offset(s * 0.76f, cy))
    }
}

@Composable
fun IconChevronLeft(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.62f, s * 0.22f)
            lineTo(s * 0.34f, s * 0.50f)
            lineTo(s * 0.62f, s * 0.78f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = s * 0.095f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconChevronRight(tint: Color, size: Dp = 16.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.38f, s * 0.22f)
            lineTo(s * 0.66f, s * 0.50f)
            lineTo(s * 0.38f, s * 0.78f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = s * 0.10f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconChevronDown(tint: Color, size: Dp = 14.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.24f, s * 0.38f)
            lineTo(s * 0.50f, s * 0.65f)
            lineTo(s * 0.76f, s * 0.38f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = s * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconCloseX(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.095f
        drawLine(tint, Offset(s * 0.25f, s * 0.25f), Offset(s * 0.75f, s * 0.75f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.75f, s * 0.25f), Offset(s * 0.25f, s * 0.75f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconCheckmark(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val path = Path().apply {
            moveTo(s * 0.22f, s * 0.52f)
            lineTo(s * 0.42f, s * 0.72f)
            lineTo(s * 0.78f, s * 0.30f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = s * 0.105f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun IconBackspaceKey(tint: Color, size: Dp = 25.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.105f
        val path = Path().apply {
            moveTo(s * 0.36f, s * 0.24f)
            lineTo(s * 0.86f, s * 0.24f)
            lineTo(s * 0.86f, s * 0.76f)
            lineTo(s * 0.36f, s * 0.76f)
            lineTo(s * 0.14f, s * 0.50f)
            close()
        }
        drawPath(path, tint, style = Stroke(width = sw, join = StrokeJoin.Round))
        drawLine(tint, Offset(s * 0.48f, s * 0.39f), Offset(s * 0.70f, s * 0.61f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.70f, s * 0.39f), Offset(s * 0.48f, s * 0.61f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconExpandCorners(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.10f
        val d = s * 0.20f
        val l = s * 0.22f
        val r = s * 0.78f
        // Top-left
        drawLine(tint, Offset(l, d + l * 0.5f), Offset(l, l), sw, StrokeCap.Round)
        drawLine(tint, Offset(l, l), Offset(l + d, l), sw, StrokeCap.Round)
        // Top-right
        drawLine(tint, Offset(r - d, l), Offset(r, l), sw, StrokeCap.Round)
        drawLine(tint, Offset(r, l), Offset(r, l + d), sw, StrokeCap.Round)
        // Bottom-left
        drawLine(tint, Offset(l, r - d), Offset(l, r), sw, StrokeCap.Round)
        drawLine(tint, Offset(l, r), Offset(l + d, r), sw, StrokeCap.Round)
        // Bottom-right
        drawLine(tint, Offset(r - d, r), Offset(r, r), sw, StrokeCap.Round)
        drawLine(tint, Offset(r, r - d), Offset(r, r), sw, StrokeCap.Round)
    }
}

@Composable
fun IconSwapArrows(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        // Top arrow right
        drawLine(tint, Offset(s * 0.20f, s * 0.35f), Offset(s * 0.80f, s * 0.35f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.64f, s * 0.21f), Offset(s * 0.80f, s * 0.35f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.64f, s * 0.49f), Offset(s * 0.80f, s * 0.35f), sw, StrokeCap.Round)
        // Bottom arrow left
        drawLine(tint, Offset(s * 0.20f, s * 0.67f), Offset(s * 0.80f, s * 0.67f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.36f, s * 0.53f), Offset(s * 0.20f, s * 0.67f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.36f, s * 0.81f), Offset(s * 0.20f, s * 0.67f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconSwapVertical(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.09f
        // Up arrow left
        drawLine(tint, Offset(s * 0.34f, s * 0.22f), Offset(s * 0.34f, s * 0.78f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.21f, s * 0.36f), Offset(s * 0.34f, s * 0.22f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.47f, s * 0.36f), Offset(s * 0.34f, s * 0.22f), sw, StrokeCap.Round)
        // Down arrow right
        drawLine(tint, Offset(s * 0.66f, s * 0.22f), Offset(s * 0.66f, s * 0.78f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.53f, s * 0.64f), Offset(s * 0.66f, s * 0.78f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.79f, s * 0.64f), Offset(s * 0.66f, s * 0.78f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconSettingsGear(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val c = Offset(s / 2f, s / 2f)
        val sw = s * 0.08f
        drawCircle(tint, s * 0.16f, c, style = Stroke(sw))
        drawCircle(tint, s * 0.31f, c, style = Stroke(sw))
        for (i in 0 until 8) {
            val angle = Math.toRadians(i * 45.0)
            val x1 = c.x + (s * 0.31f) * kotlin.math.cos(angle).toFloat()
            val y1 = c.y + (s * 0.31f) * kotlin.math.sin(angle).toFloat()
            val x2 = c.x + (s * 0.42f) * kotlin.math.cos(angle).toFloat()
            val y2 = c.y + (s * 0.42f) * kotlin.math.sin(angle).toFloat()
            drawLine(tint, Offset(x1, y1), Offset(x2, y2), sw, StrokeCap.Round)
        }
    }
}

@Composable
fun IconSearchLens(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.09f
        drawCircle(tint, s * 0.26f, Offset(s * 0.42f, s * 0.42f), style = Stroke(sw))
        drawLine(tint, Offset(s * 0.61f, s * 0.61f), Offset(s * 0.82f, s * 0.82f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconFilterLines(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.09f
        drawLine(tint, Offset(s * 0.18f, s * 0.28f), Offset(s * 0.82f, s * 0.28f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.28f, s * 0.50f), Offset(s * 0.72f, s * 0.50f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.38f, s * 0.72f), Offset(s * 0.62f, s * 0.72f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconCopySheets(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.08f
        val r = CornerRadius(s * 0.08f, s * 0.08f)
        drawRoundRect(tint, Offset(s * 0.34f, s * 0.30f), Size(s * 0.46f, s * 0.52f), r, style = Stroke(sw))
        val path = Path().apply {
            moveTo(s * 0.24f, s * 0.66f)
            lineTo(s * 0.20f, s * 0.66f)
            lineTo(s * 0.20f, s * 0.18f)
            lineTo(s * 0.64f, s * 0.18f)
            lineTo(s * 0.64f, s * 0.24f)
        }
        drawPath(path, tint, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconEditPencil(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        val path = Path().apply {
            moveTo(s * 0.20f, s * 0.80f)
            lineTo(s * 0.24f, s * 0.60f)
            lineTo(s * 0.68f, s * 0.16f)
            lineTo(s * 0.84f, s * 0.32f)
            lineTo(s * 0.40f, s * 0.76f)
            close()
        }
        drawPath(path, tint, style = Stroke(sw, join = StrokeJoin.Round))
    }
}

@Composable
fun IconTrashBin(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.08f
        drawLine(tint, Offset(s * 0.22f, s * 0.28f), Offset(s * 0.78f, s * 0.28f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.38f, s * 0.18f), Offset(s * 0.62f, s * 0.18f), sw, StrokeCap.Round)
        val path = Path().apply {
            moveTo(s * 0.28f, s * 0.32f)
            lineTo(s * 0.33f, s * 0.82f)
            lineTo(s * 0.67f, s * 0.82f)
            lineTo(s * 0.72f, s * 0.32f)
        }
        drawPath(path, tint, style = Stroke(sw, join = StrokeJoin.Round))
    }
}

@Composable
fun IconShareArrow(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        drawLine(tint, Offset(s * 0.50f, s * 0.60f), Offset(s * 0.50f, s * 0.18f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.35f, s * 0.33f), Offset(s * 0.50f, s * 0.18f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.65f, s * 0.33f), Offset(s * 0.50f, s * 0.18f), sw, StrokeCap.Round)
        val box = Path().apply {
            moveTo(s * 0.32f, s * 0.44f)
            lineTo(s * 0.22f, s * 0.44f)
            lineTo(s * 0.22f, s * 0.82f)
            lineTo(s * 0.78f, s * 0.82f)
            lineTo(s * 0.78f, s * 0.44f)
            lineTo(s * 0.68f, s * 0.44f)
        }
        drawPath(box, tint, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun IconUndoArrow(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.09f
        val path = Path().apply {
            moveTo(s * 0.24f, s * 0.42f)
            lineTo(s * 0.64f, s * 0.42f)
            cubicTo(s * 0.82f, s * 0.42f, s * 0.82f, s * 0.74f, s * 0.64f, s * 0.74f)
            lineTo(s * 0.36f, s * 0.74f)
        }
        drawPath(path, tint, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(s * 0.38f, s * 0.28f), Offset(s * 0.22f, s * 0.42f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.38f, s * 0.56f), Offset(s * 0.22f, s * 0.42f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconRedoArrow(tint: Color, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.09f
        val path = Path().apply {
            moveTo(s * 0.76f, s * 0.42f)
            lineTo(s * 0.36f, s * 0.42f)
            cubicTo(s * 0.18f, s * 0.42f, s * 0.18f, s * 0.74f, s * 0.36f, s * 0.74f)
            lineTo(s * 0.64f, s * 0.74f)
        }
        drawPath(path, tint, style = Stroke(sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawLine(tint, Offset(s * 0.62f, s * 0.28f), Offset(s * 0.78f, s * 0.42f), sw, StrokeCap.Round)
        drawLine(tint, Offset(s * 0.62f, s * 0.56f), Offset(s * 0.78f, s * 0.42f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconGraphWave(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        val path = Path().apply {
            moveTo(s * 0.14f, s * 0.54f)
            cubicTo(s * 0.30f, s * 0.16f, s * 0.46f, s * 0.16f, s * 0.50f, s * 0.50f)
            cubicTo(s * 0.54f, s * 0.84f, s * 0.70f, s * 0.84f, s * 0.86f, s * 0.46f)
        }
        drawPath(path, tint, style = Stroke(sw, cap = StrokeCap.Round))
    }
}

@Composable
fun IconPin(tint: Color, filled: Boolean = false, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.085f
        val head = Path().apply {
            moveTo(s * 0.34f, s * 0.20f)
            lineTo(s * 0.66f, s * 0.20f)
            lineTo(s * 0.60f, s * 0.46f)
            lineTo(s * 0.72f, s * 0.56f)
            lineTo(s * 0.28f, s * 0.56f)
            lineTo(s * 0.40f, s * 0.46f)
            close()
        }
        if (filled) {
            drawPath(head, tint)
        } else {
            drawPath(head, tint, style = Stroke(sw, join = StrokeJoin.Round))
        }
        drawLine(tint, Offset(s * 0.50f, s * 0.56f), Offset(s * 0.50f, s * 0.84f), sw, StrokeCap.Round)
    }
}

@Composable
fun IconStar(tint: Color, filled: Boolean = false, size: Dp = 18.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val c = Offset(s / 2f, s / 2f)
        val outer = s * 0.38f
        val inner = s * 0.17f
        val path = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val angle = Math.toRadians((i * 36 - 90).toDouble())
            val x = c.x + r * kotlin.math.cos(angle).toFloat()
            val y = c.y + r * kotlin.math.sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        if (filled) {
            drawPath(path, tint)
        } else {
            drawPath(path, tint, style = Stroke(s * 0.08f, join = StrokeJoin.Round))
        }
    }
}

@Composable
fun IconCalculatorBadge(tint: Color, size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.08f
        drawRoundRect(
            color = tint,
            topLeft = Offset(s * 0.20f, s * 0.14f),
            size = Size(s * 0.60f, s * 0.72f),
            cornerRadius = CornerRadius(s * 0.10f, s * 0.10f),
            style = Stroke(sw)
        )
        drawLine(tint, Offset(s * 0.32f, s * 0.32f), Offset(s * 0.68f, s * 0.32f), sw, StrokeCap.Round)
        val dots = listOf(
            Offset(s * 0.34f, s * 0.50f), Offset(s * 0.50f, s * 0.50f), Offset(s * 0.66f, s * 0.50f),
            Offset(s * 0.34f, s * 0.68f), Offset(s * 0.50f, s * 0.68f), Offset(s * 0.66f, s * 0.68f)
        )
        dots.forEach { drawCircle(tint, s * 0.045f, it) }
    }
}
