// Contains the routes for the Content Management System in the /home route.
package os3

import io.ktor.application.call
import io.ktor.http.ContentType.Application.Xml
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Route
import io.ktor.routing.get
import net.opens3.ourUrl

internal fun Route.siteMap() {

    get("/sitemap.xml") {
        var xml =
            """<?xml version="1.0" encoding="UTF-8"?>
                <!-- generator="$ourUrl" -->
                <urlset xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://www.sitemaps.org/schemas/sitemap/0.9"
                        xmlns:mobile="http://www.google.com/schemas/sitemap-mobile/1.0"
                        xmlns:image="http://www.google.com/schemas/sitemap-image/1.1"
                        xsi:schemaLocation="http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd">""".trimIndent()
        var pages = getAllPostsAndPages()
        for ((index, page) in pages.withIndex()) {
            for ((postIndex) in page.posts.withIndex()) {
                var thisPage = """
                        <url>
                            <loc>
                            https://$ourUrl/post/${page.posts[postIndex].id}
                            </loc>
                            <mobile:mobile/>
                            <lastmod>${page.posts[postIndex].createdTime}</lastmod>
                            <changefreq>monthly</changefreq>
                        </url>
                        """.trimIndent();
                xml += thisPage;
            }
        }
        xml += "</urlset>"
        call.respondText(contentType = Xml) { xml }
    }

}