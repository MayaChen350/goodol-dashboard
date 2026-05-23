package io.github.mayachen350.goodolServer.data.tables

import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

val WeeklyTaskingDbSchema = Schema("weekly_tasking")

object TasksTable : IntIdTable() {
    val name = varchar("name", 100)
    val category = reference("category", CategoriesTable)
}

object CategoriesTable : IntIdTable() {
    val name = varchar("name", 100)
}

object TaskTodosTable : IntIdTable() {
    val name = varchar("name", 100)
    val date = date("date")
    val responsible = reference("responsible", Someone)
}

object Someone : IntIdTable() {
    val name = varchar("name", 100)
}