package com.todou.gltransition.render
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.glDrawArrays
import android.opengl.Matrix.setIdentityM
import com.todou.gltransition.cache.TextureIdCache
import com.todou.gltransition.model.TransitionClip

abstract class TransitionDrawer(view: IRenderView, var transitionClip: TransitionClip) : ClipDrawer(view) {

    protected val modelMatrix = FloatArray(16)
    protected val viewMatrix = FloatArray(16)
    protected var mTextureIdPre: Int = 0
    protected var mTextureIdNext: Int = 0

    protected fun draw() {
        glDrawArrays(GL_TRIANGLES, 0, 6)
    }

    protected open fun updateViewMatrices(usedTime: Long) {
        mTextureIdPre = TextureIdCache.instance!!.getTextureId(transitionClip.startTime)
        mTextureIdNext = TextureIdCache.instance!!.getTextureId(transitionClip.endTime)
        setIdentityM(modelMatrix, 0)
        setIdentityM(viewMatrix, 0)
    }

    override fun drawFrame(usedTime: Long, pMatrix: FloatArray) {
        if (usedTime < transitionClip.startTime || usedTime > transitionClip.endTime) return
        updateViewMatrices(usedTime)
        if (mTextureIdPre == 0 || mTextureIdNext == 0) return
        updateProgramBindData(usedTime, pMatrix)
        draw()
    }

    abstract fun updateProgramBindData(usedTime: Long, pMatrix: FloatArray)

    protected fun getProgress(usedTime: Long): Float {
        return 1f * (usedTime - transitionClip.startTime) / transitionClip.showTime
    }
}
