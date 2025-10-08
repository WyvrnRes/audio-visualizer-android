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
import android.graphics.Rect
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import com.gauravk.audiovisualizer.model.AnimSpeed
import com.gauravk.audiovisualizer.model.PositionGravity
import com.gauravk.audiovisualizer.utils.AVConstants
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Custom view to create bar visualizer
 *
 * Created by gk
 */
class BarVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val BAR_MAX_POINTS = 120
        private const val BAR_MIN_POINTS = 3
    }

    private var mMaxBatchCount: Int = 0
    private var nPoints: Int = 0

    private lateinit var mSrcY: FloatArray
    private lateinit var mDestY: FloatArray

    private var mBarWidth: Float = -1f
    private val mClipBounds = Rect()

    private var nBatchCount: Int = 0
    private val mRandom = Random()

    override fun init() {
        nPoints = (BAR_MAX_POINTS * mDensity).toInt()
        if (nPoints < BAR_MIN_POINTS) {
            nPoints = BAR_MIN_POINTS
        }

        mBarWidth = -1f
        nBatchCount = 0

        setAnimationSpeed(mAnimSpeed)

        mSrcY = FloatArray(nPoints)
        mDestY = FloatArray(nPoints)
    }

    override fun setAnimationSpeed(animSpeed: AnimSpeed) {
        super.setAnimationSpeed(animSpeed)
        mMaxBatchCount = AVConstants.MAX_ANIM_BATCH_COUNT - mAnimSpeed.ordinal
    }

    override fun onDraw(canvas: Canvas) {
        if (mBarWidth == -1f) {
            canvas.getClipBounds(mClipBounds)
            mBarWidth = canvas.width.toFloat() / nPoints

            // Initialize points
            for (i in mSrcY.indices) {
                val posY = if (mPositionGravity == PositionGravity.TOP) {
                    mClipBounds.top.toFloat()
                } else {
                    mClipBounds.bottom.toFloat()
                }

                mSrcY[i] = posY
                mDestY[i] = posY
            }
        }

        // Create the path and draw
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) {
                return
            }

            // Find the destination bezier point for a batch
            if (nBatchCount == 0) {
                val randPosY = mDestY[mRandom.nextInt(nPoints)]
                for (i in mSrcY.indices) {
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

                mDestY[mSrcY.size - 1] = randPosY
            }

            // Increment batch count
            nBatchCount++

            // Calculate bar position and draw
            for (i in mSrcY.indices) {
                val barY = mSrcY[i] + ((nBatchCount.toFloat() / mMaxBatchCount) * (mDestY[i] - mSrcY[i]))
                val barX = (i * mBarWidth) + (mBarWidth / 2)
                canvas.drawLine(barX, canvas.height.toFloat(), barX, barY, mPaint)
            }

            // Reset the batch count
            if (nBatchCount == mMaxBatchCount) {
                nBatchCount = 0
            }
        }

        super.onDraw(canvas)
    }
}
