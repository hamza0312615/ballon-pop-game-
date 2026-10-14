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

        // ----------------------------------------------------
        // LAYER 1: Deep Sky & Atmospheric Background
        // ----------------------------------------------------
        drawRect(brush = Brush.verticalGradient(theme.bgSkyGradient))

        when (theme.envType) {
            ZoneEnvironmentType.NEON_GLADE -> {
                drawNeonGladeDistantLayer(w, h, scrollOffset, riverFlow)
            }
            ZoneEnvironmentType.PRISM_PEAKS -> {
                drawPrismPeaksDistantLayer(w, h, scrollOffset, riverFlow)
            }
            ZoneEnvironmentType.SUNSET_MESA -> {
                drawSunsetMesaDistantLayer(w, h, scrollOffset, riverFlow)
            }
            ZoneEnvironmentType.AQUA_REEF -> {
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
        when (theme.envType) {
            ZoneEnvironmentType.NEON_GLADE -> {
                drawNeonGladeMidground(w, h, riverFlow)
            }
            ZoneEnvironmentType.PRISM_PEAKS -> {
                drawPrismPeaksMidground(w, h, riverFlow)
            }
            ZoneEnvironmentType.SUNSET_MESA -> {
                drawSunsetMesaMidground(w, h)
            }
            ZoneEnvironmentType.AQUA_REEF -> {
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
        if (theme.envType == ZoneEnvironmentType.PRISM_PEAKS) {
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

        for (i in 0 until nodes.size - 1) {
            val nodeA = nodes[i]
            val nodeB = nodes[i + 1]

            val startX = (w * nodeA.xRatio).coerceIn(48f * d, w - 48f * d)
            val startY = nodeA.yDp * d
            val endX = (w * nodeB.xRatio).coerceIn(48f * d, w - 48f * d)
            val endY = nodeB.yDp * d

            val isUnlockedPath = nodeA.stageNumber <= currentUnlockedLevel && nodeB.stageNumber <= currentUnlockedLevel
            val isNextActivePath = nodeA.stageNumber <= currentUnlockedLevel && nodeB.stageNumber == currentUnlockedLevel

            val dx = endX - startX
            val dy = endY - startY
            val dist = sqrt(dx * dx + dy * dy)
            val nx = -dy / dist
            val ny = dx / dist

            // Soft Ambient Lighting / Depth Glow bleeding into terrain beneath the path
            val ambientGlowColor = if (isUnlockedPath) theme.primaryAccent.copy(alpha = 0.28f) else Color(0x14FFFFFF)
            val outerAmbientGlow = if (isUnlockedPath) theme.activeGlowColor.copy(alpha = 0.16f) else Color(0x08FFFFFF)
            val deepTerrainShadow = Color(0x40000000)

            // Deep carved terrain depression shadow
            drawLine(
                color = deepTerrainShadow,
                start = Offset(startX, startY + 6f),
                end = Offset(endX, endY + 6f),
                strokeWidth = 58f,
                cap = StrokeCap.Round
            )
            // Wide diffused ambient light spill
            drawLine(
                color = outerAmbientGlow,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 52f,
                cap = StrokeCap.Round
            )
            // Core embedded halo glow
            drawLine(
                color = ambientGlowColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 28f,
                cap = StrokeCap.Round
            )

            when (theme.pathType) {
                ZonePathType.ORGANIC_VINE_ROOTS -> {
                    // NEON GLADE: Winding Living Roots with Leaf Tendrils & Bio-veins
                    drawOrganicVineRootsPath(
                        startX, startY, endX, endY, dx, dy, dist, nx, ny,
                        isUnlockedPath, isNextActivePath, energyPulse, riverFlow
                    )
                }

                ZonePathType.CRYSTAL_CHASM_SHARDS -> {
                    // PRISM PEAKS: Fractured Crystalline Chasm with Floating Cut Gem Slabs
                    drawCrystalChasmShardsPath(
                        startX, startY, endX, endY, dx, dy, dist, nx, ny,
                        isUnlockedPath, isNextActivePath, energyPulse
                    )
                }

                ZonePathType.CANYON_STRATA_LEDGE -> {
                    // SUNSET MESA: Stepped Sandstone Ledge
                    drawCanyonStrataPath(
                        startX, startY, endX, endY, dx, dy, dist, nx, ny,
                        isUnlockedPath, isNextActivePath, energyPulse
                    )
                }

                ZonePathType.OCEANIC_TRENCH_KELP -> {
                    // AQUA REEF: Bioluminescent Kelp Ribbon
                    drawOceanicKelpPath(
                        startX, startY, endX, endY, dx, dy, dist, nx, ny,
                        isUnlockedPath, isNextActivePath, energyPulse, riverFlow
                    )
                }

                else -> {
                    // Generic Ladder fallback
                    drawGenericTimberPath(
                        startX, startY, endX, endY, dx, dy, dist, nx, ny,
                        isUnlockedPath, isNextActivePath, energyPulse
                    )
                }
            }
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
