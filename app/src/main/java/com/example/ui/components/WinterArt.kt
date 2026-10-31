package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.CharacterId
import com.example.model.OpponentState
import com.example.model.SnowballType
import com.example.ui.theme.*
import kotlin.math.sin

/**
 * Canvas drawing utilities for Dash & Friends 2013 arcade characters and winter scenery.
 */
object WinterArt {

    /**
     * Draw full winter backdrop: Sky gradient, snowy hills, distant pine trees, and foreground snow drifts.
     */
    fun drawWinterBackdrop(
        drawScope: DrawScope,
        skyTop: Color,
        skyBottom: Color,
        timeSeconds: Float
    ) {
        with(drawScope) {
            val w = size.width
            val h = size.height

            // 1. Sky Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(skyTop, skyBottom),
                    startY = 0f,
                    endY = h * 0.65f
                )
            )

            // 2. Winter Sun / Glow
            drawCircle(
                color = WinterYellow.copy(alpha = 0.45f),
                radius = w * 0.14f,
                center = Offset(w * 0.82f, h * 0.12f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = w * 0.08f,
                center = Offset(w * 0.82f, h * 0.12f)
            )

            // 3. Far Distant Snowy Mountains
            val mountainPath = Path().apply {
                moveTo(0f, h * 0.42f)
                lineTo(w * 0.22f, h * 0.28f)
                lineTo(w * 0.48f, h * 0.38f)
                lineTo(w * 0.72f, h * 0.25f)
                lineTo(w, h * 0.39f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(mountainPath, color = Color(0xFFB0BEC5).copy(alpha = 0.4f))

            // 4. Midground Rolling Snow Hills
            val midHillPath = Path().apply {
                moveTo(0f, h * 0.46f)
                cubicTo(w * 0.3f, h * 0.42f, w * 0.7f, h * 0.50f, w, h * 0.44f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                midHillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2)),
                    startY = h * 0.42f,
                    endY = h * 0.65f
                )
            )

            // 5. Pine Trees in Midground
            val treePositions = listOf(
                Pair(0.08f, 0.44f),
                Pair(0.18f, 0.46f),
                Pair(0.85f, 0.43f),
                Pair(0.94f, 0.45f)
            )
            for ((tx, ty) in treePositions) {
                drawPineTree(this, w * tx, h * ty, w * 0.07f, h * 0.11f)
            }

            // 6. Foreground Snow Ground
            val groundPath = Path().apply {
                moveTo(0f, h * 0.58f)
                cubicTo(w * 0.25f, h * 0.55f, w * 0.75f, h * 0.62f, w, h * 0.56f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                groundPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                    startY = h * 0.55f,
                    endY = h
                )
            )

            // Subtle snow ground shadows / drifts
            drawOval(
                color = SnowShadow.copy(alpha = 0.25f),
                topLeft = Offset(w * 0.1f, h * 0.68f),
                size = Size(w * 0.4f, h * 0.06f)
            )
            drawOval(
                color = SnowShadow.copy(alpha = 0.25f),
                topLeft = Offset(w * 0.55f, h * 0.72f),
                size = Size(w * 0.4f, h * 0.06f)
            )
        }
    }

    private fun drawPineTree(drawScope: DrawScope, x: Float, y: Float, width: Float, height: Float) {
        with(drawScope) {
            // Trunk
            drawRect(
                color = Color(0xFF5D4037),
                topLeft = Offset(x - width * 0.12f, y + height * 0.7f),
                size = Size(width * 0.24f, height * 0.35f)
            )
            // 3 tiers of pine branches
            val tiers = listOf(
                Triple(y + height * 0.55f, width * 0.95f, height * 0.4f),
                Triple(y + height * 0.32f, width * 0.75f, height * 0.38f),
                Triple(y + height * 0.1f, width * 0.55f, height * 0.35f)
            )
            for ((tierY, tierW, tierH) in tiers) {
                val branchPath = Path().apply {
                    moveTo(x, tierY - tierH * 0.5f)
                    lineTo(x + tierW * 0.5f, tierY + tierH * 0.5f)
                    lineTo(x - tierW * 0.5f, tierY + tierH * 0.5f)
                    close()
                }
                drawPath(branchPath, color = Color(0xFF2E7D32))
                // Snow on top of tier
                val snowBranchPath = Path().apply {
                    moveTo(x, tierY - tierH * 0.5f)
                    lineTo(x + tierW * 0.35f, tierY)
                    lineTo(x - tierW * 0.35f, tierY)
                    close()
                }
                drawPath(snowBranchPath, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }

    /**
     * Draw Fort / Obstacle Cover (Snow wall, Igloo, or Big Snowman)
     */
    fun drawCover(
        drawScope: DrawScope,
        coverType: String,
        cx: Float,
        cy: Float,
        width: Float,
        height: Float
    ) {
        with(drawScope) {
            when (coverType) {
                "Igloo" -> {
                    // Igloo Dome
                    val domeRect = Rect(cx - width * 0.5f, cy - height * 0.85f, cx + width * 0.5f, cy + height * 0.45f)
                    drawArc(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFE0F7FA), Color(0xFF81D4FA)),
                            startY = cy - height * 0.85f,
                            endY = cy + height * 0.45f
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(domeRect.left, domeRect.top),
                        size = Size(domeRect.width, domeRect.height)
                    )
                    // Ice block grid lines
                    drawArc(
                        color = Color(0xFF4FC3F7).copy(alpha = 0.5f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(domeRect.left, domeRect.top),
                        size = Size(domeRect.width, domeRect.height),
                        style = Stroke(width = 3f)
                    )
                    // Igloo Entrance
                    val archRect = Rect(cx - width * 0.22f, cy - height * 0.35f, cx + width * 0.22f, cy + height * 0.45f)
                    drawArc(
                        color = Color(0xFF003650),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(archRect.left, archRect.top),
                        size = Size(archRect.width, archRect.height)
                    )
                }
                "Snow Wall" -> {
                    // Solid curved snow barricade
                    val wallPath = Path().apply {
                        moveTo(cx - width * 0.5f, cy + height * 0.35f)
                        lineTo(cx - width * 0.48f, cy - height * 0.4f)
                        cubicTo(cx - width * 0.2f, cy - height * 0.55f, cx + width * 0.2f, cy - height * 0.55f, cx + width * 0.48f, cy - height * 0.4f)
                        lineTo(cx + width * 0.5f, cy + height * 0.35f)
                        close()
                    }
                    drawPath(
                        wallPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFF81D4FA)),
                            startY = cy - height * 0.55f,
                            endY = cy + height * 0.35f
                        )
                    )
                    drawPath(
                        wallPath,
                        color = Color(0xFF4FC3F7),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
                    )
                    // Snow brick groove details
                    val brickY1 = cy - height * 0.15f
                    val brickY2 = cy + height * 0.1f
                    drawLine(
                        color = Color(0xFF81D4FA).copy(alpha = 0.6f),
                        start = Offset(cx - width * 0.35f, brickY1),
                        end = Offset(cx + width * 0.35f, brickY1),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFF81D4FA).copy(alpha = 0.6f),
                        start = Offset(cx - width * 0.4f, brickY2),
                        end = Offset(cx + width * 0.4f, brickY2),
                        strokeWidth = 2.5f
                    )
                }
                else -> {
                    // Default Snow Drift Fort
                    drawOval(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color(0xFFB3E5FC)),
                            startY = cy - height * 0.5f,
                            endY = cy + height * 0.35f
                        ),
                        topLeft = Offset(cx - width * 0.5f, cy - height * 0.45f),
                        size = Size(width, height * 0.8f)
                    )
                    drawOval(
                        color = Color(0xFF81D4FA),
                        topLeft = Offset(cx - width * 0.5f, cy - height * 0.45f),
                        size = Size(width, height * 0.8f),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
        }
    }

    /**
     * Draw 2013 Character Peeking/Aiming/Reacting
     */
    fun drawCharacter(
        drawScope: DrawScope,
        character: CharacterId,
        state: OpponentState,
        cx: Float,
        cy: Float,
        radius: Float,
        splatProgress: Float = 0f
    ) {
        with(drawScope) {
            val headRadius = radius * 0.85f

            // Character Head Circle
            drawCircle(
                color = character.primaryColor,
                radius = headRadius,
                center = Offset(cx, cy)
            )
            // Black outline (Classic 2013 PBS KIDS cartoon outline)
            drawCircle(
                color = Color.Black,
                radius = headRadius,
                center = Offset(cx, cy),
                style = Stroke(width = 4f)
            )

            when (character) {
                CharacterId.DASH -> {
                    // Dash's spiky black hair on top
                    val hairPath = Path().apply {
                        moveTo(cx - headRadius * 0.85f, cy - headRadius * 0.2f)
                        // spikes tilting right
                        lineTo(cx - headRadius * 0.6f, cy - headRadius * 1.15f)
                        lineTo(cx - headRadius * 0.25f, cy - headRadius * 0.85f)
                        lineTo(cx + headRadius * 0.15f, cy - headRadius * 1.25f)
                        lineTo(cx + headRadius * 0.45f, cy - headRadius * 0.8f)
                        lineTo(cx + headRadius * 0.95f, cy - headRadius * 0.2f)
                        cubicTo(cx + headRadius * 0.4f, cy - headRadius * 0.65f, cx - headRadius * 0.4f, cy - headRadius * 0.65f, cx - headRadius * 0.85f, cy - headRadius * 0.2f)
                        close()
                    }
                    drawPath(hairPath, color = Color.Black, style = Fill)
                }
                CharacterId.DOT -> {
                    // Dot's iconic black bob haircut from official PBS Kids art
                    val bobHair = Path().apply {
                        moveTo(cx - headRadius * 1.05f, cy + headRadius * 0.1f)
                        lineTo(cx - headRadius * 0.85f, cy - headRadius * 0.9f)
                        cubicTo(cx - headRadius * 0.4f, cy - headRadius * 1.3f, cx + headRadius * 0.4f, cy - headRadius * 1.3f, cx + headRadius * 0.85f, cy - headRadius * 0.9f)
                        lineTo(cx + headRadius * 1.05f, cy + headRadius * 0.1f)
                        lineTo(cx + headRadius * 0.65f, cy - headRadius * 0.25f)
                        cubicTo(cx + headRadius * 0.2f, cy - headRadius * 0.65f, cx - headRadius * 0.2f, cy - headRadius * 0.65f, cx - headRadius * 0.65f, cy - headRadius * 0.25f)
                        close()
                    }
                    drawPath(bobHair, color = Color.Black)

                    // Dot's pink and white striped collar
                    drawOval(
                        color = Color(0xFFE91E63),
                        topLeft = Offset(cx - headRadius * 0.55f, cy + headRadius * 0.72f),
                        size = Size(headRadius * 1.1f, headRadius * 0.35f)
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cx - headRadius * 0.45f, cy + headRadius * 0.8f),
                        size = Size(headRadius * 0.9f, headRadius * 0.08f)
                    )
                }
                CharacterId.HOOPER -> {
                    // Hooper's fluffy guinea pig ears
                    drawCircle(color = Color(0xFF8D6E63), radius = headRadius * 0.36f, center = Offset(cx - headRadius * 0.75f, cy - headRadius * 0.75f))
                    drawCircle(color = Color(0xFFFFCCBC), radius = headRadius * 0.2f, center = Offset(cx - headRadius * 0.75f, cy - headRadius * 0.75f))
                    drawCircle(color = Color(0xFF8D6E63), radius = headRadius * 0.36f, center = Offset(cx + headRadius * 0.75f, cy - headRadius * 0.75f))
                    drawCircle(color = Color(0xFFFFCCBC), radius = headRadius * 0.2f, center = Offset(cx + headRadius * 0.75f, cy - headRadius * 0.75f))

                    // White fur stripe down forehead
                    drawOval(
                        color = Color.White,
                        topLeft = Offset(cx - headRadius * 0.22f, cy - headRadius * 0.85f),
                        size = Size(headRadius * 0.44f, headRadius * 0.9f)
                    )

                    // Big Iconic Yellow Glasses
                    drawCircle(color = WinterYellow, radius = headRadius * 0.35f, center = Offset(cx - headRadius * 0.38f, cy - headRadius * 0.1f), style = Stroke(width = 5f))
                    drawCircle(color = WinterYellow, radius = headRadius * 0.35f, center = Offset(cx + headRadius * 0.38f, cy - headRadius * 0.1f), style = Stroke(width = 5f))
                    drawLine(WinterYellow, Offset(cx - headRadius * 0.1f, cy - headRadius * 0.1f), Offset(cx + headRadius * 0.1f, cy - headRadius * 0.1f), strokeWidth = 5f)
                }
                CharacterId.RACCOON_DASH -> {
                    // Dash's spiky black hair
                    val hairPath = Path().apply {
                        moveTo(cx - headRadius * 0.85f, cy - headRadius * 0.2f)
                        lineTo(cx - headRadius * 0.6f, cy - headRadius * 1.15f)
                        lineTo(cx - headRadius * 0.25f, cy - headRadius * 0.85f)
                        lineTo(cx + headRadius * 0.15f, cy - headRadius * 1.25f)
                        lineTo(cx + headRadius * 0.45f, cy - headRadius * 0.8f)
                        lineTo(cx + headRadius * 0.95f, cy - headRadius * 0.2f)
                        cubicTo(cx + headRadius * 0.4f, cy - headRadius * 0.65f, cx - headRadius * 0.4f, cy - headRadius * 0.65f, cx - headRadius * 0.85f, cy - headRadius * 0.2f)
                        close()
                    }
                    drawPath(hairPath, color = Color.Black, style = Fill)

                    // Striped Raccoon Tail sticking out
                    val tailPath = Path().apply {
                        moveTo(cx - headRadius * 0.8f, cy + headRadius * 0.5f)
                        cubicTo(cx - headRadius * 1.6f, cy + headRadius * 0.2f, cx - headRadius * 1.7f, cy + headRadius * 0.9f, cx - headRadius * 0.8f, cy + headRadius * 0.9f)
                        close()
                    }
                    drawPath(tailPath, color = Color.Black)
                    // Brown/Gold stripes on tail
                    drawLine(Color(0xFFD7CCC8), Offset(cx - headRadius * 1.2f, cy + headRadius * 0.4f), Offset(cx - headRadius * 1.1f, cy + headRadius * 0.85f), strokeWidth = 6f)
                    drawLine(Color(0xFFD7CCC8), Offset(cx - headRadius * 1.45f, cy + headRadius * 0.45f), Offset(cx - headRadius * 1.35f, cy + headRadius * 0.75f), strokeWidth = 5f)
                }
                CharacterId.DEL -> {
                    // Backwards orange baseball cap
                    val capPath = Path().apply {
                        moveTo(cx - headRadius * 0.9f, cy - headRadius * 0.2f)
                        cubicTo(cx - headRadius * 0.6f, cy - headRadius * 1.15f, cx + headRadius * 0.6f, cy - headRadius * 1.15f, cx + headRadius * 0.9f, cy - headRadius * 0.2f)
                        close()
                    }
                    drawPath(capPath, color = WinterOrange)
                    drawPath(capPath, color = Color.Black, style = Stroke(width = 3.5f))
                    // Cap brim sticking out back left
                    drawOval(
                        color = WinterOrange,
                        topLeft = Offset(cx - headRadius * 1.25f, cy - headRadius * 0.4f),
                        size = Size(headRadius * 0.55f, headRadius * 0.25f)
                    )
                    drawOval(
                        color = Color.Black,
                        topLeft = Offset(cx - headRadius * 1.25f, cy - headRadius * 0.4f),
                        size = Size(headRadius * 0.55f, headRadius * 0.25f),
                        style = Stroke(width = 3f)
                    )
                }
                CharacterId.DIGIT -> {
                    // Cyber crest on head
                    val crestPath = Path().apply {
                        moveTo(cx, cy - headRadius * 1.3f)
                        lineTo(cx + headRadius * 0.3f, cy - headRadius * 0.8f)
                        lineTo(cx - headRadius * 0.3f, cy - headRadius * 0.8f)
                        close()
                    }
                    drawPath(crestPath, color = WinterGold)
                    drawPath(crestPath, color = Color.Black, style = Stroke(width = 3f))
                }
                CharacterId.RUFF -> {
                    // Floppy brown dog ears
                    drawOval(
                        color = Color(0xFF6D4C41),
                        topLeft = Offset(cx - headRadius * 1.15f, cy - headRadius * 0.3f),
                        size = Size(headRadius * 0.45f, headRadius * 0.9f)
                    )
                    drawOval(
                        color = Color.Black,
                        topLeft = Offset(cx - headRadius * 1.15f, cy - headRadius * 0.3f),
                        size = Size(headRadius * 0.45f, headRadius * 0.9f),
                        style = Stroke(width = 3f)
                    )
                    drawOval(
                        color = Color(0xFF6D4C41),
                        topLeft = Offset(cx + headRadius * 0.7f, cy - headRadius * 0.3f),
                        size = Size(headRadius * 0.45f, headRadius * 0.9f)
                    )
                    drawOval(
                        color = Color.Black,
                        topLeft = Offset(cx + headRadius * 0.7f, cy - headRadius * 0.3f),
                        size = Size(headRadius * 0.45f, headRadius * 0.9f),
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Eyes & Expression based on state
            if (character != CharacterId.DOT) {
                when (state) {
                    OpponentState.HIT, OpponentState.FROZEN -> {
                        // X eyes
                        drawXEye(this, cx - headRadius * 0.35f, cy - headRadius * 0.1f, headRadius * 0.18f)
                        drawXEye(this, cx + headRadius * 0.35f, cy - headRadius * 0.1f, headRadius * 0.18f)
                    }
                    OpponentState.AIMING, OpponentState.THROWING -> {
                        // Determined eyes
                        drawCircle(color = Color.Black, radius = headRadius * 0.18f, center = Offset(cx - headRadius * 0.35f, cy - headRadius * 0.05f))
                        drawCircle(color = Color.White, radius = headRadius * 0.06f, center = Offset(cx - headRadius * 0.31f, cy - headRadius * 0.08f))
                        drawCircle(color = Color.Black, radius = headRadius * 0.18f, center = Offset(cx + headRadius * 0.35f, cy - headRadius * 0.05f))
                        drawCircle(color = Color.White, radius = headRadius * 0.06f, center = Offset(cx + headRadius * 0.39f, cy - headRadius * 0.08f))
                    }
                    else -> {
                        // Cheerful arched or round eyes (PBS Kids style)
                        val eyeArcLeft = Path().apply {
                            moveTo(cx - headRadius * 0.5f, cy - headRadius * 0.05f)
                            cubicTo(cx - headRadius * 0.4f, cy - headRadius * 0.25f, cx - headRadius * 0.2f, cy - headRadius * 0.25f, cx - headRadius * 0.15f, cy - headRadius * 0.05f)
                        }
                        drawPath(eyeArcLeft, color = Color.Black, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
                        val eyeArcRight = Path().apply {
                            moveTo(cx + headRadius * 0.15f, cy - headRadius * 0.05f)
                            cubicTo(cx + headRadius * 0.2f, cy - headRadius * 0.25f, cx + headRadius * 0.4f, cy - headRadius * 0.25f, cx + headRadius * 0.5f, cy - headRadius * 0.05f)
                        }
                        drawPath(eyeArcRight, color = Color.Black, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
                    }
                }
            }

            // Dot nose
            drawCircle(
                color = Color.Black,
                radius = headRadius * 0.08f,
                center = Offset(cx, cy + headRadius * 0.12f)
            )

            // Beaming Smile or O-mouth
            if (state == OpponentState.HIT) {
                // Surprised O mouth
                drawCircle(color = Color.Black, radius = headRadius * 0.18f, center = Offset(cx, cy + headRadius * 0.42f))
                drawCircle(color = Color(0xFFEF5350), radius = headRadius * 0.12f, center = Offset(cx, cy + headRadius * 0.44f))
            } else {
                // Wide cheerful mouth (Classic PBS Kids grin with tooth)
                val mouthPath = Path().apply {
                    moveTo(cx - headRadius * 0.45f, cy + headRadius * 0.32f)
                    cubicTo(cx - headRadius * 0.2f, cy + headRadius * 0.72f, cx + headRadius * 0.2f, cy + headRadius * 0.72f, cx + headRadius * 0.45f, cy + headRadius * 0.32f)
                    close()
                }
                drawPath(mouthPath, color = Color.Black)
                // White tooth
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cx - headRadius * 0.12f, cy + headRadius * 0.32f),
                    size = Size(headRadius * 0.24f, headRadius * 0.12f)
                )
            }

            // Snow Splat on Face if Hit
            if (splatProgress > 0f) {
                val splatRadius = headRadius * 0.75f * splatProgress
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = splatRadius,
                    center = Offset(cx + headRadius * 0.1f, cy)
                )
                // Splat drips
                drawCircle(color = Color.White, radius = splatRadius * 0.35f, center = Offset(cx - headRadius * 0.3f, cy + headRadius * 0.3f))
                drawCircle(color = Color.White, radius = splatRadius * 0.3f, center = Offset(cx + headRadius * 0.4f, cy - headRadius * 0.2f))
                drawCircle(color = Color.White, radius = splatRadius * 0.25f, center = Offset(cx, cy - headRadius * 0.4f))
            }
        }
    }

    private fun drawXEye(drawScope: DrawScope, x: Float, y: Float, size: Float) {
        with(drawScope) {
            drawLine(Color.Black, Offset(x - size, y - size), Offset(x + size, y + size), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(Color.Black, Offset(x + size, y - size), Offset(x - size, y + size), strokeWidth = 4f, cap = StrokeCap.Round)
        }
    }

    /**
     * Draw flying snowball with 3D gradient shading and shadow
     */
    fun drawSnowball(
        drawScope: DrawScope,
        x: Float,
        y: Float,
        radius: Float,
        type: SnowballType
    ) {
        with(drawScope) {
            // Shadow under snowball
            drawOval(
                color = Color.Black.copy(alpha = 0.2f),
                topLeft = Offset(x - radius * 0.7f, y + radius * 0.6f),
                size = Size(radius * 1.4f, radius * 0.5f)
            )

            // Snowball sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, type.color, SnowShadow),
                    center = Offset(x - radius * 0.3f, y - radius * 0.3f),
                    radius = radius * 1.2f
                ),
                radius = radius,
                center = Offset(x, y)
            )

            // Outline
            drawCircle(
                color = Color(0xFF0288D1).copy(alpha = 0.4f),
                radius = radius,
                center = Offset(x, y),
                style = Stroke(width = 2.5f)
            )

            // Special type adornments
            if (type == SnowballType.RAINBOW) {
                drawCircle(
                    color = WinterYellow.copy(alpha = 0.6f),
                    radius = radius * 0.4f,
                    center = Offset(x - radius * 0.2f, y - radius * 0.2f)
                )
            } else if (type == SnowballType.MEGA) {
                // Ice cracks
                drawLine(
                    color = Color(0xFF0288D1).copy(alpha = 0.6f),
                    start = Offset(x - radius * 0.4f, y - radius * 0.2f),
                    end = Offset(x + radius * 0.3f, y + radius * 0.3f),
                    strokeWidth = 2.5f
                )
            }
        }
    }

    /**
     * Draw customized Snowman in Workshop
     */
    fun drawSnowman(
        drawScope: DrawScope,
        cx: Float,
        cy: Float,
        scale: Float,
        hatIndex: Int,
        scarfIndex: Int,
        faceIndex: Int,
        badgeIndex: Int
    ) {
        with(drawScope) {
            val baseRadius = 80f * scale
            val midRadius = 60f * scale
            val headRadius = 45f * scale

            val baseY = cy + 90f * scale
            val midY = cy
            val headY = cy - 75f * scale

            // Ground shadow
            drawOval(
                color = Color(0xFF81D4FA).copy(alpha = 0.4f),
                topLeft = Offset(cx - baseRadius * 1.2f, baseY + baseRadius * 0.7f),
                size = Size(baseRadius * 2.4f, baseRadius * 0.6f)
            )

            // 1. Bottom Snowball
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                    center = Offset(cx - baseRadius * 0.3f, baseY - baseRadius * 0.3f),
                    radius = baseRadius * 1.2f
                ),
                radius = baseRadius,
                center = Offset(cx, baseY)
            )
            drawCircle(color = Color(0xFF4FC3F7).copy(alpha = 0.5f), radius = baseRadius, center = Offset(cx, baseY), style = Stroke(width = 3f))

            // 2. Middle Snowball
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                    center = Offset(cx - midRadius * 0.3f, midY - midRadius * 0.3f),
                    radius = midRadius * 1.2f
                ),
                radius = midRadius,
                center = Offset(cx, midY)
            )
            drawCircle(color = Color(0xFF4FC3F7).copy(alpha = 0.5f), radius = midRadius, center = Offset(cx, midY), style = Stroke(width = 3f))

            // Stick Arms
            drawLine(color = Color(0xFF5D4037), start = Offset(cx - midRadius * 0.8f, midY - 10f * scale), end = Offset(cx - midRadius * 1.7f, midY - 45f * scale), strokeWidth = 5f * scale, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF5D4037), start = Offset(cx + midRadius * 0.8f, midY - 10f * scale), end = Offset(cx + midRadius * 1.7f, midY - 45f * scale), strokeWidth = 5f * scale, cap = StrokeCap.Round)

            // Middle Buttons / Badge
            val buttonColors = listOf(Color(0xFF212121), WinterGold, DashGreen, WinterRed)
            val btnColor = buttonColors[badgeIndex.coerceIn(0, buttonColors.size - 1)]
            drawCircle(color = btnColor, radius = 7f * scale, center = Offset(cx, midY - 18f * scale))
            drawCircle(color = btnColor, radius = 7f * scale, center = Offset(cx, midY + 12f * scale))

            // 3. Head Snowball
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
                    center = Offset(cx - headRadius * 0.3f, headY - headRadius * 0.3f),
                    radius = headRadius * 1.2f
                ),
                radius = headRadius,
                center = Offset(cx, headY)
            )
            drawCircle(color = Color(0xFF4FC3F7).copy(alpha = 0.5f), radius = headRadius, center = Offset(cx, headY), style = Stroke(width = 3f))

            // Face (Carrot, Coal, Sunglasses)
            when (faceIndex) {
                1 -> { // Cool Sunglasses
                    drawRect(color = Color(0xFF212121), topLeft = Offset(cx - headRadius * 0.6f, headY - 10f * scale), size = Size(headRadius * 1.2f, 18f * scale))
                    // Carrot under shades
                    val carrot = Path().apply {
                        moveTo(cx, headY + 5f * scale)
                        lineTo(cx + 35f * scale, headY + 14f * scale)
                        lineTo(cx, headY + 18f * scale)
                        close()
                    }
                    drawPath(carrot, color = Color(0xFFFF6D00))
                }
                2 -> { // Winking Star Face
                    drawCircle(color = Color.Black, radius = 6f * scale, center = Offset(cx - 18f * scale, headY - 10f * scale))
                    // Wink eye
                    drawLine(color = Color.Black, start = Offset(cx + 10f * scale, headY - 10f * scale), end = Offset(cx + 25f * scale, headY - 10f * scale), strokeWidth = 4f * scale, cap = StrokeCap.Round)
                    // Button smile
                    val carrot = Path().apply {
                        moveTo(cx, headY)
                        lineTo(cx + 30f * scale, headY + 8f * scale)
                        lineTo(cx, headY + 14f * scale)
                        close()
                    }
                    drawPath(carrot, color = Color(0xFFFF6D00))
                }
                else -> { // Classic Coal Eyes & Carrot Nose
                    drawCircle(color = Color.Black, radius = 6f * scale, center = Offset(cx - 16f * scale, headY - 10f * scale))
                    drawCircle(color = Color.Black, radius = 6f * scale, center = Offset(cx + 16f * scale, headY - 10f * scale))
                    val carrot = Path().apply {
                        moveTo(cx, headY)
                        lineTo(cx + 35f * scale, headY + 8f * scale)
                        lineTo(cx, headY + 15f * scale)
                        close()
                    }
                    drawPath(carrot, color = Color(0xFFFF6D00))
                    // Coal smile dots
                    val smileDots = listOf(-20f, -10f, 0f, 10f, 20f)
                    for (dotX in smileDots) {
                        val dotY = headY + 24f * scale + (dotX * dotX) / (60f * scale)
                        drawCircle(color = Color.Black, radius = 4f * scale, center = Offset(cx + dotX * scale, dotY))
                    }
                }
            }

            // Scarf
            val scarfColors = listOf(WinterRed, DashGreen, WinterPurple, WinterGold)
            val scarfColor = scarfColors[scarfIndex.coerceIn(0, scarfColors.size - 1)]
            // Neck wrap
            drawOval(
                color = scarfColor,
                topLeft = Offset(cx - headRadius * 0.8f, headY + headRadius * 0.7f),
                size = Size(headRadius * 1.6f, 24f * scale)
            )
            // Scarf tail hanging down
            val tailPath = Path().apply {
                moveTo(cx + 15f * scale, headY + headRadius * 0.9f)
                lineTo(cx + 38f * scale, headY + headRadius * 0.9f)
                lineTo(cx + 42f * scale, headY + headRadius * 0.9f + 65f * scale)
                lineTo(cx + 18f * scale, headY + headRadius * 0.9f + 60f * scale)
                close()
            }
            drawPath(tailPath, color = scarfColor)
            // Fringe
            drawLine(color = Color.White, start = Offset(cx + 18f * scale, headY + headRadius * 0.9f + 60f * scale), end = Offset(cx + 42f * scale, headY + headRadius * 0.9f + 65f * scale), strokeWidth = 3f * scale)

            // Hat
            when (hatIndex) {
                1 -> { // PBS Kids Blue Winter Beanie with pompom
                    val beanie = Path().apply {
                        moveTo(cx - headRadius * 0.9f, headY - headRadius * 0.5f)
                        cubicTo(cx - headRadius * 0.7f, headY - headRadius * 1.8f, cx + headRadius * 0.7f, headY - headRadius * 1.8f, cx + headRadius * 0.9f, headY - headRadius * 0.5f)
                        close()
                    }
                    drawPath(beanie, color = PbsCyan)
                    drawRect(color = Color.White, topLeft = Offset(cx - headRadius * 0.95f, headY - headRadius * 0.65f), size = Size(headRadius * 1.9f, 16f * scale))
                    // Pompom
                    drawCircle(color = Color.White, radius = 16f * scale, center = Offset(cx, headY - headRadius * 1.7f))
                }
                2 -> { // Classic Black Top Hat with red ribbon
                    drawRect(color = Color(0xFF212121), topLeft = Offset(cx - headRadius * 1.1f, headY - headRadius * 0.7f), size = Size(headRadius * 2.2f, 12f * scale))
                    drawRect(color = Color(0xFF212121), topLeft = Offset(cx - headRadius * 0.6f, headY - headRadius * 1.7f), size = Size(headRadius * 1.2f, headRadius * 1.05f))
                    drawRect(color = WinterRed, topLeft = Offset(cx - headRadius * 0.6f, headY - headRadius * 0.95f), size = Size(headRadius * 1.2f, 12f * scale))
                }
                3 -> { // Cozy Earmuffs
                    drawArc(
                        color = Color(0xFF424242),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(cx - headRadius * 1.05f, headY - headRadius * 1.1f),
                        size = Size(headRadius * 2.1f, headRadius * 1.6f),
                        style = Stroke(width = 6f * scale)
                    )
                    drawCircle(color = WinterOrange, radius = 20f * scale, center = Offset(cx - headRadius * 0.95f, headY - 5f * scale))
                    drawCircle(color = WinterOrange, radius = 20f * scale, center = Offset(cx + headRadius * 0.95f, headY - 5f * scale))
                }
                else -> { // Warm Red Santa/Winter Cap
                    val hatPath = Path().apply {
                        moveTo(cx - headRadius * 0.9f, headY - headRadius * 0.5f)
                        cubicTo(cx - headRadius * 0.5f, headY - headRadius * 1.6f, cx + headRadius * 0.6f, headY - headRadius * 1.2f, cx + headRadius * 1.2f, headY - headRadius * 0.2f)
                        lineTo(cx + headRadius * 0.9f, headY - headRadius * 0.5f)
                        close()
                    }
                    drawPath(hatPath, color = WinterRed)
                    drawRect(color = Color.White, topLeft = Offset(cx - headRadius * 0.95f, headY - headRadius * 0.65f), size = Size(headRadius * 1.9f, 16f * scale))
                    drawCircle(color = Color.White, radius = 14f * scale, center = Offset(cx + headRadius * 1.2f, headY - headRadius * 0.2f))
                }
            }
        }
    }
}
