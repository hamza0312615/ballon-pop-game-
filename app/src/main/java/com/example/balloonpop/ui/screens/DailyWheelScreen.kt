package com.example.balloonpop.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.WheelSegment
import com.example.balloonpop.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DailyWheelScreen(
    isSpinning: Boolean,
    wheelRotation: Float,
    wonSegment: WheelSegment?,
    dailyStreakDay: Int,
    remainingMillis: Long,
    onSpinFree: () -> Unit,
    onSpinInstant: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var timerDisplay by remember { mutableStateOf("") }

    LaunchedEffect(remainingMillis) {
        var rem = remainingMillis
        while (rem > 0) {
            val hours = rem / (1000 * 60 * 60)
            val mins = (rem % (1000 * 60 * 60)) / (1000 * 60)
            val secs = (rem % (1000 * 60)) / 1000
            timerDisplay = String.format("%02d:%02d:%02d", hours, mins, secs)
            delay(1000L)
            rem = (rem - 1000L).coerceAtLeast(0L)
        }
        timerDisplay = "READY"
    }

    val isFreeReady = remainingMillis <= 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF261250), Color(0xFF190C36), Color(0xFF2C1045))
                )
            )
            .border(
                2.dp,
                Brush.linearGradient(listOf(NeonYellow, NeonPink, NeonCyan)),
                RoundedCornerShape(26.dp)
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Modal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎡", fontSize = 26.sp)
                Column {
                    Text(
                        text = "LUCKY FORTUNE WHEEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Daily Rewards & Streak Timeline",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x55FFFFFF))
                    .clickable { onClose() }
                    .testTag("close_wheel_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 7-Day Streak Timeline
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0x40FFFFFF))
                .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(18.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 7-DAY STREAK TIMELINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldCoin
                )
                Text(
                    text = "Day $dailyStreakDay / 7",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonYellow
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(GameConstants.DAILY_STREAK_TIMELINE) { streak ->
                    val isCompleted = streak.dayNumber < dailyStreakDay
                    val isCurrent = streak.dayNumber == dailyStreakDay

                    val streakBg = when {
                        isCurrent -> Modifier.background(Brush.verticalGradient(listOf(NeonPink, Color(0xFF7B1FA2))))
                        isCompleted -> Modifier.background(Color(0x6000E676))
                        else -> Modifier.background(Color(0x30FFFFFF))
                    }

                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .then(streakBg)
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) NeonYellow else Color(0x20FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "D${streak.dayNumber}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isCurrent) NeonYellow else Color.White
                        )
                        Text(text = if (streak.isJackpot) "👑" else if (streak.rewardGems > 0) "💎" else "🪙", fontSize = 16.sp)
                        Text(
                            text = if (isCompleted) "✓" else if (streak.rewardGems > 0) "+${streak.rewardGems}💎" else "+${streak.rewardCoins}🪙",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Wheel Visual Container
        Box(
            modifier = Modifier
                .size(260.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Spinning Wheel Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(wheelRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f - 8f
                val sweepAngle = 360f / GameConstants.WHEEL_SEGMENTS.size

                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 32f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                    setShadowLayer(4f, 0f, 1f, android.graphics.Color.BLACK)
                }

                GameConstants.WHEEL_SEGMENTS.forEachIndexed { index, segment ->
                    val startAngle = index * sweepAngle
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )

                    // Draw text label on segment
                    val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                    val textDistance = radius * 0.65f
                    val textX = center.x + (cos(textAngleRad) * textDistance).toFloat()
                    val textY = center.y + (sin(textAngleRad) * textDistance).toFloat() + 10f

                    drawContext.canvas.nativeCanvas.drawText(
                        segment.label,
                        textX,
                        textY,
                        paint
                    )
                }

                // Outer border ring
                drawCircle(
                    color = GoldCoin,
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )

                // Center hub
                drawCircle(
                    color = Color(0xFF130F2D),
                    radius = 26f,
                    center = center
                )
                drawCircle(
                    color = GoldCoin,
                    radius = 22f,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
            }

            // Pointer arrow at Top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
            ) {
                Text(text = "▼", fontSize = 28.sp, color = NeonYellow)
            }
        }

        // Won Prize Announcement if any
        if (wonSegment != null && !isSpinning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E676), Color(0xFF00B0FF))
                        )
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉 YOU WON ${wonSegment.label}!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }

        // Action Buttons: Free Spin (if ready) or Countdown + Instant Spin
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isFreeReady) {
                // Free Daily Spin Button
                val spinBg = if (isSpinning) {
                    Modifier.background(Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)))
                } else {
                    Modifier.background(Brush.horizontalGradient(listOf(NeonYellow, NeonPink)))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .then(spinBg)
                        .border(1.5.dp, Color.White, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isSpinning) { onSpinFree() }
                        .testTag("spin_fortune_wheel_button")
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSpinning) "SPINNING... 🎰" else "FREE DAILY SPIN 🎁",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSpinning) Color.White.copy(alpha = 0.6f) else Color.Black,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                // Daily Spin Cooldown Badge + Instant Spin Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x33FFFFFF))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Next Free Daily Spin in:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDim
                    )
                    Text(
                        text = timerDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan
                    )
                }

                // Instant Spin Button (50 Coins or 3 Gems)
                val instantBg = if (isSpinning) {
                    Modifier.background(Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)))
                } else {
                    Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .then(instantBg)
                        .border(1.5.dp, Color.White, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isSpinning) { onSpinInstant() }
                        .testTag("instant_spin_button")
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isSpinning) "SPINNING... 🎰" else "INSTANT SPIN (50🪙 / 3💎)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSpinning) Color.White.copy(alpha = 0.6f) else Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
