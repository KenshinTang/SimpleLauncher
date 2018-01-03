package com.kapplication.launcher.behavior.player

import android.os.Bundle
import com.kapplication.launcher.player.XulMediaPlayer

import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.model.XulDataService
import com.starcor.xulapp.utils.CancellableRunnable

/**
 * Created by hy on 2015/11/4.
 */
abstract class PlayerController(internal var _mp: Player) : XulMediaPlayer.XulMediaPlayerEvents {
    private var _delayPlay: CancellableRunnable? = null

    open val uiComponents: Array<UiComponentInfo>
        get() = defaultUiComponents

    val mediaPlayer: XulMediaPlayer?
        get() = _mp.mediaPlayer

    val dataService: XulDataService
        get() = _mp.dataService

    open fun init(behaviorParameters: Bundle) {}

    open fun doAction(view: XulView, action: String, type: String, command: String, userdata: Any): Boolean {
        return false
    }

    open fun destroy() {}

    open fun doTogglePlayPause(): Boolean {
        if (!_mp.isMediaRunning) {
            return false
        }

        val mediaPlayer = mediaPlayer
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
        } else {
            mediaPlayer!!.play()
        }
        _mp.onPlayPaused(!mediaPlayer!!.isPlaying)
        return true
    }

    open fun doPlay(mediaId: String) {}

    fun delayDoPlay(mediaId: String, ms: Int) {
        val delayPlay = _delayPlay
        delayPlay?.cancel()
        _delayPlay = object : CancellableRunnable() {
            override fun doRun() {
                doPlay(mediaId)
            }
        }
        XulApplication.getAppInstance().postDelayToMainLooper(_delayPlay, ms.toLong())
    }

    open fun doSeek(pos: Float): Long {
        val mediaPlayer = mediaPlayer
        val duration = mediaPlayer!!.duration
        val seekTargetPos = percentageToOffset(pos, duration)
        mediaPlayer!!.seekTo(seekTargetPos)
        return seekTargetPos
    }

    override fun onError(xmp: XulMediaPlayer, code: Int, msg: String): Boolean {
        _mp.isMediaRunning = false
        val mediaPlayer = mediaPlayer
        mediaPlayer!!.stop()
        return false
    }

    override fun onPrepared(xmp: XulMediaPlayer): Boolean {
        _mp.isMediaRunning = true
        _mp.onPlayPaused(!xmp.isPlaying)
        return false
    }

    override open fun onSeekComplete(xmp: XulMediaPlayer, pos: Long): Boolean {
        return false
    }

    override open fun onComplete(xmp: XulMediaPlayer): Boolean {
        _mp.onPlayPaused(!xmp.isPlaying)
        _mp.isMediaRunning = false
        return false
    }

    override open fun onBuffering(xmp: XulMediaPlayer, buffering: Boolean, percentage: Float): Boolean {
        return false
    }

    override open fun onProgress(xmp: XulMediaPlayer, pos: Long): Boolean {
        val mediaPlayer = mediaPlayer ?: return false
        val duration = mediaPlayer.duration
        if (duration > 0) {
            _mp.setProgress(offsetToPercent(pos, duration))
        }
        return false
    }

    protected fun percentageToOffset(percent: Float, duration: Long): Long {
        return (percent * duration / 100.0f).toLong()
    }

    protected fun offsetToPercent(pos: Long, duration: Long): Float {
        return 100.0f * pos / duration
    }

    override fun onDestroy(xmp: XulMediaPlayer) {
        _mp.isMediaRunning = false
    }

    abstract fun createControlBarAdapter(): PlayerControlBarAdapter

    class UiComponentInfo(var targetUi: String, var componentId: String, var layoutFile: String)

    companion object {
        private val defaultUiComponents = arrayOf(UiComponentInfo(Player.UI_TITLE_FRAME, "media-player-title-frame", "media_player_default_ui.xml"), UiComponentInfo(Player.UI_CONTROL_BAR, "media-player-control-bar", "media_player_default_ui.xml"), UiComponentInfo(Player.UI_MENU, "media-player-menu", "media_player_default_ui.xml"), UiComponentInfo(Player.UI_PLAY_LIST, "media-player-playlist", "media_player_default_ui.xml"), UiComponentInfo(Player.UI_PLAYBILL, "media-player-playbill", "media_player_default_ui.xml"))
    }
}
