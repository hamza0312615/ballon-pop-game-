package com.example.balloonpop.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import com.example.balloonpop.ui.screens.LadderNodePos
import com.example.balloonpop.ui.theme.ZoneEnvironmentType
import com.example.balloonpop.ui.theme.ZonePathType
import com.example.balloonpop.ui.theme.ZoneVisualTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * High-performance, canvas-based environment renderer for campaign world zones.
 * Renders unique parallax layers, thematic environment illustrations, and custom paths.
 */
object ZoneEnvironmentRenderer {

    /**
     * Renders the complete multi-layer scenic background for the given zone.
     */
    fun DrawScope.drawScenicBackground(
        theme: ZoneVisualTheme,
        scrollOffset: Float,
        riverFlow: Float,
        density: Density,
        timeSeconds: Float = riverFlow * 25f
    ) {
        val w = size.width
        val h = size.height
        val zoneName = theme.name.lowercase()
        val isSakura = zoneName.contains("sakura")
        val isFrozen = zoneName.contains("frozen") || zoneName.contains("tundra")
        val isVolcano = zoneName.contains("volcano") || zoneName.contains("caldera") || zoneName.contains("molten")
        val isNeon = zoneName.contains("neon") || zoneName.contains("glade")

        // ----------------------------------------------------
        // LAYER 1: Deep Sky & Atmospheric Background
        // ----------------------------------------------------
        drawRect(brush = Brush.verticalGradient(theme.bgSkyGradient))

        when {
            isSakura -> drawSakuraDistantLayer(w, h, scrollOffset, riverFlow)
            isFrozen -> drawFrozenDistantLayer(w, h, scrollOffset, riverFlow)
            isVolcano -> drawVolcanoDistantLayer(w, h, scrollOffset, riverFlow)
            isNeon || theme.envType == ZoneEnvironmentType.NEON_GLADE -> {
                drawNeonGladeDistantLayer(w, h, scrollOffset, riverFlow)
            }
            theme.envType == ZoneEnvironmentType.PRISM_PEAKS -> {
                drawPrismPeaksDistantLayer(w, h, scrollOffset, riverFlow)
            }
            theme.envType == ZoneEnvironmentType.SUNSET_MESA -> {
                drawSunsetMesaDistantLayer(w, h, scrollOffset, riverFlow)
            }
            theme.envType == ZoneEnvironmentType.AQUA_REEF -> {
                drawAquaReefDistantLayer(w, h, scrollOffset, riverFlow)
            }
            else -> {
                drawGenericDistantLayer(theme, w, h, scrollOffset)
            }
        }

        // ----------------------------------------------------
        // LAYER 2: Thematic River / Water / Chasm Flow
        // ----------------------------------------------------
        drawThemedWaterway(theme, w, h, riverFlow)

        // ----------------------------------------------------
        // LAYER 3: Midground Environment Illustrations
        // ----------------------------------------------------
        when {
            isSakura -> drawSakuraMidground(w, h, riverFlow)
            isFrozen -> drawFrozenMidground(w, h, riverFlow)
            isVolcano -> drawVolcanoMidground(w, h, riverFlow)
            isNeon || theme.envType == ZoneEnvironmentType.NEON_GLADE -> {
                drawNeonGladeMidground(w, h, riverFlow)
            }
            theme.envType == ZoneEnvironmentType.PRISM_PEAKS -> {
                drawPrismPeaksMidground(w, h, riverFlow)
            }
            theme.envType == ZoneEnvironmentType.SUNSET_MESA -> {
                drawSunsetMesaMidground(w, h)
            }
            theme.envType == ZoneEnvironmentType.AQUA_REEF -> {
                drawAquaReefMidground(w, h, riverFlow)
            }
            else -> {
                drawGenericMidground(theme, w, h, riverFlow)
            }
        }

        // ----------------------------------------------------
        // LAYER 4: Thematic Ambient Particles & Floating FX
        // ----------------------------------------------------
        drawThemedParticles(theme, w, h, timeSeconds, scrollOffset)
    }

    // ========================================================
    // NEON GLADE: Bioluminescent Forest / Jungle
    // ========================================================
    private fun DrawScope.drawNeonGladeDistantLayer(
        w: Float,
        h: Float,
        scrollOffset: Float,
        riverFlow: Float
    ) {
        val parallax = scrollOffset * 0.12f

        // Distant giant ancient hollow banyan trees
        val trunkColor = Color(0xFF03100B)
        drawRect(
            color = trunkColor,
            topLeft = Offset(-20f, -parallax),
            size = Size(w * 0.22f, h + parallax)
        )
        drawRect(
            color = trunkColor,
            topLeft = Offset(w * 0.82f, -parallax),
            size = Size(w * 0.25f, h + parallax)
        )

        // Bioluminescent canopy mist bands drifting horizontally
        for (m in 0 until 4) {
            val mistY = h * (0.15f + m * 0.24f)
            val sway = sin((riverFlow * 2f + m * 1.5f) * PI.toFloat()) * (w * 0.08f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2200F0FF), Color(0x0A06D6A0), Color.Transparent),
                    center = Offset(w * 0.45f + sway, mistY),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.45f + sway, mistY),
                radius = w * 0.55f
            )
        }

        // Treetop bio-spores in upper reaches
        for (s in 0 until 12) {
            val sx = (w * ((s * 0.19f + 0.05f) % 1.0f))
            val sy = (h * ((s * 0.09f + 0.02f) % 0.4f))
            drawCircle(
                color = Color(0x3300F0FF),
                radius = 6f,
                center = Offset(sx, sy)
            )
        }
    }

    private fun DrawScope.drawNeonGladeMidground(
        w: Float,
        h: Float,
        riverFlow: Float
    ) {
        // 1. Giant layered bioluminescent tree mushrooms on forest flanks
        val shroomSpots = listOf(
            Triple(w * 0.06f, h * 0.88f, 32f),
            Triple(w * 0.94f, h * 0.78f, 36f),
            Triple(w * 0.05f, h * 0.48f, 30f),
            Triple(w * 0.95f, h * 0.36f, 34f),
            Triple(w * 0.07f, h * 0.20f, 28f),
            Triple(w * 0.93f, h * 0.08f, 32f)
        )

        for ((idx, spot) in shroomSpots.withIndex()) {
            val (sx, sy, rad) = spot
            val isLeft = sx < w * 0.5f
            val pulse = (sin((riverFlow * 3f + idx) * PI.toFloat()) * 0.2f + 0.8f)

            // Mushroom stem
            val stemEndX = if (isLeft) sx + rad * 0.8f else sx - rad * 0.8f
            drawLine(
                color = Color(0xFF1E3A2B),
                start = Offset(sx, sy),
                end = Offset(stemEndX, sy - rad * 0.6f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            // Mushroom Cap (glowing bioluminescent cyan/emerald cap)
            val capCenter = Offset(stemEndX, sy - rad * 0.6f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color(0xFF6EE7B7),
                        Color(0xFF00F0FF).copy(alpha = pulse),
                        Color(0xFF065F46)
                    ),
                    center = capCenter,
                    radius = rad
                ),
                radius = rad,
                center = capCenter
            )

            // Bioluminescent gill rim
            drawCircle(
                color = Color(0xFFD1FAE5).copy(alpha = 0.7f),
                radius = rad * 0.35f,
                center = capCenter - Offset(rad * 0.2f, rad * 0.2f)
            )
        }

        // 2. Curling Jungle Fern Fronds & Hanging Lianas
        val fernSpots = listOf(
            Pair(w * 0.12f, h * 0.72f),
            Pair(w * 0.88f, h * 0.62f),
            Pair(w * 0.10f, h * 0.32f),
            Pair(w * 0.90f, h * 0.18f)
        )
        for ((fIdx, spot) in fernSpots.withIndex()) {
            val (fx, fy) = spot
            val sway = sin((riverFlow * 2f + fIdx) * PI.toFloat()) * 8f
            val fernPath = Path().apply {
                moveTo(fx, fy)
                cubicTo(fx + sway, fy - 25f, fx + sway * 1.5f, fy - 45f, fx + sway * 0.8f, fy - 65f)
            }
            drawPath(
                path = fernPath,
                color = Color(0xFF059669).copy(alpha = 0.75f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Little leaf leaves along fern
            drawCircle(
                color = Color(0xFF34D399),
                radius = 5f,
                center = Offset(fx + sway * 0.8f, fy - 65f)
            )
        }
    }

    // ========================================================
    // PRISM PEAKS: Crystalline Mountain & Gem Cavern
    // ========================================================
    private fun DrawScope.drawPrismPeaksDistantLayer(
        w: Float,
        h: Float,
        scrollOffset: Float,
        riverFlow: Float
    ) {
        val parallax = scrollOffset * 0.15f

        // Distant sharp crystalline mountain peak silhouettes
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.40f - parallax)
            lineTo(w * 0.22f, h * 0.20f - parallax)
            lineTo(w * 0.45f, h * 0.34f - parallax)
            lineTo(w * 0.72f, h * 0.14f - parallax)
            lineTo(w, h * 0.32f - parallax)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = mountainPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF19072E), Color(0xFF0C0317)),
                startY = h * 0.14f - parallax,
                endY = h
            )
        )

        // Refracted prism light shafts (aurora beams from cavern roof fissures)
        for (b in 0 until 3) {
            val beamX = w * (0.25f + b * 0.28f)
            val beamSway = sin((riverFlow * 2.5f + b) * PI.toFloat()) * 20f
            val beamPath = Path().apply {
                moveTo(beamX - 15f, 0f)
                lineTo(beamX + 15f, 0f)
                lineTo(beamX + 60f + beamSway, h * 0.65f)
                lineTo(beamX - 40f + beamSway, h * 0.65f)
                close()
            }
            val beamColors = when (b) {
                0 -> listOf(Color(0x33C084FC), Color(0x05F472B6), Color.Transparent)
                1 -> listOf(Color(0x3338BDF8), Color(0x05A855F7), Color.Transparent)
                else -> listOf(Color(0x33F472B6), Color(0x05E879F9), Color.Transparent)
            }
            drawPath(beamPath, Brush.verticalGradient(beamColors, startY = 0f, endY = h * 0.65f))
        }

        // Geometric crystal constellations
        for (c in 0 until 8) {
            val cx = (w * (c * 0.13f + 0.08f))
            val cy = (h * (c * 0.05f + 0.04f))
            drawCircle(color = Color(0x55FAF5FF), radius = 3.5f, center = Offset(cx, cy))
        }
    }

    private fun DrawScope.drawPrismPeaksMidground(
        w: Float,
        h: Float,
        riverFlow: Float
    ) {
        // Multifaceted towering crystal spires & amethyst clusters
        val spireSpots = listOf(
            Triple(w * 0.08f, h * 0.88f, Color(0xFFC084FC)),
            Triple(w * 0.92f, h * 0.78f, Color(0xFFF472B6)),
            Triple(w * 0.06f, h * 0.48f, Color(0xFF38BDF8)),
            Triple(w * 0.94f, h * 0.36f, Color(0xFFE879F9)),
            Triple(w * 0.09f, h * 0.20f, Color(0xFFA855F7)),
            Triple(w * 0.91f, h * 0.09f, Color(0xFF818CF8))
        )

        for ((idx, spire) in spireSpots.withIndex()) {
            val (sx, sy, col) = spire
            val shimmer = sin((riverFlow * 3f + idx * 1.5f) * PI.toFloat()) * 5f
            val spireHeight = 65f + (idx % 3) * 12f
            val spireWidth = 24f

            // Faceted angular crystal path
            val crystalPath = Path().apply {
                moveTo(sx, sy - spireHeight - shimmer) // Apex tip
                lineTo(sx - spireWidth, sy - spireHeight * 0.35f)
                lineTo(sx - spireWidth * 0.6f, sy)
                lineTo(sx + spireWidth * 0.6f, sy)
                lineTo(sx + spireWidth, sy - spireHeight * 0.35f)
                close()
            }

            drawPath(
                path = crystalPath,
                brush = Brush.verticalGradient(
                    listOf(Color.White, col, col.copy(alpha = 0.5f)),
                    startY = sy - spireHeight - shimmer,
                    endY = sy
                )
            )

            // Inner facet specular ridge
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(sx, sy - spireHeight - shimmer),
                end = Offset(sx, sy),
                strokeWidth = 2f
            )

            // Floating orbital micro-shard
            val orbDist = 28f
            val orbAngle = (riverFlow * 2f + idx * 1.2f) * PI.toFloat()
            val ox = sx + cos(orbAngle) * orbDist
            val oy = (sy - spireHeight * 0.5f) + sin(orbAngle) * (orbDist * 0.4f)
            val shardPath = Path().apply {
                moveTo(ox, oy - 9f)
                lineTo(ox + 5f, oy)
                lineTo(ox, oy + 9f)
                lineTo(ox - 5f, oy)
                close()
            }
            drawPath(shardPath, col)
            drawPath(shardPath, Color.White.copy(alpha = 0.7f), style = Stroke(width = 1f))
        }
    }

    // ========================================================
    // SUNSET MESA & AQUA REEF & GENERIC MIDGROUNDS
    // ========================================================
    private fun DrawScope.drawSunsetMesaDistantLayer(w: Float, h: Float, scrollOffset: Float, riverFlow: Float) {
        val parallax = scrollOffset * 0.12f
        // Red-rock canyon butte silhouettes
        val canyonPath = Path().apply {
            moveTo(0f, h * 0.35f - parallax)
            lineTo(w * 0.30f, h * 0.35f - parallax)
            lineTo(w * 0.35f, h * 0.42f - parallax)
            lineTo(w * 0.65f, h * 0.42f - parallax)
            lineTo(w * 0.70f, h * 0.28f - parallax)
            lineTo(w, h * 0.28f - parallax)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(canyonPath, Color(0xFF260A16))
    }

    private fun DrawScope.drawSunsetMesaMidground(w: Float, h: Float) {
        val mesaSpots = listOf(
            Triple(w * 0.08f, h * 0.88f, Color(0xFF78350F)),
            Triple(w * 0.92f, h * 0.78f, Color(0xFFB45309)),
            Triple(w * 0.07f, h * 0.48f, Color(0xFF9A3412)),
            Triple(w * 0.93f, h * 0.36f, Color(0xFFD97706)),
            Triple(w * 0.09f, h * 0.20f, Color(0xFFB45309)),
            Triple(w * 0.91f, h * 0.09f, Color(0xFF78350F))
        )
        for (mesa in mesaSpots) {
            val (mx, my, mCol) = mesa
            drawRoundRect(
                color = mCol,
                topLeft = Offset(mx - 24f, my - 36f),
                size = Size(48f, 36f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFFFDBA74).copy(alpha = 0.6f),
                topLeft = Offset(mx - 22f, my - 38f),
                size = Size(44f, 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
        }
    }

    private fun DrawScope.drawAquaReefDistantLayer(w: Float, h: Float, scrollOffset: Float, riverFlow: Float) {
        // Deep water sun rays from ocean surface
        for (r in 0 until 3) {
            val rx = w * (0.2f + r * 0.3f)
            val sway = sin((riverFlow * 2f + r) * PI.toFloat()) * 18f
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0x3367E8F9), Color.Transparent)),
                start = Offset(rx, 0f),
                end = Offset(rx + sway, h * 0.7f),
                strokeWidth = 32f
            )
        }
    }

    private fun DrawScope.drawAquaReefMidground(w: Float, h: Float, riverFlow: Float) {
        val coralSpots = listOf(
            Triple(w * 0.08f, h * 0.88f, Color(0xFFF43F5E)),
            Triple(w * 0.92f, h * 0.78f, Color(0xFF06B6D4)),
            Triple(w * 0.07f, h * 0.48f, Color(0xFFA855F7)),
            Triple(w * 0.93f, h * 0.36f, Color(0xFFEC4899)),
            Triple(w * 0.09f, h * 0.20f, Color(0xFF38BDF8)),
            Triple(w * 0.91f, h * 0.09f, Color(0xFF10B981))
        )
        for ((coIdx, coral) in coralSpots.withIndex()) {
            val (cox, coy, coCol) = coral
            val sway = sin((riverFlow * 2.5f + coIdx) * PI.toFloat()) * 6f
            drawCircle(color = coCol.copy(alpha = 0.8f), radius = 20f, center = Offset(cox + sway, coy - 24f))
            drawCircle(color = Color.White.copy(alpha = 0.45f), radius = 10f, center = Offset(cox + sway - 4f, coy - 27f))
            drawLine(color = coCol, start = Offset(cox, coy), end = Offset(cox + sway, coy - 24f), strokeWidth = 4f)
        }
    }

    // ========================================================
    // SAKURA SANCTUARY: Soft Pink Mountains, Pagoda & Cherry Blossoms
    // ========================================================
    private fun DrawScope.drawSakuraDistantLayer(w: Float, h: Float, scrollOffset: Float, riverFlow: Float) {
        val parallax = scrollOffset * 0.10f

        // Distant soft pink misty mountain ridges
        val mountainPath = Path().apply {
            moveTo(0f, h * 0.42f - parallax)
            cubicTo(w * 0.25f, h * 0.28f - parallax, w * 0.45f, h * 0.38f - parallax, w * 0.70f, h * 0.22f - parallax)
            cubicTo(w * 0.85f, h * 0.28f - parallax, w * 0.95f, h * 0.20f - parallax, w, h * 0.25f - parallax)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = mountainPath,
            brush = Brush.verticalGradient(
                listOf(Color(0x35F472B6), Color(0x553B152B), Color(0x88240D1B))
            )
        )

        // Pagoda Silhouette perched atop the mountain crest (around w * 0.70f, h * 0.22f)
        val pagodaX = w * 0.70f
        val pagodaY = h * 0.22f - parallax
        val pagodaColor = Color(0xDD2A0E20)

        // Pagoda Spire (Sōrin)
        drawLine(
            color = Color(0xFFFFD1DC),
            start = Offset(pagodaX, pagodaY - 32f),
            end = Offset(pagodaX, pagodaY - 6f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Pagoda 3-Tier Roofs
        for (tier in 0..2) {
            val tierY = pagodaY - 4f + tier * 9f
            val roofWidth = 24f + (2 - tier) * 6f
            val roofPath = Path().apply {
                moveTo(pagodaX - roofWidth * 0.5f, tierY)
                cubicTo(
                    pagodaX - roofWidth * 0.3f, tierY - 4f,
                    pagodaX + roofWidth * 0.3f, tierY - 4f,
                    pagodaX + roofWidth * 0.5f, tierY
                )
                lineTo(pagodaX + roofWidth * 0.35f, tierY + 4f)
                lineTo(pagodaX - roofWidth * 0.35f, tierY + 4f)
                close()
            }
            drawPath(path = roofPath, color = pagodaColor)
            // Pagoda Body wall between tiers
            drawRect(
                color = Color(0xAA3B152B),
                topLeft = Offset(pagodaX - 5f, tierY + 3f),
                size = Size(10f, 6f)
            )
        }

        // Pagoda Base
        drawRect(
            color = pagodaColor,
            topLeft = Offset(pagodaX - 10f, pagodaY + 22f),
            size = Size(20f, 8f)
        )

        // Soft pink ambient mist clouds drifting across mountain base
        for (m in 0 until 3) {
            val mistY = h * (0.35f + m * 0.22f)
            val sway = sin((riverFlow * 1.5f + m * 2f) * PI.toFloat()) * (w * 0.06f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x30FBCFE8), Color(0x15F472B6), Color.Transparent),
                    center = Offset(w * 0.5f + sway, mistY),
                    radius = w * 0.50f
                ),
                center = Offset(w * 0.5f + sway, mistY),
                radius = w * 0.50f
            )
        }
    }

    private fun DrawScope.drawSakuraMidground(w: Float, h: Float, riverFlow: Float) {
        // Japanese Red Torii Gate on slope (w * 0.18f, h * 0.52f)
        val toriiX = w * 0.18f
        val toriiY = h * 0.52f
        val toriiRed = Color(0xFFDC2626)
        val toriiBlack = Color(0xFF1E1E24)

        // Twin Torii Pillars
        drawLine(color = toriiRed, start = Offset(toriiX - 14f, toriiY + 26f), end = Offset(toriiX - 13f, toriiY - 2f), strokeWidth = 4f, cap = StrokeCap.Round)
        drawLine(color = toriiRed, start = Offset(toriiX + 14f, toriiY + 26f), end = Offset(toriiX + 13f, toriiY - 2f), strokeWidth = 4f, cap = StrokeCap.Round)
        // Main Upper Lintel (Kasagi) with upturned tips
        val kasagiPath = Path().apply {
            moveTo(toriiX - 22f, toriiY - 6f)
            cubicTo(toriiX - 10f, toriiY - 3f, toriiX + 10f, toriiY - 3f, toriiX + 22f, toriiY - 6f)
            lineTo(toriiX + 20f, toriiY - 1f)
            lineTo(toriiX - 20f, toriiY - 1f)
            close()
        }
        drawPath(path = kasagiPath, color = toriiBlack)
        // Secondary Crossbeam (Nuki)
        drawLine(color = toriiRed, start = Offset(toriiX - 17f, toriiY + 6f), end = Offset(toriiX + 17f, toriiY + 6f), strokeWidth = 3f)

        // Left & Right Flanking Cherry Blossom Trees (Mochi Cloud Foliage)
        val sakuraTrees = listOf(
            Triple(w * 0.08f, h * 0.82f, 1.1f),
            Triple(w * 0.92f, h * 0.74f, 1.25f),
            Triple(w * 0.06f, h * 0.44f, 0.95f),
            Triple(w * 0.94f, h * 0.32f, 1.15f),
            Triple(w * 0.12f, h * 0.15f, 0.85f),
            Triple(w * 0.88f, h * 0.08f, 1.05f)
        )

        for ((idx, tree) in sakuraTrees.withIndex()) {
            val (tx, ty, scale) = tree
            val sway = sin((riverFlow * 2.2f + idx * 1.1f) * PI.toFloat()) * (4f * scale)
            val trunkColor = Color(0xFF2E1A29)

            // Gnarled dark trunk and branches
            drawLine(
                color = trunkColor,
                start = Offset(tx, ty + 20f * scale),
                end = Offset(tx + sway * 0.5f, ty - 18f * scale),
                strokeWidth = 5f * scale,
                cap = StrokeCap.Round
            )
            // Left branch
            drawLine(
                color = trunkColor,
                start = Offset(tx + sway * 0.5f, ty - 5f * scale),
                end = Offset(tx - 18f * scale + sway, ty - 24f * scale),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )
            // Right branch
            drawLine(
                color = trunkColor,
                start = Offset(tx + sway * 0.5f, ty - 8f * scale),
                end = Offset(tx + 18f * scale + sway, ty - 26f * scale),
                strokeWidth = 3f * scale,
                cap = StrokeCap.Round
            )

            // Volumetric Pink Blossom Clusters (Layered soft pink ovals)
            val clusterOffset = Offset(tx + sway, ty - 30f * scale)
            // Deep base bloom
            drawCircle(
                color = Color(0xFFF472B6).copy(alpha = 0.85f),
                radius = 24f * scale,
                center = clusterOffset
            )
            // Left blossom puff
            drawCircle(
                color = Color(0xFFFBCFE8).copy(alpha = 0.92f),
                radius = 18f * scale,
                center = Offset(clusterOffset.x - 14f * scale, clusterOffset.y - 6f * scale)
            )
            // Right blossom puff
            drawCircle(
                color = Color(0xFFFBCFE8).copy(alpha = 0.92f),
                radius = 17f * scale,
                center = Offset(clusterOffset.x + 14f * scale, clusterOffset.y - 5f * scale)
            )
            // Crown highlight puff
            drawCircle(
                color = Color(0xFFFFF0F5).copy(alpha = 0.95f),
                radius = 14f * scale,
                center = Offset(clusterOffset.x, clusterOffset.y - 12f * scale)
            )
        }

        // Stone Lantern (Tōrō) on ground with warm inner glow
        val lanternX = w * 0.82f
        val lanternY = h * 0.56f
        drawRect(color = Color(0xFF475569), topLeft = Offset(lanternX - 6f, lanternY + 6f), size = Size(12f, 10f))
        drawCircle(color = Color(0xFFFFD54F), radius = 4f, center = Offset(lanternX, lanternY + 3f))
        drawCircle(color = Color(0x55FFA000), radius = 10f, center = Offset(lanternX, lanternY + 3f))
        drawRect(color = Color(0xFF334155), topLeft = Offset(lanternX - 9f, lanternY - 4f), size = Size(18f, 5f))
    }

    // ========================================================
    // FROZEN TUNDRA: Aurora Borealis, Glaciers & Frost Pines
    // ========================================================
    private fun DrawScope.drawFrozenDistantLayer(w: Float, h: Float, scrollOffset: Float, riverFlow: Float) {
        val parallax = scrollOffset * 0.10f

        // Aurora Borealis Ribbon undulating across the night sky
        val auroraSway = sin((riverFlow * 1.8f) * PI.toFloat()) * (w * 0.08f)
        val auroraPath = Path().apply {
            moveTo(0f, h * 0.08f - parallax)
            cubicTo(
                w * 0.28f + auroraSway, h * 0.16f - parallax,
                w * 0.65f - auroraSway, h * 0.04f - parallax,
                w, h * 0.12f - parallax
            )
            lineTo(w, h * 0.22f - parallax)
            cubicTo(
                w * 0.65f - auroraSway, h * 0.14f - parallax,
                w * 0.28f + auroraSway, h * 0.26f - parallax,
                0f, h * 0.18f - parallax
            )
            close()
        }
        drawPath(
            path = auroraPath,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0x1000F0FF),
                    Color(0x4500F0FF), // Electric cyan
                    Color(0x4034D399), // Emerald
                    Color(0x35C084FC), // Aurora magenta
                    Color(0x1000F0FF)
                )
            )
        )

        // Distant Jagged Glacial Ice Spire Peaks
        val glacierPath = Path().apply {
            moveTo(0f, h * 0.44f - parallax)
            lineTo(w * 0.15f, h * 0.32f - parallax)
            lineTo(w * 0.28f, h * 0.40f - parallax)
            lineTo(w * 0.48f, h * 0.24f - parallax) // Major ice peak
            lineTo(w * 0.62f, h * 0.36f - parallax)
            lineTo(w * 0.82f, h * 0.22f - parallax) // Second sharp glacier
            lineTo(w * 0.94f, h * 0.35f - parallax)
            lineTo(w, h * 0.30f - parallax)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = glacierPath,
            brush = Brush.verticalGradient(
                listOf(Color(0x440284C7), Color(0x66082F49), Color(0x99031726))
            )
        )

        // Snow Caps on the Glacier Summits
        val snowCap1 = Path().apply {
            moveTo(w * 0.48f, h * 0.24f - parallax)
            lineTo(w * 0.44f, h * 0.28f - parallax)
            lineTo(w * 0.52f, h * 0.28f - parallax)
            close()
        }
        drawPath(path = snowCap1, color = Color(0xCCDDF4FF))

        val snowCap2 = Path().apply {
            moveTo(w * 0.82f, h * 0.22f - parallax)
            lineTo(w * 0.78f, h * 0.26f - parallax)
            lineTo(w * 0.86f, h * 0.26f - parallax)
            close()
        }
        drawPath(path = snowCap2, color = Color(0xCCDDF4FF))
    }

    private fun DrawScope.drawFrozenMidground(w: Float, h: Float, riverFlow: Float) {
        // Snow-Covered Alpine Conifers flanking the ladder
        val pineSpots = listOf(
            Triple(w * 0.08f, h * 0.84f, 1.15f),
            Triple(w * 0.92f, h * 0.72f, 1.25f),
            Triple(w * 0.06f, h * 0.46f, 0.95f),
            Triple(w * 0.94f, h * 0.34f, 1.10f),
            Triple(w * 0.10f, h * 0.16f, 0.90f),
            Triple(w * 0.88f, h * 0.07f, 1.05f)
        )

        for ((idx, pine) in pineSpots.withIndex()) {
            val (px, py, scale) = pine
            val sway = sin((riverFlow * 1.6f + idx * 0.9f) * PI.toFloat()) * (3f * scale)
            val pineDark = Color(0xFF062130)
            val snowWhite = Color(0xFFF0F9FF)

            // Conifer Trunk
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(px, py + 18f * scale),
                end = Offset(px + sway * 0.4f, py - 6f * scale),
                strokeWidth = 4f * scale,
                cap = StrokeCap.Round
            )

            // Tiered Triangular Conifer Branches (Bottom, Middle, Top)
            for (t in 0..2) {
                val tierY = py - 6f * scale - t * 14f * scale
                val tierWidth = (34f - t * 8f) * scale
                val tierHeight = 16f * scale

                // Dark evergreen needles
                val branchPath = Path().apply {
                    moveTo(px + sway * 0.7f, tierY - tierHeight)
                    lineTo(px - tierWidth * 0.5f + sway * 0.5f, tierY)
                    lineTo(px + tierWidth * 0.5f + sway * 0.5f, tierY)
                    close()
                }
                drawPath(path = branchPath, color = pineDark)

                // Snow Crest on top of each tier
                val snowPath = Path().apply {
                    moveTo(px + sway * 0.7f, tierY - tierHeight)
                    lineTo(px - tierWidth * 0.35f + sway * 0.5f, tierY - tierHeight * 0.35f)
                    lineTo(px + tierWidth * 0.35f + sway * 0.5f, tierY - tierHeight * 0.35f)
                    close()
                }
                drawPath(path = snowPath, color = snowWhite)
            }
        }

        // Crystalline Ice Monoliths with bright cyan refraction
        val iceShards = listOf(
            Pair(w * 0.22f, h * 0.62f),
            Pair(w * 0.80f, h * 0.48f),
            Pair(w * 0.20f, h * 0.26f)
        )
        for (shard in iceShards) {
            val (ix, iy) = shard
            val shardPath = Path().apply {
                moveTo(ix, iy - 26f)
                lineTo(ix + 9f, iy - 6f)
                lineTo(ix + 6f, iy + 16f)
                lineTo(ix - 7f, iy + 14f)
                lineTo(ix - 10f, iy - 8f)
                close()
            }
            drawPath(
                path = shardPath,
                brush = Brush.linearGradient(
                    listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7))
                )
            )
            // Specular glint
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(ix - 2f, iy - 22f),
                end = Offset(ix - 5f, iy + 10f),
                strokeWidth = 2f
            )
        }
    }

    // ========================================================
    // VOLCANO CALDERA: Basalt Rock Crags, Magma & Lava Fissures
    // ========================================================
    private fun DrawScope.drawVolcanoDistantLayer(w: Float, h: Float, scrollOffset: Float, riverFlow: Float) {
        val parallax = scrollOffset * 0.10f

        // Fiery Caldera Crater Rim Horizon Backglow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x70FF4500), Color(0x35EA580C), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.25f - parallax),
                radius = w * 0.70f
            ),
            center = Offset(w * 0.5f, h * 0.25f - parallax),
            radius = w * 0.70f
        )

        // Jagged Basalt Caldera Rim Silhouette
        val calderaPath = Path().apply {
            moveTo(0f, h * 0.38f - parallax)
            lineTo(w * 0.20f, h * 0.30f - parallax)
            lineTo(w * 0.38f, h * 0.40f - parallax) // Crater dip
            lineTo(w * 0.62f, h * 0.40f - parallax) // Crater dip center
            lineTo(w * 0.80f, h * 0.28f - parallax)
            lineTo(w, h * 0.36f - parallax)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = calderaPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1E0A0D), Color(0xFF2E0F14), Color(0xFF140508))
            )
        )

        // Volcanic Smoke and Ember Columns rising from the caldera
        for (c in 0 until 4) {
            val smokeX = w * (0.32f + c * 0.12f)
            val smokeSway = sin((riverFlow * 2.5f + c * 1.5f) * PI.toFloat()) * (w * 0.04f)
            val smokeY = (h * (0.35f - c * 0.08f) - parallax)
            drawCircle(
                color = Color(0x28FF6D00),
                radius = 28f + c * 6f,
                center = Offset(smokeX + smokeSway, smokeY)
            )
        }
    }

    private fun DrawScope.drawVolcanoMidground(w: Float, h: Float, riverFlow: Float) {
        // Jagged Basalt Rock Ledges and Obsidian Outcrops
        val basaltSpots = listOf(
            Triple(w * 0.08f, h * 0.84f, Color(0xFF261216)),
            Triple(w * 0.92f, h * 0.74f, Color(0xFF1E0D10)),
            Triple(w * 0.06f, h * 0.48f, Color(0xFF261216)),
            Triple(w * 0.94f, h * 0.34f, Color(0xFF1E0D10)),
            Triple(w * 0.10f, h * 0.18f, Color(0xFF2A1419)),
            Triple(w * 0.90f, h * 0.09f, Color(0xFF1F0C0F))
        )

        for ((idx, rock) in basaltSpots.withIndex()) {
            val (rx, ry, rCol) = rock
            // Basalt tiered crag
            drawRoundRect(
                color = rCol,
                topLeft = Offset(rx - 28f, ry - 32f),
                size = Size(56f, 32f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            // Hot Molten Magma Vein pulsing in the rock crevice
            val veinPulse = 0.70f + 0.30f * sin((riverFlow * 3f + idx) * PI.toFloat())
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFF3D00), Color(0xFFFFA000), Color(0xFFFF3D00))
                ),
                start = Offset(rx - 18f, ry - 14f),
                end = Offset(rx + 18f, ry - 12f),
                strokeWidth = 3f * veinPulse,
                cap = StrokeCap.Round
            )
        }
    }

    private fun DrawScope.drawGenericDistantLayer(theme: ZoneVisualTheme, w: Float, h: Float, scrollOffset: Float) {
        val parallax = scrollOffset * 0.1f
        drawCircle(
            color = theme.primaryAccent.copy(alpha = 0.15f),
            radius = w * 0.45f,
            center = Offset(w * 0.5f, h * 0.3f - parallax)
        )
    }

    private fun DrawScope.drawGenericMidground(theme: ZoneVisualTheme, w: Float, h: Float, riverFlow: Float) {
        val pCol = theme.primaryAccent
        val sCol = theme.secondaryAccent
        val treeSpots = listOf(
            Triple(w * 0.08f, h * 0.88f, pCol),
            Triple(w * 0.92f, h * 0.78f, sCol),
            Triple(w * 0.07f, h * 0.48f, pCol),
            Triple(w * 0.93f, h * 0.36f, sCol)
        )
        for ((idx, tree) in treeSpots.withIndex()) {
            val (tx, ty, crownCol) = tree
            val sway = sin((riverFlow * 2f + idx * 0.8f) * PI.toFloat()) * 5f
            drawLine(
                color = Color(0xFF8D5B4C),
                start = Offset(tx, ty),
                end = Offset(tx + sway * 0.5f, ty - 34f),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )
            drawCircle(color = crownCol.copy(alpha = 0.85f), radius = 20f, center = Offset(tx + sway, ty - 42f))
        }
    }

    // ========================================================
    // WATERWAY / RIVER / CHASM DRAWING
    // ========================================================
    private fun DrawScope.drawThemedWaterway(
        theme: ZoneVisualTheme,
        w: Float,
        h: Float,
        riverFlow: Float
    ) {
        val zoneName = theme.name.lowercase()
        val isSakura = zoneName.contains("sakura")
        val isFrozen = zoneName.contains("frozen") || zoneName.contains("tundra")
        val isVolcano = zoneName.contains("volcano") || zoneName.contains("caldera") || zoneName.contains("molten")

        if (isVolcano) {
            // Molten Glowing Lava River with basalt crust banks
            val lavaPath = Path().apply {
                moveTo(w * 0.72f, 0f)
                cubicTo(w * 0.36f, h * 0.22f, w * 0.88f, h * 0.46f, w * 0.42f, h * 0.70f)
                cubicTo(w * 0.12f, h * 0.82f, w * 0.58f, h * 0.92f, w * 0.74f, h)
                lineTo(w * 0.96f, h)
                cubicTo(w * 0.80f, h * 0.92f, w * 0.32f, h * 0.82f, w * 0.62f, h * 0.70f)
                cubicTo(w * 1.08f, h * 0.46f, w * 0.56f, h * 0.22f, w * 0.90f, 0f)
                close()
            }
            drawPath(
                path = lavaPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF7F1D1D),
                        Color(0xFFEA580C),
                        Color(0xFFFBBF24),
                        Color(0xFFFFFBEB),
                        Color(0xFFFBBF24),
                        Color(0xFFEA580C),
                        Color(0xFF7F1D1D)
                    )
                )
            )

            // Searing Magma Heat Ripples
            for (r in 0 until 8) {
                val rippleY = (h * ((r * 0.125f + riverFlow * 0.22f) % 1.0f))
                val rippleX = w * (0.45f + 0.22f * sin((rippleY / h) * 6.28f))
                drawLine(
                    color = Color(0xFFFFFBEB).copy(alpha = 0.85f),
                    start = Offset(rippleX - 18f, rippleY),
                    end = Offset(rippleX + 18f, rippleY + 2f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        } else if (isFrozen) {
            // Glacial Ice Rift with crystalline blue water
            val icePath = Path().apply {
                moveTo(w * 0.70f, 0f)
                cubicTo(w * 0.40f, h * 0.22f, w * 0.86f, h * 0.46f, w * 0.40f, h * 0.70f)
                cubicTo(w * 0.15f, h * 0.82f, w * 0.60f, h * 0.92f, w * 0.75f, h)
                lineTo(w * 0.95f, h)
                cubicTo(w * 0.80f, h * 0.92f, w * 0.34f, h * 0.82f, w * 0.60f, h * 0.70f)
                cubicTo(w * 1.06f, h * 0.46f, w * 0.60f, h * 0.22f, w * 0.90f, 0f)
                close()
            }
            drawPath(
                path = icePath,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF0369A1),
                        Color(0xFF38BDF8),
                        Color(0xFFBAE6FD),
                        Color(0xFFE0F2FE),
                        Color(0xFF38BDF8),
                        Color(0xFF0369A1)
                    )
                )
            )

            // Surface Frost Crack Ripples
            for (r in 0 until 8) {
                val rippleY = (h * ((r * 0.125f + riverFlow * 0.12f) % 1.0f))
                val rippleX = w * (0.45f + 0.22f * sin((rippleY / h) * 6.28f))
                drawLine(
                    color = Color.White.copy(alpha = 0.75f),
                    start = Offset(rippleX - 20f, rippleY),
                    end = Offset(rippleX + 20f, rippleY + 1f),
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }
        } else if (isSakura) {
            // Serene Misty Koi Stream with pink blossom reflections
            val sakuraRiverPath = Path().apply {
                moveTo(w * 0.70f, 0f)
                cubicTo(w * 0.38f, h * 0.22f, w * 0.90f, h * 0.46f, w * 0.40f, h * 0.70f)
                cubicTo(w * 0.10f, h * 0.82f, w * 0.60f, h * 0.92f, w * 0.76f, h)
                lineTo(w * 0.98f, h)
                cubicTo(w * 0.82f, h * 0.92f, w * 0.30f, h * 0.82f, w * 0.64f, h * 0.70f)
                cubicTo(w * 1.10f, h * 0.46f, w * 0.58f, h * 0.22f, w * 0.92f, 0f)
                close()
            }
            drawPath(
                path = sakuraRiverPath,
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF5A2041),
                        Color(0xFFF472B6),
                        Color(0xFFFBCFE8),
                        Color(0xFFF472B6),
                        Color(0xFF3B152B)
                    )
                )
            )

            // Drifting Cherry Blossom Petal Motes on Stream
            for (r in 0 until 8) {
                val rippleY = (h * ((r * 0.125f + riverFlow * 0.16f) % 1.0f))
                val rippleX = w * (0.45f + 0.22f * sin((rippleY / h) * 6.28f))
                drawCircle(
                    color = Color(0xFFFFF0F5).copy(alpha = 0.85f),
                    radius = 3.5f,
                    center = Offset(rippleX, rippleY)
                )
                drawCircle(
                    color = Color(0xFFF472B6).copy(alpha = 0.50f),
                    radius = 6.5f,
                    center = Offset(rippleX, rippleY)
                )
            }
        } else if (theme.envType == ZoneEnvironmentType.PRISM_PEAKS) {
            // Jagged, fractured crystalline rift river (sharp angular zig-zag bends)
            val riftPath = Path().apply {
                moveTo(w * 0.75f, 0f)
                lineTo(w * 0.45f, h * 0.18f)
                lineTo(w * 0.85f, h * 0.42f)
                lineTo(w * 0.35f, h * 0.68f)
                lineTo(w * 0.65f, h * 0.88f)
                lineTo(w * 0.85f, h)
                lineTo(w * 0.98f, h)
                lineTo(w * 0.80f, h * 0.88f)
                lineTo(w * 0.50f, h * 0.68f)
                lineTo(w * 0.98f, h * 0.42f)
                lineTo(w * 0.60f, h * 0.18f)
                lineTo(w * 0.90f, 0f)
                close()
            }
            drawPath(path = riftPath, brush = Brush.linearGradient(theme.riverGradient))

            // Crystalline shard ripples
            for (r in 0 until 8) {
                val rippleY = (h * ((r * 0.125f + riverFlow * 0.18f) % 1.0f))
                val rippleX = w * (0.50f + 0.20f * sin((rippleY / h) * 6.28f))
                drawLine(
                    color = theme.waterRippleColor,
                    start = Offset(rippleX - 18f, rippleY),
                    end = Offset(rippleX + 18f, rippleY),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Square
                )
            }
        } else {
            // Smooth, meandering organic river
            val riverPath = Path().apply {
                moveTo(w * 0.70f, 0f)
                cubicTo(w * 0.38f, h * 0.22f, w * 0.90f, h * 0.46f, w * 0.40f, h * 0.70f)
                cubicTo(w * 0.10f, h * 0.82f, w * 0.60f, h * 0.92f, w * 0.76f, h)
                lineTo(w * 0.98f, h)
                cubicTo(w * 0.82f, h * 0.92f, w * 0.30f, h * 0.82f, w * 0.64f, h * 0.70f)
                cubicTo(w * 1.10f, h * 0.46f, w * 0.58f, h * 0.22f, w * 0.92f, 0f)
                close()
            }
            drawPath(path = riverPath, brush = Brush.linearGradient(theme.riverGradient))

            // Water Current Ripples
            for (r in 0 until 8) {
                val rippleY = (h * ((r * 0.125f + riverFlow * 0.15f) % 1.0f))
                val rippleX = w * (0.45f + 0.22f * sin((rippleY / h) * 6.28f))
                drawLine(
                    color = theme.waterRippleColor,
                    start = Offset(rippleX - 22f, rippleY),
                    end = Offset(rippleX + 22f, rippleY + 2f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }

    // ========================================================
    // THEMATIC PARTICLES & FLOATING FX
    // ========================================================
    private fun DrawScope.drawThemedParticles(
        theme: ZoneVisualTheme,
        w: Float,
        h: Float,
        timeSeconds: Float,
        scrollOffset: Float = 0f
    ) {
        with(ZoneMapParticleSystem) {
            drawZoneParticles(
                theme = theme,
                w = w,
                h = h,
                timeSeconds = timeSeconds,
                scrollOffset = scrollOffset
            )
        }
    }

    // ========================================================
    // THEMED PATH DRAWING
    // ========================================================
    fun DrawScope.drawThemedPath(
        theme: ZoneVisualTheme,
        nodes: List<LadderNodePos>,
        currentUnlockedLevel: Int,
        energyPulse: Float,
        riverFlow: Float,
        density: Density
    ) {
        val w = size.width
        val d = density.density

        val zoneName = theme.name.lowercase()
        val isSakura = zoneName.contains("sakura")
        val isFrozen = zoneName.contains("frozen") || zoneName.contains("tundra")
        val isVolcano = zoneName.contains("volcano") || zoneName.contains("caldera") || zoneName.contains("molten")
        val isNeon = zoneName.contains("neon") || zoneName.contains("glade")

        val pathGlowColor = when {
            isSakura -> Color(0xFFF472B6)
            isFrozen -> Color(0xFF38BDF8)
            isVolcano -> Color(0xFFFF7A00)
            isNeon -> Color(0xFF00F0FF)
            else -> theme.primaryAccent
        }

        for (i in 0 until nodes.size - 1) {
            val nodeA = nodes[i]
            val nodeB = nodes[i + 1]

            val startX = (w * nodeA.xRatio).coerceIn(48f * d, w - 48f * d)
            val startY = nodeA.yDp * d
            val endX = (w * nodeB.xRatio).coerceIn(48f * d, w - 48f * d)
            val endY = nodeB.yDp * d

            val isUnlockedPath = nodeA.stageNumber <= currentUnlockedLevel && nodeB.stageNumber <= currentUnlockedLevel
            val dx = endX - startX
            val dy = endY - startY
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 1f) continue

            val nx = -dy / dist
            val ny = dx / dist

            // 1. Soft Ambient Luminous Ribbon tracing the path
            val ribbonAlpha = if (isUnlockedPath) 0.35f else 0.12f
            drawLine(
                brush = Brush.linearGradient(
                    listOf(
                        pathGlowColor.copy(alpha = ribbonAlpha * 0.4f),
                        pathGlowColor.copy(alpha = ribbonAlpha),
                        pathGlowColor.copy(alpha = ribbonAlpha * 0.4f)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 12f * d,
                cap = StrokeCap.Round
            )

            // 2. Luminous Dotted Celestial Trail (Dots spaced every ~14-16dp)
            val dotSpacing = 16f * d
            val dotCount = (dist / dotSpacing).toInt().coerceAtLeast(3)
            for (step in 1 until dotCount) {
                val t = step.toFloat() / dotCount.toFloat()
                val lateralCurvature = sin(t * Math.PI.toFloat()) * (10f * d) * (if (i % 2 == 0) 1f else -1f)
                val dotX = startX + dx * t + nx * lateralCurvature
                val dotY = startY + dy * t + ny * lateralCurvature

                val dotAlpha = if (isUnlockedPath) 0.95f else 0.35f
                val dotColor = if (isUnlockedPath) pathGlowColor else Color.White

                // Soft outer aura
                drawCircle(
                    color = dotColor.copy(alpha = dotAlpha * 0.5f),
                    radius = 4.5f * d,
                    center = Offset(dotX, dotY)
                )
                // Core bright bead
                drawCircle(
                    color = Color.White.copy(alpha = dotAlpha),
                    radius = 2.2f * d,
                    center = Offset(dotX, dotY)
                )
            }

            // 3. Intermediate Stepping Beads between stages (matching reference screenshots)
            val midT = 0.5f
            val midLateral = sin(midT * Math.PI.toFloat()) * (10f * d) * (if (i % 2 == 0) 1f else -1f)
            val midX = startX + dx * midT + nx * midLateral
            val midY = startY + dy * midT + ny * midLateral

            val beadRadius = 9f * d
            // Drop shadow
            drawCircle(
                color = Color(0x66000000),
                radius = beadRadius,
                center = Offset(midX, midY + 2.5f * d)
            )
            // 3D Stepping disc body
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(
                        Color.White.copy(alpha = if (isUnlockedPath) 0.95f else 0.45f),
                        pathGlowColor.copy(alpha = if (isUnlockedPath) 0.85f else 0.35f),
                        pathGlowColor.copy(alpha = if (isUnlockedPath) 0.55f else 0.20f)
                    ),
                    center = Offset(midX - beadRadius * 0.3f, midY - beadRadius * 0.3f),
                    radius = beadRadius
                ),
                radius = beadRadius,
                center = Offset(midX, midY)
            )
            // Crisp rim
            drawCircle(
                color = Color.White.copy(alpha = if (isUnlockedPath) 0.75f else 0.25f),
                radius = beadRadius,
                center = Offset(midX, midY),
                style = Stroke(width = 1.2f * d)
            )
        }
    }

    // --------------------------------------------------------
    // PATH STYLE 1: NEON GLADE (Organic Living Vines & Sprouts)
    // --------------------------------------------------------
    private fun DrawScope.drawOrganicVineRootsPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        dx: Float, dy: Float, dist: Float, nx: Float, ny: Float,
        isUnlocked: Boolean, isNextActive: Boolean, energyPulse: Float, riverFlow: Float
    ) {
        val rootHalfWidth = 16f
        val leftStart = Offset(startX + nx * rootHalfWidth, startY + ny * rootHalfWidth)
        val leftEnd = Offset(endX + nx * rootHalfWidth, endY + ny * rootHalfWidth)
        val rightStart = Offset(startX - nx * rootHalfWidth, startY - ny * rootHalfWidth)
        val rightEnd = Offset(endX - nx * rootHalfWidth, endY - ny * rootHalfWidth)

        // Deep organic root shadow
        drawLine(
            color = Color(0x66000000),
            start = leftStart + Offset(3f, 5f),
            end = leftEnd + Offset(3f, 5f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Main Living Root Bark
        val vineColor = if (isUnlocked) Color(0xFF2E6342) else Color(0xFF14291B)
        val vineGlowColor = if (isUnlocked) Color(0xFF34D399) else Color(0xFF1E3A2B)

        drawLine(
            color = vineColor,
            start = leftStart,
            end = leftEnd,
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = vineColor,
            start = rightStart,
            end = rightEnd,
            strokeWidth = 5.5f,
            cap = StrokeCap.Round
        )

        // Luminous organic sap vein core
        drawLine(
            color = vineGlowColor,
            start = leftStart,
            end = leftEnd,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = vineGlowColor,
            start = rightStart,
            end = rightEnd,
            strokeWidth = 1.8f,
            cap = StrokeCap.Round
        )

        // Stepping Lily-Pads / Tree Mushroom Discs along the root
        val stepsCount = (dist / 28f).toInt().coerceAtLeast(3)
        for (r in 1 until stepsCount) {
            val fraction = r.toFloat() / stepsCount
            val midX = startX + dx * fraction
            val midY = startY + dy * fraction

            // Stepping disc shadow
            drawCircle(
                color = Color(0x77000000),
                radius = 9f,
                center = Offset(midX + 2f, midY + 4f)
            )

            // Organic stepping disc (Bioluminescent tree fungal pad)
            val discColor = if (isUnlocked) Color(0xFF059669) else Color(0xFF0F2618)
            drawCircle(
                color = discColor,
                radius = 8.5f,
                center = Offset(midX, midY)
            )

            // Inner bioluminescent spore ring
            if (isUnlocked) {
                drawCircle(
                    color = Color(0xFF6EE7B7),
                    radius = 4f,
                    center = Offset(midX, midY)
                )
            }

            // Little leaf sprouting off the side at intervals
            if (r % 2 == 1) {
                val leafDir = if (r % 4 == 1) 1f else -1f
                val leafTip = Offset(midX + nx * leafDir * 24f, midY + ny * leafDir * 24f)
                val leafBase = Offset(midX + nx * leafDir * rootHalfWidth, midY + ny * leafDir * rootHalfWidth)
                drawLine(
                    color = if (isUnlocked) Color(0xFF34D399) else Color(0xFF1E3A2B),
                    start = leafBase,
                    end = leafTip,
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = if (isUnlocked) Color(0xFFA7F3D0) else Color(0xFF14291B),
                    radius = 3.5f,
                    center = leafTip
                )
            }
        }

        // Bio-luminescent Sap Flow Wave traveling along the central vine
        if (isUnlocked) {
            val sapWaveColor = Color(0xFF00F0FF)
            drawLine(
                color = sapWaveColor.copy(alpha = 0.85f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), energyPulse * 24f),
                cap = StrokeCap.Round
            )

            // Traveling pollen light sparkle
            val sparkFrac = (energyPulse + (startX / 1000f)) % 1.0f
            val sparkX = startX + dx * sparkFrac
            val sparkY = startY + dy * sparkFrac
            drawCircle(color = Color(0xFFD1FAE5), radius = 4f, center = Offset(sparkX, sparkY))
            drawCircle(color = Color(0x6600F0FF), radius = 9f, center = Offset(sparkX, sparkY))
        } else if (isNextActive) {
            drawLine(
                color = Color(0x6600F0FF),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), energyPulse * 16f),
                cap = StrokeCap.Round
            )
        }
    }

    // --------------------------------------------------------
    // PATH STYLE 2: PRISM PEAKS (Crystalline Fracture & Cut Gems)
    // --------------------------------------------------------
    private fun DrawScope.drawCrystalChasmShardsPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        dx: Float, dy: Float, dist: Float, nx: Float, ny: Float,
        isUnlocked: Boolean, isNextActive: Boolean, energyPulse: Float
    ) {
        val beamColor = if (isUnlocked) Color(0xFFC084FC) else Color(0xFF2E1065)
        val beamGlow = if (isUnlocked) Color(0xFFF5D0FE) else Color(0xFF1E0B3D)

        // Crystalline fracture chasm (angular jagged edge rails)
        val railOffset = 18f
        val leftStart = Offset(startX + nx * railOffset, startY + ny * railOffset)
        val leftEnd = Offset(endX + nx * railOffset, endY + ny * railOffset)
        val rightStart = Offset(startX - nx * railOffset, startY - ny * railOffset)
        val rightEnd = Offset(endX - nx * railOffset, endY - ny * railOffset)

        // Razor-sharp crystalline fracture lines
        drawLine(
            color = beamColor,
            start = leftStart,
            end = leftEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Square
        )
        drawLine(
            color = beamColor,
            start = rightStart,
            end = rightEnd,
            strokeWidth = 4f,
            cap = StrokeCap.Square
        )

        // Floating geometric diamond crystal steps over the chasm
        val stepsCount = (dist / 26f).toInt().coerceAtLeast(3)
        for (r in 1 until stepsCount) {
            val fraction = r.toFloat() / stepsCount
            val midX = startX + dx * fraction
            val midY = startY + dy * fraction

            val diamondSize = 10f
            val diamondPath = Path().apply {
                moveTo(midX, midY - diamondSize)
                lineTo(midX + diamondSize * 0.7f, midY)
                lineTo(midX, midY + diamondSize)
                lineTo(midX - diamondSize * 0.7f, midY)
                close()
            }

            // Shadow
            val shadowPath = Path().apply {
                moveTo(midX + 2f, midY - diamondSize + 4f)
                lineTo(midX + diamondSize * 0.7f + 2f, midY + 4f)
                lineTo(midX + 2f, midY + diamondSize + 4f)
                lineTo(midX - diamondSize * 0.7f + 2f, midY + 4f)
                close()
            }
            drawPath(shadowPath, Color(0x66000000))

            val slabColor = if (isUnlocked) Color(0xFF9333EA) else Color(0xFF240E3E)
            drawPath(diamondPath, slabColor)

            if (isUnlocked) {
                // Specular highlight facet
                drawPath(diamondPath, Color(0xFFF5D0FE), style = Stroke(width = 1.5f))
                drawCircle(Color.White, radius = 2f, center = Offset(midX, midY))
            }
        }

        // Concentrated Prismatic Light Laser connecting the nodes
        if (isUnlocked) {
            drawLine(
                color = Color(0xFFE879F9),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), energyPulse * 24f),
                cap = StrokeCap.Square
            )

            // Traveling Prism Particle (4-point sparkle)
            val sparkFrac = (energyPulse + (startX / 800f)) % 1.0f
            val sparkX = startX + dx * sparkFrac
            val sparkY = startY + dy * sparkFrac
            val sparkPath = Path().apply {
                moveTo(sparkX, sparkY - 7f)
                lineTo(sparkX + 5f, sparkY)
                lineTo(sparkX, sparkY + 7f)
                lineTo(sparkX - 5f, sparkY)
                close()
            }
            drawPath(sparkPath, Color.White)
            drawCircle(Color(0xFFC084FC).copy(alpha = 0.6f), radius = 8f, center = Offset(sparkX, sparkY))
        } else if (isNextActive) {
            drawLine(
                color = Color(0x66C084FC),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), energyPulse * 16f)
            )
        }
    }

    // --------------------------------------------------------
    // PATH STYLE 3: CANYON STRATA & TIMBER FALLBACKS
    // --------------------------------------------------------
    private fun DrawScope.drawCanyonStrataPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        dx: Float, dy: Float, dist: Float, nx: Float, ny: Float,
        isUnlocked: Boolean, isNextActive: Boolean, energyPulse: Float
    ) {
        val sandstoneColor = if (isUnlocked) Color(0xFFB45309) else Color(0xFF3F1912)
        drawLine(
            color = sandstoneColor,
            start = Offset(startX + nx * 18f, startY + ny * 18f),
            end = Offset(endX + nx * 18f, endY + ny * 18f),
            strokeWidth = 5f
        )
        drawLine(
            color = sandstoneColor,
            start = Offset(startX - nx * 18f, startY - ny * 18f),
            end = Offset(endX - nx * 18f, endY - ny * 18f),
            strokeWidth = 5f
        )

        // Sandstone carved slabs
        val count = (dist / 24f).toInt().coerceAtLeast(3)
        for (r in 1 until count) {
            val f = r.toFloat() / count
            val mx = startX + dx * f
            val my = startY + dy * f
            drawLine(
                color = if (isUnlocked) Color(0xFFD97706) else Color(0xFF2A100B),
                start = Offset(mx + nx * 22f, my + ny * 22f),
                end = Offset(mx - nx * 22f, my - ny * 22f),
                strokeWidth = 6f
            )
        }
    }

    private fun DrawScope.drawOceanicKelpPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        dx: Float, dy: Float, dist: Float, nx: Float, ny: Float,
        isUnlocked: Boolean, isNextActive: Boolean, energyPulse: Float, riverFlow: Float
    ) {
        val kelpColor = if (isUnlocked) Color(0xFF0284C7) else Color(0xFF082F49)
        drawLine(
            color = kelpColor,
            start = Offset(startX + nx * 16f, startY + ny * 16f),
            end = Offset(endX + nx * 16f, endY + ny * 16f),
            strokeWidth = 4.5f
        )
        drawLine(
            color = kelpColor,
            start = Offset(startX - nx * 16f, startY - ny * 16f),
            end = Offset(endX - nx * 16f, endY - ny * 16f),
            strokeWidth = 4.5f
        )
    }

    private fun DrawScope.drawGenericTimberPath(
        startX: Float, startY: Float, endX: Float, endY: Float,
        dx: Float, dy: Float, dist: Float, nx: Float, ny: Float,
        isUnlocked: Boolean, isNextActive: Boolean, energyPulse: Float
    ) {
        val ropeColor = if (isUnlocked) Color(0xFFD4A373) else Color(0xFF5D3A2C)
        drawLine(
            color = ropeColor,
            start = Offset(startX + nx * 20f, startY + ny * 20f),
            end = Offset(endX + nx * 20f, endY + ny * 20f),
            strokeWidth = 5f
        )
        drawLine(
            color = ropeColor,
            start = Offset(startX - nx * 20f, startY - ny * 20f),
            end = Offset(endX - nx * 20f, endY - ny * 20f),
            strokeWidth = 5f
        )
    }
}
