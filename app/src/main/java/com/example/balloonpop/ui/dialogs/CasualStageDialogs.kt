package com.example.balloonpop.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.components.*
import kotlinx.coroutines.delay

/**
 * Animated In-Game Stage Intro Ribbon Banner
 */
@Composable
fun StageIntroRibbon(
    level: Int,
    targetPops: Int,
    targetScore: Int,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(level) {
        visible = true
        delay(2200L)
        visible = false
        delay(300L)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp, start = 20.dp, end = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(12.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFBBF24), // Golden Amber
                                Color(0xFFF59E0B),
                                Color(0xFFEA580C)
                            )
                        )
                    )
                    .border(3.dp, Color(0xFFFFFBEB), RoundedCornerShape(22.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MascotCharacter(mood = MascotMood.JOYFUL, size = 52.dp)

                Column {
                    Text(
                        text = "STAGE $level GOAL",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF78350F),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Pop $targetPops Balloons & score $targetScore pts!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Victory Results Dialog (Level Complete with Rainbow Arch, 3 Stars, Cheering Mascot & Next Button)
 */
@Composable
fun VictoryStageDialog(
    level: Int,
    stars: Int,
    score: Int,
    coinsEarned: Int,
    gemsEarned: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 380.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Celebratory Rainbow Arc Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .offset(y = 12.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFEF4444),
                                Color(0xFFF97316),
                                Color(0xFFFACC15),
                                Color(0xFF22C55E),
                                Color(0xFF3B82F6),
                                Color(0xFFA855F7)
                            )
                        )
                    )
                    .border(2.5.dp, Color.White, RoundedCornerShape(24.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌈 LEVEL COMPLETE! 🌈",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            // Amber Wooden Result Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color.Black)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                        )
                    )
                    .border(4.dp, Color(0xFFFEF3C7), RoundedCornerShape(28.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Text(
                    text = "Level $level",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                // Inner Parchment Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(2.dp, Color(0xFFFDE68A), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 3 Large 3D Stars
                    StarsDisplay3D(
                        starsCount = stars,
                        size = 54.dp,
                        animated = true
                    )

                    // You Won Header + Mascot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MascotCharacter(mood = MascotMood.JOYFUL, size = 46.dp)

                        Column {
                            Text(
                                text = "You won!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "Your score: ${String.format("%,d", score)} points",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    // Reward Badges (Coins & Gems)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+$coinsEarned 🪙 Coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "+$gemsEarned 💎 Gems",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7)
                        )
                    }

                    // Next Button (Juicy 3D Green)
                    JellyButton(
                        text = "NEXT ▶",
                        onClick = onNextLevel,
                        height = 54.dp,
                        fontSize = 20,
                        testTag = "victory_next_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Secondary Map & Replay buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        JellyButton(
                            text = "🗺️ Map",
                            onClick = onMap,
                            height = 42.dp,
                            fontSize = 13,
                            colorVariant = JellyColorVariant.ORANGE,
                            testTag = "victory_map_button",
                            modifier = Modifier.weight(1f)
                        )

                        JellyButton(
                            text = "🔄 Replay",
                            onClick = onReplay,
                            height = 42.dp,
                            fontSize = 13,
                            colorVariant = JellyColorVariant.BLUE,
                            testTag = "victory_replay_button",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Failed Stage Dialog (Out of Moves / Time with Sad Mascot and Retry / Play On Options)
 */
@Composable
fun FailedStageDialog(
    level: Int,
    missingPops: Int,
    userCoins: Int,
    onPlayOn: () -> Unit,
    onRetry: () -> Unit,
    onMap: () -> Unit
) {
    AmberDialogFrame(
        title = "Level $level failed!",
        onClose = onMap
    ) {
        // Mascot showing sad emotion
        MascotCharacter(mood = MascotMood.SAD, size = 68.dp)

        // Missing Goal Announcement
        Text(
            text = "You are missing $missingPops targets!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF991B1B),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Keep going and pop them all to clear the stage!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF78350F),
            textAlign = TextAlign.Center
        )

        // Play On (+15s Time & +1 Life) Button
        JellyButton(
            text = "PLAY ON (+15s ⏱️)",
            onClick = onPlayOn,
            height = 52.dp,
            fontSize = 16,
            colorVariant = JellyColorVariant.GREEN,
            testTag = "failed_play_on_button",
            modifier = Modifier.fillMaxWidth()
        )

        // Retry Button
        JellyButton(
            text = "RETRY 🔄",
            onClick = onRetry,
            height = 46.dp,
            fontSize = 15,
            colorVariant = JellyColorVariant.ORANGE,
            testTag = "failed_retry_button",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
