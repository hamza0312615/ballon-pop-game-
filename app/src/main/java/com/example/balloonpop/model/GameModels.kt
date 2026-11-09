package com.example.balloonpop.model

import androidx.compose.ui.graphics.Color

enum class BalloonType(
    val typeName: String,
    val weight: Int,
    val points: Int,
    val iconEmoji: String? = null
) {
    STANDARD("standard", 45, 10, null),
    GOLDEN("golden", 10, 50, "🪙"),
    RAINBOW("rainbow", 7, 30, "🌈"),
    BOMB("bomb", 10, -20, "💣"),
    FREEZE("freeze", 4, 15, "❄️"),
    HEART("heart", 3, 10, "❤️"),
    MULTIPLIER("multiplier", 4, 20, "⭐"),
    FAST_ROCKET("fast_rocket", 6, 35, "🚀"),
    SPLITTER("splitter", 5, 25, "🫧"),
    SHIELDED("shielded", 5, 45, "🛡️"),
    MYSTERY_GIFT("mystery_gift", 4, 30, "🎁"),
    GHOST_PHANTOM("ghost", 4, 40, "👻"),
    GOLDEN_PIGGY("piggy", 3, 80, "🐷"),
    ELECTRIC_STORM("electric", 4, 35, "⚡"),
    MEGA_BOSS("mega_boss", 0, 500, "👑")
}

enum class BalloonShapeStyle(val displayName: String, val emoji: String) {
    SMILEY("Smiley Happy", "😊"),
    ANGRY("Angry Glare", "😡"),
    FUNNY("Funny Googly", "🤪"),
    COOL("Cool Sunglasses", "😎"),
    SHOCKED("Shocked Gasp", "😲"),
    CAT_FACE("Kitty Ears", "🐱"),
    STAR_SHAPE("Golden Star", "⭐"),
    HEART_SHAPE("Love Heart", "❤️")
}

/** Surface pattern a balloon skin paints over the balloon body. */
enum class BalloonPattern {
    SOLID,
    STRIPES,
    POLKA,
    SWIRL,
    SPARKLE
}

data class Balloon(
    val id: Long,
    var x: Float,
    var y: Float,
    var radius: Float,
    var vy: Float,
    val type: BalloonType,
    val color: Color,
    var wobblePhase: Float,
    val shapeStyle: BalloonShapeStyle = BalloonShapeStyle.SMILEY,
    var popped: Boolean = false,
    var hp: Int = 1,
    var maxHp: Int = 1,
    var alpha: Float = 1.0f,
    var isMini: Boolean = false,
    var vx: Float = 0f,
    // Pass 5 balloon skins: pattern overlay + specular gloss strength,
    // assigned at spawn from the equipped BalloonSkin (standard balloons).
    val pattern: BalloonPattern = BalloonPattern.SOLID,
    val glossStrength: Float = 1.0f,
    var spawnAge: Float = 0f,
    var squashTimer: Float = 0f,
    var tiltAngle: Float = 0f,
    var isRingFormation: Boolean = false,
    var ringAngle: Float = 0f,
    var ringCenterX: Float = 0f,
    var ringCenterY: Float = 0f,
    var ringTargetRadius: Float = 135f,
    var isGoldRush: Boolean = false
)

enum class ParticleType {
    GLOW_SPARK,
    RUBBER_SHRED,
    SPARKLE_STAR,
    RING_BURST,
    ICE_CRYSTAL,
    LIGHTNING_EMBER,
    CONFETTI_RIBBON
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    var isStar: Boolean = false,
    var rotation: Float = 0f,
    var vRot: Float = 0f,
    var particleType: ParticleType = ParticleType.GLOW_SPARK,
    var length: Float = 0f
)

data class PopShockwave(
    val x: Float,
    val y: Float,
    var radius: Float,
    val maxRadius: Float,
    val color: Color,
    var alpha: Float = 1.0f
)

data class AmbientElement(
    var x: Float,
    var y: Float,
    var vy: Float,
    var vx: Float,
    var size: Float,
    var alpha: Float,
    val type: AmbientType,
    val color: Color
)

enum class AmbientType {
    STAR,
    CHERRY_PETAL,
    BUBBLE,
    EMBER,
    CLOUD,
    GOLD_SPARKLE,
    SNOWFLAKE
}

data class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var alpha: Float = 1.0f,
    var vy: Float = -2.5f,
    var age: Float = 0f,
    var scale: Float = 0.5f
)

data class BladeTrailPoint(
    val x: Float,
    val y: Float,
    var alpha: Float,
    val color: Color
)

data class LightningBolt(
    val id: Long,
    val points: List<Pair<Float, Float>>,
    var alpha: Float = 1.0f,
    val color: Color = Color(0xFF2F6DB3),
    val branchPoints: List<List<Pair<Float, Float>>> = emptyList()
)

enum class InGameBooster(
    val id: String,
    val displayName: String,
    val icon: String,
    val description: String,
    val defaultCharges: Int = 3
) {
    THUNDER_STRIKE("booster_thunder", "Thunder Zap", "⚡", "Zaps all bombs & strikes balloons with electric thunder!"),
    COLOR_BOMB("booster_color_bomb", "Color Bomb", "🌈", "Pops every balloon of the most popular color on screen!"),
    HAMMER_SMASH("booster_hammer", "Lollipop Hammer", "🔨", "Giant hammer shockwave smashes balloons in a huge radius!"),
    FREEZE_BLIZZARD("booster_freeze", "Ice Freeze", "❄️", "Freezes time & all balloons completely for 6 seconds!"),
    TORNADO_VORTEX("booster_vortex", "Wind Vortex", "🌀", "Draws all balloons into the center in a huge combo pop!")
}

/**
 * Pass 4: the ONE place booster prices live.
 * Shop purchases, the in-gameplay buy dialog and the ViewModel all read these
 * values so a price can never drift between screens.
 */
object BoosterPrices {
    /** Coins for +1 booster charge in the shop / in-gameplay buy dialog. */
    const val COIN_PRICE = 150

    /** Gems for +1 booster charge in the shop. */
    const val GEM_PRICE = 3

    /** Coins charged when a booster is triggered with 0 charges left. */
    const val DEPLETED_USE_COIN_COST = 50
}

data class HammerStrikeEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    var progress: Float = 0f, // 0f (start swing) to 1f (fade out)
    val hammerSkin: HammerSkin
)

enum class CurrencyType {
    COINS,
    GEMS
}

data class HammerSkin(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val tier: Int,
    val headColor: Color,
    val handleColor: Color,
    val sparkColor: Color,
    val description: String
)

/**
 * Pass 5: an equippable look for the balloons themselves — the screen is
 * balloons, so this is the game's most visible cosmetic. Modelled on
 * [HammerSkin]: repository persistence (owned set + equipped id), shop
 * purchase with coins/gems, equip. Standard balloons spawn with the
 * equipped skin's [palette] colours, a face from [faceStyles], its
 * [pattern] overlay and [glossStrength] specular intensity.
 * All palettes are jewel/storybook tones — no neon hexes.
 */
data class BalloonSkin(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val tier: Int,
    val palette: List<Color>,
    val faceStyles: List<BalloonShapeStyle>,
    val pattern: BalloonPattern,
    val glossStrength: Float,
    val description: String
)

data class PlayFrame(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val tier: Int,
    val borderColors: List<Color>,
    val glowColor: Color,
    val description: String
)

data class BladeItem(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val trailColor: Color,
    val secondaryColor: Color = trailColor,
    val description: String = ""
)

data class FlameItem(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val primaryColor: Color,
    val glowColor: Color,
    val emberColor: Color,
    val description: String,
    val critPopBonusPercent: Int = 10
)

data class StarMilestoneReward(
    val starsRequired: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val bonusTitle: String,
    val icon: String = "⭐"
)

data class ThemeConfig(
    val id: String,
    val name: String,
    val icon: String,
    val cost: Int,
    val currency: CurrencyType,
    val gradientColors: List<Color>
)

data class CampaignLevel(
    val level: Int,
    val targetPops: Int,
    val targetScore: Int,
    val timeLimit: Int,
    val maxBombs: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val maxMissesAllowed: Int = 4
) {
    val isBossStage: Boolean get() = (level % 10 == 0)
}

enum class SpawnFormation {
    CLUSTER,
    STREAM,
    RING,
    MIXED_RAIN,
    BOMB_DODGE,
    GOLD_RUSH
}

enum class StagePacingRole(val displayName: String) {
    LEARN("Learn"),
    MIX("Mix"),
    CHALLENGE("Challenge"),
    CELEBRATION("Celebration")
}

data class StageRecipe(
    val level: Int,
    val zoneNumber: Int,
    val zoneStage: Int,
    val role: StagePacingRole,
    val targetPops: Int,
    val timeLimit: Int,
    val maxMisses: Int,
    val unlockedBalloonTypes: List<BalloonType>,
    val allowedFormations: List<SpawnFormation>,
    val specialChance: Float,
    val goldRushChance: Float
)

object StageRecipes {
    fun getRecipe(lvl: Int): StageRecipe {
        val safeLvl = lvl.coerceAtLeast(1)
        val zoneNumber = (((safeLvl - 1) / 10) + 1).coerceIn(1, 20)
        val zoneStage = (((safeLvl - 1) % 10) + 1).coerceIn(1, 10)
        val isBossStage = (safeLvl % 10 == 0)

        val role = when (zoneStage) {
            in 1..3 -> StagePacingRole.LEARN
            in 4..6 -> StagePacingRole.MIX
            in 7..9 -> StagePacingRole.CHALLENGE
            else -> StagePacingRole.CELEBRATION
        }

        // Unlocked balloon types introduced gradually across the whole game:
        // Frozen from stage 3, bombs from stage 4, Armored from stage 6, Splitter from stage 8
        val unlockedTypes = mutableListOf(BalloonType.STANDARD)
        if (safeLvl >= 1) unlockedTypes.add(BalloonType.GOLDEN)
        if (safeLvl >= 2) unlockedTypes.add(BalloonType.RAINBOW)
        if (safeLvl >= 3) unlockedTypes.add(BalloonType.FREEZE)
        if (safeLvl >= 4) unlockedTypes.add(BalloonType.BOMB)
        if (safeLvl >= 5) unlockedTypes.add(BalloonType.FAST_ROCKET)
        if (safeLvl >= 6) unlockedTypes.add(BalloonType.SHIELDED)
        if (safeLvl >= 7) {
            unlockedTypes.add(BalloonType.HEART)
            unlockedTypes.add(BalloonType.MULTIPLIER)
        }
        if (safeLvl >= 8) unlockedTypes.add(BalloonType.SPLITTER)
        if (safeLvl >= 10) {
            unlockedTypes.add(BalloonType.ELECTRIC_STORM)
            if (isBossStage) unlockedTypes.add(BalloonType.MEGA_BOSS)
        }
        if (safeLvl >= 15) unlockedTypes.add(BalloonType.MYSTERY_GIFT)
        if (safeLvl >= 20) {
            unlockedTypes.add(BalloonType.GHOST_PHANTOM)
            unlockedTypes.add(BalloonType.GOLDEN_PIGGY)
        }

        // Formations unlock progressively:
        // Early stages use CLUSTER / STREAM / RING only; heavier patterns unlock as stages progress.
        // Bomb Dodge never before stage 4.
        val allowedFormations = when {
            safeLvl < 4 -> listOf(
                SpawnFormation.CLUSTER,
                SpawnFormation.STREAM,
                SpawnFormation.RING,
                SpawnFormation.MIXED_RAIN
            )
            role == StagePacingRole.CELEBRATION -> listOf(
                SpawnFormation.GOLD_RUSH,
                SpawnFormation.CLUSTER,
                SpawnFormation.STREAM,
                SpawnFormation.RING,
                SpawnFormation.MIXED_RAIN,
                SpawnFormation.BOMB_DODGE
            )
            role == StagePacingRole.CHALLENGE -> listOf(
                SpawnFormation.BOMB_DODGE,
                SpawnFormation.STREAM,
                SpawnFormation.RING,
                SpawnFormation.CLUSTER,
                SpawnFormation.MIXED_RAIN,
                SpawnFormation.GOLD_RUSH
            )
            role == StagePacingRole.MIX -> listOf(
                SpawnFormation.CLUSTER,
                SpawnFormation.STREAM,
                SpawnFormation.BOMB_DODGE,
                SpawnFormation.RING,
                SpawnFormation.MIXED_RAIN,
                SpawnFormation.GOLD_RUSH
            )
            else -> listOf(
                SpawnFormation.CLUSTER,
                SpawnFormation.STREAM,
                SpawnFormation.RING,
                SpawnFormation.MIXED_RAIN
            )
        }

        val (targetPops, timeLimit, maxMisses, specialChance, goldRushChance) = when (role) {
            StagePacingRole.LEARN -> {
                // Zone stages 1–3 "Learn": lower target, generous time, very few specials
                val target = when (zoneStage) {
                    1 -> (18 + (zoneNumber - 1) * 3).coerceIn(18, 45)
                    2 -> (22 + (zoneNumber - 1) * 3).coerceIn(22, 50)
                    else -> (26 + (zoneNumber - 1) * 3).coerceIn(26, 55)
                }
                val time = (55 + (zoneNumber / 4) * 2).coerceIn(52, 65)
                val misses = 6
                val specials = if (safeLvl <= 2) 0.05f else 0.08f
                val goldChance = 0.05f
                RecipeStats(target, time, misses, specials, goldChance)
            }
            StagePacingRole.MIX -> {
                // Zone stages 4–6 "Mix": standard values
                val target = (32 + (zoneNumber - 1) * 3 + (zoneStage - 4) * 4).coerceIn(32, 66)
                val time = (48 + (zoneNumber / 5) * 2).coerceIn(48, 56)
                val misses = 5
                val specials = 0.22f
                val goldChance = 0.08f
                RecipeStats(target, time, misses, specials, goldChance)
            }
            StagePacingRole.CHALLENGE -> {
                // Zone stages 7–9 "Challenge": denser targets, more specials, slightly tighter time
                val target = (46 + (zoneNumber - 1) * 3 + (zoneStage - 7) * 5).coerceIn(46, 84)
                val time = (44 + (zoneNumber / 6) * 2).coerceIn(42, 50)
                val misses = 4
                val specials = 0.38f
                val goldChance = 0.10f
                RecipeStats(target, time, misses, specials, goldChance)
            }
            StagePacingRole.CELEBRATION -> {
                // Zone stage 10 "Celebration": GOLD RUSH chance up, generous time
                val target = (56 + (zoneNumber - 1) * 4).coerceIn(56, 95)
                val time = (80 + (zoneNumber / 4) * 2).coerceIn(80, 110)
                val misses = 6
                val specials = 0.30f
                val goldChance = 0.40f // GOLD RUSH chance up!
                RecipeStats(target, time, misses, specials, goldChance)
            }
        }

        return StageRecipe(
            level = safeLvl,
            zoneNumber = zoneNumber,
            zoneStage = zoneStage,
            role = role,
            targetPops = targetPops,
            timeLimit = timeLimit,
            maxMisses = maxMisses,
            unlockedBalloonTypes = unlockedTypes,
            allowedFormations = allowedFormations,
            specialChance = specialChance,
            goldRushChance = goldRushChance
        )
    }

    private data class RecipeStats(
        val targetPops: Int,
        val timeLimit: Int,
        val maxMisses: Int,
        val specialChance: Float,
        val goldRushChance: Float
    )
}


enum class GameOverReason(val title: String, val subtitle: String, val icon: String) {
    MISSED_LIMIT(
        "Too Many Balloons Escaped!",
        "Exceeded the maximum allowed missed balloons",
        "💨"
    ),
    TIME_EXPIRED(
        "Time's Up!",
        "Ran out of time before popping the target balloons",
        "⏱️"
    ),
    LIVES_DEPLETED(
        "No Lives Remaining!",
        "Ran out of lives in classic mode",
        "💔"
    ),
    BOMB_DETONATION(
        "Bomb Detonated!",
        "Accidentally tapped a hazardous bomb",
        "💣"
    ),
    // Pass 5: an endless run the player ended deliberately (Finish & Save,
    // pause-menu quit, or navigating away) — its best was saved.
    MANUAL_QUIT(
        "Run Saved!",
        "You ended this run — your best score was recorded",
        "🏁"
    )
}

enum class GameMode(val displayName: String, val desc: String, val icon: String) {
    CAMPAIGN("Story Campaign", "200 Stages • Star Rewards", "🗺️"),
    CLASSIC("Classic", "3 Lives • Increasing Speed", "🎯"),
    TIME_RUSH("Time Rush", "60 Seconds • Fast Spawns", "⏱️"),
    ARCADE("Arcade Frenzy", "Power-ups & Bomb Waves", "🔥"),
    ZEN("Zen Popping", "No Bombs • Relaxing Ambient", "🧘")
}

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAMEOVER,
    LEVELCLEAR
}

enum class SkillType(val id: String, val title: String, val icon: String) {
    HAMMER_SLIDE("skill_slice", "Hammer Slide", "🔨⚡"),
    // LEGACY (Pass 5 audit L15): CONTINUOUS_SLICE intentionally keeps the
    // duplicate persisted id "skill_slice" — it is handled together with
    // HAMMER_SLIDE everywhere and "skill_slice" is a stored unlock id, so
    // removing this entry or changing its id would risk persisted-data
    // mismatches for zero player benefit. Do not "deduplicate".
    CONTINUOUS_SLICE("skill_slice", "Hammer Slide", "🔨⚡"),
    TIME_FREEZE("skill_freeze", "Chrono Stasis", "⏱️"),
    MAGNET_PULL("skill_magnet", "Vortex Magnet", "🧲"),
    SHOCKWAVE("skill_shockwave", "Supernova Blast", "💥")
}

data class PlayerSkill(
    val id: String,
    val type: SkillType,
    val name: String,
    val icon: String,
    val description: String,
    val unlockCostCoins: Int,
    val unlockCostGems: Int,
    val maxLevel: Int = 3,
    val currentLevel: Int = 1,
    val energyCostPerSec: Float = 25f,
    val cooldownSeconds: Float = 12f,
    val accentColor: Color = Color(0xFF2F6DB3)
)

data class DailyStreakDay(
    val dayNumber: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val label: String,
    val isJackpot: Boolean = false
) {
    val day: Int get() = dayNumber
    val icon: String get() = if (isJackpot) "👑" else if (rewardGems > 0) "💎" else "🪙"
}

data class WorldZone(
    val zoneNumber: Int,
    val name: String,
    val subtitle: String,
    val startLevel: Int,
    val endLevel: Int,
    val islandColor: Color,
    val pathGlowColor: Color,
    val bgGradient: List<Color>,
    val icon: String
) {
    val title: String get() = name
    val accentColor: Color get() = islandColor
    val primaryColor: Color get() = islandColor
    val secondaryColor: Color get() = pathGlowColor
    val gradientColors: List<Color> get() = bgGradient
    val levelsRange: IntRange get() = startLevel..endLevel
    val milestoneChestLevel: Int get() = endLevel
}

data class WheelSegment(
    val label: String,
    val coins: Int,
    val gems: Int,
    val color: Color
)

enum class QuestCategory(val displayName: String, val icon: String) {
    DAILY("Daily Challenges", "📅"),
    WEEKLY("Weekly Hero", "🏆")
}

data class QuestItem(
    val id: String,
    val title: String,
    val target: Int,
    val rewardCoins: Int,
    val rewardGems: Int,
    val category: QuestCategory = QuestCategory.DAILY,
    val icon: String = "🎯"
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val target: Int,
    val rewardCoins: Int,
    val rewardGems: Int
)

data class DiamondStorePack(
    val id: String,
    val title: String,
    val diamondsAmount: Int,
    val priceTag: String,
    val bonusTag: String? = null,
    val isAdReward: Boolean = false
)

object GameConstants {
    // Pass 5: jewel palette (ruby, sapphire, amethyst, topaz, emerald, coral,
    // deep teal, rose, sky sapphire, plum) — rich and lower-saturation, zero neon.
    val BALLOON_PALETTE = listOf(
        Color(0xFFD64550),
        Color(0xFF2F6DB3),
        Color(0xFF7C4DBD),
        Color(0xFFF2B134),
        Color(0xFF2FA36B),
        Color(0xFFE86A4E),
        Color(0xFF1F7A8C),
        Color(0xFFC2436B),
        Color(0xFF4A90C4),
        Color(0xFFA03D8F)
    )

    val SHOP_BALLOON_SKINS = listOf(
        BalloonSkin(
            id = "balloon_classic",
            name = "Classic Gloss",
            icon = "🎈",
            cost = 0,
            currency = CurrencyType.COINS,
            tier = 1,
            palette = listOf(
                Color(0xFFD64550), Color(0xFF3E7CB1), Color(0xFF2E9E6B),
                Color(0xFFF2B134), Color(0xFF8E44AD)
            ),
            faceStyles = BalloonShapeStyle.values().toList(),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.0f,
            description = "The beloved storybook originals, glossy and bright."
        ),
        BalloonSkin(
            id = "balloon_ruby",
            name = "Ruby Jewel",
            icon = "🔴",
            cost = 300,
            currency = CurrencyType.COINS,
            tier = 2,
            palette = listOf(
                Color(0xFF9B1B30), Color(0xFFC1272D), Color(0xFFB03A48), Color(0xFF7B1E26)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.FUNNY, BalloonShapeStyle.COOL),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.15f,
            description = "Deep-cut rubies with a polished jewel shine."
        ),
        BalloonSkin(
            id = "balloon_sapphire",
            name = "Sapphire Jewel",
            icon = "🔵",
            cost = 300,
            currency = CurrencyType.COINS,
            tier = 2,
            palette = listOf(
                Color(0xFF1B3A6B), Color(0xFF1D4E9E), Color(0xFF2E6FB7), Color(0xFF274690)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.COOL, BalloonShapeStyle.SHOCKED),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.15f,
            description = "Royal sapphire blues, calm and brilliant."
        ),
        BalloonSkin(
            id = "balloon_emerald",
            name = "Emerald Jewel",
            icon = "🟢",
            cost = 300,
            currency = CurrencyType.COINS,
            tier = 2,
            palette = listOf(
                Color(0xFF0B6E4F), Color(0xFF12805C), Color(0xFF0E7C61), Color(0xFF14532D)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.CAT_FACE, BalloonShapeStyle.FUNNY),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.15f,
            description = "Twilight-grove emeralds with a forest gleam."
        ),
        BalloonSkin(
            id = "balloon_topaz",
            name = "Topaz Gold",
            icon = "🟡",
            cost = 500,
            currency = CurrencyType.COINS,
            tier = 3,
            palette = listOf(
                Color(0xFFB7791F), Color(0xFFD4A017), Color(0xFFC99700), Color(0xFFA67C00)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.STAR_SHAPE, BalloonShapeStyle.COOL),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.25f,
            description = "Warm topaz golds that glow like lantern light."
        ),
        BalloonSkin(
            id = "balloon_amethyst",
            name = "Amethyst",
            icon = "🟣",
            cost = 500,
            currency = CurrencyType.COINS,
            tier = 3,
            palette = listOf(
                Color(0xFF5B2A86), Color(0xFF6C3483), Color(0xFF7D3C98), Color(0xFF4A235A)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.SHOCKED, BalloonShapeStyle.CAT_FACE),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.15f,
            description = "Moonlit amethyst purples, rich and velvety."
        ),
        BalloonSkin(
            id = "balloon_coral_sunset",
            name = "Coral Sunset",
            icon = "🌇",
            cost = 800,
            currency = CurrencyType.COINS,
            tier = 4,
            palette = listOf(
                Color(0xFFE76F51), Color(0xFFF4A261), Color(0xFFE9C46A), Color(0xFFEE6C4D)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.FUNNY, BalloonShapeStyle.COOL),
            pattern = BalloonPattern.STRIPES,
            glossStrength = 1.1f,
            description = "Golden-hour stripes in coral, peach and honey."
        ),
        BalloonSkin(
            id = "balloon_candy_swirl",
            name = "Candy Swirl",
            icon = "🍭",
            cost = 1200,
            currency = CurrencyType.COINS,
            tier = 4,
            palette = listOf(
                Color(0xFFF4ACB7), Color(0xFFF7CAD9), Color(0xFFD8BBD4), Color(0xFFFFE5B4)
            ),
            faceStyles = listOf(BalloonShapeStyle.FUNNY, BalloonShapeStyle.CAT_FACE, BalloonShapeStyle.SMILEY),
            pattern = BalloonPattern.SWIRL,
            glossStrength = 1.0f,
            description = "Soft candy pastels with a peppermint swirl."
        ),
        BalloonSkin(
            id = "balloon_crystal_ice",
            name = "Crystal Ice",
            icon = "🧊",
            cost = 1800,
            currency = CurrencyType.COINS,
            tier = 5,
            palette = listOf(
                Color(0xFFA8DADC), Color(0xFFBDE0FE), Color(0xFFCDE7F0), Color(0xFF9AD1D4)
            ),
            faceStyles = listOf(BalloonShapeStyle.SMILEY, BalloonShapeStyle.SHOCKED, BalloonShapeStyle.CAT_FACE),
            pattern = BalloonPattern.SOLID,
            glossStrength = 1.35f,
            description = "Frost-clear crystals with a mirror-bright gloss."
        ),
        BalloonSkin(
            id = "balloon_galaxy",
            name = "Galaxy",
            icon = "🌌",
            cost = 25,
            currency = CurrencyType.GEMS,
            tier = 5,
            palette = listOf(
                Color(0xFF2B2D5C), Color(0xFF3D348B), Color(0xFF6A4C93), Color(0xFF8E7CC3)
            ),
            faceStyles = listOf(BalloonShapeStyle.COOL, BalloonShapeStyle.SHOCKED, BalloonShapeStyle.STAR_SHAPE),
            pattern = BalloonPattern.SPARKLE,
            glossStrength = 1.2f,
            description = "Deep-space violets dusted with silver stars."
        ),
        BalloonSkin(
            id = "balloon_panda",
            name = "Panda",
            icon = "🐼",
            cost = 40,
            currency = CurrencyType.GEMS,
            tier = 6,
            palette = listOf(
                Color(0xFFF5F5F0), Color(0xFF2B2B2B), Color(0xFFEDEDE6), Color(0xFF3A3A3A)
            ),
            faceStyles = listOf(BalloonShapeStyle.CAT_FACE, BalloonShapeStyle.SMILEY, BalloonShapeStyle.FUNNY),
            pattern = BalloonPattern.POLKA,
            glossStrength = 1.0f,
            description = "Monochrome panda charm with playful polka spots."
        ),
        BalloonSkin(
            id = "balloon_golden_luxe",
            name = "Golden Luxe",
            icon = "👑",
            cost = 60,
            currency = CurrencyType.GEMS,
            tier = 7,
            palette = listOf(
                Color(0xFFFFD700), Color(0xFFF6C453), Color(0xFFE8B54D), Color(0xFFC99700)
            ),
            faceStyles = listOf(BalloonShapeStyle.STAR_SHAPE, BalloonShapeStyle.COOL, BalloonShapeStyle.SMILEY),
            pattern = BalloonPattern.SPARKLE,
            glossStrength = 1.4f,
            description = "The crown jewel — liquid gold with stardust sparkle."
        )
    )

    val SHOP_HAMMERS = listOf(
        HammerSkin(
            id = "hammer_wood",
            name = "Oak Wooden Mallet",
            icon = "🪵",
            cost = 0,
            currency = CurrencyType.COINS,
            tier = 1,
            headColor = Color(0xFF8D5B4C),
            handleColor = Color(0xFFD4A373),
            sparkColor = Color(0xFFE9C46A),
            description = "Sturdy beginner oak mallet."
        ),
        HammerSkin(
            id = "hammer_iron",
            name = "Iron Sledgehammer",
            icon = "🔨",
            cost = 200,
            currency = CurrencyType.COINS,
            tier = 2,
            headColor = Color(0xFF9E9E9E),
            handleColor = Color(0xFF424242),
            sparkColor = Color(0xFFFFD166),
            description = "Heavy forged iron with dense impact shockwave."
        ),
        HammerSkin(
            id = "hammer_golden",
            name = "Gilded War Hammer",
            icon = "👑",
            cost = 500,
            currency = CurrencyType.COINS,
            tier = 3,
            headColor = Color(0xFFFFD700),
            handleColor = Color(0xFFB8860B),
            sparkColor = Color(0xFFFFF275),
            description = "Royal gold hammer that showers golden coins."
        ),
        HammerSkin(
            id = "hammer_cyber",
            name = "Clockwork Smasher",
            icon = "⚡",
            cost = 1000,
            currency = CurrencyType.COINS,
            tier = 4,
            headColor = Color(0xFFC99700),
            handleColor = Color(0xFF6B4226),
            sparkColor = Color(0xFFF2B134),
            description = "Brass clockwork smasher with ticking gear sparks."
        ),
        HammerSkin(
            id = "hammer_thunder",
            name = "Thunder Mjolnir",
            icon = "🌩️",
            cost = 15,
            currency = CurrencyType.GEMS,
            tier = 5,
            headColor = Color(0xFF4CC9F0),
            handleColor = Color(0xFF3F37C9),
            sparkColor = Color(0xFFC2436B),
            description = "Mythic thunder hammer forged in storm clouds."
        ),
        HammerSkin(
            id = "hammer_diamond",
            name = "Diamond Crusher",
            icon = "💎",
            cost = 35,
            currency = CurrencyType.GEMS,
            tier = 6,
            headColor = Color(0xFF4A90C4),
            handleColor = Color(0xFFE0AAFF),
            sparkColor = Color(0xFFFFFFFF),
            description = "Pristine faceted diamond hammer with prismatic flares."
        ),
        HammerSkin(
            id = "hammer_cosmic",
            name = "Cosmic God Smasher",
            icon = "🌌",
            cost = 60,
            currency = CurrencyType.GEMS,
            tier = 7,
            headColor = Color(0xFFB03060),
            handleColor = Color(0xFF240046),
            sparkColor = Color(0xFFFFBE0B),
            description = "Celestial celestial hammer radiating starlight."
        ),
        HammerSkin(
            id = "hammer_ruby",
            name = "Ruby Storybook Mallet",
            icon = "🔨",
            cost = 750,
            currency = CurrencyType.COINS,
            tier = 4,
            headColor = Color(0xFFB03A48),
            handleColor = Color(0xFF6B4226),
            sparkColor = Color(0xFFFFD166),
            description = "Storybook mallet crowned with a cut ruby."
        ),
        HammerSkin(
            id = "hammer_emerald",
            name = "Emerald Grove Hammer",
            icon = "🌿",
            cost = 25,
            currency = CurrencyType.GEMS,
            tier = 6,
            headColor = Color(0xFF0E7C61),
            handleColor = Color(0xFF14532D),
            sparkColor = Color(0xFFA7F3D0),
            description = "Forged in the Twilight Grove, humming with leaf-light."
        ),
        HammerSkin(
            id = "hammer_storybook",
            name = "Golden Storybook Mallet",
            icon = "📖",
            cost = 45,
            currency = CurrencyType.GEMS,
            tier = 7,
            headColor = Color(0xFFF6C453),
            handleColor = Color(0xFF7B341E),
            sparkColor = Color(0xFFFFF3C4),
            description = "The royal mallet from the last page of the storybook."
        )
    )

    val SHOP_FRAMES = listOf(
        PlayFrame(
            id = "frame_minimal",
            name = "Minimal Frame",
            icon = "🔲",
            cost = 0,
            currency = CurrencyType.COINS,
            tier = 1,
            borderColors = listOf(Color(0x60FFFFFF), Color(0x30FFFFFF)),
            glowColor = Color(0x20FFFFFF),
            description = "Clean, sleek border for pure focus."
        ),
        PlayFrame(
            id = "frame_neon",
            name = "Storybook Glow Frame",
            icon = "✨",
            cost = 250,
            currency = CurrencyType.COINS,
            tier = 2,
            borderColors = listOf(Color(0xFFF6C453), Color(0xFFC2436B)),
            glowColor = Color(0xFFF6C453),
            description = "Warm storybook glow border with gilded jewel corners."
        ),
        PlayFrame(
            id = "frame_royal",
            name = "Royal Gilded Gold",
            icon = "⚜️",
            cost = 600,
            currency = CurrencyType.COINS,
            tier = 3,
            borderColors = listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFFD700)),
            glowColor = Color(0xFFFFD700),
            description = "Imperial ornate gilded baroque filigree frame."
        ),
        PlayFrame(
            id = "frame_cyber",
            name = "Clockwork Matrix Frame",
            icon = "🌐",
            cost = 1200,
            currency = CurrencyType.COINS,
            tier = 4,
            borderColors = listOf(Color(0xFF2F6DB3), Color(0xFF2FA36B)),
            glowColor = Color(0xFF2FA36B),
            description = "Brass gearwork bracket corners and amber conduits."
        ),
        PlayFrame(
            id = "frame_aurora",
            name = "Cosmic Aurora",
            icon = "🌌",
            cost = 20,
            currency = CurrencyType.GEMS,
            tier = 5,
            borderColors = listOf(Color(0xFF9D4EDD), Color(0xFFC93A5E), Color(0xFF2F6DB3)),
            glowColor = Color(0xFF9D4EDD),
            description = "Holographic nebula border pulsing with celestial runes."
        ),
        PlayFrame(
            id = "frame_diamond",
            name = "Prismatic Diamond",
            icon = "💎",
            cost = 45,
            currency = CurrencyType.GEMS,
            tier = 6,
            borderColors = listOf(Color(0xFFE0AAFF), Color(0xFF4A90C4), Color(0xFFFFFFFF)),
            glowColor = Color(0xFF4A90C4),
            description = "Multi-faceted diamond cut frame with radiant shine."
        ),
        PlayFrame(
            id = "frame_storybook",
            name = "Storybook Arch",
            icon = "📖",
            cost = 900,
            currency = CurrencyType.COINS,
            tier = 4,
            borderColors = listOf(Color(0xFFF6C453), Color(0xFFB7791F), Color(0xFFF6C453)),
            glowColor = Color(0xFFF6C453),
            description = "A gilded storybook arch framing every adventure."
        ),
        PlayFrame(
            id = "frame_emerald",
            name = "Emerald Grove",
            icon = "🌿",
            cost = 1500,
            currency = CurrencyType.COINS,
            tier = 5,
            borderColors = listOf(Color(0xFF0E7C61), Color(0xFFA7F3D0), Color(0xFF0E7C61)),
            glowColor = Color(0xFF0E7C61),
            description = "Living emerald vines woven into a grove border."
        ),
        PlayFrame(
            id = "frame_ruby",
            name = "Ruby Royale",
            icon = "♦️",
            cost = 30,
            currency = CurrencyType.GEMS,
            tier = 6,
            borderColors = listOf(Color(0xFFB03A48), Color(0xFFFFD166), Color(0xFFB03A48)),
            glowColor = Color(0xFFB03A48),
            description = "Ruby and gold — the crown-jewel frame."
        )
    )

    val SHOP_BLADES = listOf(
        BladeItem("blade_classic", "Classic Pointer", "🖐️", 0, CurrencyType.COINS, Color(0xFF2F6DB3), Color(0xFF80D8FF), "Standard responsive pointer touch trail."),
        BladeItem("blade_katana", "Shadow Katana", "🗡️", 350, CurrencyType.COINS, Color(0xFFFF1744), Color(0xFFFF8A80), "Swift sharp edge leaving sleek crimson cuts."),
        BladeItem("blade_laser", "Laser Sabre", "⚔️", 500, CurrencyType.COINS, Color(0xFF2F6DB3), Color(0xFF00B0FF), "Blinding sci-fi laser slicing energy beam."),
        BladeItem("blade_flame", "Flame Blaster", "🔥⚔️", 800, CurrencyType.COINS, Color(0xFFFF5722), Color(0xFFFFAB40), "Searing ignited blade that burns rising balloons."),
        BladeItem("blade_frost", "Glacial Frostbite", "❄️🗡️", 1200, CurrencyType.COINS, Color(0xFF4A90C4), Color(0xFFB388FF), "Sub-zero chilled rapier with freezing trails."),
        BladeItem("blade_wand", "Cosmic Magic Wand", "🪄", 1800, CurrencyType.COINS, Color(0xFF9D4EDD), Color(0xFF7C4DBD), "Arcane sorcerer's wand radiating mystical stardust."),
        BladeItem("blade_cyber", "Clockwork Scythe", "⚡", 2500, CurrencyType.COINS, Color(0xFFC99700), Color(0xFFF2B134), "Clockwork scythe of polished brass and amber."),
        BladeItem("blade_dragon", "Dragon Fang Blade", "🐉🗡️", 30, CurrencyType.GEMS, Color(0xFFFF9100), Color(0xFFFF3D00), "Forged from an elder dragon's tooth with ember bursts."),
        BladeItem("blade_celestial", "Celestial God Cleaver", "👑⚔️", 65, CurrencyType.GEMS, Color(0xFFFFD700), Color(0xFFFFF176), "Divine mythical weapon bathed in blinding gold."),
        BladeItem("blade_void", "Void Reaper Edge", "🌑🗡️", 90, CurrencyType.GEMS, Color(0xFF5E2A9C), Color(0xFF3D0066), "Abyssal cosmic blade slicing through dark matter."),
        BladeItem("blade_ruby", "Ruby Edge", "♦️🗡️", 2000, CurrencyType.COINS, Color(0xFFC1272D), Color(0xFFF4A261), "A storybook blade with a ruby-cut edge."),
        BladeItem("blade_grove", "Grove Leaf Blade", "🌿🗡️", 45, CurrencyType.GEMS, Color(0xFF2E9E6B), Color(0xFFA7F3D0), "Leaf-forged blade from the heart of the grove."),
        BladeItem("blade_storybook", "Storybook Quill", "🪶", 75, CurrencyType.GEMS, Color(0xFFF6C453), Color(0xFFFFF3C4), "A golden quill that writes trails of light.")
    )

    val SHOP_FLAMES = listOf(
        FlameItem(
            id = "flame_ember",
            name = "Crimson Ember",
            icon = "🔥",
            cost = 0,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFFFF4500),
            glowColor = Color(0xFFFF8C00),
            emberColor = Color(0xFFFFD700),
            description = "Flickering fiery ember sparks on every tap.",
            critPopBonusPercent = 5
        ),
        FlameItem(
            id = "flame_phoenix",
            name = "Phoenix Blazing Fire",
            icon = "🦅🔥",
            cost = 450,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFFFF3D00),
            glowColor = Color(0xFFFF9100),
            emberColor = Color(0xFFFFE57F),
            description = "Mythic bird's immortal flame with soaring golden sparks.",
            critPopBonusPercent = 10
        ),
        FlameItem(
            id = "flame_cyber_plasma",
            name = "Moonfire Aura",
            icon = "⚡🔥",
            cost = 900,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFF7C4DBD),
            glowColor = Color(0xFF9D8BD0),
            emberColor = Color(0xFFE8E4F5),
            description = "Moonlit violet aura trailing silver starlight embers.",
            critPopBonusPercent = 15
        ),
        FlameItem(
            id = "flame_soul_fire",
            name = "Ghost Soul Flame",
            icon = "👻🔥",
            cost = 1600,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFF57B8A2),
            glowColor = Color(0xFF0077B6),
            emberColor = Color(0xFF90E0EF),
            description = "Ethereal cyan phantom flame burning with mystical energy.",
            critPopBonusPercent = 20
        ),
        FlameItem(
            id = "flame_dragon_fury",
            name = "Dragon Infernal Fury",
            icon = "🐉🔥",
            cost = 25,
            currency = CurrencyType.GEMS,
            primaryColor = Color(0xFFC1272D),
            glowColor = Color(0xFFFF5400),
            emberColor = Color(0xFFFFD000),
            description = "Ancient dragon fire that scorches balloons with radiant flares.",
            critPopBonusPercent = 25
        ),
        FlameItem(
            id = "flame_solar_flare",
            name = "Solar Flare Corona",
            icon = "☀️🔥",
            cost = 50,
            currency = CurrencyType.GEMS,
            primaryColor = Color(0xFFFFEE00),
            glowColor = Color(0xFFFF3300),
            emberColor = Color(0xFFFFFFFF),
            description = "Blinding solar tempest radiating golden coronal mass.",
            critPopBonusPercent = 35
        ),
        FlameItem(
            id = "flame_void_abyss",
            name = "Void Abyssal Flame",
            icon = "🌌🔥",
            cost = 75,
            currency = CurrencyType.GEMS,
            primaryColor = Color(0xFFBF00FF),
            glowColor = Color(0xFF1D0047),
            emberColor = Color(0xFFE0AAFF),
            description = "Cosmic dark matter flame pulling starlight into oblivion.",
            critPopBonusPercent = 50
        ),
        FlameItem(
            id = "flame_lantern",
            name = "Lantern Glow",
            icon = "🏮",
            cost = 700,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFFF6C453),
            glowColor = Color(0xFFE76F51),
            emberColor = Color(0xFFFFF3C4),
            description = "A warm storybook lantern glow on every tap.",
            critPopBonusPercent = 12
        ),
        FlameItem(
            id = "flame_emerald",
            name = "Emerald Hearth",
            icon = "💚🔥",
            cost = 2000,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFF0E7C61),
            glowColor = Color(0xFF2E9E6B),
            emberColor = Color(0xFFA7F3D0),
            description = "Grove-heart emerald fire, calm and deep.",
            critPopBonusPercent = 22
        ),
        FlameItem(
            id = "flame_ruby",
            name = "Ruby Hearth Fire",
            icon = "♦️🔥",
            cost = 40,
            currency = CurrencyType.GEMS,
            primaryColor = Color(0xFFC1272D),
            glowColor = Color(0xFFB03A48),
            emberColor = Color(0xFFFFD166),
            description = "Ruby hearth fire with golden ember sparks.",
            critPopBonusPercent = 30
        )
    )

    val SHOP_THEMES = listOf(
        ThemeConfig("theme_sunset_twilight", "Sunset Twilight", "🌅", 0, CurrencyType.COINS, listOf(Color(0xFF1B0B38), Color(0xFF330D52), Color(0xFF5A1466), Color(0xFF8C1D5D), Color(0xFFC43345), Color(0xFFE65100), Color(0xFFFFA726))),
        ThemeConfig("theme_obsidian", "Midnight Obsidian", "🖤", 0, CurrencyType.COINS, listOf(Color(0xFF000000), Color(0xFF07070C), Color(0xFF0F0F1A), Color(0xFF05050A))),
        ThemeConfig("theme_neon_cyber", "Midnight Storybook", "⚡", 300, CurrencyType.COINS, listOf(Color(0xFF171233), Color(0xFF241A4D), Color(0xFF31235E), Color(0xFF4A3418))),
        ThemeConfig("theme_crimson_black", "Crimson Eclipse", "🔴", 400, CurrencyType.COINS, listOf(Color(0xFF0C0206), Color(0xFF1F040E), Color(0xFF2D0714), Color(0xFF080104))),
        ThemeConfig("theme_dark_galaxy", "Cosmic Galaxy", "🌌", 600, CurrencyType.COINS, listOf(Color(0xFF030008), Color(0xFF0C051B), Color(0xFF140827), Color(0xFF020006))),
        ThemeConfig("theme_golden_onyx", "Royal Golden Onyx", "👑", 1500, CurrencyType.COINS, listOf(Color(0xFF050401), Color(0xFF141006), Color(0xFF1A1303), Color(0xFF060501)))
    )

    val AVAILABLE_SKILLS = listOf(
        PlayerSkill(
            id = "skill_slice",
            type = SkillType.HAMMER_SLIDE,
            name = "Hammer Slide",
            icon = "🔨⚡",
            description = "Supercharge your hammer! Allows you to freely slide and smash balloons continuously across the screen for 5 seconds!",
            unlockCostCoins = 800,
            unlockCostGems = 10,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 20f,
            cooldownSeconds = 18f,
            accentColor = Color(0xFFFFD700)
        ),
        PlayerSkill(
            id = "skill_freeze",
            type = SkillType.TIME_FREEZE,
            name = "Chrono Stasis",
            icon = "⏱️",
            description = "Slow down all rising balloons by 75% for 6 seconds to execute precision combos and chain pops.",
            unlockCostCoins = 1800,
            unlockCostGems = 25,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 20f,
            accentColor = Color(0xFF4A90C4)
        ),
        PlayerSkill(
            id = "skill_magnet",
            type = SkillType.MAGNET_PULL,
            name = "Vortex Magnet",
            icon = "🧲",
            description = "Create a magnetic singularity that pulls golden and rainbow balloons directly to your touch point!",
            unlockCostCoins = 3200,
            unlockCostGems = 45,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 24f,
            accentColor = Color(0xFFC93A5E)
        ),
        PlayerSkill(
            id = "skill_shockwave",
            type = SkillType.SHOCKWAVE,
            name = "Supernova Blast",
            icon = "💥",
            description = "Release a massive radial shockwave clearing all standard balloons on screen and showering bonus coins!",
            unlockCostCoins = 6000,
            unlockCostGems = 80,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 30f,
            accentColor = Color(0xFFFFB703)
        )
    )

    val DAILY_STREAK_TIMELINE = listOf(
        DailyStreakDay(1, 200, 2, "Day 1"),
        DailyStreakDay(2, 400, 5, "Day 2"),
        DailyStreakDay(3, 650, 8, "Day 3"),
        DailyStreakDay(4, 950, 12, "Day 4"),
        DailyStreakDay(5, 1400, 18, "Day 5"),
        DailyStreakDay(6, 2000, 25, "Day 6"),
        DailyStreakDay(7, 5000, 60, "Day 7 JACKPOT 🎉", isJackpot = true)
    )

    val WORLD_ZONES = listOf(
        WorldZone(1, "Twilight Grove", "Stages 1 - 10", 1, 10, Color(0xFF2FA36B), Color(0xFFF6C453), listOf(Color(0xFF0E3B2E), Color(0xFF14532D), Color(0xFF08231B)), "🌱"),
        WorldZone(2, "Prism Peaks", "Stages 11 - 20", 11, 20, Color(0xFF7C4DBD), Color(0xFFC2436B), listOf(Color(0xFF261042), Color(0xFF3F196A), Color(0xFF18072E)), "🔮"),
        WorldZone(3, "Sunset Mesa", "Stages 21 - 30", 21, 30, Color(0xFFFF7A00), Color(0xFFB03060), listOf(Color(0xFF3A1224), Color(0xFF5E1B38), Color(0xFF240A18)), "🌅"),
        WorldZone(4, "Aqua Reef", "Stages 31 - 40", 31, 40, Color(0xFF2E86AB), Color(0xFF35B5A0), listOf(Color(0xFF0A2239), Color(0xFF124559), Color(0xFF051622)), "🐬"),
        WorldZone(5, "Candy Wonderland", "Stages 41 - 50", 41, 50, Color(0xFFD96C8A), Color(0xFFFF9770), listOf(Color(0xFF3D1635), Color(0xFF5C1B4A), Color(0xFF260D21)), "🍭"),
        WorldZone(6, "Clockwork City", "Stages 51 - 60", 51, 60, Color(0xFFC99700), Color(0xFFF2B134), listOf(Color(0xFF2A2118), Color(0xFF3D2C1A), Color(0xFF1A130C)), "⚙️"),
        WorldZone(7, "Volcano Caldera", "Stages 61 - 70", 61, 70, Color(0xFFB23A48), Color(0xFFFFB703), listOf(Color(0xFF3A0D15), Color(0xFF5A1420), Color(0xFF22050B)), "🌋"),
        WorldZone(8, "Starry Cosmos", "Stages 71 - 80", 71, 80, Color(0xFF7209B7), Color(0xFF4CC9F0), listOf(Color(0xFF160A2C), Color(0xFF2B1354), Color(0xFF0E051C)), "🌌"),
        WorldZone(9, "Golden Citadel", "Stages 81 - 90", 81, 90, Color(0xFFFFD700), Color(0xFFFFA000), listOf(Color(0xFF382608), Color(0xFF593C0C), Color(0xFF241804)), "🏰"),
        WorldZone(10, "Frozen Tundra", "Stages 91 - 100", 91, 100, Color(0xFFA2D2FF), Color(0xFFBDE0FE), listOf(Color(0xFF102A43), Color(0xFF1A446C), Color(0xFF0B1D2E)), "❄️"),
        WorldZone(11, "Sakura Sanctuary", "Stages 101 - 110", 101, 110, Color(0xFFFF9EAA), Color(0xFFFFD1DC), listOf(Color(0xFF3B152B), Color(0xFF5A2041), Color(0xFF240D1B)), "🌸"),
        WorldZone(12, "Thunder Spire", "Stages 111 - 120", 111, 120, Color(0xFFC2436B), Color(0xFF4361EE), listOf(Color(0xFF1B1B3A), Color(0xFF2F2F64), Color(0xFF101026)), "🌩️"),
        WorldZone(13, "Emerald Jungle", "Stages 121 - 130", 121, 130, Color(0xFF38B000), Color(0xFF70E000), listOf(Color(0xFF0B2E13), Color(0xFF144D21), Color(0xFF061C0B)), "🌴"),
        WorldZone(14, "Cosmic Void", "Stages 131 - 140", 131, 140, Color(0xFF8E3A8F), Color(0xFF7209B7), listOf(Color(0xFF220538), Color(0xFF3B0861), Color(0xFF130221)), "🪐"),
        WorldZone(15, "Celestial Ascension", "Stages 141 - 150", 141, 150, Color(0xFFFFD700), Color(0xFFF6C453), listOf(Color(0xFF29104D), Color(0xFF481A85), Color(0xFF1A0A33)), "👑"),
        WorldZone(16, "Molten Core", "Stages 151 - 160", 151, 160, Color(0xFFFF5722), Color(0xFFFF9100), listOf(Color(0xFF350B05), Color(0xFF541208), Color(0xFF200703)), "☄️"),
        WorldZone(17, "Midnight Abyss", "Stages 161 - 170", 161, 170, Color(0xFF6C5FBF), Color(0xFFB8A9D9), listOf(Color(0xFF1B1040), Color(0xFF2D1B69), Color(0xFF120A2E)), "🌌"),
        WorldZone(18, "Olympus Gates", "Stages 171 - 180", 171, 180, Color(0xFFFFD700), Color(0xFFFFF176), listOf(Color(0xFF2C2204), Color(0xFF473607), Color(0xFF191302)), "🏛️"),
        WorldZone(19, "Dragon's Lair", "Stages 181 - 190", 181, 190, Color(0xFFFF1744), Color(0xFFFF9100), listOf(Color(0xFF3E0812), Color(0xFF5D0D1B), Color(0xFF25050B)), "🐲"),
        WorldZone(20, "Grand Pantheon", "Stages 191 - 200", 191, 200, Color(0xFFB03060), Color(0xFFFFD700), listOf(Color(0xFF240438), Color(0xFF420866), Color(0xFF150221)), "🏆")
    )

    val CAMPAIGN_LEVELS: List<CampaignLevel> = (1..200).map { lvl ->
        val recipe = StageRecipes.getRecipe(lvl)
        val isBossStage = recipe.role == StagePacingRole.CELEBRATION
        val isFrenzyStage = (lvl % 5 == 0 && !isBossStage)

        val baseCoins = if (lvl <= 5) 250 + lvl * 50 else 300 + lvl * 30
        val baseGems = when {
            isBossStage -> 30 + (lvl / 10) * 8
            isFrenzyStage -> 12 + (lvl / 20) * 2
            lvl <= 5 -> 5
            else -> 6 + (lvl / 20)
        }

        CampaignLevel(
            level = lvl,
            targetPops = recipe.targetPops,
            targetScore = if (isBossStage) 1500 + lvl * 350 else 500 + recipe.targetPops * 40,
            timeLimit = recipe.timeLimit,
            maxBombs = if (lvl < 4) 0 else if (isBossStage) (lvl / 8).coerceIn(2, 6) else (lvl / 10).coerceIn(1, 8),
            rewardCoins = if (isBossStage) baseCoins + 1000 else baseCoins,
            rewardGems = baseGems,
            maxMissesAllowed = recipe.maxMisses
        )
    }

    val STAR_MILESTONES = listOf(
        StarMilestoneReward(15, 500, 5, "Novice Explorer", "🌟"),
        StarMilestoneReward(30, 1000, 10, "Star Chaser", "⭐"),
        StarMilestoneReward(60, 2000, 20, "Constellation Hero", "✨"),
        StarMilestoneReward(100, 3500, 35, "Glade Master", "🏆"),
        StarMilestoneReward(150, 5000, 50, "Astral Voyager", "🌌"),
        StarMilestoneReward(220, 7500, 75, "Cosmic Champion", "👑"),
        StarMilestoneReward(300, 10000, 100, "Mythic Ascendant", "🪐"),
        StarMilestoneReward(400, 15000, 150, "Celestial Legend", "⚡"),
        StarMilestoneReward(500, 20000, 200, "Sky Sovereign", "🔥"),
        StarMilestoneReward(600, 35000, 350, "Grand Galaxy God", "🏆👑")
    )

    val WHEEL_SEGMENTS = listOf(
        WheelSegment("250 🪙", 250, 0, Color(0xFFC93A5E)),
        WheelSegment("500 🪙", 500, 0, Color(0xFF9D4EDD)),
        WheelSegment("10 💎", 0, 10, Color(0xFF2F6DB3)),
        WheelSegment("750 🪙", 750, 0, Color(0xFFFFB703)),
        WheelSegment("1,500 🪙", 1500, 0, Color(0xFF2FA36B)),
        WheelSegment("25 💎", 0, 25, Color(0xFFFF758C))
    )

    val DEFAULT_QUESTS = listOf(
        // Rotating Daily Challenges
        QuestItem("quest_pop_50", "Pop 40 Balloons", 40, 200, 1, QuestCategory.DAILY, "🎈"),
        QuestItem("quest_combo_5", "Reach 5x Combo", 5, 250, 2, QuestCategory.DAILY, "🔥"),
        QuestItem("quest_spin_wheel", "Spin the Daily Wheel", 1, 200, 2, QuestCategory.DAILY, "🎡"),
        QuestItem("quest_stage_clear_3star", "Clear Any Stage with 3 Stars", 1, 350, 3, QuestCategory.DAILY, "⭐"),
        QuestItem("quest_special_pop_10", "Pop 10 Special/Golden Balloons", 10, 300, 3, QuestCategory.DAILY, "🪙"),
        // Weekly Hero Challenges
        QuestItem("quest_pop_200", "Pop 250 Total Balloons", 250, 800, 8, QuestCategory.WEEKLY, "💥"),
        QuestItem("quest_combo_10", "Unleash a 10x Mega Combo", 10, 1000, 10, QuestCategory.WEEKLY, "⚡"),
        QuestItem("quest_stage_5", "Conquer 5 Campaign Stages", 5, 600, 6, QuestCategory.WEEKLY, "🗺️"),
        QuestItem("quest_stage_10", "Conquer 10 Campaign Stages", 10, 1200, 15, QuestCategory.WEEKLY, "🏆"),
        QuestItem("quest_stars_15", "Collect 15 Campaign Stars", 15, 1500, 20, QuestCategory.WEEKLY, "🌟")
    )

    val ACHIEVEMENTS = listOf(
        AchievementItem("ach_first_pop", "First Pop!", "Pop your first balloon ever", "🎈", 1, 150, 2),
        AchievementItem("ach_pops_100", "Balloon Apprentice", "Pop 100 balloons across all modes", "🥉", 100, 400, 4),
        AchievementItem("ach_pops_500", "Balloon Master", "Pop 500 balloons across all modes", "🥈", 500, 1200, 12),
        AchievementItem("ach_pops_2000", "Sky Legend", "Pop 2,000 balloons across all modes", "👑", 2000, 3500, 40),
        AchievementItem("ach_combo_6", "Flow State", "Reach a 6x pop combo", "🔥", 6, 350, 4),
        AchievementItem("ach_combo_12", "Combo God", "Reach a 12x pop combo", "⚡", 12, 1500, 15),
        AchievementItem("ach_stars_10", "Stargazer", "Earn 10 stars in Campaign", "⭐", 10, 500, 5),
        AchievementItem("ach_stars_30", "Constellation Explorer", "Earn 30 stars in Campaign", "🌟", 30, 1500, 15),
        AchievementItem("ach_stars_75", "Galaxy Conqueror", "Earn 75 stars in Campaign", "✨", 75, 3000, 35),
        AchievementItem("ach_wheel_5", "Lucky Spinner", "Spin the Daily Wheel 5 times", "🎡", 5, 500, 5),
        AchievementItem("ach_stage_10", "Glade Champion", "Complete 10 Campaign stages", "🏆", 10, 800, 10),
        AchievementItem("ach_stage_30", "Mesa Veteran", "Complete 30 Campaign stages", "🏰", 30, 2000, 25)
    )

    val DIAMOND_STORE_PACKS = listOf(
        DiamondStorePack("ad_free_reward", "Watch Short Ad", 10, "FREE 🎁", "INSTANT", isAdReward = true),
        DiamondStorePack("coin_convert_pack", "Exchange 500 🪙", 10, "500 🪙", "TRADE"),
        DiamondStorePack("pack_handful", "Pouch of Gems", 30, "$0.99", "POPULAR"),
        DiamondStorePack("pack_sack", "Bag of Gems", 100, "$2.99", "+20% EXTRA"),
        DiamondStorePack("pack_chest", "Vault of Gems", 350, "$8.99", "+50% VALUE"),
        DiamondStorePack("pack_vault", "King's Treasury", 1000, "$19.99", "BEST VALUE")
    )
}
