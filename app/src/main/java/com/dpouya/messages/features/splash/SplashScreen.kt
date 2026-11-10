package com.dpouya.messages.features.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.dpouya.messages.core.theme.MessagesThemeTokens
import com.dpouya.messages.core.theme.MessagesTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val colors = MessagesThemeTokens.colors
    LaunchedEffect(Unit) {
        delay(900)
        onFinished()
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val progress by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "bar",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Canvas(Modifier.size(76.dp)) {
                val stroke = 4.dp.toPx()
                drawRoundRect(
                    color = colors.accent,
                    topLeft = Offset(size.width * 0.12f, size.height * 0.1f),
                    size = Size(size.width * 0.76f, size.height * 0.62f),
                    cornerRadius = CornerRadius(8.dp.toPx()),
                    style = Stroke(width = stroke),
                )
                val cy = size.height * 0.38f
                val r = 3.5.dp.toPx()
                listOf(0.32f, 0.5f, 0.68f).forEach { x ->
                    drawCircle(colors.accent, radius = r, center = Offset(size.width * x, cy))
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Messages", style = MessagesTypography.splashTitle, color = colors.primaryText)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .width(42.dp)
                .height(3.dp)
                .background(colors.divider),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .width(42.dp * progress)
                    .height(3.dp)
                    .background(colors.accent),
            )
        }
    }
}
