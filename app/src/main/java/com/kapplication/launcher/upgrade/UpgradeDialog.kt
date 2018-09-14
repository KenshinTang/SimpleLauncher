package com.kapplication.launcher.upgrade

import android.content.Context
import android.content.DialogInterface
import com.kapplication.launcher.widget.XulBaseDialog
import com.starcor.xul.XulView
import com.starcor.xulapp.utils.XulLog

/**
 * Copyright (C) 2015 北京视达科科技有限公司 <br></br>
 * All rights reserved. <br></br>
 * author:  xuan.luo <br></br>
 * date:  16-1-7 上午9:33 <br></br>
 * description:
 */

class UpgradeDialog(context: Context, pageId: String) : XulBaseDialog(context, pageId) {

    private var okListener: DialogInterface.OnClickListener? = null
    private var cancelListener: DialogInterface.OnClickListener? = null

    init {
        init()
    }

    private fun init() {
        _xulRenderContext?.findItemById("version_name")?.setAttr("text", UpgradeUtils.instance.getNewVersionName())
    }

    fun setOkBtnClickListener(listener: DialogInterface.OnClickListener) {
        okListener = listener
    }

    fun setCancelBtnCilckListener(listener: DialogInterface.OnClickListener) {
        cancelListener = listener
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?): Boolean {
        XulLog.d("", "xulDoAction, action=%s, type=%s, command=%s, userdata=%s", action, type, command, userdata)
        if ("click" == action) {
            if ("upgrade_immediately" == command) {
                dismiss()
                if (okListener != null) {
                    okListener!!.onClick(this, 0)
                }
                return true
            } else if ("upgrade_next" == command) {
                dismiss()
                if (cancelListener != null) {
                    cancelListener!!.onClick(this, 1)
                }
                return true
            }
        }
        return super.xulDoAction(view, action, type, command, userdata)
    }
}
