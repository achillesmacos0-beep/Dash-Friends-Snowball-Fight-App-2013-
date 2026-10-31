package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundEffects
import com.example.data.SnowballPreferences
import com.example.model.CharacterId
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectScreen(
    preferences: SnowballPreferences,
    soundEffects: SoundEffects,
    onBack: () -> Unit
) {
    val selectedCharacter by preferences.selectedCharacter.collectAsState()
    val unlockedCharacters by preferences.unlockedCharacters.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dash & Friends Roster",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PbsCyanDark)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PbsCyanDark, SnowBlue, Color.White)
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Featured Selected Character Preview Card
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(3.dp, selectedCharacter.primaryColor, RoundedCornerShape(22.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Full body or avatar image
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(selectedCharacter.primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(
                                        id = selectedCharacter.fullBodyResId ?: selectedCharacter.avatarResId
                                    ),
                                    contentDescription = selectedCharacter.displayName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ACTIVE BATTLER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = selectedCharacter.primaryColor,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = selectedCharacter.displayName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F1E29)
                                )
                                Text(
                                    text = selectedCharacter.specialPower,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WinterOrange
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedCharacter.description,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Choose Your Battler",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Character List
                items(CharacterId.values().toList()) { character ->
                    val isUnlocked = unlockedCharacters.contains(character)
                    val isSelected = selectedCharacter == character

                    Surface(
                        onClick = {
                            if (isUnlocked) {
                                preferences.setSelectedCharacter(character)
                                soundEffects.playPowerUp()
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isUnlocked) Color.White else Color(0xFFEEEEEE),
                        shadowElevation = if (isSelected) 8.dp else 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 3.dp else 1.5.dp,
                                color = if (isSelected) character.primaryColor else Color.LightGray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("character_card_${character.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar Icon using real image
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(character.primaryColor.copy(alpha = 0.2f))
                                    .border(2.5.dp, character.primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = character.avatarResId),
                                    contentDescription = character.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = character.displayName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isUnlocked) Color(0xFF0F1E29) else Color.Gray
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = character.primaryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = character.description,
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Special: ${character.specialPower}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = character.primaryColor
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Rating Stars
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Speed: ${"★".repeat(character.speedRating)}",
                                        fontSize = 11.sp,
                                        color = WinterGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Power: ${"★".repeat(character.powerRating)}",
                                        fontSize = 11.sp,
                                        color = WinterOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (!isUnlocked) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Unlock in\nStages",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
