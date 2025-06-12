let currentSessionId = null;
let eventSource = null;

function clearConsole() {
    document.getElementById('assistantConsole').innerHTML = '<div class="text-muted">Console cleared...</div>';
}

function addToConsole(message, type = 'info') {
    const console = document.getElementById('assistantConsole');
    const timestamp = new Date().toLocaleTimeString();
    const colorClass = type === 'error' ? 'text-danger' : type === 'success' ? 'text-success' : type === 'progress' ? 'text-primary' : 'text-muted';
    
    const messageDiv = document.createElement('div');
    messageDiv.className = `${colorClass} mb-1`;
    messageDiv.innerHTML = `<small>[${timestamp}]</small> ${message}`;
    
    console.appendChild(messageDiv);
    console.scrollTop = console.scrollHeight;
}

function setupSSE() {
    if (eventSource) {
        eventSource.close();
    }
    
    currentSessionId = Date.now().toString();
    eventSource = new EventSource(`/demo/assistants/stream/${currentSessionId}`);
    
    eventSource.addEventListener('connected', function(e) {
        addToConsole(e.data, 'info');
    });
    
    eventSource.addEventListener('progress', function(e) {
        addToConsole(e.data, 'progress');
    });
    
    eventSource.addEventListener('info', function(e) {
        addToConsole(e.data, 'info');
    });
    
    eventSource.addEventListener('success', function(e) {
        addToConsole(e.data, 'success');
    });
    
    eventSource.addEventListener('error', function(e) {
        addToConsole(e.data, 'error');
    });
    
    eventSource.onerror = function() {
        addToConsole('❌ Connection lost, reconnecting...', 'error');
        setTimeout(setupSSE, 2000);
    };
}

function processOperation(operation) {
    if (!currentSessionId) {
        setupSSE();
        setTimeout(() => processOperation(operation), 1000);
        return;
    }

    addToConsole(`🚀 Starting: ${operation}`, 'info');

    fetch('/demo/assistants/process', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            operation: operation,
            sessionId: currentSessionId
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.status !== 'started') {
            addToConsole('❌ Failed to start operation', 'error');
        }
    })
    .catch(error => {
        addToConsole(`❌ Request failed: ${error.message}`, 'error');
    });
}

// Initialize SSE connection on page load
document.addEventListener('DOMContentLoaded', function() {
    setupSSE();
});