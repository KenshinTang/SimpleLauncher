package com.kapplication.launcher.behavior

import android.content.Intent
import android.provider.Settings
import com.kapplication.launcher.CommonMessage
import com.kapplication.launcher.UiManager
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
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
        const val NAME = "EpgBehavior"

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
    }

    override fun xulOnResume() {
        requestEpgData()
    }

    private fun requestEpgData() {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "HomePage")
                .addQueryParameter("a", "getHomeData")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getHomeData onResponse, but is not Successful")
                        UiManager.openUiPage("ErrorPage")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getHomeData onResponse")

                    val result : String = responseBody!!.string()
//                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)
                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            UiManager.openUiPage("ErrorPage")
                        }
                    } else {
                        XulApplication.getAppInstance().postToMainLooper {
                            xulGetRenderContext().refreshBinding("epg-data", dataNode)
                            XulLog.i(NAME, "refresh epg data!")
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getHomeData onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    UiManager.openUiPage("ErrorPage")
                }
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

    @XulSubscriber(tag = CommonMessage.EVENT_HALF_HOUR)
    private fun onHalfHourPassed(dummy: Any) {
        //首页刷新
        requestEpgData()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "open_vod_list_page" -> openListPage(userdata as String)
            "open_vod_detail_page" -> openDetail(userdata as String)
            "open_vod_player" -> XulLog.d(NAME, "open vod player")
            "open_live_player" -> XulLog.d(NAME, "open live player")
            "open_special_list_page" -> XulLog.d(NAME, "open special list page")
            "openSearch"  -> openSearch()
            "openAppList"  -> openAppList()
            "openSetting"  -> context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        super.xulDoAction(view, action, type, command, userdata)
    }
}