package com.example.model

import android.content.Intent
import android.graphics.drawable.Drawable

data class AppItem(
  val id: String,
  val label: String,
  val packageName: String,
  val activityName: String? = null,
  val icon: Drawable? = null,
  val isSystemApp: Boolean = false,
  val cyberAlias: String = "${label.lowercase().replace(" ", "_")}.bin",
  val launchIntent: Intent? = null,
)
