package com.kapplication.launcher.behavior.player

import android.view.KeyEvent

import com.starcor.xul.XulUtils
import com.starcor.xulapp.XulApplication

/**
 * Created by hy on 2015/11/3.
 */
class TestSeekController(mp: SeekableHost) : SeekController(mp) {
    internal var _destroyed = false
    // <0 for rewind, >0 for fast forward
    internal var _seekDirection = 0
    internal var _seekDuration = 7000f
    internal var _seekPos = -1f
    internal var _seekRange = -1f
    internal var _seekBeginTime: Long = 0

    val currentSeekPos: Float
        get() {
            var deltaTime = XulUtils.timestamp() - _seekBeginTime
            val speedUpRange = 800f
            if (deltaTime < speedUpRange) {
                val x = Math.pow((deltaTime / speedUpRange).toDouble(), 0.5).toFloat()
                deltaTime = (deltaTime * (Math.sin((x + 1) * Math.PI) / Math.PI + x) / 2).toLong()
            } else {
                deltaTime -= (speedUpRange * 0.5).toLong()
            }
            val deltaPos = deltaTime / _seekDuration * _seekRange * _seekDirection.toFloat()
            return Math.min(Math.max(_seekPos + deltaPos, 0f), _seekRange)
        }

    init {
        val appInstance = XulApplication.getAppInstance()
        appInstance.postDelayToMainLooper(object : Runnable {
            override fun run() {
                if (_destroyed) {
                    return
                }
                appInstance.postDelayToMainLooper(this, 16)
                onSeekTimer()
            }

        }, 16)
    }

    private fun onSeekTimer() {
        if (_seekDirection == 0) {
            return
        }
        notifySeekPos(currentSeekPos)
    }

    private fun persistSeekPos() {
        if (_seekPos < 0) {
            _seekPos = 0f
            _seekRange = 99.9f
        } else {
            _seekPos = currentSeekPos
        }
        _seekBeginTime = XulUtils.timestamp()
    }

    override fun setPlayPos(currentPlayPos: Float) {
        if (_seekPos < 0) {
            _seekRange = 100f
        }
        _seekPos = currentPlayPos
    }

    override fun reset() {
        updateDirection(0)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (action == KeyEvent.ACTION_DOWN) {
                updateDirection(-1)
            } else if (action == KeyEvent.ACTION_UP) {
                updateDirection(0)
            }
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (action == KeyEvent.ACTION_DOWN) {
                updateDirection(1)
            } else if (action == KeyEvent.ACTION_UP) {
                updateDirection(0)
            }
        }
        return false
    }

    private fun updateDirection(dir: Int) {
        if (_seekDirection == dir) {
            return
        }
        persistSeekPos()
        val oldDir = _seekDirection
        _seekDirection = dir
        if (_seekDirection == 0) {
            notifySeekPos(currentSeekPos)
            if (oldDir < 0) {
                notifySeekRewind(false)
            } else {
                notifySeekFastForward(false)
            }
        } else if (_seekDirection > 0) {
            notifySeekFastForward(true)
        } else {
            notifySeekRewind(true)
        }
    }

    override fun destroy() {
        _destroyed = true
    }
}
