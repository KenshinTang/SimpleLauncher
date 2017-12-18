package com.kapplication.launcher

import com.kapplication.launcher.behavior.MainBehavior
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by Kenshin on 2017/12/18.
 */
class UiManager {
    private val _uiPages = ArrayList<UiPageInfo>()

    init {
        addUiPage("MainPage", "xul_main_page.xml", MainBehavior.NAME, MainActivity::class)
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior))
    }

    fun addUiPage(pageId: String, xulFile: String) {
        _uiPages.add(UiPageInfo(pageId, xulFile))
    }

    fun addUiPage(pageId: String, xulFile: String, behavior: String, pageClass: KClass<*>) {
        _uiPages.add(UiPageInfo(pageId, xulFile, behavior, pageClass))
    }

    internal class UiPageInfo {
        var pageId: String
        var xulFile: String
        var behavior: String? = null
        lateinit var pageClass: KClass<*>

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

        constructor(pageId: String, xulFile: String, behavior: String, pageClass: KClass<*>) {
            this.pageId = pageId
            this.xulFile = xulFile
            this.behavior = behavior
            this.pageClass = pageClass
        }
    }
}