package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.data.SnowballPreferences
import com.example.model.SnowmanCustomization
import com.example.ui.components.WinterArt
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkshopScreen(
    preferences: SnowballPreferences,
    soundEffects: SoundEffects,
    onBack: () -> Unit
) {
    val initialCustomization by preferences.snowmanCustomization.collectAsState()
    var hatIndex by remember { mutableIntStateOf(initialCustomization.hatIndex) }
    var scarfIndex by remember { mutableIntStateOf(initialCustomization.scarfIndex) }
    var faceIndex by remember { mutableIntStateOf(initialCustomization.faceIndex) }
    var badgeIndex by remember { mutableIntStateOf(initialCustomization.badgeIndex) }
    var savedMessageVisible by remember { mutableStateOf(false) }

    val hats = listOf("Santa Cap", "PBS Beanie", "Top Hat", "Earmuffs")
    val scarves = listOf("Ruby Cozy", "Dash Green", "Dot Purple", "Sunny Gold")
    val faces = listOf("Classic Carrot", "Cool Shades", "Winking Star")
    val badges = listOf("Coal Buttons", "Gold Star", "Dash Pin", "Heart Pin")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Snowman Workshop",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PbsCyanDark, SnowBlue, Color.White)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Snowman Canvas Preview
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .border(3.dp, WinterGold, RoundedCornerShape(24.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    WinterArt.drawSnowman(
                        drawScope = this,
                        cx = size.width * 0.5f,
                        cy = size.height * 0.48f,
                        scale = (size.height / 300f).coerceIn(0.7f, 1.1f),
                        hatIndex = hatIndex,
                        scarfIndex = scarfIndex,
                        faceIndex = faceIndex,
                        badgeIndex = badgeIndex
                    )
                }
            }

            // Customization Options
            CustomizationSelector(
                title = "Hats & Headwear",
                options = hats,
                selectedIndex = hatIndex,
                onSelect = {
                    hatIndex = it
                    soundEffects.playDodge()
                }
            )

            CustomizationSelector(
                title = "Warm Winter Scarves",
                options = scarves,
                selectedIndex = scarfIndex,
                onSelect = {
                    scarfIndex = it
                    soundEffects.playDodge()
                }
            )

            CustomizationSelector(
                title = "Snowman Expression",
                options = faces,
                selectedIndex = faceIndex,
                onSelect = {
                    faceIndex = it
                    soundEffects.playDodge()
                }
            )

            CustomizationSelector(
                title = "Badges & Buttons",
                options = badges,
                selectedIndex = badgeIndex,
                onSelect = {
                    badgeIndex = it
                    soundEffects.playDodge()
                }
            )

            // Save Button
            Button(
                onClick = {
                    preferences.saveSnowmanCustomization(
                        SnowmanCustomization(
                            hatIndex = hatIndex,
                            scarfIndex = scarfIndex,
                            faceIndex = faceIndex,
                            badgeIndex = badgeIndex
                        )
                    )
                    soundEffects.playPowerUp()
                    savedMessageVisible = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DashGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_snowman_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Text(
                        text = if (savedMessageVisible) "SNOWMAN SAVED!" else "SAVE MY SNOWMAN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CustomizationSelector(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PbsCyanDark
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEachIndexed { index, name ->
                    val isSelected = index == selectedIndex
                    OutlinedButton(
                        onClick = { onSelect(index) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) PbsCyan.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) PbsCyanDark else Color.LightGray
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = name.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) PbsCyanDark else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
