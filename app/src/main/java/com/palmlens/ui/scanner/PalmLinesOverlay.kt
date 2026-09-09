package com.palmlens.ui.scanner

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.palmlens.domain.model.LineId
import com.palmlens.domain.model.NormPoint
import com.palmlens.domain.model.PalmLine
import com.palmlens.ui.theme.LineFate
import com.palmlens.ui.theme.LineHead
import com.palmlens.ui.theme.LineHeart
import com.palmlens.ui.theme.LineLife

/**
 * Draws the detected palm lines over the captured "photo", each tracing itself
 * on first appearance (spec §2.1). Glow stroke absorbs the placement drift a
 * general vision model produces.
 */
@Composable
fun PalmLinesOverlay(lines: List<PalmLine>, modifier: Modifier = Modifier) {
    val progress = remember(lines) { Animatable(0f) }
    LaunchedEffect(lines) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(1800, easing = FastOutSlowInEasing))
    }
    Canvas(modifier) {
        lines.filter { it.visible && it.path.size >= 2 }.forEach { line ->
            val color = lineColor(line.id)
            val measure = PathMeasure().apply { setPath(smoothPath(line.path, size), forceClosed = false) }
            val segment = Path()
            measure.getSegment(0f, measure.length * progress.value, segment, startWithMoveTo = true)
            drawPath(
                segment,
                color = color.copy(alpha = 0.22f),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            drawPath(
                segment,
                color = color,
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

private fun lineColor(id: LineId) = when (id) {
    LineId.LIFE -> LineLife
    LineId.HEAD -> LineHead
    LineId.HEART -> LineHeart
    LineId.FATE -> LineFate
}

/** Quadratic-bezier smoothing through midpoints so 5–8 points render as a curve. */
private fun smoothPath(points: List<NormPoint>, size: Size): Path {
    val path = Path()
    if (points.isEmpty()) return path
    fun toOffset(p: NormPoint) = Offset(p.x * size.width, p.y * size.height)

    val first = toOffset(points.first())
    path.moveTo(first.x, first.y)
    for (i in 1 until points.size) {
        val prev = toOffset(points[i - 1])
        val cur = toOffset(points[i])
        val mid = Offset((prev.x + cur.x) / 2f, (prev.y + cur.y) / 2f)
        path.quadraticTo(prev.x, prev.y, mid.x, mid.y)
    }
    val last = toOffset(points.last())
    path.lineTo(last.x, last.y)
    return path
}
