package com.example.balloonpop.data

import android.content.Context
import android.content.SharedPreferences

class GameRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("balloon_pop_prefs", Context.MODE_PRIVATE)

    fun getCoins(): Int = prefs.getInt("bp_coins", 150)
    fun setCoins(amount: Int) = prefs.edit().putInt("bp_coins", amount.coerceAtLeast(0)).apply()

    fun getGems(): Int = prefs.getInt("bp_gems", 10)
    fun setGems(amount: Int) = prefs.edit().putInt("bp_gems", amount.coerceAtLeast(0)).apply()

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

    // Themes
    fun getUnlockedThemes(): Set<String> {
        return prefs.getStringSet("bp_themes", setOf("theme_neon")) ?: setOf("theme_neon")
    }
    fun unlockTheme(themeId: String) {
        val current = getUnlockedThemes().toMutableSet()
        current.add(themeId)
        prefs.edit().putStringSet("bp_themes", current).apply()
    }
    fun getEquippedTheme(): String = prefs.getString("bp_eq_theme", "theme_neon") ?: "theme_neon"
    fun setEquippedTheme(themeId: String) = prefs.edit().putString("bp_eq_theme", themeId).apply()

    // Campaign Levels
    fun getCompletedLevels(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        for (i in 1..50) {
            val stars = prefs.getInt("bp_lvl_$i", 0)
            if (stars > 0) {
                result[i] = stars
            }
        }
        return result
    }
    fun setLevelStars(level: Int, stars: Int) {
        val prev = prefs.getInt("bp_lvl_$level", 0)
        if (stars > prev) {
            prefs.edit().putInt("bp_lvl_$level", stars).apply()
        }
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

    fun getSkillLevel(skillId: String): Int = prefs.getInt("bp_skill_lvl_$skillId", 1)
    fun setSkillLevel(skillId: String, level: Int) = prefs.edit().putInt("bp_skill_lvl_$skillId", level).apply()

    // Daily Wheel & Timeline
    fun getLastDailySpinTimestamp(): Long = prefs.getLong("last_daily_spin_ts", 0L)
    fun setLastDailySpinTimestamp(ts: Long) = prefs.edit().putLong("last_daily_spin_ts", ts).apply()

    fun getDailyStreakDay(): Int = prefs.getInt("daily_streak_day", 1)
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
    }

    // Stats
    fun getLifetimePops(): Int = prefs.getInt("stat_pops", 0)
    fun addLifetimePops(count: Int) = prefs.edit().putInt("stat_pops", getLifetimePops() + count).apply()

    fun getLifetimeHighScore(): Int = prefs.getInt("stat_high_score", 0)
    fun updateLifetimeHighScore(score: Int) {
        if (score > getLifetimeHighScore()) {
            prefs.edit().putInt("stat_high_score", score).apply()
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

    // Settings
    fun isSoundMuted(): Boolean = prefs.getBoolean("setting_muted", false)
    fun setSoundMuted(muted: Boolean) = prefs.edit().putBoolean("setting_muted", muted).apply()

    fun getSoundVolume(): Float = prefs.getFloat("setting_volume", 1.0f)
    fun setSoundVolume(volume: Float) = prefs.edit().putFloat("setting_volume", volume.coerceIn(0f, 1f)).apply()

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
    }
}
