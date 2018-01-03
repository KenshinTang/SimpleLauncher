package com.kapplication.launcher.behavior.player

import com.starcor.xul.XulView

/**
 * Created by hy on 2015/12/17.
 */
abstract class PlayerControlBarAdapter(protected var _mp: Player) {
    protected var _controlBarFrame: XulView

    init {
        _controlBarFrame = _mp.getUiComponent(Player.UI_CONTROL_BAR)
    }

    /**
     * set current playing progress
     * @param percent [0~1]
     */
    abstract fun setProgress(percent: Float)

    open fun setProgressTips(tips: String) {}

    abstract fun onPlayPaused(paused: Boolean)

    abstract fun onRewind(seeking: Boolean)

    abstract fun onFastForward(seeking: Boolean)

    abstract fun setTimeInfo(begin: String, end: String)
}
