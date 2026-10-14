package com.example.balloonpop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.PlayerSkill
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily

/**
 * Skill Vault Screen.
 * Exact implementation of the reference design (Image 3):
 * - Top Currency Bar: Gold Coin capsule and Purple Gem capsule
 * - "SKILL VAULT" chiseled golden title with glints & "Rare game-clearing abilities" subtitle
 * - Golden "PRO TIP" card: "Chain Hammer Slide into Chrono Stasis to freeze and clear clustered enemies."
 * - Rich Skill Cards (Hammer Slide, Chrono Stasis, Vortex Magnet, Supernova Blast) with stats, upgrade & equip
 */
@Composable
fun SkillVaultScreen(
    coins: Int,
    gems: Int,
    equippedSkillId: String,
    unlockedSkills: Set<String>,
    skillLevels: Map<String, Int>,
    onEquipSkill: (String) -> Unit,
    onUnlockSkill: (PlayerSkill) -> Unit,
    onUpgradeSkill: (PlayerSkill) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Cosmic dark background with gold dust
    val darkCosmicBg = Brush.verticalGradient(
        listOf(
            Color(0xFF070712),
            Color(0xFF0F0F24),
            Color(0xFF14122C),
            Color(0xFF0A0A16)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkCosmicBg)
            .testTag("skill_vault_screen")
    ) {
        // Ambient golden starry dust
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val sparkles = listOf(
                Offset(w * 0.15f, h * 0.14f) to 3f,
                Offset(w * 0.85f, h * 0.12f) to 4f,
                Offset(w * 0.22f, h * 0.28f) to 2.5f,
                Offset(w * 0.78f, h * 0.32f) to 3.5f,
                Offset(w * 0.90f, h * 0.55f) to 3f,
                Offset(w * 0.10f, h * 0.70f) to 3f,
                Offset(w * 0.82f, h * 0.84f) to 2.5f
            )

            for ((pos, r) in sparkles) {
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.45f),
                    radius = r,
                    center = pos
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar: Back Button & Currency Capsules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onBack() }
                        .testTag("skill_vault_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Currency Capsules
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coin Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xE6141220))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFF78350F))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🪙", fontSize = 16.sp)
                            Text(
                                text = String.format("%,d", coins),
                                fontFamily = Baloo2FontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFEF3C7)
                            )
                        }
                    }

                    // Gem Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xE6141220))
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFFA855F7), Color(0xFF581C87))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "💎", fontSize = 16.sp)
                            Text(
                                text = String.format("%,d", gems),
                                fontFamily = Baloo2FontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF3E8FF)
                            )
                        }
                    }
                }
            }

            // 2. Chiseled Golden Header Lockup
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "SKILL VAULT",
                    fontFamily = Baloo2FontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFDE68A),
                    letterSpacing = 2.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFFBEB),
                                Color(0xFFFFE082),
                                Color(0xFFF59E0B),
                                Color(0xFFD97706)
                            )
                        )
                    )
                )

                Text(
                    text = "Rare game-clearing abilities",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC4B5FD)
                )
            }

            // 3. Golden "PRO TIP" Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33FFD700))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xEE12132C))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFFFFD54F).copy(alpha = 0.8f),
                                Color(0xFFB45309).copy(alpha = 0.4f),
                                Color(0x22FFFFFF)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // Top edge glint highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color(0xFFFFE082), Color.Transparent)
                            )
                        )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Glowing Lightbulb Circle
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0x66F59E0B), Color(0x11F59E0B))
                                )
                            )
                            .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "💡", fontSize = 22.sp)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "PRO TIP",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFDE68A),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Chain Hammer Slide into Chrono Stasis to freeze and clear clustered enemies.",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // 4. Skills List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(GameConstants.AVAILABLE_SKILLS) { skill ->
                    val isUnlocked = unlockedSkills.contains(skill.id)
                    val isEquipped = equippedSkillId == skill.id
                    val currentLvl = skillLevels[skill.id] ?: 1

                    VaultSkillCard(
                        skill = skill,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        currentLevel = currentLvl,
                        userCoins = coins,
                        userGems = gems,
                        onEquip = { onEquipSkill(skill.id) },
                        onUnlock = { onUnlockSkill(skill) },
                        onUpgrade = { onUpgradeSkill(skill) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Skill Card matching Image 3 styling.
 */
@Composable
private fun VaultSkillCard(
    skill: PlayerSkill,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    currentLevel: Int,
    userCoins: Int,
    userGems: Int,
    onEquip: () -> Unit,
    onUnlock: () -> Unit,
    onUpgrade: () -> Unit
) {
    val upgradeCostCoins = (currentLevel + 1) * 300
    val upgradeCostGems = (currentLevel + 1) * 5

    val cardBorder = if (isEquipped) {
        Brush.linearGradient(
            listOf(Color(0xFF34D399), Color(0xFF059669))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0x33A78BFA), Color(0x11FFFFFF))
        )
    }

    val cardBg = if (isEquipped) {
        Color(0xEE0B2418)
    } else {
        Color(0xEE12132C)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = if (isEquipped) Color(0xFF10B981) else Color.Transparent)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                brush = cardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Top Row: Icon, Title & Level, Equip/Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // 3D Glowing Skill Icon Container
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x55F59E0B), Color(0x22F59E0B))
                                )
                            )
                            .border(1.5.dp, Color(0x66FFD54F), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (skill.id) {
                                "skill_hammer_slide" -> "🔨"
                                "skill_slice" -> "⚔️"
                                "skill_freeze" -> "⏳"
                                "skill_vortex" -> "🌀"
                                "skill_supernova" -> "🌟"
                                else -> "⚡"
                            },
                            fontSize = 28.sp
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${skill.name} LVL $currentLevel",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = skill.description,
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Equip / Equipped Status
                if (isEquipped) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF064E3B))
                            .border(1.dp, Color(0xFF34D399), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Equipped",
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "EQUIPPED",
                                fontFamily = NunitoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }

            // Stats Row: +15% range, +10% impact force
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x55000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+${10 + currentLevel * 5}% range",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "+${8 + currentLevel * 4}% impact force",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD166)
                )
                Text(
                    text = "Cooldown: ${(skill.cooldownSeconds - currentLevel * 0.5f).coerceAtLeast(4f)}s",
                    fontFamily = NunitoFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA78BFA)
                )
            }

            // Action Buttons: Equip or Upgrade or Unlock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUnlocked) {
                    // Unlock button
                    val canAfford = userCoins >= skill.unlockCostCoins && userGems >= skill.unlockCostGems
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (canAfford) Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                else Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF1F2937)))
                            )
                            .clickable(enabled = canAfford) { onUnlock() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = "UNLOCK 🪙 ${skill.unlockCostCoins} + 💎 ${skill.unlockCostGems}",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Upgrade button
                        val canUpgrade = userCoins >= upgradeCostCoins && userGems >= upgradeCostGems
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (canUpgrade) Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                    else Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF1F2937)))
                                )
                                .clickable(enabled = canUpgrade) { onUpgrade() }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "UPGRADE (🪙 $upgradeCostCoins 💎 $upgradeCostGems)",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Equip Button (if not already equipped)
                        if (!isEquipped) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))))
                                    .clickable { onEquip() }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = "EQUIP",
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
