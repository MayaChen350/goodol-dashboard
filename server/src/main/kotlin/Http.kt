package io.github.mayachen350

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.response.*
import io.ktor.server.plugins.cors.routing.*
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.*
import io.ktor.openapi.OpenApiInfo
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.OpenApiDocSource

fun Application.configureHttp() {
    install(CachingHeaders) {
        options { call, outgoingContent ->
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
                else -> null
            }
        }
    }
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(SimpleCache) {
        memoryCache {
            invalidateAt = 10.seconds
        }
    }
    install(Compression)
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
    routing {
        swaggerUI(path = "swagger") {
            /*
             Documentation source configuration goes here.
    
             This can be from file (documentation.yaml), or it can be served dynamically from your sources using the
             `describe {}` API on routes.  When `openApi` enabled in Gradle, these calls will be automatically injected
             based on your code and comments.
             */
            info = OpenApiInfo("Goodol Dashboard API", "0.1.0")
            source = OpenApiDocSource.Routing(ContentType.Application.Json) {
                routingRoot.descendants()
            }
        }
    }
}
