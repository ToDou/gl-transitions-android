/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material,
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose.
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 */
package com.todou.gltransition.utils

import android.util.Log
import android.opengl.GLES20.GL_COMPILE_STATUS
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_LINK_STATUS
import android.opengl.GLES20.GL_VALIDATE_STATUS
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glAttachShader
import android.opengl.GLES20.glCompileShader
import android.opengl.GLES20.glCreateProgram
import android.opengl.GLES20.glCreateShader
import android.opengl.GLES20.glDeleteProgram
import android.opengl.GLES20.glDeleteShader
import android.opengl.GLES20.glGetProgramInfoLog
import android.opengl.GLES20.glGetProgramiv
import android.opengl.GLES20.glGetShaderInfoLog
import android.opengl.GLES20.glGetShaderiv
import android.opengl.GLES20.glLinkProgram
import android.opengl.GLES20.glShaderSource
import android.opengl.GLES20.glValidateProgram

object ShaderHelper {
    private val TAG = "ShaderHelper"

    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     */
    fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     */
    fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private fun compileShader(type: Int, shaderCode: String): Int {
        // Create a new shader object.
        val shaderObjectId = glCreateShader(type)

        if (shaderObjectId == 0) {
            if (GLGlobalConfig.logOn) {
                Log.w(TAG, "Could not create new shader.")
            }

            return 0
        }

        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode)

        // Compile the shader.
        glCompileShader(shaderObjectId)

        // Get the compilation status.
        val compileStatus = IntArray(1)
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,
                compileStatus, 0)

        if (GLGlobalConfig.logOn) {
            // Print the shader info log to the Android log output.
            Log.v(TAG, "Results of compiling source:" + "\n" + shaderCode
                    + "\n:" + glGetShaderInfoLog(shaderObjectId))
        }

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId)

            if (GLGlobalConfig.logOn) {
                Log.w(TAG, "Compilation of shader failed.")
            }

            return 0
        }

        // Return the shader object ID.
        return shaderObjectId
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {

        // Create a new program object.
        val programObjectId = glCreateProgram()

        if (programObjectId == 0) {
            if (GLGlobalConfig.logOn) {
                Log.w(TAG, "Could not create new program")
            }

            return 0
        }

        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId)

        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId)

        // Link the two shaders together into a program.
        glLinkProgram(programObjectId)

        // Get the link status.
        val linkStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0)

        if (GLGlobalConfig.logOn) {
            // Print the program info log to the Android log output.
            Log.v(
                    TAG,
                    "Results of linking program:\n" + glGetProgramInfoLog(programObjectId))
        }

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId)

            if (GLGlobalConfig.logOn) {
                Log.w(TAG, "Linking of program failed.")
            }

            return 0
        }

        // Return the program object ID.
        return programObjectId
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    fun validateProgram(programObjectId: Int): Boolean {
        glValidateProgram(programObjectId)
        val validateStatus = IntArray(1)
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0)
        Log.v(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId))

        return validateStatus[0] != 0
    }

    /**
     * Helper function that compiles the shaders, links and validates the
     * program, returning the program ID.
     */
    fun buildProgram(vertexShaderSource: String,
                     fragmentShaderSource: String): Int {
        val program: Int

        // Compile the shaders.
        val vertexShader = compileVertexShader(vertexShaderSource)
        val fragmentShader = compileFragmentShader(fragmentShaderSource)

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader)

        if (GLGlobalConfig.logOn) {
            validateProgram(program)
        }

        return program
    }
}
