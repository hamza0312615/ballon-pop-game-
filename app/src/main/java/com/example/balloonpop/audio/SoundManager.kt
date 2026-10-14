package com.example.balloonpop.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.util.Log
import com.example.balloonpop.model.BalloonType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.math.*
import kotlin.random.Random

/**
 * Data class representing current audio volume and mute settings.
 */
data class SoundSettings(
    val masterVolume: Float = 1.0f,
    val sfxVolume: Float = 1.0f,
    val musicVolume: Float = 0.50f,
    val isMasterMuted: Boolean = false,
    val isSfxMuted: Boolean = false,
    val isMusicMuted: Boolean = false,
    val currentMusicTheme: MusicTheme = MusicTheme.MEADOW_JOY
)

/**
 * Centralized SoundManager: Unified audio feedback and background synth music engine for Balloon Pop.
 *
 * Implements Android AudioAttributes (USAGE_GAME, CONTENT_TYPE_SONIFICATION / MUSIC), SoundPool,
 * and procedural synth music synthesis with fine-grained volume controls.
 *
 * Provides a thread-safe centralized singleton accessed via [SoundManager.getInstance].
 */
class SoundManager(context: Context? = null) {

    private val engine: CentralSoundEngine = getEngine(context)

    companion object {
        private const val TAG = "SoundManager"
        private const val SAMPLE_RATE = 44100
        private const val MAX_SOUNDPOOL_STREAMS = 24

        @Volatile
        private var engineInstance: CentralSoundEngine? = null

        @Volatile
        private var singletonManager: SoundManager? = null

        internal fun getEngine(context: Context? = null): CentralSoundEngine {
            return engineInstance ?: synchronized(this) {
                engineInstance ?: CentralSoundEngine(context?.applicationContext ?: context).also {
                    engineInstance = it
                }
            }
        }

        fun getInstance(context: Context? = null): SoundManager {
            return singletonManager ?: synchronized(this) {
                singletonManager ?: SoundManager(context?.applicationContext ?: context).also {
                    singletonManager = it
                }
            }
        }
    }

    // --- Audio properties ---
    val audioAttributes: AudioAttributes get() = engine.audioAttributes
    val musicPlayer: BackgroundMusicPlayer get() = engine.musicPlayer
    val settings: StateFlow<SoundSettings> get() = engine.settings

    val masterVolume: Float get() = engine.masterVolume
    val sfxVolume: Float get() = engine.sfxVolume
    val musicVolume: Float get() = engine.musicVolume
    val isMuted: Boolean get() = engine.isMuted
    val muted: Boolean get() = engine.isMuted
    val isSfxMuted: Boolean get() = engine.isSfxMuted
    val isMusicMuted: Boolean get() = engine.isMusicMuted
    val currentMusicTheme: MusicTheme get() = engine.currentMusicTheme
    val isMusicPlaying: Boolean get() = engine.isMusicPlaying

    // --- Volume & Mute Controls ---
    fun setMasterVolume(volume: Float) = engine.setMasterVolume(volume)
    fun setSfxVolume(volume: Float) = engine.setSfxVolume(volume)
    fun setMusicVolume(volume: Float) = engine.setMusicVolume(volume)
    fun setVolume(volume: Float) = engine.setMasterVolume(volume)

    fun setMuted(value: Boolean) = engine.setMuted(value)
    fun setSfxMuted(value: Boolean) = engine.setSfxMuted(value)
    fun setMusicMuted(value: Boolean) = engine.setMusicMuted(value)

    fun toggleMute(): Boolean = engine.toggleMute()
    fun toggleSfxMute(): Boolean = engine.toggleSfxMute()
    fun toggleMusicMute(): Boolean = engine.toggleMusicMute()

    // --- Background Synth Music Controls ---
    fun startMusic(theme: MusicTheme = MusicTheme.MEADOW_JOY) = engine.startMusic(theme)
    fun playMusic(theme: MusicTheme = MusicTheme.MEADOW_JOY) = engine.playMusic(theme)
    fun pauseMusic() = engine.pauseMusic()
    fun resumeMusic() = engine.resumeMusic()
    fun stopMusic() = engine.stopMusic()
    fun setMusicTheme(theme: MusicTheme) = engine.setMusicTheme(theme)
    fun setMusicThemeForZone(zoneNumber: Int, isBossStage: Boolean = false) = engine.setMusicThemeForZone(zoneNumber, isBossStage)

    // --- Balloon Pop Audio Feedback ---
    fun playBalloonPop(type: BalloonType, radius: Float = 35f) = engine.playBalloonPop(type, radius)
    fun playPop(radius: Float = 35f) = engine.playPop(radius)
    fun playStandardPop(radius: Float = 35f) = engine.playPop(radius)
    fun playGoldenPop() = engine.playGoldenPop()
    fun playRainbowPop() = engine.playRainbowPop()
    fun playBomb() = engine.playBomb()
    fun playBombPop() = engine.playBomb()
    fun playFreeze() = engine.playFreeze()
    fun playFreezePop() = engine.playFreeze()
    fun playHeartPop() = engine.playHeartPop()
    fun playMultiplierPop() = engine.playMultiplierPop()
    fun playRocketSwoosh() = engine.playRocketSwoosh()
    fun playFastRocketPop() = engine.playRocketSwoosh()
    fun playSplitterPop(radius: Float = 35f) = engine.playSplitterPop(radius)
    fun playShieldCrack() = engine.playShieldCrack()
    fun playShieldHit() = engine.playShieldHit()
    fun playMystery() = engine.playMystery()
    fun playMysteryGiftPop() = engine.playMystery()
    fun playGhostChime() = engine.playGhostChime()
    fun playGhostPhantomPop() = engine.playGhostChime()
    fun playPiggyJackpot() = engine.playPiggyJackpot()
    fun playGoldenPiggyPop() = engine.playPiggyJackpot()
    fun playZap() = engine.playZap()
    fun playElectricStormPop() = engine.playZap()
    fun playMegaBossDefeat() = engine.playMegaBossDefeat()

    // --- Menu & UI Interaction Audio Feedback ---
    fun playMenuClick() = engine.playMenuClick()
    fun playClick() = engine.playMenuClick()
    fun playMenuTabSwitch() = engine.playMenuTabSwitch()
    fun playPurchaseSuccess() = engine.playPurchaseSuccess()
    fun playPurchaseFail() = engine.playPurchaseFail()
    fun playEquipSound() = engine.playEquipSound()
    fun playDialogOpen() = engine.playDialogOpen()
    fun playDialogClose() = engine.playDialogClose()
    fun playClaimReward() = engine.playClaimReward()
    fun playLevelSelect() = engine.playLevelSelect()
    fun playWheelTick() = engine.playWheelTick()
    fun playLevelWarp() = engine.playLevelWarp()

    // --- Gameplay Action Audio ---
    fun playCoin() = engine.playCoin()
    fun playCoinEarn() = engine.playCoinEarn()
    fun playGemEarn() = engine.playGemEarn()
    fun playRewardClaim() = engine.playRewardClaim()
    fun playHammerSmash(tier: Int = 1) = engine.playHammerSmash(tier)
    fun playSlash() = engine.playSlash()
    fun playCombo(comboLevel: Int) = engine.playCombo(comboLevel)
    fun playStageClear() = engine.playStageClear()
    fun playThunder() = engine.playThunder()
    fun playRainbowBlast() = engine.playRainbowBlast()
    fun playPraise() = engine.playPraise()
    fun playStarChime(starIndex: Int = 1) = engine.playStarChime(starIndex)
    fun playMiss() = engine.playMiss()
    fun playMissWarning() = engine.playMissWarning()
    fun playVaultOpen() = engine.playVaultOpen()
    fun playShopChime() = engine.playShopChime()
    fun playQuestReward() = engine.playQuestReward()
    fun playBoosterPop() = engine.playBoosterPop()
    fun playHighScoreFanfare() = engine.playHighScoreFanfare()

    // --- Lifecycle ---
    fun pauseAll() = engine.pauseAll()
    fun resumeAll() = engine.resumeAll()
    fun onActivityResume() = engine.onActivityResume()
    fun onActivityPause() = engine.onActivityPause()
    fun onActivityStop() = engine.onActivityStop()
    fun release() = engine.release()
}

/**
 * Central audio engine singleton worker that manages SoundPool, AudioTrack fallback mixer,
 * and procedural synth music player with fine-grained volume controls.
 */
internal class CentralSoundEngine(private val context: Context? = null) {

    companion object {
        private const val TAG = "CentralSoundEngine"
        private const val SAMPLE_RATE = 44100
        private const val MAX_SOUNDPOOL_STREAMS = 24
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 1. AudioAttributes configured strictly for game sonification
    val audioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    // 2. SoundPool configured with AudioAttributes and max streams
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_SOUNDPOOL_STREAMS)
        .setAudioAttributes(audioAttributes)
        .build()

    // 3. Integrated procedural Background Synth Music Player
    val musicPlayer: BackgroundMusicPlayer = BackgroundMusicPlayer(context)

    // SoundPool cache maps: Key -> SampleID
    private val soundPoolIds = ConcurrentHashMap<String, Int>()
    private val loadedSoundIds = ConcurrentSkipListSet<Int>()

    // In-memory PCM buffers for fallback & real-time pitch shifting
    private val pcmBuffers = ConcurrentHashMap<String, ShortArray>()

    // Fallback AudioTrack mixer for immediate pre-decode playback
    private var fallbackAudioTrack: AudioTrack? = null
    private var mixerJob: Job? = null

    private class ActiveVoice(
        val pcm: ShortArray,
        var position: Double = 0.0,
        val step: Double = 1.0,
        val volume: Float = 1.0f
    )
    private val activeVoices = CopyOnWriteArrayList<ActiveVoice>()

    @Volatile
    var isMuted: Boolean = false
        private set

    @Volatile
    var isSfxMuted: Boolean = false
        private set

    @Volatile
    var isMusicMuted: Boolean = false
        private set

    @Volatile
    var masterVolume: Float = 1.0f
        private set

    @Volatile
    var sfxVolume: Float = 1.0f
        private set

    @Volatile
    var musicVolume: Float = 0.50f
        private set

    @Volatile
    private var isReleased: Boolean = false

    @Volatile
    private var isPaused: Boolean = false

    private val prefs: SharedPreferences? = try {
        context?.getSharedPreferences("balloon_pop_audio_settings", Context.MODE_PRIVATE)
    } catch (_: Exception) {
        null
    }

    private val _settings = MutableStateFlow(SoundSettings())
    val settings: StateFlow<SoundSettings> = _settings.asStateFlow()

    init {
        // Load persisted audio settings
        prefs?.let { p ->
            masterVolume = p.getFloat("setting_master_volume", 1.0f).coerceIn(0f, 1f)
            sfxVolume = p.getFloat("setting_sfx_volume", 1.0f).coerceIn(0f, 1f)
            musicVolume = p.getFloat("setting_music_volume", 0.50f).coerceIn(0f, 1f)
            isMuted = p.getBoolean("setting_muted", false)
            isSfxMuted = p.getBoolean("setting_sfx_muted", false)
            isMusicMuted = p.getBoolean("setting_music_muted", false)
        }

        musicPlayer.setMuted(isMuted || isMusicMuted)
        musicPlayer.setVolume(masterVolume * musicVolume)

        _settings.value = SoundSettings(
            masterVolume = masterVolume,
            sfxVolume = sfxVolume,
            musicVolume = musicVolume,
            isMasterMuted = isMuted,
            isSfxMuted = isSfxMuted,
            isMusicMuted = isMusicMuted,
            currentMusicTheme = musicPlayer.theme
        )

        // Register load listener for SoundPool
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSoundIds.add(sampleId)
            } else {
                Log.w(TAG, "SoundPool sample $sampleId failed to load (status $status)")
            }
        }

        // Initialize audio generation and SoundPool preloading in background
        scope.launch(Dispatchers.IO) {
            try {
                pregenerateAllAudio()
                exportAndLoadSoundPool()
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing sound effects", e)
            }
            startFallbackMixerLoop()
        }
    }

    // =========================================================================
    //  WAVEFORM SYNTHESIS & SOUNDPOOL INITIALIZATION
    // =========================================================================

    private fun pregenerateAllAudio() {
        // --- 1. Balloon Pop Distinct Audio Effects ---
        pcmBuffers["pop_standard_1"] = generatePopPcm(440f, 75f, 0.08f)
        pcmBuffers["pop_standard_2"] = generatePopPcm(360f, 65f, 0.09f)
        pcmBuffers["pop_standard_3"] = generatePopPcm(540f, 90f, 0.07f)
        pcmBuffers["pop_golden"] = generateGoldenPopPcm()
        pcmBuffers["pop_rainbow"] = generateRainbowPcm()
        pcmBuffers["pop_bomb"] = generateBombPcm()
        pcmBuffers["pop_freeze"] = generateFreezePcm()
        pcmBuffers["pop_heart"] = generateHeartPopPcm()
        pcmBuffers["pop_multiplier"] = generateMultiplierPcm()
        pcmBuffers["pop_fast_rocket"] = generateRocketPcm()
        pcmBuffers["pop_splitter"] = generateSplitterPopPcm()
        pcmBuffers["pop_shielded"] = generateShieldCrackPcm()
        pcmBuffers["pop_mystery_gift"] = generateMysteryPcm()
        pcmBuffers["pop_ghost_phantom"] = generateGhostChimePcm()
        pcmBuffers["pop_golden_piggy"] = generatePiggyPcm()
        pcmBuffers["pop_electric_storm"] = generateZapPcm()
        pcmBuffers["pop_mega_boss"] = generateMegaBossDefeatPcm()

        // --- 2. Menu & UI Interaction Audio Effects ---
        pcmBuffers["menu_click"] = generateClickPcm()
        pcmBuffers["menu_tab_switch"] = generateTabSwitchPcm()
        pcmBuffers["menu_purchase_success"] = generateShopChimePcm()
        pcmBuffers["menu_purchase_fail"] = generatePurchaseFailPcm()
        pcmBuffers["menu_equip"] = generateEquipSnapPcm()
        pcmBuffers["menu_dialog_open"] = generateDialogOpenPcm()
        pcmBuffers["menu_dialog_close"] = generateDialogClosePcm()
        pcmBuffers["menu_claim_reward"] = generateQuestRewardPcm()
        pcmBuffers["menu_level_select"] = generateLevelSelectPcm()
        pcmBuffers["menu_wheel_tick"] = generateWheelTickPcm()
        pcmBuffers["menu_level_warp"] = generateLevelWarpPcm()

        // --- 3. Celebratory & Gameplay Action Audio Effects ---
        pcmBuffers["coin"] = generateCoinPcm()
        pcmBuffers["hammer"] = generateHammerSmashPcm()
        pcmBuffers["slash"] = generateSlashPcm()
        pcmBuffers["combo"] = generateComboPcm()
        pcmBuffers["stage_clear"] = generateStageClearPcm()
        pcmBuffers["highscore"] = generateHighScoreFanfarePcm()
        pcmBuffers["thunder"] = generateThunderPcm()
        pcmBuffers["praise"] = generatePraisePcm()
        pcmBuffers["star1"] = generateStarChimePcm(1)
        pcmBuffers["star2"] = generateStarChimePcm(2)
        pcmBuffers["star3"] = generateStarChimePcm(3)
        pcmBuffers["miss"] = generateMissPcm()
        pcmBuffers["miss_warning"] = generateMissWarningPcm()
        pcmBuffers["shield_hit"] = generateShieldHitPcm()
        pcmBuffers["vault_open"] = generateVaultOpenPcm()
    }

    private fun exportAndLoadSoundPool() {
        val cacheDirectory = context?.cacheDir?.resolve("soundpool_cache") ?: return
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdirs()
        }

        for ((key, pcm) in pcmBuffers) {
            try {
                val soundFile = File(cacheDirectory, "$key.wav")
                writeWavHeaderAndPcm(soundFile, pcm, SAMPLE_RATE)
                val sampleId = soundPool.load(soundFile.absolutePath, 1)
                if (sampleId != 0) {
                    soundPoolIds[key] = sampleId
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load $key into SoundPool: ${e.message}")
            }
        }
    }

    private fun writeWavHeaderAndPcm(file: File, pcmData: ShortArray, sampleRate: Int) {
        val totalAudioLen = pcmData.size * 2
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * 2

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = 1 // Mono
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // Block align
        header[33] = 0
        header[34] = 16 // Bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuffer = ByteBuffer.allocate(totalAudioLen).order(ByteOrder.LITTLE_ENDIAN)
            for (sample in pcmData) {
                byteBuffer.putShort(sample)
            }
            fos.write(byteBuffer.array())
        }
    }

    // =========================================================================
    //  CORE PLAYBACK ENGINE (SoundPool with AudioTrack fallback)
    // =========================================================================

    private fun play(key: String, rate: Float = 1.0f, volume: Float = 1.0f) {
        if (isMuted || isSfxMuted || masterVolume <= 0.001f || sfxVolume <= 0.001f || isReleased) return
        val finalVol = (volume * masterVolume * sfxVolume).coerceIn(0f, 1f)
        val clampedRate = rate.coerceIn(0.5f, 2.0f)

        // Try playing via SoundPool first
        val sampleId = soundPoolIds[key]
        if (sampleId != null && loadedSoundIds.contains(sampleId)) {
            val streamId = soundPool.play(sampleId, finalVol, finalVol, 1, 0, clampedRate)
            if (streamId != 0) {
                return // SoundPool played successfully!
            }
        }

        // Fallback: If SoundPool is not ready or busy, play via internal AudioTrack mixer
        val pcm = pcmBuffers[key] ?: return
        if (activeVoices.size >= 16) {
            try {
                activeVoices.removeAt(0)
            } catch (_: Exception) {}
        }
        activeVoices.add(ActiveVoice(pcm, 0.0, clampedRate.toDouble(), finalVol))
    }

    // =========================================================================
    //  DISTINCT BALLOON POP AUDIO FEEDBACK
    // =========================================================================

    /**
     * Plays satisfying and distinct audio feedback matched to the specific BalloonType.
     * Incorporates pitch modulation based on balloon radius so larger balloons sound deeper
     * and smaller balloons sound crisper.
     */
    fun playBalloonPop(type: BalloonType, radius: Float = 35f) {
        val sizeFactor = (60f - radius.coerceIn(18f, 65f)) / 60f // 0.0 to 0.7
        val radiusRate = 0.85f + sizeFactor * 0.4f

        when (type) {
            BalloonType.STANDARD -> {
                val chosen = listOf("pop_standard_1", "pop_standard_2", "pop_standard_3").random()
                play(chosen, rate = radiusRate, volume = 0.95f)
            }
            BalloonType.GOLDEN -> {
                play("pop_golden", rate = 1.0f, volume = 1.0f)
            }
            BalloonType.RAINBOW -> {
                play("pop_rainbow", rate = 1.05f, volume = 0.95f)
            }
            BalloonType.BOMB -> {
                play("pop_bomb", rate = 0.95f, volume = 1.0f)
            }
            BalloonType.FREEZE -> {
                play("pop_freeze", rate = 1.0f, volume = 0.95f)
            }
            BalloonType.HEART -> {
                play("pop_heart", rate = 1.0f, volume = 0.95f)
            }
            BalloonType.MULTIPLIER -> {
                play("pop_multiplier", rate = 1.0f, volume = 0.95f)
            }
            BalloonType.FAST_ROCKET -> {
                play("pop_fast_rocket", rate = 1.15f, volume = 1.0f)
            }
            BalloonType.SPLITTER -> {
                play("pop_splitter", rate = radiusRate, volume = 0.90f)
            }
            BalloonType.SHIELDED -> {
                play("pop_shielded", rate = 1.0f, volume = 1.0f)
            }
            BalloonType.MYSTERY_GIFT -> {
                play("pop_mystery_gift", rate = 1.0f, volume = 0.95f)
            }
            BalloonType.GHOST_PHANTOM -> {
                play("pop_ghost_phantom", rate = 0.95f, volume = 0.90f)
            }
            BalloonType.GOLDEN_PIGGY -> {
                play("pop_golden_piggy", rate = 1.0f, volume = 1.0f)
            }
            BalloonType.ELECTRIC_STORM -> {
                play("pop_electric_storm", rate = 1.05f, volume = 1.0f)
            }
            BalloonType.MEGA_BOSS -> {
                play("pop_mega_boss", rate = 0.9f, volume = 1.0f)
            }
        }
    }

    // Direct balloon pop convenience methods for compatibility
    fun playPop(radius: Float = 35f) = playBalloonPop(BalloonType.STANDARD, radius)
    fun playGoldenPop() = play("pop_golden", rate = 1.0f, volume = 1.0f)
    fun playRainbowPop() = play("pop_rainbow", rate = 1.0f, volume = 0.95f)
    fun playBomb() = play("pop_bomb", rate = 1.0f, volume = 1.0f)
    fun playFreeze() = play("pop_freeze", rate = 1.0f, volume = 0.95f)
    fun playHeartPop() = play("pop_heart", rate = 1.0f, volume = 0.95f)
    fun playMultiplierPop() = play("pop_multiplier", rate = 1.0f, volume = 0.95f)
    fun playRocketSwoosh() = play("pop_fast_rocket", rate = 1.1f, volume = 1.0f)
    fun playSplitterPop(radius: Float = 35f) = play("pop_splitter", rate = (0.85f + ((60f - radius.coerceIn(18f, 65f)) / 60f) * 0.4f), volume = 0.9f)
    fun playShieldCrack() = play("pop_shielded", rate = 1.0f, volume = 1.0f)
    fun playShieldHit() = play("shield_hit", rate = 1.0f, volume = 0.9f)
    fun playMystery() = play("pop_mystery_gift", rate = 1.0f, volume = 0.95f)
    fun playGhostChime() = play("pop_ghost_phantom", rate = 0.95f, volume = 0.9f)
    fun playPiggyJackpot() = play("pop_golden_piggy", rate = 1.0f, volume = 1.0f)
    fun playZap() = play("pop_electric_storm", rate = 1.0f, volume = 1.0f)
    fun playMegaBossDefeat() = play("pop_mega_boss", rate = 0.9f, volume = 1.0f)

    // =========================================================================
    //  DISTINCT MENU & UI INTERACTION AUDIO FEEDBACK
    // =========================================================================

    /** Snappy, tactile bubble wooden click for button taps */
    fun playMenuClick() {
        play("menu_click", rate = 1.0f + Random.nextFloat() * 0.1f, volume = 0.85f)
    }

    /** Clean glass slide sound for category/tab switches */
    fun playMenuTabSwitch() {
        play("menu_tab_switch", rate = 1.0f, volume = 0.75f)
    }

    /** Celebratory cash register / golden coins jingle for successful purchases */
    fun playPurchaseSuccess() {
        play("menu_purchase_success", rate = 1.0f, volume = 1.0f)
    }

    /** Soft warning buzzer for insufficient currency or locked items */
    fun playPurchaseFail() {
        play("menu_purchase_fail", rate = 0.95f, volume = 0.75f)
    }

    /** Solid tactile metallic lock/snap when equipping skins, frames, or hammers */
    fun playEquipSound() {
        play("menu_equip", rate = 1.05f, volume = 0.9f)
    }

    /** Gentle airy whoosh when opening stage or modal dialogs */
    fun playDialogOpen() {
        play("menu_dialog_open", rate = 1.0f, volume = 0.8f)
    }

    /** Gentle reverse pop when dismissing dialogs */
    fun playDialogClose() {
        play("menu_dialog_close", rate = 1.0f, volume = 0.75f)
    }

    /** Grand celebratory chime fanfare when claiming quest or milestone rewards */
    fun playClaimReward() {
        play("menu_claim_reward", rate = 1.0f, volume = 1.0f)
    }

    /** Springy arcade bubble sound for level select on campaign map */
    fun playLevelSelect() {
        play("menu_level_select", rate = 1.0f + Random.nextFloat() * 0.15f, volume = 0.85f)
    }

    /** Mechanical ratchet click for Daily Spin wheel ticks */
    fun playWheelTick() {
        play("menu_wheel_tick", rate = 0.9f + Random.nextFloat() * 0.2f, volume = 0.7f)
    }

    /** Sci-fi hyperspace zoom for level dive warp transition */
    fun playLevelWarp() {
        play("menu_level_warp", rate = 1.0f, volume = 1.0f)
    }

    // =========================================================================
    //  GAMEPLAY ACTION SOUNDS (Existing compatibility methods)
    // =========================================================================

    fun playClick() = playMenuClick()

    fun playCoin() = play("coin", rate = 1.0f + Random.nextFloat() * 0.08f, volume = 0.85f)

    fun playHammerSmash(tier: Int = 1) {
        val rate = when (tier) {
            1 -> 1.0f
            2 -> 0.9f
            3 -> 1.1f
            4 -> 1.25f
            5 -> 0.8f
            6 -> 1.3f
            else -> 1.0f
        }
        play("hammer", rate = rate, volume = 1.0f)
    }

    fun playSlash() = play("slash", rate = 1.0f, volume = 0.85f)

    fun playCombo(comboLevel: Int) {
        val pitch = (1.0f + (comboLevel * 0.05f)).coerceAtMost(1.8f)
        play("combo", rate = pitch, volume = 0.9f)
    }

    fun playStageClear() = play("stage_clear", rate = 1.0f, volume = 1.0f)

    fun playThunder() = play("thunder", rate = 1.0f, volume = 1.0f)

    fun playRainbowBlast() = play("pop_rainbow", rate = 1.1f, volume = 1.0f)

    fun playPraise() = play("praise", rate = 1.0f, volume = 0.9f)

    fun playStarChime(starIndex: Int = 1) {
        val key = when (starIndex) {
            1 -> "star1"
            2 -> "star2"
            else -> "star3"
        }
        play(key, rate = 1.0f, volume = 0.9f)
    }

    fun playMiss() = play("miss", rate = 1.0f, volume = 0.75f)

    fun playMissWarning() = play("miss_warning", rate = 1.0f, volume = 0.85f)

    fun playVaultOpen() = play("vault_open", rate = 1.0f, volume = 0.9f)

    fun playShopChime() = playPurchaseSuccess()

    fun playQuestReward() = playClaimReward()

    fun playCoinEarn() = playCoin()

    fun playGemEarn() = playClaimReward()

    fun playRewardClaim() = playClaimReward()

    fun playBoosterPop() = playPurchaseSuccess()

    fun playHighScoreFanfare() = play("highscore", rate = 1.0f, volume = 1.0f)

    // =========================================================================
    //  AUDIO CONTROLS (Mute, Volume, Synth Music & Lifecycle)
    // =========================================================================

    val currentMusicTheme: MusicTheme get() = musicPlayer.theme
    val isMusicPlaying: Boolean get() = musicPlayer.playing

    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        musicPlayer.setVolume(masterVolume * musicVolume)
        syncAndSaveSettings()
    }

    fun setVolume(volume: Float) = setMasterVolume(volume)

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        syncAndSaveSettings()
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        musicPlayer.setVolume(masterVolume * musicVolume)
        syncAndSaveSettings()
    }

    fun toggleMute(): Boolean {
        isMuted = !isMuted
        if (isMuted) {
            soundPool.autoPause()
            activeVoices.clear()
        } else {
            soundPool.autoResume()
        }
        musicPlayer.setMuted(isMuted || isMusicMuted)
        syncAndSaveSettings()
        return isMuted
    }

    fun setMuted(value: Boolean) {
        isMuted = value
        if (isMuted) {
            soundPool.autoPause()
            activeVoices.clear()
        } else {
            soundPool.autoResume()
        }
        musicPlayer.setMuted(isMuted || isMusicMuted)
        syncAndSaveSettings()
    }

    fun toggleSfxMute(): Boolean {
        isSfxMuted = !isSfxMuted
        if (isSfxMuted) {
            soundPool.autoPause()
            activeVoices.clear()
        } else {
            soundPool.autoResume()
        }
        syncAndSaveSettings()
        return isSfxMuted
    }

    fun setSfxMuted(value: Boolean) {
        isSfxMuted = value
        if (isSfxMuted) {
            soundPool.autoPause()
            activeVoices.clear()
        } else {
            soundPool.autoResume()
        }
        syncAndSaveSettings()
    }

    fun toggleMusicMute(): Boolean {
        isMusicMuted = !isMusicMuted
        musicPlayer.setMuted(isMuted || isMusicMuted)
        syncAndSaveSettings()
        return isMusicMuted
    }

    fun setMusicMuted(value: Boolean) {
        isMusicMuted = value
        musicPlayer.setMuted(isMuted || isMusicMuted)
        syncAndSaveSettings()
    }

    fun startMusic(theme: MusicTheme = MusicTheme.MEADOW_JOY) {
        musicPlayer.setTheme(theme)
        if (!isMuted && !isMusicMuted) {
            musicPlayer.resume()
        }
        syncAndSaveSettings()
    }

    fun playMusic(theme: MusicTheme = MusicTheme.MEADOW_JOY) = startMusic(theme)

    fun pauseMusic() {
        musicPlayer.pause()
    }

    fun resumeMusic() {
        if (!isMuted && !isMusicMuted) {
            musicPlayer.resume()
        }
    }

    fun stopMusic() {
        musicPlayer.pause()
    }

    fun setMusicTheme(theme: MusicTheme) {
        musicPlayer.setTheme(theme)
        syncAndSaveSettings()
    }

    fun setMusicThemeForZone(zoneNumber: Int, isBossStage: Boolean = false) {
        musicPlayer.setThemeForZone(zoneNumber, isBossStage)
        syncAndSaveSettings()
    }

    private fun syncAndSaveSettings() {
        _settings.value = SoundSettings(
            masterVolume = masterVolume,
            sfxVolume = sfxVolume,
            musicVolume = musicVolume,
            isMasterMuted = isMuted,
            isSfxMuted = isSfxMuted,
            isMusicMuted = isMusicMuted,
            currentMusicTheme = musicPlayer.theme
        )
        prefs?.edit()?.apply {
            putFloat("setting_master_volume", masterVolume)
            putFloat("setting_sfx_volume", sfxVolume)
            putFloat("setting_music_volume", musicVolume)
            putBoolean("setting_muted", isMuted)
            putBoolean("setting_sfx_muted", isSfxMuted)
            putBoolean("setting_music_muted", isMusicMuted)
            apply()
        }
    }

    fun pauseAll() {
        isPaused = true
        soundPool.autoPause()
        activeVoices.clear()
        musicPlayer.pause()
    }

    fun resumeAll() {
        isPaused = false
        if (!isMuted && !isSfxMuted) {
            soundPool.autoResume()
        }
        if (!isMuted && !isMusicMuted) {
            musicPlayer.resume()
        }
    }

    fun onActivityResume() {
        resumeAll()
        musicPlayer.onActivityResume()
    }

    fun onActivityPause() {
        pauseAll()
        musicPlayer.onActivityPause()
    }

    fun onActivityStop() {
        pauseAll()
        musicPlayer.onActivityStop()
    }

    fun release() {
        isReleased = true
        try {
            soundPool.release()
        } catch (_: Exception) {}
        try {
            fallbackAudioTrack?.stop()
            fallbackAudioTrack?.release()
            fallbackAudioTrack = null
        } catch (_: Exception) {}
        try {
            musicPlayer.release()
        } catch (_: Exception) {}
        mixerJob?.cancel()
        activeVoices.clear()
        pcmBuffers.clear()
        soundPoolIds.clear()
        loadedSoundIds.clear()
    }

    // =========================================================================
    //  PROCEDURAL WAVEFORM GENERATORS
    // =========================================================================

    private fun generatePopPcm(startFreq: Float, endFreq: Float, duration: Float): ShortArray {
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = startFreq + (endFreq - startFreq) * (t * t)
            val envelope = exp(-t * 12.0)
            val sample = sin(phase) * envelope
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sample * 28000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateGoldenPopPcm(): ShortArray {
        val duration = 0.28f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val popEnv = exp(-t * 22.0)
            val ringEnv = exp(-t * 7.0)
            // Golden pop: initial rubber snap + sparkling golden coin bell (987Hz & 1975Hz)
            val popSample = sin(phase1) * popEnv
            val bellSample = (sin(phase2) + 0.4 * sin(phase2 * 2.0)) * ringEnv
            val mixed = popSample * 0.6 + bellSample * 0.4
            phase1 += 2.0 * PI * (520.0 - 400.0 * t) / SAMPLE_RATE
            phase2 += 2.0 * PI * 987.77 / SAMPLE_RATE
            data[i] = (mixed * 27000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateRainbowPcm(): ShortArray {
        val duration = 0.35f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6 arpeggio

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val noteIdx = ((t * notes.size).toInt()).coerceIn(0, notes.size - 1)
            val targetFreq = notes[noteIdx]
            val env = sin(t * PI.toFloat()) * exp(-t * 2.0)
            val sample = (sin(phase) + 0.3 * sin(phase * 2.0)) * env
            phase += 2.0 * PI * targetFreq / SAMPLE_RATE
            data[i] = (sample * 25000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateBombPcm(): ShortArray {
        val duration = 0.38f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        var noise = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 160f * (1f - t * 0.8f)
            val subBass = sin(phase) * exp(-t * 5.0)
            noise = (noise * 0.85) + ((Random.nextDouble() * 2.0 - 1.0) * 0.15)
            val noiseEnv = exp(-t * 9.0)
            val sample = (subBass * 0.75 + noise * noiseEnv * 0.5)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sample * 30000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateFreezePcm(): ShortArray {
        val duration = 0.30f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 8.0)
            val crystal = (sin(phase1) + 0.5 * sin(phase2)) * env
            phase1 += 2.0 * PI * (1760.0 + 400.0 * sin(t * 12.0)) / SAMPLE_RATE
            phase2 += 2.0 * PI * (2637.0) / SAMPLE_RATE
            data[i] = (crystal * 24000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateHeartPopPcm(): ShortArray {
        val duration = 0.25f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 9.0)
            val sample = (sin(phase1) * 0.6 + sin(phase2) * 0.4) * env
            phase1 += 2.0 * PI * (659.25) / SAMPLE_RATE // E5
            phase2 += 2.0 * PI * (880.00) / SAMPLE_RATE // A5
            data[i] = (sample * 25000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateMultiplierPcm(): ShortArray {
        val duration = 0.28f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        val chord = doubleArrayOf(587.33, 739.99, 880.00, 1174.66) // D5, F#5, A5, D6

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val noteIdx = ((t * chord.size).toInt()).coerceIn(0, chord.size - 1)
            val freq = chord[noteIdx]
            val env = sin(t * PI.toFloat()) * 0.95
            val sample = sin(phase) * env
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sample * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateSplitterPopPcm(): ShortArray {
        // Two quick consecutive pops
        val duration = 0.16f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val splitPoint = (totalSamples * 0.45).toInt()

        for (i in 0 until totalSamples) {
            var sample = 0.0
            if (i < splitPoint) {
                val t = i.toFloat() / splitPoint
                sample = sin(phase1) * exp(-t * 14.0)
                phase1 += 2.0 * PI * (480.0 - 300.0 * t) / SAMPLE_RATE
            } else {
                val t = (i - splitPoint).toFloat() / (totalSamples - splitPoint)
                sample = sin(phase2) * exp(-t * 14.0)
                phase2 += 2.0 * PI * (620.0 - 400.0 * t) / SAMPLE_RATE
            }
            data[i] = (sample * 27000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateShieldCrackPcm(): ShortArray {
        val duration = 0.22f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 12.0)
            val metallic = (sin(phase) + 0.6 * sin(phase * 2.8) + (Random.nextDouble() * 0.4 - 0.2)) * env
            phase += 2.0 * PI * (800.0 - 500.0 * t) / SAMPLE_RATE
            data[i] = (metallic * 26000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateMegaBossDefeatPcm(): ShortArray {
        val duration = 0.65f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        var noise = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 3.5)
            val subBoom = sin(phase1) * env
            val crackle = (sin(phase2) + (Random.nextDouble() * 2.0 - 1.0) * 0.4) * exp(-t * 6.0)
            phase1 += 2.0 * PI * (90.0 - 50.0 * t) / SAMPLE_RATE
            phase2 += 2.0 * PI * (440.0 * (1.0 - t * 0.5)) / SAMPLE_RATE
            val sample = subBoom * 0.7 + crackle * 0.5
            data[i] = (sample * 31000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateClickPcm(): ShortArray {
        val totalSamples = (SAMPLE_RATE * 0.025f).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 1200f - 800f * t
            val envelope = (1f - t) * (1f - t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * envelope * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateTabSwitchPcm(): ShortArray {
        val duration = 0.06f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 600.0 + 400.0 * t
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 18000).toInt().toShort()
        }
        return data
    }

    private fun generatePurchaseFailPcm(): ShortArray {
        val duration = 0.16f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 8.0)
            phase += 2.0 * PI * 185.0 / SAMPLE_RATE // F#3 low buzzer
            data[i] = (sin(phase) * env * 22000).toInt().toShort()
        }
        return data
    }

    private fun generateEquipSnapPcm(): ShortArray {
        val duration = 0.07f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 1400.0 - 900.0 * t
            val env = exp(-t * 20.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 26000).toInt().toShort()
        }
        return data
    }

    private fun generateDialogOpenPcm(): ShortArray {
        val duration = 0.12f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 380.0 + 420.0 * t
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 20000).toInt().toShort()
        }
        return data
    }

    private fun generateDialogClosePcm(): ShortArray {
        val duration = 0.10f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 700.0 - 350.0 * t
            val env = (1.0 - t) * (1.0 - t)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 19000).toInt().toShort()
        }
        return data
    }

    private fun generateLevelSelectPcm(): ShortArray {
        val duration = 0.08f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 480.0 + 320.0 * (1.0 - exp(-t * 10.0))
            val env = exp(-t * 12.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateWheelTickPcm(): ShortArray {
        val totalSamples = (SAMPLE_RATE * 0.015f).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val envelope = (1f - t)
            phase += 2.0 * PI * 1800.0 / SAMPLE_RATE
            data[i] = (sin(phase) * envelope * 20000).toInt().toShort()
        }
        return data
    }

    private fun generateCoinPcm(): ShortArray {
        val duration = 0.14f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val half = totalSamples / 2

        for (i in 0 until totalSamples) {
            if (i < half) {
                val t = i.toFloat() / half
                val env = exp(-t * 6.0)
                val sample = sin(phase1) * env
                phase1 += 2.0 * PI * 987.77 / SAMPLE_RATE
                data[i] = (sample * 24000).toInt().toShort()
            } else {
                val t = (i - half).toFloat() / half
                val env = exp(-t * 6.0)
                val sample = sin(phase2) * env
                phase2 += 2.0 * PI * 1318.51 / SAMPLE_RATE
                data[i] = (sample * 24000).toInt().toShort()
            }
        }
        return data
    }

    private fun generateHammerSmashPcm(): ShortArray {
        val duration = 0.22f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 200f * (1f - t * 0.7f)
            val sub = sin(phase) * exp(-t * 8.0)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * exp(-t * 14.0) * 0.4
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = ((sub + noise) * 28000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateSlashPcm(): ShortArray {
        val duration = 0.09f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 1600f - 1100f * t
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.35
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = ((sin(phase) + noise) * env * 22000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateComboPcm(): ShortArray {
        val duration = 0.16f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 400.0 + 600.0 * (t * t)
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateStageClearPcm(): ShortArray {
        val duration = 0.45f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5-E5-G5-C6

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val idx = (t * notes.size).toInt().coerceIn(0, notes.size - 1)
            val env = sin((t * notes.size % 1.0) * PI) * exp(-t * 1.5)
            phase += 2.0 * PI * notes[idx] / SAMPLE_RATE
            data[i] = (sin(phase) * env * 25000).toInt().toShort()
        }
        return data
    }

    private fun generateRocketPcm(): ShortArray {
        val duration = 0.22f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 300.0 + 800.0 * (t * t)
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.25
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = ((sin(phase) + noise) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateZapPcm(): ShortArray {
        val duration = 0.12f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val noise = (Random.nextDouble() * 2.0 - 1.0)
            val env = exp(-t * 14.0)
            phase += 2.0 * PI * (1200.0 - 800.0 * t) / SAMPLE_RATE
            data[i] = ((sin(phase) * 0.4 + noise * 0.6) * env * 26000).toInt().toShort()
        }
        return data
    }

    private fun generateShieldHitPcm(): ShortArray {
        val duration = 0.12f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 16.0)
            phase += 2.0 * PI * 750.0 / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateMysteryPcm(): ShortArray {
        val duration = 0.25f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 700.0 + 350.0 * sin(t * 30.0)
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 22000).toInt().toShort()
        }
        return data
    }

    private fun generateGhostChimePcm(): ShortArray {
        val duration = 0.28f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 880.0 + 60.0 * sin(t * 18.0)
            val env = sin(t * PI.toFloat()) * exp(-t * 3.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 20000).toInt().toShort()
        }
        return data
    }

    private fun generatePiggyPcm(): ShortArray {
        val duration = 0.32f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val squeak = sin(phase1) * exp(-t * 6.0)
            val coin = sin(phase2) * exp(-t * 4.0)
            phase1 += 2.0 * PI * (600.0 + 400.0 * sin(t * 25.0)) / SAMPLE_RATE
            phase2 += 2.0 * PI * 1318.51 / SAMPLE_RATE
            data[i] = ((squeak * 0.5 + coin * 0.5) * 26000).toInt().toShort()
        }
        return data
    }

    private fun generateThunderPcm(): ShortArray {
        val duration = 0.40f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var noise = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            noise = noise * 0.9 + (Random.nextDouble() * 2.0 - 1.0) * 0.1
            val env = exp(-t * 4.0)
            data[i] = (noise * env * 30000).toInt().toShort()
        }
        return data
    }

    private fun generatePraisePcm(): ShortArray {
        val duration = 0.28f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 523.25 + 500.0 * t
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateStarChimePcm(starIndex: Int): ShortArray {
        val baseFreq = when (starIndex) {
            1 -> 523.25 // C5
            2 -> 659.25 // E5
            else -> 783.99 // G5
        }
        val duration = 0.20f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val env = exp(-t * 7.0)
            phase += 2.0 * PI * baseFreq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 25000).toInt().toShort()
        }
        return data
    }

    private fun generateMissPcm(): ShortArray {
        val duration = 0.14f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 220.0 - 90.0 * t
            val env = exp(-t * 10.0)
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 22000).toInt().toShort()
        }
        return data
    }

    private fun generateMissWarningPcm(): ShortArray {
        val duration = 0.20f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 330.0 + 80.0 * sin(t * 40.0)
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateVaultOpenPcm(): ShortArray {
        val duration = 0.30f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 300.0 + 600.0 * (t * t)
            val env = sin(t * PI.toFloat())
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sin(phase) * env * 24000).toInt().toShort()
        }
        return data
    }

    private fun generateShopChimePcm(): ShortArray {
        val duration = 0.24f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase1 = 0.0
        var phase2 = 0.0
        val half = totalSamples / 2

        for (i in 0 until totalSamples) {
            if (i < half) {
                val t = i.toFloat() / half
                val env = exp(-t * 6.0)
                phase1 += 2.0 * PI * 880.0 / SAMPLE_RATE
                data[i] = (sin(phase1) * env * 24000).toInt().toShort()
            } else {
                val t = (i - half).toFloat() / half
                val env = exp(-t * 5.0)
                phase2 += 2.0 * PI * 1320.0 / SAMPLE_RATE
                data[i] = (sin(phase2) * env * 26000).toInt().toShort()
            }
        }
        return data
    }

    private fun generateQuestRewardPcm(): ShortArray {
        val duration = 0.36f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        val notes = doubleArrayOf(587.33, 739.99, 880.00, 1174.66) // D5-F#5-A5-D6

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val idx = (t * notes.size).toInt().coerceIn(0, notes.size - 1)
            val env = sin((t * notes.size % 1.0) * PI) * exp(-t * 1.5)
            phase += 2.0 * PI * notes[idx] / SAMPLE_RATE
            data[i] = (sin(phase) * env * 26000).toInt().toShort()
        }
        return data
    }

    private fun generateHighScoreFanfarePcm(): ShortArray {
        val duration = 0.50f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // C5-E5-G5-C6-E6-G6

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val noteIdx = ((t * notes.size).toInt()).coerceIn(0, notes.size - 1)
            val targetFreq = notes[noteIdx]
            val subT = (t * notes.size) % 1.0
            val env = sin(subT * PI) * exp(-t * 1.8)
            val sample = (sin(phase) + 0.35 * sin(phase * 2.0)) * env
            phase += 2.0 * PI * targetFreq / SAMPLE_RATE
            data[i] = (sample * 27000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    private fun generateLevelWarpPcm(): ShortArray {
        val duration = 0.42f
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val data = ShortArray(totalSamples)
        var phase = 0.0
        for (i in 0 until totalSamples) {
            val t = i.toFloat() / totalSamples
            val freq = 200.0 + 1200.0 * (t * t * t)
            val env = sin(t * PI.toFloat())
            val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.15
            val sample = (sin(phase) + noise) * env
            phase += 2.0 * PI * freq / SAMPLE_RATE
            data[i] = (sample * 27000).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return data
    }

    // =========================================================================
    //  FALLBACK AUDIO TRACK MIXER LOOP
    // =========================================================================

    private fun ensureFallbackAudioTrack(): AudioTrack? {
        if (isReleased) return null
        val existing = fallbackAudioTrack
        if (existing != null && existing.state == AudioTrack.STATE_INITIALIZED) {
            return existing
        }
        return try {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(2048)

            val track = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            fallbackAudioTrack = track
            track
        } catch (_: Exception) {
            null
        }
    }

    private fun startFallbackMixerLoop() {
        mixerJob?.cancel()
        mixerJob = scope.launch(Dispatchers.IO) {
            val bufferSize = 512
            val chunk = ShortArray(bufferSize)

            while (isActive && !isReleased) {
                if (isPaused || isMuted || activeVoices.isEmpty() || masterVolume <= 0.001f) {
                    delay(32)
                    continue
                }

                val currentTrack = fallbackAudioTrack ?: ensureFallbackAudioTrack()
                if (currentTrack == null) {
                    delay(50)
                    continue
                }

                try {
                    if (currentTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
                        currentTrack.play()
                    }
                } catch (_: Exception) {}

                chunk.fill(0)
                var hasSound = false

                val iterator = activeVoices.iterator()
                while (iterator.hasNext()) {
                    val voice = iterator.next()
                    val pcm = voice.pcm
                    var pos = voice.position
                    val step = voice.step
                    val vol = voice.volume * masterVolume

                    var voiceDone = false
                    for (i in 0 until bufferSize) {
                        val intPos = pos.toInt()
                        if (intPos >= pcm.size) {
                            voiceDone = true
                            break
                        }
                        hasSound = true
                        val sample = (pcm[intPos] * vol).toInt()
                        val current = chunk[i].toInt()
                        chunk[i] = (current + sample).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        pos += step
                    }

                    voice.position = pos
                    if (voiceDone || pos >= pcm.size) {
                        activeVoices.remove(voice)
                    }
                }

                if (hasSound) {
                    try {
                        currentTrack.write(chunk, 0, bufferSize)
                    } catch (_: Exception) {
                        delay(10)
                    }
                } else {
                    delay(10)
                }
            }
        }
    }
}
