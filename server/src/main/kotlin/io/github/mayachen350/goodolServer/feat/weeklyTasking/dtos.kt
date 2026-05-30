package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.openapi.*
import kotlinx.serialization.Serializable

@Serializable
@JsonSchema.Title("Task")
data class TaskDTO(val name: String, val categoryId: Int)

@Serializable
@JsonSchema.Title("Someone")
data class SomeoneDisplayDTO(val id: Int, val name: String, val chosenColor: UInt)