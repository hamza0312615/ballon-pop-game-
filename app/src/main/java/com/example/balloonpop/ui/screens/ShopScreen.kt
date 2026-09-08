package com.example.balloonpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.*
import com.example.balloonpop.ui.theme.*

enum class ShopTab(val label: String, val icon: String) {
    HAMMERS("Hammers", "🔨"),
    FRAMES("Frames", "✨"),
    DIAMONDS("Get 💎", "💎"),
    BLADES("Blades", "⚔️")
}

@Composable
fun ShopScreen(
    coins: Int,
    gems: Int,
    equippedHammerId: String,
    equippedFrameId: String,
    equippedBladeId: String,
    unlockedHammers: Set<String>,
    unlockedFrames: Set<String>,
    unlockedBlades: Set<String>,
    onBuyHammer: (HammerSkin) -> Unit,
    onBuyFrame: (PlayFrame) -> Unit,
    onBuyBlade: (BladeItem) -> Unit,
    onClaimDiamondPack: (DiamondStorePack) -> Unit,
    bannerMessage: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ShopTab.HAMMERS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFA120E2B))
            .border(2.dp, Brush.linearGradient(listOf(NeonPink, NeonCyan)), RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        // Modal Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🛒", fontSize = 24.sp)
                Text(
                    text = "VAULT & SHOP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Wallet badges
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x802A1648))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "$coins 🪙", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldCoin)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x802A1648))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "$gems 💎", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GemBlue)
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x40FFFFFF))
                        .clickable { onClose() }
                        .testTag("close_shop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x9000F0FF))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bannerMessage ?: "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ShopTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val bg = if (isSelected) {
                    when (tab) {
                        ShopTab.HAMMERS -> NeonPink
                        ShopTab.FRAMES -> NeonPurple
                        ShopTab.DIAMONDS -> NeonCyan
                        ShopTab.BLADES -> NeonYellow
                    }
                } else Color(0x601A1638)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .clickable { selectedTab = tab }
                        .testTag("shop_tab_${tab.name.lowercase()}")
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = tab.icon, fontSize = 16.sp)
                        Text(
                            text = tab.label,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isSelected && tab == ShopTab.BLADES) Color.Black else TextWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTab) {
            ShopTab.HAMMERS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(GameConstants.SHOP_HAMMERS) { hammer ->
                        val isUnlocked = unlockedHammers.contains(hammer.id)
                        val isEquipped = equippedHammerId == hammer.id

                        HammerCard(
                            hammer = hammer,
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
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(GameConstants.SHOP_FRAMES) { frame ->
                        val isUnlocked = unlockedFrames.contains(frame.id)
                        val isEquipped = equippedFrameId == frame.id

                        FrameCard(
                            frame = frame,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            onAction = { onBuyFrame(frame) }
                        )
                    }
                }
            }

            ShopTab.DIAMONDS -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(GameConstants.DIAMOND_STORE_PACKS) { pack ->
                        DiamondPackCard(
                            pack = pack,
                            onClaim = { onClaimDiamondPack(pack) }
                        )
                    }
                }
            }

            ShopTab.BLADES -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(GameConstants.SHOP_BLADES) { blade ->
                        val isUnlocked = unlockedBlades.contains(blade.id)
                        val isEquipped = equippedBladeId == blade.id

                        BladeCard(
                            blade = blade,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            onAction = { onBuyBlade(blade) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HammerCard(
    hammer: HammerSkin,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x801A1638))
            .border(
                1.5.dp,
                if (isEquipped) NeonPink else Color(0x40FFFFFF),
                RoundedCornerShape(16.dp)
            )
            .clickable { onAction() }
            .testTag("hammer_item_${hammer.id}")
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Tier Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x60000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = "TIER ${hammer.tier}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonYellow)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = hammer.icon, fontSize = 34.sp)

            Text(
                text = hammer.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextWhite,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = hammer.description,
                fontSize = 10.sp,
                color = TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isEquipped -> NeonPink
                            isUnlocked -> NeonMint
                            hammer.currency == CurrencyType.GEMS -> GemBlue
                            else -> GoldCoin
                        }
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isEquipped -> "EQUIPPED"
                        isUnlocked -> "EQUIP"
                        hammer.currency == CurrencyType.GEMS -> "${hammer.cost} 💎"
                        else -> "${hammer.cost} 🪙"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (isEquipped) TextWhite else Color.Black
                )
            }
        }
    }
}

@Composable
private fun FrameCard(
    frame: PlayFrame,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x801A1638))
            .border(
                1.5.dp,
                if (isEquipped) NeonCyan else Color(0x40FFFFFF),
                RoundedCornerShape(16.dp)
            )
            .clickable { onAction() }
            .testTag("frame_item_${frame.id}")
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = frame.icon, fontSize = 34.sp)

            Text(
                text = frame.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextWhite,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = frame.description,
                fontSize = 10.sp,
                color = TextDim,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isEquipped -> NeonCyan
                            isUnlocked -> NeonMint
                            frame.currency == CurrencyType.GEMS -> GemBlue
                            else -> GoldCoin
                        }
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isEquipped -> "EQUIPPED"
                        isUnlocked -> "EQUIP"
                        frame.currency == CurrencyType.GEMS -> "${frame.cost} 💎"
                        else -> "${frame.cost} 🪙"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = if (isEquipped) Color.Black else Color.Black
                )
            }
        }
    }
}

@Composable
private fun DiamondPackCard(
    pack: DiamondStorePack,
    onClaim: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x801A1638))
            .border(1.5.dp, GemBlue, RoundedCornerShape(16.dp))
            .clickable { onClaim() }
            .testTag("diamond_pack_${pack.id}")
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Bonus Tag
            if (pack.bonusTag != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (pack.isAdReward) NeonMint else NeonPink)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pack.bonusTag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = if (pack.isAdReward) "📺" else "💎", fontSize = 34.sp)

            Text(
                text = "+${pack.diamondsAmount} DIAMONDS",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = GemBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = pack.title,
                fontSize = 11.sp,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(listOf(NeonCyan, GemBlue)))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pack.priceTag,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun BladeCard(
    blade: BladeItem,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x801A1638))
            .border(
                1.5.dp,
                if (isEquipped) NeonYellow else Color(0x40FFFFFF),
                RoundedCornerShape(16.dp)
            )
            .clickable { onAction() }
            .testTag("blade_item_${blade.id}")
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = blade.icon, fontSize = 34.sp)

            Text(
                text = blade.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextWhite,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        when {
                            isEquipped -> NeonYellow
                            isUnlocked -> NeonMint
                            blade.currency == CurrencyType.GEMS -> GemBlue
                            else -> GoldCoin
                        }
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        isEquipped -> "EQUIPPED"
                        isUnlocked -> "EQUIP"
                        blade.currency == CurrencyType.GEMS -> "${blade.cost} 💎"
                        else -> "${blade.cost} 🪙"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color.Black
                )
            }
        }
    }
}
