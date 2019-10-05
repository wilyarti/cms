import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.origin
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import os3.connectToDB


fun Route.adminAPI() {
    post("/api/deletePage") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val deletedPage = call.receive<thisPage>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                val txID = Pages.deleteWhere { Pages.id eq deletedPage.id }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/deletePost") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))

            val deletedPost = call.receive<thisPage>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                val txID = Posts.deleteWhere { Posts.id eq deletedPost.id }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/addPost") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val incomingPost = call.receive<thisPost>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                val txID = Posts.insert() {
                    it[disabled] = false
                    it[parentID] = 0
                    it[priorityBit] = 255
                    it[name] = incomingPost.name
                    it[icon] = incomingPost.icon
                    it[contents] = incomingPost.contents
                    it[pageID] = incomingPost.pageID // which page it is displayed on
                    it[author] = "root"
                    it[group] = "wheel"
                    it[createdTime] = incomingPost.createdTime
                    it[countryOfOrigin] = "AU"
                    it[language] = "EN"
                    it[executionScript] = "Nothing to see here."
                    it[metadata] = "Add me."
                    it[type] = 1
                    it[likes] = 0
                }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/addPage") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val incomingPage = call.receive<thisPage>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                val txID = Pages.insert() {
                    it[disabled] = false
                    it[parentID] = 0
                    it[priorityBit] = 255
                    it[name] = incomingPage.name
                    it[icon] = incomingPage.icon
                    it[pageID] = 0// unused
                    it[author] = "root"
                    it[group] = "wheel"
                    it[createdTime] = incomingPage.createdTime
                    it[countryOfOrigin] = "AU"
                    it[language] = "EN"
                    it[executionScript] = "Nothing to see here."
                    it[metadata] = "Add me."
                    it[type] = 1
                    it[likes] = 0
                }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/addUser") {
        try {
            //TODO fix this
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val incomingUser = call.receive<ThisUser>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                val txID = Users.insert() {
                    it[name] = incomingUser.name
                    it[group] = incomingUser.group
                    it[secondaryGroup] = incomingUser.secondaryGroup
                    it[password] = incomingUser.password
                    it[metadata] = "Add me."
                }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/updatePage") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val incomingPage = call.receive<thisPage>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                val txID = Pages.update({ Pages.id eq incomingPage.id }) {
                    it[disabled] = false
                    it[parentID] = 0
                    it[priorityBit] = 255
                    it[name] = incomingPage.name
                    it[icon] = incomingPage.icon
                    it[pageID] = incomingPage.pageID // which page it is displayed on
                    it[author] = "root"
                    it[group] = "wheel"
                    it[createdTime] = incomingPage.createdTime
                    it[countryOfOrigin] = "AU"
                    it[language] = "EN"
                    it[executionScript] = "Nothing to see here."
                    it[metadata] = "Add me."
                    it[type] = 1
                    it[likes] = 0
                }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/updatePost") {
        try {
            if (!validateAdmin(call)) throw (error("Prohibidado!"))
            val incomingPost = call.receive<thisPost>()
            val remoteHost: String = call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                val txID = Posts.update({ Posts.id eq incomingPost.id }) {
                    it[disabled] = false
                    it[parentID] = 0
                    it[priorityBit] = 255
                    it[name] = incomingPost.name
                    it[icon] = incomingPost.icon
                    it[contents] = incomingPost.contents
                    it[pageID] = incomingPost.pageID // which page it is displayed on
                    it[author] = "root"
                    it[group] = "wheel"
                    it[createdTime] = incomingPost.createdTime
                    it[countryOfOrigin] = "AU"
                    it[language] = "EN"
                    it[executionScript] = "Nothing to see here."
                    it[metadata] = "Add me."
                    it[type] = 1
                    it[likes] = 0
                }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }

    get("/api/getAllPostsAndPages") {
        call.respond(getAllPostsAndPages())
    }

    get("/api/getPosts") {
        call.respond(getPosts())
    }

    get("/api/getPages") {
        call.respond(getPages())
    }

}


fun validateAdmin(call: ApplicationCall): Boolean {
    val thisSession = call.sessions.get<MySession>()
    println(thisSession)
    return thisSession?.group == "wheel"
}

fun getPages(): MutableList<thisPage> {
    connectToDB()
    var returnedListOfPages = mutableListOf<thisPage>()
    transaction {
        SchemaUtils.create(Pages)
        for (page in Pages.selectAll()) {
            val currentPage = thisPage(
                id = page[Pages.id],
                disabled = page[Pages.disabled],
                parentID = page[Pages.parentID],
                priorityBit = page[Pages.priorityBit],
                name = page[Pages.name],
                icon = page[Pages.icon],
                pageID = page[Pages.pageID],
                author = page[Pages.author],
                group = page[Pages.group],
                createdTime = page[Pages.createdTime].toString(),
                countryOfOrigin = page[Pages.countryOfOrigin],
                language = page[Pages.language],
                executionScript = page[Pages.executionScript].toString(),
                metadata = page[Pages.metadata].toString(),
                type = page[Pages.type],
                likes = page[Pages.likes]
            )
            returnedListOfPages.add(currentPage)
        }
    }
    return returnedListOfPages
}

fun getPosts(): MutableList<thisPost> {
    connectToDB()
    var returnedListOfPosts = mutableListOf<thisPost>()
    transaction {
        SchemaUtils.create(Posts)
        for (p in Posts.selectAll()) {
            var currentPost = thisPost(
                id = p[Posts.id],
                disabled = p[Posts.disabled],
                parentID = p[Posts.parentID],
                priorityBit = p[Posts.priorityBit],
                name = p[Posts.name],
                icon = p[Posts.icon],
                pageID = p[Posts.pageID],
                author = p[Posts.author],
                group = p[Posts.group],
                createdTime = p[Posts.createdTime].toString(),
                countryOfOrigin = p[Posts.countryOfOrigin],
                language = p[Posts.language],
                executionScript = p[Posts.executionScript].toString(),
                contents = p[Posts.contents].toString(),
                metadata = p[Posts.metadata].toString(),
                type = p[Posts.type],
                likes = p[Posts.likes]
            )
            returnedListOfPosts.add(currentPost)
        }
    }
    return returnedListOfPosts
}

fun getAllPostsAndPages(): MutableList<completePage> {
    connectToDB()
    var returnedPages = mutableListOf<completePage>()
    transaction {
        SchemaUtils.create(Pages, Posts)
        val allPosts = Posts.selectAll()
        for (page in Pages.selectAll()) {
            val currentPage = completePage(
                id = page[Pages.id],
                disabled = page[Pages.disabled],
                parentID = page[Pages.parentID],
                priorityBit = page[Pages.priorityBit],
                name = page[Pages.name],
                icon = page[Pages.icon],
                pageID = page[Pages.pageID],
                author = page[Pages.author],
                group = page[Pages.group],
                createdTime = page[Pages.createdTime].toString(),
                countryOfOrigin = page[Pages.countryOfOrigin],
                language = page[Pages.language],
                executionScript = page[Pages.executionScript].toString(),
                metadata = page[Pages.metadata].toString(),
                type = page[Pages.type],
                likes = page[Pages.likes],
                posts = mutableListOf<thisPost>()
            )
            for (p in allPosts) {
                if (p[Posts.pageID] == page[Pages.id]) {
                    var currentPost = thisPost(
                        id = p[Posts.id],
                        disabled = p[Posts.disabled],
                        parentID = p[Posts.parentID],
                        priorityBit = p[Posts.priorityBit],
                        name = p[Posts.name],
                        icon = p[Posts.icon],
                        pageID = p[Posts.pageID],
                        author = p[Posts.author],
                        group = p[Posts.group],
                        createdTime = p[Posts.createdTime].toString(),
                        countryOfOrigin = p[Posts.countryOfOrigin],
                        language = p[Posts.language],
                        executionScript = p[Posts.executionScript].toString(),
                        contents = p[Posts.contents].toString(),
                        metadata = p[Posts.metadata].toString(),
                        type = p[Posts.type],
                        likes = p[Posts.likes]
                    )
                    currentPage.posts.add(currentPost)
                }
            }
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}
