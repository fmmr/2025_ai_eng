let sse = createSSE('/demo/mcp-assistant');

function setQuery(query) {
    document.getElementById('userQuery').value = query;
    document.getElementById('userQuery').focus();
}

function aiAssistedStreaming() {
    const userQuery = document.getElementById('userQuery').value.trim();
    const verbosity = document.getElementById('verbosityLevel').value;
    
    if (!userQuery) {
        sse.log('❌ Please enter a question or request first!', 'error');
        return;
    }
    
    // Disable inputs during processing
    const sendBtn = document.getElementById('sendBtn');
    const userInput = document.getElementById('userQuery');
    const verbositySelect = document.getElementById('verbosityLevel');
    
    sendBtn.disabled = true;
    sendBtn.innerHTML = '⏳ Processing...';
    userInput.disabled = true;
    verbositySelect.disabled = true;
    
    // Log user question prominently
    sse.log(userQuery, 'user_question');
    
    // Step 1: Connect SSE stream (pure communication)
    sse.connect();
    
    // Step 2: Start processing (business logic)
    sse.sendRequest('/process', { query: userQuery, verbosity: verbosity })
        .then(data => {
            if (data.status === 'started') {
                sse.log('🔗 Processing started...', 'info');
            } else {
                sse.log('❌ Failed to start processing', 'error');
                resetButtons(sendBtn, userInput, verbositySelect);
            }
        })
        .catch(error => {
            sse.log(`❌ Request failed: ${error.message}`, 'error');
            resetButtons(sendBtn, userInput, verbositySelect);
        });
}

// Handle SSE completion events
window.onSSEComplete = function() {
    const sendBtn = document.getElementById('sendBtn');
    const userInput = document.getElementById('userQuery');
    const verbositySelect = document.getElementById('verbosityLevel');
    
    resetButtons(sendBtn, userInput, verbositySelect);
    userInput.value = '';
    setTimeout(() => userInput.focus(), 100);
};

function resetButtons(sendBtn, userInput, verbositySelect) {
    sendBtn.disabled = false;
    sendBtn.innerHTML = '🤖 Send';
    userInput.disabled = false;
    verbositySelect.disabled = false;
}

async function newSession() {
    sse.log('🆕 Starting new session...', 'warning');
    
    try {
        await fetch('/demo/mcp-assistant/reset', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        });
        
        sse.log('✅ New session started - all caches cleared', 'success');
        
        // Clear input and focus
        const userInput = document.getElementById('userQuery');
        if (userInput) {
            userInput.value = '';
            setTimeout(() => userInput.focus(), 100);
        }
        
    } catch (error) {
        sse.log(`❌ New session failed: ${error.message}`, 'error');
    }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    sse.log('🚀 AI Assistant ready!', 'info');
    sse.log('💬 Session-aware conversation - I remember what we talk about', 'info');
    sse.log('💡 Try the example buttons or ask me anything!', 'warning');
    
    // Setup Enter key listener
    const userInput = document.getElementById('userQuery');
    if (userInput) {
        userInput.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                aiAssistedStreaming();
            }
        });
    }
});