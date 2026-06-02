package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.data.services.WeeklyTaskingService
import io.github.mayachen350.goodolServer.data.tables.WeeksTable
import io.github.mayachen350.goodolServer.feat.weeklyTasking.SomeoneDisplayDTO
import io.github.mayachen350.goodolServer.feat.weeklyTasking.WeekDTO
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestMethodOrder
import kotlin.test.Test
import kotlin.test.assertEquals

fun ApplicationTestBuilder.setup() {
    application {
        rootModule(H2DbConnection)
        configureFlyway(H2DbConnection)
    }
}

class ServerTest {
    @Test
    fun `test root endpoint`() = testApplication {
        application {
            rootModule(H2DbConnection)
        }
        // verify server root returns 200
        assertEquals(HttpStatusCode.OK, client.get("/").status)
        assertEquals("OK", client.get("/").bodyAsText())
    }

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
                setBody(Json.encodeToJsonElement(WeekDTO(weekId, LocalDate(2026, 2, 10))).toString())
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
                setBody(Json.encodeToJsonElement(WeekDTO(2u, LocalDate(2026, 2, 11))).toString())
            }

            assertEquals(
                HttpStatusCode.Conflict,
                client.post("/weeklyTasking/week") {
                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToJsonElement(WeekDTO(10u, LocalDate(2026, 2, 11))).toString())
                }.status
            )

            suspendTransaction {
                WeeksTable.deleteWhere { WeeksTable.id eq 2u }
            }
        }
    }
}