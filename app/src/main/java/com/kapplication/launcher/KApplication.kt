package com.kapplication.launcher

import android.text.TextUtils
import com.kapplication.launcher.behavior.*
import com.starcor.xul.XulWorker
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.debug.XulDebugServer
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.utils.XulLog
import com.starcor.xulapp.utils.XulSystemUtil
import com.starcor.xulapp.utils.XulTime
import com.tencent.bugly.crashreport.CrashReport
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.reflect.Method

/**
 * Created by Kenshin on 2017/12/14.
 */
class KApplication : XulApplication() {
//    private val XUL_FIRST_PAGE = "xul_layouts/pages/xul_main_page.xml"
    private val XUL_FIRST_PAGE = "xul_layouts/pages/xul_epg_page.xml"
    private val XUL_GLOBAL_BINDINGS = "xul_layouts/xul_global_bindings.xml"
    private val XUL_GLOBAL_SELECTORS = "xul_layouts/xul_global_selectors.xml"

    override fun onCreate() {
        XulLog.i("kenshin", "KApplication, onCreate.")
        XulDebugServer.startUp()
        CrashReport.initCrashReport(applicationContext, "b177a7e860", true)
        super.onCreate()
        XulTime.setTimeZoneOffset(8)
        startCommonMessage()
    }

    override fun onLoadXul() {
        xulLoadLayouts(XUL_FIRST_PAGE)
        xulLoadLayouts(XUL_GLOBAL_BINDINGS)
        xulLoadLayouts(XUL_GLOBAL_SELECTORS)
    }

    override fun onRegisterXulBehaviors() {
        registerComponent()
        UiManager.initUiManager()
    }

    override fun onRegisterXulServices() {
        super.onRegisterXulServices()
    }

    private fun registerComponent() {
//        val appPkgName = packageName
//        val behaviorPkgName = appPkgName + ".behavior"
//        autoRegister(behaviorPkgName, XulUiBehavior::class.java)

        EpgBehavior.register()
        VideoListBehavior.register()
        SpecialListBehavior.register()
        MediaDetailBehavior.register()
        MediaPlayerBehavior.register()
        SearchBehavior.register()
        ErrorBehavior.register()
    }

    override fun xulGetSdcardData(path: String): InputStream? {
        try {
            return FileInputStream("/mnt/usbhost/Storage01" + File.separator + path)
        } catch (e: Exception) {
        }
        return null
    }

    override fun xulResolvePath(downloadItem: XulWorker.DownloadItem?, path: String?): String {
        if (path!!.startsWith("scale:") && downloadItem is XulWorker.DrawableItem) {
            val imageItem: XulWorker.DrawableItem = downloadItem
            var url = path.substring(6)
            if (TextUtils.isEmpty(url)) {
                return ""
            }

            if (url.startsWith("http")) {
                var w = 0
                var h = 0
                if (imageItem.target_width != 0 && imageItem.target_height != 0) {
                    w = imageItem.target_width
                    h = imageItem.target_height
                } else if (imageItem.width != 0 && imageItem.height != 0) {
                    w = imageItem.width
                    h = imageItem.height
                }

                if (w != 0 && h != 0) {
                    url += "?x-oss-process=image/resize,m_fixed,h_$w,h_$h"
                }
                return url
            } else {
                return url
            }
        }
        return super.xulResolvePath(downloadItem, path)
    }

    private fun autoRegister(pkgName: String, baseClass: Class<*>?) {
        XulLog.d("kenshin", "autoRegister ", pkgName)
        if (TextUtils.isEmpty(pkgName) || baseClass == null) {
            return
        }

        val classList = XulSystemUtil.getClassInPackage(XulApplication.getAppContext(), pkgName)
        var curClass: Class<*>
        var registerMethod: Method
        for (className in classList) {
            try {
                curClass = Class.forName(className)
                if (baseClass.isAssignableFrom(curClass)) {
                    // 找到对应的子类，自动注册
                    XulLog.d("kenshin", "autoRegister ", className)
                    registerMethod = curClass.getMethod("register")
                    registerMethod.isAccessible = true
                    registerMethod.invoke(curClass)
                }
            } catch (e: Exception) {
                XulLog.e("kenshin", e)
            }
        }
    }

    private fun startCommonMessage() {
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_HALF_SECOND)
                .setInterval(500)
                .setRepeat(Integer.MAX_VALUE)
                .postSticky()

        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_HALF_HOUR)
                .setInterval(1000 * 60 * 30)
//                .setInterval(1000 * 10)
                .setRepeat(Integer.MAX_VALUE)
                .setDelay(1000 * 60 * 30)
                .postSticky()
    }
}