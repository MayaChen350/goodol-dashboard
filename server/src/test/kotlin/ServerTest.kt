package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.data.tables.CategoriesTable
import io.github.mayachen350.goodolServer.data.tables.TasksTable
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.CategoryDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewTaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.SomeoneDisplayDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.TaskEditDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.NewWeekDTO
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

suspend fun ApplicationTestBuilder.setup() {
    val testDbUUID = UUID.randomUUID()

    application {
        rootModule(H2DbConnection(testDbUUID))
        configureFlyway(H2DbConnection(testDbUUID))
    }

    // verify server root returns 200
    // also this MIGHT be warming up the database connection
    assertEquals("OK", client.get("/").bodyAsText())
}

class ServerTest {
    @Test
    fun `test correct responsibles data`() = testApplication {
        setup()

        val peopleResponse = client.get("/weeklyTasking/people")

        assertEquals(3, Json.decodeFromString<Array<SomeoneDisplayDTO>>(peopleResponse.bodyAsText()).size)
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class WeekCreationTest {
        @Test
        @Order(1)
        fun `test week created`() = testApplication {
            setup()

            val weekId = 1u

            val response = client.post("/weeklyTasking/week") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToJsonElement(NewWeekDTO(weekId, LocalDate.today())).toString())
            }

            assertEquals(HttpStatusCode.Created, response.status)

            assertEquals(1, suspendTransaction { WeeksTable.deleteWhere { WeeksTable.id eq weekId } })
        }

        @Test
        @Order(2)
        fun `test week with same date fail`() = testApplication {
            setup()

            client.post("/weeklyTasking/week") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToJsonElement(NewWeekDTO(2u, LocalDate.today())).toString())
            }

            assertEquals(
                HttpStatusCode.Conflict,
                client.post("/weeklyTasking/week") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(NewWeekDTO(10u, LocalDate.today())).toString())
                }.status
            )
        }

        @Test
        fun `test week in the past fail`() = testApplication {
            setup()

            assertEquals(
                HttpStatusCode.Forbidden,
                client.post("/weeklyTasking/week") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(NewWeekDTO(10u, LocalDate(2025, 2, 10))).toString())
                }.status
            )

            assertEquals(emptyList(), suspendTransaction {
                WeeksTable.selectAll().toList()
            })
        }
    }

    @Test
    fun `test week currentWeekId already made week`() = testApplication {
        setup()

        // create current week at id 2 (database doesn't wanna connect in time for the service it seems or smth)
        suspendTransaction {
            WeeksTable.insert {
                it[id] = 2u
                it[startDate] = LocalDate.today().atStartOfWeek()
                it[endDate] = LocalDate.today().atEndOfWeek()
            }
        }

        val weekId: UInt = client.get("/weeklyTasking/week/currentWeekId").body<Int>().toUInt()

        assertEquals(2u, weekId)
    }

    @Test
    fun `test week currentWeekId with calculated weekId`() = testApplication {
        setup()

        // create week 5 weeks ago at id 4
        // using it like that because creating a week before this week should NOT work from the endpoints
        suspendTransaction {
            WeeksTable.insert {
                it[id] = 4u
                it[startDate] = LocalDate.today().atStartOfWeek().minus(5, DateTimeUnit.WEEK)
                it[endDate] = LocalDate.today().atEndOfWeek().minus(5, DateTimeUnit.WEEK)
            }
        }

        // this should be 5 weeks later than id 4
        val weekId: UInt = client.get("/weeklyTasking/week/currentWeekId").body<Int>().toUInt()
        assertEquals(4u + 5u, weekId)
    }

    @Test
    fun `test task creation`() = testApplication {
        setup()

        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 2
                it[name] = "testt"
            }
        }

        val task = NewTaskDTO("testt", 2)

        assertEquals(HttpStatusCode.Created, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task))
        }.status)

        val response = client.get("/weeklyTasking/tasks")
        println(response.bodyAsText())

        assertTrue(Json.decodeFromString<Array<TaskDTO>>(response.bodyAsText()).any { it.name == task.name })
    }

    @Test
    fun `test task creation uncategorized`() = testApplication {
        setup()

        assertEquals(HttpStatusCode.Created, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(NewTaskDTO("testtt", null)))
        }.status)

        val response = client.get("/weeklyTasking/tasks")

        assertTrue(Json.decodeFromString<Array<TaskDTO>>(response.bodyAsText()).any { it.categoryId == null })
    }

    @Test
    fun `test task creation with conflict`() = testApplication {
        setup()

        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 2
                it[name] = "test"
            }
        }

        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 3
                it[name] = "test 2"
            }
        }

        val taskInitial = NewTaskDTO("Test", 2)
        val taskSameCategory = NewTaskDTO("Test 2", 2)
        val taskSameName = NewTaskDTO("Test", 3)

        assertEquals(HttpStatusCode.Created, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(taskInitial))
        }.status)

        // this should work
        assertEquals(HttpStatusCode.Created, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(taskSameCategory))
        }.status)

        assertEquals(HttpStatusCode.Conflict, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(taskSameName))
        }.status)
    }

    @Test
    fun `test task deletion`() = testApplication {
        setup()

        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 2
                it[name] = "testt"
            }
        }

        val task = NewTaskDTO("Test", 2)

        assertEquals(
            HttpStatusCode.NotFound,
            client.delete("/weeklyTasking/tasks/${task.name}").status
        )

        assertEquals(HttpStatusCode.Created, client.post("/weeklyTasking/tasks") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(task))
        }.status)

        assertEquals(
            HttpStatusCode.OK,
            client.delete("/weeklyTasking/tasks/${task.name}").status
        )

        val response = client.get("/weeklyTasking/tasks")
        println(response.bodyAsText())

        assertFalse(Json.decodeFromString<Array<TaskDTO>>(response.bodyAsText()).any { it.name == task.name })

        assertEquals(
            HttpStatusCode.NotFound,
            client.delete("/weeklyTasking/tasks/${task.name}").status
        )
    }

    @Test
    fun `test task updates`() = testApplication {
        setup()

        suspendTransaction {
            TasksTable.insert {
                it[id] = 2
                it[name] = "test"
                it[categoryId] = null
            }
        }

        // test not working
        assertEquals(
            HttpStatusCode.BadRequest, client.put(
                "/weeklyTasking/tasks/edit/2"
            ).status
        )

        // test not found
        assertEquals(
            HttpStatusCode.NotFound, client.put(
                "/weeklyTasking/tasks/edit/500"
            ).status
        )

        assertEquals(
            HttpStatusCode.NotFound, client.put("/weeklyTasking/tasks/edit/2") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(TaskEditDTO("test", 500)))
            }.status
        )

        // test conflict
        suspendTransaction {
            TasksTable.insert {
                it[id] = 3
                it[name] = "test 700"
                it[categoryId] = null
            }
        }

        assertEquals(
            HttpStatusCode.Conflict, client.put("/weeklyTasking/tasks/edit/2") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(TaskEditDTO("test 700", null)))
            }.status
        )

        val noConflictData = Json.decodeFromString<TaskDTO>(client.get("/weeklyTasking/tasks/2").bodyAsText())
        assertTrue { noConflictData.name == "test" }

        // test change name
        assertEquals(
            HttpStatusCode.OK, client.put("/weeklyTasking/tasks/edit/2") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(TaskEditDTO("new test", null)))
            }.status
        )

        val data = Json.decodeFromString<TaskDTO>(client.get("/weeklyTasking/tasks/2").bodyAsText())
        assertTrue { data.name == "new test" && data.categoryId == null }

        // test change category
        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 2
                it[name] = "testt"
            }
        }

        assertEquals(
            HttpStatusCode.OK, client.put("/weeklyTasking/tasks/edit/2") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(TaskEditDTO("new test", 2)))
            }.status
        )

        val newCategoryData = Json.decodeFromString<TaskDTO>(client.get("/weeklyTasking/tasks/2").bodyAsText())
        assertTrue { newCategoryData.categoryId == 2 }

        // test change all
        assertEquals(
            HttpStatusCode.OK, client.put("/weeklyTasking/tasks/edit/2") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(TaskEditDTO(":3", null)))
            }.status
        )

        val allChangedData = Json.decodeFromString<TaskDTO>(client.get("/weeklyTasking/tasks/2").bodyAsText())
        assertTrue { allChangedData.categoryId == null && allChangedData.name == ":3" }
    }

    @Test
    fun `test category creation`(): Unit = testApplication {
        setup()

        val categoryName: String = "category name :3"

        assertEquals(
            HttpStatusCode.Created,
            client.post("/weeklyTasking/categories/${categoryName}").status
        )

        // test conflict
        assertEquals(
            HttpStatusCode.Conflict,
            client.post("/weeklyTasking/categories/${categoryName}").status
        )

        // test the get all endpoint
        assertTrue {
            Json.decodeFromString<List<CategoryDTO>>(client.get("/weeklyTasking/categories").bodyAsText()).any {
                it.name == categoryName
            }
        }

    }

    @Test
    fun `test rename category`(): Unit = testApplication {
        setup()

        // test not found
        assertEquals(
            HttpStatusCode.NotFound,
            client.put("/weeklyTasking/categories/rename/500").status
        )

        val testId: Int =
            Json.decodeFromString<CategoryDTO>(client.post("/weeklyTasking/categories/Eucli").bodyAsText())
                .id

        // test not modified (I included that for some reason, but that should most likely be done to the client)
        assertEquals(
            HttpStatusCode.NotModified,
            client.put("/weeklyTasking/categories/rename/${testId}") {
                setBody("Eucli")
            }.status
        )

        // actual modification test
        assertEquals(
            CategoryDTO(testId, "Euclid"),
            Json.decodeFromString<CategoryDTO>(client.put("/weeklyTasking/categories/rename/${testId}") {
                setBody("Euclid")
            }.bodyAsText())
        )

        // test conflict
        suspendTransaction {
            CategoriesTable.insert {
                it[CategoriesTable.id] = testId + 1
                it[CategoriesTable.name] = "Keter"
            }
        }

        assertEquals(
            HttpStatusCode.Conflict,
            client.put("/weeklyTasking/categories/rename/${testId}") {
                setBody("Keter")
            }.status
        )
    }

    @Test
    fun `test category deletion`(): Unit = testApplication {
        setup()

        // test not found
        assertEquals(
            HttpStatusCode.NotFound,
            client.delete("/weeklyTasking/categories/20").status
        )

        suspendTransaction {
            CategoriesTable.insert {
                it[id] = 20
                it[name] = "Medbay"
            }
        }

        suspendTransaction {
            TasksTable.insert {
                it[id] = 20
                it[name] = "Scan!1"
                it[categoryId] = 20
            }
        }

        assertEquals(
            HttpStatusCode.Forbidden,
            client.delete("/weeklyTasking/categories/20").status
        )

        client.delete("/weeklyTasking/tasks/Scan!1") // if this wouldn't work, then deleting tasks by name would be an issue 🥀
        // thinking about it, maybe deleting by id would be better why did I not do that

        assertEquals(
            HttpStatusCode.OK,
            client.delete("/weeklyTasking/categories/20").status
        )

        assertFalse {
            Json.decodeFromString<List<CategoryDTO>>(client.get("/weeklyTasking/categories").bodyAsText()).any {
                it.id == 20
            }
        }
    }

    @Test
    fun `test people edit name`() = testApplication {
        setup()

        assertEquals(
            HttpStatusCode.NotFound,
            client.put("/weeklyTasking/people/0/rename").status
        )

        assertEquals(
            HttpStatusCode.Conflict,
            client.put("/weeklyTasking/people/1/rename") {
                setBody("bat")
            }.status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/rename") {
                setBody("Giratuna")
            }.status
        )

        assertTrue {
            Json.decodeFromString<List<SomeoneDisplayDTO>>(client.get("/weeklyTasking/people").bodyAsText()).any {
                it.id == 1 && it.name == "Giratuna"
            }
        }
    }

    @Test
    fun `test people edit color`() = testApplication {
        setup()

        assertEquals(
            HttpStatusCode.NotFound,
            client.put("/weeklyTasking/people/0/color/2").status
        )

        // test invalid values
        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("/weeklyTasking/people/1/color/num/${UInt.MAX_VALUE}").status
        )

        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("/weeklyTasking/people/1/color/num/${-1}").status
        )

        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("/weeklyTasking/people/1/color/hex/IDGAF").status
        )

        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("/weeklyTasking/people/1/color/hex/12345678").status
        )

        assertEquals(
            HttpStatusCode.BadRequest,
            client.put("/weeklyTasking/people/1/color/num/00f").status
        )

        // test correct values
        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/num/0").status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/hex/0").status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/num/00").status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/hex/00").status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/hex/00f").status
        )

        assertEquals(
            HttpStatusCode.OK,
            client.put("/weeklyTasking/people/1/color/hex/FAFA").status
        )

        assertTrue {
            Json.decodeFromString<List<SomeoneDisplayDTO>>(client.get("/weeklyTasking/people").bodyAsText()).any {
                it.id == 1 && it.chosenColor == 0xFAFAu
            }
        }
    }

}