package com.example.balloonpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameMode
import com.example.balloonpop.ui.theme.*

@Composable
fun EndlessModeScreen(
    onStartMode: (GameMode) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }

    val endlessModes = listOf(
        GameMode.CLASSIC,
        GameMode.TIME_RUSH,
        GameMode.ARCADE,
        GameMode.ZEN
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xEE120E2B))
            .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonPink)), RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
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
                    Text(text = "⚡", fontSize = 24.sp)
                    Text(
                        text = "ENDLESS MODES",
                        fontSize = 20.sp,
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
                        .testTag("close_endless_button"),
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

            Spacer(modifier = Modifier.height(20.dp))

            // Mode Selector Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                endlessModes.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val borderBrush = if (isSelected) {
                        Brush.horizontalGradient(listOf(NeonPink, NeonYellow))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x30FFFFFF), Color(0x10FFFFFF)))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xCC2A1B4D) else Color(0x801A1638))
                            .border(if (isSelected) 2.dp else 1.dp, borderBrush, RoundedCornerShape(16.dp))
                            .clickable { selectedMode = mode }
                            .testTag("endless_mode_${mode.name.lowercase()}")
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = mode.icon, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) NeonYellow else TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mode.desc,
                                fontSize = 12.sp,
                                color = TextDim
                            )
                        }
                    }
                }
            }
        }

        // Start Game Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.horizontalGradient(listOf(NeonPink, NeonPurple)))
                .border(1.5.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                .clickable { onStartMode(selectedMode) }
                .testTag("start_endless_game_button")
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "START GAME 🚀",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = TextWhite,
                letterSpacing = 1.sp
            )
        }
    }
}
