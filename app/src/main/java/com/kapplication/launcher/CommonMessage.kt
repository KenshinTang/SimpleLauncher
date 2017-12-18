package com.kapplication.launcher

/**
 * Created by hy on 2015/10/14.
 */
object CommonMessage {
    private val EVENT_ID_BASE = 0x4000
    private val EVENT_CAT_BASE = 0x100000
    @Volatile private var currentBaseMsgId = EVENT_ID_BASE
    @Volatile private var currentCatBase = EVENT_CAT_BASE * 0x20

    val CAT_ACTIVITY = EVENT_CAT_BASE * 0x01
    val EVENT_ACTIVITY_CREATED = CAT_ACTIVITY + 0x001
    val EVENT_ACTIVITY_RESUMED = CAT_ACTIVITY + 0x002
    val EVENT_ACTIVITY_STOPPED = CAT_ACTIVITY + 0x003
    val EVENT_ACTIVITY_DESTROYED = CAT_ACTIVITY + 0x004

    val EVENT_UPDATE_MAIN_PAGE = 0x1003

    @Synchronized
    fun GENERAGE_UNIQUE_MSG_ID(): Int {
        return ++currentBaseMsgId
    }

    @Synchronized
    fun GENERAGE_UNIQUE_MSG_ID(catId: Int): Int {
        return ++currentBaseMsgId + catId
    }

    @Synchronized
    fun GENERAGE_UNIQUE_CAT_ID(): Int {
        currentCatBase += EVENT_CAT_BASE
        return currentCatBase
    }
}
