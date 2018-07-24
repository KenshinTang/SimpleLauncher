package com.kapplication.launcher.behavior

import com.kapplication.launcher.utils.Utils
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException

class MediaDetailBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        val NAME = "MediaDetailBehavior"

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

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()

        val extraInfo = _presenter.xulGetBehaviorParams()
        var mediaId: String? = ""
        if (extraInfo != null) {
            mediaId = extraInfo.getString("mediaId")
            XulLog.i("MediaDetailBehavior", "mediaId: $mediaId")
        }

        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getVodInfo")
                .addQueryParameter("video_id", mediaId)
                .addQueryParameter("is_get_recom", "0")

        XulLog.i("MediaDetailBehavior", "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    // 没有UI操作, 直接在子线程操作即可. refreshBinding只是刷新数据, 不会更新UI, 更新UI是数据触发的.
                    if (!response.isSuccessful) {
                        XulLog.e("MediaDetailBehavior", "getVodInfo onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i("MediaDetailBehavior", "getVodInfo onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json("MediaDetailBehavior", result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)
                    xulGetRenderContext().refreshBinding("media-detail", dataNode)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e("kenshin", "getVodInfo onFailure")
            }
        })
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("MediaDetailBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        super.xulDoAction(view, action, type, command, userdata)
    }
}