package com.kapplication.launcher.report

import com.starcor.xulapp.XulApplication
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

class ReportUtils {

    private var property = HashMap<String, String>()

    companion object {
        val instance: ReportUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ReportUtils()
        }
    }

    fun init(appKey: String, channel: String = "kenshin", deviceType: Int = UMConfigure.DEVICE_TYPE_BOX, pushSecret: String = "") {
        UMConfigure.init(XulApplication.getAppContext(), appKey, channel, deviceType, pushSecret)
    }

    fun addProperty(key: String, value: String) : ReportUtils {
        property[key] = value
        return instance
    }

    fun onEvent(eventID: String) {
        MobclickAgent.onEvent(XulApplication.getAppContext(), eventID, property)
        property = HashMap()
    }
}