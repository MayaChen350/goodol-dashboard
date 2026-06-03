package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.SomeoneDisplayDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekDTO
import io.github.mayachen350.goodolServer.utils.atEndOfWeek
import io.github.mayachen350.goodolServer.utils.atStartOfWeek
import io.github.mayachen350.goodolServer.utils.today
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteAll
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals

suspend fun ApplicationTestBuilder.setup() {
    application {
        rootModule(H2DbConnection)
        configureFlyway(H2DbConnection)
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
                setBody(Json.encodeToJsonElement(WeekDTO(weekId, LocalDate.today())).toString())
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
                setBody(Json.encodeToJsonElement(WeekDTO(2u, LocalDate.today())).toString())
            }

            assertEquals(
                HttpStatusCode.Conflict,
                client.post("/weeklyTasking/week") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(WeekDTO(10u, LocalDate.today())).toString())
                }.status
            )

            suspendTransaction {
                WeeksTable.deleteAll()
            }
        }

        @Test
        fun `test week in the past fail`() = testApplication {
            setup()

            assertEquals(
                HttpStatusCode.Forbidden,
                client.post("/weeklyTasking/week") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(WeekDTO(10u, LocalDate(2025, 2, 10))).toString())
                }.status
            )

            assertEquals(emptyList(),suspendTransaction {
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

        suspendTransaction {
            WeeksTable.deleteWhere { WeeksTable.id eq 2u }
        }
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

        suspendTransaction {
            WeeksTable.deleteAll()
        }
    }
}