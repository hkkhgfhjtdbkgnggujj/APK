package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen
import com.example.viewmodel.LauncherTab

@Composable
fun CyberBottomNav(
  currentTab: LauncherTab,
  onTabSelected: (LauncherTab) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(CyberBlack.copy(alpha = 0.95f))
      .border(1.dp, CyberCardBorder)
      .navigationBarsPadding()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically
  ) {
    CyberNavItem(
      icon = Icons.Default.Apps,
      labelEn = "APPS",
      labelAr = "التطبيقات",
      isSelected = currentTab == LauncherTab.APPS,
      onClick = { onTabSelected(LauncherTab.APPS) },
      testTag = "tab_apps"
    )

    CyberNavItem(
      icon = Icons.Default.Terminal,
      labelEn = "TERMINAL",
      labelAr = "الموجه",
      isSelected = currentTab == LauncherTab.TERMINAL,
      onClick = { onTabSelected(LauncherTab.TERMINAL) },
      testTag = "tab_terminal"
    )

    CyberNavItem(
      icon = Icons.Default.Radar,
      labelEn = "RADAR",
      labelAr = "الرادار",
      isSelected = currentTab == LauncherTab.RADAR,
      onClick = { onTabSelected(LauncherTab.RADAR) },
      testTag = "tab_radar"
    )
  }
}

@Composable
private fun CyberNavItem(
  icon: ImageVector,
  labelEn: String,
  labelAr: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val shape = CutCornerShape(4.dp)
  val borderColor by animateColorAsState(
    targetValue = if (isSelected) MatrixNeonGreen else CyberCardBorder.copy(alpha = 0.4f),
    label = "nav_border"
  )

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .testTag(testTag)
      .clip(shape)
      .background(if (isSelected) MatrixGreenDark.copy(alpha = 0.5f) else CyberDarkSurface.copy(alpha = 0.6f))
      .border(1.dp, borderColor, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 18.dp, vertical = 8.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = icon,
        contentDescription = "$labelEn / $labelAr",
        tint = if (isSelected) MatrixNeonGreen else CyberTextMuted,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = labelEn,
        color = if (isSelected) MatrixGreenPrimary else CyberTextMuted,
        fontFamily = FontFamily.Monospace,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 10.sp
      )
    }
  }
}
