package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen
import com.example.viewmodel.AppFilterMode
import com.example.viewmodel.SystemTelemetry
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CyberHudHeader(
  telemetry: SystemTelemetry,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  filterMode: AppFilterMode,
  onFilterChange: (AppFilterMode) -> Unit,
  onOpenSettings: () -> Unit,
  onRefreshApps: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var currentTime by remember {
    mutableStateOf(SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()))
  }

  // Live second clock ticker
  LaunchedEffect(Unit) {
    while (true) {
      currentTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
      delay(1000)
    }
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(CyberBlack.copy(alpha = 0.85f))
      .border(1.dp, CyberCardBorder)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Top Status Bar: Host, Kernel, Time, Controls
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CutCornerShape(2.dp))
              .background(MatrixNeonGreen)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "ROOT@MATRIX // 0x7E3",
            color = MatrixNeonGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
          )
        }
        Text(
          text = "NET: ${telemetry.networkType} | IP: ${telemetry.ipAddress}",
          color = CyberTextMuted,
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Clock
        Text(
          text = currentTime,
          color = CyberTextSecondary,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          modifier = Modifier.padding(end = 4.dp)
        )

        // Matrix Settings Button
        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier
            .size(36.dp)
            .testTag("matrix_settings_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Matrix Settings",
            tint = MatrixGreenPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        // Refresh Apps Button
        IconButton(
          onClick = onRefreshApps,
          modifier = Modifier
            .size(36.dp)
            .testTag("refresh_apps_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Sync Apps",
            tint = MatrixGreenPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Hardware Telemetry Gauges (Battery & RAM & Storage)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(4.dp))
        .background(CyberDarkSurface.copy(alpha = 0.9f))
        .border(1.dp, MatrixGreenDark, CutCornerShape(4.dp))
        .padding(horizontal = 8.dp, vertical = 5.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      TelemetryMetricItem(
        label = "BAT",
        value = "${telemetry.batteryLevel}%",
        isWarning = telemetry.batteryLevel < 20
      )
      TelemetryMetricItem(
        label = "RAM",
        value = "${telemetry.memoryUsedPercent}%",
        isWarning = telemetry.memoryUsedPercent > 85
      )
      TelemetryMetricItem(
        label = "FREE",
        value = "${telemetry.storageFreeGb}G",
        isWarning = false
      )
      TelemetryMetricItem(
        label = "UPTIME",
        value = telemetry.uptimeFormatted,
        isWarning = false
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Search Terminal Input Bar: grep <query>
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(4.dp))
        .background(CyberBlack)
        .border(1.dp, MatrixGreenPrimary.copy(alpha = 0.8f), CutCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 7.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "> grep: ",
        color = MatrixGreenPrimary,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )

      BasicTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        textStyle = TextStyle(
          color = CyberTextPrimary,
          fontFamily = FontFamily.Monospace,
          fontSize = 13.sp
        ),
        cursorBrush = SolidColor(MatrixNeonGreen),
        singleLine = true,
        modifier = Modifier
          .weight(1f)
          .testTag("app_search_input"),
        decorationBox = { innerTextField ->
          if (searchQuery.isEmpty()) {
            Text(
              text = "filter binary or package...",
              color = CyberTextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp
            )
          }
          innerTextField()
        }
      )

      if (searchQuery.isNotEmpty()) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Clear search",
          tint = MatrixGreenPrimary,
          modifier = Modifier
            .size(16.dp)
            .clickable { onSearchChange("") }
        )
      } else {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = null,
          tint = MatrixGreenDark,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Category Filter Chips [ALL] [USER] [SYSTEM]
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        label = "ALL [0]",
        isSelected = filterMode == AppFilterMode.ALL,
        onClick = { onFilterChange(AppFilterMode.ALL) }
      )
      FilterChip(
        label = "USR [1]",
        isSelected = filterMode == AppFilterMode.USER,
        onClick = { onFilterChange(AppFilterMode.USER) }
      )
      FilterChip(
        label = "SYS [2]",
        isSelected = filterMode == AppFilterMode.SYSTEM,
        onClick = { onFilterChange(AppFilterMode.SYSTEM) }
      )
    }
  }
}

@Composable
private fun TelemetryMetricItem(
  label: String,
  value: String,
  isWarning: Boolean,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = "$label: ",
      color = CyberTextMuted,
      fontFamily = FontFamily.Monospace,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = value,
      color = if (isWarning) MatrixNeonGreen else MatrixGreenPrimary,
      fontFamily = FontFamily.Monospace,
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
private fun FilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val shape = CutCornerShape(3.dp)
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .clip(shape)
      .background(if (isSelected) MatrixGreenPrimary.copy(alpha = 0.2f) else CyberDarkSurface)
      .border(
        width = 1.dp,
        color = if (isSelected) MatrixNeonGreen else CyberCardBorder,
        shape = shape
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 4.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) MatrixNeonGreen else CyberTextSecondary,
      fontFamily = FontFamily.Monospace,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
      fontSize = 10.sp
    )
  }
}
