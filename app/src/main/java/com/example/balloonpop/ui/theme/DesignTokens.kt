package com.example.balloonpop.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Balloon Pop Unified Design System Tokens
 * Strictly enforces consistent Colors, Typography scale, Spacing scale,
 * Corner radii, and Elevation across all screens.
 */
object DesignTokens {

    // --- 1. Colors ---
    // 1 primary brand color (vibrant warm gold/amber for main CTAs)
    val PrimaryBrand = Color(0xFFF59E0B)
    val PrimaryBrandBright = Color(0xFFFFB703)
    val PrimaryBrandDark = Color(0xFFD97706)

    // 1 secondary accent (electric teal/cyan for secondary actions & navigation)
    val SecondaryAccent = Color(0xFF00E5FF)
    val SecondaryAccentDeep = Color(0xFF0284C7)

    // 2 background neutrals (dark navy base + card surface)
    val BgBaseNavy = Color(0xFF080C16)
    val BgBaseNavyDeep = Color(0xFF05070D)
    val SurfaceCardNavy = Color(0xFF131927)
    val SurfaceCardElevated = Color(0xFF1B2337)
    val SurfaceBorder = Color(0xFF26334D)
    val SurfaceBorderSubtle = Color(0x3338BDF8)

    // Semantic status colors
    val SuccessGreen = Color(0xFF10B981)
    val SuccessGreenBright = Color(0xFF34D399)
    val DangerRed = Color(0xFFEF4444)
    val DangerRedDark = Color(0xFFB91C1C)
    val WarningAmber = Color(0xFFF59E0B)

    // Star gold tokens
    val StarGoldFilled = Color(0xFFFFD700)
    val StarGoldGlow = Color(0xFFFFA000)
    val StarEmptyGray = Color(0xFF334155)

    // Text hierarchy
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    // --- 2. Typography Scale (Strict 5 Sizes) ---
    val SizeH1 = 24.sp      // Titles & Big Banners
    val SizeH2 = 18.sp      // Section Headers & Card Titles
    val SizeBody = 14.sp    // Standard Body & Button text
    val SizeCaption = 11.sp // Labels, Subtitles, Badges
    val SizeMicro = 9.sp    // Smallest tag chips & status pills

    // --- 3. Spacing Scale (4, 8, 12, 16, 24, 32 dp) ---
    val Space4 = 4.dp
    val Space8 = 8.dp
    val Space12 = 12.dp
    val Space16 = 16.dp
    val Space24 = 24.dp
    val Space32 = 32.dp

    // --- 4. Corner Radius Scale ---
    val RadiusSmall = 12.dp  // Chips, mini badges, input slots
    val RadiusCard = 20.dp   // Cards, popups, dialog frames
    val RadiusPill = 999.dp  // Full pill buttons & HUD bars

    val ShapeSmall = RoundedCornerShape(RadiusSmall)
    val ShapeCard = RoundedCornerShape(RadiusCard)
    val ShapePill = RoundedCornerShape(RadiusPill)

    // --- 5. Elevation & Shadows (3-Level Elevation System) ---
    val ElevationSubtle = 3.dp
    val ElevationMedium = 8.dp
    val ElevationStrong = 16.dp
    val ElevationAmbient = 4.dp
    val ElevationActive = 12.dp

    // Glassmorphic tokens
    val GlassBgGradient = Brush.verticalGradient(
        listOf(Color(0xD91E293B), Color(0xCC0F172A))
    )
    val GlassBorderGradient = Brush.verticalGradient(
        listOf(Color(0x55FFFFFF), Color(0x18FFFFFF))
    )
    val GlassHighlightCrescent = Brush.verticalGradient(
        listOf(Color(0x66FFFFFF), Color(0x00FFFFFF))
    )

    // --- 6. Spring Specs for Micro-interactions ---
    val ButtonPressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
