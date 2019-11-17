package os3

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.html.body

fun Route.channelGrapher() {
    get("/api/getWeatherStation") {
        call.respond(getAllWeatherStations())
    }
}