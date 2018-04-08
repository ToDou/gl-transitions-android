package com.todou.gltransition.model

import com.todou.gltransition.programs.DissolveShaderProgram
import kotlin.reflect.KClass


enum class TransitionType(val shaderClass: KClass<*>?) {
    NO(null),
    DISSOLVE(DissolveShaderProgram::class),
    FADE(DissolveShaderProgram::class),
    SLIDE(DissolveShaderProgram::class),
    WIPE(DissolveShaderProgram::class),
}