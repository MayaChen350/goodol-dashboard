package io.github.mayachen350.goodolServer

import io.github.mayachen350.goodolServer.feat.weeklyTasking.SomeoneDisplayDTO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.*

class ServerTest {

    @BeforeTest
    fun setup() {

    }

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
        application {
            rootModule(H2DbConnection)
            configureFlyway(H2DbConnection)
        }

        val peopleResponse = client.get("/weeklyTasking/people")

        assertEquals(3, Json.decodeFromString<Array<SomeoneDisplayDTO>>(peopleResponse.bodyAsText()).size)
    }

}