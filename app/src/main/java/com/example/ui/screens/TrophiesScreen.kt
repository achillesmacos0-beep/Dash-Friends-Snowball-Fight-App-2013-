package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SnowballPreferences
import com.example.model.GAME_STAGES
import com.example.ui.theme.*

data class TrophyItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrophiesScreen(
    preferences: SnowballPreferences,
    onBack: () -> Unit
) {
    val totalThrown by preferences.totalSnowballsThrown.collectAsState()
    val totalHits by preferences.totalHits.collectAsState()
    val endlessHighScore by preferences.endlessHighScore.collectAsState()
    val accuracy = if (totalThrown > 0) (totalHits.toFloat() / totalThrown * 100).toInt().coerceAtMost(100) else 0

    val totalStars = GAME_STAGES.sumOf { preferences.getStageStars(it.id) }

    val trophies = listOf(
        TrophyItem(
            title = "Snowball Rookie",
            description = "Land your first snowball hit on a friend!",
            icon = Icons.Default.AcUnit,
            isUnlocked = totalHits >= 1
        ),
        TrophyItem(
            title = "Sharp Shooter",
            description = "Land 20 snowball hits in battles!",
            icon = Icons.Default.CrisisAlert,
            isUnlocked = totalHits >= 20
        ),
        TrophyItem(
            title = "Blizzard Survivor",
            description = "Score over 1,000 points in Endless Blizzard!",
            icon = Icons.Default.SevereCold,
            isUnlocked = endlessHighScore >= 1000
        ),
        TrophyItem(
            title = "Star Collector",
            description = "Earn at least 6 stars across winter stages!",
            icon = Icons.Default.Star,
            isUnlocked = totalStars >= 6
        ),
        TrophyItem(
            title = "Snowman Architect",
            description = "Craft and dress a custom snowman in the workshop!",
            icon = Icons.Default.Build,
            isUnlocked = true
        ),
        TrophyItem(
            title = "2013 Blizzard Champion",
            description = "Conquer all 6 stages of the PBS KIDS Snowball Cup!",
            icon = Icons.Default.EmojiEvents,
            isUnlocked = preferences.getStageStars(6) > 0
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Trophies & Stats",
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
                // Stats Card
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, WinterGold, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Career Battle Stats",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = PbsCyanDark
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatPill("Snowballs Thrown", "$totalThrown")
                                StatPill("Hits Landed", "$totalHits")
                                StatPill("Accuracy", "$accuracy%")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatPill("Campaign Stars", "$totalStars / 18")
                                StatPill("Endless Best", "$endlessHighScore pts")
                            }
                        }
                    }
                }

                // Trophies Header
                item {
                    Text(
                        text = "Achievement Medals",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Trophy List
                items(trophies.size) { idx ->
                    val trophy = trophies[idx]
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (trophy.isUnlocked) Color.White else Color(0xFFEEEEEE),
                        shadowElevation = if (trophy.isUnlocked) 4.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.5.dp,
                                if (trophy.isUnlocked) WinterGold else Color.LightGray,
                                RoundedCornerShape(16.dp)
                            )
                            .testTag("trophy_item_$idx")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (trophy.isUnlocked) WinterGold else Color.Gray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = trophy.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = trophy.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (trophy.isUnlocked) Color(0xFF0F1E29) else Color.Gray
                                )
                                Text(
                                    text = trophy.description,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray
                                )
                            }

                            if (trophy.isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Unlocked",
                                    tint = DashGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SnowIce,
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = PbsCyanDark)
        }
    }
}
