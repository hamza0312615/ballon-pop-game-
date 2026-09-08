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
import com.example.balloonpop.audio.SoundSynthesizer
import com.example.balloonpop.data.GameRepository
import com.example.balloonpop.model.*
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
    HOME,
    CAMPAIGN_MAP,
    SKILL_VAULT,
    ENDLESS_SELECT,
    SHOP,
    DIAMOND_STORE,
    DAILY_WHEEL,
    QUESTS,
    GAMEPLAY
}

data class UiState(
    val currentScreen: CurrentScreen = CurrentScreen.HOME,
    val gameState: GameState = GameState.MENU,
    val gameMode: GameMode = GameMode.CAMPAIGN,
    val currentLevel: Int = 1,
    val coins: Int = 150,
    val gems: Int = 10,
    val equippedHammerId: String = "hammer_wood",
    val equippedFrameId: String = "frame_minimal",
    val equippedBladeId: String = "blade_classic",
    val equippedThemeId: String = "theme_neon",
    val unlockedHammers: Set<String> = setOf("hammer_wood"),
    val unlockedFrames: Set<String> = setOf("frame_minimal"),
    val unlockedBlades: Set<String> = setOf("blade_classic"),
    val unlockedThemes: Set<String> = setOf("theme_neon"),
    // Skills State
    val equippedSkillId: String = "skill_slice",
    val unlockedSkills: Set<String> = setOf("skill_slice"),
    val skillLevels: Map<String, Int> = mapOf("skill_slice" to 1),
    val skillEnergy: Float = 100f,
    val isSkillActive: Boolean = false,
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
    // Banner Message / Toast
    val bannerMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val sound = SoundSynthesizer(application)
    val musicPlayer = BackgroundMusicPlayer()

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
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

    private var balloonIdCounter = 0L
    private var hammerEffectCounter = 0L
    private var shockwaveIdCounter = 0L
    private var ambientIdCounter = 0L
    private var spawnAccumulator = 0f
    private var spawnIntervalSeconds = 0.75f
    private var freezeTimeRemaining = 0f
    private var multiplierTimeRemaining = 0f
    private var currentScreenShake = 0f
    private var internalSkillEnergy = 100f
    private var internalSkillCooldown = 0f
    private var uiSyncAccumulator = 0f

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
        sound.setMuted(muted)
        sound.setVolume(vol)
        musicPlayer.setMuted(muted)
        musicPlayer.setVolume(vol * 0.50f)

        val unlockedSkills = repository.getUnlockedSkills()
        val skillLevels = GameConstants.AVAILABLE_SKILLS.associate { it.id to repository.getSkillLevel(it.id) }

        _uiState.update {
            it.copy(
                coins = repository.getCoins(),
                gems = repository.getGems(),
                equippedHammerId = repository.getEquippedHammer(),
                equippedFrameId = repository.getEquippedFrame(),
                equippedBladeId = repository.getEquippedBlade(),
                equippedThemeId = repository.getEquippedTheme(),
                unlockedHammers = repository.getUnlockedHammers(),
                unlockedFrames = repository.getUnlockedFrames(),
                unlockedBlades = repository.getUnlockedBlades(),
                unlockedThemes = repository.getUnlockedThemes(),
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
                isSoundMuted = muted,
                soundVolume = vol,
                isVibrationEnabled = repository.isVibrationEnabled(),
                isScreenShakeEnabled = repository.isScreenShakeEnabled(),
                graphicsQuality = repository.getGraphicsQuality(),
                isWideAssistEnabled = repository.isWideAssistEnabled()
            )
        }
    }

    fun triggerVibration(durationMs: Long = 25L) {
        if (!_uiState.value.isVibrationEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
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
        musicPlayer.setMuted(newMuted)
        repository.setSoundMuted(newMuted)
        _uiState.update { it.copy(isSoundMuted = newMuted) }
    }

    fun setSoundVolume(volume: Float) {
        sound.setVolume(volume)
        musicPlayer.setVolume(volume * 0.50f)
        repository.setSoundVolume(volume)
        _uiState.update { it.copy(soundVolume = volume) }
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
                equippedThemeId = "theme_neon",
                unlockedHammers = setOf("hammer_wood"),
                unlockedFrames = setOf("frame_minimal"),
                unlockedBlades = setOf("blade_classic"),
                unlockedThemes = setOf("theme_neon"),
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
        sound.playClick()
        _uiState.update { it.copy(currentScreen = screen) }
    }

    // --- Hammer System ---
    fun setEquippedHammer(hammerId: String) {
        sound.playClick()
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
            sound.playCoin()
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
            sound.playCoin()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedHammers = repository.getUnlockedHammers(),
                    equippedHammerId = hammer.id,
                    bannerMessage = "Unlocked ${hammer.name}! 💎"
                )
            }
        }
    }

    // --- Frame System ---
    fun setEquippedFrame(frameId: String) {
        sound.playClick()
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
            sound.playCoin()
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
            sound.playCoin()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedFrames = repository.getUnlockedFrames(),
                    equippedFrameId = frame.id,
                    bannerMessage = "Unlocked ${frame.name} Frame! 💎"
                )
            }
        }
    }

    // --- Diamond / Store ---
    fun claimDiamondPack(pack: DiamondStorePack) {
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

        val newGems = state.gems + pack.diamondsAmount
        repository.setGems(newGems)
        sound.playStageClear()
        triggerVibration(40L)
        _uiState.update {
            it.copy(
                gems = newGems,
                bannerMessage = "Claimed +${pack.diamondsAmount} 💎 successfully!"
            )
        }
    }

    fun clearBannerMessage() {
        _uiState.update { it.copy(bannerMessage = null) }
    }

    // --- Blade & Theme ---
    fun setEquippedBlade(bladeId: String) {
        sound.playClick()
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
            sound.playCoin()
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
            sound.playCoin()
            _uiState.update {
                it.copy(
                    gems = newGems,
                    unlockedBlades = repository.getUnlockedBlades(),
                    equippedBladeId = blade.id
                )
            }
        }
    }

    fun setEquippedTheme(themeId: String) {
        sound.playClick()
        repository.setEquippedTheme(themeId)
        _uiState.update { it.copy(equippedThemeId = themeId) }
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
        initGameSession()
    }

    fun nextLevel() {
        sound.playClick()
        val nextLvl = _uiState.value.currentLevel + 1
        if (nextLvl <= 150) {
            startCampaignStage(nextLvl)
        } else {
            navigateTo(CurrentScreen.CAMPAIGN_MAP)
        }
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

        spawnAccumulator = 0f
        spawnIntervalSeconds = if (state.gameMode == GameMode.TIME_RUSH) 0.45f else 0.68f
        freezeTimeRemaining = 0f
        multiplierTimeRemaining = 0f
        currentScreenShake = 0f
        internalSkillEnergy = 100f
        internalSkillCooldown = 0f
        uiSyncAccumulator = 0f
        lastPopTime = 0L

        _uiState.update {
            it.copy(
                gameState = GameState.PLAYING,
                score = 0,
                lives = 3,
                poppedCount = 0,
                combo = 0,
                maxCombo = 1,
                isFreezeActive = false,
                isMultiplierActive = false,
                screenShake = 0f,
                earnedCoinsThisSession = 0,
                earnedGemsThisSession = 0
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
                gameOver()
            }
        } else {
            gameOver()
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

        // 3. Skill Energy Regen and Cooldown Recovery (Throttled UI sync to eliminate recomposition churn)
        var triggerSync = false
        if (!_uiState.value.isSkillActive) {
            if (internalSkillEnergy < 100f) {
                internalSkillEnergy = (internalSkillEnergy + dtSeconds * 8f).coerceAtMost(100f)
                if (internalSkillEnergy >= 100f) triggerSync = true
            }
        }
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
                    it.remove()
                    if (state.gameMode == GameMode.CLASSIC && b.type != BalloonType.BOMB) {
                        onMissBalloon()
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

        val type: BalloonType = if (isBossLevel && !bossAlreadySpawned && Random.nextInt(100) < 35) {
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
            when {
                roll < 34 -> BalloonType.STANDARD
                roll < 44 -> BalloonType.GOLDEN
                roll < 52 -> BalloonType.RAINBOW
                roll < 62 -> BalloonType.FAST_ROCKET
                roll < 70 -> BalloonType.SPLITTER
                roll < 78 -> BalloonType.SHIELDED
                roll < 84 -> BalloonType.ELECTRIC_STORM
                roll < 89 -> BalloonType.MYSTERY_GIFT
                roll < 93 -> BalloonType.GHOST_PHANTOM
                roll < 96 -> BalloonType.GOLDEN_PIGGY
                roll < 98 -> BalloonType.FREEZE
                else -> BalloonType.BOMB
            }
        }

        val baseSpeed = when {
            type == BalloonType.MEGA_BOSS -> 1.5f
            type == BalloonType.FAST_ROCKET -> 5.5f + Random.nextFloat() * 2.0f
            mode == GameMode.TIME_RUSH -> 4.5f + Random.nextFloat() * 2.5f
            mode == GameMode.ARCADE -> 4.0f + Random.nextFloat() * 2.8f
            mode == GameMode.ZEN -> 2.2f + Random.nextFloat() * 1.5f
            else -> 3.2f + (lvl * 0.02f).coerceAtMost(2.5f) + Random.nextFloat() * 2.0f
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
            BalloonType.MEGA_BOSS -> 5
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

        val blade = GameConstants.SHOP_BLADES.find { it.id == _uiState.value.equippedBladeId }
            ?: GameConstants.SHOP_BLADES.first()
        val hammer = GameConstants.SHOP_HAMMERS.find { it.id == _uiState.value.equippedHammerId }
            ?: GameConstants.SHOP_HAMMERS.first()

        // Swipe trail
        synchronized(swipeTrail) {
            if (swipeTrail.size < 30) {
                swipeTrail.add(BladeTrailPoint(x, y, 1.0f, blade.trailColor))
            }
        }

        val now = System.currentTimeMillis()

        // Continuous Slicing vs Tap Strike
        val isSliceSkillEquipped = _uiState.value.equippedSkillId == "skill_slice"
        val hasEnergyForSlice = _uiState.value.skillEnergy > 2f
        val sliceActive = !isTap && (isSliceSkillEquipped || _uiState.value.isSkillActive) && hasEnergyForSlice

        if (sliceActive) {
            // Drain small amount of slice energy per drag point without high-frequency StateFlow emission
            internalSkillEnergy = (internalSkillEnergy - 0.35f).coerceAtLeast(0f)
        }

        // Hammer Strike Trigger on tap or paced intervals
        if (isTap || (!sliceActive && now - lastHammerTapTime > 160L)) {
            lastHammerTapTime = now
            sound.playHammerSmash(hammer.tier)
            synchronized(hammerStrikes) {
                if (hammerStrikes.size < 5) {
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
                "ULTRA" -> 14
                "BATTERY_SAVER" -> 4
                else -> 8
            }
            createSparks(x, y, hammer.sparkColor, count = sparkMultiplier + hammer.tier * 2)
        }

        // Magnetic pull effect if Magnet skill is equipped/active
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

        // Hit Detection against balloons with optional Wide Assist & Continuous Slice
        val wideAssistBonus = if (_uiState.value.isWideAssistEnabled) 20f else 0f
        val sliceRadiusBonus = if (sliceActive) 16f else 0f
        val hitRadiusBonus = 24f + (hammer.tier * 4f) + wideAssistBonus + sliceRadiusBonus

        synchronized(balloons) {
            for (i in balloons.indices.reversed()) {
                val b = balloons.getOrNull(i) ?: continue
                if (b.popped) continue

                val dist = hypot(x - b.x, y - b.y)
                if (dist <= b.radius + hitRadiusBonus) {
                    popBalloon(b)
                }
            }
        }
    }

    private fun popBalloon(b: Balloon) {
        if (b.hp > 1) {
            b.hp -= 1
            sound.playShieldHit()
            triggerVibration(35L)
            addFloatingText(b.x, b.y - 15f, "HIT! 🛡️", Color(0xFF00E5FF))
            createSparks(b.x, b.y, Color(0xFF00E5FF), count = 8)
            return
        }

        b.popped = true
        triggerVibration(25L)

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

        // Recharge a bit of skill energy on pop!
        internalSkillEnergy = (internalSkillEnergy + 4f).coerceAtMost(100f)

        val now = System.currentTimeMillis()
        var newCombo = _uiState.value.combo
        if (now - lastPopTime < 750) {
            newCombo++
        } else {
            newCombo = 1
        }
        lastPopTime = now

        val newMaxCombo = maxOf(_uiState.value.maxCombo, newCombo)
        val multMultiplier = if (_uiState.value.isMultiplierActive) 2 else 1
        val comboMult = min(newCombo, 5) * multMultiplier

        if (newCombo >= 2) {
            sound.playCombo(newCombo)
            addFloatingText(b.x, b.y - 20f, "COMBO ${newCombo}x!", Color(0xFFFFB703))
        }

        when (b.type) {
            BalloonType.BOMB -> {
                sound.playBomb()
                triggerVibration(60L)
                addFloatingText(b.x, b.y, "BOMB! 💥", Color(0xFFFF2A75))
                currentScreenShake = 24f
                if (_uiState.value.isScreenShakeEnabled) {
                    _uiState.update { it.copy(screenShake = 24f) }
                }
                if (_uiState.value.gameMode != GameMode.ZEN) {
                    val remainingLives = _uiState.value.lives - 1
                    _uiState.update { it.copy(lives = remainingLives) }
                    if (remainingLives <= 0) {
                        gameOver()
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
                sound.playCoin()
                multiplierTimeRemaining = 5.0f
                _uiState.update { it.copy(isMultiplierActive = true) }
                addFloatingText(b.x, b.y, "2X BONUS! ⭐", Color(0xFFFFD700))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.HEART -> {
                sound.playPop(b.radius)
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
                sound.playCoin()
                val addedCoins = 5
                val newCoins = _uiState.value.coins + addedCoins
                repository.setCoins(newCoins)
                addFloatingText(b.x, b.y, "+50 ⭐ +5🪙", Color(0xFFFFD700))
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.RAINBOW -> {
                sound.playPop(b.radius)
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
                currentScreenShake = 16f
                if (_uiState.value.isScreenShakeEnabled) {
                    _uiState.update { it.copy(screenShake = 16f) }
                }
                addFloatingText(b.x, b.y, "THUNDER ZAP! ⚡", Color(0xFF38BDF8))
                // Chain pop nearby balloons
                synchronized(balloons) {
                    for (other in balloons) {
                        if (!other.popped && other != b && hypot(other.x - b.x, other.y - b.y) <= 240f) {
                            if (other.type != BalloonType.BOMB && other.type != BalloonType.MEGA_BOSS) {
                                other.popped = true
                                createParticleBurst(other.x, other.y, other.color, 6)
                                applyScoreAndPop(other, comboMult)
                            }
                        }
                    }
                }
                applyScoreAndPop(b, comboMult)
            }
            BalloonType.MEGA_BOSS -> {
                sound.playPiggyJackpot()
                currentScreenShake = 30f
                if (_uiState.value.isScreenShakeEnabled) {
                    _uiState.update { it.copy(screenShake = 30f) }
                }
                val newCoins = _uiState.value.coins + 30
                val newGems = _uiState.value.gems + 5
                repository.setCoins(newCoins)
                repository.setGems(newGems)
                addFloatingText(b.x, b.y, "BOSS DEFEATED! 👑 +500", Color(0xFFFFD700))
                _uiState.update { it.copy(coins = newCoins, gems = newGems) }
                applyScoreAndPop(b, comboMult)
            }
            else -> {
                sound.playPop(b.radius)
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
                lifetimeHighScore = repository.getLifetimeHighScore()
            )
        }

        // Campaign Stage Win Check
        if (_uiState.value.gameMode == GameMode.CAMPAIGN) {
            if (newPopped >= _uiState.value.targetPops) {
                stageClear()
            }
        }
    }

    private fun onMissBalloon() {
        if (_uiState.value.gameState != GameState.PLAYING) return
        val currentLives = _uiState.value.lives - 1
        _uiState.update {
            it.copy(
                lives = currentLives,
                combo = 0
            )
        }
        if (currentLives <= 0) {
            gameOver()
        }
    }

    private fun stageClear() {
        timerJob?.cancel()
        sound.playStageClear()

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
                earnedStarsThisLevel = stars
            )
        }
    }

    private fun gameOver() {
        timerJob?.cancel()
        triggerVibration(80L)

        val earnedCoins = maxOf(5, _uiState.value.score / 20)
        val newCoins = _uiState.value.coins + earnedCoins
        repository.setCoins(newCoins)
        repository.updateLifetimeMaxCombo(_uiState.value.maxCombo)

        _uiState.update {
            it.copy(
                gameState = GameState.GAMEOVER,
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

        if (_uiState.value.skillCooldownRemaining > 0f) {
            sound.playClick()
            return
        }

        when (skill.type) {
            SkillType.CONTINUOUS_SLICE -> {
                sound.playCombo(3)
                _uiState.update {
                    it.copy(
                        isSkillActive = true,
                        skillEnergy = 100f,
                        bannerMessage = "⚡ LASER STREAM ACTIVE! GLIDE TO POP!"
                    )
                }
            }
            SkillType.TIME_FREEZE -> {
                sound.playFreeze()
                freezeTimeRemaining = 6.0f
                _uiState.update {
                    it.copy(
                        isFreezeActive = true,
                        skillCooldownRemaining = skill.cooldownSeconds,
                        bannerMessage = "⏱️ CHRONO STASIS ACTIVATED!"
                    )
                }
            }
            SkillType.MAGNET_PULL -> {
                sound.playCoin()
                _uiState.update {
                    it.copy(
                        isSkillActive = true,
                        skillCooldownRemaining = skill.cooldownSeconds,
                        bannerMessage = "🧲 VORTEX MAGNET ACTIVATED!"
                    )
                }
                viewModelScope.launch {
                    delay(5000L)
                    _uiState.update { it.copy(isSkillActive = false) }
                }
            }
            SkillType.SHOCKWAVE -> {
                sound.playBomb()
                triggerVibration(70L)
                currentScreenShake = 20f
                _uiState.update {
                    it.copy(
                        skillCooldownRemaining = skill.cooldownSeconds,
                        screenShake = 20f,
                        bannerMessage = "💥 SUPERNOVA BLAST!"
                    )
                }
                // Pop all standard on screen balloons with reward
                synchronized(balloons) {
                    for (b in balloons) {
                        if (!b.popped && b.type != BalloonType.BOMB) {
                            b.popped = true
                            applyScoreAndPop(b, 2)
                            createParticleBurst(b.x, b.y, b.color, count = 8)
                        }
                    }
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

        sound.playStageClear()
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

        sound.playStageClear()
        _uiState.update {
            it.copy(
                claimedQuests = repository.getClaimedQuests(),
                coins = newCoins,
                gems = newGems,
                bannerMessage = "Claimed ${quest.title} Reward! 🎁"
            )
        }
    }
}
