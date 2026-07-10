package com.arrazyfathan.kbbi.core.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat

object VoiceRecognitionUtils {
    const val DEFAULT_LANGUAGE_TAG = "id-ID"
    private const val COMPLETE_SILENCE_TIMEOUT_MILLIS = 3_000L
    private const val POSSIBLY_COMPLETE_SILENCE_TIMEOUT_MILLIS = 2_500L
    private const val MINIMUM_INPUT_LENGTH_MILLIS = 2_000L

    fun createSpeechRecognizerIntent(
        languageTag: String = DEFAULT_LANGUAGE_TAG,
    ): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                COMPLETE_SILENCE_TIMEOUT_MILLIS,
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                POSSIBLY_COMPLETE_SILENCE_TIMEOUT_MILLIS,
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                MINIMUM_INPUT_LENGTH_MILLIS,
            )
        }

    fun isRecognitionAvailable(context: Context): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun hasRecordAudioPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun extractRecognizedTexts(data: Intent?): List<String> =
        data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.filter { it.isNotBlank() }
            .orEmpty()
}

enum class VoiceRecognitionError {
    Network,
    NetworkTimeout,
    NoMatch,
    NoSpeech,
    PermissionDenied,
    RecognizerBusy,
    Server,
    TooManyRequests,
    Client,
    Unknown,
}

class VoiceRecognitionController(
    context: Context,
    private val onReadyForSpeech: () -> Unit = {},
    private val onBeginningOfSpeech: () -> Unit = {},
    private val onPartialResults: (List<String>) -> Unit = {},
    private val onResults: (List<String>) -> Unit = {},
    private val onError: (VoiceRecognitionError) -> Unit = {},
    private val onEndOfSpeech: () -> Unit = {},
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isDestroyed = false
    private val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    init {
        runOnMain {
            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        dispatchCallback { onReadyForSpeech() }
                    }

                    override fun onBeginningOfSpeech() {
                        dispatchCallback { onBeginningOfSpeech() }
                    }

                    override fun onRmsChanged(rmsdB: Float) = Unit

                    override fun onBufferReceived(buffer: ByteArray?) = Unit

                    override fun onEndOfSpeech() {
                        dispatchCallback { onEndOfSpeech() }
                    }

                    override fun onError(error: Int) {
                        dispatchCallback { onError(error.toVoiceRecognitionError()) }
                    }

                    override fun onResults(results: Bundle?) {
                        dispatchCallback { onResults(results.extractRecognizedTexts()) }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        dispatchCallback { onPartialResults(partialResults.extractRecognizedTexts()) }
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?,
                    ) = Unit
                },
            )
        }
    }

    fun startListening(languageTag: String = VoiceRecognitionUtils.DEFAULT_LANGUAGE_TAG) {
        runOnMain {
            speechRecognizer.startListening(VoiceRecognitionUtils.createSpeechRecognizerIntent(languageTag))
        }
    }

    fun stopListening() {
        runOnMain {
            speechRecognizer.stopListening()
        }
    }

    fun cancel() {
        runOnMain {
            speechRecognizer.cancel()
        }
    }

    fun destroy() {
        runOnMain {
            isDestroyed = true
            speechRecognizer.destroy()
            mainHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun dispatchCallback(callback: () -> Unit) {
        runOnMain {
            runCatching {
                if (!isDestroyed) callback()
            }
        }
    }

    private fun runOnMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }
}

private fun Bundle?.extractRecognizedTexts(): List<String> =
    this
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.filter { it.isNotBlank() }
        .orEmpty()

private fun Int.toVoiceRecognitionError(): VoiceRecognitionError =
    when (this) {
        SpeechRecognizer.ERROR_NETWORK -> VoiceRecognitionError.Network
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceRecognitionError.NetworkTimeout
        SpeechRecognizer.ERROR_NO_MATCH -> VoiceRecognitionError.NoMatch
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceRecognitionError.NoSpeech
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceRecognitionError.PermissionDenied
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceRecognitionError.RecognizerBusy
        SpeechRecognizer.ERROR_SERVER -> VoiceRecognitionError.Server
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> VoiceRecognitionError.TooManyRequests
        SpeechRecognizer.ERROR_CLIENT -> VoiceRecognitionError.Client
        else -> VoiceRecognitionError.Unknown
    }
