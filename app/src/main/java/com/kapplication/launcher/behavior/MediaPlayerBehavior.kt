package com.kapplication.launcher.behavior

import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog

/**
 * Created by hy on 2015/11/16.
 */
class MediaPlayerBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        val NAME = "MediaPlayerBehavior"

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

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
    }


    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("VideoListBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        super.xulDoAction(view, action, type, command, userdata)
    }
}