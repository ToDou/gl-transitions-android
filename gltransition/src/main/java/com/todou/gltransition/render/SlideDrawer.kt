package com.todou.gltransition.render

import android.opengl.Matrix.setIdentityM
import android.opengl.Matrix.translateM
import com.todou.gltransition.cache.ShaderProgramCache
import com.todou.gltransition.cache.TextureIdCache
import com.todou.gltransition.data.VertexArray.Companion.BYTES_PER_FLOAT
import com.todou.gltransition.model.TransitionClip
import com.todou.gltransition.programs.ImageClipShaderProgram

class SlideDrawer(view: IRenderView, transitionClip: TransitionClip) : TransitionDrawer(view, transitionClip) {

    private val textureProgram0: ImageClipShaderProgram
    private val textureProgram1: ImageClipShaderProgram

    protected val modelMatrix1 = FloatArray(16)
    protected val viewMatrix1 = FloatArray(16)

    init {
        textureProgram0 = ShaderProgramCache
                .instance
                .getTextureId(transitionClip.transitionType.ordinal.toString() + "_0") as ImageClipShaderProgram

        textureProgram1 = ShaderProgramCache
                .instance
                .getTextureId(transitionClip.transitionType.ordinal.toString() + "_1") as ImageClipShaderProgram
    }

    fun updateProgramBindData0(usedTime: Long, pMatrix: FloatArray) {
        textureProgram0.useProgram()
        textureProgram0.setUniforms(pMatrix, viewMatrix, modelMatrix, mTextureIdPre)
        bindData0()
    }

    override fun updateProgramBindData(usedTime: Long, pMatrix: FloatArray) {
        textureProgram1.useProgram()
        textureProgram1.setUniforms(pMatrix, viewMatrix1, modelMatrix1, mTextureIdNext)
        bindData1()
    }

    private fun bindData0() {
        ClipDrawer.vertexArray.setVertexAttribPointer(
                0,
                textureProgram0.positionAttributeLocation,
                POSITION_COMPONENT_COUNT,
                STRIDE)

        ClipDrawer.vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram0.textureCoordinatesAttributeLocation,
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE)
    }


    private fun bindData1() {
        ClipDrawer.vertexArray.setVertexAttribPointer(
                0,
                textureProgram1.positionAttributeLocation,
                POSITION_COMPONENT_COUNT,
                STRIDE)

        ClipDrawer.vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram1.textureCoordinatesAttributeLocation,
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE)
    }

    override fun updateViewMatrices(usedTime: Long) {
        mTextureIdPre = TextureIdCache.instance.getTextureId(transitionClip.startTime)
        mTextureIdNext = TextureIdCache.instance.getTextureId(transitionClip.endTime)
        setIdentityM(modelMatrix, 0)
        setIdentityM(viewMatrix, 0)

        setIdentityM(modelMatrix1, 0)
        setIdentityM(viewMatrix1, 0)
        translateM(viewMatrix1, 0, 0f, -2f * (1f - getProgress(usedTime)), 0f)
    }

    override fun drawFrame(usedTime: Long, pMatrix: FloatArray) {
        if (usedTime < transitionClip.startTime || usedTime > transitionClip.endTime) return
        updateViewMatrices(usedTime)
        updateProgramBindData0(usedTime, pMatrix)
        draw()
        updateProgramBindData(usedTime, pMatrix)
        draw()
    }

    companion object {

        private val POSITION_COMPONENT_COUNT = 2
        private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }

}
