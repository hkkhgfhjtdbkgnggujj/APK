package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.matrix.MatrixCharMode
import com.example.matrix.MatrixColorMode
import com.example.matrix.MatrixRainSettings
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen

@Composable
fun CyberMatrixConfigDialog(
  settings: MatrixRainSettings,
  onUpdateSettings: ((MatrixRainSettings) -> MatrixRainSettings) -> Unit,
  onDismiss: () -> Unit,
) {
  val dialogShape = CutCornerShape(10.dp)

  Dialog(onDismissRequest = onDismiss) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(dialogShape)
        .background(CyberBlack)
        .border(1.5.dp, MatrixGreenPrimary, dialogShape)
        .padding(18.dp)
        .testTag("matrix_config_dialog")
    ) {
      Column {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = ">> MATRIX RAIN CONFIG",
            color = MatrixNeonGreen,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          IconButton(onClick = onDismiss) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = MatrixGreenPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Color Mode Selection
        Text(
          text = "THEME SPECTRUM:",
          color = CyberTextMuted,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          MatrixColorMode.entries.forEach { mode ->
            ConfigOptionRow(
              title = mode.title,
              isSelected = settings.colorMode == mode,
              indicatorColor = mode.primary,
              onClick = {
                onUpdateSettings { it.copy(colorMode = mode) }
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Rain Fall Speed Slider
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "FALL SPEED:",
            color = CyberTextMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = String.format("%.1fx", settings.speedMultiplier),
            color = MatrixNeonGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Slider(
          value = settings.speedMultiplier,
          onValueChange = { speed ->
            onUpdateSettings { it.copy(speedMultiplier = speed) }
          },
          valueRange = 0.4f..2.5f,
          colors = SliderDefaults.colors(
            thumbColor = MatrixNeonGreen,
            activeTrackColor = MatrixGreenPrimary,
            inactiveTrackColor = MatrixGreenDark
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Glyph Character Mode
        Text(
          text = "GLYPH STREAM:",
          color = CyberTextMuted,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          MatrixCharMode.entries.take(2).forEach { charMode ->
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(CutCornerShape(4.dp))
                .background(if (settings.charMode == charMode) MatrixGreenDark else CyberDarkSurface)
                .border(
                  1.dp,
                  if (settings.charMode == charMode) MatrixNeonGreen else CyberCardBorder,
                  CutCornerShape(4.dp)
                )
                .clickable {
                  onUpdateSettings { it.copy(charMode = charMode) }
                }
                .padding(vertical = 8.dp, horizontal = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = charMode.title,
                color = if (settings.charMode == charMode) MatrixNeonGreen else CyberTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                maxLines = 1
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Touch Interaction Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "TOUCH RIPPLE FX",
              color = CyberTextPrimary,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Touch bursts in matrix rain",
              color = CyberTextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            )
          }
          Switch(
            checked = settings.touchGlowEnabled,
            onCheckedChange = { enabled ->
              onUpdateSettings { it.copy(touchGlowEnabled = enabled) }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = MatrixNeonGreen,
              checkedTrackColor = MatrixGreenDark,
              uncheckedThumbColor = CyberTextMuted,
              uncheckedTrackColor = CyberDarkSurface
            )
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apply / Dismiss Button
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = MatrixGreenPrimary,
            contentColor = CyberBlack
          ),
          shape = CutCornerShape(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "CONFIRM SETTINGS",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }
      }
    }
  }
}

@Composable
private fun ConfigOptionRow(
  title: String,
  isSelected: Boolean,
  indicatorColor: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit,
) {
  val shape = CutCornerShape(4.dp)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (isSelected) MatrixGreenDark.copy(alpha = 0.5f) else CyberDarkSurface)
      .border(
        width = 1.dp,
        color = if (isSelected) MatrixNeonGreen else CyberCardBorder,
        shape = shape
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 7.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .width(12.dp)
        .height(12.dp)
        .clip(CutCornerShape(2.dp))
        .background(indicatorColor)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = title,
      color = if (isSelected) MatrixNeonGreen else CyberTextPrimary,
      fontFamily = FontFamily.Monospace,
      fontSize = 12.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
  }
}
