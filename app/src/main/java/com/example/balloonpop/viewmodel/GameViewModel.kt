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
    val transitionTargetLevel: Int? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
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
    private var multiplierTimeRemaining = 0f
    private var currentScreenShake = 0f
    private var internalSkillEnergy = 0f
    private var internalSkillCooldown = 0f
    private var hammerSlideTimeRemaining = 0f
    private var stageBoosterUsesCount = 0
    private var stageSkillUsesCount = 0
    private var uiSyncAccumulator = 0f
    private var sessionBaselineHighScore = 0

    val currentShake: Float
        get() = if (_uiState.value.isScreenShakeEnabled) currentScreenShake else 0f
    private var timerJob: Job? = null
    private var lastPopTime = 0L
    private var lastHammerTapTime = 0L

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
                unlockedThemes = setOf("theme_obsidian"),
                completedLevels = emptyMap(),
                lifetimePops = 0,
                lifetimeHighScore = 0,
                lifetimeMaxCombo = 1,
                wheelSpins = 0,
                claimedQuests = emptySet(),
                isSettingsOpen = false,
                bannerMessage = "All Game Data Reset Successfully! 🔄"
            )
        }
    }

    fun navigateTo(screen: CurrentScreen) {
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

    // --- Loss Aversion: Second Chance Revive ---
    fun reviveSession() {
        if (_uiState.value.gameState != GameState.GAMEOVER) return
        val currentCoins = _uiState.value.coins
        val reviveCost = 50
        val newCoins = if (currentCoins >= reviveCost) currentCoins - reviveCost else currentCoins
        if (currentCoins >= reviveCost) {
            repository.setCoins(newCoins)
        }
        sound.playStageClear()
        triggerVibration(50L)
        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                timeLeft = it.timeLeft + 12, // +12 seconds bonus
                missedCount = (it.missedCount - 2).coerceAtLeast(0), // forgives 2 missed balloons
                lives = 2,
                coins = newCoins,
                bannerMessage = "❤️ SECOND CHANCE REVIVE! +12s & 2 Misses Restored!"
            )
        }
        startTimer()
    }

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
        }
    }

    // --- Gameplay Navigation ---
    fun startCampaignStage(levelNum: Int) {
        val lvl = GameConstants.CAMPAIGN_LEVELS.getOrNull(levelNum - 1)
            ?: GameConstants.CAMPAIGN_LEVELS.first()
        val zoneNum = ((lvl.level - 1) / 5) + 1
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
        } else {
            _uiState.update {
                it.copy(
                    missedCount = 0
                )
            }
        }
        initGameSession()
    }

    fun playOnRevive() {
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
            val ambientType = when ((zoneNum - 1) % 5) {
                0 -> AmbientType.CHERRY_PETAL
                1 -> AmbientType.EMBER
                2 -> AmbientType.BUBBLE
                3 -> AmbientType.STAR
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
        multiplierTimeRemaining = 0f
        currentScreenShake = 0f
        internalSkillEnergy = 0f
        internalSkillCooldown = 0f
        hammerSlideTimeRemaining = 0f
        stageBoosterUsesCount = 0
        stageSkillUsesCount = 0
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
                previousHighScore = sessionBaselineHighScore
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
                    if (_uiState.value.gameState == GameState.PLAYING) {
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
        if (state.gameMode == GameMode.CAMPAIGN) {
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
        if (state.gameState != GameState.PLAYING) return

        val step = dtSeconds * 60f // Normalized to 60fps unit

        // 1. Freeze Countdown
        if (freezeTimeRemaining > 0f) {
            freezeTimeRemaining -= dtSeconds
            if (freezeTimeRemaining <= 0f) {
                freezeTimeRemaining = 0f
                _uiState.update { it.copy(isFreezeActive = false) }
            }
        }

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

        // 5. Spawning Balloons based on precise delta time
        spawnAccumulator += dtSeconds
        val effectiveInterval = if (freezeTimeRemaining > 0f) spawnIntervalSeconds * 2f else spawnIntervalSeconds
        if (spawnAccumulator >= effectiveInterval) {
            spawnAccumulator = 0f
            spawnBalloon()
        }

        // 6. Update Balloons
        val speedFactor = if (freezeTimeRemaining > 0f) 0.35f else 1.0f
        synchronized(balloons) {
            val it = balloons.iterator()
            while (it.hasNext()) {
                val b = it.next()
                if (b.popped) {
                    it.remove()
                    continue
                }
                val speedMult = if (b.type == BalloonType.FAST_ROCKET) 1.65f else 1.0f
                b.y -= b.vy * speedFactor * speedMult * step
                b.wobblePhase += 0.04f * step
                b.x += sin(b.wobblePhase) * 0.9f * step

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
                p.vy += 0.16f * step // gravity
                p.alpha -= 0.045f * step
                if (p.alpha <= 0f) {
                    it.remove()
                }
            }
        }

        // 11. Update Floating Texts
        synchronized(floatingTexts) {
            val it = floatingTexts.iterator()
            while (it.hasNext()) {
                val ft = it.next()
                ft.y += ft.vy * step
                ft.alpha -= 0.035f * step
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
        val radius = 42f + Random.nextFloat() * 24f
        val x = radius + Random.nextFloat() * (canvasWidth - radius * 2).coerceAtLeast(100f)
        val y = canvasHeight + radius + 15f

        val mode = _uiState.value.gameMode
        val lvl = _uiState.value.currentLevel

        // Milestone boss spawn on level 10, 20, 30... 150
        val isBossLevel = mode == GameMode.CAMPAIGN && (lvl % 10 == 0)
        val bossAlreadySpawned = balloons.any { it.type == BalloonType.MEGA_BOSS && !it.popped }

        val type: BalloonType = if (isBossLevel && !bossAlreadySpawned && (balloons.size <= 2 || Random.nextInt(100) < 60)) {
            BalloonType.MEGA_BOSS
        } else if (mode == GameMode.ZEN) {
            val nonBombs = listOf(
                BalloonType.STANDARD, BalloonType.GOLDEN, BalloonType.RAINBOW,
                BalloonType.FREEZE, BalloonType.MULTIPLIER, BalloonType.MYSTERY_GIFT,
                BalloonType.GOLDEN_PIGGY
            )
            nonBombs.random()
        } else {
            val roll = Random.nextInt(100)
            val bombThreshold = when {
                lvl >= 25 -> 88 // 12% bombs
                lvl >= 10 -> 90 // 10% bombs
                lvl >= 5 -> 92  // 8% bombs
                else -> 94      // 6% bombs
            }
            when {
                roll < 22 -> BalloonType.STANDARD
                roll < 32 -> BalloonType.GOLDEN
                roll < 39 -> BalloonType.RAINBOW
                roll < 50 -> BalloonType.FAST_ROCKET
                roll < 58 -> BalloonType.SPLITTER
                roll < 68 -> BalloonType.SHIELDED
                roll < 76 -> BalloonType.ELECTRIC_STORM
                roll < 81 -> BalloonType.MYSTERY_GIFT
                roll < 85 -> BalloonType.GHOST_PHANTOM
                roll < 88 -> BalloonType.GOLDEN_PIGGY
                roll < bombThreshold -> BalloonType.FREEZE
                else -> BalloonType.BOMB // Challenging bombs requiring swift focus & accuracy!
            }
        }

        val baseSpeed = when {
            type == BalloonType.MEGA_BOSS -> 2.4f
            type == BalloonType.FAST_ROCKET -> 7.8f + Random.nextFloat() * 3.5f
            mode == GameMode.TIME_RUSH -> 6.2f + Random.nextFloat() * 3.0f
            mode == GameMode.ARCADE -> 5.5f + Random.nextFloat() * 3.0f
            mode == GameMode.ZEN -> 2.6f + Random.nextFloat() * 1.5f
            else -> 4.8f + (lvl * 0.05f).coerceAtMost(4.5f) + Random.nextFloat() * 2.8f
        }

        val color = when (type) {
            BalloonType.GOLDEN -> Color(0xFFFFD700)
            BalloonType.BOMB -> Color(0xFF222226)
            BalloonType.FREEZE -> Color(0xFF00E5FF)
            BalloonType.HEART -> Color(0xFFFF2A75)
            BalloonType.MULTIPLIER -> Color(0xFFFFB703)
            BalloonType.RAINBOW -> Color(0xFFE040FB)
            BalloonType.FAST_ROCKET -> Color(0xFFFF0055)
            BalloonType.SPLITTER -> Color(0xFF00F0FF)
            BalloonType.SHIELDED -> Color(0xFF94A3B8)
            BalloonType.MYSTERY_GIFT -> Color(0xFFEC4899)
            BalloonType.GHOST_PHANTOM -> Color(0xFFE2E8F0)
            BalloonType.GOLDEN_PIGGY -> Color(0xFFF59E0B)
            BalloonType.ELECTRIC_STORM -> Color(0xFF38BDF8)
            BalloonType.MEGA_BOSS -> Color(0xFF7C3AED)
            else -> GameConstants.BALLOON_PALETTE.random()
        }

        val shapeStyle = when (type) {
            BalloonType.BOMB -> BalloonShapeStyle.ANGRY
            BalloonType.GOLDEN, BalloonType.GOLDEN_PIGGY -> BalloonShapeStyle.STAR_SHAPE
            BalloonType.HEART -> BalloonShapeStyle.HEART_SHAPE
            BalloonType.FREEZE -> BalloonShapeStyle.SHOCKED
            BalloonType.MULTIPLIER -> BalloonShapeStyle.STAR_SHAPE
            BalloonType.RAINBOW -> listOf(BalloonShapeStyle.COOL, BalloonShapeStyle.FUNNY, BalloonShapeStyle.CAT_FACE).random()
            BalloonType.MEGA_BOSS -> BalloonShapeStyle.ANGRY
            else -> BalloonShapeStyle.values().random()
        }

        val maxHp = when (type) {
            BalloonType.MEGA_BOSS -> (15 + (lvl / 10) * 5).coerceIn(15, 45)
            BalloonType.SHIELDED -> 2
            else -> 1
        }

        val spawnRadius = if (type == BalloonType.MEGA_BOSS) radius * 1.6f else radius

        synchronized(balloons) {
            if (balloons.size < 28) {
                balloons.add(
                    Balloon(
                        id = ++balloonIdCounter,
                        x = x,
                        y = y,
                        radius = spawnRadius,
                        vy = baseSpeed,
                        type = type,
                        color = color,
                        wobblePhase = Random.nextFloat() * 6.28f,
                        shapeStyle = shapeStyle,
                        hp = maxHp,
                        maxHp = maxHp
                    )
                )
            }
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

    private fun popBalloon(b: Balloon) {
        if (b.hp > 1) {
            b.hp -= 1
            sound.playShieldHit()
            triggerVibration(35L)
            if (b.type == BalloonType.MEGA_BOSS) {
                currentScreenShake = 16f
                val damageText = "BOSS -1 HP! 👑 (${b.hp}/${b.maxHp})"
                addFloatingText(b.x, b.y - 25f, damageText, Color(0xFFFFD700))
                createParticleBurst(b.x, b.y, Color(0xFFFFD700), count = 8)
                _uiState.update { it.copy(poppedCount = it.poppedCount + 1, score = it.score + 50) }
            } else {
                addFloatingText(b.x, b.y - 15f, "HIT! 🛡️", Color(0xFF00E5FF))
                createSparks(b.x, b.y, Color(0xFF00E5FF), count = 8)
            }
            return
        }

        b.popped = true
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
                addFloatingText(b.x, b.y, "BOMB! 💥", Color(0xFFFF2A75))
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
                addFloatingText(b.x, b.y, "FREEZE! ❄️", Color(0xFF00E5FF))
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
                    addFloatingText(b.x, b.y, "+1 LIFE ❤️", Color(0xFFFF2A75))
                } else {
                    addFloatingText(b.x, b.y, "+10 ❤️", Color(0xFFFF2A75))
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
                addFloatingText(b.x, b.y, "+30 🌈 +2🪙", Color(0xFFE040FB))
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
                addFloatingText(b.x, b.y, "SPLIT! 🫧", Color(0xFF00F0FF))
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
                addFloatingText(b.x, b.y, "ARMOR BROKEN! 🛡️ +35", Color(0xFF94A3B8))
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
                        addFloatingText(b.x, b.y, "GIFT: BLIZZARD! ❄️", Color(0xFF00E5FF))
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
                addFloatingText(b.x, b.y, "+$pts", b.color)
                applyScoreAndPop(b, comboMult, newCombo, newMaxCombo)
            }
        }

        // Particle burst according to performance settings
        val particleCount = when (_uiState.value.graphicsQuality) {
            "ULTRA" -> 16
            "BATTERY_SAVER" -> 6
            else -> 10
        }
        createParticleBurst(b.x, b.y, b.color, count = particleCount)
    }

    private fun applyScoreAndPop(
        b: Balloon,
        comboMultiplier: Int,
        newCombo: Int = _uiState.value.combo,
        newMaxCombo: Int = _uiState.value.maxCombo
    ) {
        val earnedScore = b.type.points * comboMultiplier
        val newScore = maxOf(0, _uiState.value.score + earnedScore)
        val newPopped = _uiState.value.poppedCount + 1

        val wasHighScoreBroken = !_uiState.value.isNewHighScoreAchieved && sessionBaselineHighScore > 0 && newScore > sessionBaselineHighScore
        if (wasHighScoreBroken) {
            sound.playHighScoreFanfare()
            triggerVibration(60L)
            triggerCandyPraise("NEW HIGH SCORE! 🏆")
            createSparks(b.x, b.y, Color(0xFFFFD700), count = 16)
            addFloatingText(b.x, b.y - 35f, "🏆 NEW RECORD! ${newScore} PTS! 🏆", Color(0xFFFFD700))
        }

        repository.addLifetimePops(1)
        repository.updateLifetimeHighScore(newScore)

        _uiState.update {
            it.copy(
                score = newScore,
                poppedCount = newPopped,
                combo = newCombo,
                maxCombo = newMaxCombo,
                skillEnergy = internalSkillEnergy,
                lifetimePops = repository.getLifetimePops(),
                lifetimeHighScore = repository.getLifetimeHighScore(),
                isNewHighScoreAchieved = it.isNewHighScoreAchieved || wasHighScoreBroken || (newScore > sessionBaselineHighScore && sessionBaselineHighScore > 0)
            )
        }

        // Campaign Stage Win Check
        if (_uiState.value.gameMode == GameMode.CAMPAIGN) {
            if (newPopped >= _uiState.value.targetPops) {
                stageClear()
            }
        }
    }

    private fun onMissBalloon(escapeX: Float = 150f) {
        if (_uiState.value.gameState != GameState.PLAYING) return
        val currentMode = _uiState.value.gameMode
        val newMissed = _uiState.value.missedCount + 1
        val maxAllowed = _uiState.value.maxMissesAllowed
        val clampedX = escapeX.coerceIn(60f, (canvasWidth - 60f).coerceAtLeast(100f))

        if (currentMode == GameMode.CAMPAIGN) {
            sound.playMiss()
            triggerVibration(35L)
            currentScreenShake = if (newMissed > maxAllowed) 26f else 16f

            if (newMissed > maxAllowed) {
                // If the person missed more balloon than limit, it automatically game over!
                sound.playMissWarning()
                addFloatingText(clampedX, 70f, "❌ LIMIT EXCEEDED! ($newMissed/$maxAllowed)", Color(0xFFEF4444))
                _uiState.update {
                    it.copy(
                        missedCount = newMissed,
                        combo = 0,
                        screenFlashAlpha = 0.35f
                    )
                }
                gameOver(GameOverReason.MISSED_LIMIT)
            } else {
                val warningColor = if (newMissed == maxAllowed) Color(0xFFF97316) else Color(0xFFFFD166)
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
                        color = Color(0xFF00F0FF),
                        branchPoints = branches
                    )
                )
            }
        }
    }

    fun triggerCandyPraise(text: String) {
        sound.playPraise()
        _uiState.update { it.copy(candyPraise = text) }
        viewModelScope.launch {
            delay(1600L)
            _uiState.update {
                if (it.candyPraise == text) it.copy(candyPraise = null) else it
            }
        }
    }

    fun triggerInGameBooster(booster: InGameBooster) {
        if (_uiState.value.gameState != GameState.PLAYING) return

        // Check booster inventory; deduct if available, or nominal coins if available
        val counts = _uiState.value.boosterCounts.toMutableMap()
        val currentCount = counts[booster.id] ?: 3

        if (currentCount > 0) {
            counts[booster.id] = currentCount - 1
            repository.setBoosterCount(booster.id, currentCount - 1)
        } else if (_uiState.value.coins >= 25) {
            val newCoins = _uiState.value.coins - 25
            repository.setCoins(newCoins)
            _uiState.update { it.copy(coins = newCoins) }
            addFloatingText(200f, 200f, "-25 🪙", Color(0xFFF59E0B))
            sound.playCoinEarn()
        }

        stageBoosterUsesCount++
        _uiState.update {
            it.copy(
                boosterCounts = counts,
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
                    createParticleBurst(b.x, b.y, Color(0xFF00F0FF), count = 16)
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
                    createShockwave(b.x, b.y, Color(0xFF00E5FF), maxRadius = 120f)
                    addFloatingText(b.x, b.y, "+120 ⚡ THUNDER!", Color(0xFF00F0FF))
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
                    addFloatingText(canvasWidth / 2f, canvasHeight / 2f, "🌈 RAINBOW BURST!", Color(0xFFFF007F))
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
                createShockwave(cx, cy, Color(0xFF00E5FF), maxRadius = 280f)
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
                createShockwave(cx, cy, Color(0xFF06D6A0), maxRadius = 300f)

                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped && b.type != BalloonType.BOMB) {
                            b.popped = true
                            applyScoreAndPop(b, comboMultiplier = 5)
                            createParticleBurst(b.x, b.y, Color(0xFF06D6A0), count = 10)
                        }
                    }
                }
                addFloatingText(cx, cy, "🌀 VORTEX! +400", Color(0xFF06D6A0))
                triggerCandyPraise("VORTEX! 🌀")
            }
        }
    }

    fun buyBooster(boosterId: String, withGems: Boolean = false) {
        val coinPrice = 150
        val gemPrice = 3
        val currentCount = _uiState.value.boosterCounts[boosterId] ?: 0

        if (withGems) {
            if (_uiState.value.gems >= gemPrice) {
                val newGems = _uiState.value.gems - gemPrice
                val newCount = currentCount + 1
                repository.setGems(newGems)
                repository.setBoosterCount(boosterId, newCount)
                val newCounts = _uiState.value.boosterCounts.toMutableMap()
                newCounts[boosterId] = newCount
                _uiState.update { it.copy(gems = newGems, boosterCounts = newCounts) }
                sound.playRewardClaim()
            } else {
                _uiState.update { it.copy(bannerMessage = "Need $gemPrice 💎 to buy this booster!") }
            }
        } else {
            if (_uiState.value.coins >= coinPrice) {
                val newCoins = _uiState.value.coins - coinPrice
                val newCount = currentCount + 1
                repository.setCoins(newCoins)
                repository.setBoosterCount(boosterId, newCount)
                val newCounts = _uiState.value.boosterCounts.toMutableMap()
                newCounts[boosterId] = newCount
                _uiState.update { it.copy(coins = newCoins, boosterCounts = newCounts) }
                sound.playRewardClaim()
            } else {
                _uiState.update { it.copy(bannerMessage = "Need $coinPrice 🪙 to buy this booster!") }
            }
        }
    }

    private fun stageClear() {
        timerJob?.cancel()

        val currentScore = _uiState.value.score
        val isHighScore = _uiState.value.isNewHighScoreAchieved || (currentScore > sessionBaselineHighScore && sessionBaselineHighScore > 0)
        if (isHighScore) {
            sound.playHighScoreFanfare()
        } else {
            sound.playStageClear()
        }

        val lvl = GameConstants.CAMPAIGN_LEVELS.getOrNull(_uiState.value.currentLevel - 1)
            ?: GameConstants.CAMPAIGN_LEVELS.first()

        val stars = when {
            _uiState.value.score >= lvl.targetScore * 1.3 -> 3
            _uiState.value.score >= lvl.targetScore -> 2
            else -> 1
        }

        repository.setLevelStars(lvl.level, stars)

        val newCoins = _uiState.value.coins + lvl.rewardCoins
        val newGems = _uiState.value.gems + lvl.rewardGems
        repository.setCoins(newCoins)
        repository.setGems(newGems)

        _uiState.update {
            it.copy(
                gameState = GameState.LEVELCLEAR,
                coins = newCoins,
                gems = newGems,
                completedLevels = repository.getCompletedLevels(),
                earnedCoinsThisSession = lvl.rewardCoins,
                earnedGemsThisSession = lvl.rewardGems,
                earnedStarsThisLevel = stars,
                isNewHighScoreAchieved = isHighScore,
                previousHighScore = sessionBaselineHighScore
            )
        }
    }

    private fun gameOver(reason: GameOverReason = GameOverReason.TIME_EXPIRED) {
        timerJob?.cancel()
        triggerVibration(80L)

        val earnedCoins = maxOf(5, _uiState.value.score / 20)
        val newCoins = _uiState.value.coins + earnedCoins
        repository.setCoins(newCoins)
        repository.updateLifetimeMaxCombo(_uiState.value.maxCombo)

        _uiState.update {
            it.copy(
                gameState = GameState.GAMEOVER,
                gameOverReason = reason,
                coins = newCoins,
                earnedCoinsThisSession = earnedCoins,
                lifetimeMaxCombo = repository.getLifetimeMaxCombo()
            )
        }
    }

    private fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        synchronized(floatingTexts) {
            if (floatingTexts.size < 12) {
                floatingTexts.add(FloatingText(x, y, text, color))
            }
        }
    }

    private fun createParticleBurst(x: Float, y: Float, color: Color, count: Int = 10) {
        val maxCap = when (_uiState.value.graphicsQuality) {
            "ULTRA" -> 120
            "BATTERY_SAVER" -> 25
            else -> 60
        }
        synchronized(particles) {
            if (particles.size >= maxCap) return
            for (i in 0 until count) {
                val angle = Random.nextFloat() * 6.28f
                val speed = 2.5f + Random.nextFloat() * 5.0f
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed,
                        radius = 4f + Random.nextFloat() * 5f,
                        color = color
                    )
                )
            }
        }
    }

    private fun createSparks(x: Float, y: Float, sparkColor: Color, count: Int = 8) {
        val maxCap = when (_uiState.value.graphicsQuality) {
            "ULTRA" -> 120
            "BATTERY_SAVER" -> 25
            else -> 60
        }
        synchronized(particles) {
            if (particles.size >= maxCap) return
            for (i in 0 until count) {
                val angle = Random.nextFloat() * 6.28f
                val speed = 4.0f + Random.nextFloat() * 6.0f
                particles.add(
                    Particle(
                        x = x,
                        y = y,
                        vx = kotlin.math.cos(angle) * speed,
                        vy = kotlin.math.sin(angle) * speed - 1.5f,
                        radius = 3f + Random.nextFloat() * 4f,
                        color = sparkColor
                    )
                )
            }
        }
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
                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "⏱️ CHRONO STASIS!", Color(0xFF00E5FF))
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
                addFloatingText(canvasWidth / 2f, canvasHeight * 0.42f, "🧲 VORTEX MAGNET!", Color(0xFFFF2A75))
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
    fun claimQuest(quest: QuestItem) {
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
