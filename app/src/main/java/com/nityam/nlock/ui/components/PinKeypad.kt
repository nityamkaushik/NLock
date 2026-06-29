package com.nityam.nlock.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.nityam.nlock.ui.theme.NLockTheme

/**
 * 3×4 numeric keypad for PIN entry.
 *
 * Each button is a 64dp circle with a subtle surface-tinted background
 * and a circular ripple. Haptic feedback fires on every tap.
 */
@Composable
internal fun PinKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("backspace", "0", "confirm")
        )

        for (row in rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (key in row) {
                    KeypadButton(
                        key = key,
                        onDigit = onDigit,
                        onBackspace = onBackspace,
                        onConfirm = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
) {
    val view = LocalView.current
    val isActionKey = key == "backspace" || key == "confirm"

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .then(
                if (!isActionKey) {
                    Modifier.background(
                        NLockTheme.colors.surface.copy(alpha = 0.14f),
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 32.dp),
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                when (key) {
                    "backspace" -> onBackspace()
                    "confirm" -> onConfirm()
                    else -> onDigit(key.first())
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        when (key) {
            "backspace" -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = NLockTheme.colors.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
            "confirm" -> {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Confirm",
                    tint = NLockTheme.colors.accent,
                    modifier = Modifier.size(24.dp),
                )
            }
            else -> {
                Text(
                    text = key,
                    color = NLockTheme.colors.textPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}
