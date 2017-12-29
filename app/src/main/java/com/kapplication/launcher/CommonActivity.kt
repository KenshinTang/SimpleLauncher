package com.kapplication.launcher

import android.os.Bundle
import android.text.TextUtils
import com.kapplication.launcher.behavior.EpgBehavior
import com.starcor.xul.IXulExternalView
import com.starcor.xul.XulView
import com.starcor.xulapp.XulBaseActivity
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.message.XulMessageCenter
import java.lang.ref.WeakReference

open class CommonActivity : XulBaseActivity() {

    companion object {
        val PAGE_MAIN = "EpgPage"
        val BEHAVIOR_MAIN = EpgBehavior.NAME
    }

    override fun xulOnInitXulBehavior(behavior: String) {
        val intentPageId = xulGetIntentPageId()
        val isSplashPage = TextUtils.isEmpty(intentPageId) || intentPageId == PAGE_MAIN
        val behaviorName = if (isSplashPage && TextUtils.isEmpty(behavior)) BEHAVIOR_MAIN else behavior
        super.xulOnInitXulBehavior(behaviorName)
    }

    override fun xulCreatePage(pageId: String) {
        val pageName = if (TextUtils.isEmpty(pageId)) PAGE_MAIN else pageId
        super.xulCreatePage(pageName)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        XulMessageCenter.getDefault().register(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_ACTIVITY_CREATED)
                .setData(PageEventInfo.obtain(this))
                .post()
    }

    override fun onResume() {
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_ACTIVITY_RESUMED)
                .setData(PageEventInfo.obtain(this))
                .post()
        super.onResume()
    }

    override fun onStop() {
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_ACTIVITY_STOPPED)
                .setData(PageEventInfo.obtain(this))
                .post()
        super.onStop()
    }

    override fun onDestroy() {
        XulMessageCenter.buildMessage()
                .setTag(CommonMessage.EVENT_ACTIVITY_DESTROYED)
                .setData(PageEventInfo.obtain(this))
                .post()
        XulMessageCenter.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun xulCreateExternalView(cls: String, x: Int, y: Int, width: Int, height: Int, view: XulView): IXulExternalView? {
        val externalView = super.xulCreateExternalView(cls, x, y, width, height, view)
        if (externalView != null) {
            return externalView
        }

        return null
    }

    class PageEventInfo(xulPresenter: XulPresenter, xulFocus: XulView?) {
        val pageId: String
        val intentPageId: String
        val presenter: WeakReference<XulPresenter>
        val focus: WeakReference<XulView>?

        init {
            focus = xulFocus?.weakReference
            pageId = xulPresenter.xulGetCurPageId()
            intentPageId = xulPresenter.xulGetIntentPageId()
            presenter = WeakReference(xulPresenter)
        }

        companion object {

            fun obtain(xulPresenter: XulPresenter): PageEventInfo {
                return PageEventInfo(xulPresenter, null)
            }

            fun obtain(xulPresenter: XulPresenter, focus: XulView): PageEventInfo {
                return PageEventInfo(xulPresenter, focus)
            }
        }
    }
}