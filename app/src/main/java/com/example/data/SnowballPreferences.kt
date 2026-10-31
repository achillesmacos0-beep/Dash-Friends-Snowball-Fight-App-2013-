package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CharacterId
import com.example.model.SnowmanCustomization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SnowballPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("dash_snowball_prefs", Context.MODE_PRIVATE)

    private val _selectedCharacter = MutableStateFlow(loadSelectedCharacter())
    val selectedCharacter: StateFlow<CharacterId> = _selectedCharacter.asStateFlow()

    private val _unlockedCharacters = MutableStateFlow(loadUnlockedCharacters())
    val unlockedCharacters: StateFlow<Set<CharacterId>> = _unlockedCharacters.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(prefs.getBoolean("vibration_enabled", true))
    val vibrationEnabled: StateFlow<Boolean> = _vibrationEnabled.asStateFlow()

    private val _endlessHighScore = MutableStateFlow(prefs.getInt("endless_high_score", 0))
    val endlessHighScore: StateFlow<Int> = _endlessHighScore.asStateFlow()

    private val _totalSnowballsThrown = MutableStateFlow(prefs.getInt("total_snowballs", 0))
    val totalSnowballsThrown: StateFlow<Int> = _totalSnowballsThrown.asStateFlow()

    private val _totalHits = MutableStateFlow(prefs.getInt("total_hits", 0))
    val totalHits: StateFlow<Int> = _totalHits.asStateFlow()

    private val _snowmanCustomization = MutableStateFlow(loadSnowmanCustomization())
    val snowmanCustomization: StateFlow<SnowmanCustomization> = _snowmanCustomization.asStateFlow()

    fun setSelectedCharacter(character: CharacterId) {
        prefs.edit().putString("selected_character", character.name).apply()
        _selectedCharacter.value = character
    }

    private fun loadSelectedCharacter(): CharacterId {
        val name = prefs.getString("selected_character", CharacterId.DASH.name)
        return try {
            CharacterId.valueOf(name ?: CharacterId.DASH.name)
        } catch (_: Exception) {
            CharacterId.DASH
        }
    }

    fun unlockCharacter(character: CharacterId) {
        val current = _unlockedCharacters.value.toMutableSet()
        current.add(character)
        prefs.edit().putStringSet("unlocked_chars", current.map { it.name }.toSet()).apply()
        _unlockedCharacters.value = current
    }

    private fun loadUnlockedCharacters(): Set<CharacterId> {
        val saved = prefs.getStringSet("unlocked_chars", null)
        return if (saved.isNullOrEmpty()) {
            setOf(CharacterId.DASH, CharacterId.DOT, CharacterId.HOOPER)
        } else {
            saved.mapNotNull {
                try { CharacterId.valueOf(it) } catch (_: Exception) { null }
            }.toSet().ifEmpty { setOf(CharacterId.DASH, CharacterId.DOT) }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
        _vibrationEnabled.value = enabled
    }

    fun saveStageProgress(stageId: Int, score: Int, stars: Int) {
        val bestScore = getStageBestScore(stageId)
        if (score > bestScore) {
            prefs.edit().putInt("stage_score_$stageId", score).apply()
        }
        val currentStars = getStageStars(stageId)
        if (stars > currentStars) {
            prefs.edit().putInt("stage_stars_$stageId", stars).apply()
        }
    }

    fun getStageBestScore(stageId: Int): Int {
        return prefs.getInt("stage_score_$stageId", 0)
    }

    fun getStageStars(stageId: Int): Int {
        return prefs.getInt("stage_stars_$stageId", 0)
    }

    fun saveEndlessScore(score: Int) {
        if (score > _endlessHighScore.value) {
            prefs.edit().putInt("endless_high_score", score).apply()
            _endlessHighScore.value = score
        }
    }

    fun recordStats(thrown: Int, hits: Int) {
        val newThrown = _totalSnowballsThrown.value + thrown
        val newHits = _totalHits.value + hits
        prefs.edit()
            .putInt("total_snowballs", newThrown)
            .putInt("total_hits", newHits)
            .apply()
        _totalSnowballsThrown.value = newThrown
        _totalHits.value = newHits
    }

    fun saveSnowmanCustomization(customization: SnowmanCustomization) {
        prefs.edit()
            .putInt("sm_hat", customization.hatIndex)
            .putInt("sm_scarf", customization.scarfIndex)
            .putInt("sm_face", customization.faceIndex)
            .putInt("sm_badge", customization.badgeIndex)
            .apply()
        _snowmanCustomization.value = customization
    }

    private fun loadSnowmanCustomization(): SnowmanCustomization {
        return SnowmanCustomization(
            hatIndex = prefs.getInt("sm_hat", 0),
            scarfIndex = prefs.getInt("sm_scarf", 0),
            faceIndex = prefs.getInt("sm_face", 0),
            badgeIndex = prefs.getInt("sm_badge", 0)
        )
    }
}
