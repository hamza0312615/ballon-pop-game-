package com.example.balloonpop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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

data class TutorialStepData(
    val stepIndex: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val targetAreaLabel: String,
    val arrowDirection: ArrowDirection,
    val mascotMood: MascotMood = MascotMood.JOYFUL
)

enum class ArrowDirection {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

@Composable
fun TutorialOverlay(
    currentStep: Int,
    totalSteps: Int = 4,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit
) {
    val steps = remember {
        listOf(
            TutorialStepData(
                stepIndex = 0,
                title = "Welcome, Balloon Popper! 🎈",
                subtitle = "STAGE GOAL",
                description = "Your mission is to pop the target number of balloons before time expires. Tap or swipe to slice through them!",
                targetAreaLabel = "🎯 Focus on Balloon Target",
                arrowDirection = ArrowDirection.TOP_LEFT,
                mascotMood = MascotMood.JOYFUL
            ),
            TutorialStepData(
                stepIndex = 1,
                title = "Beat The Clock! ⏱️",
                subtitle = "STAGE TIMER",
                description = "Keep your eye on the countdown clock. Build fast combos to earn higher star ratings and bonus coins!",
                targetAreaLabel = "⏱️ Watch Countdown Clock",
                arrowDirection = ArrowDirection.TOP_RIGHT,
                mascotMood = MascotMood.HAPPY
            ),
            TutorialStepData(
                stepIndex = 2,
                title = "Tactical Boosters! ⚡",
                subtitle = "POWER ITEMS",
                description = "Tap Thunder Zap to defuse bombs safely, Color Bomb to blast matching balloons, or Freeze to slow time!",
                targetAreaLabel = "⚡ Use Tactical Boosters",
                arrowDirection = ArrowDirection.BOTTOM_LEFT,
                mascotMood = MascotMood.JOYFUL
            ),
            TutorialStepData(
                stepIndex = 3,
                title = "Hero Blade Skill! ⚔️",
                subtitle = "SPECIAL POWER",
                description = "Popping balloons charges your skill energy gauge. When it's full, unleash your Hero Skill to wipe the screen!",
                targetAreaLabel = "⚔️ Tap Hero Skill Button",
                arrowDirection = ArrowDirection.BOTTOM_RIGHT,
                mascotMood = MascotMood.HAPPY
            )
        )
    }

    val stepData = steps.getOrElse(currentStep.coerceIn(0, steps.size - 1)) { steps.first() }

    // Floating bounce animation for arrow
    val infiniteTransition = rememberInfiniteTransition(label = "arrow_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Pulse glow animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC060A18))
            .clickable(onClick = onNext) // Tap outside also advances
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Target indicator arrows anchored based on step direction
        when (stepData.arrowDirection) {
            ArrowDirection.TOP_LEFT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 70.dp, start = 32.dp)
                        .offset(y = bounceOffset.dp)
                ) {
                    TargetPointerBadge(
                        label = "⬆ POP TARGET",
                        color = Color(0xFF06D6A0),
                        pulseScale = pulseScale
                    )
                }
            }
            ArrowDirection.TOP_RIGHT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 70.dp, end = 32.dp)
                        .offset(y = bounceOffset.dp)
                ) {
                    TargetPointerBadge(
                        label = "⏱ TIME LIMIT ⬆",
                        color = Color(0xFFFFB703),
                        pulseScale = pulseScale
                    )
                }
            }
            ArrowDirection.BOTTOM_LEFT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 110.dp, start = 36.dp)
                        .offset(y = (-bounceOffset).dp)
                ) {
                    TargetPointerBadge(
                        label = "⬇ BOOSTERS DOCK",
                        color = Color(0xFF00F0FF),
                        pulseScale = pulseScale
                    )
                }
            }
            ArrowDirection.BOTTOM_RIGHT -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 110.dp, end = 36.dp)
                        .offset(y = (-bounceOffset).dp)
                ) {
                    TargetPointerBadge(
                        label = "HERO SKILL ⬇",
                        color = Color(0xFFFF2A75),
                        pulseScale = pulseScale
                    )
                }
            }
        }

        // Central Tutorial Card (Prevent tap propagation to outer box)
        Box(
            modifier = Modifier
                .clickable(enabled = false) {}
                .fillMaxWidth(0.92f)
                .widthIn(max = 390.dp)
                .shadow(24.dp, RoundedCornerShape(26.dp), spotColor = Color.Black)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E1435), Color(0xFF110B22))
                    )
                )
                .border(2.5.dp, Color(0xFFFFB703), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar: Step tag & Skip button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFB703))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "TUTORIAL • ${currentStep + 1}/$totalSteps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD166),
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Skip Tutorial ✕",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onSkip)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("tutorial_skip_button")
                    )
                }

                // Mascot & Title
                AnimatedContent(
                    targetState = stepData,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tutorial_content"
                ) { data ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MascotCharacter(mood = data.mascotMood, size = 64.dp)

                        Text(
                            text = data.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = data.description,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                    }
                }

                // Dots Progress Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        Box(
                            modifier = Modifier
                                .size(if (i == currentStep) 10.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == currentStep) Color(0xFFFFB703) else Color(0x55FFFFFF)
                                )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        JellyButton(
                            text = "◀ Back",
                            onClick = onPrevious,
                            height = 46.dp,
                            fontSize = 14,
                            colorVariant = JellyColorVariant.BLUE,
                            testTag = "tutorial_back_button",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    JellyButton(
                        text = if (currentStep == totalSteps - 1) "LET'S POP! 🎈" else "Next ▶",
                        onClick = onNext,
                        height = 46.dp,
                        fontSize = 16,
                        colorVariant = JellyColorVariant.GREEN,
                        testTag = "tutorial_next_button",
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetPointerBadge(
    label: String,
    color: Color,
    pulseScale: Float
) {
    Box(
        modifier = Modifier
            .scale(pulseScale)
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = color)
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .border(2.dp, Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            letterSpacing = 0.5.sp
        )
    }
}
