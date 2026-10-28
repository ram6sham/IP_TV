package com.example

import android.Manifest
import android.app.Activity
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
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
import androidx.compose.runtime.DisposableEffect
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
import com.example.ui.components.RemoteConfigDialog
import com.example.ui.screens.BroadcastStudioScreen
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.ForYouScreen
import com.example.ui.screens.LiveTVScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
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

    // Keep Screen ALWAYS On while live streaming - never sleeps or dims
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // Initialize notification channels
    NotificationHelper.createNotificationChannel(this)

    // Handle intent extras if opened from notification
    intent?.getStringExtra("EXTRA_STREAM_ID")?.let { streamId ->
      viewModel.selectStreamById(streamId)
    }

    setContent {
      MyApplicationTheme {
        val context = LocalContext.current

        // Request POST_NOTIFICATIONS permission on Android 13+
        val permissionLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestPermission()
        ) { /* Granted or denied */ }

        LaunchedEffect(Unit) {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
              != PackageManager.PERMISSION_GRANTED
            ) {
              permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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

  // Orientation & Fullscreen State
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  val manualFullscreen by viewModel.isFullscreen.collectAsStateWithLifecycle()
  val effectiveFullscreen = (isLandscape || manualFullscreen)

  // Stream & User States
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val currentStream by viewModel.currentStream.collectAsStateWithLifecycle()
  val messages by viewModel.messages.collectAsStateWithLifecycle()
  val streams by viewModel.streams.collectAsStateWithLifecycle()
  val filteredStreams by viewModel.filteredStreams.collectAsStateWithLifecycle()
  val personalizedRecs by viewModel.personalizedRecommendations.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
  val isAdminMode by viewModel.isAdminMode.collectAsStateWithLifecycle()
  val remoteConfigUrl by viewModel.remoteConfigUrl.collectAsStateWithLifecycle()
  val remoteSyncStatus by viewModel.remoteSyncStatus.collectAsStateWithLifecycle()

  // Dialog States
  val showAddStreamDialog by viewModel.showAddStreamDialog.collectAsStateWithLifecycle()
  val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsStateWithLifecycle()
  val showRemoteConfigDialog by viewModel.showRemoteConfigDialog.collectAsStateWithLifecycle()
  val activeAlert by viewModel.activeAlert.collectAsStateWithLifecycle()

  // Fullscreen Landscape Immersive Mode - hides system bars
  LaunchedEffect(effectiveFullscreen) {
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
  }

  // Handle Back Button in Fullscreen
  BackHandler(enabled = effectiveFullscreen) {
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
    viewModel.setIsFullscreen(false)
  }

  // Toggle Fullscreen / Landscape function
  val toggleFullscreenAction = {
    if (effectiveFullscreen) {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
      viewModel.setIsFullscreen(false)
    } else {
      activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
      viewModel.setIsFullscreen(true)
    }
  }

  Scaffold(
    containerColor = ObsidianBackground,
    topBar = {
      // Hide TopAppBar when in true Landscape Fullscreen
      if (!effectiveFullscreen) {
        TopAppBar(
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceDark,
            titleContentColor = TextPrimary
          ),
          title = {
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
                  Text("PP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text("PP TV", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
              Spacer(modifier = Modifier.width(6.dp))

              // LIVE pill
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

              // Admin Mode Pill (Only if Unlocked)
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
            // Add Stream Link button ONLY visible if Admin Mode is unlocked!
            if (isAdminMode) {
              IconButton(
                onClick = { viewModel.openAddStreamDialog() },
                modifier = Modifier.testTag("top_bar_add_stream_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "Add Stream Link",
                  tint = BroadcastCyan
                )
              }
            }
          }
        )
      }
    },
    bottomBar = {
      // Hide Bottom Navigation when in true Landscape Fullscreen
      if (!effectiveFullscreen) {
        NavigationBar(
          containerColor = SurfaceDark,
          contentColor = TextPrimary,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentTab == MainNavTab.LIVE_TV,
            onClick = { viewModel.setTab(MainNavTab.LIVE_TV) },
            icon = { Icon(Icons.Default.LiveTv, contentDescription = "Live TV") },
            label = { Text("Live TV", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_live_tv")
          )

          NavigationBarItem(
            selected = currentTab == MainNavTab.CHANNELS,
            onClick = { viewModel.setTab(MainNavTab.CHANNELS) },
            icon = { Icon(Icons.Default.Tv, contentDescription = "Channels") },
            label = { Text("Channels", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_channels")
          )

          NavigationBarItem(
            selected = currentTab == MainNavTab.FOR_YOU,
            onClick = { viewModel.setTab(MainNavTab.FOR_YOU) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "For You") },
            label = { Text("For You", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_for_you")
          )

          // Settings Tab (Always available for viewers and admin)
          NavigationBarItem(
            selected = currentTab == MainNavTab.SETTINGS,
            onClick = { viewModel.setTab(MainNavTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = Color.Black,
              selectedTextColor = BroadcastCyan,
              indicatorColor = BroadcastCyan,
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_settings")
          )

          // Broadcast Studio Tab (ONLY VISIBLE IF ADMIN MODE IS UNLOCKED!)
          if (isAdminMode) {
            NavigationBarItem(
              selected = currentTab == MainNavTab.BROADCAST,
              onClick = { viewModel.setTab(MainNavTab.BROADCAST) },
              icon = { Icon(Icons.Default.Campaign, contentDescription = "Broadcast") },
              label = { Text("Broadcast", fontSize = 11.sp) },
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
      // Main Screens Switcher
      when (currentTab) {
        MainNavTab.LIVE_TV -> {
          LiveTVScreen(
            currentStream = currentStream,
            messages = messages,
            reactionsFlow = viewModel.reactions,
            recommendedStreams = personalizedRecs,
            isFullscreen = effectiveFullscreen,
            isAdminMode = isAdminMode,
            onSendMessage = { text -> viewModel.sendChatMessage(text) },
            onTriggerReaction = { emoji -> viewModel.triggerReaction(emoji) },
            onLikeStream = { viewModel.likeCurrentStream() },
            onLikeMessage = { id -> viewModel.likeChatMessage(id) },
            onSelectRecommendedStream = { stream -> viewModel.selectStream(stream) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() },
            onFullscreenToggle = toggleFullscreenAction
          )
        }
        MainNavTab.CHANNELS -> {
          ChannelsScreen(
            streams = filteredStreams,
            searchQuery = searchQuery,
            selectedCategory = selectedCategory,
            isAdminMode = isAdminMode,
            onSearchQueryChange = { q -> viewModel.setSearchQuery(q) },
            onCategorySelect = { cat -> viewModel.setCategoryFilter(cat) },
            onStreamClick = { stream -> viewModel.selectStream(stream) },
            onOpenAddStreamDialog = { viewModel.openAddStreamDialog() }
          )
        }
        MainNavTab.FOR_YOU -> {
          ForYouScreen(
            recommendations = personalizedRecs,
            allStreams = streams,
            onSelectStream = { stream -> viewModel.selectStream(stream) }
          )
        }
        MainNavTab.SETTINGS -> {
          SettingsScreen(
            isAdminMode = isAdminMode,
            remoteConfigUrl = remoteConfigUrl,
            remoteSyncStatus = remoteSyncStatus,
            onOpenAdminLogin = { viewModel.openAdminLoginDialog() },
            onLockAdmin = { viewModel.lockAdminMode() },
            onOpenRemoteConfig = { viewModel.openRemoteConfigDialog() }
          )
        }
        MainNavTab.BROADCAST -> {
          if (isAdminMode) {
            BroadcastStudioScreen(
              allStreams = streams,
              onSaveAndBroadcast = { title, url, cat, broadcaster, desc, notify, autoPlay ->
                viewModel.saveAndBroadcastStream(title, url, cat, broadcaster, desc, notify, autoPlay)
              },
              onSelectStream = { stream -> viewModel.selectStream(stream) },
              onOpenRemoteConfig = { viewModel.openRemoteConfigDialog() }
            )
          } else {
            // Auto redirect to Live TV if locked
            LaunchedEffect(Unit) {
              viewModel.setTab(MainNavTab.LIVE_TV)
            }
          }
        }
      }

      // In-App Notification Alert Overlay Banner (Only when not in fullscreen)
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

      // Add Stream Link Modal Dialog (Only in Admin Mode)
      if (showAddStreamDialog && isAdminMode) {
        AddStreamDialog(
          onDismiss = { viewModel.closeAddStreamDialog() },
          onSaveAndBroadcast = { title, url, cat, broadcaster, desc, notify, autoPlay ->
            viewModel.saveAndBroadcastStream(title, url, cat, broadcaster, desc, notify, autoPlay)
          }
        )
      }

      // Admin PIN Verification Dialog
      if (showAdminLoginDialog) {
        AdminUnlockDialog(
          onDismiss = { viewModel.closeAdminLoginDialog() },
          onVerifyPin = { pin -> viewModel.verifyAdminPin(pin) }
        )
      }

      // Remote Config Auto-Update Dialog
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
