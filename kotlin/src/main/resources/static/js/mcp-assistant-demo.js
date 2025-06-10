// AI Assistant Demo JavaScript
let conversationHistory = [];

function log(message, type = 'info') {
    const activityLog = document.getElementById('activityLog');
    const timestamp = new Date().toLocaleTimeString();
    
    const entry = document.createElement('div');
    
    // Special styling for user questions and AI responses
    if (type === 'user_question') {
        entry.className = 'mb-3 p-2 border-start border-primary border-3 bg-light';
        entry.innerHTML = message;
    } else if (type === 'final_result') {
        entry.className = 'mb-3 p-2 border-start border-success border-3 bg-light';
        entry.innerHTML = message;
    } else {
        // Debug/progress messages - smaller and muted
        const typeColors = {
            'info': 'text-muted',
            'success': 'text-success', 
            'error': 'text-danger',
            'warning': 'text-warning'
        };
        entry.className = `mb-1 ${typeColors[type] || 'text-muted'}`;
        entry.innerHTML = `<small class="text-muted">[${timestamp}]</small> <small>${message}</small>`;
    }
    
    activityLog.appendChild(entry);
    activityLog.scrollTop = activityLog.scrollHeight;
}

function clearLog() {
    document.getElementById('activityLog').innerHTML = 
        '<div class="text-muted">🤖 Hello! I\'m your AI assistant. Ask me anything!</div>';
}

function setupUserQueryInput() {
    const userQueryInput = document.getElementById('userQuery');
    if (userQueryInput) {
        userQueryInput.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                aiAssistedStreaming();
            }
        });
    }
}

function setSendButtonState(isProcessing) {
    const sendBtn = document.getElementById('sendBtn');
    const userQuery = document.getElementById('userQuery');
    const verbosityLevel = document.getElementById('verbosityLevel');
    
    if (isProcessing) {
        sendBtn.innerHTML = '⏳ Processing...';
        sendBtn.disabled = true;
        sendBtn.className = 'btn btn-secondary mb-3';
        userQuery.disabled = true;
        verbosityLevel.disabled = true;
    } else {
        sendBtn.innerHTML = '🤖 Send';
        sendBtn.disabled = false;
        sendBtn.className = 'btn btn-primary mb-3';
        userQuery.disabled = false;
        verbosityLevel.disabled = false;
    }
}

function setQuery(query) {
    document.getElementById('userQuery').value = query;
    document.getElementById('userQuery').focus();
}

function clearInputAndFocus() {
    const userQueryInput = document.getElementById('userQuery');
    if (userQueryInput) {
        userQueryInput.value = '';
        // Use setTimeout to ensure focus happens after any UI updates
        setTimeout(() => {
            userQueryInput.focus();
        }, 100);
    }
}


async function aiAssistedStreaming() {
    const userQuery = document.getElementById('userQuery').value.trim();
    const verbosity = document.getElementById('verbosityLevel').value;
    
    if (!userQuery) {
        log('❌ Please enter a question or request first!', 'error');
        return;
    }
    
    // Set button to processing state
    setSendButtonState(true);
    
    // Log the user question prominently
    log(userQuery, 'user_question');
    
    try {
        // Start streaming request first to get the session ID
        const response = await fetch('/demo/mcp-assistant/stream-chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                query: userQuery,
                verbosity: verbosity
            })
        });
        
        const result = await response.json();
        
        if (result.sessionId) {
            log(`🔗 Setting up SSE connection for session: ${result.sessionId}`, 'info');
            
            // Set up SSE connection with the actual session ID
            const eventSource = new EventSource(`/demo/mcp-assistant/stream/${result.sessionId}`);
            
            eventSource.onopen = function() {
                log('✅ SSE connection opened', 'success');
            };
            
            eventSource.addEventListener('connected', function(event) {
                const data = JSON.parse(event.data);
                log(`🔌 SSE connected: ${data.message}`, 'info');
            });
            
            eventSource.addEventListener('progress', function(event) {
                const data = JSON.parse(event.data);
                log(data.message, 'info');
            });
            
            eventSource.addEventListener('result', function(event) {
                const data = JSON.parse(event.data);
                
                // Extract and display the actual AI response prominently
                const aiResponse = data.data?.response || data.message;
                if (aiResponse) {
                    // Remove any "Final Result:" prefix and show just the response
                    const cleanResponse = aiResponse.replace(/^.*Final Result:\s*/, '');
                    log(cleanResponse, 'final_result');
                }
                
                // Track conversation history
                conversationHistory.push({
                    query: userQuery,
                    result: aiResponse,
                    timestamp: data.timestamp
                });
            });
            
            eventSource.addEventListener('error', function(event) {
                const data = JSON.parse(event.data);
                log(data.message, 'error');
            });
            
            eventSource.addEventListener('complete', function(event) {
                const data = JSON.parse(event.data);
                log(data.message, 'success');
                eventSource.close();
                setSendButtonState(false);
                clearInputAndFocus();
            });
            
            eventSource.onerror = function(event) {
                console.error('SSE Error:', event);
                log('❌ Streaming connection error', 'error');
                eventSource.close();
                setSendButtonState(false);
                clearInputAndFocus();
            };
            
        } else {
            log('❌ Failed to start streaming session', 'error');
            setSendButtonState(false);
            clearInputAndFocus();
        }
        
    } catch (error) {
        log(`❌ Streaming failed: ${error.message}`, 'error');
        setAiButtonState(false);
        clearInputAndFocus();
    }
}

async function newSession() {
    log('🆕 Starting new session...', 'warning');
    
    try {
        const response = await fetch('/demo/mcp-assistant/reset', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        await response.json();
        log(`✅ New session started - all caches cleared`, 'success');
        
        // Full reset including local state
        conversationHistory = [];
        
        // Clear activity log and reset
        clearLog();
        
        // Clear user input
        const userQueryEl = document.getElementById('userQuery');
        if (userQueryEl) userQueryEl.value = '';
        
    } catch (error) {
        log(`❌ New session failed: ${error.message}`, 'error');
    }
}

async function resetSession() {
    log('🔄 Clearing conversation history (keeping tools cached)...', 'info');
    
    // Only clear conversation, keep tools cache for efficiency
    conversationHistory = [];
    
    // Just clear the conversation visually
    clearLog();
    log('✅ Conversation history cleared. Tools remain cached for faster responses.', 'success');
    
    // Clear user input
    const userQueryEl = document.getElementById('userQuery');
    if (userQueryEl) userQueryEl.value = '';
}



// Initialize the demo
document.addEventListener('DOMContentLoaded', function() {
    log('🚀 AI Assistant ready!', 'info');
    log('💬 Session-aware conversation - I remember what we talk about', 'info');
    log('💡 Try the example buttons or ask me anything!', 'warning');
    setupUserQueryInput();
});