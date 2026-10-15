package com.example.finance_tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.finance_tracker.ui.theme.InkSecondary

/**
 * Кружок категории по макету: цветная точка на бледном круге того же цвета.
 */
@Composable
fun CategoryDot(
    color: Long?,
    modifier: Modifier = Modifier,
    circleSize: Dp = 36.dp,
    dotSize: Dp = 12.dp,
) {
    val dotColor = color?.let { Color(it) } ?: InkSecondary
    Box(
        modifier = modifier
            .size(circleSize)
            .background(dotColor.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(dotSize)
                .background(dotColor, CircleShape)
        )
    }
}
