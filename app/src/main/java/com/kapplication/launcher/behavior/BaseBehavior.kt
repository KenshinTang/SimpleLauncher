package com.kapplication.launcher.behavior

import com.kapplication.launcher.provider.BaseProvider
import com.starcor.xul.Script.IScriptArguments
import com.starcor.xul.Script.IScriptContext
import com.starcor.xul.ScriptWrappr.Annotation.ScriptMethod
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.model.XulDataCallback
import com.starcor.xulapp.model.XulDataService
import com.starcor.xulapp.utils.XulLog
import okhttp3.OkHttpClient

abstract class BaseBehavior(xulPresenter: XulPresenter) : XulUiBehavior(xulPresenter) {
    protected val okHttpClient: OkHttpClient = OkHttpClient()

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

    @ScriptMethod("refreshBindingByView")
    fun _script_refreshBindingByView(ctx: IScriptContext, args: IScriptArguments): Boolean? {
        if (args.size() != 2) {
            return java.lang.Boolean.FALSE
        }

        val bindingId = args.getString(0)
        val argView = args.getScriptableObject(1)
        if (bindingId == null || argView == null) {
            return java.lang.Boolean.FALSE
        }

        val xulView = argView.objectValue.unwrappedObject as XulView
        if (xulView.bindingData != null && !xulView.bindingData!!.isEmpty()) {
            _xulRenderContext.refreshBinding(
                    bindingId, xulView.bindingData!![0].makeClone())
            return java.lang.Boolean.TRUE
        } else {
            return java.lang.Boolean.FALSE
        }
    }
}
