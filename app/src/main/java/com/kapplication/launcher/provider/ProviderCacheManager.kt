package com.kapplication.launcher.provider


import com.starcor.xul.XulDataNode
import com.starcor.xulapp.cache.XulCacheCenter
import com.starcor.xulapp.cache.XulCacheDomain
import java.util.*

/**
 * Created by hy on 2015/10/13.
 */
class ProviderCacheManager {

    private var mMemoryCache: XulCacheDomain? = null
    private var mPersistentCache: XulCacheDomain? = null

    private var mTmpCache: HashMap<String, Any> = HashMap()

    companion object {
        private var sInstance = ProviderCacheManager()

        const val KEYBOARD_TYPE = "keyboard_type"

        fun persistentString(key: String, value: String) {
            sInstance.mPersistentCache!!.put(key, value)
        }

        fun loadPersistentString(key: String): String? {
            return sInstance.mPersistentCache!!.getAsString(key)
        }

        fun loadPersistentString(key: String, defValue: String): String? {
            return sInstance.mPersistentCache!!.getAsString(key) ?: return defValue
        }

        fun persistentXulDataNode(key: String, value: XulDataNode) {
            sInstance.mPersistentCache!!.put(key, value)
        }

        fun persistentMap(key: String, value: Map<*, *>) {
            sInstance.mPersistentCache!!.put(key, value)
        }

        fun loadPersistentMap(key: String): Map<*, *> {
            return sInstance.mPersistentCache!!.getAsObject(key) as Map<*, *>
        }

        fun loadPersistentXulDataNode(key: String): XulDataNode {
            return sInstance.mPersistentCache!!.getAsObject(key) as XulDataNode
        }

        fun loadPersistentList(key: String): List<Any> {
            return sInstance.mPersistentCache!!.getAsObject(key) as List<Any>
        }

        fun cacheString(key: String, value: String) {
            //sInstance.mMemoryCache.put(key, val, MEM_CACHE_LIFETIME);
            sInstance.mTmpCache[key] = value
        }

        fun getCachedString(key: String): String {
            // return sInstance.mMemoryCache.getAsString(key);
            return sInstance.mTmpCache[key] as String
        }

        fun cacheObject(key: String, value: Any) {
            // sInstance.mMemoryCache.put(key, val, MEM_CACHE_LIFETIME);
            sInstance.mTmpCache.put(key, value)
        }

        fun getCachedObject(key: String): Any? {
            // return (T) sInstance.mMemoryCache.getAsObject(key);
            return sInstance.mTmpCache[key]
        }

        fun savePersistentObject(key: String, value: Any) {
            sInstance.mPersistentCache!!.put(key, value)
        }

        fun loadPersistentObject(key: String): Any? {
            return sInstance.mPersistentCache!!.getAsObject(key)
        }

        fun removePersistentCache(key: String) {
            sInstance.mPersistentCache!!.remove(key)
        }
    }

    init {
        mMemoryCache = XulCacheCenter
                .buildCacheDomain(CommonCacheId.CACHE_ID_PROVIDER_MEMORY)
                .setDomainFlags(XulCacheCenter.CACHE_FLAG_MEMORY)
                .setMaxMemorySize((2048 * 1024).toLong())
                .build()

        mPersistentCache = XulCacheCenter
                .buildCacheDomain(CommonCacheId.CACHE_ID_PROVIDER_PERSISTENT)
                .setDomainFlags(XulCacheCenter.CACHE_FLAG_VERSION_LOCAL
                        or XulCacheCenter.CACHE_FLAG_PERSISTENT
                        or XulCacheCenter.CACHE_FLAG_PROPERTY)
                .build()
    }
}
