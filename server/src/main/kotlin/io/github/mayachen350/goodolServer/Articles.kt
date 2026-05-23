package io.github.mayachen350.goodolServer

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/articles")
class Articles(val sort: String? = "new")
