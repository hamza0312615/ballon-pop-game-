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
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.WheelSegment
import com.example.balloonpop.ui.components.CurrencyArt
import com.example.balloonpop.ui.components.DailyFortuneWheelHubArt
import com.example.balloonpop.ui.components.DailyWheelIconArt
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

    // Pass 18: reward-card visibility (the prize itself is granted by the
    // ViewModel; this only controls the confirmation card below).
    var rewardDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(wonSegment) {
        if (wonSegment != null) rewardDismissed = false
    }

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
            com.example.balloonpop.ui.theme.StageMagentaTop,
            com.example.balloonpop.ui.theme.StageMagentaDeep,
            com.example.balloonpop.ui.theme.StageCurtainEdge
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
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Streak",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(16.dp)
                    )
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

                // Center Gold Medallion Hub with Mascot / Fortune Core
                DailyFortuneWheelHubArt(modifier = Modifier.size(56.dp))

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
                        DailyWheelIconArt(modifier = Modifier.size(26.dp))
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

        // Pass 18: reward confirmation. The ViewModel grants the prize the
        // moment the spin lands and sets wonSegment — previously nothing on
        // this screen ever showed it, so a landed prize looked like nothing
        // happened. The prize is already in the balance; COLLECT just
        // dismisses the card.
        if (wonSegment != null && !isSpinning && !rewardDismissed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x990B0620)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.82f)
                        .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFFD700))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF2A1650))
                        .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                ) {
                    Text(
                        text = "🎉 YOU WON!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = wonSegment.label,
                        fontFamily = Baloo2FontFamily,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Added to your balance",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC4B5FD)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFFFE082), Color(0xFFF59E0B))
                                )
                            )
                            .clickable { rewardDismissed = true }
                            .padding(horizontal = 36.dp, vertical = 12.dp)
                            .testTag("wheel_reward_collect_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "COLLECT",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5B2E00)
                        )
                    }
                }
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

            CurrencyArt(currencyType = "gem", modifier = Modifier.size(16.dp))

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
    // Pass 18: build the disc from the SAME GameConstants.WHEEL_SEGMENTS
    // list the ViewModel draws the winner from. The old hardcoded list was
    // in a different order, so the segment under the pointer never matched
    // the prize actually granted.
    val segments = GameConstants.WHEEL_SEGMENTS.map { seg ->
        if (seg.coins > 0) {
            WheelSegmentItem("%,d\ncoins".format(seg.coins), seg.color, isCoin = true)
        } else {
            WheelSegmentItem("${seg.gems}\ngems", seg.color, isCoin = false)
        }
    }

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

            // Text on segment — Pass 20: labels pulled slightly inward
            // (0.65R -> 0.60R) and shrunk (34f -> 30f below) so the top
            // segment's label clears the pointer instead of hiding
            // behind it.
            val midAngleDeg = ((startAngle + sweepAngle / 2f) % 360f + 360f) % 360f
            val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
            val textDist = radius * 0.60f
            val tx = (cx + textDist * cos(midAngleRad)).toFloat()
            val ty = (cy + textDist * sin(midAngleRad)).toFloat()

            drawContext.canvas.nativeCanvas.apply {
                save()
                translate(tx, ty)
                val textRotation = if (midAngleDeg in 90f..270f) midAngleDeg - 90f else midAngleDeg + 90f
                rotate(textRotation)
                val paint = Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                    setShadowLayer(4f, 0f, 2f, android.graphics.Color.DKGRAY)
                }
                val lines = seg.label.split("\n")
                lines.forEachIndexed { lIdx, line ->
                    drawText(line, 0f, (lIdx * 30f) - 9f, paint)
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
