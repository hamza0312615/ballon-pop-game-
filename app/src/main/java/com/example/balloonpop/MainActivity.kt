package com.example.balloonpop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.GameState
import com.example.balloonpop.ui.components.TopBar
import com.example.balloonpop.ui.dialogs.SettingsDialog
import com.example.balloonpop.ui.screens.*
import com.example.balloonpop.ui.theme.BalloonPopTheme
import com.example.balloonpop.viewmodel.CurrentScreen
import com.example.balloonpop.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BalloonPopTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Active background theme colors
                val currentTheme = GameConstants.SHOP_THEMES.find { it.id == uiState.equippedThemeId }
                    ?: GameConstants.SHOP_THEMES.first()
                val bgBrush = Brush.linearGradient(currentTheme.gradientColors)

                val equippedFrame = GameConstants.SHOP_FRAMES.find { it.id == uiState.equippedFrameId }
                    ?: GameConstants.SHOP_FRAMES.first()

                // Handle Back Button
                BackHandler(enabled = uiState.isSettingsOpen || uiState.currentScreen != CurrentScreen.HOME) {
                    if (uiState.isSettingsOpen) {
                        viewModel.closeSettings()
                    } else if (uiState.currentScreen == CurrentScreen.GAMEPLAY) {
                        if (uiState.gameState == GameState.PLAYING) {
                            viewModel.pauseGame()
                        } else {
                            viewModel.navigateTo(CurrentScreen.HOME)
                        }
                    } else {
                        viewModel.clearBannerMessage()
                        viewModel.navigateTo(CurrentScreen.HOME)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgBrush)
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Persistent Top Bar on non-gameplay and non-home screens
                            if (uiState.currentScreen != CurrentScreen.GAMEPLAY && uiState.currentScreen != CurrentScreen.HOME && uiState.currentScreen != CurrentScreen.CAMPAIGN_MAP) {
                                TopBar(
                                    coins = uiState.coins,
                                    gems = uiState.gems,
                                    lives = uiState.lives,
                                    onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                    onOpenSettings = { viewModel.openSettings() }
                                )
                            }

                            // Active Screen Container
                            Box(modifier = Modifier.weight(1f)) {
                                when (uiState.currentScreen) {
                                    CurrentScreen.HOME -> {
                                        HomeScreen(
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            lives = uiState.lives,
                                            onOpenCampaign = { viewModel.navigateTo(CurrentScreen.CAMPAIGN_MAP) },
                                            onOpenSkillVault = { viewModel.navigateTo(CurrentScreen.SKILL_VAULT) },
                                            onOpenEndless = { viewModel.navigateTo(CurrentScreen.ENDLESS_SELECT) },
                                            onOpenDailySpin = { viewModel.navigateTo(CurrentScreen.DAILY_WHEEL) },
                                            onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                            onOpenQuests = { viewModel.navigateTo(CurrentScreen.QUESTS) },
                                            onOpenSettings = { viewModel.openSettings() }
                                        )
                                    }
                                    CurrentScreen.SKILL_VAULT -> {
                                        SkillVaultScreen(
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            equippedSkillId = uiState.equippedSkillId,
                                            unlockedSkills = uiState.unlockedSkills,
                                            skillLevels = uiState.skillLevels,
                                            onEquipSkill = { skillId -> viewModel.equipSkill(skillId) },
                                            onUnlockSkill = { skill -> viewModel.unlockSkill(skill) },
                                            onUpgradeSkill = { skill -> viewModel.upgradeSkill(skill) },
                                            onBack = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.CAMPAIGN_MAP -> {
                                        CampaignScreen(
                                            completedLevels = uiState.completedLevels,
                                            claimedMilestoneChests = uiState.claimedMilestoneChests,
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            onSelectLevel = { levelNum, _ ->
                                                viewModel.startCampaignStage(levelNum)
                                            },
                                            onClaimMilestoneChest = { milestoneLevel ->
                                                viewModel.claimMilestoneChest(milestoneLevel)
                                            },
                                            onOpenSkillVault = { viewModel.navigateTo(CurrentScreen.SKILL_VAULT) },
                                            onOpenDailySpin = { viewModel.navigateTo(CurrentScreen.DAILY_WHEEL) },
                                            onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                            onOpenQuests = { viewModel.navigateTo(CurrentScreen.QUESTS) },
                                            onOpenSettings = { viewModel.openSettings() },
                                            onClose = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.ENDLESS_SELECT -> {
                                        EndlessModeScreen(
                                            onStartMode = { mode ->
                                                viewModel.startEndlessMode(mode)
                                            },
                                            onClose = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.SHOP, CurrentScreen.DIAMOND_STORE -> {
                                        ShopScreen(
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            equippedHammerId = uiState.equippedHammerId,
                                            equippedFrameId = uiState.equippedFrameId,
                                            equippedBladeId = uiState.equippedBladeId,
                                            unlockedHammers = uiState.unlockedHammers,
                                            unlockedFrames = uiState.unlockedFrames,
                                            unlockedBlades = uiState.unlockedBlades,
                                            onBuyHammer = { hammer -> viewModel.buyHammer(hammer) },
                                            onBuyFrame = { frame -> viewModel.buyFrame(frame) },
                                            onBuyBlade = { blade -> viewModel.buyBlade(blade) },
                                            onClaimDiamondPack = { pack -> viewModel.claimDiamondPack(pack) },
                                            bannerMessage = uiState.bannerMessage,
                                            onClose = {
                                                viewModel.clearBannerMessage()
                                                viewModel.navigateTo(CurrentScreen.HOME)
                                            }
                                        )
                                    }
                                    CurrentScreen.DAILY_WHEEL -> {
                                        DailyWheelScreen(
                                            isSpinning = uiState.isWheelSpinning,
                                            wheelRotation = uiState.wheelRotation,
                                            wonSegment = uiState.wheelWonSegment,
                                            dailyStreakDay = uiState.dailyStreakDay,
                                            remainingMillis = viewModel.getDailySpinRemainingMillis(),
                                            onSpinFree = { viewModel.spinFortuneWheel(isFreeSpin = true) },
                                            onSpinInstant = { viewModel.spinFortuneWheel(isFreeSpin = false) },
                                            onClose = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.QUESTS -> {
                                        QuestsScreen(
                                            lifetimePops = uiState.lifetimePops,
                                            lifetimeMaxCombo = uiState.lifetimeMaxCombo,
                                            completedLevelsCount = uiState.completedLevels.size,
                                            wheelSpins = uiState.wheelSpins,
                                            claimedQuests = uiState.claimedQuests,
                                            onClaimQuest = { quest -> viewModel.claimQuest(quest) },
                                            onClose = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.GAMEPLAY -> {
                                        GamePlayScreen(
                                            gameState = uiState.gameState,
                                            gameMode = uiState.gameMode,
                                            currentLevel = uiState.currentLevel,
                                            score = uiState.score,
                                            lives = uiState.lives,
                                            timeLeft = uiState.timeLeft,
                                            poppedCount = uiState.poppedCount,
                                            targetPops = uiState.targetPops,
                                            combo = uiState.combo,
                                            maxCombo = uiState.maxCombo,
                                            earnedCoins = uiState.earnedCoinsThisSession,
                                            earnedGems = uiState.earnedGemsThisSession,
                                            earnedStars = uiState.earnedStarsThisLevel,
                                            screenShake = { viewModel.currentShake },
                                            isFreezeActive = uiState.isFreezeActive,
                                            isMultiplierActive = uiState.isMultiplierActive,
                                            balloons = viewModel.balloons,
                                            particles = viewModel.particles,
                                            floatingTexts = viewModel.floatingTexts,
                                            swipeTrail = viewModel.swipeTrail,
                                            hammerStrikes = viewModel.hammerStrikes,
                                            shockwaves = viewModel.shockwaves,
                                            ambients = viewModel.ambients,
                                            equippedFrame = equippedFrame,
                                            equippedTheme = currentTheme,
                                            equippedSkillId = uiState.equippedSkillId,
                                            skillEnergy = uiState.skillEnergy,
                                            skillCooldownRemaining = uiState.skillCooldownRemaining,
                                            onActivateSkill = { viewModel.activateSkill() },
                                            onStepFrame = { dt -> viewModel.stepFrame(dt) },
                                            onPointerInput = { x, y, isTap -> viewModel.handlePointerInput(x, y, isTap) },
                                            onCanvasSizeChanged = { w, h ->
                                                viewModel.canvasWidth = w
                                                viewModel.canvasHeight = h
                                            },
                                            onPause = { viewModel.pauseGame() },
                                            onResume = { viewModel.resumeGame() },
                                            onRestart = { viewModel.restartGame() },
                                            onNextLevel = { viewModel.nextLevel() },
                                            onStageMenu = { viewModel.navigateTo(CurrentScreen.CAMPAIGN_MAP) },
                                            onQuitToMenu = { viewModel.navigateTo(CurrentScreen.HOME) },
                                            onOpenSettings = { viewModel.openSettings() }
                                        )
                                    }
                                }
                            }
                        }

                        // Settings Dialog Overlay
                        if (uiState.isSettingsOpen) {
                            SettingsDialog(
                                isSoundMuted = uiState.isSoundMuted,
                                soundVolume = uiState.soundVolume,
                                isVibrationEnabled = uiState.isVibrationEnabled,
                                isScreenShakeEnabled = uiState.isScreenShakeEnabled,
                                graphicsQuality = uiState.graphicsQuality,
                                isWideAssistEnabled = uiState.isWideAssistEnabled,
                                onToggleMute = { viewModel.toggleSound() },
                                onChangeVolume = { viewModel.setSoundVolume(it) },
                                onToggleVibration = { viewModel.setVibrationEnabled(it) },
                                onToggleScreenShake = { viewModel.setScreenShakeEnabled(it) },
                                onChangeGraphicsQuality = { viewModel.setGraphicsQuality(it) },
                                onToggleWideAssist = { viewModel.setWideAssistEnabled(it) },
                                onResetAllData = { viewModel.resetAllData() },
                                onClose = { viewModel.closeSettings() }
                            )
                        }
                    }
                }
            }
        }
    }
}
