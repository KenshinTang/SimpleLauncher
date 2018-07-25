package com.kapplication.launcher.behavior

import android.content.Intent
import android.net.Uri
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.debug.XulDebugAdapter
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

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()

        val extraInfo = _presenter.xulGetBehaviorParams()
        if (extraInfo != null) {
            mMediaId = extraInfo.getString("mediaId")
            XulLog.i("MediaDetailBehavior", "mediaId: $mMediaId")
        }

        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getVodInfo")
                .addQueryParameter("video_id", mMediaId)
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
        when (command) {
            "onPlayButtonClick" -> getPlayUrlAndPlay()
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun getPlayUrlAndPlay() {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getVideoPlayUrl")
                .addQueryParameter("video_id", mMediaId)
                .addQueryParameter("video_index", "1")

        XulLog.i("MediaDetailBehavior", "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e("MediaDetailBehavior", "getVideoPlayUrl onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i("MediaDetailBehavior", "getVideoPlayUrl onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json("MediaDetailBehavior", result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    val playUrl : String = dataNode.getChildNode("data").getAttributeValue("file_url")
                    XulLog.i("MediaDetailBehavior", "getVideoPlayUrl $playUrl")

                    XulApplication.getAppInstance().postToMainLooper({
                        val uri = Uri.parse(playUrl)
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, "video/mp4")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        XulDebugAdapter.startActivity(intent)
                    })
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e("kenshin", "getVideoPlayUrl onFailure")
            }
        })
    }
}