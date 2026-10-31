package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundEffects
import com.example.data.SnowballPreferences
import com.example.game.SnowballGameEngine
import com.example.model.*
import com.example.ui.components.WinterArt
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun SnowballGameScreen(
    stage: StageInfo,
    preferences: SnowballPreferences,
    soundEffects: SoundEffects,
    onExitGame: () -> Unit,
    onNextStage: (() -> Unit)? = null
) {
    val selectedCharacter by preferences.selectedCharacter.collectAsState()
    var gameEngine by remember {
        mutableStateOf(
            SnowballGameEngine(
                stage = stage,
                playerChar = selectedCharacter,
                soundEffects = soundEffects,
                onGameFinished = { score, stars, isVictory ->
                    preferences.recordStats(thrown = 15, hits = score / 100)
                    if (isVictory) {
                        preferences.saveStageProgress(stage.id, score, stars)
                        stage.unlocksCharacter?.let { preferences.unlockCharacter(it) }
                    }
                }
            )
        )
    }

    val uiState by gameEngine.uiState.collectAsState()
    var canvasWidth by remember { mutableFloatStateOf(1000f) }
    var canvasHeight by remember { mutableFloatStateOf(1800f) }
    var isPaused by remember { mutableStateOf(false) }

    // 60 FPS Game Loop
    LaunchedEffect(gameEngine, isPaused) {
        var lastTime = System.nanoTime()
        while (isActive && !isPaused) {
            val now = System.nanoTime()
            val deltaSeconds = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastTime = now
            gameEngine.update(deltaSeconds, canvasWidth, canvasHeight)
            delay(16)
        }
    }

    // 1-Second Timer Loop
    LaunchedEffect(gameEngine, isPaused) {
        while (isActive && !isPaused && !uiState.isGameOver) {
            delay(1000)
            gameEngine.decrementSecond()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Interactive 2.5D Snowball Battle Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!isPaused && !uiState.isGameOver) {
                            val normX = (offset.x / size.width).coerceIn(0f, 1f)
                            val normY = (offset.y / size.height).coerceIn(0f, 1f)
                            gameEngine.throwSnowballAt(normX, normY, size.width.toFloat(), size.height.toFloat())
                        }
                    }
                }
        ) {
            canvasWidth = size.width
            canvasHeight = size.height

            // 1. Winter Backdrop & Environment
            WinterArt.drawWinterBackdrop(
                this,
                skyTop = stage.backdropColorTop,
                skyBottom = stage.backdropColorBottom,
                timeSeconds = uiState.timeLeftSeconds.toFloat()
            )

            // 2. Draw Cover Obstacles & Opponents (ordered by Y depth)
            val sortedOpponents = uiState.opponents.sortedBy { it.yNorm }
            for (opp in sortedOpponents) {
                val cx = opp.xNorm * size.width
                val cy = opp.yNorm * size.height
                val coverWidth = size.width * 0.28f
                val coverHeight = size.height * 0.14f

                // Opponent peeking / aiming above or behind cover
                if (opp.state != OpponentState.HIDDEN) {
                    val popUpOffset = if (opp.state == OpponentState.AIMING || opp.state == OpponentState.THROWING) {
                        coverHeight * 0.55f
                    } else if (opp.state == OpponentState.HIT) {
                        coverHeight * 0.3f
                    } else {
                        coverHeight * 0.38f
                    }
                    val oppY = cy - popUpOffset
                    val charRadius = size.width * 0.09f
                    val splatProg = if (opp.hitSplatTimerMs > 0) (opp.hitSplatTimerMs / 1200f).coerceIn(0f, 1f) else 0f

                    WinterArt.drawCharacter(
                        drawScope = this,
                        character = opp.character,
                        state = opp.state,
                        cx = cx,
                        cy = oppY,
                        radius = charRadius,
                        splatProgress = splatProg
                    )

                    // Draw snowball in hand if aiming
                    if (opp.state == OpponentState.AIMING) {
                        WinterArt.drawSnowball(
                            drawScope = this,
                            x = cx + charRadius * 0.85f,
                            y = oppY + charRadius * 0.3f,
                            radius = charRadius * 0.4f,
                            type = SnowballType.STANDARD
                        )
                    }
                }

                // Foreground Cover (Snow Fort wall / Igloo)
                WinterArt.drawCover(
                    drawScope = this,
                    coverType = opp.coverType,
                    cx = cx,
                    cy = cy,
                    width = coverWidth,
                    height = coverHeight
                )
            }

            // 3. Draw Active Flying Snowballs
            for (snowball in uiState.flyingSnowballs) {
                // Scaling based on perspective
                val scale = if (snowball.fromPlayer) {
                    1.4f - (snowball.progress * 0.7f) // player ball gets smaller as it travels into distance
                } else {
                    0.5f + (snowball.progress * 1.5f) // opponent ball gets massive as it reaches player
                }
                val ballRadius = snowball.type.radius * scale
                WinterArt.drawSnowball(
                    drawScope = this,
                    x = snowball.currentX,
                    y = snowball.currentY,
                    radius = ballRadius,
                    type = snowball.type
                )
            }

            // 4. Draw Particles (Snow splatters, sparks, floating score text)
            for (part in uiState.particles) {
                if (part.isText) {
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = part.size * 1.8f
                            isFakeBoldText = true
                            setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
                            alpha = (part.alpha * 255).toInt().coerceIn(0, 255)
                        }
                        canvas.nativeCanvas.drawText(part.text, part.x, part.y, paint)
                    }
                } else {
                    drawCircle(
                        color = part.color.copy(alpha = part.alpha),
                        radius = part.size,
                        center = Offset(part.x, part.y)
                    )
                }
            }

            // 5. Player's Own Front Snow Fort (bottom edge)
            val frontFortY = size.height * 0.94f
            val duckShift = if (uiState.isDucking) -size.height * 0.05f else 0f
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFF81D4FA)),
                    startY = frontFortY + duckShift,
                    endY = size.height
                ),
                topLeft = Offset(-size.width * 0.1f, frontFortY + duckShift),
                size = androidx.compose.ui.geometry.Size(size.width * 1.2f, size.height * 0.15f)
            )
            drawOval(
                color = Color(0xFF4FC3F7),
                topLeft = Offset(-size.width * 0.1f, frontFortY + duckShift),
                size = androidx.compose.ui.geometry.Size(size.width * 1.2f, size.height * 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
            )
        }

        // Screen Splat Overlay when player is hit by an opponent snowball
        if (uiState.screenSplatAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = uiState.screenSplatAlpha * 0.65f))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SPLAT!",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0288D1).copy(alpha = uiState.screenSplatAlpha)
                    )
                    Text(
                        text = "Duck to avoid snowballs!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = uiState.screenSplatAlpha)
                    )
                }
            }
        }

        // Top HUD Bar (Score, Target Progress, Hearts, Clock)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Back button
                IconButton(
                    onClick = { isPaused = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = PbsCyanDark
                    )
                }

                // Hearts / Warm Earmuffs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    for (i in 1..3) {
                        val active = i <= uiState.playerHearts
                        Icon(
                            imageVector = if (active) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Heart $i",
                            tint = if (active) WinterRed else Color.LightGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Countdown Timer
                val timeColor = if (uiState.timeLeftSeconds <= 10) WinterRed else Color(0xFF0F1E29)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = timeColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${uiState.timeLeftSeconds}s",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = timeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Score & Combo Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stage.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = PbsCyanDark
                            )
                            Text(
                                text = "Score: ${uiState.score} / ${stage.targetScore}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F1E29)
                            )
                        }

                        if (uiState.combo > 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WinterGold,
                                modifier = Modifier.padding(start = 6.dp)
                            ) {
                                Text(
                                    text = "${uiState.combo}x COMBO!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress to Target
                    val progress = (uiState.score.toFloat() / stage.targetScore).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DashGreen,
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }

        // Bottom Gameplay Controls: DUCK & Snowball Selector
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Snowball Type Quick Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (type in SnowballType.values()) {
                    val isSelected = uiState.selectedSnowballType == type
                    IconButton(
                        onClick = { gameEngine.selectSnowballType(type) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) type.color else Color.Transparent)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) PbsCyanDark else Color.LightGray,
                                shape = CircleShape
                            )
                            .testTag("snowball_type_${type.name.lowercase()}")
                    ) {
                        Icon(
                            imageVector = when (type) {
                                SnowballType.STANDARD -> Icons.Default.AcUnit
                                SnowballType.MEGA -> Icons.Default.Circle
                                SnowballType.TRIPLE -> Icons.Default.Filter3
                                SnowballType.SLUSH -> Icons.Default.SevereCold
                                SnowballType.RAINBOW -> Icons.Default.AutoAwesome
                            },
                            contentDescription = type.displayName,
                            tint = if (isSelected) Color.White else Color(0xFF0F1E29)
                        )
                    }
                }
            }

            // Big Action Buttons: DUCK & RELOAD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // DUCK BUTTON (Toggable / Holdable)
                Button(
                    onClick = { gameEngine.setDucking(!uiState.isDucking) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isDucking) WinterGold else WinterOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(22.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(62.dp)
                        .testTag("duck_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isDucking) Icons.Default.Shield else Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = if (uiState.isDucking) "DUCKING! (SAFE)" else "DUCK / DODGE",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }

                // RELOAD SNOWBALLS BUTTON
                Button(
                    onClick = { gameEngine.reloadSnowballs() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DashGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(22.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp)
                        .testTag("reload_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PACK SNOW",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${uiState.snowballsInHand} left",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Pause Menu Dialog
        if (isPaused) {
            AlertDialog(
                onDismissRequest = { isPaused = false },
                title = {
                    Text(
                        text = "Game Paused",
                        fontWeight = FontWeight.ExtraBold,
                        color = PbsCyanDark
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Stage: ${stage.title}")
                        Text("Current Score: ${uiState.score}")
                        Text("Tap targets to throw snowballs. Hold DUCK to avoid incoming hits!")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { isPaused = false },
                        colors = ButtonDefaults.buttonColors(containerColor = DashGreen)
                    ) {
                        Text("RESUME")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = onExitGame) {
                        Text("QUIT TO MENU")
                    }
                }
            )
        }

        // Victory / Game Over Overlay Dialog
        if (uiState.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp)
                        .border(
                            4.dp,
                            if (uiState.isVictory) WinterGold else Color(0xFFEF5350),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title
                        Text(
                            text = if (uiState.isVictory) "VICTORY!" else "SNOWED OUT!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.isVictory) DashGreenDark else WinterRed
                        )

                        Text(
                            text = if (uiState.isVictory) {
                                "Awesome snowball battle! You conquered ${stage.title}!"
                            } else {
                                "Brrr! Those snowballs came fast! Warm up and try again!"
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        // Stars (if victory)
                        if (uiState.isVictory) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (s in 1..3) {
                                    val earned = s <= uiState.starsEarned
                                    Icon(
                                        imageVector = if (earned) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $s",
                                        tint = if (earned) WinterGold else Color.LightGray,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }

                        // Score Summary Card
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SnowIce,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Final Score",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${uiState.score} pts",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PbsCyanDark
                                )
                                if (stage.unlocksCharacter != null && uiState.isVictory) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "NEW FRIEND UNLOCKED: ${stage.unlocksCharacter.displayName}!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WinterOrange
                                    )
                                }
                            }
                        }

                        // Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onExitGame,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("MENU")
                            }

                            Button(
                                onClick = {
                                    // Restart same stage
                                    gameEngine = SnowballGameEngine(
                                        stage = stage,
                                        playerChar = selectedCharacter,
                                        soundEffects = soundEffects,
                                        onGameFinished = { score, stars, isVictory ->
                                            preferences.recordStats(thrown = 15, hits = score / 100)
                                            if (isVictory) {
                                                preferences.saveStageProgress(stage.id, score, stars)
                                                stage.unlocksCharacter?.let { preferences.unlockCharacter(it) }
                                            }
                                        }
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isVictory && onNextStage != null) PbsCyan else DashGreen
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = if (uiState.isVictory && onNextStage != null) "NEXT STAGE" else "RETRY",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
