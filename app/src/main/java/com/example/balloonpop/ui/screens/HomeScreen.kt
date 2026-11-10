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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.balloonpop.model.GameMode
import com.example.balloonpop.ui.components.EndlessModeArt
import com.example.balloonpop.ui.components.HomeNavIcon
import com.example.balloonpop.ui.components.SkillVaultArt
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
import com.example.balloonpop.ui.dialogs.DailyGiftsDialog
import com.example.balloonpop.ui.components.TopBar
import com.example.balloonpop.ui.dialogs.CustomChallengeDialog
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.CoralPrimary
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.NunitoFontFamily
import com.example.balloonpop.ui.theme.CreamSurface
import com.example.balloonpop.ui.theme.HeroLimeBottom
import com.example.balloonpop.ui.theme.HeroLimeShelf
import com.example.balloonpop.ui.theme.HeroLimeTop
import com.example.balloonpop.ui.theme.StageCurtainEdge
import com.example.balloonpop.ui.theme.StageMagentaDeep
import com.example.balloonpop.ui.theme.StageMagentaTop
import com.example.balloonpop.ui.theme.StageSpotlightWarm
import com.example.balloonpop.ui.theme.StoryGold
import com.example.balloonpop.ui.theme.StorybookIndigo
import com.example.balloonpop.ui.theme.StorybookPlum
import com.example.balloonpop.ui.theme.StorybookPlumDeep
import com.example.balloonpop.ui.theme.ToyGold
import com.example.balloonpop.ui.theme.ToyGoldShelf
import com.example.balloonpop.ui.theme.ToyOrangeBottom
import com.example.balloonpop.ui.theme.ToyOrangeShelf
import com.example.balloonpop.ui.theme.ToyOrangeTop
import com.example.balloonpop.ui.theme.ToyVioletPanel
import com.example.balloonpop.ui.theme.ToyVioletPanelDark
import com.example.balloonpop.ui.theme.ToyWoodFloor
import com.example.balloonpop.ui.theme.ToyWoodFloorDark
import com.example.balloonpop.ui.theme.ToyWoodHighlight

/**
 * Home Screen (Quality Pass 4, Royal Match / Candy Crush benchmark):
 * - Themed sky gradient with soft floating balloon ambient silhouettes & confetti
 * - One prominent mascot above the "BALLOON POP" logo lockup
 * - ONE hero action only: the coral "PLAY STAGE X" CTA (CtaGradient, pulsating glow)
 * - Every secondary action (Endless, Vault, Spin, Shop, Quests, Ranks, Settings,
 *   Help, Custom) is a uniform 48dp icon button with a one-line caption
 * - Slim single-row TopBar for lives / coins / gems; ScreenPadding at the edges
 * - Strict usage of DesignTokens (spacing, radius, typography, colors)
 */
@Composable
fun HomeScreen(
    coins: Int = 1450,
    gems: Int = 320,
    lives: Int = 5,
    currentLevel: Int = 1,
    zoneName: String = "Twilight Grove",
    onOpenCampaign: () -> Unit,
    onOpenSkillVault: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenDailySpin: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenRanks: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onStartCustomChallenge: (timeSeconds: Int, targetPops: Int, maxMisses: Int) -> Unit = { _, _, _ -> },
    // Pass 9: 7-Day Daily Gifts track
    dailyTrackDay: Int = 1,
    dailyTrackCanClaimToday: Boolean = true,
    dailyTrackResetStreakNotice: Boolean = false,
    onClaimDailyGift: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showHowToPlay by remember { mutableStateOf(false) }
    var showCustomChallenge by remember { mutableStateOf(false) }
    var showDailyGifts by remember { mutableStateOf(false) }

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
            .background(DesignTokens.StageWorldGradient)
    ) {
        // Theatrical stage diorama background: warm spotlight cone, curtain vignette, wooden floorboards
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Warm spotlight cone glowing down from top center
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x55FFD166),
                        Color(0x22FFD166),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, 0f),
                    radius = h * 0.52f
                ),
                topLeft = Offset(w * 0.05f, -h * 0.08f),
                size = androidx.compose.ui.geometry.Size(w * 0.9f, h * 0.65f)
            )

            // 2. Theatrical curtain-dark vignette on left and right edges
            drawRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        StageCurtainEdge.copy(alpha = 0.55f),
                        Color.Transparent,
                        StageCurtainEdge.copy(alpha = 0.55f)
                    )
                )
            )

            // 3. Wooden floorboards at the bottom stage (#C07F3A with dark edge planking #7A4A16)
            val floorTop = h * 0.54f
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ToyWoodHighlight, ToyWoodFloor, ToyWoodFloorDark),
                    startY = floorTop,
                    endY = h
                ),
                topLeft = Offset(0f, floorTop),
                size = androidx.compose.ui.geometry.Size(w, h - floorTop)
            )

            // Wooden plank horizontal lines
            val plankCount = 5
            for (i in 0..plankCount) {
                val py = floorTop + (h - floorTop) * (i.toFloat() / plankCount)
                drawLine(
                    color = ToyWoodFloorDark.copy(alpha = 0.85f),
                    start = Offset(0f, py),
                    end = Offset(w, py),
                    strokeWidth = 2.5f
                )
                if (i < plankCount) {
                    // Wood highlight rim under line
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(0f, py + 2.5f),
                        end = Offset(w, py + 2.5f),
                        strokeWidth = 1f
                    )
                }
            }

            // Ambient floating balloons
            drawCircle(
                color = Color(0x334A90C4),
                radius = 60.dp.toPx(),
                center = Offset(w * 0.12f, h * 0.22f + ambientBalloonY)
            )
            drawCircle(
                color = Color(0x33E11D48),
                radius = 75.dp.toPx(),
                center = Offset(w * 0.88f, h * 0.26f - ambientBalloonY)
            )
            drawCircle(
                color = Color(0x33F59E0B),
                radius = 55.dp.toPx(),
                center = Offset(w * 0.18f, h * 0.45f + ambientBalloonY * 0.7f)
            )

            // Sparkle dust specks
            drawCircle(Color(0x66FFEA00), 3.dp.toPx(), Offset(w * 0.32f, h * 0.15f))
            drawCircle(Color(0x66A6F750), 3.dp.toPx(), Offset(w * 0.68f, h * 0.18f))
            drawCircle(Color(0x66FFD166), 2.5.dp.toPx(), Offset(w * 0.84f, h * 0.40f))
        }

        // Main Layout Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = DesignTokens.ScreenPadding, vertical = DesignTokens.Space8),
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

            // 2. Hero Centerpiece: Mascot + Stage Diorama + Balloon Buddies
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Space4),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Popster centre-stage diorama on wooden stage floor with balloon buddies
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = DesignTokens.Space4)
                ) {
                    // Balloon buddies flanking Popster
                    Row(
                        modifier = Modifier.width(220.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left buddy: glossy cyan balloon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset(y = (-10).dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0xFF0284C7))
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF7DD3FC), Color(0xFF0284C7))))
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                        // Right buddy: glossy ruby balloon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .offset(y = (-10).dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0xFFE11D48))
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFFFDA4AF), Color(0xFFE11D48))))
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                    }

                    // Wooden stage disc
                    Box(
                        modifier = Modifier
                            .size(138.dp)
                            .shadow(DesignTokens.ElevationStrong, CircleShape, spotColor = Color(0x66000000))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(ToyWoodHighlight, ToyWoodFloor, ToyWoodFloorDark)
                                )
                            )
                            .border(3.dp, ToyGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        com.example.balloonpop.ui.components.MascotCharacter(
                            mood = com.example.balloonpop.ui.components.MascotMood.JOYFUL,
                            size = 96.dp,
                            modifier = Modifier.offset(y = mascotBob.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignTokens.Space8))

                // Layered 3D Title Banner: BALLOON POP (Toybox Stage plank)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0x66000000))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF6B2D9E), Color(0xFF45196E))
                            )
                        )
                        .border(
                            2.5.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFEA00), ToyGold, ToyGoldShelf)
                            ),
                            RoundedCornerShape(18.dp)
                        )
                        .padding(vertical = DesignTokens.Space8, horizontal = DesignTokens.Space16),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BALLOON POP",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 2.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFF2E084D),
                                    offset = Offset(0f, 3f),
                                    blurRadius = 4f
                                )
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "TOYBOX STAGE DELUXE",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ToyGold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // 3. ONE hero action + every secondary action as a uniform icon button.
            // Nothing else on this screen is a filled CTA: Endless / Vault / Spin /
            // Shop / Quests / Ranks / Settings / Help / Custom all share the exact
            // same 48dp icon-button treatment below the PLAY hero.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DesignTokens.Space4),
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
            ) {
                // PRIMARY HERO CTA (Pass 5): the one JewelButton on this screen —
                // sunset face, 5dp shelf, gloss, press-sink; stage + zone inside it.
                Box(modifier = Modifier.scale(playPulse)) {
                    com.example.balloonpop.ui.components.JewelButtonContainer(
                        onClick = onOpenCampaign,
                        style = com.example.balloonpop.ui.components.JewelButtonStyle.SUNSET,
                        minHeight = 72.dp,
                        testTag = "home_play_campaign_button",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f))
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
                        Spacer(modifier = Modifier.width(DesignTokens.Space12))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "PLAY STAGE $currentLevel",
                                fontFamily = Baloo2FontFamily,
                                fontSize = DesignTokens.TypeTierTitle,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = zoneName,
                                fontFamily = NunitoFontFamily,
                                fontSize = DesignTokens.TypeTierCaption,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFE3D9),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Chunky toy bottom navigation on a violet bar (#55277D with beveled rim)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color(0x66000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF55277D), Color(0xFF381454))
                            )
                        )
                        .border(
                            2.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFF7E38B3), Color(0xFF30104A))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Secondary row 1: game modes and storefront actions.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            HomeIconActionButton(
                                iconContent = {
                                    EndlessModeArt(GameMode.ARCADE, modifier = Modifier.size(26.dp))
                                },
                                label = "Endless",
                                accentColor = DesignTokens.SecondaryAccent,
                                onClick = onOpenEndless,
                                testTag = "home_endless_rush_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    SkillVaultArt("skill_hammer_slide", modifier = Modifier.size(26.dp))
                                },
                                label = "Vault",
                                accentColor = Color(0xFFA855F7),
                                onClick = onOpenSkillVault,
                                testTag = "home_skill_vault_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    HomeNavIcon(type = "SPIN", modifier = Modifier.size(26.dp))
                                },
                                label = "Spin",
                                accentColor = Color(0xFFC084FC),
                                badge = "FREE",
                                onClick = onOpenDailySpin,
                                testTag = "home_quick_daily_spin",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    HomeNavIcon(type = "SHOP", modifier = Modifier.size(26.dp))
                                },
                                label = "Shop",
                                accentColor = DesignTokens.PrimaryBrandBright,
                                badge = "SALE",
                                onClick = onOpenShop,
                                testTag = "home_quick_shop",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    HomeNavIcon(type = "QUESTS", modifier = Modifier.size(26.dp))
                                },
                                label = "Quests",
                                accentColor = DesignTokens.SuccessGreenBright,
                                badge = "NEW",
                                onClick = onOpenQuests,
                                testTag = "home_quick_quests",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Secondary row 2: ranks, settings and helper actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            HomeIconActionButton(
                                iconContent = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Ranks",
                                        tint = ToyGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Ranks",
                                accentColor = ToyGold,
                                onClick = onOpenRanks,
                                testTag = "home_ranks_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = Color(0xFFC4B5FD),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Settings",
                                accentColor = Color(0xFFA78BFA),
                                onClick = onOpenSettings,
                                testTag = "home_settings_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "How to play",
                                        tint = Color(0xFF7DD3FC),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Help",
                                accentColor = DesignTokens.SecondaryAccentDeep,
                                onClick = { showHowToPlay = true },
                                testTag = "how_to_play_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Custom Lab",
                                        tint = Color(0xFFE9D5FF),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = "Custom",
                                accentColor = Color(0xFFA855F7),
                                onClick = { showCustomChallenge = true },
                                testTag = "custom_challenge_button",
                                modifier = Modifier.weight(1f)
                            )
                            HomeIconActionButton(
                                iconContent = {
                                    Text(text = "🎁", fontSize = 22.sp)
                                },
                                label = "Gifts",
                                accentColor = ToyGold,
                                badge = if (dailyTrackCanClaimToday) "CLAIM" else null,
                                onClick = { showDailyGifts = true },
                                testTag = "home_daily_gifts_button",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Pass 9: 7-Day Daily Gifts Calendar Dialog
        if (showDailyGifts) {
            DailyGiftsDialog(
                currentDay = dailyTrackDay,
                canClaimToday = dailyTrackCanClaimToday,
                streakResetNotice = dailyTrackResetStreakNotice,
                onClaimToday = {
                    onClaimDailyGift()
                },
                onDismiss = { showDailyGifts = false }
            )
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
                            accentColor = Color(0xFFC93A5E)
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
 * The ONE secondary-action treatment on Home (Pass 4): a uniform
 * [DesignTokens.IconButtonSize] circular icon button on the shared card surface,
 * with a single short caption underneath that never truncates. Every secondary
 * action (Endless, Vault, Spin, Shop, Quests, Ranks, Settings, Help, Custom)
 * uses this exact component, so nothing competes with the PLAY hero.
 */
@Composable
private fun HomeIconActionButton(
    iconContent: @Composable () -> Unit,
    label: String,
    accentColor: Color,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "home_icon_btn_press"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(vertical = DesignTokens.Space4),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Chunky violet toy tile with beveled highlight
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0x66000000))
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF6B2D9E), Color(0xFF43186B))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.50f), accentColor.copy(alpha = 0.85f))
                        ),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                iconContent()
            }

            // Small status badge (FREE / SALE / NEW), kept off the label so the
            // caption always stays on one line.
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .clip(DesignTokens.ShapeSmall)
                        .background(DesignTokens.DangerRed)
                        .border(1.dp, Color.White, DesignTokens.ShapeSmall)
                        .padding(horizontal = DesignTokens.Space4, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = DesignTokens.SizeMicro,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(DesignTokens.Space4))

        Text(
            text = label.uppercase(),
            fontFamily = Baloo2FontFamily,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.5.sp,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 1.5f),
                    blurRadius = 2f
                )
            )
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
