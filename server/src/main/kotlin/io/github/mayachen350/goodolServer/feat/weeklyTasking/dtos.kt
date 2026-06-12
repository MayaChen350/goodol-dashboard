package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.openapi.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@JsonSchema.Title("NewTask")
data class NewTaskDTO(val name: String, val categoryId: Int?)

@Serializable
@JsonSchema.Title("Task")
data class TaskDTO(val id: Int, val name: String, val categoryId: Int?)

@Serializable
@JsonSchema.Title("TaskEdit")
data class TaskEditDTO(val name: String, val categoryId: Int?)

@Serializable
@JsonSchema.Title("EditedTask")
data class EditedTaskDTO(val id: Int, val name: String, val categoryId: Int?)

@Serializable
@JsonSchema.Title("Someone")
data class SomeoneDisplayDTO(val id: Int, val name: String, val chosenColor: UInt)

@Serializable
@JsonSchema.Title("Week")
data class WeekDTO(val weekId: UInt, val date: LocalDate)

@Serializable
@JsonSchema.Title("Category")
data class CategoryDTO(val id: Int, val name: String)

@Serializable
@JsonSchema.Title("NewTodo")
data class NewTodoDTO(
    val taskId: Int,
    val weekId: UInt,
    val responsibleId: Int?
)

@Serializable
@JsonSchema.Title("Todo")
data class TodoDTO(
    val todoId: Int,
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