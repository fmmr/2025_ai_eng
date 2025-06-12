package com.vend.fmr.aieng

import com.vend.fmr.aieng.utils.Demo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.test.StepVerifier
import java.time.Duration

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoWebIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `home page should load successfully`() {
        val response = restTemplate.getForEntity("http://localhost:$port/", String::class.java)
        assert(response.statusCode == HttpStatus.OK) { "Home page should return 200 OK" }
        assert(response.body!!.contains("AI Engineering")) { "Home page should contain title" }
    }

    @ParameterizedTest
    @MethodSource("getLocalDemos")
    fun `all demo GET endpoints should return 200`(demo: Demo) {
        val url = "http://localhost:$port${demo.route}"
        val response = restTemplate.getForEntity(url, String::class.java)

        assert(response.statusCode == HttpStatus.OK) {
            "Demo ${demo.id} at ${demo.route} should return 200 OK but got ${response.statusCode}"
        }

        // Verify the page contains the demo title
        assert(response.body!!.contains(demo.title)) {
            "Demo ${demo.id} page should contain title '${demo.title}'"
        }
    }

    @ParameterizedTest
    @MethodSource("getLocalDemos")
    fun `SSE endpoints should send connected event`(demo: Demo) {
        val webTestClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .build()

        val result = webTestClient
            .get()
            .uri("${demo.route}/stream/test-session")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .returnResult<String>()

        val eventFlux = result.responseBody

        // Verify we get the SSE stream and can read initial events
        StepVerifier
            .create(eventFlux)
            .expectSubscription()
            .consumeNextWith { event ->
                // Should contain the connected event
                assert(event.contains("event: connected") || event.contains("SSE stream connected")) {
                    "First SSE event should be connected event, got: $event"
                }
            }
            .thenCancel()
            .verify(Duration.ofSeconds(5))
    }

    @Test
    fun `static resources should be accessible`() {
        // Test CSS
        val cssResponse = restTemplate.getForEntity("http://localhost:$port/css/main.css", String::class.java)
        assert(cssResponse.statusCode == HttpStatus.OK) { "CSS should be accessible" }

        // Test JS
        val jsResponse = restTemplate.getForEntity("http://localhost:$port/js/main.js", String::class.java)
        assert(jsResponse.statusCode == HttpStatus.OK) { "Main JS should be accessible" }

        // Test SSE JS
        val sseJsResponse = restTemplate.getForEntity("http://localhost:$port/js/sse.js", String::class.java)
        assert(sseJsResponse.statusCode == HttpStatus.OK) { "SSE JS should be accessible" }
    }

    @ParameterizedTest
    @MethodSource("getDemosWithJavaScript")
    fun `demo-specific JavaScript files should be accessible`(demo: Demo) {
        val jsFilename = "${demo.id}.js"
        val response = restTemplate.getForEntity("http://localhost:$port/js/$jsFilename", String::class.java)

        assert(response.statusCode == HttpStatus.OK) {
            "JavaScript file $jsFilename for demo ${demo.id} should be accessible but got ${response.statusCode}"
        }
    }

    @Test
    fun `check common javascript`() {
        listOf("main.js", "sse.js").forEach { jsFilename ->
            val response = restTemplate.getForEntity("http://localhost:$port/js/$jsFilename", String::class.java)
            assert(response.statusCode == HttpStatus.OK) {
                "JavaScript file $jsFilename should be accessible but got ${response.statusCode}"
            }
        }
    }

    fun getLocalDemos(): List<Demo> = Demo.entries.filter { it.local() }

    fun getDemosWithJavaScript(): List<Demo> = Demo.entries.filter { it.hasJavaScript }
}