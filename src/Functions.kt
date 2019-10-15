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

fun authUser(thisName: String): String? {
    connectToDB()
    var returnedPassword: String?
    returnedPassword = null
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.name eq thisName }) {
            //TODO add missing fields.
            val returnedUser = ThisUser(
                id = user[Users.id],
                disabled = false,
                name = user[Users.name],
                mobile = "1234",
                email = "foo@bar.com",
                group = user[Users.group],
                secondaryGroup = user[Users.secondaryGroup],
                metadata = user[Users.metadata],
                password = user[Users.password]
            )
            returnedPassword = returnedUser.password
        }
    }
    return returnedPassword
}

fun getUser(thisName: String): ThisUser? {
    connectToDB()
    var returnedUserList: ThisUser?
    returnedUserList = null
    transaction {
        SchemaUtils.create(Users)
        for (user in Users.select { Users.name eq thisName }) {
            val returnedUser = ThisUser(
                id = user[Users.id],
                disabled = user[Users.disabled],
                mobile = user[Users.mobile],
                email = user[Users.email],
                name = user[Users.name],
                group = user[Users.group],
                secondaryGroup = user[Users.secondaryGroup],
                metadata = user[Users.metadata],
                password = user[Users.password]
            )
            returnedUserList = returnedUser
        }
    }
    return returnedUserList
}