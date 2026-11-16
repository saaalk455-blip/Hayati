package com.hayati.app.feature.worship

import androidx.compose.ui.graphics.vector.ImageVector
import com.hayati.app.core.designsystem.icons.HayatiIcons

val WorshipSection.icon: ImageVector
    get() = when (this) {
        WorshipSection.Prayer -> HayatiIcons.Worship
        WorshipSection.Quran -> HayatiIcons.Quran
        WorshipSection.Adhkar -> HayatiIcons.Adhkar
        WorshipSection.Tasbeeh -> HayatiIcons.Tasbeeh
    }
