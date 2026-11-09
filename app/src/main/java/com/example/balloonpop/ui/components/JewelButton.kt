package com.example.balloonpop.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.balloonpop.ui.theme.Baloo2FontFamily
import com.example.balloonpop.ui.theme.DesignTokens
import com.example.balloonpop.ui.theme.PlumButton
import com.example.balloonpop.ui.theme.PlumButtonShelf
import com.example.balloonpop.ui.theme.StoryBerry
import com.example.balloonpop.ui.theme.StoryBerryShelf
import com.example.balloonpop.ui.theme.StoryGold
import com.example.balloonpop.ui.theme.StoryGoldShelf
import com.example.balloonpop.ui.theme.StoryMint
import com.example.balloonpop.ui.theme.StoryMintShelf
import com.example.balloonpop.ui.theme.SunsetBottom
import com.example.balloonpop.ui.theme.SunsetShelf
import com.example.balloonpop.ui.theme.SunsetTop

/** Colour families for [JewelButton]. Sunset is the one CTA colour (PLAY/Next/Retry). */
enum class JewelButtonStyle(
    val top: Color,
    val bottom: Color,
    val shelf: Color,
    val textColor: Color
) {
    SUNSET(SunsetTop, SunsetBottom, SunsetShelf, Color.White),
    // Pass 6: sky family for the pause menu's Settings action.
    SKY(Color(0xFF7ED3FC), Color(0xFF2F7FD0), Color(0xFF1E40AF), Color.White),
    GOLD(Color(0xFFFFD166), StoryGold, StoryGoldShelf, Color(0xFF2A1B4E)),
    MINT(Color(0xFF6FEBC0), StoryMint, StoryMintShelf, Color(0xFF0B2E23)),
    BERRY(Color(0xFFFF7A97), StoryBerry, StoryBerryShelf, Color.White),
    PLUM(Color(0xFF4A3A85), PlumButton, PlumButtonShelf, Color.White)
}

/**
 * Pass 5 physical button (research rule #2): a vertical-gradient face on a
 * darker shelf ([DesignTokens.ShelfDepth] deep). Pressing sinks the face into
 * the shelf. A gloss highlight (white at [DesignTokens.GlossAlpha]) sweeps the
 * top of the face. Used for every primary CTA in the game.
 */
@Composable
fun JewelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: JewelButtonStyle = JewelButtonStyle.SUNSET,
    enabled: Boolean = true,
    minHeight: Dp = DesignTokens.ButtonMinHeight,
    shape: Shape = DesignTokens.ShapeButtonStory,
    testTag: String? = null,
    // Pass 6: lets a caller put dark text on a bright face (fail-dialog Retry).
    textColorOverride: Color? = null,
    leading: (@Composable RowScope.() -> Unit)? = null
) {
    JewelButtonContainer(
        onClick = onClick,
        modifier = modifier,
        style = style,
        enabled = enabled,
        minHeight = minHeight,
        shape = shape,
        testTag = testTag
    ) {
        if (leading != null) leading()
        Text(
            text = text,
            color = if (enabled) (textColorOverride ?: style.textColor) else (textColorOverride ?: style.textColor).copy(alpha = 0.55f),
            fontFamily = Baloo2FontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.25f),
                    offset = Offset(0f, 2f),
                    blurRadius = 2f
                )
            ),
            maxLines = 1
        )
    }
}

/** Container half of [JewelButton] for callers that need custom face content. */
@Composable
fun JewelButtonContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: JewelButtonStyle = JewelButtonStyle.SUNSET,
    enabled: Boolean = true,
    minHeight: Dp = DesignTokens.ButtonMinHeight,
    shape: Shape = DesignTokens.ShapeButtonStory,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val sink by animateDpAsState(
        targetValue = if (pressed && enabled) DesignTokens.ShelfDepth else 0.dp,
        animationSpec = DesignTokens.ButtonPressSpringDp,
        label = "jewelSink"
    )
    val faceAlpha = if (enabled) 1f else 0.55f

    Box(
        modifier = modifier
            .padding(bottom = DesignTokens.ShelfDepth)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        // Shelf (the darker base edge the face sits on)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset(y = DesignTokens.ShelfDepth)
                .clip(shape)
                .background(style.shelf.copy(alpha = faceAlpha))
        )
        // Face
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight)
                .offset(y = sink)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            style.top.copy(alpha = faceAlpha),
                            style.bottom.copy(alpha = faceAlpha)
                        )
                    )
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            content()
        }
        // Gloss highlight over the top of the face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(minHeight * 0.52f)
                .offset(y = sink)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = DesignTokens.GlossAlpha),
                            Color.White.copy(alpha = 0f)
                        )
                    )
                )
        )
    }
}
