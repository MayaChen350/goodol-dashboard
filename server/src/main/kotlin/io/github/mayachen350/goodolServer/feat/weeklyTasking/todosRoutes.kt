package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route

@Resource("todos")
private class Todos {
    @Resource("assign")
    class Assign(val parent: Todos = Todos())

    @Resource("complete")
    class Complete(val parent: Todos = Todos()) {
        @Resource("{id}")
        class Id(val parent: Complete = Complete(), val id: Int)
    }
}

fun Route.includeTodosRoutes() {
    post<Todos> {
        val taskTodoData = call.receive<NewTodoDTO>()
    }
    post<Todos.Assign> {
        val assignTodoData = call.receive<AssignTodoDTO>()
    }
    post<Todos.Complete.Id> {

    }
}