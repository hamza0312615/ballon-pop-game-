package com.example.balloonpop.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.balloonpop.audio.SoundManager

/**
 * Premium 3D Carved Wooden & Ruby Close Button with subtle animated hover effect:
 * - Subtle hover expansion scale (1.08x) with spring return
 * - Dynamic 45-degree rotation on hover and 90-degree on press
 * - Luminous radiant ruby & gold outer halo during hover
 * - Rich mahogany carved wood ring with golden bevel
 * - Vibrant ruby dome with specular top gloss arc
 * - Crisp 3D embossed white cross (✕) with depth shadow
 * - Tactile spring compression on press
 * - Audio feedback on click
 * - 48dp minimum accessibility touch target
 */
@Composable
fun GameCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    testTag: String = "close_shop_button"
) {
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    // Smooth spring animation for hover and press scale
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isHovered -> 1.08f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "close_btn_scale"
    )

    // Smooth rotation: tilts 45 deg on hover, rotates 90 deg on press
    val rotation by animateFloatAsState(
        targetValue = when {
            isPressed -> 90f
            isHovered -> 45f
            else -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "close_btn_rotation"
    )

    // Elevation glow shadow increases on hover
    val elevation by animateFloatAsState(
        targetValue = when {
            isPressed -> 2f
            isHovered -> 12f
            else -> 6f
        },
        animationSpec = tween(150),
        label = "close_btn_elevation"
    )

    // Hover glow pulse
    val glowColor = if (isHovered) Color(0xFFFF2A55) else Color(0xFFDC2626)

    Box(
        modifier = modifier
            .size(48.dp) // Minimum 48dp touch target for accessibility
            .semantics { contentDescription = "Close dialog" }
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    soundManager.playDialogClose()
                    onClick()
                }
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Outer Carved Mahogany Wooden Ring with Bevel & Glow
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .shadow(
                    elevation = elevation.dp,
                    shape = CircleShape,
                    spotColor = glowColor,
                    ambientColor = if (isHovered) Color(0xFFB91C1C) else Color(0xFF7F1D1D)
                )
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isHovered) Color(0xFFA06B5B) else Color(0xFF8D5B4C), // Light carved wood rim
                            Color(0xFF5D3A2C),
                            Color(0xFF3E2318), // Deep shadow wood
                            Color(0xFF2B150D)
                        )
                    )
                )
                .border(
                    width = if (isHovered) 2.dp else 1.8.dp,
                    brush = Brush.verticalGradient(
                        if (isHovered) {
                            listOf(Color(0xFFFFF0D0), Color(0xFFFBBF24), Color(0xFF8D5B4C))
                        } else {
                            listOf(Color(0xFFD4A373), Color(0xFF8D5B4C), Color(0xFF2A150D))
                        }
                    ),
                    shape = CircleShape
                )
                .padding(3.dp) // Wooden border width
                .clip(CircleShape)
                // Inner Ruby Jewel Surface
                .background(
                    Brush.verticalGradient(
                        if (isHovered) {
                            listOf(
                                Color(0xFFFF6B6B), // Brighter ruby top on hover
                                Color(0xFFFF3838),
                                Color(0xFFDC2626),
                                Color(0xFF991B1B),
                                Color(0xFF5B0E0E)
                            )
                        } else {
                            listOf(
                                Color(0xFFFF5252), // Bright ruby top
                                Color(0xFFEF4444),
                                Color(0xFFDC2626),
                                Color(0xFF991B1B),
                                Color(0xFF5B0E0E)  // Dark ruby base
                            )
                        }
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (isHovered) Color(0xCCFFE4E6) else Color(0x88FFE4E6),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Specular Top Shine & 3D Carved Cross Icon with Animated Rotation
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
            ) {
                val w = this.size.width
                val h = this.size.height

                // Top Gloss Crescent Highlight
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isHovered) Color(0xD9FFFFFF) else Color(0xB3FFFFFF),
                            Color(0x00FFFFFF)
                        ),
                        startY = 0f,
                        endY = h * 0.45f
                    ),
                    topLeft = Offset(w * 0.18f, h * 0.08f),
                    size = Size(w * 0.64f, h * 0.36f)
                )

                // High-visibility crisp white 3D Cross ("✕")
                val crossPadding = w * 0.28f
                val strokeWidth = w * 0.125f

                // Drop shadow line
                drawLine(
                    color = Color(0x803B0A0A),
                    start = Offset(crossPadding + 1.2f, crossPadding + 2.2f),
                    end = Offset(w - crossPadding + 1.2f, h - crossPadding + 2.2f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0x803B0A0A),
                    start = Offset(w - crossPadding + 1.2f, crossPadding + 2.2f),
                    end = Offset(crossPadding + 1.2f, h - crossPadding + 2.2f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )

                // Crisp White Foreground Cross
                drawLine(
                    color = Color.White,
                    start = Offset(crossPadding, crossPadding),
                    end = Offset(w - crossPadding, h - crossPadding),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(w - crossPadding, crossPadding),
                    end = Offset(crossPadding, h - crossPadding),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
