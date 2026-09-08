package com.example.balloonpop.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

enum class MusicTheme(val title: String) {
    MEADOW_JOY("Sunny Meadow Joy 🌼"),
    TROPICAL_BOP("Tropical Island Bop 🏝️"),
    COSMIC_DREAM("Cosmic Starlight Synth 🌌"),
    CYBER_PULSE("Cyber Arcade Energy ⚡"),
    BOSS_ANTHEM("Epic Boss Anthem 👑")
}

class BackgroundMusicPlayer {

    private val sampleRate = 22050 // Low CPU footprint, smooth soft audio synthesis
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var musicJob: Job? = null

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var isMuted = false
    private var volume = 0.40f

    private var currentTheme: MusicTheme = MusicTheme.MEADOW_JOY
    private val pregeneratedTracks = mutableMapOf<MusicTheme, ShortArray>()

    init {
        scope.launch(Dispatchers.Default) {
            // Generate primary starter track first so music starts instantly without blocking
            try {
                pregeneratedTracks[MusicTheme.MEADOW_JOY] = generateTrackPcm(MusicTheme.MEADOW_JOY)
            } catch (_: Exception) {}
            startMusicLoop()

            // Lazily generate remaining themes in background with low contention
            for (theme in MusicTheme.values()) {
                if (theme == MusicTheme.MEADOW_JOY) continue
                try {
                    delay(50)
                    pregeneratedTracks[theme] = generateTrackPcm(theme)
                } catch (_: Exception) {}
            }
        }
    }

    fun setTheme(theme: MusicTheme) {
        if (currentTheme == theme) return
        currentTheme = theme
    }

    fun setThemeForZone(zoneNumber: Int, isBossStage: Boolean) {
        val theme = if (isBossStage) {
            MusicTheme.BOSS_ANTHEM
        } else {
            when (zoneNumber % 5) {
                1 -> MusicTheme.MEADOW_JOY
                2 -> MusicTheme.COSMIC_DREAM
                3 -> MusicTheme.TROPICAL_BOP
                4 -> MusicTheme.CYBER_PULSE
                else -> MusicTheme.BOSS_ANTHEM
            }
        }
        setTheme(theme)
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun pause() {
        isPlaying = false
        try {
            audioTrack?.pause()
        } catch (_: Exception) {}
    }

    fun resume() {
        if (!isPlaying) {
            isPlaying = true
            try {
                audioTrack?.play()
            } catch (_: Exception) {}
        }
    }

    private fun startMusicLoop() {
        musicJob?.cancel()
        musicJob = scope.launch(Dispatchers.IO) {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isPlaying = true

            val chunk = ShortArray(1024)
            var bufferPos = 0

            while (isActive) {
                if (!isPlaying || isMuted || volume <= 0.01f) {
                    delay(80)
                    continue
                }

                val trackData = pregeneratedTracks[currentTheme]
                if (trackData == null || trackData.isEmpty()) {
                    delay(50)
                    continue
                }

                val curVol = volume

                for (i in chunk.indices) {
                    val sample = trackData[bufferPos % trackData.size]
                    chunk[i] = (sample * curVol).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    bufferPos++
                }

                try {
                    audioTrack?.write(chunk, 0, chunk.size)
                } catch (_: Exception) {
                    delay(30)
                }
            }
        }
    }

    // --- Melodic procedural music generators with soft tones and gentle rhythms ---
    private fun generateTrackPcm(theme: MusicTheme): ShortArray {
        val totalSeconds = 12.0f // 12-second seamless loops
        val totalSamples = (sampleRate * totalSeconds).toInt()
        val rawBuffer = FloatArray(totalSamples)

        when (theme) {
            MusicTheme.MEADOW_JOY -> generateMeadowJoy(rawBuffer, totalSamples, totalSeconds)
            MusicTheme.TROPICAL_BOP -> generateTropicalBop(rawBuffer, totalSamples, totalSeconds)
            MusicTheme.COSMIC_DREAM -> generateCosmicDream(rawBuffer, totalSamples, totalSeconds)
            MusicTheme.CYBER_PULSE -> generateCyberPulse(rawBuffer, totalSamples, totalSeconds)
            MusicTheme.BOSS_ANTHEM -> generateBossAnthem(rawBuffer, totalSamples, totalSeconds)
        }

        // Apply smooth single-pole lowpass filter for warm, silky analog acoustics (eliminates harsh high frequencies)
        val filtered = FloatArray(totalSamples)
        val cutoff = 3000.0f
        val rc = 1.0f / (2.0f * PI.toFloat() * cutoff)
        val dt = 1.0f / sampleRate
        val alpha = dt / (rc + dt)
        var prev = 0f
        for (i in 0 until totalSamples) {
            prev += alpha * (rawBuffer[i] - prev)
            filtered[i] = prev
        }

        // Seamless loop crossfade on last 0.4 seconds
        val crossfadeSamples = (sampleRate * 0.4f).toInt()
        for (i in 0 until crossfadeSamples) {
            val tFade = i.toFloat() / crossfadeSamples
            val endIdx = totalSamples - crossfadeSamples + i
            filtered[i] = filtered[i] * tFade + filtered[endIdx] * (1f - tFade)
        }

        // Convert to 16-bit PCM with comfortable master headroom (soft, pleasant level)
        val pcm = ShortArray(totalSamples)
        val peakAmp = (Short.MAX_VALUE * 0.38f)
        for (i in 0 until totalSamples) {
            pcm[i] = (filtered[i].coerceIn(-1f, 1f) * peakAmp).toInt().toShort()
        }

        return pcm
    }

    // Helper: Soft envelope pluck (no click, soft 20ms attack, exponential decay)
    private fun softPluck(t: Float, noteT: Float, freq: Float, decayRate: Float = 5.0f, overtone: Float = 0.10f): Float {
        val attack = (noteT / 0.022f).coerceIn(0f, 1f)
        val decay = exp(-noteT * decayRate).toFloat()
        val sine1 = sin(2.0 * PI * freq * t).toFloat()
        val sine2 = sin(4.0 * PI * freq * t).toFloat()
        return (sine1 + overtone * sine2) * attack * decay
    }

    // Helper: Soft warm Rhodes electric piano chord note
    private fun softRhodes(t: Float, noteT: Float, freq: Float, duration: Float): Float {
        val attack = (noteT / 0.030f).coerceIn(0f, 1f)
        val decay = exp(-noteT * (2.8f / duration.coerceAtLeast(0.4f))).toFloat()
        val fundamental = sin(2.0 * PI * freq * t).toFloat()
        val sub = sin(2.0 * PI * (freq * 0.5) * t).toFloat() * 0.12f
        val shimmer = sin(2.0 * PI * (freq * 2.0) * t).toFloat() * 0.08f
        return (fundamental + sub + shimmer) * attack * decay
    }

    // Helper: Soft warm heartbeat kick (pure sine pitch sweep 95Hz -> 48Hz with fast decay, no harsh click)
    private fun softWarmKick(noteT: Float): Float {
        if (noteT > 0.35f) return 0f
        val pitch = 48f + 47f * exp(-noteT * 22f).toFloat()
        val attack = (noteT / 0.008f).coerceIn(0f, 1f)
        val decay = exp(-noteT * 14.0f).toFloat()
        return sin(2.0 * PI * pitch * noteT).toFloat() * attack * decay * 0.35f
    }

    // Helper: Delicate acoustic shaker/chime tap
    private fun softChimeTap(noteT: Float): Float {
        if (noteT > 0.08f) return 0f
        val attack = (noteT / 0.004f).coerceIn(0f, 1f)
        val decay = exp(-noteT * 60f).toFloat()
        return sin(2.0 * PI * 2600.0 * noteT).toFloat() * attack * decay * 0.05f
    }

    // 1. MEADOW JOY: Peaceful, sunny Lo-Fi Kalimba & Soft Rhodes progression (Cmaj7 - Fmaj7 - G6 - Am7)
    private fun generateMeadowJoy(buf: FloatArray, totalSamples: Int, totalSeconds: Float) {
        val melodyNotes = floatArrayOf(
            523.25f, 659.25f, 783.99f, 659.25f, 587.33f, 523.25f, 440.00f, 523.25f,
            659.25f, 783.99f, 880.00f, 783.99f, 659.25f, 587.33f, 523.25f, 392.00f,
            440.00f, 523.25f, 659.25f, 523.25f, 587.33f, 659.25f, 783.99f, 880.00f,
            659.25f, 587.33f, 523.25f, 440.00f, 392.00f, 440.00f, 523.25f, 523.25f
        )
        // 4 chords across 12s (3s each): Cmaj7, Fmaj7, G6, Am7
        val chordBases = arrayOf(
            floatArrayOf(261.63f, 329.63f, 392.00f, 493.88f), // Cmaj7
            floatArrayOf(174.61f, 220.00f, 261.63f, 329.63f), // Fmaj7
            floatArrayOf(196.00f, 246.94f, 293.66f, 329.63f), // G6
            floatArrayOf(220.00f, 261.63f, 329.63f, 392.00f)  // Am7
        )

        val noteDuration = totalSamples.toFloat() / melodyNotes.size
        val chordDuration = totalSamples.toFloat() / chordBases.size
        val beatDuration = sampleRate * 0.75f // 80 BPM gentle groove

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate

            // 1. Soft Kalimba Lead
            val noteIdx = (i / noteDuration).toInt().coerceIn(0, melodyNotes.size - 1)
            val noteT = (i % noteDuration) / sampleRate
            val noteFreq = melodyNotes[noteIdx]
            val lead = softPluck(t, noteT, noteFreq, decayRate = 5.2f, overtone = 0.08f) * 0.32f

            // 2. Soft Rhodes Pad Chords
            val chordIdx = (i / chordDuration).toInt().coerceIn(0, chordBases.size - 1)
            val chordT = (i % chordDuration) / sampleRate
            val chord = chordBases[chordIdx]
            var padMix = 0f
            for (f in chord) {
                padMix += softRhodes(t, chordT, f, chordDuration / sampleRate)
            }
            val pad = padMix * 0.07f

            // 3. Gentle Lo-Fi Beat (Kick on beat 1, delicate chime tap on beat 2)
            val beatT = (i % beatDuration) / sampleRate
            val kick = softWarmKick(beatT)
            val tapT = ((i + beatDuration * 0.5f) % beatDuration) / sampleRate
            val tap = softChimeTap(tapT)

            buf[i] = (lead + pad + kick + tap)
        }
    }

    // 2. TROPICAL BOP: Gentle Bossa Marimba, mellow acoustic bass, warm island breeze
    private fun generateTropicalBop(buf: FloatArray, totalSamples: Int, totalSeconds: Float) {
        val melodyNotes = floatArrayOf(
            349.23f, 440.00f, 523.25f, 440.00f, 392.00f, 349.23f, 440.00f, 523.25f,
            587.33f, 523.25f, 440.00f, 392.00f, 349.23f, 392.00f, 440.00f, 523.25f
        )
        val bassNotes = floatArrayOf(174.61f, 220.00f, 130.81f, 196.00f)

        val noteDur = totalSamples.toFloat() / (melodyNotes.size * 2)
        val bassDur = totalSamples.toFloat() / (bassNotes.size * 4)
        val beatDur = sampleRate * 0.5f // Relaxed 120 BPM Bossa Tap

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate

            // Soft Marimba Pluck
            val noteIdx = ((i / noteDur).toInt()) % melodyNotes.size
            val noteT = (i % noteDur) / sampleRate
            val freq = melodyNotes[noteIdx]
            val marimba = softPluck(t, noteT, freq, decayRate = 7.0f, overtone = 0.12f) * 0.30f

            // Smooth acoustic upright bass
            val bassIdx = ((i / bassDur).toInt()) % bassNotes.size
            val bassT = (i % bassDur) / sampleRate
            val bFreq = bassNotes[bassIdx]
            val bass = softRhodes(t, bassT, bFreq, bassDur / sampleRate) * 0.28f

            // Gentle soft rhythm tap
            val tapT = (i % beatDur) / sampleRate
            val tap = softChimeTap(tapT)

            buf[i] = (marimba + bass + tap)
        }
    }

    // 3. COSMIC DREAM: Ethereal Celeste Bells, warm floating space pads, soft starlight rhythm
    private fun generateCosmicDream(buf: FloatArray, totalSamples: Int, totalSeconds: Float) {
        val bellArpeggios = floatArrayOf(523.25f, 659.25f, 783.99f, 1046.50f, 880.00f, 659.25f, 587.33f, 783.99f)
        val bellDur = sampleRate * 0.40f

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            val slowLfo = sin(2.0 * PI * (t / 12.0f)).toFloat()

            // Dreamy warm sine pad (rich consonant harmonics)
            val f1 = 220f + slowLfo * 4f
            val f2 = 277.18f + slowLfo * 3f
            val f3 = 329.63f + slowLfo * 2f
            val pad = (sin(2.0 * PI * f1 * t) + sin(2.0 * PI * f2 * t) + sin(2.0 * PI * f3 * t)).toFloat() * 0.12f

            // Twinkling Music Box / Celeste Bell
            val bellIdx = ((i / bellDur).toInt()) % bellArpeggios.size
            val bellT = (i % bellDur) / sampleRate
            val bellFreq = bellArpeggios[bellIdx]
            val bell = softPluck(t, bellT, bellFreq, decayRate = 4.0f, overtone = 0.06f) * 0.24f

            // Deep soothing sub-bass pulse
            val sub = sin(2.0 * PI * 110.0 * t).toFloat() * 0.10f

            buf[i] = (pad + bell + sub)
        }
    }

    // 4. CYBER PULSE: Smooth Retrowave Chill, warm tape-saturator bass, soft neon arpeggio
    private fun generateCyberPulse(buf: FloatArray, totalSamples: Int, totalSeconds: Float) {
        val synthNotes = floatArrayOf(
            440.00f, 523.25f, 659.25f, 523.25f, 392.00f, 440.00f, 523.25f, 440.00f,
            349.23f, 440.00f, 523.25f, 440.00f, 392.00f, 493.88f, 587.33f, 493.88f
        )
        val noteDur = totalSamples.toFloat() / synthNotes.size
        val bassDur = sampleRate * 0.375f

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate

            // Warm mellow synth lead (smooth sine envelope, no harsh saws)
            val sIdx = ((i / noteDur).toInt()) % synthNotes.size
            val sT = (i % noteDur) / sampleRate
            val sFreq = synthNotes[sIdx]
            val lead = softPluck(t, sT, sFreq, decayRate = 5.0f, overtone = 0.14f) * 0.28f

            // Smooth 80s bassline
            val bT = (i % bassDur) / sampleRate
            val bFreq = 110.0f * (if ((i / (sampleRate * 3)) % 2 == 0) 1.0f else 0.89f)
            val bass = softRhodes(t, bT, bFreq.toFloat(), bassDur / sampleRate) * 0.22f

            // Soft heartbeat kick
            val kick = softWarmKick(bT)

            buf[i] = (lead + bass + kick)
        }
    }

    // 5. BOSS ANTHEM: Heroic yet smooth Orchestral Waltz, warm choir pad, gentle timpani pulse
    private fun generateBossAnthem(buf: FloatArray, totalSamples: Int, totalSeconds: Float) {
        val themeNotes = floatArrayOf(
            293.66f, 349.23f, 440.00f, 523.25f, 440.00f, 349.23f,
            329.63f, 392.00f, 493.88f, 587.33f, 493.88f, 392.00f,
            261.63f, 329.63f, 392.00f, 523.25f, 392.00f, 329.63f,
            220.00f, 261.63f, 329.63f, 440.00f, 329.63f, 261.63f
        )
        val noteDur = totalSamples.toFloat() / themeNotes.size
        val beatDur = sampleRate * 0.5f

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate

            // Warm horn & bell lead
            val tIdx = ((i / noteDur).toInt()) % themeNotes.size
            val tT = (i % noteDur) / sampleRate
            val tFreq = themeNotes[tIdx]
            val lead = softPluck(t, tT, tFreq, decayRate = 4.5f, overtone = 0.10f) * 0.32f

            // Rich warm cello pad
            val padFreq = 146.83f + sin(t * 0.5f).toFloat() * 10f
            val pad = (sin(2.0 * PI * padFreq * t) + 0.3 * sin(4.0 * PI * padFreq * t)).toFloat() * 0.12f

            // Warm soft timpani tap
            val kickT = (i % beatDur) / sampleRate
            val kick = softWarmKick(kickT)

            buf[i] = (lead + pad + kick)
        }
    }

    fun release() {
        musicJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
