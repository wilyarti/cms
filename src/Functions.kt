// Various functions for the web apps.
package os3

import net.opens3.db_password
import net.opens3.db_username
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.and


fun connectToDB(): Unit {
    Database.connect(
        "jdbc:mysql://127.0.0.1:3306/webappsadmin",
        "com.mysql.jdbc.Driver",
        user = db_username,
        password = db_password
    )
}

fun userList(): AuthUser? {
    connectToDB()
    var returnedUserList: AuthUser?
    returnedUserList = null
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.selectAll()) {
            val returnedUser = AuthUser(
                id = user[Users.id],
                username = user[Users.username],
                group = user[Users.group],
                password = user[Users.password]
            )
            returnedUserList = returnedUser
        }
    }
    return returnedUserList
}

fun verifyUserCredentials(thisName: String): AuthUser? {
    connectToDB()
    var authorisedUser: AuthUser?
    authorisedUser = null
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select{ Users.username.eq(thisName) and Users.disabled.eq(false)}) {
            val returnedUser = AuthUser(
                id = user[Users.id],
                username = user[Users.username],
                group = user[Users.group],
                password = user[Users.password]
            )
            authorisedUser = returnedUser
            }
    }
    return authorisedUser
}