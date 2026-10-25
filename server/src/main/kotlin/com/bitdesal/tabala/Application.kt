package com.bitdesal.tabala

import com.bitdesal.tabala.database.openDatabase
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private const val DB_PATH_ENV = "DB_PATH"
private const val DEFAULT_DB_PATH = "data/muixeranga.db"

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(databasePath: String = System.getenv(DB_PATH_ENV) ?: DEFAULT_DB_PATH) {
    val driver = openDatabase(databasePath)
    monitor.subscribe(ApplicationStopped) { driver.close() }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
    }
}
