package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.openapi.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
@JsonSchema.Title("Task")
data class TaskDTO(val name: String, val categoryId: Int)

@Serializable
@JsonSchema.Title("Someone")
data class SomeoneDisplayDTO(val id: Int, val name: String, val chosenColor: UInt)

@Serializable
@JsonSchema.Title("Week")
data class WeekDTO(val weekId: UInt, val date: LocalDate)