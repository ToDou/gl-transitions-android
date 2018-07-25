package com.todou.gltransition.render


import android.view.TextureView

import com.todou.gltransition.widget.MovieMakerTextureView
import java.util.ArrayList

class ClipProcessor(textureView: TextureView) {

    private val mImageClipDrawers: ArrayList<ImageClipDrawer>
    private val mTransitionDrawers: ArrayList<TransitionDrawer>
    private val mTextureView: MovieMakerTextureView

    init {
        mImageClipDrawers = ArrayList()
        mTransitionDrawers = ArrayList()
        mTextureView = textureView as MovieMakerTextureView
    }


    @Synchronized
    fun drawFrame(usedTime: Long, pMatrix: FloatArray) {
        for (render in mImageClipDrawers) {
            render.drawFrame(usedTime, pMatrix)
        }

        for (render in mTransitionDrawers) {
            render.drawFrame(usedTime, pMatrix)
        }
    }

    fun onDestroy(){

    }

    fun updateTransitionClipRenders(){

    }

    fun updateSubtitleClipRenders(){

    }
}
