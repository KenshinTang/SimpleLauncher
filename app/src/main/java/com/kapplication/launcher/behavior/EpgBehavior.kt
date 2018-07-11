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
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by hy on 2015/11/16.
 */
class EpgBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    private var clockLabel: XulView? = null
    private val currentDate = Date()
    private val dateFormat = SimpleDateFormat("HH:mm")

    companion object {
        val NAME = "EpgBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return EpgBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return EpgBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
        clockLabel = xulGetRenderContext().findItemById("clock_label")
        val urlBuilder = HttpUrl.parse("http://192.168.1.3/epgapi.php")!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "HomePage")
                .addQueryParameter("a", "getHomeData")

        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body()
            }

            override fun onFailure(call: Call?, e: IOException?) {
            }
        })
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
        XulLog.i("EpgBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openListPage" -> openListPage(userdata as String, view?.getAttrString("text"))
            "openDetail" -> openDetail(userdata as String)
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
        XulLog.i("EpgBehavior", "openListPage($packageId)")
        val extInfo = XulDataNode.obtainDataNode("extInfo")
        extInfo.appendChild("packageId", packageId)
        extInfo.appendChild("title", title)
        UiManager.openUiPage("VideoListPage", extInfo)
    }

    private fun openDetail(dataSource: String?) {
        XulLog.i("EpgBehavior", "openDetail($dataSource)")
        UiManager.openUiPage("MediaDetailPage")
    }

}