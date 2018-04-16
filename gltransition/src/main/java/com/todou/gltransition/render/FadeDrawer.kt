package com.todou.gltransition.render

import com.todou.gltransition.cache.ShaderProgramCache
import com.todou.gltransition.model.TransitionClip
import com.todou.gltransition.programs.FadeShaderProgram

import android.opengl.GLES20.GL_BLEND
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLES20.glDisable
import android.opengl.GLES20.glEnable
import com.todou.gltransition.data.VertexArray.Companion.BYTES_PER_FLOAT

class FadeDrawer(view: IRenderView, transitionClip: TransitionClip) : TransitionDrawer(view, transitionClip) {

    private val textureProgram: FadeShaderProgram

    init {
        textureProgram = ShaderProgramCache
                .instance
                .getTextureId(transitionClip.transitionType.ordinal.toString()) as FadeShaderProgram
    }

    override fun updateProgramBindData(usedTime: Long, pMatrix: FloatArray) {
        textureProgram.useProgram()
        textureProgram.setUniforms(pMatrix, viewMatrix, modelMatrix, mTextureIdPre, mTextureIdNext, getProgress(usedTime))
        bindData()
    }

    override fun drawFrame(usedTime: Long, pMatrix: FloatArray) {
        if (usedTime < transitionClip.startTime || usedTime > transitionClip.endTime) return
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        updateViewMatrices(usedTime)
        updateProgramBindData(usedTime, pMatrix)
        draw()
        glDisable(GL_BLEND)
    }

    private fun bindData() {
        ClipDrawer.vertexArray.setVertexAttribPointer(
                0,
                textureProgram.positionAttributeLocation,
                POSITION_COMPONENT_COUNT,
                STRIDE)

        ClipDrawer.vertexArray.setVertexAttribPointer(
                POSITION_COMPONENT_COUNT,
                textureProgram.textureCoordinatesAttributeLocation,
                TEXTURE_COORDINATES_COMPONENT_COUNT,
                STRIDE)
    }

    companion object {

        private val POSITION_COMPONENT_COUNT = 2
        private val TEXTURE_COORDINATES_COMPONENT_COUNT = 2
        private val STRIDE = (POSITION_COMPONENT_COUNT + TEXTURE_COORDINATES_COMPONENT_COUNT) * BYTES_PER_FLOAT
    }
}
