package os3

import AuthName
import CommonRoutes
import FormFields
import MySession
import adminAPI
import com.mysql.jdbc.authentication.MysqlClearPasswordPlugin
import dynamicPages
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
     dynamicPages()
}

internal fun Routing.loginRoute() {
    route(CommonRoutes.LOGIN) {
        get {
            call.respondHtml {
                body {
                    // Create a form that POSTs back to this same route
                    form(method = FormMethod.post) {
                        // handle any possible errors
                        val queryParams = call.request.queryParameters
                        val errorMsg = when {
                            "invalid" in queryParams -> "Sorry, incorrect username or password."
                            "no" in queryParams -> "Sorry, you need to be logged in to do that."
                            else -> null
                        }
                        if (errorMsg != null) {
                            div {
                                style = "color:red;"
                                +errorMsg
                            }
                        }
                        textInput(name = FormFields.USERNAME) {
                            placeholder = "user)"
                        }
                        br
                        passwordInput(name = FormFields.PASSWORD) {
                            placeholder = "password"
                        }
                        br
                        submitInput {
                            value = "Log in"
                        }
                    }
                }
            }
        }

        authenticate(AuthName.FORM) {
            post {
                // Get the principle (which we know we'll have)
                val principal = call.principal<MySession>()!!
                // Set the cookie
                call.sessions.set(principal)
                call.respondRedirect(CommonRoutes.PROFILE)
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
                body {
                    div {
                        +"Hello, $principal!"
                    }
                    div {
                        a(href = CommonRoutes.LOGOUT) {
                            +"Log out"
                        }
                    }
                }
            }
        }
    }
}
