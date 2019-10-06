import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.Parameters
import io.ktor.response.respondRedirect
import kotlinx.html.*
import io.ktor.routing.Route
import io.ktor.routing.get

internal fun Route.dynamicPages() {

    get("/") {
        call.respondRedirect("/home/1")
    }
    get("/home/{page}") {
        val requestedPageNumber: String? = call.parameters["page"]
        var pageNumber = 1
        if (requestedPageNumber !== null) {
            try {
                pageNumber = requestedPageNumber!!.toInt()
            } catch (error: Throwable) {
                println(error)
            }
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
                unsafe {
                    raw ("""<meta name="viewport" content="width=device-width, initial-scale=1">""")
                }
                /*
                link(
                    rel = "stylesheet",
                    href = "/static/blog.css",
                    type = "text/css"
                )*/
                script(src = "https://unpkg.com/feather-icons") {}
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
                        """
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
                for (post in pageData[pageNumber - 1].posts) {
                    unsafe {
                        raw(""" <div class="card" style="margin-bottom: 15px; margin-top: 10px; margin-left: 5px; margin-right: 5px">
                        <div class="card-header h6"><h3>${post.name}</h3></div>
                        <div class="card-body"><div row><div class="col">${post.contents}</div></div></div>
                    </div>""")
                    }
                }
                unsafe {
                    raw(
                        """</div></div></container-fluid>"""
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