// Contains all the data classes, SQL tables and JSON data classes.
package os3

import io.ktor.auth.Principal
import org.jetbrains.exposed.sql.Table
import java.math.BigInteger
import java.sql.Blob

const val MAXPOSTSPERPAGE = 5

data class MySession(val id: Int, val username: String, val group: String) : Principal

/** I made the decision to introduce NULLABLE rows into the SQL database.
 * The reason being is to be able to expand the functionality of the CMS system. In order to expand easily it will be
 * necessary to search for rows by type "NULL".
 * Also it makes it easy to just enter the type as NULL until the functionality of the CMS is expanded.
 *
 * I contemplated just inserting an empty string into the empty rows, but it is advantageous to be able to search by
 * NULL type rather than having to keep a convention for empty rows.
 *
 * I hope I do not regret this decision, as it will require me to fill the code with NULL safe calls.
 */
object Groups : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val groupName = varchar("groupName", length = 150) // group name
}

object Group : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val groupID = integer("groupID") // Groups.id field
    val userID = integer("userID") // Users.id field
}

object Files : Table() {
    // NOT NULLABLE entries
    val id = integer("id").autoIncrement().primaryKey() // main id
    val name = varchar("name", length = 150) // page name
    val path = varchar("path", length = 300) // page name

}

object Pages : Table() {
    // NOT NULLABLE entries
    val id = integer("id").autoIncrement().primaryKey() // main id
    val disabled = bool("disabled") // Do not delete pages. Just disable them.
    val name = varchar("name", length = 150) // page name
    val icon = varchar("icon", length = 150)  // feather icon
    val pageID = integer("pageID") // to enable sub pages.
    val author = varchar("author", length = 150)
    val createdTime = varchar("postedTime", length = 150) // date and time
    val timeZone = varchar("timeZone", 100) // timezone
    // NULLABLE entries below
    val parentID = integer("parentID").nullable() // allow tree structure (stipulate parent)
    val priorityBit = integer("priorityBit").nullable() // to re-order pages/posts and sticky bit
    val group = varchar("group", length = 150).nullable()
    val countryOfOrigin = varchar("countryOfOrigin", length = 8).nullable()
    val language = varchar("language", length = 150).nullable()
    val executionScript = text("executionScript").nullable()
    val metadata = text("metadata").nullable()
    val type = integer("type").nullable()
    val likes = integer("likes").nullable()
}

object Posts : Table() {
    // NOT NULLABLE entries
    val id = integer("id").autoIncrement().primaryKey() // main id
    val disabled = bool("disabled")
    val name = varchar("name", length = 150) // page name
    val icon = varchar("icon", length = 150)  // feather icon
    val pageID = integer("pageID")
    val author = varchar("author", length = 150)
    val createdTime = varchar("postedTime", length = 150) // date and time
    val timeZone = varchar("timeZone", 100) // timezone
    val contents = text("contents")
    // NULLABLE entries below
    val parentID = integer("parentID").nullable() // allow tree structure (stipulate parent)
    val priorityBit = integer("priorityBit").nullable() // to re-order pages and sticky bit
    val group = varchar("group", length = 150).nullable()
    val countryOfOrigin = varchar("countryOfOrigin", length = 8).nullable()
    val language = varchar("language", length = 150).nullable()
    val executionScript = text("executionScript").nullable()
    val metadata = text("metadata").nullable()
    val type = integer("type").nullable()
    val likes = integer("likes").nullable()
}

object Users : Table() {
    // NOT NULLABLE entries
    val id = integer("id").autoIncrement().primaryKey()
    val disabled = bool("disabled")
    val createdTime = varchar("createdTime", 150)
    val group = varchar("group", length = 150)
    val password = varchar("password", length = 150)
    val passwordSalt = varchar("passwordSalt", length = 150)
    val username = varchar("username", length = 32)

    // NULLABLE entries
    val firstName = varchar("firstName", 50).nullable()
    val lastName = varchar("lastName", 50).nullable()
    val streetAddress = varchar("streetAddress", 150).nullable()
    val postCode = varchar("postCode", 150).nullable()
    val state = varchar("state", 16).nullable()
    val country = varchar("country", 50).nullable()
    val countryCode = varchar("countryCode", 3).nullable()
    val language = varchar("language", length = 150).nullable()
    val email = varchar("email", length = 150).nullable()
    val areaCode = varchar("areaCode", 8).nullable()
    val mobile = varchar("mobile", length = 150).nullable()
    val secondaryGroup = integer("secondaryGroup").nullable()
    val metadata = text("metadata").nullable()
}

data class ThisFile(
    // NOT NULLABLE entries
    val id: Int, // main id
    val name: String,
    val path: String
)


data class ThisPost(
    // NOT NULLABLE entries
    val id: Int, // main id
    val disabled: Boolean,
    val name: String, // page name
    val icon: String, // feather icon
    val pageID: Int,
    val author: String,
    val createdTime: String,// date and time
    val timeZone: String, // timezone
    val contents: String,
    // NULLABLE entries below
    val parentID: Int?, // allow tree structure (stipulate parent)
    val priorityBit: Int?, // to re-order pages and sticky bit
    val group: String?,
    val countryOfOrigin: String?,
    val language: String?,
    val executionScript: String?,
    val metadata: String?,
    val type: Int?,
    val likes: Int?
)

data class ThisPage(
    // NOT NULLABLE entries
    val id: Int, // main id
    val disabled: Boolean,
    val name: String, // page name
    val icon: String, // feather icon
    val pageID: Int,
    val author: String,
    val createdTime: String,// date and time
    val timeZone: String, // timezone
    // NULLABLE entries below
    val parentID: Int?, // allow tree structure (stipulate parent)
    val priorityBit: Int?, // to re-order pages and sticky bit
    val group: String?,
    val countryOfOrigin: String?,
    val language: String?,
    val executionScript: String?,
    val metadata: String?,
    val type: Int?,
    val likes: Int?
)

data class CompletePage(
    // NOT NULLABLE entries
    val id: Int, // main id
    val disabled: Boolean,
    val name: String, // page name
    val icon: String, // feather icon
    val pageID: Int,
    val author: String,
    val createdTime: String,// date and time
    val timeZone: String, // timezone
    // NULLABLE entries below
    val parentID: Int?, // allow tree structure (stipulate parent)
    val priorityBit: Int?, // to re-order pages and sticky bit
    val group: String?,
    val countryOfOrigin: String?,
    val language: String?,
    val executionScript: String?,
    val metadata: String?,
    val type: Int?,
    val likes: Int?,
    val posts: MutableList<ThisPost>
)

data class Status(
    val success: Boolean,
    val errorMessage: String
)

data class JsonReq(
    val url: String
)

data class Success(
    val success: Boolean
)

/**
 * The Users SQL table should only be accessed through the correct data classes:
 * CreateThisUser()
 *  For initial creation of the Users() row. It includes all of the rows.
 *
 * ReadWriteThisUser()
 *  For administrative purposes. It includes all of the rows except:
 *      -> createdTime
 *      -> passwordSalt
 *
 * AuthUser()
 *  For verifying Users password. It only includes id, group, password and username.
 *
 * ReadUserInfo()
 *  For reading user data. Does not include:
 *      -> password
 *      -> passwordSalt
 */
data class ReadUserInfo(
    // NOT NULLABLE entries
    val id: Int,
    val disabled: Boolean,
    val createdTime: String,
    val group: String,
    val username: String,

    // NULLABLE entries
    val firstName: String?,
    val lastName: String?,
    val streetAddress: String?,
    val postCode: String?,
    val state: String?,
    val country: String?,
    val countryCode: String?,
    val language: String?,
    val email: String?,
    val areaCode: String?,
    val mobile: String?,
    val secondaryGroup: Int?,
    val metadata: String?
)

data class CreateThisUser(
    // NOT NULLABLE entries
    val id: Int,
    val disabled: Boolean,
    val createdTime: String,
    val group: String,
    val password: String,
    val passwordSalt: String,
    val username: String,

    // NULLABLE entries
    val firstName: String?,
    val lastName: String?,
    val streetAddress: String?,
    val postCode: String?,
    val state: String?,
    val country: String?,
    val countryCode: String?,
    val language: String?,
    val email: String?,
    val areaCode: String?,
    val mobile: String?,
    val secondaryGroup: Int?,
    val metadata: String?
)

data class ReadWriteThisUser(
    // NOT NULLABLE entries
    val id: Int,
    val disabled: Boolean,
    val group: String,
    val password: String,
    val username: String,

    // NULLABLE entries
    val firstName: String?,
    val lastName: String?,
    val streetAddress: String?,
    val postCode: String?,
    val state: String?,
    val country: String?,
    val countryCode: String?,
    val language: String?,
    val email: String?,
    val areaCode: String?,
    val mobile: String?,
    val secondaryGroup: Int?,
    val metadata: String?
)

data class AuthUser(
    val id: Int,
    val username: String,
    val group: String,
    val password: String
)

data class UserNameCheck(
    val username: String
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

/**
 * PingOmatic get request, with parameters in URL.
title=Statistics
blogurl=https%3A%2F%2Fopens3.net%2Fhome%2F1
rssurl=http%3A%2F%2F
chk_blogs=on
chk_feedburner=on
chk_tailrank=on
chk_superfeedr=on
 */

data class Ping(
    val title: String,
    val blogurl: String,
    val rssurl: String,
    val chk_blogs: String,
    val chk_feedburner: String,
    val chk_tailrank: String,
    val chk_superfeedr: String
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
    const val ADMIN = "/admin.html"
}

object Cookies {
    const val AUTH_COOKIE = "auth"
}

