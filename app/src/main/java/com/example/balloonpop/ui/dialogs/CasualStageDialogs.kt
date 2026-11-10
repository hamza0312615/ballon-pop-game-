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
import androidx.compose.ui.draw.alpha
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
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.CreamSurface
import com.example.balloonpop.ui.theme.PlumText
import com.example.balloonpop.ui.theme.StoryGold
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
                            val zoneName = if (level > 0) {
                                GameConstants.WORLD_ZONES.find { level in it.startLevel..it.endLevel }?.name ?: "Twilight Grove"
                            } else null
                            Text(
                                text = if (level > 0) "STAGE $level \u2022 $zoneName" else "CUSTOM CHALLENGE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFDE68A)
                            )
                        }
                        Text(
                            text = "START! 🎯",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4A90C4)
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
    // Pass 8: win-streak fuel — the streak AFTER this win (persisted).
    winStreak: Int = 0,
    // Pass 9: Steady Hands objective bonus
    steadyHandsBonusEarned: Int = 0,
    steadyHandsMissedCount: Int = 0,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onMap: () -> Unit,
    onMainMenu: () -> Unit = onMap
) {
    val nextLevelNum = GameConstants.CAMPAIGN_LEVELS.getOrNull(level)?.level ?: (level + 1)
    val hasNextLevel = nextLevelNum <= 200
    val animatedScoreText = AnimatedScoreCounter(score)
    val animatedCoinsText = AnimatedScoreCounter(coinsEarned, formatPrefix = "+")
    val animatedGemsText = AnimatedScoreCounter(gemsEarned, formatPrefix = "+")

    // Card pops in with a slight overshoot spring and slides up gently
    val cardPop = remember { Animatable(0.85f) }
    val cardSlideY = remember { Animatable(45f) }
    // Pass 9 win ceremony: 6 small coin icons arc from the reward row up to the dialog's top area (<= 900ms)
    val coinFlight = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        cardPop.animateTo(1f, DesignTokens.DialogPopSpring)
    }
    LaunchedEffect(Unit) {
        cardSlideY.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        coinFlight.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xA6171233)),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Sunburst God Rays Background
        SunburstBackground(
            beamColor = Color(0x35FFD700),
            centerColor = Color(0x60FFA500)
        )

        // Celebratory Falling Confetti Cannon
        ConfettiCelebrationView(particleCount = 50)

        // Self-contained coin flight ceremony (arcs from reward row to dialog top area)
        if (coinFlight.value < 1f) {
            val progress = coinFlight.value
            val sinArc = kotlin.math.sin(progress * Math.PI.toFloat())
            for (i in 0 until 6) {
                val spread = ((i - 2.5f) / 2.5f) // -1.0 .. 1.0
                val arcX = spread * (80f * sinArc)
                val arcY = 70f - (progress * 190f)
                val alpha = (1f - progress * progress).coerceIn(0f, 1f)
                val scale = (0.75f + 0.35f * sinArc).coerceIn(0.5f, 1.25f)
                Box(
                    modifier = Modifier
                        .offset(x = arcX.dp, y = arcY.dp)
                        .scale(scale)
                        .alpha(alpha)
                ) {
                    CurrencyArt(currencyType = "coin", modifier = Modifier.size(18.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .offset(y = cardSlideY.value.dp)
                .scale(cardPop.value)
                .shadow(DesignTokens.ElevationStrong, DesignTokens.ShapeCardStory)
                .clip(DesignTokens.ShapeCardStory)
                .background(CreamSurface)
                .border(2.dp, StoryGold, DesignTokens.ShapeCardStory)
                .padding(DesignTokens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space16)
        ) {
            // Ribbon headline overlapping the card's top edge (research rule 9).
            DialogRibbon(text = "Stage $level Cleared!")

            VictoryTrophyArt(modifier = Modifier.size(48.dp))

            if (isNewHighScore) {
                Text(
                    text = "New best score!",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB7791F),
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
                color = PlumText,
                textAlign = TextAlign.Center
            )

            // Rewards in ONE line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                CurrencyArt(currencyType = "coin", modifier = Modifier.size(18.dp))
                Text(
                    text = "$animatedCoinsText Coins \u00B7 $animatedGemsText Gems",
                    fontSize = DesignTokens.SizeBody,
                    fontWeight = FontWeight.Bold,
                    color = PlumText
                )
                CurrencyArt(currencyType = "gem", modifier = Modifier.size(18.dp))
            }

            // Pass 9: Steady Hands objective bonus message
            if (steadyHandsBonusEarned > 0) {
                Text(
                    text = "🎯 Steady Hands bonus: +$steadyHandsBonusEarned coins (only $steadyHandsMissedCount missed!)",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD97706),
                    textAlign = TextAlign.Center
                )
            }

            // Pass 8: win-streak fuel made visible — the next stage's +5s
            // is promised right here on the win screen.
            if (winStreak >= 2) {
                Text(
                    text = "🔥 Win streak x$winStreak — next stage gets +5s!",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB7791F),
                    textAlign = TextAlign.Center
                )
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
 * Primary CTA for stage dialogs (Pass 5): the shared JewelButton —
 * sunset face, 5dp shelf, gloss, press-sink.
 */
@Composable
private fun StagePrimaryButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    // Pass 6: optional dark text for callers on the bright sunset face.
    textColor: Color? = null
) {
    JewelButton(
        text = text,
        onClick = onClick,
        style = JewelButtonStyle.SUNSET,
        testTag = testTag,
        modifier = modifier,
        textColorOverride = textColor
    )
}

/**
 * Ribbon headline for storybook dialog cards: a gradient banner whose visual
 * position overlaps the card's top edge (drawn shifted up over the padding).
 */
@Composable
private fun DialogRibbon(
    text: String,
    berry: Boolean = false
) {
  Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.dp),
        contentAlignment = Alignment.TopCenter
    ) {
    Box(
        modifier = Modifier
            .offset(y = (-46).dp)
            .shadow(DesignTokens.ElevationMedium, DesignTokens.ShapePill)
            .clip(DesignTokens.ShapePill)
            .background(
                if (berry) {
                    Brush.verticalGradient(listOf(Color(0xFF4A3A85), Color(0xFF241A4D)))
                } else {
                    DesignTokens.SunsetGradient
                }
            )
            .border(2.dp, Color.White.copy(alpha = 0.85f), DesignTokens.ShapePill)
            .padding(horizontal = DesignTokens.Space24, vertical = DesignTokens.Space8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Baloo2FontFamily,
            fontSize = DesignTokens.SizeH2,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
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
    // Pass 5: the fail screen's ONE offer is a gold jewel pill (research rule 10).
    JewelButtonContainer(
        onClick = onClick,
        style = JewelButtonStyle.GOLD,
        minHeight = 44.dp,
        shape = DesignTokens.ShapePill,
        testTag = testTag,
        modifier = modifier
    ) {
        Text(
            text = text,
            // Pass 6: slightly smaller + single line so the longer labelled
            // revive offers ("▶ +15s · Free (Ad)") never wrap inside the pill.
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = PlumText,
            textAlign = TextAlign.Center,
            maxLines = 1
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
    // Pass 5: secondary dialog actions are plum jewel buttons.
    JewelButtonContainer(
        onClick = onClick,
        style = JewelButtonStyle.PLUM,
        minHeight = DesignTokens.ButtonMinHeight,
        testTag = testTag,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = StoryGold,
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
    // Pass 8: kindness assist — true when this fail unlocked (or an earlier
    // fail already set) Popster's Help for this stage.
    assistUnlocked: Boolean = false,
    onPlayOn: () -> Unit,
    onRetry: () -> Unit,
    onWatchAdToRevive: (() -> Unit)? = null,
    onMap: () -> Unit,
    onMainMenu: () -> Unit = onMap
) {
    // Card pops in with a slight overshoot spring and slides up gently
    val cardPop = remember { Animatable(0.85f) }
    val cardSlideY = remember { Animatable(45f) }
    LaunchedEffect(Unit) {
        cardPop.animateTo(1f, DesignTokens.DialogPopSpring)
    }
    LaunchedEffect(Unit) {
        cardSlideY.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
    }

    val resultLine = when (reason) {
        GameOverReason.MISSED_LIMIT -> "Missed $missedCount of $maxMissesAllowed"
        GameOverReason.TIME_EXPIRED -> "Needed $missingPops more pops"
        GameOverReason.BOMB_DETONATION -> "A bomb popped!"
        GameOverReason.LIVES_DEPLETED -> "Out of lives"
        GameOverReason.MANUAL_QUIT -> "Run saved — nice popping!"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xA6171233)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 400.dp)
                .offset(y = cardSlideY.value.dp)
                .scale(cardPop.value)
                .shadow(DesignTokens.ElevationStrong, DesignTokens.ShapeCardStory)
                .clip(DesignTokens.ShapeCardStory)
                .background(CreamSurface)
                .border(2.dp, Color(0xFFD8CDBB), DesignTokens.ShapeCardStory)
                .padding(DesignTokens.Space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignTokens.Space16)
        ) {
            // Pass 6: the card now OPENS with an unmistakable headline —
            // the recording's fail dialog never actually said it was one.
            Text(
                text = "Level Failed!",
                fontFamily = Baloo2FontFamily,
                fontSize = DesignTokens.SizeH1,
                fontWeight = FontWeight.Black,
                color = PlumText,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                MascotCharacter(mood = MascotMood.SAD, size = 52.dp)
                DefeatHazardArt(reason = reason, modifier = Modifier.size(44.dp))
            }

            Text(
                text = resultLine,
                fontSize = DesignTokens.SizeBody,
                fontWeight = FontWeight.Bold,
                color = PlumText,
                textAlign = TextAlign.Center
            )

            // Pass 8: visible kindness — after 3 fails on this stage the
            // assist is announced, not silently applied.
            if (assistUnlocked) {
                Text(
                    text = "💛 Popster's Help unlocked: +10s and a free Freeze on your next try!",
                    fontSize = DesignTokens.SizeCaption,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB7791F),
                    textAlign = TextAlign.Center
                )
            }

            // Compact revive pills — secondary to Retry below.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(DesignTokens.Space8)
            ) {
                RevivePill(
                    text = "⏱ +15s \u00B7 100 coins",
                    onClick = onPlayOn,
                    testTag = "failed_play_on_button",
                    modifier = Modifier.weight(1f)
                )
                if (onWatchAdToRevive != null) {
                    RevivePill(
                        text = "▶ +15s \u00B7 Free (Ad)",
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
                modifier = Modifier.fillMaxWidth(),
                // Pass 6: dark plum text — white on the sunset gradient was
                // low-contrast in the user's recording.
                textColor = PlumText
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

