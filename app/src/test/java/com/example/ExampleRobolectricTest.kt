package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RecommendationEngine
import com.example.data.StreamRepository
import com.example.model.MatchStatus
import com.example.model.SportCategory
import com.example.util.AdFreeManager
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
    assertEquals("IP TV", appName)
  }

  @Test
  fun `test MainActivity launches and resumes cleanly without crash`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    val activity = controller.get()
    assertNotNull(activity)
    controller.pause().stop().destroy()
  }

  @Test
  fun `test add and broadcast sports match link with multi-server`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    val initialCount = repository.streams.value.size
    val newStream = repository.addAndBroadcastStream(
      title = "Nepal vs UAE Live Final",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      sportCategory = SportCategory.CRICKET,
      matchStatus = MatchStatus.LIVE,
      broadcasterName = "PP Sports Live",
      description = "Live Final Match",
      score = "NEP 175/4",
      server1Url = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      server2Url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
      server3Url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
      notifyInstantly = false,
      autoPlayNow = true
    )

    assertEquals("Nepal vs UAE Live Final", newStream.title)
    assertEquals(SportCategory.CRICKET, newStream.sportCategory)
    assertEquals("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", newStream.server1Url)
    assertEquals("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", newStream.server2Url)
    assertEquals(3, newStream.getAvailableServers().size)
    assertEquals(repository.currentStream.value?.id, newStream.id)
    assertEquals(initialCount + 1, repository.streams.value.size)
  }

  @Test
  fun `test recommendation engine match calculation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    val streamA = repository.addAndBroadcastStream(
      title = "Match 1",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      sportCategory = SportCategory.FOOTBALL,
      matchStatus = MatchStatus.LIVE,
      broadcasterName = "IP TV",
      description = "Test Football Match",
      notifyInstantly = false
    )
    val streamB = repository.addAndBroadcastStream(
      title = "Match 2",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      sportCategory = SportCategory.FOOTBALL,
      matchStatus = MatchStatus.LIVE,
      broadcasterName = "IP TV",
      description = "Another Football Match",
      notifyInstantly = false
    )

    val streams = repository.streams.value
    val recommendations = RecommendationEngine.getRecommendations(
      allStreams = streams,
      watchHistoryIds = listOf(streamA.id),
      currentStreamId = streamA.id
    )

    assertTrue(recommendations.isNotEmpty())
    assertNotNull(recommendations.first().recommendationReason)
    assertTrue(recommendations.first().matchPercentage in 70..100)
  }

  @Test
  fun `test admin mode protection with password PPTV134679`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    // Initially in Viewer Mode
    assertFalse(repository.isAdminMode.value)

    // Incorrect password fails
    val wrongAttempt = repository.verifyAdminPassword("wrong123")
    assertFalse(wrongAttempt)
    assertFalse(repository.isAdminMode.value)

    // Correct password PPTV134679 unlocks
    val success = repository.verifyAdminPassword("PPTV134679")
    assertTrue(success)
    assertTrue(repository.isAdminMode.value)

    // Lock back
    repository.lockAdminMode()
    assertFalse(repository.isAdminMode.value)
  }

  @Test
  fun `test ad free manager 12 hour pass and adsense config persistence`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val adFreeManager = AdFreeManager(context)

    adFreeManager.grant12HourPass()
    assertTrue(adFreeManager.isAdFree.value)
    assertTrue(adFreeManager.remainingTimeFormatted.value.contains("h"))

    // Save AdSense config
    adFreeManager.saveAdConfig(
      publisherId = "pub-999888777666",
      bannerId = "banner-unit-1",
      interstitialId = "interstitial-unit-2",
      rewardedId = "rewarded-unit-3",
      rotationId = "rotation-unit-4",
      popupEnabled = true,
      rotationEnabled = true,
      bannerEnabled = true
    )

    val updatedConfig = adFreeManager.adConfig.value
    assertEquals("pub-999888777666", updatedConfig.publisherId)
    assertEquals("banner-unit-1", updatedConfig.bannerAdUnitId)
    assertEquals("interstitial-unit-2", updatedConfig.interstitialAdUnitId)
    assertTrue(updatedConfig.isPopupEnabled)
  }

  @Test
  fun `test delete stream removes from repository`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    val stream = repository.addAndBroadcastStream(
      title = "Temporary Test Match To Delete",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      sportCategory = SportCategory.FOOTBALL,
      matchStatus = MatchStatus.LIVE,
      broadcasterName = "IP TV",
      description = "Match to delete",
      notifyInstantly = false
    )

    assertTrue(repository.streams.value.any { it.id == stream.id })

    repository.deleteStream(stream.id)

    assertFalse(repository.streams.value.any { it.id == stream.id })

    // Simulate app reload with fresh repository instance
    val reloadedRepository = StreamRepository(context)
    assertFalse(reloadedRepository.streams.value.any { it.id == stream.id })
  }

  @Test
  fun `test duplicate stream ids are safely deduplicated without crash`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = StreamRepository(context)

    // Verify updating with same ID does not create duplicates
    val stream1 = repository.addAndBroadcastStream(
      title = "Nepal vs Netherlands",
      streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
      sportCategory = SportCategory.CRICKET,
      matchStatus = MatchStatus.LIVE,
      broadcasterName = "IP TV Sports",
      description = "Cricket Match",
      notifyInstantly = false
    )
    val countBefore = repository.streams.value.count { it.id == stream1.id }
    assertEquals(1, countBefore)

    // Update the stream
    repository.updateStream(stream1.copy(score = "NEP 160/3"))
    val countAfter = repository.streams.value.count { it.id == stream1.id }
    assertEquals(1, countAfter)
  }
}
