package com.todou.gltransition.model


import java.io.Serializable

class ImageInfo(textureObjectId: Int, var width: Int, var height: Int) : Serializable {
    var textureObjectId = -1

    init {
        this.textureObjectId = textureObjectId
    }

    override fun toString(): String {
        return "ImageInfo{" +
                "textureObjectId=" + textureObjectId +
                ", width=" + width +
                ", height=" + height +
                '}'.toString()
    }
}
