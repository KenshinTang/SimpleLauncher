package com.kapplication.launcher.behavior

import android.text.TextUtils
import com.kapplication.launcher.provider.ProviderCacheManager
import com.kapplication.launcher.utils.Utils
import com.starcor.xul.Wrapper.XulMassiveAreaWrapper
import com.starcor.xul.Wrapper.XulSliderAreaWrapper
import com.starcor.xul.XulArea
import com.starcor.xul.XulDataNode
import com.starcor.xul.XulPage
import com.starcor.xul.XulView
import com.starcor.xulapp.XulApplication
import com.starcor.xulapp.XulPresenter
import com.starcor.xulapp.behavior.XulBehaviorManager
import com.starcor.xulapp.behavior.XulUiBehavior
import com.starcor.xulapp.utils.XulLog
import okhttp3.*
import java.io.IOException


class SearchBehavior(xulPresenter: XulPresenter) : BaseBehavior(xulPresenter) {



    companion object {
        const val NAME = "SearchBehavior"
        private const val KEYBOARD_NINE = "nine"
        private const val KEYBOARD_FULL = "full"

        fun register() {
            XulBehaviorManager.registerBehavior(NAME,
                    object : XulBehaviorManager.IBehaviorFactory {
                        override fun createBehavior(
                                xulPresenter: XulPresenter): XulUiBehavior {
                            return SearchBehavior(xulPresenter)
                        }

                        override fun getBehaviorClass(): Class<*> {
                            return SearchBehavior::class.java
                        }
                    })
        }
    }

    private var mXulSearchResultPanel: XulView? = null
    private var mXulSearchResultSlider: XulView? = null
    private var mXulSearchResult: XulView? = null
    private var mXulSearchBox: XulView? = null
    private var mXulSearchResultCount: XulView? = null
    private var mKeyboardT9Area: XulView? = null
    private var mKeyboardFullArea:XulView? = null
    private var mItemT9Keyboard:XulView? = null
    private var mItemFullKeyboard:XulView? = null
    private var mAreaFullLetterPad: XulArea? = null
    private var mVideoListWrapper: XulMassiveAreaWrapper? = null

    private var mCurrentKeyboard: String = KEYBOARD_NINE
    private var mSearchStr: String = ""

    override fun appOnStartUp(success: Boolean) {

    }

    override fun xulOnRenderIsReady() {
        initView()

        val keyboardType = ProviderCacheManager.loadPersistentString(ProviderCacheManager.KEYBOARD_TYPE, KEYBOARD_NINE)
        showKeyboard(keyboardType!!)
        if (KEYBOARD_FULL == keyboardType) {
            mAreaFullLetterPad!!.rootLayout!!.requestFocus(mAreaFullLetterPad!!.firstChild)
            mAreaFullLetterPad!!.resetRender()
        }
    }

    private fun initView() {
        mXulSearchBox = _xulRenderContext.findItemById("item_search_text")
        mXulSearchResultPanel = _xulRenderContext.findItemById("area_search_result")
        mXulSearchResultSlider = _xulRenderContext.findItemById("area_result_slider")
        mXulSearchResult = _xulRenderContext.findItemById("area_result_massive")
        mXulSearchResultCount = _xulRenderContext.findItemById("item_count")
        mKeyboardT9Area = _xulRenderContext.findItemById("area_t9_keyboard")
        mKeyboardFullArea = _xulRenderContext.findItemById("area_full_keyboard")
        mItemT9Keyboard = _xulRenderContext.findItemById("item_t9_keyboard")
        mItemFullKeyboard = _xulRenderContext.findItemById("item_full_keyboard")
        mAreaFullLetterPad = _xulRenderContext.findItemById("area_full_letter_pad") as XulArea
        mVideoListWrapper = XulMassiveAreaWrapper.fromXulView(mXulSearchResult)
        _xulRenderContext.layout.initFocus()
    }


    override fun xulDoAction(view: XulView?, action: String?, type: String?, command: String?, userdata: Any?) {
        XulLog.i(NAME, "action = $action, type = $type, command = $command, userdata = $userdata")
        when (command) {
            "openDetail" -> openDetail(userdata as String)
            "keyboard_nine_click" -> showKeyboard(KEYBOARD_NINE)
            "keyboard_full_click" -> showKeyboard(KEYBOARD_FULL)
            "doSearch" -> doSearch()
        }
        super.xulDoAction(view, action, type, command, userdata)
    }

    private fun doSearch() {
        mSearchStr = mXulSearchBox!!.getAttrString("text")
        if (TextUtils.isEmpty(mSearchStr)) {
            resetSearchPage()
            return
        }

        val urlBuilder = HttpUrl.parse(Utils.HOST)!!.newBuilder()
                .addQueryParameter("m", "Epg")
                .addQueryParameter("c", "AssetCategory")
                .addQueryParameter("a", "getAssetVideoList")
                .addQueryParameter("search_val", mSearchStr)
//                .addQueryParameter("asset_category_id", "100")
                .addQueryParameter("page_num", "1")
                .addQueryParameter("page_size", "9999")

        XulLog.i(NAME, "Request url: ${urlBuilder.build()}")

        val request: Request = Request.Builder().cacheControl(cacheControl).url(urlBuilder.build()).build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                response!!.body().use { responseBody ->
                    if (!response.isSuccessful) {
                        XulLog.e(NAME, "getAssetVideoList onResponse, but is not Successful")
                        throw IOException("Unexpected code $response")
                    }

                    val result : String = responseBody!!.string()
                    val dataNode : XulDataNode = XulDataNode.buildFromJson(result)

                    if (handleError(dataNode)) {
                        XulApplication.getAppInstance().postToMainLooper {
                            XulPage.invokeAction(mXulSearchResultPanel, "appEvents:showEmptyTip")
                        }
                    } else {
                        XulApplication.getAppInstance().postToMainLooper {
                            if (TextUtils.isEmpty(mSearchStr)) {
                                XulLog.d(NAME, "用户已经取消搜索的关键字，不在显示搜索结果。")
                            } else {
                                resetSearchPage()
                                XulSliderAreaWrapper.fromXulView(mXulSearchResultSlider).scrollTo(0, false)
                                mVideoListWrapper?.clear()
                                clearResultDynamicFocus()
                                val listNode: XulDataNode? = dataNode.getChildNode("data", "list")

                                if (listNode?.size()!! <= 0) {
                                    XulPage.invokeAction(mXulSearchResultPanel, "appEvents:showEmptyTip")
                                } else {
                                    XulPage.invokeAction(mXulSearchResultPanel, "appEvents:showSearchResult")
                                    var videoNode: XulDataNode? = listNode.firstChild
                                    while (videoNode != null) {
                                        mVideoListWrapper?.addItem(videoNode)
                                        videoNode = videoNode.next
                                    }

                                    mVideoListWrapper?.syncContentView()

                                    val count = dataNode.getChildNode("data").getAttributeValue("total")
                                    mXulSearchResultCount?.setAttr("text", "${count}部")
                                    mXulSearchResultCount?.resetRender()

                                    val firstView = (mXulSearchResult as XulArea).getChild(0)
                                    (mXulSearchResult as XulArea).dynamicFocus = firstView
                                    (mXulSearchResultPanel as XulArea).dynamicFocus = firstView
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                XulLog.e(NAME, "getAssetCategoryList onFailure")
                XulApplication.getAppInstance().postToMainLooper {
                    XulPage.invokeAction(mXulSearchResultPanel, "appEvents:showEmptyTip")
                }
            }
        })
    }

    private fun resetSearchPage() {
        XulSliderAreaWrapper.fromXulView(mXulSearchResultSlider).scrollTo(0, false)
        clearResultDynamicFocus()

        mXulSearchResultCount?.setAttr("text", "0部")
        mXulSearchResultCount?.resetRender()

        mVideoListWrapper?.clear()
    }

    private fun clearResultDynamicFocus() {
        if (mXulSearchResultSlider != null) {
            (mXulSearchResultSlider as XulArea).dynamicFocus = null
        }

        if (mXulSearchResult != null) {
            (mXulSearchResult as XulArea).dynamicFocus = null
        }
        // 移除栏目动态焦点
        val filmClassifyArea = xulGetRenderContext().findItemById("film_classify_area") as XulArea
        filmClassifyArea.dynamicFocus = null
    }

    private fun showKeyboard(type: String) {
        if (KEYBOARD_NINE == type) {
            mKeyboardT9Area?.removeClass("hide")
            mKeyboardFullArea?.addClass("hide")
            mItemT9Keyboard?.addClass("keyboard_switch_btn_checked")
            mItemFullKeyboard?.removeClass("keyboard_switch_btn_checked")
            mCurrentKeyboard = KEYBOARD_NINE
        } else if (KEYBOARD_FULL == type) {
            mKeyboardT9Area?.addClass("hide")
            mKeyboardFullArea?.removeClass("hide")
            mItemT9Keyboard?.removeClass("keyboard_switch_btn_checked")
            mItemFullKeyboard?.addClass("keyboard_switch_btn_checked")
            mCurrentKeyboard = KEYBOARD_FULL
        }
        mKeyboardT9Area?.resetRender()
        mKeyboardFullArea?.resetRender()
        mItemT9Keyboard?.resetRender()
        mItemFullKeyboard?.resetRender()
    }

    override fun xulOnBackPressed(): Boolean {
        if (!TextUtils.isEmpty(mSearchStr)) {
            mSearchStr = ""
            mXulSearchBox?.setAttr("text", "")
            mXulSearchBox?.resetRender()
            resetSearchPage()

            val layout = _xulRenderContext.layout
            layout.requestFocus(null)
            layout.initFocus()
            return true
        }


        return super.xulOnBackPressed()
    }

    override fun xulOnDestroy() {
        ProviderCacheManager.persistentString(ProviderCacheManager.KEYBOARD_TYPE, mCurrentKeyboard)
    }
}