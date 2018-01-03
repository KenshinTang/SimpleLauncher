package com.kapplication.launcher.behavior.player

import android.view.KeyEvent

/**
 * Created by hy on 2015/11/3.
 */
open class PlayerUiSwitcher(internal var _mp: Player) {
    internal var _controlBarVisible = false
    internal var _menuVisible = false
    internal var _playlistVisible = false
    internal var _playbillVisible = false
    internal var _tipAVisible = false
    internal var _tipBVisible = false

    fun showControlBar(show: Boolean) {
        if (_controlBarVisible == show) {
            return
        }
        _mp.showUI(Player.UI_CONTROL_BAR, show)
        _controlBarVisible = show
    }

    fun showPlayList(show: Boolean) {
        if (_playlistVisible == show) {
            return
        }
        _mp.showUI(Player.UI_PLAY_LIST, show)
        _playlistVisible = show
    }

    fun showPlaybill(show: Boolean) {
        if (_playbillVisible == show) {
            return
        }
        _mp.showUI(Player.UI_PLAYBILL, show)
        _playbillVisible = show
    }

    fun showMenu(show: Boolean) {
        if (_menuVisible == show) {
            return
        }
        _mp.showUI(Player.UI_MENU, show)
        _menuVisible = show
    }

    fun showTipA(show: Boolean) {
        if (_tipAVisible == show) {
            return
        }
        _mp.showUI(Player.UI_TIP_FRAME_A, show)
        _tipAVisible = show
    }

    fun showTipB(show: Boolean) {
        if (_tipBVisible == show) {
            return
        }
        _mp.showUI(Player.UI_TIP_FRAME_B, show)
        _tipBVisible = show
    }

    fun showAllUI(show: Boolean) {
        _mp.showUI(Player.UI_ALL, show)
        _controlBarVisible = show
        _menuVisible = show
        _playlistVisible = show
        _playbillVisible = show
        _tipAVisible = show
        _tipBVisible = show
    }

    fun togglePlayPause() {
        val controller = _mp.controller
        controller.doTogglePlayPause()
    }

    protected fun dispatchSeekControllerEvents(event: KeyEvent): Boolean {
        return _mp.dispatchSeekControllerEvents(event)
    }

    fun destroy() {}

    open fun onKeyEvent(event: KeyEvent): Boolean {
        return false
    }

    fun onPlayerEvent(event: Int, info: String, data: Any): Boolean {
        return false
    }
}
