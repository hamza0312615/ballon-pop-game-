package com.example.balloonpop.ui.screens

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.components.AmberDialogFrame
import com.example.balloonpop.ui.components.JellyButton
import com.example.balloonpop.ui.components.JellyColorVariant
import com.example.balloonpop.ui.components.TopBar
import com.example.balloonpop.ui.dialogs.CustomChallengeDialog
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.NunitoFontFamily

/**
 * Redesigned Home Screen following Step 3 Design System:
 * - Themed sky gradient with soft floating balloon ambient silhouettes & confetti
 * - Real 3D layered logo lockup for "BALLOON POP" with integrated mascot
 * - Visually dominant "PLAY LEVEL X" primary CTA with pulsating glow idle animation
 * - Unified identical secondary action cards for Endless Rush and Skill Vault
 * - 3-column bottom button group with consistent icon/label layout & badge dots
 * - Strict usage of DesignTokens (spacing, radius, typography, colors)
 */
@Composable
fun HomeScreen(
    coins: Int = 1450,
    gems: Int = 320,
    lives: Int = 5,
    currentLevel: Int = 1,
    zoneName: String = "Neon Glade",
    onOpenCampaign: () -> Unit,
    onOpenSkillVault: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenDailySpin: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenRanks: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onStartCustomChallenge: (timeSeconds: Int, targetPops: Int, maxMisses: Int) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showCustomChallenge by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "home_animations")

    // Mascot bouncing animation
    val mascotBob by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_bob"
    )

    // Play button dominant pulsating glow
    val playPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.038f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    // Ambient floating balloon positions
    val ambientBalloonY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_balloon_drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060A14), // Deep midnight sky
                        Color(0xFF0C1424),
                        Color(0xFF131D33),
                        Color(0xFF090D18)
                    )
                )
            )
    ) {
        // Decorative background: Soft blurred floating balloons & confetti particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft glowing ambient balloons in background
            drawCircle(
                color = Color(0x1800E5FF),
                radius = 70.dp.toPx(),
                center = Offset(w * 0.15f, h * 0.28f + ambientBalloonY)
            )
            drawCircle(
                color = Color(0x15FF2A75),
                radius = 85.dp.toPx(),
                center = Offset(w * 0.85f, h * 0.35f - ambientBalloonY)
            )
            drawCircle(
                color = Color(0x12FFD700),
                radius = 65.dp.toPx(),
                center = Offset(w * 0.22f, h * 0.65f + ambientBalloonY * 0.7f)
            )
            drawCircle(
                color = Color(0x1510B981),
                radius = 80.dp.toPx(),
                center = Offset(w * 0.82f, h * 0.72f - ambientBalloonY * 0.6f)
            )

            // Ambient festive confetti specks
            drawCircle(Color(0x33FFB703), 2.5.dp.toPx(), Offset(w * 0.3f, h * 0.18f))
            drawCircle(Color(0x3300E5FF), 3.dp.toPx(), Offset(w * 0.7f, h * 0.22f))
            drawCircle(Color(0x33FF2A75), 2.dp.toPx(), Offset(w * 0.12f, h * 0.45f))
            drawCircle(Color(0x3310B981), 3.dp.toPx(), Offset(w * 0.88f, h * 0.52f))
            drawCircle(Color(0x33FFA000), 2.5.dp.toPx(), Offset(w * 0.45f, h * 0.82f))
        }

        // Main Layout Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.Space16, vertical = DesignTokens.Space8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // STEP 2: Unified Top Resource Bar Component
            TopBar(
                coins = coins,
                gems = gems,
                lives = lives,
                onOpenShop = onOpenShop,
                onOpenSettings = onOpenSettings
            )

            // 2. Hero Centerpiece: Mascot + Layered 3D Logo Lockup
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Space4),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mascot Sitting above Title
                Box(
                    modifier = Modifier
                        .offset(y = mascotBob.dp)
                        .size(68.dp)
                        .shadow(DesignTokens.ElevationActive, CircleShape, spotColor = DesignTokens.PrimaryBrand)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFFEF08A),
                                    DesignTokens.PrimaryBrandBright,
                                    DesignTokens.PrimaryBrandDark
                                )
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFEA00), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🥳", fontSize = 38.sp)
                }

                Spacer(modifier = Modifier.height(DesignTokens.Space8))

                // Layered 3D Title Banner: BALLOON POP
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .shadow(DesignTokens.ElevationActive, DesignTokens.ShapeCard, spotColor = DesignTokens.PrimaryBrand)
                        .clip(DesignTokens.ShapeCard)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E1710),
                                    Color(0xFF140F0A),
                                    Color(0xFF0C0905)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFEA00),
                                    DesignTokens.PrimaryBrandBright,
                                    DesignTokens.PrimaryBrandDark
                                )
                            ),
                            DesignTokens.ShapeCard
                        )
                        .padding(vertical = DesignTokens.Space8, horizontal = DesignTokens.Space16),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🎈", fontSize = 24.sp)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "BALLOON POP",
                                fontSize = DesignTokens.SizeH1,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFEA00),
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "CASUAL DELUXE ADVENTURE",
                                fontSize = DesignTokens.SizeMicro,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFEF3C7),
                                letterSpacing = 1.sp
                            )
                        }
                        Text(text = "✨", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Space8))

                // Helper Action Pills: How to Play + Custom Lab
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "❓ HOW TO PLAY" Helper Pill
                    HomeHelperPill(
                        icon = "❓",
                        label = "HOW TO PLAY",
                        borderColor = DesignTokens.SecondaryAccentDeep,
                        textColor = Color(0xFF7DD3FC),
                        onClick = { showHowToPlay = true },
                        testTag = "how_to_play_button"
                    )

                    // "⚡ CUSTOM LAB" Helper Pill
                    HomeHelperPill(
                        icon = "⚡",
                        label = "CUSTOM LAB",
                        borderColor = Color(0xFFA855F7),
                        textColor = Color(0xFFE9D5FF),
                        onClick = { showCustomChallenge = true },
                        testTag = "custom_challenge_button"
                    )
                }
            }

            // 3. Primary & Secondary Game Action Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Space4),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
            ) {
                // PRIMARY HERO CTA: PLAY LEVEL X (Dominant, Larger, Radiant Gradient, Glow Pulse)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(playPulse)
                        .shadow(DesignTokens.ElevationActive, DesignTokens.ShapeCard, spotColor = DesignTokens.PrimaryBrand)
                        .clip(DesignTokens.ShapeCard)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    DesignTokens.PrimaryBrandBright,
                                    DesignTokens.PrimaryBrand,
                                    DesignTokens.PrimaryBrandDark
                                )
                            )
                        )
                        .border(2.5.dp, Color(0xFFFEF08A), DesignTokens.ShapeCard)
                        .clickable { onOpenCampaign() }
                        .testTag("home_play_campaign_button")
                        .padding(horizontal = DesignTokens.Space16, vertical = DesignTokens.Space16)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Play Icon Circle with Depth
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF78350F))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        // Call to Action Text Block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "CONTINUE STAGE $currentLevel",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF5B2E00),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$zoneName — Tap to Enter!",
                                fontFamily = NunitoFontFamily,
                                fontSize = DesignTokens.SizeCaption,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF78350F)
                            )
                        }

                        // Start Badge
                        Box(
                            modifier = Modifier
                                .clip(DesignTokens.ShapeSmall)
                                .background(Color(0xFF78350F))
                                .border(1.dp, Color(0xFFFEF08A), DesignTokens.ShapeSmall)
                                .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space8)
                        ) {
                            Text(
                                text = "START ▶",
                                fontSize = DesignTokens.SizeCaption,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // SECONDARY ACTION CARD 1: ENDLESS RUSH (Unified identical layout)
                HomeSecondaryActionCard(
                    icon = "⚡",
                    iconBgColor = DesignTokens.SecondaryAccentDeep,
                    iconBorderColor = DesignTokens.SecondaryAccent,
                    title = "ENDLESS RUSH",
                    subtitle = "Fast Arcade Survival • High Score Challenge",
                    subtitleColor = Color(0xFF93C5FD),
                    accentColor = DesignTokens.SecondaryAccent,
                    cardBgGradient = listOf(Color(0xFF0F1E2E), Color(0xFF0A2E4C), Color(0xFF0D1B2A)),
                    onClick = onOpenEndless,
                    testTag = "home_endless_rush_button"
                )

                // SECONDARY ACTION CARD 2: SKILL VAULT (Unified identical layout)
                HomeSecondaryActionCard(
                    icon = "🔨",
                    iconBgColor = Color(0xFF6B21A8),
                    iconBorderColor = Color(0xFFA855F7),
                    title = "SKILL VAULT & HAMMERS",
                    subtitle = "Upgrade Hammers • Unlock 5s Slide Skill",
                    subtitleColor = Color(0xFFD8B4FE),
                    accentColor = Color(0xFFA855F7),
                    cardBgGradient = listOf(Color(0xFF1E102E), Color(0xFF331454), Color(0xFF170D24)),
                    onClick = onOpenSkillVault,
                    testTag = "home_skill_vault_button"
                )
            }

            // 4. Equal 3-Column Bottom Button Group (Daily Spin, Shop, Quests)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DesignTokens.Space4),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                // 1. Daily Spin Button
                HomeBottomNavButton(
                    icon = "🎡",
                    title = "Daily Spin",
                    badge = "FREE",
                    color = Color(0xFF9333EA),
                    borderColor = Color(0xFFC084FC),
                    onClick = onOpenDailySpin,
                    testTag = "home_quick_daily_spin",
                    modifier = Modifier.weight(1f)
                )

                // 2. Balloon Shop Button
                HomeBottomNavButton(
                    icon = "🎁",
                    title = "Shop",
                    badge = "SALE",
                    color = DesignTokens.PrimaryBrandDark,
                    borderColor = DesignTokens.PrimaryBrandBright,
                    onClick = onOpenShop,
                    testTag = "home_quick_shop",
                    modifier = Modifier.weight(1f)
                )

                // 3. Quests Button
                HomeBottomNavButton(
                    icon = "🏆",
                    title = "Quests",
                    badge = "NEW",
                    color = Color(0xFF059669),
                    borderColor = DesignTokens.SuccessGreenBright,
                    onClick = onOpenQuests,
                    testTag = "home_quick_quests",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // How to Play Dialog
        AnimatedVisibility(
            visible = showHowToPlay,
            enter = fadeIn(tween(200)) + scaleIn(tween(250)),
            exit = fadeOut(tween(150)) + scaleOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { showHowToPlay = false },
                contentAlignment = Alignment.Center
            ) {
                AmberDialogFrame(
                    title = "🎈 HOW TO PLAY 🎈",
                    frameGradient = listOf(Color(0xFF1E172F), Color(0xFF140F22), Color(0xFF0C0916)),
                    borderColor = DesignTokens.SecondaryAccent,
                    onClose = { showHowToPlay = false }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
                    ) {
                        HowToPlayRow(
                            icon = "🎈",
                            title = "1. Tap Balloons to Pop",
                            description = "Tap balloons before they float off-screen. Pop consecutive balloons to build combos and earn big bonus points!",
                            accentColor = Color(0xFFFF2A75)
                        )
                        HowToPlayRow(
                            icon = "💣",
                            title = "2. Avoid Hazard Bombs",
                            description = "Do NOT tap skull balloons or bombs! Hitting a hazard costs a heart life.",
                            accentColor = DesignTokens.DangerRed
                        )
                        HowToPlayRow(
                            icon = "🔨",
                            title = "3. Tap Hammers & Skills",
                            description = "Tap the screen with your hammer to smash clusters! Activate the 5s Hammer Slide skill to drag and wipe out everything!",
                            accentColor = DesignTokens.PrimaryBrandBright
                        )
                        HowToPlayRow(
                            icon = "💨",
                            title = "4. Don't Miss Balloons!",
                            description = "Each level has a strict time limit and a maximum number of missed balloons. If more balloons escape than allowed, it's automatic Game Over!",
                            accentColor = DesignTokens.SecondaryAccent
                        )

                        Spacer(modifier = Modifier.height(DesignTokens.Space4))

                        JellyButton(
                            text = "GOT IT! LET'S PLAY 🎮",
                            onClick = {
                                showHowToPlay = false
                                onOpenCampaign()
                            },
                            colorVariant = JellyColorVariant.GREEN,
                            testTag = "how_to_play_dismiss_play_button",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Custom Challenge Lab Dialog
        if (showCustomChallenge) {
            CustomChallengeDialog(
                initialTimeSeconds = 15,
                initialTargetPops = 23,
                initialMaxMisses = 4,
                onStartChallenge = { time, pops, misses ->
                    showCustomChallenge = false
                    onStartCustomChallenge(time, pops, misses)
                },
                onClose = { showCustomChallenge = false }
            )
        }
    }
}

/**
 * Standardized Secondary Action Card Component (Endless Rush & Skill Vault):
 * Guarantees 100% identical dimensions (68dp), 44dp circular icon container,
 * text hierarchy (H2 Title + Caption subtitle), and corner radius.
 */
@Composable
private fun HomeSecondaryActionCard(
    icon: String,
    iconBgColor: Color,
    iconBorderColor: Color,
    title: String,
    subtitle: String,
    subtitleColor: Color,
    accentColor: Color,
    cardBgGradient: List<Color>,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "card_press"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .scale(scale)
            .shadow(DesignTokens.ElevationAmbient, DesignTokens.ShapeCard, spotColor = accentColor)
            .clip(DesignTokens.ShapeCard)
            .background(Brush.horizontalGradient(cardBgGradient))
            .border(1.8.dp, accentColor, DesignTokens.ShapeCard)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space16, vertical = DesignTokens.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
        ) {
            // Uniform 44dp Circular Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
                    .border(1.5.dp, iconBorderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Column {
                Text(
                    text = title,
                    fontSize = DesignTokens.SizeH2,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = subtitleColor
                )
            }
        }

        Text(
            text = "➔",
            fontSize = 18.sp,
            color = accentColor,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * 3-Column Bottom Navigation Button Component:
 * Uniform height (54dp), consistent icon/title stack, with notification badge support.
 */
@Composable
private fun HomeBottomNavButton(
    icon: String,
    title: String,
    badge: String?,
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "btn_press"
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .scale(scale)
            .shadow(DesignTokens.ElevationAmbient, DesignTokens.ShapeCard)
            .clip(DesignTokens.ShapeCard)
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.35f),
                        Color(0x22131927)
                    )
                )
            )
            .border(1.5.dp, borderColor.copy(alpha = 0.8f), DesignTokens.ShapeCard)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space8, vertical = DesignTokens.Space4),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
            Text(
                text = title,
                fontSize = DesignTokens.SizeCaption,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // Notification Badge Dot
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(DesignTokens.ShapeSmall)
                    .background(DesignTokens.DangerRed)
                    .border(1.dp, Color.White, DesignTokens.ShapeSmall)
                    .padding(horizontal = DesignTokens.Space4, vertical = 1.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HomeHelperPill(
    icon: String,
    label: String,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(DesignTokens.ShapeSmall)
            .background(DesignTokens.SurfaceCardNavy)
            .border(1.5.dp, borderColor, DesignTokens.ShapeSmall)
            .clickable(onClick = onClick)
            .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space4)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space4)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(
            text = label,
            fontSize = DesignTokens.SizeCaption,
            fontWeight = FontWeight.Black,
            color = textColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun HowToPlayRow(
    icon: String,
    title: String,
    description: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignTokens.ShapeSmall)
            .background(Color(0xFF16152B))
            .border(1.2.dp, accentColor.copy(alpha = 0.5f), DesignTokens.ShapeSmall)
            .padding(DesignTokens.Space8),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f))
                .border(1.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = DesignTokens.SizeBody,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(DesignTokens.Space4))
            Text(
                text = description,
                fontSize = DesignTokens.SizeCaption,
                fontWeight = FontWeight.Medium,
                color = DesignTokens.TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
