package com.kapplication.launcher.behavior

import android.content.Intent
import android.provider.Settings
import com.kapplication.launcher.UiManager
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog


class ErrorBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "ErrorBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return ErrorBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return ErrorBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "onRetryButtonClick" -> UiManager.openUiPage("EpgPage")
            "openSetting"  -> context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    override fun xulOnBackPressed(): Boolean {
        return true
    }
}