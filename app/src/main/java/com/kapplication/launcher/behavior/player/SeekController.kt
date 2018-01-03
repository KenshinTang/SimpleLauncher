package com.kapplication.launcher.behavior.player

import android.view.KeyEvent

/**
 * Created by hy on 2015/11/3.
 */
open class SeekController(internal var _seekable: SeekableHost) {

    open fun onKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    open fun destroy() {}

    open fun setPlayPos(currentPlayPos: Float) {}

    protected fun notifySeekRewind(seeking: Boolean) {
        _seekable.onSeekRewindChanged(seeking)
    }

    protected fun notifySeekFastForward(seeking: Boolean) {
        _seekable.onSeekFastForwardChanged(seeking)
    }

    protected fun notifySeekPos(currentSeekPos: Float) {
        _seekable.onSeekPositionChanged(currentSeekPos)
    }

    open fun reset() {}
}
