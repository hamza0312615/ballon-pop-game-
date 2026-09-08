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
            Color(0xFF4ADE80),
            Color(0xFF16A34A),
            Color(0xFF86EFAC),
            Color(0xFF14532D),
            Color.White
        )
        JellyColorVariant.ORANGE -> listOf(
            Color(0xFFFBBF24),
            Color(0xFFD97706),
            Color(0xFFFDE68A),
            Color(0xFF78350F),
            Color.White
        )
        JellyColorVariant.BLUE -> listOf(
            Color(0xFF38BDF8),
            Color(0xFF0284C7),
            Color(0xFFBAE6FD),
            Color(0xFF0369A1),
            Color.White
        )
        JellyColorVariant.PURPLE -> listOf(
            Color(0xFFC084FC),
            Color(0xFF7E22CE),
            Color(0xFFE9D5FF),
            Color(0xFF581C87),
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
    GREEN, ORANGE, BLUE, PURPLE
}

/**
 * Casual Game Amber Wooden Framed Modal Dialog Container
 */
@Composable
fun AmberDialogFrame(
    title: String,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x990A0518))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 380.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF59E0B), // Warm amber top
                            Color(0xFFD97706),
                            Color(0xFFB45309)
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    color = Color(0xFFFEF3C7),
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
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(34.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                )
                            )
                            .border(2.dp, Color(0xFFFCA5A5), CircleShape)
                            .clickable { onClose() }
                            .testTag("dialog_close_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Inner Parchment Body Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFBEB), // Clean parchment
                                Color(0xFFFEF3C7)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(20.dp))
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
                Text(
                    text = if (isEarned) "⭐" else "☆",
                    fontSize = (size.value * 0.52f).sp
                )
            }
        }
    }
}

/**
 * Mascot Character "Popster" Composable (Joyful, Neutral, Sad)
 */
@Composable
fun MascotCharacter(
    mood: MascotMood = MascotMood.JOYFUL,
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_bob")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = modifier
            .offset(y = if (mood == MascotMood.JOYFUL) bounceY.dp else 0.dp)
            .size(size)
            .shadow(6.dp, CircleShape, spotColor = Color(0xFFF59E0B))
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFDE047), // Sunny star yellow
                        Color(0xFFF59E0B),
                        Color(0xFFD97706)
                    )
                )
            )
            .border(2.5.dp, Color(0xFFFFFBEB), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val emoji = when (mood) {
            MascotMood.JOYFUL -> "🥳"
            MascotMood.HAPPY -> "🌟"
            MascotMood.NEUTRAL -> "🎈"
            MascotMood.SAD -> "🥺"
        }
        Text(text = emoji, fontSize = (size.value * 0.55f).sp)
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
