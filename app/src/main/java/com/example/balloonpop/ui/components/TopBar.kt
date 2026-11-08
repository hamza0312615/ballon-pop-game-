package com.example.balloonpop.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.DesignTokens

/**
 * Slim single-row Top Resource Bar (Pass 4):
 * - One row only: lives, coins and gems, each as icon + number + "+" button
 * - Subtle glassmorphic container: soft background, drop shadow for depth
 * - Micro-animation on value changes (bounce pulse)
 * - Soft ambient glow pulse on "+" add buttons
 */
@Composable
fun TopBar(
    coins: Int,
    gems: Int,
    lives: Int = 5,
    maxLives: Int = 5,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pass 4: ONE slim single-row bar. The caller owns the screen-edge padding
    // (Home uses DesignTokens.ScreenPadding); this bar only adds a small inset so
    // it also sits correctly on screens that render it without outer padding.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DesignTokens.Space8, vertical = DesignTokens.Space4)
            .shadow(DesignTokens.ElevationMedium, DesignTokens.ShapeMedium, spotColor = Color(0x66000000))
            .clip(DesignTokens.ShapeMedium)
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
                1.5.dp,
                Brush.verticalGradient(
                    listOf(Color(0x66FFFFFF), Color(0x18FFFFFF), Color(0x3338BDF8))
                ),
                DesignTokens.ShapeMedium
            )
            .padding(horizontal = DesignTokens.Space8, vertical = DesignTokens.Space4)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Lives Heart Segment
            ResourceSegment(
                currencyType = "heart",
                value = "$lives/$maxLives",
                textColor = Color(0xFFFFF1F2),
                showAdd = true,
                onAddClick = onOpenShop,
                onSegmentClick = onOpenShop,
                testTag = "top_lives_badge",
                modifier = Modifier.weight(1f)
            )

            // Subtle vertical glass divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x15FFFFFF), Color(0x44FFFFFF), Color(0x15FFFFFF))
                        )
                    )
            )

            // 2. Coins Segment (icon + number + plus, same as every other segment)
            ResourceSegment(
                currencyType = "coin",
                value = String.format("%,d", coins),
                textColor = DesignTokens.PrimaryBrandBright,
                showAdd = true,
                onAddClick = onOpenShop,
                onSegmentClick = onOpenShop,
                testTag = "top_coins_badge",
                modifier = Modifier.weight(1.15f)
            )

            // Subtle vertical glass divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x15FFFFFF), Color(0x44FFFFFF), Color(0x15FFFFFF))
                        )
                    )
            )

            // 3. Gems Segment
            ResourceSegment(
                currencyType = "gem",
                value = String.format("%,d", gems),
                textColor = DesignTokens.SecondaryAccent,
                showAdd = true,
                onAddClick = onOpenShop,
                onSegmentClick = onOpenShop,
                testTag = "top_gems_badge",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResourceSegment(
    currencyType: String,
    value: String,
    textColor: Color,
    showAdd: Boolean,
    onAddClick: () -> Unit,
    onSegmentClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "segment_press"
    )

    // Micro-animation bounce on value change
    var bounceState by remember { mutableFloatStateOf(1f) }
    var prevValue by remember { mutableStateOf(value) }
    LaunchedEffect(value) {
        if (value != prevValue) {
            prevValue = value
            bounceState = 1.18f
            kotlinx.coroutines.delay(180)
            bounceState = 1f
        }
    }
    val animatedBounce by animateFloatAsState(
        targetValue = bounceState,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "value_bounce"
    )

    // Soft continuous glow pulse for the "+" button
    val infiniteTransition = rememberInfiniteTransition(label = "add_glow")
    val addPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Row(
        modifier = modifier
            .scale(pressScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSegmentClick
            )
            .testTag(testTag)
            .padding(horizontal = DesignTokens.Space4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Uniform 24dp Icon Container
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            CurrencyArt(
                type = currencyType,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(DesignTokens.Space4))

        // Uniform Typography with value bounce
        Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Black,
            fontSize = DesignTokens.SizeBody,
            maxLines = 1,
            modifier = Modifier.scale(animatedBounce)
        )

        if (showAdd) {
            Spacer(modifier = Modifier.width(DesignTokens.Space4))

            // Consistent Add "+" Button with glowing pulse. Kept compact so the bar
            // stays one slim row; the whole segment is also tappable to the shop.
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(addPulse)
                        .shadow(6.dp, CircleShape, spotColor = DesignTokens.SuccessGreenBright)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DesignTokens.SuccessGreenBright,
                                    DesignTokens.SuccessGreen
                                )
                            )
                        )
                        .border(1.2.dp, Color(0xFFA7F3D0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
