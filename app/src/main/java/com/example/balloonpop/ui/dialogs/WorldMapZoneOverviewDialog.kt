package com.example.balloonpop.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.balloonpop.model.GameConstants
import com.example.balloonpop.model.WorldZone
import com.example.balloonpop.ui.theme.ZoneNodeShapeType
import com.example.balloonpop.ui.theme.ZoneVisualTheme
import com.example.balloonpop.ui.theme.ZoneVisualThemeRegistry

/**
 * Fullscreen World Map / Realm Selection Dialog.
 * Showcases each zone's unique visual identity, distinct background art palette,
 * path style, node shapes, star progression, and lock/unlock state.
 */
@Composable
fun WorldMapZoneOverviewDialog(
    selectedZoneIndex: Int,
    currentUnlockedLevel: Int,
    completedLevels: Map<Int, Int>,
    onSelectZone: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9040814))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxSize(0.92f)
                .shadow(24.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF00F0FF))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F172A),
                            Color(0xFF090D18),
                            Color(0xFF050810)
                        )
                    )
                )
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {} // Prevent closing when tapping inside
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "🗺️", fontSize = 22.sp)
                            Text(
                                text = "WORLD MAP",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Explore & select realm environments",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .clickable { onClose() }
                            .testTag("close_world_map_dialog"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close World Map",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable List of All World Zones
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    itemsIndexed(GameConstants.WORLD_ZONES) { index, zone ->
                        val isCurrentZone = (index == selectedZoneIndex)
                        val isUnlocked = (currentUnlockedLevel >= zone.startLevel)
                        val zoneTheme = ZoneVisualThemeRegistry.getThemeForZone(index, zone)

                        // Calculate stars in this zone
                        val starsInZone = (zone.startLevel..zone.endLevel).sumOf { completedLevels[it] ?: 0 }
                        val maxZoneStars = (zone.endLevel - zone.startLevel + 1) * 3

                        ZoneSelectionCard(
                            zone = zone,
                            theme = zoneTheme,
                            zoneIndex = index,
                            isCurrentZone = isCurrentZone,
                            isUnlocked = isUnlocked,
                            starsEarned = starsInZone,
                            maxStars = maxZoneStars,
                            currentUnlockedLevel = currentUnlockedLevel,
                            onSelect = {
                                if (isUnlocked) {
                                    onSelectZone(index)
                                    onClose()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Distinct preview card representing a single zone with its unique visual style.
 */
@Composable
private fun ZoneSelectionCard(
    zone: WorldZone,
    theme: ZoneVisualTheme,
    zoneIndex: Int,
    isCurrentZone: Boolean,
    isUnlocked: Boolean,
    starsEarned: Int,
    maxStars: Int,
    currentUnlockedLevel: Int,
    onSelect: () -> Unit
) {
    val cardShape = when (theme.nodeShape) {
        ZoneNodeShapeType.FACETED_CRYSTAL_SHARD -> CutCornerShape(16.dp)
        else -> RoundedCornerShape(18.dp)
    }

    val borderColor = when {
        isCurrentZone -> theme.primaryAccent
        isUnlocked -> theme.titlePillBorder
        else -> Color(0xFF1E293B)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCurrentZone) 12.dp else 4.dp,
                shape = cardShape,
                spotColor = if (isUnlocked) theme.primaryAccent else Color.Transparent
            )
            .clip(cardShape)
            .background(
                Brush.horizontalGradient(
                    if (isUnlocked) theme.bgSkyGradient else listOf(Color(0xFF0C101A), Color(0xFF080C14))
                )
            )
            .border(
                width = if (isCurrentZone) 2.dp else 1.dp,
                color = borderColor,
                shape = cardShape
            )
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("zone_card_${zone.zoneNumber}")
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Thematic Realm Emblem & Metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Thematic Zone Emblem Node Preview
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(
                            if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD) CutCornerShape(12.dp)
                            else CircleShape
                        )
                        .background(
                            Brush.verticalGradient(
                                if (isUnlocked) theme.unlockedBgColors else theme.lockedBgColors
                            )
                        )
                        .border(
                            1.5.dp,
                            if (isUnlocked) theme.primaryAccent else theme.lockedBorderColor,
                            if (theme.nodeShape == ZoneNodeShapeType.FACETED_CRYSTAL_SHARD) CutCornerShape(12.dp)
                            else CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isUnlocked) theme.titleOrnamentPrefix else theme.lockedIconSymbol,
                        fontSize = 22.sp
                    )
                }

                // Text Description
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ZONE ${zone.zoneNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) theme.titleAccentColor else Color(0xFF64748B)
                        )
                        if (isCurrentZone) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.primaryAccent.copy(alpha = 0.25f))
                                    .border(1.dp, theme.primaryAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Text(
                        text = zone.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isUnlocked) Color.White else Color(0xFF94A3B8)
                    )

                    Text(
                        text = theme.subtitle,
                        fontSize = 11.sp,
                        color = if (isUnlocked) theme.secondaryAccent else Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stages Range & Stars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Stages ${zone.startLevel} - ${zone.endLevel}",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                        if (isUnlocked) {
                            Text(
                                text = "★ $starsEarned / $maxStars",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }

            // Right: Action / Status Button
            if (isUnlocked) {
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCurrentZone) theme.primaryAccent.copy(alpha = 0.3f) else theme.primaryAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("enter_zone_btn_${zone.zoneNumber}")
                ) {
                    Text(
                        text = if (isCurrentZone) "VIEW" else "ENTER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Stage ${zone.startLevel}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
