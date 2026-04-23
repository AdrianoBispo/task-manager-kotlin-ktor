package com.adrianobispo

import io.ktor.server.application.Application
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureStatusPages()
    configureKoin()
    configureRouting()
}
