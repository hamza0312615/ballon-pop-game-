package com.example.balloonpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.balloonpop.model.*
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily

enum class ShopTab(val label: String, val icon: String) {
    THEMES("Themes", "🎨"),
    HAMMERS("Hammers", "🔨"),
    FRAMES("Frames", "🖼️"),
    BLADES("Blades", "⚔️"),
    FLAMES("Flames", "🔥"),
    DIAMONDS("Gems", "💎")
}

/**
 * Vault & Shop Screen.
 * Exact implementation of the reference design (Image 2 from second prompt):
 * - Top Currency HUD: 3D Gold Coin Capsule & 3D Blue Diamond Gem Capsule
 * - Green Watch Ad Banner: "▶ WATCH AD FOR FREE REWARDS 🪙 +250 coins"
 * - Title: "VAULT & SHOP" with subtle cosmic background
 * - 6 Category Tabs in a 3-column x 2-row square grid with glowing golden selected state
 * - 2-column Item Card Grid with large artwork, description, EQUIPPED badge, and dual currency purchase buttons
 */
@Composable
fun ShopScreen(
    coins: Int,
    gems: Int,
    equippedHammerId: String,
    equippedFrameId: String,
    equippedBladeId: String,
    equippedFlameId: String = "flame_ember",
    equippedThemeId: String = "theme_sunset_twilight",
    unlockedHammers: Set<String>,
    unlockedFrames: Set<String>,
    unlockedBlades: Set<String>,
    unlockedFlames: Set<String> = setOf("flame_ember"),
    unlockedThemes: Set<String> = setOf("theme_sunset_twilight", "theme_obsidian"),
    onBuyHammer: (HammerSkin) -> Unit,
    onBuyFrame: (PlayFrame) -> Unit,
    onBuyBlade: (BladeItem) -> Unit,
    onBuyFlame: (FlameItem) -> Unit = {},
    onBuyTheme: (ThemeConfig) -> Unit = {},
    onClaimDiamondPack: (DiamondStorePack) -> Unit,
    onWatchAdForCoins: () -> Unit = {},
    bannerMessage: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ShopTab.THEMES) }
    var selectedIapPack by remember { mutableStateOf<DiamondStorePack?>(null) }

    val cosmicBg = Brush.verticalGradient(
        listOf(
            Color(0xFF070918),
            Color(0xFF0F122E),
            Color(0xFF141238),
            Color(0xFF0C0C1F)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(cosmicBg)
            .testTag("shop_screen")
    ) {
        // Ambient golden starry dust and subtle corner arcs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Corner geometric gold accent lines
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, Color(0x44FFD700))),
                start = Offset(0f, 60f),
                end = Offset(w * 0.45f, 0f),
                strokeWidth = 1f
            )
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color(0x44FFD700), Color.Transparent)),
                start = Offset(w * 0.55f, 0f),
                end = Offset(w, 80f),
                strokeWidth = 1f
            )

            // Sparkles
            val sparkles = listOf(
                Offset(w * 0.12f, h * 0.15f) to 2.5f,
                Offset(w * 0.88f, h * 0.12f) to 3f,
                Offset(w * 0.20f, h * 0.35f) to 2f,
                Offset(w * 0.92f, h * 0.38f) to 2.5f,
                Offset(w * 0.10f, h * 0.65f) to 2.5f,
                Offset(w * 0.85f, h * 0.85f) to 3f
            )
            for ((pos, r) in sparkles) {
                drawCircle(Color(0xFFFFD700).copy(alpha = 0.5f), radius = r, center = pos)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Bar: Currency Capsules & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xE615132B))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFF78350F))
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🪙", fontSize = 16.sp)
                            Text(
                                text = String.format("%,d", coins),
                                fontFamily = Baloo2FontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7)
                            )
                        }
                    }

                    // Gems Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xE615132B))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
                                ),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "💎", fontSize = 16.sp)
                            Text(
                                text = String.format("%,d", gems),
                                fontFamily = Baloo2FontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE0F2FE)
                            )
                        }
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onClose() }
                        .testTag("shop_close_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. Watch Ad Reward Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF10B981))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))
                        )
                    )
                    .border(1.5.dp, Color(0xFF34D399), RoundedCornerShape(22.dp))
                    .clickable { onWatchAdForCoins() }
                    .testTag("shop_watch_ad_banner"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "WATCH AD FOR FREE REWARDS",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(text = "🪙 +250 coins", fontFamily = Baloo2FontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFEF3C7))
                }
            }

            // 3. Screen Title: VAULT & SHOP
            Text(
                text = "VAULT & SHOP",
                fontFamily = Baloo2FontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Category Grid (3 Columns x 2 Rows)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Themes, Hammers, Frames
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCategoryTile(
                        tab = ShopTab.THEMES,
                        isSelected = selectedTab == ShopTab.THEMES,
                        onClick = { selectedTab = ShopTab.THEMES },
                        modifier = Modifier.weight(1f)
                    )
                    ShopCategoryTile(
                        tab = ShopTab.HAMMERS,
                        isSelected = selectedTab == ShopTab.HAMMERS,
                        onClick = { selectedTab = ShopTab.HAMMERS },
                        modifier = Modifier.weight(1f)
                    )
                    ShopCategoryTile(
                        tab = ShopTab.FRAMES,
                        isSelected = selectedTab == ShopTab.FRAMES,
                        onClick = { selectedTab = ShopTab.FRAMES },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Blades, Flames, Gems
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCategoryTile(
                        tab = ShopTab.BLADES,
                        isSelected = selectedTab == ShopTab.BLADES,
                        onClick = { selectedTab = ShopTab.BLADES },
                        modifier = Modifier.weight(1f)
                    )
                    ShopCategoryTile(
                        tab = ShopTab.FLAMES,
                        isSelected = selectedTab == ShopTab.FLAMES,
                        onClick = { selectedTab = ShopTab.FLAMES },
                        modifier = Modifier.weight(1f)
                    )
                    ShopCategoryTile(
                        tab = ShopTab.DIAMONDS,
                        isSelected = selectedTab == ShopTab.DIAMONDS,
                        onClick = { selectedTab = ShopTab.DIAMONDS },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Notification Banner
            AnimatedVisibility(
                visible = bannerMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0C2B40))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bannerMessage ?: "",
                        fontFamily = NunitoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7DD3FC)
                    )
                }
            }

            // 5. Items Grid (2 Columns)
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    ShopTab.THEMES -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.SHOP_THEMES) { theme ->
                                val isUnlocked = unlockedThemes.contains(theme.id)
                                val isEquipped = equippedThemeId == theme.id

                                CosmeticItemCard(
                                    title = theme.name,
                                    description = "Theme visual atmosphere",
                                    icon = "🎨",
                                    iconBgColor = theme.gradientColors.firstOrNull() ?: Color(0xFF0F172A),
                                    priceCoins = if (theme.currency == CurrencyType.COINS) theme.cost else 0,
                                    priceGems = if (theme.currency == CurrencyType.GEMS) theme.cost else 0,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onAction = { onBuyTheme(theme) }
                                )
                            }
                        }
                    }

                    ShopTab.HAMMERS -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.SHOP_HAMMERS) { hammer ->
                                val isUnlocked = unlockedHammers.contains(hammer.id)
                                val isEquipped = equippedHammerId == hammer.id

                                CosmeticItemCard(
                                    title = hammer.name,
                                    description = hammer.description,
                                    icon = "🔨",
                                    iconBgColor = Color(0xFFF59E0B),
                                    priceCoins = if (hammer.currency == CurrencyType.COINS) hammer.cost else 0,
                                    priceGems = if (hammer.currency == CurrencyType.GEMS) hammer.cost else 0,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onAction = { onBuyHammer(hammer) }
                                )
                            }
                        }
                    }

                    ShopTab.FRAMES -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.SHOP_FRAMES) { frame ->
                                val isUnlocked = unlockedFrames.contains(frame.id)
                                val isEquipped = equippedFrameId == frame.id

                                CosmeticItemCard(
                                    title = frame.name,
                                    description = frame.description,
                                    icon = "🖼️",
                                    iconBgColor = Color(0xFF38BDF8),
                                    priceCoins = if (frame.currency == CurrencyType.COINS) frame.cost else 0,
                                    priceGems = if (frame.currency == CurrencyType.GEMS) frame.cost else 0,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onAction = { onBuyFrame(frame) }
                                )
                            }
                        }
                    }

                    ShopTab.BLADES -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.SHOP_BLADES) { blade ->
                                val isUnlocked = unlockedBlades.contains(blade.id)
                                val isEquipped = equippedBladeId == blade.id

                                CosmeticItemCard(
                                    title = blade.name,
                                    description = blade.description,
                                    icon = "⚔️",
                                    iconBgColor = Color(0xFF34D399),
                                    priceCoins = if (blade.currency == CurrencyType.COINS) blade.cost else 0,
                                    priceGems = if (blade.currency == CurrencyType.GEMS) blade.cost else 0,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onAction = { onBuyBlade(blade) }
                                )
                            }
                        }
                    }

                    ShopTab.FLAMES -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.SHOP_FLAMES) { flame ->
                                val isUnlocked = unlockedFlames.contains(flame.id)
                                val isEquipped = equippedFlameId == flame.id

                                CosmeticItemCard(
                                    title = flame.name,
                                    description = flame.description,
                                    icon = "🔥",
                                    iconBgColor = Color(0xFFF97316),
                                    priceCoins = if (flame.currency == CurrencyType.COINS) flame.cost else 0,
                                    priceGems = if (flame.currency == CurrencyType.GEMS) flame.cost else 0,
                                    isUnlocked = isUnlocked,
                                    isEquipped = isEquipped,
                                    onAction = { onBuyFlame(flame) }
                                )
                            }
                        }
                    }

                    ShopTab.DIAMONDS -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(GameConstants.DIAMOND_STORE_PACKS) { pack ->
                                DiamondStoreCard(
                                    pack = pack,
                                    onClaim = {
                                        if (pack.isAdReward || pack.id == "coin_convert_pack") {
                                            onClaimDiamondPack(pack)
                                        } else {
                                            selectedIapPack = pack
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3x2 Square Category Tile with glowing gold highlight when selected.
 */
@Composable
private fun ShopCategoryTile(
    tab: ShopTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tile_press"
    )

    val tileBg = if (isSelected) {
        Brush.verticalGradient(
            listOf(Color(0xFF5B4310), Color(0xFF382905), Color(0xFF1E1502))
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFF16152B), Color(0xFF0F0E20))
        )
    }

    val tileBorder = if (isSelected) {
        Brush.linearGradient(
            listOf(Color(0xFFFFE082), Color(0xFFFFD54F), Color(0xFFB45309))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(72.dp)
            .shadow(
                elevation = if (isSelected) 10.dp else 3.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isSelected) Color(0xFFFFD700) else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(tileBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = tileBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag("shop_tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = tab.icon, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                fontFamily = Baloo2FontFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) Color(0xFFFEF08A) else Color(0xFFCBD5E1)
            )
        }
    }
}

/**
 * Cosmetic Item Card matching Image 2 (Neon Pop & Candy Dream reference).
 */
@Composable
private fun CosmeticItemCard(
    title: String,
    description: String,
    icon: String,
    iconBgColor: Color,
    priceCoins: Int,
    priceGems: Int,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xEE12132C))
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                brush = Brush.verticalGradient(
                    if (isEquipped) listOf(Color(0xFF34D399), Color(0xFF059669))
                    else listOf(Color(0x33A78BFA), Color(0x11FFFFFF))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Artwork Preview Hub
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(iconBgColor.copy(alpha = 0.40f), Color(0x11000000))
                        )
                    )
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 44.sp)

                // Top Right EQUIPPED Badge
                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF064E3B))
                            .border(1.dp, Color(0xFF34D399), RoundedCornerShape(10.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Equipped", tint = Color(0xFF34D399), modifier = Modifier.size(11.dp))
                            Text(
                                text = "EQUIPPED",
                                fontFamily = NunitoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }

            // Title & Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = Baloo2FontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // Bottom Buttons: Dual Currency or Equip
            if (isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isEquipped) Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF064E3B)))
                            else Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        )
                        .clickable(enabled = !isEquipped) { onAction() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEquipped) "ACTIVE" else "EQUIP",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Coin Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xDD382405))
                            .border(1.dp, Color(0xFFB45309), RoundedCornerShape(10.dp))
                            .clickable { onAction() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪙 $priceCoins",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEF3C7)
                        )
                    }

                    // Gem Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xDD0C2B40))
                            .border(1.dp, Color(0xFF0284C7), RoundedCornerShape(10.dp))
                            .clickable { onAction() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💎 $priceGems",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBAE6FD)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diamond Store Card.
 */
@Composable
private fun DiamondStoreCard(
    pack: DiamondStorePack,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xEE12132C))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x330284C7)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = pack.bonusTag ?: "💎", fontSize = 42.sp)
            }

            Text(
                text = pack.title,
                fontFamily = Baloo2FontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "+${pack.diamondsAmount} Gems",
                fontFamily = NunitoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF7DD3FC)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                        )
                    )
                    .clickable { onClaim() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pack.priceTag,
                    fontFamily = Baloo2FontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
