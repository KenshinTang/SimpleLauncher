package com.kapplication.launcher

import android.text.TextUtils
import android.util.Log
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.behavior.XulUiBehavior
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
        super.onCreate()
        Log.i("kenshin", "KApplication, onCreate.")
    }

    override fun onLoadXul() {
        xulLoadLayouts(XUL_FIRST_PAGE)
        xulLoadLayouts(XUL_GLOBAL_BINDINGS)
        xulLoadLayouts(XUL_GLOBAL_SELECTORS)
    }

    override fun onRegisterXulBehaviors() {
        registerComponent()
    }

    override fun onRegisterXulServices() {
        super.onRegisterXulServices()
    }

    private fun registerComponent() {
        val appPkgName = packageName
        val behaviorPkgName = appPkgName + ".behavior"
        autoRegister(behaviorPkgName, XulUiBehavior::class.java)
    }

    private fun autoRegister(pkgName: String, baseClass: Class<*>?) {
        XulLog.d(TAG, "autoRegister ", pkgName)
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
                    XulLog.d(TAG, "autoRegister ", className)
                    registerMethod = curClass.getMethod("register")
                    registerMethod.isAccessible = true
                    registerMethod.invoke(curClass)
                }
            } catch (e: Exception) {
                XulLog.e(TAG, e)
            }
        }
    }
}