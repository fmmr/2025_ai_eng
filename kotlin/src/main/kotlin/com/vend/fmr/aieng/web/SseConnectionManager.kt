package com.vend.fmr.aieng.web

import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class SseConnectionManager : SmartLifecycle {
    
    private val allEmitters = ConcurrentHashMap<String, SseEmitter>()
    private var running = false
    
    companion object {
        private val logger = LoggerFactory.getLogger(SseConnectionManager::class.java)
    }
    
    fun registerEmitter(sessionId: String, emitter: SseEmitter) {
        allEmitters[sessionId] = emitter
        logger.debug("Registered SSE emitter: $sessionId (total: ${allEmitters.size})")
    }
    
    fun unregisterEmitter(sessionId: String) {
        allEmitters.remove(sessionId)
        logger.debug("Unregistered SSE emitter: $sessionId (total: ${allEmitters.size})")
    }
    
    override fun start() {
        running = true
        logger.info("SSE Connection Manager started")
    }
    
    override fun stop() {
        logger.info("🔄 Closing ${allEmitters.size} SSE connections...")
        allEmitters.values.forEach { emitter ->
            try {
                emitter.complete()
            } catch (_: Exception) {
                // Ignore - already closed
            }
        }
        allEmitters.clear()
        running = false
        logger.info("✅ All SSE connections closed")
    }
    
    override fun isRunning(): Boolean = running
    
    // Run BEFORE web server graceful shutdown (phase 2147482623)
    override fun getPhase(): Int = 2147482624
}