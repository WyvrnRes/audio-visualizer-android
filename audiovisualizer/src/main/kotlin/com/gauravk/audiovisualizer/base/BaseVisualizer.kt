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
package com.gauravk.audiovisualizer.base

import android.content.Context
import android.graphics.Paint
import android.media.audiofx.Visualizer
import android.util.AttributeSet
import android.view.View
import com.gauravk.audiovisualizer.R
import com.gauravk.audiovisualizer.model.AnimSpeed
import com.gauravk.audiovisualizer.model.PaintStyle
import com.gauravk.audiovisualizer.model.PositionGravity
import com.gauravk.audiovisualizer.utils.AVConstants

/**
 * Base class for the visualizers
 *
 * Created by gk
 */
abstract class BaseVisualizer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    protected var mRawAudioBytes: ByteArray? = null
    protected val mPaint: Paint
    protected var mVisualizer: Visualizer? = null
    protected var mColor: Int = AVConstants.DEFAULT_COLOR

    protected var mPaintStyle: PaintStyle = PaintStyle.FILL
    protected var mPositionGravity: PositionGravity = PositionGravity.BOTTOM

    protected var mStrokeWidth: Float = AVConstants.DEFAULT_STROKE_WIDTH
    protected var mDensity: Float = AVConstants.DEFAULT_DENSITY

    protected var mAnimSpeed: AnimSpeed = AnimSpeed.MEDIUM
    protected var isVisualizationEnabled: Boolean = true

    init {
        initAttributes(context, attrs)
        mPaint = Paint().apply {
            color = mColor
            strokeWidth = mStrokeWidth
            style = if (mPaintStyle == PaintStyle.FILL) Paint.Style.FILL else Paint.Style.STROKE
            isAntiAlias = true // Modern improvement: enable anti-aliasing for smoother visuals
        }
        init()
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            context.theme.obtainStyledAttributes(
                it,
                R.styleable.BaseVisualizer,
                0, 0
            ).apply {
                try {
                    mDensity = getFloat(R.styleable.BaseVisualizer_avDensity, AVConstants.DEFAULT_DENSITY)
                    mColor = getColor(R.styleable.BaseVisualizer_avColor, AVConstants.DEFAULT_COLOR)
                    mStrokeWidth = getDimension(R.styleable.BaseVisualizer_avWidth, AVConstants.DEFAULT_STROKE_WIDTH)

                    getString(R.styleable.BaseVisualizer_avType)?.let { paintType ->
                        mPaintStyle = if (paintType.toLowerCase() == "outline") 
                            PaintStyle.OUTLINE 
                        else 
                            PaintStyle.FILL
                    }

                    getString(R.styleable.BaseVisualizer_avGravity)?.let { gravityType ->
                        mPositionGravity = if (gravityType.toLowerCase() == "top") 
                            PositionGravity.TOP 
                        else 
                            PositionGravity.BOTTOM
                    }

                    getString(R.styleable.BaseVisualizer_avSpeed)?.let { speedType ->
                        mAnimSpeed = when (speedType.toLowerCase()) {
                            "slow" -> AnimSpeed.SLOW
                            "fast" -> AnimSpeed.FAST
                            else -> AnimSpeed.MEDIUM
                        }
                    }
                } finally {
                    recycle()
                }
            }
        }
    }

    /**
     * Set color to visualizer with color resource id.
     *
     * @param color color resource id.
     */
    fun setColor(color: Int) {
        mColor = color
        mPaint.color = mColor
    }

    /**
     * Set the density of the visualizer
     *
     * @param density density for visualization
     */
    fun setDensity(density: Float) {
        synchronized(this) {
            mDensity = density
            init()
        }
    }

    /**
     * Sets the paint style of the visualizer
     *
     * @param paintStyle style of the visualizer.
     */
    fun setPaintStyle(paintStyle: PaintStyle) {
        mPaintStyle = paintStyle
        mPaint.style = if (paintStyle == PaintStyle.FILL) Paint.Style.FILL else Paint.Style.STROKE
    }

    /**
     * Sets the position of the Visualization [PositionGravity]
     *
     * @param positionGravity position of the Visualization
     */
    fun setPositionGravity(positionGravity: PositionGravity) {
        mPositionGravity = positionGravity
    }

    /**
     * Sets the Animation speed of the visualization [AnimSpeed]
     *
     * @param animSpeed speed of the animation
     */
    open fun setAnimationSpeed(animSpeed: AnimSpeed) {
        mAnimSpeed = animSpeed
    }

    /**
     * Sets the width of the outline [PaintStyle]
     *
     * @param width style of the visualizer.
     */
    fun setStrokeWidth(width: Float) {
        mStrokeWidth = width
        mPaint.strokeWidth = width
    }

    /**
     * Sets the audio bytes to be visualized form [Visualizer] or other sources
     *
     * @param bytes of the raw bytes of music
     */
    fun setRawAudioBytes(bytes: ByteArray?) {
        mRawAudioBytes = bytes
        invalidate()
    }

    /**
     * Sets the audio session id for the currently playing audio
     *
     * @param audioSessionId of the media to be visualised
     */
    fun setAudioSessionId(audioSessionId: Int) {
        mVisualizer?.release()

        mVisualizer = Visualizer(audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer,
                        bytes: ByteArray,
                        samplingRate: Int
                    ) {
                        mRawAudioBytes = bytes
                        invalidate()
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer,
                        bytes: ByteArray,
                        samplingRate: Int
                    ) {
                    }
                },
                Visualizer.getMaxCaptureRate() / 2,
                true,
                false
            )
            enabled = true
        }
    }

    /**
     * Releases the visualizer
     */
    fun release() {
        mVisualizer?.release()
    }

    /**
     * Enable Visualization
     */
    fun show() {
        isVisualizationEnabled = true
    }

    /**
     * Disable Visualization
     */
    fun hide() {
        isVisualizationEnabled = false
    }

    protected abstract fun init()
}
