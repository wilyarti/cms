// Contains the routes for the Content Management System in the /home route.
package os3

import com.fasterxml.jackson.annotation.JacksonAnnotation
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import net.opens3.ourUrl

/**
 *
<?xml version="1.0" encoding="UTF-8"?>

<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">

<url>

<loc>http://www.example.com/</loc>

<lastmod>2005-01-01</lastmod>

<changefreq>monthly</changefreq>

<priority>0.8</priority>

</url>

</urlset>
 */
data class url(
    val loc: String,
    val lastmod: String,
    val changefreq: String,
    @JsonProperty("foo")
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
            <!-- generator="$ourUrl" -->
            <urlset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" xmlns:mobile="http://www.google.com/schemas/sitemap-mobile/1.0" xmlns:image="http://www.google.com/schemas/sitemap-image/1.1" xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">
        """.trimIndent()
        for ((index, page) in pages.withIndex()) {
            for ((postIndex) in page.posts.withIndex()) {
                var thisUrl = url(
                    //TODO create SQL table for images
                    //TODO implement image scaping on update() and addPost() functions so the sitemap works
                    loc = "https://$ourUrl/post/${page.posts[postIndex].id}",
                    lastmod = page.posts[postIndex].createdTime,
                    changefreq = "monthly",
                    priority = "1.0"
                )
                ourXML += mapper.writeValueAsString(thisUrl) + "\n"
            }
        }
        ourXML += "</urlset>"
        call.respondText(ContentType.Application.Xml) { ourXML }
    }

}