package com.kapplication.launcher

import com.kapplication.launcher.behavior.MainBehavior
import java.util.*

/**
 * Created by Kenshin on 2017/12/18.
 */
class UiManager {
    private val _uiPages = ArrayList<UiPageInfo>()

    init {
        addUiPage("TestMainPage", "xul_main_page.xml", MainBehavior.NAME)
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior))
    }

    fun addUiPage(pageId: String, xulFile: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile))
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String, pageClass: Class<*>) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior, pageClass))
    }

    internal class UiPageInfo {
        var pageId: String
        var xulFile: String
        var behavior: String? = null
        lateinit var pageClass: Class<*>

        constructor(pageId: String, xulFile: String) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = null
        }

        constructor(pageId: String, xulFile: String, behavior: String) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = behavior
        }

        constructor(pageId: String, xulFile: String, behavior: String, pageClass: Class<*>) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = behavior
            this.pageClass = pageClass
        }
    }
}