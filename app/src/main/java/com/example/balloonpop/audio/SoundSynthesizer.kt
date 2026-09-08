package com.example.balloonpop.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundSynthesizer(context: Context) {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(16)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds = mutableMapOf<String, Int>()
    private var muted: Boolean = false
    val isMuted: Boolean get() = muted
    var masterVolume: Float = 1.0f

    init {
        scope.launch {
            try {
                pregenerateAndLoadSounds(context)
            } catch (_: Exception) {
            }
        }
    }

    private fun pregenerateAndLoadSounds(context: Context) {
        val cacheDir = context.cacheDir

        // 1. Pop sounds with slightly varying pitches
        val popPcm1 = generatePopPcm(420f, 80f, 0.08f)
        val popFile1 = writeWavFile(cacheDir, "pop1.wav", popPcm1)
        soundIds["pop1"] = soundPool.load(popFile1.absolutePath, 1)

        val popPcm2 = generatePopPcm(350f, 70f, 0.09f)
        val popFile2 = writeWavFile(cacheDir, "pop2.wav", popPcm2)
        soundIds["pop2"] = soundPool.load(popFile2.absolutePath, 1)

        val popPcm3 = generatePopPcm(520f, 100f, 0.07f)
        val popFile3 = writeWavFile(cacheDir, "pop3.wav", popPcm3)
        soundIds["pop3"] = soundPool.load(popFile3.absolutePath, 1)

        // 2. Hammer Smash Sound
        val hammerPcm = generateHammerSmashPcm()
        val hammerFile = writeWavFile(cacheDir, "hammer.wav", hammerPcm)
        soundIds["hammer"] = soundPool.load(hammerFile.absolutePath, 1)

        // 3. Coin Chime
        val coinPcm = generateCoinPcm()
        val coinFile = writeWavFile(cacheDir, "coin.wav", coinPcm)
        soundIds["coin"] = soundPool.load(coinFile.absolutePath, 1)

        // 4. Slash / Swipe
        val slashPcm = generateSlashPcm()
        val slashFile = writeWavFile(cacheDir, "slash.wav", slashPcm)
        soundIds["slash"] = soundPool.load(slashFile.absolutePath, 1)

        // 5. Bomb Blast
        val bombPcm = generateBombPcm()
        val bombFile = writeWavFile(cacheDir, "bomb.wav", bombPcm)
        soundIds["bomb"] = soundPool.load(bombFile.absolutePath, 1)

        // 6. Freeze chime
        val freezePcm = generateFreezePcm()
        val freezeFile = writeWavFile(cacheDir, "freeze.wav", freezePcm)
        soundIds["freeze"] = soundPool.load(freezeFile.absolutePath, 1)

        // 7. Click
        val clickPcm = generateClickPcm()
        val clickFile = writeWavFile(cacheDir, "click.wav", clickPcm)
        soundIds["click"] = soundPool.load(clickFile.absolutePath, 1)

        // 8. Combo fanfare
        val comboPcm = generateComboPcm()
        val comboFile = writeWavFile(cacheDir, "combo.wav", comboPcm)
        soundIds["combo"] = soundPool.load(comboFile.absolutePath, 1)

        // 9. Stage Clear Win
        val winPcm = generateStageClearPcm()
        val winFile = writeWavFile(cacheDir, "win.wav", winPcm)
        soundIds["win"] = soundPool.load(winFile.absolutePath, 1)

        // 10. Rocket Swoosh
        val rocketPcm = generateRocketPcm()
        val rocketFile = writeWavFile(cacheDir, "rocket.wav", rocketPcm)
        soundIds["rocket"] = soundPool.load(rocketFile.absolutePath, 1)

        // 11. Electric Zap
        val zapPcm = generateZapPcm()
        val zapFile = writeWavFile(cacheDir, "zap.wav", zapPcm)
        soundIds["zap"] = soundPool.load(zapFile.absolutePath, 1)

        // 12. Shield Hit / Crack
        val shieldPcm = generateShieldPcm()
        val shieldFile = writeWavFile(cacheDir, "shield.wav", shieldPcm)
        soundIds["shield"] = soundPool.load(shieldFile.absolutePath, 1)

        // 13. Mystery Chime
        val mysteryPcm = generateMysteryPcm()
        val mysteryFile = writeWavFile(cacheDir, "mystery.wav", mysteryPcm)
        soundIds["mystery"] = soundPool.load(mysteryFile.absolutePath, 1)

        // 14. Piggy Jackpot
        val piggyPcm = generatePiggyPcm()
        val piggyFile = writeWavFile(cacheDir, "piggy.wav", piggyPcm)
        soundIds["piggy"] = soundPool.load(piggyFile.absolutePath, 1)
    }

    fun toggleMute(): Boolean {
        muted = !muted
        return muted
    }

    fun setMuted(value: Boolean) {
        muted = value
    }

    fun setVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
    }

    private fun play(key: String, rate: Float = 1.0f, volume: Float = 1.0f) {
        if (isMuted || masterVolume <= 0f) return
        val id = soundIds[key] ?: return
        val finalVol = (volume * masterVolume).coerceIn(0f, 1f)
        try {
            soundPool.play(id, finalVol, finalVol, 1, 0, rate.coerceIn(0.5f, 2.0f))
        } catch (_: Exception) {}
    }

    fun playPop(radius: Float = 35f) {
        val keys = listOf("pop1", "pop2", "pop3")
        val chosen = keys.random()
        val rate = 0.85f + (60f - radius.coerceIn(20f, 60f)) / 60f * 0.4f
        play(chosen, rate = rate, volume = 0.9f)
    }

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

    fun playCoin() {
        play("coin", rate = 1.0f, volume = 0.85f)
    }

    fun playSlash() {
        play("slash", rate = 1.0f, volume = 0.5f)
    }

    fun playBomb() {
        play("bomb", rate = 1.0f, volume = 1.0f)
    }

    fun playFreeze() {
        play("freeze", rate = 1.0f, volume = 0.9f)
    }

    fun playClick() {
        play("click", rate = 1.0f, volume = 0.6f)
    }

    fun playCombo(comboLevel: Int) {
        val rate = (1.0f + (comboLevel.coerceAtMost(8) - 2) * 0.1f)
        play("combo", rate = rate, volume = 0.95f)
    }

    fun playStageClear() {
        play("win", rate = 1.0f, volume = 1.0f)
    }

    fun playWheelTick() {
        play("click", rate = 1.6f, volume = 0.4f)
    }

    fun playRocketSwoosh() {
        play("rocket", rate = 1.0f, volume = 0.85f)
    }

    fun playZap() {
        play("zap", rate = 1.0f, volume = 0.90f)
    }

    fun playShieldHit() {
        play("shield", rate = 1.2f, volume = 0.85f)
    }

    fun playShieldCrack() {
        play("shield", rate = 0.85f, volume = 1.0f)
    }

    fun playMystery() {
        play("mystery", rate = 1.0f, volume = 0.90f)
    }

    fun playGhostChime() {
        play("mystery", rate = 1.45f, volume = 0.85f)
    }

    fun playPiggyJackpot() {
        play("piggy", rate = 1.0f, volume = 1.0f)
    }

    // --- Waveform Generators ---

    private fun generatePopPcm(startFreq: Float, endFreq: Float, duration: Float): ShortArray {
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = startFreq * (1f - progress) + endFreq * progress
            val envelope = (1f - progress) * (1f - progress)
            val sine = sin(2.0 * PI * freq * t).toFloat()
            val noise = (Math.random().toFloat() * 2f - 1f) * 0.15f * exp(-progress * 6.0).toFloat()
            val sample = ((sine * 0.85f + noise) * envelope).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE * 0.9f).toInt().toShort()
        }
        return pcm
    }

    private fun generateHammerSmashPcm(): ShortArray {
        val duration = 0.18f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val envelope = exp(-progress * 9.0).toFloat()
            val whiteNoise = (Math.random().toFloat() * 2f - 1f)
            lastNoise += 0.25f * (whiteNoise - lastNoise)
            val thudFreq = 160f * (1f - progress * 0.7f)
            val thud = sin(2.0 * PI * thudFreq * t).toFloat() * 0.7f
            val sample = ((lastNoise * 0.5f + thud) * envelope).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateCoinPcm(): ShortArray {
        val duration = 0.22f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val f1 = 987.77f // B5
        val f2 = 1318.51f // E6
        val splitSample = (sampleRate * 0.05f).toInt()
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            var sample = 0f
            if (i < splitSample) {
                val env1 = (1f - (i.toFloat() / splitSample)).coerceIn(0f, 1f)
                sample += sin(2.0 * PI * f1 * t).toFloat() * env1 * 0.5f
            } else {
                val t2 = (i - splitSample).toFloat() / sampleRate
                val env2 = exp(-t2 * 14.0).toFloat()
                sample += sin(2.0 * PI * f2 * t2).toFloat() * env2 * 0.6f
            }
            pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE * 0.85f).toInt().toShort()
        }
        return pcm
    }

    private fun generateSlashPcm(): ShortArray {
        val duration = 0.07f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 1400f * (1f - progress) + 300f * progress
            val envelope = (1f - progress)
            val phase = (t * freq) % 1.0f
            val saw = (phase * 2f - 1f)
            val sample = (saw * envelope * 0.35f).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateBombPcm(): ShortArray {
        val duration = 0.35f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        var lastSample = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val envelope = exp(-progress * 6.5).toFloat()
            val whiteNoise = (Math.random().toFloat() * 2f - 1f)
            val cutoffAlpha = 0.15f * (1f - progress * 0.7f)
            lastSample += cutoffAlpha * (whiteNoise - lastSample)
            val subBass = sin(2.0 * PI * (70f * (1f - progress * 0.5f)) * t).toFloat() * 0.4f
            val sample = ((lastSample * 0.85f + subBass) * envelope).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE * 0.95f).toInt().toShort()
        }
        return pcm
    }

    private fun generateFreezePcm(): ShortArray {
        val duration = 0.35f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val freqs = floatArrayOf(1046.5f, 1318.5f, 1567.9f, 2093.0f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val envelope = exp(-progress * 5.0).toFloat()
            var s = 0f
            for (f in freqs) {
                s += sin(2.0 * PI * f * t).toFloat() * 0.2f
            }
            pcm[i] = ((s * envelope).coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateClickPcm(): ShortArray {
        val duration = 0.025f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 1200f * (1f - progress) + 400f * progress
            val envelope = 1f - progress
            val sample = sin(2.0 * PI * freq * t).toFloat() * envelope * 0.4f
            pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateComboPcm(): ShortArray {
        val duration = 0.20f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val f1 = 523.25f // C5
        val f2 = 659.25f // E5
        val f3 = 783.99f // G5
        val f4 = 1046.50f // C6
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val stepIdx = (i / step).coerceIn(0, 3)
            val freq = when (stepIdx) {
                0 -> f1
                1 -> f2
                2 -> f3
                else -> f4
            }
            val localT = (i % step).toFloat() / sampleRate
            val env = exp(-localT * 12.0).toFloat()
            val sample = sin(2.0 * PI * freq * t).toFloat() * env * 0.6f
            pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateStageClearPcm(): ShortArray {
        val duration = 0.45f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val notes = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f)
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val idx = (i / step).coerceIn(0, 3)
            val freq = notes[idx]
            val localT = (i % step).toFloat() / sampleRate
            val env = exp(-localT * 8.0).toFloat()
            val sample = (sin(2.0 * PI * freq * t).toFloat() + sin(2.0 * PI * (freq * 1.5f) * t).toFloat() * 0.3f) * env * 0.6f
            pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateRocketPcm(): ShortArray {
        val duration = 0.28f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        var lastNoise = 0f
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 300f + progress * 800f
            val env = sin(PI * progress).toFloat()
            val whiteNoise = (Math.random().toFloat() * 2f - 1f)
            lastNoise += 0.2f * (whiteNoise - lastNoise)
            val swoosh = sin(2.0 * PI * freq * t).toFloat() * 0.4f
            val sample = ((lastNoise * 0.6f + swoosh) * env).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE * 0.9f).toInt().toShort()
        }
        return pcm
    }

    private fun generateZapPcm(): ShortArray {
        val duration = 0.22f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val freq = 1200f * (1f - progress * 0.6f) + (Math.random().toFloat() * 400f - 200f)
            val env = exp(-progress * 8.0).toFloat()
            val saw = ((t * freq % 1.0f) * 2f - 1f)
            val sample = (saw * env * 0.6f).coerceIn(-1f, 1f)
            pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generateShieldPcm(): ShortArray {
        val duration = 0.18f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val metallicFreqs = floatArrayOf(880f, 1760f, 2640f, 3520f)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val progress = t / duration
            val env = exp(-progress * 14.0).toFloat()
            var s = 0f
            for (f in metallicFreqs) {
                s += sin(2.0 * PI * f * t).toFloat() * 0.25f
            }
            pcm[i] = ((s * env).coerceIn(-1f, 1f) * Short.MAX_VALUE * 0.9f).toInt().toShort()
        }
        return pcm
    }

    private fun generateMysteryPcm(): ShortArray {
        val duration = 0.35f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val notes = floatArrayOf(587.33f, 739.99f, 880.00f, 1174.66f) // D5, F#5, A5, D6
        val step = numSamples / 4
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val idx = (i / step).coerceIn(0, 3)
            val freq = notes[idx]
            val localT = (i % step).toFloat() / sampleRate
            val env = exp(-localT * 9.0).toFloat()
            val sample = sin(2.0 * PI * freq * t).toFloat() * env * 0.7f
            pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    private fun generatePiggyPcm(): ShortArray {
        val duration = 0.40f
        val numSamples = (sampleRate * duration).toInt()
        val pcm = ShortArray(numSamples)
        val coinFreqs = floatArrayOf(1046.5f, 1318.5f, 1567.9f, 2093.0f, 2637.0f)
        val step = numSamples / coinFreqs.size
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            val idx = (i / step).coerceIn(0, coinFreqs.size - 1)
            val freq = coinFreqs[idx]
            val localT = (i % step).toFloat() / sampleRate
            val env = exp(-localT * 15.0).toFloat()
            val sample = sin(2.0 * PI * freq * t).toFloat() * env * 0.75f
            pcm[i] = (sample.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return pcm
    }

    // --- WAV file writer ---
    private fun writeWavFile(dir: File, filename: String, pcmData: ShortArray): File {
        val file = File(dir, filename)
        if (file.exists()) file.delete()
        FileOutputStream(file).use { out ->
            val totalAudioLen = pcmData.size * 2
            val totalDataLen = totalAudioLen + 36
            val byteRate = sampleRate * 2

            val header = ByteBuffer.allocate(44).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                put("RIFF".toByteArray())
                putInt(totalDataLen)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16) // Subchunk1Size (16 for PCM)
                putShort(1) // AudioFormat (1 for PCM)
                putShort(1) // NumChannels (1 = Mono)
                putInt(sampleRate)
                putInt(byteRate)
                putShort(2) // BlockAlign
                putShort(16) // BitsPerSample
                put("data".toByteArray())
                putInt(totalAudioLen)
            }.array()

            out.write(header)

            val pcmBytes = ByteBuffer.allocate(pcmData.size * 2).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                for (s in pcmData) {
                    putShort(s)
                }
            }.array()

            out.write(pcmBytes)
        }
        return file
    }
}
