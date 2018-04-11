package com.todou.gltransition.render

import com.todou.gltransition.data.VertexArray

abstract class ClipDrawer(view: IRenderView) {
    protected var viewWidth: Int = 0
    protected var viewHeight: Int = 0
    init {
        viewWidth = view.getWidth()
        viewHeight = view.getHeight()
    }

    abstract fun drawFrame(usedTime: Long, pMatrix: FloatArray)

    protected fun getVertexArray(): VertexArray {
        return vertexArray
    }

    companion object {
        protected lateinit var vertexArray: VertexArray
        lateinit var mVertexData: FloatArray

        init {
            initVertex()
        }

        private fun initVertex() {
            mVertexData = floatArrayOf(-1f, 1f, 0f, 0f, -1f, -1f, 0f, 1f, 1f, -1f, 1f, 1f, 1f, -1f, 1f, 1f, 1f, 1f, 1f, 0f, -1f, 1f, 0f, 0f)
            vertexArray = VertexArray(mVertexData)
        }
    }
}
