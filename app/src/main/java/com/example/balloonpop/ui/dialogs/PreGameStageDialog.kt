package com.example.balloonpop.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.components.AmberDialogFrame
import com.example.balloonpop.ui.components.JellyButton
import com.example.balloonpop.ui.components.MascotCharacter
import com.example.balloonpop.ui.components.MascotMood
import com.example.balloonpop.ui.components.StarsDisplay3D

@Composable
fun PreGameStageDialog(
    level: Int,
    starsEarned: Int,
    targetScore: Int,
    targetPops: Int,
    userCoins: Int,
    onStartLevel: (selectedBoosters: Set<String>) -> Unit,
    onClose: () -> Unit
) {
    var selectedBoosters by remember { mutableStateOf(setOf<String>()) }

    AmberDialogFrame(
        title = "Level $level",
        onClose = onClose
    ) {
        // 3 Big 3D Stars Preview
        StarsDisplay3D(
            starsCount = starsEarned,
            size = 52.dp,
            animated = starsEarned > 0
        )

        // Goal Box with cute Mascot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFFFEF08A), Color(0xFFFDE047))
                    )
                )
                .border(2.dp, Color(0xFFFACC15), RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MascotCharacter(mood = MascotMood.HAPPY, size = 48.dp)

            Column {
                Text(
                    text = "STAGE GOAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "Pop $targetPops Balloons & score $targetScore pts",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF78350F)
                )
            }
        }

        // Select Boosters Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Select Boosters",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF92400E),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BoosterSlot(
                    id = "booster_extra_time",
                    name = "+15s Time",
                    icon = "⏱️",
                    isSelected = selectedBoosters.contains("booster_extra_time"),
                    onToggle = {
                        selectedBoosters = if (selectedBoosters.contains("booster_extra_time")) {
                            selectedBoosters - "booster_extra_time"
                        } else {
                            selectedBoosters + "booster_extra_time"
                        }
                    }
                )

                BoosterSlot(
                    id = "booster_freeze",
                    name = "Freeze Bomb",
                    icon = "❄️",
                    isSelected = selectedBoosters.contains("booster_freeze"),
                    onToggle = {
                        selectedBoosters = if (selectedBoosters.contains("booster_freeze")) {
                            selectedBoosters - "booster_freeze"
                        } else {
                            selectedBoosters + "booster_freeze"
                        }
                    }
                )

                BoosterSlot(
                    id = "booster_rainbow",
                    name = "Rainbow Pop",
                    icon = "🌈",
                    isSelected = selectedBoosters.contains("booster_rainbow"),
                    onToggle = {
                        selectedBoosters = if (selectedBoosters.contains("booster_rainbow")) {
                            selectedBoosters - "booster_rainbow"
                        } else {
                            selectedBoosters + "booster_rainbow"
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Juicy Green Play Button
        JellyButton(
            text = "PLAY ▶",
            onClick = { onStartLevel(selectedBoosters) },
            height = 56.dp,
            fontSize = 20,
            testTag = "pregame_play_button",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BoosterSlot(
    id: String,
    name: String,
    icon: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(elevation = if (isSelected) 4.dp else 1.dp, shape = RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSelected) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF86EFAC), Color(0xFF22C55E))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                        )
                    }
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.5.dp,
                    color = if (isSelected) Color(0xFF15803D) else Color(0xFFD97706),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onToggle() }
                .testTag("booster_slot_$id"),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }

        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFF15803D) else Color(0xFF92400E),
            textAlign = TextAlign.Center
        )
    }
}
