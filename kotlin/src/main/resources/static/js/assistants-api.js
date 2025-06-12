let sse = createSSE('/demo/assistants');

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

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    sse.connect();
});