// Data classes for the Kettlebell Competition Web App
package os3

import org.jetbrains.exposed.sql.Table

data class MyID(
    var id: Int,
    var active: Boolean
)

object KettleBellPresses : Table() {
    val id = integer("id").autoIncrement().primaryKey() // main id
    val uuid = integer("uuid") // allow tree structure (stipulate parent)
    val weight = integer("weight")
    val repetitions = integer("repetitions")
    val createdTime = varchar("createdTime", length = 150) // date, time and timezone
}