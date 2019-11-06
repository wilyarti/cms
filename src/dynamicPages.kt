// Contains the routes for the Content Management System in the /home route.
package os3

import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.html.*
import org.joda.time.format.DateTimeFormat

internal fun Route.dynamicPages() {

    get("/") {
        val pageData = getAllPosts()
        call.respondRedirect("/home/${pageData[0].id}")
    }
    get("/post/{postID}") {
        val requestedPostID: String? = call.parameters["postID"]
        val post = getPost(requestedPostID?.toInt())
        if (post === null) {
            return@get call.respondRedirect("/error404.html", permanent = false)
        }
        val pageData = getAllPostsAndPages()
        val pageLookup = mutableMapOf<Int, CompletePage>()
        for ((index) in pageData.withIndex()) {
            pageLookup[pageData[index].id] = pageData[index]
        }
        call.respondHtml {
            head {
                unsafe {
                    raw(
                        """<!-- Global site tag (gtag.js) - Google Analytics -->
                        <script async src="https://www.googletagmanager.com/gtag/js?id=UA-151251135-1"></script>
                        <script>
                          window.dataLayer = window.dataLayer || [];
                          function gtag(){dataLayer.push(arguments);}
                          gtag('js', new Date());
                        
                          gtag('config', 'UA-151251135-1');
                        </script>
                        """.trimIndent()
                    )
                }
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
                for ((pageID, page) in pageLookup) {
                    var navtype = "nav-item"
                    val html = """<li class='${navtype}'>
                                <a href="/home/${page.id}" class="nav-link"><span><i data-feather="${page.icon}"></i> ${page.name}</span></a>
                                </li>
                            """
                    unsafe {
                        raw(
                            html
                        )
                    }
                }

                unsafe {
                    raw(
                        """</ul></div></nav>"""
                    )
                }

                var timeString = " "
                try {
                    val mIso8601local = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    //2019-10-07T01:27:20.840Z
                    val thisTime = mIso8601local.parseDateTime(post?.createdTime)
                    val dtfOut = DateTimeFormat.forPattern("MMMM dd, yyyy")
                    timeString = dtfOut.print(thisTime)
                } catch (e: Throwable) {
                    println(e)
                }
                unsafe {
                    raw(
                        """
                           <div class="post">
                                <h3 class="post-title">${post?.name}</h3>
                                <div class="post-meta">
                                    <span><i data-feather="folder"></i> ${pageLookup[post?.id]?.name}</span>
                                    <span><i data-feather="calendar"></i> ${timeString}</span>
                                    <span><i data-feather="user"></i> ${post?.author}</span>
                                </div>
                                <div class="post-contents">
                                    ${post?.contents}
                                </div>
                            </div>
                        """.trimIndent()
                    )
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
    get("/home/{page}/{pageRange?}") {
        val requestedPageNumber: String? = call.parameters["page"]
        val requestedPageRange: String? = call.parameters["pageRange"]
        var pageNumber = 1
        var postRange = 1
        if (requestedPageNumber !== null) {
            try {
                pageNumber = requestedPageNumber.toInt()
            } catch (error: Throwable) {
                println(error)
            }
        }
        if (requestedPageRange !== null) {
            try {
                postRange = requestedPageRange.toInt()
            } catch (error: Throwable) {
                println(error)
            }
        }
        val pageData = getAllPostsAndPages()
        val pageLookup = mutableMapOf<Int, CompletePage>();
        for ((index) in pageData.withIndex()) {
            pageLookup[pageData[index].id] = pageData[index]
        }
        if (pageLookup[requestedPageNumber?.toInt()] === null) {
            return@get call.respondRedirect("/error404.html", permanent = false)
        }
        println("pagenumber $pageNumber pageRange $postRange")
        call.respondHtml {
            head {
                unsafe {
                    raw(
                        """<!-- Global site tag (gtag.js) - Google Analytics -->
                        <script async src="https://www.googletagmanager.com/gtag/js?id=UA-151251135-1"></script>
                        <script>
                          window.dataLayer = window.dataLayer || [];
                          function gtag(){dataLayer.push(arguments);}
                          gtag('js', new Date());
                        
                          gtag('config', 'UA-151251135-1');
                        </script>
                        """.trimIndent()
                    )
                }
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
                for ((pageID, page) in pageLookup) {
                    var navtype = "nav-item"
                    if (pageID == pageNumber) {
                        navtype = "nav-item active"
                    }
                    val html = """<li class='${navtype}'>
                                <a href="/home/${page.id}" class="nav-link"><span><i data-feather="${page.icon}"></i> ${page.name}</span></a>
                                </li>
                            """
                    unsafe {
                        raw(
                            html
                        )
                    }
                }

                unsafe {
                    raw(
                        """</ul></div></nav>"""
                    )
                }
                val thisPage = pageLookup[pageNumber]!!.posts.drop((postRange - 1) * MAXPOSTSPERPAGE)
                for ((postCount, post) in thisPage.withIndex()) {
                    if (postCount < MAXPOSTSPERPAGE) {
                        var timeString = " "
                        try {
                            val mIso8601local = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                            //2019-10-07T01:27:20.840Z
                            val thisTime = mIso8601local.parseDateTime(post.createdTime)
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
                                    <span><i data-feather="folder"></i> ${pageLookup[pageNumber]?.name}</span>
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
                }
                nav {
                    attributes["aria-label"] = "Page navigation"
                    ul {
                        classes = setOf("pagination")
                        var postIndex = 1
                        val posts = pageLookup[pageNumber]?.posts
                        for (post in 1..posts!!.size step MAXPOSTSPERPAGE) {
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