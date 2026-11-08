package com.example.balloonpop.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.*

/**
 * 3D Jelly Game Button with glossy shine, bevel effect, and press animation
 */
@Composable
fun JellyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    fontSize: Int = 18,
    icon: String? = null,
    testTag: String = "jelly_button",
    colorVariant: JellyColorVariant = JellyColorVariant.GREEN
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btn_press"
    )

    val (topColor, bottomColor, borderColor, shadowColor, textColor) = when (colorVariant) {
        JellyColorVariant.GREEN -> listOf(
            Color(0xFF22C55E),
            Color(0xFF15803D),
            Color(0xFF86EFAC),
            Color(0xFF14532D),
            Color.White
        )
        JellyColorVariant.ORANGE -> listOf(
            Color(0xFFF59E0B),
            Color(0xFFD97706),
            Color(0xFFFDE68A),
            Color(0xFF78350F),
            Color.White
        )
        JellyColorVariant.BLUE -> listOf(
            Color(0xFF06B6D4),
            Color(0xFF0284C7),
            Color(0xFFBAE6FD),
            Color(0xFF0369A1),
            Color.White
        )
        JellyColorVariant.PURPLE -> listOf(
            Color(0xFFA855F7),
            Color(0xFF7E22CE),
            Color(0xFFE9D5FF),
            Color(0xFF581C87),
            Color.White
        )
        JellyColorVariant.PINK -> listOf(
            Color(0xFFFF2A75),
            Color(0xFFDB2777),
            Color(0xFFFBCFE8),
            Color(0xFF831843),
            Color.White
        )
        JellyColorVariant.RED -> listOf(
            Color(0xFFEF4444),
            Color(0xFFB91C1C),
            Color(0xFFFECACA),
            Color(0xFF7F1D1D),
            Color.White
        )
        JellyColorVariant.TEAL -> listOf(
            Color(0xFF14B8A6),
            Color(0xFF0F766E),
            Color(0xFF99F6E4),
            Color(0xFF134E4A),
            Color.White
        )
        JellyColorVariant.GOLD -> listOf(
            Color(0xFFFBBF24),
            Color(0xFFB45309),
            Color(0xFFFEF3C7),
            Color(0xFF78350F),
            Color.White
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(height)
            .shadow(
                elevation = if (enabled) 6.dp else 1.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (enabled) {
                    Brush.verticalGradient(listOf(topColor, bottomColor))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFF9CA3AF), Color(0xFF6B7280)))
                }
            )
            .border(
                width = 2.5.dp,
                color = if (enabled) borderColor else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag(testTag)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top gloss highlight
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.9f)
                .height(height / 2.8f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x70FFFFFF), Color(0x10FFFFFF))
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = (fontSize + 2).sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) textColor else Color(0xFFE5E7EB),
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class JellyColorVariant {
    GREEN, ORANGE, BLUE, PURPLE, PINK, RED, TEAL, GOLD
}

/**
 * Casual Game Amber Wooden Framed Modal Dialog Container
 */
@Composable
fun AmberDialogFrame(
    title: String,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    frameGradient: List<Color>? = null,
    borderColor: Color = Color(0xFFFFD700),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD9020206))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0x66FFD700))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        frameGradient ?: listOf(
                            Color(0xFF221706), // Dark Luxury Gold/Onyx frame top
                            Color(0xFF140D03),
                            Color(0xFF0A0701)
                        )
                    )
                )
                .border(
                    width = 2.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar with Embossed Title & Red 'X' Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Title in Center
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Close Button in Top-Right
                if (onClose != null) {
                    GameCloseButton(
                        onClick = onClose,
                        size = 34.dp,
                        testTag = "dialog_close_button",
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }

            // Inner Obsidian Body Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF141422), // Midnight Obsidian Glass
                                Color(0xFF0C0C16)
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFF28283E), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content
            )
        }
    }
}

/**
 * 3 Large 3D Stars Component for Pre-game and Victory dialogs
 */
@Composable
fun StarsDisplay3D(
    starsCount: Int,
    maxStars: Int = 3,
    size: Dp = 56.dp,
    animated: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 1..maxStars) {
            val isEarned = index <= starsCount

            val scaleAnim = if (animated && isEarned) {
                val infiniteTransition = rememberInfiniteTransition(label = "star_bounce_$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600 + index * 150, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "star_scale"
                )
                scale
            } else {
                1.0f
            }

            Box(
                modifier = Modifier
                    .scale(scaleAnim)
                    .size(size)
                    .shadow(
                        elevation = if (isEarned) 6.dp else 1.dp,
                        shape = CircleShape,
                        spotColor = if (isEarned) Color(0xFFF59E0B) else Color.Transparent
                    )
                    .clip(CircleShape)
                    .background(
                        if (isEarned) {
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFEA00), Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFFD1D5DB), Color(0xFF9CA3AF))
                            )
                        }
                    )
                    .border(
                        width = 2.5.dp,
                        color = if (isEarned) Color(0xFFFFFBEB) else Color(0xFFE5E7EB),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(size * 0.60f)) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val outerR = w * 0.46f
                    val innerR = outerR * 0.40f

                    val starPath = Path()
                    for (i in 0 until 10) {
                        val angle = (i * 36 - 90) * (Math.PI / 180.0)
                        val r = if (i % 2 == 0) outerR else innerR
                        val x = (cx + r * kotlin.math.cos(angle)).toFloat()
                        val y = (cy + r * kotlin.math.sin(angle)).toFloat()
                        if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                    }
                    starPath.close()

                    if (isEarned) {
                        drawPath(
                            path = starPath,
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFF59E0B)),
                                center = Offset(cx - outerR * 0.2f, cy - outerR * 0.2f),
                                radius = outerR * 1.2f
                            )
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = outerR * 0.18f,
                            center = Offset(cx - outerR * 0.22f, cy - outerR * 0.22f)
                        )
                    } else {
                        drawPath(
                            path = starPath,
                            color = Color(0xFF4B5563)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Friendly Balloon Mascot Character "Popster" drawn in code with Compose Canvas.
 * Features glossy spherical volume, shiny specular arc highlight, expressive eyes,
 * smiling mouth, rosy cheeks, balloon tie knot, and wiggly string.
 */
@Composable
fun MascotCharacter(
    mood: MascotMood = MascotMood.JOYFUL,
    size: Dp = 72.dp,
    showGoldRing: Boolean = true,
    modifier: Modifier = Modifier
) {
    val balloonColor = when (mood) {
        MascotMood.JOYFUL -> Color(0xFFFF2A6D)  // Radiant ruby rose
        MascotMood.HAPPY -> Color(0xFFFFB703)   // Golden sunny amber
        MascotMood.NEUTRAL -> Color(0xFF00E5FF) // Electric sky cyan
        MascotMood.SAD -> Color(0xFF818CF8)     // Gentle pastel indigo
    }
    val highlightColor = Color.White.copy(alpha = 0.85f)
    val knotColor = when (mood) {
        MascotMood.JOYFUL -> Color(0xFFB0003A)
        MascotMood.HAPPY -> Color(0xFFD97706)
        MascotMood.NEUTRAL -> Color(0xFF0091EA)
        MascotMood.SAD -> Color(0xFF3949AB)
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(if (showGoldRing) 10.dp else 4.dp, CircleShape, spotColor = Color(0xFFFFD700))
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            // 0. Deep Navy Background Base if within gold frame
            if (showGoldRing) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF161C38), Color(0xFF0A0E24)),
                        center = Offset(cx, cy),
                        radius = w * 0.5f
                    ),
                    radius = w * 0.49f,
                    center = Offset(cx, cy)
                )
            }

            // 1. Balloon main body (Volumetric 3D Spherical balloon)
            val bodyR = if (showGoldRing) w * 0.31f else w * 0.38f
            val bodyCenterX = cx
            val bodyCenterY = if (showGoldRing) cy + h * 0.05f else cy

            // Soft Drop Shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.38f),
                radius = bodyR,
                center = Offset(bodyCenterX, bodyCenterY + bodyR * 0.12f)
            )

            // Multi-stop 3D sphere gradient
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        balloonColor.copy(alpha = 0.95f),
                        balloonColor,
                        knotColor,
                        Color(0xFF20000D)
                    ),
                    center = Offset(bodyCenterX - bodyR * 0.32f, bodyCenterY - bodyR * 0.32f),
                    radius = bodyR * 1.35f
                ),
                radius = bodyR,
                center = Offset(bodyCenterX, bodyCenterY)
            )

            // Bottom subtle bounce rim reflection
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0x66FFD700), Color(0x88FFA000)),
                    startY = bodyCenterY,
                    endY = bodyCenterY + bodyR
                ),
                radius = bodyR,
                center = Offset(bodyCenterX, bodyCenterY)
            )

            // 2. Glossy crescent specular highlight (top-left)
            val glintPath = Path().apply {
                moveTo(bodyCenterX - bodyR * 0.65f, bodyCenterY - bodyR * 0.25f)
                cubicTo(
                    bodyCenterX - bodyR * 0.68f, bodyCenterY - bodyR * 0.65f,
                    bodyCenterX - bodyR * 0.25f, bodyCenterY - bodyR * 0.68f,
                    bodyCenterX + bodyR * 0.15f, bodyCenterY - bodyR * 0.55f
                )
            }
            drawPath(
                path = glintPath,
                color = highlightColor,
                style = Stroke(width = bodyR * 0.18f, cap = StrokeCap.Round)
            )

            // Specular flare dot
            drawCircle(
                color = Color.White,
                radius = bodyR * 0.08f,
                center = Offset(bodyCenterX - bodyR * 0.40f, bodyCenterY - bodyR * 0.40f)
            )

            // 3. Cute Face (Eyes, Rosy Cheeks, Joyful Smile)
            val eyeRadius = bodyR * 0.13f
            val eyeY = bodyCenterY + bodyR * 0.02f
            val leftEyeX = bodyCenterX - bodyR * 0.38f
            val rightEyeX = bodyCenterX + bodyR * 0.38f

            when (mood) {
                MascotMood.JOYFUL, MascotMood.HAPPY -> {
                    // Kawaii happy anime eye sparkles
                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(leftEyeX + 1.5f, eyeY - 1.5f))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(leftEyeX - 1.5f, eyeY + 1.5f))

                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius, center = Offset(rightEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(rightEyeX + 1.5f, eyeY - 1.5f))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.22f, center = Offset(rightEyeX - 1.5f, eyeY + 1.5f))
                }
                MascotMood.NEUTRAL -> {
                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius, center = Offset(leftEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.38f, center = Offset(leftEyeX - 1.5f, eyeY - 1.5f))
                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius, center = Offset(rightEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.38f, center = Offset(rightEyeX - 1.5f, eyeY - 1.5f))
                }
                MascotMood.SAD -> {
                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius * 1.1f, center = Offset(leftEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(leftEyeX - 1.5f, eyeY - 1.5f))
                    drawCircle(color = Color(0xFF1E0A16), radius = eyeRadius * 1.1f, center = Offset(rightEyeX, eyeY))
                    drawCircle(color = Color.White, radius = eyeRadius * 0.45f, center = Offset(rightEyeX - 1.5f, eyeY - 1.5f))
                }
            }

            // Rosy blush cheeks
            val cheekY = eyeY + bodyR * 0.22f
            drawCircle(
                color = Color(0x66FF2A6D),
                radius = bodyR * 0.16f,
                center = Offset(leftEyeX - bodyR * 0.18f, cheekY)
            )
            drawCircle(
                color = Color(0x66FF2A6D),
                radius = bodyR * 0.16f,
                center = Offset(rightEyeX + bodyR * 0.18f, cheekY)
            )

            // Friendly open smile with pink tongue
            val mouthY = eyeY + bodyR * 0.24f
            val mouthPath = Path().apply {
                moveTo(bodyCenterX - bodyR * 0.22f, mouthY)
                quadraticTo(bodyCenterX, mouthY + bodyR * 0.32f, bodyCenterX + bodyR * 0.22f, mouthY)
                close()
            }
            drawPath(mouthPath, color = Color(0xFF1E0A16))
            val tonguePath = Path().apply {
                moveTo(bodyCenterX - bodyR * 0.12f, mouthY + bodyR * 0.10f)
                quadraticTo(bodyCenterX, mouthY + bodyR * 0.28f, bodyCenterX + bodyR * 0.12f, mouthY + bodyR * 0.10f)
                close()
            }
            drawPath(tonguePath, color = Color(0xFFFF4081))

            // 4. Festive Party Hat (Jauntily perched on top right)
            val hatBaseLeft = bodyCenterX - bodyR * 0.18f
            val hatBaseRight = bodyCenterX + bodyR * 0.62f
            val hatBaseY = bodyCenterY - bodyR * 0.72f
            val hatApexX = bodyCenterX + bodyR * 0.42f
            val hatApexY = bodyCenterY - bodyR * 1.48f

            val hatPath = Path().apply {
                moveTo(hatBaseLeft, hatBaseY)
                lineTo(hatApexX, hatApexY)
                lineTo(hatBaseRight, hatBaseY - bodyR * 0.08f)
                close()
            }
            drawPath(
                path = hatPath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFEA00), Color(0xFFFF5722), Color(0xFF9C27B0), Color(0xFF00E5FF)),
                    start = Offset(hatBaseLeft, hatBaseY),
                    end = Offset(hatApexX, hatApexY)
                )
            )
            // Party hat stripes
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(hatBaseLeft + (hatApexX - hatBaseLeft) * 0.35f, hatBaseY - bodyR * 0.25f),
                end = Offset(hatBaseRight - bodyR * 0.15f, hatBaseY - bodyR * 0.28f),
                strokeWidth = bodyR * 0.08f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(hatBaseLeft + (hatApexX - hatBaseLeft) * 0.65f, hatBaseY - bodyR * 0.48f),
                end = Offset(hatBaseRight - bodyR * 0.25f, hatBaseY - bodyR * 0.52f),
                strokeWidth = bodyR * 0.08f,
                cap = StrokeCap.Round
            )

            // Fluffy pom-pom on party hat tip
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFF9100)),
                    center = Offset(hatApexX, hatApexY),
                    radius = bodyR * 0.20f
                ),
                radius = bodyR * 0.18f,
                center = Offset(hatApexX, hatApexY)
            )

            // 5. Balloon Tie Knot & Wavy Curly String at Bottom
            val knotY = bodyCenterY + bodyR
            val knotPath = Path().apply {
                moveTo(bodyCenterX, knotY)
                lineTo(bodyCenterX - bodyR * 0.14f, knotY + bodyR * 0.16f)
                lineTo(bodyCenterX + bodyR * 0.14f, knotY + bodyR * 0.16f)
                close()
            }
            drawPath(knotPath, color = knotColor)

            val stringPath = Path().apply {
                val startY = knotY + bodyR * 0.16f
                moveTo(bodyCenterX, startY)
                cubicTo(
                    bodyCenterX - bodyR * 0.25f, startY + bodyR * 0.18f,
                    bodyCenterX + bodyR * 0.25f, startY + bodyR * 0.36f,
                    bodyCenterX - bodyR * 0.10f, (h * 0.96f).coerceAtLeast(startY + bodyR * 0.5f)
                )
            }
            drawPath(
                path = stringPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = bodyR * 0.06f, cap = StrokeCap.Round)
            )

            // 6. Bold Polished Gold Ring Frame (Outermost Layer)
            if (showGoldRing) {
                val ringRadius = w * 0.45f
                val ringThickness = w * 0.085f

                // Deep gold outer shadow rim
                drawCircle(
                    color = Color(0xFF6B4500),
                    radius = ringRadius + ringThickness * 0.5f,
                    center = Offset(cx, cy),
                    style = Stroke(width = ringThickness * 0.4f)
                )

                // Polished gold main ring band with dynamic shine gradient
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFEA00),
                            Color(0xFFFFF9C4),
                            Color(0xFFF59E0B),
                            Color(0xFFB45309),
                            Color(0xFFFFF59D),
                            Color(0xFFFFEA00)
                        ),
                        center = Offset(cx, cy)
                    ),
                    radius = ringRadius,
                    center = Offset(cx, cy),
                    style = Stroke(width = ringThickness)
                )

                // Inner crisp golden rim
                drawCircle(
                    color = Color(0xFFFFFDE7).copy(alpha = 0.85f),
                    radius = ringRadius - ringThickness * 0.48f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.8f)
                )
            }
        }
    }
}

enum class MascotMood {
    JOYFUL, HAPPY, NEUTRAL, SAD
}

/**
 * Cute Floating Cloud Decoration
 */
@Composable
fun FloatingCloud(
    modifier: Modifier = Modifier,
    alpha: Float = 0.85f
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = alpha))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "☁️", fontSize = 16.sp)
    }
}

/**
 * Animated High-Energy 3-2-1-GO Resume Countdown Overlay
 */
@Composable
fun ResumeCountdownOverlay(
    count: Int,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.3f) }

    LaunchedEffect(count) {
        scale.snapTo(0.4f)
        scale.animateTo(
            targetValue = 1.15f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    val (colors, text) = when (count) {
        3 -> listOf(Color(0xFF00E5FF), Color(0xFF0284C7)) to "3"
        2 -> listOf(Color(0xFFFFB703), Color(0xFFFB8500)) to "2"
        1 -> listOf(Color(0xFFFF2A75), Color(0xFFE11D48)) to "1"
        else -> listOf(Color(0xFF22C55E), Color(0xFF15803D)) to "GO! 🎈"
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x880A0A14))
            .clickable(interactionSource = interactionSource, indication = null) { /* intercept clicks during countdown */ },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161626))
                    .border(1.5.dp, Color(0xFF2E2E48), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "RESUMING GAME...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00F0FF),
                    letterSpacing = 2.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(if (count > 0) 110.dp else 140.dp)
                    .shadow(20.dp, CircleShape, spotColor = colors.first())
                    .clip(CircleShape)
                    .background(Brush.radialGradient(colors))
                    .border(3.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = if (count > 0) 56.sp else 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Vibrant Stage Splash Overlay for New Stage / Next Level Transition
 */
@Composable
fun StageIntroSplash(
    level: Int,
    targetPops: Int,
    targetScore: Int,
    timeLimit: Int,
    maxMissesAllowed: Int = 4,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xAA0A0518))
            .clickable { onStart() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFFFD700))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF181426),
                            Color(0xFF100E1C),
                            Color(0xFF08070F)
                        )
                    )
                )
                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mascot Cheering
            MascotCharacter(mood = MascotMood.JOYFUL, size = 68.dp)

            // Header Banner
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF382405))
                    .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (level > 0) "✦ STAGE $level ✦" else "✦ CHALLENGE ✦",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE68A),
                    letterSpacing = 1.5.sp
                )
            }

            // Goal Details Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF161628))
                    .border(1.5.dp, Color(0xFF28283E), RoundedCornerShape(18.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🎈", fontSize = 18.sp)
                    Text(
                        text = "POP $targetPops BALLOONS TO WIN!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏱️ Time: ${timeLimit}s",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "💨 Max Misses: $maxMissesAllowed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFCA5A5)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33EF4444))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ Miss > $maxMissesAllowed balloons = Game Over!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFCA5A5),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Action Button
            JellyButton(
                text = "LET'S POP! ▶",
                onClick = onStart,
                height = 54.dp,
                fontSize = 18,
                colorVariant = JellyColorVariant.GREEN,
                testTag = "stage_splash_start_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
            )
        }
    }
}
