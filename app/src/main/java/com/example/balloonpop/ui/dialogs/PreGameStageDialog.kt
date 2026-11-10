package com.example.balloonpop.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.StoryGold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import com.example.balloonpop.ui.components.ShopItemArt
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.StageRecipes
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.NunitoFontFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stage Intro Card.
 * Exact match to Image 2:
 * - STAGE {level} in bold white Baloo 2
 * - {zoneName} in soft candy pink
 * - 3 Gold Outline Stars
 * - 3 Stat Tiles: TARGET {pops} pops, TIME {timeLimit}s, MAX MISS {maxMisses}
 * - "CHOOSE BOOSTERS"
 * - 3 Round Booster Orbs (+15s Time, Freeze, Rainbow)
 * - Juicy 3D Golden PLAY Button
 */
@Composable
fun PreGameStageDialog(
    level: Int,
    starsEarned: Int,
    targetScore: Int,
    targetPops: Int,
    timeLimit: Int = 15,
    maxMissesAllowed: Int = 4,
    userCoins: Int,
    // Pass 8: persisted campaign win streak — at 2+ the intro announces
    // the +5s streak fuel this stage is about to receive.
    winStreak: Int = 0,
    onStartLevel: (selectedBoosters: Set<String>) -> Unit,
    onClose: () -> Unit
) {
    var selectedBoosters by remember { mutableStateOf(setOf<String>()) }

    // Dynamic Zone derivation: 100% accurate for stages 1 to 200
    val currentZone = remember(level) {
        GameConstants.WORLD_ZONES.find { level in it.startLevel..it.endLevel }
    }
    val currentZoneName = currentZone?.name ?: "Twilight Grove"

    val infiniteTransition = rememberInfiniteTransition(label = "play_pulse_intro")
    val playPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_pulse"
    )

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .shadow(28.dp, RoundedCornerShape(26.dp), spotColor = Color(0x66FFD700))
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF0120B22),
                            Color(0xF2180F30),
                            Color(0xF50E071A)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0x88FFD54F),
                            Color(0x33F59E0B),
                            Color(0x22FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .testTag("stage_intro_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header: STAGE {level} OF 200 & {zoneName}
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "STAGE $level OF 200",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentZoneName.uppercase(),
                        fontFamily = Baloo2FontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF85A2),
                        letterSpacing = 0.8.sp
                    )
                }

                // 3 Outlined / Filled Stars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { idx ->
                        val isFilled = idx < starsEarned
                        Canvas(modifier = Modifier.size(38.dp)) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val outerR = size.width * 0.46f
                            val innerR = outerR * 0.45f
                            val starPath = Path()
                            for (i in 0 until 10) {
                                val r = if (i % 2 == 0) outerR else innerR
                                val angle = (i * 36 - 90) * (Math.PI / 180.0)
                                val x = cx + (r * cos(angle)).toFloat()
                                val y = cy + (r * sin(angle)).toFloat()
                                if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                            }
                            starPath.close()

                            if (isFilled) {
                                drawPath(
                                    path = starPath,
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFFEA00), Color(0xFFF59E0B))
                                    )
                                )
                            }
                            drawPath(
                                path = starPath,
                                color = Color(0xFFFFD54F),
                                style = Stroke(width = 2.5f)
                            )
                        }
                    }
                }

                // Pass 8 & 9: intro info lines — Stage Objective Variety banner,
                // HARD tag for the zone's spike stage (position 9), win-streak fuel
                // announcement, and the one-line coach text on debut stage.
                val recipe = remember(level) { if (level > 0) StageRecipes.getRecipe(level) else null }
                val coachLine = remember(level) { StageRecipes.coachLineFor(level) }
                val objectiveLine = remember(recipe) { recipe?.let { StageRecipes.objectiveIntroLineFor(it) } }
                if (StageRecipes.isHardStage(level) || winStreak >= 2 || coachLine != null || objectiveLine != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (objectiveLine != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF261938))
                                    .border(1.dp, StoryGold, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = objectiveLine,
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD166),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (StageRecipes.isHardStage(level)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF7F1D1D))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "⚠ HARD STAGE",
                                    fontFamily = Baloo2FontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFCA5A5),
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                        if (winStreak >= 2) {
                            Text(
                                text = "🔥 Win streak x$winStreak: +5s bonus!",
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFC93C),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (coachLine != null) {
                            Text(
                                text = coachLine,
                                fontFamily = NunitoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7ED3FC),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 3 Stat Boxes Row (Target, Time, Max Miss)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Box 1: TARGET
                    IntroStatBox(
                        iconVector = Icons.Default.GpsFixed,
                        iconColor = Color(0xFFEF4444),
                        label = "TARGET",
                        value = "$targetPops pops",
                        modifier = Modifier.weight(1f)
                    )

                    // Box 2: TIME
                    IntroStatBox(
                        iconVector = Icons.Default.AvTimer,
                        iconColor = Color(0xFF38BDF8),
                        label = "TIME",
                        value = "${timeLimit}s",
                        modifier = Modifier.weight(1f)
                    )

                    // Box 3: MAX MISS
                    IntroStatBox(
                        iconVector = Icons.Default.Warning,
                        iconColor = Color(0xFFFBBF24),
                        label = "MAX MISS",
                        value = "$maxMissesAllowed",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Choose Boosters Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CHOOSE BOOSTERS",
                        fontFamily = Baloo2FontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xCCFFFFFF),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IntroBoosterOrb(
                            boosterId = "booster_freeze",
                            label = "+15s Time",
                            gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                            glowColor = Color(0xFF38BDF8),
                            isSelected = selectedBoosters.contains("booster_time"),
                            onClick = {
                                selectedBoosters = if (selectedBoosters.contains("booster_time")) {
                                    selectedBoosters - "booster_time"
                                } else {
                                    selectedBoosters + "booster_time"
                                }
                            }
                        )

                        IntroBoosterOrb(
                            boosterId = "booster_freeze",
                            label = "Freeze",
                            gradient = listOf(Color(0xFF2DD4BF), Color(0xFF0D9488)),
                            glowColor = Color(0xFF2DD4BF),
                            isSelected = selectedBoosters.contains("booster_freeze"),
                            onClick = {
                                selectedBoosters = if (selectedBoosters.contains("booster_freeze")) {
                                    selectedBoosters - "booster_freeze"
                                } else {
                                    selectedBoosters + "booster_freeze"
                                }
                            }
                        )

                        IntroBoosterOrb(
                            boosterId = "booster_color_bomb",
                            label = "Rainbow",
                            gradient = listOf(Color(0xFFFF5D8F), Color(0xFFC084FC), Color(0xFFFBBF24)),
                            glowColor = Color(0xFFFF5D8F),
                            isSelected = selectedBoosters.contains("booster_rainbow"),
                            onClick = {
                                selectedBoosters = if (selectedBoosters.contains("booster_rainbow")) {
                                    selectedBoosters - "booster_rainbow"
                                } else {
                                    selectedBoosters + "booster_rainbow"
                                }
                            }
                        )
                    }
                }

                // Primary Golden PLAY Button
                val playInteraction = remember { MutableInteractionSource() }
                val isPlayPressed by playInteraction.collectIsPressedAsState()
                val playScale by animateFloatAsState(
                    targetValue = if (isPlayPressed) 0.95f else 1.0f,
                    label = "play_press"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .scale(playPulse * playScale)
                        .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFFFFC93C))
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFFE082),
                                    Color(0xFFFFC93C),
                                    Color(0xFFF59E0B),
                                    Color(0xFFD97706)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFFFE082), Color(0xFFB45309))),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            interactionSource = playInteraction,
                            indication = null
                        ) {
                            onStartLevel(selectedBoosters)
                        }
                        .testTag("pregame_play_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Top Crescent Shine
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(0.85f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0x80FFFFFF), Color(0x00FFFFFF))
                                )
                            )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "▶",
                            fontSize = 20.sp,
                            color = Color(0xFF5B2E00)
                        )
                        Text(
                            text = "PLAY",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5B2E00),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroStatBox(
    iconVector: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x441F1A3A))
            .border(1.dp, Color(0x44FFD700), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label,
                    fontFamily = NunitoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xAAFFFFFF)
                )
            }
            Text(
                text = value,
                fontFamily = Baloo2FontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun IntroBoosterOrb(
    boosterId: String,
    label: String,
    gradient: List<Color>,
    glowColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(if (isSelected) 12.dp else 4.dp, CircleShape, spotColor = glowColor)
                .clip(CircleShape)
                .background(Brush.radialGradient(gradient))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) Color.White else Color(0x44FFFFFF),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            ShopItemArt(itemId = boosterId, modifier = Modifier.size(30.dp))
        }
        Text(
            text = label,
            fontFamily = NunitoFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color(0xFFFFD54F) else Color.White
        )
    }
}
