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

    // --- 1. Colors (Toybox Stage System) ---
    // 1 primary brand color (vibrant toy gold for medals/coins)
    val PrimaryBrand = ToyGold
    val PrimaryBrandBright = ToyGoldGlow
    val PrimaryBrandDark = ToyGoldShelf

    // 1 secondary accent (toy orange for secondary actions & navigation)
    val SecondaryAccent = ToyOrangeBottom
    val SecondaryAccentDeep = ToyOrangeShelf

    // 2 background neutrals (theatrical stage base + violet panel surface)
    val BgBaseNavy = StageMagentaDeep
    val BgBaseNavyDeep = StageCurtainEdge
    val SurfaceCardNavy = ToyVioletPanel
    val SurfaceCardElevated = Color(0xFF642E94)
    val SurfaceBorder = ToyVioletPanelLight
    val SurfaceBorderSubtle = Color(0x337A3EAE)

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
    val RadiusMedium = 16.dp // Buttons, shop cards, HUD bars
    val RadiusCard = 20.dp   // Cards, popups, dialog frames
    val RadiusLarge = 24.dp  // Hero panels, dialogs, bottom docks
    val RadiusPill = 999.dp  // Full pill buttons & HUD bars

    val ShapeSmall = RoundedCornerShape(RadiusSmall)
    val ShapeMedium = RoundedCornerShape(RadiusMedium)
    val ShapeCard = RoundedCornerShape(RadiusCard)
    val ShapeLarge = RoundedCornerShape(RadiusLarge)
    val ShapePill = RoundedCornerShape(RadiusPill)

    // --- 4b. Component Sizes (Pass 4: one size system everywhere) ---
    val ButtonMinHeight = 56.dp   // Every tappable button is at least this tall
    val IconButtonSize = 48.dp    // Secondary icon buttons on home / headers
    val BoosterOrbSize = 64.dp    // Gameplay booster dock orbs
    val ScreenPadding = Space16   // Default screen edge padding
    val ScreenPaddingWide = Space24

    // --- 4c. Typography Tiers (Pass 4: exactly 3 tiers per screen) ---
    val TypeTierTitle = SizeH1     // Tier 1: screen titles & hero numbers
    val TypeTierBody = SizeBody    // Tier 2: body, buttons, card titles (use SizeH2 for card titles)
    val TypeTierCaption = SizeCaption // Tier 3: labels, badges, captions

    // Primary CTA gradient (coral -> warm gold), the single hero-button look
    val CtaGradient = Brush.horizontalGradient(
        listOf(CoralPrimary, CoralPrimaryDark)
    )
    val GoldGradient = Brush.verticalGradient(
        listOf(PrimaryBrandBright, PrimaryBrand, PrimaryBrandDark)
    )

    // --- 4d. Toybox Stage Tokens (Pass 10) ---
    // Chunky beveled plastic button system: vertical gradient face on a darker squish shelf.
    val ShelfDepth = 6.dp
    const val ShelfDarken = 0.22f       // shelf = face bottom darkened by this fraction
    const val GlossAlpha = 0.40f        // top-edge gloss highlight strength
    val RadiusCardStory = 24.dp         // toy panels & dialogs
    val RadiusButtonStory = 20.dp       // chunky toy buttons
    val ShapeCardStory = RoundedCornerShape(RadiusCardStory)
    val ShapeButtonStory = RoundedCornerShape(RadiusButtonStory)

    // Hero Lime Gradient (PLAY / Next / Claim)
    val HeroLimeGradient = Brush.verticalGradient(
        listOf(HeroLimeTop, HeroLimeBottom)
    )
    val SunsetGradient = HeroLimeGradient

    // Secondary Toy Orange Gradient
    val ToyOrangeGradient = Brush.verticalGradient(
        listOf(ToyOrangeTop, ToyOrangeBottom)
    )

    // Theatrical Stage World Gradient (#7A1E46 -> #33103D)
    val StageWorldGradient = Brush.verticalGradient(
        listOf(StageMagentaTop, StageMagentaDeep)
    )

    // Wooden Toy Shelf Gradient (#C07F3A with dark wood edge #7A4A16)
    val WoodShelfGradient = Brush.verticalGradient(
        listOf(ToyWoodHighlight, ToyWoodFloor, ToyWoodFloorDark)
    )

    val GoldFaceGradient = Brush.verticalGradient(
        listOf(ToyGoldGlow, ToyGold)
    )
    // Currency & HUD tray fill: violet pill at 80% alpha
    val PillPlum = PlumPill

    // Motion specs (research rule: motion is the polish)
    val DialogPopSpring = spring<Float>(
        dampingRatio = 0.62f,
        stiffness = Spring.StiffnessMediumLow
    )
    val StarPopSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

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
    // Dp twin of ButtonPressSpring (animateDpAsState needs AnimationSpec<Dp>;
    // added after the Pass 5 compile gate flagged JewelButton's sink animation).
    val ButtonPressSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}
