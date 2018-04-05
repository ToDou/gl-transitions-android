package com.todou.gltransition.cache

import android.content.Context

import java.lang.reflect.Constructor
import java.util.HashMap

class ShaderProgramCache private constructor() {
    private val mShaderProgramHashMap: HashMap<String, ShaderProgram>?

    init {
        mShaderProgramHashMap = HashMap<String, ShaderProgram>()
    }

    fun init(context: Context) {
        for (type in TransitionType.values()) {
            try {
                if (type === TransitionType.SLIDE) {
                    val constructor = type.getShaderClass().getConstructor(Context::class.java)
                    val drawer = constructor.newInstance(context)
                    sDefaultInstance!!.mShaderProgramHashMap!![String.valueOf(type.getValue()) + "_0"] = drawer
                    val drawer1 = constructor.newInstance(context)
                    sDefaultInstance!!.mShaderProgramHashMap!![String.valueOf(type.getValue()) + "_1"] = drawer1
                } else if (type !== TransitionType.NO) {
                    val constructor = type.getShaderClass().getConstructor(Context::class.java)
                    val drawer = constructor.newInstance(context)
                    sDefaultInstance!!.mShaderProgramHashMap!![String.valueOf(type.getValue())] = drawer
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        try {
            val constructor = ImageClipShaderProgram::class.java!!.getConstructor(Context::class.java)
            val drawer = constructor.newInstance(context)
            sDefaultInstance!!.mShaderProgramHashMap!![NORMAL_IMAGE_PROGRAM_KEY] = drawer
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getTextureId(key: String): ShaderProgram? {
        return if (mShaderProgramHashMap == null) null else mShaderProgramHashMap[key]
    }

    fun addIdToCache(key: String, id: ShaderProgram) {
        mShaderProgramHashMap!![key] = id
    }

    companion object {

        @Volatile
        private var sDefaultInstance: ShaderProgramCache? = null
        var NORMAL_IMAGE_PROGRAM_KEY = "normal_image_program_key"

        val instance: ShaderProgramCache?
            get() {
                if (sDefaultInstance == null) {
                    synchronized(ShaderProgramCache::class.java) {
                        if (sDefaultInstance == null) {
                            sDefaultInstance = ShaderProgramCache()
                        }
                    }
                }
                return sDefaultInstance
            }
    }

}