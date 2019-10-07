// Contains the routes for the Content Management System in the /home route.
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import kotlinx.html.*
import io.ktor.routing.Route
import io.ktor.routing.get
import io.netty.handler.codec.DateFormatter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

internal fun Route.dynamicPages() {

    get("/") {
        call.respondRedirect("/home/1")
    }
    get("/home/{page}/{pageRange?}") {
        val requestedPageNumber: String? = call.parameters["page"]
        val requestedPageRange: String? = call.parameters["pageRange"]
        var pageNumber = 1
        var postRange = 1
        if (requestedPageNumber !== null) {
            try {
                pageNumber = requestedPageNumber!!.toInt()
            } catch (error: Throwable) {
                println(error)
            }
        }
        if (requestedPageRange !== null) {
            try {
                postRange = requestedPageRange!!.toInt()
            } catch (error: Throwable) {
                println(error)
            }
        }
        var pageData = getAllPostsAndPages()
        println("pagenumber ${pageNumber} pageRange ${postRange}")
        call.respondHtml {
            head {
                link(
                    rel = "stylesheet",
                    href = "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css",
                    type = "text/css"
                )
                unsafe {
                    raw("""<meta name="viewport" content="width=device-width, initial-scale=1">""")
                }
                /*
                link(
                    rel = "stylesheet",
                    href = "/static/blog.css",
                    type = "text/css"
                )*/
                script(src = "https://unpkg.com/feather-icons") {}
                unsafe {
                    raw(
                        """  
                        <style>
                            .post-title {
                                font-family: -apple-system,BlinkMacSystemFont,"Segoe UI","Roboto","Oxygen-Sans","Ubuntu","Cantarell","Helvetica Neue",sans-serif;
                            }
                            .post-meta {
                                font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "Roboto", "Oxygen-Sans", "Ubuntu", "Cantarell", "Helvetica Neue", sans-serif;
                                font-size: 13px;
                                font-weight: 400;
                                font-style: normal;
                                margin: .4375em 0 1.0em;
                                color: #b3b3b1;
                            }
                    
                            .post {
                                margin: 20px 15px 5px 15px;
                                font-size: 19px;
                            }
                    
                            .post-contents {
                                font-family: Georgia,"Times New Roman",serif;
                            }
                        </style>"""
                    )
                }
            }
            body {
                unsafe {
                    raw(
                        """
                   <container-fluid>
                    <nav class="navbar navbar-expand navbar-dark bg-dark">
                        <a href="/home/1" class="navbar-brand"><img src="/static/favicon.png" alt="" width="30" height="30"></a>
                        <div id="navbarNav" class="collapse navbar-collapse">
                        <ul class="navbar-nav">
                        """.trimIndent()
                    )
                }
                for (page in pageData) {
                    var navtype = "nav-item"
                    if (page.id == pageNumber) {
                        navtype = "nav-item active"
                    }
                    var html = """<li class='${navtype}'>
                                <a href="/home/${page.id}" class="nav-link"><span><i data-feather="${page.icon}"></i> ${page.name}</span></a>
                                </li>
                            """
                    unsafe {
                        raw(
                            """${html}"""
                        )
                    }
                }

                unsafe {
                    raw(
                        """</ul></div></nav>"""
                    )
                }
                var postCount = 0
                for (post in pageData[pageNumber - 1].posts.drop((postRange -1) * MAXPOSTSPERPAGE)) {
                    if (postCount < MAXPOSTSPERPAGE) {

                        var timeString = " "
                        try {
                            val m_ISO8601Local = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            //2019-10-07T01:27:20.840Z
                            val thisTime = m_ISO8601Local.parseDateTime(post.createdTime)
                            val dtfOut = DateTimeFormat.forPattern("MMMM dd, yyyy")
                            timeString = dtfOut.print(thisTime)
                        } catch (e: Throwable) {
                            println(e)
                        }
                        unsafe {
                            raw(
                                """
                           <div class="post">
                                <h3 class="post-title">${post.name}</h3>
                                <div class="post-meta">
                                    <span><i data-feather="folder"></i> ${pageData[pageNumber - 1].name}</span>
                                    <span><i data-feather="calendar"></i> ${timeString}</span>
                                    <span><i data-feather="user"></i> ${post.author}</span>
                                </div>
                                <div class="post-contents">
                                    ${post.contents}
                                </div>
                            </div>
                        """.trimIndent()
                            )
                        }
                    }
                    postCount++
                }
                nav {
                    attributes["aria-label"] = "Page navigation"
                    ul {
                        classes = setOf("pagination")
                        var count = 0
                        var postIndex = 1
                        val posts = pageData[pageNumber - 1].posts
                        for (post in 1..posts.size step MAXPOSTSPERPAGE) {
                            li {
                                classes = setOf("page-item")
                                    a {
                                        classes = setOf("page-link")
                                        if (postRange != postIndex) {
                                            href = "/home/$pageNumber/$postIndex"
                                        }
                                        +"$postIndex"
                                    }

                            }
                            postIndex++
                        }
                    }
                }
                unsafe {
                    raw(
                        """</container-fluid>"""
                    )
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