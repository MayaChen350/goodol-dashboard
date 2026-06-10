package io.github.mayachen350.goodolServer.data.services

import io.github.mayachen350.goodolServer.application.database
import io.github.mayachen350.goodolServer.data.tables.CategoriesTable
import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.CategoryDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.EditedTaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewTaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskEditDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekDTO
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update

object WeeklyTaskingService {
    object People {
        suspend fun getAllPeople() = suspendTransaction(database) {
            ResponsiblesTable.selectAll().toList()
        }
    }

    object Weeks {
        suspend fun createNewWeek(weekDTO: WeekDTO) = suspendTransaction(database) {
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

            val oldestRegisteredWeek: WeekDTO? = suspendTransaction(database) {
                WeeksTable
                    .selectAll()
                    .orderBy(WeeksTable.startDate)
                    .map { WeekDTO(it[WeeksTable.id], it[WeeksTable.startDate]) }
                    .firstOrNull()
            }

            val weekId = if (oldestRegisteredWeek != null) {
                oldestRegisteredWeek.weekId + (oldestRegisteredWeek.date.daysUntil(correctedDate).floorDiv(7)
                    .toUInt())
            } else 1u

            createNewWeek(WeekDTO(weekId, correctedDate))
            return weekId
        }

        suspend fun getByDate(date: LocalDate) = suspendTransaction(database) {
            val correctedDate = date.atStartOfWeek()

            WeeksTable.select(WeeksTable.id).where { WeeksTable.startDate eq correctedDate }.singleOrNull()
        }
    }

    object Category {
        suspend fun existsBId(id: Int): Boolean = suspendTransaction(database) {
            CategoriesTable.selectAll()
                .where { CategoriesTable.id eq id }
                .toList().any()
        }

        suspend fun exists(category: CategoryDTO): Boolean = suspendTransaction {
            CategoriesTable.selectAll()
                .where { CategoriesTable.id eq category.id }
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
                    .andWhere { TasksTable.categoryId eq task.categoryId }
                    .singleOrNull()?.get(TasksTable.id)?.value
            }
            val result: ResultRow

            if (taskId == null) {
                suspendTransaction(database) {
                    TasksTable.insert {
                        it[name] = task.name
                        it[categoryId] = task.categoryId
                    }
                }
                result = suspendTransaction {
                    TasksTable.selectAll()
                        .where { TasksTable.name eq task.name }
                        .andWhere { TasksTable.categoryId eq task.categoryId }
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
                it[categoryId] = editDTO.categoryId
            }

            // update returning does not currently work in MariaDB

            getTaskById(id)!!.let {
                EditedTaskDTO(
                    it[TasksTable.id].value,
                    it[TasksTable.name],
                    it[TasksTable.categoryId]?.value
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
}
