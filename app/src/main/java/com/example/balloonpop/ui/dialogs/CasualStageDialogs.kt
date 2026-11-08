package com.example.balloonpop.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.GameOverReason
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.theme.DesignTokens
import kotlinx.coroutines.delay

/**
 * Animated In-Game Stage Intro Ribbon Banner
 */
@Composable
fun StageIntroRibbon(
    level: Int,
    targetPops: Int,
    targetScore: Int,
    timeLimit: Int = 15,
    maxMissesAllowed: Int = 4,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(level) {
        visible = true
        delay(2400L)
        visible = false
        delay(300L)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 70.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x66000000))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF141424),
                                Color(0xFF1E182E),
                                Color(0xFF2E1922)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp))
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MascotCharacter(mood = MascotMood.JOYFUL, size = 56.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF382405))
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (level > 0) "STAGE $level" else "CUSTOM CHALLENGE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFDE68A)
                            )
                        }
                        Text(
                            text = "START! 🎯",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00E5FF)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pop $targetPops in ${timeLimit}s! (Max $maxMissesAllowed misses allowed)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Victory Results Dialog — Pass 4 concise layout:
 * headline, stars, score in one line, rewards in one line,
 * then Next (primary) / Replay / Map.
 */
@Composable
fun VictoryStageDialog(
    level: Int,
    stars: Int,
    score: Int,
    coinsEarned: Int,
    gemsEarned: Int,
    isNewHighScore: Boolean = false,
    previousHighScore: Int = 0,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit,
    onMainMenu: () -> Unit = onMap
) {
    val nextLevelNum = GameConstants.CAMPAIGN_LEVELS.getOrNull(level)?.level ?: (level + 1)
    val hasNextLevel = nextLevelNum <= 200
    val animatedScoreText = AnimatedScoreCounter(score)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Sunburst God Rays Background
        SunburstBackground(
            beamColor = Color(0x35FFD700),
            centerColor = Color(0x60FFA500)
        )

        // Celebratory Falling Confetti Cannon
        ConfettiCelebrationView(particleCount = 50)

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .shadow(DesignTokens.ElevationStrong, DesignTokens.ShapeLarge)
                .clip(DesignTokens.ShapeLarge)
                .background(DesignTokens.SurfaceCardNavy)
                .border(2.dp, DesignTokens.StarGoldFilled, DesignTokens.ShapeLarge)
                .padding(DesignTokens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space16)
        ) {
            VictoryTrophyArt(modifier = Modifier.size(48.dp))

            Text(
                text = "Stage $level Cleared!",
                fontSize = DesignTokens.SizeH1,
                fontWeight = FontWeight.Black,
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center
            )

            if (isNewHighScore) {
                Text(
                    text = "New best score!",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.StarGoldFilled,
                    textAlign = TextAlign.Center
                )
            }

            StarsDisplay3D(
                starsCount = stars,
                size = 54.dp,
                animated = true
            )

            Text(
                text = "Score: $animatedScoreText",
                fontSize = DesignTokens.SizeH2,
                fontWeight = FontWeight.Bold,
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center
            )

            // Rewards in ONE line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                CurrencyArt(currencyType = "coin", modifier = Modifier.size(18.dp))
                Text(
                    text = "+$coinsEarned Coins \u00B7 +$gemsEarned Gems",
                    fontSize = DesignTokens.SizeBody,
                    fontWeight = FontWeight.Bold,
                    color = DesignTokens.TextPrimary
                )
                CurrencyArt(currencyType = "gem", modifier = Modifier.size(18.dp))
            }

            if (hasNextLevel) {
                StagePrimaryButton(
                    text = "Next",
                    onClick = onNextLevel,
                    testTag = "victory_next_button",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                StagePrimaryButton(
                    text = "Finish",
                    onClick = onMap,
                    testTag = "victory_complete_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                VictoryActionButton(
                    text = "Replay",
                    icon = Icons.Default.Refresh,
                    onClick = onReplay,
                    testTag = "victory_replay_button",
                    modifier = Modifier.weight(1f)
                )
                VictoryActionButton(
                    text = "Map",
                    icon = Icons.Default.Map,
                    onClick = onMap,
                    testTag = "victory_map_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Primary CTA for stage dialogs: CtaGradient, RadiusMedium corners,
 * at least DesignTokens.ButtonMinHeight tall.
 */
@Composable
private fun StagePrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "primary_btn_press"
    )

    Box(
        modifier = modifier
            .height(DesignTokens.ButtonMinHeight)
            .scale(scale)
            .shadow(DesignTokens.ElevationMedium, DesignTokens.ShapeMedium)
            .clip(DesignTokens.ShapeMedium)
            .background(DesignTokens.CtaGradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space16),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = DesignTokens.SizeH2,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Compact secondary revive pill for the defeat dialog.
 * Deliberately smaller than the big Retry / Map / Home buttons.
 */
@Composable
private fun RevivePill(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "revive_pill_press"
    )

    Box(
        modifier = modifier
            .height(40.dp)
            .scale(scale)
            .clip(DesignTokens.ShapePill)
            .background(DesignTokens.SurfaceCardElevated)
            .border(1.dp, DesignTokens.SurfaceBorder, DesignTokens.ShapePill)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space12),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = DesignTokens.SizeCaption,
            fontWeight = FontWeight.Bold,
            color = DesignTokens.TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VictoryActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "btn_press"
    )

    Row(
        modifier = modifier
            .height(DesignTokens.ButtonMinHeight)
            .scale(scale)
            .shadow(DesignTokens.ElevationAmbient, DesignTokens.ShapeMedium)
            .clip(DesignTokens.ShapeMedium)
            .background(DesignTokens.SurfaceCardElevated)
            .border(1.5.dp, DesignTokens.SurfaceBorder, DesignTokens.ShapeMedium)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space8),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(DesignTokens.Space4))
        Text(
            text = text,
            fontSize = DesignTokens.SizeBody,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Failed Stage Dialog — Pass 4 concise layout:
 * ONE headline ("Level Failed!"), ONE short subline, ONE result line,
 * two compact revive pills (secondary), then exactly 3 big buttons:
 * Retry (primary) / Map / Home.
 *
 * Revive values come from GameViewModel.playOnRevive: +15s and,
 * for the coin path, a 100-coin cost; the rewarded-ad path is free.
 */
@Composable
fun FailedStageDialog(
    level: Int,
    missingPops: Int,
    userCoins: Int,
    missedCount: Int = 0,
    maxMissesAllowed: Int = 4,
    timeLimit: Int = 15,
    reason: GameOverReason = GameOverReason.TIME_EXPIRED,
    onPlayOn: () -> Unit,
    onRetry: () -> Unit,
    onWatchAdToRevive: (() -> Unit)? = null,
    onMap: () -> Unit,
    onMainMenu: () -> Unit = onMap
) {
    val resultLine = when (reason) {
        GameOverReason.MISSED_LIMIT -> "Missed $missedCount of $maxMissesAllowed"
        GameOverReason.TIME_EXPIRED -> "Needed $missingPops more pops"
        GameOverReason.BOMB_DETONATION -> "A bomb popped!"
        GameOverReason.LIVES_DEPLETED -> "Out of lives"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .shadow(DesignTokens.ElevationStrong, DesignTokens.ShapeLarge)
                .clip(DesignTokens.ShapeLarge)
                .background(DesignTokens.SurfaceCardNavy)
                .border(2.dp, DesignTokens.SurfaceBorder, DesignTokens.ShapeLarge)
                .padding(DesignTokens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space16)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                MascotCharacter(mood = MascotMood.SAD, size = 52.dp)
                DefeatHazardArt(reason = reason, modifier = Modifier.size(44.dp))
            }

            Text(
                text = "Level Failed!",
                fontSize = DesignTokens.SizeH1,
                fontWeight = FontWeight.Black,
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "So close \u2014 try again!",
                fontSize = DesignTokens.SizeBody,
                fontWeight = FontWeight.Bold,
                color = DesignTokens.TextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = resultLine,
                fontSize = DesignTokens.SizeBody,
                fontWeight = FontWeight.Bold,
                color = DesignTokens.TextPrimary,
                textAlign = TextAlign.Center
            )

            // Compact revive pills — secondary to Retry below.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                RevivePill(
                    text = "+15s \u00B7 100 coins",
                    onClick = onPlayOn,
                    testTag = "failed_play_on_button",
                    modifier = Modifier.weight(1f)
                )
                if (onWatchAdToRevive != null) {
                    RevivePill(
                        text = "+15s \u00B7 Free",
                        onClick = onWatchAdToRevive,
                        testTag = "failed_watch_ad_revive_button",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            StagePrimaryButton(
                text = "Retry",
                onClick = onRetry,
                testTag = "failed_retry_button",
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                VictoryActionButton(
                    text = "Map",
                    icon = Icons.Default.Map,
                    onClick = onMap,
                    testTag = "failed_map_button",
                    modifier = Modifier.weight(1f)
                )
                VictoryActionButton(
                    text = "Home",
                    icon = Icons.Default.Home,
                    onClick = onMainMenu,
                    testTag = "failed_menu_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

