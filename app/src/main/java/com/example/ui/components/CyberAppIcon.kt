package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppItem
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen

// Matrix Green Color Matrix: transforms any icon into authentic glowing hacker green
private val MatrixGreenColorMatrix = ColorMatrix(
  floatArrayOf(
    0.05f, 0.10f, 0.05f, 0f, 0f,     // R: crushed to near zero
    0.30f, 0.59f, 0.11f, 0f, 30f,    // G: converts full luminance to bright green!
    0.02f, 0.05f, 0.02f, 0f, 0f,     // B: near zero
    0.00f, 0.00f, 0.00f, 1f, 0f      // A: preserve alpha
  )
)

private fun drawableToBitmap(drawable: Drawable): Bitmap {
  val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
  val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
  val bitmap = Bitmap.createBitmap(width.coerceIn(48, 144), height.coerceIn(48, 144), Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)
  drawable.setBounds(0, 0, canvas.width, canvas.height)
  drawable.draw(canvas)
  return bitmap
}

@Composable
fun CyberAppCard(
  app: AppItem,
  onLaunch: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.93f else 1.0f,
    animationSpec = spring(),
    label = "app_press_scale"
  )

  val borderColor by animateColorAsState(
    targetValue = if (isPressed) MatrixNeonGreen else CyberCardBorder,
    label = "card_border"
  )

  val cardShape = remember { CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp) }

  val imageBitmap = remember(app.icon) {
    app.icon?.let { drawableToBitmap(it).asImageBitmap() }
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .testTag("app_item_${app.packageName}")
      .scale(scale)
      .clip(cardShape)
      .background(CyberDarkSurface.copy(alpha = 0.88f))
      .border(1.dp, borderColor, cardShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onLaunch
      )
      .padding(horizontal = 8.dp, vertical = 10.dp)
      .semantics {
        role = Role.Button
        contentDescription = "تشغيل تطبيق ${app.label}"
      }
  ) {
    // Top indicator header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (app.isSystemApp) "SYS" else "USR",
        color = if (app.isSystemApp) MatrixGreenDark else CyberTextMuted,
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "[•]",
        color = MatrixGreenPrimary,
        fontSize = 8.sp,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Glowing Green App Icon Frame
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(54.dp)
        .aspectRatio(1f)
        .clip(CutCornerShape(6.dp))
        .background(CyberBlack.copy(alpha = 0.95f))
        .border(1.5.dp, if (isPressed) MatrixNeonGreen else MatrixGreenPrimary.copy(alpha = 0.6f), CutCornerShape(6.dp))
        .padding(6.dp)
    ) {
      if (imageBitmap != null) {
        androidx.compose.foundation.Image(
          bitmap = imageBitmap,
          contentDescription = null,
          colorFilter = ColorFilter.colorMatrix(MatrixGreenColorMatrix),
          modifier = Modifier.fillMaxSize()
        )
      } else {
        Icon(
          imageVector = Icons.Default.Terminal,
          contentDescription = null,
          tint = MatrixGreenPrimary,
          modifier = Modifier.size(32.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // App Label
    Text(
      text = app.label,
      color = CyberTextPrimary,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.SemiBold,
      fontSize = 12.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )

    // Cyber Alias
    Text(
      text = "> ${app.cyberAlias}",
      color = MatrixGreenPrimary.copy(alpha = 0.75f),
      fontFamily = FontFamily.Monospace,
      fontSize = 9.sp,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth()
    )
  }
}
