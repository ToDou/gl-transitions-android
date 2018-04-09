package com.todou.gltransition.model

import com.todou.gltransition.programs.DissolveShaderProgram

enum class TransitionType(val shaderClass: Class<*>?) {
    NO(null),
    DISSOLVE(DissolveShaderProgram::class.java),
    FADE(DissolveShaderProgram::class.java),
    SLIDE(DissolveShaderProgram::class.java),
    WIPE(DissolveShaderProgram::class.java),
}