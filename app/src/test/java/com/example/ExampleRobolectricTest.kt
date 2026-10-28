package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RecommendationEngine
import com.example.data.StreamRepository
import com.example.model.StreamCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PP TV", appName)
  }

  @Test
  fun `test add and broadcast stream link`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    val initialCount = repository.streams.value.size
    val newStream = repository.addAndBroadcastStream(
      title = "Breaking Event Test",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      category = StreamCategory.NEWS,
      broadcasterName = "Test Studio",
      description = "Test stream link",
      notifyInstantly = false,
      autoPlayNow = true
    )

    assertEquals("Breaking Event Test", newStream.title)
    assertEquals(repository.currentStream.value?.id, newStream.id)
    assertEquals(initialCount + 1, repository.streams.value.size)
  }

  @Test
  fun `test recommendation engine match calculation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)
    val streams = repository.streams.value

    val recommendations = RecommendationEngine.getRecommendations(
      allStreams = streams,
      watchHistoryIds = listOf(streams.first().id),
      currentStreamId = streams.first().id
    )

    assertTrue(recommendations.isNotEmpty())
    assertNotNull(recommendations.first().recommendationReason)
    assertTrue(recommendations.first().matchPercentage in 70..100)
  }

  @Test
  fun `test admin mode protection`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    // Initially in Viewer Mode
    assertFalse(repository.isAdminMode.value)

    // Incorrect PIN fails
    val wrongAttempt = repository.verifyAdminPin("0000")
    assertFalse(wrongAttempt)
    assertFalse(repository.isAdminMode.value)

    // Correct default PIN unlocks
    val success = repository.verifyAdminPin("1234")
    assertTrue(success)
    assertTrue(repository.isAdminMode.value)

    // Lock back
    repository.lockAdminMode()
    assertFalse(repository.isAdminMode.value)
  }
}
