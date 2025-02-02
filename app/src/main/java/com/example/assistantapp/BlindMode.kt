package com.example.assistantapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BlindModeScreen() {
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current
    LocalLifecycleOwner.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }
    @Composable
    fun BlindModeScreen() {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // State variables to track the responses
        var currentMode by remember { mutableStateOf("navigation") }
        var chatResponse by remember { mutableStateOf("Awaiting command...") }
        var navigationResponse by remember { mutableStateOf("Proceed ahead for 10 meters.") }
        var readingModeResult by remember { mutableStateOf("Reading mode activated.") }
        var lastSpokenIndex by remember { mutableStateOf(0) }
        var aiResponse by remember { mutableStateOf("") }  // Store dynamic responses here

        val tts = remember {
            mutableStateOf<TextToSpeech?>(
                TextToSpeech(context) { status ->
                    if (status != TextToSpeech.ERROR) {


                    }
                }
            )
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Simulate an AI response from an API (replace this with actual API call)
                        aiResponse = sendMessageToGeminiAI("What is my location?", navigationResponse)
                        chatResponse = aiResponse  // Update the response dynamically
                        tts.value?.speak(aiResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Fetch AI Response")
            }

            // Pass updated variables to AIResponseOverlay
            AIResponseOverlay(
                currentMode = currentMode,
                navigationResponse = navigationResponse,
                chatResponse = chatResponse,
                readingModeResult = readingModeResult,
                tts = tts.value,
                lastSpokenIndex = lastSpokenIndex,
                response = aiResponse
            )
        }
    }
    var currentMode by remember { mutableStateOf("navigation") }
    var overlayText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var isAssistantMode by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(true) } // Start session immediately
    var analysisResult by remember { mutableStateOf("") }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var lastSpokenIndex by remember { mutableStateOf(0) }
    var lastProcessedTimestamp by remember { mutableStateOf(0L) }
    val frameInterval = 12000 // Process a frame every 6.5 seconds
    var navigationPaused by remember { mutableStateOf(false) }
    var isMicActive by remember { mutableStateOf(false) }
    var chatResponse by remember { mutableStateOf("") }
    var isReadingMode by remember { mutableStateOf(false) }
    var readingModeResult by remember { mutableStateOf("") }
    // State variables to track the responses

    var navigationResponse by remember { mutableStateOf("Proceed ahead for 10 meters.") }

    var aiResponse by remember { mutableStateOf("") }  // This will store API responses dynamically.


    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    LaunchedEffect(context) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set English as the default language
                val result = tts.value?.setLanguage(Locale.US)

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.value?.speak(
                        "English language not supported on this device.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                } else {
                    // Set custom speed and pitch for the assistant's voice
                    tts.value?.setSpeechRate(1.2f)  // Adjust the speech rate (1.0 is normal)
                    tts.value?.setPitch(0.9f)       // Slightly deeper pitch for a male voice

                    // Set a custom male voice if available
                    val availableVoices = tts.value?.voices
                    availableVoices?.let {
                        val maleVoice = it.find { voice ->
                            voice.name.contains("male", ignoreCase = true) ||
                                    voice.name.contains("en", ignoreCase = true) ||
                                    voice.name.contains("man", ignoreCase = true)
                        } ?: it.first()  // Fallback to the first voice if no male voice is found

                        tts.value?.voice = maleVoice
                    }

                    // Speak an initial message to confirm setup
                    tts.value?.speak(
                        "Hello, I am your assistant. And my name is Jimmy. I am delighted to be presented at SRM Techno hackathon. How are you, panelists? I am happy to meet you all. I will not take much of your time. I am an AI assistant to help visually impaired people. Please hold the camera in the direction where you want to navigate.",
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        null
                    )
                }
            }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
            speechRecognizer.destroy()
        }
    }
    // Add this function in the same file
    fun suddenAlert(tts: TextToSpeech?, alertMessage: String) {
        tts?.speak(alertMessage, TextToSpeech.QUEUE_FLUSH, null, null)
    }


    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    coroutineScope.launch {
                        chatResponse = sendMessageToGeminiAI(spokenText, analysisResult)
                        tts.value?.speak(chatResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                // Restart listening on end of speech, if navigation is paused
                if (navigationPaused) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onError(error: Int) {
                // Restart listening on error, if navigation is paused
                if (navigationPaused) {
                    speechRecognizer.startListening(speechIntent)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // Effect to handle microphone activation when navigation is paused
    LaunchedEffect(navigationPaused) {
        if (navigationPaused) {
            isMicActive = true
            speechRecognizer.startListening(speechIntent)
        } else {
            isMicActive = false
            speechRecognizer.stopListening()
            // Clear chatResponse to display the analysis result when resuming navigation
            chatResponse = ""
        }
    }

    if (hasPermission) {
        if (sessionStarted) {
            if (isReadingMode) {
                ReadingModeCamera(
                    onImageCaptured = { bitmap: Bitmap ->
                        capturedImage = bitmap
                        coroutineScope.launch {
                            readingModeResult = ""
                            sendFrameToGemini2AI(bitmap, { partialResult ->
                                readingModeResult += partialResult
                                tts.value?.speak(partialResult, TextToSpeech.QUEUE_ADD, null, null)
                            }, { error ->
                                // Handle error
                            })
                        }
                    },

                    cameraExecutor = cameraExecutor
                )
            } else if (!navigationPaused) {
                CameraPreviewWithAnalysis { imageProxy ->
                    val currentTimestamp = System.currentTimeMillis()
                    if (currentTimestamp - lastProcessedTimestamp >= frameInterval) {
                        coroutineScope.launch {
                            val bitmap = imageProxy.toBitmap()
                            if (bitmap != null) {
                                sendFrameToGeminiAI(bitmap, { partialResult ->
                                    analysisResult += " $partialResult"
                                    val newText = analysisResult.substring(lastSpokenIndex)
                                    tts.value?.speak(newText, TextToSpeech.QUEUE_ADD, null, null)
                                    lastSpokenIndex = analysisResult.length
                                }, { error ->
                                    // Handle error here
                                })
                                lastProcessedTimestamp = currentTimestamp
                            }
                            imageProxy.close()
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            }
        }
    } else {
        ActivityCompat.requestPermissions(
            (context as Activity),
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            1
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (!isReadingMode) {
                            navigationPaused = !navigationPaused
                            isAssistantMode = navigationPaused
                            if (navigationPaused) {
                                tts.value?.stop()
                                currentMode = "assistant"
                                overlayText = ""
                                tts.value?.speak("Assistant mode activated.", TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts.value?.stop()
                                currentMode = "navigation"
                                overlayText = ""
                                chatResponse = ""
                                tts.value?.speak("Assistant mode deactivated.", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                    },
                    onLongPress = {
                        if (!isAssistantMode) {
                            isReadingMode = !isReadingMode
                            if (isReadingMode) {
                                tts.value?.stop()
                                currentMode = "reading"
                                overlayText = ""
                                navigationPaused = true
                                tts.value?.speak("Entering reading mode", TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts.value?.stop()
                                currentMode = "navigation"
                                overlayText = ""
                                readingModeResult = ""
                                navigationPaused = false
                                tts.value?.speak("Exiting reading mode", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } else {
                            // Exit assistant mode and enter navigation mode
                            tts.value?.stop()
                            isAssistantMode = false
                            navigationPaused = false
                            isReadingMode = false
                            currentMode = "navigation"
                            overlayText = ""
                            chatResponse = ""
                            tts.value?.speak("Exiting assistant mode, entering navigation mode", TextToSpeech.QUEUE_FLUSH, null, null)
                        }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (sessionStarted) {
                AIResponseOverlay(
                    currentMode = currentMode,
                    navigationResponse = analysisResult,
                    response = analysisResult,
                    chatResponse = chatResponse,
                    readingModeResult = readingModeResult,
                    tts = tts.value,
                    lastSpokenIndex = lastSpokenIndex
                )
            }
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = "Book Icon",
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(64.dp),
                tint = if (isReadingMode) Color.Green else Color(0xFFB0B1B1)
            )
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = "Mic Icon",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(64.dp),
                tint = if (isMicActive) Color.Green else Color(0xFFB0B1B1)
            )
        }
    }
}



@Composable
fun ReadingModeCamera(
    onImageCaptured: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Capture image once when reading mode is activated
        val outputOptions = ImageCapture.OutputFileOptions.Builder(createTempFile(context.toString())).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: return
                    val bitmap = BitmapFactory.decodeFile(savedUri.path)
                    onImageCaptured(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    // Handle error
                }
            }
        )
    }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}

