package com.example.balloonpop.ui.theme

import androidx.compose.ui.graphics.Color

// Vivid Jewel & Neon Accents (Stunning contrast on Pitch Black)
val NeonPink = Color(0xFFC93A5E)
val NeonCyan = Color(0xFF2F6DB3)
val NeonPurple = Color(0xFFA855F7)
val NeonYellow = Color(0xFFFFD700)
val NeonMint = Color(0xFF10B981)
val NeonOrange = Color(0xFFFF6B00)
val NeonElectricBlue = Color(0xFF38BDF8)
val NeonRose = Color(0xFFFB7185)

// Pass 4 Design System: Coral Primary CTA + Deep-Navy Cosmic Base
val CoralPrimary = Color(0xFFFF6B4A)
val CoralPrimaryDark = Color(0xFFE14E2B)
val CoralPrimaryLight = Color(0xFFFF9E7D)
val CosmosNavy = Color(0xFF0A0E24)
val CosmosNavyDeep = Color(0xFF05070F)
val CosmosIndigo = Color(0xFF1B2350)
val CosmosViolet = Color(0xFF3B2E6E)

// Deep Luxury Black & Obsidian Canvas
val PureBlack = Color(0xFF000000)
val PitchBlack = Color(0xFF050508)
val DarkBg1 = Color(0xFF09090E)
val DarkBg2 = Color(0xFF101017)
val DarkBg3 = Color(0xFF151520)

// Elegant Dark Glass & Surface Cards
val SurfaceCard = Color(0xFF12121A)
val SurfaceCardElevated = Color(0xFF181824)
val SurfaceCardBorder = Color(0xFF272738)
val SurfaceGlowCyan = Color(0x332F6DB3)
val SurfaceGlowGold = Color(0x33FFD700)
val SurfaceGlowPink = Color(0x33C93A5E)

// Currency & Reward Accents
val GoldCoin = Color(0xFFFFD700)
val GoldCoinDark = Color(0xFFD97706)
val GemBlue = Color(0xFF2F6DB3)
val DiamondBlue = Color(0xFF38BDF8)

// High-Contrast Accessible Text
val TextWhite = Color(0xFFFFFFFF)
val TextLight = Color(0xFFF1F5F9)
val TextDim = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Thematic Dark Casual Game 3D Accents
val DarkGoldBorder = Color(0xFFF59E0B)
val DarkGoldBg = Color(0xFF261E05)
val DarkGoldText = Color(0xFFFEF08A)

val JellyGreenLight = Color(0xFF6EE7B7)
val JellyGreen = Color(0xFF10B981)
val JellyGreenDark = Color(0xFF047857)
val JellyGreenBorder = Color(0xFF34D399)

val StarGoldBright = Color(0xFFFFEA00)
val StarGoldDeep = Color(0xFFF59E0B)
val StarEmptyDark = Color(0xFF2D2D3A)

val IslandDarkObsidian = Color(0xFF12121E)
val IslandCosmicBlue = Color(0xFF0284C7)
val IslandNeonGlow = Color(0xFF2F6DB3)


// =====================================================================
// Pass 10 — Toybox Stage Design System (Peak Games / Toon Blast "Juicy UI")
// =====================================================================

// 1. World & Theatrical Stage
val StageMagentaTop = Color(0xFF7A1E46)    // stage gradient top
val StageMagentaDeep = Color(0xFF33103D)   // stage gradient bottom / shadow
val StageCurtainEdge = Color(0xFF19061F)   // theatrical vignette / curtain edges
val StageSpotlightWarm = Color(0xFFFFD166) // warm spotlight glow

// 2. Toy Panels & Surfaces (Deep Violet Plastic, Never Flat)
val ToyVioletPanel = Color(0xFF55277D)     // deep violet toy panel
val ToyVioletPanelDark = Color(0xFF3D165C) // panel rim / shelf
val ToyVioletPanelLight = Color(0xFF7A3EAE)// panel inner highlight
val ToyVioletPill = Color(0xCC3D165C)      // HUD / tray translucent pill

// 3. Wooden Toy Shelves & Floorboards
val ToyWoodFloor = Color(0xFFC07F3A)       // wooden floorboards & shelves
val ToyWoodFloorDark = Color(0xFF7A4A16)   // wood edge planking & dark grain
val ToyWoodHighlight = Color(0xFFE2A05B)   // wood top grain highlight

// 4. Hero Button: Lime Green (One Hero Per Screen: PLAY / NEXT / CLAIM)
val HeroLimeTop = Color(0xFFA6F750)
val HeroLimeBottom = Color(0xFF63C70A)
val HeroLimeShelf = Color(0xFF4A9605)
val HeroLimeText = Color(0xFF153B00)

// 5. Toy Action Colors
val ToyOrangeTop = Color(0xFFFF9D2E)
val ToyOrangeBottom = Color(0xFFF97D0C)
val ToyOrangeShelf = Color(0xFFC45A00)

val ToyGold = Color(0xFFFFC93C)            // medallions, coins, stars
val ToyGoldShelf = Color(0xFFD99A16)
val ToyGoldGlow = Color(0xFFFFEA00)

val ToyRedTop = Color(0xFFFF4D6D)
val ToyRedBottom = Color(0xFFE61C48)
val ToyRedShelf = Color(0xFFA00D2F)

val ToyCyanTop = Color(0xFF38BDF8)
val ToyCyanBottom = Color(0xFF0284C7)
val ToyCyanShelf = Color(0xFF0369A1)

// =====================================================================
// Backward-Compatibility Aliases (Replaces Golden Storybook with Toybox)
// =====================================================================
val StorybookPlumDeep = StageMagentaDeep
val StorybookPlum = StageMagentaTop
val StorybookIndigo = ToyVioletPanelDark
val CreamSurface = ToyVioletPanel           // Dialogs/sheets become rich violet toy panels!
val CreamText = Color(0xFFFFF9F0)          // Primary text on dark
val PlumText = Color(0xFFFFFFFF)           // High-contrast white poster text on violet!
val PlumPill = ToyVioletPill               // Beveled violet pill
val SunsetTop = HeroLimeTop                // Hero button inherits Lime
val SunsetBottom = HeroLimeBottom
val SunsetShelf = HeroLimeShelf
val StoryGold = ToyGold
val StoryGoldShelf = ToyGoldShelf
val StoryMint = Color(0xFF22C55E)
val StoryMintShelf = Color(0xFF15803D)
val StoryBerry = ToyRedBottom
val StoryBerryShelf = ToyRedShelf
val PlumButton = ToyVioletPanel
val PlumButtonShelf = ToyVioletPanelDark
