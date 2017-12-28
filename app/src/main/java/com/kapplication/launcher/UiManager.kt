package com.kapplication.launcher

import android.content.Intent
import android.os.Bundle
import com.kapplication.launcher.behavior.EpgBehavior
import com.kapplication.launcher.behavior.MainBehavior
import com.kapplication.launcher.behavior.MediaPlayerBehavior
import com.kapplication.launcher.behavior.VideoListBehavior
import com.starcor.xul.XulDataNode
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulBaseActivity
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.message.XulSubscriber
import com.starcor.xulapp.utils.XulLog
import java.lang.ref.WeakReference
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by Kenshin on 2017/12/18.
 */
object UiManager {
    var messageMonitor: Any? = null
    var activities: ArrayList<WeakReference<XulPresenter>>? = null
    val uiPages = ArrayList<UiPageInfo>()

    init {
        addUiPage("MainPage", "xul_layouts/pages/xul_main_page.xml", MainBehavior.NAME, MainActivity::class)
        addUiPage("EpgPage", "xul_layouts/pages/xul_epg_page.xml", EpgBehavior.NAME, EpgActivity::class)
        addUiPage("VideoListPage", "xul_layouts/pages/xul_video_list_page.xml", VideoListBehavior.NAME, VideoListActivity::class)
        addUiPage("MediaPlayerPage", "xul_layouts/pages/xul_media_player_page.xml", MediaPlayerBehavior.NAME, MediaPlayerActivity::class)
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String) {
        uiPages.add(UiPageInfo(pageId, xulFile, behavior))
    }

    fun addUiPage(pageId: String, xulFile: String) {
        uiPages.add(UiPageInfo(pageId, xulFile))
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String, pageClass: KClass<*>) {
        uiPages.add(UiPageInfo(pageId, xulFile, behavior, pageClass))
    }

    fun initUiManager() {
        activities = ArrayList<WeakReference<XulPresenter>>()
        messageMonitor = object : Any() {
            @XulSubscriber(tag = CommonMessage.EVENT_ACTIVITY_CREATED)
            fun onActivityCreated(info: CommonActivity.PageEventInfo) {
                XulLog.i("EVENT/Activity/Created", info)
                activities!!.add(info.presenter)
            }

            @XulSubscriber(tag = CommonMessage.EVENT_ACTIVITY_DESTROYED)
            fun onActivityDestroyed(info: CommonActivity.PageEventInfo) {
                XulLog.i("EVENT/Activity/Destroyed", info)
                for (activity in activities!!) {
                    if (activity.get() === info.presenter.get()) {
                        activities!!.remove(activity)
                        break
                    }
                }
            }

            @XulSubscriber(tag = CommonMessage.EVENT_ACTIVITY_RESUMED)
            fun onActivityResumed(info: CommonActivity.PageEventInfo) {
                XulLog.i("EVENT/Activity/Resumed", info)
            }

            @XulSubscriber(tag = CommonMessage.EVENT_ACTIVITY_STOPPED)
            fun onActivityStopped(info: CommonActivity.PageEventInfo) {
                XulLog.i("EVENT/Activity/Stopped", info)
            }
        }
        XulMessageCenter.getDefault().register(messageMonitor)
    }

    fun openUiPage(pageId: String) {
        openUiPage(pageId, null)
    }

    fun openUiPage(pageId: String, extInfo: XulDataNode?) {
        for (info in uiPages) {
            if (info.pageId == pageId) {
                val intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK

                var pageClass: Class<*>? = info.pageClass?.java
                if (pageClass == null) {
                    pageClass = CommonActivity::class.java
                }
                val intent = Intent(XulApplication.getAppContext(), pageClass)
                if (intentFlags >= 0) {
                    intent.addFlags(intentFlags)
                }
                intent.putExtra(XulBaseActivity.XPARAM_PAGE_ID, info.pageId)
                intent.putExtra(XulBaseActivity.XPARAM_LAYOUT_FILE, info.xulFile)
                intent.putExtra(XulBaseActivity.XPARAM_PAGE_BEHAVIOR, info.behavior)

                if (extInfo != null && extInfo.firstChild != null) {
                    var behaviorParams: Bundle? = null
                    var extInfoParam: XulDataNode? = extInfo.firstChild
                    while (extInfoParam != null) {
                        if (behaviorParams == null) {
                            behaviorParams = Bundle()
                        }
                        behaviorParams.putString(extInfoParam.name, extInfoParam.value)
                        extInfoParam = extInfoParam.next
                    }
                    if (behaviorParams != null && !behaviorParams.isEmpty) {
                        intent.putExtra(XulBaseActivity.XPARAM_BEHAVIOR_PARAMS, behaviorParams)
                    }
                }

                XulApplication.getAppContext().startActivity(intent)
                return
            }
        }
    }

    class UiPageInfo {
        var pageId: String
        var xulFile: String
        var behavior: String? = null
        var pageClass: KClass<*>? = null

        constructor(pageId: String, xulFile: String) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = null
        }

        constructor(pageId: String, xulFile: String, behavior: String) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = behavior
        }

        constructor(pageId: String, xulFile: String, behavior: String, pageClass: KClass<*>) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = behavior
            this.pageClass = pageClass
        }
    }
}