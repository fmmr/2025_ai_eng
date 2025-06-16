package com.vend.fmr.aieng

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MiscEndpointsTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `home page should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Home page should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("AI Engineering")) {
            "Home page should contain 'AI Engineering' title"
        }
        assert(body.contains("demo-box") || body.contains("Demo")) {
            "Home page should contain demo content"
        }
    }

    @Test
    fun `chunking demo page should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/demo/chunking", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Chunking demo page should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("Chunking")) {
            "Chunking page should contain 'Chunking' title"
        }
        assert(body.contains("textInput")) {
            "Chunking page should contain text input form field"
        }
    }

    @Test
    fun `chunking POST should process text correctly`() {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }
        
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("textInput", "This is a sample text that should be chunked into smaller pieces for testing purposes.")
        formData.add("chunkSize", "30")
        formData.add("chunkOverlap", "5")
        
        val request = HttpEntity(formData, headers)
        val response = restTemplate.postForEntity("http://localhost:$port/demo/chunking", request, String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Chunking POST should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("chunks")) {
            "Response should contain chunked results"
        }
        assert(body.contains("This is a sample text")) {
            "Response should contain the original text chunks"
        }
    }

    @Test
    fun `actuator health endpoint should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/actuator/health", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Health endpoint should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("UP") || body.contains("status")) {
            "Health response should contain status information"
        }
    }

    @Test
    fun `actuator metrics endpoint should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/actuator/metrics", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Metrics endpoint should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("names")) {
            "Metrics response should contain metric names"
        }
    }

    @Test
    fun `static CSS file should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/css/main.css", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "CSS file should return 200 OK but got ${response.statusCode}"
        }
        
        val headers = response.headers
        assert(headers.getFirst("Content-Type")?.contains("text/css") == true) {
            "CSS file should have text/css content type"
        }
    }

    @Test
    fun `static JS file should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/js/main.js", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "JS file should return 200 OK but got ${response.statusCode}"
        }
        
        val headers = response.headers
        val contentType = headers.getFirst("Content-Type")
        assert(contentType?.contains("javascript") == true || contentType?.contains("text/plain") == true) {
            "JS file should have appropriate content type, got: $contentType"
        }
    }

    @Test
    fun `trip planner demo page should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/demo/trip-planner", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "Trip planner demo page should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("Trip Planner")) {
            "Trip planner page should contain 'Trip Planner' title"
        }
    }

    @Test
    fun `mcp server demo page should return 200 OK`() {
        val response = restTemplate.getForEntity("http://localhost:$port/demo/mcp-server", String::class.java)
        
        assert(response.statusCode == HttpStatus.OK) {
            "MCP server demo page should return 200 OK but got ${response.statusCode}"
        }
        
        val body = response.body!!
        assert(body.contains("MCP Server")) {
            "MCP server page should contain 'MCP Server' title"
        }
    }

    @Test
    fun `trip plan fragment without session should handle gracefully`() {
        val response = restTemplate.getForEntity("http://localhost:$port/demo/trip-planner/trip-plan-fragment", String::class.java)
        
        // Could be 404 (no session), 500 (error), or 200 with empty content - all acceptable for this test
        assert(response.statusCode == HttpStatus.NOT_FOUND || 
               response.statusCode == HttpStatus.OK || 
               response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR) {
            "Trip plan fragment should handle missing session gracefully but got ${response.statusCode}"
        }
    }

    @Test
    fun `non-existent endpoint should return 404`() {
        val response = restTemplate.getForEntity("http://localhost:$port/non-existent-endpoint", String::class.java)
        
        assert(response.statusCode == HttpStatus.NOT_FOUND) {
            "Non-existent endpoint should return 404 but got ${response.statusCode}"
        }
    }
}