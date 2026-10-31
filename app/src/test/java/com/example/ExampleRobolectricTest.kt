package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CharacterId
import com.example.model.GAME_STAGES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dash & Friends Snowball Fight", appName)
  }

  @Test
  fun `verify game stages configured`() {
    assertEquals(6, GAME_STAGES.size)
    assertEquals("Dash's Schoolyard", GAME_STAGES[0].title)
  }

  @Test
  fun `verify dash character exists`() {
    val dash = CharacterId.DASH
    assertEquals("Dash", dash.displayName)
    assertTrue(dash.speedRating >= 4)
  }

  @Test
  fun `verify roster includes dot hooper and raccoon dash`() {
    val dot = CharacterId.DOT
    val hooper = CharacterId.HOOPER
    val raccoonDash = CharacterId.RACCOON_DASH

    assertEquals("Dot", dot.displayName)
    assertEquals("Hooper", hooper.displayName)
    assertEquals("Raccoon Dash", raccoonDash.displayName)
    assertTrue(raccoonDash.fullBodyResId != null)
  }
}
