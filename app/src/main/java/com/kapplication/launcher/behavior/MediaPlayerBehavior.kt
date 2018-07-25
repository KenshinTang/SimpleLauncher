package com.kapplication.launcher.behavior

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.kapplication.launcher.CommonMessage
import com.kapplication.launcher.behavior.player.*
import com.kapplication.launcher.player.XulMediaPlayer
import com.kapplication.launcher.player.impl.XulAndroidPlayer
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.XulManager
import com.starcor.xul.XulPage
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.message.XulSubscriber
import com.starcor.xulapp.model.XulDataService
import java.util.*


class MediaPlayerBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "media_player_behavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MediaPlayerBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MediaPlayerBehavior::class.java
                        }
                    })
        }
    }

    private var _viewRoot: FrameLayout? = null
    private var _player: XulMediaPlayer? = null
    private var _controller: PlayerController? = null
    private var _seekController: SeekController? = null
    private var _uiSwitcher: PlayerUiSwitcher? = null
    private var _localMessageCenter: XulMessageCenter? = null
    private var _controlBarAdapter: PlayerControlBarAdapter? = null

    private val _uiComponents = HashMap<String, XulView>()
    private var _seekable: SeekableHostImpl? = null
    private var _playerAdapter: PlayerAdapter? = null
    private var _xulBehaviorParams: Bundle? = null
    private var _playMode: String? = null

    init {
        _localMessageCenter = XulMessageCenter("Media Player")
        _localMessageCenter!!.register(this)
    }

    protected fun buildMessage(): XulMessageCenter.MessageHelper {
        return XulMessageCenter.buildMessage(_localMessageCenter)
    }

    override fun initRenderContextView(renderContextView: View): View {
        _viewRoot = FrameLayout(_presenter.xulGetContext())
        val viewRoot = _viewRoot
        val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
        createPlayer(viewRoot!!)
        viewRoot.addView(renderContextView, matchParent, matchParent)
        return viewRoot
    }

    protected fun createPlayer(parent: ViewGroup): View {
        _player = XulAndroidPlayer(true, true)
        return _player!!.init(_presenter.xulGetContext(), parent)
    }

    override fun initBehavior() {
        _xulBehaviorParams = _presenter.xulGetBehaviorParams()
        _seekable = SeekableHostImpl()
        _playerAdapter = PlayerAdapter()

        val mediaId = "mediaId"//DataModelUtils.parseMediaId(_xulBehaviorParams!!.getString("mediaId"))
        val categoryId = "categoryId"//DataModelUtils.parseCategoryId(_xulBehaviorParams!!.getString("categoryId"))
        _playMode = "_playMode"//DataModelUtils.selectFirstNotEmptyString(mediaId.videoType, categoryId.categoryType)

        _controller = TestVodPlayerController(_playerAdapter!!)
        _uiSwitcher = TestVodPlayerUiSwitcher(_playerAdapter!!)
        _seekController = TestSeekController(_seekable!!)

        _localMessageCenter!!.register(_controller)
        _localMessageCenter!!.register(_uiSwitcher)
        super.initBehavior()

        buildMessage()
                .setInterval(500)
                .setRepeat(Integer.MAX_VALUE)
                .setTag(CommonMessage.EVENT_PLAYER_UPDATE_TIMER)
                .post()
    }

    override fun xulOnRenderIsReady() {
        _controlBarAdapter = _controller!!.createControlBarAdapter()
        super.xulOnRenderIsReady()
    }

    override fun appOnStartUp(success: Boolean) {
        _player!!.setEventListener(_controller!!)
        if (_controller != null) {
            _controller!!.init(_xulBehaviorParams!!)
        }
    }

    override fun xulOnDestroy() {
        if (_controller != null) {
            _controller!!.destroy()
            _controller = null
        }

        if (_seekController != null) {
            _seekController!!.destroy()
            _seekController = null
        }
        if (_uiSwitcher != null) {
            _uiSwitcher!!.destroy()
            _uiSwitcher = null
        }

        if (_localMessageCenter != null) {
            _localMessageCenter!!.close()
            _localMessageCenter = null
        }

        if (_player != null) {
            _player!!.destroy()
        }
        super.xulOnDestroy()
    }

    override fun xulDoAction(view: XulView, action: String, type: String, command: String, userdata: Any) {
        if ("load" == action) {
            onXulLoaded()
            return
        }
        if (_controller != null && _controller!!.doAction(view, action, type, command, userdata)) {
            return
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    protected fun onXulLoaded() {
        val xulRenderContext = xulGetRenderContext()
        val controlBar = xulRenderContext.findItemById("control-bar")
        _uiComponents.put(Player.UI_CONTROL_BAR, controlBar)

        val titleFrame = xulRenderContext.findItemById("title-frame")
        _uiComponents.put(Player.UI_TITLE_FRAME, titleFrame)

        val playList = xulRenderContext.findItemById("player-playlist")
        _uiComponents.put(Player.UI_PLAY_LIST, playList)

        val playbill = xulRenderContext.findItemById("player-playbill")
        _uiComponents.put(Player.UI_PLAYBILL, playbill)

        val menu = xulRenderContext.findItemById("player-menu")
        _uiComponents.put(Player.UI_MENU, menu)

        val tipFrame1 = xulRenderContext.findItemById("tip-frame-a")
        _uiComponents.put(Player.UI_TIP_FRAME_A, tipFrame1)

        val tipFrame2 = xulRenderContext.findItemById("tip-frame-b")
        _uiComponents.put(Player.UI_TIP_FRAME_B, tipFrame2)

        val uiComponents = _controller!!.uiComponents
        var i = 0
        val uiComponentsLength = uiComponents.size
        while (i < uiComponentsLength) {
            val componentInfo = uiComponents[i]
            val xulView = _uiComponents[componentInfo.targetUi]
            if (xulView == null) {
                i++
                continue
            }

            val layoutFile = componentInfo.layoutFile
            if (!XulManager.isXulLoaded(layoutFile)) {
                XulApplication.getAppInstance().xulLoadLayouts(layoutFile)
            }
            xulView.setAttr(XulPropNameCache.TagId.COMPONENT, componentInfo.componentId)
            i++
        }
    }

    override fun xulOnDispatchKeyEvent(event: KeyEvent): Boolean {
        if (_uiSwitcher != null && _uiSwitcher!!.onKeyEvent(event)) {
            return true
        }
        val keyCode = event.keyCode
        return Utils.isControlKey(keyCode) && super.xulOnDispatchKeyEvent(event)
    }

    @XulSubscriber(tag = CommonMessage.EVENT_PLAYER_UPDATE_TIMER)
    fun onUpdateTimer(data: Any) {
        updatePlayerProgress()
    }

    @XulSubscriber(tag = CommonMessage.EVENT_PLAYER_PLAYLIST_FINISHED)
    fun onPlayListFinished(data: Any) {
        //		Toast.makeText(XulApplication.getAppContext(), "播放结束", Toast.LENGTH_SHORT).show();
        _presenter.xulDestroy()
    }

    protected fun updatePlayerProgress() {
        val player = _player ?: return
        player.updateProgress()
    }

    internal inner class SeekableHostImpl : SeekableHost {
        var _seekingOpCounter = 0
        var _seekPos: Float = 0.toFloat()

        protected fun setSeekBarPos(currentSeekPos: Float) {
            if (_controlBarAdapter == null) {
                return
            }
            _controlBarAdapter!!.setProgress(currentSeekPos / 100.0f)
        }

        override fun onSeekPositionChanged(currentSeekPos: Float) {
            setSeekBarPos(currentSeekPos)
            _seekPos = currentSeekPos
        }

        override fun onSeekRewindChanged(seeking: Boolean) {
            if (seeking) {
                ++_seekingOpCounter
            } else {
                --_seekingOpCounter
            }
            if (_controlBarAdapter != null) {
                _controlBarAdapter!!.onRewind(seeking)
            }
            doSeek()
        }

        override fun onSeekFastForwardChanged(seeking: Boolean) {
            if (seeking) {
                ++_seekingOpCounter
            } else {
                --_seekingOpCounter
            }

            if (_controlBarAdapter != null) {
                _controlBarAdapter!!.onFastForward(seeking)
            }
            doSeek()
        }

        protected fun doSeek() {
            val controller = _controller
            if (_seekingOpCounter == 0 && controller != null) {
                controller.doSeek(_seekPos)
            }
        }

        fun setPlayProgress(pos: Float) {
            if (_seekingOpCounter != 0) {
                return
            }
            setSeekBarPos(pos)
            val seekController = _seekController
            seekController?.setPlayPos(pos)
        }
    }

    internal inner class PlayerAdapter : Player() {

        override val mediaPlayer: XulMediaPlayer
            get() = _player!!

        override val dataService: XulDataService
            get() = xulGetDataService()

        override val seekController: SeekController
            get() = _seekController!!

        override val uiSwitcher: PlayerUiSwitcher
            get() = _uiSwitcher!!

        override val controller: PlayerController
            get() = _controller!!

        override fun showUI(componentId: String, show: Boolean) {
            val page = xulGetRenderContext().page ?: return
            XulPage.invokeActionNoPopupWithArgs(page, "appEvents:toggleUi", componentId, show)
        }

        override fun getUiComponent(name: String): XulView {
            return _uiComponents[name]!!
        }

        override fun setProgress(pos: Float) {
            val seekable = _seekable ?: return
            seekable.setPlayProgress(pos)
        }

        override fun buildMessage(): XulMessageCenter.MessageHelper {
            return this@MediaPlayerBehavior.buildMessage()
        }

        override fun dispatchSeekControllerEvents(event: KeyEvent): Boolean {
            return _seekController != null && isMediaRunning && _seekController!!.onKeyEvent(event)
        }

        override fun onPlayPaused(paused: Boolean) {
            isPaused = paused
            _controlBarAdapter!!.onPlayPaused(paused)
        }

        override fun showErrorDialog(title: String, content: String) {
//            this@MediaPlayerBehavior.showErrorDialog(title, content)
        }

        override fun destroy() {
            _presenter.xulDestroy()
        }
    }
}
