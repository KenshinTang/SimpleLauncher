package com.kapplication.launcher.player

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup

abstract class XulMediaPlayer {

    abstract val duration: Long

    abstract val currentPosition: Long

    abstract val isPlaying: Boolean

    abstract fun init(ctx: Context, parent: ViewGroup): View

    fun setScaleMode(mode: Int): Boolean {
        return false
    }

    abstract fun seekTo(pos: Long): Boolean

    abstract fun stop(): Boolean

    abstract fun pause(): Boolean

    abstract fun play(): Boolean

    abstract fun open(url: String): Boolean

    abstract fun destroy()

    abstract fun updateProgress()

    fun sendCommand(cmd: String, extInfo: Bundle): Boolean {
        return false
    }

    abstract fun setEventListener(listener: XulMediaPlayerEvents)

    interface XulMediaPlayerEvents {
        fun onError(xmp: XulMediaPlayer, code: Int, msg: String): Boolean

        fun onPrepared(xmp: XulMediaPlayer): Boolean

        fun onSeekComplete(xmp: XulMediaPlayer, pos: Long): Boolean

        fun onComplete(xmp: XulMediaPlayer): Boolean

        fun onBuffering(xmp: XulMediaPlayer, buffering: Boolean, percentage: Float): Boolean

        fun onProgress(xmp: XulMediaPlayer, pos: Long): Boolean

        fun onDestroy(xmp: XulMediaPlayer)
    }
}
