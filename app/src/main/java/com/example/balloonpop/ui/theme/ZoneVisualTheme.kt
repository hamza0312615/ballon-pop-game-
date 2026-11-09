package com.example.balloonpop.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.balloonpop.model.WorldZone

/**
 * High-level environment styling for campaign world zones.
 */
enum class ZoneEnvironmentType {
    NEON_GLADE,
    PRISM_PEAKS,
    SUNSET_MESA,
    AQUA_REEF,
    CANDY_WONDERLAND,
    GENERIC
}

/**
 * Unique path styling connecting stage nodes.
 */
enum class ZonePathType {
    ORGANIC_VINE_ROOTS,     // Neon Glade: winding living roots, tendrils, leaf buds, bio-luminescent sap
    CRYSTAL_CHASM_SHARDS,   // Prism Peaks: fractured crystal chasms, floating cut-gem slabs, refracted light laser
    CANYON_STRATA_LEDGE,    // Sunset Mesa: stepped sandstone rock shelves, forged bronze brackets, amber trail
    OCEANIC_TRENCH_KELP,    // Aqua Reef: twin bioluminescent giant kelp ribbons, seafoam bubble pads, water vortex
    TIMBER_LADDER           // Generic fallback
}

/**
 * Distinct stage node shapes and visual construction.
 */
enum class ZoneNodeShapeType {
    ORGANIC_SEED_POD,       // Neon Glade: rounded teardrop seed pod / glowing floral bulb
    FACETED_CRYSTAL_SHARD,  // Prism Peaks: angular diamond / cut-gem faceted silhouette
    SUNBURST_TALISMAN,      // Sunset Mesa: stepped mesa medallion / sun disc
    PEARL_SHELL,            // Aqua Reef: scalloped ocean shell / pearl orb
    ROUNDED_TIMBER          // Generic fallback
}

/**
 * Ambient particle effects tailored to each zone.
 */
enum class ZoneParticleType {
    CYBER_FIREFLIES,        // Neon Glade: drifting glowing fireflies with soft halo pulses
    PRISM_LIGHT_SHARDS,     // Prism Peaks: floating diamond dust & rotating 4-point light crystals
    SUNSET_EMBERS,          // Sunset Mesa: rising canyon sparks & warm heat motes
    AQUA_BUBBLES,           // Aqua Reef: rising oceanic bubbles & bioluminescent plankton
    SAKURA_PETALS,          // Sakura Sanctuary: soft pink drifting & tumbling cherry blossom petals
    FROZEN_SNOWFLAKES,      // Frozen Tundra: 6-pointed crystalline snowflakes drifting gently
    STANDARD_MOTES          // Generic
}

/**
 * Layout configuration specifying horizontal and vertical coordinates for stage nodes,
 * allowing every zone to possess its own distinct route silhouette and pacing.
 */
data class ZoneRouteLayout(
    val xCurves: FloatArray,
    val yOffsets: FloatArray
)

/**
 * Complete visual theme specification for a campaign world zone.
 * Scalable architecture: Adding a new zone simply requires creating a new [ZoneVisualTheme] config.
 */
data class ZoneVisualTheme(
    val zoneId: Int,
    val envType: ZoneEnvironmentType,
    val name: String,
    val subtitle: String,
    val titleOrnamentPrefix: String,
    val titleOrnamentSuffix: String,
    val titleAccentColor: Color,
    val titlePillBg: Color,
    val titlePillBorder: Color,
    val pathType: ZonePathType,
    val nodeShape: ZoneNodeShapeType,
    val particleType: ZoneParticleType,
    val routeLayout: ZoneRouteLayout,
    val bgSkyGradient: List<Color>,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val ambientMoteColor: Color,
    val riverGradient: List<Color>,
    val waterRippleColor: Color,
    val lockedBgColors: List<Color>,
    val lockedBorderColor: Color,
    val lockedIconSymbol: String,
    val unlockedBgColors: List<Color>,
    val unlockedBorderColor: Color,
    val clearedBgColors: List<Color>,
    val clearedBorderColor: Color,
    val activeGlowColor: Color,
    val playButtonBadge: String,
    val playButtonGradient: List<Color>,
    val playButtonBorderColor: Color,
    val playButtonSpotColor: Color,
    val jumpButtonGradient: List<Color>,
    val jumpButtonBorderColor: Color
)

/**
 * Central theme registry for world zones.
 */
object ZoneVisualThemeRegistry {

    private val TWILIGHT_GROVE_THEME = ZoneVisualTheme(
        zoneId = 0,
        envType = ZoneEnvironmentType.NEON_GLADE,
        name = "Twilight Grove",
        subtitle = "Firefly Forest",
        titleOrnamentPrefix = "🌿",
        titleOrnamentSuffix = "🍃",
        titleAccentColor = Color(0xFFF6C453),
        titlePillBg = Color(0xDD0E3B2E),
        titlePillBorder = Color(0xFF1E6B4F),
        pathType = ZonePathType.ORGANIC_VINE_ROOTS,
        nodeShape = ZoneNodeShapeType.ORGANIC_SEED_POD,
        particleType = ZoneParticleType.CYBER_FIREFLIES,
        routeLayout = ZoneRouteLayout(
            // Organic, gentle meandering S-curve weaving through the jungle canopy
            xCurves = floatArrayOf(
                0.30f, // Stage 1 (Mossy forest floor)
                0.44f, // Stage 2 (Low branch crossing)
                0.70f, // Stage 3 (Right canopy shelf)
                0.76f, // Stage 4 (High banyan bough)
                0.52f, // Stage 5 (Center - Sacred Heart Blossom)
                0.28f, // Stage 6 (Left hanging liana)
                0.26f, // Stage 7 (Cascading waterfall overlook)
                0.48f, // Stage 8 (Treetop bridge)
                0.72f, // Stage 9 (Starlit crown perch)
                0.50f  // Stage 10 (Apex - Dragon Thorn Shrine)
            ),
            yOffsets = floatArrayOf(
                935f, 845f, 755f, 665f, 570f, 475f, 380f, 285f, 180f, 75f
            )
        ),
        bgSkyGradient = listOf(
            Color(0xFF08231B),
            Color(0xFF0E3B2E),
            Color(0xFF14532D),
            Color(0xFF0E3B2E),
            Color(0xFF08231B)
        ),
        primaryAccent = Color(0xFF2FA36B),
        secondaryAccent = Color(0xFFF6C453),
        ambientMoteColor = Color(0xFFFFD166),
        riverGradient = listOf(
            Color(0xFF2FA36B),
            Color(0xFFF6C453),
            Color(0xFF14532D),
            Color(0xFFF6C453),
            Color(0xFF2FA36B)
        ),
        waterRippleColor = Color(0x99A7F3D0),
        lockedBgColors = listOf(Color(0xFF12172E), CosmosNavy, CosmosNavyDeep),
        lockedBorderColor = CosmosIndigo,
        lockedIconSymbol = "🌱", // Dormant seedling / coiled seed bud, never generic lock emoji
        unlockedBgColors = listOf(Color(0xFF57C98A), Color(0xFF2FA36B), Color(0xFF14532D)),
        unlockedBorderColor = Color(0xFFFFE9A8),
        clearedBgColors = listOf(Color(0xFFF6C453), Color(0xFFD4A017), Color(0xFF2FA36B)),
        clearedBorderColor = Color(0xFFFFF3C4),
        activeGlowColor = Color(0xFFF6C453),
        playButtonBadge = "🌿",
        playButtonGradient = listOf(
            Color(0xFFFFD166),
            Color(0xFFF6C453),
            Color(0xFF57C98A),
            Color(0xFF2FA36B),
            Color(0xFF14532D)
        ),
        playButtonBorderColor = Color(0xFFFFE9A8),
        playButtonSpotColor = CosmosViolet,
        jumpButtonGradient = listOf(CosmosIndigo, CosmosNavyDeep),
        jumpButtonBorderColor = Color(0xFFFFD700)
    )

    private val PRISM_PEAKS_THEME = ZoneVisualTheme(
        zoneId = 1,
        envType = ZoneEnvironmentType.PRISM_PEAKS,
        name = "Prism Peaks",
        subtitle = "Crystalline Caverns",
        titleOrnamentPrefix = "💠",
        titleOrnamentSuffix = "💎",
        titleAccentColor = Color(0xFFA678D8),
        titlePillBg = Color(0xDD240E3E),
        titlePillBorder = Color(0xFF9333EA),
        pathType = ZonePathType.CRYSTAL_CHASM_SHARDS,
        nodeShape = ZoneNodeShapeType.FACETED_CRYSTAL_SHARD,
        particleType = ZoneParticleType.PRISM_LIGHT_SHARDS,
        routeLayout = ZoneRouteLayout(
            // Sharp alpine switchbacks & dramatic angular zig-zags across crystal chasms
            xCurves = floatArrayOf(
                0.20f, // Stage 1 (Base amethyst cavern)
                0.68f, // Stage 2 (Sharp right chasm traverse)
                0.82f, // Stage 3 (Right quartz spire)
                0.38f, // Stage 4 (Hard left leap to central spire)
                0.16f, // Stage 5 (Far-left - Radiant Prism Geode)
                0.58f, // Stage 6 (Diagonal climb to right ridge)
                0.82f, // Stage 7 (Outer crystal cliff)
                0.34f, // Stage 8 (Sharp cut back to left ledge)
                0.70f, // Stage 9 (High crystal precipice)
                0.50f  // Stage 10 (Summit - Monolith of the Void Dragon)
            ),
            yOffsets = floatArrayOf(
                945f, 855f, 765f, 670f, 565f, 460f, 360f, 260f, 165f, 70f
            )
        ),
        bgSkyGradient = listOf(
            Color(0xFF0C0317),
            Color(0xFF220A3C),
            Color(0xFF170629),
            Color(0xFF2E0E4D),
            Color(0xFF0A0214)
        ),
        primaryAccent = Color(0xFFA678D8),
        secondaryAccent = Color(0xFFD96C8A),
        ambientMoteColor = Color(0xFFB07CC6),
        riverGradient = listOf(
            Color(0xFFD8B4E8),
            Color(0xFFA678D8),
            Color(0xFF7C4DBD),
            Color(0xFFA678D8),
            Color(0xFFD8B4E8)
        ),
        waterRippleColor = Color(0x99EADCF5),
        lockedBgColors = listOf(Color(0xFF201230), Color(0xFF160B22), Color(0xFF0F0618)),
        lockedBorderColor = Color(0xFF381B54),
        lockedIconSymbol = "💠", // Faceted geode nucleus, never generic lock emoji
        unlockedBgColors = listOf(Color(0xFFB07CC6), Color(0xFFA678D8), Color(0xFF6D28A8)),
        unlockedBorderColor = Color(0xFFEADCF5),
        clearedBgColors = listOf(Color(0xFFA678D8), Color(0xFF7C4DBD), Color(0xFF5B21A8)),
        clearedBorderColor = Color(0xFFFAF5FF),
        activeGlowColor = Color(0xFFB07CC6),
        playButtonBadge = "💎",
        playButtonGradient = listOf(
            Color(0xFFD96C8A),
            Color(0xFFB07CC6),
            Color(0xFFA678D8),
            Color(0xFF7C4DBD),
            Color(0xFF4C1D95)
        ),
        playButtonBorderColor = Color(0xFFEADCF5),
        playButtonSpotColor = Color(0xFF7C4DBD),
        jumpButtonGradient = listOf(Color(0xFF4C1D95), Color(0xFF2E1065)),
        jumpButtonBorderColor = Color(0xFFA678D8)
    )

    private val SUNSET_MESA_THEME = ZoneVisualTheme(
        zoneId = 2,
        envType = ZoneEnvironmentType.SUNSET_MESA,
        name = "Sunset Mesa",
        subtitle = "Canyon Badlands",
        titleOrnamentPrefix = "🌅",
        titleOrnamentSuffix = "🏜️",
        titleAccentColor = Color(0xFFFB923C),
        titlePillBg = Color(0xDD3A141A),
        titlePillBorder = Color(0xFFEA580C),
        pathType = ZonePathType.CANYON_STRATA_LEDGE,
        nodeShape = ZoneNodeShapeType.SUNBURST_TALISMAN,
        particleType = ZoneParticleType.SUNSET_EMBERS,
        routeLayout = ZoneRouteLayout(
            xCurves = floatArrayOf(0.34f, 0.60f, 0.74f, 0.48f, 0.22f, 0.36f, 0.66f, 0.78f, 0.46f, 0.50f),
            yOffsets = floatArrayOf(940f, 845f, 750f, 655f, 560f, 465f, 370f, 275f, 175f, 75f)
        ),
        bgSkyGradient = listOf(
            Color(0xFF1B0710),
            Color(0xFF3E1225),
            Color(0xFF290A19),
            Color(0xFF4D172E),
            Color(0xFF15040D)
        ),
        primaryAccent = Color(0xFFFB923C),
        secondaryAccent = Color(0xFFF43F5E),
        ambientMoteColor = Color(0xFFF59E0B),
        riverGradient = listOf(
            Color(0xFFFED7AA),
            Color(0xFFFB923C),
            Color(0xFFEA580C),
            Color(0xFFFB923C),
            Color(0xFFFED7AA)
        ),
        waterRippleColor = Color(0x99FED7AA),
        lockedBgColors = listOf(Color(0xFF281119), Color(0xFF1C0A11), Color(0xFF13060B)),
        lockedBorderColor = Color(0xFF4C1E2B),
        lockedIconSymbol = "☀️",
        unlockedBgColors = listOf(Color(0xFFFDBA74), Color(0xFFFB923C), Color(0xFFC2410C)),
        unlockedBorderColor = Color(0xFFFFEDD5),
        clearedBgColors = listOf(Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFF9A3412)),
        clearedBorderColor = Color(0xFFFFF7ED),
        activeGlowColor = Color(0xFFFB923C),
        playButtonBadge = "⚡",
        playButtonGradient = listOf(
            Color(0xFFFDBA74),
            Color(0xFFFB923C),
            Color(0xFFEA580C),
            Color(0xFFC2410C),
            Color(0xFF7C2D12)
        ),
        playButtonBorderColor = Color(0xFFFFEDD5),
        playButtonSpotColor = Color(0xFFEA580C),
        jumpButtonGradient = listOf(Color(0xFF7C2D12), Color(0xFF431407)),
        jumpButtonBorderColor = Color(0xFFFB923C)
    )

    private val AQUA_REEF_THEME = ZoneVisualTheme(
        zoneId = 3,
        envType = ZoneEnvironmentType.AQUA_REEF,
        name = "Aqua Reef",
        subtitle = "Sunken Abyssal Trench",
        titleOrnamentPrefix = "🌊",
        titleOrnamentSuffix = "🐚",
        titleAccentColor = Color(0xFF38BDF8),
        titlePillBg = Color(0xDD07243B),
        titlePillBorder = Color(0xFF0284C7),
        pathType = ZonePathType.OCEANIC_TRENCH_KELP,
        nodeShape = ZoneNodeShapeType.PEARL_SHELL,
        particleType = ZoneParticleType.AQUA_BUBBLES,
        routeLayout = ZoneRouteLayout(
            xCurves = floatArrayOf(0.26f, 0.38f, 0.66f, 0.78f, 0.56f, 0.30f, 0.22f, 0.42f, 0.74f, 0.50f),
            yOffsets = floatArrayOf(930f, 840f, 745f, 650f, 555f, 460f, 365f, 270f, 175f, 75f)
        ),
        bgSkyGradient = listOf(
            Color(0xFF020B17),
            Color(0xFF062338),
            Color(0xFF031726),
            Color(0xFF092D48),
            Color(0xFF010913)
        ),
        primaryAccent = Color(0xFF38BDF8),
        secondaryAccent = Color(0xFF2DD4BF),
        ambientMoteColor = Color(0xFF67E8F9),
        riverGradient = listOf(
            Color(0xFFBAE6FD),
            Color(0xFF38BDF8),
            Color(0xFF0284C7),
            Color(0xFF38BDF8),
            Color(0xFFBAE6FD)
        ),
        waterRippleColor = Color(0x99BAE6FD),
        lockedBgColors = listOf(Color(0xFF0A1C29), Color(0xFF06131D), Color(0xFF030B12)),
        lockedBorderColor = Color(0xFF14344B),
        lockedIconSymbol = "🐚",
        unlockedBgColors = listOf(Color(0xFF67E8F9), Color(0xFF06B6D4), Color(0xFF0369A1)),
        unlockedBorderColor = Color(0xFFCFFAFE),
        clearedBgColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF075985)),
        clearedBorderColor = Color(0xFFE0F2FE),
        activeGlowColor = Color(0xFF4A90C4),
        playButtonBadge = "🌊",
        playButtonGradient = listOf(
            Color(0xFF7DD3FC),
            Color(0xFF38BDF8),
            Color(0xFF0284C7),
            Color(0xFF0369A1),
            Color(0xFF082F49)
        ),
        playButtonBorderColor = Color(0xFFBAE6FD),
        playButtonSpotColor = Color(0xFF0284C7),
        jumpButtonGradient = listOf(Color(0xFF075985), Color(0xFF082F49)),
        jumpButtonBorderColor = Color(0xFF38BDF8)
    )

    // Midnight Abyss (WorldZone 17, index 16): dedicated theme so this zone can be
    // recoloured into the deep-indigo storybook family without changing the generic
    // synthesized fallback used by the other non-dedicated zones. Non-colour
    // fields intentionally mirror that fallback exactly (GENERIC env, timber
    // path/node, AQUA_BUBBLES particles, generic route, "🔒" / "🧬" symbols).
    private val MIDNIGHT_ABYSS_THEME = ZoneVisualTheme(
        zoneId = 16,
        envType = ZoneEnvironmentType.GENERIC,
        name = "Midnight Abyss",
        subtitle = "Stages 161 - 170",
        titleOrnamentPrefix = "🌌",
        titleOrnamentSuffix = "🌌",
        titleAccentColor = Color(0xFFB8A9D9),
        titlePillBg = Color(0xDD0A0E24),
        titlePillBorder = CosmosViolet,
        pathType = ZonePathType.TIMBER_LADDER,
        nodeShape = ZoneNodeShapeType.ROUNDED_TIMBER,
        particleType = ZoneParticleType.AQUA_BUBBLES,
        routeLayout = ZoneRouteLayout(
            xCurves = floatArrayOf(0.28f, 0.50f, 0.75f, 0.78f, 0.50f, 0.22f, 0.30f, 0.65f, 0.76f, 0.50f),
            yOffsets = floatArrayOf(940f, 845f, 750f, 655f, 560f, 465f, 370f, 275f, 175f, 75f)
        ),
        bgSkyGradient = listOf(
            Color(0xFF120A2E),
            Color(0xFF1B1040),
            Color(0xFF2D1B69),
            Color(0xFF241A4D),
            Color(0xFF120A2E)
        ),
        primaryAccent = Color(0xFF6C5FBF),
        secondaryAccent = Color(0xFFB8A9D9),
        ambientMoteColor = Color(0xFFCFC6EE),
        riverGradient = listOf(Color(0xFF6C5FBF), Color(0xFFB8A9D9), Color(0xFF6C5FBF)),
        waterRippleColor = Color(0x99B8A9D9),
        lockedBgColors = listOf(Color(0xFF12172E), CosmosNavyDeep),
        lockedBorderColor = CosmosIndigo,
        lockedIconSymbol = "🔒",
        unlockedBgColors = listOf(Color(0xFF6C5FBF), Color(0xFF8E7CD6)),
        unlockedBorderColor = Color.White.copy(alpha = 0.8f),
        clearedBgColors = listOf(Color(0xFFB8A9D9), Color(0xFF7C6BB8)),
        clearedBorderColor = Color(0xFFE8E4F5),
        activeGlowColor = Color(0xFFB8A9D9),
        playButtonBadge = "🌌",
        playButtonGradient = listOf(Color(0xFF6C5FBF), Color(0xFFB8A9D9)),
        playButtonBorderColor = Color.White.copy(alpha = 0.8f),
        playButtonSpotColor = CosmosViolet,
        jumpButtonGradient = listOf(CosmosIndigo, CosmosNavyDeep),
        jumpButtonBorderColor = Color(0xFFFFD700)
    )

    private val THEME_MAP = mapOf(
        0 to TWILIGHT_GROVE_THEME,
        1 to PRISM_PEAKS_THEME,
        2 to SUNSET_MESA_THEME,
        3 to AQUA_REEF_THEME,
        16 to MIDNIGHT_ABYSS_THEME
    )

    /**
     * Resolves the visual theme for a given zone index.
     * If the zone has a dedicated config, returns it.
     * Otherwise, automatically synthesizes an extensible, coherent theme derived from the [WorldZone] definition.
     */
    fun getThemeForZone(zoneIndex: Int, worldZone: WorldZone? = null): ZoneVisualTheme {
        THEME_MAP[zoneIndex]?.let { return it }

        val pCol = worldZone?.primaryColor ?: Color(0xFFF59E0B)
        val sCol = worldZone?.secondaryColor ?: Color(0xFF4A90C4)
        val grad = worldZone?.gradientColors?.takeIf { it.isNotEmpty() } ?: listOf(
            Color(0xFF060B14),
            Color(0xFF131927),
            Color(0xFF060B14)
        )
        val zoneName = worldZone?.name ?: "Zone ${zoneIndex + 1}"
        val symbol = worldZone?.icon ?: "⭐"

        // Pass 5: particles keyed on zone index (0-based), never on display
        // names, so renaming a zone cannot change its ambient effects.
        val detectedParticleType = when (zoneIndex) {
            10 -> ZoneParticleType.SAKURA_PETALS          // Sakura Sanctuary
            9 -> ZoneParticleType.FROZEN_SNOWFLAKES       // Frozen Tundra
            6, 15 -> ZoneParticleType.SUNSET_EMBERS       // Volcano Caldera, Molten Core
            0, 12 -> ZoneParticleType.CYBER_FIREFLIES     // Twilight Grove, Emerald Jungle (fireflies)
            1, 7 -> ZoneParticleType.PRISM_LIGHT_SHARDS   // Prism Peaks, Starry Cosmos
            3, 16 -> ZoneParticleType.AQUA_BUBBLES        // Aqua Reef, Midnight Abyss
            else -> ZoneParticleType.STANDARD_MOTES
        }

        // Dynamically synthesized theme for future zones
        return ZoneVisualTheme(
            zoneId = zoneIndex,
            envType = ZoneEnvironmentType.GENERIC,
            name = zoneName,
            subtitle = worldZone?.subtitle ?: "Stages ${(zoneIndex * 10) + 1} - ${(zoneIndex + 1) * 10}",
            titleOrnamentPrefix = symbol,
            titleOrnamentSuffix = symbol,
            titleAccentColor = pCol,
            titlePillBg = Color(0xDD111827),
            titlePillBorder = pCol,
            pathType = ZonePathType.TIMBER_LADDER,
            nodeShape = ZoneNodeShapeType.ROUNDED_TIMBER,
            particleType = detectedParticleType,
            routeLayout = ZoneRouteLayout(
                xCurves = floatArrayOf(0.28f, 0.50f, 0.75f, 0.78f, 0.50f, 0.22f, 0.30f, 0.65f, 0.76f, 0.50f),
                yOffsets = floatArrayOf(940f, 845f, 750f, 655f, 560f, 465f, 370f, 275f, 175f, 75f)
            ),
            bgSkyGradient = grad,
            primaryAccent = pCol,
            secondaryAccent = sCol,
            ambientMoteColor = pCol,
            riverGradient = listOf(pCol, sCol, pCol),
            waterRippleColor = pCol.copy(alpha = 0.5f),
            lockedBgColors = listOf(Color(0xFF1F1713), Color(0xFF140E0A)),
            lockedBorderColor = Color(0xFF3E2718),
            lockedIconSymbol = "🔒",
            unlockedBgColors = listOf(pCol, sCol),
            unlockedBorderColor = Color.White.copy(alpha = 0.8f),
            clearedBgColors = listOf(Color(0xFF10B981), Color(0xFF059669)),
            clearedBorderColor = Color(0xFF6EE7B7),
            activeGlowColor = pCol,
            playButtonBadge = symbol,
            playButtonGradient = listOf(pCol, sCol),
            playButtonBorderColor = Color.White.copy(alpha = 0.8f),
            playButtonSpotColor = pCol,
            jumpButtonGradient = listOf(Color(0xFF3E2318), Color(0xFF1F120B)),
            jumpButtonBorderColor = pCol
        )
    }
}
