
import io.ktor.auth.Principal
import org.jetbrains.exposed.sql.Table

data class MyID(
    var id: Int,
    var active: Boolean
)

object KettleBellPresses : Table() {
    val id = KettleBellPresses.integer("id").autoIncrement().primaryKey() // main id
    val uuid = KettleBellPresses.integer("uuid") // allow tree structure (stipulate parent)
    val weight = KettleBellPresses.integer("weight")
    val repetitions = KettleBellPresses.integer("repetitions")
    val createdTime = KettleBellPresses.varchar("createdTime", length = 150) // date, time and timezone
}