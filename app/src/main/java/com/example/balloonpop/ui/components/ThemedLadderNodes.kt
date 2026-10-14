package com.example.balloonpop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.ZoneNodeShapeType
import com.example.balloonpop.ui.theme.ZoneVisualTheme

/**
 * Themed Stage Node (Cleared / Locked / Standard) tailored to the active zone's environment.
 * Features 3D raised elevation, inner highlight ring, idle breathing animation, and high-contrast star capsules.
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
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "node_press"
    )

    // Subtle idle breathing pulse animation for the active/next available stage
    val isNextAvailable = isUnlocked && !isCleared
    val infiniteTransition = rememberInfiniteTransition(label = "node_idle_breathe")
    val idleBreathe by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isNextAvailable) 1.07f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breathe"
    )

    val outerShape: Shape = when (theme.nodeShape) {
        ZoneNodeShapeType.ORGANIC_SEED_POD -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 14.dp,
            bottomEnd = 14.dp
        )
        ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> CutCornerShape(14.dp)
        ZoneNodeShapeType.SUNBURST_TALISMAN -> RoundedCornerShape(16.dp)
        ZoneNodeShapeType.PEARL_SHELL -> RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(15.dp)
    }

    val innerShape: Shape = when (theme.nodeShape) {
        ZoneNodeShapeType.ORGANIC_SEED_POD -> RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 11.dp,
            bottomEnd = 11.dp
        )
        ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> CutCornerShape(11.dp)
        ZoneNodeShapeType.SUNBURST_TALISMAN -> RoundedCornerShape(13.dp)
        ZoneNodeShapeType.PEARL_SHELL -> RoundedCornerShape(
            topStart = 15.dp,
            topEnd = 15.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )
        else -> RoundedCornerShape(12.dp)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .scale(pressScale * idleBreathe)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isUnlocked
            ) { onClick() }
    ) {
        // Milestone or Boss Floating Crest
        if (isMilestone) {
            val milestoneIcon = when (theme.nodeShape) {
                ZoneNodeShapeType.ORGANIC_SEED_POD -> "🌸" // Blossom pod
                ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> "🔮" // Prismatic geode
                ZoneNodeShapeType.PEARL_SHELL -> "🪸"
                else -> "🎁"
            }
            Text(text = milestoneIcon, fontSize = 16.sp, modifier = Modifier.offset(y = 2.dp))
        } else if (isBoss) {
            val bossIcon = when (theme.nodeShape) {
                ZoneNodeShapeType.ORGANIC_SEED_POD -> "🐲" // Ancient Forest Dragon
                ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> "👑" // Crystal Wyrm Monolith
                ZoneNodeShapeType.PEARL_SHELL -> "🐙"
                else -> "👑"
            }
            Text(text = bossIcon, fontSize = 16.sp, modifier = Modifier.offset(y = 2.dp))
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(contentAlignment = Alignment.Center) {
            // Pulsing idle beacon ring for the next available/active unlocked level
            if (isNextAvailable) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .scale(idleBreathe)
                        .clip(outerShape)
                        .background(theme.primaryAccent.copy(alpha = 0.22f))
                        .border(
                            1.5.dp,
                            theme.primaryAccent.copy(alpha = 0.6f),
                            outerShape
                        )
                )
            }

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .alpha(if (isCleared) 0.88f else 1.0f)
                    .shadow(
                        elevation = if (isUnlocked) 10.dp else 2.dp,
                        shape = outerShape,
                        spotColor = when {
                            isCleared -> theme.clearedBorderColor
                            isBoss -> Color(0xFFDC2626)
                            isMilestone -> theme.primaryAccent
                            isUnlocked -> theme.activeGlowColor
                            else -> Color.Transparent
                        }
                    )
                    .clip(outerShape)
                    // Outer thematic border foundation
                    .background(
                        Brush.verticalGradient(
                            when {
                                isCleared -> listOf(theme.clearedBorderColor, theme.clearedBgColors.first(), Color(0xFF0F172A))
                                isBoss && isUnlocked -> listOf(Color(0xFFFECACA), Color(0xFFDC2626), Color(0xFF450A0A))
                                isMilestone && isUnlocked -> listOf(Color.White, theme.primaryAccent, Color(0xFF1E1B4B))
                                isUnlocked -> listOf(theme.unlockedBorderColor, theme.primaryAccent, Color(0xFF0F172A))
                                else -> listOf(theme.lockedBorderColor, theme.lockedBgColors.first())
                            }
                        )
                    )
                    .border(
                        width = if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD) 2.dp else 1.5.dp,
                        brush = Brush.verticalGradient(
                            if (isUnlocked) listOf(Color.White, theme.primaryAccent) else listOf(theme.lockedBorderColor, Color(0x33000000))
                        ),
                        shape = outerShape
                    )
                    .padding(3.dp)
                    .clip(innerShape)
                    // Inner 3D jewel/stone plate
                    .background(
                        when {
                            isCleared -> Brush.verticalGradient(theme.clearedBgColors)
                            isBoss && isUnlocked -> Brush.verticalGradient(
                                listOf(Color(0xFFF87171), Color(0xFFDC2626), Color(0xFF991B1B))
                            )
                            isMilestone && isUnlocked -> Brush.verticalGradient(
                                listOf(Color(0xFFE9D5FF), theme.primaryAccent, theme.secondaryAccent)
                            )
                            isUnlocked -> Brush.verticalGradient(theme.unlockedBgColors)
                            else -> Brush.verticalGradient(theme.lockedBgColors)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 3D Top Inner Gloss Crescent Highlight
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.85f)
                        .height(14.dp)
                        .clip(innerShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x70FFFFFF), Color(0x00FFFFFF))
                            )
                        )
                )

                // Inner Highlight Ring for raised 3D look
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                if (isUnlocked) listOf(Color(0x66FFFFFF), Color(0x10FFFFFF))
                                else listOf(Color(0x22FFFFFF), Color.Transparent)
                            ),
                            shape = innerShape
                        )
                )

                if (isUnlocked) {
                    Text(
                        text = "$stageNumber",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Thematic locked glyph (dormant seedling, geode nucleus, shell) - NEVER a generic lock emoji
                    Text(
                        text = theme.lockedIconSymbol,
                        fontSize = 16.sp,
                        modifier = Modifier.scale(0.95f)
                    )
                }
            }

            // Satisfying clean "checked/cleared" indicator badge at top-end
            if (isCleared) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(16.dp)
                        .shadow(4.dp, CircleShape, spotColor = Color(0xFF10B981))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF34D399), Color(0xFF059669))
                            )
                        )
                        .border(1.2.dp, Color(0xFFA7F3D0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.offset(y = (-0.5).dp)
                    )
                }
            }
        }

        // Stars underneath tile in high-contrast dark capsule (★★★)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xD9080E18))
                .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) { starIndex ->
                    val hasStar = starIndex < stars
                    val starColor = if (hasStar) Color(0xFFFFD700) else Color(0xFF334155)
                    Text(
                        text = if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD && hasStar) "✦" else "★",
                        fontSize = 12.sp,
                        color = starColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

/**
 * Active Hero Node where the player currently resides on the ladder.
 * Dynamically tailored to the active zone's theme:
 * - Neon Glade: Bioluminescent sacred lotus dais with pollen pulse rings and leaf motes
 * - Prism Peaks: Floating crystalline monolith with orbital prism shards and refraction beams
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
    val outerShape: Shape = when (theme.nodeShape) {
        ZoneNodeShapeType.ORGANIC_SEED_POD -> RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )
        ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> CutCornerShape(18.dp)
        ZoneNodeShapeType.SUNBURST_TALISMAN -> RoundedCornerShape(20.dp)
        else -> RoundedCornerShape(18.dp)
    }

    val innerShape: Shape = when (theme.nodeShape) {
        ZoneNodeShapeType.ORGANIC_SEED_POD -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 13.dp,
            bottomEnd = 13.dp
        )
        ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> CutCornerShape(14.dp)
        ZoneNodeShapeType.SUNBURST_TALISMAN -> RoundedCornerShape(16.dp)
        else -> RoundedCornerShape(14.dp)
    }

    val heroInfiniteTransition = rememberInfiniteTransition(label = "hero_idle_pulse")
    val heroBreathe by heroInfiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_breathe"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("active_hero_level_tile")
    ) {
        // Mascot & Floating Theme Elements
        Box(
            modifier = Modifier
                .offset(y = mascotBob.dp)
                .size(68.dp),
            contentAlignment = Alignment.Center
        ) {
            // Themed Beacon Pulse Ring behind mascot
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .scale(beaconPulse)
                    .clip(if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD) CutCornerShape(18.dp) else CircleShape)
                    .background(theme.primaryAccent.copy(alpha = 0.25f))
                    .border(
                        1.5.dp,
                        theme.primaryAccent.copy(alpha = 0.55f),
                        if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD) CutCornerShape(18.dp) else CircleShape
                    )
            )

            // Mascot Face Dais with 3D Radial Highlight
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(12.dp, CircleShape, spotColor = theme.activeGlowColor)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFA000))
                        )
                    )
                    .border(2.5.dp, Color(0xFFFFF9C4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBoss) "👑" else "😘",
                    fontSize = 32.sp
                )
            }

            // Floating Theme Particle Accents beside mascot
            val leftParticle = when (theme.nodeShape) {
                ZoneNodeShapeType.ORGANIC_SEED_POD -> "🌿"
                ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> "💎"
                else -> "✨"
            }
            val rightParticle = if (isBoss) "🔥" else "❤️"

            Text(
                text = rightParticle,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-6).dp)
            )

            Text(
                text = leftParticle,
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-4).dp, y = (-2).dp)
            )
        }

        // Themed Active Platform (Sacred Seed Pod / Levitating Crystal Dais)
        Box(
            modifier = Modifier
                .size(64.dp)
                .scale(heroBreathe)
                .shadow(16.dp, outerShape, spotColor = theme.activeGlowColor)
                .clip(outerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White, theme.primaryAccent, theme.secondaryAccent)
                    )
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White, theme.primaryAccent, Color(0xFF1E1B4B))
                    ),
                    shape = outerShape
                )
                .padding(4.dp)
                .clip(innerShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFE066),
                            Color(0xFFFFB703),
                            theme.primaryAccent,
                            theme.secondaryAccent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$stageNumber",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            // Special Milestone / Boss Ribbon Badge
            if (isMilestone) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF7C3AED))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (theme.nodeShape == ZoneNodeShapeType.ORGANIC_SEED_POD) "BLOSSOM" else "GEODE",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else if (isBoss) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDC2626))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(text = "SUMMIT", fontSize = 7.5.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }

        // 3 Big Shiny Gold Stars underneath
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) { starIdx ->
                val hasStar = starIdx < stars || stars == 0
                Text(
                    text = if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD && hasStar) "✦" else "★",
                    fontSize = 15.sp,
                    color = if (hasStar) Color(0xFFFFD700) else Color(0xFF334155),
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
