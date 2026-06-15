package io.github.mayachen350.goodolServer.feat.weeklyTasking

import arrow.core.getOrElse
import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.CategoriesTable
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.utils.io.ExperimentalKtorApi

@Resource("/categories")
private class Categories {
    @Resource("{id}")
    class Id(val parent: Categories = Categories(), val id: CategoryId)

    @Resource("{name}")
    class Name(val parent: Categories = Categories(), val name: String)

    @Resource("rename")
    class Rename(val parent: Categories = Categories()) {
        @Resource("{id}")
        class Id(val parent: Rename = Rename(), val id: CategoryId)
    }
}

@OptIn(ExperimentalKtorApi::class)
fun Route.includeCategoriesRoutes() {
    get<Categories> {
        call.respond<List<CategoryDTO>>(WeeklyTaskingService.Category.getAll().map {
            CategoryDTO(CategoryId(it[CategoriesTable.id].value), it[CategoriesTable.name])
        })
    }

    post<Categories.Name> {
        call.respond<CategoryDTO>(
            HttpStatusCode.Created,
            WeeklyTaskingService.Category.createNew(it.name).let {
                CategoryDTO(CategoryId(it[CategoriesTable.id].value), it[CategoriesTable.name])
            }
        )
    }

    put<Categories.Rename.Id> {
        val id = it.id.validate().getOrElse {
            return@put call.respond(HttpStatusCode.NotFound, "No category with that id.")
        }

        val categoryWithNewName = CategoryDTO(it.id, call.receive<String>())
        if (WeeklyTaskingService.Category.exists(categoryWithNewName)) {
            // I guess this can be more useful than an "OK" when you expected something changed
            return@put call.respond<CategoryDTO>(HttpStatusCode.NotModified, categoryWithNewName)
        }

        call.respond<CategoryDTO>(
            HttpStatusCode.OK,
            WeeklyTaskingService.Category.rename(id, categoryWithNewName.name).let {
                CategoryDTO(CategoryId(it[CategoriesTable.id].value), it[CategoriesTable.name])
            }
        )
    }

    delete<Categories.Id> {
        val id = it.id.validate().getOrElse {
            return@delete call.respond(HttpStatusCode.NotFound, "No category with that id.")
        }

        val result = WeeklyTaskingService.Category.delete(id)
        if (result.isLeft())
            return@delete call.respond(HttpStatusCode.Forbidden, "The category still contains tasks!")

        call.respond(HttpStatusCode.OK, "The category has been suppressed.")
    }
}