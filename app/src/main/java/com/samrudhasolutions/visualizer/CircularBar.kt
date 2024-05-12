package com.samrudhasolutions.visualizer

import android.content.Context
import android.graphics.*
import android.media.AudioRecord
import android.util.AttributeSet
import android.view.View

class CircularBar : View {

    private var audioRecord: AudioRecord? = null
    private val paint: Paint = Paint()
    private val barCount = 100 // Number of bars in the circular pattern
    private val barSpacing = 4f // Spacing between bars
    private val barThickness = 16f // Thickness of each bar
    private val maxBarHeight = 200f // Maximum height of bars
    private val baseRadius = 200f // Radius of the circular pattern
    private val baseAngle = 360 / barCount // Angle between each bar
    private val gradientColors = intArrayOf(
        Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA
    ) // Gradient colors for bars
    private lateinit var gradientShader: Shader // Gradient shader for bars
    private lateinit var gradientMatrix: Matrix // Matrix for gradient animation
    private var gradientPosition = 0f // Current position of gradient

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        paint.strokeWidth = barThickness
        paint.style = Paint.Style.STROKE
        gradientShader = LinearGradient(
            0f, 0f, 0f, maxBarHeight,
            gradientColors, null, Shader.TileMode.CLAMP
        )
        gradientMatrix = Matrix()
        paint.shader = gradientShader
        startGradientAnimation()
    }

    private fun startGradientAnimation() {
        post(object : Runnable {
            override fun run() {
                gradientPosition += 0.01f // Adjust the speed of gradient animation here
                gradientMatrix.setRotate(gradientPosition * 360, width / 2f, height / 2f)
                gradientShader.setLocalMatrix(gradientMatrix)
                invalidate()
                postDelayed(this, 16) // Update every 16 milliseconds (60 FPS)
            }
        })
    }

    fun start(audioRecord: AudioRecord) {
        this.audioRecord = audioRecord
        invalidate() // Trigger a redraw to start the visualizer
    }

    fun stop() {
        this.audioRecord = null
        invalidate() // Trigger a redraw to stop the visualizer
    }

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

        canvas.save()
        canvas.translate(width / 2f, height / 2f) // Translate canvas to center
        for (i in 0 until barCount) {
            val angle = i * baseAngle
            val barHeight = maxBarHeight * amplitude[i % amplitude.size]
            val x1 = baseRadius * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val y1 = baseRadius * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            val x2 = (baseRadius + barHeight) * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val y2 = (baseRadius + barHeight) * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            canvas.drawLine(x1, y1, x2, y2, paint)
            canvas.rotate(baseAngle.toFloat()) // Rotate canvas for the next bar
        }
        canvas.restore()
        invalidate() // Continuously redraw for real-time visualizer
    }
}
