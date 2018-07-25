package com.todou.gltransition.render

import android.content.Context
import android.view.TextureView
import com.todou.gltransition.BuildConfig
import com.todou.gltransition.IPlayerLife
import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.WindowSurface
import com.todou.gltransition.widget.MovieMakerTextureView

class GLThreadRender(var context: Context, textureView: TextureView, var iRendererWorker: IRendererWorker) : Thread("GLThreadRender"), IPlayerLife, TextureRenderer.Renderer {

    protected var mMovieMakerTextureView: MovieMakerTextureView? = null
    var isStop: Boolean = false
        protected set
    var usedTime: Long = 0
    protected var mSumTime: Long = 0
    protected var mIsManual: Boolean = false
    protected var mIsFinish: Boolean = false
    protected var mIsBackGround: Boolean = false
    private var mIsRecording = false
    private val mLock = Object()
    private var mTextureViewReadyOk = false
    private var mRequestRenderCount = 0
    private var isDrawIng = false

    init {
        mMovieMakerTextureView = textureView as MovieMakerTextureView
        mMovieMakerTextureView!!.setRenderer(this)
        mIsManual = false
        mIsFinish = false
        if (!this.isAlive) {
            this.start()
        }
    }

    fun stopUp() {
        isStop = true
    }

    fun startUp() {
        isStop = false
        checkToStart()
    }

    override fun run() {
        synchronized(mLock) {
            while (!mIsFinish) {
                try {
                    if (usedTime >= mSumTime) {
                        usedTime = mSumTime
                        mLock.wait()
                    }
                    if (isStop) {
                        checkCountToRequest()
                        mLock.wait()
                    }
                    val startTime = System.currentTimeMillis()
                    if (mMovieMakerTextureView != null && !isStop)
                        mMovieMakerTextureView!!.requestRender()
                    mLock.wait()
                    if (!mIsBackGround)
                        usedTime = usedTime + System.currentTimeMillis() - startTime
                    else
                        mIsBackGround = false
                    usedTime = if (usedTime >= mSumTime) mSumTime else usedTime
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            }
        }
    }

    private fun checkCountToRequest() {
        if (mRequestRenderCount > 0 && mMovieMakerTextureView != null && mIsManual) {
            mMovieMakerTextureView!!.requestRender()
            mRequestRenderCount--
        }
    }

    override fun onPause() {
        mMovieMakerTextureView!!.onPause()
    }

    override fun onResume() {
        mMovieMakerTextureView!!.onResume()
    }

    override fun onRestart() {
        startUp()
    }

    override fun onStop() {
        setBackGround(true)
        stopUp()
    }

    override fun onDestroy() {
        mIsFinish = true
        isStop = true
        mMovieMakerTextureView = null
    }

    fun updateTime(start: Long, end: Long) {
        usedTime = start
        mSumTime = end
    }

    fun seekToTime(usedTime: Long) {
        stopUp()
        setManual(true)
        this.usedTime = usedTime
        mRequestRenderCount++
        if (isDrawIng == false && isStop) checkCountToRequest()
    }

    fun requestRender() {
        setManual(true)
        mRequestRenderCount++
        if (isDrawIng == false && isStop) checkCountToRequest()
    }

    fun setManual(isManual: Boolean) {
        this.mIsManual = isManual
    }

    fun setBackGround(isBackGround: Boolean) {
        this.mIsBackGround = isBackGround
    }

    fun setRecording(recording: Boolean) {
        mIsRecording = recording
    }


    override fun onSurfaceCreated(windowSurface: WindowSurface, eglCore: EglCore) {
        iRendererWorker.onSurfaceCreated(windowSurface, eglCore)
        iRendererWorker.onSurfaceChanged(windowSurface, windowSurface.width, windowSurface.height)
        mTextureViewReadyOk = true
        checkToStart()
    }

    override fun onSurfaceChanged(windowSurface: WindowSurface, width: Int, height: Int) {
        iRendererWorker.onSurfaceChanged(windowSurface, width, height)
    }

    private fun checkToStart() {
        if (!mTextureViewReadyOk || isStop) return
        setManual(false)
        synchronized(mLock) {
            mLock.notify()
        }
    }

    override fun onDrawFrame(windowSurface: WindowSurface) {
        isDrawIng = true
        if (!mIsManual) {
            synchronized(mLock) {
                if (isStop) {
                    mLock.notify()
                    return
                }
            }
            iRendererWorker.drawFrame(context, windowSurface, usedTime)
            synchronized(mLock) {
                mLock.notify()
            }
        } else {
            iRendererWorker.drawFrame(context, windowSurface, usedTime)
            checkCountToRequest()
        }
        isDrawIng = false
    }

    override fun onSurfaceDestroy() {
        iRendererWorker.onSurfaceDestroy()
    }

    companion object {

        private val TAG = "GLThreadRender"

        val DEBUG = BuildConfig.DEBUG
        val RECORDFPS = 29
    }
}
