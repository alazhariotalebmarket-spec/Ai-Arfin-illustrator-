package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    var startAnim by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.35f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "logoAlpha"
    )
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    var showTitle by remember { mutableStateOf(false) }
    val titleOffsetY by animateDpAsState(
        targetValue = if (showTitle) 0.dp else 44.dp,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "titleSlide"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "titleAlpha"
    )

    val progress by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 2350, easing = LinearEasing),
        label = "splashProgress"
    )

    LaunchedEffect(Unit) {
        startAnim = true
        delay(550)
        showTitle = true
        delay(1950) // Total ~2.5 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBg)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial orange glow behind logo
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(logoScale)
                .alpha(glowAlpha * logoAlpha)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(OrangePrimary.copy(alpha = 0.55f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AiLogoBadge(
                size = 118.dp,
                fontSize = 56.sp,
                cornerRadius = 28.dp,
                showGlow = true,
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = titleOffsetY)
                    .alpha(titleAlpha)
            ) {
                Text(
                    text = "Arfin Illustrator",
                    color = PureWhite,
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Professional Mobile Vector Studio • বাংলা + EN",
                    color = MutedGray,
                    fontFamily = HindSiliguriFontFamily,
                    fontSize = 13.sp
                )
            }
        }

        // Bottom loading bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 48.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = OrangePrimary,
                trackColor = CharcoalSurfaceElevated
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Loading Studio Workspace...",
                color = SubtleGray,
                fontSize = 11.sp,
                fontFamily = PoppinsFontFamily
            )
        }
    }
}
