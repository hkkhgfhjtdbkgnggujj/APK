package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.LauncherTab
import com.example.viewmodel.LauncherViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Cyber Matrix Launcher", appName)
  }

  @Test
  fun `test viewModel initial state and tabs`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val vm = LauncherViewModel(app)

    assertEquals(LauncherTab.APPS, vm.uiState.value.currentTab)
    vm.onTabSelected(LauncherTab.TERMINAL)
    assertEquals(LauncherTab.TERMINAL, vm.uiState.value.currentTab)

    vm.executeTerminalCommand("help")
    assertTrue(vm.uiState.value.terminalLines.isNotEmpty())
  }
}

