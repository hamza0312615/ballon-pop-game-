package com.example.balloonpop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import com.example.balloonpop.ui.theme.CoralPrimary
import com.example.balloonpop.ui.theme.DesignTokens
import androidx.compose.ui.draw.rotate
import com.example.balloonpop.ui.theme.SunsetBottom
import com.example.balloonpop.ui.theme.StoryGold
import com.example.balloonpop.ui.theme.ZoneVisualTheme

/**
 * UNIFIED LEVEL-MAP NODE SYSTEM (Quality Pass 4) — the ONLY node renderer for the
 * campaign map, identical in every one of the 20 zones (Candy Crush / Royal Match style).
 * Zone themes only tint the (separate) scenic background; nodes never change shape,
 * size or colour logic per zone.
 *
 * Fixed slot: 108dp x 120dp, orb centred exactly on the slot centre so the winding
 * path drawn by CampaignScreen always meets the orb centre.
 * - Cleared  -> 62dp gold orb (DesignTokens.GoldGradient), stage number inside,
 *               earned stars (gold) / unearned stars (grey) in a row beneath it.
 * - Unlocked -> 62dp coral orb with the stage number (reachable but not cleared).
 * - Locked   -> 62dp grey orb with a lock glyph, stage number in a muted pill beneath.
 * - Current  -> see [ThemedLadderActiveHeroNode]: 68dp pulsing coral orb + Popster.
 * Boss stages (every 10th) get a small crown badge; milestone stages a tiny gold dot.
 */
private val MapNodeSlotWidth = 108.dp
private val MapNodeSlotHeight = 120.dp
private val MapOrbSize = 62.dp
private val MapHeroOrbSize = 68.dp

@Composable
fun ThemedLadderTileNode(
    theme: ZoneVisualTheme,
    stageNumber: Int,
    stars: Int,
    isUnlocked: Boolean,
    isCleared: Boolean,
    isMilestone: Boolean,
    isBoss: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "node_press"
    )

    // Gentle bob for the earned-stars row only (kept from the previous design).
    val infiniteTransition = rememberInfiniteTransition(label = "star_float")
    val starBob by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_bob"
    )

    Box(
        modifier = Modifier
            .size(width = MapNodeSlotWidth, height = MapNodeSlotHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isUnlocked
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // --- Orb ---
        Box(
            modifier = Modifier
                .size(MapOrbSize)
                .scale(pressScale)
                .shadow(
                    elevation = DesignTokens.ElevationMedium,
                    shape = CircleShape,
                    spotColor = when {
                        isCleared -> DesignTokens.StarGoldGlow
                        isUnlocked -> CoralPrimary
                        else -> Color(0x66000000)
                    }
                )
                .clip(CircleShape)
                .background(
                    when {
                        isCleared -> DesignTokens.GoldGradient
                        isUnlocked -> DesignTokens.SunsetGradient
                        else -> Brush.verticalGradient(
                            listOf(Color(0xFF4A4460), Color(0xFF322C47), Color(0xFF241F38))
                        )
                    }
                )
                .border(
                    width = if (isBoss) 3.dp else 2.dp,
                    brush = Brush.verticalGradient(
                        when {
                            isCleared -> listOf(Color.White, DesignTokens.StarGoldFilled, DesignTokens.PrimaryBrandDark)
                            isUnlocked -> listOf(Color.White, SunsetBottom)
                            else -> listOf(Color(0x66FFFFFF), Color(0xFF475569))
                        }
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Top gloss crescent for the chunky 3D orb look.
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.78f)
                    .height(15.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)
                        )
                    )
            )

            if (isUnlocked) {
                Text(
                    text = "$stageNumber",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCleared) Color(0xFF451A03) else Color.White,
                    textAlign = TextAlign.Center
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Boss crown badge on the orb's top-right shoulder.
        if (isBoss) {
            Text(
                text = "👑",
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 27.dp, y = (-27).dp)
            )
        }

        // Milestone marker: tiny gold dot on the orb's top-left shoulder.
        if (isMilestone && !isBoss) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-27).dp, y = (-27).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(DesignTokens.StarGoldFilled)
                    .border(1.dp, Color.White, CircleShape)
            )
        }

        // --- Beneath the orb: earned stars (cleared) or number pill (locked) ---
        if (isCleared) {
            val earnedStars = if (stars in 1..3) stars else 3
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .offset(y = starBob.dp)
            ) {
                repeat(3) { starIdx ->
                    Text(
                        text = "★",
                        fontSize = if (starIdx == 1) 15.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (starIdx < earnedStars) {
                            DesignTokens.StarGoldFilled
                        } else {
                            DesignTokens.StarEmptyGray
                        }
                    )
                }
            }
        } else if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xB30B101A))
                    .border(1.dp, Color(0x33475569), RoundedCornerShape(10.dp))
                    .padding(horizontal = 7.dp, vertical = 1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stageNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Current (next-to-play) stage node — same unified slot as [ThemedLadderTileNode],
 * but enlarged and highlighted:
 * - 68dp coral orb (DesignTokens.CtaGradient) with the stage number, ringed by two
 *   expanding pulse halos driven by [beaconPulse].
 * - The Popster mascot ([MascotCharacter], the same component used on Home) sits on
 *   top of the orb, bobbing with [mascotBob].
 * - A coral "NOW" pill beneath the orb; earned stars (if replaying) are not shown,
 *   matching the previous hero node's behaviour.
 */
@Composable
fun ThemedLadderActiveHeroNode(
    theme: ZoneVisualTheme,
    stageNumber: Int,
    stars: Int,
    mascotBob: Float,
    beaconPulse: Float,
    isMilestone: Boolean,
    isBoss: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "hero_press"
    )

    Box(
        modifier = Modifier
            .size(width = MapNodeSlotWidth, height = MapNodeSlotHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .testTag("active_hero_level_tile"),
        contentAlignment = Alignment.Center
    ) {
        // Pass 5 (amendment 5): expanding pulse RINGS behind the orb — a scale
        // pulse in storybook gold, never a neon-style glow.
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(beaconPulse)
                .clip(CircleShape)
                .border(2.5.dp, StoryGold.copy(alpha = 0.55f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(beaconPulse * 0.88f)
                .clip(CircleShape)
                .border(2.dp, StoryGold.copy(alpha = 0.35f), CircleShape)
        )

        // --- Enlarged current-stage orb ---
        Box(
            modifier = Modifier
                .size(MapHeroOrbSize)
                .scale(pressScale)
                .shadow(
                    elevation = DesignTokens.ElevationActive,
                    shape = CircleShape,
                    spotColor = CoralPrimary
                )
                .clip(CircleShape)
                .background(DesignTokens.SunsetGradient)
                .border(
                    width = if (isBoss) 3.dp else 2.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White, DesignTokens.StarGoldFilled)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.78f)
                    .height(17.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.60f), Color.Transparent)
                        )
                    )
            )

            Text(
                text = "$stageNumber",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Balloon knot: a small tied diamond at the orb's base, so the current
        // node reads as a balloon pin tied to the map's string path.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 40.dp)
                .size(13.dp)
                .rotate(45f)
                .clip(RoundedCornerShape(3.dp))
                .background(SunsetBottom)
                .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(3.dp))
        )

        // Boss crown badge.
        if (isBoss) {
            Text(
                text = "👑",
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = 30.dp, y = (-30).dp)
            )
        }

        // Popster mascot standing on the current orb (same component as Home screen).
        MascotCharacter(
            mood = MascotMood.JOYFUL,
            size = 44.dp,
            showGoldRing = false,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = mascotBob.dp)
        )

        // "NOW" pill beneath the orb.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = SunsetBottom)
                .clip(RoundedCornerShape(14.dp))
                .background(DesignTokens.SunsetGradient)
                .border(1.5.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NOW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
        }
    }
}
