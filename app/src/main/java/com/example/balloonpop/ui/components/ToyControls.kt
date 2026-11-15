package com.example.balloonpop.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Pass 12: chunky toy toggle that replaces the stock Material Switch.
 * A plastic track with a shelf edge and a big glossy knob that slides
 * with a spring — the settings screen now feels like the rest of the
 * Toybox Stage instead of a default Android form.
 * Same contract as Switch: [checked], [onCheckedChange], [modifier]
 * (testTags keep working — QA hooks unchanged).
 */
@Composable
fun ToySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "toySwitchKnob"
    )
    val trackBrush = if (checked) {
        Brush.verticalGradient(listOf(Color(0xFF7EE82E), Color(0xFF63C70A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFF4A2470), Color(0xFF2A1450)))
    }
    Box(
        modifier = modifier
            .size(width = 54.dp, height = 32.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(trackBrush)
            .border(
                2.dp,
                if (checked) Color(0xFF4A9605) else Color(0xFF7A3EAE),
                RoundedCornerShape(16.dp)
            )
            .toggleable(
                value = checked,
                role = Role.Switch
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onCheckedChange(it)
            }
    ) {
        // inner groove shadow line at the top of the track
        Box(
            modifier = Modifier
                .offset(x = 4.dp, y = 3.dp)
                .size(width = 46.dp, height = 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.Black.copy(alpha = 0.18f))
        )
        // the knob: white plastic ball with a coloured core dot
        Box(
            modifier = Modifier
                .offset(x = 3.dp + knobOffset, y = 3.dp)
                .size(26.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFE9DEF9)))
                )
                .border(1.5.dp, Color.Black.copy(alpha = 0.12f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 8.dp, y = 8.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (checked) Color(0xFF63C70A) else Color(0xFF8E7CC3))
            )
        }
    }
}

/**
 * Pass 12: chunky slider thumb — a lime plastic ball with a white ring,
 * sized to match ToySwitch's knob. Used via Slider's thumb slot so the
 * settings sliders stop looking like a stock Android form.
 */
@Composable
fun ToySliderThumb() {
    Box(
        modifier = Modifier
            .size(26.dp)
            .shadow(3.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(listOf(Color(0xFFC9FF7A), Color(0xFF63C70A)))
            )
            .border(2.5.dp, Color.White, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .offset(x = 6.dp, y = 5.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.55f))
        )
    }
}
