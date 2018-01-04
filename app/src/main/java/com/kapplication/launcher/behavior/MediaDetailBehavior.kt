package com.kapplication.launcher.behavior

import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog

class MediaDetailBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        val NAME = "MediaDetailBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MediaDetailBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MediaDetailBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("MediaDetailBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        super.xulDoAction(view, action, type, command, userdata)
    }
}