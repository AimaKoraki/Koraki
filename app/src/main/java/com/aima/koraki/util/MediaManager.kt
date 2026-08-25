package com.aima.koraki.util

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentRecordingPath: String? = null

    init {
        File(context.filesDir, "images").mkdirs()
        File(context.filesDir, "audio").mkdirs()
    }

    /**
     * Copies a file from the provided content URI to the internal `filesDir/images` directory.
     * Returns the absolute file path of the copied image.
     */
    suspend fun copyImageToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val outputFile = File(File(context.filesDir, "images"), fileName)
            
            outputFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Starts recording audio to the internal `filesDir/audio` directory.
     */
    fun startRecording() {
        val fileName = "audio_${UUID.randomUUID()}.m4a"
        val outputFile = File(File(context.filesDir, "audio"), fileName)
        currentRecordingPath = outputFile.absolutePath

        mediaRecorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentRecordingPath)
            prepare()
            start()
        }
    }

    /**
     * Stops recording audio and returns the absolute file path of the recorded file.
     */
    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentRecordingPath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Plays the audio file at the specified path.
     */
    fun playAudio(path: String) {
        stopAudio() // Stop any currently playing audio
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
            setOnCompletionListener {
                it.release()
                mediaPlayer = null
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}
