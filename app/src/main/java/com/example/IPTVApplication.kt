package com.example

import android.app.Application
import android.os.Build
import android.util.Log
import android.webkit.WebView

class IPTVApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // Multi-process safety: Only non-main processes should use setDataDirectorySuffix.
    // The main process (where packageName == processName) must use the default directory.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      try {
        val processName = Application.getProcessName()
        if (!processName.isNullOrBlank() && processName != packageName) {
          val safeSuffix = processName.replace(':', '_').replace('/', '_')
          WebView.setDataDirectorySuffix(safeSuffix)
        }
      } catch (t: Throwable) {
        Log.w("IPTVApplication", "Could not set WebView data directory suffix", t)
      }
    }

    // Protect application from crashing on unhandled background thread exceptions
    try {
      val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
      Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        Log.e("IPTVApplication", "Handled uncaught exception on thread ${thread.name}", throwable)
        // If it's a non-fatal background exception or WebView cache lock, prevent instant termination
        if (throwable is IllegalStateException || throwable is IllegalArgumentException) {
          Log.w("IPTVApplication", "Recoverable exception caught: ${throwable.message}")
        } else {
          defaultHandler?.uncaughtException(thread, throwable)
        }
      }
    } catch (_: Throwable) {
      // Safe ignore
    }
  }
}
