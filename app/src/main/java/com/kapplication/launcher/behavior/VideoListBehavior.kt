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
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.debug.XulDebugAdapter.startActivity
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


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

    private var mPackageId: String = ""
    private var mOkHttpClient: OkHttpClient = OkHttpClient()
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
            XulLog.i("VideoListBehavior", "VideoListBehavior packageId = $mPackageId")
        }

        requestCategory(mPackageId)
    }

    private fun initView() {
        mVideoListView = _xulRenderContext.findItemById("area_video_list")
        mVideoListWrapper =  XulMassiveAreaWrapper.fromXulView(mVideoListView)
        mNoDataHintView = _xulRenderContext.findItemById("poster_no_data")
        mVideoCountView = _xulRenderContext.findItemById("filmsNumber")
    }

    private fun requestCategory(packageId: String) {
        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getAssetCategoryList")
                .addQueryParameter("asset_category_id", packageId)

        XulLog.i("VideoListBehavior", "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e("VideoListBehavior", "getAssetCategoryList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i("VideoListBehavior", "getAssetCategoryList onResponse")

                    val result : String = responseBody!!.string()
                    XulLog.json("VideoListBehavior", result)

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)
                    xulGetRenderContext().refreshBinding("category-data", dataNode)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e("VideoListBehavior", "getAssetCategoryList onFailure")
            }
        })
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("VideoListBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "switchCategory" -> switchCategory(userdata as String)
            "openPlayer" -> openPlayer(userdata as String)
            "openDetail" -> openDetail(userdata as String)
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
                .addQueryParameter("page_size", "200")

        XulLog.i("VideoListBehavior", "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e("VideoListBehavior", "getAssetVideoList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    XulLog.i("VideoListBehavior", "getAssetVideoList onResponse")

                    val ownerSlider = mVideoListView?.findParentByType("slider")
                    val ownerLayer = mVideoListView?.findParentByType("layer")

                    ownerLayer?.dynamicFocus = null
                    XulSliderAreaWrapper.fromXulView(ownerSlider).scrollTo(0, false)
                    mVideoListWrapper?.clear()

                    mVideoListView?.setStyle("display", "block")
                    mVideoListView?.resetRender()
                    mNoDataHintView?.setStyle("display", "none")
                    mNoDataHintView?.resetRender()

                    val result : String = responseBody!!.string()

                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    var videoNode: XulDataNode? = dataNode.getChildNode("data", "list").firstChild
                    while (videoNode != null) {
                        mVideoListWrapper?.addItem(videoNode)
                        videoNode = videoNode.next
                    }
                    mVideoListWrapper?.syncContentView()


//                    mVideoCountView?.setAttr("text", dataNode.getChildNode("l", "page_ctrl", "total_rows").value + " 部")
//                    mVideoCountView?.resetRender()
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e("VideoListBehavior", "getAssetCategoryList onFailure")
            }
        })

//        var fakeStream: InputStream? = null
//        when (mPackageId) {
//            "movie" -> fakeStream = xulGetAssets("fakeData/videolist_movie_$categoryId.xml")
//            "tvplay" -> fakeStream = xulGetAssets("fakeData/videolist_tvplay_$categoryId.xml")
//            "variety" -> fakeStream = xulGetAssets("fakeData/videolist_variety_$categoryId.xml")
//            "animation" -> fakeStream = xulGetAssets("fakeData/videolist_animation_$categoryId.xml")
////            "movie" -> fakeStream = xulGetFakeStreamFromSD("/mnt/usbhost/Storage01/SL/fakeData/videolist_movie_$categoryId.xml")
////            "tvplay" -> fakeStream = xulGetFakeStreamFromSD("/mnt/usbhost/Storage01/SL/fakeData/videolist_tvplay_$categoryId.xml")
////            "variety" -> fakeStream = xulGetFakeStreamFromSD("/mnt/usbhost/Storage01/SL/fakeData/videolist_variety_$categoryId.xml")
////            "animation" -> fakeStream = xulGetFakeStreamFromSD("/mnt/usbhost/Storage01/SL/fakeData/videolist_animation_$categoryId.xml")
//        }
//
//        if (fakeStream == null) {
//            val ownerLayer = mVideoListView?.findParentByType("layer")
//            val ownerSlider = mVideoListView?.findParentByType("slider")
//
//            ownerLayer?.dynamicFocus = null
//            XulSliderAreaWrapper.fromXulView(ownerSlider).scrollTo(0, false)
//            mVideoListWrapper?.clear()
//
//            mVideoListView?.setStyle("display", "none")
//            mVideoListView?.resetRender()
//            mNoDataHintView?.setStyle("display", "block")
//            mNoDataHintView?.resetRender()
//
//            mVideoCountView?.setAttr("text", "0 部")
//            mVideoCountView?.resetRender()
//        } else {
//            val ownerSlider = mVideoListView?.findParentByType("slider")
//            val ownerLayer = mVideoListView?.findParentByType("layer")
//
//            ownerLayer?.dynamicFocus = null
//            XulSliderAreaWrapper.fromXulView(ownerSlider).scrollTo(0, false)
//            mVideoListWrapper?.clear()
//
//            mVideoListView?.setStyle("display", "block")
//            mVideoListView?.resetRender()
//            mNoDataHintView?.setStyle("display", "none")
//            mNoDataHintView?.resetRender()
//
//            val dataNode: XulDataNode = XulDataNode.build(fakeStream)
//            var videoNode: XulDataNode? = dataNode.getChildNode("l", "il").firstChild
//            while (videoNode != null) {
//                mVideoListWrapper?.addItem(videoNode)
//                videoNode = videoNode.next
//            }
//            mVideoListWrapper?.syncContentView()
//
//            mVideoCountView?.setAttr("text", dataNode.getChildNode("l", "page_ctrl", "total_rows").value + " 部")
//            mVideoCountView?.resetRender()
//        }
    }

    private fun openDetail(dataSource: String) {
        XulLog.i("VideoListBehavior", "openDetail($dataSource)")
        UiManager.openUiPage("MediaDetailPage")
    }

    private fun openPlayer(dataSource: String) {
        XulLog.i("VideoListBehavior", "openPlayer($dataSource)")
        val uri = Uri.parse(dataSource)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "video/mp4")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun xulGetAssets(path: String): InputStream? {
        try {
            return _presenter.xulGetContext().assets.open(path, Context.MODE_PRIVATE)
        } catch (e: IOException) {
        }
        return null
    }

    private fun xulGetFakeStreamFromSD(path: String): InputStream? {
        try {
            return FileInputStream(path)
        } catch (e: Exception) {
        }
        return null
    }
}