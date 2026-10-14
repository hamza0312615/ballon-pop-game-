package com.example.balloonpop.ui.dialogs

import android.content.Intent
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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
 * Victory Results Dialog (Level Complete with Rainbow Arch, Confetti Cannon, Sunburst, 3 Stars, High Score Banner, Mascot & Stage Bridge)
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
    val nextLevelData = GameConstants.CAMPAIGN_LEVELS.getOrNull(level)
    val nextLevelNum = nextLevelData?.level ?: (level + 1)
    val nextTargetPops = nextLevelData?.targetPops ?: (20 + (nextLevelNum * 3))
    val nextTimeLimit = nextLevelData?.timeLimit ?: 45
    val nextRewardCoins = nextLevelData?.rewardCoins ?: (150 + nextLevelNum * 20)
    val nextRewardGems = nextLevelData?.rewardGems ?: (1 + nextLevelNum / 5)

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
                .fillMaxWidth(0.95f)
                .widthIn(max = 400.dp)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Celebratory Rainbow Arc Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .offset(y = 12.dp)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
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
                    text = "🌈 STAGE $level CLEARED! 🌈",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            // Luxury Onyx/Gold Result Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(28.dp, RoundedCornerShape(28.dp), spotColor = Color(0x88FFD700))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF281C08), Color(0xFF160E04), Color(0xFF0C0701))
                        )
                    )
                    .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Text(
                    text = "Stage $level of 200",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                // High Score Badge if Achieved
                if (isNewHighScore) {
                    HighScoreCelebrationBadge(
                        previousHighScore = previousHighScore,
                        newScore = score
                    )
                }

                // Inner Obsidian Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF141422))
                        .border(1.5.dp, Color(0xFF28283E), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 3 Large 3D Stars
                    StarsDisplay3D(
                        starsCount = stars,
                        size = 54.dp,
                        animated = true
                    )

                    // You Won Header + Mascot + Animated Rolling Score
                    val animatedScoreText = AnimatedScoreCounter(score)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MascotCharacter(mood = MascotMood.JOYFUL, size = 46.dp)

                        Column {
                            Text(
                                text = "Victory!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Score: $animatedScoreText points",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Reward Badges (Coins & Gems)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1E2E))
                            .border(1.dp, Color(0xFF383852), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+$coinsEarned 🪙 Coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "+$gemsEarned 💎 Gems",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Visual Stage Bridge (Level N -> Level N+1 across 200 stages)
                    if (nextLevelNum <= 200) {
                        StagePathBridge(
                            currentLevel = level,
                            nextLevel = nextLevelNum,
                            maxLevels = 200
                        )
                    }

                    // Next Button (Juicy 3D Green with pulse)
                    val infiniteTransition = rememberInfiniteTransition(label = "victory_pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.04f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "next_pulse"
                    )

                    if (nextLevelNum <= 200) {
                        JellyButton(
                            text = "PLAY NEXT ▶",
                            onClick = onNextLevel,
                            height = 54.dp,
                            fontSize = 18,
                            colorVariant = JellyColorVariant.GOLD,
                            testTag = "victory_next_button",
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(pulseScale)
                        )
                    } else {
                        // Grand Champion Finale Button
                        JellyButton(
                            text = "👑 GRAND VICTORY! 🏆",
                            onClick = onMap,
                            height = 54.dp,
                            fontSize = 17,
                            colorVariant = JellyColorVariant.GOLD,
                            testTag = "victory_complete_button",
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(pulseScale)
                        )
                    }

                    // Secondary Actions: 2x2 Grid (Menu, Map, Replay, Share)
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VictoryActionButton(
                                text = "Main Menu",
                                icon = "🏠",
                                onClick = onMainMenu,
                                testTag = "victory_menu_button",
                                modifier = Modifier.weight(1f)
                            )
                            VictoryActionButton(
                                text = "Stage Map",
                                icon = "🗺️",
                                onClick = onMap,
                                testTag = "victory_map_button",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VictoryActionButton(
                                text = "Replay",
                                icon = "🔄",
                                onClick = onReplay,
                                testTag = "victory_replay_button",
                                modifier = Modifier.weight(1f)
                            )
                            VictoryActionButton(
                                text = "Share",
                                icon = "📣",
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Balloon Pop Deluxe Victory!"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "🎈 I just beat Stage $level of 200 with $stars ⭐ in Balloon Pop Deluxe! Scored ${String.format("%,d", score)} points! Can you beat my score?"
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Your Balloon Pop Score")
                                    )
                                },
                                testTag = "victory_share_button",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VictoryActionButton(
    text: String,
    icon: String,
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
            .height(46.dp)
            .scale(scale)
            .shadow(DesignTokens.ElevationAmbient, DesignTokens.ShapeSmall)
            .clip(DesignTokens.ShapeSmall)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF222034), Color(0xFF141322))
                )
            )
            .border(1.5.dp, Color(0xFF3E3B5A), DesignTokens.ShapeSmall)
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
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(DesignTokens.Space4))
        Text(
            text = text,
            fontSize = DesignTokens.SizeCaption,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Failed Stage Dialog (Out of Moves / Time / Missed Limit with Mascot and Retry / Play On Options)
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
    val stageTitle = if (level > 0) "Stage $level Failed!" else "Challenge Failed!"

    AmberDialogFrame(
        title = stageTitle,
        onClose = onMap
    ) {
        // Mascot showing sad emotion
        MascotCharacter(mood = MascotMood.SAD, size = 64.dp)

        // Specific failure reason banner & description
        when (reason) {
            GameOverReason.MISSED_LIMIT -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF450A0A))
                        .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💨 TOO MANY BALLOONS MISSED!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFCA5A5),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "You missed $missedCount balloons (Limit: $maxMissesAllowed misses allowed)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = "You can only miss up to $maxMissesAllowed balloons in this stage. Pop them fast before they float away!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
            GameOverReason.TIME_EXPIRED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3F1505))
                        .border(1.5.dp, Color(0xFFF97316), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "⏱️ TIME'S UP! (${timeLimit}s EXPIRED)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFED7AA),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Needed $missingPops more balloon pops",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = "The clock ran out before you reached the target. Keep popping at lightning speed!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
            GameOverReason.BOMB_DETONATION -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF3B0715))
                        .border(1.5.dp, Color(0xFFFF2A75), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "💣 BOMB DETONATION!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFDA4AF),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "A hazard bomb exploded",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Text(
                    text = "Careful! Avoid tapping black spiky bomb balloons.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
            GameOverReason.LIVES_DEPLETED -> {
                Text(
                    text = "💔 NO LIVES REMAINING!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFF87171),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Don't let balloons slip past your hammer!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Play On (+15s Time & Reset Misses) Button
        JellyButton(
            text = "PLAY ON (+15s ⏱️ & RESET MISSES)",
            onClick = onPlayOn,
            height = 52.dp,
            fontSize = 15,
            colorVariant = JellyColorVariant.GREEN,
            testTag = "failed_play_on_button",
            modifier = Modifier.fillMaxWidth()
        )

        // Free Continue / Revive via Rewarded Ad
        if (onWatchAdToRevive != null) {
            JellyButton(
                text = "REVIVE FREE (+15s & 3 ❤️) 🎬",
                onClick = onWatchAdToRevive,
                height = 50.dp,
                fontSize = 15,
                colorVariant = JellyColorVariant.PURPLE,
                testTag = "failed_watch_ad_revive_button",
                modifier = Modifier.fillMaxWidth()
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VictoryActionButton(
                text = "Retry",
                icon = "🔄",
                onClick = onRetry,
                testTag = "failed_retry_button",
                modifier = Modifier.weight(1f)
            )
            VictoryActionButton(
                text = "Map",
                icon = "🗺️",
                onClick = onMap,
                testTag = "failed_map_button",
                modifier = Modifier.weight(1f)
            )
            VictoryActionButton(
                text = "Menu",
                icon = "🏠",
                onClick = onMainMenu,
                testTag = "failed_menu_button",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
