package os3

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.adminAPI() {
    post("/api/deletePage") {
        try {
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val deletedPage = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.deleteWhere { Pages.id eq deletedPage.id }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/deletePost") {
        try {
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }

            val deletedPost = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                Posts.deleteWhere { Posts.id eq deletedPost.id }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/deleteUser") {
        try {
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }

            val deletedUser = call.receive<UserID>()
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.deleteWhere { Users.id eq deletedUser.id }
            }
            call.respond(Status(success = true, errorMessage = ""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/addPost") {
        try {
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val incomingPost = call.receive<ThisPost>()
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                Posts.insert() {
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
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val incomingPage = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.insert() {
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
            //TODO fix what?
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val incomingUser = call.receive<ThisUser>()
            call.request.origin.remoteHost
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.insert {
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
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val incomingPage = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.update({ Pages.id eq incomingPage.id }) {
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
            if (!validateAdmin(call)) {
                Status(success = true, errorMessage = "Error! Prohibited.")
            }
            val incomingPost = call.receive<ThisPost>()
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                Posts.update({ Posts.id eq incomingPost.id }) {
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

    get("/api/getUsers") {
        call.respond(getUsers())
    }

}


fun validateAdmin(call: ApplicationCall): Boolean {
    val thisSession = call.sessions.get<MySession>()
    println(thisSession)
    return thisSession?.group == "wheel"
}

fun getPages(): MutableList<ThisPage> {
    connectToDB()
    val returnedListOfPages = mutableListOf<ThisPage>()
    transaction {
        SchemaUtils.create(Pages)
        for (page in Pages.selectAll()) {
            val currentPage = ThisPage(
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

fun getPosts(): MutableList<ThisPost> {
    connectToDB()
    val returnedListOfPosts = mutableListOf<ThisPost>()
    transaction {
        SchemaUtils.create(Posts)
        for (p in Posts.selectAll()) {
            val currentPost = ThisPost(
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

fun getUsers(): MutableList<ThisUser> {
    connectToDB()
    val returnedListOfUsers = mutableListOf<ThisUser>()
    transaction {
        SchemaUtils.create(Users)
        for (p in Users.selectAll()) {
            val currentUser = ThisUser(
                id = p[Users.id],
                name = p[Users.name],
                group = p[Users.group],
                secondaryGroup = p[Users.secondaryGroup],
                password = "",
                metadata = p[Users.metadata]
            )
            returnedListOfUsers.add(currentUser)
        }
    }
    return returnedListOfUsers
}

fun getAllPostsAndPages(): MutableList<CompletePage> {
    connectToDB()
    val returnedPages = mutableListOf<CompletePage>()
    transaction {
        SchemaUtils.create(Pages, Posts)
        val allPosts = Posts.selectAll()
        for (page in Pages.selectAll()) {
            val currentPage = CompletePage(
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
                posts = mutableListOf<ThisPost>()
            )
            for (p in allPosts) {
                if (p[Posts.pageID] == page[Pages.id]) {
                    val currentPost = ThisPost(
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
