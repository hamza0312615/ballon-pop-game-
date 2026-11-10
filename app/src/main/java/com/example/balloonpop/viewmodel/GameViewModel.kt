package com.example.balloonpop.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.balloonpop.audio.BackgroundMusicPlayer
import com.example.balloonpop.audio.MusicTheme
import com.example.balloonpop.audio.SoundManager
import com.example.balloonpop.data.GameRepository
import com.example.balloonpop.model.*
import com.example.balloonpop.util.ScreenShakeUtility
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class CurrentScreen {
    SPLASH,
    HOME,
    CAMPAIGN_MAP,
    SKILL_VAULT,
    ENDLESS_SELECT,
    SHOP,
    DIAMOND_STORE,
    DAILY_WHEEL,
    QUESTS,
    RANKS,
    GAMEPLAY
}

data class UiState(
    val currentScreen: CurrentScreen = CurrentScreen.SPLASH,
    val gameState: GameState = GameState.MENU,
    val gameMode: GameMode = GameMode.CAMPAIGN,
    val currentLevel: Int = 1,
    val coins: Int = 150,
    val gems: Int = 10,
    val equippedHammerId: String = "hammer_wood",
    val equippedFrameId: String = "frame_minimal",
    val equippedBladeId: String = "blade_classic",
    val equippedFlameId: String = "flame_ember",
    val equippedThemeId: String = "theme_sunset_twilight",
    val unlockedHammers: Set<String> = setOf("hammer_wood"),
    val unlockedFrames: Set<String> = setOf("frame_minimal"),
    val unlockedBlades: Set<String> = setOf("blade_classic"),
    val unlockedFlames: Set<String> = setOf("flame_ember"),
    val unlockedBalloonSkins: Set<String> = setOf("balloon_classic"),
    val equippedBalloonSkinId: String = "balloon_classic",
    val unlockedThemes: Set<String> = setOf("theme_sunset_twilight", "theme_obsidian"),
    val claimedStarMilestones: Set<Int> = emptySet(),
    // Skills State
    val equippedSkillId: String = "skill_slice",
    val unlockedSkills: Set<String> = setOf("skill_slice"),
    val skillLevels: Map<String, Int> = mapOf("skill_slice" to 1),
    val skillEnergy: Float = 100f,
    val isSkillActive: Boolean = false,
    val isHammerSlideActive: Boolean = false,
    val hammerSlideTimeRemaining: Float = 0f,
    val stageSkillUsesCount: Int = 0,
    val stageBoosterUsesCount: Int = 0,
    val skillCooldownRemaining: Float = 0f,
    // Daily Timeline & Fortune Wheel
    val lastDailySpinTimestamp: Long = 0L,
    val dailyStreakDay: Int = 1,
    val isWheelSpinning: Boolean = false,
    val wheelRotation: Float = 0f,
    val wheelWonSegment: WheelSegment? = null,
    val claimedMilestoneChests: Set<Int> = emptySet(),
    val completedLevels: Map<Int, Int> = emptyMap(),
    val score: Int = 0,
    val lives: Int = 3,
    val timeLeft: Int = 60,
    val poppedCount: Int = 0,
    val targetPops: Int = 20,
    val targetScore: Int = 350,
    val combo: Int = 0,
    val maxCombo: Int = 1,
    val isFreezeActive: Boolean = false,
    val isMultiplierActive: Boolean = false,
    val screenShake: Float = 0f,
    // Settings
    val isSoundMuted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val isMusicMuted: Boolean = false,
    val musicVolume: Float = 0.50f,
    val isSfxMuted: Boolean = false,
    val sfxVolume: Float = 1.0f,
    val currentMusicTheme: MusicTheme = MusicTheme.MEADOW_JOY,
    val isVibrationEnabled: Boolean = true,
    val isScreenShakeEnabled: Boolean = true,
    val graphicsQuality: String = "BALANCED", // "ULTRA", "BALANCED", "BATTERY_SAVER"
    val isWideAssistEnabled: Boolean = false,
    val isSettingsOpen: Boolean = false,
    // Lifetime stats
    val lifetimePops: Int = 0,
    val lifetimeHighScore: Int = 0,
    val lifetimeMaxCombo: Int = 1,
    val wheelSpins: Int = 0,
    val claimedQuests: Set<String> = emptySet(),
    // Game Over / Stage Clear summaries
    val earnedCoinsThisSession: Int = 0,
    val earnedGemsThisSession: Int = 0,
    val earnedStarsThisLevel: Int = 0,
    val missedCount: Int = 0,
    val maxMissesAllowed: Int = 4,
    val gameOverReason: GameOverReason = GameOverReason.TIME_EXPIRED,
    // Banner Message / Toast
    val bannerMessage: String? = null,
    // In-Game Boosters (Candy Crush Style)
    val boosterCounts: Map<String, Int> = mapOf(
        "booster_thunder" to 3,
        "booster_color_bomb" to 3,
        "booster_hammer" to 3,
        "booster_freeze" to 3,
        "booster_vortex" to 3
    ),
    val boosterCooldowns: Map<String, Float> = emptyMap(),
    val candyPraise: String? = null,
    val screenFlashAlpha: Float = 0f,
    // Onboarding & Milestones
    val claimedAchievements: Set<String> = emptySet(),
    val showTutorial: Boolean = false,
    val tutorialStep: Int = 0,
    // Stage Transitions & High Score Celebrations
    val isNewHighScoreAchieved: Boolean = false,
    val previousHighScore: Int = 0,
    val isTransitioningLevel: Boolean = false,
    val transitionTargetLevel: Int? = null,
    val shouldPromptFirstClearNotification: Boolean = false,
    val showComboHintBanner: Boolean = false,
    // Pass 8: win-streak fuel (persisted) + kindness-assist flag for the
    // stage currently being attempted (drives the fail-dialog help line).
    val winStreak: Int = 0,
    val assistUnlocked: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val repository = GameRepository(application)
    val sound = SoundManager.getInstance(application)
    val musicPlayer: BackgroundMusicPlayer get() = sound.musicPlayer

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    private val hasHardwareVibrator: Boolean by lazy {
        try {
            vibrator?.hasVibrator() == true
        } catch (_: Throwable) {
            false
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Game Entities
    val balloons = mutableListOf<Balloon>()
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()
    val swipeTrail = mutableListOf<BladeTrailPoint>()
    val hammerStrikes = mutableListOf<HammerStrikeEffect>()
    val shockwaves = mutableListOf<PopShockwave>()
    val ambients = mutableListOf<AmbientElement>()
    val lightningBolts = mutableListOf<LightningBolt>()

    private var balloonIdCounter = 0L
    private var hammerEffectCounter = 0L
    private var shockwaveIdCounter = 0L
    private var ambientIdCounter = 0L
    private var lightningIdCounter = 0L
    private var spawnAccumulator = 0f
    private var spawnIntervalSeconds = 0.75f
    private var freezeTimeRemaining = 0f
    // Pass 8 juice: hit-stop — while > 0, balloon motion and the spawner
    // pause for a beat (bomb detonations, gold-rush burst completions).
    private var hitStopRemaining = 0f
    private var multiplierTimeRemaining = 0f
    private var currentScreenShake = 0f
    private var internalSkillEnergy = 0f
    private var internalSkillCooldown = 0f
    private var hammerSlideTimeRemaining = 0f
    private var stageBoosterUsesCount = 0
    private var stageSkillUsesCount = 0
    // Pass 5 (audit L12): playOnRevive is capped at once per stage attempt.
    private var stageReviveUsesCount = 0
    private var uiSyncAccumulator = 0f
    private var sessionBaselineHighScore = 0

    data class PendingSpawn(
        var delayRemaining: Float,
        val x: Float,
        val y: Float,
        val radius: Float,
        val vy: Float,
        val type: BalloonType,
        val color: Color,
        val shapeStyle: BalloonShapeStyle,
        val hp: Int = 1,
        val isRingFormation: Boolean = false,
        val ringAngle: Float = 0f,
        val ringCenterX: Float = 0f,
        val ringCenterY: Float = 0f,
        val ringTargetRadius: Float = 135f,
        val isGoldRush: Boolean = false
    )

    private val pendingSpawns = mutableListOf<PendingSpawn>()
    private var patternCycleIndex = 0
    private var hasShownComboHint = false
    private var goldRushParticleAccumulator = 0f

    val currentShake: Float
        get() = if (_uiState.value.isScreenShakeEnabled) currentScreenShake else 0f
    private var timerJob: Job? = null
    private var lastPopTime = 0L
    private var lastHammerTapTime = 0L
    private val internalBoosterCooldowns = mutableMapOf<String, Float>()
    private var lastPraiseTime = 0L
    private var lastComboFloatingTextTime = 0L

    var canvasWidth: Float = 1080f
    var canvasHeight: Float = 1920f

    init {
        loadSavedData()
    }

    private fun loadSavedData() {
        val muted = repository.isSoundMuted()
        val vol = repository.getSoundVolume()
        val musicMuted = repository.isMusicMuted()
        val musicVol = repository.getMusicVolume()
        val sfxMuted = repository.isSfxMuted()
        val sfxVol = repository.getSfxVolume()

        sound.setMasterVolume(vol)
        sound.setMuted(muted)
        sound.setMusicVolume(musicVol)
        sound.setMusicMuted(musicMuted)
        sound.setSfxVolume(sfxVol)
        sound.setSfxMuted(sfxMuted)

        val unlockedSkills = repository.getUnlockedSkills()
        val skillLevels = GameConstants.AVAILABLE_SKILLS.associate { it.id to repository.getSkillLevel(it.id) }

        _uiState.update {
            it.copy(
                coins = repository.getCoins(),
                gems = repository.getGems(),
                equippedHammerId = repository.getEquippedHammer(),
                equippedFrameId = repository.getEquippedFrame(),
                equippedBladeId = repository.getEquippedBlade(),
                equippedFlameId = repository.getEquippedFlame(),
                equippedThemeId = repository.getEquippedTheme(),
                unlockedHammers = repository.getUnlockedHammers(),
                unlockedFrames = repository.getUnlockedFrames(),
                unlockedBlades = repository.getUnlockedBlades(),
                unlockedFlames = repository.getUnlockedFlames(),
                unlockedBalloonSkins = repository.getUnlockedBalloonSkins(),
                equippedBalloonSkinId = repository.getEquippedBalloonSkin(),
                unlockedThemes = repository.getUnlockedThemes(),
                claimedStarMilestones = repository.getClaimedStarMilestones(),
                equippedSkillId = repository.getEquippedSkill(),
                unlockedSkills = unlockedSkills,
                skillLevels = skillLevels,
                lastDailySpinTimestamp = repository.getLastDailySpinTimestamp(),
                dailyStreakDay = repository.getDailyStreakDay(),
                claimedMilestoneChests = repository.getClaimedMilestoneChests(),
                completedLevels = repository.getCompletedLevels(),
                lifetimePops = repository.getLifetimePops(),
                lifetimeHighScore = repository.getLifetimeHighScore(),
                lifetimeMaxCombo = repository.getLifetimeMaxCombo(),
                wheelSpins = repository.getWheelSpins(),
                claimedQuests = repository.getClaimedQuests(),
                claimedAchievements = repository.getClaimedAchievements(),
                winStreak = repository.getWinStreak(),
                isSoundMuted = muted,
                soundVolume = vol,
                isMusicMuted = musicMuted,
                musicVolume = musicVol,
                isSfxMuted = sfxMuted,
                sfxVolume = sfxVol,
                currentMusicTheme = sound.currentMusicTheme,
                isVibrationEnabled = repository.isVibrationEnabled(),
                isScreenShakeEnabled = repository.isScreenShakeEnabled(),
                graphicsQuality = repository.getGraphicsQuality(),
                isWideAssistEnabled = repository.isWideAssistEnabled(),
                boosterCounts = repository.getAllBoosterCounts()
            )
        }
    }

    fun triggerVibration(durationMs: Long = 25L) {
        if (!_uiState.value.isVibrationEnabled || !hasHardwareVibrator) return
        try {
            val v = vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(durationMs)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Triggers intensity-weighted vibrations and visual displacements for popping special balloons.
     */
    fun triggerSpecialBalloonShake(type: BalloonType) {
        val profile = ScreenShakeUtility.getProfileForBalloon(type)
        if (profile.visualDisplacement > 0f) {
            currentScreenShake = maxOf(currentScreenShake, profile.visualDisplacement)
            if (_uiState.value.isScreenShakeEnabled) {
                _uiState.update {
                    it.copy(
                        screenShake = currentScreenShake,
                        screenFlashAlpha = if (profile.screenFlashAlpha > 0f) maxOf(it.screenFlashAlpha, profile.screenFlashAlpha) else it.screenFlashAlpha
                    )
                }
            }
        }
        ScreenShakeUtility.triggerHapticFeedback(
            vibrator = vibrator,
            profile = profile,
            isVibrationEnabled = _uiState.value.isVibrationEnabled,
            hasHardwareVibrator = hasHardwareVibrator
        )
    }

    // --- Settings Methods ---
    fun openSettings() {
        sound.playClick()
        _uiState.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        sound.playClick()
        _uiState.update { it.copy(isSettingsOpen = false) }
    }

    fun toggleSound() {
        val newMuted = sound.toggleMute()
        repository.setSoundMuted(newMuted)
        _uiState.update { it.copy(isSoundMuted = newMuted) }
    }

    fun setSoundVolume(volume: Float) {
        sound.setMasterVolume(volume)
        repository.setSoundVolume(volume)
        _uiState.update { it.copy(soundVolume = volume) }
    }

    fun toggleMusicMute() {
        val newMuted = sound.toggleMusicMute()
        repository.setMusicMuted(newMuted)
        _uiState.update { it.copy(isMusicMuted = newMuted) }
    }

    fun setMusicVolume(volume: Float) {
        sound.setMusicVolume(volume)
        repository.setMusicVolume(volume)
        _uiState.update { it.copy(musicVolume = volume) }
    }

    fun toggleSfxMute() {
        val newMuted = sound.toggleSfxMute()
        repository.setSfxMuted(newMuted)
        _uiState.update { it.copy(isSfxMuted = newMuted) }
    }

    fun setSfxVolume(volume: Float) {
        sound.setSfxVolume(volume)
        repository.setSfxVolume(volume)
        _uiState.update { it.copy(sfxVolume = volume) }
    }

    fun setMusicTheme(theme: MusicTheme) {
        sound.setMusicTheme(theme)
        _uiState.update { it.copy(currentMusicTheme = theme) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        repository.setVibrationEnabled(enabled)
        _uiState.update { it.copy(isVibrationEnabled = enabled) }
        if (enabled) triggerVibration(30L)
    }

    fun setScreenShakeEnabled(enabled: Boolean) {
        repository.setScreenShakeEnabled(enabled)
        _uiState.update { it.copy(isScreenShakeEnabled = enabled) }
    }

    fun setGraphicsQuality(quality: String) {
        sound.playClick()
        repository.setGraphicsQuality(quality)
        _uiState.update { it.copy(graphicsQuality = quality) }
    }

    fun setWideAssistEnabled(enabled: Boolean) {
        sound.playClick()
        repository.setWideAssistEnabled(enabled)
        _uiState.update { it.copy(isWideAssistEnabled = enabled) }
    }

    fun resetAllData() {
        repository.resetAllData()
        loadSavedData()
        sound.playStageClear()
        _uiState.update {
            it.copy(
                coins = 150,
                gems = 10,
                equippedHammerId = "hammer_wood",
                equippedFrameId = "frame_minimal",
                equippedBladeId = "blade_classic",
                equippedThemeId = "theme_obsidian",
                unlockedHammers = setOf("hammer_wood"),
                unlockedFrames = setOf("frame_minimal"),
                unlockedBlades = setOf("blade_classic"),
                unlockedBalloonSkins = setOf("balloon_classic"),
                equippedBalloonSkinId = "balloon_classic",
                unlockedThemes = setOf("theme_obsidian"),
                completedLevels = emptyMap(),
                lifetimePops = 0,
                lifetimeHighScore = 0,
                lifetimeMaxCombo = 1,
                wheelSpins = 0,
                claimedQuests = emptySet(),
                claimedAchievements = emptySet(),
                isSettingsOpen = false,
                bannerMessage = "All Game Data Reset Successfully! 🔄"
            )
        }
    }

    fun navigateTo(screen: CurrentScreen) {
        // Pass 5 (audit L4): leaving gameplay to a non-game screen must end
        // the run cleanly. Previously the stage timer kept running
        // off-screen and fired gameOver() behind the menus — paying fail
        // rewards and writing an endless best entry for a run the player
        // never finished. Endless runs now finalize (saving their best) on
        // exit; campaign/custom runs are simply abandoned with no payout.
        val state = _uiState.value
        if (state.currentScreen == CurrentScreen.GAMEPLAY &&
            screen != CurrentScreen.GAMEPLAY &&
            (state.gameState == GameState.PLAYING || state.gameState == GameState.PAUSED) &&
            state.gameMode != GameMode.CAMPAIGN
        ) {
            // PLAYING covers direct navigation away; PAUSED covers the
            // pause-menu quit (timer already stopped, run still live).
            finishEndlessRun(saveBest = true)
        }
        if (screen == CurrentScreen.HOME) {
            sound.playDialogClose()
        } else {
            sound.playDialogOpen()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    // --- Hammer System ---
    fun setEquippedHammer(hammerId: String) {
        sound.playEquipSound()
        repository.setEquippedHammer(hammerId)
        _uiState.update { it.copy(equippedHammerId = hammerId) }
    }

    fun buyHammer(hammer: HammerSkin) {
        val state = _uiState.value
        if (state.unlockedHammers.contains(hammer.id)) {
            setEquippedHammer(hammer.id)
            return
        }
        if (hammer.currency == CurrencyType.COINS && state.coins >= hammer.cost) {
            val newCoins = state.coins - hammer.cost
            repository.setCoins(newCoins)
            repository.unlockHammer(hammer.id)
            repository.setEquippedHammer(hammer.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedHammers = repository.getUnlockedHammers(),
                    equippedHammerId = hammer.id,
                    bannerMessage = "Unlocked ${hammer.name}! 🎉"
                )
            }
        } else if (hammer.currency == CurrencyType.GEMS && state.gems >= hammer.cost) {
            val newGems = state.gems - hammer.cost
            repository.setGems(newGems)
            repository.unlockHammer(hammer.id)
            repository.setEquippedHammer(hammer.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedHammers = repository.getUnlockedHammers(),
                    equippedHammerId = hammer.id,
                    bannerMessage = "Unlocked ${hammer.name}! 💎"
                )
            }
        } else {
            sound.playPurchaseFail()
            _uiState.update { it.copy(bannerMessage = "Not enough currency for ${hammer.name}!") }
        }
    }

    // --- Frame System ---
    fun setEquippedFrame(frameId: String) {
        sound.playEquipSound()
        repository.setEquippedFrame(frameId)
        _uiState.update { it.copy(equippedFrameId = frameId) }
    }

    fun buyFrame(frame: PlayFrame) {
        val state = _uiState.value
        if (state.unlockedFrames.contains(frame.id)) {
            setEquippedFrame(frame.id)
            return
        }
        if (frame.currency == CurrencyType.COINS && state.coins >= frame.cost) {
            val newCoins = state.coins - frame.cost
            repository.setCoins(newCoins)
            repository.unlockFrame(frame.id)
            repository.setEquippedFrame(frame.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedFrames = repository.getUnlockedFrames(),
                    equippedFrameId = frame.id,
                    bannerMessage = "Unlocked ${frame.name} Frame! ✨"
                )
            }
        } else if (frame.currency == CurrencyType.GEMS && state.gems >= frame.cost) {
            val newGems = state.gems - frame.cost
            repository.setGems(newGems)
            repository.unlockFrame(frame.id)
            repository.setEquippedFrame(frame.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedFrames = repository.getUnlockedFrames(),
                    equippedFrameId = frame.id,
                    bannerMessage = "Unlocked ${frame.name} Frame! 💎"
                )
            }
        } else {
            sound.playPurchaseFail()
            _uiState.update { it.copy(bannerMessage = "Not enough currency for ${frame.name}!") }
        }
    }

    // --- Diamond / Store ---
    fun claimDiamondPack(pack: DiamondStorePack, isPurchased: Boolean = false) {
        val state = _uiState.value
        if (pack.id == "coin_convert_pack") {
            if (state.coins >= 500) {
                val newCoins = state.coins - 500
                val newGems = state.gems + pack.diamondsAmount
                repository.setCoins(newCoins)
                repository.setGems(newGems)
                sound.playCoin()
                _uiState.update {
                    it.copy(
                        coins = newCoins,
                        gems = newGems,
                        bannerMessage = "Converted 500🪙 into +${pack.diamondsAmount}💎!"
                    )
                }
            } else {
                _uiState.update { it.copy(bannerMessage = "Not enough coins! Need 500🪙") }
            }
            return
        }

        if (pack.isAdReward) {
            val newGems = state.gems + pack.diamondsAmount
            repository.setGems(newGems)
            sound.playStageClear()
            triggerVibration(40L)
            _uiState.update {
                it.copy(
                    gems = newGems,
                    bannerMessage = "Ad Completed! Claimed +${pack.diamondsAmount} 💎!"
                )
            }
            return
        }

        // Real-money In-App Purchase packs ($0.99, $2.99, $8.99, $19.99)
        // Must NOT award free diamonds on click without verified Google Play Billing payment
        if (!isPurchased) {
            _uiState.update {
                it.copy(
                    bannerMessage = "${pack.title} (${pack.priceTag}) requires Google Play In-App Billing."
                )
            }
            return
        }

        val newGems = state.gems + pack.diamondsAmount
        repository.setGems(newGems)
        sound.playStageClear()
        triggerVibration(40L)
        _uiState.update {
            it.copy(
                gems = newGems,
                bannerMessage = "Purchased +${pack.diamondsAmount} 💎 successfully!"
            )
        }
    }

    fun clearBannerMessage() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    /** Pass 5: small helper for the claim-guard messages (the codebase
     *  otherwise sets bannerMessage inline). */
    private fun showBannerMessage(message: String) {
        _uiState.update { it.copy(bannerMessage = message) }
    }

    fun rewardAdCoinsAndGems(coins: Int = 250, gems: Int = 10) {
        val state = _uiState.value
        val newCoins = state.coins + coins
        val newGems = state.gems + gems
        repository.setCoins(newCoins)
        repository.setGems(newGems)
        sound.playStageClear()
        triggerVibration(50L)
        _uiState.update {
            it.copy(
                coins = newCoins,
                gems = newGems,
                bannerMessage = "🎬 Ad Reward Claimed: +$coins 🪙 & +$gems 💎!"
            )
        }
    }

    // --- Blade & Theme ---
    fun setEquippedBlade(bladeId: String) {
        sound.playEquipSound()
        repository.setEquippedBlade(bladeId)
        _uiState.update { it.copy(equippedBladeId = bladeId) }
    }

    fun buyBlade(blade: BladeItem) {
        val state = _uiState.value
        if (state.unlockedBlades.contains(blade.id)) {
            setEquippedBlade(blade.id)
            return
        }
        if (blade.currency == CurrencyType.COINS && state.coins >= blade.cost) {
            val newCoins = state.coins - blade.cost
            repository.setCoins(newCoins)
            repository.unlockBlade(blade.id)
            repository.setEquippedBlade(blade.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedBlades = repository.getUnlockedBlades(),
                    equippedBladeId = blade.id
                )
            }
        } else if (blade.currency == CurrencyType.GEMS && state.gems >= blade.cost) {
            val newGems = state.gems - blade.cost
            repository.setGems(newGems)
            repository.unlockBlade(blade.id)
            repository.setEquippedBlade(blade.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedBlades = repository.getUnlockedBlades(),
                    equippedBladeId = blade.id
                )
            }
        } else {
            sound.playPurchaseFail()
        }
    }

    fun setEquippedTheme(themeId: String) {
        sound.playEquipSound()
        repository.setEquippedTheme(themeId)
        _uiState.update { it.copy(equippedThemeId = themeId) }
    }

    // --- Flame Items ---
    fun setEquippedFlame(flameId: String) {
        sound.playEquipSound()
        repository.setEquippedFlame(flameId)
        _uiState.update { it.copy(equippedFlameId = flameId) }
    }

    fun buyFlame(flame: FlameItem) {
        val state = _uiState.value
        if (state.unlockedFlames.contains(flame.id)) {
            setEquippedFlame(flame.id)
            return
        }
        if (flame.currency == CurrencyType.COINS && state.coins >= flame.cost) {
            val newCoins = state.coins - flame.cost
            repository.setCoins(newCoins)
            repository.unlockFlame(flame.id)
            repository.setEquippedFlame(flame.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedFlames = repository.getUnlockedFlames(),
                    equippedFlameId = flame.id,
                    bannerMessage = "Unlocked ${flame.name}! 🔥"
                )
            }
        } else if (flame.currency == CurrencyType.GEMS && state.gems >= flame.cost) {
            val newGems = state.gems - flame.cost
            repository.setGems(newGems)
            repository.unlockFlame(flame.id)
            repository.setEquippedFlame(flame.id)
            sound.playPurchaseSuccess()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedFlames = repository.getUnlockedFlames(),
                    equippedFlameId = flame.id,
                    bannerMessage = "Unlocked ${flame.name}! 🔥"
                )
            }
        } else {
            sound.playPurchaseFail()
            _uiState.update { it.copy(bannerMessage = "Not enough currency for ${flame.name}!") }
        }
    }

    // --- Star Road Milestones ---
    fun claimStarMilestone(milestone: StarMilestoneReward) {
        // Pass 5 (audit L8): verify the player actually EARNED the stars —
        // previously any claim paid out regardless of progress.
        val totalStars = _uiState.value.completedLevels.values.sum()
        if (totalStars < milestone.starsRequired) {
            showBannerMessage("⭐ Earn ${milestone.starsRequired} total stars to claim ${milestone.bonusTitle}!")
            return
        }
        val claimed = repository.getClaimedStarMilestones()
        if (claimed.contains(milestone.starsRequired)) return

        repository.claimStarMilestone(milestone.starsRequired)
        val newCoins = _uiState.value.coins + milestone.rewardCoins
        val newGems = _uiState.value.gems + milestone.rewardGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        sound.playClaimReward()
        triggerVibration(60L)
        _uiState.update {
            it.copy(
                claimedStarMilestones = repository.getClaimedStarMilestones(),
                coins = newCoins,
                gems = newGems,
                bannerMessage = "Claimed ${milestone.bonusTitle}! +${milestone.rewardCoins}🪙 +${milestone.rewardGems}💎 ⭐"
            )
        }
    }

    // Pass 5 (audit L12): the dead free `reviveSession()` was deleted — it was
    // never called, and its logic would have revived for free when the player
    // could not afford the cost. The single live revive is playOnRevive()
    // (100 coins / +15s), capped once per stage attempt.

    fun buyTheme(theme: ThemeConfig) {
        val state = _uiState.value
        if (state.unlockedThemes.contains(theme.id)) {
            setEquippedTheme(theme.id)
            return
        }
        if (theme.currency == CurrencyType.COINS && state.coins >= theme.cost) {
            val newCoins = state.coins - theme.cost
            repository.setCoins(newCoins)
            repository.unlockTheme(theme.id)
            repository.setEquippedTheme(theme.id)
            sound.playCoin()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedThemes = repository.getUnlockedThemes(),
                    equippedThemeId = theme.id
                )
            }
        } else if (theme.currency == CurrencyType.GEMS && state.gems >= theme.cost) {
            // Pass 5 (audit §6): the GEMS branch was missing — gem-priced
            // themes could never be bought. Mirrors the COINS branch.
            val newGems = state.gems - theme.cost
            repository.setGems(newGems)
            repository.unlockTheme(theme.id)
            repository.setEquippedTheme(theme.id)
            sound.playCoin()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedThemes = repository.getUnlockedThemes(),
                    equippedThemeId = theme.id
                )
            }
        }
    }

    // --- Gameplay Navigation ---
    /**
     * Pass 8: campaign stage-start bonuses. Must run AFTER the stage's base
     * state (timeLeft etc.) is set and BEFORE initGameSession() (which reads
     * timeLeft for spawn calibration and refreshes booster counts):
     * - Win-streak fuel: a streak of 2+ grants +5s on the timer.
     * - Kindness assist: a pending assist for this stage is consumed —
     *   +10s and +1 Freeze charge. The charge goes through
     *   [GameRepository.addBooster], the sanctioned setter that re-saves
     *   the integrity signature (booster totals are signed); the assist
     *   flag itself is an unsigned key, cleared with a plain write.
     */
    private fun applyCampaignStartBonuses(levelNum: Int) {
        if (levelNum <= 0) return
        var bonusSeconds = 0
        if (repository.getWinStreak() >= 2) {
            bonusSeconds += 5
        }
        if (repository.isAssistPending(levelNum)) {
            repository.setAssistPending(levelNum, false)
            repository.addBooster("booster_freeze", 1)
            bonusSeconds += 10
            showBannerMessage("💛 Popster's Help: +10s & free Freeze!")
        }
        if (bonusSeconds > 0) {
            _uiState.update { it.copy(timeLeft = it.timeLeft + bonusSeconds) }
        }
    }

    fun startCampaignStage(levelNum: Int) {
        val lvl = GameConstants.CAMPAIGN_LEVELS.getOrNull(levelNum - 1)
            ?: GameConstants.CAMPAIGN_LEVELS.first()
        // Pass 5 (audit L13): zones hold 10 stages, not 5 — the old /5 put
        // stage 41+ in nonexistent zones up to 41 and mismatched the music
        // theme and ambient particles against the map's zone art.
        val zoneNum = ((lvl.level - 1) / 10) + 1
        val isBoss = lvl.isBossStage || lvl.level % 10 == 0

        musicPlayer.setThemeForZone(zoneNum, isBoss)
        musicPlayer.resume()
        initAmbientsForZone(zoneNum)

        _uiState.update {
            it.copy(
                gameMode = GameMode.CAMPAIGN,
                currentLevel = lvl.level,
                targetPops = lvl.targetPops,
                targetScore = lvl.targetScore,
                timeLeft = lvl.timeLimit,
                maxMissesAllowed = lvl.maxMissesAllowed,
                missedCount = 0,
                currentScreen = CurrentScreen.GAMEPLAY
            )
        }
        applyCampaignStartBonuses(lvl.level)
        initGameSession()
    }

    /**
     * Custom Challenge Lab where players can input custom seconds, balloon target, and missed balloon limit.
     */
    fun startCustomChallenge(timeSeconds: Int, targetBalloons: Int, maxMisses: Int) {
        musicPlayer.setTheme(MusicTheme.TROPICAL_BOP)
        musicPlayer.resume()
        initAmbientsForZone(1)

        val clampedTime = timeSeconds.coerceIn(5, 120)
        val clampedPops = targetBalloons.coerceIn(5, 150)
        val clampedMisses = maxMisses.coerceIn(1, 20)

        _uiState.update {
            it.copy(
                gameMode = GameMode.CAMPAIGN,
                currentLevel = 0, // 0 denotes Custom Challenge
                targetPops = clampedPops,
                targetScore = clampedPops * 25,
                timeLeft = clampedTime,
                maxMissesAllowed = clampedMisses,
                missedCount = 0,
                currentScreen = CurrentScreen.GAMEPLAY
            )
        }
        initGameSession()
    }

    fun startEndlessMode(mode: GameMode) {
        when (mode) {
            GameMode.TIME_RUSH -> musicPlayer.setTheme(MusicTheme.CYBER_PULSE)
            GameMode.ZEN -> musicPlayer.setTheme(MusicTheme.COSMIC_DREAM)
            GameMode.ARCADE -> musicPlayer.setTheme(MusicTheme.TROPICAL_BOP)
            else -> musicPlayer.setTheme(MusicTheme.MEADOW_JOY)
        }
        musicPlayer.resume()
        initAmbientsForZone(1)

        _uiState.update {
            it.copy(
                gameMode = mode,
                timeLeft = if (mode == GameMode.TIME_RUSH) 60 else 0,
                missedCount = 0,
                currentScreen = CurrentScreen.GAMEPLAY
            )
        }
        initGameSession()
    }

    fun resumeGame() {
        sound.playClick()
        musicPlayer.resume()
        _uiState.update { it.copy(gameState = GameState.PLAYING) }
    }

    fun pauseGame() {
        sound.playClick()
        musicPlayer.pause()
        _uiState.update { it.copy(gameState = GameState.PAUSED) }
    }

    fun restartGame() {
        sound.playClick()
        val state = _uiState.value
        val levelNum = state.currentLevel
        val lvl = GameConstants.CAMPAIGN_LEVELS.getOrNull(levelNum - 1)
        if (state.gameMode == GameMode.CAMPAIGN && lvl != null && levelNum > 0) {
            _uiState.update {
                it.copy(
                    timeLeft = lvl.timeLimit,
                    maxMissesAllowed = lvl.maxMissesAllowed,
                    targetPops = lvl.targetPops,
                    missedCount = 0
                )
            }
            // Pass 8: a Retry is also a "next try" — streak +5s and a
            // pending kindness assist apply here exactly as on a fresh start.
            applyCampaignStartBonuses(levelNum)
        } else {
            _uiState.update {
                it.copy(
                    missedCount = 0
                )
            }
        }
        initGameSession()
    }

    fun playOnRevive(withCoins: Boolean = false) {
        // Pass 5 (audit L12): one revive per stage attempt — previously the
        // 100-coin/+15s revive could be farmed without limit on a rich save.
        if (stageReviveUsesCount >= 1) {
            sound.playPurchaseFail()
            _uiState.update { it.copy(bannerMessage = "Revive already used this attempt!") }
            return
        }
        if (withCoins) {
            val cost = 100
            val cur = _uiState.value.coins
            if (cur >= cost) {
                val newCoins = cur - cost
                repository.setCoins(newCoins)
                _uiState.update { it.copy(coins = newCoins) }
                addFloatingText(canvasWidth / 2f, 220f, "-100 🪙", Color(0xFFF59E0B))
            } else {
                sound.playPurchaseFail()
                _uiState.update { it.copy(bannerMessage = "Need 100 🪙 to revive!") }
                return
            }
        }
        stageReviveUsesCount++
        sound.playRainbowBlast()
        musicPlayer.resume()
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                timeLeft = it.timeLeft + 15,
                missedCount = 0,
                lives = 3
            )
        }
        startTimer()
    }

    fun nextLevel() {
        sound.playLevelWarp()
        val nextLvl = _uiState.value.currentLevel + 1
        if (nextLvl <= com.example.balloonpop.data.SecurityGuard.MAX_CAMPAIGN_LEVEL) {
            startLevelTransition(nextLvl)
        } else {
            navigateTo(CurrentScreen.CAMPAIGN_MAP)
        }
    }

    fun startLevelTransition(levelNum: Int) {
        sound.playLevelWarp()
        _uiState.update {
            it.copy(
                isTransitioningLevel = true,
                transitionTargetLevel = levelNum
            )
        }
    }

    fun finishLevelTransition() {
        val target = _uiState.value.transitionTargetLevel ?: (_uiState.value.currentLevel + 1)
        _uiState.update {
            it.copy(
                isTransitioningLevel = false,
                transitionTargetLevel = null
            )
        }
        startCampaignStage(target)
    }

    private fun initAmbientsForZone(zoneNum: Int) {
        synchronized(ambients) {
            ambients.clear()
            // Pass 5 (audit L13): 10-based zone indexing to match the
            // 10-stages-per-zone structure (was % 5, which mismatched the
            // zone's music theme and map art).
            val ambientType = when ((zoneNum - 1) % 10) {
                0 -> AmbientType.CHERRY_PETAL
                1 -> AmbientType.EMBER
                2 -> AmbientType.BUBBLE
                3 -> AmbientType.STAR
                4 -> AmbientType.GOLD_SPARKLE
                5 -> AmbientType.SNOWFLAKE
                6 -> AmbientType.CLOUD
                7 -> AmbientType.CHERRY_PETAL
                8 -> AmbientType.EMBER
                else -> AmbientType.GOLD_SPARKLE
            }
            val count = when (_uiState.value.graphicsQuality) {
                "ULTRA" -> 35
                "BATTERY_SAVER" -> 10
                else -> 22
            }
            for (i in 0 until count) {
                ambients.add(
                    AmbientElement(
                        x = Random.nextFloat() * canvasWidth,
                        y = Random.nextFloat() * canvasHeight,
                        vx = (Random.nextFloat() * 1.5f - 0.75f),
                        vy = (Random.nextFloat() * 1.2f + 0.5f),
                        size = Random.nextFloat() * 10f + 4f,
                        alpha = Random.nextFloat() * 0.6f + 0.2f,
                        color = when (ambientType) {
                            AmbientType.CHERRY_PETAL -> Color(0xFFFFB7B2)
                            AmbientType.EMBER -> Color(0xFFFF7A00)
                            AmbientType.BUBBLE -> Color(0x8038BDF8)
                            AmbientType.STAR -> Color(0xCCFFFFFF)
                            AmbientType.GOLD_SPARKLE -> Color(0xFFFFD700)
                            AmbientType.SNOWFLAKE -> Color(0xFFBDE0FE)
                            AmbientType.CLOUD -> Color(0xCCFFFFFF)
                            else -> Color.White
                        },
                        type = ambientType
                    )
                )
            }
        }
    }

    private fun initGameSession() {
        val state = _uiState.value
        synchronized(balloons) { balloons.clear() }
        synchronized(particles) { particles.clear() }
        synchronized(floatingTexts) { floatingTexts.clear() }
        synchronized(swipeTrail) { swipeTrail.clear() }
        synchronized(hammerStrikes) { hammerStrikes.clear() }
        synchronized(shockwaves) { shockwaves.clear() }
        synchronized(lightningBolts) { lightningBolts.clear() }
        synchronized(pendingSpawns) { pendingSpawns.clear() }

        patternCycleIndex = 0
        hasShownComboHint = false
        goldRushParticleAccumulator = 0f
        spawnAccumulator = 0f
        spawnIntervalSeconds = when {
            state.gameMode == GameMode.CAMPAIGN -> {
                // Calibrate spawn tempo so target balloon count spawns comfortably within the time limit
                val popsNeeded = state.targetPops.coerceAtLeast(10)
                val seconds = state.timeLeft.coerceAtLeast(10)
                val ideal = (seconds.toFloat() / (popsNeeded.toFloat() * 1.55f)).coerceIn(0.28f, 0.45f)
                ideal
            }
            state.gameMode == GameMode.TIME_RUSH -> 0.35f
            else -> 0.50f
        }
        freezeTimeRemaining = 0f
        hitStopRemaining = 0f
        multiplierTimeRemaining = 0f
        currentScreenShake = 0f
        internalSkillEnergy = 0f
        internalSkillCooldown = 0f
        hammerSlideTimeRemaining = 0f
        stageBoosterUsesCount = 0
        stageSkillUsesCount = 0
        stageReviveUsesCount = 0
        uiSyncAccumulator = 0f
        lastPopTime = 0L
        sessionBaselineHighScore = repository.getLifetimeHighScore()

        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                score = 0,
                lives = 3,
                poppedCount = 0,
                missedCount = 0,
                combo = 0,
                maxCombo = 1,
                isFreezeActive = false,
                isMultiplierActive = false,
                isHammerSlideActive = false,
                hammerSlideTimeRemaining = 0f,
                stageSkillUsesCount = 0,
                stageBoosterUsesCount = 0,
                screenShake = 0f,
                screenFlashAlpha = 0f,
                candyPraise = null,
                boosterCounts = repository.getAllBoosterCounts(),
                earnedCoinsThisSession = 0,
                earnedGemsThisSession = 0,
                isNewHighScoreAchieved = false,
                previousHighScore = sessionBaselineHighScore,
                showComboHintBanner = false,
                assistUnlocked = false
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        val mode = _uiState.value.gameMode
        if (mode == GameMode.CAMPAIGN || mode == GameMode.TIME_RUSH) {
            timerJob = viewModelScope.launch {
                while (_uiState.value.gameState == GameState.PLAYING || _uiState.value.gameState == GameState.PAUSED) {
                    delay(1000L)
                    if (_uiState.value.gameState == GameState.PLAYING && !com.example.balloonpop.ads.AdMobManager.isAdShowing) {
                        val current = _uiState.value.timeLeft
                        if (current > 1) {
                            _uiState.update { it.copy(timeLeft = current - 1) }
                        } else {
                            _uiState.update { it.copy(timeLeft = 0) }
                            checkGameEndOnTimer()
                            break
                        }
                    }
                }
            }
        }
    }

    private fun checkGameEndOnTimer() {
        val state = _uiState.value
        // Pass 5 (audit L2): a custom challenge (CAMPAIGN mode, currentLevel
        // 0) must never reach stageClear() — it ends as a plain game over.
        if (state.gameMode == GameMode.CAMPAIGN && state.currentLevel > 0) {
            if (state.poppedCount >= state.targetPops) {
                stageClear()
            } else {
                gameOver(GameOverReason.TIME_EXPIRED)
            }
        } else {
            gameOver(GameOverReason.TIME_EXPIRED)
        }
    }

    /**
     * Synchronous frame step driven by withFrameNanos.
     * Guarantees perfectly smooth 60/120 FPS without race conditions or stuttering.
     */
    fun stepFrame(dtSeconds: Float) {
        val state = _uiState.value
        if (state.gameState != GameState.PLAYING || com.example.balloonpop.ads.AdMobManager.isAdShowing) return

        val step = dtSeconds * 60f // Normalized to 60fps unit

        // 0. Booster Cooldowns Countdown
        if (internalBoosterCooldowns.isNotEmpty()) {
            val it = internalBoosterCooldowns.iterator()
            var changed = false
            while (it.hasNext()) {
                val entry = it.next()
                val nextVal = (entry.value - dtSeconds).coerceAtLeast(0f)
                if (nextVal <= 0f) {
                    it.remove()
                    changed = true
                } else {
                    entry.setValue(nextVal)
                    changed = true
                }
            }
            if (changed) {
                _uiState.update { it.copy(boosterCooldowns = internalBoosterCooldowns.toMap()) }
            }
        }

        // 1. Freeze Countdown
        if (freezeTimeRemaining > 0f) {
            freezeTimeRemaining -= dtSeconds
            if (freezeTimeRemaining <= 0f) {
                freezeTimeRemaining = 0f
                _uiState.update { it.copy(isFreezeActive = false) }
            }
        }

        // 1b. Pass 8: Hit-stop Countdown — decremented by real frame dt,
        // mirroring the freeze pattern. While hitStopped, balloon motion
        // (section 6) and the spawner (section 5 + trigger block) pause;
        // particles, timers' visual effects and rendering continue.
        if (hitStopRemaining > 0f) {
            hitStopRemaining -= dtSeconds
            if (hitStopRemaining < 0f) hitStopRemaining = 0f
        }
        val hitStopped = hitStopRemaining > 0f

        // 2. Multiplier Countdown
        if (multiplierTimeRemaining > 0f) {
            multiplierTimeRemaining -= dtSeconds
            if (multiplierTimeRemaining <= 0f) {
                multiplierTimeRemaining = 0f
                _uiState.update { it.copy(isMultiplierActive = false) }
            }
        }

        // 3. Hammer Slide Duration Countdown (5 seconds active duration)
        if (hammerSlideTimeRemaining > 0f) {
            hammerSlideTimeRemaining -= dtSeconds
            if (hammerSlideTimeRemaining <= 0f) {
                hammerSlideTimeRemaining = 0f
                _uiState.update { it.copy(isHammerSlideActive = false, hammerSlideTimeRemaining = 0f) }
            } else {
                _uiState.update { it.copy(isHammerSlideActive = true, hammerSlideTimeRemaining = hammerSlideTimeRemaining) }
            }
        }

        // 3. Skill Cooldown Recovery (Skill energy is strictly earned via popping balloons to make skills rare!)
        var triggerSync = false
        if (internalSkillCooldown > 0f) {
            internalSkillCooldown = (internalSkillCooldown - dtSeconds).coerceAtLeast(0f)
            if (internalSkillCooldown == 0f) triggerSync = true
        }

        uiSyncAccumulator += dtSeconds
        if (uiSyncAccumulator >= 0.12f || triggerSync) {
            uiSyncAccumulator = 0f
            if (_uiState.value.skillEnergy != internalSkillEnergy || _uiState.value.skillCooldownRemaining != internalSkillCooldown) {
                _uiState.update {
                    it.copy(
                        skillEnergy = internalSkillEnergy,
                        skillCooldownRemaining = internalSkillCooldown
                    )
                }
            }
        }

        // 4. Screen Shake Decay (High-performance float decay read directly by GameCanvas)
        if (currentScreenShake > 0.3f) {
            currentScreenShake *= kotlin.math.max(0f, 1f - (dtSeconds * 8f))
        } else {
            currentScreenShake = 0f
        }

        // 5. Spawning Formations & Pending Balloons
        // Pass 8: hit-stop pauses the spawner too (pending emissions wait).
        if (!hitStopped) synchronized(pendingSpawns) {
            if (pendingSpawns.isNotEmpty()) {
                val it = pendingSpawns.iterator()
                while (it.hasNext()) {
                    val pending = it.next()
                    pending.delayRemaining -= dtSeconds
                    if (pending.delayRemaining <= 0f) {
                        it.remove()
                        // Pass 8: a GOLD_RUSH burst completes when its last
                        // staggered balloon is emitted — mark it with a
                        // beat of hit-stop.
                        if (pending.isGoldRush && pendingSpawns.none { other -> other.isGoldRush }) {
                            hitStopRemaining = 0.06f
                        }
                        val skin = equippedBalloonSkin()
                        synchronized(balloons) {
                            if (balloons.size < 32) {
                                balloons.add(
                                    Balloon(
                                        id = ++balloonIdCounter,
                                        x = pending.x,
                                        y = pending.y,
                                        radius = pending.radius,
                                        vy = pending.vy,
                                        type = pending.type,
                                        color = pending.color,
                                        wobblePhase = Random.nextFloat() * 6.28f,
                                        shapeStyle = pending.shapeStyle,
                                        hp = pending.hp,
                                        maxHp = pending.hp,
                                        pattern = if (pending.type == BalloonType.STANDARD) skin.pattern else BalloonPattern.SOLID,
                                        glossStrength = if (pending.type == BalloonType.STANDARD) skin.glossStrength else 1.0f,
                                        isRingFormation = pending.isRingFormation,
                                        ringAngle = pending.ringAngle,
                                        ringCenterX = pending.ringCenterX,
                                        ringCenterY = pending.ringCenterY,
                                        ringTargetRadius = pending.ringTargetRadius,
                                        isGoldRush = pending.isGoldRush
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (!hitStopped) spawnAccumulator += dtSeconds
        val effectiveInterval = if (freezeTimeRemaining > 0f) spawnIntervalSeconds * 2f else spawnIntervalSeconds
        val activeBalloons = synchronized(balloons) { balloons.count { !it.popped } }
        val pendingCount = synchronized(pendingSpawns) { pendingSpawns.size }
        val densityFloor = if (state.gameMode == GameMode.CAMPAIGN) {
            when {
                state.currentLevel <= 20 -> 7  // zones 1-2 (stages 1-20)
                state.currentLevel <= 80 -> 8  // zones 3-8
                else -> 9                       // zones 9-20
            }
        } else {
            8 // endless modes
        }

        if (!hitStopped) {
            if (activeBalloons + pendingCount < densityFloor) {
                spawnAccumulator = 0f
                triggerNextFormation()
            } else if (spawnAccumulator >= effectiveInterval && pendingCount == 0) {
                spawnAccumulator = 0f
                triggerNextFormation()
            }
        }

        // Check for first-stage combo hint: 3 same-colour balloons on screen
        if (state.gameMode == GameMode.CAMPAIGN && state.currentLevel <= 2 && !hasShownComboHint && state.gameState == GameState.PLAYING) {
            val maxSameColor = synchronized(balloons) {
                balloons.filter { !it.popped && it.type == BalloonType.STANDARD }
                    .groupBy { it.color }
                    .values.maxOfOrNull { it.size } ?: 0
            }
            if (maxSameColor >= 3) {
                hasShownComboHint = true
                _uiState.update { it.copy(showComboHintBanner = true) }
                viewModelScope.launch {
                    delay(4500L)
                    _uiState.update { it.copy(showComboHintBanner = false) }
                }
            }
        }

        // 6. Update Balloons (motion, scale-in, ring expansion, sway tilt, and gold rush sparkle trail)
        // Pass 8: during hit-stop the whole motion update is skipped —
        // balloons hang frozen for the beat while rendering continues.
        val speedFactor = if (freezeTimeRemaining > 0f) 0.35f else 1.0f
        if (!hitStopped) synchronized(balloons) {
            val it = balloons.iterator()
            while (it.hasNext()) {
                val b = it.next()
                if (b.popped) {
                    it.remove()
                    continue
                }
                b.spawnAge += dtSeconds
                if (b.squashTimer > 0f) {
                    b.squashTimer = (b.squashTimer - dtSeconds).coerceAtLeast(0f)
                }

                val speedMult = if (b.type == BalloonType.FAST_ROCKET) 1.65f else 1.0f
                val deltaY = b.vy * speedFactor * speedMult * step

                if (b.isRingFormation) {
                    // RING formation expands outward from center while drifting up
                    b.ringCenterY -= deltaY
                    val expansionProgress = (b.spawnAge / 0.60f).coerceIn(0.20f, 1.0f)
                    val currentRingR = b.ringTargetRadius * expansionProgress
                    b.x = b.ringCenterX + kotlin.math.cos(b.ringAngle) * currentRingR
                    b.y = b.ringCenterY + kotlin.math.sin(b.ringAngle) * currentRingR
                } else {
                    b.y -= deltaY
                    b.wobblePhase += 0.045f * step
                    b.x += sin(b.wobblePhase) * 0.95f * step
                }

                // Sway tilt
                b.tiltAngle = sin(b.wobblePhase) * 6.5f

                // Gold rush sparkle trails
                if (b.isGoldRush && b.y < canvasHeight + 10f) {
                    goldRushParticleAccumulator += dtSeconds
                    if (goldRushParticleAccumulator >= 0.07f) {
                        goldRushParticleAccumulator = 0f
                        createSparks(
                            b.x + (Random.nextFloat() - 0.5f) * (b.radius * 0.7f),
                            b.y + b.radius * 0.85f,
                            Color(0xFFFFD700),
                            count = 2
                        )
                    }
                }

                // Ghost Phantom alpha shimmer
                if (b.type == BalloonType.GHOST_PHANTOM) {
                    b.alpha = (0.5f + 0.45f * sin(b.wobblePhase * 1.8f)).coerceIn(0.15f, 1f)
                }

                // Missed balloon out of screen
                if (b.y + b.radius < -30f) {
                    if (b.type == BalloonType.MEGA_BOSS) {
                        // Boss swoops back with thunderous fury so the battle continues!
                        b.y = canvasHeight + b.radius + 15f
                        b.x = canvasWidth * (0.25f + Random.nextFloat() * 0.5f)
                        addFloatingText(b.x, canvasHeight - 80f, "⚠️ BOSS RETURNING! 👑", Color(0xFFFF3366))
                        currentScreenShake = 16f
                    } else {
                        it.remove()
                        if (b.type != BalloonType.BOMB) {
                            onMissBalloon(b.x)
                        }
                    }
                }
            }
        }

        // 7. Update Ambients
        synchronized(ambients) {
            for (amb in ambients) {
                amb.x += amb.vx * step
                amb.y += amb.vy * step
                if (amb.y > canvasHeight + 20f) amb.y = -20f
                if (amb.x > canvasWidth + 20f) amb.x = -20f
                if (amb.x < -20f) amb.x = canvasWidth + 20f
            }
        }

        // 8. Update Shockwaves
        synchronized(shockwaves) {
            val it = shockwaves.iterator()
            while (it.hasNext()) {
                val sw = it.next()
                sw.radius += 10f * step
                sw.alpha -= 0.06f * step
                if (sw.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 9. Update Hammer Strikes
        synchronized(hammerStrikes) {
            val it = hammerStrikes.iterator()
            while (it.hasNext()) {
                val strike = it.next()
                strike.progress += 0.12f * step
                if (strike.progress >= 1.0f) {
                    it.remove()
                }
            }
        }

        // 10. Update Particles
        synchronized(particles) {
            val it = particles.iterator()
            while (it.hasNext()) {
                val p = it.next()
                p.x += p.vx * step
                p.y += p.vy * step
                p.rotation += p.vRot * step
                p.vx *= (1f - 0.02f * step)
                val gravityFactor = when (p.particleType) {
                    ParticleType.RUBBER_SHRED -> 0.22f
                    ParticleType.CONFETTI_RIBBON -> 0.12f
                    ParticleType.ICE_CRYSTAL -> 0.18f
                    ParticleType.GLOW_SPARK -> 0.08f
                    else -> 0.16f
                }
                p.vy += gravityFactor * step
                val decayRate = when (p.particleType) {
                    ParticleType.GLOW_SPARK -> 0.065f
                    ParticleType.SPARKLE_STAR -> 0.045f
                    ParticleType.RUBBER_SHRED -> 0.032f
                    else -> 0.038f
                }
                p.alpha -= decayRate * step
                if (p.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 11. Update Floating Texts (bounce in and fade)
        synchronized(floatingTexts) {
            val it = floatingTexts.iterator()
            while (it.hasNext()) {
                val ft = it.next()
                ft.age += dtSeconds
                ft.y += ft.vy * step
                ft.alpha -= 0.035f * step
                val t = (ft.age / 0.22f).coerceIn(0f, 1f)
                ft.scale = if (t < 0.6f) 0.5f + 0.75f * (t / 0.6f) else 1.25f - 0.25f * ((t - 0.6f) / 0.4f)
                if (ft.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 12. Update Swipe Trail
        synchronized(swipeTrail) {
            val it = swipeTrail.iterator()
            while (it.hasNext()) {
                val t = it.next()
                t.alpha -= 0.12f * step
                if (t.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 13. Update Lightning Bolts
        synchronized(lightningBolts) {
            val it = lightningBolts.iterator()
            while (it.hasNext()) {
                val bolt = it.next()
                bolt.alpha -= 0.07f * step
                if (bolt.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 14. Screen Flash decay
        if (_uiState.value.screenFlashAlpha > 0f) {
            val nextFlash = (_uiState.value.screenFlashAlpha - 0.05f * step).coerceAtLeast(0f)
            _uiState.update { it.copy(screenFlashAlpha = nextFlash) }
        }
    }

    private fun spawnBalloon() {
        triggerNextFormation()
    }

    private fun triggerNextFormation() {
        val mode = _uiState.value.gameMode
        val lvl = _uiState.value.currentLevel
        val recipe = if (mode == GameMode.CAMPAIGN && lvl > 0) StageRecipes.getRecipe(lvl) else null

        val formation: SpawnFormation = if (recipe != null) {
            val allowed = recipe.allowedFormations
            if (allowed.contains(SpawnFormation.GOLD_RUSH) && Random.nextFloat() < recipe.goldRushChance) {
                SpawnFormation.GOLD_RUSH
            } else {
                patternCycleIndex = (patternCycleIndex + 1) % allowed.size
                allowed[patternCycleIndex]
            }
        } else if (mode == GameMode.ZEN) {
            listOf(SpawnFormation.CLUSTER, SpawnFormation.STREAM, SpawnFormation.RING, SpawnFormation.MIXED_RAIN).random()
        } else {
            listOf(
                SpawnFormation.CLUSTER,
                SpawnFormation.STREAM,
                SpawnFormation.RING,
                SpawnFormation.BOMB_DODGE,
                SpawnFormation.MIXED_RAIN,
                SpawnFormation.GOLD_RUSH
            ).random()
        }

        scheduleFormation(formation, recipe)
    }

    private fun scheduleFormation(formation: SpawnFormation, recipe: StageRecipe?) {
        val skin = equippedBalloonSkin()
        when (formation) {
            SpawnFormation.CLUSTER -> {
                // CLUSTER: 5–7 balloons of one colour in a loose bunch
                val count = Random.nextInt(5, 8)
                val clusterColor = skin.palette.random()
                val cx = (radiusBase() + 30f + Random.nextFloat() * (canvasWidth - radiusBase() * 2f - 60f).coerceAtLeast(100f))
                val cy = canvasHeight + radiusBase() + 30f
                val clusterSpeed = computeBaseSpeed(BalloonType.STANDARD)

                synchronized(pendingSpawns) {
                    for (i in 0 until count) {
                        val ox = (Random.nextFloat() - 0.5f) * 150f
                        val oy = (Random.nextFloat() - 0.5f) * 130f
                        val r = computeBalloonRadius(BalloonType.STANDARD)
                        pendingSpawns.add(
                            PendingSpawn(
                                delayRemaining = i * 0.04f,
                                x = (cx + ox).coerceIn(r + 15f, canvasWidth - r - 15f),
                                y = cy + oy,
                                radius = r,
                                vy = clusterSpeed * (0.94f + Random.nextFloat() * 0.12f),
                                type = BalloonType.STANDARD,
                                color = clusterColor,
                                shapeStyle = skin.faceStyles.random()
                            )
                        )
                    }
                }
            }
            SpawnFormation.STREAM -> {
                // STREAM: 6–8 balloons along a diagonal, released in quick sequence
                val count = Random.nextInt(6, 9)
                val goRight = Random.nextBoolean()
                val startX = if (goRight) canvasWidth * 0.16f else canvasWidth * 0.84f
                val dx = if (goRight) (canvasWidth * 0.68f / count) else (-canvasWidth * 0.68f / count)
                val streamSpeed = computeBaseSpeed(BalloonType.STANDARD)

                synchronized(pendingSpawns) {
                    for (i in 0 until count) {
                        val type = pickBalloonType(recipe)
                        val color = getBalloonColor(type, skin)
                        val r = computeBalloonRadius(type)
                        val maxHp = if (type == BalloonType.SHIELDED) 2 else 1
                        pendingSpawns.add(
                            PendingSpawn(
                                delayRemaining = i * 0.14f,
                                x = (startX + i * dx).coerceIn(r + 15f, canvasWidth - r - 15f),
                                y = canvasHeight + r + 20f,
                                radius = r,
                                vy = streamSpeed,
                                type = type,
                                color = color,
                                shapeStyle = getBalloonShape(type, skin),
                                hp = maxHp
                            )
                        )
                    }
                }
            }
            SpawnFormation.RING -> {
                // RING: 8 balloons in a circle drifting up together
                val cx = canvasWidth * (0.35f + Random.nextFloat() * 0.30f)
                val cy = canvasHeight + 115f
                val ringSpeed = computeBaseSpeed(BalloonType.STANDARD)
                val targetR = 135f

                synchronized(pendingSpawns) {
                    for (i in 0 until 8) {
                        val angle = (i.toFloat() / 8f) * 6.2831853f
                        val type = pickBalloonType(recipe)
                        val color = getBalloonColor(type, skin)
                        val r = computeBalloonRadius(type)
                        val maxHp = if (type == BalloonType.SHIELDED) 2 else 1
                        pendingSpawns.add(
                            PendingSpawn(
                                delayRemaining = 0f,
                                x = cx + 25f * kotlin.math.cos(angle),
                                y = cy + 25f * kotlin.math.sin(angle),
                                radius = r,
                                vy = ringSpeed,
                                type = type,
                                color = color,
                                shapeStyle = getBalloonShape(type, skin),
                                hp = maxHp,
                                isRingFormation = true,
                                ringAngle = angle,
                                ringCenterX = cx,
                                ringCenterY = cy,
                                ringTargetRadius = targetR
                            )
                        )
                    }
                }
            }
            SpawnFormation.BOMB_DODGE -> {
                // BOMB DODGE: 6 normal balloons arranged around 1 bomb balloon (never before stage 4)
                val cx = canvasWidth * (0.35f + Random.nextFloat() * 0.30f)
                val cy = canvasHeight + 120f
                val dodgeSpeed = computeBaseSpeed(BalloonType.STANDARD)
                val surroundR = 125f

                synchronized(pendingSpawns) {
                    // Center Bomb
                    pendingSpawns.add(
                        PendingSpawn(
                            delayRemaining = 0f,
                            x = cx,
                            y = cy,
                            radius = computeBalloonRadius(BalloonType.BOMB),
                            vy = dodgeSpeed,
                            type = BalloonType.BOMB,
                            color = Color(0xFF222226),
                            shapeStyle = BalloonShapeStyle.ANGRY
                        )
                    )
                    // 6 normal balloons arranged around it
                    for (k in 0 until 6) {
                        val angle = (k.toFloat() / 6f) * 6.2831853f
                        val color = skin.palette.random()
                        val r = computeBalloonRadius(BalloonType.STANDARD)
                        pendingSpawns.add(
                            PendingSpawn(
                                delayRemaining = 0f,
                                x = cx + surroundR * kotlin.math.cos(angle),
                                y = cy + surroundR * kotlin.math.sin(angle),
                                radius = r,
                                vy = dodgeSpeed,
                                type = BalloonType.STANDARD,
                                color = color,
                                shapeStyle = skin.faceStyles.random()
                            )
                        )
                    }
                }
            }
            SpawnFormation.GOLD_RUSH -> {
                // GOLD RUSH: a short burst of 4–6 golden balloons (rare, celebratory)
                val count = Random.nextInt(4, 7)
                val cx = canvasWidth * (0.30f + Random.nextFloat() * 0.40f)
                val cy = canvasHeight + 90f
                val goldSpeed = computeBaseSpeed(BalloonType.GOLDEN)

                synchronized(pendingSpawns) {
                    for (i in 0 until count) {
                        val ox = (Random.nextFloat() - 0.5f) * 180f
                        val oy = (Random.nextFloat() - 0.5f) * 120f
                        val r = computeBalloonRadius(BalloonType.GOLDEN)
                        pendingSpawns.add(
                            PendingSpawn(
                                delayRemaining = i * 0.08f,
                                x = (cx + ox).coerceIn(r + 20f, canvasWidth - r - 20f),
                                y = cy + oy,
                                radius = r,
                                vy = goldSpeed * (0.94f + Random.nextFloat() * 0.12f),
                                type = BalloonType.GOLDEN,
                                color = Color(0xFFFFD700),
                                shapeStyle = BalloonShapeStyle.STAR_SHAPE,
                                isGoldRush = true
                            )
                        )
                    }
                }
            }
            SpawnFormation.MIXED_RAIN -> {
                // MIXED RAIN: standard single balloon (used sparingly as filler between patterns)
                scheduleSingleBalloon(recipe, skin)
            }
        }
    }

    private fun scheduleSingleBalloon(recipe: StageRecipe?, skin: BalloonSkin) {
        val type = pickBalloonType(recipe)
        val radius = computeBalloonRadius(type)
        val x = radius + Random.nextFloat() * (canvasWidth - radius * 2).coerceAtLeast(100f)
        val y = canvasHeight + radius + 15f
        val vy = computeBaseSpeed(type)
        val color = getBalloonColor(type, skin)
        val shape = getBalloonShape(type, skin)
        val maxHp = when (type) {
            BalloonType.MEGA_BOSS -> (15 + (_uiState.value.currentLevel / 10) * 5).coerceIn(15, 45)
            BalloonType.SHIELDED -> 2
            else -> 1
        }
        synchronized(pendingSpawns) {
            pendingSpawns.add(
                PendingSpawn(
                    delayRemaining = 0f,
                    x = x,
                    y = y,
                    radius = radius,
                    vy = vy,
                    type = type,
                    color = color,
                    shapeStyle = shape,
                    hp = maxHp
                )
            )
        }
    }

    private fun radiusBase(): Float {
        val mode = _uiState.value.gameMode
        val lvl = _uiState.value.currentLevel
        val isEarlyStage = mode == GameMode.CAMPAIGN && lvl <= 2
        return if (isEarlyStage) (68f + Random.nextFloat() * 20f) else (56f + Random.nextFloat() * 24f)
    }

    private fun computeBalloonRadius(type: BalloonType): Float {
        val baseR = radiusBase()
        return if (type == BalloonType.MEGA_BOSS) baseR * 1.6f else baseR
    }

    private fun computeBaseSpeed(type: BalloonType): Float {
        val mode = _uiState.value.gameMode
        val lvl = _uiState.value.currentLevel
        val rawSpeed = when {
            type == BalloonType.MEGA_BOSS -> 2.4f
            type == BalloonType.FAST_ROCKET -> 7.8f + Random.nextFloat() * 3.5f
            mode == GameMode.TIME_RUSH -> 6.2f + Random.nextFloat() * 3.0f
            mode == GameMode.ARCADE -> 5.5f + Random.nextFloat() * 3.0f
            mode == GameMode.ZEN -> 2.6f + Random.nextFloat() * 1.5f
            else -> 4.8f + (lvl * 0.05f).coerceAtMost(4.5f) + Random.nextFloat() * 2.8f
        }
        return when {
            mode == GameMode.CAMPAIGN && lvl <= 2 -> rawSpeed * 0.70f
            mode == GameMode.CAMPAIGN && lvl <= 20 -> rawSpeed * 0.85f
            else -> rawSpeed
        }
    }

    private fun pickBalloonType(recipe: StageRecipe?): BalloonType {
        val mode = _uiState.value.gameMode
        val lvl = _uiState.value.currentLevel

        val isBossLevel = mode == GameMode.CAMPAIGN && (lvl % 10 == 0)
        val bossAlreadySpawned = synchronized(balloons) { balloons.any { it.type == BalloonType.MEGA_BOSS && !it.popped } }
            || synchronized(pendingSpawns) { pendingSpawns.any { it.type == BalloonType.MEGA_BOSS } }
        if (isBossLevel && !bossAlreadySpawned && (balloons.size <= 2 || Random.nextInt(100) < 50)) {
            return BalloonType.MEGA_BOSS
        }

        if (mode == GameMode.ZEN) {
            val nonBombs = listOf(
                BalloonType.STANDARD, BalloonType.GOLDEN, BalloonType.RAINBOW,
                BalloonType.FREEZE, BalloonType.MULTIPLIER, BalloonType.MYSTERY_GIFT,
                BalloonType.GOLDEN_PIGGY
            )
            return nonBombs.random()
        }

        if (recipe == null) {
            val roll = Random.nextInt(100)
            return if (roll < 75) BalloonType.STANDARD else BalloonType.GOLDEN
        }

        val roll = Random.nextFloat()
        if (roll >= recipe.specialChance || recipe.unlockedBalloonTypes.size <= 1) {
            return BalloonType.STANDARD
        }

        val availableSpecials = recipe.unlockedBalloonTypes.filter {
            it != BalloonType.STANDARD && it != BalloonType.MEGA_BOSS
        }
        return if (availableSpecials.isNotEmpty()) availableSpecials.random() else BalloonType.STANDARD
    }

    private fun getBalloonColor(type: BalloonType, skin: BalloonSkin): Color {
        return when (type) {
            BalloonType.GOLDEN -> Color(0xFFFFD700)
            BalloonType.BOMB -> Color(0xFF222226)
            BalloonType.FREEZE -> Color(0xFF38BDF8)
            BalloonType.HEART -> Color(0xFFE5484D)
            BalloonType.MULTIPLIER -> Color(0xFFFFB703)
            BalloonType.RAINBOW -> Color(0xFF7C4DBD)
            BalloonType.FAST_ROCKET -> Color(0xFFFF0055)
            BalloonType.SPLITTER -> Color(0xFF2F6DB3)
            BalloonType.SHIELDED -> Color(0xFF94A3B8)
            BalloonType.MYSTERY_GIFT -> Color(0xFFEC4899)
            BalloonType.GHOST_PHANTOM -> Color(0xFFE2E8F0)
            BalloonType.GOLDEN_PIGGY -> Color(0xFFF59E0B)
            BalloonType.ELECTRIC_STORM -> Color(0xFF38BDF8)
            BalloonType.MEGA_BOSS -> Color(0xFF7C3AED)
            else -> skin.palette.random()
        }
    }

    private fun getBalloonShape(type: BalloonType, skin: BalloonSkin): BalloonShapeStyle {
        return when (type) {
            BalloonType.BOMB -> BalloonShapeStyle.ANGRY
            BalloonType.GOLDEN, BalloonType.GOLDEN_PIGGY -> BalloonShapeStyle.STAR_SHAPE
            BalloonType.HEART -> BalloonShapeStyle.HEART_SHAPE
            BalloonType.FREEZE -> BalloonShapeStyle.SHOCKED
            BalloonType.MULTIPLIER -> BalloonShapeStyle.STAR_SHAPE
            BalloonType.RAINBOW -> listOf(BalloonShapeStyle.COOL, BalloonShapeStyle.FUNNY, BalloonShapeStyle.CAT_FACE).random()
            BalloonType.MEGA_BOSS -> BalloonShapeStyle.ANGRY
            else -> skin.faceStyles.random()
        }
    }

    fun handlePointerInput(x: Float, y: Float, isTap: Boolean = false) {
        if (_uiState.value.gameState != GameState.PLAYING) return

        val hammer = GameConstants.SHOP_HAMMERS.find { it.id == _uiState.value.equippedHammerId }
            ?: GameConstants.SHOP_HAMMERS.first()

        val isHammerSlideActive = _uiState.value.isHammerSlideActive && hammerSlideTimeRemaining > 0f

        if (isTap) {
            // TAP ONLY ACTION: Hammer strike at tap location
            val now = System.currentTimeMillis()
            lastHammerTapTime = now
            sound.playHammerSmash(hammer.tier)
            synchronized(hammerStrikes) {
                if (hammerStrikes.size < 6) {
                    hammerStrikes.add(
                        HammerStrikeEffect(
                            id = ++hammerEffectCounter,
                            x = x,
                            y = y,
                            progress = 0f,
                            hammerSkin = hammer
                        )
                    )
                }
            }
            val sparkMultiplier = when (_uiState.value.graphicsQuality) {
                "ULTRA" -> 16
                "BATTERY_SAVER" -> 5
                else -> 10
            }
            val equippedFlame = GameConstants.SHOP_FLAMES.find { it.id == _uiState.value.equippedFlameId }
                ?: GameConstants.SHOP_FLAMES.first()

            createSparks(x, y, hammer.sparkColor, count = sparkMultiplier + hammer.tier * 2)
            createSparks(x, y, equippedFlame.primaryColor, count = 4)

            // Hit detection for tap strike
            val wideAssistBonus = if (_uiState.value.isWideAssistEnabled) 20f else 0f
            val hitRadius = 26f + (hammer.tier * 4f) + wideAssistBonus

            synchronized(balloons) {
                for (i in balloons.indices.reversed()) {
                    val b = balloons.getOrNull(i) ?: continue
                    if (b.popped) continue

                    val dist = hypot(x - b.x, y - b.y)
                    if (dist <= b.radius + hitRadius) {
                        popBalloon(b)
                    }
                }
            }
        } else {
            // SLIDING / DRAGGING ACTION (!isTap):
            // The hammer ONLY works on tap option, NOT sliding!
            // Sliding is strictly restricted unless the 5-second Hammer Slide skill is ACTIVE!
            if (!isHammerSlideActive) {
                return // Ignore sliding gesture! No trail, no popping balloons!
            }

            // Hammer Slide Skill IS ACTIVE: slide hammer across the screen for 5s!
            synchronized(swipeTrail) {
                if (swipeTrail.size < 35) {
                    swipeTrail.add(BladeTrailPoint(x, y, 1.0f, hammer.sparkColor))
                }
            }

            // Magnetic pull effect if Magnet skill is active
            if (_uiState.value.equippedSkillId == "skill_magnet" && _uiState.value.isSkillActive) {
                synchronized(balloons) {
                    for (b in balloons) {
                        if (b.type == BalloonType.GOLDEN || b.type == BalloonType.RAINBOW || b.type == BalloonType.MULTIPLIER) {
                            b.x += (x - b.x) * 0.08f
                            b.y += (y - b.y) * 0.08f
                        }
                    }
                }
            }

            // Hit Detection along sliding trail during active Hammer Slide
            val wideAssistBonus = if (_uiState.value.isWideAssistEnabled) 20f else 0f
            val slideHitRadius = 30f + (hammer.tier * 5f) + wideAssistBonus

            synchronized(balloons) {
                for (i in balloons.indices.reversed()) {
                    val b = balloons.getOrNull(i) ?: continue
                    if (b.popped) continue

                    val dist = hypot(x - b.x, y - b.y)
                    if (dist <= b.radius + slideHitRadius) {
                        popBalloon(b)
                        createSparks(x, y, hammer.sparkColor, count = 4)
                    }
                }
            }
        }
    }

    fun popBalloon(b: Balloon) {
        if (b.hp > 1) {
            b.hp -= 1
            b.squashTimer = 0.15f
            sound.playShieldHit()
            triggerVibration(35L)
            if (b.type == BalloonType.MEGA_BOSS) {
                currentScreenShake = 16f
                val damageText = "BOSS -1 HP! 👑 (${b.hp}/${b.maxHp})"
                addFloatingText(b.x, b.y - 25f, damageText, Color(0xFFFFD700))
                createParticleBurst(b.x, b.y, Color(0xFFFFD700), count = 8)
                _uiState.update { it.copy(poppedCount = it.poppedCount + 1, score = it.score + 50) }
            } else {
                addFloatingText(b.x, b.y - 15f, "HIT! 🛡️", Color(0xFF38BDF8))
                createSparks(b.x, b.y, Color(0xFF4A90C4), count = 8)
            }
            return
        }

        b.popped = true
        b.squashTimer = 0.12f
        triggerSpecialBalloonShake(b.type)

        // Spawn visual pop shockwave
        synchronized(shockwaves) {
            if (shockwaves.size < 8) {
                shockwaves.add(
                    PopShockwave(
                        x = b.x,
                        y = b.y,
                        radius = 12f,
                        maxRadius = b.radius * 2.2f,
                        color = b.color
                    )
                )
            }
        }

        val now = System.currentTimeMillis()
        var newCombo = _uiState.value.combo
        if (now - lastPopTime < 750) {
            newCombo++
        } else {
            newCombo = 1
        }
        lastPopTime = now

        // Pass 8 praise callouts: escalating Candy Crush-style praise via
        // the bannerMessage system, fired once per threshold crossing
        // (the combo climbs one pop at a time, so equality IS the crossing;
        // past 20 the top praise repeats every further +10).
        val prevCombo = _uiState.value.combo
        val praiseText = when {
            newCombo == 5 && prevCombo < 5 -> "Great! 🔥"
            newCombo == 10 && prevCombo < 10 -> "Amazing! ⚡"
            newCombo == 15 && prevCombo < 15 -> "Incredible! 🌟"
            newCombo >= 20 && newCombo % 10 == 0 && prevCombo < newCombo -> "UNSTOPPABLE! 👑"
            else -> null
        }
        if (praiseText != null) showBannerMessage(praiseText)

        // Flame Aura Critical Pop Bonus (Psychological variable ratio reward)
        val flame = GameConstants.SHOP_FLAMES.find { it.id == _uiState.value.equippedFlameId }
            ?: GameConstants.SHOP_FLAMES.first()
        if (Random.nextInt(100) < flame.critPopBonusPercent) {
            createSparks(b.x, b.y, flame.primaryColor, count = 10)
            addFloatingText(b.x, b.y - 30f, "🔥 CRIT POP! +5🪙", flame.glowColor)
            val newCoins = _uiState.value.coins + 5
            repository.setCoins(newCoins)
            _uiState.update { it.copy(coins = newCoins) }
        }

        // Recharge skill energy rare & earned: requires high combos and deliberate pops!
        val energyGain = if (newCombo >= 8) 1.0f else if (newCombo >= 4) 0.5f else 0.25f
        internalSkillEnergy = (internalSkillEnergy + energyGain).coerceAtMost(100f)

        val newMaxCombo = maxOf(_uiState.value.maxCombo, newCombo)
        val multMultiplier = if (_uiState.value.isMultiplierActive) 2 else 1
        val comboMult = min(newCombo, 5) * multMultiplier

        if (newCombo >= 2) {
            if (_uiState.value.showComboHintBanner) {
                _uiState.update { it.copy(showComboHintBanner = false) }
            }
            sound.playCombo(newCombo)
            addFloatingText(b.x, b.y - 20f, "COMBO ${newCombo}x!", Color(0xFFFFB703))
            when (newCombo) {
                3 -> triggerCandyPraise("NICE! 🌟")
                5 -> triggerCandyPraise("SWEET! 🍬")
                8 -> triggerCandyPraise("TASTY! 🍭")
                12 -> triggerCandyPraise("DIVINE! ⚡👑")
            }
        }

        when (b.type) {
            BalloonType.BOMB -> {
                sound.playBomb()
                // Pass 8 juice: a bomb detonation earns a beat of hit-stop.
                hitStopRemaining = 0.06f
                addFloatingText(b.x, b.y, "BOMB! 💥", Color(0xFFFF5C8A))
                internalSkillEnergy = (internalSkillEnergy - 20f).coerceAtLeast(0f)
                if (_uiState.value.gameMode != GameMode.ZEN) {
                    val remainingLives = _uiState.value.lives - 1
                    _uiState.update { it.copy(lives = remainingLives) }
                    if (remainingLives <= 0) {
                        gameOver(GameOverReason.BOMB_DETONATION)
                        return
                    }
                }
            }
            BalloonType.FREEZE -> {
                sound.playFreeze()
                freezeTimeRemaining = 4.0f
                _uiState.update { it.copy(isFreezeActive = true) }
                addFloatingText(b.x, b.y, "FREEZE! ❄️", Color(0xFF38BDF8))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.MULTIPLIER -> {
                sound.playMultiplierPop()
                multiplierTimeRemaining = 5.0f
                _uiState.update { it.copy(isMultiplierActive = true) }
                addFloatingText(b.x, b.y, "2X BONUS! ⭐", Color(0xFFFFD700))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.HEART -> {
                sound.playHeartPop()
                val currentLives = _uiState.value.lives
                if (currentLives < 3) {
                    _uiState.update { it.copy(lives = currentLives + 1) }
                    addFloatingText(b.x, b.y, "+1 LIFE ❤️", Color(0xFFFF5C8A))
                } else {
                    addFloatingText(b.x, b.y, "+10 ❤️", Color(0xFFFF5C8A))
                }
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.GOLDEN -> {
                sound.playGoldenPop()
                val addedCoins = 5
                val newCoins = _uiState.value.coins + addedCoins
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "+50 ⭐ +5🪙", Color(0xFFFFD700))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.RAINBOW -> {
                sound.playRainbowPop()
                val newCoins = _uiState.value.coins + 2
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "+30 🌈 +2🪙", Color(0xFFB388FF))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.FAST_ROCKET -> {
                sound.playRocketSwoosh()
                val newCoins = _uiState.value.coins + 3
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "SPEED BONUS! 🚀 +45", Color(0xFFFFB703))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.SPLITTER -> {
                sound.playPop(b.radius)
                addFloatingText(b.x, b.y, "SPLIT! 🫧", Color(0xFF38BDF8))
                // Spawn 2 mini-balloons
                synchronized(balloons) {
                    if (balloons.size < 26) {
                        balloons.add(
                            Balloon(
                                id = ++balloonIdCounter,
                                x = (b.x - 30f).coerceAtLeast(40f),
                                y = b.y,
                                radius = b.radius * 0.65f,
                                vy = b.vy * 1.2f,
                                type = BalloonType.STANDARD,
                                color = b.color,
                                wobblePhase = 1f
                            )
                        )
                        balloons.add(
                            Balloon(
                                id = ++balloonIdCounter,
                                x = (b.x + 30f).coerceAtMost(canvasWidth - 40f),
                                y = b.y,
                                radius = b.radius * 0.65f,
                                vy = b.vy * 1.2f,
                                type = BalloonType.STANDARD,
                                color = b.color,
                                wobblePhase = 4f
                            )
                        )
                    }
                }
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.SHIELDED -> {
                sound.playShieldCrack()
                val newCoins = _uiState.value.coins + 3
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "ARMOR BROKEN! 🛡️ +35", Color(0xFFE2E8F0))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.MYSTERY_GIFT -> {
                sound.playMystery()
                val prizeRoll = Random.nextInt(3)
                when (prizeRoll) {
                    0 -> {
                        val newGems = _uiState.value.gems + 3
                        repository.setGems(newGems)
                        addFloatingText(b.x, b.y, "GIFT: +3 💎!", Color(0xFFEC4899))
                        _uiState.update { it.copy(gems = newGems) }
                    }
                    1 -> {
                        val newCoins = _uiState.value.coins + 25
                        repository.setCoins(newCoins)
                        addFloatingText(b.x, b.y, "GIFT: +25 🪙!", Color(0xFFFFD700))
                        _uiState.update { it.copy(coins = newCoins) }
                    }
                    else -> {
                        freezeTimeRemaining = 3.5f
                        _uiState.update { it.copy(isFreezeActive = true) }
                        addFloatingText(b.x, b.y, "GIFT: BLIZZARD! ❄️", Color(0xFF38BDF8))
                    }
                }
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.GHOST_PHANTOM -> {
                sound.playGhostChime()
                addFloatingText(b.x, b.y, "GHOST POP! 👻 +40", Color(0xFFE2E8F0))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.GOLDEN_PIGGY -> {
                sound.playPiggyJackpot()
                val newCoins = _uiState.value.coins + 15
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "JACKPOT! 🐷 +15🪙", Color(0xFFF59E0B))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.ELECTRIC_STORM -> {
                sound.playZap()
                createLightningBolt(startX = b.x, startY = 0f, endX = b.x, endY = b.y)
                addFloatingText(b.x, b.y, "THUNDER ZAP! ⚡", Color(0xFF38BDF8))
                // Chain pop nearby balloons with lightning arcs
                synchronized(balloons) {
                    for (other in balloons) {
                        if (!other.popped && other != b && hypot(other.x - b.x, other.y - b.y) <= 280f) {
                            if (other.type != BalloonType.BOMB && other.type != BalloonType.MEGA_BOSS) {
                                other.popped = true
                                createLightningBolt(startX = b.x, startY = b.y, endX = other.x, endY = other.y)
                                createParticleBurst(other.x, other.y, other.color, 8)
                                applyScoreAndPop(other, comboMult)
                            }
                        }
                    }
                }
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.MEGA_BOSS -> {
                sound.playMegaBossDefeat()
                val newCoins = _uiState.value.coins + 30
                val newGems = _uiState.value.gems + 5
                repository.setCoins(newCoins)
                repository.setGems(newGems)
                addFloatingText(b.x, b.y, "BOSS DEFEATED! 👑 +500", Color(0xFFFFD700))
                _uiState.update { it.copy(coins = newCoins, gems = newGems) }
                applyScoreAndPop(b, comboMult)
            }
            else -> {
                sound.playBalloonPop(b.type, b.radius)
                val pts = b.type.points * comboMult
                // Pass 6: score pops are white — balloon colours include dark
                // navy/purple skin palettes that rendered near-black.
                addFloatingText(b.x, b.y, "+$pts", Color.White)
                applyScoreAndPop(b, comboMult, newCombo, newMaxCombo)
            }
        }

        // Particle burst according to performance settings
        val particleCount = when (_uiState.value.graphicsQuality) {
            "ULTRA" -> 16
            "BATTERY_SAVER" -> 6
            else -> 10
        }
        createExplosionBurst(b.x, b.y, b.color, b.type, b.radius, count = particleCount)
    }

    private fun applyScoreAndPop(
        b: Balloon,
        comboMultiplier: Int,
        newCombo: Int = _uiState.value.combo,
        newMaxCombo: Int = _uiState.value.maxCombo
    ) {
        val earnedScore = b.type.points * comboMultiplier
        val wasHighScoreBroken = !_uiState.value.isNewHighScoreAchieved && sessionBaselineHighScore > 0 && (_uiState.value.score + earnedScore) > sessionBaselineHighScore
        if (wasHighScoreBroken) {
            sound.playHighScoreFanfare()
            triggerVibration(60L)
            triggerCandyPraise("NEW HIGH SCORE! 🏆")
            createSparks(b.x, b.y, Color(0xFFFFD700), count = 16)
            addFloatingText(b.x, b.y - 35f, "🏆 NEW RECORD!", Color(0xFFFFD700))
        }

        repository.addLifetimePops(1)

        var shouldClearStage = false
        _uiState.update { current ->
            val updatedScore = maxOf(0, current.score + earnedScore)
            val updatedPopped = maxOf(current.poppedCount + 1, current.poppedCount)
            repository.updateLifetimeHighScore(updatedScore)
            // Pass 5 (audit L2): currentLevel > 0 keeps custom challenges
            // (CAMPAIGN mode, level 0) out of the campaign clear path.
            if (current.gameMode == GameMode.CAMPAIGN && current.currentLevel > 0 && updatedPopped >= current.targetPops) {
                shouldClearStage = true
            }
            current.copy(
                score = updatedScore,
                poppedCount = updatedPopped,
                combo = newCombo,
                maxCombo = newMaxCombo,
                skillEnergy = internalSkillEnergy,
                lifetimePops = repository.getLifetimePops(),
                lifetimeHighScore = repository.getLifetimeHighScore(),
                isNewHighScoreAchieved = current.isNewHighScoreAchieved || wasHighScoreBroken
            )
        }

        // Campaign Stage Win Check
        if (shouldClearStage) {
            stageClear()
        }
    }

    private fun onMissBalloon(escapeX: Float = 150f) {
        if (_uiState.value.gameState != GameState.PLAYING || com.example.balloonpop.ads.AdMobManager.isAdShowing) return
        val currentMode = _uiState.value.gameMode
        val newMissed = _uiState.value.missedCount + 1
        val maxAllowed = _uiState.value.maxMissesAllowed
        val clampedX = escapeX.coerceIn(60f, (canvasWidth - 60f).coerceAtLeast(100f))

        if (currentMode == GameMode.CAMPAIGN) {
            sound.playMiss()
            triggerVibration(35L)
            // Pass 8 FTUE kindness: stage 1 can NOT be failed by misses —
            // the counter still ticks and warns, but reaching the limit
            // never ends the stage (it can still end on the timer).
            val isFirstStage = _uiState.value.currentLevel == 1
            currentScreenShake = if (newMissed >= maxAllowed && !isFirstStage) 26f else 16f

            if (newMissed >= maxAllowed && !isFirstStage) {
                // Pass 6: the miss that REACHES the displayed limit ends the
                // stage (was `>`, so the HUD could sit at 6/6 for ~30s with
                // the stage still alive — user's recording).
                sound.playMissWarning()
                addFloatingText(clampedX, 70f, "❌ MISS LIMIT REACHED! ($newMissed/$maxAllowed)", Color(0xFFEF4444))
                _uiState.update {
                    it.copy(
                        missedCount = newMissed,
                        combo = 0,
                        screenFlashAlpha = 0.35f
                    )
                }
                gameOver(GameOverReason.MISSED_LIMIT)
            } else {
                // Pass 6: orange warning now fires on the LAST safe miss
                // (one before the limit), since the limit miss ends the stage.
                val warningColor = if (newMissed == maxAllowed - 1) Color(0xFFF97316) else Color(0xFFFFD166)
                val text = "💨 MISSED ($newMissed/$maxAllowed)"
                addFloatingText(clampedX, 70f, text, warningColor)
                _uiState.update {
                    it.copy(
                        missedCount = newMissed,
                        combo = 0,
                        screenFlashAlpha = 0.2f
                    )
                }
            }
        } else if (currentMode == GameMode.CLASSIC) {
            val currentLives = _uiState.value.lives - 1
            sound.playMiss()
            triggerVibration(40L)
            currentScreenShake = 18f
            addFloatingText(clampedX, 70f, "💔 -1 LIFE", Color(0xFFEF4444))
            _uiState.update {
                it.copy(
                    lives = currentLives,
                    missedCount = newMissed,
                    combo = 0,
                    screenFlashAlpha = 0.25f
                )
            }
            if (currentLives <= 0) {
                gameOver(GameOverReason.LIVES_DEPLETED)
            }
        } else {
            currentScreenShake = 12f
            _uiState.update {
                it.copy(
                    missedCount = newMissed,
                    combo = 0
                )
            }
        }
    }

    private fun createShockwave(x: Float, y: Float, color: Color, maxRadius: Float = 140f) {
        synchronized(shockwaves) {
            if (shockwaves.size < 12) {
                shockwaves.add(
                    PopShockwave(
                        x = x,
                        y = y,
                        radius = 12f,
                        maxRadius = maxRadius,
                        color = color
                    )
                )
            }
        }
    }

    private fun createLightningBolt(startX: Float, startY: Float, endX: Float, endY: Float) {
        val points = mutableListOf<Pair<Float, Float>>()
        points.add(Pair(startX, startY))
        val segments = 8
        val dx = (endX - startX) / segments
        val dy = (endY - startY) / segments

        for (i in 1 until segments) {
            val jitterX = (Random.nextFloat() * 48f - 24f)
            val jitterY = (Random.nextFloat() * 16f - 8f)
            val curX = startX + dx * i + jitterX
            val curY = startY + dy * i + jitterY
            points.add(Pair(curX, curY))
        }
        points.add(Pair(endX, endY))

        // Create 1-2 branch forks
        val branches = mutableListOf<List<Pair<Float, Float>>>()
        if (points.size >= 4) {
            val forkStart = points[points.size / 2]
            val branch = mutableListOf<Pair<Float, Float>>()
            branch.add(forkStart)
            var bx = forkStart.first
            var by = forkStart.second
            for (j in 0..2) {
                bx += (Random.nextFloat() * 50f - 25f)
                by += 20f + Random.nextFloat() * 20f
                branch.add(Pair(bx, by))
            }
            branches.add(branch)
        }

        synchronized(lightningBolts) {
            if (lightningBolts.size < 16) {
                lightningBolts.add(
                    LightningBolt(
                        id = ++lightningIdCounter,
                        points = points,
                        alpha = 1.0f,
                        color = Color(0xFF2F6DB3),
                        branchPoints = branches
                    )
                )
            }
        }
    }

    fun triggerCandyPraise(text: String) {
        val now = System.currentTimeMillis()
        if (now - lastPraiseTime < 3500L) return
        lastPraiseTime = now
        sound.playPraise()
        _uiState.update { it.copy(candyPraise = text) }
        viewModelScope.launch {
            delay(900L)
            _uiState.update {
                if (it.candyPraise == text) it.copy(candyPraise = null) else it
            }
        }
    }

    fun triggerInGameBooster(booster: InGameBooster) {
        if (_uiState.value.gameState != GameState.PLAYING) return

        // 1. Cooldown check (3.5s cooldown)
        val activeCd = internalBoosterCooldowns[booster.id] ?: 0f
        if (activeCd > 0f) {
            sound.playClick()
            return
        }

        // 1b. Pass 5 (audit L14): boosters that act on on-screen balloons
        //     must not consume a charge when there is nothing to act on
        //     (e.g. Thunder with an empty screen burned the charge, played
        //     the bolt and silently did nothing).
        val needsTargets = booster == InGameBooster.THUNDER_STRIKE ||
            booster == InGameBooster.HAMMER_SMASH ||
            booster == InGameBooster.TORNADO_VORTEX
        if (needsTargets) {
            val hasTargets = synchronized(balloons) { balloons.any { !it.popped } }
            if (!hasTargets) {
                sound.playPurchaseFail()
                _uiState.update { it.copy(bannerMessage = "No balloons on screen for ${booster.name.replace('_', ' ')}!") }
                return
            }
        }

        // 2. Persistent inventory: consume exactly ONE persisted charge.
        //    Counts live in the repository (SharedPreferences), so a charge
        //    used here is gone everywhere — and remaining charges stay fully
        //    usable in every later level; nothing is per-level any more.
        //    (The gameplay UI intercepts 0-count taps first and offers the
        //    one-tap buy dialog; this coin fallback only covers other callers.)
        val counts = _uiState.value.boosterCounts.toMutableMap()
        val currentCharges = counts[booster.id] ?: 3

        if (currentCharges > 0) {
            // Pass 5 (audit L14): if the repository refuses the consume
            // (count race), the booster must NOT fire — previously its
            // effects ran anyway, granting a free use.
            if (repository.consumeBooster(booster.id)) {
                counts[booster.id] = repository.getBoosterCount(booster.id)
            } else {
                sound.playPurchaseFail()
                return
            }
        } else {
            // Charges depleted: requires BoosterPrices.DEPLETED_USE_COIN_COST coins to activate!
            val useCost = BoosterPrices.DEPLETED_USE_COIN_COST
            if (_uiState.value.coins >= useCost) {
                val newCoins = _uiState.value.coins - useCost
                repository.setCoins(newCoins)
                _uiState.update { it.copy(coins = newCoins) }
                addFloatingText(canvasWidth / 2f, 220f, "-$useCost 🪙", Color(0xFFF59E0B))
                sound.playCoinEarn()
            } else {
                sound.playPurchaseFail()
                _uiState.update { it.copy(bannerMessage = "Need $useCost 🪙 to use ${booster.name.replace('_', ' ')}!") }
                return
            }
        }

        internalBoosterCooldowns[booster.id] = 3.5f
        stageBoosterUsesCount++
        _uiState.update {
            it.copy(
                boosterCounts = counts,
                boosterCooldowns = internalBoosterCooldowns.toMap(),
                stageBoosterUsesCount = stageBoosterUsesCount
            )
        }
        triggerVibration(45L)

        when (booster) {
            InGameBooster.THUNDER_STRIKE -> {
                sound.playThunder()
                currentScreenShake = 22f
                _uiState.update {
                    it.copy(
                        screenShake = 22f,
                        screenFlashAlpha = 0.55f,
                        bannerMessage = "⚡ THUNDER STRIKE UNLEASHED!"
                    )
                }

                val bombTargets = mutableListOf<Balloon>()
                val otherTargets = mutableListOf<Balloon>()
                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped) {
                            if (b.type == BalloonType.BOMB) bombTargets.add(b)
                            else otherTargets.add(b)
                        }
                    }
                }

                for (b in bombTargets) {
                    createLightningBolt(startX = b.x + (Random.nextFloat() * 80f - 40f), startY = 0f, endX = b.x, endY = b.y)
                    b.popped = true
                    createParticleBurst(b.x, b.y, Color(0xFF2F6DB3), count = 16)
                    createShockwave(b.x, b.y, Color(0xFF38BDF8), maxRadius = 180f)
                    addFloatingText(b.x, b.y, "⚡ DEFUSED! +50🪙", Color(0xFFFFD700))
                    val newCoins = _uiState.value.coins + 50
                    repository.setCoins(newCoins)
                    _uiState.update { it.copy(coins = newCoins) }
                }

                val chosenOthers = otherTargets.shuffled().take(8)
                for (b in chosenOthers) {
                    createLightningBolt(startX = Random.nextFloat() * canvasWidth, startY = 0f, endX = b.x, endY = b.y)
                    b.popped = true
                    applyScoreAndPop(b, comboMultiplier = 3)
                    createParticleBurst(b.x, b.y, Color(0xFF38BDF8), count = 14)
                    createShockwave(b.x, b.y, Color(0xFF4A90C4), maxRadius = 120f)
                    addFloatingText(b.x, b.y, "+120 ⚡ THUNDER!", Color(0xFF38BDF8))
                }

                if (bombTargets.isEmpty() && chosenOthers.isEmpty()) {
                    createLightningBolt(startX = canvasWidth / 2f, startY = 0f, endX = canvasWidth / 2f, endY = canvasHeight / 2f)
                }

                triggerCandyPraise("THUNDERBOLT! ⚡")
            }

            InGameBooster.COLOR_BOMB -> {
                sound.playRainbowBlast()
                currentScreenShake = 16f
                _uiState.update {
                    it.copy(
                        screenShake = 16f,
                        bannerMessage = "🌈 COLOR BOMB DISCO CASCADE!"
                    )
                }

                val activeBalloons = synchronized(balloons) { balloons.filter { !it.popped && it.type != BalloonType.BOMB } }
                if (activeBalloons.isNotEmpty()) {
                    val grouped = activeBalloons.groupBy { it.color }
                    val mostCommonColor = grouped.maxByOrNull { it.value.size }?.key ?: activeBalloons.first().color
                    val targets = activeBalloons.filter { it.color == mostCommonColor }

                    for (b in targets) {
                        b.popped = true
                        applyScoreAndPop(b, comboMultiplier = 4)
                        createParticleBurst(b.x, b.y, b.color, count = 18)
                        createShockwave(b.x, b.y, Color(0xFFFFD700), maxRadius = 130f)
                        addFloatingText(b.x, b.y, "+200 🌈 COLOR CRUSH!", Color(0xFFFFB703))
                    }
                    triggerCandyPraise("COLOR CRUSH! 🌈")
                } else {
                    addFloatingText(canvasWidth / 2f, canvasHeight / 2f, "🌈 RAINBOW BURST!", Color(0xFFFF5C8A))
                }
            }

            InGameBooster.HAMMER_SMASH -> {
                sound.playHammerSmash(5)
                currentScreenShake = 24f
                val cx = canvasWidth / 2f
                val cy = canvasHeight / 2f
                createShockwave(cx, cy, Color(0xFFF59E0B), maxRadius = 320f)
                createShockwave(cx, cy, Color.White, maxRadius = 240f)

                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped && b.type != BalloonType.BOMB) {
                            val dist = hypot(b.x - cx, b.y - cy)
                            if (dist < 320f) {
                                b.popped = true
                                applyScoreAndPop(b, comboMultiplier = 3)
                                createParticleBurst(b.x, b.y, Color(0xFFF59E0B), count = 12)
                            }
                        }
                    }
                }
                addFloatingText(cx, cy - 80f, "🔨 SMASH! +300", Color(0xFFFACC15))
                triggerCandyPraise("SMASHING! 🔨")
            }

            InGameBooster.FREEZE_BLIZZARD -> {
                sound.playFreeze()
                freezeTimeRemaining = 6.0f
                _uiState.update {
                    it.copy(
                        isFreezeActive = true,
                        bannerMessage = "❄️ BLIZZARD CHRONO STASIS!"
                    )
                }
                val cx = canvasWidth / 2f
                val cy = canvasHeight / 2f
                createShockwave(cx, cy, Color(0xFF4A90C4), maxRadius = 280f)
                addFloatingText(cx, cy, "❄️ TIME FROZEN!", Color(0xFF38BDF8))
                triggerCandyPraise("ICE COLD! ❄️")
            }

            InGameBooster.TORNADO_VORTEX -> {
                sound.playRocketSwoosh()
                currentScreenShake = 18f
                val cx = canvasWidth / 2f
                val cy = canvasHeight / 2f
                _uiState.update {
                    it.copy(
                        screenShake = 18f,
                        bannerMessage = "🌀 TORNADO VORTEX ACTIVE!"
                    )
                }
                createShockwave(cx, cy, Color(0xFF2FA36B), maxRadius = 300f)

                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped && b.type != BalloonType.BOMB) {
                            b.popped = true
                            applyScoreAndPop(b, comboMultiplier = 5)
                            createParticleBurst(b.x, b.y, Color(0xFF2FA36B), count = 10)
                        }
                    }
                }
                addFloatingText(cx, cy, "🌀 VORTEX! +400", Color(0xFF3ED6A5))
                triggerCandyPraise("VORTEX! 🌀")
            }
        }
    }

    fun buyBooster(boosterId: String, withGems: Boolean = false) {
        // Pass 4: prices come from the single BoosterPrices constants place and
        // the purchase itself goes through the repository's one purchase path,
        // so balance + inventory can never drift apart between shop & gameplay.
        val coinPrice = BoosterPrices.COIN_PRICE
        val gemPrice = BoosterPrices.GEM_PRICE

        if (withGems) {
            if (repository.purchaseBoosterWithGems(boosterId, gemPrice)) {
                _uiState.update {
                    it.copy(
                        gems = repository.getGems(),
                        boosterCounts = repository.getAllBoosterCounts()
                    )
                }
                sound.playRewardClaim()
            } else {
                _uiState.update { it.copy(bannerMessage = "Need $gemPrice 💎 to buy this booster!") }
            }
        } else {
            if (repository.purchaseBoosterWithCoins(boosterId, coinPrice)) {
                _uiState.update {
                    it.copy(
                        coins = repository.getCoins(),
                        boosterCounts = repository.getAllBoosterCounts()
                    )
                }
                sound.playRewardClaim()
            } else {
                _uiState.update { it.copy(bannerMessage = "Need $coinPrice 🪙 to buy this booster!") }
            }
        }
    }

    private fun stageClear() {
        // Pass 5 (audit L2): hard guard — custom challenges run in CAMPAIGN
        // mode with currentLevel 0 and must never touch campaign progress
        // or rewards. (The fallback level lookup previously paid stage-1
        // rewards for a custom run and even wrote stage-1 stars.)
        if (_uiState.value.currentLevel <= 0) return

        timerJob?.cancel()

        val lvl = GameConstants.CAMPAIGN_LEVELS.getOrNull(_uiState.value.currentLevel - 1)
            ?: GameConstants.CAMPAIGN_LEVELS.first()

        // Pass 8: a campaign win clears this stage's consecutive-fail count
        // (the kindness assist re-arms from zero) and extends the persisted
        // win streak that fuels the next stage's +5s.
        repository.setStageFailCount(lvl.level, 0)
        val newWinStreak = repository.getWinStreak() + 1
        repository.setWinStreak(newWinStreak)

        val currentScore = _uiState.value.score
        // Pass 5 (audit L10): the old score floor
        // (maxOf(score, poppedCount * 35 + level * 25)) is removed — score
        // and stars now reflect actual play, not a guaranteed minimum.
        val finalScore = currentScore
        repository.updateLifetimeHighScore(finalScore)

        val isHighScore = _uiState.value.isNewHighScoreAchieved || (finalScore > sessionBaselineHighScore && sessionBaselineHighScore > 0)
        if (isHighScore) {
            sound.playHighScoreFanfare()
        } else {
            sound.playStageClear()
        }

        val stars = when {
            finalScore >= lvl.targetScore * 1.3 -> 3
            finalScore >= lvl.targetScore -> 2
            else -> 1
        }

        repository.setLevelStars(lvl.level, stars)

        // Pass 5 (audit L10): replay taper — the FIRST clear pays the full
        // stage reward; replays of an already-cleared stage pay 25%, so
        // re-running stage 1 is no longer the game's best economy.
        val isReplay = _uiState.value.completedLevels.containsKey(lvl.level)
        val paidCoins = if (isReplay) (lvl.rewardCoins * 0.25f).toInt() else lvl.rewardCoins
        val paidGems = if (isReplay) (lvl.rewardGems * 0.25f).toInt() else lvl.rewardGems

        val newCoins = _uiState.value.coins + paidCoins
        val newGems = _uiState.value.gems + paidGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        val wasFirstLevelCompleted = _uiState.value.completedLevels.isEmpty()
        val updatedCompletedLevels = repository.getCompletedLevels()

        _uiState.update {
            it.copy(
                gameState = GameState.LEVELCLEAR,
                score = finalScore,
                coins = newCoins,
                gems = newGems,
                completedLevels = updatedCompletedLevels,
                earnedCoinsThisSession = paidCoins,
                earnedGemsThisSession = paidGems,
                earnedStarsThisLevel = stars,
                lifetimeHighScore = repository.getLifetimeHighScore(),
                isNewHighScoreAchieved = isHighScore,
                previousHighScore = sessionBaselineHighScore,
                shouldPromptFirstClearNotification = wasFirstLevelCompleted,
                winStreak = newWinStreak
            )
        }
    }

    fun onNotificationPromptDismissed() {
        _uiState.update { it.copy(shouldPromptFirstClearNotification = false) }
    }

    private fun gameOver(reason: GameOverReason = GameOverReason.TIME_EXPIRED) {
        timerJob?.cancel()
        triggerVibration(80L)

        val currentScore = _uiState.value.score
        repository.updateLifetimeHighScore(currentScore)
        // Pass 5 (audit L4): only genuine endless modes record an endless
        // best — the old code also wrote a junk "CAMPAIGN" entry on every
        // campaign fail. And the fail consolation coins (max(5, score/20))
        // are gone: failing a stage pays nothing; coins come from clears.
        if (_uiState.value.gameMode != GameMode.CAMPAIGN) {
            repository.updateEndlessHighScore(_uiState.value.gameMode.name, currentScore)
        }
        repository.updateLifetimeMaxCombo(_uiState.value.maxCombo)

        // Pass 8: campaign fail bookkeeping. ANY campaign fail breaks the
        // win streak. Consecutive fails on this stage feed the kindness
        // assist: at 3, a pending assist is set for the stage (consumed at
        // its next start — see applyCampaignStartBonuses) and the fail
        // dialog shows the unlock line. A revived attempt is the SAME
        // attempt, so a post-revive game over doesn't double-count.
        val failState = _uiState.value
        val isCampaignStageFail = failState.gameMode == GameMode.CAMPAIGN && failState.currentLevel > 0
        if (isCampaignStageFail) {
            repository.setWinStreak(0)
            if (stageReviveUsesCount == 0) {
                val fails = repository.getStageFailCount(failState.currentLevel) + 1
                repository.setStageFailCount(failState.currentLevel, fails)
                if (fails >= 3) {
                    repository.setAssistPending(failState.currentLevel, true)
                }
            }
        }
        val assistNowPending = isCampaignStageFail && repository.isAssistPending(failState.currentLevel)

        _uiState.update {
            it.copy(
                gameState = GameState.GAMEOVER,
                gameOverReason = reason,
                earnedCoinsThisSession = 0,
                lifetimeHighScore = repository.getLifetimeHighScore(),
                lifetimeMaxCombo = repository.getLifetimeMaxCombo(),
                winStreak = if (isCampaignStageFail) 0 else it.winStreak,
                assistUnlocked = assistNowPending
            )
        }
    }

    /**
     * Pass 5 (audit L3): finalize an endless run WITHOUT a game over — used
     * by the pause-menu quit, by navigation away from gameplay, and by the
     * in-game "Finish & Save" button (the only way a Zen run, which has no
     * timer and no lives, can ever end and record its BEST). The run's best
     * is persisted; campaign/custom runs are simply abandoned.
     */
    fun finishEndlessRun(saveBest: Boolean = true) {
        val state = _uiState.value
        if (state.gameMode == GameMode.CAMPAIGN) return
        timerJob?.cancel()
        if (saveBest && state.score > 0) {
            repository.updateEndlessHighScore(state.gameMode.name, state.score)
        }
        repository.updateLifetimeMaxCombo(state.maxCombo)
        _uiState.update {
            it.copy(
                gameState = GameState.GAMEOVER,
                gameOverReason = GameOverReason.MANUAL_QUIT,
                lifetimeHighScore = repository.getLifetimeHighScore(),
                lifetimeMaxCombo = repository.getLifetimeMaxCombo()
            )
        }
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        val now = System.currentTimeMillis()
        if (text.startsWith("COMBO")) {
            if (now - lastComboFloatingTextTime < 300L) return
            lastComboFloatingTextTime = now
        }
        val clampedX = x.coerceIn(80f, (canvasWidth - 80f).coerceAtLeast(160f))
        synchronized(floatingTexts) {
            // Prevent double-rendered floating text by deduplicating identical nearby text
            val isDuplicate = floatingTexts.any { it.text == text && kotlin.math.abs(it.x - clampedX) < 30f && kotlin.math.abs(it.y - y) < 30f }
            if (isDuplicate) return
            if (floatingTexts.size < 12) {
                val laneOffset = (floatingTexts.size % 3) * 22f
                floatingTexts.add(FloatingText(clampedX, y - laneOffset, text, color))
            }
        }
    }

    private fun createExplosionBurst(
        x: Float,
        y: Float,
        color: Color,
        type: BalloonType = BalloonType.STANDARD,
        radius: Float = 48f,
        count: Int = 14
    ) {
        val maxCap = when (_uiState.value.graphicsQuality) {
            "ULTRA" -> 160
            "BATTERY_SAVER" -> 35
            else -> 90
        }
        synchronized(particles) {
            if (particles.size >= maxCap) return

            // 1. Torn Rubber Shreds (Balloon membrane shreds flying outwards with flutter and rotation)
            val shredCount = (count * 0.75f).toInt().coerceAtLeast(6)
            for (i in 0 until shredCount) {
                val angle = Random.nextFloat() * 6.283f
                val speed = 2.5f + Random.nextFloat() * 5.5f
                val shredColor = if (i % 3 == 0) color.copy(alpha = 0.9f) else color
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed - 1.2f,
                        radius = 4f + Random.nextFloat() * 4.5f,
                        color = shredColor,
                        alpha = 1.0f,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 24f,
                        particleType = ParticleType.RUBBER_SHRED,
                        length = radius * (0.28f + Random.nextFloat() * 0.35f)
                    )
                )
            }

            // 2. High-speed Glow Sparks (Bright hot core radiating outward)
            val sparkCount = (count * 0.65f).toInt().coerceAtLeast(5)
            for (i in 0 until sparkCount) {
                val angle = Random.nextFloat() * 6.283f
                val speed = 4.5f + Random.nextFloat() * 6.5f
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed - 2.0f,
                        radius = 2.5f + Random.nextFloat() * 3.5f,
                        color = Color.White,
                        alpha = 1.0f,
                        rotation = 0f,
                        vRot = 0f,
                        particleType = ParticleType.GLOW_SPARK
                    )
                )
            }

            // 3. Radiant Star Glints (Sparkle twinkle stars)
            val starCount = 3 + Random.nextInt(3)
            for (i in 0 until starCount) {
                val angle = Random.nextFloat() * 6.283f
                val speed = 1.5f + Random.nextFloat() * 3.5f
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed - 1.0f,
                        radius = 5f + Random.nextFloat() * 4f,
                        color = Color(0xFFFFE082),
                        alpha = 1.0f,
                        isStar = true,
                        rotation = Random.nextFloat() * 360f,
                        vRot = (Random.nextFloat() - 0.5f) * 16f,
                        particleType = ParticleType.SPARKLE_STAR
                    )
                )
            }

            // 4. Special Type Particle Enhancements
            when (type) {
                BalloonType.BOMB -> {
                    for (i in 0 until 10) {
                        val angle = Random.nextFloat() * 6.283f
                        val speed = 3.5f + Random.nextFloat() * 7.0f
                        val fireColor = if (i % 2 == 0) Color(0xFFFF3D00) else Color(0xFFFFD600)
                        particles.add(
                            Particle(
                                x = x,
                                y = y,
                                vx = kotlin.math.cos(angle) * speed,
                                vy = kotlin.math.sin(angle) * speed - 3f,
                                radius = 4f + Random.nextFloat() * 5f,
                                color = fireColor,
                                alpha = 1.0f,
                                particleType = ParticleType.GLOW_SPARK
                            )
                        )
                    }
                }
                BalloonType.FREEZE -> {
                    for (i in 0 until 8) {
                        val angle = Random.nextFloat() * 6.283f
                        val speed = 2.0f + Random.nextFloat() * 4.5f
                        particles.add(
                            Particle(
                                x = x,
                                y = y,
                                vx = kotlin.math.cos(angle) * speed,
                                vy = kotlin.math.sin(angle) * speed,
                                radius = 5f + Random.nextFloat() * 5f,
                                color = Color(0xFFE0F7FA),
                                alpha = 1.0f,
                                rotation = Random.nextFloat() * 360f,
                                vRot = (Random.nextFloat() - 0.5f) * 10f,
                                particleType = ParticleType.ICE_CRYSTAL
                            )
                        )
                    }
                }
                BalloonType.ELECTRIC_STORM -> {
                    for (i in 0 until 8) {
                        val angle = Random.nextFloat() * 6.283f
                        val speed = 5.0f + Random.nextFloat() * 6.0f
                        particles.add(
                            Particle(
                                x = x,
                                y = y,
                                vx = kotlin.math.cos(angle) * speed,
                                vy = kotlin.math.sin(angle) * speed,
                                radius = 3f + Random.nextFloat() * 3f,
                                color = Color(0xFF4A90C4),
                                alpha = 1.0f,
                                particleType = ParticleType.LIGHTNING_EMBER
                            )
                        )
                    }
                }
                BalloonType.RAINBOW -> {
                    val rainbowColors = listOf(Color(0xFFFF1744), Color(0xFFFF9100), Color(0xFFFFEA00), Color(0xFF00E676), Color(0xFF2979FF), Color(0xFFD500F9))
                    for (i in 0 until 10) {
                        val angle = Random.nextFloat() * 6.283f
                        val speed = 2.0f + Random.nextFloat() * 5.0f
                        particles.add(
                            Particle(
                                x = x,
                                y = y,
                                vx = kotlin.math.cos(angle) * speed,
                                vy = kotlin.math.sin(angle) * speed - 1.5f,
                                radius = 4f + Random.nextFloat() * 4f,
                                color = rainbowColors[i % rainbowColors.size],
                                alpha = 1.0f,
                                rotation = Random.nextFloat() * 360f,
                                vRot = (Random.nextFloat() - 0.5f) * 20f,
                                particleType = ParticleType.CONFETTI_RIBBON,
                                length = 14f + Random.nextFloat() * 12f
                            )
                        )
                    }
                }
                BalloonType.GOLDEN, BalloonType.GOLDEN_PIGGY -> {
                    for (i in 0 until 8) {
                        val angle = Random.nextFloat() * 6.283f
                        val speed = 2.5f + Random.nextFloat() * 5.5f
                        particles.add(
                            Particle(
                                x = x,
                                y = y,
                                vx = kotlin.math.cos(angle) * speed,
                                vy = kotlin.math.sin(angle) * speed - 2.0f,
                                radius = 5f + Random.nextFloat() * 5f,
                                color = Color(0xFFFFD700),
                                alpha = 1.0f,
                                isStar = true,
                                rotation = Random.nextFloat() * 360f,
                                vRot = (Random.nextFloat() - 0.5f) * 14f,
                                particleType = ParticleType.SPARKLE_STAR
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

    private fun createParticleBurst(x: Float, y: Float, color: Color, count: Int = 10) {
        createExplosionBurst(x, y, color, BalloonType.STANDARD, 44f, count)
    }

    private fun createSparks(x: Float, y: Float, sparkColor: Color, count: Int = 8) {
        createExplosionBurst(x, y, sparkColor, BalloonType.STANDARD, 36f, count)
    }

    // --- Skills System ---
    fun activateSkill() {
        if (_uiState.value.gameState != GameState.PLAYING) return
        val skillId = _uiState.value.equippedSkillId
        val skill = GameConstants.AVAILABLE_SKILLS.find { it.id == skillId } ?: return

        // Skills are rare: require full 99%+ energy gauge and no cooldown!
        if (internalSkillEnergy < 99f || _uiState.value.skillCooldownRemaining > 0f) {
            sound.playClick()
            return
        }

        // In one level: first skill use is default. Subsequent uses double the cooldown and rarity!
        val cooldownMultiplier = 1 shl stageSkillUsesCount.coerceAtMost(3)
        val effectiveCooldown = skill.cooldownSeconds * cooldownMultiplier

        internalSkillEnergy = 0f
        internalSkillCooldown = effectiveCooldown
        stageSkillUsesCount++

        triggerVibration(80L)

        when (skill.type) {
            SkillType.HAMMER_SLIDE, SkillType.CONTINUOUS_SLICE -> {
                // 5-second Hammer Slide superpower!
                hammerSlideTimeRemaining = 5.0f
                sound.playHammerSmash(5)
                sound.playThunder()
                currentScreenShake = 20f

                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "🔨⚡ 5s HAMMER SLIDE!", Color(0xFFFFD700))
                _uiState.update {
                    it.copy(
                        isHammerSlideActive = true,
                        hammerSlideTimeRemaining = 5.0f,
                        skillEnergy = 0f,
                        skillCooldownRemaining = effectiveCooldown,
                        stageSkillUsesCount = stageSkillUsesCount,
                        screenShake = 20f,
                        screenFlashAlpha = 0.6f,
                        candyPraise = "SLIDE HAMMER! 🔨⚡",
                        bannerMessage = "⚡ HAMMER SLIDE ACTIVATED (5s)! SLIDE TO SMASH!"
                    )
                }
            }
            SkillType.TIME_FREEZE -> {
                freezeTimeRemaining = 6.0f
                sound.playFreeze()
                currentScreenShake = 12f
                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "⏱️ CHRONO STASIS!", Color(0xFF38BDF8))
                _uiState.update {
                    it.copy(
                        isFreezeActive = true,
                        skillEnergy = 0f,
                        skillCooldownRemaining = effectiveCooldown,
                        stageSkillUsesCount = stageSkillUsesCount,
                        screenShake = 12f,
                        candyPraise = "FROZEN! ❄️",
                        bannerMessage = "⏱️ TIME SLOWED FOR 6 SECONDS!"
                    )
                }
            }
            SkillType.MAGNET_PULL -> {
                sound.playPop()
                currentScreenShake = 14f
                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "🧲 VORTEX MAGNET!", Color(0xFFFF5C8A))
                _uiState.update {
                    it.copy(
                        isSkillActive = true,
                        skillEnergy = 0f,
                        skillCooldownRemaining = effectiveCooldown,
                        stageSkillUsesCount = stageSkillUsesCount,
                        screenShake = 14f,
                        candyPraise = "MAGNETIC! 🧲",
                        bannerMessage = "🧲 VORTEX MAGNET ACTIVATED!"
                    )
                }
            }
            SkillType.SHOCKWAVE -> {
                sound.playStageClear()
                currentScreenShake = 24f

                // Defuse all bombs safely into coins
                var defusedBombsCount = 0
                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped && b.type == BalloonType.BOMB) {
                            b.popped = true
                            defusedBombsCount++
                            createParticleBurst(b.x, b.y, Color(0xFFFFD700), count = 10)
                        }
                    }
                }
                if (defusedBombsCount > 0) {
                    val newCoins = _uiState.value.coins + defusedBombsCount * 30
                    repository.setCoins(newCoins)
                    _uiState.update { it.copy(coins = newCoins) }
                }

                // Pop visible non-bomb balloons
                var poppedOnScreen = 0
                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped) {
                            b.popped = true
                            poppedOnScreen++
                            applyScoreAndPop(b, 2)
                            createParticleBurst(b.x, b.y, b.color, count = 8)
                        }
                    }
                }

                val scoreBonus = poppedOnScreen * 60
                val newScore = _uiState.value.score + scoreBonus
                val newPopped = _uiState.value.poppedCount + poppedOnScreen

                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "💥 SUPERNOVA BLAST! +$scoreBonus PTS!", Color(0xFFFFB703))
                _uiState.update {
                    it.copy(
                        score = newScore,
                        poppedCount = newPopped,
                        skillEnergy = 0f,
                        skillCooldownRemaining = effectiveCooldown,
                        stageSkillUsesCount = stageSkillUsesCount,
                        screenShake = 24f,
                        screenFlashAlpha = 0.7f,
                        candyPraise = "SUPERNOVA! 💥",
                        bannerMessage = "💥 SUPERNOVA CLEARED SCREEN! (+$scoreBonus PTS)"
                    )
                }
            }
        }
    }

    fun unlockSkill(skill: PlayerSkill) {
        sound.playClick()
        val currentCoins = _uiState.value.coins
        val currentGems = _uiState.value.gems

        if (currentCoins < skill.unlockCostCoins || currentGems < skill.unlockCostGems) {
            _uiState.update { it.copy(bannerMessage = "Not enough coins or diamonds!") }
            return
        }

        val newCoins = currentCoins - skill.unlockCostCoins
        val newGems = currentGems - skill.unlockCostGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)
        repository.unlockSkill(skill.id)
        repository.setEquippedSkill(skill.id)

        sound.playStageClear()
        _uiState.update {
            it.copy(
                coins = newCoins,
                gems = newGems,
                unlockedSkills = repository.getUnlockedSkills(),
                equippedSkillId = skill.id,
                bannerMessage = "Unlocked ${skill.name}! ⚡"
            )
        }
    }

    fun upgradeSkill(skill: PlayerSkill) {
        sound.playClick()
        val currentLvl = repository.getSkillLevel(skill.id)
        if (currentLvl >= skill.maxLevel) return

        val costCoins = (currentLvl + 1) * 300
        val costGems = (currentLvl + 1) * 5

        if (_uiState.value.coins < costCoins || _uiState.value.gems < costGems) {
            _uiState.update { it.copy(bannerMessage = "Need $costCoins🪙 and $costGems💎 to upgrade!") }
            return
        }

        val newCoins = _uiState.value.coins - costCoins
        val newGems = _uiState.value.gems - costGems
        val newLvl = currentLvl + 1

        repository.setCoins(newCoins)
        repository.setGems(newGems)
        repository.setSkillLevel(skill.id, newLvl)

        val updatedLevels = GameConstants.AVAILABLE_SKILLS.associate { it.id to repository.getSkillLevel(it.id) }

        sound.playStageClear()
        _uiState.update {
            it.copy(
                coins = newCoins,
                gems = newGems,
                skillLevels = updatedLevels,
                bannerMessage = "${skill.name} Upgraded to Level $newLvl! 🚀"
            )
        }
    }

    fun equipSkill(skillId: String) {
        sound.playClick()
        repository.setEquippedSkill(skillId)
        _uiState.update {
            it.copy(
                equippedSkillId = skillId,
                bannerMessage = "Equipped Skill!"
            )
        }
    }

    // --- Fortune Wheel & Daily Timeline ---
    fun getDailySpinRemainingMillis(): Long {
        val last = _uiState.value.lastDailySpinTimestamp
        if (last == 0L) return 0L
        val cooldown = 24 * 60 * 60 * 1000L // 24 hours
        val elapsed = System.currentTimeMillis() - last
        return (cooldown - elapsed).coerceAtLeast(0L)
    }

    fun isDailySpinReady(): Boolean {
        if (!repository.isTimeIntegrityValid()) {
            return false
        }
        return getDailySpinRemainingMillis() == 0L
    }

    fun spinFortuneWheel(isFreeSpin: Boolean = true) {
        if (_uiState.value.isWheelSpinning) return

        val isReady = isDailySpinReady()
        if (isFreeSpin && !isReady) {
            _uiState.update { it.copy(bannerMessage = "Daily spin is on cooldown! Use Instant Spin.") }
            return
        }

        if (!isFreeSpin) {
            // Instant spin cost: 50 coins or 3 gems
            if (_uiState.value.coins >= 50) {
                val newCoins = _uiState.value.coins - 50
                repository.setCoins(newCoins)
                _uiState.update { it.copy(coins = newCoins) }
            } else if (_uiState.value.gems >= 3) {
                val newGems = _uiState.value.gems - 3
                repository.setGems(newGems)
                _uiState.update { it.copy(gems = newGems) }
            } else {
                _uiState.update { it.copy(bannerMessage = "Not enough coins or diamonds for Instant Spin!") }
                return
            }
        }

        _uiState.update {
            it.copy(
                isWheelSpinning = true,
                wheelWonSegment = null
            )
        }

        viewModelScope.launch {
            val totalSegments = GameConstants.WHEEL_SEGMENTS.size
            val winningIndex = Random.nextInt(totalSegments)
            val segmentAngle = 360f / totalSegments
            val targetRotation = 360f * 5 + (winningIndex * segmentAngle) + (segmentAngle / 2f)

            val current = _uiState.value.wheelRotation % 360f
            val diff = targetRotation
            val steps = 80
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val ease = 1f - kotlin.math.cos((t * Math.PI) / 2).toFloat()
                val rot = current + diff * ease
                _uiState.update { it.copy(wheelRotation = rot) }
                if (i % 4 == 0) sound.playWheelTick()
                delay(20L)
            }

            val won = GameConstants.WHEEL_SEGMENTS[winningIndex]
            val now = System.currentTimeMillis()

            var streakDay = repository.getDailyStreakDay()
            if (isFreeSpin) {
                repository.setLastDailySpinTimestamp(now)
                streakDay = if (streakDay >= 7) 1 else streakDay + 1
                repository.setDailyStreakDay(streakDay)
            }

            val newCoins = _uiState.value.coins + won.coins
            val newGems = _uiState.value.gems + won.gems
            repository.setCoins(newCoins)
            repository.setGems(newGems)
            repository.incrementWheelSpins()

            sound.playStageClear()
            _uiState.update {
                it.copy(
                    isWheelSpinning = false,
                    wheelWonSegment = won,
                    coins = newCoins,
                    gems = newGems,
                    wheelSpins = repository.getWheelSpins(),
                    lastDailySpinTimestamp = repository.getLastDailySpinTimestamp(),
                    dailyStreakDay = streakDay,
                    bannerMessage = "Won ${won.label} from Daily Wheel! 🎉"
                )
            }
        }
    }

    // --- Campaign Milestone Chests ---
    fun claimMilestoneChest(milestoneLevel: Int) {
        // Pass 5 (audit L8): verify the zone is actually complete (all 10
        // stages of the zone behind the milestone cleared) — previously any
        // chest paid out on tap regardless of progress.
        val zoneComplete = (1..10).all { stage ->
            _uiState.value.completedLevels.containsKey(milestoneLevel - 10 + stage)
        }
        if (!zoneComplete) {
            showBannerMessage("🗺️ Finish all 10 stages of this zone to open its chest!")
            return
        }
        val claimed = repository.getClaimedMilestoneChests()
        if (claimed.contains(milestoneLevel)) return

        repository.claimMilestoneChest(milestoneLevel)
        val bonusCoins = 350 + milestoneLevel * 50
        val bonusGems = 5 + (milestoneLevel / 5) * 2

        val newCoins = _uiState.value.coins + bonusCoins
        val newGems = _uiState.value.gems + bonusGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        sound.playClaimReward()
        _uiState.update {
            it.copy(
                claimedMilestoneChests = repository.getClaimedMilestoneChests(),
                coins = newCoins,
                gems = newGems,
                bannerMessage = "Opened Milestone $milestoneLevel Chest! +$bonusCoins🪙 +$bonusGems💎"
            )
        }
    }

    // --- Quests ---
    /** Pass 5: quest progress for the claim check. Mirrors the progress
     *  mapping the QuestsScreen itself displays (same quest ids, same
     *  sources) so a claim is accepted exactly when the UI shows the
     *  quest as complete — never stricter, never looser. */
    fun questProgress(quest: QuestItem): Int {
        val state = _uiState.value
        return when (quest.id) {
            "quest_pop_50", "quest_pop_200", "quest_special_pop_10" -> state.lifetimePops
            "quest_combo_5", "quest_combo_10" -> state.lifetimeMaxCombo
            "quest_spin_wheel" -> state.wheelSpins
            "quest_stage_5", "quest_stage_10", "quest_stage_clear_3star" -> state.completedLevels.size
            "quest_stars_15" -> state.completedLevels.size * 3
            else -> state.lifetimePops
        }
    }

    fun claimQuest(quest: QuestItem) {
        // Pass 5 (audit L9): verify progress before paying — previously a
        // quest could be claimed without meeting its target.
        if (questProgress(quest) < quest.target) {
            showBannerMessage("🎯 Keep going — ${quest.title} isn't complete yet!")
            return
        }
        val claimed = repository.getClaimedQuests()
        if (claimed.contains(quest.id)) return

        repository.claimQuest(quest.id)
        val newCoins = _uiState.value.coins + quest.rewardCoins
        val newGems = _uiState.value.gems + quest.rewardGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        sound.playClaimReward()
        _uiState.update {
            it.copy(
                claimedQuests = repository.getClaimedQuests(),
                coins = newCoins,
                gems = newGems,
                bannerMessage = "Claimed ${quest.title} Reward! 🎁"
            )
        }
    }

    // --- Achievements (Pass 5: data existed with no claim path at all) ---
    /** Progress toward an achievement. AchievementItem carries no type
     *  field, so progress is derived from the achievement id, matching
     *  each entry's description in GameConstants.ACHIEVEMENTS. */
    fun achievementProgress(achievement: AchievementItem): Int {
        val state = _uiState.value
        return when {
            achievement.id.startsWith("ach_combo") -> state.lifetimeMaxCombo
            achievement.id.startsWith("ach_stars") -> state.completedLevels.values.sum()
            achievement.id.startsWith("ach_stage") -> state.completedLevels.size
            achievement.id.startsWith("ach_wheel") -> state.wheelSpins
            else -> state.lifetimePops
        }
    }

    fun claimAchievement(achievement: AchievementItem) {
        if (achievementProgress(achievement) < achievement.target) {
            showBannerMessage("🏆 Keep going — ${achievement.title} isn't unlocked yet!")
            return
        }
        val claimed = repository.getClaimedAchievements()
        if (claimed.contains(achievement.id)) return

        repository.claimAchievement(achievement.id)
        val newCoins = _uiState.value.coins + achievement.rewardCoins
        val newGems = _uiState.value.gems + achievement.rewardGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        sound.playClaimReward()
        _uiState.update {
            it.copy(
                claimedAchievements = repository.getClaimedAchievements(),
                coins = newCoins,
                gems = newGems,
                bannerMessage = "Claimed ${achievement.title}! +${achievement.rewardCoins}🪙 +${achievement.rewardGems}💎 🏆"
            )
        }
    }

    // --- Balloon Skins (Pass 5: mirrors the HammerSkin plumbing) ---
    fun equippedBalloonSkin(): BalloonSkin {
        val id = _uiState.value.equippedBalloonSkinId
        return GameConstants.SHOP_BALLOON_SKINS.find { it.id == id } ?: GameConstants.SHOP_BALLOON_SKINS.first()
    }

    fun setEquippedBalloonSkin(skinId: String) {
        sound.playEquipSound()
        repository.setEquippedBalloonSkin(skinId)
        _uiState.update { it.copy(equippedBalloonSkinId = skinId) }
    }

    fun buyBalloonSkin(skin: BalloonSkin) {
        val state = _uiState.value
        if (state.unlockedBalloonSkins.contains(skin.id)) {
            setEquippedBalloonSkin(skin.id)
            return
        }
        if (skin.currency == CurrencyType.COINS && state.coins >= skin.cost) {
            val newCoins = state.coins - skin.cost
            repository.setCoins(newCoins)
            repository.unlockBalloonSkin(skin.id)
            repository.setEquippedBalloonSkin(skin.id)
            sound.playCoin()
            _uiState.update {
                it.copy(
                    coins = newCoins,
                    unlockedBalloonSkins = repository.getUnlockedBalloonSkins(),
                    equippedBalloonSkinId = skin.id
                )
            }
        } else if (skin.currency == CurrencyType.GEMS && state.gems >= skin.cost) {
            val newGems = state.gems - skin.cost
            repository.setGems(newGems)
            repository.unlockBalloonSkin(skin.id)
            repository.setEquippedBalloonSkin(skin.id)
            sound.playCoin()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedBalloonSkins = repository.getUnlockedBalloonSkins(),
                    equippedBalloonSkinId = skin.id
                )
            }
        } else {
            sound.playPurchaseFail()
            _uiState.update { it.copy(bannerMessage = "Not enough currency for ${skin.name}!") }
        }
    }

    // --- Lifecycle & Audio Focus Management ---
    fun onAppForegrounded() {
        sound.resumeAll()
        musicPlayer.onActivityResume()
    }

    fun onAppBackgrounded() {
        sound.pauseAll()
        musicPlayer.onActivityStop()
        if (_uiState.value.gameState == GameState.PLAYING) {
            pauseGame()
        }
    }

    fun onAppDestroyed() {
        sound.release()
        musicPlayer.release()
    }

    override fun onCleared() {
        super.onCleared()
        onAppDestroyed()
    }
}
