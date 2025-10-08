/*
        Copyright 2018 Gaurav Kumar

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.gauravk.audiovisualizer.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import com.gauravk.audiovisualizer.model.PaintStyle
import kotlin.math.*

/**
 * @author maple on 2019/4/25 10:17.
 * @version v1.0
 * @see 1040441325@qq.com
 */
class HiFiVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_MAX_POINTS = 240
        private const val BAR_MIN_POINTS = 30
        private const val PER_RADIUS = 0.65f
    }

    private var mRadius: Int = -1
    private var mPoints: Int = 0
    private lateinit var mHeights: IntArray
    private lateinit var mPath: Path // outward path
    private lateinit var mPath1: Path // inward path

    /**
     * This is the distance from center to bezier control point.
     * We can calculate the bezier control points of each segment this distance and its angle;
     */
    private var mBezierControlPointLen: Int = 0

    override fun init() {
        mRadius = -1
        mPath = Path()
        mPath1 = Path()
        mPaint.style = Paint.Style.STROKE
        mPaint.isAntiAlias = true
        mPaint.strokeWidth = 1.0f
        mPoints = (BAR_MAX_POINTS * mDensity).toInt()
        if (mPoints < BAR_MIN_POINTS) mPoints = BAR_MIN_POINTS
        mHeights = IntArray(mPoints)
    }

    /**
     * You cannot change the style of paint;
     * the paintStyle fixed at Paint.Style.STROKE:
     *
     * @param paintStyle style of the visualizer.
     */
    @Deprecated("Paint style is fixed for HiFiVisualizer", ReplaceWith(""))
    override fun setPaintStyle(paintStyle: PaintStyle) {
        // No-op: style is fixed for this visualizer
    }

    override fun onDraw(canvas: Canvas) {
        if (mRadius == -1) {
            mRadius = (min(width, height) / 2 * PER_RADIUS).toInt()
            mBezierControlPointLen = (mRadius / cos(PI / mPoints)).toInt()
        }
        updateData()
        mPath.reset()
        mPath1.reset()

        // Start the outward path from the last point
        val angleL = (360 - 360 / mPoints) * PI / 180
        val cxL = (width / 2 + cos(angleL) * (mRadius + mHeights[mPoints - 1])).toFloat()
        val cyL = (height / 2 - sin(angleL) * (mRadius + mHeights[mPoints - 1])).toFloat()
        mPath.moveTo(cxL, cyL)

        // Start the inward path from the last point
        val cxL1 = (width / 2 + cos(angleL) * (mRadius - mHeights[mPoints - 1])).toFloat()
        val cyL1 = (height / 2 - sin(angleL) * (mRadius - mHeights[mPoints - 1])).toFloat()
        mPath1.moveTo(cxL1, cyL1)

        for (i in 0 until 360 step 360 / mPoints) {
            val pointIndex = i * mPoints / 360
            val angle = i * PI / 180
            val controlAngle = (i - (180 / mPoints)) * PI / 180

            // Outward
            // The next point of path
            val cx = (width / 2 + cos(angle) * (mRadius + mHeights[pointIndex])).toFloat()
            val cy = (height / 2 - sin(angle) * (mRadius + mHeights[pointIndex])).toFloat()

            // Second bezier control point
            val bx = (width / 2 + cos(controlAngle) * (mBezierControlPointLen + mHeights[pointIndex])).toFloat()
            val by = (height / 2 - sin(controlAngle) * (mBezierControlPointLen + mHeights[pointIndex])).toFloat()

            val lastPoint = if (i == 0) mPoints - 1 else pointIndex - 1

            // First bezier control point
            val ax = (width / 2 + cos(controlAngle) * (mBezierControlPointLen + mHeights[lastPoint])).toFloat()
            val ay = (height / 2 - sin(controlAngle) * (mBezierControlPointLen + mHeights[lastPoint])).toFloat()
            mPath.cubicTo(ax, ay, bx, by, cx, cy)

            // Inward
            val cx1 = (width / 2 + cos(angle) * (mRadius - mHeights[pointIndex])).toFloat()
            val cy1 = (height / 2 - sin(angle) * (mRadius - mHeights[pointIndex])).toFloat()
            val bx1 = (width / 2 + cos(controlAngle) * (mBezierControlPointLen - mHeights[pointIndex])).toFloat()
            val by1 = (height / 2 - sin(controlAngle) * (mBezierControlPointLen - mHeights[pointIndex])).toFloat()
            val ax1 = (width / 2 + cos(controlAngle) * (mBezierControlPointLen - mHeights[lastPoint])).toFloat()
            val ay1 = (height / 2 - sin(controlAngle) * (mBezierControlPointLen - mHeights[lastPoint])).toFloat()
            mPath1.cubicTo(ax1, ay1, bx1, by1, cx1, cy1)
            canvas.drawLine(cx, cy, cx1, cy1, mPaint)
        }
        canvas.drawPath(mPath, mPaint)
        canvas.drawPath(mPath1, mPaint)
    }

    private fun updateData() {
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) return
            for (i in mHeights.indices) {
                val x = ceil((i + 1) * (bytes.size.toFloat() / mPoints)).toInt()
                var t = 0
                if (x < 1024) {
                    t = (abs(bytes[x].toInt()) + 128) * mRadius / 128
                }
                mHeights[i] = -t
            }
        }
    }
}
