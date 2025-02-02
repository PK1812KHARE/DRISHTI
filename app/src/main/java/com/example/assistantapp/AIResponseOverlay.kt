package com.example.assistantapp

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AIResponseOverlay(
    currentMode: String,
    navigationResponse: String,
    chatResponse: String,
    readingModeResult: String,
    tts: TextToSpeech?,
    lastSpokenIndex: Int,
    response: String
) {
    var currentIndex by remember { mutableStateOf(lastSpokenIndex) }
    val sentences = response.split(".")
    var lastSpokenText by remember { mutableStateOf("") }

    // Automatically cycle through sentences every 8 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(8000)
            if (sentences.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % sentences.size
                val newText = sentences[currentIndex].trim()
                if (newText.isNotEmpty() && newText != lastSpokenText) {
                    tts?.speak(newText, TextToSpeech.QUEUE_FLUSH, null, null)
                    lastSpokenText = newText
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1976D2),  // Blue shade
                            Color(0xFF004BA0)
                        )
                    )
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                when (currentMode) {
                    "reading" -> {
                        Text(
                            text = "📖 Reading: $readingModeResult",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    "assistant" -> {
                        Text(
                            text = "🤖 Chat: $chatResponse",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    "navigation" -> {
                        Text(
                            text = "🧭 Navigation: $navigationResponse",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
