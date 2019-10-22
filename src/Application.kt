package os3


import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.files
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.http.content.staticRootFolder
import io.ktor.request.path
import io.ktor.response.respondRedirect
import io.ktor.routing.routing
import io.ktor.sessions.SessionStorageMemory
import io.ktor.sessions.Sessions
import io.ktor.sessions.cookie
import net.opens3.db_filepath
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.event.Level
import java.io.File
import java.lang.Error

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf

fun Application.module() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson {
        }
    }
    install(Sessions) {
        // We need to save auth data in a cookie, which we configure here.
        // https://ktor.io/servers/features/sessions.html
        configureAuthCookie()
    }

    install(Authentication) {
        configureSessionAuth()
        configureFormAuth()
    }
    install(StatusPages) {
        statusFile(HttpStatusCode.NotFound, HttpStatusCode.Unauthorized, filePattern = "error#.html")
    }

    val root = File("sitefiles").takeIf { it.exists() }
        ?: File("files").takeIf { it.exists() }
        ?: error("Can't locate files folder")

    routing {
        // setup routes
        homepageRoute()
        loginRoute()
        logoutRoute()
        profileRoute()
        // our static files
        static("/") {
            //defaultResource("main.html", "static")
            resources("static")
        }
        static("/files/") {
            files(root)
        }
    }
}

private fun Sessions.Configuration.configureAuthCookie() {
    cookie<MySession>(
        // We set a cookie by this name upon login.
        Cookies.AUTH_COOKIE,
        // Stores session contents in memory...good for development only.
        storage = SessionStorageMemory()
    ) {
        cookie.path = "/"
        // CSRF protection in modern browsers. Make sure your important side-effect-y operations, like ordering,
        // uploads, and changing settings, use "unsafe" HTTP verbs like POST and PUT, not GET or HEAD.
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#SameSite_cookies
        cookie.extensions["SameSite"] = "lax"
    }
}

/**
 * Form-based authentication is a interceptor that reads attributes off a POST request in order to validate the user.
 * Only needed by whatever your login form is POSTing to.
 *
 * If validation fails, the user will be challenged, e.g. sent to a login page to authenticate.
 */
private fun Authentication.Configuration.configureFormAuth() {
    form(AuthName.FORM) {
        userParamName = FormFields.USERNAME
        passwordParamName = FormFields.PASSWORD
        challenge {
            // I don't think form auth supports multiple errors, but we're conservatively assuming there will be at
            // most one error, which we handle here. Worst case, we just send the user to login with no context.
            val errors: Map<Any, AuthenticationFailedCause> = call.authentication.errors
            when (errors.values.singleOrNull()) {
                AuthenticationFailedCause.InvalidCredentials ->
                    call.respondRedirect("${CommonRoutes.LOGIN}?invalid")

                AuthenticationFailedCause.NoCredentials ->
                    call.respondRedirect("${CommonRoutes.LOGIN}?no")

                else ->
                    call.respondRedirect(CommonRoutes.LOGIN)
            }
        }
        validate { cred: UserPasswordCredential ->
            val userCredentials = verifyUserCredentials(cred.name)
            println("Username: ${cred.name} Password: ${cred.password} : ${userCredentials?.password}")
            var userValidated: Boolean = false
            try {
                userValidated = BCrypt.checkpw(cred.password, userCredentials?.password)
            } catch (e: Throwable) {
                println(e)
            }
            if (userCredentials !== null && userValidated) {
                println("Session validated....")
                MySession(id = userCredentials.id, username = userCredentials.username, group = userCredentials.group)
            } else {
                println("Invalid login....")
                null
            }
        }
    }
}

/**
 * Let the user authenticate by their session (a cookie).
 *
 * This is related to the configureAuthCookie method by virtue of the common `PrincipalType` object.
 */
private fun Authentication.Configuration.configureSessionAuth() {
    session<MySession>(AuthName.SESSION) {
        challenge {
            // What to do if the user isn't authenticated
            call.respondRedirect("${CommonRoutes.LOGIN}?no")
        }
        validate { session: MySession ->
            // If you need to do additional validation on session data, you can do so here.
            session
        }
    }
}
