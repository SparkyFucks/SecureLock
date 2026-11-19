package com.securelock.app.ui.screens.setup.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

@Composable
fun PatternLockView(
    onPatternComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedNodes = remember { mutableStateListOf<Int>() }
    var currentTouchPoint by remember { mutableStateOf<Offset?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val nodeInactiveColor = MaterialTheme.colorScheme.outlineVariant
    val nodeActiveGlow = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selectedNodes.clear()
                            val node = findNodeAt(offset, size.width.toFloat(), size.height.toFloat())
                            if (node != null && !selectedNodes.contains(node)) {
                                selectedNodes.add(node)
                            }
                            currentTouchPoint = offset
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentTouchPoint = change.position
                            val node = findNodeAt(change.position, size.width.toFloat(), size.height.toFloat())
                            if (node != null && !selectedNodes.contains(node)) {
                                selectedNodes.add(node)
                            }
                        },
                        onDragEnd = {
                            currentTouchPoint = null
                            if (selectedNodes.size >= 4) {
                                val patternString = selectedNodes.joinToString("-")
                                onPatternComplete(patternString)
                            } else {
                                selectedNodes.clear()
                            }
                        },
                        onDragCancel = {
                            currentTouchPoint = null
                            selectedNodes.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val nodePositions = calculateNodePositions(size.width, size.height)

                // 1. Draw connecting lines between already selected nodes
                for (i in 0 until selectedNodes.size - 1) {
                    val start = nodePositions[selectedNodes[i]]
                    val end = nodePositions[selectedNodes[i + 1]]
                    drawLine(
                        color = primaryColor,
                        start = start,
                        end = end,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }

                // 2. Draw line from last selected node to the current finger drag position
                val touch = currentTouchPoint
                if (selectedNodes.isNotEmpty() && touch != null) {
                    val last = nodePositions[selectedNodes.last()]
                    drawLine(
                        color = primaryColor.copy(alpha = 0.6f),
                        start = last,
                        end = touch,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }

                // 3. Draw 3x3 Nodes
                for (i in 0 until 9) {
                    val center = nodePositions[i]
                    val isSelected = selectedNodes.contains(i)

                    if (isSelected) {
                        // Outer glow
                        drawCircle(
                            color = nodeActiveGlow,
                            radius = 32f,
                            center = center
                        )
                        // Inner active circle
                        drawCircle(
                            color = primaryColor,
                            radius = 16f,
                            center = center
                        )
                    } else {
                        // Inactive dot
                        drawCircle(
                            color = nodeInactiveColor,
                            radius = 12f,
                            center = center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedNodes.isEmpty()) {
                "Draw pattern by connecting at least 4 dots"
            } else if (selectedNodes.size < 4) {
                "Connect at least 4 dots (${selectedNodes.size}/4)"
            } else {
                "Release finger to set pattern"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun calculateNodePositions(width: Float, height: Float): List<Offset> {
    val positions = mutableListOf<Offset>()
    val cellW = width / 3f
    val cellH = height / 3f

    for (row in 0 until 3) {
        for (col in 0 until 3) {
            val x = (col + 0.5f) * cellW
            val y = (row + 0.5f) * cellH
            positions.add(Offset(x, y))
        }
    }
    return positions
}

private fun findNodeAt(touch: Offset, width: Float, height: Float, hitRadius: Float = 60f): Int? {
    val positions = calculateNodePositions(width, height)
    for (i in positions.indices) {
        val dist = hypot(touch.x - positions[i].x, touch.y - positions[i].y)
        if (dist <= hitRadius) {
            return i
        }
    }
    return null
}
