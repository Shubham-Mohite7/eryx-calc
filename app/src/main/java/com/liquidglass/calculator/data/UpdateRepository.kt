package com.liquidglass.calculator.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val fileSizeBytes: Long = 0L
)

sealed interface UpdateStatus {
    data object Idle : UpdateStatus
    data object Checking : UpdateStatus
    data class UpToDate(val checkedAtMillis: Long) : UpdateStatus
    data class Available(val info: AppUpdateInfo) : UpdateStatus
    data class Error(val message: String) : UpdateStatus
}

class UpdateRepository(private val context: Context) {

    companion object {
        private const val GITHUB_LATEST_RELEASE_URL =
            "https://api.github.com/repos/Shubham-Mohite7/eryx-calc/releases/latest"
        private const val GITHUB_LATEST_RELEASE_PAGE =
            "https://github.com/Shubham-Mohite7/eryx-calc/releases/latest"
        private const val RAW_MANIFEST_URL =
            "https://raw.githubusercontent.com/Shubham-Mohite7/eryx-calc/main/update.json"
    }

    suspend fun checkForUpdate(
        currentVersionCode: Int,
        currentVersionName: String
    ): Result<AppUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val fromRelease = fetchFromGitHubRelease()
            val fromManifest = fetchFromManifest()

            val best = listOfNotNull(fromRelease, fromManifest)
                .maxWithOrNull { a, b -> compareVersions(a.versionName, a.versionCode, b.versionName, b.versionCode) }

            if (best != null && isNewer(best, currentVersionCode, currentVersionName)) {
                Result.success(best)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFromGitHubRelease(): AppUpdateInfo? {
        return try {
            val conn = (URL(GITHUB_LATEST_RELEASE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "eryx-android")
            }
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "").removePrefix("v").trim()
            if (tag.isBlank()) return null

            val assets = json.optJSONArray("assets") ?: return null
            var apkUrl: String? = null
            var apkSize = 0L
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.optString("name", "")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url", "")
                    apkSize = asset.optLong("size", 0L)
                    break
                }
            }
            if (apkUrl.isNullOrBlank()) return null

            AppUpdateInfo(
                versionCode = 0,
                versionName = tag,
                apkUrl = apkUrl,
                releaseNotes = json.optString("body", "Bug fixes and improvements.").ifBlank { "Bug fixes and improvements." },
                fileSizeBytes = apkSize
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromManifest(): AppUpdateInfo? {
        return try {
            val conn = (URL(RAW_MANIFEST_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = 7000
                readTimeout = 7000
                requestMethod = "GET"
                setRequestProperty("Cache-Control", "no-cache")
            }
            if (conn.responseCode !in 200..299) return null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val vCode = json.optInt("versionCode", 0)
            val vName = json.optString("versionName", "").removePrefix("v").trim()
            val apkUrl = json.optString("apkUrl", "").trim()
            if (vName.isBlank() || apkUrl.isBlank()) return null

            AppUpdateInfo(
                versionCode = vCode,
                versionName = vName,
                apkUrl = apkUrl,
                releaseNotes = json.optString("releaseNotes", "Bug fixes and improvements."),
                fileSizeBytes = json.optLong("fileSizeBytes", 0L)
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Opens the official GitHub APK download URL in the system browser so no in-app package
     * installation permission (REQUEST_INSTALL_PACKAGES) is required.
     */
    fun openUpdateDownload(activityContext: Context, info: AppUpdateInfo) {
        val targetUrl = info.apkUrl.ifBlank { GITHUB_LATEST_RELEASE_PAGE }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { activityContext.startActivity(intent) }
    }

    private fun isNewer(
        remote: AppUpdateInfo,
        currentCode: Int,
        currentName: String
    ): Boolean {
        if (remote.versionCode > 0 && remote.versionCode > currentCode) return true
        return compareSemver(remote.versionName, currentName.removePrefix("v")) > 0
    }

    private fun compareVersions(nameA: String, codeA: Int, nameB: String, codeB: Int): Int {
        val sem = compareSemver(nameA, nameB)
        return if (sem != 0) sem else codeA.compareTo(codeB)
    }

    private fun compareSemver(a: String, b: String): Int {
        val pa = a.split(".", "-", "+").mapNotNull { it.toIntOrNull() }
        val pb = b.split(".", "-", "+").mapNotNull { it.toIntOrNull() }
        val len = maxOf(pa.size, pb.size)
        for (i in 0 until len) {
            val va = pa.getOrElse(i) { 0 }
            val vb = pb.getOrElse(i) { 0 }
            if (va != vb) return va.compareTo(vb)
        }
        return 0
    }
}
