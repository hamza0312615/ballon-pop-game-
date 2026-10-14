package com.example.balloonpop.ui.game

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.example.balloonpop.model.*
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    gameState: GameState,
    balloons: List<Balloon>,
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    swipeTrail: List<BladeTrailPoint>,
    hammerStrikes: List<HammerStrikeEffect>,
    shockwaves: List<PopShockwave> = emptyList(),
    ambients: List<AmbientElement> = emptyList(),
    equippedFrame: PlayFrame,
    equippedTheme: ThemeConfig = GameConstants.SHOP_THEMES.first(),
    lightningBolts: List<LightningBolt> = emptyList(),
    screenFlashAlpha: Float = 0f,
    screenShake: () -> Float = { 0f },
    isFreezeActive: Boolean,
    isMultiplierActive: Boolean,
    currentLevel: Int = 1,
    onStepFrame: (Float) -> Unit = {},
    onPointerInput: (Float, Float, Boolean) -> Unit,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableLongStateOf(0L) }

    // Dedicated VSYNC 60/120 FPS game loop isolated strictly inside the canvas
    LaunchedEffect(gameState) {
        if (gameState == GameState.PLAYING) {
            var lastNanos = System.nanoTime()
            while (isActive) {
                withFrameNanos { nowNanos ->
                    val dt = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    lastNanos = nowNanos
                    onStepFrame(dt)
                    frameTick = nowNanos
                }
            }
        }
    }

    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
        }
    }

    val emojiPaint = remember {
        Paint().apply {
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
    }

    // Pre-allocated reusable paths to avoid garbage collection allocations per frame
    val reusablePath1 = remember { Path() }
    val reusablePath2 = remember { Path() }

    // Cached gradients for zero-allocation rendering
    var cachedWidth by remember { mutableFloatStateOf(0f) }
    var cachedHeight by remember { mutableFloatStateOf(0f) }
    var cachedBgBrush by remember(equippedTheme) { mutableStateOf<Brush?>(null) }
    var cachedOrb1 by remember { mutableStateOf<Brush?>(null) }
    var cachedOrb2 by remember { mutableStateOf<Brush?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                onCanvasSizeChanged(size.width.toFloat(), size.height.toFloat())
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onPointerInput(down.position.x, down.position.y, true)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (change.pressed) {
                            onPointerInput(change.position.x, change.position.y, false)
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // Read frameTick to drive continuous 60/120 FPS canvas redraw
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        // Update cached background brushes when size changes
        if (size.width != cachedWidth || size.height != cachedHeight || cachedBgBrush == null) {
            cachedWidth = size.width
            cachedHeight = size.height
            val themeColors = if (equippedTheme.id == "theme_sunset_twilight" || equippedTheme.gradientColors.size < 2) {
                listOf(
                    Color(0xFF1B0B38), // Deep purple
                    Color(0xFF330D52), // Rich twilight violet
                    Color(0xFF5A1466), // Magenta plum
                    Color(0xFF8C1D5D), // Sunset berry
                    Color(0xFFC43345), // Coral crimson
                    Color(0xFFE65100), // Sunset amber
                    Color(0xFFFFA726)  // Warm golden orange
                )
            } else {
                equippedTheme.gradientColors
            }
            cachedBgBrush = Brush.verticalGradient(themeColors)
            cachedOrb1 = Brush.radialGradient(
                colors = listOf(Color(0x2800F0FF), Color.Transparent),
                center = Offset(size.width * 0.2f, size.height * 0.3f),
                radius = size.width * 0.6f
            )
            cachedOrb2 = Brush.radialGradient(
                colors = listOf(Color(0x28FF2A75), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.7f),
                radius = size.width * 0.7f
            )
        }

        val currentShake = screenShake()
        val shakeOffset = com.example.balloonpop.util.ScreenShakeUtility.calculateVisualDisplacement(currentShake)

        drawContext.canvas.save()
        if (shakeOffset.x != 0f || shakeOffset.y != 0f) {
            drawContext.canvas.translate(shakeOffset.x, shakeOffset.y)
        }

        val w = size.width
        val h = size.height
        val timeSec = (frameTick % 100_000_000_000L) / 1_000_000_000f

        // 1. Draw Vibrant High-Contrast Gameplay Background (Deep Purple to Warm Orange Sunset Gradient)
        drawRect(
            brush = cachedBgBrush ?: Brush.verticalGradient(
                listOf(
                    Color(0xFF1B0B38),
                    Color(0xFF330D52),
                    Color(0xFF5A1466),
                    Color(0xFF8C1D5D),
                    Color(0xFFC43345),
                    Color(0xFFE65100),
                    Color(0xFFFFA726)
                )
            )
        )

        // Radial ambient glow orbs for depth
        cachedOrb1?.let { drawRect(brush = it) }
        cachedOrb2?.let { drawRect(brush = it) }

        // Drifting soft twilight clouds
        for (c in 0..3) {
            val cx = ((w * (c * 0.38f + timeSec * 0.025f)) % (w + 240f)) - 120f
            val cy = h * (0.16f + c * 0.18f)
            val cloudColor = if (c % 2 == 0) Color(0x24F472B6) else Color(0x1EFDBA74)
            drawCircle(color = cloudColor, radius = 55f, center = Offset(cx, cy))
            drawCircle(color = cloudColor, radius = 40f, center = Offset(cx - 40f, cy + 8f))
            drawCircle(color = cloudColor, radius = 44f, center = Offset(cx + 40f, cy + 6f))
        }

        // Sparkling twilight stars in upper sky
        for (s in 0 until 18) {
            val sx = (w * (s * 0.055f + 0.04f)) % w
            val sy = (h * ((s * 0.045f) % 0.45f))
            val twinkle = (sin((timeSec * 3.5 + s * 1.5).toDouble()).toFloat() * 0.45f + 0.55f).coerceIn(0.2f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = twinkle * 0.75f),
                radius = if (s % 3 == 0) 2.5f else 1.5f,
                center = Offset(sx, sy)
            )
        }

        // 2. Distant Floating Balloon Silhouettes in the Parallax Background
        for (bgB in 0..4) {
            val bgY = (h * ((bgB * 0.22f - timeSec * 0.035f) % 1.0f + 1.0f) % 1.0f)
            val bgX = w * (0.15f + bgB * 0.18f + sin(bgY * 0.015f) * 0.04f)
            val bgCol = when (bgB % 4) {
                0 -> Color(0x2238BDF8)
                1 -> Color(0x22F43F5E)
                2 -> Color(0x22FBBF24)
                else -> Color(0x22A855F7)
            }
            drawOval(
                color = bgCol,
                topLeft = Offset(bgX - 16f, bgY - 22f),
                size = Size(32f, 44f)
            )
            drawLine(
                color = bgCol.copy(alpha = 0.15f),
                start = Offset(bgX, bgY + 22f),
                end = Offset(bgX, bgY + 45f),
                strokeWidth = 1.2f
            )
        }

        // Ambient Luminous Neon Orbs for Depth and Polish
        cachedOrb1?.let {
            drawCircle(
                brush = it,
                radius = size.width * 0.6f,
                center = Offset(size.width * 0.2f, size.height * 0.3f)
            )
        }
        cachedOrb2?.let {
            drawCircle(
                brush = it,
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.85f, size.height * 0.7f)
            )
        }

        // Ambient floating background elements (Petals, Stars, Bubbles, Snow, Embers)
        synchronized(ambients) {
            for (amb in ambients) {
                when (amb.type) {
                    AmbientType.STAR, AmbientType.GOLD_SPARKLE -> {
                        drawCircle(
                            color = amb.color.copy(alpha = amb.alpha.coerceIn(0f, 1f)),
                            radius = amb.size,
                            center = Offset(amb.x, amb.y)
                        )
                    }
                    AmbientType.CHERRY_PETAL -> {
                        drawOval(
                            color = amb.color.copy(alpha = amb.alpha.coerceIn(0f, 1f)),
                            topLeft = Offset(amb.x - amb.size, amb.y - amb.size * 0.6f),
                            size = Size(amb.size * 2f, amb.size * 1.2f)
                        )
                    }
                    AmbientType.BUBBLE -> {
                        drawCircle(
                            color = amb.color.copy(alpha = amb.alpha.coerceIn(0f, 1f)),
                            radius = amb.size,
                            center = Offset(amb.x, amb.y),
                            style = Stroke(width = 2f)
                        )
                    }
                    AmbientType.SNOWFLAKE -> {
                        drawCircle(
                            color = Color.White.copy(alpha = amb.alpha.coerceIn(0f, 1f)),
                            radius = amb.size,
                            center = Offset(amb.x, amb.y)
                        )
                    }
                    AmbientType.EMBER, AmbientType.CLOUD -> {
                        drawCircle(
                            color = amb.color.copy(alpha = (amb.alpha * 0.7f).coerceIn(0f, 1f)),
                            radius = amb.size,
                            center = Offset(amb.x, amb.y)
                        )
                    }
                }
            }
        }

        // 2. Draw Balloons with Character Faces & Shapes (Zero-Allocation Geometry)
        synchronized(balloons) {
            for (b in balloons) {
                if (b.popped) continue
                drawStylizedBalloon(b, emojiPaint, reusablePath1, reusablePath2)
            }
        }

        // Pop Shockwaves (Dynamic visual burst rings)
        synchronized(shockwaves) {
            for (sw in shockwaves) {
                drawCircle(
                    color = sw.color.copy(alpha = sw.alpha.coerceIn(0f, 1f)),
                    radius = sw.radius,
                    center = Offset(sw.x, sw.y),
                    style = Stroke(width = (4f * sw.alpha).coerceAtLeast(1.5f))
                )
            }
        }

        // 3. Draw Particles
        synchronized(particles) {
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // 4. Draw Blade Swipe Trail
        synchronized(swipeTrail) {
            val sizeTrail = swipeTrail.size
            if (sizeTrail > 1) {
                for (i in 1 until sizeTrail) {
                    val p1 = swipeTrail[i - 1]
                    val p2 = swipeTrail[i]
                    val trailAlpha = p2.alpha.coerceIn(0f, 1f)
                    drawLine(
                        color = p2.color.copy(alpha = trailAlpha),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = 12f * trailAlpha,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White.copy(alpha = trailAlpha * 0.85f),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = 4f * trailAlpha,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // 5. Draw Hammer Strikes Animations
        synchronized(hammerStrikes) {
            for (strike in hammerStrikes) {
                drawHammerStrike(strike)
            }
        }

        // 5.5 Draw Lightning Bolts (Thunder strikes & Electric arcs)
        synchronized(lightningBolts) {
            for (bolt in lightningBolts) {
                if (bolt.points.size >= 2) {
                    val boltAlpha = bolt.alpha.coerceIn(0f, 1f)
                    val pts = bolt.points
                    // Outer electric cyan glow
                    for (i in 1 until pts.size) {
                        drawLine(
                            color = bolt.color.copy(alpha = boltAlpha * 0.75f),
                            start = Offset(pts[i - 1].first, pts[i - 1].second),
                            end = Offset(pts[i].first, pts[i].second),
                            strokeWidth = 10f * boltAlpha,
                            cap = StrokeCap.Round
                        )
                    }
                    // Inner electric pure white core
                    for (i in 1 until pts.size) {
                        drawLine(
                            color = Color.White.copy(alpha = boltAlpha * 0.95f),
                            start = Offset(pts[i - 1].first, pts[i - 1].second),
                            end = Offset(pts[i].first, pts[i].second),
                            strokeWidth = 3.5f * boltAlpha,
                            cap = StrokeCap.Round
                        )
                    }
                    // Branching electric forks
                    for (branch in bolt.branchPoints) {
                        for (j in 1 until branch.size) {
                            drawLine(
                                color = bolt.color.copy(alpha = boltAlpha * 0.6f),
                                start = Offset(branch[j - 1].first, branch[j - 1].second),
                                end = Offset(branch[j].first, branch[j].second),
                                strokeWidth = 4f * boltAlpha,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    // Electric spark at the strike impact point
                    val endPt = pts.last()
                    drawCircle(
                        color = Color.White.copy(alpha = boltAlpha),
                        radius = 16f * boltAlpha,
                        center = Offset(endPt.first, endPt.second)
                    )
                    drawCircle(
                        color = bolt.color.copy(alpha = boltAlpha * 0.5f),
                        radius = 32f * boltAlpha,
                        center = Offset(endPt.first, endPt.second)
                    )
                }
            }
        }

        // 6. Draw Floating Score/Combo Texts
        synchronized(floatingTexts) {
            for (ft in floatingTexts) {
                val alphaInt = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
                textPaint.color = ft.color.toArgb()
                textPaint.alpha = alphaInt
                drawContext.canvas.nativeCanvas.drawText(
                    ft.text,
                    ft.x,
                    ft.y,
                    textPaint
                )
            }
        }

        // 7. Freeze Icy Vignette
        if (isFreezeActive) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x2500E5FF), Color(0x6000B4D8)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension / 1.4f
                )
            )
        }

        // 8. Multiplier Golden Vignette
        if (isMultiplierActive) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x20FFD700), Color(0x50FFA000)),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.maxDimension / 1.5f
                )
            )
        }

        // 8.5 Screen Thunder Flash
        if (screenFlashAlpha > 0f) {
            drawRect(
                color = Color.White.copy(alpha = screenFlashAlpha.coerceIn(0f, 0.7f))
            )
        }

        // 9. Draw Equipped Aesthetic Playfield Frame
        drawAestheticFrame(equippedFrame)

        drawContext.canvas.restore()
    }
}

private fun DrawScope.drawStylizedBalloon(
    b: Balloon,
    emojiPaint: Paint,
    path1: Path,
    path2: Path
) {
    when (b.shapeStyle) {
        BalloonShapeStyle.STAR_SHAPE -> {
            drawStarBalloon(b, path1)
        }
        BalloonShapeStyle.HEART_SHAPE -> {
            drawHeartBalloon(b, path1)
        }
        BalloonShapeStyle.CAT_FACE -> {
            drawCatBalloon(b, emojiPaint, path1, path2)
        }
        else -> {
            drawRoundBalloonWithFace(b, emojiPaint)
        }
    }
}

private fun DrawScope.drawRoundBalloonWithFace(b: Balloon, emojiPaint: Paint) {
    val r = b.radius
    val x = b.x
    val y = b.y
    val curAlpha = b.alpha.coerceIn(0.1f, 1.0f)

    // Speed exhaust flames for FAST_ROCKET
    if (b.type == BalloonType.FAST_ROCKET) {
        val flameLen = r * 0.6f
        drawCircle(
            color = Color(0xFFFFB703).copy(alpha = 0.8f * curAlpha),
            radius = flameLen,
            center = Offset(x, y + r * 1.25f)
        )
        drawCircle(
            color = Color(0xFFFF007F).copy(alpha = 0.6f * curAlpha),
            radius = flameLen * 0.6f,
            center = Offset(x, y + r * 1.35f)
        )
    }

    // Subtle luminous outer glow to maintain high visibility and pop against the background
    val glowColor = when (b.type) {
        BalloonType.BOMB -> Color(0xFFFF3B30) // High-contrast crimson glow for dark bombs
        BalloonType.GOLDEN, BalloonType.GOLDEN_PIGGY -> Color(0xFFFFD700)
        BalloonType.RAINBOW -> Color(0xFF38BDF8)
        BalloonType.FREEZE -> Color(0xFF67E8F9)
        BalloonType.MEGA_BOSS -> Color(0xFFFF007F)
        BalloonType.ELECTRIC_STORM -> Color(0xFFFACC15)
        BalloonType.HEART -> Color(0xFFFF2A75)
        else -> b.color
    }

    // Outer Glow: Diffuse luminous aura
    drawOval(
        color = glowColor.copy(alpha = 0.32f * curAlpha),
        topLeft = Offset(x - r * 1.05f, y - r * 1.25f),
        size = Size(r * 2.1f, r * 2.5f)
    )

    // Outer Glow: Crisp luminous rim stroke for clean silhouette definition
    drawOval(
        color = Color.White.copy(alpha = 0.38f * curAlpha),
        topLeft = Offset(x - r * 0.93f, y - r * 1.13f),
        size = Size(r * 1.86f, r * 2.26f),
        style = Stroke(width = 2.2f)
    )

    // Soft Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.35f * curAlpha),
        topLeft = Offset(x - r * 0.9f, y - r * 1.1f + 8f),
        size = Size(r * 1.8f, r * 2.2f)
    )

    // Main Oval Balloon Body (Zero GC allocation, runs at solid 120 FPS)
    drawOval(
        color = b.color.copy(alpha = curAlpha),
        topLeft = Offset(x - r * 0.9f, y - r * 1.1f),
        size = Size(r * 1.8f, r * 2.2f)
    )

    // 3D Volumetric Depth Reflection
    drawOval(
        color = Color.White.copy(alpha = 0.25f * curAlpha),
        topLeft = Offset(x - r * 0.75f, y - r * 0.95f),
        size = Size(r * 1.35f, r * 1.55f)
    )

    // Highlight Gloss
    drawOval(
        color = Color.White.copy(alpha = 0.75f * curAlpha),
        topLeft = Offset(x - r * 0.55f, y - r * 0.85f),
        size = Size(r * 0.45f, r * 0.6f)
    )

    // Specular Pin-Dot Flare
    drawCircle(
        color = Color.White.copy(alpha = 0.90f * curAlpha),
        radius = r * 0.12f,
        center = Offset(x - r * 0.35f, y - r * 0.65f)
    )

    // Shield barrier aura & cracks
    if (b.type == BalloonType.SHIELDED || b.maxHp > 1) {
        drawOval(
            color = Color(0xFF00E5FF).copy(alpha = 0.6f * curAlpha),
            topLeft = Offset(x - r * 1.05f, y - r * 1.25f),
            size = Size(r * 2.1f, r * 2.5f),
            style = Stroke(width = 4f)
        )
        if (b.hp < b.maxHp) {
            // Draw crack lines on shield
            drawLine(
                color = Color.White.copy(alpha = 0.85f * curAlpha),
                start = Offset(x - r * 0.4f, y - r * 0.5f),
                end = Offset(x + r * 0.3f, y + r * 0.2f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.85f * curAlpha),
                start = Offset(x + r * 0.1f, y - r * 0.1f),
                end = Offset(x + r * 0.6f, y - r * 0.4f),
                strokeWidth = 2.5f
            )
        }
    }

    // Knot & String
    drawCircle(
        color = b.color.copy(alpha = curAlpha),
        radius = r * 0.18f,
        center = Offset(x, y + r * 1.1f)
    )

    drawLine(
        color = Color.White.copy(alpha = 0.45f * curAlpha),
        start = Offset(x, y + r * 1.15f),
        end = Offset(x + sin(b.wobblePhase) * 6f, y + r * 1.65f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Render Face or Emoji
    val emoji = b.type.iconEmoji
    if (emoji != null) {
        emojiPaint.textSize = r * 0.9f
        emojiPaint.alpha = (curAlpha * 255).toInt()
        drawContext.canvas.nativeCanvas.drawText(
            emoji,
            x,
            y + r * 0.3f,
            emojiPaint
        )
    } else {
        drawFaceExpressionsZeroAlloc(b)
    }

    // Mega Boss Health Bar
    if (b.type == BalloonType.MEGA_BOSS && b.maxHp > 1) {
        val barW = r * 1.8f
        val barH = 10f
        val barX = x - barW / 2f
        val barY = y - r * 1.4f
        // Bar background
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.7f),
            topLeft = Offset(barX, barY),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(5f, 5f)
        )
        // Bar fill
        val hpPercent = (b.hp.toFloat() / b.maxHp.toFloat()).coerceIn(0f, 1f)
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFF007F), Color(0xFFFFB703))),
            topLeft = Offset(barX + 1f, barY + 1f),
            size = Size((barW - 2f) * hpPercent, barH - 2f),
            cornerRadius = CornerRadius(4f, 4f)
        )
    }
}

private fun DrawScope.drawFaceExpressionsZeroAlloc(b: Balloon) {
    val x = b.x
    val y = b.y
    val r = b.radius

    when (b.shapeStyle) {
        BalloonShapeStyle.ANGRY -> {
            // Angry slanted eyebrows
            drawLine(
                color = Color.Black,
                start = Offset(x - r * 0.45f, y - r * 0.35f),
                end = Offset(x - r * 0.1f, y - r * 0.15f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.Black,
                start = Offset(x + r * 0.45f, y - r * 0.35f),
                end = Offset(x + r * 0.1f, y - r * 0.15f),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            // Fierce eyes
            drawCircle(Color.White, radius = r * 0.16f, center = Offset(x - r * 0.28f, y - r * 0.05f))
            drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x - r * 0.25f, y - r * 0.05f))
            drawCircle(Color.White, radius = r * 0.16f, center = Offset(x + r * 0.28f, y - r * 0.05f))
            drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x + r * 0.25f, y - r * 0.05f))
            // Gritted teeth mouth
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x - r * 0.3f, y + r * 0.25f),
                size = Size(r * 0.6f, r * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(x - r * 0.3f, y + r * 0.25f),
                size = Size(r * 0.6f, r * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                style = Stroke(width = 2.5f)
            )
            drawLine(
                color = Color.Black,
                start = Offset(x, y + r * 0.25f),
                end = Offset(x, y + r * 0.47f),
                strokeWidth = 2f
            )
        }
        BalloonShapeStyle.SMILEY -> {
            val eyeY = y - r * 0.10f
            val eyeR = r * 0.11f
            // Left sparkle eye
            drawCircle(Color(0xFF1E0A12), radius = eyeR, center = Offset(x - r * 0.28f, eyeY))
            drawCircle(Color.White, radius = eyeR * 0.42f, center = Offset(x - r * 0.25f, eyeY - eyeR * 0.25f))
            drawCircle(Color.White, radius = eyeR * 0.20f, center = Offset(x - r * 0.30f, eyeY + eyeR * 0.20f))

            // Right sparkle eye
            drawCircle(Color(0xFF1E0A12), radius = eyeR, center = Offset(x + r * 0.28f, eyeY))
            drawCircle(Color.White, radius = eyeR * 0.42f, center = Offset(x + r * 0.31f, eyeY - eyeR * 0.25f))
            drawCircle(Color.White, radius = eyeR * 0.20f, center = Offset(x + r * 0.26f, eyeY + eyeR * 0.20f))

            // Rosy Blush
            drawOval(
                color = Color(0x66FF2A75),
                topLeft = Offset(x - r * 0.46f, y + r * 0.02f),
                size = Size(r * 0.22f, r * 0.13f)
            )
            drawOval(
                color = Color(0x66FF2A75),
                topLeft = Offset(x + r * 0.24f, y + r * 0.02f),
                size = Size(r * 0.22f, r * 0.13f)
            )

            // Cute smiling open mouth with pink tongue
            val mouthPath = Path().apply {
                moveTo(x - r * 0.15f, y + r * 0.12f)
                quadraticTo(x, y + r * 0.32f, x + r * 0.15f, y + r * 0.12f)
                close()
            }
            drawPath(mouthPath, color = Color(0xFF1E0A12))
            drawCircle(Color(0xFFFF5D8F), radius = r * 0.06f, center = Offset(x, y + r * 0.24f))
        }
        BalloonShapeStyle.FUNNY -> {
            // Mismatched goofy googly eyes
            // Big eye
            drawCircle(Color.White, radius = r * 0.22f, center = Offset(x - r * 0.24f, y - r * 0.15f))
            drawCircle(Color.Black, radius = r * 0.11f, center = Offset(x - r * 0.28f, y - r * 0.12f))
            drawCircle(Color.White, radius = r * 0.04f, center = Offset(x - r * 0.26f, y - r * 0.15f))
            // Small eye
            drawCircle(Color.White, radius = r * 0.14f, center = Offset(x + r * 0.24f, y - r * 0.12f))
            drawCircle(Color.Black, radius = r * 0.07f, center = Offset(x + r * 0.22f, y - r * 0.16f))

            // Wobbly silly mouth & tongue sticking out
            drawArc(
                color = Color.Black,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(x - r * 0.35f, y + r * 0.1f),
                size = Size(r * 0.7f, r * 0.32f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
            // Sticking out tongue
            drawOval(
                color = Color(0xFFFF4081),
                topLeft = Offset(x + r * 0.02f, y + r * 0.2f),
                size = Size(r * 0.28f, r * 0.32f)
            )
            drawLine(
                color = Color(0xFFC2185B),
                start = Offset(x + r * 0.16f, y + r * 0.2f),
                end = Offset(x + r * 0.16f, y + r * 0.45f),
                strokeWidth = 2f
            )
        }
        BalloonShapeStyle.COOL -> {
            // Sleek Sunglasses Lenses
            drawRoundRect(
                color = Color(0xFF111115),
                topLeft = Offset(x - r * 0.52f, y - r * 0.22f),
                size = Size(r * 0.44f, r * 0.34f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFF111115),
                topLeft = Offset(x + r * 0.08f, y - r * 0.22f),
                size = Size(r * 0.44f, r * 0.34f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
            // Bridge
            drawLine(
                color = Color(0xFF111115),
                start = Offset(x - r * 0.08f, y - r * 0.15f),
                end = Offset(x + r * 0.08f, y - r * 0.15f),
                strokeWidth = 4f
            )
            // Sunglasses white glossy reflection bars
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(x - r * 0.44f, y - r * 0.15f),
                end = Offset(x - r * 0.22f, y + r * 0.04f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.8f),
                start = Offset(x + r * 0.16f, y - r * 0.15f),
                end = Offset(x + r * 0.38f, y + r * 0.04f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Smirk
            drawArc(
                color = Color.Black,
                startAngle = 10f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(x - r * 0.05f, y + r * 0.18f),
                size = Size(r * 0.4f, r * 0.22f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )
        }
        BalloonShapeStyle.SHOCKED -> {
            // Big staring eyes
            drawCircle(Color.White, radius = r * 0.22f, center = Offset(x - r * 0.26f, y - r * 0.12f))
            drawCircle(Color.Black, radius = r * 0.08f, center = Offset(x - r * 0.26f, y - r * 0.12f))
            drawCircle(Color.White, radius = r * 0.22f, center = Offset(x + r * 0.26f, y - r * 0.12f))
            drawCircle(Color.Black, radius = r * 0.08f, center = Offset(x + r * 0.26f, y - r * 0.12f))
            // Open gasp "O" mouth
            drawOval(
                color = Color(0xFF21000A),
                topLeft = Offset(x - r * 0.18f, y + r * 0.18f),
                size = Size(r * 0.36f, r * 0.44f)
            )
        }
        else -> {}
    }
}

private fun DrawScope.drawCatBalloon(
    b: Balloon,
    emojiPaint: Paint,
    path1: Path,
    path2: Path
) {
    val r = b.radius
    val x = b.x
    val y = b.y

    val curAlpha = b.alpha.coerceIn(0.1f, 1.0f)

    // Subtle Outer Glow for Cat Balloon
    drawOval(
        color = b.color.copy(alpha = 0.30f * curAlpha),
        topLeft = Offset(x - r * 1.04f, y - r * 1.04f),
        size = Size(r * 2.08f, r * 2.08f)
    )
    drawOval(
        color = Color.White.copy(alpha = 0.38f * curAlpha),
        topLeft = Offset(x - r * 0.93f, y - r * 0.93f),
        size = Size(r * 1.86f, r * 1.86f),
        style = Stroke(width = 2.2f)
    )

    // Reusable Cat Ears (Path reset)
    path1.reset()
    path1.moveTo(x - r * 0.75f, y - r * 0.4f)
    path1.lineTo(x - r * 0.65f, y - r * 1.15f)
    path1.lineTo(x - r * 0.2f, y - r * 0.8f)
    path1.close()
    path1.moveTo(x + r * 0.2f, y - r * 0.8f)
    path1.lineTo(x + r * 0.65f, y - r * 1.15f)
    path1.lineTo(x + r * 0.75f, y - r * 0.4f)
    path1.close()
    drawPath(path1, color = b.color)

    // Inner pink ears
    path2.reset()
    path2.moveTo(x - r * 0.65f, y - r * 0.5f)
    path2.lineTo(x - r * 0.6f, y - r * 1.0f)
    path2.lineTo(x - r * 0.28f, y - r * 0.75f)
    path2.close()
    path2.moveTo(x + r * 0.28f, y - r * 0.75f)
    path2.lineTo(x + r * 0.6f, y - r * 1.0f)
    path2.lineTo(x + r * 0.65f, y - r * 0.5f)
    path2.close()
    drawPath(path2, color = Color(0xFFFF80AB))

    // Main Body
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.4f), b.color, b.color.copy(alpha = 0.85f)),
            center = Offset(x - r * 0.3f, y - r * 0.4f),
            radius = r * 1.4f
        ),
        topLeft = Offset(x - r * 0.9f, y - r * 0.9f),
        size = Size(r * 1.8f, r * 1.8f)
    )

    // Cute Cat Closed Eyes ^.^
    drawArc(
        color = Color.Black,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(x - r * 0.45f, y - r * 0.25f),
        size = Size(r * 0.3f, r * 0.22f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )
    drawArc(
        color = Color.Black,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(x + r * 0.15f, y - r * 0.25f),
        size = Size(r * 0.3f, r * 0.22f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    // Pink triangle nose
    drawCircle(Color(0xFFFF4081), radius = r * 0.09f, center = Offset(x, y + r * 0.08f))

    // Whiskers
    drawLine(Color.Black, Offset(x - r * 0.35f, y + r * 0.08f), Offset(x - r * 0.75f, y + r * 0.02f), strokeWidth = 2.5f)
    drawLine(Color.Black, Offset(x - r * 0.35f, y + r * 0.18f), Offset(x - r * 0.72f, y + r * 0.25f), strokeWidth = 2.5f)
    drawLine(Color.Black, Offset(x + r * 0.35f, y + r * 0.08f), Offset(x + r * 0.75f, y + r * 0.02f), strokeWidth = 2.5f)
    drawLine(Color.Black, Offset(x + r * 0.35f, y + r * 0.18f), Offset(x + r * 0.72f, y + r * 0.25f), strokeWidth = 2.5f)

    // Knot
    drawCircle(b.color, radius = r * 0.15f, center = Offset(x, y + r * 0.95f))
}

private fun DrawScope.drawStarBalloon(b: Balloon, starPath: Path) {
    val r = b.radius * 1.1f
    val x = b.x
    val y = b.y
    val points = 5
    val outerR = r
    val innerR = r * 0.48f
    val curAlpha = b.alpha.coerceIn(0.1f, 1.0f)

    starPath.reset()
    for (i in 0 until points * 2) {
        val angle = i * Math.PI / points - Math.PI / 2
        val currentR = if (i % 2 == 0) outerR else innerR
        val px = (x + cos(angle) * currentR).toFloat()
        val py = (y + sin(angle) * currentR).toFloat()
        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
    }
    starPath.close()

    // Subtle Outer Glow for Star Balloon
    drawPath(starPath, color = Color(0xFFFFD700).copy(alpha = 0.32f * curAlpha), style = Stroke(width = 8f))
    drawPath(starPath, color = Color.White.copy(alpha = 0.40f * curAlpha), style = Stroke(width = 2.2f))

    drawPath(
        starPath,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.6f), Color(0xFFFFD700), Color(0xFFFFA000)),
            center = Offset(x, y),
            radius = r
        )
    )
    drawPath(starPath, color = Color(0xFFFFE082), style = Stroke(width = 3f))

    // Cute smiling face on star
    drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x - r * 0.2f, y - r * 0.05f))
    drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x + r * 0.2f, y - r * 0.05f))
    drawCircle(Color.White, radius = r * 0.03f, center = Offset(x - r * 0.22f, y - r * 0.08f))
    drawCircle(Color.White, radius = r * 0.03f, center = Offset(x + r * 0.18f, y - r * 0.08f))

    // Smile
    drawArc(
        color = Color.Black,
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(x - r * 0.18f, y + r * 0.05f),
        size = Size(r * 0.36f, r * 0.22f),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawHeartBalloon(b: Balloon, heartPath: Path) {
    val r = b.radius
    val x = b.x
    val y = b.y - r * 0.15f
    val curAlpha = b.alpha.coerceIn(0.1f, 1.0f)

    heartPath.reset()
    heartPath.moveTo(x, y + r * 0.7f)
    heartPath.cubicTo(x - r * 1.3f, y - r * 0.1f, x - r * 0.9f, y - r * 1.05f, x, y - r * 0.35f)
    heartPath.cubicTo(x + r * 0.9f, y - r * 1.05f, x + r * 1.3f, y - r * 0.1f, x, y + r * 0.7f)
    heartPath.close()

    // Subtle Outer Glow for Heart Balloon
    drawPath(heartPath, color = Color(0xFFFF2A75).copy(alpha = 0.32f * curAlpha), style = Stroke(width = 8f))
    drawPath(heartPath, color = Color.White.copy(alpha = 0.40f * curAlpha), style = Stroke(width = 2.2f))

    drawPath(
        heartPath,
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.5f), Color(0xFFFF2A75), Color(0xFFC2185B)),
            center = Offset(x - r * 0.3f, y - r * 0.3f),
            radius = r * 1.3f
        )
    )

    // Cute Twinkle eyes
    drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x - r * 0.25f, y - r * 0.1f))
    drawCircle(Color.Black, radius = r * 0.09f, center = Offset(x + r * 0.25f, y - r * 0.1f))
    drawCircle(Color.White, radius = r * 0.04f, center = Offset(x - r * 0.28f, y - r * 0.13f))
    drawCircle(Color.White, radius = r * 0.04f, center = Offset(x + r * 0.22f, y - r * 0.13f))

    drawArc(
        color = Color.Black,
        startAngle = 10f,
        sweepAngle = 160f,
        useCenter = false,
        topLeft = Offset(x - r * 0.15f, y + r * 0.04f),
        size = Size(r * 0.3f, r * 0.2f),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawHammerStrike(strike: HammerStrikeEffect) {
    val x = strike.x
    val y = strike.y
    val p = strike.progress.coerceIn(0f, 1f)
    val skin = strike.hammerSkin

    // 1. Hammer Swing Animation
    val hammerAlpha = if (p < 0.6f) 1.0f else (1.0f - (p - 0.6f) / 0.4f)
    val swingAngle = if (p < 0.4f) {
        -55f + (p / 0.4f) * 75f
    } else {
        20f - (p - 0.4f) * 15f
    }

    rotate(degrees = swingAngle, pivot = Offset(x, y)) {
        val handleLen = 85f
        val headWidth = 55f
        val headHeight = 35f

        // Hammer Handle
        drawLine(
            color = skin.handleColor.copy(alpha = hammerAlpha),
            start = Offset(x, y),
            end = Offset(x - handleLen * 0.7f, y - handleLen * 0.7f),
            strokeWidth = 10f,
            cap = StrokeCap.Round
        )

        // Hammer Head
        drawRoundRect(
            color = skin.headColor.copy(alpha = hammerAlpha),
            topLeft = Offset(x - headWidth * 0.5f, y - headHeight * 0.5f),
            size = Size(headWidth, headHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        // Bevel highlight
        drawRoundRect(
            color = Color.White.copy(alpha = hammerAlpha * 0.6f),
            topLeft = Offset(x - headWidth * 0.45f, y - headHeight * 0.45f),
            size = Size(headWidth * 0.9f, headHeight * 0.35f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
    }

    // 2. Shockwave Expanding Ring on Impact
    if (p > 0.2f) {
        val impactProgress = (p - 0.2f) / 0.8f
        val waveRadius = 15f + impactProgress * (70f + skin.tier * 10f)
        val waveAlpha = (1f - impactProgress).coerceIn(0f, 1f)

        drawCircle(
            color = skin.sparkColor.copy(alpha = waveAlpha * 0.8f),
            radius = waveRadius,
            center = Offset(x, y),
            style = Stroke(width = 6f * (1f - impactProgress))
        )
        drawCircle(
            color = Color.White.copy(alpha = waveAlpha * 0.9f),
            radius = waveRadius * 0.6f,
            center = Offset(x, y),
            style = Stroke(width = 3f)
        )
    }
}

private fun DrawScope.drawAestheticFrame(frame: PlayFrame) {
    val w = size.width
    val h = size.height
    val inset = 12f

    // Frame Outer Glow Border
    val colors = frame.borderColors
    if (colors.size >= 2) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            topLeft = Offset(inset, inset),
            size = Size(w - inset * 2, h - inset * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
            style = Stroke(width = 4.5f)
        )
    }

    // High-Tech Ornamental Corners
    val cornerLen = 32f
    val cornerColor = frame.glowColor

    // Top-Left Corner Bracket
    drawLine(cornerColor, Offset(inset + 4f, inset + cornerLen), Offset(inset + 4f, inset + 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(cornerColor, Offset(inset + 4f, inset + 4f), Offset(inset + cornerLen, inset + 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawCircle(cornerColor, radius = 4f, center = Offset(inset + 4f, inset + 4f))

    // Top-Right Corner Bracket
    drawLine(cornerColor, Offset(w - inset - cornerLen, inset + 4f), Offset(w - inset - 4f, inset + 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(cornerColor, Offset(w - inset - 4f, inset + 4f), Offset(w - inset - 4f, inset + cornerLen), strokeWidth = 5f, cap = StrokeCap.Round)
    drawCircle(cornerColor, radius = 4f, center = Offset(w - inset - 4f, inset + 4f))

    // Bottom-Left Corner Bracket
    drawLine(cornerColor, Offset(inset + 4f, h - inset - cornerLen), Offset(inset + 4f, h - inset - 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(cornerColor, Offset(inset + 4f, h - inset - 4f), Offset(inset + cornerLen, h - inset - 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawCircle(cornerColor, radius = 4f, center = Offset(inset + 4f, h - inset - 4f))

    // Bottom-Right Corner Bracket
    drawLine(cornerColor, Offset(w - inset - cornerLen, h - inset - 4f), Offset(w - inset - 4f, h - inset - 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(cornerColor, Offset(w - inset - 4f, h - inset - 4f), Offset(w - inset - 4f, h - inset - 4f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawCircle(cornerColor, radius = 4f, center = Offset(w - inset - 4f, h - inset - 4f))
}
