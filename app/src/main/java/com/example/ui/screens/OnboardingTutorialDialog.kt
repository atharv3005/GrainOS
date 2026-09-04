package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class TutorialStep(
    val title: String,
    val marathiTitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OnboardingTutorialDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val steps = remember {
        listOf(
            TutorialStep(
                title = "1. Inbound Gate Entry & Weighbridge",
                marathiTitle = "१. गेट नोंदणी व वजन काटा",
                description = "Register incoming trucks at the weighbridge. Capture Gross Weight, perform moisture meter test, compute automatic dockage cuts, and print instant thermal weighment slips.",
                icon = Icons.Default.LocalShipping,
                color = Color(0xFF10B981)
            ),
            TutorialStep(
                title = "2. Guarded Silo & Stock Management",
                marathiTitle = "२. सुरक्षित गोडाउन व साठा व्यवस्थापन",
                description = "Silos have active negative stock protection. Outbound dispatches will be strictly blocked if requested grain volume exceeds actual silo capacity.",
                icon = Icons.Default.Inventory,
                color = Color(0xFF3B82F6)
            ),
            TutorialStep(
                title = "3. Post-Dated Cheque (PDC) Lifecycle",
                marathiTitle = "३. धनादेश (PDC) व्यवस्थापन",
                description = "Track manual cheque lifecycles: ISSUED → DEPOSITED → PRESENTED → CLEARED. If a cheque bounces, the payable liability automatically reopens with audit tracking.",
                icon = Icons.Default.AccountBalance,
                color = Color(0xFFF59E0B)
            ),
            TutorialStep(
                title = "4. Day-End Statutory Closing & CA Export",
                marathiTitle = "४. दिवस अखेर बंद (Day-End) व हिशोब",
                description = "Perform EOD closing to freeze sequence numbers against tampering. Export full 6-sheet audit workbooks with APMC cess and TDS 194Q calculations directly for your CA.",
                icon = Icons.Default.Security,
                color = Color(0xFF8B5CF6)
            )
        )
    }

    var currentStepIdx by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIdx]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF131B26),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GrainOS Quick Start Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF9CA3AF))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(step.color.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = step.color,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF9FAFB),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = step.marathiTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = step.color,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    steps.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == currentStepIdx) 12.dp else 8.dp)
                                .background(
                                    if (idx == currentStepIdx) step.color else Color(0xFF374151),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIdx > 0) {
                        OutlinedButton(
                            onClick = { currentStepIdx-- },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE2E8F0))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStepIdx < steps.size - 1) {
                        Button(
                            onClick = { currentStepIdx++ },
                            colors = ButtonDefaults.buttonColors(containerColor = step.color)
                        ) {
                            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next", modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                onComplete()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Get Started", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Started (सुरु करा)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
