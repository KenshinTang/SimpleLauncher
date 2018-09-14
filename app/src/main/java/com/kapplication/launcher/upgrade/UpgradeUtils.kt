package com.kapplication.launcher.upgrade

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import com.kapplication.launcher.BuildConfig
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.XulDataNode
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.File
import java.io.IOException

class UpgradeUtils {

    companion object {
        val instance: UpgradeUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            UpgradeUtils()
        }

        private const val NAME = "UpgradeUtils"
        private const val TEMP_FILE = "/SL/SL_TEMP.apk"
        private var UPGRADE_FILE_PATH:String = Environment.getExternalStorageDirectory().absolutePath + TEMP_FILE
    }

    private var mAPKUrl: String = ""
    private var mVersionName: String? = ""
    private var mDownloadId: Long = -1
    private var mAllowedToUpgrade = true

    fun startCheckUpgrade(okHttpClient: OkHttpClient) {
        if (!mAllowedToUpgrade) {
            return
        }
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Terminal")
                .addQueryParameter("c", "Version")
                .addQueryParameter("a", "checkVersion")
                .addQueryParameter("version", BuildConfig.VERSION_NAME)

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "Check upgrade onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "Check upgrade onResponse")

                    val result : String = responseBody!!.string()
//                    XulLog.json(NAME, result)

                    val dataNode: XulDataNode = XulDataNode.buildFromJson(result)
                    if (!handleError(dataNode)) {
                        mAPKUrl = dataNode.getChildNode("data").getAttributeValue("new_version_url")
                        mVersionName = dataNode.getChildNode("data").getAttributeValue("new_version")
                        XulLog.i(NAME, "new apk url is: $mAPKUrl")
                        mDownloadId = downloadAPK(mAPKUrl)
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "Check upgrade onFailure")
            }
        })
    }

    fun stopCheckUpgrade() {
        mAllowedToUpgrade = false
    }

    fun restartCheckUpgrade() {
        mAllowedToUpgrade = true
    }

    private fun downloadAPK(url: String) : Long {
        if (TextUtils.isEmpty(url)) {
            return -1
        }

        val upgradeFile = File(UPGRADE_FILE_PATH)
        if (upgradeFile.exists()) {
            upgradeFile.delete()
        }

        val downloadManager: DownloadManager = XulApplication.getAppContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
                .setDestinationInExternalPublicDir("", TEMP_FILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        return downloadManager.enqueue(request)
    }

    fun getDownloadId() : Long {
        return mDownloadId
    }

    fun getNewVersionName() : String {
        if (mVersionName == null) {
            return ""
        }
        return mVersionName as String
    }

    fun doUpgrade() {
        XulLog.d(NAME, "install apk($UPGRADE_FILE_PATH)")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromFile(File(UPGRADE_FILE_PATH))
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        XulApplication.getAppContext().startActivity(intent)
    }

    private fun handleError(dataNode: XulDataNode?) : Boolean {
        if (dataNode == null || dataNode.size() == 0) {
            return true
        }
        val code = dataNode.getAttributeValue("ret")
        val reason = dataNode.getAttributeValue("reason")

        XulLog.d(NAME, "Request result:, ret=$code, reason=$reason")

        if ("0" != code) {
            return true
        }
        return false
    }

}