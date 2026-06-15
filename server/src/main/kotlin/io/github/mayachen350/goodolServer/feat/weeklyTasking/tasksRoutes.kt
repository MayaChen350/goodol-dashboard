package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.getOrElse
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.TasksTable
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
    class Id(val parent: Tasks = Tasks(), val id: TaskId)

    @Resource("edit")
    class Edit(val parent: Tasks = Tasks()) {
        @Resource("{id}")
        class Id(val parent: Edit = Edit(), val id: TaskId)
    }

    @Resource("{name}")
    class Name(val parent: Tasks = Tasks(), val name: String)
}

fun Route.includeTasksRoutes() {
    get<Tasks.Id> {
        val task: TaskDTO? = WeeklyTaskingService.Tasks.getTaskById(it.id.value)?.let {
            TaskDTO(
                TaskId(it[TasksTable.id].value), it[TasksTable.name],
                it[TasksTable.categoryId]?.value?.let {
                    CategoryId(it)
                })
        }

        if (task == null) {
            call.respond(HttpStatusCode.NotFound, "Task not found")
            return@get
        }

        call.respond<TaskDTO>(task)
    }
    get<Tasks> {
        call.respond(WeeklyTaskingService.Tasks.getAllTasks().map {
            TaskDTO(
                TaskId(it[TasksTable.id].value), it[TasksTable.name],
                it[TasksTable.categoryId]?.value?.let {
                    CategoryId(it)
                })
        })
    }
    post<Tasks> {
        with(call.receive<NewTaskDTO>()) {
            val newTask: TaskDTO = WeeklyTaskingService.Tasks.createNewTask(this).let {
                TaskDTO(
                    TaskId(it[TasksTable.id].value), it[TasksTable.name],
                    it[TasksTable.categoryId]?.value?.let {
                        CategoryId(it)
                    })
            }
            call.respond(HttpStatusCode.Created, newTask)
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
        val id = it.id.validate().getOrElse {
            return@put call.respond(HttpStatusCode.NotFound, "No task with that id.")
        }

        val data: TaskEditDTO = call.receive<TaskEditDTO>()

        val categoryId = data.categoryId?.validate()?.getOrElse {
            return@put call.respond(HttpStatusCode.NotFound, "Invalid category. No category with that id.")
        }

        val result: EditedTaskDTO = WeeklyTaskingService.Tasks.editTask(id, data)
        call.respond(HttpStatusCode.OK, result)
    }
}
