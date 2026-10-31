package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SnowballPreferences
import com.example.model.GAME_STAGES
import com.example.model.StageInfo
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StageSelectScreen(
    preferences: SnowballPreferences,
    onSelectStage: (StageInfo) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Winter Stages",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PbsCyanDark
                )
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
                items(GAME_STAGES) { stage ->
                    val bestScore = preferences.getStageBestScore(stage.id)
                    val stars = preferences.getStageStars(stage.id)
                    val isUnlocked = stage.id == 1 || preferences.getStageStars(stage.id - 1) > 0

                    Surface(
                        onClick = { if (isUnlocked) onSelectStage(stage) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isUnlocked) Color.White else Color(0xFFECEFF1),
                        shadowElevation = if (isUnlocked) 6.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = if (isUnlocked) PbsCyan else Color.LightGray,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .testTag("stage_card_${stage.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Stage Number Circle
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isUnlocked) DashGreen else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = "${stage.id}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = Color.White
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked Stage",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Stage Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stage.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) Color(0xFF0F1E29) else Color.Gray
                                )
                                Text(
                                    text = stage.subtitle,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Stars
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        for (s in 1..3) {
                                            Icon(
                                                imageVector = if (s <= stars) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = if (s <= stars) WinterGold else Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    if (bestScore > 0) {
                                        Text(
                                            text = "Best: $bestScore",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PbsCyanDark
                                        )
                                    }
                                }

                                if (stage.unlocksCharacter != null && isUnlocked) {
                                    Text(
                                        text = "Unlocks: ${stage.unlocksCharacter.displayName}!",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WinterOrange
                                    )
                                }
                            }

                            // Play Arrow
                            if (isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Play",
                                    tint = DashGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
