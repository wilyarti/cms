import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.html.*
import io.ktor.http.Parameters
import io.ktor.http.parametersOf
import io.ktor.response.respond
import kotlinx.css.div
import kotlinx.css.em
import kotlinx.css.p
import kotlinx.css.script
import kotlinx.html.*
import net.opens3.db_password
import net.opens3.db_username
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import os3.respondCss

object pages : Table() {
    val id = pages.integer("id").autoIncrement().primaryKey() // main id
    val disabled = pages.bool("disabled")
    val parentID = pages.integer("pid") // allow tree structure (stipulate parent)
    val priorityBit = pages.integer("priorityBit") // to re-order pages and sticky bit
    val name = pages.varchar("name", length = 150) // page name
    val icon = pages.varchar("icon", length = 150)  // feather icon
    val pageID = pages.integer("pageID")
    val author = pages.varchar("author", length = 150)
    val group = pages.varchar("group", length = 150)
    val createdTime = posts.varchar("createdTime", length = 150) // date, time and timezone
    val countryOfOrigin = pages.varchar("countryOfOrigin", length = 8)
    val language = pages.varchar("language", length = 150)
    val executionScript = pages.text("executionScript")
    val contents = pages.text("contents")
    val metadata = pages.text("metadata")
}

object posts : Table() {
    val id = posts.integer("id").autoIncrement().primaryKey() // main id
    val disabled = posts.bool("disabled")
    val parentID = posts.integer("pid") // allow tree structure (stipulate parent)
    val priorityBit = posts.integer("priorityBit") // to re-order pages and sticky bit
    val name = posts.varchar("name", length = 150) // page name
    val icon = posts.varchar("icon", length = 150)  // feather icon
    val pageID = posts.integer("pageID")
    val author = posts.varchar("author", length = 150)
    val group = posts.varchar("group", length = 150)
    val createdTime = posts.varchar("createdTime", length = 150) // date, time and timezone
    val countryOfOrigin = posts.varchar("countryOfOrigin", length = 8)
    val language = posts.varchar("language", length = 150)
    val executionScript = posts.text("executionScript")
    val contents = posts.text("contents")
    val metadata = posts.text("metadata")
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
    val metadata: String
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
    val contents: String,
    val metadata: String,
    val posts: MutableList<thisPost>
)

data class pageList(
    val id: Int,
    val name: String,
    val icon: String
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
        SchemaUtils.create(pages, posts)
        val allPosts = posts.selectAll()
        for (page in pages.selectAll()) {
            val currentPage = completePage(
                id = page[pages.id],
                disabled = page[pages.disabled],
                parentID = page[pages.parentID],
                priorityBit = page[pages.priorityBit],
                name = page[pages.name],
                icon = page[pages.icon],
                pageID = page[pages.pageID],
                author = page[pages.author],
                group = page[pages.group],
                createdTime = page[pages.createdTime].toString(),
                countryOfOrigin = page[pages.countryOfOrigin],
                language = page[pages.language],
                executionScript = page[pages.executionScript].toString(),
                contents = page[pages.contents].toString(),
                metadata = page[pages.metadata].toString(),
                posts = mutableListOf<thisPost>()
            )
            for (p in allPosts) {
                if (p[posts.pageID] == page[pages.id]) {
                    var currentPost = thisPost(
                        id = p[posts.id],
                        disabled = p[posts.disabled],
                        parentID = p[posts.parentID],
                        priorityBit = p[posts.priorityBit],
                        name = p[posts.name],
                        icon = p[posts.icon],
                        pageID = p[posts.pageID],
                        author = p[posts.author],
                        group = p[posts.group],
                        createdTime = p[posts.createdTime].toString(),
                        countryOfOrigin = p[posts.countryOfOrigin],
                        language = p[posts.language],
                        executionScript = p[posts.executionScript].toString(),
                        contents = p[posts.contents].toString(),
                        metadata = p[posts.metadata].toString()
                    )
                    currentPage.posts.add(currentPost)
                }
            }
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}
