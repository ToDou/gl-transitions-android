package com.todou.gltransition.cache

import android.content.Context
import com.todou.gltransition.model.TransitionType
import com.todou.gltransition.programs.ImageClipShaderProgram
import com.todou.gltransition.programs.ShaderProgram
import java.lang.reflect.Constructor
import java.util.HashMap

class ShaderProgramCache private constructor() {
    private val shaderProgramHashMap: HashMap<String, ShaderProgram> = HashMap()

    fun init(context: Context) {
        for (type in TransitionType.values()) {
            try {
                if (type === TransitionType.SLIDE) {
                    val constructor = type.shaderClass!!.getConstructor(Context::class.java) as Constructor<ShaderProgram>
                    val drawer = constructor.newInstance(context)
                    defaultInstance.shaderProgramHashMap[type.ordinal.toString() + "_0"] = drawer
                    val drawer1 = constructor.newInstance(context)
                    defaultInstance.shaderProgramHashMap[type.ordinal.toString() + "_1"] = drawer1
                } else if (type !== TransitionType.NO) {
                    val constructor = type.shaderClass!!.getConstructor(Context::class.java) as Constructor<ShaderProgram>
                    val drawer = constructor.newInstance(context)
                    defaultInstance.shaderProgramHashMap[type.ordinal.toString()] = drawer
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        try {
            val constructor = ImageClipShaderProgram::class.java.getConstructor(Context::class.java)
            val drawer = constructor.newInstance(context)
            defaultInstance.shaderProgramHashMap[NORMAL_IMAGE_PROGRAM_KEY] = drawer
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getTextureId(key: String): ShaderProgram? {
        return shaderProgramHashMap[key]
    }

    fun addIdToCache(key: String, id: ShaderProgram) {
        shaderProgramHashMap[key] = id
    }

    companion object {

        @Volatile
        private lateinit var defaultInstance: ShaderProgramCache
        var NORMAL_IMAGE_PROGRAM_KEY = "normal_image_program_key"

        val instance: ShaderProgramCache
            get() {
                if (defaultInstance == null) {
                    synchronized(ShaderProgramCache::class.java) {
                        if (defaultInstance == null) {
                            defaultInstance = ShaderProgramCache()
                        }
                    }
                }
                return defaultInstance
            }
    }

}
