import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.html.*
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import kotlinx.html.*
import net.opens3.db_password
import net.opens3.db_username
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Pages : Table() {
    val id = Pages.integer("id").autoIncrement().primaryKey() // main id
    val disabled = Pages.bool("disabled")
    val parentID = Pages.integer("parentID") // allow tree structure (stipulate parent)
    val priorityBit = Pages.integer("priorityBit") // to re-order pages and sticky bit
    val name = Pages.varchar("name", length = 150) // page name
    val icon = Pages.varchar("icon", length = 150)  // feather icon
    val pageID = Pages.integer("pageID")
    val author = Pages.varchar("author", length = 150)
    val group = Pages.varchar("group", length = 150)
    val createdTime = Pages.varchar("createdTime", length = 150) // date, time and timezone
    val countryOfOrigin = Pages.varchar("countryOfOrigin", length = 8)
    val language = Pages.varchar("language", length = 150)
    val executionScript = Pages.text("executionScript")
    val metadata = Pages.text("metadata")
    val type = Pages.integer("type") // 00  = page
    val likes = Pages.integer("likes")
}

object Posts : Table() {
    val id = Posts.integer("id").autoIncrement().primaryKey() // main id
    val disabled = Posts.bool("disabled")
    val parentID = Posts.integer("parentID") // allow tree structure (stipulate parent)
    val priorityBit = Posts.integer("priorityBit") // to re-order pages and sticky bit
    val name = Posts.varchar("name", length = 150) // page name
    val icon = Posts.varchar("icon", length = 150)  // feather icon
    val pageID = Posts.integer("pageID")
    val author = Posts.varchar("author", length = 150)
    val group = Posts.varchar("group", length = 150)
    val createdTime = Posts.varchar("postedTime", length = 150) // date, time and timezone
    val countryOfOrigin = Posts.varchar("countryOfOrigin", length = 8)
    val language = Posts.varchar("language", length = 150)
    val executionScript = Posts.text("executionScript")
    val contents = Posts.text("contents")
    val metadata = Posts.text("metadata")
    val type = Posts.integer("type") // 00  = page
    val likes = Posts.integer("likes")
}

data class thisPost(
    val id: Int,
    val disabled: Boolean,
    val parentID: Int,
    val priorityBit: Int,
    val name: String,
    val icon: String,
    val pageID: Int,
    val author: String,
    val group: String,
    val createdTime: String,
    val countryOfOrigin: String,
    val language: String,
    val executionScript: String,
    val contents: String,
    val metadata: String,
    val type: Int,
    val likes: Int
)

data class thisPage(
    val id: Int,
    val disabled: Boolean,
    val parentID: Int,
    val priorityBit: Int,
    val name: String,
    val icon: String,
    val pageID: Int,
    val author: String,
    val group: String,
    val createdTime: String,
    val countryOfOrigin: String,
    val language: String,
    val executionScript: String,
    val metadata: String,
    val type: Int,
    val likes: Int
)

data class completePage(
    val id: Int,
    val disabled: Boolean,
    val parentID: Int,
    val priorityBit: Int,
    val name: String,
    val icon: String,
    val pageID: Int,
    val author: String,
    val group: String,
    val createdTime: String,
    val countryOfOrigin: String,
    val language: String,
    val executionScript: String,
    val metadata: String,
    val type: Int,
    val likes: Int,
    val posts: MutableList<thisPost>
)

data class pageList(
    val id: Int,
    val name: String,
    val icon: String
)

data class Status (
    val success: Boolean,
    val errorMessage: String
)
fun connectToDB(): Unit {
    Database.connect(
        "jdbc:mysql://127.0.0.1:3306/webappsadmin",
        "com.mysql.jdbc.Driver",
        user = db_username,
        password = db_password
    )
}

fun Route.dynamicPagesAPI() {
    post ("/api/addPost") {
        try {
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
            call.respond(Status(success=true, errorMessage = ""))
        } catch(e: Throwable) {
            call.respond(Status(success=false, errorMessage= e.toString()))
        }
    }
    post ("/api/addPage") {
        try {
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
            call.respond(Status(success=true, errorMessage = ""))
        } catch(e: Throwable) {
            call.respond(Status(success=false, errorMessage= e.toString()))
        }
    }
    get("{page}") {
        val queryParameters: Parameters = call.request.queryParameters
        val requestedPageNumber: String? = call.request.queryParameters["page"]
        var pageNumber = 1
        if (requestedPageNumber !== null) {
            pageNumber = requestedPageNumber!!.toInt()
        }
        var pageData = getAllPostsAndPages()
        println("pagenumber ${pageNumber}")
        call.respondHtml {
            head {
                link(
                    rel = "stylesheet",
                    href = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css",
                    type = "text/css"
                )
                link(
                    rel = "stylesheet",
                    href = "/static/blog.css",
                    type = "text/css"
                )
                script(src = "https://unpkg.com/feather-icons") {}
            }
            body {
                div(classes = "container-fluid") {
                    nav {
                        classes = setOf("navbar", "navbar-expand-lg", "navbar-light", "bg-light")
                        a(href = "/home/") {
                            classes = setOf("navbar-brand")
                            img(src = "/static/favicon.ico") {
                                attributes["width"] = "30"
                                attributes["height"] = "30"
                                attributes["alt"] = ""
                            }
                        }
                        div {
                            attributes["id"] = "navbarNav"
                            classes = setOf("collapse", "navbar-collapse")
                            ul {
                                classes = setOf("navbar-nav")
                                for (page in pageData) {
                                    li {
                                        if (page.id == pageNumber) {
                                            classes = setOf("nav-item active")
                                        } else {
                                            classes = setOf("nav-item")
                                        }
                                        a(href = "/&?page=${page.id}") {
                                            classes = setOf("nav-link")
                                            span {
                                                unsafe {
                                                    raw("""<i data-feather="${page.icon}"></i>""")
                                                }
                                                +" "
                                                +page.name
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    div {
                        for (post in pageData[pageNumber - 1].posts) {
                            div {
                                classes = setOf("blog-post")
                                div {
                                    h4 {
                                        +post.name
                                    }
                                }
                                div {
                                    classes = setOf("card-body")
                                    unsafe {
                                        +post.contents
                                    }
                                }
                            }
                        }
                    }
                }
            }
            unsafe {
                raw(
                    """<script>feather.replace();</script>"""
                )
            }
        }
    }
    get("/api/getAllPostsAndPages") {
        call.respond(getAllPostsAndPages())
    }
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
