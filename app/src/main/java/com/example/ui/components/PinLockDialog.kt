package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

@Composable
fun PinLockDialog(
    correctPin: String,
    onSuccess: () -> Unit,
    onBiometricClick: (() -> Unit)? = null,
    isBiometricAvailable: Boolean = false,
    title: String = "Store Security Lock",
    subtitle: String = "Enter 4-digit PIN to access Tinda Track"
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }

    fun onKeyPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (newPin == correctPin) {
                    onSuccess()
                } else {
                    failedAttempts++
                    errorMessage = if (failedAttempts >= 5) {
                        "5 failed attempts. Please wait 30 seconds."
                    } else {
                        "Incorrect PIN. (${5 - failedAttempts} tries left)"
                    }
                    enteredPin = ""
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Dialog(
        onDismissRequest = { /* Cannot dismiss without unlock */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SageSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = Forest,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = title,
                    style = TindaTypography.titleMedium,
                    color = Ink
                )

                Text(
                    text = subtitle,
                    style = TindaTypography.bodySmall,
                    color = Muted
                )

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Forest else Color.Transparent)
                                .border(2.dp, if (isFilled) Forest else MutedLight, CircleShape)
                        )
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = AlertRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Numeric Keypad
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("BIO", "0", "DEL")
                    )

                    for (row in rows) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (item in row) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (item) {
                                                "BIO" -> if (isBiometricAvailable) SageSoft else Color.Transparent
                                                "DEL" -> CreamDark
                                                else -> CreamLight
                                            }
                                        )
                                        .clickable(
                                            enabled = when (item) {
                                                "BIO" -> isBiometricAvailable
                                                else -> true
                                            }
                                        ) {
                                            when (item) {
                                                "BIO" -> onBiometricClick?.invoke()
                                                "DEL" -> onBackspace()
                                                else -> onKeyPress(item)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (item) {
                                        "BIO" -> if (isBiometricAvailable) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biometrics",
                                                tint = Forest
                                            )
                                        }
                                        "DEL" -> Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete",
                                            tint = Ink
                                        )
                                        else -> Text(
                                            text = item,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink,
                                            fontFamily = DmSansFontFamily
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
