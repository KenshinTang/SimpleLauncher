package com.kapplication.launcher.provider

class CommonCacheId {

    companion object {

        private const val CACHE_ID_BASE = 0x2000

        const val CACHE_ID_PROVIDER_MEMORY = CACHE_ID_BASE + 1
        const val CACHE_ID_PROVIDER_PERSISTENT = CACHE_ID_BASE + 2
    }
}
