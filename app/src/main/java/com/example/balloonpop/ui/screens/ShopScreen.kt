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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.*
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.CosmosIndigo
import com.example.balloonpop.ui.theme.CosmosNavy
import com.example.balloonpop.ui.theme.CosmosNavyDeep
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.DiamondBlue
import com.example.balloonpop.ui.theme.GoldCoin
import com.example.balloonpop.ui.theme.NunitoFontFamily

enum class ShopTab(val label: String, val icon: String) {
    BOOSTERS("Boosters", "⚡"),
    THEMES("Themes", "🎨"),
    HAMMERS("Hammers", "🔨"),
    FRAMES("Frames", "🖼️"),
    BLADES("Blades", "⚔️"),
    FLAMES("Flames", "🔥"),
    DIAMONDS("Gems", "💎")
}

// Pass 4 uniform shop grid: EVERY purchasable card (boosters, cosmetics and
// gem packs) uses these exact sizes, so a grid row can never mix card heights,
// art sizes or button heights, and no text can push a card taller.
private val ShopCardHeight = 260.dp
private val ShopCardArtHeight = 72.dp
private val ShopCardTitleHeight = 36.dp
private val ShopCardSubtitleHeight = 26.dp
private val ShopCardPricePillHeight = 22.dp
private val ShopBuyButtonHeight = 48.dp

/**
 * Vault & Shop Screen.
 * - Top Currency HUD: 3D Gold Coin Capsule & 3D Blue Diamond Gem Capsule
 * - Green Watch Ad Banner: "▶ WATCH AD FOR FREE REWARDS 🪙 +250 coins"
 * - Title: "VAULT & SHOP" with subtle cosmic background
 * - Category Tiles in a uniform 3-column grid with glowing golden selected state
 * - 2-column uniform Item Card Grid: fixed-height art, 2-line ellipsised name,
 *   one price pill and exactly one 48.dp action button per card.
 * - Boosters tab: the 5 [InGameBooster]s are buyable with coins via
 *   [BoosterPrices.COIN_PRICE] and the existing buyBooster ViewModel path.
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
    boosterCounts: Map<String, Int> = emptyMap(),
    onBuyHammer: (HammerSkin) -> Unit,
    onBuyFrame: (PlayFrame) -> Unit,
    onBuyBlade: (BladeItem) -> Unit,
    onBuyFlame: (FlameItem) -> Unit = {},
    onBuyTheme: (ThemeConfig) -> Unit = {},
    onBuyBooster: (boosterId: String, withGems: Boolean) -> Unit = { _, _ -> },
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
            CosmosNavyDeep,
            CosmosNavy,
            CosmosIndigo,
            CosmosNavyDeep
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
                .padding(horizontal = DesignTokens.ScreenPadding, vertical = DesignTokens.Space16),
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12)
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
                                color = Color(0xFFFEF3C7),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                color = Color(0xFFE0F2FE),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(DesignTokens.IconButtonSize)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = DesignTokens.Space12)
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
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "🪙 +250 coins",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFEF3C7),
                        maxLines = 1
                    )
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Category Grid (3 uniform columns; 7 tabs = 3 + 3 + 1)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: Boosters, Themes, Hammers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCategoryTile(
                        tab = ShopTab.BOOSTERS,
                        isSelected = selectedTab == ShopTab.BOOSTERS,
                        onClick = { selectedTab = ShopTab.BOOSTERS },
                        modifier = Modifier.weight(1f)
                    )
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
                }

                // Row 2: Frames, Blades, Flames
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCategoryTile(
                        tab = ShopTab.FRAMES,
                        isSelected = selectedTab == ShopTab.FRAMES,
                        onClick = { selectedTab = ShopTab.FRAMES },
                        modifier = Modifier.weight(1f)
                    )
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
                }

                // Row 3: Gems (kept the same tile width as every other tile)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ShopCategoryTile(
                        tab = ShopTab.DIAMONDS,
                        isSelected = selectedTab == ShopTab.DIAMONDS,
                        onClick = { selectedTab = ShopTab.DIAMONDS },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f))
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
                        color = Color(0xFF7DD3FC),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 5. Items Grid (2 uniform columns for every tab)
            Box(modifier = Modifier.weight(1f)) {
                key(selectedTab) {
                    when (selectedTab) {
                        ShopTab.BOOSTERS -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(InGameBooster.entries, key = { it.id }) { booster ->
                                    BoosterItemCard(
                                        booster = booster,
                                        ownedCount = boosterCounts[booster.id] ?: 0,
                                        onBuy = { onBuyBooster(booster.id, false) }
                                    )
                                }
                            }
                        }

                        ShopTab.THEMES -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.SHOP_THEMES, key = { it.id }) { theme ->
                                    val isUnlocked = unlockedThemes.contains(theme.id)
                                    val isEquipped = equippedThemeId == theme.id

                                    CosmeticItemCard(
                                        title = theme.name,
                                        description = "Theme visual atmosphere",
                                        icon = "🎨",
                                        iconBgColor = theme.gradientColors.firstOrNull() ?: Color(0xFF0F172A),
                                        previewBrush = Brush.verticalGradient(theme.gradientColors),
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
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.SHOP_HAMMERS, key = { it.id }) { hammer ->
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
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.SHOP_FRAMES, key = { it.id }) { frame ->
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
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.SHOP_BLADES, key = { it.id }) { blade ->
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
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.SHOP_FLAMES, key = { it.id }) { flame ->
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
                                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                verticalArrangement = Arrangement.spacedBy(DesignTokens.Space12),
                                contentPadding = PaddingValues(bottom = DesignTokens.Space16),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(GameConstants.DIAMOND_STORE_PACKS, key = { it.id }) { pack ->
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
}

/**
 * Uniform Category Tile with glowing gold highlight when selected.
 * Every tile is the same height and clips its label, so the tab grid can
 * never change size when a tab is added or selected.
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
                shape = DesignTokens.ShapeMedium,
                spotColor = if (isSelected) Color(0xFFFFD700) else Color.Transparent
            )
            .clip(DesignTokens.ShapeMedium)
            .background(tileBg)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                brush = tileBorder,
                shape = DesignTokens.ShapeMedium
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = DesignTokens.Space8)
        ) {
            Text(text = tab.icon, fontSize = 22.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tab.label,
                fontFamily = Baloo2FontFamily,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) Color(0xFFFEF08A) else Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * The ONE purchasable card used by every shop section (Pass 4 uniform grid).
 * Fixed total height with fixed-height slots — art / title / subtitle /
 * price pill / action button — so every card in every tab is the same size,
 * every button lines up, and long text ellipsises instead of overflowing.
 */
@Composable
private fun UniformShopCard(
    title: String,
    subtitle: String,
    artType: String,
    artColor: Color,
    priceText: String,
    priceIsGem: Boolean,
    buttonText: String,
    buttonGradient: Brush,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewBrush: Brush? = null,
    badgeText: String? = null,
    borderBrush: Brush = Brush.verticalGradient(
        listOf(Color(0x33A78BFA), Color(0x11FFFFFF))
    ),
    buttonEnabled: Boolean = true,
    buttonTestTag: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ShopCardHeight)
            .shadow(6.dp, DesignTokens.ShapeMedium)
            .clip(DesignTokens.ShapeMedium)
            .background(Color(0xEE12132C))
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = DesignTokens.ShapeMedium
            )
            .padding(DesignTokens.Space12)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
        ) {
            // Fixed-height art area, with an optional badge (owned count /
            // equipped / pack bonus) pinned to its top-end corner.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShopCardArtHeight)
                    .clip(RoundedCornerShape(DesignTokens.RadiusSmall))
                    .background(
                        previewBrush ?: Brush.radialGradient(
                            listOf(artColor.copy(alpha = 0.40f), Color(0x11000000))
                        )
                    )
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(DesignTokens.RadiusSmall)),
                contentAlignment = Alignment.Center
            ) {
                com.example.balloonpop.ui.components.ShopItemArt(
                    type = artType,
                    title = title,
                    primaryColor = artColor,
                    modifier = Modifier.size(52.dp)
                )
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(DesignTokens.Space4)
                            .clip(DesignTokens.ShapePill)
                            .background(Color(0xCC05070F))
                            .border(1.dp, Color(0x55FFFFFF), DesignTokens.ShapePill)
                            .padding(horizontal = DesignTokens.Space8, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = Baloo2FontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Item name: fixed 2-line slot, always ellipsised.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShopCardTitleHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontFamily = Baloo2FontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            // Description / amount: fixed 2-line slot, always ellipsised.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShopCardSubtitleHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = subtitle,
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }

            // Exactly one price pill, with its currency icon.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ShopCardPricePillHeight),
                contentAlignment = Alignment.Center
            ) {
                ShopPricePill(text = priceText, isGem = priceIsGem)
            }

            // Exactly one action button, the same 48.dp height on every card.
            ShopActionButton(
                text = buttonText,
                gradient = buttonGradient,
                enabled = buttonEnabled,
                testTag = buttonTestTag,
                onClick = onButtonClick
            )
        }
    }
}

@Composable
private fun ShopPricePill(
    text: String,
    isGem: Boolean,
    modifier: Modifier = Modifier
) {
    val accent = if (isGem) DiamondBlue else GoldCoin
    Box(
        modifier = modifier
            .height(ShopCardPricePillHeight)
            .clip(DesignTokens.ShapePill)
            .background(Color(0xCC05070F))
            .border(1.dp, accent.copy(alpha = 0.65f), DesignTokens.ShapePill)
            .padding(horizontal = DesignTokens.Space12),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Baloo2FontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGem) Color(0xFFE0F2FE) else Color(0xFFFEF3C7),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShopActionButton(
    text: String,
    gradient: Brush,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val taggedModifier = if (testTag != null) modifier.testTag(testTag) else modifier
    Box(
        modifier = taggedModifier
            .fillMaxWidth()
            .height(ShopBuyButtonHeight)
            .clip(RoundedCornerShape(DesignTokens.RadiusSmall))
            .background(gradient)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space4),
            modifier = Modifier.padding(horizontal = DesignTokens.Space8)
        ) {
            if (text == "EQUIPPED") {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                fontFamily = Baloo2FontFamily,
                fontSize = DesignTokens.SizeBody,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Booster card: owned count badge, booster description, the single coin
 * price from [BoosterPrices] and one BUY button that buys with coins.
 * Accent colours match the gameplay booster dock.
 */
@Composable
private fun BoosterItemCard(
    booster: InGameBooster,
    ownedCount: Int,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when (booster) {
        InGameBooster.THUNDER_STRIKE -> Color(0xFF38BDF8)
        InGameBooster.COLOR_BOMB -> Color(0xFFFF007F)
        InGameBooster.HAMMER_SMASH -> Color(0xFFFBBF24)
        InGameBooster.FREEZE_BLIZZARD -> Color(0xFF00E5FF)
        InGameBooster.TORNADO_VORTEX -> Color(0xFF34D399)
    }

    UniformShopCard(
        title = booster.displayName,
        subtitle = booster.description,
        artType = booster.id,
        artColor = accent,
        badgeText = "Owned ×$ownedCount",
        priceText = "🪙 ${BoosterPrices.COIN_PRICE}",
        priceIsGem = false,
        buttonText = "BUY",
        buttonGradient = DesignTokens.CtaGradient,
        buttonTestTag = "shop_buy_${booster.id}",
        onButtonClick = onBuy,
        borderBrush = Brush.verticalGradient(
            listOf(accent.copy(alpha = 0.55f), Color(0x11FFFFFF))
        ),
        modifier = modifier
    )
}

/**
 * Cosmetic item card on the shared uniform grid. Purchase logic is
 * unchanged: the single action button calls the same onAction the screen
 * received (buy / equip for that item).
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
    previewBrush: Brush? = null,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val usesGems = priceGems > 0
    UniformShopCard(
        title = title,
        subtitle = description,
        artType = "$title $description".lowercase(),
        artColor = iconBgColor,
        previewBrush = previewBrush,
        badgeText = if (isEquipped) "EQUIPPED" else null,
        priceText = when {
            isUnlocked -> "OWNED"
            usesGems -> "💎 $priceGems"
            else -> "🪙 $priceCoins"
        },
        priceIsGem = usesGems,
        buttonText = when {
            isEquipped -> "EQUIPPED"
            isUnlocked -> "EQUIP"
            else -> "BUY"
        },
        buttonGradient = when {
            isEquipped -> Brush.linearGradient(listOf(Color(0xFF064E3B), Color(0xFF064E3B)))
            isUnlocked -> Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
            usesGems -> Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
            else -> DesignTokens.GoldGradient
        },
        buttonEnabled = !isEquipped,
        onButtonClick = onAction,
        borderBrush = Brush.verticalGradient(
            if (isEquipped) listOf(Color(0xFF34D399), Color(0xFF059669))
            else listOf(Color(0x33A78BFA), Color(0x11FFFFFF))
        ),
        modifier = modifier
    )
}

/**
 * Diamond / gem pack card on the shared uniform grid. The pack's priceTag
 * is the price pill; the single action button keeps the existing claim
 * behaviour (handled by the caller).
 */
@Composable
private fun DiamondStoreCard(
    pack: DiamondStorePack,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subtitle = if (pack.bonusTag != null) {
        "+${pack.diamondsAmount} Gems • ${pack.bonusTag}"
    } else {
        "+${pack.diamondsAmount} Gems"
    }

    UniformShopCard(
        title = pack.title,
        subtitle = subtitle,
        artType = "diamond pack",
        artColor = DiamondBlue,
        badgeText = pack.bonusTag,
        priceText = pack.priceTag,
        priceIsGem = true,
        buttonText = when {
            pack.isAdReward -> "WATCH AD"
            pack.id == "coin_convert_pack" -> "EXCHANGE"
            else -> "BUY"
        },
        buttonGradient = Brush.horizontalGradient(
            listOf(Color(0xFF0284C7), Color(0xFF0369A1))
        ),
        onButtonClick = onClaim,
        borderBrush = Brush.verticalGradient(
            listOf(Color(0xFF38BDF8), Color(0xFF0369A1))
        ),
        modifier = modifier
    )
}
