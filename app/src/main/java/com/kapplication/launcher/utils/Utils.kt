package com.kapplication.launcher.utils

import com.starcor.xul.XulDataNode
import com.starcor.xulapp.utils.XulLog
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

/**
 * Created by Kenshin on 2018/7/19.
 */
class Utils{
    companion object {
        const val HOST = "http://dev.yusihuo.com/assetsapi.php"

        fun printXulDataNode(data: XulDataNode) {
            var xmlSerializer: XmlSerializer? = null
            try {
                xmlSerializer = XmlPullParserFactory.newInstance().newSerializer()
                val stringWriter = StringWriter()

                xmlSerializer!!.setOutput(stringWriter)
                xmlSerializer.startDocument("utf-8", true)
                XulDataNode.dumpXulDataNode(data, xmlSerializer)
                xmlSerializer.endDocument()
                xmlSerializer.flush()

                val info = stringWriter.toString()
                printLongLog(info)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun printLongLog(s: String) {
            var index = 0
            val maxLength = 3000
            var sub: String
            XulLog.i("Utils", "-------------------------------")
            while (index < s.length) {
                if (s.length <= index + maxLength) {
                    sub = s.substring(index)
                } else {
                    sub = s.substring(index, maxLength + index)
                }

                index += maxLength
                XulLog.i("Utils", sub)
            }
            XulLog.i("Utils", "-------------------------------")
        }
    }

}