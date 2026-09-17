package com.project.lumina.client.util

import android.app.Activity
import android.content.Context
import android.util.Base64
import java.io.File

/**
 * Local session state for Shift Client.
 *
 * Linkvertise and other third-party gatekeeping have intentionally been removed.
 * The client no longer redirects users through an advertising or link-shortening
 * service before starting.
 */
class SessionManager(private val context: Context) {

    companion object {
        private const val SESSION_FILE = "session_data"
        private const val SESSION_DURATION_HOURS = 4
        private const val SESSION_DURATION_MS = SESSION_DURATION_HOURS * 60 * 60 * 1000L
    }

    /**
     * Authentication is no longer gated by Linkvertise. Keep the parameter for
     * source compatibility with existing callers.
     */
    fun checkSession(@Suppress("UNUSED_PARAMETER") activity: Activity): Boolean = true

    /**
     * Retained for compatibility with existing callback/deep-link code. A valid
     * locally generated request still creates a normal local session.
     */
    fun validateAndSaveSession(key: String, req: String): Boolean {
        if (key.isBlank() || req.isBlank()) return false
        saveSession()
        return true
    }

    fun saveSession() {
        val sessionFile = File(context.filesDir, SESSION_FILE)
        val timestamp = System.currentTimeMillis().toString()
        sessionFile.writeText(Base64.encodeToString(timestamp.toByteArray(), Base64.NO_WRAP))
    }

    fun getStoredReqCode(): String? = null

    fun clearSession() {
        File(context.filesDir, SESSION_FILE).delete()
        File(context.filesDir, "req_code").delete()
    }

    fun getRemainingSessionTime(): Long {
        val sessionFile = File(context.filesDir, SESSION_FILE)
        if (!sessionFile.exists()) return 0L

        return try {
            val timestamp = String(Base64.decode(sessionFile.readText(), Base64.DEFAULT)).toLong()
            (SESSION_DURATION_MS - (System.currentTimeMillis() - timestamp)).coerceAtLeast(0L)
        } catch (_: Exception) {
            0L
        }
    }
}
