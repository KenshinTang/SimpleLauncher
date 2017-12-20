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
class VideoListBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        val NAME = "VideoListBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return VideoListBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return VideoListBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            val packageId = extInfo.getString("packageId")
            val title = extInfo.getString("title")
            XulLog.i("VideoListBehavior", "VideoListBehavior packageId = $packageId")

            val titleView = _xulRenderContext.findItemById("title")
            titleView?.setAttr("text", title)
            titleView?.resetRender()
        }
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("VideoListBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openListPage" -> openListPage(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun openListPage(packageId: String) {
        XulLog.i("VideoListBehavior", "openListPage($packageId)")
        UiManager.openUiPage("ListPage")
    }
}