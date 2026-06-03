package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.includePeopleRoutes() {
    route("/people") {
        get {
            call.respond<List<SomeoneDisplayDTO>>(WeeklyTaskingService.People.getAllPeople().map {
                with(ResponsiblesTable) {
                    SomeoneDisplayDTO(it[id].value, it[name], it[chosenColorRGB])
                }
            })
        }
    }
}