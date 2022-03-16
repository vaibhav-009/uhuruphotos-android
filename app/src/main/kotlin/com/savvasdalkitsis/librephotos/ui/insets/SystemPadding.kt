package com.savvasdalkitsis.librephotos.ui.insets

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable

@Composable
fun systemPadding(sides: WindowInsetsSides) = WindowInsets
    .systemBars
    .only(sides)
    .asPaddingValues()