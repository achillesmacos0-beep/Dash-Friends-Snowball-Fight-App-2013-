package com.example.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.audio.SoundEffects
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.random.Random

data class GameUiState(
    val stage: StageInfo,
    val playerCharacter: CharacterId,
    val score: Int = 0,
    val combo: Int = 0,
    val timeLeftSeconds: Int = 45,
    val playerHearts: Int = 3,
    val isDucking: Boolean = false,
    val selectedSnowballType: SnowballType = SnowballType.STANDARD,
    val snowballsInHand: Int = 10,
    val specialCharge: Float = 0f, // 0..1 to charge up special snowball
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val starsEarned: Int = 0,
    val screenSplatAlpha: Float = 0f,
    val opponents: List<OpponentSlot> = emptyList(),
    val flyingSnowballs: List<ActiveSnowball> = emptyList(),
    val particles: List<Particle> = emptyList()
)

class SnowballGameEngine(
    private val stage: StageInfo,
    private val playerChar: CharacterId,
    private val soundEffects: SoundEffects,
    private val onGameFinished: (score: Int, stars: Int, isVictory: Boolean) -> Unit
) {
    private val _uiState = MutableStateFlow(
        GameUiState(
            stage = stage,
            playerCharacter = playerChar,
            timeLeftSeconds = stage.timeLimitSeconds,
            opponents = createOpponentSlots()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var nextSnowballId = 1L
    private var isPaused = false

    private fun createOpponentSlots(): List<OpponentSlot> {
        val covers = listOf("Snow Wall", "Igloo", "Pine Tree", "Snow Fort", "Snow Drift")
        val availableCharacters = CharacterId.values().toList().filter { it != playerChar }
        val slots = mutableListOf<OpponentSlot>()

        val layoutPositions = listOf(
            Pair(0.20f, 0.42f),
            Pair(0.50f, 0.38f),
            Pair(0.80f, 0.44f),
            Pair(0.35f, 0.55f),
            Pair(0.65f, 0.53f),
            Pair(0.12f, 0.50f)
        )

        for (i in 0 until stage.maxOpponents.coerceAtMost(layoutPositions.size)) {
            val (x, y) = layoutPositions[i]
            val char = availableCharacters[i % availableCharacters.size]
            slots.add(
                OpponentSlot(
                    id = i,
                    xNorm = x,
                    yNorm = y,
                    character = char,
                    state = OpponentState.HIDDEN,
                    stateTimerMs = Random.nextLong(600, 2500),
                    coverType = covers[i % covers.size],
                    hp = 1
                )
            )
        }
        return slots
    }

    fun setDucking(ducking: Boolean) {
        if (_uiState.value.isGameOver) return
        if (ducking && !_uiState.value.isDucking) {
            soundEffects.playDodge()
        }
        _uiState.value = _uiState.value.copy(isDucking = ducking)
    }

    fun selectSnowballType(type: SnowballType) {
        _uiState.value = _uiState.value.copy(selectedSnowballType = type)
    }

    fun reloadSnowballs() {
        if (_uiState.value.isGameOver) return
        soundEffects.playThrow()
        _uiState.value = _uiState.value.copy(
            snowballsInHand = 12
        )
    }

    fun throwSnowballAt(targetXNorm: Float, targetYNorm: Float, screenWidth: Float, screenHeight: Float) {
        val current = _uiState.value
        if (current.isGameOver || current.isDucking) return

        if (current.snowballsInHand <= 0) {
            reloadSnowballs()
            return
        }

        val type = current.selectedSnowballType
        soundEffects.playThrow()

        val startX = screenWidth * 0.5f
        val startY = screenHeight * 0.95f
        val targetX = targetXNorm * screenWidth
        val targetY = targetYNorm * screenHeight

        val newSnowballs = current.flyingSnowballs.toMutableList()

        if (type == SnowballType.TRIPLE) {
            // Triple spread
            for (offset in listOf(-60f, 0f, 60f)) {
                newSnowballs.add(
                    ActiveSnowball(
                        id = nextSnowballId++,
                        fromPlayer = true,
                        startX = startX,
                        startY = startY,
                        targetX = targetX + offset,
                        targetY = targetY,
                        currentX = startX,
                        currentY = startY,
                        progress = 0f,
                        speed = 1.8f + (playerChar.speedRating * 0.15f),
                        arcHeight = 140f,
                        type = type
                    )
                )
            }
        } else {
            newSnowballs.add(
                ActiveSnowball(
                    id = nextSnowballId++,
                    fromPlayer = true,
                    startX = startX,
                    startY = startY,
                    targetX = targetX,
                    targetY = targetY,
                    currentX = startX,
                    currentY = startY,
                    progress = 0f,
                    speed = 1.6f + (playerChar.speedRating * 0.18f),
                    arcHeight = 160f,
                    type = type
                )
            )
        }

        val remainingSnow = (current.snowballsInHand - 1).coerceAtLeast(0)
        _uiState.value = current.copy(
            snowballsInHand = remainingSnow,
            flyingSnowballs = newSnowballs
        )
    }

    /**
     * Called by game loop every frame (approx 16ms delta)
     */
    fun update(deltaSeconds: Float, screenWidth: Float, screenHeight: Float) {
        if (isPaused) return
        val current = _uiState.value
        if (current.isGameOver) return

        val deltaMs = (deltaSeconds * 1000).toLong()

        // 1. Update Opponents AI
        val updatedOpponents = current.opponents.map { opp ->
            val updated = opp.copy()
            updated.stateTimerMs -= deltaMs

            if (updated.hitSplatTimerMs > 0) {
                updated.hitSplatTimerMs = (updated.hitSplatTimerMs - deltaMs).coerceAtLeast(0)
            }

            if (updated.stateTimerMs <= 0) {
                when (updated.state) {
                    OpponentState.HIDDEN -> {
                        // Pop up to peek!
                        updated.state = OpponentState.PEEKING
                        updated.stateTimerMs = Random.nextLong(1000, 2200)
                    }
                    OpponentState.PEEKING -> {
                        // Aim snowball at player
                        updated.state = OpponentState.AIMING
                        updated.stateTimerMs = Random.nextLong(900, 1800)
                    }
                    OpponentState.AIMING -> {
                        // Throw snowball at player!
                        updated.state = OpponentState.THROWING
                        updated.stateTimerMs = 500
                        // Launch opponent snowball
                        launchOpponentSnowball(updated, screenWidth, screenHeight)
                    }
                    OpponentState.THROWING -> {
                        // Return to cover
                        updated.state = OpponentState.HIDDEN
                        updated.stateTimerMs = Random.nextLong(1200, 3000)
                    }
                    OpponentState.HIT, OpponentState.FROZEN -> {
                        // Recover and hide
                        updated.state = OpponentState.HIDDEN
                        updated.stateTimerMs = Random.nextLong(2000, 4000)
                        updated.hp = 1
                    }
                }
            }
            updated
        }

        // 2. Update Flying Snowballs
        val newFlying = mutableListOf<ActiveSnowball>()
        var addedScore = 0
        var newCombo = current.combo
        var newHearts = current.playerHearts
        var splatAlpha = (current.screenSplatAlpha - deltaSeconds * 0.9f).coerceAtLeast(0f)
        val newParticles = current.particles.toMutableList()
        var chargeBoost = 0f

        for (ball in current.flyingSnowballs) {
            val updatedBall = ball.copy()
            updatedBall.progress += deltaSeconds * updatedBall.speed

            // Interpolate position with parabolic arc
            val p = updatedBall.progress.coerceIn(0f, 1f)
            updatedBall.currentX = updatedBall.startX + (updatedBall.targetX - updatedBall.startX) * p
            val linearY = updatedBall.startY + (updatedBall.targetY - updatedBall.startY) * p
            val arc = 4 * updatedBall.arcHeight * p * (1 - p)
            updatedBall.currentY = linearY - arc

            if (updatedBall.progress >= 1f) {
                // Snowball landed!
                if (updatedBall.fromPlayer) {
                    // Check collision with opponents
                    var hitAny = false
                    for (opp in updatedOpponents) {
                        val oppX = opp.xNorm * screenWidth
                        val oppY = opp.yNorm * screenHeight
                        val dist = hypot(updatedBall.targetX - oppX, updatedBall.targetY - oppY)

                        val hitRadius = screenWidth * 0.15f * (if (updatedBall.type == SnowballType.MEGA) 1.6f else 1.0f)
                        if (dist < hitRadius && opp.state != OpponentState.HIDDEN && opp.state != OpponentState.HIT) {
                            // HIT!
                            hitAny = true
                            opp.state = if (updatedBall.type == SnowballType.SLUSH) OpponentState.FROZEN else OpponentState.HIT
                            opp.stateTimerMs = if (updatedBall.type == SnowballType.SLUSH) 3500 else 1800
                            opp.hitSplatTimerMs = 1200

                            val hitBonus = if (opp.state == OpponentState.AIMING) 200 else 100
                            val typeMult = if (updatedBall.type == SnowballType.RAINBOW) 2 else 1
                            newCombo++
                            val points = (hitBonus * newCombo * typeMult)
                            addedScore += points
                            chargeBoost += 0.25f

                            soundEffects.playHit()
                            if (newCombo % 3 == 0) {
                                soundEffects.playChime()
                            }

                            // Spawn floating text particle
                            newParticles.add(
                                Particle(
                                    x = oppX,
                                    y = oppY - 40f,
                                    vx = 0f,
                                    vy = -70f,
                                    alpha = 1f,
                                    color = if (updatedBall.type == SnowballType.RAINBOW) WinterGold else DashGreen,
                                    size = 24f,
                                    maxLifeMs = 900,
                                    isText = true,
                                    text = if (newCombo > 1) "+$points (${newCombo}x!)" else "+$points"
                                )
                            )

                            // Spawn snow splat particles
                            for (k in 0 until 14) {
                                val angle = Random.nextFloat() * 6.28f
                                val speed = Random.nextFloat() * 140f + 40f
                                newParticles.add(
                                    Particle(
                                        x = oppX,
                                        y = oppY,
                                        vx = kotlin.math.cos(angle) * speed,
                                        vy = kotlin.math.sin(angle) * speed,
                                        alpha = 1f,
                                        color = if (updatedBall.type == SnowballType.RAINBOW) WinterYellow else Color.White,
                                        size = Random.nextFloat() * 8f + 4f,
                                        maxLifeMs = 600
                                    )
                                )
                            }
                            break
                        }
                    }

                    if (!hitAny) {
                        // Missed - combo reset
                        newCombo = 0
                        // Snow puff on ground
                        for (k in 0 until 6) {
                            newParticles.add(
                                Particle(
                                    x = updatedBall.targetX,
                                    y = updatedBall.targetY,
                                    vx = (Random.nextFloat() - 0.5f) * 60f,
                                    vy = -Random.nextFloat() * 40f,
                                    alpha = 0.8f,
                                    color = Color.White,
                                    size = 6f,
                                    maxLifeMs = 400
                                )
                            )
                        }
                    }
                } else {
                    // Snowball from opponent reached player
                    if (!current.isDucking) {
                        // Player is hit!
                        newHearts = (newHearts - 1).coerceAtLeast(0)
                        splatAlpha = 0.95f
                        newCombo = 0
                        soundEffects.playPlayerHit()

                        // Snow splat particles on screen
                        for (k in 0 until 16) {
                            newParticles.add(
                                Particle(
                                    x = screenWidth * 0.5f + (Random.nextFloat() - 0.5f) * 200f,
                                    y = screenHeight * 0.5f + (Random.nextFloat() - 0.5f) * 200f,
                                    vx = (Random.nextFloat() - 0.5f) * 120f,
                                    vy = (Random.nextFloat() - 0.5f) * 120f,
                                    alpha = 1f,
                                    color = Color.White,
                                    size = 12f,
                                    maxLifeMs = 700
                                )
                            )
                        }
                    } else {
                        // Ducked! Whistled overhead
                        soundEffects.playDodge()
                    }
                }
            } else {
                newFlying.add(updatedBall)
            }
        }

        // 3. Update Particles
        val updatedParticles = mutableListOf<Particle>()
        for (part in newParticles) {
            part.ageMs += deltaMs
            if (part.ageMs < part.maxLifeMs) {
                part.x += part.vx * deltaSeconds
                part.y += part.vy * deltaSeconds
                part.alpha = (1f - (part.ageMs.toFloat() / part.maxLifeMs)).coerceIn(0f, 1f)
                updatedParticles.add(part)
            }
        }

        // Background falling snowflakes
        if (updatedParticles.size < 60 && Random.nextFloat() < 0.35f) {
            updatedParticles.add(
                Particle(
                    x = Random.nextFloat() * screenWidth,
                    y = -10f,
                    vx = (Random.nextFloat() - 0.5f) * 30f,
                    vy = Random.nextFloat() * 80f + 40f,
                    alpha = Random.nextFloat() * 0.7f + 0.3f,
                    color = Color.White,
                    size = Random.nextFloat() * 5f + 3f,
                    maxLifeMs = 8000
                )
            )
        }

        // Auto reload if out
        var handSnow = current.snowballsInHand
        if (handSnow <= 0 && current.flyingSnowballs.isEmpty()) {
            handSnow = 10
        }

        val updatedCharge = (current.specialCharge + chargeBoost).coerceIn(0f, 1f)
        if (current.specialCharge < 1f && updatedCharge >= 1f) {
            soundEffects.playPowerUp()
        }

        // Check Victory / Game Over
        val isDefeated = newHearts <= 0
        val isVictorious = (current.score + addedScore) >= stage.targetScore

        if (isDefeated || isVictorious) {
            val totalScore = current.score + addedScore
            val stars = if (isVictorious) {
                if (totalScore >= stage.targetScore * 1.5f && newHearts >= 2) 3
                else if (totalScore >= stage.targetScore * 1.2f) 2
                else 1
            } else 0

            if (isVictorious) soundEffects.playVictory() else soundEffects.playDefeat()
            onGameFinished(totalScore, stars, isVictorious)

            _uiState.value = current.copy(
                score = totalScore,
                combo = newCombo,
                playerHearts = newHearts,
                screenSplatAlpha = splatAlpha,
                opponents = updatedOpponents,
                flyingSnowballs = newFlying,
                particles = updatedParticles,
                specialCharge = updatedCharge,
                isGameOver = true,
                isVictory = isVictorious,
                starsEarned = stars
            )
            return
        }

        _uiState.value = current.copy(
            score = current.score + addedScore,
            combo = newCombo,
            playerHearts = newHearts,
            screenSplatAlpha = splatAlpha,
            opponents = updatedOpponents,
            flyingSnowballs = newFlying,
            particles = updatedParticles,
            snowballsInHand = handSnow,
            specialCharge = updatedCharge
        )
    }

    private fun launchOpponentSnowball(opp: OpponentSlot, screenWidth: Float, screenHeight: Float) {
        val startX = opp.xNorm * screenWidth
        val startY = opp.yNorm * screenHeight
        val targetX = screenWidth * 0.5f + (Random.nextFloat() - 0.5f) * screenWidth * 0.35f
        val targetY = screenHeight * 0.85f

        val current = _uiState.value
        val list = current.flyingSnowballs.toMutableList()
        list.add(
            ActiveSnowball(
                id = nextSnowballId++,
                fromPlayer = false,
                startX = startX,
                startY = startY,
                targetX = targetX,
                targetY = targetY,
                currentX = startX,
                currentY = startY,
                progress = 0f,
                speed = 0.95f + (opp.character.speedRating * 0.12f),
                arcHeight = 120f,
                type = SnowballType.STANDARD
            )
        )
        soundEffects.playThrow()
        _uiState.value = current.copy(flyingSnowballs = list)
    }

    fun decrementSecond() {
        val current = _uiState.value
        if (current.isGameOver) return
        val newTime = current.timeLeftSeconds - 1
        if (newTime <= 0) {
            val isVictorious = current.score >= stage.targetScore
            val stars = if (isVictorious) 1 else 0
            if (isVictorious) soundEffects.playVictory() else soundEffects.playDefeat()
            onGameFinished(current.score, stars, isVictorious)
            _uiState.value = current.copy(
                timeLeftSeconds = 0,
                isGameOver = true,
                isVictory = isVictorious,
                starsEarned = stars
            )
        } else {
            _uiState.value = current.copy(timeLeftSeconds = newTime)
        }
    }
}
