package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropType
import kotlinx.coroutines.delay

@Composable
fun PinLockScreen(
    activeCrop: CropType,
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    // Hardcoded PIN for demo purposes. In production, this would be encrypted in DataStore.
    val correctPin = "1234"

    LaunchedEffect(enteredPin) {
        if (enteredPin.length == 4) {
            if (enteredPin == correctPin) {
                onUnlockSuccess()
            } else {
                isError = true
                delay(500)
                enteredPin = ""
                isError = false
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Security Lock",
                tint = if (isError) Color(0xFFEF4444) else activeCrop.primaryColor,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                text = "Enter Finance PIN",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (isError) {
                Text("Incorrect PIN. Default is 1234.", color = Color(0xFFEF4444), fontSize = 12.sp)
            } else {
                Text("Default PIN is 1234", color = Color.Gray, fontSize = 12.sp)
            }

            // PIN Dots
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (i < enteredPin.length) activeCrop.primaryColor
                                else Color(0xFF334155)
                            )
                    )
                }
            }

            // Number Pad
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val padData = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )
                
                padData.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { key ->
                            Button(
                                onClick = {
                                    when (key) {
                                        "DEL" -> {
                                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        }
                                        "BIO" -> {
                                            // Mock Biometric unlock
                                            onUnlockSuccess()
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) enteredPin += key
                                        }
                                    }
                                },
                                modifier = Modifier.size(70.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                            ) {
                                if (key == "DEL") {
                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                                } else if (key == "BIO") {
                                    Icon(Icons.Default.Fingerprint, contentDescription = "Biometric", tint = activeCrop.primaryColor)
                                } else {
                                    Text(text = key, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
