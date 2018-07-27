package com.kapplication.launcher.behavior

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.MotionEvent
import com.kapplication.launcher.utils.Utils
import com.kapplication.launcher.widget.XulExt_GSYVideoPlayer
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.starcor.xul.IXulExternalView
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException

class MediaPlayerBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter){

    companion object {
        const val NAME = "MediaPlayerBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MediaPlayerBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MediaPlayerBehavior::class.java
                        }
                    })
        }
    }

    private var mMediaId: String? = ""
    private var mMediaName: String? = ""
    private var mMediaPlayer: StandardGSYVideoPlayer? = null
    private var mContext: Context? = null

    init {
        mContext = _presenter.xulGetContext()
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        initView()

        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            mMediaId = extInfo.getString("mediaId")
            mMediaName = extInfo.getString("mediaName")
            XulLog.i(NAME, "mediaId = $mMediaId")
        }

        requestPlayUrl()

    }

    private fun initView() {
        mMediaPlayer = xulGetRenderContext().findItemById("player").externalView as StandardGSYVideoPlayer
        mMediaPlayer!!.setVideoAllCallBack(object: GSYSampleCallBack() {
            override fun onAutoComplete(url: String?, vararg objects: Any?) {
                (mContext as Activity).finish()
                super.onAutoComplete(url, *objects)
            }
        })
    }

    override fun xulCreateExternalView(cls: String, x: Int, y: Int, width: Int, height: Int, view: XulView): IXulExternalView? {
        if ("GSYVideoPlayer" == cls) {
            val player = XulExt_GSYVideoPlayer(context)
            _presenter.xulGetRenderContextView().addView(player, width, height)
            return player
        }

        return null
    }

    private fun requestPlayUrl() {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getVideoPlayUrl")
                .addQueryParameter("video_id", mMediaId)
                .addQueryParameter("video_index", "1")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")
        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getVideoPlayUrl onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getVideoPlayUrl onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    val playUrl : String = dataNode.getChildNode("data").getAttributeValue("file_url")
                    XulLog.i(NAME, "getVideoPlayUrl $playUrl")

                    XulApplication.getAppInstance().postToMainLooper {
                        mMediaPlayer!!.setUp(playUrl, true, mMediaName)
                        mMediaPlayer!!.startPlayLogic()
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getVideoPlayUrl onFailure")
            }
        })
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        super.xulDoAction(view, action, type, command, userdata)
    }

    override fun xulOnDispatchTouchEvent(event: MotionEvent?): Boolean {
        // 返回false, 交给Player自己处理
        return false
    }

    override fun xulOnDispatchKeyEvent(event: KeyEvent?): Boolean {
        // 返回false, 交给Player自己处理
        return false
    }

    override fun xulOnDestroy() {
        GSYVideoManager.releaseAllVideos()
    }
}
