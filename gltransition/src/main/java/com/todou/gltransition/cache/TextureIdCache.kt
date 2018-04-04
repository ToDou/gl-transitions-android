package com.todou.gltransition.cache


import java.util.HashMap

class TextureIdCache private constructor() {
    private val mIdsMap: HashMap<Int, Int> = HashMap()

    fun getTextureId(key: Int): Int {
        return if (mIdsMap == null) 0 else mIdsMap[key] ?: return 0
    }

    fun addIdToCache(key: Int, id: Int) {
        mIdsMap!![key] = id
    }

    companion object {

        @Volatile
        private var sDefaultInstance: TextureIdCache? = null

        val instance: TextureIdCache?
            get() {
                if (sDefaultInstance == null) {
                    synchronized(TextureIdCache::class.java) {
                        if (sDefaultInstance == null) {
                            sDefaultInstance = TextureIdCache()
                        }
                    }
                }
                return sDefaultInstance
            }
    }

}
