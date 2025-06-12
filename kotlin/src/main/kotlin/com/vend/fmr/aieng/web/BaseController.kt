package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.utils.Demo
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

abstract class BaseController(protected val demo: Demo) {
    init {
        // Runtime validation
        require (this::class.simpleName == demo.expectedControllerName()) {
            logger.error("Controller ${this::class.simpleName} should be named ${demo.expectedControllerName()}")
        }

        val path = (this::class.annotations.first { it is RequestMapping } as RequestMapping).value.first()
        require(path == demo.route){
            val msg = "Controller ${this::class.simpleName} should have path: ${demo.route} but has $path"
            logger.error(msg)
            msg
        }
    }

    // Common SSE emitter storage for all controllers
    protected val activeEmitters = ConcurrentHashMap<String, SseEmitter>()

    @ModelAttribute("demo")
    fun demo(): Demo = demo

    @ModelAttribute("pageTitle")
    fun pageTitle(): String = demo.title

    @ModelAttribute("activeTab")
    fun activeTab(): String = demo.id

    /**
     * Hook for controllers to add custom model attributes
     */
    open fun addDefaultModel(model: Model, session: HttpSession) {
        // Default implementation does nothing
    }

    /**
     * Standard demo page endpoint - returns template based on demo.id
     */
    @GetMapping
    open fun showDemo(model: Model, session: HttpSession): String {
        addDefaultModel(model, session)
        return demo.id
    }

    /**
     * Standard SSE stream endpoint that all demos can use.
     * Just establishes the communication channel - no business logic.
     */
    @GetMapping("/stream/{sessionId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamUpdates(@PathVariable sessionId: String): SseEmitter {
        val emitter = SseEmitter(300000L) // 5 minute timeout
        activeEmitters[sessionId] = emitter

        emitter.onCompletion { activeEmitters.remove(sessionId) }
        emitter.onTimeout { activeEmitters.remove(sessionId) }
        emitter.onError { activeEmitters.remove(sessionId) }

        // Send connected event
        emitter.send(SseEmitter.event().name("connected").data("SSE stream connected"))

        return emitter
    }

    /**
     * Helper method to send SSE events to a specific session
     */
    protected fun sendSseEvent(sessionId: String, eventType: String, data: String) {
        val emitter = activeEmitters[sessionId]
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(data))
            } catch (_: Exception) {
                activeEmitters.remove(sessionId)
            }
        }
    }

    /**
     * Helper method to send SSE events with structured data
     */
    protected fun sendSseEvent(sessionId: String, eventType: String, message: String, data: Any? = null) {
        val emitter = activeEmitters[sessionId]
        if (emitter != null) {
            try {
                val eventData = if (data != null) {
                    mapOf("message" to message, "data" to data)
                } else {
                    message
                }
                emitter.send(SseEmitter.event().name(eventType).data(eventData))
            } catch (_: Exception) {
                activeEmitters.remove(sessionId)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaseController::class.java)
    }


}