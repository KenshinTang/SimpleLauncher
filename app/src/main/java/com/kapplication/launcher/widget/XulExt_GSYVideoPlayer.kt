package com.kapplication.launcher.widget

import android.content.Context
import android.graphics.Rect
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import com.starcor.xul.IXulExternalView


class XulExt_GSYVideoPlayer(context: Context) : StandardGSYVideoPlayer(context), IXulExternalView {

    override fun extMoveTo(x: Int, y: Int, width: Int, height: Int) {
        val layoutParams = this.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = x
        layoutParams.topMargin = y
        layoutParams.width = width
        layoutParams.height = height
        this.requestLayout()
    }

    override fun extMoveTo(rect: Rect) {
        extMoveTo(rect.left, rect.top, rect.width(), rect.height())
    }

    override fun extOnKeyEvent(event: KeyEvent): Boolean {
//        XulLog.i("kenshin", "$event")
        val keyCode = event.keyCode
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (event.action == KeyEvent.ACTION_UP)
                    when (currentState) {
                        GSYVideoView.CURRENT_STATE_PLAYING -> onVideoPause()
                        GSYVideoView.CURRENT_STATE_PAUSE -> onVideoResume()
                    }
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    cancelProgressTimer()
                    onVideoPause()
                    mProgressBar.progress++
                } else if (event.action == KeyEvent.ACTION_UP) {
                    seekTo((duration * mProgressBar.progress / 100).toLong())
                    onVideoResume()
                    startProgressTimer()
                }
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    cancelProgressTimer()
                    onVideoPause()
                    mProgressBar.progress--
                } else if (event.action == KeyEvent.ACTION_UP) {
                    seekTo((duration * mProgressBar.progress / 100).toLong())
                    onVideoResume()
                    startProgressTimer()
                }
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> return dispatchKeyEvent(event)
        }
        return false
    }

    override fun extOnFocus() {
        this.requestFocus()
    }

    override fun extOnBlur() {
        this.clearFocus()
    }

    override fun extShow() {
        this.visibility = View.VISIBLE
    }

    override fun extHide() {
        this.visibility = View.GONE
    }

    override fun extDestroy() {}

    override fun getAttr(key: String, defVal: String): String {
        return defVal
    }

    override fun setAttr(key: String, `val`: String): Boolean {
        return false
    }

    override fun extSyncData() {

    }
}
