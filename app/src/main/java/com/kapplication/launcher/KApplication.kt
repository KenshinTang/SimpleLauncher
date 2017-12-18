package com.kapplication.launcher

import android.text.TextUtils
import com.kapplication.launcher.behavior.MainBehavior
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.debug.XulDebugServer
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.utils.XulLog
import com.starcor.xulapp.utils.XulSystemUtil
import java.lang.reflect.Method

/**
 * Created by Kenshin on 2017/12/14.
 */
class KApplication : XulApplication() {
    private val XUL_FIRST_PAGE = "xul_layouts/pages/xul_main_page.xml"
    private val XUL_GLOBAL_BINDINGS = "xul_layouts/xul_global_bindings.xml"
    private val XUL_GLOBAL_SELECTORS = "xul_layouts/xul_global_selectors.xml"

    override fun onCreate() {
        XulLog.i("kenshin", "KApplication, onCreate.")
//        XulMessageCenter.getDefault().register(this)
        XulDebugServer.startUp()
        super.onCreate()
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
        val appPkgName = packageName
        val behaviorPkgName = appPkgName + ".behavior"
//        autoRegister(behaviorPkgName, XulUiBehavior::class.java)
        MainBehavior.register()
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
                .post()
    }
}