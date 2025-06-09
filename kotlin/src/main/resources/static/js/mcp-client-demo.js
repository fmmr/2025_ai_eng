// MCP Client Demo JavaScript
let currentStep = 1;
let availableTools = [];
let serverConnected = false;
let selectedTool = null;
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
        '<div class="text-muted">🔗 Follow the 3 steps to learn MCP protocol...</div>';
}

// Removed AI-related functions - this is educational MCP protocol demo only

function updateStep(step) {
    currentStep = step;
    
    // Update button states based on step progression
    const connectBtn = document.getElementById('connectBtn');
    const discoverBtn = document.getElementById('discoverBtn');
    const testBtn = document.getElementById('testBtn');
    
    if (connectBtn) connectBtn.disabled = false;
    if (discoverBtn) discoverBtn.disabled = step < 2;
    if (testBtn) testBtn.disabled = step < 3;
    
    // Update learning steps visual
    const steps = document.querySelectorAll('#learningSteps li');
    steps.forEach((stepEl, index) => {
        if (index < step - 1) {
            stepEl.style.color = 'green';
            stepEl.innerHTML = stepEl.innerHTML.replace(/^[🔌🛠️📞]/, '✅');
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
            log(`❌ Connection failed: ${initResponse.error.message}`, 'error');
            return;
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
            log(`❌ Tool discovery failed: ${toolsResponse.error.message}`, 'error');
            return;
        }
        
        availableTools = toolsResponse.result.tools || [];
        log(`✅ Found ${availableTools.length} tools:`, 'success');
        
        availableTools.forEach((tool, index) => {
            const toolLink = document.createElement('span');
            toolLink.innerHTML = `  ${index + 1}. <a href="#" onclick="showToolParameterForm(availableTools[${index}]); return false;" class="text-decoration-none">${tool.name}</a> - ${tool.description}`;
            toolLink.className = 'text-info d-block';
            
            const logEntry = document.createElement('div');
            logEntry.className = 'mb-1 text-info';
            logEntry.innerHTML = `<small class="text-muted">[${new Date().toLocaleTimeString()}]</small> `;
            logEntry.appendChild(toolLink);
            
            document.getElementById('activityLog').appendChild(logEntry);
        });
        
        document.getElementById('activityLog').scrollTop = document.getElementById('activityLog').scrollHeight;
        log('💡 Click any tool name above to test it with custom parameters!', 'warning');
        
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
            log(`❌ Tool call failed: ${callResponse.error.message}`, 'error');
            return;
        }
        
        const result = callResponse.result.content[0].text;
        log(`✅ Tool result: ${result}`, 'success');
        log('🎓 Protocol learning complete! You now understand MCP basics.', 'success');
        
    } catch (error) {
        log(`❌ Tool call failed: ${error.message}`, 'error');
    }
}

// AI assistant functions removed - this is educational MCP protocol demo only

function getDefaultParameterValue(toolName, paramName) {
    // Use testParams from Tools enum via server-provided data
    if (window.toolDefaults && window.toolDefaults[toolName]) {
        return window.toolDefaults[toolName][paramName] || null;
    }
    return null;
}

function showToolParameterForm(tool) {
    selectedTool = tool;
    const toolParamsDiv = document.getElementById('toolParams');
    const paramInputsDiv = document.getElementById('paramInputs');
    
    // Clear previous inputs
    paramInputsDiv.innerHTML = '';
    
    const properties = tool.inputSchema?.properties || {};
    
    if (Object.keys(properties).length === 0) {
        paramInputsDiv.innerHTML = '<p class="text-muted">This tool requires no parameters.</p>';
    } else {
        Object.entries(properties).forEach(([paramName, paramDef]) => {
            const inputGroup = document.createElement('div');
            inputGroup.className = 'mb-2';
            
            const label = document.createElement('label');
            label.className = 'form-label';
            label.textContent = `${paramName} (${paramDef.type})`;
            
            const input = document.createElement('input');
            input.type = paramDef.type === 'number' ? 'number' : 'text';
            input.className = 'form-control form-control-sm';
            input.id = `param-${paramName}`;
            input.placeholder = paramDef.description || `Enter ${paramName}`;
            
            // Add enter key handler
            input.addEventListener('keydown', function(event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    executeToolWithParams();
                }
            });
            
            inputGroup.appendChild(label);
            inputGroup.appendChild(input);
            paramInputsDiv.appendChild(inputGroup);
        });
    }
    
    toolParamsDiv.style.display = 'block';
    document.getElementById('executeBtn').disabled = false;
}

async function executeToolWithParams() {
    if (!selectedTool) return;
    
    const serverUrl = document.getElementById('serverUrl').value;
    const properties = selectedTool.inputSchema?.properties || {};
    const arguments = {};
    
    // Collect parameter values from inputs, with fallback to sensible defaults
    Object.keys(properties).forEach(paramName => {
        const input = document.getElementById(`param-${paramName}`);
        if (input && input.value) {
            arguments[paramName] = input.value;
        } else {
            // Provide default values for common parameters when user doesn't enter anything
            const defaultValue = getDefaultParameterValue(selectedTool.name, paramName);
            if (defaultValue !== null) {
                arguments[paramName] = defaultValue;
            }
        }
    });
    
    try {
        log(`🔧 Calling ${selectedTool.name} with custom parameters...`, 'info');
        
        const callResponse = await sendMcpRequest(serverUrl, {
            jsonrpc: "2.0",
            id: Math.floor(Math.random() * 1000) + 200, // Small random ID
            method: "tools/call",
            params: {
                name: selectedTool.name,
                arguments: arguments
            }
        });
        
        if (callResponse.error) {
            log(`❌ Custom tool execution failed: ${callResponse.error.message}`, 'error');
            return;
        }
        
        const result = callResponse.result.content[0].text;
        log(`✅ Custom tool result: ${result}`, 'success');
        
    } catch (error) {
        log(`❌ Custom tool execution failed: ${error.message}`, 'error');
    }
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
    log('🚀 MCP Protocol Demo initialized', 'info');
    log('📚 Educational focus on JSON-RPC 2.0 fundamentals', 'info');
    log('💡 Follow the 3 steps to understand MCP protocol basics', 'warning');
    updateStep(1);
});