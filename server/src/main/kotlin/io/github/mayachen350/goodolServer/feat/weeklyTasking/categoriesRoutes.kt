package io.github.mayachen350.goodolServer.feat.weeklyTasking

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.CategoriesTable
import io.github.mayachen350.goodolServer.utils.catchConflicts
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.utils.io.ExperimentalKtorApi

@Resource("/categories")
private class Categories {
    @Resource("{id}")
    class Id(val parent: Categories = Categories(), val id: Int)

    @Resource("{name}")
    class Name(val parent: Categories = Categories(), val name: String)
}

@OptIn(ExperimentalKtorApi::class)
fun Route.includeCategoriesRoutes() {
    get<Categories> {
        call.respond<List<CategoryDTO>>(WeeklyTaskingService.Category.getAll().map {
            CategoryDTO(it[CategoriesTable.id].value, it[CategoriesTable.name])
        })
    }
    post<Categories.Name> {
        catchConflicts {
            call.respond<CategoryDTO>(
                HttpStatusCode.Created,
                WeeklyTaskingService.Category.createNew(it.name).let {
                    CategoryDTO(it[CategoriesTable.id].value, it[CategoriesTable.name])
                }
            )
        }
    }
}