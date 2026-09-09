package com.example.matrix

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.random.Random

enum class MatrixColorMode(val title: String, val primary: Color, val head: Color, val dark: Color) {
  CLASSIC_GREEN(
    title = "Matrix Green",
    primary = Color(0xFF00FF41),
    head = Color(0xFFE5FFE5),
    dark = Color(0xFF004411)
  ),
  CYBER_CYAN(
    title = "Cyber Cyan",
    primary = Color(0xFF00E5FF),
    head = Color(0xFFE0FFFF),
    dark = Color(0xFF003844)
  ),
  BLOOD_RED(
    title = "Breach Red",
    primary = Color(0xFFFF2A2A),
    head = Color(0xFFFFE0E0),
    dark = Color(0xFF4A0000)
  ),
  AMBER_GOLD(
    title = "Mainframe Amber",
    primary = Color(0xFFFFB300),
    head = Color(0xFFFFF6E0),
    dark = Color(0xFF472F00)
  )
}

enum class MatrixCharMode(val title: String, val chars: String) {
  FULL_MATRIX(
    title = "Matrix (Katakana & Code)",
    chars = "0123456789ABCDEFｦｱｳｴｵｶｷｹｺｻｼｽｾｿﾀﾂﾃﾅﾆﾇﾈﾊﾋﾎﾏﾐﾑﾒﾓﾔﾕﾗﾘﾜXYZ><#$%&*=+-/?"
  ),
  BINARY_ONLY(
    title = "Binary (01)",
    chars = "0101010101010101"
  ),
  HEX_CODE(
    title = "Hexadecimal",
    chars = "0123456789ABCDEF"
  ),
  HACKER_SYMBOLS(
    title = "Cyber Ops",
    chars = "$>#_~{}[]/!%*+=?&^|:;@0123456789"
  )
}

data class MatrixRainSettings(
  val speedMultiplier: Float = 1.0f,
  val densityStep: Int = 36, // column width in px
  val colorMode: MatrixColorMode = MatrixColorMode.CLASSIC_GREEN,
  val charMode: MatrixCharMode = MatrixCharMode.FULL_MATRIX,
  val touchGlowEnabled: Boolean = true,
  val isPaused: Boolean = false,
)

class MatrixColumn(
  val x: Float,
  val fontSize: Float,
  var y: Float,
  var speed: Float,
  var trailLength: Int,
  val chars: CharArray,
  var nextCharSpawnY: Float = y,
)

@Composable
fun MatrixRainBackground(
  modifier: Modifier = Modifier,
  settings: MatrixRainSettings = MatrixRainSettings(),
) {
  var frameTime by remember { mutableLongStateOf(0L) }
  val columns = remember { mutableStateListOf<MatrixColumn>() }
  var viewWidth by remember { mutableStateOf(0f) }
  var viewHeight by remember { mutableStateOf(0f) }

  // Touch bursts for interactive hacking feel
  val touchBursts = remember { mutableStateListOf<Triple<Float, Float, Long>>() }

  // Animation frame loop
  LaunchedEffect(settings.isPaused) {
    if (settings.isPaused) return@LaunchedEffect
    while (true) {
      withInfiniteAnimationFrameMillis { time ->
        frameTime = time
      }
    }
  }

  // Pre-configured native paints for maximum 60fps performance
  val headPaint = remember(settings.colorMode) {
    Paint().apply {
      isAntiAlias = true
      typeface = Typeface.MONOSPACE
      color = settings.colorMode.head.toArgb()
      style = Paint.Style.FILL
      setShadowLayer(14f, 0f, 0f, settings.colorMode.primary.toArgb())
    }
  }

  val trailPaint = remember(settings.colorMode) {
    Paint().apply {
      isAntiAlias = true
      typeface = Typeface.MONOSPACE
      color = settings.colorMode.primary.toArgb()
      style = Paint.Style.FILL
      setShadowLayer(6f, 0f, 0f, settings.colorMode.primary.toArgb())
    }
  }

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(settings.touchGlowEnabled) {
        if (settings.touchGlowEnabled) {
          detectTapGestures { offset ->
            if (touchBursts.size > 8) touchBursts.removeAt(0)
            touchBursts.add(Triple(offset.x, offset.y, System.currentTimeMillis()))
          }
        }
      }
      .pointerInput(settings.touchGlowEnabled) {
        if (settings.touchGlowEnabled) {
          detectDragGestures { change, _ ->
            change.consume()
            if (touchBursts.size > 8) touchBursts.removeAt(0)
            touchBursts.add(Triple(change.position.x, change.position.y, System.currentTimeMillis()))
          }
        }
      }
  ) {
    // Read frameTime to trigger recomposition per frame
    val currentFrame = frameTime
    val width = size.width
    val height = size.height

    if (width <= 0 || height <= 0) return@Canvas

    val charset = settings.charMode.chars
    val colSpacing = settings.densityStep.toFloat()

    // Initialize or adapt columns to layout dimensions
    if (viewWidth != width || viewHeight != height || columns.isEmpty()) {
      viewWidth = width
      viewHeight = height
      columns.clear()

      val numColumns = (width / colSpacing).toInt() + 1
      for (i in 0 until numColumns) {
        val colX = i * colSpacing
        val fontSize = (colSpacing * 0.85f).coerceIn(24f, 44f)
        val trailLen = Random.nextInt(12, 30)
        val initialY = Random.nextFloat() * height * -1.5f
        val colSpeed = Random.nextFloat() * 12f + 8f

        val charArray = CharArray(trailLen) {
          charset[Random.nextInt(charset.length)]
        }

        columns.add(
          MatrixColumn(
            x = colX,
            fontSize = fontSize,
            y = initialY,
            speed = colSpeed,
            trailLength = trailLen,
            chars = charArray,
          )
        )
      }
    }

    // Draw background matrix canvas
    drawIntoCanvas { canvas ->
      val native = canvas.nativeCanvas
      val now = System.currentTimeMillis()

      // Cleanup old touch bursts (older than 1200ms)
      touchBursts.removeAll { now - it.third > 1200L }

      // Update and render each rain column
      val speedFactor = settings.speedMultiplier
      for (col in columns) {
        headPaint.textSize = col.fontSize
        trailPaint.textSize = col.fontSize

        // Advance column head position if not paused
        if (!settings.isPaused) {
          col.y += col.speed * speedFactor
        }

        // When column completely leaves bottom, reset to random height above top
        val totalLengthPx = col.trailLength * col.fontSize
        if (col.y - totalLengthPx > height) {
          col.y = -Random.nextFloat() * (height * 0.6f) - 50f
          col.speed = Random.nextFloat() * 12f + 8f
          col.trailLength = Random.nextInt(12, 28)
          // Shuffle glyphs
          for (j in col.chars.indices) {
            col.chars[j] = charset[Random.nextInt(charset.length)]
          }
        }

        // Random glitch: occasionally mutate a character in the stream
        if (Random.nextFloat() < 0.08f) {
          val glitchIdx = Random.nextInt(col.chars.size)
          col.chars[glitchIdx] = charset[Random.nextInt(charset.length)]
        }

        // Render glyphs along the column trail
        for (i in 0 until col.trailLength) {
          val charY = col.y - (i * col.fontSize)

          // Only draw if visible on screen
          if (charY >= -col.fontSize && charY <= height + col.fontSize) {
            val glyph = if (i < col.chars.size) col.chars[i].toString() else "0"

            if (i == 0) {
              // Lead / Head Character: Glowing Bright / White-Hot
              native.drawText(glyph, col.x, charY, headPaint)
            } else {
              // Tail Characters: Alpha gradient from bright green to deep dark green
              val fade = (1.0f - (i.toFloat() / col.trailLength.toFloat())).coerceIn(0.08f, 0.95f)
              val alphaInt = (fade * 255).toInt()

              trailPaint.alpha = alphaInt
              native.drawText(glyph, col.x, charY, trailPaint)
            }
          }
        }
      }

      // Draw interactive touch bursts if any
      for (burst in touchBursts) {
        val age = (now - burst.third).toFloat() / 1200f
        if (age in 0f..1f) {
          val burstRadius = age * 160f
          val burstAlpha = ((1f - age) * 200).toInt()
          headPaint.alpha = burstAlpha

          val randomBurstChar = charset[Random.nextInt(charset.length)].toString()
          native.drawText(
            randomBurstChar,
            burst.first + (Random.nextFloat() * 20 - 10),
            burst.second + (Random.nextFloat() * 20 - 10),
            headPaint
          )
        }
      }
    }
  }
}
