package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CyberRadarToolsView(
  isScanning: Boolean,
  scannedPorts: List<Pair<Int, String>>,
  onStartPortScan: () -> Unit,
  cipherInput: String,
  cipherOutput: String,
  onCipherInputChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()

  // Rotating Radar Sweep Animation
  val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
  val angle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar_angle"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBlack.copy(alpha = 0.90f))
      .verticalScroll(scrollState)
      .padding(14.dp)
  ) {
    // Section 1: Cyber Radar Sweep Display
    Text(
      text = ">> NETWORK SPECTRUM RADAR",
      color = MatrixNeonGreen,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(8.dp))
        .background(CyberDarkSurface)
        .border(1.dp, CyberCardBorder, CutCornerShape(8.dp))
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Canvas(
        modifier = Modifier
          .size(200.dp)
          .aspectRatio(1f)
      ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.width / 2

        // Concentric Rings
        drawCircle(color = MatrixGreenDark, radius = radius, style = Stroke(width = 1.5f))
        drawCircle(color = MatrixGreenDark, radius = radius * 0.75f, style = Stroke(width = 1f))
        drawCircle(color = MatrixGreenDark, radius = radius * 0.5f, style = Stroke(width = 1f))
        drawCircle(color = MatrixGreenDark, radius = radius * 0.25f, style = Stroke(width = 1f))

        // Crosshairs
        drawLine(color = MatrixGreenDark, start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1f)
        drawLine(color = MatrixGreenDark, start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1f)

        // Rotating Sweep Line
        val rad = Math.toRadians(angle.toDouble())
        val endX = center.x + radius * cos(rad).toFloat()
        val endY = center.y + radius * sin(rad).toFloat()

        drawLine(
          brush = Brush.radialGradient(
            colors = listOf(MatrixNeonGreen, MatrixGreenPrimary, Color.Transparent),
            center = center,
            radius = radius
          ),
          start = center,
          end = Offset(endX, endY),
          strokeWidth = 3f
        )

        // Radar Blips (Simulated Nodes)
        val blips = listOf(
          Offset(center.x + radius * 0.45f, center.y - radius * 0.3f),
          Offset(center.x - radius * 0.6f, center.y - radius * 0.5f),
          Offset(center.x - radius * 0.35f, center.y + radius * 0.4f),
          Offset(center.x + radius * 0.55f, center.y + radius * 0.25f),
        )

        for (blip in blips) {
          drawCircle(color = MatrixNeonGreen, radius = 4f, center = blip)
          drawCircle(color = MatrixGreenPrimary.copy(alpha = 0.4f), radius = 10f, center = blip)
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section 2: Port Scanner
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = ">> PORT & VULN SCANNER",
        color = MatrixNeonGreen,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )

      Button(
        onClick = onStartPortScan,
        enabled = !isScanning,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isScanning) CyberCardBorder else MatrixGreenPrimary,
          contentColor = CyberBlack
        ),
        shape = CutCornerShape(4.dp),
        modifier = Modifier.testTag("start_port_scan_btn")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (isScanning) Icons.Default.Radar else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isScanning) "SCANNING..." else "START SCAN",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Scanned Ports Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(6.dp))
        .background(CyberDarkSurface)
        .border(1.dp, CyberCardBorder, CutCornerShape(6.dp))
        .padding(12.dp)
    ) {
      Column {
        if (scannedPorts.isEmpty() && !isScanning) {
          Text(
            text = "No active scan. Press 'START SCAN' to probe local ports.",
            color = CyberTextMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          )
        } else {
          scannedPorts.forEach { (port, desc) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "PORT $port [OPEN]",
                color = MatrixGreenPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
              )
              Text(
                text = desc,
                color = CyberTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Section 3: Matrix Hex Encryptor / Decryptor
    Text(
      text = ">> QUANTUM HEX CIPHER",
      color = MatrixNeonGreen,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
      value = cipherInput,
      onValueChange = onCipherInputChange,
      label = {
        Text("Plaintext / Payload Input", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MatrixNeonGreen,
        unfocusedBorderColor = MatrixGreenDark,
        focusedTextColor = CyberTextPrimary,
        unfocusedTextColor = CyberTextSecondary,
        focusedLabelColor = MatrixNeonGreen,
        unfocusedLabelColor = CyberTextMuted,
        cursorColor = MatrixNeonGreen,
      ),
      shape = CutCornerShape(4.dp),
      singleLine = true,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("cipher_input_field")
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(4.dp))
        .background(CyberDarkSurface)
        .border(1.dp, MatrixGreenDark, CutCornerShape(4.dp))
        .padding(10.dp)
    ) {
      Column {
        Text(
          text = "HEXADECIMAL ENCODING:",
          color = CyberGold,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = cipherOutput.ifEmpty { "00 00 00 00" },
          color = MatrixGreenPrimary,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          lineHeight = 16.sp
        )
      }
    }
  }
}
