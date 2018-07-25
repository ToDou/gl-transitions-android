package com.todou.gltransition.render

import android.content.Context

import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.WindowSurface

interface IRendererWorker {

    fun onSurfaceCreated(windowSurface: WindowSurface, eglCore: EglCore)

    fun onSurfaceChanged(windowSurface: WindowSurface, width: Int, height: Int)

    fun drawFrame(context: Context, windowSurface: WindowSurface, usedTime: Long)

    fun onSurfaceDestroy()

}