package com.kapplication.launcher.behavior

import com.kapplication.launcher.CommonMessage
import com.starcor.xul.Prop.XulPropNameCache
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.message.XulSubscriber
import com.starcor.xulapp.utils.XulLog
import com.starcor.xulapp.utils.XulTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by hy on 2015/11/16.
 */
class MainBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    private var clockLabel: XulView? = null
    private val currentDate = Date()
    private val dateFormat = SimpleDateFormat("HH:mm")

    companion object {
        val NAME = "MainBehavior"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MainBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return MainBehavior::class.java
                        }
                    })
        }
    }

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
        clockLabel = xulGetRenderContext().findItemById("clock_label")
    }

    @XulSubscriber(tag = CommonMessage.EVENT_HALF_SECOND)
    private fun onHalfSecondPassed(dummy: Any) {
        XulLog.e("kenshin", "onHalfSecondPassed")
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis / 1000 != currentDate.time / 1000) {
            currentDate.time = currentTimeMillis
            dateFormat.timeZone = XulTime.getTimeZone()
            val curTimeStr = dateFormat.format(currentDate)
            val clockTimeStr = clockLabel?.getAttrString(XulPropNameCache.TagId.TEXT)
            if (clockTimeStr != curTimeStr) {
                clockLabel?.setAttr(XulPropNameCache.TagId.TEXT, curTimeStr)
                clockLabel?.resetRender()
            }
        }
    }
}
