package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.*

enum class CharacterId(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val specialPower: String,
    val speedRating: Int, // 1 to 5
    val powerRating: Int, // 1 to 5
    val avatarResId: Int,
    val fullBodyResId: Int?
) {
    DASH(
        displayName = "Dash",
        description = "The classic green PBS Kids mascot! Super fast thrower & agile dodger.",
        primaryColor = DashGreen,
        secondaryColor = Color.White,
        specialPower = "Dash Speed Barrage",
        speedRating = 5,
        powerRating = 4,
        avatarResId = R.drawable.avatar_dash,
        fullBodyResId = R.drawable.dash_standing
    ),
    DOT(
        displayName = "Dot",
        description = "Dash's clever sister in her iconic pink-and-white striped sweater! Pinpoint accuracy & curveballs.",
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color.White,
        specialPower = "Rainbow Curveball",
        speedRating = 4,
        powerRating = 4,
        avatarResId = R.drawable.avatar_dot,
        fullBodyResId = R.drawable.dot_character
    ),
    HOOPER(
        displayName = "Hooper",
        description = "The cheerful 3D guinea pig friend with bright yellow glasses! Rapid double snowballs.",
        primaryColor = Color(0xFF8D6E63),
        secondaryColor = WinterYellow,
        specialPower = "Double Acorn Volley",
        speedRating = 4,
        powerRating = 4,
        avatarResId = R.drawable.avatar_hooper,
        fullBodyResId = R.drawable.hooper_character
    ),
    RACCOON_DASH(
        displayName = "Raccoon Dash",
        description = "Dash in his special raccoon suit with a striped tail! Agile tail-spin snowball volley.",
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFF8D6E63),
        specialPower = "Tail-Spin Blizzard",
        speedRating = 5,
        powerRating = 5,
        avatarResId = R.drawable.avatar_raccoon_dash,
        fullBodyResId = R.drawable.raccoon_dash
    ),
    DEL(
        displayName = "Del",
        description = "Cool backwards cap friend! Packs massive, heavy snow boulders.",
        primaryColor = WinterOrange,
        secondaryColor = PbsCyan,
        specialPower = "Mega Snow Boulder",
        speedRating = 3,
        powerRating = 5,
        avatarResId = R.drawable.avatar_dash,
        fullBodyResId = null
    ),
    DIGIT(
        displayName = "Digit",
        description = "The cyber-bird pal! Deploys ice shields and cyber snowballs.",
        primaryColor = PbsCyan,
        secondaryColor = WinterGold,
        specialPower = "Cyber Frost Shield",
        speedRating = 4,
        powerRating = 4,
        avatarResId = R.drawable.avatar_dash,
        fullBodyResId = null
    ),
    RUFF(
        displayName = "Ruff Ruffman",
        description = "The goofy dog host! Throws squeaky bone ice balls that bounce.",
        primaryColor = Color(0xFFD78235),
        secondaryColor = Color(0xFF8D6E63),
        specialPower = "Squeaky Bone Slush",
        speedRating = 3,
        powerRating = 4,
        avatarResId = R.drawable.avatar_dash,
        fullBodyResId = null
    )
}

enum class SnowballType(
    val displayName: String,
    val iconName: String,
    val color: Color,
    val radius: Float, // base canvas size
    val damage: Int,
    val description: String
) {
    STANDARD(
        displayName = "Classic Snow",
        iconName = "ac_unit",
        color = Color(0xFFE1F5FE),
        radius = 24f,
        damage = 1,
        description = "Reliable, crisp 2013 snowball with fast packing speed."
    ),
    MEGA(
        displayName = "Mega Snowball",
        iconName = "lens",
        color = Color(0xFF81D4FA),
        radius = 42f,
        damage = 2,
        description = "Heavy snowball boulder that shatters snow barriers."
    ),
    TRIPLE(
        displayName = "Triple Burst",
        iconName = "filter_3",
        color = Color(0xFF80DEEA),
        radius = 20f,
        damage = 1,
        description = "Throws 3 snowballs in a fan spread across the yard."
    ),
    SLUSH(
        displayName = "Slush Freeze",
        iconName = "severe_cold",
        color = Color(0xFF4DD0E1),
        radius = 22f,
        damage = 1,
        description = "Icy slush that freezes opponents in place for 3 seconds."
    ),
    RAINBOW(
        displayName = "Star Sparkle",
        iconName = "auto_awesome",
        color = Color(0xFFFFD54F),
        radius = 26f,
        damage = 2,
        description = "Magical sparkling snowball worth 2x score & bonus confetti!"
    )
}

data class StageInfo(
    val id: Int,
    val title: String,
    val subtitle: String,
    val targetScore: Int,
    val timeLimitSeconds: Int,
    val maxOpponents: Int,
    val backdropColorTop: Color,
    val backdropColorBottom: Color,
    val unlocksCharacter: CharacterId?
)

val GAME_STAGES = listOf(
    StageInfo(
        id = 1,
        title = "Dash's Schoolyard",
        subtitle = "Warm up behind the classic snow forts!",
        targetScore = 1000,
        timeLimitSeconds = 45,
        maxOpponents = 2,
        backdropColorTop = Color(0xFF0288D1),
        backdropColorBottom = Color(0xFFB3E5FC),
        unlocksCharacter = CharacterId.DOT
    ),
    StageInfo(
        id = 2,
        title = "Snowy Playground",
        subtitle = "Duck and dodge by the slides & swings!",
        targetScore = 2000,
        timeLimitSeconds = 50,
        maxOpponents = 3,
        backdropColorTop = Color(0xFF039BE5),
        backdropColorBottom = Color(0xFFE1F5FE),
        unlocksCharacter = CharacterId.HOOPER
    ),
    StageInfo(
        id = 3,
        title = "Frosty Pine Woods",
        subtitle = "Watch out for fast snowballers hiding behind evergreen pines!",
        targetScore = 3200,
        timeLimitSeconds = 55,
        maxOpponents = 4,
        backdropColorTop = Color(0xFF0277BD),
        backdropColorBottom = Color(0xFF80DEEA),
        unlocksCharacter = CharacterId.DEL
    ),
    StageInfo(
        id = 4,
        title = "Igloo Village",
        subtitle = "Slippery ice and igloo barricades!",
        targetScore = 4500,
        timeLimitSeconds = 60,
        maxOpponents = 4,
        backdropColorTop = Color(0xFF01579B),
        backdropColorBottom = Color(0xFFB2EBF2),
        unlocksCharacter = CharacterId.DIGIT
    ),
    StageInfo(
        id = 5,
        title = "Snowman Ridge",
        subtitle = "Giant snowmen and rapid multi-throwers!",
        targetScore = 6000,
        timeLimitSeconds = 65,
        maxOpponents = 5,
        backdropColorTop = Color(0xFF1A237E),
        backdropColorBottom = Color(0xFF90CAF9),
        unlocksCharacter = CharacterId.RUFF
    ),
    StageInfo(
        id = 6,
        title = "2013 Blizzard Cup",
        subtitle = "The ultimate winter championship with all Dash & Friends!",
        targetScore = 8000,
        timeLimitSeconds = 70,
        maxOpponents = 5,
        backdropColorTop = Color(0xFF283593),
        backdropColorBottom = Color(0xFFE1F5FE),
        unlocksCharacter = CharacterId.RACCOON_DASH
    )
)

enum class OpponentState {
    HIDDEN,
    PEEKING,
    AIMING,
    THROWING,
    HIT,
    FROZEN
}

data class OpponentSlot(
    val id: Int,
    val xNorm: Float, // 0..1 horizontal position
    val yNorm: Float, // 0..1 depth / height position
    val character: CharacterId,
    var state: OpponentState = OpponentState.HIDDEN,
    var stateTimerMs: Long = 0,
    var maxStateDurationMs: Long = 2000,
    var coverType: String = "Snow Wall",
    var hp: Int = 1,
    var hitSplatTimerMs: Long = 0
)

data class ActiveSnowball(
    val id: Long,
    val fromPlayer: Boolean,
    val startX: Float,
    val startY: Float,
    val targetX: Float,
    val targetY: Float,
    var currentX: Float,
    var currentY: Float,
    var progress: Float, // 0.0 -> 1.0
    val speed: Float,
    val arcHeight: Float,
    val type: SnowballType,
    var hasLanded: Boolean = false
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    val color: Color,
    val size: Float,
    val maxLifeMs: Long,
    var ageMs: Long = 0,
    val isText: Boolean = false,
    val text: String = ""
)

data class SnowmanCustomization(
    val hatIndex: Int = 0,
    val scarfIndex: Int = 0,
    val faceIndex: Int = 0,
    val badgeIndex: Int = 0,
    val snowballSize: Float = 1.0f
)
