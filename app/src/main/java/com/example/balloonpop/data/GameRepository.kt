package com.example.balloonpop.data

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("balloon_pop_prefs", Context.MODE_PRIVATE)

    init {
        verifyAndRepairIntegrity()
        recordTimeAnchor()
    }

    private fun saveIntegritySignature() {
        val coins = prefs.getInt("bp_coins", 150)
        val gems = prefs.getInt("bp_gems", 10)
        val highScore = prefs.getInt("stat_high_score", 0)
        val pops = prefs.getInt("stat_pops", 0)
        val starsSum = getCompletedLevels().values.sum()
        val boostersSum = getAllBoosterCounts().values.sum()
        val sig = SecurityGuard.computeSignature(coins, gems, highScore, pops, starsSum, boostersSum)
        prefs.edit().putString("bp_sec_sig", sig).apply()
    }

    private fun verifyAndRepairIntegrity() {
        val coins = prefs.getInt("bp_coins", 150)
        val gems = prefs.getInt("bp_gems", 10)
        val highScore = prefs.getInt("stat_high_score", 0)
        val pops = prefs.getInt("stat_pops", 0)
        val starsSum = getCompletedLevels().values.sum()
        val boostersSum = getAllBoosterCounts().values.sum()
        val sig = prefs.getString("bp_sec_sig", null)

        if (sig != null && !SecurityGuard.verifySignature(coins, gems, highScore, pops, starsSum, boostersSum, sig)) {
            // Tampering detected (e.g. root XML modification or memory inject).
            // Sanitize and self-heal back to valid secure parameters
            val safeCoins = SecurityGuard.sanitizeCoins(coins).coerceAtMost(1500)
            val safeGems = SecurityGuard.sanitizeGems(gems).coerceAtMost(50)
            prefs.edit()
                .putInt("bp_coins", safeCoins)
                .putInt("bp_gems", safeGems)
                .apply()
            saveIntegritySignature()
        } else if (sig == null) {
            saveIntegritySignature()
        }
    }

    private fun recordTimeAnchor() {
        prefs.edit()
            .putLong("sec_wall_time", System.currentTimeMillis())
            .putLong("sec_elapsed_time", SystemClock.elapsedRealtime())
            .apply()
    }

    fun isTimeIntegrityValid(): Boolean {
        val lastWall = prefs.getLong("sec_wall_time", 0L)
        val lastElapsed = prefs.getLong("sec_elapsed_time", 0L)
        val valid = SecurityGuard.validateTimeIntegrity(System.currentTimeMillis(), lastWall, lastElapsed)
        recordTimeAnchor()
        return valid
    }

    fun getCoins(): Int {
        val raw = prefs.getInt("bp_coins", 150)
        return SecurityGuard.sanitizeCoins(raw)
    }

    fun setCoins(amount: Int) {
        val sanitized = SecurityGuard.sanitizeCoins(amount)
        prefs.edit().putInt("bp_coins", sanitized).apply()
        saveIntegritySignature()
    }

    fun getGems(): Int {
        val raw = prefs.getInt("bp_gems", 10)
        return SecurityGuard.sanitizeGems(raw)
    }

    fun setGems(amount: Int) {
        val sanitized = SecurityGuard.sanitizeGems(amount)
        prefs.edit().putInt("bp_gems", sanitized).apply()
        saveIntegritySignature()
    }

    // Hammers
    fun getUnlockedHammers(): Set<String> {
        return prefs.getStringSet("bp_hammers", setOf("hammer_wood")) ?: setOf("hammer_wood")
    }
    fun unlockHammer(hammerId: String) {
        val current = getUnlockedHammers().toMutableSet()
        current.add(hammerId)
        prefs.edit().putStringSet("bp_hammers", current).apply()
    }
    fun getEquippedHammer(): String = prefs.getString("bp_eq_hammer", "hammer_wood") ?: "hammer_wood"
    fun setEquippedHammer(hammerId: String) = prefs.edit().putString("bp_eq_hammer", hammerId).apply()

    // Frames
    fun getUnlockedFrames(): Set<String> {
        return prefs.getStringSet("bp_frames", setOf("frame_minimal")) ?: setOf("frame_minimal")
    }
    fun unlockFrame(frameId: String) {
        val current = getUnlockedFrames().toMutableSet()
        current.add(frameId)
        prefs.edit().putStringSet("bp_frames", current).apply()
    }
    fun getEquippedFrame(): String = prefs.getString("bp_eq_frame", "frame_minimal") ?: "frame_minimal"
    fun setEquippedFrame(frameId: String) = prefs.edit().putString("bp_eq_frame", frameId).apply()

    // Blades
    fun getUnlockedBlades(): Set<String> {
        return prefs.getStringSet("bp_blades", setOf("blade_classic")) ?: setOf("blade_classic")
    }
    fun unlockBlade(bladeId: String) {
        val current = getUnlockedBlades().toMutableSet()
        current.add(bladeId)
        prefs.edit().putStringSet("bp_blades", current).apply()
    }
    fun getEquippedBlade(): String = prefs.getString("bp_eq_blade", "blade_classic") ?: "blade_classic"
    fun setEquippedBlade(bladeId: String) = prefs.edit().putString("bp_eq_blade", bladeId).apply()

    // Flames & Auras
    fun getUnlockedFlames(): Set<String> {
        return prefs.getStringSet("bp_flames", setOf("flame_ember")) ?: setOf("flame_ember")
    }
    fun unlockFlame(flameId: String) {
        val current = getUnlockedFlames().toMutableSet()
        current.add(flameId)
        prefs.edit().putStringSet("bp_flames", current).apply()
    }
    fun getEquippedFlame(): String = prefs.getString("bp_eq_flame", "flame_ember") ?: "flame_ember"
    fun setEquippedFlame(flameId: String) = prefs.edit().putString("bp_eq_flame", flameId).apply()

    // Themes
    fun getUnlockedThemes(): Set<String> {
        val set = prefs.getStringSet("bp_themes", setOf("theme_sunset_twilight", "theme_obsidian")) ?: setOf("theme_sunset_twilight", "theme_obsidian")
        return set + "theme_sunset_twilight"
    }
    fun unlockTheme(themeId: String) {
        val current = getUnlockedThemes().toMutableSet()
        current.add(themeId)
        prefs.edit().putStringSet("bp_themes", current).apply()
    }
    fun getEquippedTheme(): String {
        val stored = prefs.getString("bp_eq_theme", "theme_sunset_twilight") ?: "theme_sunset_twilight"
        return if (stored == "theme_crimson_black") "theme_sunset_twilight" else stored
    }
    fun setEquippedTheme(themeId: String) = prefs.edit().putString("bp_eq_theme", themeId).apply()

    // Campaign Levels (Up to 200 Stages)
    fun getCompletedLevels(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (i in 1..SecurityGuard.MAX_CAMPAIGN_LEVEL) {
            val stars = prefs.getInt("bp_lvl_$i", 0)
            if (stars > 0) {
                result[i] = SecurityGuard.sanitizeStars(stars)
            }
        }
        return result
    }
    fun setLevelStars(level: Int, stars: Int) {
        val safeLvl = SecurityGuard.sanitizeLevel(level)
        val safeStars = SecurityGuard.sanitizeStars(stars)
        val prev = prefs.getInt("bp_lvl_$safeLvl", 0)
        if (safeStars > prev) {
            prefs.edit().putInt("bp_lvl_$safeLvl", safeStars).apply()
            saveIntegritySignature()
        }
    }

    // Star Milestones Track (Star Road)
    fun getClaimedStarMilestones(): Set<Int> {
        val strings = prefs.getStringSet("bp_claimed_star_milestones", emptySet()) ?: emptySet()
        return strings.mapNotNull { it.toIntOrNull() }.toSet()
    }
    fun claimStarMilestone(starCount: Int) {
        val current = getClaimedStarMilestones().toMutableSet()
        current.add(starCount)
        prefs.edit().putStringSet("bp_claimed_star_milestones", current.map { it.toString() }.toSet()).apply()
    }

    // Skills
    fun getUnlockedSkills(): Set<String> {
        return prefs.getStringSet("bp_skills", setOf("skill_slice")) ?: setOf("skill_slice")
    }
    fun unlockSkill(skillId: String) {
        val current = getUnlockedSkills().toMutableSet()
        current.add(skillId)
        prefs.edit().putStringSet("bp_skills", current).apply()
    }
    fun getEquippedSkill(): String = prefs.getString("bp_eq_skill", "skill_slice") ?: "skill_slice"
    fun setEquippedSkill(skillId: String) = prefs.edit().putString("bp_eq_skill", skillId).apply()

    fun getSkillLevel(skillId: String): Int = prefs.getInt("bp_skill_lvl_$skillId", 1).coerceIn(1, 10)
    fun setSkillLevel(skillId: String, level: Int) = prefs.edit().putInt("bp_skill_lvl_$skillId", level.coerceIn(1, 10)).apply()

    // Daily Wheel & Timeline
    fun getLastDailySpinTimestamp(): Long = prefs.getLong("last_daily_spin_ts", 0L)
    fun setLastDailySpinTimestamp(ts: Long) {
        prefs.edit().putLong("last_daily_spin_ts", ts).apply()
        recordTimeAnchor()
    }

    fun getDailyStreakDay(): Int = prefs.getInt("daily_streak_day", 1).coerceIn(1, 7)
    fun setDailyStreakDay(day: Int) = prefs.edit().putInt("daily_streak_day", day.coerceIn(1, 7)).apply()

    // Milestone Chests
    fun getClaimedMilestoneChests(): Set<Int> {
        val strSet = prefs.getStringSet("claimed_milestone_chests", emptySet()) ?: emptySet()
        return strSet.mapNotNull { it.toIntOrNull() }.toSet()
    }
    fun claimMilestoneChest(level: Int) {
        val current = getClaimedMilestoneChests().map { it.toString() }.toMutableSet()
        current.add(level.toString())
        prefs.edit().putStringSet("claimed_milestone_chests", current).apply()
        saveIntegritySignature()
    }

    // Stats
    fun getLifetimePops(): Int = prefs.getInt("stat_pops", 0)
    fun addLifetimePops(count: Int) {
        val updated = (getLifetimePops() + count).coerceAtLeast(0)
        prefs.edit().putInt("stat_pops", updated).apply()
        saveIntegritySignature()
    }

    fun getLifetimeHighScore(): Int = SecurityGuard.sanitizeHighScore(prefs.getInt("stat_high_score", 0))
    fun updateLifetimeHighScore(score: Int) {
        val sanitized = SecurityGuard.sanitizeHighScore(score)
        if (sanitized > getLifetimeHighScore()) {
            prefs.edit().putInt("stat_high_score", sanitized).apply()
            saveIntegritySignature()
        }
    }

    fun getLifetimeMaxCombo(): Int = prefs.getInt("stat_max_combo", 1)
    fun updateLifetimeMaxCombo(combo: Int) {
        if (combo > getLifetimeMaxCombo()) {
            prefs.edit().putInt("stat_max_combo", combo).apply()
        }
    }

    fun getWheelSpins(): Int = prefs.getInt("stat_wheel_spins", 0)
    fun incrementWheelSpins() = prefs.edit().putInt("stat_wheel_spins", getWheelSpins() + 1).apply()

    fun getClaimedQuests(): Set<String> = prefs.getStringSet("claimed_quests", emptySet()) ?: emptySet()
    fun claimQuest(questId: String) {
        val current = getClaimedQuests().toMutableSet()
        current.add(questId)
        prefs.edit().putStringSet("claimed_quests", current).apply()
    }

    // Achievements & Milestone Badges
    fun getClaimedAchievements(): Set<String> = prefs.getStringSet("claimed_achievements", emptySet()) ?: emptySet()
    fun claimAchievement(achievementId: String) {
        val current = getClaimedAchievements().toMutableSet()
        current.add(achievementId)
        prefs.edit().putStringSet("claimed_achievements", current).apply()
    }

    // First-time Onboarding Tutorial
    fun hasCompletedTutorial(): Boolean = prefs.getBoolean("has_completed_tutorial", false)
    fun setCompletedTutorial(completed: Boolean) = prefs.edit().putBoolean("has_completed_tutorial", completed).apply()

    // In-game Boosters (Candy Crush style)
    fun getBoosterCount(boosterId: String): Int = prefs.getInt("bp_booster_$boosterId", 3)
    fun setBoosterCount(boosterId: String, count: Int) = prefs.edit().putInt("bp_booster_$boosterId", count.coerceAtLeast(0)).apply()
    fun consumeBooster(boosterId: String): Boolean {
        val count = getBoosterCount(boosterId)
        if (count > 0) {
            setBoosterCount(boosterId, count - 1)
            return true
        }
        return false
    }
    fun addBooster(boosterId: String, amount: Int = 1) {
        setBoosterCount(boosterId, getBoosterCount(boosterId) + amount)
    }
    fun getAllBoosterCounts(): Map<String, Int> {
        return mapOf(
            "booster_thunder" to getBoosterCount("booster_thunder"),
            "booster_color_bomb" to getBoosterCount("booster_color_bomb"),
            "booster_hammer" to getBoosterCount("booster_hammer"),
            "booster_freeze" to getBoosterCount("booster_freeze"),
            "booster_vortex" to getBoosterCount("booster_vortex")
        )
    }

    // Settings
    fun isSoundMuted(): Boolean = prefs.getBoolean("setting_muted", false)
    fun setSoundMuted(muted: Boolean) = prefs.edit().putBoolean("setting_muted", muted).apply()

    fun getSoundVolume(): Float = prefs.getFloat("setting_volume", 1.0f)
    fun setSoundVolume(volume: Float) = prefs.edit().putFloat("setting_volume", volume.coerceIn(0f, 1f)).apply()

    fun isMusicMuted(): Boolean = prefs.getBoolean("setting_music_muted", false)
    fun setMusicMuted(muted: Boolean) = prefs.edit().putBoolean("setting_music_muted", muted).apply()

    fun getMusicVolume(): Float = prefs.getFloat("setting_music_volume", 0.50f)
    fun setMusicVolume(volume: Float) = prefs.edit().putFloat("setting_music_volume", volume.coerceIn(0f, 1f)).apply()

    fun isSfxMuted(): Boolean = prefs.getBoolean("setting_sfx_muted", false)
    fun setSfxMuted(muted: Boolean) = prefs.edit().putBoolean("setting_sfx_muted", muted).apply()

    fun getSfxVolume(): Float = prefs.getFloat("setting_sfx_volume", 1.0f)
    fun setSfxVolume(volume: Float) = prefs.edit().putFloat("setting_sfx_volume", volume.coerceIn(0f, 1f)).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean("setting_vibration", true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean("setting_vibration", enabled).apply()

    fun isScreenShakeEnabled(): Boolean = prefs.getBoolean("setting_shake", true)
    fun setScreenShakeEnabled(enabled: Boolean) = prefs.edit().putBoolean("setting_shake", enabled).apply()

    fun getGraphicsQuality(): String = prefs.getString("setting_graphics", "BALANCED") ?: "BALANCED"
    fun setGraphicsQuality(quality: String) = prefs.edit().putString("setting_graphics", quality).apply()

    fun isWideAssistEnabled(): Boolean = prefs.getBoolean("setting_wide_assist", false)
    fun setWideAssistEnabled(enabled: Boolean) = prefs.edit().putBoolean("setting_wide_assist", enabled).apply()

    fun resetAllData() {
        prefs.edit().clear().apply()
        saveIntegritySignature()
    }
}
