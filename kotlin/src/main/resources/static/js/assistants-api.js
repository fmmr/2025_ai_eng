let sse = createSSE('/demo/assistants-api');

function processOperation(operation) {
    sse.log(`🚀 Starting: ${operation}`, 'info');
    
    sse.sendRequest('/process', { operation })
        .then(data => {
            if (data.status !== 'started') {
                sse.log('❌ Failed to start operation', 'error');
            }
        })
        .catch(error => {
            sse.log(`❌ Request failed: ${error.message}`, 'error');
        });
}

function queryAssistant() {
    const messageInput = document.getElementById('chatMessage');
    const message = messageInput.value.trim();
    
    if (!message) {
        sse.log('❌ Please enter a message', 'error');
        return;
    }
    
    sse.log(`🚀 Starting: query`, 'info');
    
    sse.sendRequest('/process', { operation: 'query', message: message })
        .then(data => {
            if (data.status !== 'started') {
                sse.log('❌ Failed to start query', 'error');
            } else {
                messageInput.value = ''; // Clear input on successful start
            }
        })
        .catch(error => {
            sse.log(`❌ Query failed: ${error.message}`, 'error');
        });
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    sse.connect();
});