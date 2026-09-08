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
    val trailColor: Color
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
    val rewardGems: Int
) {
    val isBossStage: Boolean get() = (level % 10 == 0)
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
    CONTINUOUS_SLICE("skill_slice", "Laser Blade Stream", "⚡"),
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
    val levelsRange: IntRange get() = startLevel..endLevel
    val milestoneChestLevel: Int get() = endLevel
}

data class WheelSegment(
    val label: String,
    val coins: Int,
    val gems: Int,
    val color: Color
)

data class QuestItem(
    val id: String,
    val title: String,
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
        BladeItem("blade_classic", "Classic Pointer", "🖐️", 0, CurrencyType.COINS, Color(0xFF00F0FF)),
        BladeItem("blade_laser", "Laser Sabre", "⚔️", 500, CurrencyType.COINS, Color(0xFF00F0FF)),
        BladeItem("blade_flame", "Flame Blade", "🔥", 1000, CurrencyType.COINS, Color(0xFFFF5722)),
        BladeItem("blade_wand", "Magic Wand", "🪄", 2000, CurrencyType.COINS, Color(0xFF9D4EDD)),
        BladeItem("blade_cyber", "Cyber Pulse", "⚡", 30, CurrencyType.GEMS, Color(0xFF06D6A0))
    )

    val SHOP_THEMES = listOf(
        ThemeConfig("theme_neon", "Neon Arcade", "🎆", 0, CurrencyType.COINS, listOf(Color(0xFF1B0E3D), Color(0xFF2E1764), Color(0xFF130930))),
        ThemeConfig("theme_sunset", "Sunset Aurora", "🌇", 300, CurrencyType.COINS, listOf(Color(0xFF381232), Color(0xFF5D1D45), Color(0xFF260D25))),
        ThemeConfig("theme_ocean", "Prism Lagoon", "🌊", 600, CurrencyType.COINS, listOf(Color(0xFF0F2B48), Color(0xFF134E6F), Color(0xFF0A1E33))),
        ThemeConfig("theme_galaxy", "Celestial Nebula", "🌌", 1500, CurrencyType.COINS, listOf(Color(0xFF200F3E), Color(0xFF3B1869), Color(0xFF150A2D)))
    )

    val AVAILABLE_SKILLS = listOf(
        PlayerSkill(
            id = "skill_slice",
            type = SkillType.CONTINUOUS_SLICE,
            name = "Laser Stream Slice",
            icon = "⚡",
            description = "Glide your finger smoothly across the screen to slice and pop all balloons continuously without lifting!",
            unlockCostCoins = 200,
            unlockCostGems = 0,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 22f,
            cooldownSeconds = 0f,
            accentColor = Color(0xFF00F0FF)
        ),
        PlayerSkill(
            id = "skill_freeze",
            type = SkillType.TIME_FREEZE,
            name = "Chrono Stasis",
            icon = "⏱️",
            description = "Slow down all rising balloons by 75% for 6 seconds to execute precision combos and chain pops.",
            unlockCostCoins = 450,
            unlockCostGems = 5,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 15f,
            accentColor = Color(0xFF00E5FF)
        ),
        PlayerSkill(
            id = "skill_magnet",
            type = SkillType.MAGNET_PULL,
            name = "Vortex Magnet",
            icon = "🧲",
            description = "Create a magnetic singularity that pulls golden and rainbow balloons directly to your touch point!",
            unlockCostCoins = 800,
            unlockCostGems = 12,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 18f,
            accentColor = Color(0xFFFF2A75)
        ),
        PlayerSkill(
            id = "skill_shockwave",
            type = SkillType.SHOCKWAVE,
            name = "Supernova Blast",
            icon = "💥",
            description = "Release a massive radial shockwave clearing all standard balloons on screen and showering bonus coins!",
            unlockCostCoins = 1500,
            unlockCostGems = 25,
            maxLevel = 3,
            currentLevel = 1,
            energyCostPerSec = 0f,
            cooldownSeconds = 25f,
            accentColor = Color(0xFFFFB703)
        )
    )

    val DAILY_STREAK_TIMELINE = listOf(
        DailyStreakDay(1, 100, 0, "Day 1"),
        DailyStreakDay(2, 250, 1, "Day 2"),
        DailyStreakDay(3, 400, 3, "Day 3"),
        DailyStreakDay(4, 600, 5, "Day 4"),
        DailyStreakDay(5, 850, 8, "Day 5"),
        DailyStreakDay(6, 1200, 15, "Day 6"),
        DailyStreakDay(7, 3000, 50, "Day 7 JACKPOT 🎉", isJackpot = true)
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
        WorldZone(15, "Celestial Ascension", "Stages 141 - 150", 141, 150, Color(0xFFFFD700), Color(0xFF00FFFF), listOf(Color(0xFF29104D), Color(0xFF481A85), Color(0xFF1A0A33)), "👑")
    )

    val CAMPAIGN_LEVELS: List<CampaignLevel> = (1..150).map { lvl ->
        val isBossStage = (lvl % 10 == 0)
        val isFrenzyStage = (lvl % 5 == 0 && !isBossStage)
        CampaignLevel(
            level = lvl,
            targetPops = if (isBossStage) 30 + lvl * 2 else if (isFrenzyStage) 25 + lvl * 2 else 15 + lvl * 2,
            targetScore = if (isBossStage) 500 + lvl * 200 else 200 + lvl * 140,
            timeLimit = if (isBossStage) 45 else (60 - (lvl / 8)).coerceIn(30, 60),
            maxBombs = if (isBossStage) (lvl / 8).coerceIn(2, 6) else (lvl / 10).coerceIn(0, 10),
            rewardCoins = 50 + lvl * 15,
            rewardGems = if (isBossStage) 10 else if (isFrenzyStage) 5 else 1
        )
    }

    val WHEEL_SEGMENTS = listOf(
        WheelSegment("100 🪙", 100, 0, Color(0xFFFF2A75)),
        WheelSegment("500 🪙", 500, 0, Color(0xFF9D4EDD)),
        WheelSegment("5 💎", 0, 5, Color(0xFF00F0FF)),
        WheelSegment("250 🪙", 250, 0, Color(0xFFFFB703)),
        WheelSegment("1,000 🪙", 1000, 0, Color(0xFF06D6A0)),
        WheelSegment("10 💎", 0, 10, Color(0xFFFF758C))
    )

    val DEFAULT_QUESTS = listOf(
        QuestItem("quest_pop_50", "Pop 50 Balloons", 50, 100, 0),
        QuestItem("quest_pop_200", "Pop 200 Balloons", 200, 300, 2),
        QuestItem("quest_combo_5", "Reach 5x Combo", 5, 150, 1),
        QuestItem("quest_combo_10", "Reach 10x Combo", 10, 350, 5),
        QuestItem("quest_stage_5", "Clear Stage 5", 5, 250, 3),
        QuestItem("quest_stage_10", "Clear Stage 10", 10, 500, 8),
        QuestItem("quest_spin_wheel", "Spin the Daily Wheel", 1, 100, 1)
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
