package com.kapplication.launcher.render

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextUtils

import com.starcor.xul.Graphics.XulDC
import com.starcor.xul.Graphics.XulDrawable
import com.starcor.xul.Render.Drawer.XulDrawer
import com.starcor.xul.Render.XulImageRender
import com.starcor.xul.Render.XulRenderFactory
import com.starcor.xul.Render.XulViewRender
import com.starcor.xul.XulItem
import com.starcor.xul.XulRenderContext
import com.starcor.xul.XulUtils
import com.starcor.xul.XulView

/**
 * Created by hy on 2015/11/19.
 */
class PlayerSeekBarRender(ctx: XulRenderContext, view: XulView) : XulImageRender(ctx, view as XulItem) {

    internal var _fontMetrics: Paint.FontMetrics? = null
    internal var _textWidth: Int = 0
    internal var _textHeight: Int = 0

    internal var _seekBarPos = 0.0f

    internal var _seekTips: String = ""

    override fun syncData() {
        super.syncData()
    }

    override fun syncTextInfo(recalAutoWrap: Boolean) {
        super.syncTextInfo(recalAutoWrap)
        val textPaint = textPaint
        _fontMetrics = textPaint.fontMetrics
        val rect = Rect()
        textPaint.getTextBounds("00:00:00", 0, 8, rect)
        _textWidth = rect.width()
        _textHeight = rect.height()
    }

    override fun draw(dc: XulDC, rect: Rect, xBase: Int, yBase: Int) {
        super.draw(dc, rect, xBase, yBase)

        if (!TextUtils.isEmpty(_seekTips)) {
            val textPaint = textPaint
            val padding = padding
            val animRect = animRect
            val xOffset = (padding.left + (animRect.width() - padding.left.toFloat() - padding.right.toFloat() - _textWidth.toFloat()) * _seekBarPos).toInt()
            dc.drawText(_seekTips, 0, _seekTips.length, animRect.left + xOffset.toFloat() + xBase.toFloat() + _screenX.toFloat(), animRect.top + yBase.toFloat() + _screenY.toFloat() - _fontMetrics!!.top, textPaint)
        }
    }

    override fun drawImage(dc: XulDC, paint: Paint, imgInfo: XulImageRender.DrawableInfo, bmp: XulDrawable, drawer: XulDrawer, xBase: Int, yBase: Int) {
        val idx = imgInfo.idx
        if (idx == 4) {
            imgInfo.alignX = _seekBarPos
        } else if (idx == 3) {
            val dstRc = animRect
            val scalarY = _scalarY
            val scalarX = _scalarX

            val paddingLeft = XulUtils.roundToInt(imgInfo.paddingLeft * scalarX)
            val paddingRight = XulUtils.roundToInt(imgInfo.paddingRight * scalarX)
            val paddingTop = XulUtils.roundToInt(imgInfo.paddingTop * scalarY)
            val paddingBottom = XulUtils.roundToInt(imgInfo.paddingBottom * scalarY)
            dstRc.left += paddingLeft.toFloat()
            dstRc.top += paddingTop.toFloat()
            dstRc.right -= paddingRight.toFloat()
            dstRc.bottom -= paddingBottom.toFloat()
            XulUtils.offsetRect(dstRc, xBase.toFloat(), yBase.toFloat())
            dstRc.right = dstRc.right * _seekBarPos + dstRc.left * (1.0f - _seekBarPos)

            dc.save()
            dc.clipRect(dstRc)
            super.drawImage(dc, paint, imgInfo, bmp, drawer, xBase, yBase)
            dc.restore()
            return
        }
        super.drawImage(dc, paint, imgInfo, bmp, drawer, xBase, yBase)
    }

    fun setSeekBarPos(percent: Float) {
        _seekBarPos = Math.max(0f, Math.min(percent, 1.0f))
        markDirtyView()
    }

    fun setSeekBarTips(tips: String) {
        _seekTips = tips
        markDirtyView()
    }

    companion object {

        fun register() {
            XulRenderFactory.registerBuilder("item", "seek_bar", object : XulRenderFactory.RenderBuilder() {
                override fun createRender(ctx: XulRenderContext, view: XulView): XulViewRender {
                    assert(view is XulItem)
                    return PlayerSeekBarRender(ctx, view)
                }
            })
        }
    }
}
