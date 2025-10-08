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
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import com.gauravk.audiovisualizer.model.AnimSpeed
import com.gauravk.audiovisualizer.model.PaintStyle
import com.gauravk.audiovisualizer.utils.BezierSpline
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom view to create blob visualizer
 *
 * Created by gk
 */
class BlobVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val BLOB_MAX_POINTS = 60
        private const val BLOB_MIN_POINTS = 3
    }

    private lateinit var mBlobPath: Path
    private var mRadius: Int = -1

    private var nPoints: Int = 0

    private lateinit var mBezierPoints: Array<PointF>
    private lateinit var mBezierSpline: BezierSpline

    private var mAngleOffset: Float = 0f
    private var mChangeFactor: Float = 0f

    override fun init() {
        mRadius = -1
        nPoints = (mDensity * BLOB_MAX_POINTS).toInt()
        if (nPoints < BLOB_MIN_POINTS) {
            nPoints = BLOB_MIN_POINTS
        }

        mAngleOffset = 360.0f / nPoints

        updateChangeFactor(mAnimSpeed, false)

        mBlobPath = Path()

        // Initialize mBezierPoints, 2 extra for the smoothing first and last point
        mBezierPoints = Array(nPoints + 2) { PointF() }

        mBezierSpline = BezierSpline(mBezierPoints.size)
    }

    override fun setAnimationSpeed(animSpeed: AnimSpeed) {
        super.setAnimationSpeed(animSpeed)
        updateChangeFactor(animSpeed, true)
    }

    private fun updateChangeFactor(animSpeed: AnimSpeed, useHeight: Boolean) {
        var height = 1
        if (useHeight) {
            height = if (getHeight() > 0) getHeight() else 1000
        }

        mChangeFactor = when (animSpeed) {
            AnimSpeed.SLOW -> height * 0.003f
            AnimSpeed.MEDIUM -> height * 0.006f
            AnimSpeed.FAST -> height * 0.01f
        }
    }

    override fun onDraw(canvas: Canvas) {
        var angle = 0.0

        // First time initialization
        if (mRadius == -1) {
            mRadius = if (height < width) height else width
            mRadius = (mRadius * 0.65 / 2).toInt()

            mChangeFactor *= height

            // Initialize bezier points
            for (i in 0 until nPoints) {
                val posX = (width / 2 + mRadius * cos(Math.toRadians(angle))).toFloat()
                val posY = (height / 2 + mRadius * sin(Math.toRadians(angle))).toFloat()
                mBezierPoints[i].set(posX, posY)
                angle += mAngleOffset
            }
        }

        // Create the path and draw
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) {
                return
            }

            mBlobPath.rewind()
            angle = 0.0

            // Find the destination bezier point for a batch
            for (i in 0 until nPoints) {
                val x = ceil((i + 1) * (bytes.size.toFloat() / nPoints)).toInt()
                var t = 0
                if (x < 1024) {
                    t = (-kotlin.math.abs(bytes[x].toInt()) + 128) * (canvas.height / 4) / 128
                }

                val posX = (width / 2 + (mRadius + t) * cos(Math.toRadians(angle))).toFloat()
                val posY = (height / 2 + (mRadius + t) * sin(Math.toRadians(angle))).toFloat()

                // Calculate the new x based on change
                if (posX - mBezierPoints[i].x > 0) {
                    mBezierPoints[i].x += mChangeFactor
                } else {
                    mBezierPoints[i].x -= mChangeFactor
                }

                // Calculate the new y based on change
                if (posY - mBezierPoints[i].y > 0) {
                    mBezierPoints[i].y += mChangeFactor
                } else {
                    mBezierPoints[i].y -= mChangeFactor
                }

                angle += mAngleOffset
            }

            // Set the first and last point as first
            mBezierPoints[nPoints].set(mBezierPoints[0].x, mBezierPoints[0].y)
            mBezierPoints[nPoints + 1].set(mBezierPoints[0].x, mBezierPoints[0].y)

            // Update the control points
            mBezierSpline.updateCurveControlPoints(mBezierPoints)
            val firstCP = mBezierSpline.firstControlPoints
            val secondCP = mBezierSpline.secondControlPoints

            // Create the path
            mBlobPath.moveTo(mBezierPoints[0].x, mBezierPoints[0].y)
            for (i in firstCP.indices) {
                mBlobPath.cubicTo(
                    firstCP[i].x, firstCP[i].y,
                    secondCP[i].x, secondCP[i].y,
                    mBezierPoints[i + 1].x, mBezierPoints[i + 1].y
                )
            }

            // Add an extra line to center cover the gap generated by last cubicTo
            if (mPaintStyle == PaintStyle.FILL) {
                mBlobPath.lineTo((width / 2).toFloat(), (height / 2).toFloat())
            }

            canvas.drawPath(mBlobPath, mPaint)
        }

        super.onDraw(canvas)
    }
}
