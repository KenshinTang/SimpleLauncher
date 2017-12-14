package com.kapplication.launcher

import android.app.Application
import android.util.Log

/**
 * Created by Kenshin on 2017/12/14.
 */
class KApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("kenshin", "KApplication, onCreate.")
    }
}