package io.github.mayachen350.goodolServer.utils

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

// this is peak trust
suspend inline fun <T> RoutingContext.catchConflicts(crossinline dbCreationAction: suspend () -> T): Either<DatabaseConflictError, T> =
    try {
        dbCreationAction().right()
        // WeeklyTaskingService.Weeks.createNewWeek(weekData)
        // call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
    } catch (e: Exception) {
        if (e.message != null && (e.message!!.startsWith("Duplicate entry") || e.message!!.startsWith("Unique index"))) {
            call.respond(HttpStatusCode.Conflict, e.message.toString())
            DatabaseConflictError.left()
        } else throw e
    }
