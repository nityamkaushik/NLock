package com.nityam.nlock.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nityam.nlock.ui.theme.NLockTheme

/**
 * Displays a row of circular dots indicating PIN entry progress.
 *
 * Unfilled dots show as outlined rings; filled dots are solid circles
 * with a subtle scale-up pop animation.
 */
@Composable
internal fun TickMarkIndicator(
    pinLength: Int,
    filledCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until pinLength) {
            val isFilled = i < filledCount
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0.85f,
                animationSpec = if (isFilled) {
                    spring(dampingRatio = 0.5f, stiffness = 400f)
                } else {
                    tween(durationMillis = 120)
                },
                label = "dot_scale_$i",
            )

            val fillAlpha by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0f,
                animationSpec = tween(durationMillis = 100),
                label = "dot_fill_$i",
            )

            val accentColor = NLockTheme.colors.accent
            val inactiveColor = NLockTheme.colors.textSecondary.copy(alpha = 0.35f)

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .scale(scale)
                    .then(
                        if (fillAlpha > 0.01f) {
                            Modifier.background(
                                accentColor.copy(alpha = fillAlpha),
                                CircleShape
                            )
                        } else {
                            Modifier.border(
                                width = 1.5.dp,
                                color = inactiveColor,
                                shape = CircleShape
                            )
                        }
                    )
            )
        }
    }
}
