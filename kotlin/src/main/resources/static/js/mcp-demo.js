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

function testCompanyInfo() {
    const selectedTicker = document.getElementById('tickerSelect').value;
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 4,
        method: "tools/call",
        params: {
            name: "get_company_info",
            arguments: {
                symbol: selectedTicker
            }
        }
    });
}

function testStockPrice() {
    const selectedTicker = document.getElementById('tickerSelect').value;
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 5,
        method: "tools/call",
        params: {
            name: "get_stock_price",
            arguments: {
                symbol: selectedTicker
            }
        }
    });
}

function testWeatherNowcast() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 6,
        method: "tools/call",
        params: {
            name: "get_weather_nowcast",
            arguments: {
                latitude: "59.9139",
                longitude: "10.7522"
            }
        }
    });
}

function testWeatherForecast() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 7,
        method: "tools/call",
        params: {
            name: "get_weather_forecast",
            arguments: {
                latitude: "35.6762",
                longitude: "139.6503"
            }
        }
    });
}

function testLocationTool() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 8,
        method: "tools/call",
        params: {
            name: "get_location_from_ip",
            arguments: {
                ip: "8.8.8.8"
            }
        }
    });
}

function testRandomQuote() {
    sendMcpRequest({
        jsonrpc: "2.0",
        id: 9,
        method: "tools/call",
        params: {
            name: "get_random_quote",
            arguments: {}
        }
    });
}