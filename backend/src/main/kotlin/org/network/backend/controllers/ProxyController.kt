package org.network.backend.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.io.FileWriter
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/v1")
class ProxyController {

    val restTemplate = RestTemplate()
    val cache = ConcurrentHashMap<String, CacheEntry>()
    val blacklist = listOf("youtube.com")

    @GetMapping("/public/proxy/{url}")
    fun proxyGet(@PathVariable url: String): ResponseEntity<String> {
        if (blacklist.contains(url)) {
            return ResponseEntity("URL is blocked", HttpStatus.FORBIDDEN)
        }
        return try {
            val cachedEntry = cache[url]
            if (cachedEntry != null && cachedEntry.isValid()) {
                logRequest(url, HttpStatus.OK, true)
                ResponseEntity(cachedEntry.responseBody, HttpStatus.OK)
            } else {
                val headers = if (cachedEntry != null) {
                    val ifModifiedSince = cachedEntry.lastModified
                    mapOf("If-Modified-Since" to ifModifiedSince.toString())
                } else {
                    null
                }
                val responseEntity = restTemplate.getForEntity("http://$url", String::class.java, headers)
                if (responseEntity.statusCode == HttpStatus.NOT_MODIFIED) {
                    cache[url]!!.updateAccessTime()
                    logRequest(url, HttpStatus.OK, true)
                    ResponseEntity(cache[url]!!.responseBody, HttpStatus.OK)
                } else {
                    val responseBody = responseEntity.body ?: ""
                    val lastModified = responseEntity.headers.lastModified
                    val cacheEntry = CacheEntry(responseBody, lastModified)
                    cache[url] = cacheEntry
                    logRequest(url, responseEntity.statusCode, false)
                    ResponseEntity(responseBody, responseEntity.statusCode)
                }
            }
        } catch (e: HttpClientErrorException) {
            logRequest(url, e.statusCode, false)
            ResponseEntity(e.message, e.statusCode)
        }
    }

    private fun logRequest(url: String, statusCode: HttpStatusCode, fromCache: Boolean) {
        val logMessage = "[$url] - ${statusCode.value()} ${LocalDateTime.now()} - From Cache: $fromCache"
        println(logMessage)
        FileWriter("proxy.log", true).use {
            it.write(logMessage + "\n")
        }
    }

    class CacheEntry(val responseBody: String, var lastModified: Long = System.currentTimeMillis()) {
        fun isValid(): Boolean {
            // Define your cache expiration logic here
            // For simplicity, let's say cache entries are valid for 1 minute
            return System.currentTimeMillis() - lastModified < 60000
        }

        fun updateAccessTime() {
            lastModified = System.currentTimeMillis()
        }
    }

}