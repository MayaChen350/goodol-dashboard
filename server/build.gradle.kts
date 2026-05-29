plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "io.github.mayachen350"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.cio.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

ktor {
    openApi {
        enabled = true
        codeInferenceEnabled = true
        onlyCommented = false
    }
}

// https://github.com/JetBrains/Exposed/blob/1.3.0/documentation-website/Writerside/snippets/exposed-migrations/build.gradle.kts
tasks.register<JavaExec>("generateMigrationScript") {
    // TODO: Make this script with parameters so I don't have to recompile... and pollute the git repo

    group = "application"
    description = "Generate a migration script"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "io.github.mayachen350.goodolServer.data.MigrationMainKt"
}

dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.cachingHeaders)
    implementation(ktorLibs.server.cio)
    implementation(ktorLibs.server.compression)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.cors)
    implementation(ktorLibs.server.defaultHeaders)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.routingOpenapi)
    implementation(ktorLibs.server.sessions)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.swagger)
    implementation(libs.exposed.core)
    implementation(libs.exposed.r2dbc)
    implementation(libs.exposed.datetime)
    implementation(libs.exposed.migration.core)
    implementation(libs.exposed.migration.r2dbc)
    implementation("org.mariadb:r2dbc-mariadb:1.3.0")
    implementation(libs.h2database.h2)
    implementation(libs.h2database.r2dbc)
    implementation(libs.logback.classic)
    implementation(libs.ucasoft.ktorSimpleCache)
    implementation(libs.ucasoft.ktorSimpleMemoryCache)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
}
