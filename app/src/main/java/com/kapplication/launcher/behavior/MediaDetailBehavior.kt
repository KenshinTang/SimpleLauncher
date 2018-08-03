package com.kapplication.launcher.behavior

import com.kapplication.launcher.utils.Utils
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException

class MediaDetailBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "MediaDetailBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MediaDetailBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MediaDetailBehavior::class.java
                        }
                    })
        }
    }

    private var mMediaId: String? = ""
    private var mMediaName: String? = ""

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()

        val extraInfo = _presenter.xulGetBehaviorParams()
        if (extraInfo != null) {
            mMediaId = extraInfo.getString("mediaId")
            XulLog.i(NAME, "mediaId: $mMediaId")
        }

        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getVodInfo")
                .addQueryParameter("video_id", mMediaId)
                .addQueryParameter("is_get_recom", "0")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    // 没有UI操作, 直接在子线程操作即可. refreshBinding只是刷新数据, 不会更新UI, 更新UI是数据触发的.
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getVodInfo onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getVodInfo onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    if (handleError(dataNode)) {
                        //TODO: 错误处理
                    } else {
                        mMediaName = dataNode.getChildNode("data", "video_info").getAttributeValue("video_name")
                        XulApplication.getAppInstance().postToMainLooper {
                            xulGetRenderContext().refreshBinding("media-detail", dataNode)
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e("kenshin", "getVodInfo onFailure")
            }
        })
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
//            "onPlayButtonClick" -> getPlayUrlAndPlay()
            "onPlayButtonClick" -> openPlayer(mMediaId, mMediaName)
            "openDetail" -> openDetail(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }
}