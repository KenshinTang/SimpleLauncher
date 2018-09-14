package com.kapplication.launcher.widget

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ServiceConnection
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import com.kapplication.launcher.R
import com.starcor.xul.*
import com.starcor.xul.Utils.XulPropParser
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.debug.XulDebugMonitor
import com.starcor.xulapp.debug.XulDebugServer
import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.utils.XulLog
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

/**
 *
 */
abstract class XulBaseDialog : Dialog, XulPresenter {

    protected val TAG = javaClass.simpleName

    var _xulRenderContext: XulRenderContext? = null
    private var _xulView: FrameLayout? = null
    private var _xulDrawingView: View? = null
    private var _xulHandleKeyEvent = false
    private var _blurBkg = false
    private var _xulBlurView: View? = null
    private var _clipPath: Path? = null
    private var _clipPaint: Paint? = null
    private var _pageId: String? = null
    private val _dbgMonitor: XulDebugMonitor?
    private var mContext: Context

    protected var serviceConnection: ServiceConnection? = null


    constructor(context: Context, pageId: String) : super(context, R.style.dialogNoTitle) {
        _dbgMonitor = XulDebugServer.getMonitor()
        mContext = context
        init(pageId)
    }

    constructor(context: Context, pageId: String, them: Int) : super(context, them) {
        _dbgMonitor = XulDebugServer.getMonitor()
        mContext = context
        init(pageId)
    }

    private fun init(pageId: String) {
        ownerActivity = (mContext as Activity?)!!
        initXul(pageId, true)
        _doBlurBkg()
        _dbgMonitor?.onPageCreate(this)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (handleMotionEvent(event)) {
            true
        } else super.onTouchEvent(event)
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        return if (handleMotionEvent(event)) {
            true
        } else super.onTrackballEvent(event)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        return if (handleMotionEvent(event)) {
            true
        } else super.onGenericMotionEvent(event)
    }

    private fun handleMotionEvent(event: MotionEvent): Boolean {
        return if (_xulRenderContext != null && _xulRenderContext!!.onMotionEvent(event)) {
            true
        } else false
    }

    override fun dismiss() {
        _destroyBlurView()
        XulMessageCenter.getDefault().unregister(this)
        try {
            super.dismiss()
        } catch (e: Exception) {
            XulLog.w(TAG, e)
        }

    }

    override fun show() {
        if (_xulBlurView != null) {
            _xulBlurView!!.visibility = View.VISIBLE
        }

        XulMessageCenter.getDefault().register(this)

        try {
            super.show()
        } catch (e: Exception) {
            XulLog.w(TAG, e)
        }

        _dbgMonitor?.onPageResumed(this)

    }

    override fun hide() {
        if (_xulBlurView != null) {
            _xulBlurView!!.visibility = View.GONE
        }
        super.hide()
    }

    override fun onStop() {
        XulLog.i(TAG, "onStop")
        if (_dbgMonitor != null) {
            _dbgMonitor.onPageStopped(this)
            _dbgMonitor.onPageDestroy(this)
        }
        _destroyBlurView()

        if (null != _xulRenderContext) {
            _xulRenderContext!!.destroy()
        }

        super.onStop()
    }

    private fun _destroyBlurView() {
        if (_xulBlurView != null) {
            ownerActivity!!.windowManager.removeViewImmediate(_xulBlurView)
            _xulBlurView = null
        }
    }

    fun initXul(pageId: String, handleKeyEvent: Boolean) {
        _pageId = pageId
        _xulHandleKeyEvent = handleKeyEvent
        _xulDrawingView = object : View(context) {
            internal var _drawingRc = Rect()
            internal var _refreshBegin: Long = 0

            internal fun beginClip(canvas: Canvas) {
                if (_clipPath != null) {
                    canvas.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
                }
            }

            internal fun endClip(canvas: Canvas) {
                if (_clipPath != null) {
                    canvas.drawPath(_clipPath!!, _clipPaint!!)
                    canvas.restore()
                }

                _dbgMonitor?.onPageRefreshed(this@XulBaseDialog, XulUtils.timestamp_us() - _refreshBegin)

            }

            override fun onDraw(canvas: Canvas) {
                if (_dbgMonitor != null) {
                    _refreshBegin = XulUtils.timestamp_us()
                }

                if (_xulRenderContext != null) {
                    var drawingRc: Rect? = canvas.clipBounds
                    if (drawingRc == null || (drawingRc.left == 0 && drawingRc.top == 0
                                    && drawingRc.right == 0 && drawingRc.bottom == 0)) {
                        this.getDrawingRect(_drawingRc)
                        drawingRc = _drawingRc
                    }
                    beginClip(canvas)
                    if (_xulRenderContext!!.beginDraw(canvas, drawingRc)) {
                        _xulRenderContext!!.endDraw()
                        endClip(canvas)
                        return
                    }
                    endClip(canvas)
                }
                super.onDraw(canvas)
            }
        }

        _xulDrawingView!!.isFocusable = handleKeyEvent
        _xulDrawingView!!.isFocusableInTouchMode = handleKeyEvent
        _xulView = object : FrameLayout(context!!) {
            override fun onFocusChanged(gainFocus: Boolean, direction: Int,
                                        previouslyFocusedRect: Rect?) {
                super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
                if (!gainFocus) {
                    XulLog.d(TAG, "focus lost!!!!")
                    post {
                        XulLog.d(TAG, "update focus!!!!")
                        val view = findFocus()
                        var xulView: IXulExternalView? = null
                        if (view != null) {
                            if (view is IXulExternalView) {
                                xulView = view
                            } else {
                                var vp: ViewParent? = view.parent
                                while (vp != null && vp !is IXulExternalView) {
                                    vp = vp.parent
                                }
                                if (vp != null) {
                                    xulView = vp as IXulExternalView?
                                }
                            }
                        }
                        if (xulView != null) {
                            val customItemByExtView = _xulRenderContext!!.findCustomItemByExtView(xulView)
                            _xulRenderContext!!.layout.requestFocus(customItemByExtView)
                        }
                    }
                }
            }

        }
        _xulView!!.isFocusable = handleKeyEvent
        _xulView!!.isFocusableInTouchMode = handleKeyEvent
        _xulView!!.addView(_xulDrawingView)

        _xulRenderContext = XulManager.createXulRender(pageId, object : XulRenderContext.IXulRenderHandler {
            internal var _handler = Handler()

            override fun invalidate(rect: Rect?) {
                if (_xulDrawingView == null) {
                    return
                }
                if (rect == null) {
                    _xulDrawingView!!.invalidate()
                } else {
                    _xulDrawingView!!.invalidate(rect)
                }
            }

            override fun uiRun(runnable: Runnable) {
                _handler.post(runnable)
            }

            override fun uiRun(runnable: Runnable, delayMS: Int) {
                _handler.postDelayed(runnable, delayMS.toLong())
            }

            override fun createExternalView(cls: String, x: Int, y: Int, width: Int,
                                            height: Int, view: XulView): IXulExternalView? {
                return null
            }

            override fun resolve(item: XulWorker.DownloadItem, path: String): String? {
                return null
            }

            override fun getAssets(item: XulWorker.DownloadItem, path: String): InputStream? {
                return null
            }

            override fun getAppData(item: XulWorker.DownloadItem, path: String): InputStream? {
                return null
            }

            override fun getSdcardData(item: XulWorker.DownloadItem, path: String): InputStream? {
                return null
            }

            override fun onRenderIsReady() {
                xulOnRenderIsReady()
            }

            override fun onRenderEvent(eventId: Int, param1: Int, param2: Int, msg: Any?) {

            }

            override fun onDoAction(view: XulView?, action: String?, type: String?,
                                    command: String?, userdata: Any?) {
                if (xulDoAction(view, action, type, command, userdata)) {
                    XulLog.i(TAG, "onDoAction")
                    return
                }
            }
        })

        val layout = _xulRenderContext!!.layout
        if ("true" == layout.getAttrString("blur-bkg")) {
            _blurBkg = true
        }

        super.setContentView(_xulView!!)
        if (null != _xulRenderContext) {
            val focusRect = layout.focusRc

            val borderStyle = layout.getStyle("border")
            if (borderStyle != null) {
                val border = borderStyle.getParsedValue<XulPropParser.xulParsedStyle_Border>()
                if (border!!.xRadius > 1 && border.yRadius > 1) {
                    val path = Path()
                    _clipPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    _clipPaint!!.color = Color.WHITE
                    _clipPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                    val ownerPage = layout.ownerPage
                    path.addRoundRect(RectF(focusRect),
                            ownerPage.xScalar * border.xRadius,
                            ownerPage.yScalar * border.yRadius,
                            Path.Direction.CCW)
                    _clipPath = path
                }
            }

            val layoutParams = _xulDrawingView!!.layoutParams
            layoutParams.width = focusRect.width().toInt()
            layoutParams.height = focusRect.height().toInt()

            val win = this.window
            val params = win!!.attributes
            params.width = layoutParams.width
            params.height = layoutParams.height
            win.attributes = params

            val lp2 = _xulView!!.layoutParams
            lp2.width = ViewGroup.LayoutParams.WRAP_CONTENT
            lp2.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (_xulHandleKeyEvent && _xulRenderContext != null) {
            if (_xulRenderContext!!.onKeyEvent(event)) {
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    protected fun xulOnRenderIsReady() {
        _dbgMonitor?.onPageRenderIsReady(this)
    }

    private fun _doBlurBkg() {
        if (_blurBkg) {
            val ownerActivity = ownerActivity
            if (ownerActivity != null) {
                val decorView = ownerActivity.window.decorView
                val marker = XulUtils.ticketMarker("blur ", false)

                marker.mark()
                var scaledBitmap: Bitmap? = null
                val width = decorView.width / 8
                val height = decorView.height / 8
                run {
                    scaledBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(scaledBitmap)

                    canvas.scale(0.125f, 0.125f)
                    canvas.save()
                    decorView.draw(canvas)
                    canvas.restore()
                    canvas.setBitmap(null)
                }

                val buf = ByteBuffer.allocateDirect(scaledBitmap!!.byteCount)

                scaledBitmap?.copyPixelsToBuffer(buf)
                marker.mark("init")
                XulUtils.doBlurOnBuffer(buf, width, height, 2)
                marker.mark("blur")

                buf.rewind()
                scaledBitmap?.copyPixelsFromBuffer(buf)
                marker.mark("cpyPXBack")
                XulLog.d(TAG, marker.toString())

                _xulBlurView = View(ownerActivity)
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.x = 0
                layoutParams.y = 0
                ownerActivity.windowManager.addView(_xulBlurView, layoutParams)
                _xulBlurView!!.setBackgroundDrawable(BitmapDrawable(scaledBitmap))
                _xulBlurView!!.visibility = View.GONE
            }
        }
    }

    protected open fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?,
                                   userdata: Any?): Boolean {
        XulLog.i(TAG, "xulDoAction action=" + action + " type=" + type + " command=" + command
                + " userdata=" + userdata)
        return false
    }

    fun refreshBinding(bindingId: String, dataNode: XulDataNode?) {
        if (TextUtils.isEmpty(bindingId) || dataNode == null) {
            return
        }
        if (_xulRenderContext != null) {
            _xulRenderContext!!.refreshBinding(bindingId, dataNode)
        }
    }

    override fun xulGetContext(): Context {
        return getContext()
    }

    override fun xulGetIntentPageId(): String? {
        return _pageId
    }

    override fun xulGetCurPageId(): String? {
        return _pageId
    }

    override fun xulGetIntentLayoutFile(): String {
        return ""
    }

    override fun xulGetCurLayoutFile(): String {
        return ""
    }

    override fun xulGetCurBehaviorName(): String {
        return ""
    }

    override fun xulGetRenderContext(): XulRenderContext? {
        return _xulRenderContext
    }

    override fun xulGetRenderContextView(): FrameLayout? {
        return _xulView
    }

    override fun xulLoadLayoutFile(layoutFile: String) {

    }

    override fun xulDefaultDispatchKeyEvent(event: KeyEvent): Boolean {
        return super.dispatchKeyEvent(event)
    }

    override fun xulDefaultDispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event)
    }

    override fun xulGetBehaviorParams(): Bundle? {
        return null
    }

    override fun xulDestroy() {
        dismiss()
    }

    override fun xulIsAlive(): Boolean {
        return isShowing
    }


    fun inputKeyEvent(keyCode: Int, delay: Int) {
        Handler().postDelayed({
            try {
                val builder = ProcessBuilder("input", "keyevent", keyCode.toString())
                builder.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, delay.toLong())
    }

}
