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
    val id = pages.integer("id").autoIncrement().primaryKey() // Column<Int>
    val name = pages.varchar("name", length = 150) // Column<String>
    val icon = pages.varchar("icon", length = 150) // Column<String>
}

object posts : Table() {
    val id = posts.integer("id").autoIncrement().primaryKey() // Column<Int>
    val pageID = posts.integer("pageID").nullable()
    val name = posts.varchar("name", length = 150) // Column<String>
    val contents = posts.text("contents") // Column<String>
}

data class thisPost(
    val id: Int,
    val pageID: Int?,
    val name: String,
    val contents: String
)

data class completePage(
    val id: Int,
    val name: String,
    val icon: String,
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
        var pageData = getPages()
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
    get("/api/getPages") {
        call.respond(getPages())
    }
    get("/api/getPosts") {
        call.respond(getPosts())
    }
}

fun getPages(): MutableList<completePage> {
    connectToDB()
    var returnedPages = mutableListOf<completePage>()
    transaction {
        SchemaUtils.create(pages, posts)
        val allPosts = posts.selectAll()
        for (page in pages.selectAll()) {
            val currentPage = completePage(
                id = page[pages.id],
                name = page[pages.name],
                icon = page[pages.icon],
                posts = mutableListOf<thisPost>()
            )
            for (p in allPosts) {
                if (p[posts.pageID] == page[pages.id]) {
                    var currentPost = thisPost(
                        id = p[posts.id],
                        pageID = p[posts.pageID],
                        name = p[posts.name],
                        contents = p[posts.contents].toString()
                    )
                    currentPage.posts.add(currentPost)
                }
            }
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}

fun getPosts(): MutableList<thisPost> {
    connectToDB()
    var returnedPosts = mutableListOf<thisPost>()
    transaction {
        SchemaUtils.create(posts)
        val allPosts = posts.selectAll()
        for (p in allPosts) {
            val currentPost = thisPost(
                id = p[posts.id],
                pageID = p[posts.pageID],
                name = p[posts.name],
                contents = p[posts.contents].toString()
            )
            returnedPosts.add(currentPost)
        }
    }
    return returnedPosts
}

fun getPageList(): MutableList<pageList> {
    connectToDB()
    var returnedPages = mutableListOf<pageList>()
    transaction {
        SchemaUtils.create(pages, posts)
        val allPosts = posts.selectAll()
        for (page in pages.selectAll()) {
            val currentPage = pageList(
                id = page[pages.id],
                name = page[pages.name],
                icon = page[pages.icon]
            )
            returnedPages.add(currentPage)
        }
    }
    return returnedPages
}
