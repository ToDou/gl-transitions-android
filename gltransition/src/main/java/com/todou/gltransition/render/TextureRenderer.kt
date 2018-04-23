package com.todou.gltransition.render

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.os.Message

import com.todou.gltransition.BuildConfig
import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.WindowSurface

import java.lang.ref.WeakReference

class TextureRenderer : Thread("TextureRenderer") {

    private var mEglCore: EglCore = null
    private var mRenderer: Renderer? = null
    private var mWindowSurface: WindowSurface? = null
    @Volatile
    var handler: RenderHandler? = null
        private set
    private val mStartLock = Any()
    private var mReady = false

    fun setRenderer(renderer: Renderer) {
        mRenderer = renderer
    }

    override fun run() {
        Looper.prepare()
        handler = RenderHandler(this)
        synchronized(mStartLock) {
            mReady = true
            mStartLock.notify()
        }
        mEglCore = EglCore(null, 0)
        Looper.loop()
        releaseGl()
        mEglCore!!.release()

        synchronized(mStartLock) {
            mReady = false
        }
    }

    private fun releaseGl() {
        mWindowSurface?.let {
            it.release()
            mWindowSurface = null
        }

        mEglCore?.makeNothingCurrent()
    }

    fun waitUntilReady() {
        synchronized(mStartLock) {
            while (!mReady) {
                try {
                    mStartLock.wait()
                } catch (ie: InterruptedException) {
                }

            }
        }
    }

    fun requestRender() {
        handler!!.sendRedraw()
    }

    private fun draw() {
        if (mWindowSurface == null) return
        mRenderer?.onDrawFrame(mWindowSurface)
    }

    private fun frameAvailable() {

    }

    private fun shutdown() {
        Looper.myLooper()?.quitSafely()
    }

    private fun surfaceDestroyed() {
        mRenderer!!.onSurfaceDestroy()
        releaseGl()
    }

    private fun surfaceChanged(width: Int, height: Int) {
        mRenderer!!.onSurfaceChanged(mWindowSurface, width, height)
    }

    private fun surfaceAvailable(surfaceTexture: SurfaceTexture, b: Boolean) {
        mEglCore = EglCore(null, EglCore.FLAG_RECORDABLE or EglCore.FLAG_TRY_GLES3)
        mWindowSurface = WindowSurface(mEglCore!!, surfaceTexture)
        mWindowSurface?.makeCurrent()

        mRenderer?.onSurfaceCreated(mWindowSurface, mEglCore)
    }

    interface Renderer {
        fun onSurfaceCreated(windowSurface: WindowSurface, eglCore: EglCore)

        fun onSurfaceChanged(windowSurface: WindowSurface, width: Int, height: Int)

        fun onDrawFrame(windowSurface: WindowSurface)

        fun onSurfaceDestroy()
    }

    class RenderHandler(rt: TextureRenderer) : Handler() {

        private val mWeakRenderThread: WeakReference<TextureRenderer>

        init {
            mWeakRenderThread = WeakReference(rt)
        }

        fun sendSurfaceAvailable(st: SurfaceTexture, width: Int, height: Int) {
            sendMessage(obtainMessage(MSG_SURFACE_AVAILABLE, width, height, st))
        }

        fun sendSurfaceChanged(width: Int,
                               height: Int) {
            sendMessage(obtainMessage(MSG_SURFACE_CHANGED, width, height))
        }

        fun sendSurfaceDestroyed() {
            sendMessage(obtainMessage(MSG_SURFACE_DESTROYED))
        }

        fun sendShutdown() {
            sendMessage(obtainMessage(MSG_SHUTDOWN))
        }

        fun sendFrameAvailable() {
            sendMessage(obtainMessage(MSG_FRAME_AVAILABLE))
        }

        fun sendRedraw() {
            sendMessage(obtainMessage(MSG_REDRAW))
        }

        override fun handleMessage(msg: Message) {
            val what = msg.what
            val renderThread = mWeakRenderThread.get() ?: return

            when (what) {
                MSG_SURFACE_AVAILABLE -> renderThread.surfaceAvailable(msg.obj as SurfaceTexture, msg.arg1 != 0)
                MSG_SURFACE_CHANGED -> renderThread.surfaceChanged(msg.arg1, msg.arg2)
                MSG_SURFACE_DESTROYED -> renderThread.surfaceDestroyed()
                MSG_SHUTDOWN -> renderThread.shutdown()
                MSG_FRAME_AVAILABLE -> renderThread.frameAvailable()
                MSG_REDRAW -> renderThread.draw()
                else -> throw RuntimeException("unknown message $what")
            }
        }

        companion object {
            private val MSG_SURFACE_AVAILABLE = 0
            private val MSG_SURFACE_CHANGED = 1
            private val MSG_SURFACE_DESTROYED = 2
            private val MSG_SHUTDOWN = 3
            private val MSG_FRAME_AVAILABLE = 4
            private val MSG_REDRAW = 9
        }
    }

    companion object {
        private val TAG = "TextureRenderer"
        val DEBUG = BuildConfig.DEBUG
    }
}