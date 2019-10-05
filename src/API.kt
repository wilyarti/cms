import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.html.respondHtml
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import kotlinx.css.body

fun Route.API() {
    post("/getJSON") {
        val thisReq = call.receive<jsonReq>()
        val req = khttp.get(thisReq.url)
        println(thisReq.url)
        if (req.statusCode == 200) {
            call.respond(req.jsonObject)
        } else {
            call.respond(Success(false))
        }
    }

}