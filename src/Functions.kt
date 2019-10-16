// Various functions for the web apps.
package os3

import net.opens3.db_password
import net.opens3.db_username
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


fun connectToDB(): Unit {
    Database.connect(
        "jdbc:mysql://127.0.0.1:3306/webappsadmin",
        "com.mysql.jdbc.Driver",
        user = db_username,
        password = db_password
    )
}

fun authUser(thisName: String): AuthUser? {
    connectToDB()
    var returnedUserList: AuthUser?
    returnedUserList = null
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.username eq thisName; Users.disabled eq false}) {
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