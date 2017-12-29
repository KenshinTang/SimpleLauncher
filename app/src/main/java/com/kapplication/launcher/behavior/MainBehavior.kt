package com.kapplication.launcher.behavior

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import com.kapplication.launcher.CommonMessage
import com.kapplication.launcher.UiManager
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.message.XulSubscriber
import com.starcor.xulapp.utils.XulLog
import com.starcor.xulapp.utils.XulTime
import java.text.SimpleDateFormat
import java.util.*

class MainBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    private var clockLabel: XulView? = null
    private val currentDate = Date()
    private val dateFormat = SimpleDateFormat("HH:mm")

    companion object {
        val NAME = "MainBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MainBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MainBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
        clockLabel = xulGetRenderContext().findItemById("clock_label")
    }

    override fun xulOnBackPressed(): Boolean {
        return true
    }

    @XulSubscriber(tag = CommonMessage.EVENT_HALF_SECOND)
    private fun onHalfSecondPassed(dummy: Any) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis / 1000 != currentDate.time / 1000) {
            currentDate.time = currentTimeMillis
            dateFormat.timeZone = XulTime.getTimeZone()
            val curTimeStr = dateFormat.format(currentDate)
            val clockTimeStr = clockLabel?.getAttrString(XulPropNameCache.TagId.TEXT)
            if (clockTimeStr != curTimeStr) {
                clockLabel?.setAttr(XulPropNameCache.TagId.TEXT, curTimeStr)
                clockLabel?.resetRender()
            }
        }
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("MainBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openListPage" -> openListPage(userdata as String, view?.getAttrString("text"))
            "openAppList"  -> openAppList()
            "openSetting"  -> context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun openAppList() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
        }
    }

    private fun openListPage(packageId: String, title: String?) {
        XulLog.i("MainBehavior", "openListPage($packageId)")
        val extInfo = XulDataNode.obtainDataNode("extInfo")
        extInfo.appendChild("packageId", packageId)
        extInfo.appendChild("title", title)
        UiManager.openUiPage("VideoListPage", extInfo)
    }
}