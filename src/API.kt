package os3

import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.API() {
    post("/getJSON") {
        val thisReq = call.receive<JsonReq>()
        val req = khttp.get(thisReq.url)
        println(thisReq.url)
        if (req.statusCode == 200) {
            call.respond(req.jsonObject)
        } else {
            call.respond(Success(false))
        }
    }

}