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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

        // Initialize Google AdMob SDK and preload ad units.
        // Pass 5 (audit §2.2): the preload callback actually loads the
        // interstitial and rewarded units — before, nothing ever loaded
        // them, so gameplay had no ad ready to show.
        AdMobManager.initialize(this) {
            AdMobManager.loadInterstitialAd(this)
            AdMobManager.loadRewardedAd(this)
        }

        // Schedule once-per-day mobile notification reminder
        DailyNotificationHelper.scheduleDailyNotification(this)

        setContent {
            BalloonPopTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+) only AFTER completing first stage
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val notifLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        DailyNotificationHelper.setNotificationEnabled(this@MainActivity, isGranted)
                        if (isGranted) {
                            DailyNotificationHelper.scheduleDailyNotification(this@MainActivity)
                        }
                    }
                    LaunchedEffect(uiState.shouldPromptFirstClearNotification) {
                        // Only prompt the user after they successfully complete their first stage during gameplay
                        if (uiState.shouldPromptFirstClearNotification &&
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            viewModel.onNotificationPromptDismissed()
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
                        // Pass 20 (root width fix): the app runs edge-to-edge
                        // and every screen laid its first/last items out from
                        // its own immediate parent width with no shared safe
                        // inset, so edge pills, grid labels and overlay cards
                        // repeatedly landed flush against — or past — the
                        // physical edge and read as clipped/shifted left.
                        // One shared horizontal safe inset on the content
                        // column makes every screen (and every in-screen
                        // overlay dialog) lay out inside the same fluid,
                        // guaranteed-visible width.
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp)
                        ) {
                            // Persistent Top Bar on non-gameplay, non-splash, and non-home screens.
                            // Pass 20: SHOP is excluded too — the shop renders
                            // its own live currency row (with its close
                            // button), and showing this global bar above it
                            // duplicated the balances on one screen.
                            if (uiState.currentScreen != CurrentScreen.GAMEPLAY && uiState.currentScreen != CurrentScreen.HOME && uiState.currentScreen != CurrentScreen.CAMPAIGN_MAP && uiState.currentScreen != CurrentScreen.SPLASH && uiState.currentScreen != CurrentScreen.SHOP && uiState.currentScreen != CurrentScreen.DIAMOND_STORE) {
                                TopBar(
                                    coins = uiState.coins,
                                    gems = uiState.gems,
                                    lives = uiState.lives,
                                    // Pass 18: the real lives cap is 3 (initGameSession
                                    // and the HEART balloon both cap at 3) — the bar
                                    // showed "3/5" from TopBar's old default of 5.
                                    maxLives = 3,
                                    onOpenShop = { viewModel.navigateTo(CurrentScreen.SHOP) },
                                    onOpenSettings = { viewModel.openSettings() }
                                )
                            }

                            // Active Screen Container — Pass 11: produced screen
                            // transitions (crossfade + a whisper of scale on menu
                            // screens, quick fade into/out of gameplay) instead of
                            // the prototype hard cut.
                            Box(modifier = Modifier.weight(1f)) {
                                AnimatedContent(
                                    targetState = uiState.currentScreen,
                                    transitionSpec = {
                                        if (targetState == CurrentScreen.GAMEPLAY || initialState == CurrentScreen.GAMEPLAY) {
                                            fadeIn(tween(160)) togetherWith fadeOut(tween(120))
                                        } else {
                                            (fadeIn(tween(240)) + scaleIn(initialScale = 0.985f, animationSpec = tween(240))) togetherWith fadeOut(tween(160))
                                        }
                                    },
                                    label = "screenTransition"
                                ) { animatedScreen ->
                                when (animatedScreen) {
                                    CurrentScreen.SPLASH -> {
                                        SplashScreen(
                                            onSplashFinished = {
                                                // Pass 8 (research §5): no App Open ad until the
                                                // player has completed at least one stage EVER —
                                                // "never before the first fun". The persisted
                                                // completed-levels count is read via GameRepository
                                                // (the ViewModel's instance) right at this call site.
                                                if (viewModel.repository.getCompletedLevels().isNotEmpty()) {
                                                    AdMobManager.showAppOpenAd(this@MainActivity) {
                                                        viewModel.navigateTo(CurrentScreen.HOME)
                                                    }
                                                } else {
                                                    viewModel.navigateTo(CurrentScreen.HOME)
                                                }
                                            }
                                        )
                                    }
                                    CurrentScreen.HOME -> {
                                        val nextUnlockedLevel = ((uiState.completedLevels.keys.maxOrNull() ?: 0) + 1).coerceIn(1, 200)
                                        val currentZone = GameConstants.WORLD_ZONES.find { nextUnlockedLevel in it.startLevel..it.endLevel }
                                        val currentZoneName = currentZone?.name ?: "Twilight Grove"
                                        // Pass 20: zone progress for the Home
                                        // progress strip — read straight from
                                        // the same completedLevels state.
                                        val currentZoneStars = if (currentZone != null) {
                                            (currentZone.startLevel..currentZone.endLevel).sumOf { uiState.completedLevels[it] ?: 0 }
                                        } else 0
                                        HomeScreen(
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            lives = uiState.lives,
                                            currentLevel = nextUnlockedLevel,
                                            zoneName = currentZoneName,
                                            zoneStars = currentZoneStars,
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
                                            },
                                            dailyTrackDay = uiState.dailyTrackDay,
                                            dailyTrackCanClaimToday = uiState.dailyTrackCanClaimToday,
                                            dailyTrackResetStreakNotice = uiState.dailyTrackResetStreakNotice,
                                            onClaimDailyGift = { viewModel.claimDailyTrackGift() }
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
                                            winStreak = uiState.winStreak,
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
                                            highScore = uiState.lifetimeHighScore,
                                            lives = uiState.lives,
                                            coins = uiState.coins,
                                            gems = uiState.gems,
                                            classicBest = viewModel.repository.getEndlessHighScore("CLASSIC"),
                                            timeRushBest = viewModel.repository.getEndlessHighScore("TIME_RUSH"),
                                            arcadeBest = viewModel.repository.getEndlessHighScore("ARCADE"),
                                            zenBest = viewModel.repository.getEndlessHighScore("ZEN"),
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
                                            equippedBalloonSkinId = uiState.equippedBalloonSkinId,
                                            equippedThemeId = uiState.equippedThemeId,
                                            unlockedBalloonSkins = uiState.unlockedBalloonSkins,
                                            unlockedHammers = uiState.unlockedHammers,
                                            unlockedFrames = uiState.unlockedFrames,
                                            unlockedBlades = uiState.unlockedBlades,
                                            unlockedFlames = uiState.unlockedFlames,
                                            unlockedThemes = uiState.unlockedThemes,
                                            boosterCounts = uiState.boosterCounts,
                                            onBuyBooster = { boosterId, withGems -> viewModel.buyBooster(boosterId, withGems) },
                                            onBuyHammer = { hammer -> viewModel.buyHammer(hammer) },
                                            onBuyFrame = { frame -> viewModel.buyFrame(frame) },
                                            onBuyBlade = { blade -> viewModel.buyBlade(blade) },
                                            onBuyFlame = { flame -> viewModel.buyFlame(flame) },
                                            onBuyBalloonSkin = { skin -> viewModel.buyBalloonSkin(skin) },
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
                                            // Pass 18: let the shop auto-dismiss the
                                            // banner after a few seconds so it behaves
                                            // identically on every tab.
                                            onClearBanner = { viewModel.clearBannerMessage() },
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
                                                // Pass 18: the free daily spin now runs
                                                // directly. Pass 5 gated it behind a
                                                // rewarded ad, but that made the whole
                                                // wheel dead whenever ads no-filled
                                                // (playtest: two spins, zero prizes, spin
                                                // never consumed). The anti-abuse intent
                                                // is preserved by the ViewModel's own
                                                // 24h cooldown (isDailySpinReady +
                                                // timestamp), which still gates this
                                                // button. Ads remain the way to earn
                                                // EXTRA spins while on cooldown (below).
                                                viewModel.spinFortuneWheel(isFreeSpin = true)
                                            },
                                            onSpinInstant = { viewModel.spinFortuneWheel(isFreeSpin = false) },
                                            onWatchAdForSpin = {
                                                AdMobManager.showRewardedAd(
                                                    this@MainActivity,
                                                    onRewardEarned = {
                                                        // Pass 18: bypassCooldown — the ad
                                                        // is the payment for this extra spin;
                                                        // without it the ViewModel bounced
                                                        // the spin off the 24h cooldown.
                                                        viewModel.spinFortuneWheel(isFreeSpin = true, bypassCooldown = true)
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
                                            achievements = GameConstants.ACHIEVEMENTS,
                                            claimedAchievements = uiState.claimedAchievements,
                                            onAchievementProgress = { achievement -> viewModel.achievementProgress(achievement) },
                                            onClaimAchievement = { achievement -> viewModel.claimAchievement(achievement) },
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
                                            boosterCooldowns = uiState.boosterCooldowns,
                                            candyPraise = uiState.candyPraise,
                                            winStreak = uiState.winStreak,
                                            assistUnlocked = uiState.assistUnlocked,
                                            onTriggerBooster = { booster -> viewModel.triggerInGameBooster(booster) },
                                            onBuyBooster = { boosterId -> viewModel.buyBooster(boosterId) },
                                            onBoosterBuyOpenChange = { open -> viewModel.setBoosterBuyOpen(open) },
                                            coins = uiState.coins,
                                            equippedSkillId = uiState.equippedSkillId,
                                            skillEnergy = uiState.skillEnergy,
                                            skillCooldownRemaining = uiState.skillCooldownRemaining,
                                            onPlayOnRevive = { withCoins -> viewModel.playOnRevive(withCoins) },
                                            isHammerSlideActive = uiState.isHammerSlideActive,
                                            hammerSlideTimeRemaining = uiState.hammerSlideTimeRemaining,
                                            stageBoosterUsesCount = uiState.stageBoosterUsesCount,
                                            onFinishRun = { viewModel.finishEndlessRun() },
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
                                            onFinishLevelTransition = { viewModel.finishLevelTransition() },
                                            showComboHintBanner = uiState.showComboHintBanner,
                                            showRushBanner = uiState.showRushBanner,
                                            objectiveType = uiState.currentObjectiveType,
                                            focusColors = uiState.currentFocusColors,
                                            steadyHandsBonusEarned = uiState.steadyHandsBonusEarned,
                                            steadyHandsMissedCount = uiState.steadyHandsMissedCount
                                        )
                                    }
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
