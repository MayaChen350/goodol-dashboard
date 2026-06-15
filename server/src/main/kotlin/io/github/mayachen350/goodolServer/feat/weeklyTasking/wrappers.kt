package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.utils.NotFound
import kotlinx.serialization.Serializable


private interface Id <T> {
    val value: T
    suspend fun validate(): Either<NotFound, T>
}

@JvmInline
@Serializable
value class TaskId(override val value: Int) : Id<Int> {
    override suspend fun validate(): Either<NotFound, Int> =
        if (WeeklyTaskingService.Tasks.existsById(value))
            value.right()
        else NotFound.left()
}

@JvmInline
@Serializable
value class ResponsibleId(override val value: Int) : Id<Int> {
    override suspend fun validate(): Either<NotFound, Int> =
        if (WeeklyTaskingService.People.existsById(value))
            value.right()
        else NotFound.left()
}

@JvmInline
@Serializable
value class WeekId(override val value: UInt) : Id<UInt> {
    override suspend fun validate(): Either<NotFound, UInt> =
        if (WeeklyTaskingService.Weeks.existsById(value))
            value.right()
        else NotFound.left()
}

@JvmInline
@Serializable
value class CategoryId(override val value: Int) : Id<Int> {
    override suspend fun validate(): Either<NotFound, Int>  =
        if (WeeklyTaskingService.Category.existsById(value))
            value.right()
        else NotFound.left()
}

@JvmInline
@Serializable
value class TaskTodoId(override val value: Int) : Id<Int> {
    override suspend fun validate(): Either<NotFound, Int>  =
        if (WeeklyTaskingService.TaskTodos.existsById(value))
            value.right()
        else NotFound.left()
}
