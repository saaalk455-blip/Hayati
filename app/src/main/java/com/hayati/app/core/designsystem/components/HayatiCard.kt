package com.hayati.app.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hayati.app.core.designsystem.theme.HayatiSpacing
import com.hayati.app.core.designsystem.theme.LocalHayatiColors

/** Standard elegant card: dark card color, subtle border and a soft shadow. */
@Composable
fun HayatiCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalHayatiColors.current
    val shape = MaterialTheme.shapes.large
    val border = BorderStroke(1.dp, colors.border)
    val inner: @Composable () -> Unit = {
        Column(modifier = Modifier.padding(HayatiSpacing.CardPadding), content = content)
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = colors.card,
            border = border,
            shadowElevation = 4.dp,
            content = inner,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = colors.card,
            border = border,
            shadowElevation = 4.dp,
            content = inner,
        )
    }
}
