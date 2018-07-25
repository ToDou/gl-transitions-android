package com.todou.gltransition.render

import android.content.Context
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.view.TextureView

import com.todou.gltransition.BuildConfig
import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.GlUtil
import com.todou.gltransition.gles.WindowSurface

import java.io.IOException

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.Matrix.setIdentityM

class GLRenderWorker(context: Context, private val mTextureView: TextureView) : IRendererWorker {

    private var mImageClipProcessor: VideoClipProcessor? = null
    private val projectionMatrix = FloatArray(16)
    private var mIsRecording: Boolean = false

    private val mIdentityMatrix: FloatArray

    // Used for off-screen rendering.
    private var mOffscreenTexture: Int = 0
    private var mFramebuffer: Int = 0
    private var mDepthBuffer: Int = 0
    private var mEglCore: EglCore? = null

    // Used for recording.
    private val mInputWindowSurface: WindowSurface? = null
    private val mVideoRect: Rect

    init {
        mIdentityMatrix = FloatArray(16)
        Matrix.setIdentityM(mIdentityMatrix, 0)
        mVideoRect = Rect()
    }

    override fun onSurfaceCreated(windowSurface: WindowSurface, eglCore: EglCore) {

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mImageClipProcessor = VideoClipProcessor(mTextureView)
        mEglCore = eglCore
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
    }

    override fun onSurfaceChanged(windowSurface: WindowSurface, width: Int, height: Int) {
        prepareFramebuffer(width, height)
        glViewport(0, 0, width, height)
    }

    override fun drawFrame(context: Context, windowSurface: WindowSurface, usedTime: Long) {
        glClear(GL_COLOR_BUFFER_BIT)
        setIdentityM(projectionMatrix, 0)
        mImageClipProcessor!!.drawFrame(usedTime, projectionMatrix)
        windowSurface.swapBuffers()
    }

    override fun onSurfaceDestroy() {
        if (mImageClipProcessor != null) mImageClipProcessor!!.onDestroy()
    }

    fun refreshTransitionRender() {
        if (mImageClipProcessor != null) mImageClipProcessor!!.updateTransitionClipRenders()
    }

    fun refreshSubtitleRender() {
        if (mImageClipProcessor != null) mImageClipProcessor!!.updateSubtitleClipRenders()
    }

    @Synchronized
    fun startRecording(filename: String) {
        startEncoder(filename)
        mIsRecording = true
    }

    @Synchronized
    fun endRecording(): String? {
        var path: String? = null
        if (mIsRecording) {
            mIsRecording = false
            path = stopRecording()
        }
        return path
    }

    private fun prepareFramebuffer(width: Int, height: Int) {
        GlUtil.checkGlError("prepareFramebuffer start")

        val values = IntArray(1)
        GLES20.glGenTextures(1, values, 0)
        GlUtil.checkGlError("glGenTextures")
        mOffscreenTexture = values[0]   // expected > 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture)
        GlUtil.checkGlError("glBindTexture $mOffscreenTexture")
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE)
        GlUtil.checkGlError("glTexParameter")
        GLES20.glGenFramebuffers(1, values, 0)
        GlUtil.checkGlError("glGenFramebuffers")
        mFramebuffer = values[0]    // expected > 0
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer)
        GlUtil.checkGlError("glBindFramebuffer $mFramebuffer")

        // Create a depth buffer and bind it.
        GLES20.glGenRenderbuffers(1, values, 0)
        GlUtil.checkGlError("glGenRenderbuffers")
        mDepthBuffer = values[0]    // expected > 0
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBuffer)
        GlUtil.checkGlError("glBindRenderbuffer $mDepthBuffer")

        // Allocate storage for the depth buffer.
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16,
                width, height)
        GlUtil.checkGlError("glRenderbufferStorage")

        // Attach the depth buffer and the texture (color buffer) to the framebuffer object.
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT,
                GLES20.GL_RENDERBUFFER, mDepthBuffer)
        GlUtil.checkGlError("glFramebufferRenderbuffer")
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0)
        GlUtil.checkGlError("glFramebufferTexture2D")

        // See if GLES is happy with all this.
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw RuntimeException("Framebuffer not complete, status=$status")
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        GlUtil.checkGlError("prepareFramebuffer done")
    }

    private fun startEncoder(fileName: String) {
        Log.d(TAG, "starting to record")
        val BIT_RATE = 1000000  //码率(kbps)=文件大小(字节)X8 /时间(秒)/1000 4000000 1000000
        val VIDEO_WIDTH = 1280
        val VIDEO_HEIGHT = 720
        val windowWidth = mTextureView.width
        val windowHeight = mTextureView.height
        val windowAspect = windowHeight.toFloat() / windowWidth.toFloat()
        val outWidth: Int
        val outHeight: Int
        if (VIDEO_HEIGHT > VIDEO_WIDTH * windowAspect) {
            // limited by narrow width; reduce height
            outWidth = VIDEO_WIDTH
            outHeight = (VIDEO_WIDTH * windowAspect).toInt()
        } else {
            // limited by short height; restrict width
            outHeight = VIDEO_HEIGHT
            outWidth = (VIDEO_HEIGHT / windowAspect).toInt()
        }
        val offX = (VIDEO_WIDTH - outWidth) / 2
        val offY = (VIDEO_HEIGHT - outHeight) / 2
        mVideoRect.set(offX, offY, offX + outWidth, offY + outHeight)
        startRecording(fileName, VIDEO_WIDTH, VIDEO_HEIGHT, BIT_RATE)
    }

    companion object {

        val DEBUG = BuildConfig.DEBUG && true
        private val TAG = "GLRenderWorker"
    }

}
