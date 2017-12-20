package com.kapplication.launcher.behavior

import android.content.Context
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.Wrapper.XulSliderAreaWrapper
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.OkHttpClient
import java.io.IOException
import java.io.InputStream

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

    private var mPackageId: String = ""
    private var mOkHttpClient: OkHttpClient = OkHttpClient()
    private var mVideoListView: XulView? = null
    private var mVideoListWrapper: XulMassiveAreaWrapper? = null

    override fun appOnStartUp(success: Boolean) {

    }

    private fun setTitle(title: String) {
        val titleView = _xulRenderContext.findItemById("title")
        titleView?.setAttr("text", title)
        titleView?.resetRender()
    }

    override fun xulOnRenderIsReady() {
        initView()
        val extInfo = _presenter.xulGetBehaviorParams()
        if (extInfo != null) {
            mPackageId = extInfo.getString("packageId")
            XulLog.i("VideoListBehavior", "VideoListBehavior packageId = $mPackageId")

            setTitle(extInfo.getString("title"))
        }

        requestCategory(mPackageId)
    }

    private fun initView() {
        mVideoListView = _xulRenderContext.findItemById("area_video_list")
        mVideoListWrapper =  XulMassiveAreaWrapper.fromXulView(mVideoListView)
    }

    private fun requestCategory(packageId: String) {
        var fakeStream: InputStream? = null
        when (packageId) {
            "movie" -> fakeStream = xulGetAssets("fakeData/category_movie.xml")
            "tvplay" -> fakeStream = xulGetAssets("fakeData/category_tvplay.xml")
            "variety" -> fakeStream = xulGetAssets("fakeData/category_variety.xml")
            "animation" -> fakeStream = xulGetAssets("fakeData/category_animation.xml")
        }
        val dataNode: XulDataNode = XulDataNode.build(fakeStream)
        xulGetRenderContext().refreshBinding("category-data", dataNode)
//        val request = Request.Builder().url("http://www.baidu.com").get().build()
//        val call = mOkHttpClient.newCall(request)
//        call.enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//
//            }
//
//            @Throws(IOException::class)
//            override fun onResponse(call: Call, response: Response) {
//                val fakeData_category = """<epg><type>asset</type><id>movie</id><name>电影</name><subname>电影</subname><img0>http://3img.imgo.tv/preview/internettv/prev/starcor/asset/movie/8611d8639f49fea5ce53e12c86b180c5.png</img0><img1>http://0img.imgo.tv/preview/internettv/prev/starcor/asset/movie/043b4c6c56c0360312a97103526af060.png</img1><img2>http://2img.imgo.tv/preview/internettv/prev/starcor/asset/movie/1000120/6c9df6a1f74719d6b6dad11c0504aa3c.png</img2><img_icon>栏目图标</img_icon><img_content_background>栏目背景图</img_content_background><img_default_img>默认图标</img_default_img><img_focus_img>焦点图标</img_focus_img><img_select_img>选中图标</img_select_img><language><en_us><name>媒资包美式英文名称</name></en_us></language><arg_list><category_list><i><id>1000031</id><name>院线热播</name><type>0</type><special_poster_width>专题占用海报位个数</special_poster_width><language><en_us><name>媒资包栏目美式英文名称</name></en_us></language></i><i><id>1000120</id><name>VIP电影</name><subname>VIP电影</subname><img0>http://1img.imgo.tv/preview/internettv/prev/starcor/asset/movie/1000120/a9a698dbb8ce75747d56a0d566739b11.png</img0><img1>http://0img.imgo.tv/preview/internettv/prev/starcor/asset/movie/1000120/9f8b91dc83ab4248c8d31b3ae4558cfd.png</img1><img2>http://2img.imgo.tv/preview/internettv/prev/starcor/asset/movie/1000120/6c9df6a1f74719d6b6dad11c0504aa3c.png</img2><img_icon>栏目图标</img_icon><img_content_background>栏目背景图</img_content_background><img_default_img>默认图标</img_default_img><img_focus_img>焦点图标</img_focus_img><img_select_img>选中图标</img_select_img><type>0</type><special_poster_width>专题占用海报位个数</special_poster_width><ad_info><i><ad_pos_id>ttmgtvgg-pic</ad_pos_id>//广告位ID<ad_pos_name>ttmgtvgg-pic</ad_pos_name>//广告位名称<ad_pos_width>50</ad_pos_width>//广告位宽<ad_pos_height>50</ad_pos_height>//广告位高<ad_pos_device_type>stb</ad_pos_device_type>//终端类型<ad_pos_content_type>image</ad_pos_content_type>//广告位内容类型<ad_pos_priority>0</ad_pos_priority>//优先级<ad_pos_is_vip_closed>0</ad_pos_is_vip_closed>//VIP是否可以跳过<ad_pos_is_force_play>0</ad_pos_is_force_play>//该广告位为是否强制播出<ad_pos_is_product_closed>0</ad_pos_is_product_closed>//已购买产品是否跳过广告</i></ad_info></i><i><id>1000666</id><name>每周排行</name><type>0</type></i><i><id>1000002</id><name>华语电影</name><type>0</type></i><i><id>1000222</id><name>日韩电影</name><type>0</type></i><i><id>1000038</id><name>动作冒险</name><third_id>3333</third_id><type>1</type></i><i><id>1000039</id><name>爱情喜剧</name><type>1</type></i><i><id>1000901</id><name>测试排行榜</name><type>6</type><child_category_list><i><id>1000901001</id><name>日</name><type>6</type></i><i><id>1000901002</id><name>月</name><type>6</type></i><i><id>1000901003</id><name>周</name><type>6</type></i></child_category_list></i></category_list><metadata_list><i><key>test</key><value>热热</value></i><i><key>test3</key><value>热人</value></i></metadata_list></arg_list><result><state>200</state><reason>ok</reason></result></epg>"""
//                val dataNode: XulDataNode = XulDataNode.build(fakeData_category.toByteArray())
//                xulGetRenderContext().refreshBinding("category-data", dataNode)
//            }
//        })
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("VideoListBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "switchCategory" -> switchCategory(userdata as String)
            "openPlayer" -> openPlayer()
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun switchCategory(categoryId: String) {
        var fakeStream: InputStream? = null
        when (mPackageId) {
            "movie" -> fakeStream = xulGetAssets("fakeData/videolist_movie1.xml")
//            "tvplay" -> fakeStream = xulGetAssets("fakeData/category_tvplay.xml")
//            "variety" -> fakeStream = xulGetAssets("fakeData/category_variety.xml")
//            "animation" -> fakeStream = xulGetAssets("fakeData/category_animation.xml")
        }
        val dataNode: XulDataNode = XulDataNode.build(fakeStream)
        val ownerSlider = mVideoListView?.findParentByType("slider")
        XulSliderAreaWrapper.fromXulView(ownerSlider).scrollTo(0, false)
        mVideoListWrapper?.clear()
        mVideoListView?.setStyle("display", "block")
        mVideoListView?.resetRender()
        var videoNode: XulDataNode? = dataNode.getChildNode("l","il").firstChild
        while (videoNode != null) {
            mVideoListWrapper?.addItem(videoNode)
            videoNode = videoNode.next
        }
        mVideoListWrapper?.syncContentView()
    }

    private fun openPlayer() {
        XulLog.i("VideoListBehavior", "openPlayer()")
    }

    private fun xulGetAssets(path: String): InputStream? {
        try {
            return _presenter.xulGetContext().getAssets().open(path, Context.MODE_PRIVATE)
        } catch (e: IOException) {
        }
        return null
    }
}