package io.github.mayachen350.goodolServer.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Route

//inline fun Route.catchConflicts(dbCreationAction: suspend () -> T) {
//    try {
//        return Result.failure<>(.success(dbCreationAction())
//        // WeeklyTaskingService.Weeks.createNewWeek(weekData)
//        // call.respond(HttpStatusCode.Created, "Successfully registered new week in the database.")
//    } catch (e: Exception) {
//        if (e.message != null && (e.message!!.contains("Duplicate entry") || e.message!!.startsWith("Unique index"))) {
//            call.respond(HttpStatusCode.Conflict, e.message.toString())
//            return CanFail.err()
//        } else throw eq
//    }
//}
