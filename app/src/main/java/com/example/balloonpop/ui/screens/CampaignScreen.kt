package com.example.balloonpop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.audio.SoundManager
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.MiniPetRegistry
import com.example.balloonpop.model.StarMilestoneReward
import com.example.balloonpop.model.WorldZone
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.dialogs.PreGameStageDialog
import com.example.balloonpop.ui.dialogs.WorldMapZoneOverviewDialog
import com.example.balloonpop.ui.theme.ZoneVisualTheme
import com.example.balloonpop.ui.theme.ZoneVisualThemeRegistry
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Node position data on the ascending vertical level ladder.
 * Coordinates specify the exact center of each stage stepping platform.
 */
data class LadderNodePos(
    val levelIndex: Int, // 0 to 9 in the current zone
    val stageNumber: Int,
    val xRatio: Float,   // horizontal ratio (0.20f to 0.80f)
    val yDp: Float,      // vertical center distance in dp from top
    val isMilestone: Boolean,
    val isBoss: Boolean
)

/**
 * Campaign World Map Screen - Wooden-Refined Adventure Level Progression.
 * Features:
 * - Hand-crafted wooden suspension ladder with braided hemp ropes and carved timber rungs
 * - Exact mathematical center alignment linking rope ladder rungs directly to stage stepping platforms
 * - Carved timber stage platforms with brass rivets and glowing jewel inserts (Emerald, Gold, Amethyst, Ruby)
 * - Active Level Platform with bouncing 3D emoji mascot (😘), floating love hearts, and golden beacon shockwaves
 * - Wooden trailhead signposts, milestone posts with lanterns, and summit dragon shrine
 * - Swaying marshmallow candy trees, striped candy canes, and floating balloons with starburst pops
 * - Meandering glowing river with water ripples and swaying lotus water lilies
 * - Compact wooden-framed top header and status strip leaving 85%+ viewport for the ladder map
 * - Floating "🎯 Focus Active Stage" button with smooth auto-scroll
 * - 3D Wooden-Refined Casual Bottom Dock with Home, Juicy Play Button, and Shop
 * - Pre-Game Stage Lobby Dialog & Cinematic Level Dive overlay
 */
@Composable
fun CampaignScreen(
    completedLevels: Map<Int, Int>,
    claimedMilestoneChests: Set<Int> = emptySet(),
    claimedStarMilestones: Set<Int> = emptySet(),
    coins: Int = 150,
    gems: Int = 10,
    lives: Int = 5,
    onSelectLevel: (Int, Set<String>) -> Unit,
    onClaimMilestoneChest: (Int) -> Unit = {},
    onClaimStarMilestone: (StarMilestoneReward) -> Unit = {},
    onOpenSkillVault: () -> Unit = {},
    onOpenDailySpin: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenEndless: () -> Unit = {},
    onOpenQuests: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = remember { SoundManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val maxCompleted = completedLevels.keys.maxOrNull() ?: 0
    val currentUnlockedLevel = (maxCompleted + 1).coerceAtMost(200)

    // Current zone calculation (10 stages per zone, 20 zones total)
    val defaultZoneIndex = ((currentUnlockedLevel - 1) / 10).coerceIn(0, GameConstants.WORLD_ZONES.size - 1)
    var selectedZoneIndex by remember { mutableIntStateOf(defaultZoneIndex) }
    val currentZone = GameConstants.WORLD_ZONES.getOrElse(selectedZoneIndex) { GameConstants.WORLD_ZONES.first() }
    val zoneTheme = remember(selectedZoneIndex, currentZone) {
        ZoneVisualThemeRegistry.getThemeForZone(selectedZoneIndex, currentZone)
    }

    // World Map Realm Selector dialog state
    var showZoneOverviewDialog by remember { mutableStateOf(false) }

    // Pre-game dialog and level dive state
    var selectedLevelForDialog by remember { mutableStateOf<Int?>(null) }
    var levelDivingTarget by remember { mutableStateOf<Pair<Int, Set<String>>?>(null) }

    // Ladder vertical scroll state
    val ladderScrollState = rememberScrollState()

    // Height of the ladder canvas
    val ladderHeightDp = 1060.dp

    // Stars earned in the current zone
    val zoneStars = remember(selectedZoneIndex, completedLevels) {
        val start = currentZone.startLevel
        val end = currentZone.endLevel
        (start..end).sumOf { completedLevels[it] ?: 0 }
    }

    // Infinite transitions for rich ambient animations
    val infiniteTransition = rememberInfiniteTransition(label = "wooden_campaign_ambient")

    // Mascot vertical bounce with springy squash
    val mascotBob by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_bob"
    )

    // Golden beacon pulse expanding outwards from active tile
    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beacon_pulse"
    )

    // Play button breathing pulse
    val playButtonPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "play_btn_pulse"
    )

    // Flowing river & wave shimmer phase
    val riverFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "river_flow"
    )

    // Energy pulse moving along ladder rails and rungs
    val energyPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ladder_energy_pulse"
    )

    // Continuous particle system clock (3600 seconds continuous seamless flow)
    val particleTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3600f,
        animationSpec = infiniteRepeatable(
            animation = tween(3_600_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zone_particle_time"
    )

    // Auto-scroll to center on active stage when zone changes or screen opens
    LaunchedEffect(selectedZoneIndex, currentUnlockedLevel) {
        val zoneStart = currentZone.startLevel
        val zoneEnd = currentZone.endLevel
        if (currentUnlockedLevel in zoneStart..zoneEnd) {
            val stageOffset = (currentUnlockedLevel - zoneStart).coerceIn(0, 9)
            // Stage 1 is at ~940dp, Stage 10 is at ~70dp
            val targetFraction = (9 - stageOffset) / 9f
            ladderScrollState.animateScrollTo((targetFraction * 540).toInt())
        } else if (selectedZoneIndex > defaultZoneIndex) {
            // Future zone, show start at bottom
            ladderScrollState.animateScrollTo(540)
        } else {
            // Past zone, show top cleared stages
            ladderScrollState.animateScrollTo(0)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050912))
    ) {
        // Main Screen Structure
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 78.dp)
        ) {
            // 1. Modern Glassmorphic Top Resource Bar (Back Button + Hearts, Coins, Gems capsules)
            ModernCampaignTopStatusBar(
                lives = lives,
                coins = coins,
                gems = gems,
                onBack = {
                    soundManager.playDialogClose()
                    onClose()
                },
                onOpenShop = {
                    soundManager.playMenuClick()
                    onOpenShop()
                }
            )

            // 2. Modern Zone Header Card (Zone emblem with shimmer glint, Name + Subtitle, Chevrons with pressed state)
            ModernZoneHeaderCard(
                theme = zoneTheme,
                zoneName = currentZone.name,
                canPrev = selectedZoneIndex > 0,
                canNext = selectedZoneIndex < GameConstants.WORLD_ZONES.size - 1,
                onPrevZone = {
                    if (selectedZoneIndex > 0) {
                        soundManager.playMenuTabSwitch()
                        selectedZoneIndex--
                    }
                },
                onNextZone = {
                    if (selectedZoneIndex < GameConstants.WORLD_ZONES.size - 1) {
                        soundManager.playMenuTabSwitch()
                        selectedZoneIndex++
                    }
                },
                onOpenZoneSelector = {
                    soundManager.playDialogOpen()
                    showZoneOverviewDialog = true
                }
            )

            // 3. Spacing / Subtle separator line between header and secondary nav
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                zoneTheme.primaryAccent.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // 4. Modern Unified Segmented Toolbar (Build, Map, Stars, Spin with tiny pulsing notification dot, Settings)
            ModernSecondaryNavRow(
                starsEarned = zoneStars,
                totalStars = 30,
                buildPoints = 24,
                onOpenBuild = {
                    soundManager.playMenuClick()
                    onOpenSkillVault()
                },
                onOpenWorldMap = {
                    soundManager.playDialogOpen()
                    showZoneOverviewDialog = true
                },
                onOpenSpin = {
                    soundManager.playMenuClick()
                    onOpenDailySpin()
                },
                onOpenSettings = {
                    soundManager.playMenuClick()
                    onOpenSettings()
                }
            )

            // 5. Full-Height Level Ladder Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                VerticalLevelLadder(
                    theme = zoneTheme,
                    modifier = Modifier.fillMaxSize(),
                    scrollState = ladderScrollState,
                    heightDp = ladderHeightDp,
                    startLevel = currentZone.startLevel,
                    endLevel = currentZone.endLevel,
                    currentUnlockedLevel = currentUnlockedLevel,
                    completedLevels = completedLevels,
                    currentZone = currentZone,
                    selectedZoneIndex = selectedZoneIndex,
                    mascotBob = mascotBob,
                    beaconPulse = beaconPulse,
                    riverFlow = riverFlow,
                    energyPulse = energyPulse,
                    particleTime = particleTime,
                    onLevelClick = { lvl ->
                        soundManager.playLevelSelect()
                        selectedLevelForDialog = lvl
                    }
                )

                // Atmospheric Zone-Specific Floating Particle Overlay (Fireflies in Neon Glade, Crystal Shards in Prism Peaks)
                ZoneMapParticleOverlay(
                    theme = zoneTheme,
                    scrollOffset = ladderScrollState.value.toFloat(),
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Side Chevrons for quick zone switching
                if (selectedZoneIndex < GameConstants.WORLD_ZONES.size - 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                            .clip(CircleShape)
                            .background(Color(0xEE1E1510))
                            .border(1.5.dp, Color(0xFFD4A373), CircleShape)
                            .clickable {
                                soundManager.playMenuTabSwitch()
                                selectedZoneIndex++
                            }
                            .testTag("floating_next_zone_arrow"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "›",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }

                if (selectedZoneIndex > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 6.dp)
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, spotColor = Color(0xFF00E5FF))
                            .clip(CircleShape)
                            .background(Color(0xEE1E1510))
                            .border(1.5.dp, Color(0xFFD4A373), CircleShape)
                            .clickable {
                                soundManager.playMenuTabSwitch()
                                selectedZoneIndex--
                            }
                            .testTag("floating_prev_zone_arrow"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    }
                }

                // Quick Floating Themed "Jump to Current Stage" Button
                if (currentUnlockedLevel in currentZone.startLevel..currentZone.endLevel) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 10.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = zoneTheme.jumpButtonBorderColor)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(zoneTheme.jumpButtonGradient)
                            )
                            .border(1.5.dp, zoneTheme.jumpButtonBorderColor, RoundedCornerShape(20.dp))
                            .clickable {
                                soundManager.playMenuClick()
                                coroutineScope.launch {
                                    val offset = (currentUnlockedLevel - currentZone.startLevel).coerceIn(0, 9)
                                    val target = ((9 - offset) / 9f * 540).toInt()
                                    ladderScrollState.animateScrollTo(target)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = zoneTheme.playButtonBadge, fontSize = 14.sp)
                            Text(
                                text = "Stage $currentUnlockedLevel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 4. Modern Floating Bottom Dock
        ModernBottomFloatingDock(
            theme = zoneTheme,
            modifier = Modifier.align(Alignment.BottomCenter),
            currentLevel = currentUnlockedLevel,
            playPulse = playButtonPulse,
            onHomeClick = {
                soundManager.playDialogClose()
                onClose()
            },
            onEndlessClick = {
                soundManager.playMenuClick()
                onOpenEndless()
            },
            onPlayClick = {
                soundManager.playLevelSelect()
                selectedLevelForDialog = currentUnlockedLevel
            },
            onVaultClick = {
                soundManager.playMenuClick()
                onOpenSkillVault()
            },
            onShopClick = {
                soundManager.playMenuClick()
                onOpenShop()
            }
        )

        // 5. Pre-Game Stage Lobby Modal
        selectedLevelForDialog?.let { level ->
            val starsEarned = completedLevels[level] ?: 0
            val lvlConfig = GameConstants.CAMPAIGN_LEVELS.getOrNull(level - 1)
            val targetPops = lvlConfig?.targetPops ?: (15 + level * 2)
            val targetScore = lvlConfig?.targetScore ?: (level * 1000)
            val timeLimit = lvlConfig?.timeLimit ?: 15
            val maxMissesAllowed = lvlConfig?.maxMissesAllowed ?: 4

            PreGameStageDialog(
                level = level,
                starsEarned = starsEarned,
                targetScore = targetScore,
                targetPops = targetPops,
                timeLimit = timeLimit,
                maxMissesAllowed = maxMissesAllowed,
                userCoins = coins,
                onStartLevel = { boosters ->
                    selectedLevelForDialog = null
                    soundManager.playLevelWarp()
                    // Initiate cinematic Level Dive
                    levelDivingTarget = Pair(level, boosters)
                },
                onClose = {
                    soundManager.playDialogClose()
                    selectedLevelForDialog = null
                }
            )
        }

        // 6. Cinematic Level Dive Transition Overlay
        levelDivingTarget?.let { (targetLvl, boosters) ->
            val targetZone = GameConstants.WORLD_ZONES.find { targetLvl in it.startLevel..it.endLevel } ?: currentZone
            LevelDiveOverlay(
                level = targetLvl,
                zoneName = targetZone.name,
                onDiveFinished = {
                    levelDivingTarget = null
                    onSelectLevel(targetLvl, boosters)
                }
            )
        }

        // 7. World Map Realm Selection Overview Modal
        if (showZoneOverviewDialog) {
            WorldMapZoneOverviewDialog(
                selectedZoneIndex = selectedZoneIndex,
                currentUnlockedLevel = currentUnlockedLevel,
                completedLevels = completedLevels,
                onSelectZone = { newZoneIndex ->
                    soundManager.playMenuTabSwitch()
                    selectedZoneIndex = newZoneIndex
                },
                onClose = {
                    soundManager.playDialogClose()
                    showZoneOverviewDialog = false
                }
            )
        }
    }
}

/**
 * Modern Glassmorphic Top Resource Bar (Hearts, Coins, Gems):
 * - Subtle glassmorphic capsules: soft dark background blur/gradient, 1px inner highlight border, slight drop shadow
 * - Micro-animation on value change (coin/gem/heart count ticks up with springy bounce pulse)
 * - Soft continuous glow/pulse animation on the "+" add buttons
 * - Generous touch targets (>= 44x44dp)
 */
@Composable
private fun ModernCampaignTopStatusBar(
    lives: Int,
    maxLives: Int = 5,
    coins: Int,
    gems: Int,
    onBack: () -> Unit,
    onOpenShop: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Green Circular Glassmorphic Back Button with White Arrow
        val backInteraction = remember { MutableInteractionSource() }
        val backPressed by backInteraction.collectIsPressedAsState()
        val backScale by animateFloatAsState(
            targetValue = if (backPressed) 0.90f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "back_btn_press"
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(backScale)
                .shadow(6.dp, CircleShape, spotColor = Color(0xFF16A34A))
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF16A34A))
                    )
                )
                .border(1.5.dp, Color(0xFF86EFAC), CircleShape)
                .clickable(
                    interactionSource = backInteraction,
                    indication = null
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBack()
                }
                .testTag("campaign_green_back_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.offset(y = (-2).dp)
            )
        }

        // Glassmorphic Capsule Row for Hearts, Coins, Gems
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hearts Capsule
            GlassmorphicResourceCapsule(
                icon = "❤️",
                value = "$lives/$maxLives",
                textColor = Color(0xFFFFF1F2),
                showAdd = true,
                onAddClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenShop()
                },
                testTag = "campaign_lives_badge"
            )

            // Coins Capsule
            GlassmorphicResourceCapsule(
                icon = "🪙",
                value = String.format("%,d", coins),
                textColor = Color(0xFFFFD54F),
                showAdd = true,
                onAddClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenShop()
                },
                testTag = "campaign_coins_badge",
                addTestTag = "campaign_add_coins_btn"
            )

            // Gems Capsule
            GlassmorphicResourceCapsule(
                icon = "💎",
                value = String.format("%,d", gems),
                textColor = Color(0xFF67E8F9),
                showAdd = true,
                onAddClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenShop()
                },
                testTag = "campaign_gems_badge"
            )
        }
    }
}

@Composable
private fun GlassmorphicResourceCapsule(
    icon: String,
    value: String,
    textColor: Color,
    showAdd: Boolean,
    onAddClick: () -> Unit,
    testTag: String,
    addTestTag: String? = null
) {
    // Micro-animation on value change
    var bounceTarget by remember { mutableFloatStateOf(1.0f) }
    var previousValue by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        if (value != previousValue) {
            previousValue = value
            bounceTarget = 1.20f
            kotlinx.coroutines.delay(180)
            bounceTarget = 1.0f
        }
    }
    val animatedBounce by animateFloatAsState(
        targetValue = bounceTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "capsule_bounce"
    )

    // Soft glow pulse on the "+" add buttons
    val infiniteTransition = rememberInfiniteTransition(label = "capsule_add_pulse")
    val addGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "add_glow_pulse"
    )

    Row(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x66000000))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE6131D2E),
                        Color(0xF00D1420),
                        Color(0xE6080D16)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color(0x66FFFFFF), Color(0x18FFFFFF), Color(0x2238BDF8))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(start = 7.dp, end = if (showAdd) 2.dp else 7.dp, top = 3.dp, bottom = 3.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = icon, fontSize = 13.sp)

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = textColor,
            modifier = Modifier.scale(animatedBounce)
        )

        if (showAdd) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onAddClick() }
                    .then(if (addTestTag != null) Modifier.testTag(addTestTag) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(19.dp)
                        .scale(addGlowPulse)
                        .shadow(4.dp, CircleShape, spotColor = Color(0xFF22C55E))
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF16A34A))
                            )
                        )
                        .border(1.dp, Color(0xFFA7F3D0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

/**
 * Modern Zone Header Card:
 * - Single cohesive card with rounded corners (18px radius), subtle gradient background matching zone's palette,
 *   and soft outer glow in the zone's accent color
 * - Embedded themed icon/emblem to the left of zone name with a subtle shimmer/glint sweep animation every few seconds
 * - Proper type scale: zone name larger + bolder (19px semibold/black), subtitle smaller + 70% opacity
 * - Left/right chevron navigation arrows with interactive pressed/hover states (scale down slightly + opacity change)
 */
@Composable
private fun ModernZoneHeaderCard(
    theme: ZoneVisualTheme,
    zoneName: String,
    canPrev: Boolean,
    canNext: Boolean,
    onPrevZone: () -> Unit,
    onNextZone: () -> Unit,
    onOpenZoneSelector: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Subtle glint / shimmer sweep animation every 3 seconds across the emblem
    val infiniteTransition = rememberInfiniteTransition(label = "zone_header_glint")
    val glintSweep by infiniteTransition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glint_sweep"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = theme.activeGlowColor)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        theme.titlePillBg,
                        Color(0xF00D1424),
                        theme.titlePillBg
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(
                        theme.primaryAccent.copy(alpha = 0.85f),
                        theme.titlePillBorder,
                        theme.secondaryAccent.copy(alpha = 0.85f)
                    )
                ),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Chevron Arrow with interactive pressed state
        val prevInteraction = remember { MutableInteractionSource() }
        val prevPressed by prevInteraction.collectIsPressedAsState()
        val prevScale by animateFloatAsState(
            targetValue = if (prevPressed) 0.88f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "prev_press"
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(prevScale)
                .alpha(if (canPrev) (if (prevPressed) 0.70f else 1.0f) else 0.25f)
                .clip(CircleShape)
                .background(Color(0x2BFFFFFF))
                .border(1.dp, if (canPrev) theme.titleAccentColor.copy(alpha = 0.6f) else Color(0x1AFFFFFF), CircleShape)
                .clickable(
                    interactionSource = prevInteraction,
                    indication = null,
                    enabled = canPrev
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPrevZone()
                }
                .testTag("campaign_prev_zone_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                color = if (canPrev) theme.titleAccentColor else Color.Gray,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }

        // Center Zone Title Area: Embedded Emblem + Zone Name + Subtitle
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenZoneSelector() }
                .padding(horizontal = 6.dp)
                .testTag("campaign_zone_pill_selector"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Embedded Themed Emblem with shimmer/glint sweep animation
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x33000000))
                    .border(1.2.dp, theme.primaryAccent.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Shimmer glint canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val cx = w * glintSweep
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.35f),
                                Color.Transparent
                            ),
                            startX = cx - 14f,
                            endX = cx + 14f
                        )
                    )
                }

                Text(
                    text = theme.titleOrnamentPrefix,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = zoneName,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "— ${theme.subtitle} —",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.titleAccentColor.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(text = "🗺️", fontSize = 10.sp)
                }
            }
        }

        // Right Chevron Arrow with interactive pressed state
        val nextInteraction = remember { MutableInteractionSource() }
        val nextPressed by nextInteraction.collectIsPressedAsState()
        val nextScale by animateFloatAsState(
            targetValue = if (nextPressed) 0.88f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "next_press"
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .scale(nextScale)
                .alpha(if (canNext) (if (nextPressed) 0.70f else 1.0f) else 0.25f)
                .clip(CircleShape)
                .background(Color(0x2BFFFFFF))
                .border(1.dp, if (canNext) theme.titleAccentColor.copy(alpha = 0.6f) else Color(0x1AFFFFFF), CircleShape)
                .clickable(
                    interactionSource = nextInteraction,
                    indication = null,
                    enabled = canNext
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNextZone()
                }
                .testTag("campaign_next_zone_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "›",
                color = if (canNext) theme.titleAccentColor else Color.Gray,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.offset(y = (-1).dp)
            )
        }
    }
}

/**
 * Modern Segmented Secondary Nav Toolbar:
 * - Unified segmented toolbar with consistent 8px grid spacing, rounded pill backgrounds, and clear pressed states
 * - Crisp consistently-sized icons with subtle background tint per button
 * - Subtle pulsing notification dot on Spin button
 */
@Composable
private fun ModernSecondaryNavRow(
    starsEarned: Int,
    totalStars: Int,
    buildPoints: Int,
    onOpenBuild: () -> Unit,
    onOpenWorldMap: () -> Unit,
    onOpenSpin: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Pulsing notification dot animation for Spin
    val infiniteTransition = rememberInfiniteTransition(label = "spin_badge_pulse")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_pulse"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Build Segment Pill (Amber tint)
        ModernNavSegment(
            icon = "🔨",
            label = "Build $buildPoints",
            tintColor = Color(0x28F59E0B),
            borderColor = Color(0x88F59E0B),
            textColor = Color.White,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenBuild()
            },
            testTag = "campaign_status_build_btn",
            modifier = Modifier.weight(1f)
        )

        // 2. World Map Segment Pill (Cyan tint)
        ModernNavSegment(
            icon = "🗺️",
            label = "Map",
            tintColor = Color(0x2800E5FF),
            borderColor = Color(0x8800E5FF),
            textColor = Color(0xFF38BDF8),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenWorldMap()
            },
            testTag = "campaign_status_world_map_btn",
            modifier = Modifier.weight(0.85f)
        )

        // 3. Stars Earned Segment Pill (Gold tint)
        ModernNavSegment(
            icon = "⭐",
            label = "$starsEarned / $totalStars",
            tintColor = Color(0x28FFD700),
            borderColor = Color(0x88FFD700),
            textColor = Color(0xFFFFD54F),
            onClick = {},
            testTag = "campaign_status_stars_btn",
            modifier = Modifier.weight(1.05f)
        )

        // 4. Daily Spin Segment Pill (Sky Blue tint with pulsing notification dot)
        ModernNavSegment(
            icon = "🎡",
            label = "Spin",
            tintColor = Color(0x2838BDF8),
            borderColor = Color(0x8838BDF8),
            textColor = Color(0xFF7DD3FC),
            showPulsingDot = true,
            dotPulse = dotPulse,
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onOpenSpin()
            },
            testTag = "campaign_status_spin_btn",
            modifier = Modifier.weight(0.85f)
        )

        // 5. Settings Gear (Purple tint)
        val settingsInteraction = remember { MutableInteractionSource() }
        val settingsPressed by settingsInteraction.collectIsPressedAsState()
        val settingsScale by animateFloatAsState(
            targetValue = if (settingsPressed) 0.88f else 1.0f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "settings_press"
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .scale(settingsScale)
                .clip(CircleShape)
                .background(Color(0x288B5CF6))
                .border(1.2.dp, Color(0x888B5CF6), CircleShape)
                .clickable(
                    interactionSource = settingsInteraction,
                    indication = null
                ) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings()
                }
                .testTag("campaign_settings_btn"),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⚙️", fontSize = 15.sp)
        }
    }
}

@Composable
private fun ModernNavSegment(
    icon: String,
    label: String,
    tintColor: Color,
    borderColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    showPulsingDot: Boolean = false,
    dotPulse: Float = 1.0f
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "segment_press"
    )

    Box(
        modifier = modifier
            .height(34.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(tintColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 12.sp)
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
        }

        // Tiny pulsing notification dot
        if (showPulsingDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
                    .size(7.dp)
                    .scale(dotPulse)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}

/**
 * Vertical Level Ladder Component:
 * Climbs up from Stage 1 (Bottom) to Stage 10 (Summit Boss) in an S-curve.
 * Connected by realistic dual braided hemp rope rails and carved timber rungs.
 * Coordinates specify the exact center of each stage stepping platform.
 */
@Composable
private fun VerticalLevelLadder(
    theme: ZoneVisualTheme,
    scrollState: androidx.compose.foundation.ScrollState,
    heightDp: Dp,
    startLevel: Int,
    endLevel: Int,
    currentUnlockedLevel: Int,
    completedLevels: Map<Int, Int>,
    currentZone: WorldZone,
    selectedZoneIndex: Int,
    mascotBob: Float,
    beaconPulse: Float,
    riverFlow: Float,
    energyPulse: Float,
    particleTime: Float = riverFlow * 25f,
    onLevelClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate the 10 nodes with theme-specific route coordinates and pacing
    val nodes = remember(startLevel, endLevel, theme) {
        val list = mutableListOf<LadderNodePos>()
        val totalStages = 10
        val xCurves = theme.routeLayout.xCurves
        val yOffsets = theme.routeLayout.yOffsets

        for (i in 0 until totalStages) {
            val stageNum = startLevel + i
            list.add(
                LadderNodePos(
                    levelIndex = i,
                    stageNumber = stageNum,
                    xRatio = xCurves.getOrElse(i) { 0.5f },
                    yDp = yOffsets.getOrElse(i) { 500f },
                    isMilestone = (i == 4), // Stage 5 is milestone
                    isBoss = (i == 9)       // Stage 10 is boss
                )
            )
        }
        list
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
        ) {
            // 1. Unique Scenic Multi-Layer Environment Canvas & Thematic Paths
            Canvas(modifier = Modifier.fillMaxSize()) {
                with(ZoneEnvironmentRenderer) {
                    drawScenicBackground(
                        theme = theme,
                        scrollOffset = scrollState.value.toFloat(),
                        riverFlow = riverFlow,
                        density = this@Canvas,
                        timeSeconds = particleTime
                    )
                    drawThemedPath(
                        theme = theme,
                        nodes = nodes,
                        currentUnlockedLevel = currentUnlockedLevel,
                        energyPulse = energyPulse,
                        riverFlow = riverFlow,
                        density = this@Canvas
                    )
                }
            }

            // 2. Interactive Themed Stage Platform Nodes
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val availableWidth = maxWidth

                nodes.forEach { node ->
                    val isUnlocked = node.stageNumber <= currentUnlockedLevel
                    val isCurrent = node.stageNumber == currentUnlockedLevel
                    val isCleared = completedLevels.containsKey(node.stageNumber)
                    val stars = completedLevels[node.stageNumber] ?: if (isCleared) 3 else 0

                    // Exact Platform Center in Dp
                    val centerX = (availableWidth * node.xRatio).coerceIn(48.dp, availableWidth - 48.dp)
                    val centerY = node.yDp.dp

                    if (isCurrent) {
                        // Active Hero Platform: Centered precisely on (centerX, centerY)
                        Box(
                            modifier = Modifier
                                .offset(x = centerX - 36.dp, y = centerY - 100.dp)
                                .testTag("ladder_node_${node.stageNumber}")
                        ) {
                            ThemedLadderActiveHeroNode(
                                theme = theme,
                                stageNumber = node.stageNumber,
                                stars = stars,
                                mascotBob = mascotBob,
                                beaconPulse = beaconPulse,
                                isMilestone = node.isMilestone,
                                isBoss = node.isBoss,
                                onClick = { onLevelClick(node.stageNumber) }
                            )
                        }

                        // Animated Zone Mini Pet Balloon Companion floating beside the player
                        val zonePet = remember(selectedZoneIndex) { MiniPetRegistry.getPetForZone(selectedZoneIndex) }
                        val petX = if (node.xRatio > 0.5f) {
                            (centerX - 92.dp).coerceAtLeast(8.dp)
                        } else {
                            (centerX + 44.dp).coerceAtMost(availableWidth - 92.dp)
                        }
                        val petY = (centerY - 96.dp).coerceAtLeast(8.dp)
                        Box(
                            modifier = Modifier.offset(x = petX, y = petY)
                        ) {
                            MiniPetMapCompanion(
                                pet = zonePet,
                                isBossZone = node.isBoss
                            )
                        }
                    } else {
                        // Standard Cleared / Locked Platform
                        Box(
                            modifier = Modifier
                                .offset(x = centerX - 28.dp, y = centerY - 32.dp)
                                .testTag("ladder_node_${node.stageNumber}")
                        ) {
                            ThemedLadderTileNode(
                                theme = theme,
                                stageNumber = node.stageNumber,
                                stars = stars,
                                isUnlocked = isUnlocked,
                                isCleared = isCleared,
                                isMilestone = node.isMilestone,
                                isBoss = node.isBoss,
                                onClick = { if (isUnlocked) onLevelClick(node.stageNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modern Bottom Floating Dock:
 * - Elevated off the screen bottom slightly (8dp padding), 24dp rounded corners, subtle translucent dark background
 * - Active tab indicator: smooth sliding pill / soft glow under the selected icon
 * - Centerpiece: Big juicy themed "Play Stage X" Button with breathing pulse and 3D specular highlight
 * - Badge notifications: tiny pulsing dot on shop/rewards
 * - Micro-haptics / scale bounce on tab tap for tactile feel
 */
@Composable
private fun ModernBottomFloatingDock(
    theme: ZoneVisualTheme,
    currentLevel: Int,
    playPulse: Float,
    onHomeClick: () -> Unit,
    onEndlessClick: () -> Unit,
    onPlayClick: () -> Unit,
    onVaultClick: () -> Unit,
    onShopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Pulsing notification dot for rewards/shop
    val infiniteTransition = rememberInfiniteTransition(label = "bottom_dock_pulse")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shop_dot_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x99000000))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF00D1526),
                        Color(0xFA070B14)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.verticalGradient(
                    listOf(
                        Color(0x55FFFFFF),
                        Color(0x15FFFFFF),
                        theme.primaryAccent.copy(alpha = 0.35f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Top Gloss Highlight Arc
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.90f)
                .height(12.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0x30FFFFFF), Color(0x00FFFFFF))
                    )
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home / Map (Active)
            DockItem(
                icon = "🏠",
                label = "Map",
                isActive = true,
                activeAccentColor = theme.primaryAccent,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onHomeClick()
                },
                testTag = "bottom_home_btn",
                modifier = Modifier.weight(0.85f)
            )

            // Tab 2: Endless Mode
            DockItem(
                icon = "♾️",
                label = "Endless",
                isActive = false,
                activeAccentColor = Color(0xFF00E5FF),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEndlessClick()
                },
                testTag = "bottom_endless_btn",
                modifier = Modifier.weight(0.85f)
            )

            // Centerpiece: Big Juicy Themed "Play Stage X" Primary Action
            val playInteraction = remember { MutableInteractionSource() }
            val playPressed by playInteraction.collectIsPressedAsState()
            val playPressScale by animateFloatAsState(
                targetValue = if (playPressed) 0.94f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "play_btn_press"
            )

            Box(
                modifier = Modifier
                    .weight(2.05f)
                    .height(50.dp)
                    .scale(playPulse * playPressScale)
                    .shadow(12.dp, RoundedCornerShape(25.dp), spotColor = theme.playButtonSpotColor)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Brush.verticalGradient(theme.playButtonGradient))
                    .border(2.dp, theme.playButtonBorderColor, RoundedCornerShape(25.dp))
                    .clickable(
                        interactionSource = playInteraction,
                        indication = null
                    ) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onPlayClick()
                    }
                    .testTag("bottom_center_play_btn")
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                // Top Gloss Crescent Highlight
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.85f)
                        .height(15.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x80FFFFFF), Color(0x00FFFFFF))
                            )
                        )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Text(text = theme.playButtonBadge, fontSize = 13.sp)
                    Text(
                        text = "Play Stage $currentLevel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.1.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Tab 3: Vault / Customize
            DockItem(
                icon = "⚡",
                label = "Vault",
                isActive = false,
                activeAccentColor = Color(0xFFA855F7),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onVaultClick()
                },
                testTag = "bottom_vault_btn",
                modifier = Modifier.weight(0.85f)
            )

            // Tab 4: Shop (with tiny pulsing notification dot)
            DockItem(
                icon = "🛒",
                label = "Shop",
                isActive = false,
                activeAccentColor = Color(0xFF38BDF8),
                hasNotification = true,
                dotPulse = dotPulse,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShopClick()
                },
                testTag = "bottom_shop_btn",
                modifier = Modifier.weight(0.85f)
            )
        }
    }
}

@Composable
private fun DockItem(
    icon: String,
    label: String,
    isActive: Boolean,
    activeAccentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    hasNotification: Boolean = false,
    dotPulse: Float = 1.0f
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_item_press"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.scale(if (isActive) 1.08f else 1.0f)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                color = if (isActive) Color.White else Color(0xFF94A3B8)
            )

            // Active Tab Indicator: Smooth glowing pill under selected tab
            if (isActive) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(3.dp)
                        .shadow(4.dp, RoundedCornerShape(2.dp), spotColor = activeAccentColor)
                        .clip(RoundedCornerShape(2.dp))
                        .background(activeAccentColor)
                )
            } else {
                Spacer(modifier = Modifier.height(5.dp))
            }
        }

        // Tiny pulsing notification dot
        if (hasNotification) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(7.dp)
                    .scale(dotPulse)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
                    .border(1.dp, Color.White, CircleShape)
            )
        }
    }
}
