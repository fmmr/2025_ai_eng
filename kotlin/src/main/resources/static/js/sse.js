/**
 * Unified SSE Manager - Simple and consistent across all demos
 */
class SSEManager {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
        this.eventSource = null;
        this.currentSessionId = null;
        this.isReconnecting = false;
    }

    // Simple message logging with color types
    log(message, type = 'info') {
        const console = document.getElementById('sseConsole');
        if (!console) return;

        const timestamp = new Date().toLocaleTimeString();
        const entry = document.createElement('div');
        
        // Message type styling
        if (type === 'user_question') {
            entry.className = 'mb-3 p-2 border-start border-primary border-3 bg-light';
            entry.innerHTML = message;
        } else if (type === 'final_result') {
            entry.className = 'mb-3 p-2 border-start border-success border-3 bg-light';
            entry.innerHTML = message;
        } else {
            // Standard colored messages with timestamp
            const colorClass = this.getColorClass(type);
            entry.className = `mb-1 ${colorClass}`;
            entry.innerHTML = `<small>[${timestamp}]</small> ${message}`;
        }
        
        console.appendChild(entry);
        console.scrollTop = console.scrollHeight;
    }

    getColorClass(type) {
        const colors = {
            'error': 'text-danger',
            'success': 'text-success', 
            'progress': 'text-primary',
            'info': 'text-dark',
            'warning': 'text-warning'
        };
        return colors[type] || 'text-muted';
    }

    // Connect to SSE stream
    connect(urlParams = '') {
        if (this.eventSource) {
            this.eventSource.close();
        }
        
        this.currentSessionId = Date.now().toString();
        const url = `${this.baseUrl}/stream/${this.currentSessionId}${urlParams}`;
        this.eventSource = new EventSource(url);
        
        // Auto-parse JSON or use plain text
        const parseData = (data) => {
            try { return JSON.parse(data); } 
            catch { return data; }
        };

        // Handle all event types automatically
        ['connected', 'start', 'progress', 'info', 'success', 'error', 'result', 'complete'].forEach(eventType => {
            this.eventSource.addEventListener(eventType, (e) => {
                const data = parseData(e.data);
                const message = typeof data === 'object' ? data.message : data;
                
                // Special handling for result events
                if (eventType === 'result') {
                    const finalAnswer = data.data?.response || message;
                    if (finalAnswer) {
                        this.log(finalAnswer.replace(/^.*Final Result:\s*/, ''), 'final_result');
                    }
                } else if (eventType === 'complete') {
                    this.log(message, 'success');
                    
                    // Let individual demos handle their own completion logic
                    if (window.onSSEComplete) {
                        window.onSSEComplete();
                    }
                } else {
                    // Standard message logging
                    this.log(message, eventType === 'connected' ? 'info' : eventType);
                }
            });
        });

        this.eventSource.onerror = () => {
            if (!this.isReconnecting) {
                this.log('❌ Connection lost, reconnecting...', 'error');
                this.reconnect();
            }
        };

        return this.currentSessionId;
    }

    reconnect() {
        if (this.isReconnecting) return;
        this.isReconnecting = true;
        setTimeout(() => {
            this.connect();
            this.isReconnecting = false;
        }, 2000);
    }

    // Send POST request with sessionId
    sendRequest(endpoint, data = {}) {
        if (!this.currentSessionId) {
            this.connect();
            setTimeout(() => this.sendRequest(endpoint, data), 1000);
            return;
        }

        return fetch(`${this.baseUrl}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ ...data, sessionId: this.currentSessionId })
        }).then(response => response.json());
    }

    close() {
        if (this.eventSource) {
            this.eventSource.close();
            this.eventSource = null;
        }
        this.currentSessionId = null;
    }
}

// Global functions for templates
function clearConsole() {
    const console = document.getElementById('sseConsole');
    if (console) {
        console.innerHTML = '<div class="text-muted">Console cleared...</div>';
    }
}

function copyConsole() {
    const console = document.getElementById('sseConsole');
    if (!console) return;
    
    const textContent = console.innerText;
    
    // Check if clipboard API is available (requires HTTPS)
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(textContent).then(() => {
            showCopyFeedback();
        }).catch(err => {
            console.error('Clipboard API failed: ', err);
            fallbackCopy(textContent);
        });
    } else {
        // Fallback for HTTP or older browsers
        fallbackCopy(textContent);
    }
}

function fallbackCopy(text) {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    
    try {
        document.execCommand('copy');
        showCopyFeedback();
    } catch (err) {
        console.error('Fallback copy failed: ', err);
        alert('Copy failed. Please select and copy the text manually.');
    } finally {
        document.body.removeChild(textarea);
    }
}

function showCopyFeedback() {
    const copyBtn = event.target;
    const originalText = copyBtn.innerHTML;
    copyBtn.innerHTML = '✅ Copied!';
    copyBtn.disabled = true;
    
    setTimeout(() => {
        copyBtn.innerHTML = originalText;
        copyBtn.disabled = false;
    }, 2000);
}

// Simple factory function
function createSSE(baseUrl) {
    return new SSEManager(baseUrl);
}