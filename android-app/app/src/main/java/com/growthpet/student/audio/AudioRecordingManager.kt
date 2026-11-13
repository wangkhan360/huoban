package com.growthpet.student.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecordingManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var exoPlayer: ExoPlayer? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    private var currentOutputFile: File? = null

    /**
     * Recording may only start after explicit child authorization.
     */
    fun startAuthorizedRecording(childAuthorized: Boolean): Result<File> {
        if (!childAuthorized) {
            return Result.failure(SecurityException("需要孩子主动点击授权后才能开始录音"))
        }
        val dir = File(context.filesDir, "reading_recordings").apply { mkdirs() }
        val file = File(dir, "reading_${UUID.randomUUID()}.m4a")
        currentOutputFile = file

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return runCatching {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _isRecording.value = true
            file
        }.onFailure {
            _isRecording.value = false
        }
    }

    fun stopRecording(): File? {
        runCatching {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        }
        mediaRecorder = null
        _isRecording.value = false
        return currentOutputFile
    }

    fun playPreview(filePath: String) {
        val player = exoPlayer ?: ExoPlayer.Builder(context).build().also { exoPlayer = it }
        player.setMediaItem(MediaItem.fromUri(File(filePath).toURI().toString()))
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                    _isPlayingPreview.value = false
                }
            }
        })
        player.prepare()
        player.play()
        _isPlayingPreview.value = true
    }

    fun stopPreview() {
        exoPlayer?.stop()
        _isPlayingPreview.value = false
    }
}
