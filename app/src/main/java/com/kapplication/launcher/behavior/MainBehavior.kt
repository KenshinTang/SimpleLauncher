package com.kapplication.launcher.behavior

import com.kapplication.launcher.CommonMessage
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.message.XulMessageCenter

/**
 * Created by hy on 2015/11/16.
 */
class MainBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {


    companion object {
        val NAME = "MainPageBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MainBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MainBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_UPDATE_MAIN_PAGE)
                .setInterval(500)
                .setRepeat(Integer.MAX_VALUE)
                .post()
    }
}
