package com.kapplication.launcher.behavior

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.kapplication.launcher.UiManager
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.Wrapper.XulSliderAreaWrapper
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.debug.XulDebugAdapter.startActivity
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


class SearchBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "SearchBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return SearchBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return SearchBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
        XulLog.i(NAME, "xulOnRenderIsReady")
    }

    private fun initView() {
    }


    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
//            "switchCategory" -> switchCategory(userdata as String)
            "openDetail" -> openDetail(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }


    private fun openDetail(dataSource: String) {
        XulLog.i(NAME, "openDetail($dataSource)")
        val extInfoNode = XulDataNode.obtainDataNode("ext_info")
        extInfoNode.appendChild("mediaId", dataSource)
        UiManager.openUiPage("MediaDetailPage", extInfoNode)
    }
}