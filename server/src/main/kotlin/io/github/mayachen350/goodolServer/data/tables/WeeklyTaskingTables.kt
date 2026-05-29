package io.github.mayachen350.goodolServer.data.tables

import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

object TasksTable : IntIdTable("goodol_weekly_tasking.tasks") {
    val name = varchar("title", 100)
    val categoryId = reference("category_id", CategoriesTable)
}

object CategoriesTable : IntIdTable("goodol_weekly_tasking.categories") {
    val name = varchar("title", 100)
}

object TaskTodosTable : IntIdTable("goodol_weekly_tasking.task_todos") {
    val task = reference("task_id", TasksTable)
    val weekId = reference("week_id", WeeksTable.id)
    val weekDay = enumeration("week_day", DayOfWeek::class)
    val responsibleId = reference("responsible_id", ResponsiblesTable)
}

object ResponsiblesTable : IntIdTable("goodol_weekly_tasking.responsibles") {
    val name = varchar("display_name", 100)
}

object WeeksTable : Table("goodol_weekly_tasking.weeks") {
    val id = integer("id") // important: this should NEVER be auto incremented as this would break the way the browser client interacts with the server
    val startDate = date("start_date").uniqueIndex()
    val endDate = date("end_date").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

val WeeklyTaskingTables
    get() = arrayOf(
        TasksTable,
        CategoriesTable,
        TaskTodosTable,
        ResponsiblesTable
    )