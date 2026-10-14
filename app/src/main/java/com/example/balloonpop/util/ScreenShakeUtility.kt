package com.example.balloonpop.util

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.geometry.Offset
import com.example.balloonpop.model.BalloonType
import kotlin.random.Random

/**
 * ScreenShakeUtility:
 * Triggers distinct, intensity-weighted vibrations and visual displacements
 * when popping special balloons and executing impactful game actions.
 *
 * Uses the existing Android Vibrator and hardware haptic capabilities as a base,
 * applying nuanced vibration patterns, amplitudes, and randomized directional
 * displacements scaled by effect intensity.
 */
object ScreenShakeUtility {

    /**
     * Vibration and Displacement profile for different special events.
     */
    data class ShakeProfile(
        val visualDisplacement: Float, // Peak pixel translation displacement
        val vibrationDurationMs: Long,  // Duration of haptic vibration
        val vibrationAmplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE, // 1 - 255
        val screenFlashAlpha: Float = 0f, // Optional flash effect
        val isDoublePulse: Boolean = false // Special multi-pulse rumble
    )

    /**
     * Intensity-weighted shake profiles mapped to specific Balloon Types.
     */
    fun getProfileForBalloon(type: BalloonType): ShakeProfile {
        return when (type) {
            BalloonType.MEGA_BOSS -> ShakeProfile(
                visualDisplacement = 32f,
                vibrationDurationMs = 95L,
                vibrationAmplitude = 255,
                screenFlashAlpha = 0.45f,
                isDoublePulse = true
            )
            BalloonType.BOMB -> ShakeProfile(
                visualDisplacement = 26f,
                vibrationDurationMs = 75L,
                vibrationAmplitude = 230,
                screenFlashAlpha = 0.40f,
                isDoublePulse = true
            )
            BalloonType.GOLDEN_PIGGY -> ShakeProfile(
                visualDisplacement = 22f,
                vibrationDurationMs = 60L,
                vibrationAmplitude = 190
            )
            BalloonType.ELECTRIC_STORM -> ShakeProfile(
                visualDisplacement = 20f,
                vibrationDurationMs = 50L,
                vibrationAmplitude = 180,
                screenFlashAlpha = 0.35f
            )
            BalloonType.SHIELDED -> ShakeProfile(
                visualDisplacement = 18f,
                vibrationDurationMs = 45L,
                vibrationAmplitude = 160
            )
            BalloonType.FAST_ROCKET -> ShakeProfile(
                visualDisplacement = 16f,
                vibrationDurationMs = 40L,
                vibrationAmplitude = 150
            )
            BalloonType.RAINBOW -> ShakeProfile(
                visualDisplacement = 15f,
                vibrationDurationMs = 38L,
                vibrationAmplitude = 140
            )
            BalloonType.FREEZE -> ShakeProfile(
                visualDisplacement = 14f,
                vibrationDurationMs = 35L,
                vibrationAmplitude = 135
            )
            BalloonType.MYSTERY_GIFT -> ShakeProfile(
                visualDisplacement = 14f,
                vibrationDurationMs = 35L,
                vibrationAmplitude = 130
            )
            BalloonType.MULTIPLIER -> ShakeProfile(
                visualDisplacement = 13f,
                vibrationDurationMs = 30L,
                vibrationAmplitude = 125
            )
            BalloonType.GOLDEN -> ShakeProfile(
                visualDisplacement = 12f,
                vibrationDurationMs = 28L,
                vibrationAmplitude = 120
            )
            BalloonType.SPLITTER -> ShakeProfile(
                visualDisplacement = 12f,
                vibrationDurationMs = 25L,
                vibrationAmplitude = 115
            )
            BalloonType.HEART -> ShakeProfile(
                visualDisplacement = 10f,
                vibrationDurationMs = 25L,
                vibrationAmplitude = 100
            )
            BalloonType.GHOST_PHANTOM -> ShakeProfile(
                visualDisplacement = 10f,
                vibrationDurationMs = 25L,
                vibrationAmplitude = 95
            )
            BalloonType.STANDARD -> ShakeProfile(
                visualDisplacement = 0f,
                vibrationDurationMs = 18L,
                vibrationAmplitude = 70
            )
        }
    }

    /**
     * Executes the hardware vibration based on the intensity-weighted profile.
     */
    fun triggerHapticFeedback(
        vibrator: Vibrator?,
        profile: ShakeProfile,
        isVibrationEnabled: Boolean,
        hasHardwareVibrator: Boolean
    ) {
        if (!isVibrationEnabled || !hasHardwareVibrator || vibrator == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (profile.isDoublePulse) {
                    // Double-pulse rumble for explosive events like Bomb and Mega Boss
                    val timings = longArrayOf(0, (profile.vibrationDurationMs * 0.45f).toLong(), 25, (profile.vibrationDurationMs * 0.55f).toLong())
                    val amplitudes = intArrayOf(0, profile.vibrationAmplitude, 0, (profile.vibrationAmplitude * 0.9f).toInt())
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            profile.vibrationDurationMs,
                            profile.vibrationAmplitude.coerceIn(1, 255)
                        )
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(profile.vibrationDurationMs)
            }
        } catch (_: Throwable) {
            // Graceful fallback on devices with non-standard haptic drivers
        }
    }

    /**
     * Calculates the 2D visual displacement translation (x, y) given the current shake magnitude.
     * Uses non-linear scaling for tactile feel: low magnitude creates subtle organic jitter,
     * while high magnitude delivers meaty screen-rattling displacement.
     */
    fun calculateVisualDisplacement(currentShake: Float): Offset {
        if (currentShake <= 0.4f) return Offset.Zero

        // Power-curved factor so high intensities feel dramatic without being unplayable
        val normalizedIntensity = (currentShake / 32f).coerceIn(0f, 1f)
        val jitterScale = currentShake * (1.2f + 0.5f * normalizedIntensity)

        val angle = Random.nextFloat() * (2f * Math.PI.toFloat())
        val distance = Random.nextFloat() * jitterScale

        val offsetX = kotlin.math.cos(angle) * distance
        val offsetY = kotlin.math.sin(angle) * distance

        return Offset(offsetX, offsetY)
    }
}
