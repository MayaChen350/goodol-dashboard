package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

@Serializable
@JsonSchema.Title("Task")
data class TaskDTO(val name: String, val categoryId: Int)

@Serializable
@JsonSchema.Title("Someone")
data class SomeoneDTO(val name: String)