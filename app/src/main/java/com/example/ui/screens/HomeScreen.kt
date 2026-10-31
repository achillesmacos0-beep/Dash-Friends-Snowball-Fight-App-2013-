package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SnowballPreferences
import com.example.model.CharacterId
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    preferences: SnowballPreferences,
    onPlayCampaign: () -> Unit,
    onPlayEndless: () -> Unit,
    onOpenWorkshop: () -> Unit,
    onOpenCharacters: () -> Unit,
    onOpenTrophies: () -> Unit
) {
    val selectedCharacter by preferences.selectedCharacter.collectAsState()
    val endlessHighScore by preferences.endlessHighScore.collectAsState()
    val soundEnabled by preferences.soundEnabled.collectAsState()
    val unlockedCharacters by preferences.unlockedCharacters.collectAsState()

    // Bouncing animation for hero
    val infiniteTransition = rememberInfiniteTransition(label = "title_bounce")
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PbsCyanDark,
                        PbsCyan,
                        SnowIce,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Sound Toggle & High Score Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = WinterGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Best: $endlessHighScore pts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0F1E29)
                        )
                    }
                }

                // Sound Button
                IconButton(
                    onClick = { preferences.setSoundEnabled(!soundEnabled) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.92f))
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (soundEnabled) "Mute Sound" else "Enable Sound",
                        tint = if (soundEnabled) PbsCyanDark else Color.Gray
                    )
                }
            }

            // Hero Character Avatar with animated bounce and dynamic selection
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = bounceOffset.dp)
                    .size(175.dp)
                    .shadow(14.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(6.dp, selectedCharacter.primaryColor, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = selectedCharacter.avatarResId),
                    contentDescription = selectedCharacter.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2013 Style Title Badge: "Dash & Friends Snowball Fight"
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, WinterGold, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PBS KIDS 2013",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                        color = PbsCyanDark
                    )
                    Text(
                        text = "Dash & Friends",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DashGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "SNOWBALL FIGHT!",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Black,
                        color = PbsCyanDark,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Quick Character Switcher Bar (Dash, Dot, Hooper, Raccoon Dash)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dash & Friends Lineup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PbsCyanDark
                        )
                        Text(
                            text = "Tap to switch",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val roster = listOf(
                            CharacterId.DASH,
                            CharacterId.DOT,
                            CharacterId.HOOPER,
                            CharacterId.RACCOON_DASH
                        )

                        for (char in roster) {
                            val isSelected = selectedCharacter == char
                            val isUnlocked = unlockedCharacters.contains(char)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (isUnlocked) preferences.setSelectedCharacter(char)
                                        else onOpenCharacters()
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(char.primaryColor.copy(alpha = 0.2f))
                                        .border(
                                            width = if (isSelected) 3.5.dp else 1.5.dp,
                                            color = if (isSelected) char.primaryColor else Color.LightGray,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = char.avatarResId),
                                        contentDescription = char.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (!isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.45f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = char.displayName.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) char.primaryColor else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            // Main Action Buttons
            Button(
                onClick = onPlayCampaign,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DashGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("play_campaign_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "PLAY WINTER STAGES",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Button(
                onClick = onPlayEndless,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PbsCyan,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("play_endless_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AcUnit,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ENDLESS BLIZZARD",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Secondary Options: Snowman Workshop & Trophies
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenWorkshop,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, WinterOrange),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("snowman_workshop_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = WinterOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Snowman",
                            fontWeight = FontWeight.Bold,
                            color = WinterOrange,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenTrophies,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, WinterGold),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("trophies_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = WinterGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Trophies",
                            fontWeight = FontWeight.Bold,
                            color = WinterGold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
