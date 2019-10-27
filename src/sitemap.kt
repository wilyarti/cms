// Contains the routes for the Content Management System in the /home route.
package os3

import com.fasterxml.jackson.dataformat.xml.XmlMapper
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
    val priority: String
)

data class Sitemap (
    val urlset: List<url>
)

internal fun Route.siteMap() {

    get("/sitemap.xml") {
        val mapper = XmlMapper()
        var pages = getAllPostsAndPages()
        var urlset = mutableListOf<url>()
        for ((index, page) in pages.withIndex()) {
            for ((postIndex) in page.posts.withIndex()) {
                var thisUrl = url(
                    loc = "https://$ourUrl/post/${page.posts[postIndex].id}",
                    lastmod = page.posts[postIndex].createdTime,
                    changefreq = "monthly",
                    priority = "1.0"
                )
                urlset.add(thisUrl)
            }
        }
        val sitemap = Sitemap(urlset = urlset)
        call.respondText(ContentType.Application.Xml) { mapper.writeValueAsString(sitemap) }
    }

}