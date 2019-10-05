import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.Parameters
import io.ktor.response.respondRedirect
import kotlinx.html.*
import io.ktor.routing.Route
import io.ktor.routing.get

internal fun Route.dynamicPages() {

    get("/") {
        call.respondRedirect("/home/page=1")
    }
    get("/home/{page}") {
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
                )/*
                link(
                    rel = "stylesheet",
                    href = "/static/blog.css",
                    type = "text/css"
                )*/
                script(src = "https://unpkg.com/feather-icons") {}
            }
            body {
                div(classes = "container-fluid") {
                    nav {
                        classes = setOf("navbar", "navbar-expand-lg", "navbar-light", "bg-light")
                        a(href = "/home/") {
                            classes = setOf("navbar-brand")
                            img(src = "/static/favicon.png") {
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
                                        a(href = "/home/page=${page.id}") {
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
}