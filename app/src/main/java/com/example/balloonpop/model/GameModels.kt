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
    var vx: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    var isStar: Boolean = false
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
    var vy: Float = -2.5f
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
    val color: Color = Color(0xFF00F0FF),
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
    )
}

enum class GameMode(val displayName: String, val desc: String, val icon: String) {
    CAMPAIGN("Story Campaign", "50 Stages • Star Rewards", "🗺️"),
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
    val accentColor: Color = Color(0xFF00F0FF)
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
    val BALLOON_PALETTE = listOf(
        Color(0xFFFF2A75),
        Color(0xFF00F0FF),
        Color(0xFF9D4EDD),
        Color(0xFFFFB703),
        Color(0xFF06D6A0),
        Color(0xFFFF5722),
        Color(0xFFE040FB),
        Color(0xFF00E676),
        Color(0xFFFF007F),
        Color(0xFF4361EE)
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
            name = "Cyber Neon Smasher",
            icon = "⚡",
            cost = 1000,
            currency = CurrencyType.COINS,
            tier = 4,
            headColor = Color(0xFF00F0FF),
            handleColor = Color(0xFF7209B7),
            sparkColor = Color(0xFF00F0FF),
            description = "High-tech plasma hammer with electrical arcs."
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
            sparkColor = Color(0xFFF72585),
            description = "Mythic thunder hammer forged in storm clouds."
        ),
        HammerSkin(
            id = "hammer_diamond",
            name = "Diamond Crusher",
            icon = "💎",
            cost = 35,
            currency = CurrencyType.GEMS,
            tier = 6,
            headColor = Color(0xFF00E5FF),
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
            headColor = Color(0xFFFF007F),
            handleColor = Color(0xFF240046),
            sparkColor = Color(0xFFFFBE0B),
            description = "Celestial celestial hammer radiating starlight."
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
            name = "Aesthetic Neon",
            icon = "✨",
            cost = 250,
            currency = CurrencyType.COINS,
            tier = 2,
            borderColors = listOf(Color(0xFFFF2A75), Color(0xFF00F0FF)),
            glowColor = Color(0xFF00F0FF),
            description = "Vibrant dual neon glowing border with crystal corners."
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
            name = "Cyber Matrix HUD",
            icon = "🌐",
            cost = 1200,
            currency = CurrencyType.COINS,
            tier = 4,
            borderColors = listOf(Color(0xFF00F0FF), Color(0xFF06D6A0)),
            glowColor = Color(0xFF06D6A0),
            description = "Sci-fi tactical bracket corners and data conduits."
        ),
        PlayFrame(
            id = "frame_aurora",
            name = "Cosmic Aurora",
            icon = "🌌",
            cost = 20,
            currency = CurrencyType.GEMS,
            tier = 5,
            borderColors = listOf(Color(0xFF9D4EDD), Color(0xFFFF2A75), Color(0xFF00F0FF)),
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
            borderColors = listOf(Color(0xFFE0AAFF), Color(0xFF00E5FF), Color(0xFFFFFFFF)),
            glowColor = Color(0xFF00E5FF),
            description = "Multi-faceted diamond cut frame with radiant shine."
        )
    )

    val SHOP_BLADES = listOf(
        BladeItem("blade_classic", "Classic Pointer", "🖐️", 0, CurrencyType.COINS, Color(0xFF00F0FF), Color(0xFF80D8FF), "Standard responsive pointer touch trail."),
        BladeItem("blade_katana", "Shadow Katana", "🗡️", 350, CurrencyType.COINS, Color(0xFFFF1744), Color(0xFFFF8A80), "Swift sharp edge leaving sleek crimson cuts."),
        BladeItem("blade_laser", "Laser Sabre", "⚔️", 500, CurrencyType.COINS, Color(0xFF00F0FF), Color(0xFF00B0FF), "Blinding sci-fi laser slicing energy beam."),
        BladeItem("blade_flame", "Flame Blaster", "🔥⚔️", 800, CurrencyType.COINS, Color(0xFFFF5722), Color(0xFFFFAB40), "Searing ignited blade that burns rising balloons."),
        BladeItem("blade_frost", "Glacial Frostbite", "❄️🗡️", 1200, CurrencyType.COINS, Color(0xFF00E5FF), Color(0xFFB388FF), "Sub-zero chilled rapier with freezing trails."),
        BladeItem("blade_wand", "Cosmic Magic Wand", "🪄", 1800, CurrencyType.COINS, Color(0xFF9D4EDD), Color(0xFFE040FB), "Arcane sorcerer's wand radiating mystical stardust."),
        BladeItem("blade_cyber", "Cyber Scythe", "⚡", 2500, CurrencyType.COINS, Color(0xFF06D6A0), Color(0xFF00E676), "Futuristic hyper-tech neon scythe."),
        BladeItem("blade_dragon", "Dragon Fang Blade", "🐉🗡️", 30, CurrencyType.GEMS, Color(0xFFFF9100), Color(0xFFFF3D00), "Forged from an elder dragon's tooth with ember bursts."),
        BladeItem("blade_celestial", "Celestial God Cleaver", "👑⚔️", 65, CurrencyType.GEMS, Color(0xFFFFD700), Color(0xFFFFF176), "Divine mythical weapon bathed in blinding gold."),
        BladeItem("blade_void", "Void Reaper Edge", "🌑🗡️", 90, CurrencyType.GEMS, Color(0xFFE040FB), Color(0xFF3D0066), "Abyssal cosmic blade slicing through dark matter.")
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
            name = "Cyber Plasma Fire",
            icon = "⚡🔥",
            cost = 900,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFF00F0FF),
            glowColor = Color(0xFF7000FF),
            emberColor = Color(0xFF00E5FF),
            description = "Electric neon blue-violet high temperature plasma arc.",
            critPopBonusPercent = 15
        ),
        FlameItem(
            id = "flame_soul_fire",
            name = "Ghost Soul Flame",
            icon = "👻🔥",
            cost = 1600,
            currency = CurrencyType.COINS,
            primaryColor = Color(0xFF00FFCC),
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
            primaryColor = Color(0xFFFF0055),
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
        )
    )

    val SHOP_THEMES = listOf(
        ThemeConfig("theme_sunset_twilight", "Sunset Twilight", "🌅", 0, CurrencyType.COINS, listOf(Color(0xFF1B0B38), Color(0xFF330D52), Color(0xFF5A1466), Color(0xFF8C1D5D), Color(0xFFC43345), Color(0xFFE65100), Color(0xFFFFA726))),
        ThemeConfig("theme_obsidian", "Midnight Obsidian", "🖤", 0, CurrencyType.COINS, listOf(Color(0xFF000000), Color(0xFF07070C), Color(0xFF0F0F1A), Color(0xFF05050A))),
        ThemeConfig("theme_neon_cyber", "Cyberpunk Neon", "⚡", 300, CurrencyType.COINS, listOf(Color(0xFF050508), Color(0xFF0A101C), Color(0xFF16091D), Color(0xFF020205))),
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
            accentColor = Color(0xFF00E5FF)
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
            accentColor = Color(0xFFFF2A75)
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
        WorldZone(1, "Neon Glade", "Stages 1 - 10", 1, 10, Color(0xFF00F0FF), Color(0xFF06D6A0), listOf(Color(0xFF1B0E3D), Color(0xFF2A1556), Color(0xFF130930)), "🌱"),
        WorldZone(2, "Prism Peaks", "Stages 11 - 20", 11, 20, Color(0xFF9D4EDD), Color(0xFFFF2A75), listOf(Color(0xFF261042), Color(0xFF3F196A), Color(0xFF18072E)), "🔮"),
        WorldZone(3, "Sunset Mesa", "Stages 21 - 30", 21, 30, Color(0xFFFF7A00), Color(0xFFFF007F), listOf(Color(0xFF3A1224), Color(0xFF5E1B38), Color(0xFF240A18)), "🌅"),
        WorldZone(4, "Aqua Reef", "Stages 31 - 40", 31, 40, Color(0xFF00D2FF), Color(0xFF00F5D4), listOf(Color(0xFF0A2239), Color(0xFF124559), Color(0xFF051622)), "🐬"),
        WorldZone(5, "Candy Wonderland", "Stages 41 - 50", 41, 50, Color(0xFFFF70A6), Color(0xFFFF9770), listOf(Color(0xFF3D1635), Color(0xFF5C1B4A), Color(0xFF260D21)), "🍭"),
        WorldZone(6, "Cyber Metropolis", "Stages 51 - 60", 51, 60, Color(0xFF06D6A0), Color(0xFF00F0FF), listOf(Color(0xFF112233), Color(0xFF173852), Color(0xFF0B1622)), "⚡"),
        WorldZone(7, "Volcano Caldera", "Stages 61 - 70", 61, 70, Color(0xFFFF3366), Color(0xFFFFB703), listOf(Color(0xFF3A0D15), Color(0xFF5A1420), Color(0xFF22050B)), "🌋"),
        WorldZone(8, "Starry Cosmos", "Stages 71 - 80", 71, 80, Color(0xFF7209B7), Color(0xFF4CC9F0), listOf(Color(0xFF160A2C), Color(0xFF2B1354), Color(0xFF0E051C)), "🌌"),
        WorldZone(9, "Golden Citadel", "Stages 81 - 90", 81, 90, Color(0xFFFFD700), Color(0xFFFFA000), listOf(Color(0xFF382608), Color(0xFF593C0C), Color(0xFF241804)), "🏰"),
        WorldZone(10, "Frozen Tundra", "Stages 91 - 100", 91, 100, Color(0xFFA2D2FF), Color(0xFFBDE0FE), listOf(Color(0xFF102A43), Color(0xFF1A446C), Color(0xFF0B1D2E)), "❄️"),
        WorldZone(11, "Sakura Sanctuary", "Stages 101 - 110", 101, 110, Color(0xFFFF9EAA), Color(0xFFFFD1DC), listOf(Color(0xFF3B152B), Color(0xFF5A2041), Color(0xFF240D1B)), "🌸"),
        WorldZone(12, "Thunder Spire", "Stages 111 - 120", 111, 120, Color(0xFFF72585), Color(0xFF4361EE), listOf(Color(0xFF1B1B3A), Color(0xFF2F2F64), Color(0xFF101026)), "🌩️"),
        WorldZone(13, "Emerald Jungle", "Stages 121 - 130", 121, 130, Color(0xFF38B000), Color(0xFF70E000), listOf(Color(0xFF0B2E13), Color(0xFF144D21), Color(0xFF061C0B)), "🌴"),
        WorldZone(14, "Cosmic Void", "Stages 131 - 140", 131, 140, Color(0xFFB5179E), Color(0xFF7209B7), listOf(Color(0xFF220538), Color(0xFF3B0861), Color(0xFF130221)), "🪐"),
        WorldZone(15, "Celestial Ascension", "Stages 141 - 150", 141, 150, Color(0xFFFFD700), Color(0xFF00FFFF), listOf(Color(0xFF29104D), Color(0xFF481A85), Color(0xFF1A0A33)), "👑"),
        WorldZone(16, "Molten Core", "Stages 151 - 160", 151, 160, Color(0xFFFF5722), Color(0xFFFF9100), listOf(Color(0xFF350B05), Color(0xFF541208), Color(0xFF200703)), "☄️"),
        WorldZone(17, "Neon Abyss", "Stages 161 - 170", 161, 170, Color(0xFF00E5FF), Color(0xFF7C4DFF), listOf(Color(0xFF05172E), Color(0xFF0C2448), Color(0xFF020E1F)), "🧬"),
        WorldZone(18, "Olympus Gates", "Stages 171 - 180", 171, 180, Color(0xFFFFD700), Color(0xFFFFF176), listOf(Color(0xFF2C2204), Color(0xFF473607), Color(0xFF191302)), "🏛️"),
        WorldZone(19, "Dragon's Lair", "Stages 181 - 190", 181, 190, Color(0xFFFF1744), Color(0xFFFF9100), listOf(Color(0xFF3E0812), Color(0xFF5D0D1B), Color(0xFF25050B)), "🐲"),
        WorldZone(20, "Grand Pantheon", "Stages 191 - 200", 191, 200, Color(0xFFFF007F), Color(0xFFFFD700), listOf(Color(0xFF240438), Color(0xFF420866), Color(0xFF150221)), "🏆")
    )

    val CAMPAIGN_LEVELS: List<CampaignLevel> = (1..200).map { lvl ->
        val isBossStage = (lvl % 10 == 0)
        val isFrenzyStage = (lvl % 5 == 0 && !isBossStage)
        // Generous rewards matching deep playtime
        val baseCoins = if (lvl <= 5) 250 + lvl * 50 else 300 + lvl * 30
        val baseGems = when {
            isBossStage -> 30 + (lvl / 10) * 8
            isFrenzyStage -> 12 + (lvl / 20) * 2
            lvl <= 5 -> 5
            else -> 6 + (lvl / 20)
        }

        // Calibrated deep session playtime in seconds so levels provide rich engagement
        val time = when {
            isBossStage -> (80 + (lvl / 10) * 4).coerceIn(80, 120) // Epic extended boss battle
            isFrenzyStage -> (50 + (lvl / 10) * 2).coerceIn(50, 75)
            lvl == 1 -> 45
            lvl == 2 -> 48
            lvl == 3 -> 50
            lvl == 4 -> 54
            else -> (45 + (lvl % 10) * 3).coerceIn(45, 75)
        }

        // Calibrated target pops for satisfying combo chaining and multi-wave feel
        val targetPops = when {
            isBossStage -> (60 + (lvl / 5) * 4).coerceIn(60, 110) // Includes Boss Multi-HP pops!
            isFrenzyStage -> (50 + (lvl / 6) * 3).coerceIn(50, 85)
            lvl == 1 -> 35
            lvl == 2 -> 40
            lvl == 3 -> 44
            lvl == 4 -> 48
            else -> (36 + (lvl * 2)).coerceIn(36, 80)
        }

        val maxMisses = when {
            isBossStage -> 7
            lvl <= 3 -> 6
            lvl <= 20 -> 5
            lvl > 100 -> 4
            else -> 5
        }

        CampaignLevel(
            level = lvl,
            targetPops = targetPops,
            targetScore = if (isBossStage) 1500 + lvl * 350 else 500 + targetPops * 40,
            timeLimit = time,
            maxBombs = if (isBossStage) (lvl / 8).coerceIn(2, 6) else (lvl / 10).coerceIn(0, 8),
            rewardCoins = if (isBossStage) baseCoins + 1000 else baseCoins,
            rewardGems = baseGems,
            maxMissesAllowed = maxMisses
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
        WheelSegment("250 🪙", 250, 0, Color(0xFFFF2A75)),
        WheelSegment("500 🪙", 500, 0, Color(0xFF9D4EDD)),
        WheelSegment("10 💎", 0, 10, Color(0xFF00F0FF)),
        WheelSegment("750 🪙", 750, 0, Color(0xFFFFB703)),
        WheelSegment("1,500 🪙", 1500, 0, Color(0xFF06D6A0)),
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
