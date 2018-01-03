package com.kapplication.launcher.behavior.player

import android.view.KeyEvent
import com.kapplication.launcher.player.XulMediaPlayer

import com.starcor.xul.XulView
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.model.XulDataService

/**
 * Created by hy on 2015/11/3.
 */
abstract class Player {
    var isPaused = false
        protected set
    var isMediaRunning = false
        set(running) {
            field = running
            isPaused = false
        }

    abstract val mediaPlayer: XulMediaPlayer

    abstract val dataService: XulDataService

    abstract val seekController: SeekController

    abstract val uiSwitcher: PlayerUiSwitcher

    abstract val controller: PlayerController

    abstract fun showUI(componentId: String, show: Boolean)

    abstract fun getUiComponent(name: String): XulView

    abstract fun setProgress(pos: Float)

    abstract fun buildMessage(): XulMessageCenter.MessageHelper

    abstract fun dispatchSeekControllerEvents(event: KeyEvent): Boolean

    abstract fun onPlayPaused(paused: Boolean)

    abstract fun showErrorDialog(title: String, content: String)

    abstract fun destroy()

    companion object {
        val UI_CONTROL_BAR = "ControlBar"
        val UI_TITLE_FRAME = "TitleFrame"
        val UI_PLAY_LIST = "PlayList"
        val UI_PLAYBILL = "Playbill"
        val UI_MENU = "Menu"
        val UI_TIP_FRAME_A = "TipFrameA"
        val UI_TIP_FRAME_B = "TipFrameB"
        val UI_ALL = "[ALL]"

        var EVENT_STOP = 0x0001
        var EVENT_PAUSE = 0x0002
        var EVENT_START = 0x0003
        var EVENT_ERROR = 0x0004
    }
}
