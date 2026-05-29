package com.bitdesal.tabala.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.bitdesal.tabala.ui.figures.FiguresScreen
import com.bitdesal.tabala.ui.inici.IniciScreen
import com.bitdesal.tabala.ui.muixeranges.MuixerangesScreen
import com.bitdesal.tabala.ui.perfil.PerfilScreen

fun NavBackStack<NavKey>.navigateToTopLevel(destination: NavKey) {
    if (lastOrNull() == destination) return
    while (isNotEmpty()) {
        removeLastOrNull()
    }
    add(destination)
}

fun tabalaEntryProvider(
    modifier: Modifier = Modifier.fillMaxSize(),
): (NavKey) -> NavEntry<NavKey> = { key ->
    when (key) {
        IniciNavKey -> NavEntry(key) {
            IniciScreen(modifier = modifier)
        }

        MuixerangesNavKey -> NavEntry(key) {
            MuixerangesScreen(modifier = modifier)
        }

        FiguresNavKey -> NavEntry(key) {
            FiguresScreen(modifier = modifier)
        }

        PerfilNavKey -> NavEntry(key) {
            PerfilScreen(modifier = modifier)
        }

        else -> error("Unknown destination: $key")
    }
}
