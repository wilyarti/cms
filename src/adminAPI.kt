package os3

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.coroutines.*
import net.opens3.OUR_URL
import net.opens3.STATIC_FILESTORAGE
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.io.InputStream
import java.io.OutputStream

fun Route.adminAPI() {
    post("/api/deletePage") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val deletedPage = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.deleteWhere { Pages.id eq deletedPage.id }
            }
            call.respond(Status(success = true, errorMessage = "Successfully deleted page \"${deletedPage.name}\""))
        } catch (e: Throwable) {
            call.response.status(HttpStatusCode.InternalServerError)
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/deletePost") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }

            val deletedPost = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                Posts.deleteWhere { Posts.id eq deletedPost.id }
            }
            call.respond(Status(success = true, errorMessage = "Successfully deleted post \"${deletedPost.name}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/deleteUser") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val incomingUser = call.receive<ReadWriteThisUser>()
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.deleteWhere { Users.id eq incomingUser.id }
            }
            call.respond(
                Status(
                    success = true,
                    errorMessage = "Successfully deleted user \"${incomingUser.username}\""
                )
            )
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }

    post("/api/uploadFile") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val thisSession = call.sessions.get<MySession>()
            val creatingUser = getThisUser(thisSession?.id);
            if (creatingUser === null) {
                call.response.status(HttpStatusCode.Unauthorized)
                call.respond(
                    Status(
                        success = false,
                        errorMessage = "ERROR: User ${thisSession?.username} does not exit in database."
                    )
                )
            }
            // multipart upload
            val multipart = call.receiveMultipart()
            var fileName = ""
            var filePath = "some path"
            // Processes each part of the multipart input content of the user
            multipart.forEachPart { part ->
                if (part is PartData.FormItem) {
                    if (part.name == "title") {
                        fileName = part.value
                    }
                } else if (part is PartData.FileItem) {
                    val ext = File(part.originalFileName).extension
                    val file = File(
                        "${STATIC_FILESTORAGE}/",
                        "upload-${System.currentTimeMillis()}-${thisSession?.id?.hashCode()}-${fileName.hashCode()}.$ext"
                    )
                    fileName =
                        "upload-${System.currentTimeMillis()}-${thisSession?.id?.hashCode()}-${fileName.hashCode()}.$ext"
                    filePath = "${STATIC_FILESTORAGE}/$fileName"
                    part.streamProvider().use { its -> file.outputStream().buffered().use { its.copyToSuspend(it) } }
                }
                part.dispose()
            }
            //call.respondRedirect(VideoPage(id))
            connectToDB()
            transaction {
                SchemaUtils.create(Files)
                Files.insert {
                    it[name] = fileName
                    it[path] = filePath
                }
            }
            call.respond(HttpStatusCode.OK)
        } catch (e: Throwable) {
            println(e)
            call.respond(HttpStatusCode.BadRequest)
        }
    }
    post("/api/addPost") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val thisSession = call.sessions.get<MySession>()
            val creatingUser = getThisUser(thisSession?.id);
            val incomingPost = call.receive<ThisPost>()
            if (creatingUser === null) {
                call.response.status(HttpStatusCode.Unauthorized)
                call.respond(
                    Status(
                        success = false,
                        errorMessage = "ERROR: User ${thisSession?.username} does not exit in database."
                    )
                )
            }
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                val insertedPost = Posts.insert {
                    it[disabled] = incomingPost.disabled
                    it[name] = incomingPost.name
                    it[icon] = incomingPost.icon
                    it[pageID] = incomingPost.pageID
                    it[author] = creatingUser!!.username
                    it[createdTime] = incomingPost.createdTime
                    it[timeZone] = incomingPost.timeZone
                    it[contents] = incomingPost.contents
                    // NULLABLE entries below
                    it[parentID] = null
                    it[priorityBit] = null
                    it[group] = null
                    it[countryOfOrigin] = null
                    it[language] = null
                    it[executionScript] = null
                    it[metadata] = null
                    it[type] = null
                    it[likes] = null
                } get Posts.id
                ping(
                    Ping(
                        title = incomingPost.name,
                        rssurl = "https://",
                        blogurl = "https://$OUR_URL/post/${insertedPost}",
                        chk_blogs = "on",
                        chk_feedburner = "on",
                        chk_superfeedr = "on",
                        chk_tailrank = "on"
                    )
                )
            }
            call.respond(Status(success = true, errorMessage = "Successfully added post \"${incomingPost.name}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/addPage") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val thisSession = call.sessions.get<MySession>()
            val creatingUser = getThisUser(thisSession?.id);
            val incomingPage = call.receive<ThisPage>()
            if (creatingUser === null) {
                call.response.status(HttpStatusCode.Unauthorized)
                call.respond(
                    Status(
                        success = false,
                        errorMessage = "ERROR: User ${thisSession?.username} does not exit in database."
                    )
                )
            }
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.insert {
                    it[disabled] = incomingPage.disabled
                    it[name] = incomingPage.name
                    it[icon] = incomingPage.icon
                    it[pageID] = incomingPage.pageID
                    it[author] = creatingUser!!.username
                    it[createdTime] = incomingPage.createdTime
                    it[timeZone] = incomingPage.timeZone
                    // NULLABLE entries below
                    it[parentID] = null
                    it[priorityBit] = null
                    it[group] = null
                    it[countryOfOrigin] = null
                    it[language] = null
                    it[executionScript] = null
                    it[metadata] = null
                    it[type] = null
                    it[likes] = null
                }
            }
            call.respond(Status(success = true, errorMessage = "Successfully added page \"${incomingPage.name}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }

    post("/api/addUser") {
        try {
            //TODO implement missing fields
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val incomingUser = call.receive<CreateThisUser>()
            if (!isUsernameAvailable(incomingUser.username)) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(Status(success = false, errorMessage = "ERROR: Username is not available."))
            }
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.insert {
                    it[disabled] = incomingUser.disabled
                    it[group] = incomingUser.group
                    it[createdTime] = incomingUser.createdTime
                    it[username] = incomingUser.username
                    // NULLABLE entries
                    it[firstName] = incomingUser.firstName
                    it[lastName] = incomingUser.lastName
                    it[streetAddress] = incomingUser.streetAddress
                    it[postCode] = incomingUser.postCode
                    it[state] = incomingUser.state
                    it[country] = incomingUser.country
                    it[countryCode] = incomingUser.countryCode
                    it[language] = incomingUser.language
                    it[email] = incomingUser.email
                    it[areaCode] = incomingUser.areaCode
                    it[mobile] = incomingUser.mobile
                    it[secondaryGroup] = incomingUser.secondaryGroup
                    it[metadata] = incomingUser.metadata
                    it[password] = incomingUser.password
                    it[passwordSalt] = "TODO"
                }
            }
            call.respond(Status(success = true, errorMessage = "Successfully added user \"${incomingUser.username}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/isUserNameAvailable") {
        val incomingUser = call.receive<UserNameCheck>()

        if (!isUsernameAvailable(incomingUser.username)) {
            call.respond(Status(success = false, errorMessage = "Username is not available"))
        } else {
            call.respond(Status(success = true, errorMessage = "Username is available."))
        }
    }
    post("/api/userSignUp") {
        try {
            //TODO implement missing fields
            val incomingUser = call.receive<CreateThisUser>()
            if (!isUsernameAvailable(incomingUser.username)) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respond(Status(success = false, errorMessage = "ERROR: Username is not available."))
            }
            val saltyPassword = BCrypt.gensalt()
            val passwordHashed = BCrypt.hashpw(incomingUser.password, saltyPassword)
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.insert {
                    it[disabled] = true
                    it[group] = "user"
                    it[createdTime] = incomingUser.createdTime
                    it[username] = incomingUser.username
                    // NULLABLE entries
                    it[firstName] = incomingUser.firstName
                    it[lastName] = incomingUser.lastName
                    it[streetAddress] = null
                    it[postCode] = null
                    it[state] = null
                    it[country] = null
                    it[countryCode] = null
                    it[language] = null
                    it[email] = incomingUser.email
                    it[areaCode] = null
                    it[mobile] = null
                    it[secondaryGroup] = null
                    it[metadata] = null
                    it[password] = passwordHashed
                    it[passwordSalt] = saltyPassword
                }
            }
            call.respond(Status(success = true, errorMessage = "Successfully added user \"${incomingUser.username}\""))
        } catch (e: Throwable) {
            call.response.status(HttpStatusCode.InternalServerError)
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/updateUser") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val incomingUser = call.receive<CreateThisUser>()
            if (!isUsernameChangeAvailable(incomingUser.username, incomingUser.id)) {
                call.response.status(HttpStatusCode.Unauthorized)
                call.respond(Status(success = false, errorMessage = "ERROR: Username is not available."))
            }
            connectToDB()
            transaction {
                SchemaUtils.create(Users)
                Users.update({ Users.id eq incomingUser.id }) {
                    it[disabled] = incomingUser.disabled
                    it[group] = incomingUser.group
                    it[createdTime] = incomingUser.createdTime
                    it[username] = incomingUser.username
                    // NULLABLE entries
                    it[firstName] = incomingUser.firstName
                    it[lastName] = incomingUser.lastName
                    it[streetAddress] = incomingUser.streetAddress
                    it[postCode] = incomingUser.postCode
                    it[state] = incomingUser.state
                    it[country] = incomingUser.country
                    it[countryCode] = incomingUser.countryCode
                    it[language] = incomingUser.language
                    it[email] = incomingUser.email
                    it[areaCode] = incomingUser.areaCode
                    it[mobile] = incomingUser.mobile
                    it[secondaryGroup] = incomingUser.secondaryGroup
                    it[metadata] = incomingUser.metadata
                    it[password] = incomingUser.password
                }
            }
            call.respond(
                Status(
                    success = true,
                    errorMessage = "Successfully updated user \"${incomingUser.username}\""
                )
            )
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/updatePage") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val incomingPage = call.receive<ThisPage>()
            connectToDB()
            transaction {
                SchemaUtils.create(Pages)
                Pages.update({ Pages.id eq incomingPage.id }) {
                    it[disabled] = incomingPage.disabled
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
            call.respond(Status(success = true, errorMessage = "Successfully updated page \"${incomingPage.name}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }
    post("/api/updatePost") {
        try {
            if (!validateAdmin(call)) {
                call.response.status(HttpStatusCode.Forbidden)
                call.respond(Status(success = false, errorMessage = "Access denied."))
            }
            val thisSession = call.sessions.get<MySession>()
            val creatingUser = getThisUser(thisSession?.id)
            val incomingPost = call.receive<ThisPost>()
            if (creatingUser === null) {
                call.response.status(HttpStatusCode.Unauthorized)
                call.respond(
                    Status(
                        success = false,
                        errorMessage = "ERROR: User ${thisSession?.username} does not exit in database."
                    )
                )
            }
            connectToDB()
            transaction {
                SchemaUtils.create(Posts)
                Posts.update({ Posts.id eq incomingPost.id }) {
                    it[disabled] = incomingPost.disabled
                    it[name] = incomingPost.name
                    it[icon] = incomingPost.icon
                    it[pageID] = incomingPost.pageID
                    it[author] = creatingUser!!.username
                    it[createdTime] = incomingPost.createdTime
                    it[timeZone] = incomingPost.timeZone
                    it[contents] = incomingPost.contents
                    // NULLABLE entries below
                    it[parentID] = null
                    it[priorityBit] = null
                    it[group] = null
                    it[countryOfOrigin] = null
                    it[language] = null
                    it[executionScript] = null
                    it[metadata] = null
                    it[type] = null
                    it[likes] = null
                }
                ping(
                    Ping(
                        title = incomingPost.name,
                        rssurl = "https://",
                        blogurl = "https://$OUR_URL/post/${incomingPost.id}",
                        chk_blogs = "on",
                        chk_feedburner = "on",
                        chk_superfeedr = "on",
                        chk_tailrank = "on"
                    )
                )
            }
            call.respond(Status(success = true, errorMessage = "Successfully updated post \"${incomingPost.name}\""))
        } catch (e: Throwable) {
            call.respond(Status(success = false, errorMessage = e.toString()))
        }
    }

    get("/api/getAllPostsAndPages") {
        if (validateAdmin(call)) {
            call.respond(getAllPostsAndPages())
        } else {
            call.response.status(HttpStatusCode.Forbidden)
            call.respond(Status(success = false, errorMessage = "ERROR: Access denied."))
        }
    }
    get("/api/getFiles") {
        if (validateAdmin(call)) {
            call.respond(getFiles())
        } else {
            call.response.status(HttpStatusCode.Forbidden)
            call.respond(Status(success = false, errorMessage = "ERROR: Access denied."))
        }
    }
    get("/api/getPosts") {
        if (validateAdmin(call)) {
            call.respond(getPosts())
        } else {
            call.response.status(HttpStatusCode.Forbidden)
            call.respond(Status(success = false, errorMessage = "ERROR: Access denied."))
        }
    }

    get("/api/getPages") {
        if (validateAdmin(call)) {
            call.respond(getPages())
        } else {
            call.response.status(HttpStatusCode.Forbidden)
            call.respond(Status(success = false, errorMessage = "ERROR: Access denied."))
        }
    }

    get("/api/getUsers") {
        if (validateAdmin(call)) {
            call.respond(getUsers())
        } else {
            call.response.status(HttpStatusCode.Forbidden)
            call.respond(Status(success = false, errorMessage = "ERROR: Access denied."))
        }
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
                name = page[Pages.name],
                icon = page[Pages.icon],
                pageID = page[Pages.pageID],
                author = page[Pages.author],
                createdTime = page[Pages.createdTime],
                timeZone = page[Pages.timeZone],
                // NULLABLE entries below= page[Pages.],
                parentID = page[Pages.parentID],
                priorityBit = page[Pages.priorityBit],
                group = page[Pages.group],
                countryOfOrigin = page[Pages.countryOfOrigin],
                language = page[Pages.language],
                executionScript = page[Pages.executionScript],
                metadata = page[Pages.metadata],
                type = page[Pages.type],
                likes = page[Pages.likes]
            )
            returnedListOfPages.add(currentPage)
        }
    }
    return returnedListOfPages
}

fun getPost(queriedPost: Int?): ThisPost? {
    connectToDB()
    var returnedPost: ThisPost?
    returnedPost = null
    if (queriedPost == null) {
        return null
    }
    transaction {
        SchemaUtils.create(Posts)
        for (post in Posts.select { Posts.id eq queriedPost }) {
            val currentPost = ThisPost(
                id = post[Posts.id],
                disabled = post[Posts.disabled],
                name = post[Posts.name],
                icon = post[Posts.icon],
                pageID = post[Posts.pageID],
                author = post[Posts.author],
                createdTime = post[Posts.createdTime],
                timeZone = post[Posts.timeZone],
                contents = post[Posts.contents],
                // NULLABLE entries below= post[Posts.],
                parentID = post[Posts.parentID],
                priorityBit = post[Posts.priorityBit],
                group = post[Posts.group],
                countryOfOrigin = post[Posts.countryOfOrigin],
                language = post[Posts.language],
                executionScript = post[Posts.executionScript],
                metadata = post[Posts.metadata],
                type = post[Posts.type],
                likes = post[Posts.likes]
            )
            returnedPost = currentPost
        }
    }
    return returnedPost
}

fun getPosts(): MutableList<ThisPost> {
    connectToDB()
    val returnedListOfPosts = mutableListOf<ThisPost>()
    transaction {
        SchemaUtils.create(Posts)
        for (post in Posts.selectAll()) {
            val currentPost = ThisPost(
                id = post[Posts.id],
                disabled = post[Posts.disabled],
                name = post[Posts.name],
                icon = post[Posts.icon],
                pageID = post[Posts.pageID],
                author = post[Posts.author],
                createdTime = post[Posts.createdTime],
                timeZone = post[Posts.timeZone],
                contents = post[Posts.contents],
                // NULLABLE entries below= post[Posts.],
                parentID = post[Posts.parentID],
                priorityBit = post[Posts.priorityBit],
                group = post[Posts.group],
                countryOfOrigin = post[Posts.countryOfOrigin],
                language = post[Posts.language],
                executionScript = post[Posts.executionScript],
                metadata = post[Posts.metadata],
                type = post[Posts.type],
                likes = post[Posts.likes]
            )
            returnedListOfPosts.add(currentPost)
        }
    }
    return returnedListOfPosts
}

fun isUsernameAvailable(username: String?): Boolean {
    connectToDB()
    var available = true
    // check if our principal is null. It shouldn't be
    if (username === null) {
        return false
    }
    connectToDB()
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.username eq username }) {
            available = false
        }
    }
    return available
}

fun isUsernameChangeAvailable(username: String?, userID: Int?): Boolean {
    connectToDB()
    var available = true // check if our principal is null. It shouldn't be
    if (username === null || userID === null) {
        return false
    }
    connectToDB()
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.username.eq(username) and Users.id.neq(userID) }) {
            available = false
        }
    }
    return available
}

fun getThisUser(userID: Int?): ReadUserInfo? {
    var thisUser: ReadUserInfo?
    thisUser = null
    // check if our principal is null. It shouldn't be
    if (userID === null) {
        return null
    }
    connectToDB()
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.id eq userID }) {
            val currentUser = ReadUserInfo(
                // NOT NULLABLE entries
                id = user[Users.id],
                disabled = user[Users.disabled],
                group = user[Users.group],
                createdTime = user[Users.createdTime],
                username = user[Users.username],
                // NULLABLE entries
                firstName = user[Users.firstName],
                lastName = user[Users.lastName],
                streetAddress = user[Users.streetAddress],
                postCode = user[Users.postCode],
                state = user[Users.state],
                country = user[Users.country],
                countryCode = user[Users.countryCode],
                language = user[Users.language],
                email = user[Users.email],
                areaCode = user[Users.areaCode],
                mobile = user[Users.mobile],
                secondaryGroup = user[Users.secondaryGroup],
                metadata = user[Users.metadata]
            )
            thisUser = currentUser
        }
    }
    return thisUser
}

fun getFiles(): MutableList<ThisFile> {
    connectToDB()
    val returnedListOfFiles = mutableListOf<ThisFile>()
    transaction {
        SchemaUtils.create(Users)
        for (file in Files.selectAll()) {
            val currentFile = ThisFile(
                // NOT NULLABLE entries
                id = file[Files.id],
                name = file[Files.name],
                path = file[Files.path]
            )
            returnedListOfFiles.add(currentFile)
        }
    }
    return returnedListOfFiles
}

fun getUsers(): MutableList<ReadWriteThisUser> {
    connectToDB()
    val returnedListOfUsers = mutableListOf<ReadWriteThisUser>()
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.selectAll()) {
            val currentUser = ReadWriteThisUser(
                // NOT NULLABLE entries
                id = user[Users.id],
                disabled = user[Users.disabled],
                group = user[Users.group],
                // password = user[Users.password],
                // passwordSalt = user[Users.passwordSalt],
                password = user[Users.password],
                username = user[Users.username],
                // NULLABLE entries
                firstName = user[Users.firstName],
                lastName = user[Users.lastName],
                streetAddress = user[Users.streetAddress],
                postCode = user[Users.postCode],
                state = user[Users.state],
                country = user[Users.country],
                countryCode = user[Users.countryCode],
                language = user[Users.language],
                email = user[Users.email],
                areaCode = user[Users.areaCode],
                mobile = user[Users.mobile],
                secondaryGroup = user[Users.secondaryGroup],
                metadata = user[Users.metadata]
            )
            returnedListOfUsers.add(currentUser)
        }
    }
    return returnedListOfUsers
}

fun getAllPosts(): MutableList<ThisPage> {
    connectToDB()
    val returnedPages = mutableListOf<ThisPage>()
    transaction {
        SchemaUtils.create(Pages, Posts)
        for (page in Pages.select { Pages.disabled eq false }) {
            val currentPage = ThisPage(
                id = page[Pages.id],
                disabled = page[Pages.disabled],
                name = page[Pages.name],
                icon = page[Pages.icon],
                pageID = page[Pages.pageID],
                author = page[Pages.author],
                createdTime = page[Pages.createdTime],
                timeZone = page[Pages.timeZone],
                // NULLABLE entries below= page[Pages.],
                parentID = page[Pages.parentID],
                priorityBit = page[Pages.priorityBit],
                group = page[Pages.group],
                countryOfOrigin = page[Pages.countryOfOrigin],
                language = page[Pages.language],
                executionScript = page[Pages.executionScript],
                metadata = page[Pages.metadata],
                type = page[Pages.type],
                likes = page[Pages.likes]
            )
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}

fun getAllPostsAndPages(): MutableList<CompletePage> {
    connectToDB()
    val returnedPages = mutableListOf<CompletePage>()
    transaction {
        SchemaUtils.create(Pages, Posts)
        for (page in Pages.select { Pages.disabled eq false }) {
            val currentPage = CompletePage(
                id = page[Pages.id],
                disabled = page[Pages.disabled],
                name = page[Pages.name],
                icon = page[Pages.icon],
                pageID = page[Pages.pageID],
                author = page[Pages.author],
                createdTime = page[Pages.createdTime],
                timeZone = page[Pages.timeZone],
                // NULLABLE entries below= page[Pages.],
                parentID = page[Pages.parentID],
                priorityBit = page[Pages.priorityBit],
                group = page[Pages.group],
                countryOfOrigin = page[Pages.countryOfOrigin],
                language = page[Pages.language],
                executionScript = page[Pages.executionScript],
                metadata = page[Pages.metadata],
                type = page[Pages.type],
                likes = page[Pages.likes],
                posts = mutableListOf<ThisPost>()
            )
            var allPosts = Posts.select { Posts.disabled eq false }
            // reverse post order?
            // normal == 0, reversed == 1
            if (currentPage.type == 1) {
                allPosts = Posts.select { Posts.disabled.eq(false) }.orderBy(Posts.id to SortOrder.DESC)
            }
            for (post in allPosts) {
                if (post[Posts.pageID] == page[Pages.id]) {
                    val currentPost = ThisPost(
                        id = post[Posts.id],
                        disabled = post[Posts.disabled],
                        name = post[Posts.name],
                        icon = post[Posts.icon],
                        pageID = post[Posts.pageID],
                        author = post[Posts.author],
                        createdTime = post[Posts.createdTime],
                        timeZone = post[Posts.timeZone],
                        contents = post[Posts.contents],
                        // NULLABLE entries below= post[Posts.],
                        parentID = post[Posts.parentID],
                        priorityBit = post[Posts.priorityBit],
                        group = post[Posts.group],
                        countryOfOrigin = post[Posts.countryOfOrigin],
                        language = post[Posts.language],
                        executionScript = post[Posts.executionScript],
                        metadata = post[Posts.metadata],
                        type = post[Posts.type],
                        likes = post[Posts.likes]
                    )
                    currentPage.posts.add(currentPost)
                }
            }
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}

fun ping(pageToPing: Ping): Boolean {
    runBlocking {
        val client = HttpClient(Apache) {
        }
        val urlString =
            "https://pingomatic.com/ping/?title=${pageToPing.title}&blogurl=${pageToPing.blogurl}&rssurl=${pageToPing.rssurl}&chk_blogs=${pageToPing.chk_blogs}&chk_feedburner=${pageToPing.chk_feedburner}&chk_tailrank=${pageToPing.chk_tailrank}&chk_superfeedr=${pageToPing.chk_superfeedr}"
        println(urlString)
        val htmlContent = client.get<String>(urlString)
    }
    return true
}

/**
 * https://github.com/ktorio/ktor-samples/blob/master/app/youkube/src/Upload.kt
 * Utility boilerplate method that suspending,
 * copies a [this] [InputStream] into an [out] [OutputStream] in a separate thread.
 *
 * [bufferSize] and [yieldSize] allows to control how and when the suspending is performed.
 * The [dispatcher] allows to specify where will be this executed (for example a specific thread pool).
 */
suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}