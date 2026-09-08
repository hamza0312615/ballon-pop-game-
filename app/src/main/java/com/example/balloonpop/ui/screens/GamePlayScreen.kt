package com.example.balloonpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.*
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.dialogs.*
import com.example.balloonpop.ui.game.GameCanvas
import com.example.balloonpop.ui.theme.*
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
    equippedSkillId: String = "skill_slice",
    skillEnergy: Float = 100f,
    skillCooldownRemaining: Float = 0f,
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
    modifier: Modifier = Modifier
) {
    var showIntroRibbon by remember(currentLevel, gameState) { mutableStateOf(gameState == GameState.PLAYING) }

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
            screenShake = screenShake,
            isFreezeActive = isFreezeActive,
            isMultiplierActive = isMultiplierActive,
            onStepFrame = onStepFrame,
            onPointerInput = onPointerInput,
            onCanvasSizeChanged = onCanvasSizeChanged,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Casual Top HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level / Goal Badge
            Row(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFB45309))
                        )
                    )
                    .border(1.5.dp, Color(0xFFFEF3C7), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "🎈", fontSize = 14.sp)
                Text(
                    text = if (gameMode == GameMode.CAMPAIGN) "$poppedCount/$targetPops" else "$poppedCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Score Badge (Center)
            Box(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF075985))
                        )
                    )
                    .border(1.5.dp, Color(0xFFBAE6FD), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format("%,d", score),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Time / Lives & Pause
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (gameMode == GameMode.CAMPAIGN || gameMode == GameMode.TIME_RUSH) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(18.dp))
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (timeLeft <= 10) listOf(Color(0xFFEF4444), Color(0xFF991B1B))
                                    else listOf(Color(0xFF10B981), Color(0xFF047857))
                                )
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(18.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏱️ ${timeLeft}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Pause Button (Wooden Style)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFB45309))
                            )
                        )
                        .border(1.5.dp, Color(0xFFFEF3C7), CircleShape)
                        .clickable { onPause() }
                        .testTag("pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⏸️", fontSize = 16.sp)
                }
            }
        }

        // Combo Floating Alert
        AnimatedVisibility(
            visible = combo >= 2,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 58.dp)
        ) {
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFACC15), Color(0xFFFB923C), Color(0xFFF43F5E))
                        )
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${combo}x COMBO! 🔥",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        // Bottom Skill Box & Energy Gauge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
        ) {
            val isReady = skillCooldownRemaining <= 0f && skillEnergy >= 10f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(6.dp, CircleShape, spotColor = Color(0xFFF59E0B))
                        .clip(CircleShape)
                        .background(
                            if (isReady) Brush.radialGradient(
                                listOf(Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFD97706))
                            ) else Brush.radialGradient(listOf(Color(0xFF6B7280), Color(0xFF374151)))
                        )
                        .border(
                            2.5.dp,
                            if (isReady) Color(0xFFFFFBEB) else Color(0xFF9CA3AF),
                            CircleShape
                        )
                        .clickable(enabled = isReady) { onActivateSkill() }
                        .testTag("skill_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (skillCooldownRemaining > 0f) {
                        Text(
                            text = "${skillCooldownRemaining.toInt() + 1}s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    } else {
                        Text(text = equippedSkill.icon, fontSize = 24.sp)
                    }
                }

                // Energy Bar
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x66000000))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(skillEnergy / 100f)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFDE047), Color(0xFF22C55E))
                                )
                            )
                    )
                }

                Text(
                    text = equippedSkill.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Active Powerups Badges at Bottom Left
        if (isFreezeActive || isMultiplierActive) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isFreezeActive) {
                    PowerupBadge(icon = "❄️", label = "SLOW MOTION", color = Color(0xFF38BDF8))
                }
                if (isMultiplierActive) {
                    PowerupBadge(icon = "⭐", label = "2X POINTS", color = Color(0xFFFBBF24))
                }
            }
        }

        // Stage Intro Goal Ribbon Dropdown
        if (showIntroRibbon && gameMode == GameMode.CAMPAIGN) {
            StageIntroRibbon(
                level = currentLevel,
                targetPops = targetPops,
                targetScore = targetScore,
                onDismiss = { showIntroRibbon = false }
            )
        }

        // Dialog Overlays (Paused, Victory, Failed)
        when (gameState) {
            GameState.PAUSED -> {
                AmberDialogFrame(
                    title = "GAME PAUSED",
                    onClose = onResume
                ) {
                    MascotCharacter(mood = MascotMood.NEUTRAL, size = 56.dp)

                    JellyButton(
                        text = "RESUME 🎮",
                        onClick = onResume,
                        colorVariant = JellyColorVariant.GREEN,
                        testTag = "resume_game_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    JellyButton(
                        text = "SETTINGS ⚙️",
                        onClick = onOpenSettings,
                        colorVariant = JellyColorVariant.BLUE,
                        testTag = "pause_settings_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    JellyButton(
                        text = "RESTART 🔄",
                        onClick = onRestart,
                        colorVariant = JellyColorVariant.ORANGE,
                        testTag = "restart_game_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    JellyButton(
                        text = "MAIN MENU 🏠",
                        onClick = onQuitToMenu,
                        colorVariant = JellyColorVariant.PURPLE,
                        testTag = "quit_game_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            GameState.LEVELCLEAR -> {
                VictoryStageDialog(
                    level = currentLevel,
                    stars = earnedStars,
                    score = score,
                    coinsEarned = earnedCoins,
                    gemsEarned = earnedGems,
                    onNextLevel = onNextLevel,
                    onReplay = onRestart,
                    onMap = onStageMenu
                )
            }
            GameState.GAMEOVER -> {
                FailedStageDialog(
                    level = currentLevel,
                    missingPops = (targetPops - poppedCount).coerceAtLeast(1),
                    userCoins = earnedCoins,
                    onPlayOn = onRestart,
                    onRetry = onRestart,
                    onMap = onQuitToMenu
                )
            }
            else -> {}
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
