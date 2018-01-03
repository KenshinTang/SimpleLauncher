package com.kapplication.launcher.player.impl

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup

import com.kapplication.launcher.player.XulMediaPlayer
import com.starcor.xulapp.utils.XulLog

import java.io.IOException
import java.lang.ref.WeakReference

/**
 * Created by hy on 2015/11/2.
 */
class XulAndroidPlayer @JvmOverloads constructor(singlePlayer: Boolean = false, disposablePlayer: Boolean = false) : XulMediaPlayer() {
    internal var _ctx: Context? = null
    internal var _parent: ViewGroup? = null
    internal var _surface: SurfaceView? = null
    internal var _mp: MediaPlayer? = null
    internal var _url: String? = null
    internal var _seekTarget: Long = 0
    private var _singlePlayer = false
    private var _disposablePlayer = false

    internal var _playerState = PS_UNINITIALIZED
    private var _listener: XulMediaPlayer.XulMediaPlayerEvents? = null
    private var _surfaceHolderCallback: SurfaceHolder.Callback? = null
    private var _surfaceHolder: SurfaceHolder? = null
    private val _previousPlayer: WeakReference<XulAndroidPlayer>?

    private val isMediaStopped: Boolean
        get() = !_hasState(PS_PREPARED) || _hasAnyState(PS_STOPPED or PS_RELEASED or PS_UNINITIALIZED)

    override val duration: Long
        get() = if (_hasState(PS_PREPARED)) {
            _mp!!.duration.toLong()
        } else 0

    override val currentPosition: Long
        get() {
            if (_hasState(PS_PREPARED)) {
                if (_hasAnyState(PS_SEEKING or PS_BUFFERING)) {
                    return _seekTarget
                }
                val currentPosition = _mp!!.currentPosition
                if (currentPosition >= _mp!!.duration) {
                    return _seekTarget
                }
                _seekTarget = currentPosition.toLong()
                return _seekTarget
            }
            return 0
        }

    override val isPlaying: Boolean
        get() = _mp != null && _hasState(PS_PLAYING or PS_PREPARED) && !_hasAnyState(PS_STOPPED or PS_UNINITIALIZED or PS_RELEASED)

    private fun _changeState(removeState: Int, addState: Int) {
        _playerState = _playerState and removeState.inv() or addState
    }

    private fun _hasState(state: Int): Boolean {
        return _playerState and state == state
    }

    private fun _hasAnyState(state: Int): Boolean {
        return _playerState and state != 0
    }

    init {
        _singlePlayer = singlePlayer
        _disposablePlayer = disposablePlayer
        _previousPlayer = _currentPlayer
        _currentPlayer = WeakReference(this)
    }

    override fun init(ctx: Context, parent: ViewGroup): View {
        _ctx = ctx
        _parent = parent
        _surface = SurfaceView(ctx)
        _surface!!.setZOrderOnTop(false)
        _surface!!.setZOrderMediaOverlay(false)
        _createMediaPlayer()
        _surfaceHolderCallback = object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                _onInitialized(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                _onInitialized(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                _onSurfaceDestroyed()
            }
        }
        _surface!!.holder.addCallback(_surfaceHolderCallback)

        _parent!!.addView(_surface)
        return _surface as SurfaceView
    }

    private fun _createMediaPlayer() {
        if (_singlePlayer && _previousPlayer != null) {
            val prevPlayer = _previousPlayer.get()
            prevPlayer?._disposePlayer()
        }

        val mediaPlayer = MediaPlayer()
        mediaPlayer.setScreenOnWhilePlaying(true)
        mediaPlayer.setOnPreparedListener(MediaPlayer.OnPreparedListener { mp ->
            if (_mp !== mp) {
                return@OnPreparedListener
            }
            _onPrepared(mp)
        })
        mediaPlayer.setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener { mp ->
            if (_mp !== mp) {
                return@OnSeekCompleteListener
            }
            _onSeekComplete(mp)
        })
        mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener { mp ->
            if (_mp !== mp) {
                return@OnCompletionListener
            }
            _onCompletion(mp)
        })
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            if (_mp !== mp) {
                true
            } else _onError(mp, what, extra)
        }
        mediaPlayer.setOnInfoListener { mp, what, extra ->
            if (_mp !== mp) {
                true
            } else _onInfo(mp, what, extra)
        }

        val oldMediaPlayer = _mp
        _mp = mediaPlayer
        oldMediaPlayer?.release()

        if (_surfaceHolder != null) {
            mediaPlayer.setDisplay(_surfaceHolder)
        }
    }

    private fun _disposePlayer() {
        val mp = _mp
        _mp = null
        if (mp != null) {
            mp.setOnErrorListener(null)
            mp.setOnBufferingUpdateListener(null)
            mp.setOnSeekCompleteListener(null)
            mp.setOnCompletionListener(null)
            mp.setOnPreparedListener(null)
            mp.setOnInfoListener(null)
            mp.setDisplay(null)
            _changeState(PS_PREPARED or PS_PREPARING or PS_STOPPED or PS_ERROR or PS_PLAYING, PS_REBUILD or PS_RELEASED)
            mp.release()
        }
    }

    private fun _onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        if (isMediaStopped) {
            return false
        }
        val listener = _listener
        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
            if (_hasState(PS_BUFFERING)) {
                _changeState(PS_BUFFERING, 0)
                listener?.onBuffering(this, false, 100.0f)
            }
        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
            if (!_hasState(PS_BUFFERING)) {
                _changeState(0, PS_BUFFERING)
                listener?.onBuffering(this, true, 0.0f)
            }
        }
        if (what == 3) {
            XulLog.d("playVideo", "video first frame")
        }
        return false
    }

    private fun _onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        _changeState(0, PS_ERROR)
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            _changeState(0, PS_REBUILD)
        }
        val listener = _listener
        return listener?.onError(this, what, extra.toString()) ?: true
    }

    private fun _onCompletion(mp: MediaPlayer) {
        if (isMediaStopped) {
            return
        }
        val listener = _listener
        listener?.onComplete(this)
    }

    private fun _onSeekComplete(mp: MediaPlayer) {
        if (_hasAnyState(PS_SEEK_AGAIN)) {
            _changeState(PS_SEEK_AGAIN, 0)
            _mp!!.seekTo(_seekTarget.toInt())
            return
        }

        _changeState(PS_SEEKING, 0)
        if (_hasState(PS_STOPPED)) {
        } else if (_hasState(PS_PLAYING)) {
            _mp!!.start()
        } else {
            _mp!!.pause()
        }

        if (isMediaStopped) {
            return
        }
        val listener = _listener
        listener?.onSeekComplete(this, currentPosition)
    }

    private fun _onPrepared(mp: MediaPlayer) {
        _changeState(PS_PREPARING, PS_PREPARED)
        if (_hasState(PS_STOPPED)) {
        } else if (_hasState(PS_SEEKING)) {
            _mp!!.seekTo(_seekTarget.toInt())
        } else if (_hasState(PS_PLAYING)) {
            _mp!!.start()
        }

        if (_surface != null) {
            _surface!!.requestLayout()
        }

        val listener = _listener
        listener?.onPrepared(this)
    }

    private fun _onInitialized(holder: SurfaceHolder?) {
        _surfaceHolder = holder
        if (_mp != null && holder != null) {
            _mp!!.setDisplay(holder)
        }
        if (!_hasState(PS_UNINITIALIZED)) {
            _onSurfaceRestored()
            return
        }
        _changeState(PS_UNINITIALIZED, 0)
        if (_hasState(PS_PREPARING)) {
            _mp!!.prepareAsync()
        }
    }

    private fun _onSurfaceRestored() {
        if (!_hasState(PS_SURFACE_LOST)) {
            return
        }
        _changeState(PS_SURFACE_LOST, 0)

        if (_hasAnyState(PS_STOPPED or PS_ERROR)) {
            return
        }

        if (_hasState(PS_PLAYING)) {
            _mp!!.start()
        }
    }

    private fun _onSurfaceDestroyed() {
        _surfaceHolder = null
        if (_mp == null) {
            return
        }
        _mp!!.setDisplay(null)
        if (_hasState(PS_UNINITIALIZED)) {
            return
        }
        if (_hasState(PS_PREPARED) && !_hasAnyState(PS_UNINITIALIZED or PS_STOPPED or PS_ERROR)) {
            _mp!!.pause()
        }
        _changeState(0, PS_SURFACE_LOST)
    }

    override fun seekTo(pos: Long): Boolean {
        if (_hasState(PS_PREPARED or PS_SEEKING)) {
            _seekTarget = pos
            _changeState(0, PS_SEEK_AGAIN)
            return true
        }

        _changeState(0, PS_SEEKING)
        _seekTarget = pos
        if (_hasState(PS_PREPARED)) {
            _mp!!.seekTo(pos.toInt())
        }
        return true
    }

    override fun stop(): Boolean {
        if (_disposablePlayer && _hasAnyState(PS_PREPARED or PS_PREPARING or PS_ERROR)) {
            _disposePlayer()
            return true
        }

        val mp = _mp ?: return false
        if (_hasAnyState(PS_REBUILD or PS_RELEASED)) {
            _changeState(_playerState and (PS_REBUILD or PS_RELEASED).inv(), 0)
        } else {
            _changeState(_playerState, PS_STOPPED)
            mp.reset()
        }
        return true
    }

    override fun pause(): Boolean {
        _changeState(PS_PLAYING, 0)
        if (_hasState(PS_PREPARED)) {
            _mp!!.pause()
        }
        return true
    }

    override fun play(): Boolean {
        _changeState(PS_STOPPED, PS_PLAYING)
        if (_hasState(PS_PREPARED)) {
            _mp!!.start()
        }
        return true
    }

    override fun open(url: String): Boolean {
        if (_disposablePlayer && _hasAnyState(PS_PREPARED or PS_PREPARING or PS_ERROR)) {
            _disposePlayer()
        }

        if (_hasAnyState(PS_REBUILD or PS_RELEASED)) {
            _changeState(PS_REBUILD or PS_RELEASED or PS_PREPARED or PS_PREPARING or PS_STOPPED or PS_ERROR, 0)
            _createMediaPlayer()
        }

        val mp = _mp ?: return false
        try {
            _url = url
            XulLog.d(TAG, "playUrl = " + _url)
            if (_hasAnyState(PS_PREPARED or PS_PREPARING)) {
                mp.reset()
            }
            mp.setDataSource(_ctx, Uri.parse(_url))
            _changeState(PS_BUFFERING or PS_PREPARED, PS_PREPARING)
            if (!_hasState(PS_UNINITIALIZED)) {
                mp.prepareAsync()
            }
            return true
        } catch (e: IOException) {
            XulLog.e(TAG, e)
        }

        return false
    }

    override fun destroy() {
        _changeState(_playerState, PS_UNINITIALIZED)
        val mp = _mp
        _mp = null
        if (mp != null) {
            if (mp.isPlaying) {
                mp.reset()
            }
            mp.setDisplay(null)
            mp.release()
        }

        val surface = _surface
        _surface = null
        if (surface != null) {
            _parent!!.removeView(surface)
            _parent = null
        }
    }

    override fun updateProgress() {
        val player = _mp
        val listener = _listener
        if (player == null || listener == null) {
            return
        }
        if (_hasAnyState(PS_STOPPED or PS_RELEASED or PS_SEEKING or PS_UNINITIALIZED)) {
            return
        }
        if (!_hasState(PS_PREPARED or PS_PLAYING)) {
            return
        }
        listener.onProgress(this, currentPosition)
    }

    override fun setEventListener(listener: XulMediaPlayer.XulMediaPlayerEvents) {
        _listener = listener
    }

    companion object {
        private val TAG = XulAndroidPlayer::class.java.simpleName

        internal val PS_REBUILD = 0x20000     // player service died, must rebuild player
        internal val PS_SURFACE_LOST = 0x10000
        internal val PS_UNINITIALIZED = 0x8000
        internal val PS_RELEASED = 0x4000
        internal val PS_PREPARED = 0x2000
        internal val PS_STOPPED = 0x1000
        internal val PS_ERROR = 0x0800
        internal val PS_SEEK_AGAIN = 0x0010
        internal val PS_SEEKING = 0x0008
        internal val PS_BUFFERING = 0x0004
        internal val PS_PREPARING = 0x0002
        internal val PS_PLAYING = 0x0001

        private var _currentPlayer: WeakReference<XulAndroidPlayer>? = null
    }
}
