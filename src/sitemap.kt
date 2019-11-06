// Contains the routes for the Content Management System in the /home route.
package os3

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import net.opens3.OUR_URL
import net.opens3.STATIC_WWW
import org.joda.time.DateTime
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import org.joda.time.format.DateTimeFormat




/**
 * <image:image>
 *     <image:loc>https://clinetworking.files.wordpress.com/2019/10/screenshot-from-2019-10-22-23-11-05.png</image:loc>
 *     <image:title>Screenshot from 2019-10-22 23-11-05</image:title>
 * </image:image>
 */

//TODO implement image SQL table for sitemap.xml
data class image(
    val image_loc: String,
    val image_title: String
)
data class url(
    val loc: String,
    val lastmod: String,
    val changefreq: String,
    val priority: String
)

/** FasterXML does not support XML Schema Declarations...
 * https://github.com/FasterXML/jackson-dataformat-xml/issues/90
 * So we have to build this by hand.
 *
 * It's not so messy as each url node is built using the XML serializer.
 * This allows us to keep the form type checked.
 */

internal fun Route.siteMap() {

    get("/sitemap.xml") {
        val mapper = XmlMapper()
        val pages = getAllPostsAndPages()
        var ourXML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- generator="$OUR_URL" -->
            <urlset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:mobile="http://www.google.com/schemas/sitemap-mobile/1.0" xmlns:image="http://www.google.com/schemas/sitemap-image/1.1" xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
        """.trimIndent()
        for ((index, page) in pages.withIndex()) {
            for ((postIndex) in page.posts.withIndex()) {
                val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
                val time = DateTime(page.posts[postIndex].createdTime)
                var thisUrl = url(
                    //TODO create SQL table for images
                    //TODO implement image scraping on update() and addPost() functions so the sitemap works
                    loc = "https://$OUR_URL/post/${page.posts[postIndex].id}",
                    lastmod = fmt.print(time),
                    changefreq = "monthly",
                    priority = "1.0"
                )
                ourXML += mapper.writeValueAsString(thisUrl) + "\n"
            }
        }
        File(STATIC_WWW).walk().forEach {
            val pattern = "..html$".toRegex()
            if (pattern.containsMatchIn(it.absolutePath)) {
                try {
                    val path = Paths.get(it.absolutePath)
                    val attr: BasicFileAttributes
                    attr = Files.readAttributes(path, BasicFileAttributes::class.java)
                    val fmt = DateTimeFormat.forPattern("YYYY-MM-dd")
                    val time = DateTime(attr.creationTime().toString())
                    val thisUrl = url(
                        loc = "https://$OUR_URL/${it.name}",
                        lastmod = fmt.print(time),
                        changefreq = "monthly",
                        priority = "1.0"
                    )
                    ourXML += mapper.writeValueAsString(thisUrl) + "\n"
                } catch (e:Error) {
                    println(e)
                }
            }
        }
        ourXML += "</urlset>"
        call.respondText(ContentType.Application.Xml) { ourXML }
    }

}