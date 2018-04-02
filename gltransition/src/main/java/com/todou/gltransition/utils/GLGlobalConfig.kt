package com.todou.gltransition.utils

import android.util.Log

class GLGlobalConfig private constructor(){

    init { Log.i("GLGlobalConfig", "This ($this) is a singleton") }

    private object Holder { val INSTANCE = GLGlobalConfig() }

    private var logOn: Boolean = false

    companion object {
        val instance: GLGlobalConfig by lazy { Holder.INSTANCE }

        var logOn: Boolean
            get() = instance.logOn
            set(logOn) {
                instance.logOn = logOn
            }
    }
}
