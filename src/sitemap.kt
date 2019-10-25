// Contains the routes for the Content Management System in the /home route.
package os3

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Xml
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import net.opens3.ourUrl
import javax.management.monitor.StringMonitor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.response.respondBytes

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
data class UrlSet(
    val loc: String,
    val lastmod: String,
    val changefreq: String,
    val priority: String
)


internal fun Route.siteMap() {

    get("/sitemap.xml") {
        val mapper = jacksonObjectMapper()
        var pages = getAllPostsAndPages()
        var thisUrlSet = mutableListOf<UrlSet>()
        for ((index, page) in pages.withIndex()) {
            for ((postIndex) in page.posts.withIndex()) {
                var thisUrl = UrlSet(
                    loc = "https://$ourUrl/post/${page.posts[postIndex].id}",
                    lastmod = page.posts[postIndex].createdTime,
                    changefreq = "monthly",
                    priority = "1.0"
                )
                thisUrlSet.add(thisUrl)
            }
        }
        call.respond(thisUrlSet)
    }

}