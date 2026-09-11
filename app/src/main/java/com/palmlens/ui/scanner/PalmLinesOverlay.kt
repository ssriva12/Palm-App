package com.palmlens.ui.scanner

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.palmlens.domain.model.LineId
import com.palmlens.domain.model.NormPoint
import com.palmlens.domain.model.PalmLine

// This draws over a photo, so it uses fixed high-contrast colours, not the (muted) theme.
private val LineHalo = Color(0x99101010)
private val LineCore = Color(0xFFFFE7A0)

/**
 * Traces each palm line over the captured photo. A line draws itself in while its id is in
 * [shown] and retracts when removed. When the model gives no usable path for a line, an
 * anatomical default is used so the trace still appears.
 */
@Composable
fun PalmLinesOverlay(
    lines: List<PalmLine>,
    shown: Set<LineId>,
    modifier: Modifier = Modifier,
) {
    val tracks = lines.filter { it.visible }.map { line ->
        val anim = animateFloatAsState(
            targetValue = if (line.id in shown) 1f else 0f,
            animationSpec = tween(1100, easing = FastOutSlowInEasing),
            label = "line-${line.id.name}",
        )
        Track(line.id, line.path.takeIf { it.size >= 2 } ?: defaultPath(line.id), anim)
    }

    Canvas(modifier) {
        tracks.forEach { track ->
            val fraction = track.anim.value
            if (fraction <= 0.01f) return@forEach
            val core = coreWidth(track.id)
            val measure = PathMeasure().apply {
                setPath(smoothPath(track.points, size), forceClosed = false)
            }
            val drawn = Path()
            measure.getSegment(0f, measure.length * fraction, drawn, startWithMoveTo = true)

            drawPath(drawn, LineHalo, style = roundStroke(core + 12.dp.toPx()))
            drawPath(drawn, LineCore, style = roundStroke(core))
        }
    }
}

private class Track(val id: LineId, val points: List<NormPoint>, val anim: State<Float>)

private fun roundStroke(width: Float) =
    Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)

private fun DrawScope.coreWidth(id: LineId): Float = when (id) {
    LineId.LIFE -> 7.dp.toPx()
    LineId.HEAD -> 6.dp.toPx()
    LineId.HEART -> 6.dp.toPx()
    LineId.FATE -> 5.dp.toPx()
}

/** Rough positions for a palm filling the frame — the fallback when the model gives no path. */
private fun defaultPath(id: LineId): List<NormPoint> = when (id) {
    LineId.LIFE -> listOf(
        NormPoint(0.36f, 0.14f), NormPoint(0.31f, 0.30f), NormPoint(0.28f, 0.47f),
        NormPoint(0.30f, 0.63f), NormPoint(0.37f, 0.78f), NormPoint(0.47f, 0.88f),
    )
    LineId.HEAD -> listOf(
        NormPoint(0.31f, 0.42f), NormPoint(0.45f, 0.45f), NormPoint(0.58f, 0.49f),
        NormPoint(0.70f, 0.54f), NormPoint(0.81f, 0.60f),
    )
    LineId.HEART -> listOf(
        NormPoint(0.27f, 0.31f), NormPoint(0.42f, 0.25f), NormPoint(0.56f, 0.23f),
        NormPoint(0.69f, 0.24f), NormPoint(0.80f, 0.29f),
    )
    LineId.FATE -> listOf(
        NormPoint(0.53f, 0.90f), NormPoint(0.53f, 0.71f), NormPoint(0.54f, 0.52f),
        NormPoint(0.55f, 0.35f), NormPoint(0.56f, 0.20f),
    )
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
