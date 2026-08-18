package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundLibrary {

    data class SoundItem(
        val title: String,
        val id: String,
        val category: String,
        val description: String
    )

    val PRESET_SOUNDS = listOf(
        SoundItem("Zenith Chimes", "preset_zenith", "Chimes", "Crystal melodic morning tones"),
        SoundItem("Cyber Pulse", "preset_cyber", "Electronic", "Futuristic synth alert pulse"),
        SoundItem("Dawn Breeze", "preset_dawn", "Relaxing", "Soft ascending breeze melody"),
        SoundItem("Gentle Harp", "preset_harp", "Acoustic", "Warm acoustic harp arpeggio"),
        SoundItem("Energetic Brass", "preset_brass", "Loud", "High impact rhythmic brass alarm"),
        SoundItem("Synthwave Rise", "preset_synthwave", "Electronic", "Retro 80s synth rise scale"),
        SoundItem("Morning Symphony", "preset_symphony", "Classical", "Bright orchestrated wake up notes"),
        SoundItem("Roost Rooster", "preset_rooster", "Natural", "Classic rooster crowing rhythm"),
        SoundItem("Heavy Beat", "preset_heavy", "Loud", "Deep bass drum pulse and bell")
    )

    private var audioTrackPlayer: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: Job? = null

    fun playSound(context: Context, soundUri: String, initialVolume: Float, scope: CoroutineScope) {
        stopSound()

        if (soundUri.startsWith("content://") || soundUri.startsWith("file://")) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    setDataSource(context, Uri.parse(soundUri))
                    isLooping = true
                    setVolume(initialVolume, initialVolume)
                    prepare()
                    start()
                }
                return
            } catch (e: Exception) {
                Log.e("SoundLibrary", "Failed to play imported URI, falling back to synth", e)
            }
        }

        // Play synthesized melodic patterns based on preset sound id
        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        audioTrackPlayer = AudioTrack.Builder()
            .setAudioAttributes(attributes)
            .setAudioFormat(format)
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrackPlayer?.play()

        playbackJob = scope.launch(Dispatchers.Default) {
            val notes = getMelodyNotes(soundUri)
            var currentVol = initialVolume

            while (isActive) {
                for (note in notes) {
                    if (!isActive) break
                    val durationMs = note.second
                    val freq = note.first
                    val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                    val buffer = ShortArray(numSamples)

                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val envelope = if (i < 500) i / 500.0 else if (i > numSamples - 1000) (numSamples - i) / 1000.0 else 1.0
                        val sampleValue = (sin(2.0 * Math.PI * freq * t) * 32767 * currentVol * envelope).toInt()
                        buffer[i] = sampleValue.coerceIn(-32768, 32767).toShort()
                    }

                    audioTrackPlayer?.write(buffer, 0, buffer.size)
                    delay(50)
                }
                delay(300)
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(clamped, clamped)
        // audioTrackPlayer volume is adjusted per sample envelope
    }

    fun stopSound() {
        playbackJob?.cancel()
        playbackJob = null

        try {
            audioTrackPlayer?.stop()
            audioTrackPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        audioTrackPlayer = null

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
    }

    private fun getMelodyNotes(presetId: String): List<Pair<Double, Long>> {
        return when (presetId) {
            "preset_cyber" -> listOf(
                Pair(523.25, 120L), Pair(659.25, 120L), Pair(783.99, 120L),
                Pair(1046.50, 200L), Pair(783.99, 120L), Pair(1046.50, 300L)
            )
            "preset_dawn" -> listOf(
                Pair(440.00, 250L), Pair(554.37, 250L), Pair(659.25, 250L),
                Pair(880.00, 500L), Pair(659.25, 250L), Pair(880.00, 500L)
            )
            "preset_harp" -> listOf(
                Pair(329.63, 200L), Pair(392.00, 200L), Pair(493.88, 200L),
                Pair(587.33, 200L), Pair(659.25, 400L)
            )
            "preset_brass" -> listOf(
                Pair(587.33, 150L), Pair(587.33, 150L), Pair(587.33, 150L),
                Pair(880.00, 400L), Pair(659.25, 200L), Pair(880.00, 400L)
            )
            "preset_synthwave" -> listOf(
                Pair(220.00, 150L), Pair(277.18, 150L), Pair(329.63, 150L),
                Pair(440.00, 150L), Pair(554.37, 150L), Pair(659.25, 300L)
            )
            "preset_symphony" -> listOf(
                Pair(523.25, 200L), Pair(587.33, 200L), Pair(659.25, 200L),
                Pair(698.46, 200L), Pair(783.99, 400L)
            )
            "preset_rooster" -> listOf(
                Pair(392.00, 150L), Pair(523.25, 150L), Pair(659.25, 150L),
                Pair(783.99, 500L), Pair(659.25, 200L), Pair(783.99, 600L)
            )
            "preset_heavy" -> listOf(
                Pair(150.00, 100L), Pair(880.00, 200L), Pair(150.00, 100L),
                Pair(987.77, 200L), Pair(1046.50, 400L)
            )
            else -> listOf( // "preset_zenith"
                Pair(587.33, 200L), Pair(659.25, 200L), Pair(783.99, 200L),
                Pair(880.00, 300L), Pair(1046.50, 400L)
            )
        }
    }
}
