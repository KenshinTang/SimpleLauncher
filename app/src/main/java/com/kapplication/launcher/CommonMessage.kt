package com.kapplication.launcher

object CommonMessage {
    private const val EVENT_ID_BASE = 0x4000
    private const val EVENT_CAT_BASE = 0x100000
    @Volatile private var currentBaseMsgId = EVENT_ID_BASE
    @Volatile private var currentCatBase = EVENT_CAT_BASE * 0x20

    const val CAT_ACTIVITY = EVENT_CAT_BASE * 0x01
    const val EVENT_ACTIVITY_CREATED = CAT_ACTIVITY + 0x001
    const val EVENT_ACTIVITY_RESUMED = CAT_ACTIVITY + 0x002
    const val EVENT_ACTIVITY_STOPPED = CAT_ACTIVITY + 0x003
    const val EVENT_ACTIVITY_DESTROYED = CAT_ACTIVITY + 0x004

    const val EVENT_HALF_SECOND = 0x1001
    const val EVENT_HALF_HOUR = 0x1002
    const val EVENT_TEN_MINUTES = 0x1003

    const val EVENT_SHOW_UPGRADE = 0x2001

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
