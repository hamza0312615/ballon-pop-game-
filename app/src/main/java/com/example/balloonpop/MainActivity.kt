package com.example.balloonpop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.balloonpop.ads.AdMobManager
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.GameState
import com.example.balloonpop.notification.DailyNotificationHelper
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

        // Initialize Google AdMob SDK and preload ad units
        AdMobManager.initialize(this)

        // Schedule once-per-day mobile notification reminder
        DailyNotificationHelper.scheduleDailyNotification(this)

        setContent {
            BalloonPopTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            DailyNotificationHelper.scheduleDailyNotification(this@MainActivity)
                        }
                    }
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Active background theme colors
                val currentTheme = GameConstants.SHOP_THEMES.find { it.id == uiState.equippedThemeId }
                    ?: GameConstants.SHOP_THEMES.first()
                val bgBrush = Brush.linearGradient(currentTheme.gradientColors)

                val equippedFrame = GameConstants.SHOP_FRAMES.find { it.id == uiState.equippedFrameId }
                    ?: GameConstants.SHOP_FRAMES.first()

                // Handle Back Button
                BackHandler(enabled = uiState.isSettingsOpen || (uiState.currentScreen != CurrentScreen.HOME && uiState.currentScreen != CurrentScreen.SPLASH)) {
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
                            // Persistent Top Bar on non-gameplay, non-splash, and non-home screens
                            if (uiState.currentScreen != CurrentScreen.GAMEPLAY && uiState.currentScreen != CurrentScreen.HOME && uiState.currentScreen != CurrentScreen.CAMPAIGN_MAP && uiState.currentScreen != CurrentScreen.SPLASH) {
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
                                    CurrentScreen.SPLASH -> {
                                        SplashScreen(
                                            onSplashFinished = {
                                                AdMobManager.showAppOpenAd(this@MainActivity) {
                                                    viewModel.navigateTo(CurrentScreen.HOME)
                                                }
                                            }
                                        )
                                    }
                                    CurrentScreen.HOME -> {
                                        val nextUnlockedLevel = ((uiState.completedLevels.keys.maxOrNull() ?: 0) + 1).coerceIn(1, 200)
                                        val currentZoneName = GameConstants.WORLD_ZONES.find { nextUnlockedLevel in it.startLevel..it.endLevel }?.name ?: "Neon Glade"
                                        HomeScreen(
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            lives = uiState.lives,
                                            currentLevel = nextUnlockedLevel,
                                            zoneName = currentZoneName,
                                            onOpenCampaign = { viewModel.navigateTo(CurrentScreen.CAMPAIGN_MAP) },
                                            onOpenSkillVault = { viewModel.navigateTo(CurrentScreen.SKILL_VAULT) },
                                            onOpenEndless = { viewModel.navigateTo(CurrentScreen.ENDLESS_SELECT) },
                                            onOpenDailySpin = { viewModel.navigateTo(CurrentScreen.DAILY_WHEEL) },
                                            onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                            onOpenQuests = { viewModel.navigateTo(CurrentScreen.QUESTS) },
                                            onOpenRanks = { viewModel.navigateTo(CurrentScreen.RANKS) },
                                            onOpenSettings = { viewModel.openSettings() },
                                            onStartCustomChallenge = { time, pops, misses ->
                                                viewModel.startCustomChallenge(time, pops, misses)
                                            }
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
                                            claimedStarMilestones = uiState.claimedStarMilestones,
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            lives = uiState.lives,
                                            onSelectLevel = { levelNum, _ ->
                                                viewModel.startLevelTransition(levelNum)
                                                viewModel.navigateTo(CurrentScreen.GAMEPLAY)
                                            },
                                            onClaimMilestoneChest = { milestoneLevel ->
                                                viewModel.claimMilestoneChest(milestoneLevel)
                                            },
                                            onClaimStarMilestone = { milestone ->
                                                viewModel.claimStarMilestone(milestone)
                                            },
                                            onOpenSkillVault = { viewModel.navigateTo(CurrentScreen.SKILL_VAULT) },
                                            onOpenDailySpin = { viewModel.navigateTo(CurrentScreen.DAILY_WHEEL) },
                                            onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                            onOpenEndless = { viewModel.navigateTo(CurrentScreen.ENDLESS_SELECT) },
                                            onOpenQuests = { viewModel.navigateTo(CurrentScreen.QUESTS) },
                                            onOpenSettings = { viewModel.openSettings() },
                                            onClose = { viewModel.navigateTo(CurrentScreen.HOME) }
                                        )
                                    }
                                    CurrentScreen.ENDLESS_SELECT -> {
                                        EndlessModeScreen(
                                            lives = uiState.lives,
                                            coins = uiState.coins,
                                            gems = uiState.gems,
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
                                            equippedFlameId = uiState.equippedFlameId,
                                            equippedThemeId = uiState.equippedThemeId,
                                            unlockedHammers = uiState.unlockedHammers,
                                            unlockedFrames = uiState.unlockedFrames,
                                            unlockedBlades = uiState.unlockedBlades,
                                            unlockedFlames = uiState.unlockedFlames,
                                            unlockedThemes = uiState.unlockedThemes,
                                            onBuyHammer = { hammer -> viewModel.buyHammer(hammer) },
                                            onBuyFrame = { frame -> viewModel.buyFrame(frame) },
                                            onBuyBlade = { blade -> viewModel.buyBlade(blade) },
                                            onBuyFlame = { flame -> viewModel.buyFlame(flame) },
                                            onBuyTheme = { theme -> viewModel.buyTheme(theme) },
                                            onClaimDiamondPack = { pack ->
                                                if (pack.isAdReward) {
                                                    AdMobManager.showRewardedAd(
                                                        this@MainActivity,
                                                        onRewardEarned = {
                                                            viewModel.claimDiamondPack(pack)
                                                        }
                                                    )
                                                } else {
                                                    viewModel.claimDiamondPack(pack)
                                                }
                                            },
                                            onWatchAdForCoins = {
                                                AdMobManager.showRewardedAd(
                                                    this@MainActivity,
                                                    onRewardEarned = {
                                                        viewModel.rewardAdCoinsAndGems(250, 10)
                                                    }
                                                )
                                            },
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
                                            onSpinFree = {
                                                AdMobManager.showRewardedAd(
                                                    this@MainActivity,
                                                    onRewardEarned = {
                                                        viewModel.spinFortuneWheel(isFreeSpin = true)
                                                    },
                                                    onDismiss = {
                                                        if (!uiState.isWheelSpinning && uiState.wheelWonSegment == null) {
                                                            viewModel.spinFortuneWheel(isFreeSpin = true)
                                                        }
                                                    }
                                                )
                                            },
                                            onSpinInstant = { viewModel.spinFortuneWheel(isFreeSpin = false) },
                                            onWatchAdForSpin = {
                                                AdMobManager.showRewardedAd(
                                                    this@MainActivity,
                                                    onRewardEarned = {
                                                        viewModel.spinFortuneWheel(isFreeSpin = true)
                                                    }
                                                )
                                            },
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
                                    CurrentScreen.RANKS -> {
                                        RanksScreen(
                                            currentScore = uiState.lifetimeHighScore,
                                            currentLevel = uiState.currentLevel,
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
                                            missedCount = uiState.missedCount,
                                            maxMissesAllowed = uiState.maxMissesAllowed,
                                            gameOverReason = uiState.gameOverReason,
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
                                            lightningBolts = viewModel.lightningBolts,
                                            screenFlashAlpha = uiState.screenFlashAlpha,
                                            boosterCounts = uiState.boosterCounts,
                                            candyPraise = uiState.candyPraise,
                                            onTriggerBooster = { booster -> viewModel.triggerInGameBooster(booster) },
                                            coins = uiState.coins,
                                            equippedSkillId = uiState.equippedSkillId,
                                            skillEnergy = uiState.skillEnergy,
                                            skillCooldownRemaining = uiState.skillCooldownRemaining,
                                            isHammerSlideActive = uiState.isHammerSlideActive,
                                            hammerSlideTimeRemaining = uiState.hammerSlideTimeRemaining,
                                            stageBoosterUsesCount = uiState.stageBoosterUsesCount,
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
                                            onOpenSettings = { viewModel.openSettings() },
                                            isNewHighScore = uiState.isNewHighScoreAchieved,
                                            previousHighScore = uiState.previousHighScore,
                                            isTransitioningLevel = uiState.isTransitioningLevel,
                                            transitionTargetLevel = uiState.transitionTargetLevel,
                                            onFinishLevelTransition = { viewModel.finishLevelTransition() }
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
                                isMusicMuted = uiState.isMusicMuted,
                                musicVolume = uiState.musicVolume,
                                isSfxMuted = uiState.isSfxMuted,
                                sfxVolume = uiState.sfxVolume,
                                currentMusicTheme = uiState.currentMusicTheme,
                                onToggleMusicMute = { viewModel.toggleMusicMute() },
                                onChangeMusicVolume = { viewModel.setMusicVolume(it) },
                                onToggleSfxMute = { viewModel.toggleSfxMute() },
                                onChangeSfxVolume = { viewModel.setSfxVolume(it) },
                                onSelectMusicTheme = { viewModel.setMusicTheme(it) },
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

    override fun onStart() {
        super.onStart()
        viewModel.onAppForegrounded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onAppForegrounded()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppBackgrounded()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackgrounded()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onAppDestroyed()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && isFinishing) {
            viewModel.onAppBackgrounded()
        }
    }
}
