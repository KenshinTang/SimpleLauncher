package com.kapplication.launcher.behavior

import android.text.Html
import android.text.TextUtils
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


class SpecialBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "SpecialBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return SpecialBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return SpecialBehavior::class.java
                        }
                    })
        }
    }

    private var mSpecialId: String? = null
    private var mPageNumberView: XulView? = null
    private var mVideoCount: Int = 0
    private var mVideoListWrapper: XulMassiveAreaWrapper? = null

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
        initView()

        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            mSpecialId = extInfo.getString("specialId")
            XulLog.i(NAME, "mSpecialId = $mSpecialId")
        }

        if (TextUtils.isEmpty(mSpecialId)) {
            showEmptyTips()
            XulLog.i(NAME, "special id is null, show EmptyTips")
            return
        }

        requestSpecial()
    }

    private fun initView() {
        mPageNumberView = xulGetRenderContext().findItemById("page_number")
        mVideoListWrapper = XulMassiveAreaWrapper.fromXulView(_xulRenderContext.findItemById("area_special_list"))
    }

    private fun requestSpecial() {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "Special")
                .addQueryParameter("a", "getSpecialVideoList")
                .addQueryParameter("special_id", mSpecialId)

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().cacheControl(cacheControl).url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getSpecialVideoList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getSpecialVideoList onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            showEmptyTips()
                        }
                    } else {
                        xulGetRenderContext().refreshBinding("special-data", dataNode)
                        var videoNode: XulDataNode? = dataNode.getChildNode("data", "list")?.firstChild
                        var index = 0
                        while (videoNode != null) {
                            videoNode.setAttribute("index", ++index)
                            mVideoListWrapper?.addItem(videoNode)
                            videoNode = videoNode.next
                        }
                        mVideoCount = mVideoListWrapper?.itemNum()!!
                        XulApplication.getAppInstance().postToMainLooper {
                            if (dataNode.getChildNode("data", "list").size() == 0) {
                                showEmptyTips()
                            } else {
                                if (mVideoListWrapper?.itemNum()!! > 0) {
                                    mVideoListWrapper?.syncContentView()
                                } else {
                                    showEmptyTips()
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getSpecialVideoList onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    showEmptyTips()
                }
            }
        })
    }

    private fun showEmptyTips() {
//        mNoDataHintView?.setStyle("display", "block")
//        mNoDataHintView?.resetRender()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openDetail" -> openDetailPage(userdata as String)
            "posterFocus" -> {
                val bindingData = view?.getBindingData(0)
                val position = bindingData!!.getAttributeValue("index")
                val html = Html.fromHtml("<![CDATA[<font color=\"#ff9833\">" + position + "</font>/" +
                        mVideoCount + "]]>")
                mPageNumberView?.setAttr("text", html.toString())
                mPageNumberView?.resetRender()
            }
        }
        super.xulDoAction(view, action, type, command, userdata)
    }
}