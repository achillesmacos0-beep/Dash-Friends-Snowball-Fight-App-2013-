package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEffects
import com.example.data.SnowballPreferences
import com.example.model.CharacterId
import com.example.model.GAME_STAGES
import com.example.model.StageInfo
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    STAGE_SELECT,
    GAME_PLAY,
    WORKSHOP,
    CHARACTERS,
    TROPHIES
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: SnowballPreferences
    private lateinit var soundEffects: SoundEffects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = SnowballPreferences(applicationContext)
        soundEffects = SoundEffects(applicationContext)

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var activeStage by remember { mutableStateOf(GAME_STAGES.first()) }
                var isEndlessMode by remember { mutableStateOf(false) }

                val soundEnabled by preferences.soundEnabled.collectAsState()
                val vibrationEnabled by preferences.vibrationEnabled.collectAsState()

                LaunchedEffect(soundEnabled) {
                    soundEffects.setMuted(!soundEnabled)
                }
                LaunchedEffect(vibrationEnabled) {
                    soundEffects.setVibrationEnabled(vibrationEnabled)
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0288D1)
                ) {
                    when (currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                preferences = preferences,
                                onPlayCampaign = {
                                    currentScreen = AppScreen.STAGE_SELECT
                                },
                                onPlayEndless = {
                                    isEndlessMode = true
                                    activeStage = StageInfo(
                                        id = 99,
                                        title = "Endless Blizzard",
                                        subtitle = "Survive as many waves as possible!",
                                        targetScore = 999999,
                                        timeLimitSeconds = 120,
                                        maxOpponents = 5,
                                        backdropColorTop = Color(0xFF0B1E36),
                                        backdropColorBottom = Color(0xFF80DEEA),
                                        unlocksCharacter = null
                                    )
                                    currentScreen = AppScreen.GAME_PLAY
                                },
                                onOpenWorkshop = {
                                    currentScreen = AppScreen.WORKSHOP
                                },
                                onOpenCharacters = {
                                    currentScreen = AppScreen.CHARACTERS
                                },
                                onOpenTrophies = {
                                    currentScreen = AppScreen.TROPHIES
                                }
                            )
                        }

                        AppScreen.STAGE_SELECT -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            StageSelectScreen(
                                preferences = preferences,
                                onSelectStage = { stage ->
                                    isEndlessMode = false
                                    activeStage = stage
                                    currentScreen = AppScreen.GAME_PLAY
                                },
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.GAME_PLAY -> {
                            BackHandler {
                                currentScreen = if (isEndlessMode) AppScreen.HOME else AppScreen.STAGE_SELECT
                            }
                            SnowballGameScreen(
                                stage = activeStage,
                                preferences = preferences,
                                soundEffects = soundEffects,
                                onExitGame = {
                                    currentScreen = if (isEndlessMode) AppScreen.HOME else AppScreen.STAGE_SELECT
                                },
                                onNextStage = if (!isEndlessMode && activeStage.id < GAME_STAGES.size) {
                                    {
                                        val next = GAME_STAGES.firstOrNull { it.id == activeStage.id + 1 }
                                        if (next != null) {
                                            activeStage = next
                                        } else {
                                            currentScreen = AppScreen.STAGE_SELECT
                                        }
                                    }
                                } else null
                            )
                        }

                        AppScreen.WORKSHOP -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            WorkshopScreen(
                                preferences = preferences,
                                soundEffects = soundEffects,
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.CHARACTERS -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            CharacterSelectScreen(
                                preferences = preferences,
                                soundEffects = soundEffects,
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.TROPHIES -> {
                            BackHandler {
                                currentScreen = AppScreen.HOME
                            }
                            TrophiesScreen(
                                preferences = preferences,
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
