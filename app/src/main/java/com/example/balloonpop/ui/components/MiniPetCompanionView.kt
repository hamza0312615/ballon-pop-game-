package com.example.balloonpop.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.balloonpop.model.ZonePet
import com.example.balloonpop.ui.theme.DesignTokens
import kotlinx.coroutines.delay

/**
 * Animated Zone Mini Pet Companion:
 * Floats with lifelike balloon physics, reacts to taps with cute speech quotes,
 * and brings cheerful personality to the World Map and Gameplay.
 */
@Composable
fun MiniPetMapCompanion(
    pet: ZonePet,
    isBossZone: Boolean = false,
    onPetTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showSpeechBubble by remember { mutableStateOf(false) }
    var currentSpeech by remember { mutableStateOf(if (isBossZone) pet.bossCheerQuote else pet.voiceQuote) }

    LaunchedEffect(pet, isBossZone) {
        currentSpeech = if (isBossZone) pet.bossCheerQuote else pet.voiceQuote
        showSpeechBubble = true
        delay(3500L)
        showSpeechBubble = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pet_float")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet_bob"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pet_aura"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = DesignTokens.ButtonPressSpring,
        label = "pet_press"
    )

    Column(
        modifier = modifier.offset(y = bobOffset.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Speech Bubble
        AnimatedVisibility(
            visible = showSpeechBubble,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .widthIn(max = 160.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = pet.glowColor)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xF00A0F1D))
                    .border(1.5.dp, pet.primaryColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentSpeech,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }

        // Pet Balloon Avatar with Glowing Aura
        Box(
            modifier = Modifier
                .scale(pressScale)
                .size(54.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    showSpeechBubble = !showSpeechBubble
                    currentSpeech = if (isBossZone) pet.bossCheerQuote else pet.voiceQuote
                    onPetTap()
                }
                .testTag("mini_pet_avatar"),
            contentAlignment = Alignment.Center
        ) {
            // Glowing Aura Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(auraPulse)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(pet.glowColor.copy(alpha = 0.5f), Color.Transparent)
                        )
                    )
            )

            // Pet Bubble Container
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(DesignTokens.ElevationActive, CircleShape, spotColor = pet.primaryColor)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF020617)
                            )
                        )
                    )
                    .border(2.dp, pet.primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = pet.emoji, fontSize = 24.sp)
            }
        }

        // Mini Pet Name Badge
        Box(
            modifier = Modifier
                .offset(y = (-4).dp)
                .clip(DesignTokens.ShapeSmall)
                .background(Color(0xDD0D131F))
                .border(1.dp, pet.primaryColor.copy(alpha = 0.8f), DesignTokens.ShapeSmall)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${pet.name} 🐾",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = pet.primaryColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * In-Game Floating Mini Pet:
 * Floats in top right of gameplay screen and cheers the player on!
 */
@Composable
fun MiniPetInGameHud(
    pet: ZonePet,
    isBossLevel: Boolean,
    combo: Int,
    timeLeft: Int,
    modifier: Modifier = Modifier
) {
    var petReactionText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(combo) {
        if (combo >= 5) {
            petReactionText = "🔥 ULTRA!"
            delay(1800L)
            petReactionText = null
        } else if (combo >= 2) {
            petReactionText = "✨ ${combo}x!"
            delay(1400L)
            petReactionText = null
        }
    }

    LaunchedEffect(isBossLevel) {
        if (isBossLevel) {
            petReactionText = "👑 BOSS!"
            delay(2500L)
            petReactionText = null
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ingame_pet")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ingame_pet_bob"
    )

    Row(
        modifier = modifier.offset(y = bobOffset.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Pet Bubble Avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(6.dp, CircleShape, spotColor = pet.primaryColor)
                .clip(CircleShape)
                .background(Color(0xEE0B1120))
                .border(1.5.dp, pet.primaryColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = pet.emoji, fontSize = 18.sp)
        }

        // Reaction Speech Pop
        AnimatedVisibility(
            visible = petReactionText != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .clip(DesignTokens.ShapeSmall)
                    .background(Color(0xF00A0F1D))
                    .border(1.dp, pet.primaryColor, DesignTokens.ShapeSmall)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = petReactionText ?: "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = pet.primaryColor
                )
            }
        }
    }
}
