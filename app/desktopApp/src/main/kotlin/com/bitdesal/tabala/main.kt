package com.bitdesal.tabala

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tabala",
    ) {
        App()
    }
}