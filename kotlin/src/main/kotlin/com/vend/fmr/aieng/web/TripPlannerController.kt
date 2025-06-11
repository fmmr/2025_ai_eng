package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.agents.TripPlanningCoordinator
import com.vend.fmr.aieng.utils.Demo
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Controller
@RequestMapping("/demo/trip-planner")
class TripPlannerController(private val tripPlanningCoordinator: TripPlanningCoordinator) : BaseController(Demo.TRAVEL_AGENT) {

    private val activeEmitters = ConcurrentHashMap<String, SseEmitter>()

    @GetMapping
    fun showDemo(model: Model): String {
        model.addAttribute("pageTitle", Demo.TRAVEL_AGENT.title)
        model.addAttribute("activeTab", "ai-agents")
        return "trip-planner-demo"
    }


    @GetMapping("/stream/{sessionId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    suspend fun streamTripPlanning(
        @PathVariable sessionId: String,
        @RequestParam destination: String,
        request: HttpServletRequest
    ): SseEmitter {
        val session = request.session
        val emitter = SseEmitter(300000L) // 5 minute timeout
        activeEmitters[sessionId] = emitter

        emitter.onCompletion { activeEmitters.remove(sessionId) }
        emitter.onTimeout { activeEmitters.remove(sessionId) }
        emitter.onError { activeEmitters.remove(sessionId) }


        // Start async processing immediately without blocking the SSE response
        val startEvent = SseEmitter.event().name("start").data("🚀 Starting trip planning for $destination...")
        emitter.send(startEvent)
        
        // Launch trip planning asynchronously - don't wait for it
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val tripPlan = tripPlanningCoordinator.planTrip(destination) { progress ->
                    try {
                        emitter.send(SseEmitter.event().name("progress").data(progress))
                    } catch (_: Exception) {
                        // Emitter might be closed, ignore
                    }
                }
                
                session.setAttribute("tripPlan", tripPlan)
                
                // Send completion event
                val completeEvent = SseEmitter.event().name("complete").data("ready")
                emitter.send(completeEvent)
                emitter.complete()
                
            } catch (e: Exception) {
                val errorEvent = SseEmitter.event().name("error").data("❌ Trip planning failed: ${e.message}")
                emitter.send(errorEvent)
                emitter.completeWithError(e)
            }
        }
        
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
