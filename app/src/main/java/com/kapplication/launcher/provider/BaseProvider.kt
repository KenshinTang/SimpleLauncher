package com.kapplication.launcher.provider

import com.starcor.xulapp.message.XulMessageCenter
import com.starcor.xulapp.model.*

/**
 * Created by hy on 2015/9/21.
 */
class BaseProvider : XulDataProvider() {
    private val TAG = "BaseProvider"

    companion object {
        var DP_STARTUP = "startup"
    }

    @Throws(XulDataException::class)
    override fun execClause(ctx: XulDataServiceContext, clauseInfo: XulClauseInfo): XulDataOperation? {
        val action = clauseInfo.verb
        when (action) {
            XulDataService.XVERB_QUERY -> return execQueryClause(ctx, clauseInfo)
            XulDataService.XVERB_UPDATE -> return execUpdateClause(ctx, clauseInfo)
            XulDataService.XVERB_DELETE -> return execDeleteClause(ctx, clauseInfo)
            XulDataService.XVERB_INSERT -> return execInsertClause(ctx, clauseInfo)
        }
        return null
    }

    @Throws(XulDataException::class)
    fun execInsertClause(ctx: XulDataServiceContext, clauseInfo: XulClauseInfo): XulDataOperation? {
        return null
    }

    @Throws(XulDataException::class)
    fun execDeleteClause(ctx: XulDataServiceContext, clauseInfo: XulClauseInfo): XulDataOperation? {
        return null
    }

    @Throws(XulDataException::class)
    fun execUpdateClause(ctx: XulDataServiceContext, clauseInfo: XulClauseInfo): XulDataOperation? {
        return null
    }

    @Throws(XulDataException::class)
    fun execQueryClause(ctx: XulDataServiceContext, clauseInfo: XulClauseInfo): XulDataOperation? {
        return null
    }

    protected fun notifyEvent(event: Int) {
        XulMessageCenter.buildMessage().setTag(event).post()
    }
}
