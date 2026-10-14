package com.example.balloonpop.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.security.MessageDigest
import kotlin.math.abs

/**
 * SecurityGuard provides anti-tampering, integrity validation, input sanitization,
 * and anti-clock manipulation guards to protect the app against cheat engines,
 * rooted device XML modifications, time-travel exploits, and memory tampering.
 */
object SecurityGuard {
    private const val TAG = "SecurityGuard"
    
    // Obfuscated cryptographic salt components
    private const val SALT_ALPHA = "B4ll00n_P0p_D3lux3"
    private const val SALT_BETA = "S3cur3_Int3gr1ty_Gu4rd_v2"
    private const val SALT_GAMMA = "Pl4y_St0r3_C0mpl14nt_2026"

    // Safe operating ceilings to prevent integer overflow & memory injection attacks
    const val MAX_SAFE_COINS = 99_999_999
    const val MAX_SAFE_GEMS = 999_999
    const val MAX_SAFE_SCORE = 50_000_000
    const val MAX_STARS_PER_LEVEL = 3
    const val MAX_CAMPAIGN_LEVEL = 200

    /**
     * Computes a SHA-256 cryptographic checksum signature for game progress & currencies.
     */
    fun computeSignature(
        coins: Int,
        gems: Int,
        highScore: Int,
        lifetimePops: Int,
        starsSum: Int,
        boostersSum: Int
    ): String {
        return try {
            val payload = "$SALT_ALPHA:$coins:$SALT_BETA:$gems:$highScore:$SALT_GAMMA:$lifetimePops:$starsSum:$boostersSum"
            val md = MessageDigest.getInstance("SHA-256")
            val hashBytes = md.digest(payload.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error computing security signature", e)
            "fallback_sig_${coins}_${gems}"
        }
    }

    /**
     * Verifies if stored signature matches the actual values.
     * Returns true if data integrity is intact, false if tampered externally.
     */
    fun verifySignature(
        coins: Int,
        gems: Int,
        highScore: Int,
        lifetimePops: Int,
        starsSum: Int,
        boostersSum: Int,
        storedSignature: String?
    ): Boolean {
        if (storedSignature.isNullOrEmpty()) {
            // First run or uninitialized signature
            return true
        }
        val expected = computeSignature(coins, gems, highScore, lifetimePops, starsSum, boostersSum)
        return expected.equals(storedSignature, ignoreCase = true)
    }

    /**
     * Validates wall clock time against monotonic elapsed real-time to detect
     * "time-travel" cheats (e.g., users setting device date forward 10 days to exploit daily spins).
     */
    fun validateTimeIntegrity(
        currentTimeMillis: Long,
        lastRecordedWallTime: Long,
        lastRecordedElapsedRealtime: Long
    ): Boolean {
        if (lastRecordedWallTime <= 0L || lastRecordedElapsedRealtime <= 0L) {
            return true // First time recording
        }

        // 1. Clock Rollback Check: Wall time cannot be substantially earlier than last recorded wall time
        if (currentTimeMillis < lastRecordedWallTime - 60_000L) {
            Log.w(TAG, "Clock rollback detected! current=$currentTimeMillis, recorded=$lastRecordedWallTime")
            return false
        }

        // 2. Unrealistic Clock Forward Jump: If wall time jumped ahead by > 24 hours,
        // but monotonic elapsed realtime only advanced seconds, clock was spoofed!
        val wallDelta = currentTimeMillis - lastRecordedWallTime
        val currentElapsed = SystemClock.elapsedRealtime()
        val elapsedDelta = currentElapsed - lastRecordedElapsedRealtime

        // If device did not reboot (currentElapsed >= lastRecordedElapsedRealtime)
        if (elapsedDelta >= 0) {
            val oneDayMillis = 24 * 60 * 60 * 1000L
            if (wallDelta > oneDayMillis && elapsedDelta < (oneDayMillis / 2)) {
                Log.w(TAG, "Time travel jump detected! wallDelta=$wallDelta ms, elapsedDelta=$elapsedDelta ms")
                return false
            }
        }

        return true
    }

    // --- Input Sanitizers ---
    fun sanitizeCoins(amount: Int): Int = amount.coerceIn(0, MAX_SAFE_COINS)
    fun sanitizeGems(amount: Int): Int = amount.coerceIn(0, MAX_SAFE_GEMS)
    fun sanitizeHighScore(score: Int): Int = score.coerceIn(0, MAX_SAFE_SCORE)
    fun sanitizeStars(stars: Int): Int = stars.coerceIn(0, MAX_STARS_PER_LEVEL)
    fun sanitizeLevel(level: Int): Int = level.coerceIn(1, MAX_CAMPAIGN_LEVEL)
}
