package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.agents.TripPlanningCoordinator
import com.vend.fmr.aieng.utils.Demo
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Controller
@RequestMapping("/demo/trip-planner")
class TripPlannerController(private val tripPlanningCoordinator: TripPlanningCoordinator) : BaseController(Demo.TRAVEL_AGENT) {

    private val activeEmitters = ConcurrentHashMap<String, SseEmitter>()

    @GetMapping
    fun showDemo(): String {
        return "trip-planner-demo"
    }


    @PostMapping("/process")
    @ResponseBody
    suspend fun processTripPlanning(@RequestBody request: Map<String, String>, httpRequest: HttpServletRequest): ResponseEntity<Map<String, String>> {
        val destination = request["destination"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing destination"))
        val sessionId = request["sessionId"] ?: return ResponseEntity.badRequest().body(mapOf("error" to "Missing sessionId"))
        
        val session = httpRequest.session
        val emitter = activeEmitters[sessionId] ?: return ResponseEntity.badRequest().body(mapOf("error" to "No active SSE connection"))

        // Launch trip planning asynchronously
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                emitter.send(SseEmitter.event().name("start").data("🚀 Starting trip planning for $destination..."))
                
                val tripPlan = tripPlanningCoordinator.planTrip(destination) { progress ->
                    try {
                        emitter.send(SseEmitter.event().name("progress").data(progress))
                    } catch (_: Exception) {
                        // Emitter might be closed, ignore
                    }
                }
                
                session.setAttribute("tripPlan", tripPlan)
                
                // Send completion event
                emitter.send(SseEmitter.event().name("complete").data("Trip planning completed"))
                
            } catch (e: Exception) {
                emitter.send(SseEmitter.event().name("error").data("❌ Trip planning failed: ${e.message}"))
            }
        }
        
        return ResponseEntity.ok(mapOf("status" to "started"))
    }

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

    @Suppress("SpringMVCViewInspection")
    @GetMapping("/trip-plan-fragment")
    fun getTripPlanFragment(model: Model, request: HttpServletRequest): String {
        val tripPlan = request.session.getAttribute("tripPlan") ?: throw IllegalArgumentException("No trip plan found")
        model.addAttribute("tripPlan", tripPlan)
        request.session.removeAttribute("tripPlan")
        return "fragments/trip-plan-results :: trip-plan"
    }

}
