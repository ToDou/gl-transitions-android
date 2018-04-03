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

import com.todou.gltransition.utils.ShaderHelper
import com.todou.gltransition.utils.TextResourceReader

import android.opengl.GLES20.glUseProgram

abstract class ShaderProgram protected constructor(context: Context, vertexShaderResourceId: Int,
                                                   fragmentShaderResourceId: Int) {

    protected val program: Int

    init {
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId))
    }

    fun useProgram() {
        glUseProgram(program)
    }

    companion object {

        val U_MMATRIX = "u_MMatrix"
        val U_VMATRIX = "u_VMatrix"
        val U_PMATRIX = "u_PMatrix"
        val U_TEXTURE_UNIT0 = "u_TextureUnit0"
        val U_TEXTURE_UNIT1 = "u_TextureUnit1"

        val A_POSITION = "a_Position"
        val A_THRESHOLD = "a_Threshold"

        val A_TEXTURE_COORDINATES = "a_TextureCoordinates"

        val U_PROGRESS = "u_Progress"
        val U_DIRECTION = "u_Direction"
    }
}
