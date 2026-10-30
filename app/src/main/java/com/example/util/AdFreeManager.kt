package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

data class AdSenseConfig(
  val publisherId: String = "ca-pub-9009399398398580",
  val bannerAdUnitId: String = "9034181554",
  val interstitialAdUnitId: String = "9034181554",
  val rewardedAdUnitId: String = "9034181554",
  val rotationAdUnitId: String = "9034181554",
  val authorizedDomain: String = "https://ram6sham.github.io",
  val isPopupEnabled: Boolean = true,
  val isRotationEnabled: Boolean = true,
  val isBannerEnabled: Boolean = true
)

class AdFreeManager(context: Context) {

  private val prefs: SharedPreferences = context.getSharedPreferences("pptv_ad_prefs", Context.MODE_PRIVATE)

  private val _isAdFree = MutableStateFlow(checkIfActive())
  val isAdFree: StateFlow<Boolean> = _isAdFree.asStateFlow()

  private val _remainingTimeFormatted = MutableStateFlow(calculateRemainingTime())
  val remainingTimeFormatted: StateFlow<String> = _remainingTimeFormatted.asStateFlow()

  private val _adConfig = MutableStateFlow(loadAdConfig())
  val adConfig: StateFlow<AdSenseConfig> = _adConfig.asStateFlow()

  private fun checkIfActive(): Boolean {
    val expiry = prefs.getLong("ad_free_until", 0L)
    return System.currentTimeMillis() < expiry
  }

  fun updateStatus() {
    _isAdFree.value = checkIfActive()
    _remainingTimeFormatted.value = calculateRemainingTime()
  }

  /**
   * Grants a 12-hour Ad-Free pass to the user.
   * Required after watching either the 15-second mandatory popup ad or the 20-second rewarded ad.
   */
  fun grant12HourPass() {
    val twelveHoursInMillis = TimeUnit.HOURS.toMillis(12)
    val expiry = System.currentTimeMillis() + twelveHoursInMillis
    prefs.edit().putLong("ad_free_until", expiry).apply()
    updateStatus()
  }

  fun calculateRemainingTime(): String {
    val expiry = prefs.getLong("ad_free_until", 0L)
    val remainingMillis = expiry - System.currentTimeMillis()
    if (remainingMillis <= 0) return "No active pass"

    val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis) % 60
    return "${hours}h ${minutes}m left"
  }

  private fun loadAdConfig(): AdSenseConfig {
    return AdSenseConfig(
      publisherId = prefs.getString("ad_pub_id", "ca-pub-9009399398398580") ?: "ca-pub-9009399398398580",
      bannerAdUnitId = prefs.getString("ad_banner_id", "9034181554") ?: "9034181554",
      interstitialAdUnitId = prefs.getString("ad_interstitial_id", "9034181554") ?: "9034181554",
      rewardedAdUnitId = prefs.getString("ad_rewarded_id", "9034181554") ?: "9034181554",
      rotationAdUnitId = prefs.getString("ad_rotation_id", "9034181554") ?: "9034181554",
      authorizedDomain = prefs.getString("ad_auth_domain", "https://ram6sham.github.io") ?: "https://ram6sham.github.io",
      isPopupEnabled = prefs.getBoolean("ad_popup_enabled", true),
      isRotationEnabled = prefs.getBoolean("ad_rotation_enabled", true),
      isBannerEnabled = prefs.getBoolean("ad_banner_enabled", true)
    )
  }

  fun saveAdConfig(
    publisherId: String,
    bannerId: String,
    interstitialId: String,
    rewardedId: String,
    rotationId: String,
    authorizedDomain: String = "https://ram6sham.github.io",
    popupEnabled: Boolean,
    rotationEnabled: Boolean,
    bannerEnabled: Boolean
  ) {
    val updated = AdSenseConfig(
      publisherId = publisherId.trim(),
      bannerAdUnitId = bannerId.trim(),
      interstitialAdUnitId = interstitialId.trim(),
      rewardedAdUnitId = rewardedId.trim(),
      rotationAdUnitId = rotationId.trim(),
      authorizedDomain = if (authorizedDomain.isBlank()) "https://ram6sham.github.io" else authorizedDomain.trim(),
      isPopupEnabled = popupEnabled,
      isRotationEnabled = rotationEnabled,
      isBannerEnabled = bannerEnabled
    )
    prefs.edit()
      .putString("ad_pub_id", updated.publisherId)
      .putString("ad_banner_id", updated.bannerAdUnitId)
      .putString("ad_interstitial_id", updated.interstitialAdUnitId)
      .putString("ad_rewarded_id", updated.rewardedAdUnitId)
      .putString("ad_rotation_id", updated.rotationAdUnitId)
      .putString("ad_auth_domain", updated.authorizedDomain)
      .putBoolean("ad_popup_enabled", updated.isPopupEnabled)
      .putBoolean("ad_rotation_enabled", updated.isRotationEnabled)
      .putBoolean("ad_banner_enabled", updated.isBannerEnabled)
      .apply()

    _adConfig.value = updated
  }
}
