@file:Suppress("unused")

package com.vend.fmr.aieng.utils

import com.vend.fmr.aieng.impl.mocks.Mocks
import com.vend.fmr.aieng.impl.openai.Message
import com.vend.fmr.aieng.openAI

/**
 * OpenAI Function Calling Examples
 * 
 * This implements OpenAI's native function calling where:
 * 1. We define structured function schemas
 * 2. OpenAI decides which functions to call based on user query
 * 3. We execute the functions and return results
 * 4. OpenAI generates a final response
 */
object FunctionCallingExamples {
    
    /**
     * Main function calling agent - uses OpenAI's native function calling API
     */
    suspend fun functionCallingAgent(userQuery: String, debug: Boolean = false): String {
        val messages = mutableListOf<Message>()
        val tools = Mocks.getAvailableTools()
        
        // Add system message to encourage function usage
        messages.add(Message(
            role = Prompts.Roles.SYSTEM, 
            content = Prompts.FUNCTION_CALLING_SYSTEM
        ))
        
        // Add initial user query
        messages.add(Message(role = Prompts.Roles.USER, content = userQuery))
        
        if (debug) {
            println("🛠️ Starting Function Calling Agent for query: $userQuery")
            println("Available tools: ${tools.map { it.function.name }}")
            println("=".repeat(50))
        }
        
        var iteration = 0
        val maxIterations = 5
        
        while (iteration < maxIterations) {
            iteration++
            
            if (debug) {
                println("\n--- Iteration $iteration ---")
                println("💬 Messages in conversation: ${messages.size}")
            }
            
            // Call OpenAI with function definitions
            val response = openAI.createChatCompletionWithTools(
                messages = messages,
                tools = tools,
                temperature = 0.1,
                maxTokens = 400,
                toolChoice = "auto",
                debug = debug
            )
            
            if (debug) {
                println("AI Response: ${response.text()}")
                println("Has tool calls: ${response.hasToolCalls()}")
            }
            
            // Add assistant response to conversation
            messages.add(response.choices.first().message)
            
            // Check if OpenAI wants to call functions
            if (response.hasToolCalls()) {
                val toolCalls = response.getToolCalls()
                
                if (debug) {
                    println("🔧 Tool calls requested: ${toolCalls.size}")
                }
                
                // Execute each function call
                for (toolCall in toolCalls) {
                    if (debug) {
                        println("Executing: ${toolCall.function.name}(${toolCall.function.arguments})")
                    }
                    
                    val result = Mocks.executeFunctionCall(toolCall.function)
                    
                    if (debug) {
                        println("📋 Result: $result")
                    }
                    
                    // Add function result as a tool message
                    messages.add(Message(
                        role = Prompts.Roles.TOOL,
                        content = result,
                        toolCallId = toolCall.id
                    ))
                }
            } else {
                // No more function calls needed, AI has final answer
                if (debug) {
                    println("\n✅ Final Answer reached!")
                }
                return response.text()
            }
        }
        
        return "I wasn't able to complete this task within the iteration limit."
    }
}