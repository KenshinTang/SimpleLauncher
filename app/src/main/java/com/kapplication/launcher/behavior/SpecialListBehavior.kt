package com.kapplication.launcher.behavior

import com.kapplication.launcher.utils.Utils
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException


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

    private var mSpecialListView: XulView? = null
    private var mNoDataHintView: XulView? = null
    private var mSpecialListWrapper: XulMassiveAreaWrapper? = null

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
        initView()
        requestSpecialList()
    }

    private fun initView() {
        mSpecialListView = _xulRenderContext.findItemById("area_special_list")
        mSpecialListWrapper =  XulMassiveAreaWrapper.fromXulView(mSpecialListView)
        mNoDataHintView = _xulRenderContext.findItemById("area_no_data")
    }

    private fun requestSpecialList() {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "Special")
                .addQueryParameter("a", "getSpecialList")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().cacheControl(cacheControl).url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getSpecialList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getSpecialList onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            showEmptyTips()
                        }
                    } else {
                        XulApplication.getAppInstance().postToMainLooper {
                            if (dataNode.getChildNode("data", "list").size() == 0) {
                                showEmptyTips()
                            } else {
                                var specialNode: XulDataNode? = dataNode.getChildNode("data", "list")?.firstChild
                                while (specialNode != null) {
                                    mSpecialListWrapper?.addItem(specialNode)
                                    specialNode = specialNode.next
                                }

                                mSpecialListWrapper?.syncContentView()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getSpecialList onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    showEmptyTips()
                }
            }
        })
    }

    private fun showEmptyTips() {
        mNoDataHintView?.setStyle("display", "block")
        mNoDataHintView?.resetRender()

    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openSpecial" -> openSpecialPage(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

}