package com.bitdesal.tabala

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.*

class ApplicationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testRoot() = testApplication {
        application {
            module(databasePath = tempFolder.root.resolve("app.db").path)
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, Ktor!", response.bodyAsText())
    }
}
