package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.ResponsiblesTable
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.get
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

@Resource("/people")
private class People {
    @Resource("{id}")
    class Id(val parent: People = People(), val id: Int) {
        @Resource("color")
        class Color(val parent: Id) {

            @Resource("hex")
            class Hex(val parent: Color) {
                @Resource("{hexColorRGB}")
                class HexColorRGB(val parent: Hex, val hexColorRGB: String)
            }

            @Resource("num")
            class Num(val parent: Color) {
                @Resource("{colorRGB}")
                class ColorRGB(val parent: Num, val colorRGB: UInt)
            }
        }

        @Resource("rename")
        class Rename(val parent: Id) {
            @Resource("{name}")
            class Name(val parent: Rename, val name: String)
        }
    }
}


fun Route.includePeopleRoutes() {
    get<People> {
        call.respond<List<SomeoneDisplayDTO>>(WeeklyTaskingService.People.getAllPeople().map {
            with(ResponsiblesTable) {
                SomeoneDisplayDTO(it[id].value, it[name], it[chosenColorRGB])
            }
        })
    }

    put<People.Id.Rename> {
        if (!WeeklyTaskingService.People.existsBId(it.parent.id)) {
            return@put call.respond(HttpStatusCode.NotFound, "Invalid responsible id.")
        }

        val newName = call.receive<String>()

        WeeklyTaskingService.People.rename(it.parent.id, newName)
        call.respond(HttpStatusCode.OK, "New name: $newName")
    }

    suspend fun RoutingContext.editColor(id: Int, colorRGB: UInt) {
        if (!WeeklyTaskingService.People.existsBId(id)) {
            return call.respond(HttpStatusCode.NotFound, "Invalid responsible id.")
        }

        if (colorRGB !in 0x0u..0xFFFFFFu)
            return call.respond(
                HttpStatusCode.BadRequest,
                "#${colorRGB.toHexString().uppercase().drop(2)} is not a valid RGB color!"
            )

        WeeklyTaskingService.People.editColor(id, colorRGB)
        call.respond(
            HttpStatusCode.OK,
            "New chosen color is now: #${colorRGB.toHexString().uppercase().drop(2)}"
        )
    }

    // change the color from a number
    put<People.Id.Color.Num.ColorRGB> {
        editColor(it.parent.parent.parent.id, it.colorRGB)
    }

    // change the color from a hex string
    put<People.Id.Color.Hex.HexColorRGB> {
        val colorRGB: UInt
        try {
            // it's amazing that the kotlin standard library comes with this for some reason
            colorRGB = it.hexColorRGB.lowercase().hexToUInt()
        } catch (e: IllegalArgumentException) {
            return@put call.respond(HttpStatusCode.BadRequest, "Impossible to parse the hex.")
        }

        editColor(it.parent.parent.parent.id, colorRGB)

    }
}