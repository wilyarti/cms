// Contains all the data classes, SQL tables and JSON data classes.
package os3

import io.ktor.auth.Principal
import org.jetbrains.exposed.sql.Table

const val MAXPOSTSPERPAGE  =5
data class MySession(val id: Int, val username: String, val group: String) : Principal

object Pages : Table() {
    val id = integer("id").autoIncrement().primaryKey() // main id
    val disabled = bool("disabled")
    val parentID = integer("parentID") // allow tree structure (stipulate parent)
    val priorityBit = integer("priorityBit") // to re-order pages and sticky bit
    val name = varchar("name", length = 150) // page name
    val icon = varchar("icon", length = 150)  // feather icon
    val pageID = integer("pageID")
    val author = varchar("author", length = 150)
    val group = varchar("group", length = 150)
    val createdTime = varchar("createdTime", length = 150) // date, time and timezone
    val countryOfOrigin = varchar("countryOfOrigin", length = 8)
    val language = varchar("language", length = 150)
    val executionScript = text("executionScript")
    val metadata = text("metadata")
    val type = integer("type") // 00  = page
    val likes = integer("likes")
}
object Users : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 150)
    val group = varchar("group", length = 150)
    val secondaryGroup = varchar("secondaryGroup", length = 150)
    val password = varchar("password", length = 150)
    val metadata = text("metadata")
}
object Posts : Table() {
    val id = integer("id").autoIncrement().primaryKey() // main id
    val disabled = bool("disabled")
    val parentID = integer("parentID") // allow tree structure (stipulate parent)
    val priorityBit = integer("priorityBit") // to re-order pages and sticky bit
    val name = varchar("name", length = 150) // page name
    val icon = varchar("icon", length = 150)  // feather icon
    val pageID = integer("pageID")
    val author = varchar("author", length = 150)
    val group = varchar("group", length = 150)
    val createdTime = varchar("postedTime", length = 150) // date, time and timezone
    val countryOfOrigin = varchar("countryOfOrigin", length = 8)
    val language = varchar("language", length = 150)
    val executionScript = text("executionScript")
    val contents = text("contents")
    val metadata = text("metadata")
    val type = integer("type") // 00  = page
    val likes = integer("likes")
}

data class ThisPost(
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

data class ThisPage(
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

data class CompletePage(
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
    val posts: MutableList<ThisPost>
)

data class PageList(
    val id: Int,
    val name: String,
    val icon: String
)

data class Status (
    val success: Boolean,
    val errorMessage: String
)
data class JsonReq(
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

