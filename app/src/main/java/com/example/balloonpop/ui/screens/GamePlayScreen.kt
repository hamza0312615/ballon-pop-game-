package com.example.balloonpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
    boosterCooldowns: Map<String, Float> = emptyMap(),
    candyPraise: String? = null,
    // Pass 8: persisted campaign win streak (victory-dialog line) and the
    // kindness-assist flag for the current stage (fail-dialog help line).
    winStreak: Int = 0,
    assistUnlocked: Boolean = false,
    onTriggerBooster: (InGameBooster) -> Unit = {},
    onBuyBooster: (String) -> Unit = {},
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
    onPlayOnRevive: (Boolean) -> Unit = {},
    // Pass 5: finalize an endless run (saves its best) — the only way a
    // Zen run can end, and the clean quit path for the other endless modes.
    onFinishRun: () -> Unit = {},
    isNewHighScore: Boolean = false,
    previousHighScore: Int = 0,
    isTransitioningLevel: Boolean = false,
    transitionTargetLevel: Int? = null,
    onFinishLevelTransition: () -> Unit = {},
    showComboHintBanner: Boolean = false,
    // Pass 9: Stage Objective Variety & Steady Hands Bonus
    objectiveType: StageObjectiveType = StageObjectiveType.STANDARD,
    focusColors: List<Color> = emptyList(),
    steadyHandsBonusEarned: Int = 0,
    steadyHandsMissedCount: Int = 0,
    modifier: Modifier = Modifier
) {
    var resumeCountdown by remember { mutableStateOf<Int?>(null) }

    // In-game score counter scale punch when points land
    val scoreScale = remember { Animatable(1f) }
    LaunchedEffect(score) {
        if (score > 0) {
            scoreScale.snapTo(1.22f)
            scoreScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    // Pass 4: tapping a 0-charge booster opens the one-tap coin buy dialog
    // instead of triggering (and instead of leaving the level for the shop).
    var boosterBuyTarget by remember { mutableStateOf<InGameBooster?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    // Pass 8 (research §5 ad policy): interstitials fire ONLY after a
    // stage WIN — never after a loss (Lose + Ad stacks a second negative
    // on frustration and is the genre's top churn pattern) — and never
    // before stage 10. The manager still enforces the 180s cooldown and
    // 60s post-ad gap, preloads the next unit after each show, and no-ops
    // when no ad is ready.
    LaunchedEffect(gameState) {
        if (gameState == GameState.LEVELCLEAR && gameMode == GameMode.CAMPAIGN && currentLevel >= 10) {
            activity?.let { AdMobManager.showInterstitialAd(it) }
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
                .shadow(12.dp, RoundedCornerShape(26.dp), spotColor = Color(0x66171233))
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xD9171233))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x88FFD166), Color(0x33B8A9D9), Color(0x22FFFFFF))
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
                            drawCircle(Color(0x66C93A5E), radius = r * 0.14f, center = Offset(cx - r * 0.45f, cy + 4f))
                            drawCircle(Color(0x66C93A5E), radius = r * 0.14f, center = Offset(cx + r * 0.45f, cy + 4f))

                            // Sweet smile
                            val mouthPath = Path().apply {
                                moveTo(cx - 3.5f, cy + 3.5f)
                                quadraticTo(cx, cy + 7f, cx + 3.5f, cy + 3.5f)
                            }
                            drawPath(mouthPath, color = Color(0xFF1E0A12), style = Stroke(width = 1.5f, cap = StrokeCap.Round))
                        }
                    }

                    // Pass 5 (amendment 7): the goal reads as a picture pill —
                    // balloon count in its own plum pill, never a sentence.
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x8C171233))
                            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (gameMode == GameMode.CAMPAIGN) "🎈 $poppedCount/$targetPops" else "🎈 $poppedCount",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            // Colour Focus: show the two target color dots beside the counter
                            if (gameMode == GameMode.CAMPAIGN && objectiveType == StageObjectiveType.COLOUR_FOCUS && focusColors.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    focusColors.forEach { c ->
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                                .border(1.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                        )
                                    }
                                }
                            }
                        }

                        // Objective type subtitle tag if not standard
                        if (gameMode == GameMode.CAMPAIGN && objectiveType != StageObjectiveType.STANDARD) {
                            val objectiveTag = when (objectiveType) {
                                StageObjectiveType.COLOUR_FOCUS -> "Focus: Only 🔴🔵"
                                StageObjectiveType.NO_MISS_BONUS -> "Goal: ≤2 Miss (+30🪙)"
                                StageObjectiveType.BOMB_SURVIVAL -> "💣 Bomb Survival"
                                else -> ""
                            }
                            Text(
                                text = objectiveTag,
                                fontFamily = Baloo2FontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD166)
                            )
                        }

                        // Pass 8 FTUE: the Miss pill stays hidden for
                        // stages 1–2 (progressive disclosure; stage 1 can't
                        // be failed by misses anyway) and appears from
                        // stage 3 onward. Custom challenges (level 0) keep it.
                        if (gameMode == GameMode.CAMPAIGN && currentLevel !in 1..2) {
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

                // Center: Score — one large readable number with scale punch animation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scoreScale.value)
                ) {
                    Text(
                        text = "SCORE",
                        fontFamily = NunitoFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format("%,d", score),
                        fontFamily = Baloo2FontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700)
                    )
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
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (timeLeft <= 10) StoryBerry else Color.White
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

        // One-time Combo Hint Banner for Stages 1–2
        AnimatedVisibility(
            visible = showComboHintBanner && gameState == GameState.PLAYING,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(12.dp, DesignTokens.ShapePill, spotColor = Color(0x66000000))
                    .clip(DesignTokens.ShapePill)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF2E1922),
                                Color(0xFF4A1A35),
                                Color(0xFF2E1922)
                            )
                        )
                    )
                    .border(2.dp, StoryGold, DesignTokens.ShapePill)
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎈✨", fontSize = 16.sp)
                    Text(
                        text = "Pop 3 of the same colour for a combo!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE68A)
                    )
                }
            }
        }

        // Pass 5 (audit L3): endless modes get an explicit FINISH & SAVE
        // exit. Zen has no timer and no lives, so without this its BEST
        // could never be recorded; the other endless modes gain a clean
        // quit that keeps the run's score.
        if (gameMode != GameMode.CAMPAIGN && gameState == GameState.PLAYING) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 80.dp, start = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xCC171233))
                    .border(1.5.dp, Color(0xFFFFC93C), RoundedCornerShape(22.dp))
                    .clickable { onFinishRun() }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "🏁 FINISH & SAVE",
                    fontFamily = Baloo2FontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFC93C)
                )
            }
        }

        // Mini Pet In-Game Companion cheering the player on!
        // 3. Floating COMBO & Multiplier Golden Pill Badge (Merged cleanly into single pill)
        AnimatedVisibility(
            visible = (combo >= 2 || isMultiplierActive) && gameState == GameState.PLAYING,
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
                    val comboLabel = if (combo >= 2 && isMultiplierActive) {
                        "${combo}x COMBO • 2X ⭐"
                    } else if (combo >= 2) {
                        "${combo}x COMBO!"
                    } else {
                        "2X POINTS ⭐"
                    }
                    Text(
                        text = comboLabel,
                        fontFamily = Baloo2FontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5B2E00),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Live High Score Celebration Banner (Compact top header position)
        AnimatedVisibility(
            visible = isNewHighScore && gameState == GameState.PLAYING,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFFFD700))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFB45309), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFFFBEB), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "🏆", fontSize = 13.sp)
                    Text(
                        text = "NEW HIGH SCORE: ${String.format("%,d", score)} PTS!",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // 4. Candy Style Floating Praise Banner (Compact, docked cleanly at top edge, never blocking balloons)
        AnimatedVisibility(
            visible = candyPraise != null && gameState == GameState.PLAYING,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 62.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFF5D8F))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xF2FF5D8F),
                                Color(0xF2FB923C),
                                Color(0xF2FFC93C)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFFFE082), Color(0xFFC93A5E))
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = candyPraise ?: "SWEET!",
                    fontFamily = Baloo2FontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Active Powerups Badges (Floating directly centered above bottom dock)
        if ((isFreezeActive || isMultiplierActive || (isHammerSlideActive && hammerSlideTimeRemaining > 0f)) && gameState == GameState.PLAYING) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp),
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

        // 5. High-End 5-Orb Power-Up & Booster Dock (Pass 5: all five
        //    boosters — HAMMER and VORTEX were sold in the shop but had no
        //    orb here, so they could never be fired in a stage.)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0x99000000))
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xD9171233))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color(0x55FFD166), Color(0x22B8A9D9), Color(0x11FFFFFF))
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
                val bombCount = boosterCounts["booster_color_bomb"] ?: 3
                val bombCd = boosterCooldowns["booster_color_bomb"] ?: 0f
                GameOrbButton(
                    icon = "💣",
                    label = "BOMB",
                    count = bombCount,
                    coins = coins,
                    cooldownRemaining = bombCd,
                    badgeBgColor = if (bombCount > 0) Color(0xFF3B82F6) else Color(0xFFF59E0B),
                    orbGradient = listOf(Color(0xFFA855F7), Color(0xFF7E22CE), Color(0xFF3B0764)),
                    glowColor = Color(0xFFA855F7),
                    onClick = {
                        if (bombCount > 0) onTriggerBooster(InGameBooster.COLOR_BOMB)
                        else boosterBuyTarget = InGameBooster.COLOR_BOMB
                    },
                    testTag = "booster_bomb"
                )

                // 2. LIGHTNING ORB BUTTON
                val thunderCount = boosterCounts["booster_thunder"] ?: 3
                val thunderCd = boosterCooldowns["booster_thunder"] ?: 0f
                GameOrbButton(
                    icon = "⚡",
                    label = "LIGHTNING",
                    count = thunderCount,
                    coins = coins,
                    cooldownRemaining = thunderCd,
                    badgeBgColor = if (thunderCount > 0) Color(0xFF0284C7) else Color(0xFFF59E0B),
                    orbGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
                    glowColor = Color(0xFF4A90C4),
                    onClick = {
                        if (thunderCount > 0) onTriggerBooster(InGameBooster.THUNDER_STRIKE)
                        else boosterBuyTarget = InGameBooster.THUNDER_STRIKE
                    },
                    testTag = "booster_lightning"
                )

                // 3. FREEZE ORB BUTTON
                val freezeCount = boosterCounts["booster_freeze"] ?: 3
                val freezeCd = boosterCooldowns["booster_freeze"] ?: 0f
                GameOrbButton(
                    icon = "❄️",
                    label = "FREEZE",
                    count = freezeCount,
                    coins = coins,
                    cooldownRemaining = freezeCd,
                    badgeBgColor = if (isFreezeActive) Color(0xFF06B6D4) else if (freezeCount > 0) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                    orbGradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF082F49)),
                    glowColor = Color(0xFF38BDF8),
                    onClick = {
                        if (freezeCount > 0) onTriggerBooster(InGameBooster.FREEZE_BLIZZARD)
                        else boosterBuyTarget = InGameBooster.FREEZE_BLIZZARD
                    },
                    testTag = "booster_freeze"
                )

                // 4. HAMMER ORB BUTTON (Pass 5)
                val hammerCount = boosterCounts["booster_hammer"] ?: 3
                val hammerCd = boosterCooldowns["booster_hammer"] ?: 0f
                GameOrbButton(
                    icon = "🔨",
                    label = "HAMMER",
                    count = hammerCount,
                    coins = coins,
                    cooldownRemaining = hammerCd,
                    badgeBgColor = if (hammerCount > 0) Color(0xFFB7791F) else Color(0xFFF59E0B),
                    orbGradient = listOf(Color(0xFFF6C453), Color(0xFFB7791F), Color(0xFF7B341E)),
                    glowColor = Color(0xFFF6C453),
                    onClick = {
                        if (hammerCount > 0) onTriggerBooster(InGameBooster.HAMMER_SMASH)
                        else boosterBuyTarget = InGameBooster.HAMMER_SMASH
                    },
                    testTag = "booster_hammer"
                )

                // 5. VORTEX ORB BUTTON (Pass 5)
                val vortexCount = boosterCounts["booster_vortex"] ?: 3
                val vortexCd = boosterCooldowns["booster_vortex"] ?: 0f
                GameOrbButton(
                    icon = "🌀",
                    label = "VORTEX",
                    count = vortexCount,
                    coins = coins,
                    cooldownRemaining = vortexCd,
                    badgeBgColor = if (vortexCount > 0) Color(0xFF0E7C61) else Color(0xFFF59E0B),
                    orbGradient = listOf(Color(0xFF2E9E6B), Color(0xFF0E7C61), Color(0xFF14532D)),
                    glowColor = Color(0xFF2E9E6B),
                    onClick = {
                        if (vortexCount > 0) onTriggerBooster(InGameBooster.TORNADO_VORTEX)
                        else boosterBuyTarget = InGameBooster.TORNADO_VORTEX
                    },
                    testTag = "booster_vortex"
                )
            }
        }



        // Pass 4: one-tap booster buy dialog (opened from a 0-charge orb tap)
        val buyTarget = boosterBuyTarget
        if (buyTarget != null && gameState == GameState.PLAYING) {
            BoosterBuyDialog(
                booster = buyTarget,
                coins = coins,
                onBuy = {
                    onBuyBooster(buyTarget.id)
                    boosterBuyTarget = null
                },
                onDismiss = { boosterBuyTarget = null }
            )
        }

        // Animated Resume 3-2-1-GO Countdown Overlay (Guarded against double-rendering during transitions)
        if (resumeCountdown != null && !isTransitioningLevel && gameState == GameState.PLAYING) {
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

                            // Pass 6: pause actions are storybook JewelButtons
                            // on the plum card (was: generic candy pill buttons).
                            // 1. RESUME — SUNSET
                            JewelButton(
                                text = "RESUME",
                                onClick = onResume,
                                style = JewelButtonStyle.SUNSET,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "resume_game_button",
                                leading = {
                                    Text(text = "▶", fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(end = 10.dp))
                                }
                            )

                            // 2. SETTINGS — SKY
                            JewelButton(
                                text = "SETTINGS",
                                onClick = onOpenSettings,
                                style = JewelButtonStyle.SKY,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "pause_settings_button",
                                leading = {
                                    Text(text = "⚙️", fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(end = 10.dp))
                                }
                            )

                            // 3. RESTART — PLUM
                            JewelButton(
                                text = "RESTART",
                                onClick = onRestart,
                                style = JewelButtonStyle.PLUM,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "restart_game_button",
                                leading = {
                                    Text(text = "🔄", fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(end = 10.dp))
                                }
                            )

                            // 4. MAIN MENU — PLUM
                            JewelButton(
                                text = "MAIN MENU",
                                onClick = onQuitToMenu,
                                style = JewelButtonStyle.PLUM,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "quit_game_button",
                                leading = {
                                    Text(text = "🏠", fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(end = 10.dp))
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtle Footer Caption
                            Text(
                                text = "Version 1.4 • Tap outside to resume",
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
                    winStreak = winStreak,
                    steadyHandsBonusEarned = steadyHandsBonusEarned,
                    steadyHandsMissedCount = steadyHandsMissedCount,
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
                    userCoins = coins,
                    missedCount = missedCount,
                    maxMissesAllowed = maxMissesAllowed,
                    timeLimit = timeLeft,
                    reason = gameOverReason,
                    assistUnlocked = assistUnlocked,
                    onPlayOn = { onPlayOnRevive(true) },
                    onRetry = onRestart,
                    onWatchAdToRevive = {
                        activity?.let { act ->
                            AdMobManager.showRewardedAd(
                                act,
                                onRewardEarned = {
                                    onPlayOnRevive(false)
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

/**
 * Pass 4: one-tap booster purchase without leaving the level.
 * Shown when the player taps a booster orb that has 0 charges left.
 * Price comes from the single BoosterPrices constants place.
 */
@Composable
private fun BoosterBuyDialog(
    booster: InGameBooster,
    coins: Int,
    onBuy: () -> Unit,
    onDismiss: () -> Unit
) {
    val price = BoosterPrices.COIN_PRICE
    val canAfford = coins >= price
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .shadow(24.dp, DesignTokens.ShapeLarge, spotColor = Color(0x99000000))
                .clip(DesignTokens.ShapeLarge)
                .background(Color(0xF50E1328))
                .border(1.5.dp, Color(0x55FFD54F), DesignTokens.ShapeLarge)
                .padding(horizontal = DesignTokens.Space24, vertical = DesignTokens.Space24),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
            ) {
                Text(text = booster.icon, fontSize = 40.sp)
                Text(
                    text = booster.displayName,
                    fontFamily = Baloo2FontFamily,
                    fontSize = DesignTokens.SizeH2,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "No charges left",
                    fontFamily = NunitoFontFamily,
                    fontSize = DesignTokens.SizeBody,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                // Price pill
                Box(
                    modifier = Modifier
                        .clip(DesignTokens.ShapePill)
                        .background(Color(0xFF261E05))
                        .border(1.5.dp, Color(0xFFF59E0B), DesignTokens.ShapePill)
                        .padding(horizontal = DesignTokens.Space16, vertical = DesignTokens.Space8)
                ) {
                    Text(
                        text = "+1 charge · $price 🪙",
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.SizeBody,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFEF08A)
                    )
                }
                Text(
                    text = "You have $coins 🪙",
                    fontFamily = NunitoFontFamily,
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) Color(0xFF94A3B8) else Color(0xFFEF4444)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesignTokens.ButtonMinHeight)
                        .clip(DesignTokens.ShapeMedium)
                        .background(
                            if (canAfford) DesignTokens.CtaGradient
                            else Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF334155)))
                        )
                        .clickable(enabled = canAfford) { onBuy() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (canAfford) "BUY" else "NEED $price 🪙",
                        fontFamily = Baloo2FontFamily,
                        fontSize = DesignTokens.SizeH2,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
                Text(
                    text = "Not now",
                    fontFamily = NunitoFontFamily,
                    fontSize = DesignTokens.SizeBody,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .clip(DesignTokens.ShapeSmall)
                        .clickable { onDismiss() }
                        .padding(horizontal = DesignTokens.Space12, vertical = DesignTokens.Space8)
                )
            }
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
        InGameBooster.COLOR_BOMB -> Color(0xFFB03060)
        InGameBooster.HAMMER_SMASH -> Color(0xFFFBBF24)
        InGameBooster.FREEZE_BLIZZARD -> Color(0xFF4A90C4)
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
                            InGameBooster.COLOR_BOMB -> Brush.radialGradient(listOf(Color(0xFFB03060), Color(0xFF7928CA)))
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
    count: Int,
    coins: Int,
    cooldownRemaining: Float = 0f,
    coinCost: Int = 50,
    badgeBgColor: Color,
    orbGradient: List<Color>,
    glowColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isOnCooldown = cooldownRemaining > 0f
    val isAffordable = count > 0 || coins >= coinCost
    val isEnabled = !isOnCooldown && isAffordable

    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isEnabled) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "orb_scale"
    )

    val badgeText = when {
        isOnCooldown -> String.format(java.util.Locale.US, "%.1fs", cooldownRemaining)
        count > 0 -> "x$count"
        else -> "+" // Pass 4: 0 charges -> clear "+" buy affordance (opens buy dialog)
    }

    // Pass 6: the dock badge is ALWAYS a filled dark-plum circle with a
    // 1.5dp gold border and a bold white number (Pass 5's per-state badge
    // colours were unreadable over the orb art in the user's recording).
    // The badge text still shows the cooldown seconds / "+" buy affordance.

    Column(
        modifier = modifier
            .scale(scale)
            .alpha(if (!isAffordable && !isOnCooldown) 0.48f else 1.0f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = true,
                onClick = onClick
            )
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pass 6: outer wrapper Box — the count badge is a child of THIS box
        // (not of the clipped orb circle below), so the orb's CircleShape
        // clip can no longer crop the badge where it overhangs the corner.
        Box(modifier = Modifier.size(56.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, CircleShape, spotColor = if (isEnabled) glowColor else Color.Transparent)
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
            // Specular sheen
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.60f), Color.Transparent),
                        startY = 0f,
                        endY = h * 0.45f
                    ),
                    topLeft = Offset(w * 0.15f, h * 0.05f),
                    size = Size(w * 0.70f, h * 0.35f)
                )

                // Cooldown radial progress sweep overlay
                if (isOnCooldown) {
                    val sweepProgress = (cooldownRemaining / 3.5f).coerceIn(0f, 1f)
                    drawArc(
                        color = Color(0xCC0B0F19),
                        startAngle = -90f,
                        sweepAngle = 360f * sweepProgress,
                        useCenter = true,
                        size = Size(w, h),
                        topLeft = Offset(0f, 0f)
                    )
                }
            }

            Text(text = icon, fontSize = 26.sp)

        }

            // Pass 6: top-right count badge — filled dark-plum circle (22dp)
            // with a 1.5dp gold border and a bold white number, sitting proud
            // of the orb's corner so it reads at a glance mid-game.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF241A4D))
                    .border(1.5.dp, StoryGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    fontFamily = NunitoFontFamily,
                    fontSize = if (isOnCooldown) 8.sp else 11.sp,
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
            color = if (isEnabled) Color.White else Color(0x99FFFFFF),
            letterSpacing = 0.5.sp
        )
    }
}
