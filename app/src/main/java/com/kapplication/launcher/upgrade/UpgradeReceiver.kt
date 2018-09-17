package com.kapplication.launcher.upgrade

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kapplication.launcher.EpgActivity
import com.starcor.xulapp.utils.XulLog

class UpgradeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        XulLog.i("UpgradeUtils", "UpgradeReceiver onReceive().")
        if (Intent.ACTION_PACKAGE_REPLACED == intent?.action) {
            XulLog.i("UpgradeUtils", "install finish.")
            val i = Intent(context, EpgActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        }
    }
}