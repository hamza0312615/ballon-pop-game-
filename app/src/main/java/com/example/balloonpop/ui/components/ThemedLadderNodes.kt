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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.ZoneVisualTheme

/**
 * 3D Themed Stage Node (Cleared / Locked) matching reference level design:
 * - Cleared stages: 3D circular pedestal with floating golden stars and stage number tag
 * - Locked stages: Dark circular stone disc with metallic lock icon and stage number
 */
@Composable
fun ThemedLadderTileNode(
    theme: ZoneVisualTheme,
    stageNumber: Int,
    stars: Int,
    isUnlocked: Boolean,
    isCleared: Boolean,
    isMilestone: Boolean,
    isBoss: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "node_press"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "star_float")
    val starBob by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_bob"
    )

    val zoneName = theme.name.lowercase()
    val isSakura = zoneName.contains("sakura")
    val isFrozen = zoneName.contains("frozen") || zoneName.contains("tundra")
    val isVolcano = zoneName.contains("volcano") || zoneName.contains("molten") || zoneName.contains("caldera")
    val isNeon = zoneName.contains("neon") || zoneName.contains("glade")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isUnlocked
            ) { onClick() }
    ) {
        if (isCleared) {
            // 1. Floating Golden Stars above the Cleared Pedestal
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset(y = starBob.dp)
                    .padding(bottom = 2.dp)
            ) {
                val earnedStars = if (stars > 0) stars else 3
                repeat(earnedStars) { starIdx ->
                    val starScale = if (starIdx == 1) 1.15f else 0.95f
                    Text(
                        text = "⭐",
                        fontSize = (13 * starScale).sp,
                        modifier = Modifier.shadow(4.dp, CircleShape, spotColor = Color(0xFFFFD700))
                    )
                }
            }

            // 2. 3D Themed Circular / Oval Pedestal Disc
            Box(
                modifier = Modifier
                    .size(width = 62.dp, height = 46.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(23.dp),
                        spotColor = theme.primaryAccent.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(23.dp))
                    .background(
                        when {
                            isSakura -> Brush.verticalGradient(
                                listOf(Color(0xFFFFF0F5), Color(0xFFFBCFE8), Color(0xFFF472B6))
                            )
                            isFrozen -> Brush.verticalGradient(
                                listOf(Color(0xFFE0F2FE), Color(0xFF7DD3FC), Color(0xFF0284C7))
                            )
                            isVolcano -> Brush.verticalGradient(
                                listOf(Color(0xFF26262B), Color(0xFF19191E), Color(0xFF0F0F14))
                            )
                            isNeon -> Brush.verticalGradient(
                                listOf(Color(0xFFA7F3D0), Color(0xFF34D399), Color(0xFF059669))
                            )
                            else -> Brush.verticalGradient(theme.clearedBgColors)
                        }
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            when {
                                isSakura -> listOf(Color.White, Color(0xFFF472B6))
                                isFrozen -> listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8))
                                isVolcano -> listOf(Color(0xFFFF7A00), Color(0xFF991B1B))
                                isNeon -> listOf(Color(0xFFD1FAE5), Color(0xFF10B981))
                                else -> listOf(Color.White, theme.primaryAccent)
                            }
                        ),
                        shape = RoundedCornerShape(23.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Top Gloss Reflection Arc for 3D glassy look
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                )

                // Volcano Lava Veins on Pedestal Surface
                if (isVolcano) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.4f)
                            lineTo(size.width * 0.45f, size.height * 0.6f)
                            lineTo(size.width * 0.8f, size.height * 0.35f)
                        }
                        drawPath(path, color = Color(0xFFFF7A00), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                        drawPath(path, color = Color(0xFFFEF08A), style = Stroke(width = 1f, cap = StrokeCap.Round))
                    }
                }
            }

            // 3. Stage Number Pill Tag beneath the Pedestal
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xD90D1322))
                    .border(
                        1.dp,
                        theme.primaryAccent.copy(alpha = 0.6f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stageNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        } else {
            // --- LOCKED STAGE NODE ---
            // Sleek circular dark frosted stone disc with metallic lock icon (🔒)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(4.dp, CircleShape, spotColor = theme.primaryAccent.copy(alpha = 0.35f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF2E3440),
                                Color(0xFF1E222B),
                                theme.primaryAccent.copy(alpha = 0.22f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0x66FFFFFF),
                                theme.primaryAccent.copy(alpha = 0.45f),
                                Color(0x22FFFFFF)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Subtle top specular crescent
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.8f)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                            )
                        )
                )

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Locked Stage Number Tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xB30B101A))
                    .padding(horizontal = 7.dp, vertical = 1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stageNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * 3D Active / Current Hero Level Node matching reference screenshots:
 * - Elevated 3D Golden Disc Pedestal with concentric expanding ripple rings
 * - Waving golden/red victory flag planted on top
 * - Bold high-contrast stage number on the golden disc
 * - Radiant "NOW" / "CURRENT" golden banner pill underneath
 */
@Composable
fun ThemedLadderActiveHeroNode(
    theme: ZoneVisualTheme,
    stageNumber: Int,
    stars: Int,
    mascotBob: Float,
    beaconPulse: Float,
    isMilestone: Boolean,
    isBoss: Boolean,
    onClick: () -> Unit
) {
    val heroTransition = rememberInfiniteTransition(label = "hero_active_pulse")
    val flagSway by heroTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flag_sway"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("active_hero_level_tile")
    ) {
        // 1. Slender Golden Flagpole with Waving Victory Flag
        Box(
            modifier = Modifier
                .height(34.dp)
                .width(42.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val poleX = size.width * 0.4f
                val poleBottom = size.height
                val poleTop = 6f

                // Golden Pole
                drawLine(
                    color = Color(0xFFFFD700),
                    start = Offset(poleX, poleBottom),
                    end = Offset(poleX, poleTop),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                // Pole Finial Ball
                drawCircle(
                    color = Color(0xFFFFF9C4),
                    radius = 3.5f,
                    center = Offset(poleX, poleTop)
                )

                // Waving Victory Flag (Red or Gold)
                val flagWidth = 24f
                val flagHeight = 15f
                val flagSwayPx = flagSway * 1.5f

                val flagPath = Path().apply {
                    moveTo(poleX, poleTop + 2f)
                    cubicTo(
                        poleX + flagWidth * 0.5f, poleTop + 2f + flagSwayPx,
                        poleX + flagWidth * 0.8f, poleTop + 2f - flagSwayPx,
                        poleX + flagWidth, poleTop + 5f
                    )
                    lineTo(poleX + flagWidth, poleTop + 5f + flagHeight)
                    cubicTo(
                        poleX + flagWidth * 0.8f, poleTop + 5f + flagHeight - flagSwayPx,
                        poleX + flagWidth * 0.5f, poleTop + 5f + flagHeight + flagSwayPx,
                        poleX, poleTop + 2f + flagHeight
                    )
                    close()
                }
                drawPath(
                    path = flagPath,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFEF4444), Color(0xFFDC2626), Color(0xFFB91C1C))
                    )
                )
            }
        }

        // 2. 3D Radiant Golden Disc Pedestal with Concentric Ripple Rings
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            // Concentric expanding glowing aura rings
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(beaconPulse)
                    .clip(CircleShape)
                    .background(Color(0x28FFD700))
                    .border(2.dp, Color(0x66FFD700), CircleShape)
            )

            // Inner Ripple Halo
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .scale(beaconPulse * 0.85f)
                    .clip(CircleShape)
                    .background(Color(0x30FFA000))
            )

            // Main 3D Elevated Golden Disc
            Box(
                modifier = Modifier
                    .size(width = 66.dp, height = 52.dp)
                    .shadow(14.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFFFB703))
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFDE047),
                                Color(0xFFF59E0B),
                                Color(0xFFB45309)
                            )
                        )
                    )
                    .border(
                        width = 2.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFE082), Color(0xFF92400E))
                        ),
                        shape = RoundedCornerShape(26.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Top Specular Highlight Arc
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.85f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.80f), Color.Transparent)
                            )
                        )
                )

                // High-Contrast Bold Stage Number in center
                Text(
                    text = "$stageNumber",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF451A03),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 3. Golden "NOW" / "CURRENT" Capsule Badge
        Box(
            modifier = Modifier
                .offset(y = (-6).dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFFFD700))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFBEB), Color(0xFFFEF08A), Color(0xFFF59E0B))
                    )
                )
                .border(1.5.dp, Color(0xFFFFF9C4), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NOW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF451A03),
                letterSpacing = 0.5.sp
            )
        }
    }
}
