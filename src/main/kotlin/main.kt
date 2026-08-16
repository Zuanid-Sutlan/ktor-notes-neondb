package com.dreamcode

import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.engine.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    val env = dotenv {
        ignoreIfMissing = true
    }
    env.entries().forEach { entry ->
        System.setProperty(entry.key, entry.value)
    }
    
    io.ktor.server.netty.EngineMain.main(args)
}
