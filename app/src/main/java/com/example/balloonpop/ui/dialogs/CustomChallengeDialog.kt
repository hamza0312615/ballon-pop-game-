package com.example.balloonpop.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.balloonpop.ui.components.JellyColorVariant
import com.example.balloonpop.ui.components.MascotCharacter
import com.example.balloonpop.ui.components.MascotMood

/**
 * Custom Challenge Creator Dialog.
 * Allows the player to input their own custom level parameters:
 * 1. Time duration in seconds (e.g. 15s)
 * 2. Target balloons to pop (e.g. 23 balloons)
 * 3. Max missed balloons allowed before automatic Game Over (e.g. 4 misses)
 */
@Composable
fun CustomChallengeDialog(
    initialTimeSeconds: Int = 15,
    initialTargetPops: Int = 23,
    initialMaxMisses: Int = 4,
    onStartChallenge: (timeSeconds: Int, targetPops: Int, maxMisses: Int) -> Unit,
    onClose: () -> Unit
) {
    var timeSeconds by remember { mutableIntStateOf(initialTimeSeconds) }
    var targetPops by remember { mutableIntStateOf(initialTargetPops) }
    var maxMisses by remember { mutableIntStateOf(initialMaxMisses) }

    AmberDialogFrame(
        title = "⚡ CUSTOM LEVEL LAB ⚡",
        frameGradient = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0F172A)),
        borderColor = Color(0xFF818CF8),
        onClose = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mascot Intro Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF18182E))
                    .border(1.dp, Color(0xFF3730A3), RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MascotCharacter(mood = MascotMood.HAPPY, size = 42.dp)
                Column {
                    Text(
                        text = "DESIGN YOUR CHALLENGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFDE047)
                    )
                    Text(
                        text = "Customize time, target balloons & miss limits!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Quick Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(
                    label = "23 in 15s (4 miss)",
                    isSelected = timeSeconds == 15 && targetPops == 23 && maxMisses == 4,
                    onClick = {
                        timeSeconds = 15
                        targetPops = 23
                        maxMisses = 4
                    },
                    modifier = Modifier.weight(1f)
                )
                PresetChip(
                    label = "15 in 10s (3 miss)",
                    isSelected = timeSeconds == 10 && targetPops == 15 && maxMisses == 3,
                    onClick = {
                        timeSeconds = 10
                        targetPops = 15
                        maxMisses = 3
                    },
                    modifier = Modifier.weight(1f)
                )
                PresetChip(
                    label = "35 in 25s (5 miss)",
                    isSelected = timeSeconds == 25 && targetPops == 35 && maxMisses == 5,
                    onClick = {
                        timeSeconds = 25
                        targetPops = 35
                        maxMisses = 5
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // 1. Time Limit Selector (Seconds)
            ParameterAdjuster(
                icon = "⏱️",
                title = "TIME LIMIT",
                unit = "Seconds",
                value = timeSeconds,
                onDecrease = { timeSeconds = (timeSeconds - 5).coerceAtLeast(5) },
                onIncrease = { timeSeconds = (timeSeconds + 5).coerceAtMost(120) },
                accentColor = Color(0xFF38BDF8)
            )

            // 2. Target Balloons to Pop
            ParameterAdjuster(
                icon = "🎈",
                title = "TARGET BALLOONS",
                unit = "Balloons to Pop",
                value = targetPops,
                onDecrease = { targetPops = (targetPops - 1).coerceAtLeast(5) },
                onIncrease = { targetPops = (targetPops + 1).coerceAtMost(100) },
                accentColor = Color(0xFFFBBF24)
            )

            // 3. Max Missed Balloons Allowed
            ParameterAdjuster(
                icon = "💨",
                title = "MAX MISSED BALLOONS",
                unit = "Allowed Misses",
                value = maxMisses,
                onDecrease = { maxMisses = (maxMisses - 1).coerceAtLeast(1) },
                onIncrease = { maxMisses = (maxMisses + 1).coerceAtMost(20) },
                accentColor = Color(0xFFF87171)
            )

            // Dynamic Scenario Summary & Game Over Warning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF231018))
                    .border(1.dp, Color(0xFFDC2626), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠️ SCENARIO: Pop $targetPops balloons in ${timeSeconds}s.\nIf you miss more than $maxMisses balloons, GAME OVER!",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp
                )
            }

            // Start Challenge Action
            JellyButton(
                text = "START CUSTOM CHALLENGE ▶",
                onClick = { onStartChallenge(timeSeconds, targetPops, maxMisses) },
                height = 48.dp,
                fontSize = 14,
                colorVariant = JellyColorVariant.GREEN,
                testTag = "start_custom_challenge_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ParameterAdjuster(
    icon: String,
    title: String,
    unit: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF141424))
            .border(1.2.dp, Color(0xFF282842), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = icon, fontSize = 20.sp)
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Stepper Controls (- / Value / +)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF24243A))
                    .border(1.dp, Color(0xFF475569), CircleShape)
                    .clickable { onDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "−", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Box(
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F0F1B))
                    .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$value",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF24243A))
                    .border(1.dp, Color(0xFF475569), CircleShape)
                    .clickable { onIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF3730A3) else Color(0xFF17172A))
            .border(
                1.dp,
                if (isSelected) Color(0xFF818CF8) else Color(0xFF334155),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}
