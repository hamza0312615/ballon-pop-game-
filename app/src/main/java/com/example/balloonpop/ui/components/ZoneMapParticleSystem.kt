package com.example.balloonpop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.balloonpop.ui.theme.ZoneParticleType
import com.example.balloonpop.ui.theme.ZoneVisualTheme
import kotlin.math.*

/**
 * High-performance, Canvas-based zone-specific particle system.
 * Renders distinct physical atmospheric effects for each world zone:
 * - Neon Glade: Drifting bioluminescent fireflies with breathing halos, wing flutter & soft light motes
 * - Prism Peaks: Floating faceted crystal shards, tumbling cut-gem prisms & radiant 4-point diamond glints
 * - Sunset Mesa: Rising canyon updraft embers & golden heat sparks
 * - Aqua Reef: Deep-sea oceanic bubbles with crescent specular rims & plankton
 * - Sakura Sanctuary: Soft pink tumbling cherry blossom petals
 * - Frozen Tundra: 6-pointed crystalline snowflakes drifting gently
 */
object ZoneMapParticleSystem {

    /**
     * Immutable seed definition for deterministic particle placement and movement.
     */
    data class MapParticle(
        val seedX: Float,          // 0f .. 1f normalized across canvas width
        val seedY: Float,          // 0f .. 1f normalized across canvas height
        val baseSize: Float,       // Reference size in dp/px
        val speedX: Float,         // Horizontal drift velocity
        val speedY: Float,         // Vertical drift velocity
        val wobbleFreqX: Float,    // Frequency of lateral sinusoidal sway
        val wobbleAmpX: Float,     // Amplitude of lateral sway
        val wobbleFreqY: Float,    // Frequency of vertical undulation
        val wobbleAmpY: Float,     // Amplitude of vertical undulation
        val pulseFreq: Float,      // Breathing / flickering frequency
        val pulsePhase: Float,     // Initial phase offset
        val rotSpeed: Float,       // Rotation speed in degrees per second
        val initialRot: Float,     // Starting rotation in degrees
        val sizeTier: Int,         // 0 = Micro, 1 = Medium, 2 = Macro/Hero
        val depthLayer: Float,     // 0.3f (distant) to 1.0f (foreground)
        val facetShape: Int,       // Geometry variation per particle type
        val colorVariant: Int      // Palette tint index
    )

    // Pre-allocated deterministic particle seeds per zone type (Zero per-frame allocations)
    private val FIREFLY_SEEDS = generateSeeds(
        count = 32,
        baseSizeRange = 3f..9.5f,
        speedXRange = -12f..14f,
        speedYRange = 10f..26f, // Gentle upward drift
        wobbleAmpRange = 12f..28f,
        rotSpeedRange = 0f..0f
    )

    private val CRYSTAL_SHARD_SEEDS = generateSeeds(
        count = 30,
        baseSizeRange = 4.5f..15f,
        speedXRange = 10f..28f, // Diagonal chasm breeze
        speedYRange = 14f..34f, // Gentle floating descent/drift
        wobbleAmpRange = 8f..20f,
        rotSpeedRange = 18f..65f // Dynamic tumbling rotation
    )

    private val EMBER_SEEDS = generateSeeds(
        count = 28,
        baseSizeRange = 3f..8f,
        speedXRange = -16f..16f,
        speedYRange = 30f..65f, // Rapid canyon thermal rise
        wobbleAmpRange = 10f..24f,
        rotSpeedRange = 30f..110f
    )

    private val BUBBLE_SEEDS = generateSeeds(
        count = 26,
        baseSizeRange = 4f..11f,
        speedXRange = -8f..8f,
        speedYRange = 22f..48f, // Buoyant rise
        wobbleAmpRange = 12f..22f,
        rotSpeedRange = 0f..0f
    )

    private val SAKURA_SEEDS = generateSeeds(
        count = 24,
        baseSizeRange = 6f..14f,
        speedXRange = 18f..38f, // Spring wind gust
        speedYRange = 16f..32f,
        wobbleAmpRange = 14f..30f,
        rotSpeedRange = 25f..75f
    )

    private val SNOWFLAKE_SEEDS = generateSeeds(
        count = 22,
        baseSizeRange = 5f..13f,
        speedXRange = -10f..12f,
        speedYRange = 18f..36f,
        wobbleAmpRange = 8f..18f,
        rotSpeedRange = 12f..40f
    )

    private val GENERIC_MOTE_SEEDS = generateSeeds(
        count = 20,
        baseSizeRange = 3f..8f,
        speedXRange = -10f..10f,
        speedYRange = 12f..28f,
        wobbleAmpRange = 10f..20f,
        rotSpeedRange = 0f..0f
    )

    private fun generateSeeds(
        count: Int,
        baseSizeRange: ClosedFloatingPointRange<Float>,
        speedXRange: ClosedFloatingPointRange<Float>,
        speedYRange: ClosedFloatingPointRange<Float>,
        wobbleAmpRange: ClosedFloatingPointRange<Float>,
        rotSpeedRange: ClosedFloatingPointRange<Float>
    ): List<MapParticle> {
        val list = mutableListOf<MapParticle>()
        for (i in 0 until count) {
            // Pseudorandom deterministic values via hash
            val h1 = ((i * 1337 + 7331) % 9973) / 9973f
            val h2 = ((i * 3571 + 2459) % 9973) / 9973f
            val h3 = ((i * 5107 + 1013) % 9973) / 9973f
            val h4 = ((i * 7919 + 8707) % 9973) / 9973f
            val h5 = ((i * 9221 + 4001) % 9973) / 9973f

            val sizeTier = when {
                h3 < 0.45f -> 0 // Micro
                h3 < 0.80f -> 1 // Medium
                else -> 2       // Hero / Macro
            }

            val size = baseSizeRange.start + h3 * (baseSizeRange.endInclusive - baseSizeRange.start)
            val spdX = speedXRange.start + h1 * (speedXRange.endInclusive - speedXRange.start)
            val spdY = speedYRange.start + h2 * (speedYRange.endInclusive - speedYRange.start)
            val wobbleAmp = wobbleAmpRange.start + h4 * (wobbleAmpRange.endInclusive - wobbleAmpRange.start)
            val rotSpd = rotSpeedRange.start + h5 * (rotSpeedRange.endInclusive - rotSpeedRange.start)

            list.add(
                MapParticle(
                    seedX = h1,
                    seedY = h2,
                    baseSize = size,
                    speedX = spdX,
                    speedY = spdY,
                    wobbleFreqX = 0.6f + h3 * 1.6f,
                    wobbleAmpX = wobbleAmp,
                    wobbleFreqY = 0.8f + h4 * 1.4f,
                    wobbleAmpY = wobbleAmp * 0.45f,
                    pulseFreq = 0.8f + h5 * 2.2f,
                    pulsePhase = (h1 * 2f * PI).toFloat(),
                    rotSpeed = rotSpd * if (i % 2 == 0) 1f else -1f,
                    initialRot = h2 * 360f,
                    sizeTier = sizeTier,
                    depthLayer = 0.4f + h3 * 0.6f,
                    facetShape = i % 4,
                    colorVariant = i % 4
                )
            )
        }
        return list
    }

    /**
     * Primary DrawScope extension to render zone-specific particles across the map canvas.
     */
    fun DrawScope.drawZoneParticles(
        theme: ZoneVisualTheme,
        w: Float,
        h: Float,
        timeSeconds: Float,
        scrollOffset: Float = 0f
    ) {
        when (theme.particleType) {
            ZoneParticleType.CYBER_FIREFLIES -> {
                drawNeonGladeFireflies(w, h, timeSeconds, scrollOffset)
            }
            ZoneParticleType.PRISM_LIGHT_SHARDS -> {
                drawPrismPeaksCrystalShards(w, h, timeSeconds, scrollOffset)
            }
            ZoneParticleType.SUNSET_EMBERS -> {
                drawSunsetMesaEmbers(w, h, timeSeconds, scrollOffset)
            }
            ZoneParticleType.AQUA_BUBBLES -> {
                drawAquaReefBubbles(w, h, timeSeconds, scrollOffset)
            }
            ZoneParticleType.SAKURA_PETALS -> {
                drawSakuraPetals(w, h, timeSeconds, scrollOffset)
            }
            ZoneParticleType.FROZEN_SNOWFLAKES -> {
                drawFrozenSnowflakes(w, h, timeSeconds, scrollOffset)
            }
            else -> {
                drawGenericMotes(theme, w, h, timeSeconds, scrollOffset)
            }
        }
    }

    // =========================================================================
    // 1. NEON GLADE: BIOLUMINESCENT FOREST FIREFLIES
    // =========================================================================
    private fun DrawScope.drawNeonGladeFireflies(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = FIREFLY_SEEDS

        for (p in particles) {
            // Natural upward drifting & gentle settling with harmonic lateral wander
            val riseDist = timeSec * p.speedY
            val rawYNorm = (p.seedY - (riseDist / h)) % 1.0f
            val yNorm = if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm

            // Lateral organic wandering with dual-sine harmonic curve
            val swayX = sin(timeSec * p.wobbleFreqX + p.pulsePhase) * p.wobbleAmpX +
                    cos(timeSec * 0.7f + p.seedX * 5f) * (p.wobbleAmpX * 0.45f)
            val rawX = (p.seedX * w + swayX).mod(w)

            // Vertical undulation and subtle parallax scroll response
            val undulateY = sin(timeSec * p.wobbleFreqY + p.pulsePhase) * p.wobbleAmpY
            val parallaxY = scrollOffset * (p.depthLayer - 0.5f) * 0.35f
            val posX = rawX.coerceIn(8f, w - 8f)
            val posY = (yNorm * h + undulateY - parallaxY).mod(h)

            // Organic bioluminescent breathing pulse
            val pulseCycle = (sin(timeSec * p.pulseFreq + p.pulsePhase) * 0.5f + 0.5f)
            val breathingAlpha = (pulseCycle.pow(1.5f) * 0.70f + 0.30f) * p.depthLayer

            // Palette tints: mint, emerald, cyan, and gold-green
            val auraColor = when (p.colorVariant) {
                0 -> Color(0x3500F0FF) // Cyan bio-glow
                1 -> Color(0x3A34D399) // Mint green bio-glow
                2 -> Color(0x3010B981) // Deep emerald
                else -> Color(0x356EE7B7) // Warm spring glow
            }

            val nucleusColor = when (p.colorVariant) {
                0 -> Color(0xFF38BDF8)
                1 -> Color(0xFF6EE7B7)
                2 -> Color(0xFF34D399)
                else -> Color(0xFFA7F3D0)
            }

            val radius = p.baseSize * p.depthLayer

            // LAYER A: Outer Soft Diffused Glowing Halo (Radial Aura)
            val haloRadius = radius * if (p.sizeTier == 2) 3.8f else 2.6f
            drawCircle(
                color = auraColor.copy(alpha = breathingAlpha * 0.45f),
                radius = haloRadius,
                center = Offset(posX, posY)
            )

            // LAYER B: Soft Inner Corona Glow
            drawCircle(
                color = nucleusColor.copy(alpha = breathingAlpha * 0.75f),
                radius = radius * 1.5f,
                center = Offset(posX, posY)
            )

            // LAYER C: High-Luminance Phosphorescent Nucleus
            drawCircle(
                color = nucleusColor.copy(alpha = breathingAlpha),
                radius = radius,
                center = Offset(posX, posY)
            )

            // LAYER D: Pure White-Hot Specular Center
            drawCircle(
                color = Color.White.copy(alpha = breathingAlpha * 0.92f),
                radius = radius * 0.38f,
                center = Offset(posX, posY)
            )

            // LAYER E: Hero Firefly Winglets (Tier 2 only)
            if (p.sizeTier == 2) {
                val wingFlutter = sin(timeSec * 22f + p.pulsePhase) * 0.4f
                val wingPath = Path().apply {
                    // Left winglet
                    moveTo(posX - radius * 0.4f, posY - radius * 0.2f)
                    quadraticTo(
                        posX - radius * 1.8f,
                        posY - radius * (1.6f + wingFlutter),
                        posX - radius * 0.2f,
                        posY - radius * 1.4f
                    )
                    close()
                    // Right winglet
                    moveTo(posX + radius * 0.4f, posY - radius * 0.2f)
                    quadraticTo(
                        posX + radius * 1.8f,
                        posY - radius * (1.6f - wingFlutter),
                        posX + radius * 0.2f,
                        posY - radius * 1.4f
                    )
                    close()
                }
                drawPath(wingPath, Color.White.copy(alpha = breathingAlpha * 0.42f))

                // Faint lagging bio-luminescent trail mote
                val trailX = posX - (swayX * 0.15f)
                val trailY = posY + radius * 1.8f
                drawCircle(
                    color = auraColor.copy(alpha = breathingAlpha * 0.3f),
                    radius = radius * 0.6f,
                    center = Offset(trailX, trailY)
                )
            }
        }
    }

    // =========================================================================
    // 2. PRISM PEAKS: FLOATING CRYSTAL SHARDS & DIAMOND DUST
    // =========================================================================
    private fun DrawScope.drawPrismPeaksCrystalShards(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = CRYSTAL_SHARD_SEEDS

        for (p in particles) {
            // Diagonal drift across cavern draft currents
            val driftX = timeSec * p.speedX
            val driftY = timeSec * p.speedY
            val rawXNorm = (p.seedX + (driftX / w)) % 1.0f
            val rawYNorm = (p.seedY + (driftY / h)) % 1.0f

            val xNorm = if (rawXNorm < 0f) rawXNorm + 1.0f else rawXNorm
            val yNorm = if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm

            // Floating pendulum sway
            val pendX = sin(timeSec * p.wobbleFreqX + p.pulsePhase) * p.wobbleAmpX
            val pendY = cos(timeSec * p.wobbleFreqY + p.pulsePhase) * p.wobbleAmpY
            val parallaxY = scrollOffset * (p.depthLayer - 0.5f) * 0.4f

            val posX = (xNorm * w + pendX).mod(w)
            val posY = (yNorm * h + pendY - parallaxY).mod(h)

            // Dynamic rotation & 3D tumbling flip ratio
            val currentRot = (p.initialRot + timeSec * p.rotSpeed) % 360f
            val flipScale = cos(timeSec * (abs(p.rotSpeed) * 0.04f) + p.pulsePhase)
            val widthScale = abs(flipScale).coerceIn(0.15f, 1.0f)

            // Specular refraction flare when face catches the light
            val glintIntensity = max(0f, sin(timeSec * p.pulseFreq + p.pulsePhase)).pow(6)
            val alpha = (0.35f + 0.55f * p.depthLayer).coerceIn(0.2f, 0.95f)

            // Prismatic Cavern Color Palette
            val (darkFacetCol, lightFacetCol, glowCol) = when (p.colorVariant) {
                0 -> Triple(Color(0xBB7E22CE), Color(0xEEF0ABFC), Color(0x40C084FC)) // Amethyst to Lilac
                1 -> Triple(Color(0xBB9333EA), Color(0xEEF472B6), Color(0x40E879F9)) // Royal Magenta
                2 -> Triple(Color(0xBB0284C7), Color(0xEEBAE6FD), Color(0x4038BDF8)) // Ice Cyan Diamond
                else -> Triple(Color(0xBBD946EF), Color(0xEEFAF5FF), Color(0x40F0ABFC)) // Rose Quartz
            }

            val shardSize = p.baseSize * p.depthLayer

            rotate(degrees = currentRot, pivot = Offset(posX, posY)) {
                when (p.facetShape) {
                    0 -> {
                        // GEOMETRY A: Two-Tone Faceted Elongated Crystal Shard
                        val hw = (shardSize * 0.55f) * widthScale
                        val hh = shardSize * 1.2f

                        // Outer soft refractive glow
                        if (p.sizeTier == 2) {
                            drawCircle(
                                color = glowCol.copy(alpha = alpha * 0.5f),
                                radius = hh * 1.3f,
                                center = Offset(posX, posY)
                            )
                        }

                        // Left Shaded Facet
                        val leftFacet = Path().apply {
                            moveTo(posX, posY - hh)
                            lineTo(posX - hw, posY)
                            lineTo(posX, posY + hh)
                            close()
                        }
                        drawPath(leftFacet, darkFacetCol.copy(alpha = alpha * 0.85f))

                        // Right Illuminated Facet
                        val rightFacet = Path().apply {
                            moveTo(posX, posY - hh)
                            lineTo(posX + hw, posY)
                            lineTo(posX, posY + hh)
                            close()
                        }
                        drawPath(rightFacet, lightFacetCol.copy(alpha = alpha))

                        // Center 3D Facet Ridge Line
                        drawLine(
                            color = Color.White.copy(alpha = alpha * 0.9f),
                            start = Offset(posX, posY - hh),
                            end = Offset(posX, posY + hh),
                            strokeWidth = 1.0f
                        )

                        // Specular Crisp Outline
                        val fullDiamond = Path().apply {
                            moveTo(posX, posY - hh)
                            lineTo(posX + hw, posY)
                            lineTo(posX, posY + hh)
                            lineTo(posX - hw, posY)
                            close()
                        }
                        drawPath(
                            fullDiamond,
                            color = Color.White.copy(alpha = alpha * 0.65f),
                            style = Stroke(width = 1.2f)
                        )
                    }

                    1 -> {
                        // GEOMETRY B: Faceted Hexagonal Cut-Gem Slab
                        val r = shardSize * 0.85f
                        val hw = r * widthScale
                        val hh = r

                        val hexPath = Path().apply {
                            moveTo(posX, posY - hh)
                            lineTo(posX + hw * 0.86f, posY - hh * 0.5f)
                            lineTo(posX + hw * 0.86f, posY + hh * 0.5f)
                            lineTo(posX, posY + hh)
                            lineTo(posX - hw * 0.86f, posY + hh * 0.5f)
                            lineTo(posX - hw * 0.86f, posY - hh * 0.5f)
                            close()
                        }
                        drawPath(hexPath, lightFacetCol.copy(alpha = alpha * 0.65f))
                        drawPath(hexPath, color = Color.White.copy(alpha = alpha * 0.8f), style = Stroke(width = 1.2f))

                        // Inner triangular facet cuts
                        drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(posX, posY - hh), Offset(posX, posY + hh), strokeWidth = 1f)
                        drawLine(Color.White.copy(alpha = alpha * 0.5f), Offset(posX - hw * 0.86f, posY), Offset(posX + hw * 0.86f, posY), strokeWidth = 1f)
                    }

                    2 -> {
                        // GEOMETRY C: 4-Point Radiant Light Cross / Star Shard
                        val rayL = shardSize * 1.4f
                        val rayW = (shardSize * 0.35f) * widthScale

                        val starPath = Path().apply {
                            moveTo(posX, posY - rayL)
                            lineTo(posX + rayW, posY)
                            lineTo(posX, posY + rayL)
                            lineTo(posX - rayW, posY)
                            close()
                        }
                        drawPath(starPath, lightFacetCol.copy(alpha = alpha))

                        val starCross = Path().apply {
                            moveTo(posX - rayL * widthScale, posY)
                            lineTo(posX, posY + rayW)
                            lineTo(posX + rayL * widthScale, posY)
                            lineTo(posX, posY - rayW)
                            close()
                        }
                        drawPath(starCross, lightFacetCol.copy(alpha = alpha * 0.85f))

                        // High-luminance central diamond core
                        drawCircle(
                            color = Color.White.copy(alpha = alpha),
                            radius = shardSize * 0.28f,
                            center = Offset(posX, posY)
                        )
                    }

                    else -> {
                        // GEOMETRY D: Micro Diamond Dust (Twinkling Prism Specks)
                        val s = (shardSize * 0.65f) * widthScale
                        val diamondPath = Path().apply {
                            moveTo(posX, posY - s)
                            lineTo(posX + s, posY)
                            lineTo(posX, posY + s)
                            lineTo(posX - s, posY)
                            close()
                        }
                        drawPath(diamondPath, lightFacetCol.copy(alpha = alpha))
                        drawCircle(Color.White.copy(alpha = alpha), radius = s * 0.35f, center = Offset(posX, posY))
                    }
                }

                // LAYER E: Brilliant Specular Lens Gleam Burst
                if (glintIntensity > 0.05f) {
                    val gleamLen = shardSize * (0.8f + glintIntensity * 1.4f)
                    val gleamAlpha = glintIntensity * alpha

                    // Horizontal & Vertical specular spikes
                    drawLine(
                        color = Color.White.copy(alpha = gleamAlpha),
                        start = Offset(posX - gleamLen, posY),
                        end = Offset(posX + gleamLen, posY),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color.White.copy(alpha = gleamAlpha),
                        start = Offset(posX, posY - gleamLen),
                        end = Offset(posX, posY + gleamLen),
                        strokeWidth = 1.5f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = gleamAlpha),
                        radius = shardSize * 0.35f,
                        center = Offset(posX, posY)
                    )
                }
            }
        }
    }

    // =========================================================================
    // 3. SUNSET MESA: RISING CANYON EMBERS & HEAT MOTES
    // =========================================================================
    private fun DrawScope.drawSunsetMesaEmbers(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = EMBER_SEEDS

        for (p in particles) {
            val riseDist = timeSec * p.speedY
            val rawYNorm = (p.seedY - (riseDist / h)) % 1.0f
            val yNorm = if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm

            // Updraft turbulent horizontal swirl
            val swayX = sin(timeSec * p.wobbleFreqX + p.seedY * 8f) * p.wobbleAmpX +
                    cos(timeSec * 1.8f + p.seedX * 4f) * 6f
            val posX = (p.seedX * w + swayX).mod(w)
            val posY = (yNorm * h - scrollOffset * (p.depthLayer - 0.5f) * 0.3f).mod(h)

            val flicker = (sin(timeSec * p.pulseFreq + p.pulsePhase) * 0.4f + 0.6f) * p.depthLayer
            val emberSize = p.baseSize * p.depthLayer

            val (glowCol, coreCol) = when (p.colorVariant) {
                0 -> Pair(Color(0x40EA580C), Color(0xFFF59E0B)) // Burning Amber
                1 -> Pair(Color(0x40E11D48), Color(0xFFFB923C)) // Crimson Heat
                2 -> Pair(Color(0x40F59E0B), Color(0xFFFED7AA)) // Golden Ash
                else -> Pair(Color(0x40D97706), Color(0xFFFFB703)) // Solar Flare
            }

            // Outer heat aura
            drawCircle(glowCol.copy(alpha = flicker * 0.45f), radius = emberSize * 2.8f, center = Offset(posX, posY))

            // Teardrop ember shape rising upwards
            rotate(degrees = (sin(timeSec * 3f + p.pulsePhase) * 15f), pivot = Offset(posX, posY)) {
                val emberPath = Path().apply {
                    moveTo(posX, posY - emberSize * 1.5f) // Flame tip pointing up
                    quadraticTo(posX + emberSize * 0.9f, posY, posX, posY + emberSize * 0.8f)
                    quadraticTo(posX - emberSize * 0.9f, posY, posX, posY - emberSize * 1.5f)
                    close()
                }
                drawPath(emberPath, coreCol.copy(alpha = flicker))
                drawCircle(Color.White.copy(alpha = flicker * 0.9f), radius = emberSize * 0.3f, center = Offset(posX, posY - emberSize * 0.3f))
            }
        }
    }

    // =========================================================================
    // 4. AQUA REEF: BUOYANT DEEP-SEA BUBBLES & PLANKTON
    // =========================================================================
    private fun DrawScope.drawAquaReefBubbles(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = BUBBLE_SEEDS

        for (p in particles) {
            val riseDist = timeSec * p.speedY
            val rawYNorm = (p.seedY - (riseDist / h)) % 1.0f
            val yNorm = if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm

            val swayX = sin(timeSec * p.wobbleFreqX + p.seedY * 6f) * p.wobbleAmpX
            val posX = (p.seedX * w + swayX).mod(w)
            val posY = (yNorm * h - scrollOffset * (p.depthLayer - 0.5f) * 0.35f).mod(h)

            val radius = p.baseSize * p.depthLayer
            val alpha = (0.35f + 0.45f * p.depthLayer).coerceIn(0.2f, 0.9f)

            // Outer soft oceanic blue glow
            drawCircle(Color(0x2538BDF8).copy(alpha = alpha * 0.4f), radius = radius * 1.6f, center = Offset(posX, posY))

            // Translucent bubble body
            drawCircle(Color(0x3567E8F9).copy(alpha = alpha * 0.65f), radius = radius, center = Offset(posX, posY))

            // Specular rim stroke
            drawCircle(Color(0x80BAE6FD).copy(alpha = alpha), radius = radius * 0.9f, style = Stroke(width = 1.3f), center = Offset(posX, posY))

            // Crescent specular reflection highlight
            val highlightRadius = radius * 0.32f
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.9f),
                radius = highlightRadius,
                center = Offset(posX - radius * 0.36f, posY - radius * 0.36f)
            )
        }
    }

    // =========================================================================
    // 5. SAKURA SANCTUARY: TUMBLING CHERRY BLOSSOM PETALS
    // =========================================================================
    private fun DrawScope.drawSakuraPetals(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = SAKURA_SEEDS

        for (p in particles) {
            val driftDistX = timeSec * p.speedX
            val driftDistY = timeSec * p.speedY
            val rawXNorm = (p.seedX + (driftDistX / w)) % 1.0f
            val rawYNorm = (p.seedY + (driftDistY / h)) % 1.0f

            val posX = (if (rawXNorm < 0f) rawXNorm + 1.0f else rawXNorm) * w
            val posY = ((if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm) * h - scrollOffset * (p.depthLayer - 0.5f) * 0.35f).mod(h)

            val currentRot = (p.initialRot + timeSec * p.rotSpeed) % 360f
            val tumbleScale = abs(cos(timeSec * 2.2f + p.pulsePhase)).coerceIn(0.18f, 1.0f)
            val size = p.baseSize * p.depthLayer
            val alpha = (0.45f + 0.45f * p.depthLayer).coerceIn(0.3f, 0.92f)

            rotate(degrees = currentRot, pivot = Offset(posX, posY)) {
                val pw = (size * 0.65f) * tumbleScale
                val ph = size

                // Heart-notched curved cherry blossom petal
                val petalPath = Path().apply {
                    moveTo(posX, posY + ph * 0.7f) // Stem base
                    cubicTo(posX - pw * 1.2f, posY + ph * 0.2f, posX - pw, posY - ph * 0.7f, posX - pw * 0.25f, posY - ph)
                    lineTo(posX, posY - ph * 0.75f) // Center petal notch
                    lineTo(posX + pw * 0.25f, posY - ph)
                    cubicTo(posX + pw, posY - ph * 0.7f, posX + pw * 1.2f, posY + ph * 0.2f, posX, posY + ph * 0.7f)
                    close()
                }

                val petalCol = if (p.colorVariant % 2 == 0) Color(0xFFF472B6) else Color(0xFFFBCFE8)
                drawPath(petalPath, petalCol.copy(alpha = alpha))
                drawPath(petalPath, Color(0xFFFDF2F8).copy(alpha = alpha * 0.7f), style = Stroke(width = 0.8f))
            }
        }
    }

    // =========================================================================
    // 6. FROZEN TUNDRA: ROTATING 6-POINT CRYSTALLINE SNOWFLAKES
    // =========================================================================
    private fun DrawScope.drawFrozenSnowflakes(
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = SNOWFLAKE_SEEDS

        for (p in particles) {
            val driftDistX = timeSec * p.speedX
            val driftDistY = timeSec * p.speedY
            val rawXNorm = (p.seedX + (driftDistX / w)) % 1.0f
            val rawYNorm = (p.seedY + (driftDistY / h)) % 1.0f

            val posX = (if (rawXNorm < 0f) rawXNorm + 1.0f else rawXNorm) * w
            val posY = ((if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm) * h - scrollOffset * (p.depthLayer - 0.5f) * 0.35f).mod(h)

            val currentRot = (p.initialRot + timeSec * p.rotSpeed) % 360f
            val flakeRadius = p.baseSize * p.depthLayer
            val alpha = (0.4f + 0.5f * p.depthLayer).coerceIn(0.25f, 0.95f)

            rotate(degrees = currentRot, pivot = Offset(posX, posY)) {
                // 6-pointed hexagonal crystal dendrite
                for (branch in 0 until 6) {
                    val angleRad = (branch * 60f) * (PI / 180f)
                    val cosA = cos(angleRad).toFloat()
                    val sinA = sin(angleRad).toFloat()

                    val endX = posX + cosA * flakeRadius
                    val endY = posY + sinA * flakeRadius

                    // Main arm
                    drawLine(
                        color = Color(0xFFE0F2FE).copy(alpha = alpha),
                        start = Offset(posX, posY),
                        end = Offset(endX, endY),
                        strokeWidth = 1.2f
                    )

                    // Side barb
                    val barbStartX = posX + cosA * (flakeRadius * 0.55f)
                    val barbStartY = posY + sinA * (flakeRadius * 0.55f)
                    val barbLen = flakeRadius * 0.32f

                    val perpA = angleRad + PI / 4f
                    drawLine(
                        color = Color.White.copy(alpha = alpha * 0.85f),
                        start = Offset(barbStartX, barbStartY),
                        end = Offset(
                            barbStartX + cos(perpA).toFloat() * barbLen,
                            barbStartY + sin(perpA).toFloat() * barbLen
                        ),
                        strokeWidth = 1.0f
                    )
                }

                drawCircle(Color.White.copy(alpha = alpha), radius = flakeRadius * 0.22f, center = Offset(posX, posY))
            }
        }
    }

    // =========================================================================
    // 7. GENERIC / COSMIC MOTES
    // =========================================================================
    private fun DrawScope.drawGenericMotes(
        theme: ZoneVisualTheme,
        w: Float,
        h: Float,
        timeSec: Float,
        scrollOffset: Float
    ) {
        val particles = GENERIC_MOTE_SEEDS

        for (p in particles) {
            val riseDist = timeSec * p.speedY
            val rawYNorm = (p.seedY - (riseDist / h)) % 1.0f
            val yNorm = if (rawYNorm < 0f) rawYNorm + 1.0f else rawYNorm

            val swayX = sin(timeSec * p.wobbleFreqX + p.seedY * 6f) * p.wobbleAmpX
            val posX = (p.seedX * w + swayX).mod(w)
            val posY = (yNorm * h - scrollOffset * (p.depthLayer - 0.5f) * 0.3f).mod(h)

            val flicker = (sin(timeSec * p.pulseFreq + p.pulsePhase) * 0.45f + 0.55f) * p.depthLayer
            val size = p.baseSize * p.depthLayer

            drawCircle(theme.primaryAccent.copy(alpha = flicker * 0.35f), radius = size * 2.2f, center = Offset(posX, posY))
            drawCircle(theme.secondaryAccent.copy(alpha = flicker * 0.85f), radius = size, center = Offset(posX, posY))
            drawCircle(Color.White.copy(alpha = flicker), radius = size * 0.35f, center = Offset(posX, posY))
        }
    }
}

/**
 * Reusable Canvas composable for ambient zone particles with smooth GPU-accelerated motion clock.
 */
@Composable
fun ZoneMapParticleOverlay(
    theme: ZoneVisualTheme,
    scrollOffset: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "zone_particles_clock")
    // Seamless continuous clock looping every 3600 seconds (1 hour of continuous non-repeating flow)
    val timeSeconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_600_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_seconds"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        with(ZoneMapParticleSystem) {
            drawZoneParticles(
                theme = theme,
                w = size.width,
                h = size.height,
                timeSeconds = timeSeconds,
                scrollOffset = scrollOffset
            )
        }
    }
}
