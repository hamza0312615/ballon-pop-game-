package com.example.balloonpop.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameMode
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily

/**
 * Endless Modes Screen.
 * Exact implementation of the reference design (Image 2):
 * - Top Currency HUD: 3 glass capsules for Hearts, Coins, and Gems
 * - Centered bold "ENDLESS MODES" title + "Survive endless waves — set record high scores"
 * - 4 High-end glowing mode cards:
 *   1. Classic Survival (Emerald theme with bullseye icon, "SURVIVAL" tag & checkmark)
 *   2. Time Rush (Golden amber with stopwatch & "BLITZ" tag)
 *   3. Arcade Frenzy (Fire flame red-orange & "CHAOS" tag)
 *   4. Zen Popping (Calm teal lotus & "ZEN" tag)
 * - Large Bottom Hero Pill Button ("START [MODE] - tap to enter the arena")
 * - Deep cosmic space background with floating balloon silhouettes and soft light rays
 */
@Composable
fun EndlessModeScreen(
    onStartMode: (GameMode) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    lives: Int = 124,
    coins: Int = 8540,
    gems: Int = 320
) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }

    val infiniteTransition = rememberInfiniteTransition(label = "endless_ambient")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Deep cosmic space gradient background
    val cosmicGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF0B1026),
            Color(0xFF140F2D),
            Color(0xFF1B1035),
            Color(0xFF120A24),
            Color(0xFF090616)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cosmicGradient)
            .testTag("endless_modes_screen")
    ) {
        // 1. Ambient Background: Atmospheric Light Streaks & Faint Balloon Silhouettes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft diagonal light beams
            val beamPath = Path().apply {
                moveTo(w * 0.10f, 0f)
                lineTo(w * 0.45f, 0f)
                lineTo(w * 0.05f, h * 0.55f)
                lineTo(0f, h * 0.40f)
                close()
            }
            drawPath(
                path = beamPath,
                color = Color(0x12A78BFA)
            )

            // Faint floating balloon outlines at ~6% opacity
            val ambientBalloons = listOf(
                Offset(w * 0.08f, h * 0.22f) to 26f,
                Offset(w * 0.92f, h * 0.16f) to 32f,
                Offset(w * 0.06f, h * 0.56f) to 28f,
                Offset(w * 0.94f, h * 0.44f) to 24f,
                Offset(w * 0.90f, h * 0.70f) to 34f,
                Offset(w * 0.12f, h * 0.85f) to 22f
            )

            for ((pos, r) in ambientBalloons) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = r,
                    center = pos
                )
                // String
                drawLine(
                    color = Color.White.copy(alpha = 0.035f),
                    start = Offset(pos.x, pos.y + r),
                    end = Offset(pos.x - 4f, pos.y + r + 24f),
                    strokeWidth = 1.2f
                )
            }
        }

        // 2. Main Foreground Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Back Button + Currency Capsules + Titles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Back Button & 3 Glass Resource Capsules
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable { onClose() }
                            .testTag("endless_back_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // 3 Glass Resource Capsules (Hearts, Coins, Gems)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hearts Capsule
                        EndlessGlassResourceCapsule(
                            icon = "❤️",
                            amount = lives,
                            iconColor = Color(0xFFEF4444)
                        )

                        // Coins Capsule
                        EndlessGlassResourceCapsule(
                            icon = "🪙",
                            amount = coins,
                            iconColor = Color(0xFFFFC93C)
                        )

                        // Gems Capsule
                        EndlessGlassResourceCapsule(
                            icon = "💎",
                            amount = gems,
                            iconColor = Color(0xFFA855F7)
                        )
                    }
                }

                // Title & Subtitle Lockup
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ENDLESS MODES",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = "Survive endless waves — set record high scores",
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC4B5FD),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Middle Section: 4 Mode Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Classic Survival
                EndlessModeCard(
                    title = "Classic Survival",
                    subtitle = "3 Lives • Increasing Speed",
                    icon = "🎯",
                    badgeText = "SURVIVAL",
                    accentColor = Color(0xFF10B981),
                    badgeBgColor = Color(0xFF064E3B),
                    badgeTextColor = Color(0xFF34D399),
                    isSelected = selectedMode == GameMode.CLASSIC,
                    glowPulse = glowPulse,
                    onClick = { selectedMode = GameMode.CLASSIC },
                    testTag = "mode_card_classic"
                )

                // Card 2: Time Rush
                EndlessModeCard(
                    title = "Time Rush",
                    subtitle = "60 Seconds • Fast Spawns",
                    icon = "⏱️",
                    badgeText = "BLITZ",
                    accentColor = Color(0xFFF59E0B),
                    badgeBgColor = Color(0xFF78350F),
                    badgeTextColor = Color(0xFFFDE68A),
                    isSelected = selectedMode == GameMode.TIME_RUSH,
                    glowPulse = glowPulse,
                    onClick = { selectedMode = GameMode.TIME_RUSH },
                    testTag = "mode_card_time_rush"
                )

                // Card 3: Arcade Frenzy
                EndlessModeCard(
                    title = "Arcade Frenzy",
                    subtitle = "Power-ups & Bomb Waves",
                    icon = "🔥",
                    badgeText = "CHAOS",
                    accentColor = Color(0xFFF97316),
                    badgeBgColor = Color(0xFF7C2D12),
                    badgeTextColor = Color(0xFFFDBA74),
                    isSelected = selectedMode == GameMode.ARCADE,
                    glowPulse = glowPulse,
                    onClick = { selectedMode = GameMode.ARCADE },
                    testTag = "mode_card_arcade"
                )

                // Card 4: Zen Popping
                EndlessModeCard(
                    title = "Zen Popping",
                    subtitle = "No Bombs • Relaxing Ambient",
                    icon = "🪷",
                    badgeText = "ZEN",
                    accentColor = Color(0xFF2DD4BF),
                    badgeBgColor = Color(0xFF134E4A),
                    badgeTextColor = Color(0xFF99F6E4),
                    isSelected = selectedMode == GameMode.ZEN,
                    glowPulse = glowPulse,
                    onClick = { selectedMode = GameMode.ZEN },
                    testTag = "mode_card_zen"
                )
            }

            // Bottom Section: Big Hero Pill Button
            val (heroGradient, heroLabel) = when (selectedMode) {
                GameMode.CLASSIC -> listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)) to "START CLASSIC"
                GameMode.TIME_RUSH -> listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706)) to "START TIME RUSH"
                GameMode.ARCADE -> listOf(Color(0xFFFB923C), Color(0xFFF97316), Color(0xFFDC2626)) to "START ARCADE"
                GameMode.ZEN -> listOf(Color(0xFF5EEAD4), Color(0xFF2DD4BF), Color(0xFF0F766E)) to "START ZEN"
                else -> listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669)) to "START GAME"
            }

            val buttonInteraction = remember { MutableInteractionSource() }
            val isPressed by buttonInteraction.collectIsPressedAsState()
            val buttonScale by animateFloatAsState(
                targetValue = if (isPressed) 0.96f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "hero_button_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .scale(buttonScale)
                    .shadow(18.dp, RoundedCornerShape(28.dp), spotColor = heroGradient[1])
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.verticalGradient(heroGradient))
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.65f), Color.White.copy(alpha = 0.15f))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .clickable(
                        interactionSource = buttonInteraction,
                        indication = null,
                        onClick = { onStartMode(selectedMode) }
                    )
                    .testTag("endless_start_button"),
                contentAlignment = Alignment.Center
            ) {
                // Top shine highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.40f), Color.Transparent)
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = heroLabel,
                        fontFamily = Baloo2FontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "tap to enter the arena",
                        fontFamily = NunitoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }
    }
}

/**
 * Top Glass Resource Capsule for Hearts, Coins, and Gems.
 */
@Composable
private fun EndlessGlassResourceCapsule(
    icon: String,
    amount: Int,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xB3181230))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x55FFFFFF), Color(0x15FFFFFF))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = String.format("%,d", amount),
                fontFamily = Baloo2FontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * High-End Mode Card with dynamic border glow and squircle icon tile.
 */
@Composable
private fun EndlessModeCard(
    title: String,
    subtitle: String,
    icon: String,
    badgeText: String,
    accentColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    isSelected: Boolean,
    glowPulse: Float,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_scale"
    )

    val borderBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                accentColor,
                accentColor.copy(alpha = 0.7f),
                accentColor
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x33FFFFFF),
                Color(0x11FFFFFF)
            )
        )
    }

    val cardBg = if (isSelected) {
        Color(0xCC181335)
    } else {
        Color(0xB3110E24)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 14.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) accentColor else Color.Transparent
            )
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = borderBrush,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Squircle Icon Tile + Title & Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Squircle Icon Tile with Glossy Gradient
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.85f),
                                    accentColor.copy(alpha = 0.40f)
                                )
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        fontSize = 26.sp
                    )
                }

                // Text Description
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = Baloo2FontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = subtitle,
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Right: Circular Checkmark (if selected) + Pill Badge Tag
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF0B1026),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Tag Badge (SURVIVAL, BLITZ, CHAOS, ZEN)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBgColor)
                        .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = NunitoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
