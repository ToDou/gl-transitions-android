package com.todou.gltransition.render

import android.content.Context
import android.view.TextureView
import com.todou.gltransition.IPlayerLife
import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.WindowSurface
import com.todou.gltransition.widget.MovieMakerTextureView

class GLThreadRender(var mContext: Context, textureView: TextureView,var mIRendererWorker: IRendererWorker) : Thread("GLThreadRender"), IPlayerLife, TextureRenderer.Renderer {

    protected var mMovieMakerTextureView: MovieMakerTextureView? = null
    var isStop: Boolean = false
        protected set
    var usedTime: Long = 0
    protected var mSumTime: Long = 0
    protected var mIsManual: Boolean = false
    protected var mIsFinish: Boolean = false
    protected var mIsBackGround: Boolean = false
    private var mIsRecording = false
    private val mLock = Any()
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
                    if (!mIsRecording)
                        Thread.sleep(Math.max(0, 1000 / RECORDFPS - (System.currentTimeMillis() - startTime)))//睡眠
                    if (!mIsBackGround)
                        usedTime = usedTime + (if (mIsRecording) 1000 / RECORDFPS else System.currentTimeMillis() - startTime)
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

    fun onPause() {
        mMovieMakerTextureView!!.onPause()
    }

    fun onResume() {
        mMovieMakerTextureView!!.onResume()
    }

    fun onRestart() {
        startUp()
    }

    fun onStop() {
        setBackGround(true)
        stopUp()
    }

    fun onDestroy() {
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


    fun onSurfaceCreated(windowSurface: WindowSurface, eglCore: EglCore) {
        mIRendererWorker.onSurfaceCreated(windowSurface, eglCore)
        mIRendererWorker.onSurfaceChanged(windowSurface, windowSurface.getWidth(), windowSurface.getHeight())
        mTextureViewReadyOk = true
        checkToStart()
    }

    fun onSurfaceChanged(windowSurface: WindowSurface, width: Int, height: Int) {
        mIRendererWorker.onSurfaceChanged(windowSurface, width, height)
    }

    private fun checkToStart() {
        if (!mTextureViewReadyOk || isStop) return
        setManual(false)
        synchronized(mLock) {
            mLock.notify()
        }
    }

    fun onDrawFrame(windowSurface: WindowSurface) {
        isDrawIng = true
        if (!mIsManual) {
            synchronized(mLock) {
                if (isStop) {
                    mLock.notify()
                    return
                }
            }
            mIRendererWorker.drawFrame(mContext, windowSurface, usedTime)
            synchronized(mLock) {
                mLock.notify()
            }
        } else {
            mIRendererWorker.drawFrame(mContext, windowSurface, usedTime)
            checkCountToRequest()
        }
        isDrawIng = false
    }

    override fun onSurfaceDestroy() {
        mIRendererWorker.onSurfaceDestroy()
    }

    companion object {

        private val TAG = "GLThreadRender"

        val DEBUG = BuildConfig.DEBUG
        val RECORDFPS = 29
    }
}
