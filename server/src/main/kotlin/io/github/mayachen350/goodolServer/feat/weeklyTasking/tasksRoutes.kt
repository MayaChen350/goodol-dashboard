package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.utils.catchConflicts
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

@Resource("/tasks")
private class Tasks {
    @Resource("new")
    class New(val parent: Tasks = Tasks())

    @Resource("{id}")
    class Id(val parent: Tasks = Tasks(), val id: Int)

    @Resource("edit")
    class Edit(val parent: Tasks = Tasks()) {
        @Resource("{id}")
        class Id(val parent: Edit = Edit(), val id: Int)
    }

    @Resource("{name}")
    class Name(val parent: Tasks = Tasks(), val name: String)
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {
        val task: TaskDTO? = WeeklyTaskingService.Tasks.getTaskById(it.id)?.let {
            TaskDTO(it[TasksTable.id].value, it[TasksTable.name], it[TasksTable.categoryId]?.value)
        }

        if (task == null) {
            call.respond(HttpStatusCode.NotFound, "Task not found")
            return@get
        }

        call.respond<TaskDTO>(task)
    }
    get<Tasks> {
        call.respond(WeeklyTaskingService.Tasks.getAllTasks().map {
            TaskDTO(it[TasksTable.id].value, it[TasksTable.name], it[TasksTable.categoryId]?.value)
        })
    }
    post<Tasks> {
        with(call.receive<NewTaskDTO>()) {
            catchConflicts {
                val newTask: TaskDTO = WeeklyTaskingService.Tasks.createNewTask(this).let {
                    TaskDTO(it[TasksTable.id].value, it[TasksTable.name], it[TasksTable.categoryId]?.value)
                }
                call.respond(HttpStatusCode.Created, newTask)
            }
        }
    }

    // Delete from task's name
    delete<Tasks.Name> {
        val name = it.name

        if (WeeklyTaskingService.Tasks.existsByName(name)) {
            WeeklyTaskingService.Tasks.deleteTaskByName(name)
            call.respond(HttpStatusCode.OK, "Task successfully removed.")
        } else call.respond(HttpStatusCode.NotFound, "No task with that name.")
    }

    put<Tasks.Edit.Id> {
        if (!WeeklyTaskingService.Tasks.existsById(it.id)) {
            call.respond(HttpStatusCode.NotFound, "No task with that id.")
            return@put;
        }

        val data: TaskEditDTO = call.receive<TaskEditDTO>()

        if (data.categoryId != null && !WeeklyTaskingService.Category.existsBId(data.categoryId)) {
            call.respond(HttpStatusCode.NotFound, "Invalid category. No category with that id.")
            return@put;
        }

        catchConflicts {
            val result: EditedTaskDTO = WeeklyTaskingService.Tasks.editTask(it.id, data)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
