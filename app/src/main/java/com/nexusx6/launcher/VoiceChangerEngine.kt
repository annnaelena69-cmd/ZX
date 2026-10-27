package com.nexusx6.launcher

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.media.AudioManager
import android.os.Process
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Low-latency live microphone monitor with a raised playback rate/pitch.
 * This changes audio heard through this app; Android apps cannot inject this
 * processed signal into arbitrary third-party games' microphone streams.
 */
class VoiceChangerEngine {
    private val running = AtomicBoolean(false)
    @Volatile private var worker: Thread? = null
    @Volatile private var recorder: AudioRecord? = null
    @Volatile private var player: AudioTrack? = null

    fun start(pitchRate: Float) {
        stop()
        val rate = pitchRate.coerceIn(1.0f, 1.7f)
        val sampleRate = 44100
        val minIn = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val minOut = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minIn <= 0 || minOut <= 0) throw IllegalStateException("Perangkat tidak mendukung format audio ini.")
        val bufferBytes = maxOf(minIn, minOut, 4096) * 2
        val input = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_IN_MONO).build())
            .setBufferSizeInBytes(bufferBytes)
            .build()
        val output = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(bufferBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        try {
            output.setPlaybackRate((sampleRate * rate).toInt())
            input.startRecording()
            output.play()
            recorder = input
            player = output
            running.set(true)
            worker = thread(name = "NexusLiveVoice", isDaemon = true) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                val pcm = ShortArray(1024)
                try {
                    while (running.get() && !Thread.currentThread().isInterrupted) {
                        val count = input.read(pcm, 0, pcm.size, AudioRecord.READ_BLOCKING)
                        if (count > 0 && running.get()) output.write(pcm, 0, count, AudioTrack.WRITE_BLOCKING)
                    }
                } catch (_: Exception) {
                    // Audio devices can be disconnected while the monitor is active.
                } finally {
                    running.set(false)
                }
            }
        } catch (e: Exception) {
            running.set(false)
            try { input.release() } catch (_: Exception) {}
            try { output.release() } catch (_: Exception) {}
            throw e
        }
    }

    fun stop() {
        running.set(false)
        try { recorder?.stop() } catch (_: Exception) {}
        try { player?.pause() } catch (_: Exception) {}
        try { player?.flush() } catch (_: Exception) {}
        try { worker?.join(250) } catch (_: Exception) {}
        try { recorder?.release() } catch (_: Exception) {}
        try { player?.stop() } catch (_: Exception) {}
        try { player?.release() } catch (_: Exception) {}
        recorder = null
        player = null
        worker = null
    }

    fun isRunning(): Boolean = running.get()
}
