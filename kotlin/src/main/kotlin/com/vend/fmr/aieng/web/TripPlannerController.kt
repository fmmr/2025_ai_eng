package com.vend.fmr.aieng.web

import com.vend.fmr.aieng.agents.TripPlanningCoordinator
import com.vend.fmr.aieng.utils.Demo
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/demo/trip-planner")
class TripPlannerController(private val tripPlanningCoordinator: TripPlanningCoordinator) : BaseController(Demo.TRAVEL_AGENT) {

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

        // Launch trip planning asynchronously
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                sendSseEvent(sessionId, "start", "🚀 Starting trip planning for $destination...")
                
                val tripPlan = tripPlanningCoordinator.planTrip(destination) { progress ->
                    sendSseEvent(sessionId, "progress", progress)
                }
                
                session.setAttribute("tripPlan", tripPlan)
                
                // Send completion event
                sendSseEvent(sessionId, "complete", "Trip planning completed")
                
            } catch (e: Exception) {
                sendSseEvent(sessionId, "error", "❌ Trip planning failed: ${e.message}")
            }
        }
        
        return ResponseEntity.ok(mapOf("status" to "started"))
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
