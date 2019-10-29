// Various functions for the web apps.
package os3

import net.opens3.DB_ADDRESS
import net.opens3.DB_NAME
import net.opens3.DB_PASSWORD
import net.opens3.DB_USERNAME
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun connectToDB(): Unit {
    Database.connect(
        "jdbc:mysql://${DB_ADDRESS}/${DB_NAME}",
        "com.mysql.jdbc.Driver",
        user = DB_USERNAME,
        password = DB_PASSWORD
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