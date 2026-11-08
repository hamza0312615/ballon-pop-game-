package com.example.balloonpop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.balloonpop.model.GameMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Professional commercial game vector artwork suite.
 * Replaces all raw emojis with custom-rendered, high-resolution Canvas artwork.
 */

// =========================================================================
// 1. ENDLESS MODES ARTWORK
// =========================================================================

@Composable
fun EndlessModeArt(
    mode: GameMode,
    modifier: Modifier = Modifier.size(54.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (mode) {
            GameMode.CLASSIC -> {
                // Classic Survival: Precision Shield Emblem with Concentric Target Rings & Golden Star
                val emeraldGlow = Color(0xFF10B981)
                val emeraldDark = Color(0xFF064E3B)

                // Background soft aura
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0x6610B981), Color.Transparent)),
                    radius = w * 0.48f,
                    center = Offset(cx, cy)
                )

                // Outer Target Ring
                drawCircle(
                    brush = Brush.sweepGradient(listOf(Color(0xFF34D399), Color(0xFF059669), Color(0xFF6EE7B7), Color(0xFF34D399))),
                    radius = w * 0.40f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.5f)
                )

                // Inner Target Ring
                drawCircle(
                    color = Color(0xFF6EE7B7),
                    radius = w * 0.26f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )

                // Crosshairs
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(cx - w * 0.44f, cy),
                    end = Offset(cx + w * 0.44f, cy),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(cx, cy - h * 0.44f),
                    end = Offset(cx, cy + h * 0.44f),
                    strokeWidth = 2f
                )

                // Central Golden Star Bullseye
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFF59E0B))),
                    radius = w * 0.14f,
                    center = Offset(cx, cy)
                )
                drawCircle(color = Color.White, radius = w * 0.05f, center = Offset(cx - 2f, cy - 2f))
            }

            GameMode.TIME_RUSH -> {
                // Time Rush: Cyber Chronometer Stopwatch with Golden Hands & Lightning Ticks
                val amberGold = Color(0xFFF59E0B)
                val amberLight = Color(0xFFFDE68A)

                // Glow
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0x66F59E0B), Color.Transparent)),
                    radius = w * 0.48f,
                    center = Offset(cx, cy + 2f)
                )

                // Top stopwatch button
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(Color(0xFFFEF3C7), Color(0xFFD97706))),
                    topLeft = Offset(cx - w * 0.12f, cy - h * 0.46f),
                    size = Size(w * 0.24f, h * 0.12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )

                // Outer Clock Dial
                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFFFFBEB), amberGold, Color(0xFFB45309))),
                    radius = w * 0.36f,
                    center = Offset(cx, cy + h * 0.04f),
                    style = Stroke(width = 4f)
                )

                // Clock Face
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFF261A05), Color(0xFF0F0B02))),
                    radius = w * 0.33f,
                    center = Offset(cx, cy + h * 0.04f)
                )

                // Hour tick marks
                val clockCy = cy + h * 0.04f
                for (i in 0 until 12) {
                    val angle = (i * 30f) * (PI.toFloat() / 180f)
                    val r1 = w * 0.26f
                    val r2 = w * 0.31f
                    drawLine(
                        color = if (i % 3 == 0) amberLight else Color(0x88F59E0B),
                        start = Offset(cx + cos(angle) * r1, clockCy + sin(angle) * r1),
                        end = Offset(cx + cos(angle) * r2, clockCy + sin(angle) * r2),
                        strokeWidth = if (i % 3 == 0) 2.5f else 1.5f
                    )
                }

                // Clock hands (pointing to 2 o'clock and 12 o'clock)
                drawLine(
                    color = Color.White,
                    start = Offset(cx, clockCy),
                    end = Offset(cx, clockCy - w * 0.22f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(cx, clockCy),
                    end = Offset(cx + w * 0.16f, clockCy - w * 0.10f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx, clockCy))
            }

            GameMode.ARCADE -> {
                // Arcade Frenzy: Dual Rising Fire Comets with Spark Eruption
                val fireRed = Color(0xFFEF4444)
                val fireOrange = Color(0xFFF97316)
                val fireYellow = Color(0xFFFACC15)

                // Glow
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0x66EF4444), Color.Transparent)),
                    radius = w * 0.48f,
                    center = Offset(cx, cy)
                )

                // Large outer flame path
                val flamePath = Path().apply {
                    moveTo(cx, cy - h * 0.42f)
                    cubicTo(cx + w * 0.35f, cy - h * 0.15f, cx + w * 0.40f, cy + h * 0.25f, cx, cy + h * 0.42f)
                    cubicTo(cx - w * 0.40f, cy + h * 0.25f, cx - w * 0.35f, cy - h * 0.15f, cx, cy - h * 0.42f)
                    close()
                }
                drawPath(
                    path = flamePath,
                    brush = Brush.verticalGradient(listOf(fireYellow, fireOrange, fireRed))
                )

                // Inner intense core flame
                val innerFlame = Path().apply {
                    moveTo(cx, cy - h * 0.24f)
                    cubicTo(cx + w * 0.20f, cy - h * 0.05f, cx + w * 0.22f, cy + h * 0.20f, cx, cy + h * 0.34f)
                    cubicTo(cx - w * 0.22f, cy + h * 0.20f, cx - w * 0.20f, cy - h * 0.05f, cx, cy - h * 0.24f)
                    close()
                }
                drawPath(
                    path = innerFlame,
                    brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF9C4), fireYellow))
                )

                // Rising embers
                drawCircle(Color.White, radius = 3f, center = Offset(cx - w * 0.22f, cy - h * 0.26f))
                drawCircle(Color(0xFFFFEA00), radius = 2.5f, center = Offset(cx + w * 0.24f, cy - h * 0.32f))
                drawCircle(Color(0xFFFF5722), radius = 2.2f, center = Offset(cx + w * 0.05f, cy - h * 0.44f))
            }

            GameMode.ZEN -> {
                // Zen Popping: Sacred Glowing Lotus Flower with Concentric Calm Water Ripples
                val lotusTeal = Color(0xFF2DD4BF)
                val lotusCyan = Color(0xFF38BDF8)

                // Water ripples underneath
                drawOval(
                    color = Color(0x3338BDF8),
                    topLeft = Offset(cx - w * 0.42f, cy + h * 0.16f),
                    size = Size(w * 0.84f, h * 0.24f),
                    style = Stroke(width = 2f)
                )
                drawOval(
                    color = Color(0x552DD4BF),
                    topLeft = Offset(cx - w * 0.30f, cy + h * 0.20f),
                    size = Size(w * 0.60f, h * 0.16f),
                    style = Stroke(width = 1.5f)
                )

                // Center Lotus Petal
                val centerPetal = Path().apply {
                    moveTo(cx, cy - h * 0.38f)
                    cubicTo(cx + w * 0.16f, cy - h * 0.10f, cx + w * 0.14f, cy + h * 0.18f, cx, cy + h * 0.24f)
                    cubicTo(cx - w * 0.14f, cy + h * 0.18f, cx - w * 0.16f, cy - h * 0.10f, cx, cy - h * 0.38f)
                    close()
                }
                drawPath(
                    path = centerPetal,
                    brush = Brush.verticalGradient(listOf(Color(0xFFE0F2FE), lotusCyan, Color(0xFF0369A1)))
                )

                // Left Wing Petal
                val leftPetal = Path().apply {
                    moveTo(cx, cy + h * 0.24f)
                    cubicTo(cx - w * 0.35f, cy + h * 0.12f, cx - w * 0.40f, cy - h * 0.12f, cx - w * 0.28f, cy - h * 0.22f)
                    cubicTo(cx - w * 0.18f, cy - h * 0.05f, cx - w * 0.05f, cy + h * 0.12f, cx, cy + h * 0.24f)
                    close()
                }
                drawPath(
                    path = leftPetal,
                    brush = Brush.linearGradient(listOf(Color(0xFFCCFBF1), lotusTeal, Color(0xFF0F766E)))
                )

                // Right Wing Petal
                val rightPetal = Path().apply {
                    moveTo(cx, cy + h * 0.24f)
                    cubicTo(cx + w * 0.35f, cy + h * 0.12f, cx + w * 0.40f, cy - h * 0.12f, cx + w * 0.28f, cy - h * 0.22f)
                    cubicTo(cx + w * 0.18f, cy - h * 0.05f, cx + w * 0.05f, cy + h * 0.12f, cx, cy + h * 0.24f)
                    close()
                }
                drawPath(
                    path = rightPetal,
                    brush = Brush.linearGradient(listOf(Color(0xFFCCFBF1), lotusTeal, Color(0xFF0F766E)))
                )

                // Floating sacred water droplet
                drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx, cy - h * 0.15f))
            }

            else -> {
                val goldAccent = Color(0xFFFFD700)
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0x66FFD700), Color.Transparent)),
                    radius = w * 0.45f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = goldAccent,
                    radius = w * 0.35f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3f)
                )
            }
        }
    }
}

// =========================================================================
// 2. SKILL VAULT ARTWORK
// =========================================================================

@Composable
fun SkillVaultArt(
    skillId: String,
    modifier: Modifier = Modifier.size(46.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (skillId) {
            "skill_hammer_slide" -> {
                // Golden Thunder Hammer with Electric Impact Sparks
                // Handle
                drawLine(
                    brush = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFF78350F))),
                    start = Offset(cx - w * 0.26f, cy + h * 0.32f),
                    end = Offset(cx + w * 0.12f, cy - h * 0.08f),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                // Hammer Head
                val headPath = Path().apply {
                    moveTo(cx - w * 0.06f, cy - h * 0.28f)
                    lineTo(cx + w * 0.34f, cy - h * 0.18f)
                    lineTo(cx + w * 0.26f, cy + h * 0.08f)
                    lineTo(cx - w * 0.14f, cy - h * 0.02f)
                    close()
                }
                drawPath(
                    path = headPath,
                    brush = Brush.linearGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD54F), Color(0xFFB45309)))
                )
                drawPath(headPath, color = Color.White, style = Stroke(width = 1.8f))
                // Lightning spark
                drawLine(Color(0xFF00E5FF), start = Offset(cx + w * 0.30f, cy - h * 0.22f), end = Offset(cx + w * 0.44f, cy - h * 0.32f), strokeWidth = 2.5f)
                drawLine(Color(0xFF00E5FF), start = Offset(cx + w * 0.32f, cy + h * 0.04f), end = Offset(cx + w * 0.46f, cy + h * 0.12f), strokeWidth = 2.5f)
            }

            "skill_slice" -> {
                // Dual Crossing Laser Plasma Blades
                // Blade 1 (Cyan)
                drawLine(
                    brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF80D8FF), Color.White)),
                    start = Offset(cx - w * 0.35f, cy + h * 0.35f),
                    end = Offset(cx + w * 0.35f, cy - h * 0.35f),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
                // Blade 2 (Ruby Rose)
                drawLine(
                    brush = Brush.linearGradient(listOf(Color(0xFFFF2A6D), Color(0xFFFF80AB), Color.White)),
                    start = Offset(cx + w * 0.35f, cy + h * 0.35f),
                    end = Offset(cx - w * 0.35f, cy - h * 0.35f),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
                // Center Clash Sparkle
                drawCircle(color = Color.White, radius = 5f, center = Offset(cx, cy))
                drawCircle(color = Color(0xFFFFEA00), radius = 8f, center = Offset(cx, cy), style = Stroke(width = 2f))
            }

            "skill_freeze" -> {
                // Frosted Ice Crystal Hexagram with Frost Crystals
                val iceCyan = Color(0xFF00E5FF)
                for (i in 0 until 6) {
                    val angle = (i * 60f) * (PI.toFloat() / 180f)
                    val r1 = w * 0.38f
                    drawLine(
                        brush = Brush.linearGradient(listOf(Color.White, iceCyan)),
                        start = Offset(cx, cy),
                        end = Offset(cx + cos(angle) * r1, cy + sin(angle) * r1),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                    // Crystal branch
                    val bx = cx + cos(angle) * (r1 * 0.65f)
                    val by = cy + sin(angle) * (r1 * 0.65f)
                    val branchAngle1 = angle + 45f * (PI.toFloat() / 180f)
                    val branchAngle2 = angle - 45f * (PI.toFloat() / 180f)
                    drawLine(Color.White, start = Offset(bx, by), end = Offset(bx + cos(branchAngle1) * 8f, by + sin(branchAngle1) * 8f), strokeWidth = 2f)
                    drawLine(Color.White, start = Offset(bx, by), end = Offset(bx + cos(branchAngle2) * 8f, by + sin(branchAngle2) * 8f), strokeWidth = 2f)
                }
                drawCircle(Color.White, radius = 4f, center = Offset(cx, cy))
            }

            "skill_vortex" -> {
                // Gravitational Vortex Galaxy Spiral
                val vortexPurple = Color(0xFFA855F7)
                val vortexCyan = Color(0xFF06D6A0)
                drawCircle(
                    brush = Brush.sweepGradient(listOf(vortexPurple, vortexCyan, Color(0xFFEC4899), vortexPurple)),
                    radius = w * 0.36f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4.5f)
                )
                drawCircle(
                    brush = Brush.sweepGradient(listOf(vortexCyan, vortexPurple, vortexCyan)),
                    radius = w * 0.22f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 3.5f)
                )
                drawCircle(color = Color.White, radius = 4f, center = Offset(cx, cy))
            }

            "skill_supernova" -> {
                // Supernova Golden Solar Flare Starburst
                val sunGold = Color(0xFFFFD700)
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (PI.toFloat() / 180f)
                    val len = if (i % 2 == 0) w * 0.44f else w * 0.28f
                    drawLine(
                        brush = Brush.linearGradient(listOf(Color.White, sunGold, Color.Transparent)),
                        start = Offset(cx, cy),
                        end = Offset(cx + cos(angle) * len, cy + sin(angle) * len),
                        strokeWidth = if (i % 2 == 0) 3.5f else 2f,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color.White, Color(0xFFFFEA00), Color(0xFFFF9100))),
                    radius = w * 0.18f,
                    center = Offset(cx, cy)
                )
            }

            else -> {
                // Lightning Flash
                val boltPath = Path().apply {
                    moveTo(cx + 4f, cy - h * 0.38f)
                    lineTo(cx - 8f, cy - 2f)
                    lineTo(cx + 2f, cy)
                    lineTo(cx - 6f, cy + h * 0.38f)
                    lineTo(cx + 10f, cy + 2f)
                    lineTo(cx, cy)
                    close()
                }
                drawPath(boltPath, brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFACC15), Color(0xFFF59E0B))))
            }
        }
    }
}

// =========================================================================
// 3. SHOP ITEM ARTWORK
// =========================================================================

@Composable
fun ShopItemArt(
    itemId: String,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val (type, title, color) = when {
        itemId.contains("freeze") -> Triple("booster", "Freeze", Color(0xFF00E5FF))
        itemId.contains("color_bomb") || itemId.contains("rainbow") -> Triple("booster", "Rainbow", Color(0xFFFF2A75))
        itemId.contains("thunder") -> Triple("booster", "Thunder", Color(0xFFFFB703))
        itemId.contains("vortex") -> Triple("booster", "Vortex", Color(0xFF10B981))
        itemId.contains("hammer") -> Triple("hammer", "Hammer", Color(0xFFFFD700))
        else -> Triple("booster", "Booster", Color(0xFF38BDF8))
    }
    ShopItemArt(type = type, title = title, primaryColor = color, modifier = modifier)
}

@Composable
fun ShopItemArt(
    type: String,
    title: String,
    primaryColor: Color,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when {
            type.contains("hammer") -> {
                // Heavy 3D War Hammer / Mallet with energy rim
                val handleTop = Offset(cx + w * 0.15f, cy - h * 0.15f)
                val handleBottom = Offset(cx - w * 0.30f, cy + h * 0.35f)
                drawLine(
                    brush = Brush.linearGradient(listOf(Color(0xFF78350F), Color(0xFF451A03))),
                    start = handleTop,
                    end = handleBottom,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                // Heavy metallic block head
                val headW = w * 0.44f
                val headH = h * 0.30f
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color.White, primaryColor, Color(0xFF0F172A))),
                    topLeft = Offset(cx - headW * 0.35f, cy - headH * 0.85f),
                    size = Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.8f),
                    topLeft = Offset(cx - headW * 0.35f, cy - headH * 0.85f),
                    size = Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                    style = Stroke(width = 2f)
                )
                // Energy sparks
                drawCircle(color = primaryColor, radius = 4f, center = Offset(cx + headW * 0.38f, cy - headH * 0.70f))
            }

            type.contains("frame") -> {
                // Luxury Glowing Ornate Playfield Frame with Corner Studs
                val frameW = w * 0.65f
                val frameH = h * 0.70f
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color.White, primaryColor, Color(0xFF0284C7))),
                    topLeft = Offset(cx - frameW / 2f, cy - frameH / 2f),
                    size = Size(frameW, frameH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                    style = Stroke(width = 5f)
                )
                // Inner bezel
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.6f),
                    topLeft = Offset(cx - frameW / 2f + 5f, cy - frameH / 2f + 5f),
                    size = Size(frameW - 10f, frameH - 10f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                    style = Stroke(width = 1.5f)
                )
                // Corner Diamond Studs
                val halfW = frameW / 2f
                val halfH = frameH / 2f
                val corners = listOf(
                    Offset(cx - halfW, cy - halfH),
                    Offset(cx + halfW, cy - halfH),
                    Offset(cx - halfW, cy + halfH),
                    Offset(cx + halfW, cy + halfH)
                )
                for (pt in corners) {
                    drawCircle(color = Color(0xFFFFD700), radius = 5f, center = pt)
                    drawCircle(color = Color.White, radius = 2.5f, center = pt)
                }
            }

            type.contains("blade") -> {
                // Sleek Plasma Blade / Sword with Energy Glow
                val bladeTip = Offset(cx + w * 0.35f, cy - h * 0.35f)
                val bladeHilt = Offset(cx - w * 0.22f, cy + h * 0.22f)
                val bladeEnd = Offset(cx - w * 0.32f, cy + h * 0.32f)

                // Hilt
                drawLine(
                    color = Color(0xFF334155),
                    start = bladeHilt,
                    end = bladeEnd,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                // Crossguard
                drawLine(
                    color = Color(0xFFFFD700),
                    start = Offset(bladeHilt.x - 8f, bladeHilt.y - 8f),
                    end = Offset(bladeHilt.x + 8f, bladeHilt.y + 8f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                // Glowing Plasma Blade
                drawLine(
                    color = primaryColor.copy(alpha = 0.45f),
                    start = bladeHilt,
                    end = bladeTip,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    brush = Brush.linearGradient(listOf(primaryColor, Color.White)),
                    start = bladeHilt,
                    end = bladeTip,
                    strokeWidth = 4.5f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color.White, radius = 3.5f, center = bladeTip)
            }

            type.contains("flame") -> {
                // Multi-Layer Searing Flame with Spark Particle Eruption
                val flamePath = Path().apply {
                    moveTo(cx, cy - h * 0.38f)
                    cubicTo(cx + w * 0.30f, cy - h * 0.10f, cx + w * 0.32f, cy + h * 0.25f, cx, cy + h * 0.38f)
                    cubicTo(cx - w * 0.32f, cy + h * 0.25f, cx - w * 0.30f, cy - h * 0.10f, cx, cy - h * 0.38f)
                    close()
                }
                drawPath(flamePath, brush = Brush.verticalGradient(listOf(Color(0xFFFFF7ED), primaryColor, Color(0xFF7F1D1D))))
                val innerFlame = Path().apply {
                    moveTo(cx, cy - h * 0.20f)
                    cubicTo(cx + w * 0.16f, cy - h * 0.05f, cx + w * 0.18f, cy + h * 0.18f, cx, cy + h * 0.30f)
                    cubicTo(cx - w * 0.18f, cy + h * 0.18f, cx - w * 0.16f, cy - h * 0.05f, cx, cy - h * 0.20f)
                    close()
                }
                drawPath(innerFlame, brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFD54F), primaryColor)))
                drawCircle(Color.White, radius = 2.5f, center = Offset(cx - w * 0.16f, cy - h * 0.24f))
                drawCircle(Color(0xFFFFEA00), radius = 2.5f, center = Offset(cx + w * 0.18f, cy - h * 0.28f))
            }

            type.contains("diamond") || type.contains("pack") -> {
                // Faceted 3D Cut Brilliant Cyan Gem with Refraction Lines
                val gemPath = Path().apply {
                    moveTo(cx, cy - h * 0.32f)
                    lineTo(cx + w * 0.32f, cy - h * 0.10f)
                    lineTo(cx, cy + h * 0.35f)
                    lineTo(cx - w * 0.32f, cy - h * 0.10f)
                    close()
                }
                drawPath(gemPath, brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0369A1))))
                drawPath(gemPath, color = Color.White, style = Stroke(width = 2f))
                // Facet lines
                drawLine(Color.White.copy(alpha = 0.8f), start = Offset(cx - w * 0.32f, cy - h * 0.10f), end = Offset(cx + w * 0.32f, cy - h * 0.10f), strokeWidth = 1.5f)
                drawLine(Color.White.copy(alpha = 0.8f), start = Offset(cx, cy - h * 0.32f), end = Offset(cx, cy + h * 0.35f), strokeWidth = 1.5f)
                drawCircle(color = Color.White, radius = 3.5f, center = Offset(cx - 3f, cy - h * 0.12f))
            }

            else -> {
                // Theme Scenic Atmospheric Sphere
                drawCircle(
                    brush = Brush.sweepGradient(listOf(primaryColor, Color(0xFF38BDF8), Color(0xFFFF5D8F), primaryColor)),
                    radius = w * 0.34f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = w * 0.34f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.5f)
                )
                // Specular sheen
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 200f,
                    sweepAngle = 70f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.28f, cy - h * 0.28f),
                    size = Size(w * 0.56f, h * 0.56f),
                    style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

// =========================================================================
// 4. RESOURCE ICONS (HEART, COIN, GEM)
// =========================================================================
// 4. CURRENCY BADGES ARTWORK
// =========================================================================

@Composable
fun CurrencyArt(
    type: String = "coin",
    currencyType: String = type,
    modifier: Modifier = Modifier.size(18.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val resolvedType = currencyType.ifEmpty { type }.lowercase()
        when (resolvedType) {
            "heart", "hearts", "life", "lives" -> {
                val heartPath = Path().apply {
                    moveTo(cx, cy + h * 0.38f)
                    cubicTo(cx - w * 0.48f, cy + h * 0.05f, cx - w * 0.45f, cy - h * 0.38f, cx - w * 0.20f, cy - h * 0.38f)
                    cubicTo(cx - w * 0.06f, cy - h * 0.38f, cx, cy - h * 0.20f, cx, cy - h * 0.14f)
                    cubicTo(cx, cy - h * 0.20f, cx + w * 0.06f, cy - h * 0.38f, cx + w * 0.20f, cy - h * 0.38f)
                    cubicTo(cx + w * 0.45f, cy - h * 0.38f, cx + w * 0.48f, cy + h * 0.05f, cx, cy + h * 0.38f)
                    close()
                }
                drawPath(heartPath, brush = Brush.verticalGradient(listOf(Color(0xFFFF80AB), Color(0xFFFF1744), Color(0xFFB71C1C))))
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.5f, center = Offset(cx - w * 0.16f, cy - h * 0.20f))
            }

            "coin", "coins", "gold" -> {
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFD700), Color(0xFFF57F17))),
                    radius = w * 0.46f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color(0xFFFFF9C4),
                    radius = w * 0.36f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5f)
                )
                drawCircle(color = Color(0xFFFF8F00), radius = w * 0.15f, center = Offset(cx, cy))
            }

            else -> {
                // Gem / Diamond
                val gemPath = Path().apply {
                    moveTo(cx, cy - h * 0.44f)
                    lineTo(cx + w * 0.42f, cy - h * 0.12f)
                    lineTo(cx, cy + h * 0.44f)
                    lineTo(cx - w * 0.42f, cy - h * 0.12f)
                    close()
                }
                drawPath(gemPath, brush = Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF7C3AED))))
                drawPath(gemPath, color = Color.White, style = Stroke(width = 1.2f))
                drawCircle(color = Color.White, radius = 2f, center = Offset(cx - 2f, cy - 2f))
            }
        }
    }
}

// =========================================================================
// 5. ZONE EMBLEMS (ALL 20 REALMS)
// =========================================================================

@Composable
fun ZoneEmblemArt(
    zoneIndex: Int,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier.size(36.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        if (!isUnlocked) {
            // Locked Frost / Dark Metallic Shield
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF0F172A))),
                radius = w * 0.45f,
                center = Offset(cx, cy)
            )
            drawCircle(color = Color(0xFF475569), radius = w * 0.42f, center = Offset(cx, cy), style = Stroke(width = 2f))
            // Minimal padlock shackle
            drawArc(
                color = Color(0xFF94A3B8),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cx - w * 0.15f, cy - h * 0.28f),
                size = Size(w * 0.30f, h * 0.30f),
                style = Stroke(width = 2.5f)
            )
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(cx - w * 0.18f, cy - h * 0.05f),
                size = Size(w * 0.36f, h * 0.30f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            return@Canvas
        }

        when (zoneIndex % 20) {
            0 -> {
                // Zone 1: Neon Glade - Living bio-seedling & leaf bud
                val leaf = Path().apply {
                    moveTo(cx, cy - h * 0.40f)
                    cubicTo(cx + w * 0.35f, cy - h * 0.15f, cx + w * 0.30f, cy + h * 0.25f, cx, cy + h * 0.38f)
                    cubicTo(cx - w * 0.30f, cy + h * 0.25f, cx - w * 0.35f, cy - h * 0.15f, cx, cy - h * 0.40f)
                    close()
                }
                drawPath(leaf, brush = Brush.verticalGradient(listOf(Color(0xFF34D399), Color(0xFF059669))))
                drawLine(Color(0xFFD1FAE5), Offset(cx, cy - h * 0.32f), Offset(cx, cy + h * 0.30f), strokeWidth = 2f)
            }
            1 -> {
                // Zone 2: Prism Peaks - Faceted violet crystal
                val crystal = Path().apply {
                    moveTo(cx, cy - h * 0.42f)
                    lineTo(cx + w * 0.35f, cy - h * 0.10f)
                    lineTo(cx + w * 0.22f, cy + h * 0.38f)
                    lineTo(cx - w * 0.22f, cy + h * 0.38f)
                    lineTo(cx - w * 0.35f, cy - h * 0.10f)
                    close()
                }
                drawPath(crystal, brush = Brush.verticalGradient(listOf(Color(0xFFE879F9), Color(0xFF9333EA))))
                drawLine(Color.White, Offset(cx, cy - h * 0.42f), Offset(cx, cy + h * 0.38f), strokeWidth = 1.8f)
            }
            2 -> {
                // Zone 3: Sunset Mesa - Desert canyon sunburst
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFEF08A), Color(0xFFF97316))), radius = w * 0.35f, center = Offset(cx, cy))
                drawRect(Color(0xFF9A3412), topLeft = Offset(cx - w * 0.35f, cy + h * 0.05f), size = Size(w * 0.70f, h * 0.30f))
            }
            3 -> {
                // Zone 4: Aqua Reef - Ocean pearl & wave crest
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7))), radius = w * 0.36f, center = Offset(cx, cy))
                drawCircle(Color.White, radius = w * 0.14f, center = Offset(cx - w * 0.08f, cy - h * 0.08f))
            }
            4 -> {
                // Zone 5: Candy Wonderland - Peppermint swirl
                drawCircle(brush = Brush.sweepGradient(listOf(Color(0xFFFF5D8F), Color.White, Color(0xFFFF5D8F), Color.White)), radius = w * 0.38f, center = Offset(cx, cy))
                drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
            }
            5 -> {
                // Zone 6: Cyber Metropolis - Circuit matrix node
                drawRoundRect(brush = Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF06D6A0))), topLeft = Offset(cx - w * 0.32f, cy - h * 0.32f), size = Size(w * 0.64f, h * 0.64f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
                drawCircle(Color(0xFF0B1622), radius = w * 0.18f, center = Offset(cx, cy))
                drawCircle(Color(0xFF00F0FF), radius = w * 0.08f, center = Offset(cx, cy))
            }
            6 -> {
                // Zone 7: Volcano Caldera - Molten fire droplet
                val flame = Path().apply {
                    moveTo(cx, cy - h * 0.42f)
                    cubicTo(cx + w * 0.35f, cy, cx + w * 0.28f, cy + h * 0.38f, cx, cy + h * 0.38f)
                    cubicTo(cx - w * 0.28f, cy + h * 0.38f, cx - w * 0.35f, cy, cx, cy - h * 0.42f)
                    close()
                }
                drawPath(flame, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFF7A00), Color(0xFFDC2626))))
            }
            7 -> {
                // Zone 8: Starry Cosmos - Ringed planet
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFE9D5FF), Color(0xFF7C3AED))), radius = w * 0.26f, center = Offset(cx, cy))
                drawOval(color = Color(0xFF38BDF8), topLeft = Offset(cx - w * 0.44f, cy - h * 0.12f), size = Size(w * 0.88f, h * 0.24f), style = Stroke(width = 2.5f))
            }
            8 -> {
                // Zone 9: Golden Citadel - Fortress crown
                val crown = Path().apply {
                    moveTo(cx - w * 0.32f, cy + h * 0.25f)
                    lineTo(cx + w * 0.32f, cy + h * 0.25f)
                    lineTo(cx + w * 0.35f, cy - h * 0.20f)
                    lineTo(cx + w * 0.16f, cy - h * 0.05f)
                    lineTo(cx, cy - h * 0.32f)
                    lineTo(cx - w * 0.16f, cy - h * 0.05f)
                    lineTo(cx - w * 0.35f, cy - h * 0.20f)
                    close()
                }
                drawPath(crown, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFB45309))))
            }
            9 -> {
                // Zone 10: Frozen Tundra - Crystal snowflake
                for (a in 0 until 6) {
                    val angle = a * (PI.toFloat() / 3f)
                    val r = w * 0.36f
                    drawLine(Color(0xFFBAE6FD), Offset(cx, cy), Offset(cx + cos(angle) * r, cy + sin(angle) * r), strokeWidth = 2.5f, cap = StrokeCap.Round)
                }
                drawCircle(Color.White, radius = 3f, center = Offset(cx, cy))
            }
            10 -> {
                // Zone 11: Sakura Sanctuary - 5-petal cherry blossom
                for (a in 0 until 5) {
                    val angle = a * (PI.toFloat() * 2f / 5f) - PI.toFloat() / 2f
                    val px = cx + cos(angle) * (w * 0.22f)
                    val py = cy + sin(angle) * (h * 0.22f)
                    drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFFF0F5), Color(0xFFF472B6))), radius = w * 0.16f, center = Offset(px, py))
                }
                drawCircle(Color(0xFFFFD700), radius = 3.5f, center = Offset(cx, cy))
            }
            else -> {
                // Generic High-Tier Realm: Radiant Diamond Crest
                val crest = Path().apply {
                    moveTo(cx, cy - h * 0.40f)
                    lineTo(cx + w * 0.35f, cy)
                    lineTo(cx, cy + h * 0.40f)
                    lineTo(cx - w * 0.35f, cy)
                    close()
                }
                drawPath(crest, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFEA580C))))
                drawCircle(Color.White, radius = 3f, center = Offset(cx, cy))
            }
        }
    }
}

// =========================================================================
// 6. VICTORY & DEFEAT HIGH-END ARTWORK
// =========================================================================

@Composable
fun VictoryTrophyArt(
    modifier: Modifier = Modifier.size(54.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Radiant starburst glow behind trophy
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x66FFD700), Color(0x22F59E0B), Color.Transparent)),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )

        // Trophy Chalice Cup
        val cupPath = Path().apply {
            moveTo(cx - w * 0.28f, cy - h * 0.32f)
            lineTo(cx + w * 0.28f, cy - h * 0.32f)
            cubicTo(cx + w * 0.26f, cy + h * 0.05f, cx + w * 0.14f, cy + h * 0.16f, cx, cy + h * 0.18f)
            cubicTo(cx - w * 0.14f, cy + h * 0.16f, cx - w * 0.26f, cy + h * 0.05f, cx - w * 0.28f, cy - h * 0.32f)
            close()
        }
        drawPath(cupPath, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFD97706), Color(0xFF92400E))))

        // Winged Handles
        val leftHandle = Path().apply {
            moveTo(cx - w * 0.25f, cy - h * 0.25f)
            cubicTo(cx - w * 0.45f, cy - h * 0.25f, cx - w * 0.42f, cy + h * 0.05f, cx - w * 0.16f, cy + h * 0.08f)
        }
        drawPath(leftHandle, color = Color(0xFFFFD54F), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        val rightHandle = Path().apply {
            moveTo(cx + w * 0.25f, cy - h * 0.25f)
            cubicTo(cx + w * 0.45f, cy - h * 0.25f, cx + w * 0.42f, cy + h * 0.05f, cx + w * 0.16f, cy + h * 0.08f)
        }
        drawPath(rightHandle, color = Color(0xFFFFD54F), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        // Stem & Base
        drawLine(Color(0xFFF59E0B), Offset(cx, cy + h * 0.18f), Offset(cx, cy + h * 0.30f), strokeWidth = 5f, cap = StrokeCap.Round)
        drawRoundRect(brush = Brush.horizontalGradient(listOf(Color(0xFFB45309), Color(0xFFFFD700), Color(0xFFB45309))), topLeft = Offset(cx - w * 0.26f, cy + h * 0.30f), size = Size(w * 0.52f, h * 0.12f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))

        // Specular glint
        drawCircle(Color.White, radius = 2.5f, center = Offset(cx - w * 0.12f, cy - h * 0.18f))
    }
}

@Composable
fun DefeatHazardArt(
    reason: com.example.balloonpop.model.GameOverReason,
    modifier: Modifier = Modifier.size(54.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Ambient dark red hazard aura
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0x55DC2626), Color.Transparent)),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )

        when (reason) {
            com.example.balloonpop.model.GameOverReason.BOMB_DETONATION -> {
                // Hazard Spiky Bomb with Fuse & Sparks
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF374151), Color(0xFF111827))), radius = w * 0.32f, center = Offset(cx, cy + h * 0.06f))
                drawCircle(Color(0xFFEF4444), radius = w * 0.28f, center = Offset(cx, cy + h * 0.06f), style = Stroke(width = 2f))
                // Burning fuse spark
                drawLine(Color(0xFFF59E0B), Offset(cx, cy - h * 0.26f), Offset(cx + w * 0.14f, cy - h * 0.40f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(cx + w * 0.14f, cy - h * 0.40f))
            }
            com.example.balloonpop.model.GameOverReason.TIME_EXPIRED -> {
                // Shattered Chronometer Hourglass
                val glass = Path().apply {
                    moveTo(cx - w * 0.26f, cy - h * 0.32f)
                    lineTo(cx + w * 0.26f, cy - h * 0.32f)
                    lineTo(cx, cy)
                    lineTo(cx + w * 0.26f, cy + h * 0.32f)
                    lineTo(cx - w * 0.26f, cy + h * 0.32f)
                    lineTo(cx, cy)
                    close()
                }
                drawPath(glass, brush = Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0369A1))))
                drawPath(glass, Color.White, style = Stroke(width = 1.8f))
            }
            com.example.balloonpop.model.GameOverReason.LIVES_DEPLETED -> {
                // Shattered Heart with Crack line
                val heartPath = Path().apply {
                    moveTo(cx, cy + h * 0.36f)
                    cubicTo(cx - w * 0.45f, cy + h * 0.05f, cx - w * 0.42f, cy - h * 0.35f, cx - w * 0.18f, cy - h * 0.35f)
                    cubicTo(cx - w * 0.05f, cy - h * 0.35f, cx, cy - h * 0.18f, cx, cy - h * 0.12f)
                    cubicTo(cx, cy - h * 0.18f, cx + w * 0.05f, cy - h * 0.35f, cx + w * 0.18f, cy - h * 0.35f)
                    cubicTo(cx + w * 0.42f, cy - h * 0.35f, cx + w * 0.45f, cy + h * 0.05f, cx, cy + h * 0.36f)
                    close()
                }
                drawPath(heartPath, brush = Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B))))
                // Diagonal zig-zag fracture crack
                val crack = Path().apply {
                    moveTo(cx, cy - h * 0.25f)
                    lineTo(cx - w * 0.08f, cy - h * 0.08f)
                    lineTo(cx + w * 0.06f, cy + h * 0.10f)
                    lineTo(cx, cy + h * 0.32f)
                }
                drawPath(crack, color = Color.White, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
            }
            com.example.balloonpop.model.GameOverReason.MISSED_LIMIT -> {
                // Floating Escaped Balloon Silhouette with Dissipating Wind Trails
                val balloonCenter = Offset(cx, cy - h * 0.08f)
                val balloonR = w * 0.28f
                drawCircle(
                    brush = Brush.radialGradient(listOf(Color(0xFFFCA5A5), Color(0xFFDC2626))),
                    radius = balloonR,
                    center = balloonCenter
                )
                // Escaping wind swoosh marks
                drawLine(Color(0xFFE2E8F0), Offset(cx - w * 0.35f, cy + h * 0.24f), Offset(cx - w * 0.10f, cy + h * 0.24f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                drawLine(Color(0xFFE2E8F0), Offset(cx - w * 0.20f, cy + h * 0.34f), Offset(cx + w * 0.15f, cy + h * 0.34f), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }
        }
    }
}

// =========================================================================
// 7. DAILY FORTUNE WHEEL ARTWORK
// =========================================================================

@Composable
fun DailyFortuneWheelHubArt(
    modifier: Modifier = Modifier.size(54.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Outer faceted gold bezel
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFB45309))),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )
        // Inner ruby core
        drawCircle(
            brush = Brush.radialGradient(listOf(Color(0xFFFF2A75), Color(0xFF880E4F))),
            radius = w * 0.36f,
            center = Offset(cx, cy)
        )
        // Golden Star Emblem in center
        val star = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) w * 0.26f else w * 0.12f
            val angle = (i * 36 - 90) * (PI / 180.0)
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
        }
        star.close()
        drawPath(star, brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFFD54F))))
    }
}

@Composable
fun DailyWheelIconArt(
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.44f

        val colors = listOf(Color(0xFFFF5D8F), Color(0xFF00F0FF), Color(0xFFFFC93C), Color(0xFF9D4EDD), Color(0xFF10B981), Color(0xFFFF7A00))
        val sweep = 360f / colors.size
        for (i in colors.indices) {
            drawArc(colors[i], startAngle = i * sweep, sweepAngle = sweep, useCenter = true, topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2))
        }
        drawCircle(Color(0xFFFFD700), radius = r, center = Offset(cx, cy), style = Stroke(width = 2f))
        drawCircle(Color.White, radius = r * 0.25f, center = Offset(cx, cy))
    }
}

// =========================================================================
// 8. QUEST AND ACHIEVEMENT ICONS
// =========================================================================

@Composable
fun QuestIconArt(
    questId: String,
    modifier: Modifier = Modifier.size(32.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when {
            questId.contains("pop") -> {
                // 3D Balloon cluster
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFF80AB), Color(0xFFFF1744))), radius = w * 0.34f, center = Offset(cx, cy - h * 0.05f))
                drawCircle(Color.White.copy(alpha = 0.8f), radius = 2.5f, center = Offset(cx - w * 0.12f, cy - h * 0.18f))
                drawLine(Color(0xAAFFFFFF), Offset(cx, cy + h * 0.28f), Offset(cx, cy + h * 0.45f), strokeWidth = 2f)
            }
            questId.contains("combo") -> {
                // Blazing Mega Combo Fire
                val flame = Path().apply {
                    moveTo(cx, cy - h * 0.42f)
                    cubicTo(cx + w * 0.32f, cy - h * 0.05f, cx + w * 0.25f, cy + h * 0.35f, cx, cy + h * 0.38f)
                    cubicTo(cx - w * 0.25f, cy + h * 0.35f, cx - w * 0.32f, cy - h * 0.05f, cx, cy - h * 0.42f)
                    close()
                }
                drawPath(flame, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFF7A00), Color(0xFFDC2626))))
            }
            questId.contains("spin") -> {
                // Wheel
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7))), radius = w * 0.38f, center = Offset(cx, cy))
                drawCircle(Color(0xFFFFD700), radius = w * 0.38f, center = Offset(cx, cy), style = Stroke(width = 2.5f))
                drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
            }
            questId.contains("star") -> {
                // 3D Star
                val star = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) w * 0.42f else w * 0.18f
                    val angle = (i * 36 - 90) * (PI / 180.0)
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) star.moveTo(x, y) else star.lineTo(x, y)
                }
                star.close()
                drawPath(star, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFF59E0B))))
            }
            questId.contains("stage") -> {
                // Golden Trophy Cup
                val cup = Path().apply {
                    moveTo(cx - w * 0.25f, cy - h * 0.30f)
                    lineTo(cx + w * 0.25f, cy - h * 0.30f)
                    cubicTo(cx + w * 0.22f, cy + h * 0.10f, cx + w * 0.10f, cy + h * 0.18f, cx, cy + h * 0.20f)
                    cubicTo(cx - w * 0.10f, cy + h * 0.18f, cx - w * 0.22f, cy + h * 0.10f, cx - w * 0.25f, cy - h * 0.30f)
                    close()
                }
                drawPath(cup, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFB45309))))
                drawLine(Color(0xFFFFD700), Offset(cx, cy + h * 0.20f), Offset(cx, cy + h * 0.35f), strokeWidth = 3f)
                drawLine(Color(0xFFFFD700), Offset(cx - w * 0.20f, cy + h * 0.35f), Offset(cx + w * 0.20f, cy + h * 0.35f), strokeWidth = 3f)
            }
            else -> {
                // Golden Laurel Coin
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFFFDE7), Color(0xFFFFD700), Color(0xFFB45309))), radius = w * 0.40f, center = Offset(cx, cy))
                drawCircle(Color.White, radius = w * 0.15f, center = Offset(cx, cy))
            }
        }
    }
}

// =========================================================================
// 9. HOME & DOCK NAVIGATION ICONS
// =========================================================================

@Composable
fun HomeNavIcon(
    type: String,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (type.uppercase()) {
            "SPIN" -> {
                // Fortune Wheel
                val colors = listOf(Color(0xFFFF5D8F), Color(0xFF00F0FF), Color(0xFFFFC93C), Color(0xFF9D4EDD), Color(0xFF10B981))
                val sweep = 360f / colors.size
                for (i in colors.indices) {
                    drawArc(colors[i], startAngle = i * sweep, sweepAngle = sweep, useCenter = true, topLeft = Offset(cx - w * 0.42f, cy - h * 0.42f), size = Size(w * 0.84f, h * 0.84f))
                }
                drawCircle(Color(0xFFFFD700), radius = w * 0.42f, center = Offset(cx, cy), style = Stroke(width = 2f))
                drawCircle(Color.White, radius = w * 0.12f, center = Offset(cx, cy))
            }
            "SHOP" -> {
                // Golden Gift Chest
                drawRoundRect(brush = Brush.verticalGradient(listOf(Color(0xFFFFB703), Color(0xFFD97706))), topLeft = Offset(cx - w * 0.36f, cy - h * 0.16f), size = Size(w * 0.72f, h * 0.50f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                drawRoundRect(brush = Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))), topLeft = Offset(cx - w * 0.40f, cy - h * 0.32f), size = Size(w * 0.80f, h * 0.18f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f))
                drawLine(Color(0xFFEF4444), Offset(cx, cy - h * 0.32f), Offset(cx, cy + h * 0.34f), strokeWidth = 3f)
            }
            "QUESTS" -> {
                // Championship Trophy
                val cup = Path().apply {
                    moveTo(cx - w * 0.28f, cy - h * 0.32f)
                    lineTo(cx + w * 0.28f, cy - h * 0.32f)
                    cubicTo(cx + w * 0.24f, cy + h * 0.08f, cx + w * 0.12f, cy + h * 0.18f, cx, cy + h * 0.20f)
                    cubicTo(cx - w * 0.12f, cy + h * 0.18f, cx - w * 0.24f, cy + h * 0.08f, cx - w * 0.28f, cy - h * 0.32f)
                    close()
                }
                drawPath(cup, brush = Brush.verticalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFFD700), Color(0xFFD97706))))
                drawLine(Color(0xFFFFD700), Offset(cx, cy + h * 0.20f), Offset(cx, cy + h * 0.34f), strokeWidth = 3f)
                drawLine(Color(0xFFFFD700), Offset(cx - w * 0.22f, cy + h * 0.34f), Offset(cx + w * 0.22f, cy + h * 0.34f), strokeWidth = 3.5f)
            }
        }
    }
}
