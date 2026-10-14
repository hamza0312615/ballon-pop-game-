package com.example.balloonpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LeaderboardPlayer(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val avatarBgColor: Color,
    val score: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

@Composable
fun RanksScreen(
    currentScore: Int,
    currentLevel: Int,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val players = listOf(
        LeaderboardPlayer(1, "Sophia", "👑", Color(0xFFFFD700), 54200, 98),
        LeaderboardPlayer(2, "Lucas", "⚡", Color(0xFFC0C0C0), 48900, 92),
        LeaderboardPlayer(3, "Oliver", "🎯", Color(0xFFCD7F32), 43400, 85),
        LeaderboardPlayer(4, "Emma", "🌸", Color(0xFFEC4899), 39100, 78),
        LeaderboardPlayer(5, "Noah", "🚀", Color(0xFF38BDF8), 34800, 71),
        LeaderboardPlayer(6, "Maya", "🍭", Color(0xFFA855F7), 31200, 68),
        LeaderboardPlayer(7, "Ethan", "🔥", Color(0xFFEF4444), 27500, 62),
        LeaderboardPlayer(8, "Ava", "⭐", Color(0xFFFBBF24), 24100, 56),
        LeaderboardPlayer(9, "Leo", "🎈", Color(0xFF10B981), 21300, 50),
        LeaderboardPlayer(10, "Zoe", "✨", Color(0xFF6366F1), 18900, 46),
        LeaderboardPlayer(42, "You", "😎", Color(0xFFFF2A75), currentScore.coerceAtLeast(12450), currentLevel, isCurrentUser = true)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color(0x66FFD700))
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF0C0C16))
            .border(1.5.dp, Color(0xFF28283E), RoundedCornerShape(26.dp))
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🏆", fontSize = 26.sp)
                Column {
                    Text(
                        text = "POP CHAMPIONSHIPS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "👑 SKY DIAMOND LEAGUE • Season 12",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E2E))
                    .border(1.dp, Color(0xFF383852), CircleShape)
                    .clickable { onClose() }
                    .testTag("ranks_close_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Countdown ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2E1065))
                .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏳ Season ends in: 2d 14h",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Top 10 win 500 💎",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFDE047)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Player's Own Card
        val userPlayer = players.first { it.isCurrentUser }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFFFD700))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF3B2306), Color(0xFF201302))
                    )
                )
                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "#${userPlayer.rank}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFDE68A)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = userPlayer.avatarEmoji, fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = "You (Level ${userPlayer.level})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Top 5% of all Balloon Masters",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE68A)
                    )
                }
            }

            Text(
                text = "${String.format("%,d", userPlayer.score)} 🎈",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFFD700)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Leaderboard List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(players.filter { !it.isCurrentUser }) { _, player ->
                LeaderboardRow(player)
            }
        }
    }
}

@Composable
private fun LeaderboardRow(player: LeaderboardPlayer) {
    val rankBadgeColor = when (player.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xFF475569)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141422))
            .border(1.dp, Color(0xFF28283E), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(rankBadgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.rank}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (player.rank <= 3) Color(0xFF1A1A24) else Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(player.avatarBgColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = player.avatarEmoji, fontSize = 16.sp)
            }

            Column {
                Text(
                    text = player.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Level ${player.level}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Text(
            text = "${String.format("%,d", player.score)} 🎈",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFBBF24)
        )
    }
}
