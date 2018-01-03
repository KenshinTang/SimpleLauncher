package com.kapplication.launcher.behavior.player

import com.kapplication.launcher.render.PlayerSeekBarRender
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.XulView

/**
 * Created by hy on 2015/12/17.
 */
open class TestPlayerControlBarAdapter(player: Player) : PlayerControlBarAdapter(player) {
    private val _playerState: XulView?
    private val _playerPos: XulView?
    private val _playerTimeInfoEnd: XulView?
    private val _playerTimeInfoBegin: XulView?
    private val _seekBarRender: PlayerSeekBarRender?

    init {
        _playerState = _controlBarFrame.findItemById("player-state")
        _playerPos = _controlBarFrame.findItemById("player-pos")
        _playerTimeInfoBegin = _controlBarFrame.findItemById("player-time-begin")
        _playerTimeInfoEnd = _controlBarFrame.findItemById("player-time-end")
        val render = _playerPos!!.render
        if (render is PlayerSeekBarRender) {
            _seekBarRender = render as PlayerSeekBarRender
        } else {
            _seekBarRender = null
        }
    }

    override fun setProgress(percent: Float) {
        if (_seekBarRender == null) {
            return
        }
        _seekBarRender!!.setSeekBarPos(percent)
    }

    override fun setProgressTips(tips: String) {
        if (_seekBarRender != null) {
            _seekBarRender!!.setSeekBarTips(tips)
        } else if (_playerPos != null) {
            _playerPos.setAttr(XulPropNameCache.TagId.TEXT, tips)
            _playerPos.resetRender()
        }
    }

    override fun onPlayPaused(paused: Boolean) {
        if (_playerState == null) {
            return
        }
        if (if (paused) _playerState.addClass("player-state-paused") else _playerState.removeClass("player-state-paused")) {
            _playerState.resetRender()
        }
    }

    override fun onRewind(seeking: Boolean) {
        if (_playerState == null) {
            return
        }
        if (if (seeking) _playerState.addClass("player-state-rewind") else _playerState.removeClass("player-state-rewind")) {
            _playerState.resetRender()
        }
    }

    override fun onFastForward(seeking: Boolean) {
        if (_playerState == null) {
            return
        }
        if (if (seeking) _playerState.addClass("player-state-fast-forward") else _playerState.removeClass("player-state-fast-forward")) {
            _playerState.resetRender()
        }
    }

    override fun setTimeInfo(begin: String, end: String) {
        if (_playerTimeInfoBegin != null) {
            val attrVal = _playerTimeInfoBegin.getAttrString(XulPropNameCache.TagId.TEXT)
            if (begin != attrVal) {
                _playerTimeInfoBegin.setAttr(XulPropNameCache.TagId.TEXT, begin)
                _playerTimeInfoBegin.resetRender()
            }
        }
        if (_playerTimeInfoEnd != null) {
            val attrVal = _playerTimeInfoEnd.getAttrString(XulPropNameCache.TagId.TEXT)
            if (end != attrVal) {
                _playerTimeInfoEnd.setAttr(XulPropNameCache.TagId.TEXT, end)
                _playerTimeInfoEnd.resetRender()
            }
        }
    }
}
