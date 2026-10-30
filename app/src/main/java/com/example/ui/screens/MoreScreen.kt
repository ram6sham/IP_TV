package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveStream
import com.example.model.SportCategory
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Requirement 2 & 3:
 * "Other" renamed to "More" Screen.
 * Public "Unlock Access / Broadcaster Access" is completely removed.
 * Admin can only be unlocked via secret 7-tap on the top IP TV logo.
 */
@Composable
fun MoreScreen(
  otherStreams: List<LiveStream>,
  isAdminMode: Boolean = false,
  onStreamClick: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedSportFilter by remember { mutableStateOf<SportCategory?>(null) } // null = All More Sports

  val filteredMatches = otherStreams.filter { match ->
    val matchesQuery = searchQuery.isBlank() ||
        match.title.contains(searchQuery, ignoreCase = true) ||
        match.league.contains(searchQuery, ignoreCase = true)

    val matchesSport = selectedSportFilter == null || match.sportCategory == selectedSportFilter
    matchesQuery && matchesSport
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("more_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(SurfaceDark)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = Color(0xFF6366F1).copy(alpha = 0.2f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("🏐", fontSize = 18.sp)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "More Sports (Volleyball, WWE & More)",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
              )
              Text(
                text = "Volleyball, Wrestling, Basketball & Combat Sports",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          if (isAdminMode) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = LiveRed,
              modifier = Modifier.clickable { onOpenAddStreamDialog() }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search Volleyball, WWE, or other events...", color = TextMuted, fontSize = 12.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = BroadcastCyan)
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceVariantDark,
            unfocusedContainerColor = SurfaceVariantDark,
            focusedBorderColor = BroadcastCyan,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(12.dp)
        )
      }

      // Sports Category Filter Chips (Volleyball, WWE, Other)
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .background(SurfaceDark)
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          val isSelected = selectedSportFilter == null
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
            modifier = Modifier.clickable { selectedSportFilter = null }
          ) {
            Text(
              text = "All Sports",
              color = if (isSelected) Color.Black else TextSecondary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }

        listOf(SportCategory.VOLLYBALL, SportCategory.WWE, SportCategory.OTHER).forEach { sport ->
          item {
            val isSelected = selectedSportFilter == sport
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
              modifier = Modifier.clickable { selectedSportFilter = sport }
            ) {
              Text(
                text = "${sport.icon} ${sport.displayName}",
                color = if (isSelected) Color.Black else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      // Matches List
      if (filteredMatches.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏐", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No matches found in this category", fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Check back soon or select another sport", color = TextSecondary, fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val uniqueMatches = filteredMatches.distinctBy { it.id }
          items(uniqueMatches, key = { "more_${it.id}" }) { match ->
            SportsMatchCard(
              match = match,
              onClick = { onStreamClick(match) }
            )
          }
        }
      }
    }

    if (isAdminMode) {
      FloatingActionButton(
        onClick = onOpenAddStreamDialog,
        containerColor = LiveRed,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Match")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Match", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Backward compatibility alias for OtherSportsScreen.
 */
@Composable
fun OtherSportsScreen(
  otherStreams: List<LiveStream>,
  isAdminMode: Boolean = false,
  onStreamClick: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit = {},
  onOpenAdminLogin: () -> Unit = {},
  onLockAdmin: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  MoreScreen(
    otherStreams = otherStreams,
    isAdminMode = isAdminMode,
    onStreamClick = onStreamClick,
    onOpenAddStreamDialog = onOpenAddStreamDialog,
    modifier = modifier
  )
}
