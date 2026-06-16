package io.github.mayachen350.goodolServer.data.services

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.github.mayachen350.goodolServer.application.database
import io.github.mayachen350.goodolServer.data.tables.CategoriesTable
import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.github.mayachen350.goodolServer.data.tables.TaskTodosTable
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.CategoryDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.CategoryId
import io.github.mayachen350.goodolServer.feat.weeklyTasking.EditedTaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewTaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewTodoDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskEditDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskId
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewWeekDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskTodoId
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekId
import io.github.mayachen350.goodolServer.utils.HasChildrenError
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update

object WeeklyTaskingService {
    object People {
        suspend fun existsById(id: Int): Boolean = suspendTransaction {
            ResponsiblesTable.select(ResponsiblesTable.id)
                .where { ResponsiblesTable.id eq id }
                .toList().any()
        }

        suspend fun getAllPeople() = suspendTransaction(database) {
            ResponsiblesTable.selectAll().toList()
        }

        suspend fun rename(id: Int, newName: String) = suspendTransaction(database) {
            ResponsiblesTable.update(where = {
                ResponsiblesTable.id eq id
            }) {
                it[name] = newName
            }
        }

        suspend fun editColor(id: Int, newColorRGB: UInt) = suspendTransaction(database) {
            ResponsiblesTable.update(where = {
                ResponsiblesTable.id eq id
            }) {
                it[chosenColorRGB] = newColorRGB
            }
        }
    }

    object Weeks {
        suspend fun existsById(value: UInt): Boolean = suspendTransaction {
            WeeksTable.selectAll()
                .where { WeeksTable.id eq value }
                .toList().any()
        }

        suspend fun createNewWeek(weekDTO: NewWeekDTO) = suspendTransaction(database) {
            WeeksTable.insert {
                it[id] = weekDTO.weekId
                it[startDate] = weekDTO.date
                it[endDate] = weekDTO.date.atEndOfWeek()
            }
        }

        /**
         * This function calculates which ID the Week should have before creating it and returning the ID.
         */
        suspend fun createNewWeekFromDate(date: LocalDate): UInt {
            val correctedDate = date.atStartOfWeek()

            val oldestRegisteredWeek: NewWeekDTO? = suspendTransaction(database) {
                WeeksTable
                    .selectAll()
                    .orderBy(WeeksTable.startDate)
                    .map { NewWeekDTO(it[WeeksTable.id], it[WeeksTable.startDate]) }
                    .firstOrNull()
            }

            val weekId = if (oldestRegisteredWeek != null) {
                oldestRegisteredWeek.weekId + (oldestRegisteredWeek.date.daysUntil(correctedDate).floorDiv(7)
                    .toUInt())
            } else 1u

            createNewWeek(NewWeekDTO(weekId, correctedDate))
            return weekId
        }

        suspend fun getById(id: UInt) = suspendTransaction(database) {
            WeeksTable.selectAll()
                .where { WeeksTable.id eq id }
                .singleOrNull()
        }

        suspend fun getByDate(date: LocalDate) = suspendTransaction(database) {
            val correctedDate = date.atStartOfWeek()

            WeeksTable.select(WeeksTable.id).where { WeeksTable.startDate eq correctedDate }.singleOrNull()
        }
    }

    object Category {
        suspend fun existsById(id: Int): Boolean = suspendTransaction(database) {
            CategoriesTable.selectAll()
                .where { CategoriesTable.id eq id }
                .toList().any()
        }

        suspend fun exists(category: CategoryDTO): Boolean = suspendTransaction {
            CategoriesTable.selectAll()
                .where { CategoriesTable.id eq category.id.value }
                .andWhere { CategoriesTable.name eq category.name }
                .toList().any()
        }

        suspend fun getByName(name: String) = suspendTransaction {
            CategoriesTable.selectAll()
                .where { CategoriesTable.name eq name }
                .singleOrNull()
        }

        suspend fun getById(id: Int) = suspendTransaction {
            CategoriesTable.selectAll()
                .where { CategoriesTable.id eq id }
                .singleOrNull()
        }

        suspend fun getAll() = suspendTransaction(database) {
            CategoriesTable.selectAll()
                .toList()
        }

        suspend fun createNew(name: String): ResultRow {
            suspendTransaction {
                CategoriesTable.insert {
                    it[CategoriesTable.name] = name
                }
            }

            return getByName(name)!!
        }

        suspend fun rename(id: Int, name: String): ResultRow {
            suspendTransaction {
                CategoriesTable.update(where = {
                    CategoriesTable.id eq id
                }) {
                    it[CategoriesTable.name] = name
                }
            }

            return getById(id)!!
        }

        suspend fun delete(id: Int): Either<HasChildrenError, Unit> {
            val hasChildren: Boolean = suspendTransaction {
                TasksTable.selectAll()
                    .where { TasksTable.categoryId eq id }
                    .andWhere { TasksTable.isDeleted eq false }
                    .toList().any()
            }
            if (hasChildren) return HasChildrenError.left()

            // Delete remaining task rows, marked as deleted, if any
            suspendTransaction {
                TasksTable.deleteWhere {
                    TasksTable.categoryId eq id
                }
            }

            suspendTransaction {
                CategoriesTable.deleteWhere {
                    CategoriesTable.id eq id
                }
            }

            return Unit.right()
        }
    }

    object Tasks {
        suspend fun getAllTasks() = suspendTransaction(database) {
            TasksTable.selectAll().where { TasksTable.isDeleted eq false }.toList()
        }

        suspend fun getTaskById(id: Int) = suspendTransaction {
            TasksTable.selectAll()
                .where { TasksTable.id eq id }
                .andWhere { TasksTable.isDeleted eq false }
                .singleOrNull()
        }

        suspend fun existsById(id: Int): Boolean = suspendTransaction(database) {
            TasksTable.selectAll()
                .where { TasksTable.id eq id }
                .andWhere { TasksTable.isDeleted eq false }
                .toList().any()
        }

        suspend fun existsByName(name: String): Boolean = suspendTransaction(database) {
            TasksTable.selectAll()
                .where { TasksTable.name eq name }
                .andWhere { TasksTable.isDeleted eq false }
                .toList().any()
        }

        /**Create a new task in the database IF it's not already present but deleted.
         *
         * If it is, toggle the deleted state. */
        suspend fun createNewTask(task: NewTaskDTO): ResultRow {
            val taskId: Int? = suspendTransaction {
                TasksTable.select(TasksTable.id)
                    .where { TasksTable.isDeleted eq true }
                    .andWhere { TasksTable.name eq task.name }
                    .andWhere { TasksTable.categoryId eq task.categoryId?.value }
                    .singleOrNull()?.get(TasksTable.id)?.value
            }
            val result: ResultRow

            if (taskId == null) {
                suspendTransaction(database) {
                    TasksTable.insert {
                        it[name] = task.name
                        it[categoryId] = task.categoryId?.value
                    }
                }
                result = suspendTransaction {
                    TasksTable.selectAll()
                        .where { TasksTable.name eq task.name }
                        .andWhere { TasksTable.categoryId eq task.categoryId?.value }
                        .single()
                }
            } else {
                suspendTransaction(database) {
                    TasksTable.update(where = {
                        TasksTable.id eq taskId
                    }) {
                        it[isDeleted] = false
                    }
                }

                result = getTaskById(taskId)!!
            }

            return result
        }

        suspend fun editTask(id: Int, editDTO: TaskEditDTO) = suspendTransaction {
            TasksTable.update(where = {
                TasksTable.id eq id
            }) {
                it[name] = editDTO.name
                it[categoryId] = editDTO.categoryId?.value
            }

            // update returning does not currently work in MariaDB

            getTaskById(id)!!.let {
                EditedTaskDTO(
                    TaskId(it[TasksTable.id].value),
                    it[TasksTable.name],
                    it[TasksTable.categoryId]?.value?.let {
                        CategoryId(it)
                    }
                )
            }
        }

        suspend fun renameTask(id: Int, newName: String) = suspendTransaction {
            TasksTable.update(where = {
                TasksTable.id eq id
            }) {
                it[name] = newName
            }
        }

        suspend fun editTaskCategory(id: Int, newCategoryId: Int) = suspendTransaction {
            TasksTable.update(where = {
                TasksTable.id eq id
            }) {
                it[categoryId] = newCategoryId
            }
        }

        /**This function does *not* actually delete any task in the database, but *mark it* as deleted.
         *
         * This is to prevent accidental damage to the DB's content, and to be able to safely undo.
         *
         * Garbage collection for marked as deleted tasks could eventually be done later to not fill the database.
         * (Especially for tasks with no Todos assigned to it)*/
        suspend fun deleteTaskByName(name: String) = suspendTransaction(database) {
            TasksTable.update(where = {
                TasksTable.name eq name
            }) {
                it[isDeleted] = true
            }
        }
    }

    object TaskTodos {
        suspend fun existsById(id: Int): Boolean = suspendTransaction {
            TaskTodosTable.selectAll()
                .where { TaskTodosTable.id eq id }
                .toList().any()
        }

        suspend fun getById(id: Int): ResultRow? = suspendTransaction {
            TaskTodosTable.selectAll()
                .where { TaskTodosTable.id eq id }
                .singleOrNull()
        }

        suspend fun getAllThisDay(date: LocalDate, ofSomeoneId: Int? = null): List<ResultRow> =
            suspendTransaction(database) {
                val query = TaskTodosTable
                    .innerJoin(WeeksTable, { WeeksTable.id }, { TaskTodosTable.id })
                    .selectAll()
                    .where { TaskTodosTable.weekDay eq date.dayOfWeek }
                    .andWhere { WeeksTable.startDate eq date.atStartOfWeek() }

                // lisp type shit
                (if (ofSomeoneId != null)
                    query.andWhere { TaskTodosTable.responsibleId eq ofSomeoneId }
                else
                    query).toList()
            }

        suspend fun getAllThisWeek(weekId: UInt, ofSomeoneId: Int? = null): List<ResultRow> =
            suspendTransaction(database) {
                val query = TaskTodosTable
                    .selectAll()
                    .where { TaskTodosTable.weekId eq weekId }

                // lisp type shit
                (if (ofSomeoneId != null)
                    query.andWhere { TaskTodosTable.responsibleId eq ofSomeoneId }
                else
                    query).toList()
            }

        suspend fun create(
            taskId: Int,
            weekId: UInt,
            responsibleId: Int?,
            weekday: DayOfWeek
        ): ResultRow {
            suspendTransaction {
                TaskTodosTable.insert {
                    it[TaskTodosTable.taskId] = taskId
                    it[TaskTodosTable.weekId] = weekId
                    it[TaskTodosTable.weekDay] = weekday
                    it[TaskTodosTable.responsibleId] = responsibleId
                }
            }

            return suspendTransaction {
                TaskTodosTable.selectAll()
                    .where { TaskTodosTable.taskId eq taskId }
                    .andWhere { TaskTodosTable.weekId eq weekId }
                    .andWhere { TaskTodosTable.weekDay eq weekday }
                    .singleOrNull()!!
            }
        }

        suspend fun assign(todoId: Int, someoneId: Int?) {
            suspendTransaction {
                TaskTodosTable.update(where = {
                    TaskTodosTable.id eq todoId
                }) {
                    it[TaskTodosTable.responsibleId] = someoneId
                }
            }
        }

        suspend fun toggleCompletion(todoId: Int): Boolean {
            suspendTransaction {
                // omg raw sql!!!
                TransactionManager.current().exec(
                    "UPDATE weekly_tasking__task_todos " +
                            "SET is_completed = NOT is_completed " +
                            "WHERE id = $todoId;"
                )
            }

            return suspendTransaction {
                TaskTodosTable.select(TaskTodosTable.isCompleted)
                    .where { TaskTodosTable.id eq todoId }
                    .single()
            }[TaskTodosTable.isCompleted]
        }
    }
}
