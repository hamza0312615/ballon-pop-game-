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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ads.AdMobManager
import com.example.balloonpop.model.*
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.dialogs.*
import com.example.balloonpop.ui.game.GameCanvas
import com.example.balloonpop.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GamePlayScreen(
    gameState: GameState,
    gameMode: GameMode,
    currentLevel: Int,
    score: Int,
    lives: Int,
    timeLeft: Int,
    poppedCount: Int,
    missedCount: Int = 0,
    maxMissesAllowed: Int = 4,
    gameOverReason: GameOverReason = GameOverReason.TIME_EXPIRED,
    targetPops: Int,
    targetScore: Int = 1000,
    combo: Int,
    maxCombo: Int,
    earnedCoins: Int,
    earnedGems: Int,
    earnedStars: Int,
    screenShake: () -> Float = { 0f },
    isFreezeActive: Boolean,
    isMultiplierActive: Boolean,
    balloons: List<Balloon>,
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    swipeTrail: List<BladeTrailPoint>,
    hammerStrikes: List<HammerStrikeEffect>,
    shockwaves: List<PopShockwave> = emptyList(),
    ambients: List<AmbientElement> = emptyList(),
    equippedFrame: PlayFrame,
    equippedTheme: ThemeConfig = GameConstants.SHOP_THEMES.first(),
    lightningBolts: List<LightningBolt> = emptyList(),
    screenFlashAlpha: Float = 0f,
    boosterCounts: Map<String, Int> = emptyMap(),
    candyPraise: String? = null,
    onTriggerBooster: (InGameBooster) -> Unit = {},
    coins: Int = 0,
    equippedSkillId: String = "skill_slice",
    skillEnergy: Float = 100f,
    skillCooldownRemaining: Float = 0f,
    isHammerSlideActive: Boolean = false,
    hammerSlideTimeRemaining: Float = 0f,
    stageBoosterUsesCount: Int = 0,
    onActivateSkill: () -> Unit = {},
    onStepFrame: (Float) -> Unit,
    onPointerInput: (Float, Float, Boolean) -> Unit,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onStageMenu: () -> Unit,
    onQuitToMenu: () -> Unit,
    onOpenSettings: () -> Unit,
    isNewHighScore: Boolean = false,
    previousHighScore: Int = 0,
    isTransitioningLevel: Boolean = false,
    transitionTargetLevel: Int? = null,
    onFinishLevelTransition: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var resumeCountdown by remember { mutableStateOf<Int?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    // Full-screen Interstitial Ad on Game Over and Stage Clear
    LaunchedEffect(gameState) {
        if (gameState == GameState.LEVELCLEAR || gameState == GameState.GAMEOVER) {
            activity?.let { act ->
                AdMobManager.showInterstitialAd(act)
            }
        }
    }

    LaunchedEffect(resumeCountdown) {
        val c = resumeCountdown
        if (c != null) {
            if (c > 0) {
                delay(650L)
                resumeCountdown = c - 1
            } else {
                delay(450L)
                resumeCountdown = null
                onResume()
            }
        }
    }

    val equippedSkill = GameConstants.AVAILABLE_SKILLS.find { it.id == equippedSkillId }
        ?: GameConstants.AVAILABLE_SKILLS.first()

    Box(modifier = modifier.fillMaxSize()) {
        // 1. High-performance isolated Game Canvas (Runs at 60/120 FPS without HUD recomposition)
        GameCanvas(
            gameState = gameState,
            balloons = balloons,
            particles = particles,
            floatingTexts = floatingTexts,
            swipeTrail = swipeTrail,
            hammerStrikes = hammerStrikes,
            shockwaves = shockwaves,
            ambients = ambients,
            equippedFrame = equippedFrame,
            equippedTheme = equippedTheme,
            lightningBolts = lightningBolts,
            screenFlashAlpha = screenFlashAlpha,
            screenShake = screenShake,
            isFreezeActive = isFreezeActive,
            isMultiplierActive = isMultiplierActive,
            currentLevel = currentLevel,
            onStepFrame = { dt ->
                if (resumeCountdown == null) {
                    onStepFrame(dt)
                } else {
                    onStepFrame(0f)
                }
            },
            onPointerInput = onPointerInput,
            onCanvasSizeChanged = onCanvasSizeChanged,
            modifier = Modifier.fillMaxSize()
        )

        // 2. High-End Translucent Glossy Capsule Top HUD (Compact floating glass pill)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = Color(0x6600E5FF))
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xD90D1326))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x8893C5FD), Color(0x33A855F7), Color(0x22FFFFFF))
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top Inner Specular Sheen Arc (matches pill size, never expanding parent)
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.20f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.70f
                    ),
                    topLeft = Offset(-w * 0.05f, -h * 0.3f),
                    size = Size(w * 1.1f, h * 0.8f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Cute Pink Kawaii Balloon Mascot + Pops Counter & Miss Warning
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cute Pink Kawaii Balloon Icon
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xFFFF5D8F))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFB3C6), Color(0xFFFF5D8F), Color(0xFFE11D48)),
                                    center = Offset(12f, 12f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = size.width * 0.38f

                            // Specular sheen
                            drawCircle(Color.White.copy(alpha = 0.65f), radius = r * 0.35f, center = Offset(cx - r * 0.4f, cy - r * 0.4f))

                            // Sparkling eyes
                            drawCircle(Color(0xFF1E0A12), radius = r * 0.16f, center = Offset(cx - r * 0.3f, cy))
                            drawCircle(Color.White, radius = r * 0.08f, center = Offset(cx - r * 0.25f, cy - 1.5f))
                            drawCircle(Color(0xFF1E0A12), radius = r * 0.16f, center = Offset(cx + r * 0.3f, cy))
                            drawCircle(Color.White, radius = r * 0.08f, center = Offset(cx + r * 0.35f, cy - 1.5f))

                            // Rosy blush
                            drawCircle(Color(0x66FF2A75), radius = r * 0.14f, center = Offset(cx - r * 0.45f, cy + 4f))
                            drawCircle(Color(0x66FF2A75), radius = r * 0.14f, center = Offset(cx + r * 0.45f, cy + 4f))

                            // Sweet smile
                            val mouthPath = Path().apply {
                                moveTo(cx - 3.5f, cy + 3.5f)
                                quadraticTo(cx, cy + 7f, cx + 3.5f, cy + 3.5f)
                            }
                            drawPath(mouthPath, color = Color(0xFF1E0A12), style = Stroke(width = 1.5f, cap = StrokeCap.Round))
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = if (gameMode == GameMode.CAMPAIGN) "$poppedCount/$targetPops pops" else "$poppedCount pops",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        if (gameMode == GameMode.CAMPAIGN) {
                            val isDanger = missedCount >= maxMissesAllowed - 1
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Miss: $missedCount/$maxMissesAllowed",
                                    fontFamily = NunitoFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDanger) Color(0xFFEF4444) else Color(0xFFFBBF24)
                                )
                                Text(text = "⚠️", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Right: Clock Icon + Time Countdown & Pause
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (gameMode == GameMode.CAMPAIGN || gameMode == GameMode.TIME_RUSH) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Glowing Clock Circle
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0x55C084FC), Color(0x22818CF8))
                                        )
                                    )
                                    .border(1.5.dp, Color(0xFFC084FC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🕒", fontSize = 15.sp)
                            }

                            Text(
                                text = "${timeLeft}s",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (timeLeft <= 5) Color(0xFFEF4444) else Color.White
                            )
                        }
                    }

                    // Seamless Pause Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                            .border(1.dp, Color(0x44FFFFFF), CircleShape)
                            .clickable { onPause() }
                            .testTag("pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⏸️", fontSize = 14.sp)
                    }
                }
            }
        }

        // Active Boss Battle Health Bar (When on Boss Stages: 10, 20, 30...)
        val isBossStage = (currentLevel % 10 == 0)
        val activeBoss = balloons.find { it.type == BalloonType.MEGA_BOSS && !it.popped }
        if (isBossStage && gameState == GameState.PLAYING && activeBoss != null) {
            BossBattleHealthBar(
                currentHp = activeBoss.hp,
                maxHp = activeBoss.maxHp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp)
            )
        }

        // Mini Pet In-Game Companion cheering the player on!
        val currentZoneIdx = ((currentLevel - 1) / 10).coerceAtLeast(0)
        val currentPet = remember(currentZoneIdx) { MiniPetRegistry.getPetForZone(currentZoneIdx) }
        MiniPetInGameHud(
            pet = currentPet,
            isBossLevel = isBossStage,
            combo = combo,
            timeLeft = timeLeft,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = if (isBossStage && activeBoss != null) 116.dp else 58.dp, end = DesignTokens.Space12)
        )

        // 3. Floating 3x COMBO! Golden Pill Badge (Matching Image 1 & 4)
        AnimatedVisibility(
            visible = combo >= 2,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFFFFD700))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFFBEB), Color(0xFFFFE082), Color(0xFFF59E0B), Color(0xFFB45309))
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFD54F), Color(0xFF92400E))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "✨", fontSize = 13.sp)
                    Text(
                        text = "${combo}x COMBO!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B2E00),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Live High Score Celebration Banner (Triggers on new record during gameplay)
        AnimatedVisibility(
            visible = isNewHighScore && gameState == GameState.PLAYING,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 98.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFD700))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .border(2.dp, Color(0xFFFFFBEB), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "🏆", fontSize = 16.sp)
                    Text(
                        text = "NEW HIGH SCORE: ${String.format("%,d", score)} PTS!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // 4. Candy Crush Style "SWEET!" Floating Praise Banner (Matching Image 1)
        AnimatedVisibility(
            visible = candyPraise != null,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(24.dp, RoundedCornerShape(36.dp), spotColor = Color(0xFFFF5D8F))
                    .clip(RoundedCornerShape(36.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xE6FF5D8F),
                                Color(0xF2FB923C),
                                Color(0xE6FFC93C)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFE082), Color(0xFFFF2A75))
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
                    .padding(horizontal = 32.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Deep 3D Shadow Layer
                    Text(
                        text = candyPraise ?: "SWEET!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B2E00),
                        modifier = Modifier.offset(y = 4.dp)
                    )
                    // Orange Outline Bevel
                    Text(
                        text = candyPraise ?: "SWEET!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEA580C),
                        modifier = Modifier.offset(y = 2.dp)
                    )
                    // Creamy Yellow-to-Peach Gradient Face
                    Text(
                        text = candyPraise ?: "SWEET!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFFBEB),
                                    Color(0xFFFEF08A),
                                    Color(0xFFFDE047),
                                    Color(0xFFF59E0B)
                                )
                            )
                        )
                    )
                }
            }
        }

        // Active Powerups Badges (Floating above bottom dock)
        if (isFreezeActive || isMultiplierActive || (isHammerSlideActive && hammerSlideTimeRemaining > 0f)) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, bottom = 86.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isHammerSlideActive && hammerSlideTimeRemaining > 0f) {
                    PowerupBadge(
                        icon = "🔨⚡",
                        label = "HAMMER SLIDE ${String.format(java.util.Locale.US, "%.1f", hammerSlideTimeRemaining)}s",
                        color = Color(0xFFFFD700)
                    )
                }
                if (isFreezeActive) {
                    PowerupBadge(icon = "❄️", label = "SLOW MOTION", color = Color(0xFF38BDF8))
                }
                if (isMultiplierActive) {
                    PowerupBadge(icon = "⭐", label = "2X POINTS", color = Color(0xFFFBBF24))
                }
            }
        }

        // 5. High-End 3-Orb Power-Up & Booster Dock (Matching Image 1)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0x99000000))
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xD90D1326))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x5538BDF8), Color(0x22A855F7), Color(0x11FFFFFF))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. BOMB ORB BUTTON
                GameOrbButton(
                    icon = "💣",
                    label = "BOMB",
                    badgeText = "READY",
                    badgeBgColor = Color(0xFF3B82F6),
                    orbGradient = listOf(Color(0xFFA855F7), Color(0xFF7E22CE), Color(0xFF3B0764)),
                    glowColor = Color(0xFFA855F7),
                    onClick = { onTriggerBooster(InGameBooster.COLOR_BOMB) },
                    testTag = "booster_bomb"
                )

                // 2. LIGHTNING ORB BUTTON
                GameOrbButton(
                    icon = "⚡",
                    label = "LIGHTNING",
                    badgeText = "READY",
                    badgeBgColor = Color(0xFF0284C7),
                    orbGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
                    glowColor = Color(0xFF00E5FF),
                    onClick = { onTriggerBooster(InGameBooster.THUNDER_STRIKE) },
                    testTag = "booster_lightning"
                )

                // 3. FREEZE ORB BUTTON
                GameOrbButton(
                    icon = "❄️",
                    label = "FREEZE",
                    badgeText = if (isFreezeActive) "ACTIVE" else "READY",
                    badgeBgColor = Color(0xFF38BDF8),
                    orbGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF082F49)),
                    glowColor = Color(0xFF38BDF8),
                    onClick = { onTriggerBooster(InGameBooster.FREEZE_BLIZZARD) },
                    testTag = "booster_freeze"
                )
            }
        }



        // Animated Resume 3-2-1-GO Countdown Overlay
        if (resumeCountdown != null) {
            ResumeCountdownOverlay(count = resumeCountdown!!)
        }

        // Dialog Overlays (Paused, Victory, Failed)
        when (gameState) {
            GameState.PAUSED -> {
                // Game Paused Fullscreen Glass Overlay (Matching Image 1)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xD9060A18))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onResume() }
                        .testTag("pause_dialog_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    // Ambient celebratory blurred balloons in background
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        drawCircle(Color(0x2222C55E), radius = 90.dp.toPx(), center = Offset(w * 0.15f, h * 0.25f))
                        drawCircle(Color(0x20F43F5E), radius = 110.dp.toPx(), center = Offset(w * 0.85f, h * 0.20f))
                        drawCircle(Color(0x22FBBF24), radius = 80.dp.toPx(), center = Offset(w * 0.82f, h * 0.75f))
                        drawCircle(Color(0x2238BDF8), radius = 95.dp.toPx(), center = Offset(w * 0.18f, h * 0.70f))
                    }

                    // Main Glass Capsule Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .shadow(28.dp, RoundedCornerShape(32.dp), spotColor = Color(0x77FFD700))
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xF50E1328))
                            .border(
                                width = 1.8.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color(0x99FFD54F),
                                        Color(0x55A855F7),
                                        Color(0x3338BDF8),
                                        Color(0x66FFD54F)
                                    )
                                ),
                                shape = RoundedCornerShape(32.dp)
                            )
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { /* intercept clicks */ }
                            .padding(horizontal = 24.dp, vertical = 28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Top Centered 3D Golden Star Medallion
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .shadow(12.dp, CircleShape, spotColor = Color(0xFFFFD700))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(0xFFFFFBEB), Color(0xFFFFD54F), Color(0xFFD97706), Color(0xFF78350F))
                                        )
                                    )
                                    .border(2.dp, Color(0xFFFFFBEB), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "⭐", fontSize = 28.sp)
                            }

                            // Title: ॥ GAME PAUSED ॥
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "॥",
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xAAFFFFFF)
                                )
                                Text(
                                    text = "GAME PAUSED",
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "॥",
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xAAFFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 1. RESUME PILL BUTTON (Lush Emerald Green)
                            PausePillButton(
                                text = "RESUME",
                                icon = "▶",
                                gradient = listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF15803D)),
                                shadowSpotColor = Color(0xFF22C55E),
                                onClick = onResume,
                                testTag = "resume_game_button"
                            )

                            // 2. SETTINGS PILL BUTTON (Royal Cerulean Blue)
                            PausePillButton(
                                text = "SETTINGS",
                                icon = "⚙️",
                                gradient = listOf(Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF1D4ED8)),
                                shadowSpotColor = Color(0xFF38BDF8),
                                onClick = onOpenSettings,
                                testTag = "pause_settings_button"
                            )

                            // 3. RESTART PILL BUTTON (Warm Tangerine Orange)
                            PausePillButton(
                                text = "RESTART",
                                icon = "🔄",
                                gradient = listOf(Color(0xFFFDBA74), Color(0xFFFB923C), Color(0xFFEA580C)),
                                shadowSpotColor = Color(0xFFFB923C),
                                onClick = onRestart,
                                testTag = "restart_game_button"
                            )

                            // 4. MAIN MENU PILL BUTTON (Deep Candy Purple)
                            PausePillButton(
                                text = "MAIN MENU",
                                icon = "🏠",
                                gradient = listOf(Color(0xFFC084FC), Color(0xFFA855F7), Color(0xFF7C3AED)),
                                shadowSpotColor = Color(0xFFA855F7),
                                onClick = onQuitToMenu,
                                testTag = "quit_game_button"
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtle Footer Caption
                            Text(
                                text = "Version 1.4.2 • Tap outside to resume",
                                fontFamily = NunitoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0x66FFFFFF),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            GameState.LEVELCLEAR -> {
                VictoryStageDialog(
                    level = currentLevel,
                    stars = earnedStars,
                    score = score,
                    coinsEarned = earnedCoins,
                    gemsEarned = earnedGems,
                    isNewHighScore = isNewHighScore,
                    previousHighScore = previousHighScore,
                    onNextLevel = onNextLevel,
                    onReplay = onRestart,
                    onMap = onStageMenu,
                    onMainMenu = onQuitToMenu
                )
            }
            GameState.GAMEOVER -> {
                FailedStageDialog(
                    level = currentLevel,
                    missingPops = (targetPops - poppedCount).coerceAtLeast(1),
                    userCoins = earnedCoins,
                    missedCount = missedCount,
                    maxMissesAllowed = maxMissesAllowed,
                    timeLimit = timeLeft,
                    reason = gameOverReason,
                    onPlayOn = onRestart,
                    onRetry = onRestart,
                    onWatchAdToRevive = {
                        activity?.let { act ->
                            AdMobManager.showRewardedAd(
                                act,
                                onRewardEarned = {
                                    onRestart()
                                }
                            )
                        }
                    },
                    onMap = onStageMenu,
                    onMainMenu = onQuitToMenu
                )
            }
            else -> {}
        }

        // Stage-to-Stage Transition Portal (Between the 200 levels)
        if (isTransitioningLevel && transitionTargetLevel != null) {
            val targetLevelData = GameConstants.CAMPAIGN_LEVELS.getOrNull(transitionTargetLevel - 1)
            val zoneIndex = ((transitionTargetLevel - 1) / 10).coerceIn(0, GameConstants.WORLD_ZONES.size - 1)
            val zone = GameConstants.WORLD_ZONES.getOrNull(zoneIndex)
            LevelTransitionPortal(
                fromLevel = currentLevel,
                toLevel = transitionTargetLevel,
                targetPops = targetLevelData?.targetPops ?: (20 + transitionTargetLevel * 3),
                timeLimit = targetLevelData?.timeLimit ?: 45,
                maxMissesAllowed = targetLevelData?.maxMissesAllowed ?: 4,
                isBossStage = (transitionTargetLevel % 10 == 0),
                isFrenzyStage = (transitionTargetLevel % 5 == 0 && transitionTargetLevel % 10 != 0),
                zone = zone,
                onAnimationFinished = onFinishLevelTransition
            )
        }
    }
}

@Composable
private fun PowerupBadge(icon: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xCC0F172A))
            .border(1.5.dp, color, RoundedCornerShape(18.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun BoosterPowerUpButton(
    booster: InGameBooster,
    count: Int,
    boosterMultiplier: Int,
    hasItems: Boolean,
    coinCost: Int,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "booster_btn_press"
    )

    val boosterAccentColor = when (booster) {
        InGameBooster.THUNDER_STRIKE -> Color(0xFF38BDF8)
        InGameBooster.COLOR_BOMB -> Color(0xFFFF007F)
        InGameBooster.HAMMER_SMASH -> Color(0xFFFBBF24)
        InGameBooster.FREEZE_BLIZZARD -> Color(0xFF00E5FF)
        InGameBooster.TORNADO_VORTEX -> Color(0xFF34D399)
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .size(50.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("booster_${booster.id}"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Enhanced Circular Booster Button with Double Border Lines & Arcade Glow
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = if (isAvailable) 6.dp else 1.dp,
                    shape = CircleShape,
                    spotColor = if (isAvailable) boosterAccentColor else Color.Transparent
                )
                .clip(CircleShape)
                .background(
                    if (isAvailable) {
                        when (booster) {
                            InGameBooster.THUNDER_STRIKE -> Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
                            InGameBooster.COLOR_BOMB -> Brush.radialGradient(listOf(Color(0xFFFF007F), Color(0xFF7928CA)))
                            InGameBooster.HAMMER_SMASH -> Brush.radialGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706)))
                            InGameBooster.FREEZE_BLIZZARD -> Brush.radialGradient(listOf(Color(0xFFA5F3FC), Color(0xFF0891B2)))
                            InGameBooster.TORNADO_VORTEX -> Brush.radialGradient(listOf(Color(0xFF6EE7B7), Color(0xFF059669)))
                        }
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    }
                )
                // Enhanced Outer Glowing Neon Border Line
                .border(
                    width = 2.dp,
                    color = if (isAvailable) boosterAccentColor else Color(0xFF475569),
                    shape = CircleShape
                )
                .padding(2.dp)
                // Enhanced Inner Specular Metallic Bevel Line
                .border(
                    width = 1.dp,
                    color = if (isAvailable) Color(0x88FFFFFF) else Color(0x33FFFFFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Top Crescent Gloss Arc
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp)
                    .width(22.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0x44FFFFFF))
            )

            Text(text = booster.icon, fontSize = 21.sp)
        }

        // 2. User-Friendly Numbering / Cost Badge pinned cleanly in the Top-Right Corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 3.dp, y = (-2).dp)
                .shadow(4.dp, if (hasItems) CircleShape else RoundedCornerShape(8.dp))
                .clip(if (hasItems) CircleShape else RoundedCornerShape(8.dp))
                .background(
                    if (hasItems) Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857)))
                    else Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309)))
                )
                .border(
                    width = 1.2.dp,
                    color = Color.White,
                    shape = if (hasItems) CircleShape else RoundedCornerShape(8.dp)
                )
                .padding(horizontal = if (hasItems) 5.dp else 4.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (hasItems) {
                    if (boosterMultiplier > 1) "$count(x$boosterMultiplier)" else "$count"
                } else {
                    "${coinCost}🪙"
                },
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SkillPowerUpButton(
    skill: PlayerSkill,
    skillEnergy: Float,
    skillCooldownRemaining: Float,
    onActivateSkill: () -> Unit
) {
    val isReady = skillCooldownRemaining <= 0f && skillEnergy >= 95f
    val infiniteTransition = rememberInfiniteTransition(label = "skill_pulse")
    val readyScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isReady) 1.08f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skill_scale"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "skill_press"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .scale(pressScale)
            .alpha(if (isReady) 1.0f else 0.65f)
            .clickable(
                enabled = isReady,
                interactionSource = interactionSource,
                indication = null,
                onClick = onActivateSkill
            )
            .testTag("skill_action_button")
    ) {
        Box(
            modifier = Modifier
                .scale(readyScale)
                .size(46.dp)
                .shadow(
                    elevation = if (isReady) DesignTokens.ElevationActive else 0.dp,
                    shape = CircleShape,
                    spotColor = DesignTokens.PrimaryBrand
                )
                .clip(CircleShape)
                .background(
                    if (isReady) {
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFFBEB),
                                DesignTokens.PrimaryBrandBright,
                                DesignTokens.PrimaryBrand,
                                DesignTokens.PrimaryBrandDark
                            )
                        )
                    } else {
                        Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    }
                )
                .border(
                    width = if (isReady) 2.dp else 1.5.dp,
                    color = if (isReady) Color(0xFFFFFBEB) else Color(0xFF475569),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (skillCooldownRemaining > 0f) {
                Text(
                    text = "${skillCooldownRemaining.toInt() + 1}s",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            } else if (isReady) {
                Text(text = skill.icon, fontSize = 23.sp)
            } else {
                Text(
                    text = "${skillEnergy.toInt()}%",
                    fontSize = DesignTokens.SizeMicro,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFCBD5E1)
                )
            }
        }

        // Small Charge Progress Bar
        Box(
            modifier = Modifier
                .offset(y = (-3).dp)
                .width(36.dp)
                .height(5.dp)
                .clip(DesignTokens.ShapeSmall)
                .background(Color(0x66000000))
                .border(0.5.dp, Color.White.copy(alpha = 0.5f), DesignTokens.ShapeSmall)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(skillEnergy / 100f)
                    .background(
                        Brush.horizontalGradient(
                            if (isReady) listOf(DesignTokens.SuccessGreenBright, DesignTokens.SuccessGreen)
                            else listOf(DesignTokens.SecondaryAccent, DesignTokens.SecondaryAccentDeep)
                        )
                    )
            )
        }
    }
}

/**
 * Boss Battle Health Bar:
 * Displays dynamic HP status for the Titan Boss in Stage 10, 20, 30...
 */
@Composable
private fun BossBattleHealthBar(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier
) {
    val hpFraction = (currentHp.toFloat() / maxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val animatedHp by animateFloatAsState(
        targetValue = hpFraction,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "boss_hp_bar"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Space16)
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFEF4444))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xF00F172A))
            .border(1.5.dp, Color(0xFFF87171), RoundedCornerShape(16.dp))
            .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space8)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "👑", fontSize = 18.sp)
                    Text(
                        text = "TITAN BOSS ENCOUNTER",
                        fontSize = DesignTokens.SizeMicro,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFCA5A5),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "$currentHp / $maxHp HP",
                    fontSize = DesignTokens.SizeMicro,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Health Bar Track & Fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedHp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFEF4444),
                                    Color(0xFFF97316),
                                    Color(0xFFFBBF24)
                                )
                            )
                        )
                )
            }
        }
    }
}

/**
 * 3D Glossy Circular Power-Up Orb Button from Image 1.
 */
@Composable
private fun GameOrbButton(
    icon: String,
    label: String,
    badgeText: String,
    badgeBgColor: Color,
    orbGradient: List<Color>,
    glowColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "orb_scale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(12.dp, CircleShape, spotColor = glowColor)
                .clip(CircleShape)
                .background(Brush.radialGradient(orbGradient))
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.85f), glowColor, Color.White.copy(alpha = 0.25f))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Top specular shine crescent
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.60f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.45f
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.05f),
                    size = androidx.compose.ui.geometry.Size(w * 0.70f, h * 0.35f)
                )
            }

            Text(text = icon, fontSize = 26.sp)

            // Top-right Cooldown / Status Pill Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBgColor)
                    .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = badgeText,
                    fontFamily = NunitoFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Text(
            text = label,
            fontFamily = Baloo2FontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun PausePillButton(
    text: String,
    icon: String,
    gradient: List<Color>,
    shadowSpotColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f, label = "pill_press")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = shadowSpotColor)
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(gradient))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.8f), Color.White.copy(alpha = 0.15f))),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .testTag(testTag)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Specular highlight crescent
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.85f)
                .height(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Icon in translucent circular badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontFamily = Baloo2FontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }
    }
}
