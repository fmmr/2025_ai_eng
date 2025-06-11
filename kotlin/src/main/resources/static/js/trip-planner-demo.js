let eventSource = null;
let sessionId = Date.now().toString();

function setDestination(destination) {
    document.getElementById('destination').value = destination;
    document.getElementById('destination').focus();
}

function planTrip() {
    const destination = document.getElementById('destination').value.trim();
    if (!destination) {
        addToLog('❌ Please enter a destination', 'error');
        return;
    }

    if (eventSource) {
        eventSource.close();
    }

    const planTripBtn = document.getElementById('planTripBtn');
    planTripBtn.disabled = true;
    planTripBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Planning...';

    addToLog(`🚀 Starting multi-agent trip planning for ${destination}...`, 'info');
    addToLog('🤖 Deploying 4 specialized agents in parallel...', 'info');

    sessionId = Date.now().toString();
    eventSource = new EventSource(`/demo/trip-planner/stream/${sessionId}?destination=${encodeURIComponent(destination)}`);

    eventSource.addEventListener('start', function(event) {
        addToLog(event.data, 'progress');
    });

    eventSource.addEventListener('progress', function(event) {
        addToLog(event.data, 'progress');
    });

    eventSource.addEventListener('complete', function(event) {
        addToLog('✅ All agents completed! Generating comprehensive trip plan...', 'success');
        fetchAndDisplayTripPlan(); // No sessionId needed
        
        planTripBtn.disabled = false;
        planTripBtn.innerHTML = '<i class="bi bi-compass"></i> Plan Trip';
        eventSource.close();
    });

    eventSource.addEventListener('error', function(event) {
        addToLog('❌ ' + event.data, 'error');
        planTripBtn.disabled = false;
        planTripBtn.innerHTML = '<i class="bi bi-compass"></i> Plan Trip';
        eventSource.close();
    });

    eventSource.onerror = function(event) {
        console.error('SSE connection error:', event);
        addToLog('❌ Connection error - please try again', 'error');
        planTripBtn.disabled = false;
        planTripBtn.innerHTML = '<i class="bi bi-compass"></i> Plan Trip';
    };
}

function addToLog(message, type = 'info') {
    const resultsLog = document.getElementById('resultsLog');
    const timestamp = new Date().toLocaleTimeString();
    
    let className = 'text-muted';
    if (type === 'error') className = 'text-danger';
    if (type === 'success') className = 'text-success';
    if (type === 'progress') className = 'text-info';
    
    const logEntry = document.createElement('div');
    logEntry.className = `mb-1 ${className}`;
    logEntry.innerHTML = `<span class="text-muted">[${timestamp}]</span> ${message}`;
    
    resultsLog.appendChild(logEntry);
    resultsLog.scrollTop = resultsLog.scrollHeight;
}

function fetchAndDisplayTripPlan() {
    const resultsLog = document.getElementById('resultsLog');
    
    // Add separator
    const separator = document.createElement('div');
    separator.className = 'border-top my-3';
    resultsLog.appendChild(separator);
    
    // Simple GET request - session scope handles the data
    fetch('/demo/trip-planner/trip-plan-fragment')
        .then(response => response.text())
        .then(htmlContent => {
            const tripPlanDiv = document.createElement('div');
            tripPlanDiv.innerHTML = htmlContent;
            tripPlanDiv.style.fontFamily = 'inherit'; // Use normal font for trip plan
            resultsLog.appendChild(tripPlanDiv);
            resultsLog.scrollTop = resultsLog.scrollHeight;
        })
        .catch(error => {
            addToLog('❌ Error displaying trip plan: ' + error.message, 'error');
        });
}

function clearResults() {
    document.getElementById('resultsLog').innerHTML = 
        '<div class="text-muted">🗺️ Ready to plan your trip! Select a destination above.</div>';
}

// Allow Enter key to submit
document.getElementById('destination').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        planTrip();
    }
});

// Focus destination input on page load
window.addEventListener('load', function() {
    document.getElementById('destination').focus();
});