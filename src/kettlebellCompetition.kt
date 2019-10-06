// Kettlebell competition web app
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.routing.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.ktor.routing.Routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import os3.connectToDB

internal fun Routing.kettlebellCompetition() {
    authenticate(AuthName.SESSION, optional = true) {
        post("/api/addSet") {
            val thisSession = call.sessions.get<MySession>()
            try {
                val addSet = call.receive<ThisSet>()
                if (thisSession?.id != addSet.uuid) {
                    throw (error("Access not permitted."))
                }
                connectToDB()
                transaction {
                    SchemaUtils.create(KettleBellPresses)
                    KettleBellPresses.insert() {
                        it[uuid] = addSet.uuid
                        it[weight] = addSet.weight
                        it[repetitions] = addSet.repetitions
                        it[createdTime] = addSet.createdTime
                    }
                }
                call.respond(Status(success = true, errorMessage = ""))
            } catch (e: Throwable) {
                call.respond(Status(success = false, errorMessage = e.toString()))
            }
        }
        get("/api/getUserList") {
            call.respond(getUserList())

        }
        get("/api/getKettlebellPresses") {
            call.respond(kettlebellPresses());
        }
        get("/api/getMyID") {
            val thisSession = call.sessions.get<MySession>()
            var userName: String
            if (thisSession !== null) {
                userName = thisSession.username
                call.respond(getMyID(userName));
            } else {
                call.respond(MyID(id = 0, active = false))
            }
        }
    }
}

fun getMyID(userName: String): MyID {
    connectToDB()
    var activeID = MyID(id = 0, active = false)
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select({ Users.name eq userName })) {
            val currentUser = UserID(
                id = user[Users.id],
                name = user[Users.name]
            )
            activeID.id = currentUser.id
            activeID.active = true
        }
    }
    return activeID
}

fun getUserList(): MutableList<UserID> {
    connectToDB()
    var returnedListOfUsers = mutableListOf<UserID>()
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.selectAll()) {
            val currentUser = UserID(
                id = user[Users.id],
                name = user[Users.name]
            )
            returnedListOfUsers.add(currentUser)
        }
    }
    return returnedListOfUsers
}

fun kettlebellPresses(): MutableList<ThisSet> {
    connectToDB()
    var returnedListOfSets = mutableListOf<ThisSet>()
    transaction {
        SchemaUtils.create(KettleBellPresses)
        for (set in KettleBellPresses.selectAll()) {
            val currentSet = ThisSet(
                id = set[KettleBellPresses.id],
                uuid = set[KettleBellPresses.uuid],
                weight = set[KettleBellPresses.weight],
                repetitions = set[KettleBellPresses.repetitions],
                createdTime = set[KettleBellPresses.createdTime]
            )
            returnedListOfSets.add(currentSet)
        }
    }
    return returnedListOfSets
}