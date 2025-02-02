package com.example.assistantapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun MainPage(navController: NavHostController) {
    var startAnimation by remember { mutableStateOf(false) }

    // Start animation after a small delay
    LaunchedEffect(Unit) {
        delay(300)
        startAnimation = true
    }

    Scaffold(
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B1F3A),
                                Color(0xFF0F0B29)
                            )
                        )
                    )
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 24.dp)
                ) {
                    // Title Section
                    AnimatedVisibility(
                        visible = startAnimation,
                        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                            initialOffsetY = { -50 }
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Welcome to Drishti",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE0E6F1),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Your AI-Powered Guide",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = Color(0xFFA1B0C7),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Blind Navigation Button
                    GlowingCard(
                        title = "Blind Navigation Mode",
                        description = "Navigate your surroundings safely",
                        backgroundColor = Color(0xFF1E88E5),
                        glowColor = Color(0xFF64B5F6),
                        icon = Icons.Default.Visibility,
                        onClick = { navController.navigate("blindMode") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Settings Button
                    GlowingCard(
                        title = "Settings",
                        description = "Configure your preferences",
                        backgroundColor = Color(0xFF8E24AA),
                        glowColor = Color(0xFFD81B60),
                        icon = Icons.Default.Settings,
                        onClick = { navController.navigate("settings") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Help Button
                    GlowingCard(
                        title = "Help",
                        description = "Learn how to use the app",
                        backgroundColor = Color(0xFF43A047),
                        glowColor = Color(0xFF66BB6A),
                        icon = Icons.Default.Help,
                        onClick = { navController.navigate("help") }
                    )
                }
            }
        }
    )
}

@Composable
fun GlowingCard(
    title: String,
    description: String,
    backgroundColor: Color,
    glowColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1f)
    val shadowAlpha = if (isHovered) 0.8f else 0.4f

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(16.dp, shape = RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shadowElevation = if (isHovered) 12.dp.value else 8.dp.value
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = shadowAlpha), Color.Transparent),
                            radius = 200f
                        )
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Light,
                    color = Color(0xFFE3F2FD)
                )
            }
        }
    }
}
