package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewTaskDTO
import io.ktor.openapi.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@JsonSchema.Title("NewTask")
data class NewTaskDTO(val name: String, val categoryId: CategoryId?) {
    constructor(name: String, categoryId: Int): this(name, CategoryId(categoryId))
}

@Serializable
@JsonSchema.Title("Task")
data class TaskDTO(val id: TaskId, val name: String, val categoryId: CategoryId?)

@Serializable
@JsonSchema.Title("TaskEdit")
data class TaskEditDTO(val name: String, val categoryId: CategoryId?)  {
    constructor(name: String, categoryId: Int): this(name, CategoryId(categoryId))
}

@Serializable
@JsonSchema.Title("EditedTask")
data class EditedTaskDTO(val id: TaskId, val name: String, val categoryId: CategoryId?)

@Serializable
@JsonSchema.Title("Someone")
data class SomeoneDisplayDTO(val id: ResponsibleId, val name: String, val chosenColor: UInt)

@Serializable
@JsonSchema.Title("NewWeek")
data class NewWeekDTO(val weekId: UInt, val date: LocalDate)

@Serializable
@JsonSchema.Title("Category")
data class CategoryDTO(val id: CategoryId, val name: String)

@Serializable
@JsonSchema.Title("NewTodo")
data class NewTodoDTO(
    val taskId: TaskId,
    val weekId: WeekId,
    val responsibleId: ResponsibleId?
)

@Serializable
@JsonSchema.Title("Todo")
data class TodoDTO(
    val todoId: TaskTodoId,
    val taskId: Int,
    val weekId: UInt,
    val responsibleId: Int?,
    val isCompleted: Boolean
)

@Serializable
@JsonSchema.Title("AssignTodo")
data class AssignTodoDTO(
    val todoId: Int,
    val responsibleId: Int?,
)