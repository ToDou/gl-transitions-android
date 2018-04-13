package com.todou.gltransition.model

import java.io.Serializable
import java.util.UUID

open class Clip : Serializable {
    var startTime = 0
    var showTime = 2000
    var key: String

    val endTime: Int
        get() = showTime + startTime - 1

    init {
        key = UUID.randomUUID().toString()
    }

    override fun toString(): String {
        return "Clip{" +
                "startTime=" + startTime +
                ", showTime=" + showTime +
                '}'.toString()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj.javaClass != this.javaClass) false else key == (obj as Clip).key
    }
}
