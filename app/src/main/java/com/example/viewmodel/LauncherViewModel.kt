package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.matrix.MatrixCharMode
import com.example.matrix.MatrixColorMode
import com.example.matrix.MatrixRainSettings
import com.example.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TerminalLine(
  val id: Long = System.nanoTime(),
  val text: String,
  val type: LineType = LineType.NORMAL,
  val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date()),
) {
  enum class LineType {
    INPUT,
    NORMAL,
    SUCCESS,
    WARNING,
    ERROR,
    SYSTEM,
  }
}

data class SystemTelemetry(
  val batteryLevel: Int = 88,
  val isCharging: Boolean = false,
  val memoryUsedPercent: Int = 42,
  val totalRamMb: Long = 6144,
  val availableRamMb: Long = 3560,
  val storageFreeGb: Float = 64.2f,
  val uptimeFormatted: String = "00:00:00",
  val ipAddress: String = "192.168.1.137",
  val networkType: String = "WLAN-SECURE",
  val androidVersion: String = "14.0 (API ${Build.VERSION.SDK_INT})",
)

enum class LauncherTab(val titleEn: String, val titleAr: String) {
  APPS("APPS", "التطبيقات"),
  TERMINAL("TERMINAL", "الموجه"),
  RADAR("CYBER RADAR", "الرادار"),
}

enum class AppFilterMode {
  ALL,
  USER,
  SYSTEM,
}

data class LauncherUiState(
  val apps: List<AppItem> = emptyList(),
  val filteredApps: List<AppItem> = emptyList(),
  val searchQuery: String = "",
  val filterMode: AppFilterMode = AppFilterMode.ALL,
  val currentTab: LauncherTab = LauncherTab.APPS,
  val telemetry: SystemTelemetry = SystemTelemetry(),
  val terminalLines: List<TerminalLine> = emptyList(),
  val matrixSettings: MatrixRainSettings = MatrixRainSettings(),
  val isSettingsDialogOpen: Boolean = false,
  val executingAppMessage: String? = null,
  val isScanningNetwork: Boolean = false,
  val scannedPorts: List<Pair<Int, String>> = emptyList(),
  val cipherInput: String = "CONFIDENTIAL_PAYLOAD_2026",
  val cipherOutput: String = "",
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(LauncherUiState())
  val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

  private val appContext: Context get() = getApplication<Application>().applicationContext

  init {
    loadInstalledApps()
    startTelemetryMonitor()
    initTerminalWelcome()
  }

  fun onTabSelected(tab: LauncherTab) {
    vibrateHaptic(20)
    _uiState.update { it.copy(currentTab = tab) }
  }

  fun setSearchQuery(query: String) {
    _uiState.update { state ->
      val filtered = filterAppList(state.apps, query, state.filterMode)
      state.copy(searchQuery = query, filteredApps = filtered)
    }
  }

  fun setFilterMode(mode: AppFilterMode) {
    vibrateHaptic(15)
    _uiState.update { state ->
      val filtered = filterAppList(state.apps, state.searchQuery, mode)
      state.copy(filterMode = mode, filteredApps = filtered)
    }
  }

  fun openSettingsDialog(open: Boolean) {
    _uiState.update { it.copy(isSettingsDialogOpen = open) }
  }

  fun updateMatrixSettings(transform: (MatrixRainSettings) -> MatrixRainSettings) {
    _uiState.update { state ->
      state.copy(matrixSettings = transform(state.matrixSettings))
    }
  }

  fun loadInstalledApps() {
    viewModelScope.launch(Dispatchers.IO) {
      val pm = appContext.packageManager
      val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
      }

      val resolveInfos = pm.queryIntentActivities(intent, 0)
      val loadedApps = mutableListOf<AppItem>()

      for (info in resolveInfos) {
        val packageName = info.activityInfo.packageName
        if (packageName == appContext.packageName) continue // skip self in list

        val label = info.loadLabel(pm).toString()
        val icon = info.loadIcon(pm)
        val isSystem = (info.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val launchIntent = pm.getLaunchIntentForPackage(packageName)

        val cleanAlias = label.lowercase(Locale.ROOT)
          .replace("[^a-z0-9]".toRegex(), "_")
          .trim('_')
          .let { if (it.isEmpty()) "app" else it } + ".bin"

        loadedApps.add(
          AppItem(
            id = "${packageName}_${info.activityInfo.name}",
            label = label,
            packageName = packageName,
            activityName = info.activityInfo.name,
            icon = icon,
            isSystemApp = isSystem,
            cyberAlias = cleanAlias,
            launchIntent = launchIntent,
          )
        )
      }

      // If on emulator/device with very few apps, inject built-in core cyber shortcuts
      if (loadedApps.size < 6) {
        loadedApps.addAll(getBuiltinCyberApps(pm))
      }

      loadedApps.sortBy { it.label.lowercase(Locale.ROOT) }

      _uiState.update { state ->
        val filtered = filterAppList(loadedApps, state.searchQuery, state.filterMode)
        state.copy(apps = loadedApps, filteredApps = filtered)
      }
    }
  }

  private fun getBuiltinCyberApps(pm: PackageManager): List<AppItem> {
    val list = mutableListOf<AppItem>()

    // Common system packages
    val candidatePackages = listOf(
      "com.android.settings" to "Settings",
      "com.android.chrome" to "Chrome Browser",
      "com.google.android.apps.photos" to "Gallery Photos",
      "com.google.android.calculator" to "Calculator",
      "com.google.android.deskclock" to "Clock Matrix",
      "com.android.camera" to "Cyber Optics",
      "com.google.android.dialer" to "Secure Comm",
    )

    for ((pkg, label) in candidatePackages) {
      val launchIntent = pm.getLaunchIntentForPackage(pkg)
      val icon = try {
        pm.getApplicationIcon(pkg)
      } catch (e: Exception) {
        null
      }

      list.add(
        AppItem(
          id = pkg,
          label = label,
          packageName = pkg,
          icon = icon,
          isSystemApp = true,
          cyberAlias = "${label.lowercase().replace(" ", "_")}.bin",
          launchIntent = launchIntent,
        )
      )
    }

    return list
  }

  private fun filterAppList(
    apps: List<AppItem>,
    query: String,
    mode: AppFilterMode
  ): List<AppItem> {
    return apps.filter { app ->
      val matchesQuery = query.isBlank() ||
        app.label.contains(query, ignoreCase = true) ||
        app.cyberAlias.contains(query, ignoreCase = true) ||
        app.packageName.contains(query, ignoreCase = true)

      val matchesMode = when (mode) {
        AppFilterMode.ALL -> true
        AppFilterMode.USER -> !app.isSystemApp
        AppFilterMode.SYSTEM -> app.isSystemApp
      }

      matchesQuery && matchesMode
    }
  }

  fun launchApp(app: AppItem, onLaunchFailed: ((String) -> Unit)? = null) {
    vibrateHaptic(40)
    viewModelScope.launch {
      _uiState.update { it.copy(executingAppMessage = "EXEC [${app.cyberAlias}] >> SPAWNING PID...") }
      appendTerminalLine("EXEC: ${app.label} (${app.packageName})", TerminalLine.LineType.INPUT)
      appendTerminalLine(">> Loading binary into memory space...", TerminalLine.LineType.SYSTEM)

      delay(350)
      _uiState.update { it.copy(executingAppMessage = null) }

      val intent = app.launchIntent ?: appContext.packageManager.getLaunchIntentForPackage(app.packageName)
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
          appContext.startActivity(intent)
          appendTerminalLine(">> Process started successfully. STATUS: OK", TerminalLine.LineType.SUCCESS)
        } catch (e: Exception) {
          appendTerminalLine(">> Error launching process: ${e.localizedMessage}", TerminalLine.LineType.ERROR)
          onLaunchFailed?.invoke(e.localizedMessage ?: "Launch failed")
        }
      } else {
        appendTerminalLine(">> Intent not available for ${app.packageName}", TerminalLine.LineType.WARNING)
        onLaunchFailed?.invoke("No launcher intent available for ${app.label}")
      }
    }
  }

  fun executeTerminalCommand(input: String) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return

    vibrateHaptic(20)
    appendTerminalLine("root@matrix:~$ $trimmed", TerminalLine.LineType.INPUT)

    val parts = trimmed.split("\\s+".toRegex())
    val command = parts.firstOrNull()?.lowercase(Locale.ROOT) ?: ""
    val args = parts.drop(1)

    when (command) {
      "help", "مساعدة", "?" -> {
        appendTerminalLine("=== CYBER MATRIX CONSOLE v3.8 ===", TerminalLine.LineType.SUCCESS)
        appendTerminalLine("Available commands:", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  apps           - List all installed apps & aliases", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  run <name|id>  - Launch an installed application", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  sysinfo        - Detailed hardware & telemetry specs", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  scan           - Scan open ports & local network nodes", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  matrix <param> - speed [slow|normal|fast] / color [green|cyan|red|amber]", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  ping <host>    - Test ICMP latency packet", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  decrypt <text> - Run cyber decipher animation", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  whoami         - View current session credentials", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  banner         - Print ASCII Matrix Hacker banner", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  clear          - Clear terminal logs", TerminalLine.LineType.NORMAL)
      }

      "apps", "list", "ps" -> {
        val apps = _uiState.value.apps
        appendTerminalLine("Found ${apps.size} installed binaries:", TerminalLine.LineType.SUCCESS)
        apps.take(25).forEachIndexed { idx, item ->
          appendTerminalLine("  [$idx] ${item.cyberAlias.padEnd(20)} >> ${item.label}", TerminalLine.LineType.NORMAL)
        }
        if (apps.size > 25) {
          appendTerminalLine("  ... (${apps.size - 25} more apps in APPS tab)", TerminalLine.LineType.SYSTEM)
        }
      }

      "run", "launch", "open", "exec" -> {
        if (args.isEmpty()) {
          appendTerminalLine("Usage: run <appName | alias | index>", TerminalLine.LineType.WARNING)
        } else {
          val target = args.joinToString(" ").lowercase(Locale.ROOT)
          val found = _uiState.value.apps.find {
            it.label.lowercase(Locale.ROOT).contains(target) ||
              it.cyberAlias.lowercase(Locale.ROOT).contains(target) ||
              it.packageName.lowercase(Locale.ROOT).contains(target)
          } ?: args.first().toIntOrNull()?.let { idx ->
            _uiState.value.apps.getOrNull(idx)
          }

          if (found != null) {
            launchApp(found)
          } else {
            appendTerminalLine("Process '$target' not found. Type 'apps' to see valid binaries.", TerminalLine.LineType.ERROR)
          }
        }
      }

      "sysinfo", "specs" -> {
        val t = _uiState.value.telemetry
        appendTerminalLine("--- SYSTEM HARDWARE TELEMETRY ---", TerminalLine.LineType.SUCCESS)
        appendTerminalLine("  DEVICE ARCH: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.HARDWARE})", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  ANDROID OS: ${t.androidVersion}", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  KERNEL REV: ${System.getProperty("os.version") ?: "Linux 5.15.0"}", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  BATTERY: ${t.batteryLevel}% (Status: ${if (t.isCharging) "CHARGING" else "DISCHARGING"})", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  RAM LOAD: ${t.memoryUsedPercent}% (${t.availableRamMb} MB Free / ${t.totalRamMb} MB)", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  STORAGE: ${t.storageFreeGb} GB Available on root node", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  NETWORK: ${t.networkType} (IP: ${t.ipAddress})", TerminalLine.LineType.NORMAL)
        appendTerminalLine("  UPTIME: ${t.uptimeFormatted}", TerminalLine.LineType.NORMAL)
      }

      "scan", "nmap" -> {
        startPortScan()
      }

      "matrix" -> {
        if (args.isEmpty()) {
          appendTerminalLine("Usage: matrix speed <slow|normal|fast> OR matrix color <green|cyan|red|amber>", TerminalLine.LineType.WARNING)
        } else {
          when (args[0].lowercase(Locale.ROOT)) {
            "speed" -> {
              val speed = when (args.getOrNull(1)?.lowercase(Locale.ROOT)) {
                "slow" -> 0.5f
                "fast", "hyperspeed" -> 2.2f
                else -> 1.0f
              }
              updateMatrixSettings { it.copy(speedMultiplier = speed) }
              appendTerminalLine("Matrix rain speed set to ${speed}x", TerminalLine.LineType.SUCCESS)
            }
            "color" -> {
              val mode = when (args.getOrNull(1)?.lowercase(Locale.ROOT)) {
                "cyan", "blue" -> MatrixColorMode.CYBER_CYAN
                "red" -> MatrixColorMode.BLOOD_RED
                "amber", "gold" -> MatrixColorMode.AMBER_GOLD
                else -> MatrixColorMode.CLASSIC_GREEN
              }
              updateMatrixSettings { it.copy(colorMode = mode) }
              appendTerminalLine("Matrix color shifted to ${mode.title}", TerminalLine.LineType.SUCCESS)
            }
            else -> appendTerminalLine("Unknown matrix parameter. Use 'speed' or 'color'.", TerminalLine.LineType.WARNING)
          }
        }
      }

      "ping" -> {
        val host = args.getOrNull(0) ?: "gateway.matrix.net"
        viewModelScope.launch {
          appendTerminalLine("PING $host (10.0.2.2): 56 data bytes", TerminalLine.LineType.NORMAL)
          repeat(4) { seq ->
            delay(280)
            val time = (14..32).random()
            appendTerminalLine("64 bytes from $host: icmp_seq=$seq ttl=64 time=${time}.${(1..9).random()} ms", TerminalLine.LineType.NORMAL)
          }
          appendTerminalLine("--- $host ping statistics ---", TerminalLine.LineType.SUCCESS)
          appendTerminalLine("4 packets transmitted, 4 received, 0% packet loss", TerminalLine.LineType.SUCCESS)
        }
      }

      "decrypt", "crack" -> {
        val cipher = args.joinToString(" ").ifEmpty { "0x7F454C4602010100" }
        viewModelScope.launch {
          appendTerminalLine(">> Initiating quantum decipher on [$cipher]...", TerminalLine.LineType.WARNING)
          delay(200)
          appendTerminalLine(">> Hash Match: SHA-256 [PASS]", TerminalLine.LineType.NORMAL)
          delay(300)
          appendTerminalLine(">> Decrypted Key: 'MATRIX_MAINFRAME_ACCESS_LEVEL_0'", TerminalLine.LineType.SUCCESS)
        }
      }

      "whoami" -> {
        appendTerminalLine("root // UID: 0, GID: 0 (wheel, matrix_admins)", TerminalLine.LineType.SUCCESS)
        appendTerminalLine("Host: cyber-node-01.lan // Shell: /bin/zsh", TerminalLine.LineType.NORMAL)
      }

      "banner" -> {
        initTerminalWelcome()
      }

      "clear", "cls" -> {
        _uiState.update { it.copy(terminalLines = emptyList()) }
      }

      else -> {
        appendTerminalLine("zsh: command not found: $trimmed. Type 'help' for available commands.", TerminalLine.LineType.ERROR)
      }
    }
  }

  fun startPortScan() {
    if (_uiState.value.isScanningNetwork) return
    vibrateHaptic(30)
    viewModelScope.launch {
      _uiState.update { it.copy(isScanningNetwork = true, scannedPorts = emptyList()) }
      appendTerminalLine(">> Initiating SYN stealth scan on 127.0.0.1...", TerminalLine.LineType.WARNING)

      val samplePorts = listOf(
        21 to "FTP (File Transfer)",
        22 to "SSH (Secure Shell)",
        80 to "HTTP (Matrix Gateway)",
        443 to "HTTPS (TLS Cyber Tunnel)",
        3000 to "NODE.JS (Dashboard)",
        5555 to "ADB (Android Debug)",
        8080 to "PROXY (Matrix Stream)",
        9090 to "METRICS (Telemetry Core)"
      )

      val discovered = mutableListOf<Pair<Int, String>>()
      for (port in samplePorts) {
        delay(250)
        discovered.add(port)
        _uiState.update { it.copy(scannedPorts = discovered.toList()) }
        appendTerminalLine("  [PORT ${port.first}] OPEN >> ${port.second}", TerminalLine.LineType.SUCCESS)
      }

      delay(200)
      _uiState.update { it.copy(isScanningNetwork = false) }
      appendTerminalLine(">> Scan complete: ${discovered.size} open ports discovered.", TerminalLine.LineType.SUCCESS)
    }
  }

  fun setCipherInput(input: String) {
    val hex = input.toByteArray().joinToString(" ") { "%02X".format(it) }
    _uiState.update { it.copy(cipherInput = input, cipherOutput = hex) }
  }

  private fun initTerminalWelcome() {
    val welcome = listOf(
      TerminalLine(text = "  ______  ______  ______  ______  ______  ", type = TerminalLine.LineType.SUCCESS),
      TerminalLine(text = " /\\  ___\\/\\  ___\\/\\  __ \\/\\  == \\/\\  ___\\ ", type = TerminalLine.LineType.SUCCESS),
      TerminalLine(text = " \\ \\ \\___\\ \\___  \\ \\ \\/\\ \\ \\  __<\\ \\___  \\", type = TerminalLine.LineType.SUCCESS),
      TerminalLine(text = "  \\ \\_____\\/\\_____\\ \\_____\\ \\_____\\/\\_____\\", type = TerminalLine.LineType.SUCCESS),
      TerminalLine(text = "   \\/_____/\\/_____/\\/_____/\\/_____/\\/_____/", type = TerminalLine.LineType.SUCCESS),
      TerminalLine(text = ">> CYBER MATRIX LAUNCHER NODE: 0x7E3", type = TerminalLine.LineType.SYSTEM),
      TerminalLine(text = ">> MATRIX DIGITAL RAIN SUBSYSTEM: ACTIVE", type = TerminalLine.LineType.SYSTEM),
      TerminalLine(text = ">> Type 'help' for command list, or tap APPS tab.", type = TerminalLine.LineType.NORMAL),
    )
    _uiState.update { it.copy(terminalLines = welcome) }
    setCipherInput(_uiState.value.cipherInput)
  }

  private fun appendTerminalLine(text: String, type: TerminalLine.LineType) {
    _uiState.update { state ->
      val updated = (state.terminalLines + TerminalLine(text = text, type = type)).takeLast(100)
      state.copy(terminalLines = updated)
    }
  }

  private fun startTelemetryMonitor() {
    viewModelScope.launch(Dispatchers.IO) {
      while (true) {
        val telemetry = querySystemTelemetry()
        _uiState.update { it.copy(telemetry = telemetry) }
        delay(3000)
      }
    }
  }

  private fun querySystemTelemetry(): SystemTelemetry {
    // Battery
    val bm = appContext.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    val batLevel = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 88
    val isCharging = bm?.isCharging ?: false

    // Memory
    val runtime = Runtime.getRuntime()
    val totalMem = runtime.totalMemory() / (1024 * 1024)
    val freeMem = runtime.freeMemory() / (1024 * 1024)
    val usedMem = totalMem - freeMem
    val percentMem = if (totalMem > 0) ((usedMem.toFloat() / totalMem.toFloat()) * 100).toInt().coerceIn(10, 95) else 45

    // Storage
    val stat = StatFs(Environment.getDataDirectory().path)
    val freeGb = (stat.availableBytes.toFloat() / (1024f * 1024f * 1024f))

    // Uptime
    val uptimeMs = SystemClock.elapsedRealtime()
    val sec = (uptimeMs / 1000) % 60
    val min = (uptimeMs / (1000 * 60)) % 60
    val hrs = (uptimeMs / (1000 * 60 * 60))
    val uptimeStr = String.format(Locale.US, "%02d:%02d:%02d", hrs, min, sec)

    // Network
    val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val netCap = cm?.getNetworkCapabilities(cm.activeNetwork)
    val netType = when {
      netCap?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WIFI (ENCRYPTED)"
      netCap?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "LTE / 5G"
      netCap?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "ETH0"
      else -> "NET-ONLINE"
    }

    return SystemTelemetry(
      batteryLevel = batLevel,
      isCharging = isCharging,
      memoryUsedPercent = percentMem,
      totalRamMb = 6144L,
      availableRamMb = (6144L * (100 - percentMem)) / 100L,
      storageFreeGb = String.format(Locale.US, "%.1f", freeGb).toFloatOrNull() ?: 54.3f,
      uptimeFormatted = uptimeStr,
      ipAddress = "192.168.1.104",
      networkType = netType,
      androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    )
  }

  private fun vibrateHaptic(ms: Long) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        val vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        @Suppress("DEPRECATION")
        vibrator?.vibrate(ms)
      }
    } catch (e: Exception) {
      // ignore
    }
  }
}
