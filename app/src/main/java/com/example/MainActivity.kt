package com.example

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddStreamDialog
import com.example.ui.components.AdminUnlockDialog
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.MandatoryPopupAdDialog
import com.example.ui.components.RemoteConfigDialog
import com.example.ui.components.RewardedAdDialog
import com.example.ui.components.RotationAdDialog
import com.example.ui.screens.BroadcastStudioScreen
import com.example.ui.screens.CricketScreen
import com.example.ui.screens.FootballScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.NotificationHelper
import com.example.viewmodel.MainNavTab
import com.example.viewmodel.PPTVViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: PPTVViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Requirement: Keep Screen ALWAYS On while live streaming - never sleeps or dims
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    try {
      NotificationHelper.createNotificationChannel(this)
    } catch (_: Throwable) {
      // Safe ignore
    }

    intent?.getStringExtra("EXTRA_STREAM_ID")?.let { streamId ->
      viewModel.selectStreamById(streamId)
    }

    setContent {
      MyApplicationTheme {
        val context = LocalContext.current

        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { /* Granted or denied */ }

        LaunchedEffect(Unit) {
          try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
              ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            }
          } catch (_: Throwable) {
            // Safe ignore
          }
        }

        PPTVAppContent(viewModel = viewModel)
      }
    }
  }

  override fun onNewIntent(intent: android.content.Intent) {
    super.onNewIntent(intent)
    intent.getStringExtra("EXTRA_STREAM_ID")?.let { streamId ->
      viewModel.selectStreamById(streamId)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PPTVAppContent(viewModel: PPTVViewModel) {
  val context = LocalContext.current
  val activity = context as? Activity
  val configuration = LocalConfiguration.current

  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val manualFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
  val effectiveFullscreen = (isLandscape || manualFullscreen)

  // Listen to mobile rotation for Requirement 6 Rotation Ad
  LaunchedEffect(isLandscape) {
    viewModel.onOrientationChanged(isLandscape)
  }

  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val currentStream by viewModel.currentStream.collectAsStateWithLifecycle()
  val activeServerUrl by viewModel.activeServerUrl.collectAsStateWithLifecycle()
  val selectedServerName by viewModel.selectedServerName.collectAsStateWithLifecycle()
  val liveMatches by viewModel.currentlyLiveMatches.collectAsStateWithLifecycle()
  val footballStreams by viewModel.footballStreams.collectAsStateWithLifecycle()
  val cricketStreams by viewModel.cricketStreams.collectAsStateWithLifecycle()
  val otherStreams by viewModel.otherSportsStreams.collectAsStateWithLifecycle()
  val recommendations by viewModel.personalizedRecommendations.collectAsStateWithLifecycle()
  val allStreams by viewModel.streams.collectAsStateWithLifecycle()
  val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
  val remoteConfigUrl by viewModel.remoteConfigUrl.collectAsStateWithLifecycle()
  val remoteSyncStatus by viewModel.remoteSyncStatus.collectAsStateWithLifecycle()

  // Ad Free State & Google AdSense Config (Requirement 6)
  val isAdFree by viewModel.isAdFree.collectAsStateWithLifecycle()
  val remainingAdFreeTime by viewModel.remainingAdFreeTime.collectAsStateWithLifecycle()
  val adSenseConfig by viewModel.adSenseConfig.collectAsStateWithLifecycle()

  // Ad Dialog States
  val showMandatoryPopupAd by viewModel.showMandatoryPopupAd.collectAsStateWithLifecycle()
  val showRotationAd by viewModel.showRotationAd.collectAsStateWithLifecycle()
  val showRewardedAdDialog by viewModel.showRewardedAdDialog.collectAsStateWithLifecycle()

  // Other Dialog States
  val showAddStreamDialog by viewModel.showAddStreamDialog.collectAsStateWithLifecycle()
  val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsStateWithLifecycle()
  val showRemoteConfigDialog by viewModel.showRemoteConfigDialog.collectAsStateWithLifecycle()
  val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()

  LaunchedEffect(effectiveFullscreen) {
    try {
      val window = activity?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (effectiveFullscreen) {
          insetsController.hide(WindowInsetsCompat.Type.systemBars())
          insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
          insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
      }
    } catch (_: Throwable) {
      // Safe ignore
    }
  }

  BackHandler(enabled = effectiveFullscreen) {
    try {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
      viewModel.setIsFullscreen(false)
    } catch (_: Throwable) {
      // Safe ignore
    }
  }

  val toggleFullscreenAction = {
    try {
      if (effectiveFullscreen) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
        viewModel.setIsFullscreen(false)
      } else {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        viewModel.setIsFullscreen(true)
      }
    } catch (_: Throwable) {
      // Safe ignore
    }
  }

  Scaffold(
    containerColor = ObsidianBackground,
    topBar = {
      if (!effectiveFullscreen) {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary
          ),
          title = {
            // Tapping logo 7 times unlocks Admin Panel (Requirement 5)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.clickable { viewModel.onLogoClicked() }
            ) {
              Surface(
                shape = CircleShape,
                color = LiveRed,
                modifier = Modifier.size(32.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text("IP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text("IP TV", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
              Spacer(modifier = Modifier.width(6.dp))

              Surface(
                shape = CircleShape,
                color = LiveRed.copy(alpha = 0.2f)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(LiveRed)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("LIVE", color = LiveRed, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
              }

              if (isAdminMode) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Color(0xFF10B981).copy(alpha = 0.2f)
                ) {
                  Text(
                    text = "ADMIN",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                  )
                }
              }
            }
          },
          actions = {
            // Ad-Free Pass trigger / badge (Requirement 6)
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isAdFree) Color(0xFF10B981).copy(alpha = 0.2f) else SurfaceHighlight,
              modifier = Modifier
                .clickable { viewModel.openRewardedAdDialog() }
                .padding(end = 8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = if (isAdFree) "🛡️ Ad-Free" else "🎁 Ads Free",
                  fontSize = 11.sp,
                  color = if (isAdFree) Color(0xFF10B981) else BroadcastCyan,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            if (isAdminMode) {
              IconButton(
                onClick = { viewModel.openAddStreamDialog() },
                modifier = Modifier.testTag("top_bar_add_stream_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add Match Link",
                  tint = BroadcastCyan
                )
              }
            }
          }
        )
      }
    },
    bottomBar = {
      if (!effectiveFullscreen) {
        NavigationBar(
          containerColor = SurfaceDark,
          contentColor = TextPrimary,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          // Tab 1: Home (Requirement 1)
          NavigationBarItem(
            selected = currentTab == MainNavTab.HOME,
            onClick = { viewModel.setTab(MainNavTab.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_home")
          )

          // Tab 2: Football (Requirement 2)
          NavigationBarItem(
            selected = currentTab == MainNavTab.FOOTBALL,
            onClick = { viewModel.setTab(MainNavTab.FOOTBALL) },
            icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Football") },
            label = { Text("Football", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_football")
          )

          // Tab 3: Cricket (Requirement 3)
          NavigationBarItem(
            selected = currentTab == MainNavTab.CRICKET,
            onClick = { viewModel.setTab(MainNavTab.CRICKET) },
            icon = { Icon(Icons.Default.SportsCricket, contentDescription = "Cricket") },
            label = { Text("Cricket", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_cricket")
          )

          // Tab 4: More (Requirement 2 & 4: Volleyball, WWE & More sports)
          NavigationBarItem(
            selected = currentTab == MainNavTab.MORE,
            onClick = { viewModel.setTab(MainNavTab.MORE) },
            icon = { Icon(Icons.Default.SportsVolleyball, contentDescription = "More") },
            label = { Text("More", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_more")
          )

          // Tab 5: Admin Panel (Requirement 5 - ONLY visible when 7 taps unlocked)
          if (isAdminMode) {
            NavigationBarItem(
              selected = currentTab == MainNavTab.BROADCAST,
              onClick = { viewModel.setTab(MainNavTab.BROADCAST) },
              icon = { Icon(Icons.Default.Campaign, contentDescription = "Admin Panel") },
              label = { Text("Admin", fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = LiveRed,
                indicatorColor = LiveRed,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
              ),
              modifier = Modifier.testTag("nav_tab_broadcast")
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(if (effectiveFullscreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
    ) {
      when (currentTab) {
        MainNavTab.HOME -> {
          HomeScreen(
            currentStream = currentStream,
            activeServerUrl = activeServerUrl,
            selectedServerName = selectedServerName,
            liveMatches = liveMatches,
            recommendedMatches = recommendations,
            isAdFree = isAdFree,
            remainingAdFreeTime = remainingAdFreeTime,
            adSenseConfig = adSenseConfig,
            isFullscreen = effectiveFullscreen,
            isAdminMode = isAdminMode,
            onSelectServer = { name, url -> viewModel.selectServer(name, url) },
            onLikeStream = { viewModel.likeCurrentStream() },
            onSelectLiveMatch = { match -> viewModel.selectStream(match) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() },
            onOpenRewardedAd = { viewModel.openRewardedAdDialog() },
            onSyncRemoteGist = { viewModel.syncRemoteStreamsNow() },
            onFullscreenToggle = toggleFullscreenAction
          )
        }
        MainNavTab.FOOTBALL -> {
          FootballScreen(
            footballStreams = footballStreams,
            isAdminMode = isAdminMode,
            onStreamClick = { stream -> viewModel.selectStream(stream) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() }
          )
        }
        MainNavTab.CRICKET -> {
          CricketScreen(
            cricketStreams = cricketStreams,
            isAdminMode = isAdminMode,
            onStreamClick = { stream -> viewModel.selectStream(stream) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() }
          )
        }
        MainNavTab.MORE -> {
          MoreScreen(
            otherStreams = otherStreams,
            isAdminMode = isAdminMode,
            onStreamClick = { stream -> viewModel.selectStream(stream) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() }
          )
        }
        MainNavTab.BROADCAST -> {
          if (isAdminMode) {
            BroadcastStudioScreen(
              allStreams = allStreams,
              adSenseConfig = adSenseConfig,
              onSaveAndBroadcast = { title, url, sport, status, broadcaster, desc, score, s1, s2, s3, notify, autoPlay ->
                viewModel.saveAndBroadcastStream(title, url, sport, status, broadcaster, desc, score, s1, s2, s3, notify, autoPlay)
              },
              onSelectStream = { stream -> viewModel.selectStream(stream) },
              onSaveAdSenseConfig = { pub, banner, inter, rew, rot, authDom, popE, rotE, banE ->
                viewModel.saveAdSenseConfig(pub, banner, inter, rew, rot, authDom, popE, rotE, banE)
              },
              onDeleteStream = { streamId -> viewModel.deleteStream(streamId) },
              onUpdateStream = { stream -> viewModel.updateStream(stream) },
              onOpenRemoteConfig = { viewModel.openRemoteConfigDialog() }
            )
          } else {
            LaunchedEffect(Unit) {
              viewModel.setTab(MainNavTab.HOME)
            }
          }
        }
      }

      if (!effectiveFullscreen) {
        InAppNotificationBanner(
          alert = activeAlert,
          onWatchNow = { streamId ->
            viewModel.dismissAlert()
            viewModel.selectStreamById(streamId)
          },
          onDismiss = { viewModel.dismissAlert() },
          modifier = Modifier.align(Alignment.TopCenter)
        )
      }

      if (showAddStreamDialog && isAdminMode) {
        AddStreamDialog(
          onDismiss = { viewModel.closeAddStreamDialog() },
          onSaveAndBroadcast = { title, url, sport, status, broadcaster, desc, score, s1, s2, s3, notify, autoPlay ->
            viewModel.saveAndBroadcastStream(title, url, sport, status, broadcaster, desc, score, s1, s2, s3, notify, autoPlay)
          }
        )
      }

      // Requirement 5: Admin Login Dialog (Triggered by 7 taps on logo)
      if (showAdminLoginDialog) {
        AdminUnlockDialog(
          onDismiss = { viewModel.closeAdminLoginDialog() },
          onVerifyPin = { pass -> viewModel.verifyAdminPassword(pass) }
        )
      }

      // Requirement 6: Mandatory 15s Startup Popup Ad
      if (showMandatoryPopupAd) {
        MandatoryPopupAdDialog(
          publisherId = adSenseConfig.publisherId,
          adUnitId = adSenseConfig.interstitialAdUnitId,
          authorizedDomain = adSenseConfig.authorizedDomain,
          onDismiss = { viewModel.closeMandatoryPopupAd() },
          onRewardEarned = { viewModel.grant12HourAdFreePass() }
        )
      }

      // Requirement 6: Mobile Rotation Interstitial Ad
      if (showRotationAd) {
        RotationAdDialog(
          adUnitId = adSenseConfig.rotationAdUnitId,
          publisherId = adSenseConfig.publisherId,
          authorizedDomain = adSenseConfig.authorizedDomain,
          onDismiss = { viewModel.closeRotationAd() }
        )
      }

      // Requirement 6: Rewarded 20s Ad for 12 Hours Ad-Free
      if (showRewardedAdDialog) {
        RewardedAdDialog(
          publisherId = adSenseConfig.publisherId,
          adUnitId = adSenseConfig.rewardedAdUnitId,
          authorizedDomain = adSenseConfig.authorizedDomain,
          onDismiss = { viewModel.closeRewardedAdDialog() },
          onRewardEarned = { viewModel.grant12HourAdFreePass() }
        )
      }

      // Requirement 1 & 7: Remote GitHub Gist Config Dialog
      if (showRemoteConfigDialog) {
        RemoteConfigDialog(
          currentUrl = remoteConfigUrl,
          syncStatus = remoteSyncStatus,
          onDismiss = { viewModel.closeRemoteConfigDialog() },
          onSaveUrl = { url -> viewModel.setRemoteConfigUrl(url) },
          onSyncNow = { viewModel.syncRemoteStreamsNow() }
        )
      }
    }
  }
}
