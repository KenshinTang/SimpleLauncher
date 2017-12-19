package com.kapplication.launcher.behavior

import com.kapplication.launcher.UiManager
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog

/**
 * Created by hy on 2015/11/16.
 */
class ListBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        val NAME = "ListBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return ListBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return ListBehavior::class.java
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
        XulLog.i("ListBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openListPage" -> openListPage(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun openListPage(packageId: String) {
        XulLog.i("ListBehavior", "openListPage($packageId)")
        UiManager.openUiPage("ListPage")
    }
}