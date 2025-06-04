async function sendMcpRequest(request) {
    try {
        const response = await fetch('/mcp/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        });
        
        const result = await response.text();
        document.getElementById('response').textContent = JSON.stringify(JSON.parse(result), null, 2);
    } catch (error) {
        document.getElementById('response').textContent = 'Error: ' + error.message;
    }
}

function testInitialize() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 1,
        method: "initialize",
        params: {
            protocolVersion: "2024-11-05",
            clientInfo: {
                name: "Test Client",
                version: "1.0.0"
            }
        }
    });
}

function testToolsList() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 2,
        method: "tools/list"
    });
}

function testHelloTool() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 3,
        method: "tools/call",
        params: {
            name: "hello_world",
            arguments: {
                name: "BOSS"
            }
        }
    });
}

function testStockTool() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 4,
        method: "tools/call",
        params: {
            name: "get_stock_price",
            arguments: {
                symbol: "AAPL"
            }
        }
    });
}

function testWeatherTool() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 5,
        method: "tools/call",
        params: {
            name: "get_weather",
            arguments: {
                latitude: "59.9139",
                longitude: "10.7522"
            }
        }
    });
}

function testLocationTool() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 6,
        method: "tools/call",
        params: {
            name: "get_location_from_ip",
            arguments: {
                ip: "8.8.8.8"
            }
        }
    });
}