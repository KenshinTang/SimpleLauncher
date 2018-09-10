package com.kapplication.launcher.upgrade

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.starcor.xulapp.utils.XulLog

class DownloadCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -2)
        XulLog.i("UpgradeUtils", "DownloadCompleteReceiver onReceive($downloadId, ${UpgradeUtils.instance.getDownloadId()}).")
        if (downloadId == UpgradeUtils.instance.getDownloadId()) {
            XulLog.i("UpgradeUtils", "Upgrade apk file download finish.")
            UpgradeUtils.instance.doUpgrade()
        }
    }
}