package com.samrudhasolutions.visualizer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.media.AudioRecord
import android.util.AttributeSet
import android.view.View

class VisualizerView : View {

    private var audioRecord: AudioRecord? = null
    private val paint: Paint = Paint()
    private val lineSpacing = 4f // Spacing between lines in the visualizer
    private val lineThickness = 10f // Thickness of each line in the visualizer
    private val lineColor = 0xFF000000.toInt() // Green color for the visualizer lines



    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.color = lineColor
        paint.strokeWidth = lineThickness
    }

    fun start(audioRecord: AudioRecord) {
        this.audioRecord = audioRecord

        // Calculate buffer size based on audio record parameters


        // Start the audio record and invalidate the view
        audioRecord.startRecording()
        invalidate() // Trigger a redraw to start the visualizer
    }


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val audioRecord = this.audioRecord ?: return
        val bufferSizeInBytes = audioRecord.let {
            AudioRecord.getMinBufferSize(
                it.sampleRate,
                audioRecord.channelConfiguration,
                audioRecord.audioFormat
            )
        }

        val audioData = ByteArray(bufferSizeInBytes)
        val amplitude = FloatArray(bufferSizeInBytes / 2)

        audioRecord.read(audioData, 0, bufferSizeInBytes)

        for (i in 0 until bufferSizeInBytes / 2) {
            val magnitude = (audioData[i * 2 + 1].toInt() shl 8 or (audioData[i * 2].toInt() and 0xFF)).toFloat()
            amplitude[i] = magnitude / 32767.0f // Normalize to [-1.0, 1.0] range
        }

        canvas.drawLines(getLines(amplitude), paint)
        invalidate() // Continuously redraw for real-time visualizer
    }



    private fun getLines(amplitude: FloatArray): FloatArray {
        val lines = FloatArray(amplitude.size * 4) // Each amplitude value corresponds to 2 lines (start and end points)

        val maxAmplitude = 32767.0f // Maximum amplitude value for normalization
        val sensitivity = 0.5f // Adjust sensitivity as needed (0.5 means half sensitivity)

        for (i in amplitude.indices) {
            val scaledAmplitude = amplitude[i] * sensitivity // Scale amplitude based on sensitivity
            val y = (1 - scaledAmplitude) * height / 2

            val x = i * (width.toFloat() / (amplitude.size - 1))
            lines[i * 4] = x
            lines[i * 4 + 1] = y + lineSpacing
            lines[i * 4 + 2] = x
            lines[i * 4 + 3] = y - lineSpacing
        }

        return lines
    }

}
