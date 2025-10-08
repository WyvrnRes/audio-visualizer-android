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
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import com.gauravk.audiovisualizer.model.AnimSpeed
import com.gauravk.audiovisualizer.model.PaintStyle
import com.gauravk.audiovisualizer.model.PositionGravity
import com.gauravk.audiovisualizer.utils.AVConstants
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Custom view to create wave visualizer
 *
 * Created by gk
 */
class WaveVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val WAVE_MAX_POINTS = 54
        private const val WAVE_MIN_POINTS = 3
    }

    private var mMaxBatchCount: Int = 0
    private lateinit var mWavePath: Path
    private var nPoints: Int = 0

    private lateinit var mBezierPoints: Array<PointF>
    private lateinit var mBezierControlPoints1: Array<PointF>
    private lateinit var mBezierControlPoints2: Array<PointF>

    private lateinit var mSrcY: FloatArray
    private lateinit var mDestY: FloatArray

    private var mWidthOffset: Float = -1f
    private val mClipBounds = Rect()

    private var nBatchCount: Int = 0
    private val mRandom = Random()

    override fun init() {
        nPoints = (WAVE_MAX_POINTS * mDensity).toInt()
        if (nPoints < WAVE_MIN_POINTS) {
            nPoints = WAVE_MIN_POINTS
        }

        mWidthOffset = -1f
        nBatchCount = 0

        setAnimationSpeed(mAnimSpeed)

        mWavePath = Path()

        mSrcY = FloatArray(nPoints + 1)
        mDestY = FloatArray(nPoints + 1)

        // Initialize mBezierPoints
        mBezierPoints = Array(nPoints + 1) { PointF() }
        mBezierControlPoints1 = Array(nPoints + 1) { PointF() }
        mBezierControlPoints2 = Array(nPoints + 1) { PointF() }
    }

    override fun setAnimationSpeed(animSpeed: AnimSpeed) {
        super.setAnimationSpeed(animSpeed)
        mMaxBatchCount = AVConstants.MAX_ANIM_BATCH_COUNT - mAnimSpeed.ordinal
    }

    override fun onDraw(canvas: Canvas) {
        if (mWidthOffset == -1f) {
            canvas.getClipBounds(mClipBounds)
            mWidthOffset = canvas.width.toFloat() / nPoints

            // Initialize bezier points
            for (i in mBezierPoints.indices) {
                val posX = mClipBounds.left + (i * mWidthOffset)
                val posY = if (mPositionGravity == PositionGravity.TOP) {
                    mClipBounds.top.toFloat()
                } else {
                    mClipBounds.bottom.toFloat()
                }

                mSrcY[i] = posY
                mDestY[i] = posY
                mBezierPoints[i].set(posX, posY)
            }
        }

        // Create the path and draw
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) {
                return
            }

            mWavePath.rewind()

            // Find the destination bezier point for a batch
            if (nBatchCount == 0) {
                val randPosY = mDestY[mRandom.nextInt(nPoints)]
                for (i in mBezierPoints.indices) {
                    val x = ceil((i + 1) * (bytes.size.toFloat() / nPoints)).toInt()

                    var t = 0
                    if (x < 1024) {
                        t = canvas.height + (abs(bytes[x].toInt()) + 128) * canvas.height / 128
                    }

                    val posY = if (mPositionGravity == PositionGravity.TOP) {
                        mClipBounds.bottom - t
                    } else {
                        mClipBounds.top + t
                    }.toFloat()

                    // Change the source and destination y
                    mSrcY[i] = mDestY[i]
                    mDestY[i] = posY
                }

                mDestY[mBezierPoints.size - 1] = randPosY
            }

            // Increment batch count
            nBatchCount++

            // For smoothing animation
            for (i in mBezierPoints.indices) {
                mBezierPoints[i].y = mSrcY[i] + ((nBatchCount.toFloat() / mMaxBatchCount) * (mDestY[i] - mSrcY[i]))
            }

            // Reset the batch count
            if (nBatchCount == mMaxBatchCount) {
                nBatchCount = 0
            }

            // Calculate the bezier curve control points
            for (i in 1 until mBezierPoints.size) {
                mBezierControlPoints1[i].set(
                    (mBezierPoints[i].x + mBezierPoints[i - 1].x) / 2,
                    mBezierPoints[i - 1].y
                )
                mBezierControlPoints2[i].set(
                    (mBezierPoints[i].x + mBezierPoints[i - 1].x) / 2,
                    mBezierPoints[i].y
                )
            }

            // Create the path
            mWavePath.moveTo(mBezierPoints[0].x, mBezierPoints[0].y)
            for (i in 1 until mBezierPoints.size) {
                mWavePath.cubicTo(
                    mBezierControlPoints1[i].x, mBezierControlPoints1[i].y,
                    mBezierControlPoints2[i].x, mBezierControlPoints2[i].y,
                    mBezierPoints[i].x, mBezierPoints[i].y
                )
            }

            // Add last 3 line to close the view
            if (mPaintStyle == PaintStyle.FILL) {
                mWavePath.lineTo(mClipBounds.right.toFloat(), mClipBounds.bottom.toFloat())
                mWavePath.lineTo(mClipBounds.left.toFloat(), mClipBounds.bottom.toFloat())
                mWavePath.close()
            }

            canvas.drawPath(mWavePath, mPaint)
        }

        super.onDraw(canvas)
    }
}
