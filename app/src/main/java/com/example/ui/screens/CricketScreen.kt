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
import com.example.model.MatchStatus
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CricketScreen(
  cricketStreams: List<LiveStream>,
  isAdminMode: Boolean = false,
  onStreamClick: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var filterStatus by remember { mutableStateOf("ALL") }

  val filteredMatches = cricketStreams.filter { match ->
    val matchesQuery = searchQuery.isBlank() ||
        match.title.contains(searchQuery, ignoreCase = true) ||
        match.teamA.contains(searchQuery, ignoreCase = true) ||
        match.teamB.contains(searchQuery, ignoreCase = true) ||
        match.league.contains(searchQuery, ignoreCase = true)

    val matchesStatus = when (filterStatus) {
      "LIVE" -> match.matchStatus == MatchStatus.LIVE
      "UPCOMING" -> match.matchStatus == MatchStatus.UPCOMING
      else -> true
    }
    matchesQuery && matchesStatus
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
      .testTag("cricket_screen")
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
              color = Color(0xFFF59E0B).copy(alpha = 0.2f),
              modifier = Modifier.size(36.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text("🏏", fontSize = 18.sp)
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Cricket Live & Tournaments",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
              )
              Text(
                text = "${cricketStreams.size} Cricket matches available (Live & Upcoming)",
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
                Text("Add Cricket", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search country, series, or tournament...", color = TextMuted, fontSize = 12.sp) },
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

      // Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SurfaceDark)
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("ALL" to "All Matches", "LIVE" to "🔴 Live Now", "UPCOMING" to "⏰ Upcoming").forEach { (key, label) ->
          val isSelected = filterStatus == key
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
            modifier = Modifier.clickable { filterStatus = key }
          ) {
            Text(
              text = label,
              color = if (isSelected) Color.Black else TextSecondary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
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
            Text("🏏", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("No cricket matches found", fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Check back soon or search another cricket series", color = TextSecondary, fontSize = 12.sp)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val uniqueMatches = filteredMatches.distinctBy { it.id }
          items(uniqueMatches, key = { "cricket_${it.id}" }) { match ->
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
          Icon(Icons.Default.Add, contentDescription = "Add Cricket Match")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Cricket", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
