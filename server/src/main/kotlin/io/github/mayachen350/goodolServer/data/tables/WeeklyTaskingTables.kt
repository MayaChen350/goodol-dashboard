package io.github.mayachen350.goodolServer.data.tables

import kotlinx.datetime.DayOfWeek
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.between
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.date

object TasksTable : IntIdTable("weekly_tasking__tasks") {
    val name = varchar("title", 100).uniqueIndex()
    val categoryId = reference("category_id", CategoriesTable).nullable().default(null)
    val isDeleted = bool("is_deleted").default(false)
}

object CategoriesTable : IntIdTable("weekly_tasking__categories") {
    val name = varchar("title", 100).uniqueIndex()
}

object TaskTodosTable : IntIdTable("weekly_tasking__task_todos") {
    val taskId = reference("task_id", TasksTable)
    val weekId = reference("week_id", WeeksTable.id)
    val weekDay = enumeration("week_day", DayOfWeek::class).check {
        it.between(DayOfWeek.MONDAY, DayOfWeek.SUNDAY) // oh no, the secret 8th day of the week!!
    }
    val responsibleId = reference("responsible_id", ResponsiblesTable)
        .nullable() // maybe updating to null instead of deleting the task entirely could be good who knows
    // or maybe it could means like "waiting to be assigned"

    init {
        // can't have same task same day assigned twice
        uniqueIndex(null, taskId, weekId, weekDay)
    }
}

object ResponsiblesTable : IntIdTable("weekly_tasking__responsibles") {
    val name = varchar("display_name", 100).uniqueIndex()
    val chosenColorRGB = uinteger("chosen_color").check {
        it.between(0x0u, 0xFFFFFFu) // can be removed if ARGB would somehow be allowed idk
    }
}

object WeeksTable : Table("weekly_tasking__weeks") {
    val id =
        // important: this should NEVER be auto incremented as this would break the way the browser client interacts with the server
        uinteger("id")
    val startDate = date("start_date").uniqueIndex()
    val endDate =
        // I haven't found a use for actually saving it in the db, but I guess I'll keep it for now as adding it back later would be more annoying
        date("end_date").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

val WeeklyTaskingTables
    get() = arrayOf(
        TasksTable,
        CategoriesTable,
        TaskTodosTable,
        ResponsiblesTable,
        WeeksTable
    )