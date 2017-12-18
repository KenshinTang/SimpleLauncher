package com.kapplication.launcher

import com.kapplication.launcher.behavior.MainBehavior
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
class UiManager {
    private val _uiPages = ArrayList<UiPageInfo>()

    init {
        addUiPage("MainPage", "xul_main_page.xml", MainBehavior.NAME, MainActivity::class)
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior))
    }

    fun addUiPage(pageId: String, xulFile: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile))
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String, pageClass: KClass<*>) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior, pageClass))
    }

    companion object {
        var messageMonitor: Any? = null
        var activities: ArrayList<WeakReference<XulPresenter>>? = null

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
    }

    internal class UiPageInfo {
        var pageId: String
        var xulFile: String
        var behavior: String? = null
        lateinit var pageClass: KClass<*>

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