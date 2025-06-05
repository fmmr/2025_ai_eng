// AI Assistant Demo JavaScript
let conversationHistory = [];

function log(message, type = 'info') {
    const activityLog = document.getElementById('activityLog');
    const timestamp = new Date().toLocaleTimeString();
    const typeColors = {
        'info': 'text-primary',
        'success': 'text-success', 
        'error': 'text-danger',
        'warning': 'text-warning',
        'request': 'text-info',
        'response': 'text-success'
    };
    
    const entry = document.createElement('div');
    entry.className = `mb-2 ${typeColors[type] || 'text-muted'}`;
    entry.innerHTML = `<small class="text-muted">[${timestamp}]</small> ${message}`;
    
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
                aiAssisted();
            }
        });
    }
}

function setAiButtonState(isProcessing) {
    const aiBtn = document.getElementById('aiBtn');
    const userQuery = document.getElementById('userQuery');
    
    if (isProcessing) {
        aiBtn.innerHTML = '⏳ Processing...';
        aiBtn.disabled = true;
        aiBtn.className = 'btn btn-secondary mb-3';
        userQuery.disabled = true;
    } else {
        aiBtn.innerHTML = '🤖 Send Message';
        aiBtn.disabled = false;
        aiBtn.className = 'btn btn-primary mb-3';
        userQuery.disabled = false;
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


async function aiAssisted() {
    const userQuery = document.getElementById('userQuery').value.trim();
    if (!userQuery) {
        log('❌ Please enter a question or request first!', 'error');
        return;
    }
    
    // Set button to processing state
    setAiButtonState(true);
    
    // AI assistant can work without manual tool discovery - it handles connection internally
    
    log(`🤖 AI analyzing request: "${userQuery}"`, 'info');
    
    try {
        const response = await fetch('/demo/mcp-assistant/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                query: userQuery
            })
        });
        
        const result = await response.json();
        
        if (result.error) {
            log(`❌ AI Error: ${result.error}`, 'error');
            setAiButtonState(false);
            clearInputAndFocus();
            return;
        }
        
        if (result.status === 'no_tool_needed') {
            log(`💭 AI Response: ${result.response}`, 'info');
            setAiButtonState(false);
            clearInputAndFocus();
            return;
        }
        
        if (result.status === 'success') {
            log(`🎯 ${result.reasoning}`, 'success');
            
            // Show multi-step tool execution details
            if (result.selectedTool && result.selectedTool.includes('→')) {
                const tools = result.selectedTool.split(' → ');
                log(`🔄 Multi-step execution: ${tools.length} tools used`, 'info');
                tools.forEach((tool, index) => {
                    log(`  Step ${index + 1}: ${tool}`, 'info');
                });
            } else if (result.selectedTool) {
                log(`🛠️ Tool used: ${result.selectedTool}`, 'info');
            }
            
            log(`✅ Final Result: ${result.response}`, 'success');
            log(`💬 Session: Conversation context maintained (${conversationHistory.length + 1} messages)`, 'info');
            
            // Tool already executed by MCP client - no need to execute again
            conversationHistory.push({
                query: userQuery,
                tool: result.selectedTool,
                result: result.response,
                timestamp: new Date().toLocaleTimeString()
            });
        }
        
        // Reset button state and clear input for next query
        setAiButtonState(false);
        clearInputAndFocus();
        
    } catch (error) {
        log(`❌ AI assistance failed: ${error.message}`, 'error');
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