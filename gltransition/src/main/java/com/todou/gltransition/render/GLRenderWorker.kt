package com.todou.gltransition.render

import android.content.Context
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.Matrix
import android.view.TextureView

import com.todou.gltransition.BuildConfig
import com.todou.gltransition.gles.EglCore
import com.todou.gltransition.gles.GlUtil
import com.todou.gltransition.gles.WindowSurface

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glViewport
import android.opengl.Matrix.setIdentityM

class GLRenderWorker(context: Context, private val mTextureView: TextureView) : IRendererWorker {

    private lateinit var mClipProcessor: ClipProcessor
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
        mClipProcessor = ClipProcessor(mTextureView)
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
        mClipProcessor.drawFrame(usedTime, projectionMatrix)
        windowSurface.swapBuffers()
    }

    override fun onSurfaceDestroy() {
        mClipProcessor.onDestroy()
    }

    fun refreshTransitionRender() {
        mClipProcessor.updateTransitionClipRenders()
    }

    fun refreshSubtitleRender() {
        mClipProcessor.updateSubtitleClipRenders()
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

    companion object {
        val DEBUG = BuildConfig.DEBUG && true
        private val TAG = "GLRenderWorker"
    }

}
