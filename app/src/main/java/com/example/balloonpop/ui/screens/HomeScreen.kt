package com.example.balloonpop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.balloonpop.ui.components.*
import com.example.balloonpop.ui.theme.*

@Composable
fun HomeScreen(
    coins: Int = 0,
    gems: Int = 0,
    lives: Int = 5,
    onOpenCampaign: () -> Unit,
    onOpenSkillVault: () -> Unit,
    onOpenEndless: () -> Unit,
    onOpenDailySpin: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenQuests: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_home_bob")
    val mascotY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_y"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF38BDF8), // Sunny sky blue
                        Color(0xFF7DD3FC),
                        Color(0xFFBAE6FD),
                        Color(0xFFFEF08A), // Warm sunny glow
                        Color(0xFF86EFAC)  // Meadow green
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar with Lives, Coins & Gems
        TopBar(
            coins = coins,
            gems = gems,
            lives = lives,
            onOpenShop = onOpenShop,
            onOpenSettings = onOpenSettings
        )

        // 2. Sunny Mascot & Floating Title Centerpiece
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            // Mascot Character bouncing
            Box(
                modifier = Modifier
                    .offset(y = mascotY.dp)
                    .shadow(12.dp, CircleShape, spotColor = Color(0xFFF59E0B))
            ) {
                MascotCharacter(mood = MascotMood.JOYFUL, size = 96.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game Logo Banner
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
                        )
                    )
                    .border(3.5.dp, Color(0xFFFEF3C7), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "🎈 BALLOON POP 🎈",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "CASUAL DELUXE ADVENTURE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFEF3C7),
                    letterSpacing = 1.5.sp
                )
            }
        }

        // 3. Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big Juicy 3D Green Play Button
            JellyButton(
                text = "PLAY CAMPAIGN 🗺️",
                onClick = onOpenCampaign,
                height = 58.dp,
                fontSize = 20,
                colorVariant = JellyColorVariant.GREEN,
                testTag = "campaign_start_button",
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(pulseScale)
            )

            // Skill Vault Action
            JellyButton(
                text = "SKILL VAULT & FINGER SLICE ⚡",
                onClick = onOpenSkillVault,
                height = 48.dp,
                fontSize = 15,
                colorVariant = JellyColorVariant.BLUE,
                testTag = "skill_vault_button",
                modifier = Modifier.fillMaxWidth()
            )

            // 2x2 Grid of Actions (Endless, Daily Spin, Shop, Quests)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CasualMenuButton(
                    icon = "⚡",
                    title = "Endless",
                    gradient = listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                    onClick = onOpenEndless,
                    testTag = "endless_menu_button",
                    modifier = Modifier.weight(1f)
                )

                CasualMenuButton(
                    icon = "🎡",
                    title = "Daily Spin",
                    gradient = listOf(Color(0xFFC084FC), Color(0xFF7E22CE)),
                    onClick = onOpenDailySpin,
                    testTag = "daily_spin_menu_button",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CasualMenuButton(
                    icon = "🛒",
                    title = "Shop",
                    gradient = listOf(Color(0xFFFBBF24), Color(0xFFD97706)),
                    onClick = onOpenShop,
                    testTag = "shop_menu_button",
                    modifier = Modifier.weight(1f)
                )

                CasualMenuButton(
                    icon = "📜",
                    title = "Quests",
                    gradient = listOf(Color(0xFF4ADE80), Color(0xFF16A34A)),
                    onClick = onOpenQuests,
                    testTag = "quests_menu_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
private fun CasualMenuButton(
    icon: String,
    title: String,
    gradient: List<Color>,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(gradient))
            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}
