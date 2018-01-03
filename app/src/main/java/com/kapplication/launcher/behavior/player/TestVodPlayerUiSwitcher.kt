package com.kapplication.launcher.behavior.player

import android.view.KeyEvent

import com.kapplication.launcher.CommonMessage
import com.starcor.xul.XulUtils
import com.starcor.xulapp.message.XulSubscriber

/**
 * Created by hy on 2015/11/3.
 */
class TestVodPlayerUiSwitcher(mp: Player) : PlayerUiSwitcher(mp) {

    internal var _lastKeyEvent: Long = -1

    @XulSubscriber(tag = CommonMessage.EVENT_PLAYER_UPDATE_TIMER)
    fun onUpdateTimer(data: Any) {
        if (_lastKeyEvent < 0) {
            return
        }
        if (!_mp.isMediaRunning || _mp.isPaused) {
            return
        }
        val idleTime = XulUtils.timestamp() - _lastKeyEvent
        if (idleTime > 5000) {
            _lastKeyEvent = -1
            showAllUI(false)
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        _lastKeyEvent = XulUtils.timestamp()
        val keyCode = event.keyCode
        val keyAction = event.action
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (_controlBarVisible || _menuVisible || _playlistVisible) {
                showAllUI(false)
                return true
            }
        }

        if (_menuVisible) {
            return false
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            showMenu(true)
            showPlayList(false)
            showControlBar(false)
            return true
        }

        if (_playlistVisible) {
            return false
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            showPlayList(true)
            showMenu(false)
            showControlBar(false)
            return true
        }

        if (_controlBarVisible) {
            if (dispatchSeekControllerEvents(event)) {
                return true
            }
            if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)
                    && keyAction == KeyEvent.ACTION_UP
                    && _mp.isMediaRunning) {
                togglePlayPause()
                return true
            }
            return Utils.isControlKey(keyCode)
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            showControlBar(true)
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            showControlBar(true)
            return true
        }

        return (!(_playlistVisible || _menuVisible)
                && Utils.isControlKey(keyCode)
                && keyCode != KeyEvent.KEYCODE_BACK)
    }
}
