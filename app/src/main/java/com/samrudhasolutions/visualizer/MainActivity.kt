package com.samrudhasolutions.visualizer

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var circularBarVisualizerView: CircularBar
    private lateinit var audioRecord: AudioRecord
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        circularBarVisualizerView = findViewById(R.id.visualizerView)
        val startButton: Button = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            if (!isRecording) {
                if (checkPermissions()) {
                    startRecording()
                    circularBarVisualizerView.start(audioRecord)
                    isRecording = true
                } else {
                    requestPermissions()
                }
            }
        }

    }

    private fun checkPermissions(): Boolean {
        val recordPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        return recordPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            RECORD_AUDIO_PERMISSION_CODE
        )
    }

    private fun startRecording() {
        val audioSource = MediaRecorder.AudioSource.MIC
        val sampleRate = 44100
        val channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO
        val audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord.startRecording()
    }

    private fun stopRecording() {
        if (isRecording) {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
                circularBarVisualizerView.start(audioRecord)
            }
        }
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 101
    }
}
