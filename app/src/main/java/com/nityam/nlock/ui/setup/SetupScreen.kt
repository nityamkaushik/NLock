package com.nityam.nlock.ui.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nityam.nlock.NLockApplication
import com.nityam.nlock.ui.components.PinKeypad
import com.nityam.nlock.ui.components.TickMarkIndicator
import com.nityam.nlock.ui.theme.NLockTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
internal fun SetupScreen(
    onSetupComplete: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = viewModel<SetupViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = context.applicationContext as NLockApplication
                return SetupViewModel(app.repository, app.pinHashManager) as T
            }
        }
    )

    val step by viewModel.step.collectAsState()
    var currentPin by remember { mutableStateOf("") }

    // Shake animation for error
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(step) {
        when (step) {
            SetupStep.DONE -> onSetupComplete()
            SetupStep.ERROR_MISMATCH -> {
                // Shake, wait, then auto-reset
                for (i in 0 until 4) {
                    shakeOffset.animateTo(15f, tween(30))
                    shakeOffset.animateTo(-15f, tween(60))
                    shakeOffset.animateTo(0f, tween(30))
                }
                delay(600)
                currentPin = ""
                viewModel.reset()
            }
            SetupStep.CONFIRM_PIN -> {
                currentPin = ""
            }
            else -> { /* no-op */ }
        }
    }

    val title = when (step) {
        SetupStep.ENTER_PIN -> "Create PIN"
        SetupStep.CONFIRM_PIN -> "Confirm PIN"
        SetupStep.ERROR_MISMATCH -> "PINs don't match"
        SetupStep.DONE -> "Success"
    }

    val titleColor by animateColorAsState(
        targetValue = when (step) {
            SetupStep.ERROR_MISMATCH -> NLockTheme.colors.warning
            else -> NLockTheme.colors.textPrimary
        },
        animationSpec = tween(200),
        label = "title_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NLockTheme.colors.base),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset {
                IntOffset(x = shakeOffset.value.roundToInt(), y = 0)
            }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = titleColor,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (step) {
                    SetupStep.ENTER_PIN -> "Choose a 4-digit PIN to protect your apps"
                    SetupStep.CONFIRM_PIN -> "Enter the same PIN again"
                    SetupStep.ERROR_MISMATCH -> "Try again from the beginning"
                    SetupStep.DONE -> ""
                },
                style = MaterialTheme.typography.bodyMedium,
                color = NLockTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            TickMarkIndicator(pinLength = 4, filledCount = currentPin.length)

            Spacer(modifier = Modifier.height(48.dp))

            PinKeypad(
                onDigit = {
                    if (currentPin.length < 4) {
                        currentPin += it
                        // Auto-submit when 4 digits entered
                        if (currentPin.length == 4) {
                            viewModel.submitPin(currentPin)
                        }
                    }
                },
                onBackspace = {
                    if (currentPin.isNotEmpty()) currentPin = currentPin.dropLast(1)
                },
                onConfirm = {
                    if (currentPin.length == 4) {
                        viewModel.submitPin(currentPin)
                    }
                }
            )
        }
    }
}
