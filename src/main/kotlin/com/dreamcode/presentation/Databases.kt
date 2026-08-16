package com.dreamcode.presentation

import com.dreamcode.data.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureDatabases() {
    DatabaseFactory.init(environment.config)
}
