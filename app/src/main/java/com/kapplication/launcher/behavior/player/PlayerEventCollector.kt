package com.kapplication.launcher.behavior.player

/**
 * Created by hy on 2015/12/14.
 */
class PlayerEventCollector(private val _openType: String, private val _mediaId: String, private val _videoId: String) {
    private var _playUrl: String? = null
    internal var _finished = false

    fun onOpen(playUrl: String) {
        _playUrl = playUrl
    }

    fun onPrepared(currentPlayPos: Long) {}

    fun onPlay(currentPlayPos: Long) {}

    fun onPaused(currentPlayPos: Long) {}

    fun onComplete(currentPlayPos: Long) {
        _finishCollect()
    }

    fun onTerminate(currentPlayPos: Long) {
        _finishCollect()
    }

    fun onBufferBegin(currentPlayPos: Long) {}

    fun onBufferEnd(currentPlayPos: Long) {}

    fun onSeekTo(currentPlayPos: Long, seekTarget: Long) {}

    fun onSeekComplete(currentPlayPos: Long) {}

    fun onError(code: Int, msg: String) {}

    private fun _finishCollect() {
        _finished = true
    }

    companion object {

        fun createPlayerEventCollector(openType: String, mediaId: String, videoId: String): PlayerEventCollector {
            return PlayerEventCollector(openType, mediaId, videoId)
        }
    }
}
