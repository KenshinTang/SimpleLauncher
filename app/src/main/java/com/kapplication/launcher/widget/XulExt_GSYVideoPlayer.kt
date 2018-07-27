package com.kapplication.launcher.widget

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Selection
import android.text.Spannable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.TypedValue
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RelativeLayout

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.starcor.xul.IXulExternalView
import com.starcor.xul.Prop.XulAttr
import com.starcor.xul.Prop.XulStyle
import com.starcor.xul.Utils.XulPropParser
import com.starcor.xul.XulLayout
import com.starcor.xul.XulUtils
import com.starcor.xul.XulView


class XulExt_GSYVideoPlayer(context: Context) : StandardGSYVideoPlayer(context), IXulExternalView {

    override fun extMoveTo(x: Int, y: Int, width: Int, height: Int) {
        val layoutParams = this.layoutParams as FrameLayout.LayoutParams
        layoutParams.leftMargin = x
        layoutParams.topMargin = y
        layoutParams.width = width
        layoutParams.height = height
        this.requestLayout()
    }

    override fun extMoveTo(rect: Rect) {
        extMoveTo(rect.left, rect.top, rect.width(), rect.height())
    }

    override fun extOnKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> return dispatchKeyEvent(event)
        }
        return false
    }

    override fun extOnFocus() {
        this.requestFocus()
    }

    override fun extOnBlur() {
        this.clearFocus()
    }

    override fun extShow() {
        this.visibility = View.VISIBLE
    }

    override fun extHide() {
        this.visibility = View.GONE
    }

    override fun extDestroy() {}

    override fun getAttr(key: String, defVal: String): String {
        return defVal
    }

    override fun setAttr(key: String, `val`: String): Boolean {
        return false
    }

    override fun extSyncData() {

    }
}
