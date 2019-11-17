// Various functions for the web apps.
package os3

import net.opens3.DB_ADDRESS
import net.opens3.DB_NAME
import net.opens3.DB_PASSWORD
import net.opens3.DB_USERNAME
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import os3.ActiveChannelTable.channel_id
import os3.ActiveChannelTable.description
import os3.ActiveChannelTable.last_entry_date
import os3.ActiveChannelTable.latitude
import os3.ActiveChannelTable.longitude
import os3.ActiveChannelTable.name
import os3.ActiveChannelTable.weather_station


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
fun getAllWeatherStations(): List<ActiveChannel> {
    val allChannels = mutableListOf<ActiveChannel>()
    connectToDB()
    transaction {
        SchemaUtils.create(ActiveChannelTable)
        for (thisChannel in ActiveChannelTable.selectAll()) {
            val addedChannel = ActiveChannel(
                channel_id = thisChannel[channel_id],
                last_entry_date = thisChannel[last_entry_date],
                name = thisChannel[name],
                description = thisChannel[description],
                latitude = thisChannel[latitude],
                longitude = thisChannel[longitude],
                weatherStation = thisChannel[weather_station]
            )
            allChannels.add(addedChannel)
        }

    }
    return allChannels
}