package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen
import com.example.viewmodel.TerminalLine

@Composable
fun CyberTerminalView(
  lines: List<TerminalLine>,
  onExecuteCommand: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var commandText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom when new terminal lines are emitted
  LaunchedEffect(lines.size) {
    if (lines.isNotEmpty()) {
      listState.animateScrollToItem(lines.size - 1)
    }
  }

  // Blinking cursor transition
  val transition = rememberInfiniteTransition(label = "cursor_blink")
  val cursorAlpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(500),
      repeatMode = RepeatMode.Reverse
    ),
    label = "cursor_alpha"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBlack.copy(alpha = 0.90f))
      .padding(8.dp)
  ) {
    // Terminal window top title bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(topStart = 6.dp, topEnd = 6.dp))
        .background(CyberDarkSurface)
        .border(1.dp, MatrixGreenDark, CutCornerShape(topStart = 6.dp, topEnd = 6.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CutCornerShape(2.dp))
            .background(CyberRed)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CutCornerShape(2.dp))
            .background(CyberGold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CutCornerShape(2.dp))
            .background(MatrixGreenPrimary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "bash - 80x24 - /dev/pts/0",
          color = CyberTextSecondary,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
      Text(
        text = "TTY1",
        color = MatrixNeonGreen,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold
      )
    }

    // Scrollable Console Output Area
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .background(CyberBlack)
        .border(1.dp, MatrixGreenDark)
        .padding(8.dp)
    ) {
      items(lines, key = { it.id }) { line ->
        val color = when (line.type) {
          TerminalLine.LineType.INPUT -> MatrixNeonGreen
          TerminalLine.LineType.SUCCESS -> MatrixGreenPrimary
          TerminalLine.LineType.WARNING -> CyberGold
          TerminalLine.LineType.ERROR -> CyberRed
          TerminalLine.LineType.SYSTEM -> CyberCyan
          TerminalLine.LineType.NORMAL -> CyberTextPrimary
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
        ) {
          Text(
            text = "[${line.timestamp}] ",
            color = CyberTextMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          )
          Text(
            text = line.text,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Quick Command Pills
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      val quickCmds = listOf("help", "apps", "sysinfo", "scan", "ping", "matrix speed fast", "matrix color cyan", "whoami", "clear")
      for (cmd in quickCmds) {
        QuickCommandChip(
          command = cmd,
          onClick = {
            onExecuteCommand(cmd)
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bottom Interactive Terminal Input Line
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(CutCornerShape(4.dp))
        .background(CyberDarkSurface)
        .border(1.dp, MatrixGreenPrimary, CutCornerShape(4.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "root@matrix:~$ ",
        color = MatrixNeonGreen,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
      )

      BasicTextField(
        value = commandText,
        onValueChange = { commandText = it },
        textStyle = TextStyle(
          color = CyberTextPrimary,
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp
        ),
        cursorBrush = SolidColor(Color.Transparent), // custom blinking cursor below
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
          onSend = {
            if (commandText.isNotBlank()) {
              onExecuteCommand(commandText)
              commandText = ""
            }
          }
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("terminal_input_field"),
        decorationBox = { innerTextField ->
          Row(verticalAlignment = Alignment.CenterVertically) {
            innerTextField()
            // Blinking cursor block
            Box(
              modifier = Modifier
                .size(width = 7.dp, height = 13.dp)
                .background(MatrixNeonGreen.copy(alpha = cursorAlpha))
            )
          }
        }
      )

      IconButton(
        onClick = {
          if (commandText.isNotBlank()) {
            onExecuteCommand(commandText)
            commandText = ""
          }
        },
        modifier = Modifier
          .size(32.dp)
          .testTag("terminal_send_btn")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Execute Command",
          tint = MatrixGreenPrimary,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
private fun QuickCommandChip(
  command: String,
  onClick: () -> Unit,
) {
  val shape = CutCornerShape(3.dp)
  Box(
    modifier = Modifier
      .clip(shape)
      .background(CyberBlack)
      .border(1.dp, MatrixGreenDark, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(
      text = "> $command",
      color = MatrixGreenPrimary,
      fontFamily = FontFamily.Monospace,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
