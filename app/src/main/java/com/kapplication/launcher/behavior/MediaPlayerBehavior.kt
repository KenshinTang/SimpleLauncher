package com.kapplication.launcher.behavior

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.XulDataNode
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException

class MediaPlayerBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter), SurfaceHolder.Callback {

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

    private var mLocalMessageCenter: XulMessageCenter? = null
    private var mMediaId: String? = ""
    private var mMediaPlayer: MediaPlayer? = null
    private var mContext: Context? = null

    init {
        mContext = _presenter.xulGetContext()

        mLocalMessageCenter = XulMessageCenter("Media Player")
        mLocalMessageCenter!!.register(this)

        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setOnPreparedListener({
            mMediaPlayer!!.start()
        })
        mMediaPlayer!!.setOnCompletionListener {
            (mContext as Activity).finish()
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        initView()

        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            mMediaId = extInfo.getString("mediaId")
            XulLog.i(NAME, "mediaId = $mMediaId")
        }

        requestPlayUrl()

    }

    private fun initView() {
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

                    mMediaPlayer!!.setDataSource(playUrl)
                    mMediaPlayer!!.prepareAsync()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getVideoPlayUrl onFailure")
            }
        })
    }

    override fun initRenderContextView(renderContextView: View): View {
        val viewRoot = FrameLayout(mContext)

        val surfaceView = SurfaceView(mContext)
        surfaceView.setZOrderOnTop(false)
        surfaceView.setZOrderMediaOverlay(false)

        surfaceView.holder.addCallback(this)
        viewRoot.addView(renderContextView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        viewRoot.addView(surfaceView)
        return viewRoot
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mMediaPlayer!!.setDisplay(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun xulOnDestroy() {
        mMediaPlayer!!.stop()
        mMediaPlayer!!.setDisplay(null)
        mMediaPlayer!!.release()
    }
}
