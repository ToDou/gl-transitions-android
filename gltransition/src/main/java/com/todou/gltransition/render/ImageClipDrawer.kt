package com.todou.gltransition.render


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.opengl.GLES20
import android.os.Looper
import android.view.TextureView

import android.opengl.GLES20.GL_CLAMP_TO_EDGE
import android.opengl.GLES20.GL_LINEAR
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.GL_TEXTURE_MAG_FILTER
import android.opengl.GLES20.GL_TEXTURE_MIN_FILTER
import android.opengl.GLES20.GL_TEXTURE_WRAP_S
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glGenTextures
import android.opengl.GLES20.glTexParameteri
import android.opengl.GLUtils.texImage2D
import android.opengl.Matrix.setIdentityM
import com.todou.gltransition.programs.ImageClipShaderProgram

class ImageClipDrawer(view: TextureView, var mImageClip: ImageClip) : ClipDrawer(view) {

    private val mContext: Context

    private var mBitmap: Bitmap? = null
    private var mBlurBitmap: Bitmap? = null
    var mImageInfo: ImageInfo? = null

    private val textureProgram: ImageClipShaderProgram
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)

    private val mCanvasTextureId = intArrayOf(-1)

    private val mHorizontalBlockNum = 1
    private val mVerticalBlockNum = 1

    private var mViewScaleFactor: Float = 0.toFloat()
    private var mBlurViewScaleFactor: Float = 0.toFloat()
    private var mTextureLoader: TextureLoader? = null

    init {
        mContext = view.context

        textureProgram = ShaderProgramCache
                .getInstance()
                .getTextureId(ShaderProgramCache.NORMAL_IMAGE_PROGRAM_KEY) as ImageClipShaderProgram

        if (mViewWidth > MAX_VIEW_WITH) {
            mViewHeight = MAX_VIEW_WITH * mViewHeight / mViewWidth
            mViewWidth = MAX_VIEW_WITH
        }
    }

    fun preLoadTexture(glView: MovieMakerTextureView, textureLoader: TextureLoader?) {
        mTextureLoader = textureLoader
        val handler = HandlerWrapper(
                Looper.getMainLooper(),
                HandlerWrapper.TYPE_LOAD_IMAGE, mImageClip.path) { t ->
            checkBitmapReady()
            VideoPlayManagerContainer.getDefault().bitmapLoadReady(mContext, mImageClip.path)
        }
        textureLoader!!.loadImageTexture(handler)
    }

    fun checkBitmapReady() {
        mBitmap = BitmapFactory.getInstance().getBitmapFromMemCache(mImageClip.path)
        mBlurBitmap = BitmapFactory.getInstance().getBlurBitmapFromCache(mImageClip.path, mBitmap)
        if (mBitmap == null || mBlurBitmap == null) return
        mImageInfo = ImageInfo(-1, mBitmap!!.width, mBitmap!!.height)
        if (1f * mBitmap!!.width / mBitmap!!.height > 1f * mViewWidth / mViewHeight) {
            mViewScaleFactor = 1f * mViewWidth / mBitmap!!.width
            mBlurViewScaleFactor = 1f * mViewHeight / mBitmap!!.height
        } else {
            mViewScaleFactor = 1f * mViewHeight / mBitmap!!.height
            mBlurViewScaleFactor = 1f * mViewWidth / mBitmap!!.width
        }
    }

    private fun bindData() {
        ClipDrawer.vertexArray.setVertexAttribPointer(
                0,
                textureProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT,
                STRIDE)

        ClipDrawer.vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.getTextureCoordinatesAttributeLocation(),
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE)
    }

    private fun draw() {
        glDrawArrays(GL_TRIANGLES, 0, mHorizontalBlockNum * mVerticalBlockNum * 6)
    }

    private fun getTexture(usedTime: Long) {
        val matrix = Matrix()
        matrix.postScale(mViewScaleFactor, mViewScaleFactor)
        matrix.postTranslate(-1f * mViewScaleFactor * mBitmap!!.width.toFloat() / 2, -1f * mViewScaleFactor * mBitmap!!.height.toFloat() / 2)
        matrix.postScale(mImageClip.getScaleFactor(usedTime), mImageClip.getScaleFactor(usedTime))
        matrix.postTranslate(mViewWidth / 2, mViewHeight / 2)
        matrix.postTranslate(mImageClip.getTransX(usedTime) * mViewWidth, mImageClip.getTransY(usedTime) * mViewHeight)

        val localBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888)

        val localCanvas = Canvas(localBitmap)
        val imageMatrix = ColorMatrix()
        imageMatrix.setScale(BRIGHTNESS_VALUE, BRIGHTNESS_VALUE, BRIGHTNESS_VALUE, 1f)
        val blurImagePaint = Paint()
        blurImagePaint.colorFilter = ColorMatrixColorFilter(imageMatrix)
        val blurMatrix = Matrix()
        blurMatrix.postScale(mBlurViewScaleFactor, mBlurViewScaleFactor)
        blurMatrix.postTranslate(-1f * (mBlurViewScaleFactor * mBitmap!!.width / 2 - mViewWidth / 2), -1f * (mBlurViewScaleFactor * mBitmap!!.height / 2 - mViewHeight / 2))
        if (mBlurBitmap!!.isRecycled) return
        localCanvas.drawBitmap(mBlurBitmap!!, blurMatrix, blurImagePaint)

        val localPaint = Paint()
        localPaint.isFilterBitmap = true
        if (mBitmap!!.isRecycled) return
        localCanvas.drawBitmap(mBitmap!!, matrix, localPaint)

        if (mCanvasTextureId[0] != -1) {
            GLES20.glDeleteTextures(1, mCanvasTextureId, 0)
        }
        glGenTextures(1, mCanvasTextureId, 0)
        glBindTexture(GL_TEXTURE_2D, mCanvasTextureId[0])
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        texImage2D(GL_TEXTURE_2D, 0, 6408, localBitmap, 0)
        localBitmap.recycle()
        if (usedTime > mImageClip.getEndTime()) {
            TextureIdCache.getInstance().addIdToCache(mImageClip.getEndTime() + 1, mCanvasTextureId[0])
        }
        if (usedTime < mImageClip.startTime) {
            TextureIdCache.getInstance().addIdToCache(mImageClip.startTime - 1, mCanvasTextureId[0])
        }
    }

    private fun updateViewMatrices(usedTime: Long) {
        getTexture(usedTime)
        setIdentityM(modelMatrix, 0)
        setIdentityM(viewMatrix, 0)
    }

    override fun drawFrame(usedTime: Long, pMatrix: FloatArray) {
        if (mImageInfo == null) return
        if (usedTime < mImageClip.startWithPreTransitionTime || mImageClip.endWithNextTransitionTime > 0 && usedTime > mImageClip.endWithNextTransitionTime) return
        if (mBitmap == null
                || mBitmap!!.isRecycled
                || mBlurBitmap == null
                || mBlurBitmap!!.isRecycled) {
            preLoadTexture(mMovieMakerTextureView, mTextureLoader)
            return
        }
        updateViewMatrices(usedTime)
        if (usedTime < mImageClip.startTime || usedTime > mImageClip.getEndTime()) return
        textureProgram.useProgram()
        textureProgram.setUniforms(pMatrix, viewMatrix, modelMatrix, mCanvasTextureId[0])
        bindData()
        draw()
    }

    companion object {
        private val TAG = "ImageClipDrawer"
        private val MAX_VIEW_WITH = 1080

        private val POSITION_COMPONENT_COUNT = 2
        private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }
}
