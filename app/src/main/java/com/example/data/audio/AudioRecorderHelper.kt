package com.example.data.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.OutputStream

class AudioRecorderHelper {

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(2048)

    @Volatile
    private var isRecording = false
    private var audioRecord: AudioRecord? = null
    private val outputStream = ByteArrayOutputStream()

    @SuppressLint("MissingPermission")
    fun startRecording(): Boolean {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return false
            }

            outputStream.reset()
            isRecording = true
            audioRecord?.startRecording()

            Thread {
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (read > 0) {
                        synchronized(outputStream) {
                            outputStream.write(buffer, 0, read)
                        }
                    }
                }
            }.start()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            isRecording = false
            return false
        }
    }

    suspend fun stopRecordingAndGetWavBase64(): String? = withContext(Dispatchers.IO) {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val pcmData = synchronized(outputStream) {
            outputStream.toByteArray()
        }

        if (pcmData.isEmpty()) return@withContext null

        val wavData = ByteArrayOutputStream()
        writeWavHeader(wavData, pcmData.size.toLong(), sampleRate, 1, 16)
        wavData.write(pcmData)

        Base64.encodeToString(wavData.toByteArray(), Base64.NO_WRAP)
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    private fun writeWavHeader(
        out: OutputStream,
        totalAudioLen: Long,
        longSampleRate: Int,
        channels: Int,
        byteRate: Int
    ) {
        val totalDataLen = totalAudioLen + 36
        val dataRate = (longSampleRate * channels * byteRate / 8).toLong()

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xffL).toByte()
        header[5] = ((totalDataLen shr 8) and 0xffL).toByte()
        header[6] = ((totalDataLen shr 16) and 0xffL).toByte()
        header[7] = ((totalDataLen shr 24) and 0xffL).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1 (PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (dataRate and 0xffL).toByte()
        header[29] = ((dataRate shr 8) and 0xffL).toByte()
        header[30] = ((dataRate shr 16) and 0xffL).toByte()
        header[31] = ((dataRate shr 24) and 0xffL).toByte()
        header[32] = (channels * byteRate / 8).toByte() // block align
        header[33] = 0
        header[34] = byteRate.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xffL).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xffL).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xffL).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xffL).toByte()

        out.write(header, 0, 44)
    }
}
