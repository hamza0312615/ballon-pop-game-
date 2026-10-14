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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.QuestCategory
import com.example.balloonpop.model.QuestItem
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily

/**
 * Quests Screen.
 * Exact match to Image 4:
 * - Title: QUESTS + Complete challenges, earn rewards
 * - Navy glass cards with double gold borders & subtle filigree corners
 * - Circular glowing icon frames (Balloon, Map Pin, Trophy)
 * - Category Badges: DAILY, WEEKLY, ACHIEVEMENT
 * - Gradient progress bars with numeric progress & reward pills
 * - Golden CLAIM button for ready quests
 */
@Composable
fun QuestsScreen(
    lifetimePops: Int,
    lifetimeMaxCombo: Int,
    completedLevelsCount: Int,
    wheelSpins: Int,
    claimedQuests: Set<String>,
    onClaimQuest: (QuestItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF070B1C),
            Color(0xFF0D1432),
            Color(0xFF11183C),
            Color(0xFF080C20)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .testTag("quests_screen")
    ) {
        // Ambient background stars and golden nebula trails
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Subtle golden nebula glow
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0x22F59E0B), Color.Transparent), center = Offset(w * 0.85f, h * 0.15f), radius = w * 0.6f)
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(Color(0x1838BDF8), Color.Transparent), center = Offset(w * 0.15f, h * 0.65f), radius = w * 0.5f)
            )
            // Tiny star dust specks
            listOf(
                Offset(w * 0.1f, h * 0.12f),
                Offset(w * 0.9f, h * 0.22f),
                Offset(w * 0.25f, h * 0.45f),
                Offset(w * 0.8f, h * 0.75f),
                Offset(w * 0.15f, h * 0.85f)
            ).forEach { pos ->
                drawCircle(Color.White.copy(alpha = 0.6f), radius = 1.8f, center = pos)
            }
        }

        // Top Back Button
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x33FFFFFF))
                .border(1.dp, Color(0x44FFFFFF), CircleShape)
                .clickable { onClose() }
                .testTag("quests_back_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(42.dp))

            // Title Block
            Text(
                text = "QUESTS",
                fontFamily = Baloo2FontFamily,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Complete challenges, earn rewards",
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF93C5FD)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quests List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(GameConstants.DEFAULT_QUESTS) { quest ->
                    val currentProgress = when (quest.id) {
                        "quest_pop_50" -> lifetimePops
                        "quest_pop_200" -> lifetimePops
                        "quest_combo_5" -> lifetimeMaxCombo
                        "quest_combo_10" -> lifetimeMaxCombo
                        "quest_spin_wheel" -> wheelSpins
                        "quest_stage_5" -> completedLevelsCount
                        "quest_stage_10" -> completedLevelsCount
                        "quest_stage_clear_3star" -> completedLevelsCount
                        "quest_stars_15" -> completedLevelsCount * 3
                        else -> lifetimePops
                    }.coerceAtMost(quest.target)

                    val isCompleted = currentProgress >= quest.target
                    val isClaimed = claimedQuests.contains(quest.id)

                    QuestItemCard(
                        quest = quest,
                        currentProgress = currentProgress,
                        isCompleted = isCompleted,
                        isClaimed = isClaimed,
                        onClaim = { onClaimQuest(quest) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestItemCard(
    quest: QuestItem,
    currentProgress: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val progressFraction = (currentProgress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f)

    val (badgeText, badgeColor, badgeTextColor) = when (quest.category) {
        QuestCategory.DAILY -> Triple("DAILY", Color(0xFF382E1E), Color(0xFFFFD54F))
        QuestCategory.WEEKLY -> Triple("WEEKLY", Color(0xFF3D231E), Color(0xFFFB923C))
        else -> Triple("ACHIEVEMENT", Color(0xFF382A18), Color(0xFFFFC93C))
    }

    val progressBarBrush = when (quest.category) {
        QuestCategory.DAILY -> Brush.horizontalGradient(listOf(Color(0xFFFF5D8F), Color(0xFFFB923C), Color(0xFFFFD54F)))
        QuestCategory.WEEKLY -> Brush.horizontalGradient(listOf(Color(0xFF2DD4BF), Color(0xFF38BDF8), Color(0xFFFFD54F)))
        else -> Brush.horizontalGradient(listOf(Color(0xFFFFD54F), Color(0xFFF59E0B)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = Color(0x66FFD700))
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xF50D132A))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x99FFD54F), Color(0x33FFD54F), Color(0x11FFFFFF), Color(0x66FFD54F))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Circular Icon + Title + Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Glowing Icon Frame
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape, spotColor = Color(0x88FFD54F))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x4438BDF8), Color(0x221E1B4B))
                            )
                        )
                        .border(1.5.dp, Color(0x66FFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = quest.icon, fontSize = 26.sp)
                }

                // Title + Description
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (quest.category == QuestCategory.DAILY) "Daily Quest" else if (quest.category == QuestCategory.WEEKLY) "Weekly Quest" else "Achievement",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Category Pill Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor)
                                .border(1.dp, badgeTextColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontFamily = NunitoFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = badgeTextColor,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = quest.title,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xCCFFFFFF)
                    )
                }
            }

            // Progress Bar Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0x331E293B))
                    .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(progressBarBrush)
                )
            }

            // Footer Row: Numeric Stats & Rewards Pill / Claim Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Numeric Progress Text (e.g. 320/500)
                Text(
                    text = "$currentProgress/${quest.target}",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xCCFFFFFF)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Reward Pill (e.g. 🪙 200 coins)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, Color(0x55FFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (quest.rewardCoins > 0) "🪙 ${quest.rewardCoins} coins" else "💎 ${quest.rewardGems} gems",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }

                    // Action Button (CLAIM, CLAIMED, or In-Progress)
                    if (isClaimed) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3310B981))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "CLAIMED ✓",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    } else if (isCompleted) {
                        val claimInteraction = remember { MutableInteractionSource() }
                        val isClaimPressed by claimInteraction.collectIsPressedAsState()
                        val claimScale by animateFloatAsState(targetValue = if (isClaimPressed) 0.94f else 1.0f, label = "claim_btn")

                        Box(
                            modifier = Modifier
                                .height(38.dp)
                                .scale(claimScale)
                                .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFFFFD700))
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFFFFBEB), Color(0xFFFFD54F), Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                )
                                .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                                .clickable(interactionSource = claimInteraction, indication = null) { onClaim() }
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CLAIM",
                                fontFamily = Baloo2FontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF5B2E00),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
