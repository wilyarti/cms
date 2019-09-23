package os3

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.content.*
import io.ktor.http.content.*
import io.ktor.features.*
import org.slf4j.event.*
import io.ktor.gson.*
import dynamicPagesAPI

data class jsonReq(
    val url: String
)
data class Success(val success: Boolean)

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        static("/static") {
            resources("static")
            resources("static/static")
        }
        dynamicPagesAPI()
        post("/api/getJSON") {
            val thisReq = call.receive<jsonReq>()
            val req = khttp.get(thisReq.url)
            println(thisReq.url)
            if (req.statusCode == 200) {
                call.respond(req.jsonObject)
            } else {
                call.respond( Success (success = false ))
            }
        }
        post("/api/getHTML") {
            val thisReq = call.receive<jsonReq>()
            val req = khttp.get(thisReq.url)
            println(thisReq.url)
            if (req.statusCode == 200) {
                call.respond(req.content)
            } else {
                call.respondHtml { body { +"Unsuccessful request" } }
            }
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
