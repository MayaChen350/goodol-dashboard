package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.ktor.http.HttpStatusCode
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*

@Resource("/tasks")
private class Tasks {
    @Resource("new")
    class New(val parent: Tasks = Tasks())

    @Resource("{id}")
    class Id(val parent: Tasks = Tasks(), val id: Long) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {

    }
    post("/tasks") {
        with(call.receive<TaskDTO>()) {
            WeeklyTaskingService.Tasks.createNewTask(this)
            call.respond(HttpStatusCode.Created, "New task created!")
        }
    }

    // alternative method to do this if this is preferable later:

//    route("/tasks") {
//        get("/{id}") {
//            call.parameters["id"]
//        }
//    }
}
