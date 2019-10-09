// Routes and API for the /rtstats.html web app.
package os3

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import redis.clients.jedis.Jedis

fun initRedis(): Jedis {
    return Jedis()
}
fun checkParam(param: String?): Boolean {
    val dateParam = param!!.split("-")
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    for (month in months) {
        if (month == dateParam[0]) {
            for (i in 1..31) {
                if (dateParam[1] == i.toString()) {
                    return true
                }
            }
        }
    }
    return false
}

fun Route.rtStatsGraphingAPI(){
    get ("/graphingAPI/test") {
        call.respond(Success(true))
    }
    get("/graphingAPI/rtallowed/{date}") {
        val param = call.parameters["date"]
        if (checkParam(param)) {
            val query: MutableMap<String, String>
            var s1: String
            var s2: String
            val redis = initRedis()
            query = redis.hgetAll("${param}:query")

            for (i in 0..23) {
                if (i < 10) {
                    s1 = "0${i}"
                } else {
                    s1 = i.toString()
                }
                for (j in 0..59) {
                    if (j < 10) {
                        s2 = "0${j}"
                    } else {
                        s2 = j.toString()
                    }
                    val s = ("${s1}:${s2}")
                    if (!query.containsKey(s)) {
                        query[s] = "0"
                    }
                }
            }
            println("Route /rtallowed called with ${call.parameters["date"]}")
            call.respond(query.toSortedMap())
        } else {
            println("Invalid /rtallowed called with ${call.parameters["date"]}")
            call.respondText { "invalid request" }
        }
    }
    get("/graphingAPI/rtblocked/{date}") {
        val param = call.parameters["date"]
        if (checkParam(param)) {
            val query: MutableMap<String, String>
            var s1: String
            var s2: String
            val redis = initRedis()
            query = redis.hgetAll("${param}:blocked")

            for (i in 0..23) {
                if (i < 10) {
                    s1 = "0${i}"
                } else {
                    s1 = i.toString()
                }
                for (j in 0..59) {
                    if (j < 10) {
                        s2 = "0${j}"
                    } else {
                        s2 = j.toString()
                    }
                    val s = ("${s1}:${s2}")
                    if (!query.containsKey(s)) {
                        query[s] = "0"
                    }
                }
            }
            println("Route /rtblocked called with ${call.parameters["date"]}")
            call.respond(query.toSortedMap())
        } else {
            println("Invalid /rtblocked called with ${call.parameters["date"]}")
            call.respondText { "invalid request" }
        }
    }
    get("/graphingAPI/topblocked/{date}") {
        val param = call.parameters["date"]
        if (checkParam(param)) {
            val query: MutableMap<String, String>
            val topQuery = mutableMapOf<String, String>()
            val redis = initRedis()
            query = redis.hgetAll("${param}:blocked:domains")
            println("Route /topblocked called with ${call.parameters["date"]}")
            var i = 0
            for ((k, v) in query.toList().sortedByDescending { (_, value) -> value.toInt() }.toMap()) {
                topQuery[k] = v
                i++
                if (i == 30) {
                    break
                }
            }
            call.respond(topQuery)
        } else {
            println("Invalid /topblocked called with ${call.parameters["date"]}")
            call.respondText { "invalid request" }
        }
    }
    get("/graphingAPI/rtstats") {
        val query = mutableMapOf<String, String>()
        val redis = initRedis()
        query["Total Queries: "] = redis.hget("totals", "query")
        query["Total Clients: "] = redis.hlen("totals").toString()
        query["Total Domains: "] = redis.hlen("domains").toString()
        call.respond(query.toSortedMap())
    }
    get ("/graphingAPI/getLocationData") {
        call.respond(getLocationData())
    }
}

fun getLocationData(): MutableList<List<String?>> {
    val redis = initRedis()
    val query: MutableMap<String, String>
    val ourLocationData = mutableListOf<List<String?>>()
    query = redis.hgetAll("locationCodeTotals")
    val countryCodes = countryCode()
    for ((k,v) in query) {
        val thisCode = listOf(countryCodes[k],v)
        ourLocationData.add(thisCode)
    }
    return ourLocationData
}