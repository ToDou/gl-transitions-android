package com.todou.gltransition.render

import com.todou.gltransition.cache.ShaderProgramCache
import com.todou.gltransition.data.VertexArray.Companion.BYTES_PER_FLOAT
import com.todou.gltransition.model.TransitionClip
import com.todou.gltransition.programs.DissolveShaderProgram

class DissolveDrawer(view: IRenderView, transitionClip: TransitionClip) : TransitionDrawer(view, transitionClip) {

    private val textureProgram: DissolveShaderProgram = ShaderProgramCache
            .instance
            .getTextureId(transitionClip.transitionType.ordinal.toString()) as DissolveShaderProgram

    override fun updateProgramBindData(usedTime: Long, pMatrix: FloatArray) {
        textureProgram.useProgram()
        textureProgram.setUniforms(pMatrix, viewMatrix, modelMatrix, mTextureIdPre, mTextureIdNext, getProgress(usedTime))
        bindData()
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
