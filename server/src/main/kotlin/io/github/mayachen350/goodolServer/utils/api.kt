package io.github.mayachen350.goodolServer.utils

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// this is peak trust
suspend inline fun <T> RoutingContext.catchConflicts(crossinline dbCreationAction: suspend () -> T): Either<DatabaseConflict, T> =
    try {
        dbCreationAction().right()
        // WeeklyTaskingService.Weeks.createNewWeek(weekData)
        // call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
    } catch (e: Exception) {
        if (e.message != null && (e.message!!.startsWith("Duplicate entry") || e.message!!.startsWith("Unique index"))) {
            call.respond(HttpStatusCode.Conflict, e.message.toString())
            DatabaseConflict.left()
        } else throw e
    }
