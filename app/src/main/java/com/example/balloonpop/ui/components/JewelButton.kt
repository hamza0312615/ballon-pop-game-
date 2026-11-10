package com.example.balloonpop.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.balloonpop.ui.theme.HeroLimeBottom
import com.example.balloonpop.ui.theme.HeroLimeShelf
import com.example.balloonpop.ui.theme.HeroLimeTop
import com.example.balloonpop.ui.theme.ToyOrangeBottom
import com.example.balloonpop.ui.theme.ToyOrangeShelf
import com.example.balloonpop.ui.theme.ToyOrangeTop
import com.example.balloonpop.ui.theme.ToyRedBottom
import com.example.balloonpop.ui.theme.ToyRedShelf
import com.example.balloonpop.ui.theme.ToyRedTop
import com.example.balloonpop.ui.theme.ToyVioletPanel
import com.example.balloonpop.ui.theme.ToyVioletPanelDark

/**
 * Toybox Stage chunky beveled plastic buttons (ToyButton system).
 * The HERO button (SUNSET) is juicy lime green (#A6F750 -> #63C70A with shelf #4A9605).
 * Secondary actions use Toy Orange (#F97D0C) or Toy Violet (#55277D).
 */
enum class JewelButtonStyle(
    val top: Color,
    val bottom: Color,
    val shelf: Color,
    val textColor: Color
) {
    // HERO CTA: Lime Green (PLAY / NEXT / CLAIM / REVIVE)
    SUNSET(HeroLimeTop, HeroLimeBottom, HeroLimeShelf, Color.White),
    // Toy Orange (Secondary actions & prizes)
    GOLD(ToyOrangeTop, ToyOrangeBottom, ToyOrangeShelf, Color.White),
    // Toy Cyan / Sky (Settings & info)
    SKY(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1), Color.White),
    // Toy Green
    MINT(Color(0xFF4ADE80), Color(0xFF16A34A), Color(0xFF15803D), Color.White),
    // Toy Red (Danger / retry / discount)
    BERRY(ToyRedTop, ToyRedBottom, ToyRedShelf, Color.White),
    // Toy Violet (Neutral dialog actions & map options)
    PLUM(Color(0xFF7E38B3), ToyVioletPanel, ToyVioletPanelDark, Color.White)
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
            text = text.uppercase(),
            color = if (enabled) (textColorOverride ?: style.textColor) else (textColorOverride ?: style.textColor).copy(alpha = 0.55f),
            fontFamily = Baloo2FontFamily,
            fontWeight = FontWeight.Black,
            fontSize = 19.sp,
            letterSpacing = 0.8.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.45f),
                    offset = Offset(0f, 2.5f),
                    blurRadius = 3f
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

    // Slow subtle shine sweep so the main call-to-action feels alive
    val infiniteTransition = rememberInfiniteTransition(label = "jewelShineTransition")
    val shineProgress by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "jewelShineProgress"
    )

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
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.12f))
                    ),
                    shape = shape
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
        // Slow, subtle shine sweep overlay for primary CTA (SUNSET style)
        if (style == JewelButtonStyle.SUNSET && enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(minHeight)
                    .offset(y = sink)
                    .clip(shape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            start = Offset(shineProgress * 360f, 0f),
                            end = Offset(shineProgress * 360f + 140f, 150f)
                        )
                    )
            )
        }
    }
}
