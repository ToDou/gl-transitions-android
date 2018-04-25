package com.todou.gltransition.utils

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.util.DisplayMetrics

object DeviceScreenUtils {

    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    fun getDensity(context: Context): Float {
        return getDisplayMetrics(context).density
    }

    fun getScaledDensity(context: Context): Float {
        return getDisplayMetrics(context).scaledDensity
    }

    fun getScreenWidth(context: Context): Int {
        return getDisplayMetrics(context).widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return getDisplayMetrics(context).heightPixels
    }

    fun px2dp(pxValue: Float, activity: Activity): Int {
        return (pxValue / getDisplayMetrics(activity).density + 0.5f).toInt()
    }

    fun dp2px(dipValue: Float, activity: Activity): Int {
        return (dipValue * getDisplayMetrics(activity).density + 0.5f).toInt()
    }

    fun dp2px(dpValue: Float, context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2sp(pxValue: Float, activity: Activity): Int {
        return (pxValue / getDisplayMetrics(activity).scaledDensity + 0.5f).toInt()
    }

    fun sp2px(spValue: Float, activity: Activity): Int {
        return (spValue * getDisplayMetrics(activity).scaledDensity + 0.5f).toInt()
    }

    fun getTextLength(textSize: Float, text: String): Float {
        val paint = Paint()
        paint.textSize = textSize
        return paint.measureText(text)
    }

    /**
     * 获取实际屏幕高度
     * 如 1920 * 1080
     * @param activity
     * @return
     */
    fun getRealMetrics(activity: Activity): IntArray {
        val dpi = IntArray(2)
        val display = activity.windowManager.defaultDisplay
        val dm = DisplayMetrics()
        val c: Class<*>
        try {
            c = Class.forName("android.view.Display")
            val method = c.getMethod("getRealMetrics", DisplayMetrics::class.java)
            method.invoke(display, dm)
            dpi[0] = dm.widthPixels
            dpi[1] = dm.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return dpi
    }

    /**
     *
     * @param activity
     * @return
     */
    fun getStatusHeight(activity: Activity): Int {
        val rect = Rect()
        activity.window.decorView.getWindowVisibleDisplayFrame(rect)
        return rect.top
    }
}