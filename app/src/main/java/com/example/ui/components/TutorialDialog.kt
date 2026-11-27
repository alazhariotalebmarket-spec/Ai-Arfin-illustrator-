package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

private data class TutorialStep(
    val icon: ImageVector,
    val title: String,
    val titleBn: String,
    val description: String
)

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            TutorialStep(
                icon = Icons.Default.Brush,
                title = "1. Vector Artboard & Tools",
                titleBn = "ভেক্টর আর্টবোর্ড ও টুলস",
                description = "Pick any canvas size (Logo, Poster, YouTube, Custom) and draw smoothly with Pen, Brush, Shapes, and 100+ Islamic & Graphic Stickers."
            ),
            TutorialStep(
                icon = Icons.Default.Layers,
                title = "2. Bangla + English Fonts & Layers",
                titleBn = "বাংলা + ইংরেজি ফন্ট ও লেয়ার",
                description = "Style text with 50+ Bengali & English fonts, gradients, shadows, and outlines. Manage visibility, locking, and layer order from the right panel."
            ),
            TutorialStep(
                icon = Icons.Default.HighQuality,
                title = "3. 4K / 8K Export & Offline Auto-Save",
                titleBn = "৪কে / ৮কে এক্সপোর্ট ও অটো-সেভ",
                description = "Export PNG, JPG, SVG, VECTOR, PDF, or WEBP up to 8K Ultra HD with transparent background, remove photo backgrounds, and save directly to Gallery."
            )
        )
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val step = steps[currentStep]

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "How to Use • ব্যবহারবিধি",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OrangePrimary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = OrangePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = step.titleBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OrangeLight,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Step Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (idx == currentStep) 24.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentStep) OrangePrimary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("tutorial_skip_btn")
                    ) {
                        Text("Skip")
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.lastIndex) currentStep++ else onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.testTag("tutorial_next_btn")
                    ) {
                        Text(if (currentStep < steps.lastIndex) "Next" else "Start Designing")
                    }
                }
            }
        }
    }
}
