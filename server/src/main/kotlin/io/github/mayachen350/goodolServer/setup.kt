package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.feat.weeklyTasking.setupWeeklyTasking

suspend fun setup() {
    setupWeeklyTasking()
}