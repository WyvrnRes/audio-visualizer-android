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
import android.graphics.*
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import kotlin.math.*

/**
 * @author maple on 2019/4/24 15:17.
 * @version v1.0
 * @see 1040441325@qq.com
 */
class CircleLineVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_MAX_POINTS = 240
        private const val BAR_MIN_POINTS = 30
    }

    private val mClipBounds = Rect()
    private var mPoints: Int = 0
    private var mPointRadius: Int = 0
    private lateinit var mSrcY: FloatArray
    private var mRadius: Int = 0
    private lateinit var mGPaint: Paint
    var isDrawLine: Boolean = false

    override fun init() {
        mPoints = (BAR_MAX_POINTS * mDensity).toInt()
        if (mPoints < BAR_MIN_POINTS) {
            mPoints = BAR_MIN_POINTS
        }
        mSrcY = FloatArray(mPoints)
        setAnimationSpeed(mAnimSpeed)
        mPaint.isAntiAlias = true
        mGPaint = Paint().apply {
            isAntiAlias = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mRadius = min(w, h) / 4
        mPointRadius = abs((2 * mRadius * sin(PI / mPoints / 3)).toInt())
        
        // Modern gradient colors
        val lg = LinearGradient(
            (width / 2 + mRadius).toFloat(),
            (height / 2).toFloat(),
            (width / 2 + mRadius + mPointRadius * 5).toFloat(),
            (height / 2).toFloat(),
            Color.parseColor("#77FF5722"),
            Color.parseColor("#10FF5722"),
            Shader.TileMode.CLAMP
        )
        mGPaint.shader = lg
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(mClipBounds)
        updateData()

        // Draw circle's points
        for (i in 0 until 360 step 360 / mPoints) {
            val cx = (width / 2 + cos(i * PI / 180) * mRadius).toFloat()
            val cy = (height / 2 - sin(i * PI / 180) * mRadius).toFloat()
            canvas.drawCircle(cx, cy, mPointRadius.toFloat(), mPaint)
        }

        // Draw lines
        if (isDrawLine) drawLines(canvas)

        // Draw bar
        for (i in 0 until 360 step 360 / mPoints) {
            if (mSrcY[i * mPoints / 360] == 0f) continue
            canvas.save()
            canvas.rotate(-i.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
            val cx = (width / 2 + mRadius).toFloat()
            val cy = (height / 2).toFloat()
            canvas.drawRect(
                cx,
                cy - mPointRadius,
                cx + mSrcY[i * mPoints / 360],
                cy + mPointRadius,
                mPaint
            )
            canvas.drawCircle(
                cx + mSrcY[i * mPoints / 360],
                cy,
                mPointRadius.toFloat(),
                mPaint
            )
            canvas.restore()
        }
    }

    /**
     * Draw a translucent ray
     *
     * @param canvas target canvas
     */
    private fun drawLines(canvas: Canvas) {
        val lineLen = 14 * mPointRadius // default len
        for (i in 0 until 360 step 360 / mPoints) {
            canvas.save()
            canvas.rotate(-i.toFloat(), (width / 2).toFloat(), (height / 2).toFloat())
            val cx = (width / 2 + mRadius).toFloat() + mSrcY[i * mPoints / 360]
            val cy = (height / 2).toFloat()
            val path = Path()
            path.moveTo(cx, cy + mPointRadius)
            path.lineTo(cx, cy - mPointRadius)
            path.lineTo(cx + lineLen, cy)
            canvas.drawPath(path, mGPaint)
            canvas.restore()
        }
    }

    private fun updateData() {
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) return
            for (i in mSrcY.indices) {
                val x = ceil((i + 1) * (bytes.size.toFloat() / mPoints)).toInt()
                var t = 0
                if (x < 1024) {
                    t = (abs(bytes[x].toInt()) + 128) * mRadius / 128
                }
                mSrcY[i] = -t.toFloat()
            }
        }
    }
}
