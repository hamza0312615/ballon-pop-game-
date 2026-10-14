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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium Glassmorphism Button with frosted background, gradient glass borders,
 * tactile spring press scale, and glowing drop shadow.
 */
@Composable
fun GlassmorphismButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: String? = null,
    badge: String? = null,
    glowColor: Color = Color(0xFF00E5FF),
    bgGradient: List<Color> = listOf(Color(0x33FFFFFF), Color(0x10FFFFFF)),
    borderGradient: List<Color> = listOf(Color(0x80FFFFFF), Color(0x20FFFFFF)),
    textColor: Color = Color.White,
    fontSize: Int = 13,
    height: Dp = 48.dp,
    enabled: Boolean = true,
    testTag: String = "glass_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "glass_press_scale"
    )

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (enabled) glowColor else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    if (enabled) bgGradient else listOf(Color(0x15FFFFFF), Color(0x0AFFFFFF))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    if (enabled) borderGradient else listOf(Color(0x25FFFFFF), Color(0x10FFFFFF))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    fontSize = (fontSize + 4).sp,
                    modifier = Modifier.padding(end = 6.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Black,
                    color = if (enabled) textColor else Color(0xFF64748B),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 0.5.sp
                )

                if (badge != null) {
                    Text(
                        text = badge,
                        fontSize = (fontSize - 3).coerceAtLeast(8).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = glowColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

/**
 * Skill-Morphism Button for Equip / Cosmic / Superpower actions.
 * Features a cosmic nebula gradient, glowing animated iridescent border, and tactile depth.
 */
@Composable
fun SkillMorphismButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEquipped: Boolean = false,
    isCosmic: Boolean = false,
    costGems: Int? = null,
    costCoins: Int? = null,
    enabled: Boolean = true,
    height: Dp = 42.dp,
    testTag: String = "skill_morphism_btn"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "skill_btn_scale"
    )

    val bgGradient = when {
        isEquipped -> listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF059669))
        isCosmic -> listOf(Color(0xFF4C1D95), Color(0xFF7C3AED), Color(0xFFC026D3))
        costGems != null && costGems > 0 -> listOf(Color(0xFF3B0764), Color(0xFF6B21A8), Color(0xFF9333EA))
        costCoins != null && costCoins > 0 -> listOf(Color(0xFF78350F), Color(0xFFB45309), Color(0xFFD97706))
        else -> listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF38BDF8))
    }

    val glowColor = when {
        isEquipped -> Color(0xFF10B981)
        isCosmic -> Color(0xFFE879F9)
        costGems != null && costGems > 0 -> Color(0xFFA855F7)
        costCoins != null && costCoins > 0 -> Color(0xFFFBBF24)
        else -> Color(0xFF38BDF8)
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 1.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = glowColor
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    if (enabled) bgGradient else listOf(Color(0xFF1E1E2C), Color(0xFF141422))
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.8f),
                        glowColor,
                        Color.White.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isEquipped) {
                Text(text = "✓ ", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Text(
                text = text,
                color = if (enabled) Color.White else Color(0xFF64748B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Claim Morphism Button for Quest rewards, Daily Spin, and Milestone Chests.
 * Radiant pulsating gold/green celebratory badge with shimmer effect.
 */
@Composable
fun ClaimMorphismButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isClaimed: Boolean = false,
    enabled: Boolean = true,
    testTag: String = "claim_morphism_btn"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "claim_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (enabled && !isClaimed) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .scale(pulseScale)
            .shadow(
                elevation = if (enabled && !isClaimed) 10.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = Color(0xFF10B981)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    isClaimed -> Brush.verticalGradient(listOf(Color(0xFF1A1A24), Color(0xFF12121A)))
                    enabled -> Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)))
                    else -> Brush.verticalGradient(listOf(Color(0xFF262638), Color(0xFF1A1A28)))
                }
            )
            .border(
                width = 1.5.dp,
                color = when {
                    isClaimed -> Color(0xFF2F2F45)
                    enabled -> Color(0xFFA7F3D0)
                    else -> Color(0xFF383852)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled && !isClaimed) { onClick() }
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = when {
                isClaimed -> Color(0xFF64748B)
                enabled -> Color.White
                else -> Color(0xFF94A3B8)
            },
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Responsive Morphism Pill Tab.
 * Never breaks words or drops letters (like "Hammer" and "s") across screens.
 * Uses softWrap = false, text maxLines = 1, and generous touch target.
 */
@Composable
fun MorphismPillTab(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF00E5FF),
    activeGradient: List<Color> = listOf(Color(0xFF0C2B40), Color(0xFF071926)),
    testTag: String = "tab"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
        label = "pill_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 6.dp else 1.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = activeColor
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Brush.verticalGradient(activeGradient)
                else Brush.verticalGradient(listOf(Color(0xFF141424), Color(0xFF0D0D18)))
            )
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = if (isSelected) activeColor else Color(0xFF26263B),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 16.sp)
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Level Dive Cinematic Animation Overlay.
 * Triggers a hyper-speed zoom into the chosen stage portal with speed lines,
 * sound-synced countdown, and smooth stage start!
 */
@Composable
fun LevelDiveOverlay(
    level: Int,
    zoneName: String = "Neon Glade",
    onDiveFinished: () -> Unit
) {
    var diveProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val duration = 850L
        while (System.currentTimeMillis() - startTime < duration) {
            diveProgress = ((System.currentTimeMillis() - startTime).toFloat() / duration).coerceIn(0f, 1f)
            delay(16L)
        }
        diveProgress = 1f
        onDiveFinished()
    }

    val cameraScale = 1.0f + diveProgress * 2.8f
    val alphaFade = (1.0f - (diveProgress - 0.7f).coerceAtLeast(0f) / 0.3f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE605050C))
            .clickable { onDiveFinished() } // Tap to skip
            .testTag("level_dive_overlay"),
        contentAlignment = Alignment.Center
    ) {
        // Hyperspace Speed Lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.width.coerceAtLeast(size.height)
            val rayCount = 28

            for (i in 0 until rayCount) {
                val angle = (i.toFloat() / rayCount) * (2f * PI.toFloat())
                val innerR = 40f + (diveProgress * maxR * 0.4f)
                val outerR = maxR * (0.3f + diveProgress * 0.8f)

                val start = Offset(
                    center.x + cos(angle) * innerR,
                    center.y + sin(angle) * innerR
                )
                val end = Offset(
                    center.x + cos(angle) * outerR,
                    center.y + sin(angle) * outerR
                )

                drawLine(
                    color = if (i % 2 == 0) Color(0x6600E5FF) else Color(0x66FFD700),
                    start = start,
                    end = end,
                    strokeWidth = 3f + diveProgress * 6f
                )
            }
        }

        // Diving Portal Ring
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .scale(cameraScale)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(24.dp, CircleShape, spotColor = Color(0xFFFFD700))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFEA00), Color(0xFFF59E0B), Color(0xFFB45309))
                        )
                    )
                    .border(3.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$level",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Text(
                text = "DIVING INTO STAGE $level",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = zoneName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
        }
    }
}
