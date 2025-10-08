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
import android.util.AttributeSet
import com.gauravk.audiovisualizer.base.BaseVisualizer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom view to create blast visualizer
 *
 * Created by gk
 */
class BlastVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseVisualizer(context, attrs, defStyleAttr) {

    companion object {
        private const val BLAST_MAX_POINTS = 1000
        private const val BLAST_MIN_POINTS = 3
    }

    private lateinit var mSpikePath: Path
    private var mRadius: Int = -1
    private var nPoints: Int = 0

    override fun init() {
        mRadius = -1
        nPoints = (BLAST_MAX_POINTS * mDensity).toInt()
        if (nPoints < BLAST_MIN_POINTS) {
            nPoints = BLAST_MIN_POINTS
        }

        mSpikePath = Path()
    }

    override fun onDraw(canvas: Canvas) {
        // First time initialization
        if (mRadius == -1) {
            mRadius = if (height < width) height else width
            mRadius = (mRadius * 0.65 / 2).toInt()
        }

        // Create the path and draw
        if (isVisualizationEnabled && mRawAudioBytes != null) {
            val bytes = mRawAudioBytes ?: return
            if (bytes.isEmpty()) {
                return
            }

            mSpikePath.rewind()

            var angle = 0.0
            for (i in 0 until nPoints) {
                val x = ceil(i * (bytes.size.toFloat() / nPoints)).toInt()
                var t = 0
                if (x < 1024) {
                    t = (-abs(bytes[x].toInt()) + 128) * (canvas.height / 4) / 128
                }

                val posX = (width / 2 + (mRadius + t) * cos(Math.toRadians(angle))).toFloat()
                val posY = (height / 2 + (mRadius + t) * sin(Math.toRadians(angle))).toFloat()

                if (i == 0) {
                    mSpikePath.moveTo(posX, posY)
                } else {
                    mSpikePath.lineTo(posX, posY)
                }

                angle += 360.0f / nPoints
            }
            mSpikePath.close()

            canvas.drawPath(mSpikePath, mPaint)
        }

        super.onDraw(canvas)
    }
}
