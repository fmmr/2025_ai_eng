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
        '<div class="text-muted">🎯 Click "Connect to Server" to begin...</div>';
}

function updateStep(step) {
    currentStep = step;
    
    // Update button states
    document.getElementById('connectBtn').disabled = step > 1;
    document.getElementById('discoverBtn').disabled = step < 2;
    document.getElementById('testBtn').disabled = step < 3;
    document.getElementById('aiBtn').disabled = step < 4;
    
    // Show AI input section when step 4 is reached
    if (step >= 4) {
        document.getElementById('aiSection').style.display = 'block';
    }
    
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
    const userQuery = document.getElementById('userQuery').value.trim();
    if (!userQuery) {
        log('❌ Please enter a question or request first!', 'error');
        return;
    }
    
    if (availableTools.length === 0) {
        log('❌ Must discover tools first!', 'error');
        return;
    }
    
    log(`🤖 AI analyzing request: "${userQuery}"`, 'info');
    
    try {
        const response = await fetch('/demo/mcp-client/ai-assist', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                query: userQuery,
                tools: availableTools
            })
        });
        
        const result = await response.json();
        
        if (result.error) {
            log(`❌ AI Error: ${result.error}`, 'error');
            return;
        }
        
        if (result.status === 'no_tool_needed') {
            log(`💭 AI Response: ${result.response}`, 'info');
            return;
        }
        
        if (result.status === 'success') {
            log(`🎯 ${result.reasoning}`, 'success');
            log(`🛠️ Selected tool: ${result.selectedTool}`, 'info');
            log(`✅ AI Result: ${result.response}`, 'success');
            
            // Tool already executed by MCP client - no need to execute again
            conversationHistory.push({
                query: userQuery,
                tool: result.selectedTool,
                result: result.response,
                timestamp: new Date().toLocaleTimeString()
            });
            
            // Clear the input for next query
            document.getElementById('userQuery').value = '';
        }
        
    } catch (error) {
        log(`❌ AI assistance failed: ${error.message}`, 'error');
    }
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
    
    // Collect parameter values
    Object.keys(properties).forEach(paramName => {
        const input = document.getElementById(`param-${paramName}`);
        if (input && input.value) {
            arguments[paramName] = input.value;
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
            throw new Error(callResponse.error.message);
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
    log('🚀 MCP Client Demo initialized', 'info');
    log('📚 This demo teaches you step-by-step how to build an MCP client', 'info');
    updateStep(1);
});