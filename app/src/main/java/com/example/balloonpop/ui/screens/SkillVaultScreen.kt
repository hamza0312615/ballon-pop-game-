package com.example.balloonpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.PlayerSkill
import com.example.balloonpop.ui.theme.*

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E0E3D), Color(0xFF13092A), Color(0xFF230B40))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x40FFFFFF))
                        .clickable { onBack() }
                        .testTag("back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column {
                    Text(
                        text = "⚡ SKILL BOX & VAULT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Unlock abilities for smooth finger slicing & combos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            // Currency Wallet
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x40000000))
                        .border(1.dp, GoldCoin, RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🪙 $coins",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldCoin
                    )
                }

                // Gems
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x40000000))
                        .border(1.dp, DiamondBlue, RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "💎 $gems",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = DiamondBlue
                    )
                }
            }
        }

        // Info Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x5500E5FF), Color(0x55FF2A75))
                    )
                )
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "💡", fontSize = 24.sp)
                Column {
                    Text(
                        text = "PRO TIP: SMOOTH FINGER GLIDE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonYellow
                    )
                    Text(
                        text = "Equip 'Laser Stream Slice' to continuously glide your finger across screen without tapping, popping multiple balloons instantly!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

        // Skills List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(GameConstants.AVAILABLE_SKILLS) { skill ->
                val isUnlocked = unlockedSkills.contains(skill.id)
                val isEquipped = equippedSkillId == skill.id
                val currentLvl = skillLevels[skill.id] ?: 1

                SkillCard(
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

@Composable
private fun SkillCard(
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

    val cardBg = if (isEquipped) {
        Modifier.background(
            Brush.linearGradient(
                listOf(Color(0xFF381A66), Color(0xFF220C47))
            )
        )
    } else {
        Modifier.background(Color(0x35FFFFFF))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .then(cardBg)
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) skill.accentColor else Color(0x30FFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Skill Icon Box
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                listOf(skill.accentColor, Color(0xFF120826))
                            )
                        )
                        .border(1.5.dp, skill.accentColor, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = skill.icon, fontSize = 26.sp)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = skill.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        if (isUnlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(skill.accentColor.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LVL $currentLevel",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = skill.accentColor
                                )
                            }
                        }
                    }

                    Text(
                        text = skill.description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isUnlocked) {
                // Unlock Button
                val canAfford = userCoins >= skill.unlockCostCoins && userGems >= skill.unlockCostGems
                val unlockBg = if (canAfford) {
                    Modifier.background(Brush.horizontalGradient(listOf(NeonYellow, NeonPink)))
                } else {
                    Modifier.background(Color(0x50FFFFFF))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .then(unlockBg)
                        .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                        .clickable(enabled = canAfford) { onUnlock() }
                        .testTag("unlock_skill_${skill.id}")
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (skill.unlockCostGems > 0) "UNLOCK: ${skill.unlockCostCoins}🪙 + ${skill.unlockCostGems}💎" else "UNLOCK: ${skill.unlockCostCoins}🪙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (canAfford) Color.Black else Color.White
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Upgrade Button if below max level
                    if (currentLevel < skill.maxLevel) {
                        val canUpgrade = userCoins >= upgradeCostCoins && userGems >= upgradeCostGems
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x40FFFFFF))
                                .border(1.dp, NeonCyan, RoundedCornerShape(14.dp))
                                .clickable(enabled = canUpgrade) { onUpgrade() }
                                .testTag("upgrade_skill_${skill.id}")
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "UPGRADE (${upgradeCostCoins}🪙)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (canUpgrade) NeonCyan else Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }

                    // Equip Button
                    val equipBg = if (isEquipped) {
                        Modifier.background(Color(0xFF00E676))
                    } else {
                        Modifier.background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF7C4DFF))))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .then(equipBg)
                            .clickable(enabled = !isEquipped) { onEquip() }
                            .testTag("equip_skill_${skill.id}")
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = if (isEquipped) "EQUIPPED ✓" else "EQUIP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
