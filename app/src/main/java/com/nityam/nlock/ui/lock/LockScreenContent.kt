package com.nityam.nlock.ui.lock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.nityam.nlock.ui.components.PinKeypad
import com.nityam.nlock.ui.components.TickMarkIndicator
import com.nityam.nlock.ui.theme.NLockTheme
import kotlin.math.roundToInt

/**
 * The main lock screen overlay UI.
 *
 * Always shows the full PIN pad. When biometrics are prompting,
 * the PIN pad stays visible behind the system biometric dialog
 * so there's never a blank screen.
 */
@Composable
internal fun LockScreenContent(
    state: LockScreenState,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    onBiometric: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state !is LockScreenState.Locked) return

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(state.showError) {
        if (state.showError) {
            // Shake animation (4 cycles)
            for (i in 0 until 4) {
                shakeOffset.animateTo(15f, tween(30))
                shakeOffset.animateTo(-15f, tween(60))
                shakeOffset.animateTo(0f, tween(30))
            }
        }
    }

    val errorTintAlpha by animateFloatAsState(
        targetValue = if (state.showError) 0.15f else 0f,
        animationSpec = tween(200),
        label = "error_tint_anim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NLockTheme.colors.base) // Always opaque
            .then(
                if (errorTintAlpha > 0f) {
                    Modifier.background(NLockTheme.colors.warning.copy(alpha = errorTintAlpha))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 48.dp)
                .offset {
                    IntOffset(x = shakeOffset.value.roundToInt(), y = 0)
                }
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // 1. App Icon
            val iconBitmap = remember(state.appIconDrawable) {
                state.appIconDrawable?.toBitmap(
                    width = 144,
                    height = 144,
                )?.asImageBitmap()
            }

            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.7f)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .alpha(0.5f)
                        .background(Color.Gray, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. "Enter PIN" label
            Text(
                text = if (state.isBiometricPrompting) "Unlock with Fingerprint" else "Enter PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = NLockTheme.colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Dot Indicator — always visible
            TickMarkIndicator(
                pinLength = state.pinLength,
                filledCount = state.enteredDigits,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 4. Keypad — always visible so there's never a blank screen
            PinKeypad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                onConfirm = onConfirm,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Biometric Icon — always visible when biometric is available
            if (state.biometricAvailable) {
                IconButton(onClick = onBiometric) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Use Biometrics",
                        tint = if (state.isBiometricPrompting) {
                            NLockTheme.colors.accent.copy(alpha = 0.5f)
                        } else {
                            NLockTheme.colors.accent
                        },
                        modifier = Modifier.size(36.dp),
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}
