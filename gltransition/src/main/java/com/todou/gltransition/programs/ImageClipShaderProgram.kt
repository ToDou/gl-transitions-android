/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.todou.gltransition.programs

import android.content.Context
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_2D
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import com.todou.gltransition.R

class ImageClipShaderProgram(context: Context) : ShaderProgram(context, R.raw.image_clip_vertex, R.raw.image_clip_fragment) {

    private val uMMatrixLocation: Int
    private val uVMatrixLocation: Int
    private val uPMatrixLocation: Int
    private val uTextureUnitLocation0: Int

    val positionAttributeLocation: Int
    val textureCoordinatesAttributeLocation: Int

    init {

        // Retrieve uniform locations for the shader program.
        uMMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_MMATRIX)
        uVMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_VMATRIX)
        uPMatrixLocation = glGetUniformLocation(program, ShaderProgram.U_PMATRIX)
        uTextureUnitLocation0 = glGetUniformLocation(program, ShaderProgram.U_TEXTURE_UNIT0)

        // Retrieve attribute locations for the shader program.
        positionAttributeLocation = glGetAttribLocation(program, ShaderProgram.A_POSITION)
        textureCoordinatesAttributeLocation = glGetAttribLocation(program, ShaderProgram.A_TEXTURE_COORDINATES)
    }

    fun setUniforms(pMatrix: FloatArray, vMatrix: FloatArray, mMatrix: FloatArray, textureId0: Int) {
        // Pass the matrix into the shader program.
        glUniformMatrix4fv(uPMatrixLocation, 1, false, pMatrix, 0)
        glUniformMatrix4fv(uVMatrixLocation, 1, false, vMatrix, 0)
        glUniformMatrix4fv(uMMatrixLocation, 1, false, mMatrix, 0)

        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0)

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId0)
        glUniform1i(uTextureUnitLocation0, 0)
    }
}
