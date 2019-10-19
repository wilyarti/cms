// Contains the boiler plate for the majority of the routes.
// This is needed for authentication and sessions, which are stored on the server.
package os3

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.auth.principal
import io.ktor.html.respondHtml
import io.ktor.response.respondRedirect
import io.ktor.routing.*
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kotlinx.html.*

internal fun Routing.homepageRoute() {
    /*authenticate(AuthName.SESSION, optional = true) {
        get("/") {
            // Redirect user to login if they're not already logged in.
            // Otherwise redirect them to a page that requires auth.
            if (call.principal<MySession>() == null) {
                call.respondRedirect(CommonRoutes.LOGIN)
            } else {
                val thisSession = call.principal<MySession>()
                if (thisSession?.group == "wheel") {
                    call.respondRedirect(CommonRoutes.ADMIN)
                } else {
                    call.respondRedirect(CommonRoutes.PROFILE)

                }
            }
        }
    }
    */
    // siteMap
    siteMap()
    // DSL pages
    dynamicPages()
    // for the /kbcomp.html web app
    kettlebellCompetition()
    // for the /admin.html web app
    adminAPI()
    // various API routes
    API()
    // legacy routes for /rtstats.html
    rtStatsGraphingAPI()

}

internal fun Routing.loginRoute() {
    route(CommonRoutes.LOGIN) {
        get {
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
                }
                body {
                    // Create a form that POSTs back to this same route
                    unsafe {
                        raw(
                            """
                            <nav class="navbar navbar-expand navbar-dark bg-dark">
                                <a href="#home" class="navbar-brand"><img alt="" src="/static/favicon.png" class="d-inline-block align-top" width="30" height="30"> Login</a>
                            </nav><container-fluid>
                        """.trimIndent()
                        )
                    }
                    form(method = FormMethod.post) {
                        // handle any possible errors
                        val queryParams = call.request.queryParameters
                        val errorMsg = when {
                            "invalid" in queryParams -> "Sorry, incorrect username or password."
                            "no" in queryParams -> "Sorry, you need to be logged in to do that."
                            else -> null
                        }
                        h3 {
                            +"Please login."
                        }
                        if (errorMsg != null) {
                            div {
                                style = "color:red;"
                                +errorMsg
                            }
                        }
                        textInput(name = FormFields.USERNAME) {
                            placeholder = "Username:"
                        }
                        br
                        passwordInput(name = FormFields.PASSWORD) {
                            placeholder = "Password:"
                        }
                        br
                        submitInput {
                            value = "Log in"
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

        authenticate(AuthName.FORM) {
            post {
                // Get the principle (which we know we'll have)
                val principal = call.principal<MySession>()!!
                // Set the cookie
                call.sessions.set(principal)
                if (principal.group == "wheel") {
                    call.respondRedirect(CommonRoutes.ADMIN)
                } else {
                    call.respondRedirect(CommonRoutes.PROFILE)
                }
            }
        }
    }
}

internal fun Routing.logoutRoute() {
    get(CommonRoutes.LOGOUT) {
        // Purge ExamplePrinciple from cookie data
        call.sessions.clear<MySession>()
        call.respondRedirect(CommonRoutes.LOGIN)
    }
}

internal fun Route.profileRoute() {
    authenticate(AuthName.SESSION) {
        adminAPI()
        get(CommonRoutes.PROFILE) {
            val principal = call.principal<MySession>()!!
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
                }
                body {
                    unsafe {
                        raw("""<container-fluid>""")
                    }
                    div {
                        +"Hello, ${principal.username}!"
                    }
                    div {
                        a(href = "/kbcomp.html") {
                            +"Kettlebell Competition"
                        }
                    }
                    div {
                        a(href = CommonRoutes.LOGOUT) {
                            +"Log out"
                        }
                    }
                    unsafe {
                        raw("""</container-fluid>""")
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
}
