package com.kapplication.launcher.behavior

import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog


class SpecialListBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "SpecialListBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return SpecialListBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return SpecialListBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
    }

    private fun initView() {
    }


    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "switchCategory" -> XulLog.i(NAME, "switchCategory")
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

}