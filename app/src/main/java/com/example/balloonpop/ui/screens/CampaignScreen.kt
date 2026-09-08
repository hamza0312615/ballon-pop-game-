package com.example.balloonpop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.WorldZone
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.dialogs.PreGameStageDialog
import com.example.balloonpop.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CampaignScreen(
    completedLevels: Map<Int, Int>,
    claimedMilestoneChests: Set<Int> = emptySet(),
    coins: Int = 150,
    gems: Int = 10,
    onSelectLevel: (Int, Set<String>) -> Unit,
    onClaimMilestoneChest: (Int) -> Unit = {},
    onOpenSkillVault: () -> Unit = {},
    onOpenDailySpin: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenQuests: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Current highest unlocked level
    val maxCompleted = completedLevels.keys.maxOrNull() ?: 0
    val currentUnlockedLevel = (maxCompleted + 1).coerceAtMost(150)
    val currentZoneIndex = (currentUnlockedLevel - 1) / 10
    val initialReversedIndex = (GameConstants.WORLD_ZONES.size - 1 - currentZoneIndex).coerceIn(0, GameConstants.WORLD_ZONES.size - 1)

    // Pre-game dialog state for tapped level
    var selectedLevelForDialog by remember { mutableStateOf<Int?>(null) }

    // Start list directly at player's current zone with zero delay
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialReversedIndex)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A), // Cosmic sky top
                        Color(0xFF1E1B4B),
                        Color(0xFF312E81),
                        Color(0xFF38BDF8), // Sunny sky bottom
                        Color(0xFF7DD3FC)
                    )
                )
            )
    ) {
        // Vertical Scrollable World Map with Floating Thematic Islands
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 76.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 120.dp, bottom = 24.dp)
        ) {
            // Render zones in reverse order (5 -> 1) so player starts at Island 1 at bottom and climbs up!
            items(GameConstants.WORLD_ZONES.size) { index ->
                val zone = GameConstants.WORLD_ZONES[GameConstants.WORLD_ZONES.size - 1 - index]
                FloatingWorldIsland(
                    zone = zone,
                    completedLevels = completedLevels,
                    currentUnlockedLevel = currentUnlockedLevel,
                    claimedMilestoneChests = claimedMilestoneChests,
                    onStageClicked = { lvl -> selectedLevelForDialog = lvl },
                    onClaimChest = { onClaimMilestoneChest(zone.endLevel) }
                )
            }
        }

        // Top Fixed Quick-Play & Zone Jump Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xF00D0B20), Color(0xCC0D0B20), Color.Transparent)
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Hero Quick Play Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                        )
                    )
                    .border(1.5.dp, Color(0xFF67E8F9), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                        .clickable { onClose() }
                        .testTag("campaign_back_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Current Stage Info & Direct Play Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFFB703), Color(0xFFFB8500))
                            )
                        )
                        .clickable { selectedLevelForDialog = currentUnlockedLevel }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("quick_play_stage_btn")
                ) {
                    Text(text = "STAGE $currentUnlockedLevel", color = Color(0xFF2A1100), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(text = "▶ PLAY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }

                // Jump To Stage Action
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x3300F0FF))
                        .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(initialReversedIndex)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("jump_to_stage_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯 Go to Stage", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // World Zone Fast Navigation Strip
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(GameConstants.WORLD_ZONES) { zoneIndex, zone ->
                    val isZoneUnlocked = currentUnlockedLevel >= zone.startLevel
                    val isCurrentZone = zoneIndex == currentZoneIndex
                    val bg = if (isCurrentZone) {
                        Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFF0284C7)))
                    } else if (isZoneUnlocked) {
                        Brush.horizontalGradient(listOf(Color(0x661E293B), Color(0x66334155)))
                    } else {
                        Brush.horizontalGradient(listOf(Color(0x33000000), Color(0x33000000)))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(
                                width = if (isCurrentZone) 1.5.dp else 0.5.dp,
                                color = if (isCurrentZone) Color.White else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                val targetReversed = (GameConstants.WORLD_ZONES.size - 1 - zoneIndex).coerceAtLeast(0)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetReversed)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = zone.icon, fontSize = 12.sp)
                            Text(
                                text = "Z${zone.zoneNumber}: ${zone.name}",
                                color = if (isCurrentZone) Color.Black else if (isZoneUnlocked) Color.White else Color(0x88FFFFFF),
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentZone) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Bottom Circular Quick-Action Toolbar (as seen in Casual Game World Maps)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0x990A0518), Color(0xEE0A0518))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF59E0B),
                                Color(0xFFD97706),
                                Color(0xFFB45309)
                            )
                        )
                    )
                    .border(2.5.dp, Color(0xFFFEF3C7), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapCircleButton(icon = "⚙️", label = "Settings", onClick = onOpenSettings, testTag = "map_settings_btn")
                MapCircleButton(icon = "📜", label = "Quests", onClick = onOpenQuests, testTag = "map_quests_btn")
                MapCircleButton(icon = "⚡", label = "Skills", onClick = onOpenSkillVault, testTag = "map_skills_btn")
                MapCircleButton(icon = "🎡", label = "Spin", onClick = onOpenDailySpin, testTag = "map_spin_btn")
                MapCircleButton(icon = "🛒", label = "Shop", onClick = onOpenShop, testTag = "map_shop_btn")
            }
        }

        // Pre-Game Stage Lobby Modal
        selectedLevelForDialog?.let { level ->
            val starsEarned = completedLevels[level] ?: 0
            val targetPops = 15 + level * 2
            val targetScore = level * 1000

            PreGameStageDialog(
                level = level,
                starsEarned = starsEarned,
                targetScore = targetScore,
                targetPops = targetPops,
                userCoins = coins,
                onStartLevel = { boosters ->
                    selectedLevelForDialog = null
                    onSelectLevel(level, boosters)
                },
                onClose = { selectedLevelForDialog = null }
            )
        }
    }
}

@Composable
private fun FloatingWorldIsland(
    zone: WorldZone,
    completedLevels: Map<Int, Int>,
    currentUnlockedLevel: Int,
    claimedMilestoneChests: Set<Int>,
    onStageClicked: (Int) -> Unit,
    onClaimChest: () -> Unit
) {
    val startLvl = zone.startLevel
    val endLvl = zone.endLevel
    val isZoneUnlocked = startLvl <= currentUnlockedLevel
    val isZoneCompleted = (startLvl..endLvl).all { completedLevels.containsKey(it) }
    val isChestClaimed = claimedMilestoneChests.contains(endLvl)

    val islandGradient = when ((zone.zoneNumber - 1) % 10) {
        0 -> listOf(Color(0xFF86EFAC), Color(0xFF22C55E), Color(0xFF15803D)) // Lush Grass Meadow
        1 -> listOf(Color(0xFFFED7AA), Color(0xFFFB923C), Color(0xFFC2410C)) // Sunset Orange Mesa
        2 -> listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0369A1)) // Aqua Reef
        3 -> listOf(Color(0xFFFBCFE8), Color(0xFFF472B6), Color(0xFFDB2777)) // Candy Pink
        4 -> listOf(Color(0xFFA7F3D0), Color(0xFF10B981), Color(0xFF047857)) // Cyber Neon
        5 -> listOf(Color(0xFFFECDD3), Color(0xFFF43F5E), Color(0xFFBE123C)) // Volcano Crimson
        6 -> listOf(Color(0xFFDDD6FE), Color(0xFF8B5CF6), Color(0xFF5B21B6)) // Starry Cosmos
        7 -> listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFB45309)) // Golden Citadel
        8 -> listOf(Color(0xFFCFFAFE), Color(0xFF06B6D4), Color(0xFF0E7490)) // Frozen Tundra
        else -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF6B21A8)) // Celestial Violet
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = Color(0x60000000))
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    if (isZoneUnlocked) islandGradient else listOf(Color(0xFF64748B), Color(0xFF334155))
                )
            )
            .border(
                width = 3.dp,
                color = if (isZoneUnlocked) Color(0xFFFEF3C7) else Color(0xFF94A3B8),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Island Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = zone.icon, fontSize = 22.sp)
                }

                Column {
                    Text(
                        text = zone.name.uppercase(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "World Zone ${zone.zoneNumber} • Stages $startLvl - $endLvl",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFBEB)
                    )
                }
            }

            // Milestone Reward Chest
            if (isZoneCompleted) {
                if (isChestClaimed) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x80000000))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = "CHEST OPENED ✓", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF4ADE80))
                    }
                } else {
                    JellyButton(
                        text = "CLAIM 🎁",
                        onClick = onClaimChest,
                        height = 36.dp,
                        fontSize = 11,
                        colorVariant = JellyColorVariant.ORANGE,
                        testTag = "claim_chest_${endLvl}"
                    )
                }
            }
        }

        // Winding Snaking Path of Stages
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x35000000))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Divide 10 stages into rows of 3, 4, 3 with zigzag snaking
            val row1 = (startLvl..(startLvl + 2)).toList()
            val row2 = ((startLvl + 3)..(startLvl + 6)).toList().reversed() // Reversed for snake curve
            val row3 = ((startLvl + 7)..endLvl).toList()

            StageRow(
                levels = row1,
                completedLevels = completedLevels,
                currentUnlockedLevel = currentUnlockedLevel,
                onStageClicked = onStageClicked
            )

            StageRow(
                levels = row2,
                completedLevels = completedLevels,
                currentUnlockedLevel = currentUnlockedLevel,
                onStageClicked = onStageClicked
            )

            StageRow(
                levels = row3,
                completedLevels = completedLevels,
                currentUnlockedLevel = currentUnlockedLevel,
                onStageClicked = onStageClicked
            )
        }
    }
}

@Composable
private fun StageRow(
    levels: List<Int>,
    completedLevels: Map<Int, Int>,
    currentUnlockedLevel: Int,
    onStageClicked: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (lvl in levels) {
            val isUnlocked = lvl <= currentUnlockedLevel
            val isCurrent = lvl == currentUnlockedLevel
            val stars = completedLevels[lvl] ?: 0

            StageNodeCasual(
                level = lvl,
                isUnlocked = isUnlocked,
                isCurrent = isCurrent,
                stars = stars,
                onClick = { if (isUnlocked) onStageClicked(lvl) }
            )
        }
    }
}

@Composable
private fun StageNodeCasual(
    level: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Mascot indicator sitting on top of current active stage!
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .scale(pulseScale)
            ) {
                MascotCharacter(mood = MascotMood.JOYFUL, size = 32.dp)
            }
        }

        // Circular 3D Node
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(
                    elevation = if (isUnlocked) 6.dp else 1.dp,
                    shape = CircleShape,
                    spotColor = if (isCurrent) Color(0xFFF59E0B) else Color(0x50000000)
                )
                .clip(CircleShape)
                .background(
                    when {
                        isCurrent -> Brush.radialGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                        isUnlocked -> Brush.radialGradient(
                            listOf(Color(0xFF4ADE80), Color(0xFF22C55E), Color(0xFF15803D))
                        )
                        else -> Brush.radialGradient(
                            listOf(Color(0xFF94A3B8), Color(0xFF475569))
                        )
                    }
                )
                .border(
                    width = if (isCurrent) 3.dp else 2.dp,
                    color = if (isCurrent) Color(0xFFFFFBEB) else if (isUnlocked) Color(0xFF86EFAC) else Color(0xFFCBD5E1),
                    shape = CircleShape
                )
                .clickable(enabled = isUnlocked) { onClick() }
                .testTag("stage_node_$level"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) "$level" else "🔒",
                fontSize = if (isUnlocked) 16.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        // 3 Mini Stars below the stage node
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            for (s in 1..3) {
                Text(
                    text = if (s <= stars) "⭐" else "☆",
                    fontSize = 8.sp,
                    color = if (s <= stars) Color(0xFFFFEA00) else Color(0xFFCBD5E1)
                )
            }
        }
    }
}

@Composable
private fun MapCircleButton(
    icon: String,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.clickable { onClick() }.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                    )
                )
                .border(2.dp, Color(0xFFD97706), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
