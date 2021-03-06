package com.todou.gltransition.programs

import android.content.Context

import com.todou.gltransition.R

import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE1
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1f
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv

class WipeShaderProgram(context: Context) : ShaderProgram(context, R.raw.wipe_vertex, R.raw.wipe_fragment) {
    private val uMMatrixLocation: Int
    private val uVMatrixLocation: Int
    private val uPMatrixLocation: Int
    private val uTextureUnitLocation0: Int
    private val uTextureUnitLocation1: Int

    val positionAttributeLocation: Int
    val textureCoordinatesAttributeLocation: Int

    private val uProgress: Int
    private val uDirection: Int

    init {

        uMMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_MMATRIX)
        uVMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_VMATRIX)
        uPMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_PMATRIX)
        uTextureUnitLocation0 = glGetUniformLocation(program, ShaderProgram.U_TEXTURE_UNIT0)
        uTextureUnitLocation1 = glGetUniformLocation(program, ShaderProgram.U_TEXTURE_UNIT1)

        positionAttributeLocation = glGetAttribLocation(program, ShaderProgram.A_POSITION)
        textureCoordinatesAttributeLocation = glGetAttribLocation(program, ShaderProgram.A_TEXTURE_COORDINATES)

        uProgress = glGetUniformLocation(program, ShaderProgram.U_PROGRESS)
        uDirection = glGetUniformLocation(program, ShaderProgram.U_DIRECTION)
    }

    fun setUniforms(pMatrix: FloatArray, vMatrix: FloatArray, mMatrix: FloatArray, textureId0: Int, textureId1: Int, progress: Float, direction: Int) {
        glUniformMatrix4fv(uPMatrixLocation, 1, false, pMatrix, 0)
        glUniformMatrix4fv(uVMatrixLocation, 1, false, vMatrix, 0)
        glUniformMatrix4fv(uMMatrixLocation, 1, false, mMatrix, 0)

        glUniform1f(uProgress, progress)
        glUniform1i(uDirection, direction)

        glActiveTexture(GL_TEXTURE0)

        glBindTexture(GL_TEXTURE_2D, textureId0)
        glUniform1i(uTextureUnitLocation0, 0)

        glActiveTexture(GL_TEXTURE1)

        glBindTexture(GL_TEXTURE_2D, textureId1)

        glUniform1i(uTextureUnitLocation1, 1)
    }
}