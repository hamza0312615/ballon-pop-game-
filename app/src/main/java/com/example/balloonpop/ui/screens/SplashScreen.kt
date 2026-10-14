package com.example.balloonpop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.audio.SoundManager
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

data class SplashBalloon(
    val id: Int,
    val xRatio: Float,
    val yRatio: Float,
    val radiusDp: Float,
    val color: Color,
    val isPopped: Boolean = false
)

/**
 * Balloon Pop Deluxe Premium Splash / Loading Screen.
 * Faithful implementation of the Candy-Deluxe reference design:
 * - Magical cosmic twilight nebula background with stars and confetti ribbons
 * - Adorable kawaii pink party balloon mascot with polka-dot hat, shiny eyes and rosy blush
 * - 3D bubbly candy extruded logo: "BALLOON POP DELUXE"
 * - Glossy candy progress capsule bar with "LOADING XX%" display
 * - Rotating whimsical loading statuses and animated pagination dots
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onPopBalloon: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }

    var progress by remember { mutableFloatStateOf(0f) }
    var currentTipIndex by remember { mutableIntStateOf(0) }
    var isReadyToStart by remember { mutableStateOf(false) }

    val tips = remember {
        listOf(
            "Carving wooden mountain bridges...",
            "Inflating colorful rainbow balloons...",
            "Polishing golden star rewards...",
            "Tuning power-up shockwaves...",
            "Ready for Adventure!"
        )
    }

    // Interactive poppable floating balloons
    var splashBalloons by remember {
        mutableStateOf(
            listOf(
                SplashBalloon(1, 0.14f, 0.20f, 24f, Color(0xFFFF5D8F)),
                SplashBalloon(2, 0.88f, 0.26f, 28f, Color(0xFF60A5FA)),
                SplashBalloon(3, 0.16f, 0.36f, 32f, Color(0xFF1E1035)),
                SplashBalloon(4, 0.82f, 0.40f, 26f, Color(0xFF3B1647)),
                SplashBalloon(5, 0.12f, 0.84f, 22f, Color(0xFFFF5D8F)),
                SplashBalloon(6, 0.86f, 0.86f, 24f, Color(0xFFA78BFA)),
                SplashBalloon(7, 0.30f, 0.16f, 16f, Color(0xFFFFC93C)),
                SplashBalloon(8, 0.72f, 0.34f, 18f, Color(0xFFE9D5FF))
            )
        )
    }

    // Smooth progress animation over 2.2 seconds
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val duration = 2200L

        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val fraction = (elapsed / duration.toFloat()).coerceIn(0f, 1f)
            progress = 1f - (1f - fraction) * (1f - fraction) * (1f - fraction)
            currentTipIndex = ((progress * (tips.size - 1)).toInt()).coerceIn(0, tips.size - 1)

            if (fraction >= 1f) {
                isReadyToStart = true
                delay(300L)
                onSplashFinished()
                break
            }
            delay(16L)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_infinite")

    // Mascot gentle float & breathing
    val mascotFloatY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_float"
    )

    val mascotScaleY by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_squish"
    )

    // Shimmer wave for candy progress bar
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_progress"
    )

    // Cosmic background gradient: deep space indigo to vibrant twilight purple
    val cosmicBg = Brush.verticalGradient(
        listOf(
            Color(0xFF0C102A),
            Color(0xFF16153B),
            Color(0xFF24164C),
            Color(0xFF33185E),
            Color(0xFF1E103D)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cosmicBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSplashFinished()
            }
            .testTag("splash_screen_root")
    ) {
        // 1. Ambient Background Layer: Stars, Confetti Ribbons & Ambient Balloons
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft radial ambient nebula behind mascot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55A78BFA),
                        Color(0x22EC4899),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.32f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.5f, h * 0.32f)
            )

            // Twinkling 4-point stars
            val starPositions = listOf(
                Offset(w * 0.12f, h * 0.12f) to 6f,
                Offset(w * 0.86f, h * 0.14f) to 8f,
                Offset(w * 0.28f, h * 0.32f) to 5f,
                Offset(w * 0.82f, h * 0.38f) to 7f,
                Offset(w * 0.08f, h * 0.65f) to 7f,
                Offset(w * 0.92f, h * 0.68f) to 6f,
                Offset(w * 0.76f, h * 0.82f) to 5f,
                Offset(w * 0.22f, h * 0.88f) to 6f
            )

            for ((pos, r) in starPositions) {
                drawStarGlint(pos, r, Color.White.copy(alpha = 0.85f))
            }

            // Confetti ribbons & flakes floating in air
            val confetti = listOf(
                Triple(Offset(w * 0.22f, h * 0.26f), Color(0xFFFFC93C), 35f),
                Triple(Offset(w * 0.78f, h * 0.22f), Color(0xFFFF5D8F), -25f),
                Triple(Offset(w * 0.85f, h * 0.18f), Color(0xFFA78BFA), 50f),
                Triple(Offset(w * 0.18f, h * 0.56f), Color(0xFFFFC93C), 15f),
                Triple(Offset(w * 0.84f, h * 0.55f), Color(0xFF60A5FA), -40f),
                Triple(Offset(w * 0.72f, h * 0.44f), Color(0xFFA78BFA), 20f),
                Triple(Offset(w * 0.28f, h * 0.54f), Color(0xFFFF5D8F), -30f),
                Triple(Offset(w * 0.18f, h * 0.82f), Color(0xFFA78BFA), 45f),
                Triple(Offset(w * 0.58f, h * 0.83f), Color(0xFFFFC93C), -15f)
            )

            for ((pos, color, rot) in confetti) {
                drawRect(
                    color = color.copy(alpha = 0.85f),
                    topLeft = pos,
                    size = Size(10f, 5f)
                )
            }

            // Interactive/Ambient Floating Balloons
            for (balloon in splashBalloons) {
                if (balloon.isPopped) continue
                val bx = balloon.xRatio * w
                val by = balloon.yRatio * h
                val r = balloon.radiusDp * density

                // Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = r,
                    center = Offset(bx + 4f, by + 6f)
                )

                // Balloon Body
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            balloon.color.copy(alpha = 0.85f),
                            balloon.color.copy(alpha = 0.45f)
                        ),
                        center = Offset(bx - r * 0.3f, by - r * 0.3f),
                        radius = r * 1.2f
                    ),
                    radius = r,
                    center = Offset(bx, by)
                )

                // Specular highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.45f),
                    radius = r * 0.28f,
                    center = Offset(bx - r * 0.35f, by - r * 0.35f)
                )

                // Curled string underneath
                val stringPath = Path().apply {
                    moveTo(bx, by + r)
                    cubicTo(
                        bx - 6f, by + r + 10f,
                        bx + 6f, by + r + 18f,
                        bx, by + r + 26f
                    )
                }
                drawPath(
                    path = stringPath,
                    color = Color.White.copy(alpha = 0.35f),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // 2. Foreground Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center Group: Kawaii Mascot + 3D Logo Lockup
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.offset(y = mascotFloatY.dp)
            ) {
                // Kawaii Party Balloon Mascot
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(scaleX = 1f, scaleY = mascotScaleY),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawKawaiiPartyMascot(size)
                    }
                }

                // 3D Extruded Candy Title: BALLOON POP DELUXE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((-6).dp)
                ) {
                    Candy3DText(text = "BALLOON", fontSize = 42.sp)
                    Candy3DText(text = "POP", fontSize = 48.sp)
                    Candy3DText(text = "DELUXE", fontSize = 42.sp)
                }
            }

            // Bottom Group: Glossy Candy Progress Bar, Status Subtitle & Indicator Dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Rounded Candy Progress Capsule
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(48.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFFFF5D8F))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xE6140D26))
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFD8B4FE), Color(0xFF6B21A8), Color(0xFF3B0764))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Glowing Candy Fill
                    val fillFraction = progress.coerceIn(0.06f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = fillFraction)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFF5D8F), // Hot pink
                                        Color(0xFFFB923C), // Warm orange
                                        Color(0xFFFFC93C)  // Bright gold
                                    )
                                )
                            )
                    ) {
                        // Top Specular Highlight Sweep
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White.copy(alpha = 0.55f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Traveling Shimmer Wave
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val waveX = size.width * shimmerOffset
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.55f),
                                        Color.Transparent
                                    ),
                                    startX = waveX - 40f,
                                    endX = waveX + 40f
                                )
                            )
                        }
                    }

                    // Inside Text: LOADING XX%
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "LOADING ${(progress * 100).toInt()}%",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Whimsical Status Subtitle
                Text(
                    text = tips[currentTipIndex],
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center
                )

                // 4 Animated Pagination / Loading Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeDot = (currentTipIndex % 4)
                    for (i in 0 until 4) {
                        val isActive = (i == activeDot)
                        Box(
                            modifier = Modifier
                                .size(if (isActive) 9.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) Color(0xFFFFC93C) else Color(0x66A78BFA)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isActive) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Extruded Candy Cartoon Typography.
 * Renders multiple layered outlines and glossy faces to produce the candy pop look from the reference.
 */
@Composable
private fun Candy3DText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Deep Drop Shadow Layer
        Text(
            text = text,
            fontFamily = Baloo2FontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFF4A0427),
            modifier = Modifier.offset(y = 5.dp)
        )

        // Magenta/Pink Bevel Outer Stroke
        Text(
            text = text,
            fontFamily = Baloo2FontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFF2A75),
            modifier = Modifier.offset(y = 2.dp)
        )

        // Warm Yellow-to-Peach Face Gradient Text
        Text(
            text = text,
            fontFamily = Baloo2FontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFF7ED),
            style = androidx.compose.ui.text.TextStyle(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFFAEB), // Cream white top highlight
                        Color(0xFFFFD166), // Sunny gold
                        Color(0xFFFB923C), // Peach
                        Color(0xFFFF5D8F)  // Candy pink base
                    )
                )
            )
        )
    }
}

/**
 * Procedural Kawaii Party Balloon Mascot Canvas Drawer.
 */
private fun DrawScope.drawKawaiiPartyMascot(size: Size) {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.56f
    val r = w * 0.36f

    // 1. Balloon Drop Shadow
    drawCircle(
        color = Color(0x44000000),
        radius = r,
        center = Offset(cx, cy + 6f)
    )

    // 2. Balloon Body with Multi-Tone Radiant Shading
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFB3C6), // Top-left soft shine
                Color(0xFFFF5D8F), // Mid candy pink
                Color(0xFFE11D48), // Bottom-right shadow pink
                Color(0xFF9F1239)
            ),
            center = Offset(cx - r * 0.25f, cy - r * 0.25f),
            radius = r * 1.25f
        ),
        radius = r,
        center = Offset(cx, cy)
    )

    // 3. Top-Left Curved Glossy Specular Sheen
    drawOval(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = Offset(cx - r * 0.72f, cy - r * 0.76f),
        size = Size(r * 0.55f, r * 0.35f)
    )

    // Secondary smaller specular dot
    drawCircle(
        color = Color.White.copy(alpha = 0.75f),
        radius = r * 0.08f,
        center = Offset(cx - r * 0.65f, cy - r * 0.35f)
    )

    // 4. Rosy Blush Cheeks
    drawOval(
        color = Color(0xFFFF2A75).copy(alpha = 0.45f),
        topLeft = Offset(cx - r * 0.70f, cy + r * 0.12f),
        size = Size(r * 0.32f, r * 0.18f)
    )
    drawOval(
        color = Color(0xFFFF2A75).copy(alpha = 0.45f),
        topLeft = Offset(cx + r * 0.38f, cy + r * 0.12f),
        size = Size(r * 0.32f, r * 0.18f)
    )

    // 5. Kawaii Sparkle Eyes
    val eyeY = cy + r * 0.02f
    val eyeRadius = r * 0.12f
    // Left eye
    val leftEyeCenter = Offset(cx - r * 0.38f, eyeY)
    drawCircle(color = Color(0xFF1E0A12), radius = eyeRadius, center = leftEyeCenter)
    drawCircle(color = Color.White, radius = eyeRadius * 0.42f, center = Offset(leftEyeCenter.x + 2.5f, eyeY - 2.5f))
    drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(leftEyeCenter.x - 2f, eyeY + 2f))

    // Right eye
    val rightEyeCenter = Offset(cx + r * 0.38f, eyeY)
    drawCircle(color = Color(0xFF1E0A12), radius = eyeRadius, center = rightEyeCenter)
    drawCircle(color = Color.White, radius = eyeRadius * 0.42f, center = Offset(rightEyeCenter.x + 2.5f, eyeY - 2.5f))
    drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(rightEyeCenter.x - 2f, eyeY + 2f))

    // 6. Cute Open Smiling Mouth
    val mouthPath = Path().apply {
        moveTo(cx - r * 0.15f, cy + r * 0.15f)
        quadraticTo(cx, cy + r * 0.34f, cx + r * 0.15f, cy + r * 0.15f)
        close()
    }
    drawPath(mouthPath, color = Color(0xFF1E0A12))
    // Tiny pink tongue
    val tonguePath = Path().apply {
        moveTo(cx - r * 0.08f, cy + r * 0.22f)
        quadraticTo(cx, cy + r * 0.32f, cx + r * 0.08f, cy + r * 0.22f)
        close()
    }
    drawPath(tonguePath, color = Color(0xFFFF5D8F))

    // 7. Balloon Tie Knot at Bottom
    val knotPath = Path().apply {
        moveTo(cx - r * 0.12f, cy + r)
        lineTo(cx + r * 0.12f, cy + r)
        lineTo(cx + r * 0.18f, cy + r + 12f)
        lineTo(cx - r * 0.18f, cy + r + 12f)
        close()
    }
    drawPath(knotPath, color = Color(0xFFE11D48))

    // 8. Hanging Curlicue Golden String
    val stringPath = Path().apply {
        moveTo(cx, cy + r + 10f)
        cubicTo(
            cx - 10f, cy + r + 24f,
            cx + 14f, cy + r + 38f,
            cx - 6f, cy + r + 56f
        )
    }
    drawPath(stringPath, color = Color(0xFFFFD166), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

    // 9. Party Hat on Top (Golden cone with pink polka dots and pom-pom ribbon!)
    val hatBaseLeft = Offset(cx - r * 0.18f, cy - r * 0.72f)
    val hatBaseRight = Offset(cx + r * 0.44f, cy - r * 0.88f)
    val hatTip = Offset(cx + r * 0.32f, cy - r * 1.62f)

    val hatPath = Path().apply {
        moveTo(hatBaseLeft.x, hatBaseLeft.y)
        lineTo(hatTip.x, hatTip.y)
        lineTo(hatBaseRight.x, hatBaseRight.y)
        quadraticTo(cx + r * 0.12f, cy - r * 0.70f, hatBaseLeft.x, hatBaseLeft.y)
        close()
    }
    drawPath(
        hatPath,
        brush = Brush.linearGradient(
            listOf(Color(0xFFFFE082), Color(0xFFFFC93C), Color(0xFFF59E0B)),
            start = hatBaseLeft,
            end = hatTip
        )
    )

    // Hat Polka Dots
    drawCircle(Color(0xFFFF5D8F), radius = 5f, center = Offset(cx + r * 0.08f, cy - r * 0.98f))
    drawCircle(Color(0xFFFF5D8F), radius = 4.5f, center = Offset(cx + r * 0.24f, cy - r * 1.15f))
    drawCircle(Color(0xFFFF5D8F), radius = 4f, center = Offset(cx + r * 0.12f, cy - r * 1.30f))

    // Pom-pom Ribbon Topper
    drawCircle(Color(0xFFFF5D8F), radius = 9f, center = hatTip)
    drawCircle(Color(0xFFFFC93C), radius = 4f, center = hatTip)
    // Tiny ribbon loops on hat tip
    drawLine(Color(0xFFFF5D8F), hatTip, Offset(hatTip.x - 6f, hatTip.y - 8f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFFFF5D8F), hatTip, Offset(hatTip.x + 6f, hatTip.y - 8f), strokeWidth = 3f, cap = StrokeCap.Round)
    drawLine(Color(0xFFFF5D8F), hatTip, Offset(hatTip.x, hatTip.y - 10f), strokeWidth = 3f, cap = StrokeCap.Round)
}

/**
 * 4-Point Star Sparkle Glint.
 */
private fun DrawScope.drawStarGlint(center: Offset, radius: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius * 1.8f)
        quadraticTo(center.x, center.y, center.x + radius * 1.8f, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius * 1.8f)
        quadraticTo(center.x, center.y, center.x - radius * 1.8f, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius * 1.8f)
        close()
    }
    drawPath(path, color)
    drawCircle(color = Color.White, radius = radius * 0.45f, center = center)
}
