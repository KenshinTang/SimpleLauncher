package com.kapplication.launcher.behavior

import android.text.TextUtils
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.Wrapper.XulSliderAreaWrapper
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException


class VideoListBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "VideoListBehavior"

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

    private var mPackageId: String = ""
    private var mVideoListView: XulView? = null
    private var mNoDataHintView: XulView? = null
    private var mVideoListWrapper: XulMassiveAreaWrapper? = null
    private var mVideoCountView: XulView? = null

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
        initView()
        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            mPackageId = extInfo.getString("packageId")
            XulLog.i(NAME, "VideoListBehavior packageId = $mPackageId")
        }

        if (TextUtils.isEmpty(mPackageId)) {
            showEmptyTips()
            XulLog.i(NAME, "package id is null, show EmptyTips")
            return
        }

        requestCategory(mPackageId)
    }

    private fun initView() {
        mVideoListView = _xulRenderContext.findItemById("area_video_list")
        mVideoListWrapper =  XulMassiveAreaWrapper.fromXulView(mVideoListView)
        mNoDataHintView = _xulRenderContext.findItemById("area_no_data")
        mVideoCountView = _xulRenderContext.findItemById("filmsNumber")
    }

    private fun requestCategory(packageId: String) {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getAssetCategoryList")
                .addQueryParameter("asset_category_id", packageId)

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().cacheControl(cacheControl).url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getAssetCategoryList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getAssetCategoryList onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json(NAME, result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            showEmptyTips()
                        }
                    } else {
                        XulApplication.getAppInstance().postToMainLooper {
                            xulGetRenderContext().refreshBinding("category-data", dataNode)
                            if (dataNode.getChildNode("data", "list").size() == 0) {
                                showEmptyTips()
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getAssetCategoryList onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    showEmptyTips()
                }
            }
        })
    }

    private fun showEmptyTips() {
        mVideoListView?.setStyle("display", "none")
        mVideoListView?.resetRender()
        mNoDataHintView?.setStyle("display", "block")
        mNoDataHintView?.resetRender()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "switchCategory" -> switchCategory(userdata as String)
            "openPlayer" -> openPlayer(userdata as String, "")
            "openDetail" -> openDetailPage(userdata as String)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun switchCategory(categoryId: String) {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getAssetVideoList")
                .addQueryParameter("asset_category_id", categoryId)
                .addQueryParameter("page_num", "1")
                .addQueryParameter("page_size", "9999")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().cacheControl(cacheControl).url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getAssetVideoList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i(NAME, "getAssetVideoList onResponse")
                    val result : String = responseBody!!.string()

                    mVideoListWrapper?.clear()
                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)
                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            showEmptyTips()
                        }
                    } else {
                        var videoNode: XulDataNode? = dataNode.getChildNode("data", "list")?.firstChild
                        while (videoNode != null) {
                            mVideoListWrapper?.addItem(videoNode)
                            videoNode = videoNode.next
                        }

                        //update UI
                        XulApplication.getAppInstance().postToMainLooper {
                            val ownerSlider = mVideoListView?.findParentByType("slider")
                            val ownerLayer = mVideoListView?.findParentByType("layer")

                            ownerLayer?.dynamicFocus = null
                            XulSliderAreaWrapper.fromXulView(ownerSlider).scrollTo(0, false)

                            if (mVideoListWrapper?.itemNum()!! > 0) {
                                mVideoListView?.setStyle("display", "block")
                                mNoDataHintView?.setStyle("display", "none")
                            } else {
                                mVideoListView?.setStyle("display", "none")
                                mNoDataHintView?.setStyle("display", "block")
                            }
                            mVideoListView?.resetRender()
                            mNoDataHintView?.resetRender()

                            mVideoCountView?.setAttr("text", """${xulGetFocus().getDataString("count")} 部""")
                            mVideoCountView?.resetRender()

                            mVideoListWrapper?.syncContentView()
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getAssetCategoryList onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    showEmptyTips()
                }
            }
        })
    }
}