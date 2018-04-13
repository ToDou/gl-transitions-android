package com.todou.gltransition.model

class TransitionClip() : Clip() {
    lateinit var transitionType: TransitionType

    init {
        showTime = DEFAULT_TRANSITION_CLIP_SHOW_TIME
    }

    constructor(startTime: Int) : this() {
        this.startTime = startTime
        showTime = 0
        this.transitionType = TransitionType.NO
    }

    constructor(transitionType: TransitionType) : this() {
        this.transitionType = transitionType
    }

    companion object {
        val DEFAULT_TRANSITION_CLIP_SHOW_TIME = 1000;
    }
}
