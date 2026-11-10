package com.example.balloonpop.ui.dialogs

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import com.example.balloonpop.ui.components.CurrencyArt
import com.example.balloonpop.ui.components.JewelButton
import com.example.balloonpop.ui.components.JewelButtonStyle
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.CreamSurface
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.NunitoFontFamily
import com.example.balloonpop.ui.theme.PlumText
import com.example.balloonpop.ui.theme.StoryGold

data class DailyGiftDefinition(
    val day: Int,
    val title: String,
    val rewardSummary: String,
    val iconType: String,
    val amountText: String
)

val DAILY_GIFTS_TRACK = listOf(
    DailyGiftDefinition(1, "Day 1", "50 Coins", "coin", "+50"),
    DailyGiftDefinition(2, "Day 2", "100 Coins", "coin", "+100"),
    DailyGiftDefinition(3, "Day 3", "Freeze", "freeze", "+1"),
    DailyGiftDefinition(4, "Day 4", "150 Coins", "coin", "+150"),
    DailyGiftDefinition(5, "Day 5", "10 Gems", "gem", "+10"),
    DailyGiftDefinition(6, "Day 6", "250 Coins", "coin", "+250"),
    DailyGiftDefinition(7, "Day 7", "Royal Chest", "chest", "500🪙 + 25💎 + 5⚡")
)

/**
 * 7-Day Daily Gifts Calendar Dialog
 * Studio finish with Golden Storybook finish: CreamSurface, StoryGold borders,
 * Jewel cards, gold check seal for claimed days, and a prominent Day 7 Chest.
 */
@Composable
fun DailyGiftsDialog(
    currentDay: Int, // 1..7
    canClaimToday: Boolean,
    streakResetNotice: Boolean = false,
    onClaimToday: () -> Unit,
    onDismiss: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "dailyPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .widthIn(max = 420.dp)
                .shadow(DesignTokens.ElevationStrong, DesignTokens.ShapeCardStory)
                .clip(DesignTokens.ShapeCardStory)
                .background(CreamSurface)
                .border(2.5.dp, StoryGold, DesignTokens.ShapeCardStory)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Row with Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(36.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎁 DAILY GIFTS 🎁",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = PlumText,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Log in every day for royal rewards!",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7C6F93)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x18171233))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PlumText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Streak Reset notice banner if applicable
                if (streakResetNotice) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF1F2))
                            .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Streak reset — Day 1 is waiting for you tomorrow!",
                            fontFamily = NunitoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBE123C),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Grid: Days 1–6 (2 rows of 3)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row 1: Days 1, 2, 3
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (d in 1..3) {
                            val def = DAILY_GIFTS_TRACK[d - 1]
                            val isClaimed = d < currentDay || (d == currentDay && !canClaimToday)
                            val isTodayClaimable = (d == currentDay && canClaimToday)
                            DayGiftCard(
                                def = def,
                                isClaimed = isClaimed,
                                isToday = isTodayClaimable,
                                pulseScale = if (isTodayClaimable) pulseScale else 1f,
                                onClick = { if (isTodayClaimable) onClaimToday() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Row 2: Days 4, 5, 6
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (d in 4..6) {
                            val def = DAILY_GIFTS_TRACK[d - 1]
                            val isClaimed = d < currentDay || (d == currentDay && !canClaimToday)
                            val isTodayClaimable = (d == currentDay && canClaimToday)
                            DayGiftCard(
                                def = def,
                                isClaimed = isClaimed,
                                isToday = isTodayClaimable,
                                pulseScale = if (isTodayClaimable) pulseScale else 1f,
                                onClick = { if (isTodayClaimable) onClaimToday() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Day 7: Featured Royal Chest Card (Full Width)
                val day7Def = DAILY_GIFTS_TRACK[6]
                val isDay7Claimed = currentDay < 7 && !canClaimToday // when wrapped, or past
                val isDay7Today = currentDay == 7 && canClaimToday
                Day7FeaturedCard(
                    def = day7Def,
                    isClaimed = isDay7Claimed,
                    isToday = isDay7Today,
                    pulseScale = if (isDay7Today) pulseScale else 1f,
                    onClick = { if (isDay7Today) onClaimToday() }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Action CTA
                if (canClaimToday) {
                    JewelButton(
                        text = "CLAIM DAY $currentDay REWARD",
                        style = JewelButtonStyle.SUNSET,
                        onClick = onClaimToday,
                        testTag = "daily_gift_claim_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    JewelButton(
                        text = "NEXT GIFT TOMORROW ⏱️",
                        style = JewelButtonStyle.MINT,
                        enabled = false,
                        onClick = {},
                        testTag = "daily_gift_claimed_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DayGiftCard(
    def: DailyGiftDefinition,
    isClaimed: Boolean,
    isToday: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    val bgBrush = when {
        isToday -> DesignTokens.SunsetGradient
        isClaimed -> Brush.verticalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFAF7EE), Color(0xFFF3EEDC)))
    }

    val borderColor = when {
        isToday -> StoryGold
        isClaimed -> Color(0xFF8B5CF6)
        else -> Color(0xFFE2D9BC)
    }

    Box(
        modifier = modifier
            .scale(pulseScale)
            .height(106.dp)
            .shadow(if (isToday) 8.dp else 2.dp, RoundedCornerShape(14.dp), spotColor = if (isToday) StoryGold else Color(0x33000000))
            .clip(RoundedCornerShape(14.dp))
            .background(bgBrush)
            .border(if (isToday) 2.5.dp else 1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Day label
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isToday) Color(0x33FFFFFF) else Color(0x22171233))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = def.title,
                    fontFamily = Baloo2FontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isToday) Color.White else PlumText
                )
            }

            // Icon representation
            Box(
                modifier = Modifier.size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isClaimed) {
                    // Gold Check Seal
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFFEA00), StoryGold, Color(0xFFB45309))))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    when (def.iconType) {
                        "coin" -> CurrencyArt(currencyType = "coin", modifier = Modifier.size(28.dp))
                        "gem" -> CurrencyArt(currencyType = "gem", modifier = Modifier.size(28.dp))
                        "freeze" -> Text(text = "❄️", fontSize = 24.sp)
                        else -> Text(text = "🎁", fontSize = 24.sp)
                    }
                }
            }

            // Amount summary
            Text(
                text = if (isClaimed) "CLAIMED" else def.amountText,
                fontFamily = Baloo2FontFamily,
                fontSize = if (isClaimed) 10.sp else 12.sp,
                fontWeight = FontWeight.Black,
                color = if (isToday) Color(0xFFFFD166) else if (isClaimed) Color(0xFF6D28D9) else PlumText,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun Day7FeaturedCard(
    def: DailyGiftDefinition,
    isClaimed: Boolean,
    isToday: Boolean,
    pulseScale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }

    val bgBrush = when {
        isToday -> Brush.horizontalGradient(
            listOf(
                Color(0xFF3B184F),
                Color(0xFF5B2266),
                Color(0xFF3B184F)
            )
        )
        isClaimed -> Brush.horizontalGradient(
            listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE), Color(0xFFEDE9FE))
        )
        else -> Brush.horizontalGradient(
            listOf(
                Color(0xFF261938),
                Color(0xFF381F4A),
                Color(0xFF261938)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .shadow(if (isToday) 10.dp else 4.dp, RoundedCornerShape(16.dp), spotColor = StoryGold)
            .clip(RoundedCornerShape(16.dp))
            .background(bgBrush)
            .border(2.5.dp, StoryGold, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Chest / Check seal
                if (isClaimed) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(Color(0xFFFFEA00), StoryGold, Color(0xFFB45309))))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✓", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                } else {
                    Text(text = "👑🎁", fontSize = 32.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "DAY 7 \u2022 ROYAL CHEST",
                            fontFamily = Baloo2FontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isClaimed) Color(0xFF5B21B6) else Color(0xFFFFD166)
                        )
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFFFD166))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "READY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E0A12)
                                )
                            }
                        }
                    }
                    Text(
                        text = "500 Coins + 25 Gems + 5 Boosters (1 each)",
                        fontFamily = NunitoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isClaimed) Color(0xFF4C1D95) else Color.White
                    )
                }
            }

            if (isClaimed) {
                Text(
                    text = "CLAIMED",
                    fontFamily = Baloo2FontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5B21B6)
                )
            } else {
                Text(
                    text = "🏆",
                    fontSize = 22.sp
                )
            }
        }
    }
}
