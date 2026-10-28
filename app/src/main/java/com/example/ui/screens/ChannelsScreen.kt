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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveStream
import com.example.model.StreamCategory
import com.example.ui.theme.BroadcastCyan
import com.example.ui.theme.LiveRed
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ChannelsScreen(
  streams: List<LiveStream>,
  searchQuery: String,
  selectedCategory: StreamCategory,
  isAdminMode: Boolean = false,
  onSearchQueryChange: (String) -> Unit,
  onCategorySelect: (StreamCategory) -> Unit,
  onStreamClick: (LiveStream) -> Unit,
  onOpenAddStreamDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(ObsidianBackground)
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header & Search
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
          Column {
            Text(
              text = "PP TV Channels & Streams",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = TextPrimary
            )
            Text(
              text = "${streams.size} active live streams ready to watch",
              fontSize = 12.sp,
              color = TextSecondary
            )
          }

          if (isAdminMode) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = LiveRed,
              modifier = Modifier
                .clickable { onOpenAddStreamDialog() }
                .testTag("add_stream_banner_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Stream Link", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search channel name, tag, or topic...", color = TextMuted, fontSize = 13.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = BroadcastCyan)
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("channel_search_input"),
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

      // Category Filter Chips Carousel
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .background(SurfaceDark)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(StreamCategory.entries) { category ->
          val isSelected = selectedCategory == category
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) BroadcastCyan else SurfaceVariantDark,
            modifier = Modifier
              .clickable { onCategorySelect(category) }
              .testTag("category_chip_${category.name}")
          ) {
            Text(
              text = category.displayName,
              color = if (isSelected) Color.Black else TextSecondary,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
          }
        }
      }

      // Streams List
      if (streams.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Tv,
              contentDescription = "No streams",
              tint = TextMuted,
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No channels match your filter",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = TextPrimary
            )
            Text(
              text = "Try clearing search or add your custom stream link!",
              color = TextSecondary,
              fontSize = 13.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            if (isAdminMode) {
              Surface(
                shape = RoundedCornerShape(20.dp),
                color = BroadcastCyan,
                modifier = Modifier.clickable { onOpenAddStreamDialog() }
              ) {
                Text(
                  text = "+ Add New Stream Link",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("channels_list"),
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(streams, key = { it.id }) { stream ->
            ChannelCard(
              stream = stream,
              onClick = { onStreamClick(stream) }
            )
          }
        }
      }
    }

    // Floating Action Button to Add Stream Link (Only visible in Admin Mode)
    if (isAdminMode) {
      FloatingActionButton(
        onClick = onOpenAddStreamDialog,
        containerColor = LiveRed,
        contentColor = Color.White,
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(20.dp)
          .testTag("channels_fab_add_stream")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Stream")
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Stream", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun ChannelCard(
  stream: LiveStream,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = SurfaceDark,
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("channel_card_${stream.id}")
  ) {
    Column {
      // Stream Banner / Mock Video Thumbnail Frame
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(140.dp)
          .background(SurfaceHighlight),
        contentAlignment = Alignment.Center
      ) {
        // Gradient backdrop
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              androidx.compose.ui.graphics.Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
              )
            )
        )

        Icon(
          imageVector = Icons.Default.Tv,
          contentDescription = null,
          tint = Color.White.copy(alpha = 0.4f),
          modifier = Modifier.size(48.dp)
        )

        // LIVE Badge on top left
        Row(
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = LiveRed
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(Color.White)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "LIVE",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp
              )
            }
          }

          if (stream.isBreaking) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFF59E0B)
            ) {
              Text(
                text = "BREAKING",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        // Resolution & Viewers on bottom
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.7f)
          ) {
            Text(
              text = "👁️ ${stream.viewersCount / 1000}.${(stream.viewersCount % 1000) / 100}k viewers",
              color = Color.White,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.7f)
          ) {
            Text(
              text = stream.resolution,
              color = BroadcastCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      // Stream Details
      Column(modifier = Modifier.padding(14.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stream.title,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary,
              maxLines = 1
            )
            Text(
              text = "${stream.broadcasterName} • ${stream.category.displayName}",
              fontSize = 12.sp,
              color = BroadcastCyan,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          // Watch Now Button
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = BroadcastCyan,
            modifier = Modifier.padding(start = 8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Watch",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text("Watch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Text(
          text = stream.description,
          fontSize = 12.sp,
          color = TextSecondary,
          maxLines = 2,
          modifier = Modifier.padding(top = 6.dp)
        )
      }
    }
  }
}
