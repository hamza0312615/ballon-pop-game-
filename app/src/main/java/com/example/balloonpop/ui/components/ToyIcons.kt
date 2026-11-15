package com.example.balloonpop.ui.components

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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pass 12: drawn toy icon set. Real games ship drawn glyphs, not emoji —
 * emoji render differently on every phone and read as "vibe-coded".
 * Every glyph is drawn bold and simple on a 24-unit grid so it stays
 * readable at 16–32dp, in the Toybox palette (single tint per icon).
 */
enum class ToyGlyph {
    GEAR, SPEAKER, SPEAKER_OFF, BELL, BELL_OFF, MUSIC, BOLT, BOOK, SHIELD,
    LOCK, WARNING, VIBRATE, TARGET, BALLOON, STAR, HEART, BOMB, SNOWFLAKE,
    RAINBOW, PERSON, GLOBE, CROWN, GIFT, FLOWER, SUN, GAUGE, BATTERY,
    TROPHY, HAMMER, PALETTE, FRAME, SWORD, FLAME, ROCKET, CANDY, PAW,
    LEAF, WAVE, GEM, SHELL, SPARKLE, COIN, MAP, WHEEL, HOME, REFRESH,
    CLOCK, FLAG, DASH, CHECK_CIRCLE, SPIRAL, MOON
}

@Composable
fun ToyIcon(
    glyph: ToyGlyph,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = Color.White
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val u = w / 24f // one grid unit
        fun p(x: Float, y: Float) = Offset(x * u, y * u)
        when (glyph) {
            ToyGlyph.GEAR -> {
                // 8 chunky teeth + hub
                for (i in 0 until 8) {
                    rotate(degrees = i * 45f, pivot = p(12f, 12f)) {
                        drawRoundRect(
                            color = tint,
                            topLeft = p(10.4f, 2.6f),
                            size = Size(3.2f * u, 5.2f * u),
                            cornerRadius = CornerRadius(1.2f * u, 1.2f * u)
                        )
                    }
                }
                drawCircle(color = tint, radius = 7.6f * u, center = p(12f, 12f))
                drawCircle(color = Color.Black.copy(alpha = 0.28f), radius = 3.1f * u, center = p(12f, 12f))
            }
            ToyGlyph.SPEAKER, ToyGlyph.SPEAKER_OFF -> {
                drawRoundRect(
                    color = tint,
                    topLeft = p(3f, 9.5f),
                    size = Size(4f * u, 5f * u),
                    cornerRadius = CornerRadius(1f * u, 1f * u)
                )
                val horn = Path().apply {
                    moveTo(7f * u, 9.5f * u)
                    lineTo(13.5f * u, 5f * u)
                    lineTo(13.5f * u, 19f * u)
                    lineTo(7f * u, 14.5f * u)
                    close()
                }
                drawPath(horn, tint)
                if (glyph == ToyGlyph.SPEAKER) {
                    drawArc(
                        color = tint, startAngle = -55f, sweepAngle = 110f,
                        useCenter = false, topLeft = p(13.5f, 7f),
                        size = Size(6.5f * u, 10f * u),
                        style = Stroke(width = 2f * u, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = tint, startAngle = -55f, sweepAngle = 110f,
                        useCenter = false, topLeft = p(16f, 4.8f),
                        size = Size(6.5f * u, 14.4f * u),
                        style = Stroke(width = 2f * u, cap = StrokeCap.Round)
                    )
                } else {
                    drawLine(tint, p(16f, 9f), p(21.5f, 15f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
                    drawLine(tint, p(21.5f, 9f), p(16f, 15f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
                }
            }
            ToyGlyph.BELL, ToyGlyph.BELL_OFF -> {
                val dome = Path().apply {
                    moveTo(5.5f * u, 17.5f * u)
                    cubicTo(5.5f * u, 10f * u, 8f * u, 5.5f * u, 12f * u, 5.5f * u)
                    cubicTo(16f * u, 5.5f * u, 18.5f * u, 10f * u, 18.5f * u, 17.5f * u)
                    close()
                }
                drawPath(dome, tint)
                drawCircle(color = tint, radius = 1.8f * u, center = p(12f, 4.2f))
                drawRoundRect(
                    color = tint,
                    topLeft = p(4f, 17.2f),
                    size = Size(16f * u, 2.4f * u),
                    cornerRadius = CornerRadius(1.2f * u, 1.2f * u)
                )
                drawCircle(color = tint, radius = 2f * u, center = p(12f, 21.2f))
                if (glyph == ToyGlyph.BELL_OFF) {
                    drawLine(
                        Color(0xFFFF5252), p(4.5f, 3.5f), p(19.5f, 20.5f),
                        strokeWidth = 2.6f * u, cap = StrokeCap.Round
                    )
                }
            }
            ToyGlyph.MUSIC -> {
                drawCircle(color = tint, radius = 3f * u, center = p(7.5f, 17.5f))
                drawCircle(color = tint, radius = 3f * u, center = p(18.5f, 15.5f))
                drawRect(color = tint, topLeft = p(9.9f, 5.4f), size = Size(1.9f * u, 12.1f * u))
                drawRect(color = tint, topLeft = p(20.9f, 3.4f), size = Size(1.9f * u, 12.1f * u))
                val beam = Path().apply {
                    moveTo(9.9f * u, 5.4f * u)
                    lineTo(22.8f * u, 3.4f * u)
                    lineTo(22.8f * u, 7.6f * u)
                    lineTo(9.9f * u, 9.6f * u)
                    close()
                }
                drawPath(beam, tint)
            }
            ToyGlyph.BOLT -> {
                val bolt = Path().apply {
                    moveTo(13.2f * u, 2f * u)
                    lineTo(5f * u, 13.6f * u)
                    lineTo(10.6f * u, 13.6f * u)
                    lineTo(9.2f * u, 22f * u)
                    lineTo(19f * u, 9.8f * u)
                    lineTo(13.2f * u, 9.8f * u)
                    close()
                }
                drawPath(bolt, tint)
            }
            ToyGlyph.BOOK -> {
                val left = Path().apply {
                    moveTo(12f * u, 6.5f * u)
                    cubicTo(9.5f * u, 4.6f * u, 6f * u, 4.2f * u, 3.5f * u, 5f * u)
                    lineTo(3.5f * u, 18.5f * u)
                    cubicTo(6f * u, 17.7f * u, 9.5f * u, 18.1f * u, 12f * u, 20f * u)
                    close()
                }
                val right = Path().apply {
                    moveTo(12f * u, 6.5f * u)
                    cubicTo(14.5f * u, 4.6f * u, 18f * u, 4.2f * u, 20.5f * u, 5f * u)
                    lineTo(20.5f * u, 18.5f * u)
                    cubicTo(18f * u, 17.7f * u, 14.5f * u, 18.1f * u, 12f * u, 20f * u)
                    close()
                }
                drawPath(left, tint)
                drawPath(right, tint.copy(alpha = 0.72f))
                drawRect(color = tint, topLeft = p(11.4f, 6.2f), size = Size(1.2f * u, 13.6f * u))
            }
            ToyGlyph.SHIELD -> {
                val shield = Path().apply {
                    moveTo(12f * u, 2.6f * u)
                    lineTo(19.5f * u, 5.2f * u)
                    cubicTo(19.5f * u, 13f * u, 16.5f * u, 18.4f * u, 12f * u, 21.4f * u)
                    cubicTo(7.5f * u, 18.4f * u, 4.5f * u, 13f * u, 4.5f * u, 5.2f * u)
                    close()
                }
                drawPath(shield, tint)
                val check = Path().apply {
                    moveTo(8.6f * u, 11.8f * u)
                    lineTo(11.2f * u, 14.4f * u)
                    lineTo(15.6f * u, 9.4f * u)
                }
                drawPath(check, Color.Black.copy(alpha = 0.3f), style = Stroke(width = 2.2f * u, cap = StrokeCap.Round))
            }
            ToyGlyph.LOCK -> {
                drawArc(
                    color = tint, startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = p(6.8f, 4.6f), size = Size(10.4f * u, 10.4f * u),
                    style = Stroke(width = 2.8f * u)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = p(4.8f, 11.4f),
                    size = Size(14.4f * u, 9.6f * u),
                    cornerRadius = CornerRadius(2.4f * u, 2.4f * u)
                )
                drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = 1.9f * u, center = p(12f, 15.4f))
                drawRect(color = Color.Black.copy(alpha = 0.3f), topLeft = p(11.1f, 15.4f), size = Size(1.8f * u, 3f * u))
            }
            ToyGlyph.WARNING -> {
                val tri = Path().apply {
                    moveTo(12f * u, 3.4f * u)
                    lineTo(21.6f * u, 20f * u)
                    lineTo(2.4f * u, 20f * u)
                    close()
                }
                drawPath(tri, tint)
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.32f),
                    topLeft = p(11f, 9.6f),
                    size = Size(2f * u, 5.6f * u),
                    cornerRadius = CornerRadius(1f * u, 1f * u)
                )
                drawCircle(color = Color.Black.copy(alpha = 0.32f), radius = 1.15f * u, center = p(12f, 17.6f))
            }
            ToyGlyph.VIBRATE -> {
                drawRoundRect(
                    color = tint,
                    topLeft = p(9f, 3.4f),
                    size = Size(6.4f * u, 17.2f * u),
                    cornerRadius = CornerRadius(1.8f * u, 1.8f * u)
                )
                drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = 0.95f * u, center = p(12.2f, 17.6f))
                for (side in listOf(-1f, 1f)) {
                    for (r in listOf(3.4f, 6f)) {
                        drawArc(
                            color = tint,
                            startAngle = if (side < 0) 110f else -70f,
                            sweepAngle = 140f, useCenter = false,
                            topLeft = Offset(12.2f * u - r * u, 12f * u - r * u),
                            size = Size(2 * r * u, 2 * r * u),
                            style = Stroke(width = 1.9f * u, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            ToyGlyph.TARGET -> {
                drawCircle(color = tint, radius = 9.6f * u, center = p(12f, 12f))
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = 6.2f * u, center = p(12f, 12f))
                drawCircle(color = tint, radius = 3.1f * u, center = p(12f, 12f))
            }
            ToyGlyph.BALLOON -> {
                drawOval(color = tint, topLeft = p(5.4f, 2.2f), size = Size(13.2f * u, 15.6f * u))
                drawOval(
                    color = Color.White.copy(alpha = 0.4f),
                    topLeft = p(8f, 4.8f), size = Size(4.2f * u, 6.4f * u)
                )
                val knot = Path().apply {
                    moveTo(10.4f * u, 17.6f * u)
                    lineTo(13.6f * u, 17.6f * u)
                    lineTo(12f * u, 19.8f * u)
                    close()
                }
                drawPath(knot, tint)
                val string = Path().apply {
                    moveTo(12f * u, 19.8f * u)
                    cubicTo(10.6f * u, 21f * u, 13.4f * u, 21.6f * u, 12f * u, 23f * u)
                }
                drawPath(string, tint, style = Stroke(width = 1.5f * u, cap = StrokeCap.Round))
            }
            ToyGlyph.STAR -> {
                val star = Path()
                for (i in 0 until 10) {
                    val ang = -PI / 2 + i * PI / 5
                    val r = if (i % 2 == 0) 10.2f else 4.4f
                    val x = 12f + r * cos(ang).toFloat()
                    val y = 12f + r * sin(ang).toFloat()
                    if (i == 0) star.moveTo(x * u, y * u) else star.lineTo(x * u, y * u)
                }
                star.close()
                drawPath(star, tint)
            }
            ToyGlyph.HEART -> {
                val heart = Path().apply {
                    moveTo(12f * u, 20.6f * u)
                    cubicTo(4f * u, 14.6f * u, 3.2f * u, 9.4f * u, 6.6f * u, 6.4f * u)
                    cubicTo(9f * u, 4.3f * u, 11.6f * u, 5.4f * u, 12f * u, 7.6f * u)
                    cubicTo(12.4f * u, 5.4f * u, 15f * u, 4.3f * u, 17.4f * u, 6.4f * u)
                    cubicTo(20.8f * u, 9.4f * u, 20f * u, 14.6f * u, 12f * u, 20.6f * u)
                    close()
                }
                drawPath(heart, tint)
            }
            ToyGlyph.BOMB -> {
                drawCircle(color = tint, radius = 8.2f * u, center = p(11f, 14.6f))
                drawOval(
                    color = Color.White.copy(alpha = 0.38f),
                    topLeft = p(5.6f, 9.4f), size = Size(4.6f * u, 6.2f * u)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = p(13.6f, 4.6f),
                    size = Size(4.6f * u, 3.4f * u),
                    cornerRadius = CornerRadius(1f * u, 1f * u)
                )
                val fuse = Path().apply {
                    moveTo(16f * u, 4.8f * u)
                    cubicTo(17.5f * u, 2.6f * u, 19.6f * u, 2.8f * u, 20.6f * u, 4.2f * u)
                }
                drawPath(fuse, tint, style = Stroke(width = 1.7f * u, cap = StrokeCap.Round))
                drawCircle(color = Color(0xFFFFC531), radius = 1.7f * u, center = p(20.9f, 4.6f))
            }
            ToyGlyph.SNOWFLAKE -> {
                for (i in 0 until 6) {
                    rotate(degrees = i * 60f, pivot = p(12f, 12f)) {
                        drawLine(tint, p(12f, 3.4f), p(12f, 20.6f), strokeWidth = 2f * u, cap = StrokeCap.Round)
                        drawLine(tint, p(12f, 6.4f), p(9.6f, 4.2f), strokeWidth = 1.7f * u, cap = StrokeCap.Round)
                        drawLine(tint, p(12f, 6.4f), p(14.4f, 4.2f), strokeWidth = 1.7f * u, cap = StrokeCap.Round)
                    }
                }
            }
            ToyGlyph.RAINBOW -> {
                val bands = listOf(
                    Color(0xFFFF5252) to 9.8f,
                    Color(0xFFFFC531) to 7.4f,
                    Color(0xFF63C70A) to 5f
                )
                for ((c, r) in bands) {
                    drawArc(
                        color = c, startAngle = 180f, sweepAngle = 180f, useCenter = false,
                        topLeft = Offset(12f * u - r * u, 13.5f * u - r * u),
                        size = Size(2 * r * u, 2 * r * u),
                        style = Stroke(width = 2.3f * u, cap = StrokeCap.Round)
                    )
                }
                drawCircle(color = tint, radius = 2.9f * u, center = p(4.6f, 15.6f))
                drawCircle(color = tint, radius = 2.9f * u, center = p(19.4f, 15.6f))
                drawRoundRect(
                    color = tint,
                    topLeft = p(3f, 14.6f),
                    size = Size(18f * u, 4.6f * u),
                    cornerRadius = CornerRadius(2.3f * u, 2.3f * u)
                )
            }
            ToyGlyph.PERSON -> {
                drawCircle(color = tint, radius = 4.4f * u, center = p(12f, 8f))
                val body = Path().apply {
                    moveTo(3.8f * u, 21.4f * u)
                    cubicTo(3.8f * u, 15.6f * u, 7.4f * u, 13.4f * u, 12f * u, 13.4f * u)
                    cubicTo(16.6f * u, 13.4f * u, 20.2f * u, 15.6f * u, 20.2f * u, 21.4f * u)
                    close()
                }
                drawPath(body, tint)
            }
            ToyGlyph.GLOBE -> {
                drawCircle(color = tint, radius = 9.6f * u, center = p(12f, 12f), style = Stroke(width = 2.2f * u))
                drawOval(
                    color = tint, topLeft = p(7.6f, 2.4f), size = Size(8.8f * u, 19.2f * u),
                    style = Stroke(width = 1.8f * u)
                )
                drawLine(tint, p(2.6f, 12f), p(21.4f, 12f), strokeWidth = 1.8f * u)
            }
            ToyGlyph.CROWN -> {
                val crown = Path().apply {
                    moveTo(3.4f * u, 18.6f * u)
                    lineTo(4.6f * u, 7.6f * u)
                    lineTo(9.2f * u, 12.4f * u)
                    lineTo(12f * u, 5.6f * u)
                    lineTo(14.8f * u, 12.4f * u)
                    lineTo(19.4f * u, 7.6f * u)
                    lineTo(20.6f * u, 18.6f * u)
                    close()
                }
                drawPath(crown, tint)
                drawCircle(color = tint, radius = 1.6f * u, center = p(4.6f, 5.8f))
                drawCircle(color = tint, radius = 1.6f * u, center = p(12f, 3.9f))
                drawCircle(color = tint, radius = 1.6f * u, center = p(19.4f, 5.8f))
            }
            ToyGlyph.GIFT -> {
                drawRoundRect(
                    color = tint,
                    topLeft = p(4.4f, 10.6f),
                    size = Size(15.2f * u, 9.8f * u),
                    cornerRadius = CornerRadius(1.6f * u, 1.6f * u)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = p(3.4f, 6.8f),
                    size = Size(17.2f * u, 3.8f * u),
                    cornerRadius = CornerRadius(1.6f * u, 1.6f * u)
                )
                drawRect(color = Color.Black.copy(alpha = 0.22f), topLeft = p(10.9f, 6.8f), size = Size(2.2f * u, 13.6f * u))
                drawOval(
                    color = tint, topLeft = p(6.2f, 1.8f), size = Size(4.6f * u, 4.6f * u),
                    style = Stroke(width = 2f * u)
                )
                drawOval(
                    color = tint, topLeft = p(13.2f, 1.8f), size = Size(4.6f * u, 4.6f * u),
                    style = Stroke(width = 2f * u)
                )
            }
            ToyGlyph.FLOWER -> {
                for (i in 0 until 5) {
                    val ang = -PI / 2 + i * 2 * PI / 5
                    val cx = 12f + 4.6f * cos(ang).toFloat()
                    val cy = 11f + 4.6f * sin(ang).toFloat()
                    drawCircle(color = tint, radius = 3.6f * u, center = p(cx, cy))
                }
                drawCircle(color = Color(0xFFFFC531), radius = 2.9f * u, center = p(12f, 11f))
                drawLine(tint, p(12f, 15.4f), p(12f, 21.6f), strokeWidth = 2f * u, cap = StrokeCap.Round)
            }
            ToyGlyph.SUN -> {
                for (i in 0 until 8) {
                    rotate(degrees = i * 45f, pivot = p(12f, 12f)) {
                        drawLine(tint, p(12f, 2.4f), p(12f, 5.6f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
                    }
                }
                drawCircle(color = tint, radius = 5.6f * u, center = p(12f, 12f))
            }
            ToyGlyph.GAUGE -> {
                drawArc(
                    color = tint, startAngle = 180f, sweepAngle = 180f, useCenter = false,
                    topLeft = p(2.8f, 5.6f), size = Size(18.4f * u, 18.4f * u),
                    style = Stroke(width = 3f * u, cap = StrokeCap.Round)
                )
                drawLine(tint, p(12f, 14.8f), p(16.8f, 9f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
                drawCircle(color = tint, radius = 2.2f * u, center = p(12f, 14.8f))
                drawRoundRect(
                    color = tint,
                    topLeft = p(3.4f, 18.6f),
                    size = Size(17.2f * u, 2.6f * u),
                    cornerRadius = CornerRadius(1.3f * u, 1.3f * u)
                )
            }
            ToyGlyph.BATTERY -> {
                drawRoundRect(
                    color = tint,
                    topLeft = p(2.6f, 8f),
                    size = Size(17f * u, 8.6f * u),
                    cornerRadius = CornerRadius(2f * u, 2f * u),
                    style = Stroke(width = 2.2f * u)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = p(4.8f, 10.2f),
                    size = Size(8.6f * u, 4.2f * u),
                    cornerRadius = CornerRadius(1.2f * u, 1.2f * u)
                )
                drawRoundRect(
                    color = tint,
                    topLeft = p(20.2f, 10.6f),
                    size = Size(1.8f * u, 3.4f * u),
                    cornerRadius = CornerRadius(0.9f * u, 0.9f * u)
                )
            }
            ToyGlyph.TROPHY -> {
                val cup = Path().apply {
                    moveTo(6f * u, 3.4f * u)
                    lineTo(18f * u, 3.4f * u)
                    cubicTo(18f * u, 9.4f * u, 15.4f * u, 12.4f * u, 12f * u, 12.4f * u)
                    cubicTo(8.6f * u, 12.4f * u, 6f * u, 9.4f * u, 6f * u, 3.4f * u)
                    close()
                }
                drawPath(cup, tint)
                drawArc(color = tint, startAngle = 300f, sweepAngle = 130f, useCenter = false,
                    topLeft = p(1.8f, 3.8f), size = Size(6.4f * u, 6.4f * u), style = Stroke(width = 2f * u))
                drawArc(color = tint, startAngle = 110f, sweepAngle = 130f, useCenter = false,
                    topLeft = p(15.8f, 3.8f), size = Size(6.4f * u, 6.4f * u), style = Stroke(width = 2f * u))
                drawRect(color = tint, topLeft = p(10.9f, 12.2f), size = Size(2.2f * u, 3.6f * u))
                drawRoundRect(color = tint, topLeft = p(7.4f, 15.8f), size = Size(9.2f * u, 2.6f * u),
                    cornerRadius = CornerRadius(1.3f * u, 1.3f * u))
                drawRoundRect(color = tint, topLeft = p(5.6f, 19.6f), size = Size(12.8f * u, 2.2f * u),
                    cornerRadius = CornerRadius(1.1f * u, 1.1f * u))
            }
            ToyGlyph.HAMMER -> {
                val head = Path().apply {
                    moveTo(4.2f * u, 6.4f * u)
                    cubicTo(8f * u, 3.4f * u, 12.4f * u, 3f * u, 16.8f * u, 5.2f * u)
                    lineTo(14.8f * u, 10.4f * u)
                    cubicTo(11.6f * u, 8.6f * u, 8.4f * u, 8.8f * u, 5.6f * u, 10.8f * u)
                    close()
                }
                drawPath(head, tint)
                rotate(degrees = 45f, pivot = p(13.5f, 13.5f)) {
                    drawRoundRect(color = tint, topLeft = p(11.9f, 11f), size = Size(3.2f * u, 10.6f * u),
                        cornerRadius = CornerRadius(1.4f * u, 1.4f * u))
                }
            }
            ToyGlyph.PALETTE -> {
                val blob = Path().apply {
                    moveTo(12f * u, 3f * u)
                    cubicTo(17.8f * u, 3f * u, 21f * u, 6.8f * u, 21f * u, 11.4f * u)
                    cubicTo(21f * u, 15.4f * u, 18.4f * u, 17.4f * u, 15.4f * u, 17.4f * u)
                    lineTo(13.8f * u, 17.4f * u)
                    cubicTo(12.4f * u, 17.4f * u, 11.6f * u, 16.5f * u, 11.6f * u, 15.2f * u)
                    cubicTo(11.6f * u, 13.6f * u, 10.4f * u, 13f * u, 8.6f * u, 13f * u)
                    cubicTo(5.4f * u, 13f * u, 3f * u, 10.2f * u, 3f * u, 11.4f * u)
                    cubicTo(3f * u, 6.8f * u, 6.2f * u, 3f * u, 12f * u, 3f * u)
                    close()
                }
                drawPath(blob, tint)
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = 1.6f * u, center = p(8.2f, 7.6f))
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = 1.6f * u, center = p(13.4f, 6.4f))
                drawCircle(color = Color.Black.copy(alpha = 0.25f), radius = 1.6f * u, center = p(16.8f, 10.2f))
                drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 1.9f * u, center = p(8.4f, 16.6f))
            }
            ToyGlyph.FRAME -> {
                drawRoundRect(color = tint, topLeft = p(3.4f, 3.4f), size = Size(17.2f * u, 17.2f * u),
                    cornerRadius = CornerRadius(2.4f * u, 2.4f * u), style = Stroke(width = 2.8f * u))
                val hills = Path().apply {
                    moveTo(5.4f * u, 18.6f * u)
                    lineTo(10.4f * u, 12.4f * u)
                    lineTo(13.8f * u, 16f * u)
                    lineTo(16.2f * u, 13.6f * u)
                    lineTo(18.6f * u, 18.6f * u)
                    close()
                }
                drawPath(hills, tint)
                drawCircle(color = tint, radius = 1.8f * u, center = p(9f, 8.6f))
            }
            ToyGlyph.SWORD -> {
                rotate(degrees = 45f, pivot = p(12f, 12f)) {
                    val blade = Path().apply {
                        moveTo(10.6f * u, 2.2f * u)
                        lineTo(13.4f * u, 2.2f * u)
                        lineTo(13.4f * u, 14.6f * u)
                        lineTo(10.6f * u, 14.6f * u)
                        close()
                    }
                    drawPath(blade, tint)
                    drawRoundRect(color = tint, topLeft = p(6.4f, 14.8f), size = Size(11.2f * u, 2.4f * u),
                        cornerRadius = CornerRadius(1.2f * u, 1.2f * u))
                    drawRoundRect(color = tint, topLeft = p(10.9f, 17.2f), size = Size(2.2f * u, 3.4f * u),
                        cornerRadius = CornerRadius(1.1f * u, 1.1f * u))
                    drawCircle(color = tint, radius = 1.7f * u, center = p(12f, 21.6f))
                }
            }
            ToyGlyph.FLAME -> {
                val flame = Path().apply {
                    moveTo(12f * u, 2.4f * u)
                    cubicTo(14.6f * u, 6.4f * u, 18.6f * u, 9.2f * u, 18.6f * u, 14f * u)
                    cubicTo(18.6f * u, 18.4f * u, 15.6f * u, 21.2f * u, 12f * u, 21.2f * u)
                    cubicTo(8.4f * u, 21.2f * u, 5.4f * u, 18.4f * u, 5.4f * u, 14f * u)
                    cubicTo(5.4f * u, 10.6f * u, 7.6f * u, 8.4f * u, 9.2f * u, 6f * u)
                    cubicTo(9.8f * u, 8.6f * u, 10.8f * u, 9.6f * u, 11.6f * u, 10.2f * u)
                    cubicTo(11f * u, 7.2f * u, 11.4f * u, 4.6f * u, 12f * u, 2.4f * u)
                    close()
                }
                drawPath(flame, tint)
                drawCircle(color = Color.White.copy(alpha = 0.45f), radius = 2.6f * u, center = p(12f, 16.8f))
            }
            ToyGlyph.ROCKET -> {
                val body = Path().apply {
                    moveTo(12f * u, 2.2f * u)
                    cubicTo(15.6f * u, 5.4f * u, 16.8f * u, 9.4f * u, 16.4f * u, 13.8f * u)
                    lineTo(7.6f * u, 13.8f * u)
                    cubicTo(7.2f * u, 9.4f * u, 8.4f * u, 5.4f * u, 12f * u, 2.2f * u)
                    close()
                }
                drawPath(body, tint)
                drawCircle(color = Color.Black.copy(alpha = 0.28f), radius = 2.1f * u, center = p(12f, 9f))
                val finL = Path().apply {
                    moveTo(7.8f * u, 11.4f * u)
                    lineTo(4.6f * u, 16.6f * u)
                    lineTo(7.8f * u, 15.6f * u)
                    close()
                }
                val finR = Path().apply {
                    moveTo(16.2f * u, 11.4f * u)
                    lineTo(19.4f * u, 16.6f * u)
                    lineTo(16.2f * u, 15.6f * u)
                    close()
                }
                drawPath(finL, tint)
                drawPath(finR, tint)
                val flame = Path().apply {
                    moveTo(9.8f * u, 15.4f * u)
                    lineTo(14.2f * u, 15.4f * u)
                    lineTo(12f * u, 21.4f * u)
                    close()
                }
                drawPath(flame, Color(0xFFFFC531))
            }
            ToyGlyph.CANDY -> {
                drawCircle(color = tint, radius = 5.6f * u, center = p(12f, 12f))
                drawCircle(color = Color.White.copy(alpha = 0.45f), radius = 2f * u, center = p(10.2f, 10.2f))
                val wrapL = Path().apply {
                    moveTo(6.8f * u, 9.4f * u)
                    lineTo(2.4f * u, 6.8f * u)
                    lineTo(3.4f * u, 12f * u)
                    lineTo(2.4f * u, 17.2f * u)
                    lineTo(6.8f * u, 14.6f * u)
                    close()
                }
                val wrapR = Path().apply {
                    moveTo(17.2f * u, 9.4f * u)
                    lineTo(21.6f * u, 6.8f * u)
                    lineTo(20.6f * u, 12f * u)
                    lineTo(21.6f * u, 17.2f * u)
                    lineTo(17.2f * u, 14.6f * u)
                    close()
                }
                drawPath(wrapL, tint)
                drawPath(wrapR, tint)
            }
            ToyGlyph.PAW -> {
                drawOval(color = tint, topLeft = p(7.2f, 12.4f), size = Size(9.6f * u, 7.6f * u))
                drawCircle(color = tint, radius = 2f * u, center = p(5.4f, 10.2f))
                drawCircle(color = tint, radius = 2.2f * u, center = p(9.4f, 7.4f))
                drawCircle(color = tint, radius = 2.2f * u, center = p(14.6f, 7.4f))
                drawCircle(color = tint, radius = 2f * u, center = p(18.6f, 10.2f))
            }
            ToyGlyph.LEAF -> {
                val leaf = Path().apply {
                    moveTo(5f * u, 19f * u)
                    cubicTo(5f * u, 9.5f * u, 10.5f * u, 4.5f * u, 20f * u, 4f * u)
                    cubicTo(19.5f * u, 14f * u, 14.5f * u, 19.5f * u, 5f * u, 19f * u)
                    close()
                }
                drawPath(leaf, tint)
                drawLine(Color.Black.copy(alpha = 0.25f), p(6.8f, 17.2f), p(16.8f, 7.2f),
                    strokeWidth = 1.8f * u, cap = StrokeCap.Round)
            }
            ToyGlyph.WAVE -> {
                for (dy in listOf(0f, 5.4f)) {
                    val wave = Path().apply {
                        moveTo(2.6f * u, (9.6f + dy) * u)
                        cubicTo(6.4f * u, (5.6f + dy) * u, 9.4f * u, (13.6f + dy) * u, 13.2f * u, (9.6f + dy) * u)
                        cubicTo(16.2f * u, (6.8f + dy) * u, 18.6f * u, (12.4f + dy) * u, 21.4f * u, (9.6f + dy) * u)
                    }
                    drawPath(wave, tint, style = Stroke(width = 2.8f * u, cap = StrokeCap.Round))
                }
            }
            ToyGlyph.GEM -> {
                val gem = Path().apply {
                    moveTo(6.4f * u, 4.6f * u)
                    lineTo(17.6f * u, 4.6f * u)
                    lineTo(20.8f * u, 9.4f * u)
                    lineTo(12f * u, 20f * u)
                    lineTo(3.2f * u, 9.4f * u)
                    close()
                }
                drawPath(gem, tint)
                drawLine(Color.White.copy(alpha = 0.55f), p(3.2f, 9.4f), p(20.8f, 9.4f),
                    strokeWidth = 1.6f * u)
                drawLine(Color.White.copy(alpha = 0.4f), p(8.6f, 4.6f), p(12f, 20f),
                    strokeWidth = 1.4f * u)
                drawLine(Color.White.copy(alpha = 0.4f), p(15.4f, 4.6f), p(12f, 20f),
                    strokeWidth = 1.4f * u)
            }
            ToyGlyph.SHELL -> {
                val shell = Path().apply {
                    moveTo(12f * u, 20.6f * u)
                    cubicTo(5f * u, 17.6f * u, 3.4f * u, 12.6f * u, 5.2f * u, 8.4f * u)
                    cubicTo(7f * u, 4.2f * u, 17f * u, 4.2f * u, 18.8f * u, 8.4f * u)
                    cubicTo(20.6f * u, 12.6f * u, 19f * u, 17.6f * u, 12f * u, 20.6f * u)
                    close()
                }
                drawPath(shell, tint)
                for (dx in listOf(-4.6f, 0f, 4.6f)) {
                    drawLine(Color.Black.copy(alpha = 0.2f), p(12f + dx * 0.4f, 17.8f), p(12f + dx, 7.6f),
                        strokeWidth = 1.6f * u, cap = StrokeCap.Round)
                }
                drawArc(color = Color.Black.copy(alpha = 0.2f), startAngle = 200f, sweepAngle = 140f,
                    useCenter = false, topLeft = p(6.4f, 5.4f), size = Size(11.2f * u, 11.2f * u),
                    style = Stroke(width = 1.6f * u))
            }
            ToyGlyph.SPARKLE -> {
                val big = Path().apply {
                    moveTo(10.4f * u, 2.6f * u)
                    cubicTo(11.2f * u, 7.4f * u, 12.6f * u, 8.8f * u, 17.4f * u, 9.6f * u)
                    cubicTo(12.6f * u, 10.4f * u, 11.2f * u, 11.8f * u, 10.4f * u, 16.6f * u)
                    cubicTo(9.6f * u, 11.8f * u, 8.2f * u, 10.4f * u, 3.4f * u, 9.6f * u)
                    cubicTo(8.2f * u, 8.8f * u, 9.6f * u, 7.4f * u, 10.4f * u, 2.6f * u)
                    close()
                }
                drawPath(big, tint)
                val small = Path().apply {
                    moveTo(18.2f * u, 14.8f * u)
                    cubicTo(18.7f * u, 17.4f * u, 19.4f * u, 18.1f * u, 22f * u, 18.6f * u)
                    cubicTo(19.4f * u, 19.1f * u, 18.7f * u, 19.8f * u, 18.2f * u, 22.4f * u)
                    cubicTo(17.7f * u, 19.8f * u, 17f * u, 19.1f * u, 14.4f * u, 18.6f * u)
                    cubicTo(17f * u, 18.1f * u, 17.7f * u, 17.4f * u, 18.2f * u, 14.8f * u)
                    close()
                }
                drawPath(small, tint.copy(alpha = 0.75f))
            }
            ToyGlyph.COIN -> {
                drawCircle(color = tint, radius = 9.4f * u, center = p(12f, 12f))
                drawCircle(color = Color.Black.copy(alpha = 0.18f), radius = 6.4f * u, center = p(12f, 12f),
                    style = Stroke(width = 1.8f * u))
                val starP = Path()
                for (i in 0 until 10) {
                    val ang = -PI / 2 + i * PI / 5
                    val r = if (i % 2 == 0) 4.6f else 2f
                    val x = 12f + r * cos(ang).toFloat()
                    val y = 12f + r * sin(ang).toFloat()
                    if (i == 0) starP.moveTo(x * u, y * u) else starP.lineTo(x * u, y * u)
                }
                starP.close()
                drawPath(starP, Color.White.copy(alpha = 0.65f))
            }
            ToyGlyph.MAP -> {
                val map = Path().apply {
                    moveTo(3.4f * u, 5.6f * u)
                    lineTo(9f * u, 3.4f * u)
                    lineTo(15f * u, 5.6f * u)
                    lineTo(20.6f * u, 3.4f * u)
                    lineTo(20.6f * u, 18.4f * u)
                    lineTo(15f * u, 20.6f * u)
                    lineTo(9f * u, 18.4f * u)
                    lineTo(3.4f * u, 20.6f * u)
                    close()
                }
                drawPath(map, tint)
                drawLine(Color.Black.copy(alpha = 0.22f), p(9f, 3.4f), p(9f, 18.4f), strokeWidth = 1.6f * u)
                drawLine(Color.Black.copy(alpha = 0.22f), p(15f, 5.6f), p(15f, 20.6f), strokeWidth = 1.6f * u)
                drawCircle(color = Color(0xFFFF5252), radius = 2.2f * u, center = p(15.8f, 10.4f))
            }
            ToyGlyph.WHEEL -> {
                drawCircle(color = tint, radius = 7.6f * u, center = p(12f, 10.6f), style = Stroke(width = 2.4f * u))
                for (i in 0 until 4) {
                    rotate(degrees = i * 45f, pivot = p(12f, 10.6f)) {
                        drawLine(tint, p(12f, 3.4f), p(12f, 17.8f), strokeWidth = 1.5f * u)
                    }
                }
                drawCircle(color = tint, radius = 1.9f * u, center = p(12f, 10.6f))
                drawLine(tint, p(7.4f, 21.4f), p(12f, 16.4f), strokeWidth = 2.2f * u, cap = StrokeCap.Round)
                drawLine(tint, p(16.6f, 21.4f), p(12f, 16.4f), strokeWidth = 2.2f * u, cap = StrokeCap.Round)
            }
            ToyGlyph.HOME -> {
                val roof = Path().apply {
                    moveTo(2.6f * u, 12.4f * u)
                    lineTo(12f * u, 3.6f * u)
                    lineTo(21.4f * u, 12.4f * u)
                    lineTo(18.6f * u, 12.4f * u)
                    lineTo(12f * u, 6.4f * u)
                    lineTo(5.4f * u, 12.4f * u)
                    close()
                }
                drawPath(roof, tint)
                drawRoundRect(color = tint, topLeft = p(5.6f, 12.2f), size = Size(12.8f * u, 8.2f * u),
                    cornerRadius = CornerRadius(1.4f * u, 1.4f * u))
                drawRoundRect(color = Color.Black.copy(alpha = 0.28f), topLeft = p(10.2f, 14.6f),
                    size = Size(3.6f * u, 5.8f * u), cornerRadius = CornerRadius(1.2f * u, 1.2f * u))
            }
            ToyGlyph.REFRESH -> {
                drawArc(color = tint, startAngle = 300f, sweepAngle = 250f, useCenter = false,
                    topLeft = p(3.6f, 3.6f), size = Size(16.8f * u, 16.8f * u),
                    style = Stroke(width = 2.8f * u, cap = StrokeCap.Round))
                val arrow = Path().apply {
                    moveTo(15.2f * u, 1.6f * u)
                    lineTo(20.4f * u, 4.4f * u)
                    lineTo(14.6f * u, 7.6f * u)
                    close()
                }
                drawPath(arrow, tint)
            }
            ToyGlyph.CLOCK -> {
                drawCircle(color = tint, radius = 9.6f * u, center = p(12f, 12f), style = Stroke(width = 2.6f * u))
                drawLine(tint, p(12f, 12f), p(12f, 6.8f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
                drawLine(tint, p(12f, 12f), p(15.8f, 14.2f), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
                drawCircle(color = tint, radius = 1.6f * u, center = p(12f, 12f))
            }
            ToyGlyph.FLAG -> {
                drawRoundRect(color = tint, topLeft = p(5.4f, 2.8f), size = Size(2.2f * u, 18.4f * u),
                    cornerRadius = CornerRadius(1.1f * u, 1.1f * u))
                val flag = Path().apply {
                    moveTo(7.6f * u, 3.6f * u)
                    lineTo(19.4f * u, 3.6f * u)
                    lineTo(16.4f * u, 7.2f * u)
                    lineTo(19.4f * u, 10.8f * u)
                    lineTo(7.6f * u, 10.8f * u)
                    close()
                }
                drawPath(flag, tint)
                drawRect(color = Color.Black.copy(alpha = 0.25f), topLeft = p(9.6f, 5.4f), size = Size(2.6f * u, 2.6f * u))
                drawRect(color = Color.Black.copy(alpha = 0.25f), topLeft = p(14.8f, 5.4f), size = Size(2.6f * u, 2.6f * u))
            }
            ToyGlyph.DASH -> {
                drawLine(tint, p(13.4f, 6.4f), p(21.4f, 6.4f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
                drawLine(tint, p(9.4f, 12f), p(21.4f, 12f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
                drawLine(tint, p(13.4f, 17.6f), p(21.4f, 17.6f), strokeWidth = 2.6f * u, cap = StrokeCap.Round)
                drawCircle(color = tint.copy(alpha = 0.55f), radius = 2.4f * u, center = p(6.4f, 12f))
            }
            ToyGlyph.CHECK_CIRCLE -> {
                drawCircle(color = tint, radius = 9.6f * u, center = p(12f, 12f))
                val check = Path().apply {
                    moveTo(7.8f * u, 12.4f * u)
                    lineTo(10.8f * u, 15.4f * u)
                    lineTo(16.4f * u, 9.2f * u)
                }
                drawPath(check, Color.White, style = Stroke(width = 2.6f * u, cap = StrokeCap.Round))
            }
            ToyGlyph.SPIRAL -> {
                drawArc(color = tint, startAngle = 0f, sweepAngle = 300f, useCenter = false,
                    topLeft = p(3.4f, 3.4f), size = Size(17.2f * u, 17.2f * u),
                    style = Stroke(width = 2.6f * u, cap = StrokeCap.Round))
                drawArc(color = tint, startAngle = 40f, sweepAngle = 300f, useCenter = false,
                    topLeft = p(6.8f, 6.8f), size = Size(10.4f * u, 10.4f * u),
                    style = Stroke(width = 2.4f * u, cap = StrokeCap.Round))
                drawCircle(color = tint, radius = 1.8f * u, center = p(12f, 12f))
            }
            ToyGlyph.MOON -> {
                val moon = Path().apply {
                    moveTo(19.6f * u, 15.8f * u)
                    cubicTo(16.8f * u, 19.4f * u, 12.4f * u, 21f * u, 8.6f * u, 19.2f * u)
                    cubicTo(3.8f * u, 17f * u, 2.6f * u, 11.2f * u, 5.4f * u, 6.8f * u)
                    cubicTo(7.2f * u, 4f * u, 10.4f * u, 2.8f * u, 13.6f * u, 3.4f * u)
                    cubicTo(10.8f * u, 5.2f * u, 9.4f * u, 8.2f * u, 9.4f * u, 11.4f * u)
                    cubicTo(9.4f * u, 16.2f * u, 14f * u, 18.4f * u, 19.6f * u, 15.8f * u)
                    close()
                }
                drawPath(moon, tint)
                drawCircle(color = tint.copy(alpha = 0.8f), radius = 1.5f * u, center = p(18.2f, 5.6f))
                drawCircle(color = tint.copy(alpha = 0.6f), radius = 1.1f * u, center = p(20.6f, 9.8f))
            }
        }
    }
}
