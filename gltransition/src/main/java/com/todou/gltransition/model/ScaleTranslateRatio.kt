package com.todou.gltransition.model


class ScaleTranslateRatio : BaseModel {

    var scaleFactor = 1f
    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()

    constructor() {}

    constructor(scaleFactor: Float, x: Float, y: Float) {
        this.scaleFactor = scaleFactor
        this.x = x
        this.y = y
    }
}
