package io.github.mayachen350.goodolServer.feat.weeklyTasking

import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

suspend fun setupWeeklyTasking() {
    garbageCollectEmptyWeeks()
}

private suspend fun garbageCollectEmptyWeeks() = suspendTransaction {
    TransactionManager.current().exec(
        "DELETE FROM weekly_tasking__weeks WHERE id IN ( " +
                "SELECT * FROM ( " +
                "SELECT w.id FROM weekly_tasking__weeks w " +
                "LEFT JOIN weekly_tasking__task_todos t " +
                "ON w.id = t.week_id " +
                "GROUP BY w.id " +
                "HAVING COUNT(t.id) < 1 " +
                ") AS p " +
                ")"
    )
}