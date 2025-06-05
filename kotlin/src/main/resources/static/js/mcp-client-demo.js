// MCP Client Demo JavaScript
let currentStep = 1;
let availableTools = [];
let serverConnected = false;

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
        '<div class="text-muted">🎯 Click "Connect to Server" to begin...</div>';
}

function updateStep(step) {
    currentStep = step;
    
    // Update button states
    document.getElementById('connectBtn').disabled = step > 1;
    document.getElementById('discoverBtn').disabled = step < 2;
    document.getElementById('testBtn').disabled = step < 3;
    document.getElementById('aiBtn').disabled = step < 4;
    
    // Update learning steps visual
    const steps = document.querySelectorAll('#learningSteps li');
    steps.forEach((stepEl, index) => {
        if (index < step - 1) {
            stepEl.style.color = 'green';
            stepEl.innerHTML = stepEl.innerHTML.replace(/^[🔌🛠️📞🤖]/, '✅');
        } else if (index === step - 1) {
            stepEl.style.color = 'blue';
            stepEl.style.fontWeight = 'bold';
        } else {
            stepEl.style.color = 'gray';
            stepEl.style.fontWeight = 'normal';
        }
    });
}

async function connectToServer() {
    const serverUrl = document.getElementById('serverUrl').value;
    log(`🔌 Connecting to MCP server: ${serverUrl}`, 'info');
    
    try {
        // Step 1: Initialize connection
        log('📡 Sending initialize request...', 'request');
        const initResponse = await sendMcpRequest(serverUrl, {
            jsonrpc: "2.0",
            id: 1,
            method: "initialize",
            params: {
                protocolVersion: "2024-11-05",
                clientInfo: {
                    name: "Kotlin MCP Client Demo",
                    version: "1.0.0"
                }
            }
        });
        
        if (initResponse.error) {
            throw new Error(initResponse.error.message);
        }
        
        log(`✅ Connected! Server: ${initResponse.result.serverInfo.name}`, 'success');
        log(`📋 Protocol: ${initResponse.result.protocolVersion}`, 'info');
        
        serverConnected = true;
        updateStep(2);
        
    } catch (error) {
        log(`❌ Connection failed: ${error.message}`, 'error');
    }
}

async function discoverTools() {
    if (!serverConnected) {
        log('❌ Must connect to server first!', 'error');
        return;
    }
    
    log('🔍 Discovering available tools...', 'info');
    
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        log('📡 Sending tools/list request...', 'request');
        
        const toolsResponse = await sendMcpRequest(serverUrl, {
            jsonrpc: "2.0",
            id: 2,
            method: "tools/list"
        });
        
        if (toolsResponse.error) {
            throw new Error(toolsResponse.error.message);
        }
        
        availableTools = toolsResponse.result.tools || [];
        log(`✅ Found ${availableTools.length} tools:`, 'success');
        
        availableTools.forEach((tool, index) => {
            log(`  ${index + 1}. ${tool.name} - ${tool.description}`, 'info');
        });
        
        updateStep(3);
        
    } catch (error) {
        log(`❌ Tool discovery failed: ${error.message}`, 'error');
    }
}

async function testTool() {
    if (availableTools.length === 0) {
        log('❌ Must discover tools first!', 'error');
        return;
    }
    
    // Test the hello_world tool for simplicity
    const testTool = availableTools.find(tool => tool.name === 'hello_world') || availableTools[0];
    log(`🧪 Testing tool: ${testTool.name}`, 'info');
    
    try {
        const serverUrl = document.getElementById('serverUrl').value;
        const testParams = testTool.name === 'hello_world' 
            ? { name: "BOSS" }
            : testTool.name === 'get_random_quote'
            ? {}
            : { symbol: "AAPL" };
            
        log(`📡 Calling ${testTool.name} with params: ${JSON.stringify(testParams)}`, 'request');
        
        const callResponse = await sendMcpRequest(serverUrl, {
            jsonrpc: "2.0",
            id: 3,
            method: "tools/call",
            params: {
                name: testTool.name,
                arguments: testParams
            }
        });
        
        if (callResponse.error) {
            throw new Error(callResponse.error.message);
        }
        
        const result = callResponse.result.content[0].text;
        log(`✅ Tool result: ${result}`, 'success');
        
        updateStep(4);
        
    } catch (error) {
        log(`❌ Tool call failed: ${error.message}`, 'error');
    }
}

async function aiAssisted() {
    log('🤖 AI-Assisted tool selection coming soon!', 'warning');
    log('💡 This would let AI choose which tools to call based on user intent', 'info');
    log('🎯 For now, try the working tools manually', 'info');
}

async function sendMcpRequest(serverUrl, request) {
    const response = await fetch(serverUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(request)
    });
    
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    const result = await response.json();
    log(`📥 Response: ${JSON.stringify(result, null, 2)}`, 'response');
    return result;
}

// Initialize the demo
document.addEventListener('DOMContentLoaded', function() {
    log('🚀 MCP Client Demo initialized', 'info');
    log('📚 This demo teaches you step-by-step how to build an MCP client', 'info');
    updateStep(1);
});