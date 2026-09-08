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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.balloonpop.model.QuestItem
import com.example.balloonpop.ui.theme.*

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xEE120E2B))
            .border(2.dp, Brush.linearGradient(listOf(NeonMint, NeonCyan)), RoundedCornerShape(24.dp))
            .padding(16.dp)
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
                Text(text = "📜", fontSize = 24.sp)
                Text(
                    text = "QUESTS & REWARDS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = TextWhite,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x60FFFFFF))
                    .clickable { onClose() }
                    .testTag("close_quests_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quests List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(GameConstants.DEFAULT_QUESTS) { quest ->
                val currentProgress = when (quest.id) {
                    "quest_pop_50", "quest_pop_200" -> lifetimePops
                    "quest_combo_5", "quest_combo_10" -> lifetimeMaxCombo
                    "quest_stage_5", "quest_stage_10" -> completedLevelsCount
                    "quest_spin_wheel" -> wheelSpins
                    else -> 0
                }
                val isCompleted = currentProgress >= quest.target
                val isClaimed = claimedQuests.contains(quest.id)

                QuestRowCard(
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

@Composable
private fun QuestRowCard(
    quest: QuestItem,
    currentProgress: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val progressFraction = (currentProgress.toFloat() / quest.target).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xCC1A1638))
            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Progress: ${currentProgress.coerceAtMost(quest.target)} / ${quest.target}",
                fontSize = 12.sp,
                color = TextDim
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) NeonMint else NeonCyan,
                trackColor = Color(0x40FFFFFF)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Claim Button
        val rewardText = buildString {
            if (quest.rewardCoins > 0) append("${quest.rewardCoins} 🪙 ")
            if (quest.rewardGems > 0) append("${quest.rewardGems} 💎")
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isClaimed) Color(0x40FFFFFF)
                    else if (isCompleted) NeonMint
                    else Color(0x30FFFFFF)
                )
                .clickable(enabled = isCompleted && !isClaimed) { onClaim() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isClaimed) "CLAIMED ✅" else if (isCompleted) "CLAIM $rewardText" else rewardText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (isCompleted && !isClaimed) Color.Black else TextWhite
            )
        }
    }
}
