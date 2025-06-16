package com.vend.fmr.aieng.mcp

import com.vend.fmr.aieng.Application
import com.vend.fmr.aieng.utils.AgentTool
import kotlinx.serialization.json.Json
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

@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class McpApiControllerTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Test
    fun `initialize request should return proper MCP response`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 1,
                "method": "initialize",
                "params": {
                    "protocolVersion": "2024-11-05",
                    "clientInfo": {
                        "name": "test-client",
                        "version": "1.0.0"
                    }
                }
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Initialize request should return 200 OK but got ${response.statusCode}" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 1) { "Response ID should match request ID" }
        assert(mcpResponse.result != null) { "Response should have result" }
        assert(mcpResponse.error == null) { "Response should not have error" }
        assert(mcpResponse.result!!.protocolVersion == "2024-11-05") { "Protocol version should match" }
        assert(mcpResponse.result!!.serverInfo != null) { "Server info should be present" }
        assert(mcpResponse.result!!.serverInfo!!.name == "Kotlin AI Engineering MCP Server") { 
            "Server name should be correct" 
        }
    }

    @Test
    fun `tools list request should return available tools`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 2,
                "method": "tools/list"
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Tools list request should return 200 OK but got ${response.statusCode}" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 2) { "Response ID should match request ID" }
        assert(mcpResponse.result != null) { "Response should have result" }
        assert(mcpResponse.error == null) { "Response should not have error" }
        assert(mcpResponse.result!!.tools != null) { "Response should have tools list" }
        assert(mcpResponse.result!!.tools!!.isNotEmpty()) { "Should have at least one tool" }

        // Verify hello_world tool is present (since it doesn't require API keys)
        val helloWorldTool = mcpResponse.result!!.tools!!.find { it.name == "hello_world" }
        assert(helloWorldTool != null) { "hello_world tool should be available" }
        assert(helloWorldTool!!.description.isNotEmpty()) { "Tool should have description" }
    }

    @Test
    fun `tools list should include all AgentTool entries`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 8,
                "method": "tools/list"
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Tools list request should return 200 OK but got ${response.statusCode}" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)
        val tools = mcpResponse.result!!.tools!!

        // Verify all AgentTool entries are present
        AgentTool.entries.forEach { agentTool ->
            val foundTool = tools.find { it.name == agentTool.functionName }
            assert(foundTool != null) { 
                "AgentTool ${agentTool.functionName} should be present in tools list" 
            }
        }

        // Verify count matches
        assert(tools.size == AgentTool.entries.size) {
            "Tools list size ${tools.size} should match AgentTool entries size ${AgentTool.entries.size}"
        }
    }

    @Test
    fun `hello world tool call should work without API keys`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 3,
                "method": "tools/call",
                "params": {
                    "name": "hello_world",
                    "arguments": {
                        "name": "TestRunner"
                    }
                }
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Tool call request should return 200 OK but got ${response.statusCode}" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 3) { "Response ID should match request ID" }
        assert(mcpResponse.result != null) { "Response should have result" }
        assert(mcpResponse.error == null) { "Response should not have error" }
        assert(mcpResponse.result!!.content != null) { "Response should have content" }
        assert(mcpResponse.result!!.content!!.isNotEmpty()) { "Content should not be empty" }
        
        val resultText = mcpResponse.result!!.content!!.first().text
        assert(resultText.contains("Hello, TestRunner")) { 
            "Result should contain greeting, got: $resultText" 
        }
    }

    @Test
    fun `current time tool call should work without API keys`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 4,
                "method": "tools/call",
                "params": {
                    "name": "current_time",
                    "arguments": {}
                }
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Current time tool call should return 200 OK but got ${response.statusCode}" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 4) { "Response ID should match request ID" }
        assert(mcpResponse.result != null) { "Response should have result" }
        assert(mcpResponse.error == null) { "Response should not have error" }
        assert(mcpResponse.result!!.content != null) { "Response should have content" }
        
        val resultText = mcpResponse.result!!.content!!.first().text
        assert(resultText.contains("Server time")) { 
            "Result should contain server time info, got: $resultText" 
        }
    }

    @Test
    fun `invalid method should return method not found error`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 5,
                "method": "invalid/method"
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Invalid method should still return 200 OK (JSON-RPC error in body)" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 5) { "Response ID should match request ID" }
        assert(mcpResponse.result == null) { "Error response should not have result" }
        assert(mcpResponse.error != null) { "Response should have error" }
        assert(mcpResponse.error!!.code == -32601) { "Should be method not found error code" }
        assert(mcpResponse.error!!.message.contains("Method not found")) { 
            "Error message should indicate method not found" 
        }
    }

    @Test
    fun `invalid JSON should return parse error`() {
        val requestBody = "{ invalid json }"

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Invalid JSON should still return 200 OK (JSON-RPC error in body)" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == null) { "Parse error response should have null ID" }
        assert(mcpResponse.result == null) { "Error response should not have result" }
        assert(mcpResponse.error != null) { "Response should have error" }
        assert(mcpResponse.error!!.code == -32700) { "Should be parse error code" }
        assert(mcpResponse.error!!.message.contains("Parse error")) { 
            "Error message should indicate parse error" 
        }
    }

    @Test
    fun `tool call with missing tool name should return invalid params error`() {
        val requestBody = """
            {
                "jsonrpc": "2.0",
                "id": 6,
                "method": "tools/call",
                "params": {
                    "arguments": {}
                }
            }
        """.trimIndent()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(requestBody, headers)

        val response = restTemplate.postForEntity(
            "http://localhost:$port/mcp/",
            request,
            String::class.java
        )

        assert(response.statusCode == HttpStatus.OK) { 
            "Missing tool name should return 200 OK (JSON-RPC error in body)" 
        }

        val responseBody = response.body!!
        val mcpResponse = json.decodeFromString<McpResponse>(responseBody)

        assert(mcpResponse.jsonrpc == "2.0") { "Response should be JSON-RPC 2.0" }
        assert(mcpResponse.id == 6) { "Response ID should match request ID" }
        assert(mcpResponse.result == null) { "Error response should not have result" }
        assert(mcpResponse.error != null) { "Response should have error" }
        assert(mcpResponse.error!!.code == -32602) { "Should be invalid params error code" }
        assert(mcpResponse.error!!.message.contains("Missing tool name")) { 
            "Error message should indicate missing tool name" 
        }
    }
}