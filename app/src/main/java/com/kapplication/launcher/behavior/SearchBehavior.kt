package com.kapplication.launcher.behavior

import com.starcor.xul.XulArea
import com.starcor.xul.XulView
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog

class SearchBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {

    companion object {
        const val NAME = "SearchBehavior"
        const val KEYBOARD_NINE = "nine"
        const val KEYBOARD_FULL = "full"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return MediaDetailBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return SearchBehavior::class.java
                        }
                    })
        }
    }

    private var mXulSearchBox: XulView? = null
    private var mXulHotWordsPanel: XulView? = null
    private var mXulHotWordsSlider: XulView? = null
    private var mXulSearchResultPanel: XulView? = null
    private var mXulSearchResultSlider: XulView? = null
    private var mXulSearchResult: XulView? = null
    private var mCurrentPage: XulView? = null
    private var mPageNum: XulView? = null
    private var mXulHotWordArea: XulView? = null
    private var keyboardT9Area: XulView? = null
    private var keyboardFullArea: XulView? = null
    private var itemT9Keyboard: XulView? = null
    private var itemFullKeyboard: XulView? = null
    private var mAreaFullLetterPad: XulArea? = null

    private var currKeyType: String? = ""

    override fun appOnStartUp(success: Boolean) {
    }

    override fun xulOnRenderIsReady() {
        super.xulOnRenderIsReady()

        initViews()
        showKeyboard(KEYBOARD_FULL)
    }

    private fun initViews() {
        mXulSearchBox = _xulRenderContext.findItemById("item_search_text")
        mXulSearchResultPanel = _xulRenderContext.findItemById("area_search_result")
        mXulSearchResultSlider = _xulRenderContext.findItemById("area_result_slider")
        mXulSearchResult = _xulRenderContext.findItemById("area_result_massive")
        mXulHotWordsPanel = _xulRenderContext.findItemById("area_hot_search")
        mXulHotWordsSlider = _xulRenderContext.findItemById("area_hot_search_result")
        mCurrentPage = _xulRenderContext.findItemById("item_page_index")
        mPageNum = _xulRenderContext.findItemById("item_page_count")
        _xulRenderContext.layout.initFocus()
        mXulHotWordArea = _xulRenderContext.findItemById("area_hot_search_result")
        keyboardT9Area = _xulRenderContext.findItemById("area_t9_keyboard")
        keyboardFullArea = _xulRenderContext.findItemById("area_full_keyboard")
        itemT9Keyboard = _xulRenderContext.findItemById("item_t9_keyboard")
        itemFullKeyboard = _xulRenderContext.findItemById("item_full_keyboard")
        mAreaFullLetterPad = _xulRenderContext.findItemById("area_full_letter_pad") as XulArea
    }

    private fun showKeyboard(type: String) {
        if (KEYBOARD_NINE == type) {
            keyboardT9Area?.removeClass("hide")
            keyboardFullArea?.addClass("hide")
            itemT9Keyboard?.addClass("keyboard_switch_btn_checked")
            itemFullKeyboard?.removeClass("keyboard_switch_btn_checked")
            currKeyType = KEYBOARD_NINE
        } else if (KEYBOARD_FULL == type) {
            keyboardT9Area?.addClass("hide")
            keyboardFullArea?.removeClass("hide")
            itemT9Keyboard?.removeClass("keyboard_switch_btn_checked")
            itemFullKeyboard?.addClass("keyboard_switch_btn_checked")
            currKeyType = KEYBOARD_FULL
        }
        keyboardT9Area?.resetRender()
        keyboardFullArea?.resetRender()
        itemT9Keyboard?.resetRender()
        itemFullKeyboard?.resetRender()
    }

    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i("SearchBehavior", "action = $action, type = $type, command = $command, userdata = $userdata")

        when (command) {
            "keyboard_nine_click" -> showKeyboard(KEYBOARD_NINE)
            "keyboard_full_click" -> showKeyboard(KEYBOARD_FULL)
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

}