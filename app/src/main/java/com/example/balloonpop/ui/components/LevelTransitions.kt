package com.example.balloonpop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.WorldZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Confetti particle state for celebration animation
 */
private data class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    var vRot: Float,
    var size: Float,
    val color: Color,
    val isRibbon: Boolean,
    var phase: Float
)

/**
 * Dynamic physics-based Confetti Cannon Particle System
 */
@Composable
fun ConfettiCelebrationView(
    modifier: Modifier = Modifier,
    particleCount: Int = 48
) {
    val confettiColors = remember {
        listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF2A75), // Hot Pink
            Color(0xFF00E5FF), // Cyan
            Color(0xFF10B981), // Emerald
            Color(0xFFA855F7), // Purple
            Color(0xFFFF9100), // Tangerine
            Color(0xFFFBBF24), // Amber
            Color(0xFFFFFFFF)  // White diamond
        )
    }

    val particles = remember {
        List(particleCount) {
            val angle = Random.nextFloat() * 6.28f
            val speed = Random.nextFloat() * 7f + 2f
            ConfettiParticle(
                x = Random.nextFloat() * 1000f,
                y = -Random.nextFloat() * 300f - 20f,
                vx = cos(angle) * speed * 0.7f,
                vy = (sin(angle) * speed * 0.5f).coerceAtLeast(0f) + Random.nextFloat() * 4f + 3f,
                rotation = Random.nextFloat() * 360f,
                vRot = (Random.nextFloat() - 0.5f) * 12f,
                size = Random.nextFloat() * 14f + 8f,
                color = confettiColors.random(),
                isRibbon = Random.nextBoolean(),
                phase = Random.nextFloat() * 6.28f
            )
        }
    }

    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameNanos {
                frameTick++
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            // Apply physics
            p.x += p.vx + sin(p.phase) * 1.5f
            p.y += p.vy
            p.phase += 0.08f
            p.rotation += p.vRot
            p.vy += 0.04f // gentle gravity

            // Respawn at top if fallen past screen
            if (p.y > h + 50f || p.x < -60f || p.x > w + 60f) {
                p.y = -Random.nextFloat() * 80f - 20f
                p.x = Random.nextFloat() * w
                p.vy = Random.nextFloat() * 4f + 3f
                p.vx = (Random.nextFloat() - 0.5f) * 4f
            }

            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                if (p.isRibbon) {
                    drawRect(
                        color = p.color,
                        topLeft = Offset(p.x - p.size / 2f, p.y - p.size / 4f),
                        size = Size(p.size * 1.6f, p.size * 0.65f)
                    )
                } else {
                    // Star diamond flake
                    drawCircle(
                        color = p.color,
                        radius = p.size * 0.45f,
                        center = Offset(p.x, p.y)
                    )
                }
            }
        }
    }
}

/**
 * Rotating Golden Sunburst / God Rays Background
 */
@Composable
fun SunburstBackground(
    modifier: Modifier = Modifier,
    beamColor: Color = Color(0x33FFD700),
    centerColor: Color = Color(0x55FFA500)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sunburst_rot")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst_angle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxDim = kotlin.math.hypot(size.width, size.height)
        val numRays = 14
        val rayAngle = (360f / numRays)

        rotate(rotation, pivot = center) {
            for (i in 0 until numRays) {
                if (i % 2 == 0) {
                    val angle1 = (i * rayAngle) * (PI.toFloat() / 180f)
                    val angle2 = ((i + 1) * rayAngle) * (PI.toFloat() / 180f)

                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x + cos(angle1) * maxDim, center.y + sin(angle1) * maxDim)
                        lineTo(center.x + cos(angle2) * maxDim, center.y + sin(angle2) * maxDim)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(centerColor, beamColor, Color.Transparent),
                            center = center,
                            radius = maxDim * 0.8f
                        ),
                        style = Fill
                    )
                }
            }
        }
    }
}

/**
 * Animated High Score Badge with Trophy, Radiant Aura & Score Delta
 */
@Composable
fun HighScoreCelebrationBadge(
    previousHighScore: Int,
    newScore: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "trophy_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .scale(glowScale)
            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF382305),
                        Color(0xFF1E1303),
                        Color(0xFF0F0A02)
                    )
                )
            )
            .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🏆", fontSize = 24.sp)
            Text(
                text = "NEW ALL-TIME RECORD!",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFDE68A),
                letterSpacing = 1.sp
            )
            Text(text = "👑", fontSize = 20.sp)
        }

        val diff = (newScore - previousHighScore).coerceAtLeast(0)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Previous: ${String.format("%,d", previousHighScore)}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD1D5DB)
            )
            Text(text = "➔", fontSize = 11.sp, color = Color(0xFFFFD700))
            Text(
                text = "New: ${String.format("%,d", newScore)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFBBF24)
            )
            if (diff > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF15803D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+${String.format("%,d", diff)} PTS!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFDCFCE7)
                    )
                }
            }
        }
    }
}

/**
 * Animated Progress Bridge between 200 levels (Stage N -> Stage N+1)
 */
@Composable
fun StagePathBridge(
    currentLevel: Int,
    nextLevel: Int,
    maxLevels: Int = 200,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "runner_pulse")
    val runnerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "runner_t"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF19162E), Color(0xFF0F0D1C))
                )
            )
            .border(1.5.dp, Color(0xFF38335C), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage N Cleared Node
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                        .border(1.5.dp, Color(0xFF86EFAC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
                Text(
                    text = "Stage $currentLevel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF86EFAC)
                )
            }

            // Connecting Arrow
            Text(text = "════▶", fontSize = 11.sp, color = Color(0xFFA855F7), fontWeight = FontWeight.Black)

            // Stage N+1 Unlocked Node
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Stage $nextLevel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE047)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFEA00), Color(0xFFF59E0B))
                            )
                        )
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⭐", fontSize = 13.sp)
                }
            }
        }

        // Animated progress track with Popster mascot indicator running along the line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF282442))
            )

            // Active track fill
            Box(
                modifier = Modifier
                    .fillMaxWidth((currentLevel.toFloat() / maxLevels.toFloat()).coerceIn(0.05f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF10B981), Color(0xFFFBBF24), Color(0xFFA855F7))
                        )
                    )
            )

            // Moving mascot avatar along connection
            val mascotOffsetFraction = runnerOffset
            Box(
                modifier = Modifier
                    .fillMaxWidth(mascotOffsetFraction)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(text = "🎈", fontSize = 14.sp)
            }
        }

        // Campaign completion percentage
        val pct = (currentLevel * 100 / maxLevels).coerceIn(0, 100)
        Text(
            text = "Campaign Progress: $currentLevel/$maxLevels Stages Cleared ($pct%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
        )
    }
}

/**
 * Animated Integer Counter (Rolling up numbers smoothly)
 */
@Composable
fun AnimatedScoreCounter(
    targetValue: Int,
    formatPrefix: String = "",
    formatSuffix: String = "",
    durationMillis: Int = 1000
): String {
    var animatedValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < durationMillis) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
            val easeOut = 1f - (1f - fraction) * (1f - fraction)
            animatedValue = (targetValue * easeOut).toInt()
            delay(16L)
        }
        animatedValue = targetValue
    }

    return "$formatPrefix${String.format("%,d", animatedValue)}$formatSuffix"
}

/**
 * Full-Screen Level-to-Level Warp Transition Portal
 * Provides an exciting, satisfying visual transition between stages 1 to 200.
 */
@Composable
fun LevelTransitionPortal(
    fromLevel: Int,
    toLevel: Int,
    targetPops: Int,
    timeLimit: Int,
    maxMissesAllowed: Int,
    isBossStage: Boolean = (toLevel % 10 == 0),
    isFrenzyStage: Boolean = (toLevel % 5 == 0 && !isBossStage),
    zone: WorldZone? = null,
    onAnimationFinished: () -> Unit
) {
    var countdown by remember { mutableIntStateOf(3) }
    var phase by remember { mutableStateOf("WARP") } // "WARP" -> "COUNTDOWN" -> "READY"

    // Portal scaling animation
    val transition = rememberInfiniteTransition(label = "portal_anim")
    val portalScale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(toLevel) {
        delay(900L)
        phase = "COUNTDOWN"
        countdown = 3
        delay(550L)
        countdown = 2
        delay(550L)
        countdown = 1
        delay(550L)
        countdown = 0
        delay(350L)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF005050A))
            .clickable { onAnimationFinished() } // Allow tapping to skip
            .testTag("level_transition_portal"),
        contentAlignment = Alignment.Center
    ) {
        // Sunburst God Rays Background
        SunburstBackground(
            beamColor = if (isBossStage) Color(0x44A855F7) else Color(0x44FFD700),
            centerColor = if (isBossStage) Color(0x777C3AED) else Color(0x77F59E0B)
        )

        // Floating Confetti
        ConfettiCelebrationView(particleCount = 35)

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .scale(portalScale)
                .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFFD700))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isBossStage) Color(0xFF2C1338) else Color(0xFF1F1708),
                            Color(0xFF140D24),
                            Color(0xFF090712)
                        )
                    )
                )
                .border(
                    3.dp,
                    if (isBossStage) Color(0xFFA855F7) else Color(0xFFFFD700),
                    RoundedCornerShape(32.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Ribbon / Zone Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (isBossStage) listOf(Color(0xFF7C3AED), Color(0xFF4C1D95))
                            else listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isBossStage) "👑 BOSS STAGE • LEVEL $toLevel" else "⭐ STAGE $toLevel OF 200",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            // Zone Name & Emblem
            if (zone != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🏝️", fontSize = 22.sp)
                    Text(
                        text = "Zone ${zone.zoneNumber}: ${zone.name}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE68A)
                    )
                }
            }

            // 3-Star Target Preview
            StarsDisplay3D(starsCount = 0, size = 46.dp, animated = false)

            // Objectives Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF161528))
                    .border(1.5.dp, Color(0xFF2C284A), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    GoalBadge(icon = "🎯", label = "Target", value = "$targetPops Pops")
                    GoalBadge(icon = "⏱️", label = "Time", value = "${timeLimit}s")
                    GoalBadge(icon = "💨", label = "Max Miss", value = "$maxMissesAllowed")
                }

                if (isBossStage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF4C1D95))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ MEGA BOSS BALLOON INCOMING! ⚡",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF3E8FF)
                        )
                    }
                } else if (isFrenzyStage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF7C2D12))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥 ARCADE FRENZY! RAPID RESPAWNS! 🔥",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFEDD5)
                        )
                    }
                }
            }

            // Dramatic 3-2-1 Countdown or POP!
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEA00), Color(0xFFF59E0B), Color(0xFFB45309))
                        )
                    )
                    .border(2.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (countdown) {
                        3 -> "3"
                        2 -> "2"
                        1 -> "1"
                        else -> "POP!"
                    },
                    fontSize = if (countdown == 0) 18.sp else 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Text(
                text = "Tap anywhere to skip countdown ▶",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun GoalBadge(icon: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 18.sp)
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Black)
    }
}
