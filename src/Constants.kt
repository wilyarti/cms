// Contains all the data classes, SQL tables and JSON data classes.
import io.ktor.auth.Principal
import org.jetbrains.exposed.sql.Table


data class MySession(val id: Int, val username: String, val group: String) : Principal

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
object Users : Table() {
    val id = Users.integer("id").autoIncrement().primaryKey()
    val name = Users.varchar("name", length = 150)
    val group = Users.varchar("group", length = 150)
    val secondaryGroup = Users.varchar("secondaryGroup", length = 150)
    val password = Users.varchar("password", length = 150)
    val metadata = Users.text("metadata")
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
data class jsonReq(
    val url: String
)

data class Success(
    val success: Boolean
)
data class ThisUser(
    val id: Int,
    val name: String,
    val group: String,
    val secondaryGroup: String,
    val password: String,
    val metadata: String
)
data class UserID(
    val id: Int,
    val name: String
)

data class ThisSet(
    val id: Int,
    val uuid: Int,
    val weight: Int,
    val repetitions: Int,
    val createdTime: String
)

object FormFields {
    const val USERNAME = "username"
    const val PASSWORD = "password"
}

object AuthName {
    const val SESSION = "session"
    const val FORM = "form"
}

object CommonRoutes {
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val PROFILE = "/profile"
    const val ADMIN = "/static/admin.html"
}

object Cookies {
    const val AUTH_COOKIE = "auth"
}

