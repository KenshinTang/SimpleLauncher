package com.kapplication.launcher.behavior.player

import android.view.KeyEvent

/**
 * Created by hy on 2015/11/21.
 */
object Utils {
    fun isControlKey(keyCode: Int): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_DOWN -> return true
        }
        return false
    }
}
