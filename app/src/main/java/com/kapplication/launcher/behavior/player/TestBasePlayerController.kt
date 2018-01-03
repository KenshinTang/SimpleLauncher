package com.kapplication.launcher.behavior.player

import com.kapplication.launcher.player.XulMediaPlayer

/**
 * Created by hy on 2015/11/4.
 */
abstract class TestBasePlayerController(mp: Player) : PlayerController(mp) {
    internal var _eventCollector: PlayerEventCollector? = null
    internal var _controlBarAdapter: PlayerControlBarAdapter? = null

    protected val currentPlayPos: Long
        get() {
            val mediaPlayer = mediaPlayer ?: return -1
            return mediaPlayer.currentPosition
        }

    protected val mediaBeginTime: Long
        get() = 0

    protected val mediaEndTime: Long
        get() {
            val mediaPlayer = mediaPlayer ?: return -1
            return mediaPlayer.duration
        }

    protected abstract val currentMediaId: String

    override fun onError(xmp: XulMediaPlayer, code: Int, msg: String): Boolean {
        super.onError(xmp, code, msg)
        fireEventPlayError(code, msg)
        _mp.showErrorDialog("error", String.format("player error %d - %s", code, msg))
        return false
    }

    override fun doTogglePlayPause(): Boolean {
        val ret = super.doTogglePlayPause()
        val eventCollector = _eventCollector
        if (ret && eventCollector != null) {
            if (mediaPlayer!!.isPlaying) {
                eventCollector.onPlay(currentPlayPos)
            } else {
                eventCollector.onPaused(currentPlayPos)
            }
        }
        return ret
    }

    override fun doSeek(pos: Float): Long {
        val currentPlayPos = currentPlayPos
        val seekTargetPos = defaultDoSeek(pos)
        fireEventSeekTo(currentPlayPos, seekTargetPos)
        return seekTargetPos
    }

    protected fun defaultDoSeek(pos: Float): Long {
        return super.doSeek(pos)
    }

    override fun destroy() {
        fireEventPlayTerminate()
        super.destroy()
    }

    override fun onBuffering(xmp: XulMediaPlayer, buffering: Boolean, percentage: Float): Boolean {
        eventBuffering(buffering, percentage)
        return super.onBuffering(xmp, buffering, percentage)
    }

    protected fun eventBuffering(buffering: Boolean, percentage: Float) {
        if (_eventCollector != null) {
            if (buffering && percentage == 0f) {
                _eventCollector!!.onBufferBegin(currentPlayPos)
            } else if (!buffering) {
                _eventCollector!!.onBufferEnd(currentPlayPos)
            }
        }
    }

    override fun onSeekComplete(xmp: XulMediaPlayer, pos: Long): Boolean {
        val ret = super.onSeekComplete(xmp, pos)
        eventSeekComplete(pos)
        return ret
    }

    protected fun eventSeekComplete(pos: Long) {
        if (_eventCollector != null) {
            _eventCollector!!.onSeekComplete(pos)
        }
    }

    override fun onComplete(xmp: XulMediaPlayer): Boolean {
        val ret = super.onComplete(xmp)
        fireEventPlayComplete()
        return ret
    }

    override fun onPrepared(xmp: XulMediaPlayer): Boolean {
        val ret = super.onPrepared(xmp)
        fireEventPlayPrepared()
        syncProgressTimeInfo()
        return ret
    }

    protected fun syncProgressTimeInfo() {
        _controlBarAdapter!!.setTimeInfo(convertPlayingPos(mediaBeginTime), convertPlayingPos(mediaEndTime))
    }

    override fun onProgress(xmp: XulMediaPlayer, pos: Long): Boolean {
        val mediaBeginTime = mediaBeginTime
        val percent = 100.0f * (currentPlayPos - mediaBeginTime) / (mediaEndTime - mediaBeginTime)
        _mp.setProgress(percent)
        return false
    }

    override fun createControlBarAdapter(): PlayerControlBarAdapter {
        if (_controlBarAdapter != null) {
            return _controlBarAdapter!!
        }
        _controlBarAdapter = createControlBarAdapter(_mp)
        return _controlBarAdapter!!
    }

    protected fun createControlBarAdapter(mp: Player): TestPlayerControlBarAdapter {
        return object : TestPlayerControlBarAdapter(mp) {
            internal var _lastProgress: Long = 0

            override fun setProgress(percent: Float) {
                val mediaBeginTime = mediaBeginTime
                val curTime = (((mediaEndTime - mediaBeginTime) * percent + 500) / 1000).toLong()
                if (_lastProgress != curTime) {
                    _lastProgress = curTime
                }
                super.setProgress(percent)
                setProgressTips(convertPlayingPos(mediaBeginTime + _lastProgress * 1000))
            }
        }
    }

    protected fun fireEventPrePlay(videoId: String, playType: String) {
        if (_eventCollector != null) {
            _eventCollector!!.onTerminate(currentPlayPos)
        }
        _eventCollector = PlayerEventCollector.createPlayerEventCollector(playType, currentMediaId, videoId)
    }

    protected fun fireEventPostPlay(playUrl: String) {
        if (_eventCollector != null) {
            _eventCollector!!.onOpen(playUrl)
            _eventCollector!!.onPlay(currentPlayPos)
        }
    }

    protected fun fireEventPlayPrepared() {
        if (_eventCollector != null) {
            _eventCollector!!.onPrepared(currentPlayPos)
        }
    }

    protected fun fireEventPlayComplete() {
        if (_eventCollector != null) {
            _eventCollector!!.onComplete(currentPlayPos)
        }
        _eventCollector = null
    }

    protected fun fireEventPlayTerminate() {
        if (_eventCollector != null) {
            _eventCollector!!.onTerminate(currentPlayPos)
        }
        _eventCollector = null
    }

    protected fun fireEventPlayError(code: Int, msg: String) {
        if (_eventCollector != null) {
            _eventCollector!!.onError(code, msg)
            _eventCollector!!.onTerminate(currentPlayPos)
        }
        _eventCollector = null
    }

    protected fun fireEventSeekTo(currentPlayPos: Long, seekTargetPos: Long) {
        if (_eventCollector != null) {
            _eventCollector!!.onSeekTo(currentPlayPos, seekTargetPos)
        }
    }

    protected fun convertPlayingPos(pos: Long): String {
        var sec = pos / 1000
        sec %= (24 * 60 * 60).toLong()
        var min = sec / 60
        sec %= 60
        val hour = min / 60
        min %= 60
        return String.format("%02d:%02d:%02d", hour, min, sec)
    }

    companion object {
        private val TAG = TestBasePlayerController::class.java.simpleName
    }
}
