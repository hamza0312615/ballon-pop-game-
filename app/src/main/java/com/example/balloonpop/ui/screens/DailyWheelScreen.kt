package com.example.balloonpop.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.WheelSegment
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lucky Fortune Wheel Screen.
 * Exact match to Image 3:
 * - Cosmic starry deep-indigo background
 * - LUCKY FORTUNE WHEEL header + Daily Rewards and Streak Timeline
 * - 7-Day Streak Timeline (D1..D7 with gems +10..+60 and NOW gold badge)
 * - 6-Segment Fortune Wheel with golden rim & gift hub
 * - Gold FREE DAILY SPIN button + countdown timer
 */
@Composable
fun DailyWheelScreen(
    isSpinning: Boolean,
    wheelRotation: Float,
    wonSegment: WheelSegment?,
    dailyStreakDay: Int,
    remainingMillis: Long,
    onSpinFree: () -> Unit,
    onSpinInstant: () -> Unit,
    onWatchAdForSpin: () -> Unit = onSpinFree,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timerDisplay by remember { mutableStateOf("") }

    LaunchedEffect(remainingMillis) {
        var rem = remainingMillis
        while (rem > 0) {
            val hours = rem / (1000 * 60 * 60)
            val mins = (rem % (1000 * 60 * 60)) / (1000 * 60)
            timerDisplay = "${hours}h ${mins}m"
            delay(1000L)
            rem = (rem - 1000L).coerceAtLeast(0L)
        }
        timerDisplay = "READY"
    }

    val isFreeReady = remainingMillis <= 0L

    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF070B1F),
            Color(0xFF0F1738),
            Color(0xFF131742),
            Color(0xFF0A0D24)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .testTag("daily_wheel_screen")
    ) {
        // Ambient background stars and soft nebulae
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Twinkling subtle stars
            val starPositions = listOf(
                Offset(w * 0.12f, h * 0.10f),
                Offset(w * 0.88f, h * 0.12f),
                Offset(w * 0.22f, h * 0.25f),
                Offset(w * 0.78f, h * 0.35f),
                Offset(w * 0.08f, h * 0.55f),
                Offset(w * 0.92f, h * 0.60f),
                Offset(w * 0.20f, h * 0.82f),
                Offset(w * 0.85f, h * 0.88f)
            )
            starPositions.forEachIndexed { i, pos ->
                drawCircle(Color.White.copy(alpha = if (i % 2 == 0) 0.8f else 0.4f), radius = if (i % 3 == 0) 3f else 1.8f, center = pos)
            }
        }

        // Close / Back button at top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
                .clickable { onClose() }
                .testTag("wheel_close_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                Text(
                    text = "LUCKY FORTUNE WHEEL",
                    fontFamily = Baloo2FontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Daily Rewards and Streak Timeline",
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD)
                )
            }

            // 7-DAY STREAK TIMELINE
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(text = "🔥", fontSize = 14.sp)
                    Text(
                        text = "7-DAY STREAK",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF93C5FD),
                        letterSpacing = 1.sp
                    )
                }

                val streakGems = listOf(10, 15, 20, 25, 30, 40, 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    streakGems.forEachIndexed { index, gemAmt ->
                        val dayNum = index + 1
                        val isToday = dayNum == dailyStreakDay
                        StreakDayCard(
                            dayNum = dayNum,
                            gemAmount = gemAmt,
                            isToday = isToday,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // GIANT FORTUNE WHEEL
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .testTag("fortune_wheel_canvas"),
                contentAlignment = Alignment.Center
            ) {
                // Wheel Rotating Disc
                FortuneWheelDisc(
                    rotation = wheelRotation,
                    modifier = Modifier.size(280.dp)
                )

                // Outer Gold Ring with Sparkles
                Canvas(modifier = Modifier.size(284.dp)) {
                    val r = size.width / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.Transparent, Color(0x33FFD700), Color(0xFFFFD700)),
                            radius = r
                        ),
                        radius = r,
                        style = Stroke(width = 5f)
                    )
                }

                // Center Gold Medallion Hub with Gift Box
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .shadow(12.dp, CircleShape, spotColor = Color(0xFFFFD700))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFFBEB), Color(0xFFFFE082), Color(0xFFF59E0B), Color(0xFFB45309))
                            )
                        )
                        .border(3.dp, Color(0xFFFFFBEB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎁", fontSize = 30.sp)
                }

                // Top Pointer Needle pointing down at 12 o'clock
                Canvas(
                    modifier = Modifier
                        .size(36.dp, 44.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-14).dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val needlePath = Path().apply {
                        moveTo(w * 0.15f, 0f)
                        lineTo(w * 0.85f, 0f)
                        lineTo(w * 0.5f, h)
                        close()
                    }
                    drawPath(
                        path = needlePath,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFFBEB), Color(0xFFFFD54F), Color(0xFFD97706))
                        )
                    )
                    drawPath(
                        path = needlePath,
                        color = Color.White,
                        style = Stroke(width = 2f)
                    )
                }
            }

            // Bottom CTA Button & Resets Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                val spinInteraction = remember { MutableInteractionSource() }
                val isSpinPressed by spinInteraction.collectIsPressedAsState()
                val spinScale by animateFloatAsState(targetValue = if (isSpinPressed) 0.95f else 1.0f, label = "spin_btn")

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(60.dp)
                        .scale(spinScale)
                        .shadow(16.dp, RoundedCornerShape(30.dp), spotColor = Color(0xFFFFD700))
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFFBEB),
                                    Color(0xFFFFE082),
                                    Color(0xFFF59E0B),
                                    Color(0xFFD97706)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFFFD54F), Color(0xFF92400E))),
                            RoundedCornerShape(30.dp)
                        )
                        .clickable(
                            enabled = !isSpinning,
                            interactionSource = spinInteraction,
                            indication = null
                        ) {
                            if (isFreeReady) onSpinFree() else onWatchAdForSpin()
                        }
                        .testTag("free_daily_spin_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    // Top Crescent Shine
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(0.85f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color.Transparent)))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "🎁", fontSize = 26.sp)
                        Text(
                            text = if (isFreeReady) "FREE DAILY SPIN" else "WATCH AD TO SPIN",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5B2E00),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Text(
                    text = if (isFreeReady) "Free spin available now!" else "Resets in $timerDisplay",
                    fontFamily = NunitoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD)
                )
            }
        }
    }
}

@Composable
private fun StreakDayCard(
    dayNum: Int,
    gemAmount: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) Color(0x33F59E0B) else Color(0x221E293B))
            .border(
                width = if (isToday) 2.dp else 1.dp,
                color = if (isToday) Color(0xFFFFD54F) else Color(0x3394A3B8),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "D$dayNum",
                fontFamily = Baloo2FontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isToday) Color(0xFFFFD54F) else Color(0xCCFFFFFF)
            )

            Text(text = "💎", fontSize = 16.sp)

            Text(
                text = "+$gemAmount",
                fontFamily = NunitoFontFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFD54F))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "NOW",
                        fontFamily = NunitoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B2E00)
                    )
                }
            }
        }
    }
}

@Composable
private fun FortuneWheelDisc(
    rotation: Float,
    modifier: Modifier = Modifier
) {
    val segments = listOf(
        WheelSegmentItem("750\ncoins", Color(0xFFFF5D8F), isCoin = true),
        WheelSegmentItem("1500\ncoins", Color(0xFF00F0FF), isCoin = true),
        WheelSegmentItem("25\ngems", Color(0xFFFFC93C), isCoin = false),
        WheelSegmentItem("250\ncoins", Color(0xFF9D4EDD), isCoin = true),
        WheelSegmentItem("500\ncoins", Color(0xFF3B82F6), isCoin = true),
        WheelSegmentItem("10\ngems", Color(0xFFE040FB), isCoin = false)
    )

    Canvas(modifier = modifier.rotate(rotation)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val radius = w / 2f
        val sweepAngle = 360f / segments.size

        // Draw colored pie slices
        segments.forEachIndexed { i, seg ->
            val startAngle = i * sweepAngle - 90f - sweepAngle / 2f
            drawArc(
                color = seg.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = Size(w, h)
            )

            // Outer slice divider line
            val rad = Math.toRadians(startAngle.toDouble())
            drawLine(
                color = Color(0x66FFFFFF),
                start = Offset(cx, cy),
                end = Offset((cx + radius * cos(rad)).toFloat(), (cy + radius * sin(rad)).toFloat()),
                strokeWidth = 2f
            )

            // Text on segment
            val midAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
            val textDist = radius * 0.65f
            val tx = (cx + textDist * cos(midAngleRad)).toFloat()
            val ty = (cy + textDist * sin(midAngleRad)).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                save()
                translate(tx, ty)
                rotate((startAngle + sweepAngle / 2f + 90f).toFloat())
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 34f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(4f, 0f, 2f, android.graphics.Color.DKGRAY)
                }
                val lines = seg.label.split("\n")
                lines.forEachIndexed { lIdx, line ->
                    drawText(line, 0f, (lIdx * 34f) - 10f, paint)
                }
                restore()
            }
        }
    }
}

private data class WheelSegmentItem(
    val label: String,
    val color: Color,
    val isCoin: Boolean
)
