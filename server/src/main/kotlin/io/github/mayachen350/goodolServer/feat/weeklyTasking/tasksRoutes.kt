package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.routing.*

@Resource("/tasks")
private class Tasks {
    @Resource("{id}")
    class Id(val parent: Tasks = Tasks(), val id: Long)
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {

    }
}