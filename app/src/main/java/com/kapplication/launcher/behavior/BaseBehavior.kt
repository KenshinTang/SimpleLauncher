package com.kapplication.launcher.behavior

import com.kapplication.launcher.provider.BaseProvider
import com.starcor.xul.XulDataNode
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.model.XulDataCallback
import com.starcor.xulapp.model.XulDataService
import com.starcor.xulapp.utils.XulLog

/**
 * Created by hy on 2015/11/16.
 */
abstract class BaseBehavior(xulPresenter: XulPresenter) : XulUiBehavior(xulPresenter) {

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()
        val ds = xulGetDataService()
        ds.query(BaseProvider.DP_STARTUP)
                .where("restart").`is`("true")
                .exec(object : XulDataCallback() {
                    override fun onResult(clause: XulDataService.Clause, code: Int, data: XulDataNode) {
                        appOnStartUp(true)
                    }

                    override fun onError(clause: XulDataService.Clause, code: Int) {
                        appOnStartUp(false)
                        XulLog.e("kenshin", "start up failed.")
                    }
                })
    }

    protected abstract fun appOnStartUp(success: Boolean)
}
