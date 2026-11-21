package com.example.core.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SpeechCapabilities(
    val ttsReady: Boolean = false,
    val frenchOfflineVoiceAvailable: Boolean = false,
    val ttsStatusMessage: String = "Initialisation de la synthèse vocale locale...",
    val sttAvailable: Boolean = false,
    val sttStatusMessage: String = "Vérification de la reconnaissance vocale hors ligne..."
)

/**
 * Gestionnaire local de synthèse vocale (TTS) et reconnaissance vocale (STT).
 * Détecte explicitement la disponibilité hors ligne et ne suppose jamais
 * qu'un moteur vocal distant ou local est présent sur tous les appareils.
 */
class LocalSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _capabilities = MutableStateFlow(SpeechCapabilities())
    val capabilities: StateFlow<SpeechCapabilities> = _capabilities.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _sttError = MutableStateFlow<String?>(null)
    val sttError: StateFlow<String?> = _sttError.asStateFlow()

    init {
        initializeTts()
        checkSttCapability()
    }

    private fun initializeTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.FRENCH)
                    val frenchAvailable = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED
                    val hasOfflineFrench = try {
                        tts?.voices?.any { voice ->
                            voice.locale.language.equals("fr", ignoreCase = true) && !voice.isNetworkConnectionRequired
                        } == true
                    } catch (_: Exception) {
                        frenchAvailable
                    }

                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }
                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                        }
                    })

                    _capabilities.value = _capabilities.value.copy(
                        ttsReady = frenchAvailable,
                        frenchOfflineVoiceAvailable = hasOfflineFrench,
                        ttsStatusMessage = when {
                            hasOfflineFrench -> "Voix française hors ligne détectée et prête."
                            frenchAvailable -> "Voix française détectée (vérifiez le pack vocal hors ligne dans les paramètres Android)."
                            else -> "Voix française locale absente : le mode lecture visuelle rythmée prend le relais."
                        }
                    )
                } else {
                    _capabilities.value = _capabilities.value.copy(
                        ttsReady = false,
                        frenchOfflineVoiceAvailable = false,
                        ttsStatusMessage = "Synthèse vocale indisponible : mode lecture visuelle rythmée activé."
                    )
                }
            }
        } catch (_: Exception) {
            _capabilities.value = _capabilities.value.copy(
                ttsReady = false,
                frenchOfflineVoiceAvailable = false,
                ttsStatusMessage = "Synthèse vocale non disponible sur cet appareil : mode visuel actif."
            )
        }
    }

    private fun checkSttCapability() {
        val available = try {
            SpeechRecognizer.isRecognitionAvailable(context.applicationContext)
        } catch (_: Exception) {
            false
        }
        _capabilities.value = _capabilities.value.copy(
            sttAvailable = available,
            sttStatusMessage = if (available) {
                "Reconnaissance vocale système détectée (mode hors ligne prioritaire). Si aucun pack vocal français hors ligne n'est installé sur l'appareil, utilisez le mode entraînement guidé."
            } else {
                "Aucun moteur de reconnaissance vocale hors ligne installé sur cet appareil. Le mode pratique phonétique et dictée manuelle reste disponible."
            }
        )
    }

    fun speak(text: String, speechRate: Float = 0.92f) {
        val engine = tts ?: return
        if (!_capabilities.value.ttsReady) return
        engine.setSpeechRate(speechRate.coerceIn(0.6f, 1.4f))
        _isSpeaking.value = true
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "francais_pro_utterance")
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startOfflineListening() {
        _sttError.value = null
        if (!_capabilities.value.sttAvailable) {
            _sttError.value = "Aucun moteur vocal hors ligne détecté. Installez le pack français hors ligne dans Paramètres Android > Langues, ou utilisez la saisie manuelle."
            return
        }
        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }
                    override fun onError(error: Int) {
                        _isListening.value = false
                        _sttError.value = when (error) {
                            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Un pack vocal français hors ligne supplémentaire est requis dans les réglages Android pour la reconnaissance en mode avion."
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Autorisation microphone requise pour la reconnaissance vocale."
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "Aucune parole reconnue clairement. Parlez posément près du micro ou utilisez l'auto-évaluation guidée."
                            else ->
                                "Reconnaissance vocale interrompue (code $error). Le mode entraînement guidé reste accessible hors ligne."
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        _recognizedText.value = matches?.firstOrNull().orEmpty()
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _recognizedText.value = matches.first()
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {
            _isListening.value = false
            _sttError.value = "Impossible de démarrer la reconnaissance locale. Utilisez le mode d'auto-évaluation guidée."
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
        _isListening.value = false
    }

    fun setManualTranscript(text: String) {
        _recognizedText.value = text
    }

    fun clearSttError() {
        _sttError.value = null
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
