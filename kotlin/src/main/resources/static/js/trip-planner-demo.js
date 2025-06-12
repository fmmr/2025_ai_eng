let sse = createSSE('/demo/trip-planner');

function setDestination(destination) {
    document.getElementById('destination').value = destination;
    document.getElementById('destination').focus();
}

function planTrip() {
    const destination = document.getElementById('destination').value.trim();
    if (!destination) {
        sse.log('❌ Please enter a destination', 'error');
        return;
    }

    const planTripBtn = document.getElementById('planTripBtn');
    planTripBtn.disabled = true;
    planTripBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Planning...';

    sse.log(`🚀 Starting multi-agent trip planning for ${destination}...`, 'info');
    
    // Step 1: Connect SSE stream (pure communication)
    sse.connect();
    
    // Step 2: Start trip planning (business logic)
    sse.sendRequest('/process', { destination })
        .then(data => {
            if (data.status === 'started') {
                sse.log('🤖 Deploying 4 specialized agents in parallel...', 'info');
            } else {
                sse.log('❌ Failed to start trip planning', 'error');
                resetButton(planTripBtn);
            }
        })
        .catch(error => {
            sse.log(`❌ Request failed: ${error.message}`, 'error');
            resetButton(planTripBtn);
        });
}

function resetButton(planTripBtn) {
    planTripBtn.disabled = false;
    planTripBtn.innerHTML = '<i class="bi bi-compass"></i> Plan Trip';
}

function fetchAndDisplayTripPlan() {
    const console = document.getElementById('sseConsole');
    
    // Add separator
    const separator = document.createElement('div');
    separator.className = 'border-top my-3';
    console.appendChild(separator);
    
    // Simple GET request - session scope handles the data
    fetch('/demo/trip-planner/trip-plan-fragment')
        .then(response => response.text())
        .then(htmlContent => {
            const tripPlanDiv = document.createElement('div');
            tripPlanDiv.innerHTML = htmlContent;
            tripPlanDiv.style.fontFamily = 'inherit';
            console.appendChild(tripPlanDiv);
            console.scrollTop = console.scrollHeight;
        })
        .catch(error => {
            sse.log('❌ Error displaying trip plan: ' + error.message, 'error');
        });
}

// Handle SSE completion events
window.onSSEComplete = function() {
    fetchAndDisplayTripPlan();
    const planTripBtn = document.getElementById('planTripBtn');
    if (planTripBtn) {
        resetButton(planTripBtn);
    }
};

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    // Setup Enter key listener
    const destinationInput = document.getElementById('destination');
    if (destinationInput) {
        destinationInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                planTrip();
            }
        });
        destinationInput.focus();
    }
});