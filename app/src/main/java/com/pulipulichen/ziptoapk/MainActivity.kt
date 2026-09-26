package com.pulipulichen.ziptoapk

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class MainActivity : Activity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val worker = java.util.concurrent.Executors.newSingleThreadExecutor()
    private var progress: AlertDialog? = null
    private var pendingApk: File? = null
    private var launched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (launched) return
        val uri = when (intent?.action) {
            Intent.ACTION_VIEW -> intent.data
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            else -> null
        }
        if (uri == null) {
            Toast.makeText(this, "請從檔案管理員開啟 ZIP 檔。", Toast.LENGTH_LONG).show()
            mainHandler.postDelayed({ if (!isFinishing) finish() }, 1_500)
            return
        }
        launched = true
        showProgress()
        worker.execute {
            try {
                val apks = extractApks(uri)
                mainHandler.post {
                    dismissProgress()
                    when (apks.size) {
                        0 -> showError("這個 ZIP 檔裡沒有 APK。")
                        1 -> chooseApk(apks.first())
                        else -> chooseFromList(apks)
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    dismissProgress()
                    showError(e.message ?: "無法讀取這個 ZIP 檔。")
                }
            }
        }
    }

    private fun extractApks(uri: Uri): List<ExtractedApk> {
        val dir = File(cacheDir, "install")
        dir.deleteRecursively()
        check(dir.mkdirs() || dir.isDirectory) { "無法建立暫存資料夾。" }
        val found = mutableListOf<ExtractedApk>()
        var totalExpanded = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        contentResolver.openInputStream(uri)?.use { raw ->
            ZipInputStream(raw).use { zip ->
                var entry = zip.nextEntry
                var index = 0
                while (entry != null) {
                    if (!entry.isDirectory && entry.name.substringAfterLast('/').endsWith(".apk", ignoreCase = true)) {
                        check(found.size < MAX_APKS) { "ZIP 內 APK 數量過多（最多 $MAX_APKS 個）。" }
                        val output = File(dir, "package-${index++}.apk")
                        var apkBytes = 0L
                        FileOutputStream(output).use { sink ->
                            while (true) {
                                val count = zip.read(buffer)
                                if (count < 0) break
                                apkBytes += count
                                totalExpanded += count
                                check(apkBytes <= MAX_APK_BYTES) { "APK 太大，無法處理（上限 512 MB）。" }
                                check(totalExpanded <= MAX_TOTAL_BYTES) { "ZIP 解壓縮內容超過 1 GB 上限。" }
                                sink.write(buffer, 0, count)
                            }
                        }
                        check(apkBytes > 0) { "ZIP 內含有空白 APK 檔案。" }
                        found += ExtractedApk(entry.name, output)
                    } else {
                        while (true) {
                            val count = zip.read(buffer)
                            if (count < 0) break
                            totalExpanded += count
                            check(totalExpanded <= MAX_TOTAL_BYTES) { "ZIP 解壓縮內容超過 1 GB 上限。" }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: error("無法讀取 ZIP 檔，請確認檔案存取權限。")
        return found
    }

    private fun chooseFromList(apks: List<ExtractedApk>) {
        AlertDialog.Builder(this)
            .setTitle("選擇要安裝的 APK")
            .setItems(apks.map { it.name }.toTypedArray()) { _, which -> chooseApk(apks[which]) }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun chooseApk(apk: ExtractedApk) {
        pendingApk = apk.file
        if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
            AlertDialog.Builder(this)
                .setTitle("允許安裝未知應用程式")
                .setMessage("Android 需要你允許此應用程式啟動 APK 安裝。接下來請在系統設定中開啟「允許此來源」，再返回繼續。")
                .setNegativeButton("取消") { _, _ -> finish() }
                .setPositiveButton("前往設定") { _, _ ->
                    startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")),
                        REQUEST_INSTALL_PERMISSION
                    )
                }
                .setOnCancelListener { finish() }
                .show()
        } else {
            launchInstaller(apk.file)
        }
    }

    @Deprecated("Required for the Android unknown-apps settings flow on API 26+")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            val apk = pendingApk
            if (apk != null && packageManager.canRequestPackageInstalls()) launchInstaller(apk) else finish()
        }
    }

    private fun launchInstaller(file: File) {
        try {
            val apkUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(installIntent)
        } catch (e: Exception) {
            showError("無法啟動 Android 安裝程式：${e.message ?: "未知錯誤"}")
            return
        }
        finish()
    }

    private fun showProgress() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(20))
            addView(ProgressBar(this@MainActivity), LinearLayout.LayoutParams(dp(32), dp(32)))
            addView(TextView(this@MainActivity).apply {
                text = "正在尋找 ZIP 裡的 APK…"
                textSize = 16f
                setPadding(dp(16), 0, 0, 0)
            }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }
        progress = AlertDialog.Builder(this).setView(layout).setCancelable(false).create().also { it.show() }
    }

    private fun dismissProgress() {
        progress?.dismiss()
        progress = null
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("無法繼續")
            .setMessage(message)
            .setPositiveButton("關閉") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        dismissProgress()
        worker.shutdownNow()
        super.onDestroy()
    }

    private data class ExtractedApk(val name: String, val file: File)

    companion object {
        private const val REQUEST_INSTALL_PERMISSION = 701
        private const val MAX_APKS = 100
        private const val MAX_APK_BYTES = 512L * 1024 * 1024
        private const val MAX_TOTAL_BYTES = 1024L * 1024 * 1024
    }
}
