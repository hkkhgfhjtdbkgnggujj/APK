package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.matrix.MatrixRainBackground
import com.example.ui.components.CyberAppCard
import com.example.ui.components.CyberBottomNav
import com.example.ui.components.CyberHudHeader
import com.example.ui.components.CyberMatrixConfigDialog
import com.example.ui.components.CyberRadarToolsView
import com.example.ui.components.CyberTerminalView
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.MatrixGreenDark
import com.example.ui.theme.MatrixGreenPrimary
import com.example.ui.theme.MatrixNeonGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.LauncherTab
import com.example.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CyberMatrixLauncherApp()
      }
    }
  }
}

@Composable
fun CyberMatrixLauncherApp(
  viewModel: LauncherViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = CyberBlack,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { _ ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(CyberBlack)
    ) {
      // Background Layer: Animated Matrix Rain Effect
      MatrixRainBackground(
        settings = uiState.matrixSettings,
        modifier = Modifier.fillMaxSize()
      )

      // Main UI Layer
      Column(
        modifier = Modifier
          .fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
      ) {
        // Top HUD Header with Telemetry & Search Filter
        CyberHudHeader(
          telemetry = uiState.telemetry,
          searchQuery = uiState.searchQuery,
          onSearchChange = { viewModel.setSearchQuery(it) },
          filterMode = uiState.filterMode,
          onFilterChange = { viewModel.setFilterMode(it) },
          onOpenSettings = { viewModel.openSettingsDialog(true) },
          onRefreshApps = { viewModel.loadInstalledApps() },
          modifier = Modifier.fillMaxWidth()
        )

        // Process Execution Overlay Banner
        AnimatedVisibility(
          visible = uiState.executingAppMessage != null,
          enter = slideInVertically() + fadeIn(),
          exit = slideOutVertically() + fadeOut()
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(MatrixGreenDark)
              .border(1.dp, MatrixNeonGreen)
              .padding(vertical = 6.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              CircularProgressIndicator(
                color = MatrixNeonGreen,
                strokeWidth = 2.dp,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = uiState.executingAppMessage ?: "",
                color = MatrixNeonGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }

        // Active Tab Screen Content
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          when (uiState.currentTab) {
            LauncherTab.APPS -> {
              AppsGridView(
                apps = uiState.filteredApps,
                onLaunchApp = { app ->
                  viewModel.launchApp(app) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.fillMaxSize()
              )
            }

            LauncherTab.TERMINAL -> {
              CyberTerminalView(
                lines = uiState.terminalLines,
                onExecuteCommand = { cmd -> viewModel.executeTerminalCommand(cmd) },
                modifier = Modifier.fillMaxSize()
              )
            }

            LauncherTab.RADAR -> {
              CyberRadarToolsView(
                isScanning = uiState.isScanningNetwork,
                scannedPorts = uiState.scannedPorts,
                onStartPortScan = { viewModel.startPortScan() },
                cipherInput = uiState.cipherInput,
                cipherOutput = uiState.cipherOutput,
                onCipherInputChange = { viewModel.setCipherInput(it) },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }

        // Bottom Navigation Bar
        CyberBottomNav(
          currentTab = uiState.currentTab,
          onTabSelected = { viewModel.onTabSelected(it) },
          modifier = Modifier.fillMaxWidth()
        )
      }

      // Matrix Rain Configuration Modal Dialog
      if (uiState.isSettingsDialogOpen) {
        CyberMatrixConfigDialog(
          settings = uiState.matrixSettings,
          onUpdateSettings = { viewModel.updateMatrixSettings(it) },
          onDismiss = { viewModel.openSettingsDialog(false) }
        )
      }
    }
  }
}

@Composable
private fun AppsGridView(
  apps: List<com.example.model.AppItem>,
  onLaunchApp: (com.example.model.AppItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (apps.isEmpty()) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(CyberBlack.copy(alpha = 0.85f))
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .clip(CutCornerShape(8.dp))
          .background(CyberDarkSurface)
          .border(1.dp, CyberCardBorder, CutCornerShape(8.dp))
          .padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = null,
          tint = MatrixGreenPrimary,
          modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "NO BINARY FOUND",
          color = MatrixNeonGreen,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "No packages match the current grep filter.",
          color = CyberTextMuted,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          textAlign = TextAlign.Center
        )
      }
    }
  } else {
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 90.dp),
      contentPadding = PaddingValues(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = modifier
        .fillMaxSize()
        .testTag("apps_grid")
    ) {
      items(apps, key = { it.id }) { app ->
        CyberAppCard(
          app = app,
          onLaunch = { onLaunchApp(app) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(
    text = "Hello $name!",
    modifier = modifier,
    color = MatrixGreenPrimary,
    fontFamily = FontFamily.Monospace
  )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme {
    Greeting("Hacker")
  }
}

