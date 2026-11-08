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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameMode
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.DesignTokens
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
    highScore: Int = 0,
    classicBest: Int = 0,
    timeRushBest: Int = 0,
    arcadeBest: Int = 0,
    zenBest: Int = 0,
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

    // Deep navy/indigo game theme background with subtle gradient depth
    val cosmicGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF070B18),
            Color(0xFF0D1429),
            Color(0xFF131A36),
            Color(0xFF0E1428),
            Color(0xFF060914)
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
                color = Color(0x0C38BDF8)
            )

            // Faint floating balloon outlines at ~5% opacity
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
                    color = Color.White.copy(alpha = 0.04f),
                    radius = r,
                    center = pos
                )
                // String
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
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
                .padding(DesignTokens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Back Button + Currency Capsules + Titles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
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
                            .size(DesignTokens.IconButtonSize)
                            .clip(CircleShape)
                            .background(Color(0x2AFFFFFF))
                            .border(1.dp, Color(0x3AFFFFFF), CircleShape)
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
                        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hearts Capsule
                        EndlessGlassResourceCapsule(
                            type = "HEART",
                            amount = lives
                        )

                        // Coins Capsule
                        EndlessGlassResourceCapsule(
                            type = "COIN",
                            amount = coins
                        )

                        // Gems Capsule
                        EndlessGlassResourceCapsule(
                            type = "GEM",
                            amount = gems
                        )
                    }
                }

                // Title & Subtitle Lockup
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Space4)
                ) {
                    Text(
                        text = "ENDLESS MODES",
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.TypeTierTitle,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.8.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Survive endless waves — set record high scores",
                        fontFamily = NunitoFontFamily,
                        fontSize = DesignTokens.TypeTierBody,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (highScore > 0) {
                        Box(
                            modifier = Modifier
                                .clip(DesignTokens.ShapeSmall)
                                .background(Color(0x33F59E0B))
                                .border(1.dp, Color(0x99F59E0B), DesignTokens.ShapeSmall)
                                .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space4)
                        ) {
                            Text(
                                text = "🏆 BEST: $highScore",
                                fontFamily = Baloo2FontFamily,
                                fontSize = DesignTokens.TypeTierCaption,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Middle Section: 4 Mode Cards (Harmonious Modern Palette)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Space4),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
            ) {
                // Card 1: Classic Survival (Emerald Cyan Accent)
                EndlessModeCard(
                    title = "Classic Survival",
                    subtitle = "3 Lives • Increasing Speed",
                    mode = GameMode.CLASSIC,
                    badgeText = "SURVIVAL",
                    accentColor = Color(0xFF10B981),
                    badgeBgColor = Color(0x3310B981),
                    badgeTextColor = Color(0xFF6EE7B7),
                    isSelected = selectedMode == GameMode.CLASSIC,
                    glowPulse = glowPulse,
                    bestScore = classicBest,
                    onClick = { selectedMode = GameMode.CLASSIC },
                    testTag = "mode_card_classic"
                )

                // Card 2: Time Rush (Golden Amber Accent)
                EndlessModeCard(
                    title = "Time Rush",
                    subtitle = "60 Seconds • Fast Spawns",
                    mode = GameMode.TIME_RUSH,
                    badgeText = "BLITZ",
                    accentColor = Color(0xFFF59E0B),
                    badgeBgColor = Color(0x33F59E0B),
                    badgeTextColor = Color(0xFFFDE68A),
                    isSelected = selectedMode == GameMode.TIME_RUSH,
                    glowPulse = glowPulse,
                    bestScore = timeRushBest,
                    onClick = { selectedMode = GameMode.TIME_RUSH },
                    testTag = "mode_card_time_rush"
                )

                // Card 3: Arcade Frenzy (Sunset Coral / Flame Accent)
                EndlessModeCard(
                    title = "Arcade Frenzy",
                    subtitle = "Power-ups & Bomb Waves",
                    mode = GameMode.ARCADE,
                    badgeText = "CHAOS",
                    accentColor = Color(0xFFFF5722),
                    badgeBgColor = Color(0x33FF5722),
                    badgeTextColor = Color(0xFFFFCCBC),
                    isSelected = selectedMode == GameMode.ARCADE,
                    glowPulse = glowPulse,
                    bestScore = arcadeBest,
                    onClick = { selectedMode = GameMode.ARCADE },
                    testTag = "mode_card_arcade"
                )

                // Card 4: Zen Popping (Tranquil Teal Accent)
                EndlessModeCard(
                    title = "Zen Popping",
                    subtitle = "No Bombs • Relaxing Ambient",
                    mode = GameMode.ZEN,
                    badgeText = "ZEN",
                    accentColor = Color(0xFF00E5FF),
                    badgeBgColor = Color(0x3300E5FF),
                    badgeTextColor = Color(0xFFBAE6FD),
                    isSelected = selectedMode == GameMode.ZEN,
                    glowPulse = glowPulse,
                    bestScore = zenBest,
                    onClick = { selectedMode = GameMode.ZEN },
                    testTag = "mode_card_zen"
                )
            }

            // Bottom Section: Big Hero Pill Button
            val (heroGradient, heroLabel) = when (selectedMode) {
                GameMode.CLASSIC -> listOf(Color(0xFF10B981), Color(0xFF059669)) to "START CLASSIC"
                GameMode.TIME_RUSH -> listOf(Color(0xFFF59E0B), Color(0xFFD97706)) to "START TIME RUSH"
                GameMode.ARCADE -> listOf(Color(0xFFFF5722), Color(0xFFDC2626)) to "START ARCADE"
                GameMode.ZEN -> listOf(Color(0xFF00E5FF), Color(0xFF0284C7)) to "START ZEN"
                else -> listOf(Color(0xFF10B981), Color(0xFF059669)) to "START GAME"
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
                    .heightIn(min = DesignTokens.ButtonMinHeight)
                    .scale(buttonScale)
                    .shadow(16.dp, DesignTokens.ShapeLarge, spotColor = heroGradient[0])
                    .clip(DesignTokens.ShapeLarge)
                    .background(Brush.horizontalGradient(heroGradient))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.60f), Color.White.copy(alpha = 0.20f))
                        ),
                        shape = DesignTokens.ShapeLarge
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
                        .fillMaxWidth(0.9f)
                        .height(20.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = DesignTokens.Space8)
                ) {
                    Text(
                        text = heroLabel,
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.SizeH2,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "tap to enter the arena",
                        fontFamily = NunitoFontFamily,
                        fontSize = DesignTokens.TypeTierCaption,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.90f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
    type: String,
    amount: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(DesignTokens.ShapeMedium)
            .background(Color(0xCC0D1326))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x44FFFFFF), Color(0x11FFFFFF))
                ),
                shape = DesignTokens.ShapeMedium
            )
            .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space4),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space4)
        ) {
            com.example.balloonpop.ui.components.CurrencyArt(
                type = type,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = String.format("%,d", amount),
                fontFamily = Baloo2FontFamily,
                fontSize = DesignTokens.TypeTierBody,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

/**
 * High-End Mode Card with unified card background and luminous accent glow.
 */
@Composable
private fun EndlessModeCard(
    title: String,
    subtitle: String,
    mode: GameMode,
    badgeText: String,
    accentColor: Color,
    badgeBgColor: Color,
    badgeTextColor: Color,
    isSelected: Boolean,
    glowPulse: Float,
    bestScore: Int = 0,
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
        Brush.horizontalGradient(
            listOf(
                accentColor,
                accentColor.copy(alpha = 0.85f),
                accentColor
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x2AFFFFFF),
                Color(0x10FFFFFF)
            )
        )
    }

    val cardBg = if (isSelected) {
        Color(0xF0121A33)
    } else {
        Color(0xD90E1428)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .scale(scale)
            .shadow(
                elevation = if (isSelected) 12.dp else 2.dp,
                shape = DesignTokens.ShapeMedium,
                spotColor = if (isSelected) accentColor else Color.Transparent
            )
            .clip(DesignTokens.ShapeMedium)
            .background(cardBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = borderBrush,
                shape = DesignTokens.ShapeMedium
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space16, vertical = DesignTokens.Space12),
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
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                modifier = Modifier.weight(1f)
            ) {
                // Squircle Icon Tile with Deep Gradient & Vector EndlessModeArt
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(DesignTokens.ShapeSmall)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor.copy(alpha = 0.75f),
                                    accentColor.copy(alpha = 0.35f)
                                )
                            )
                        )
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = DesignTokens.ShapeSmall
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    com.example.balloonpop.ui.components.EndlessModeArt(
                        mode = mode,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Text Description
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignTokens.Space4)
                ) {
                    Text(
                        text = title,
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.SizeH2,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = subtitle,
                        fontFamily = NunitoFontFamily,
                        fontSize = DesignTokens.TypeTierCaption,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Always rendered (even at 0) so every card keeps the same
                    // height and the BEST line sits in the same place.
                    Text(
                        text = if (bestScore > 0) "BEST: $bestScore" else "BEST: —",
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.TypeTierCaption,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Circular Checkmark (if selected) + Pill Badge Tag.
            // The check slot is always reserved so the badge sits in the
            // same place on selected and unselected cards alike.
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space4)
            ) {
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF070B18),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Tag Badge (SURVIVAL, BLITZ, CHAOS, ZEN)
                Box(
                    modifier = Modifier
                        .clip(DesignTokens.ShapeSmall)
                        .background(badgeBgColor)
                        .border(1.dp, accentColor.copy(alpha = 0.6f), DesignTokens.ShapeSmall)
                        .padding(horizontal = DesignTokens.Space8, vertical = DesignTokens.Space4),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        fontFamily = NunitoFontFamily,
                        fontSize = DesignTokens.SizeMicro,
                        fontWeight = FontWeight.ExtraBold,
                        color = badgeTextColor,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
