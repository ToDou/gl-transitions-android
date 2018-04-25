package com.todou.gltransition.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import com.todou.gltransition.R
import com.todou.gltransition.render.TextureRenderer
import com.todou.gltransition.utils.DeviceScreenUtils


class MovieMakerTextureView : TextureView, TextureView.SurfaceTextureListener {

    protected var mRatioX: Float = 0.toFloat()
    protected var mRatioY: Float = 0.toFloat()
    private var mTextureRenderer: TextureRenderer? = null
    private var mRenderer: TextureRenderer.Renderer? = null
    private var mSurfaceWidth: Int = 0
    private var mSurfaceHeight: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        getAttrs(context, attrs, 0)
        isOpaque = false
        surfaceTextureListener = this
    }

    private fun getAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        if (attrs == null) return
        val a = context.obtainStyledAttributes(attrs, R.styleable.MovieMakerTextureView, defStyleAttr, 0)
                ?: return

        mRatioX = a.getFloat(R.styleable.MovieMakerTextureView_screenRatioX, 1f)
        mRatioY = a.getFloat(R.styleable.MovieMakerTextureView_screenRatioY, 1f)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(View.getDefaultSize(0, widthMeasureSpec), View.getDefaultSize(0, heightMeasureSpec))
            val childWidthSize = measuredWidth
            if (childWidthSize == Math.max(DeviceScreenUtils.getScreenHeight(context), DeviceScreenUtils.getScreenWidth(context))) {
                val childHeightSize = Math.min(DeviceScreenUtils.getScreenHeight(context), DeviceScreenUtils.getScreenWidth(context))
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(childHeightSize, View.MeasureSpec.EXACTLY)
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((1f * childHeightSize.toFloat() * mRatioX / mRatioY).toInt(), View.MeasureSpec.EXACTLY)
            } else {
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(childWidthSize, View.MeasureSpec.EXACTLY)
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((1f * childWidthSize.toFloat() * mRatioY / mRatioX).toInt(), View.MeasureSpec.EXACTLY)
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setRenderer(renderer: TextureRenderer.Renderer) {
        mRenderer = renderer
    }

    fun requestRender() {
        if (mTextureRenderer != null) {
            mTextureRenderer!!.requestRender()
        }
    }

    fun onPause() {
        val rh = mTextureRenderer?.getHandler()
        rh.sendShutdown()
        mTextureRenderer = null
    }

    fun onResume() {
        mTextureRenderer = TextureRenderer()
        mTextureRenderer!!.start()
        mTextureRenderer!!.waitUntilReady()
        mTextureRenderer!!.setRenderer(mRenderer)
        if (surfaceTexture != null) {
            mTextureRenderer!!.getHandler().sendSurfaceAvailable(surfaceTexture, mSurfaceWidth, mSurfaceHeight)
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (mTextureRenderer != null) {
            mTextureRenderer!!.getHandler().sendSurfaceAvailable(surface, mSurfaceWidth, mSurfaceHeight)
            mSurfaceWidth = width
            mSurfaceHeight = height
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        if (mTextureRenderer != null) {
            mTextureRenderer!!.getHandler().sendSurfaceChanged(width, height)
            mSurfaceWidth = width
            mSurfaceHeight = height
        }
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (DEBUG) Log.e(TAG, "onSurfaceTextureDestroyed")

        if (mTextureRenderer != null) {
            mTextureRenderer!!.getHandler().sendSurfaceDestroyed()
        }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {

    }

    companion object {

        private val TAG = "TextureRenderer View"
        val DEBUG = BuildConfig.DEBUG
    }
}
