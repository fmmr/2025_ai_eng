@file:Suppress("unused")

package com.vend.fmr.aieng.examples

import com.vend.fmr.aieng.utils.*
import com.vend.fmr.aieng.apis.mocks.Mocks
import com.vend.fmr.aieng.apis.openai.Message
import com.vend.fmr.aieng.apis.openai.TextContent
import com.vend.fmr.aieng.openAI

/**
 * ReAct Agent Examples - Reasoning + Acting pattern implementation
 * 
 * This implements the ReAct pattern where the AI:
 * 1. Thinks about what to do
 * 2. Takes an action (calls a function)  
 * 3. Observes the result
 * 4. Repeats until it can provide a final answer
 */
object ReActAgentExamples {
    
    
    
    /**
     * Main ReAct agent function - implements the Thought/Action/Observation loop
     * Uses proper message-based conversation with OpenAI
     */
    suspend fun reactAgent(userQuery: String, maxIterations: Int = 10, debug: Boolean = false): String {
        val messages = mutableListOf<Message>()
        
        messages.add(Message(Prompts.Roles.SYSTEM, TextContent(Prompts.REACT_AGENT_SYSTEM)))
        
        messages.add(Message(Prompts.Roles.USER, TextContent(userQuery)))
        
        if (debug) {
            println("🤖 Starting ReAct Agent for query: $userQuery")
            println("=".repeat(50))
        }
        
        for (iteration in 0 until maxIterations) {
            if (debug) {
                println("\n--- Iteration ${iteration + 1} ---")
                println("💬 Messages in conversation: ${messages.size}")
            }
            
            val aiResponse = openAI.createChatCompletion(
                messages = messages,
                temperature = 0.1,
                maxTokens = 400
            )
            
            val currentResponse = aiResponse.text()
            
            if (debug) {
                println("AI: $currentResponse")
            }
            
            messages.add(Message(Prompts.Roles.ASSISTANT, TextContent(currentResponse)))
            
            if (currentResponse.contains("Final Answer:", ignoreCase = true)) {
                val finalAnswerRegex = Regex("Final Answer:\\s*(.*)", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                val match = finalAnswerRegex.find(currentResponse)
                if (match != null) {
                    val finalAnswer = match.groupValues[1].trim()
                    if (debug) {
                        println("\n✅ Final Answer reached!")
                    }
                    return finalAnswer
                }
            }
            
            val action = Mocks.parseAction(currentResponse)
            if (action != null) {
                val (functionName, params) = action
                if (debug) {
                    println("🔧 Executing: $functionName(${params.joinToString(", ")})")
                }
                
                val result = Mocks.executeFunction(functionName, params)
                val observation = "Observation: $result"
                
                if (debug) {
                    println("📋 $observation")
                }
                
                messages.add(Message(Prompts.Roles.USER, TextContent(observation)))
            } else {
                // No action found, AI might be done or confused
                if (debug) {
                    println("⚠️ No action found in response, ending loop")
                }
                break
            }
        }
        
        return "I wasn't able to complete this task within the iteration limit."
    }
}