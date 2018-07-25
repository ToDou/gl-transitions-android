package com.todou.gltransition.model

import com.todou.gltransition.Constants.DEFAULT_IMAGE_CLIP_SHOW_TIME


class ImageClip(path: String, startTime: Int) : Clip() {

    var startWithPreTransitionTime = 0
    var endWithNextTransitionTime = 0
    var startScaleTransRatio: ScaleTranslateRatio? = null
    var endScaleTransRatio: ScaleTranslateRatio? = null

    init {
        showTime = DEFAULT_IMAGE_CLIP_SHOW_TIME
        startWithPreTransitionTime = startTime
        endWithNextTransitionTime = endTime
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) return false
        return if (obj.javaClass != this.javaClass) false else startTime == (obj as ImageClip).startTime && super.equals(obj)
    }

    fun getScaleFactor(usedTime: Long): Float {
        return getRatio(usedTime) * (endScaleTransRatio!!.scaleFactor - startScaleTransRatio!!.scaleFactor) + startScaleTransRatio!!.scaleFactor
    }

    fun getTransX(usedTime: Long): Float {
        return getRatio(usedTime) * (endScaleTransRatio!!.x - startScaleTransRatio!!.x) + startScaleTransRatio!!.x
    }

    fun getTransY(usedTime: Long): Float {
        return getRatio(usedTime) * (endScaleTransRatio!!.y - startScaleTransRatio!!.y) + startScaleTransRatio!!.y
    }

    private fun getRatio(usedTime: Long): Float {
        return 1f * (usedTime - startTime) / (showTime - 1)
    }

    override fun toString(): String {
        return "ImageClip{" +
                super.toString() +
                "startWithPreTransitionTime=" + startWithPreTransitionTime +
                ", endWithNextTransitionTime=" + endWithNextTransitionTime +
                '}'.toString()
    }

    companion object {
        val BRIGHTNESS_VALUE = 0.9f
        val BLUR_RADIUS = 20
    }
}
