plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.sqldelight)
}

group = "com.bitdesal.tabala"
version = "1.0.0"
application {
    mainClass = "com.bitdesal.tabala.ApplicationKt"
}

val flywayMigrationsDir = layout.buildDirectory.dir("generated/flyway")

sqldelight {
    databases {
        create("TabalaDatabase") {
            packageName = "com.bitdesal.tabala.db"
            dialect(libs.sqldelight.sqlite338Dialect)
            module(libs.sqldelight.sqliteJsonModule)
            // Schema is owned by the .sqm migrations; Flyway applies them at startup.
            deriveSchemaFromMigrations = true
            migrationOutputDirectory = flywayMigrationsDir.map { it.dir("db/migration") }
            migrationOutputFileFormat = ".sql"
        }
    }
}

sourceSets.main {
    resources.srcDir(flywayMigrationsDir)
}

tasks.processResources {
    dependsOn("generateMainTabalaDatabaseMigrations")
}

dependencies {
    api(project(":core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.flyway.core)
    implementation(libs.sqlite.jdbc)
    implementation(libs.sqldelight.sqliteDriver)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
