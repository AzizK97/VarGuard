const express = require('express');
const cors = require('cors');
const fs = require('fs');
const readline = require('readline');
const path = require('path');

const app = express();
const port = 4000;

app.use(cors());
app.use(express.json());

// Keep Nmap mock endpoints for compatibility
const store = {};
app.post('/network/nmap/scan', (req, res) => {
  const { target } = req.body || req.query || {};
  if (!target) return res.status(400).json({ error: 'target required' });
  const scannedAt = new Date().toISOString();
  const result = {
    targetIp: target,
    scannedAt,
    ports: [
      { port: 22, protocol: 'tcp', state: 'open', service: 'ssh' },
      { port: 80, protocol: 'tcp', state: 'open', service: 'http' },
      { port: 443, protocol: 'tcp', state: 'open', service: 'https' }
    ],
    raw: `Nmap scan simulated for ${target} at ${scannedAt}`
  };
  store[target] = {
    result, vulns: []
  };
  return res.json({ status: 'started', target });
});

app.get('/network/nmap/results', (req, res) => {
  const target = req.query.target;
  if (!target) return res.status(400).json({ error: 'target query param required' });
  const entry = store[target];
  if (!entry) {
    return res.json({ targetIp: target, scannedAt: new Date().toISOString(), ports: [] });
  }
  return res.json(entry.result);
});

app.get('/network/vulnerabilities', (req, res) => {
  return res.json([]);
});

// --- Real Suricata Log Integration ---

const LOG_FILE_PATH = '/var/log/suricata/eve.json';

// Helper to read logs
async function readLogs(limit = 100) {
  if (!fs.existsSync(LOG_FILE_PATH)) {
    console.log('Log file not found:', LOG_FILE_PATH);
    return [];
  }

  const fileStream = fs.createReadStream(LOG_FILE_PATH);
  const rl = readline.createInterface({
    input: fileStream,
    crlfDelay: Infinity
  });

  const alerts = [];
  for await (const line of rl) {
    try {
      const event = JSON.parse(line);
      if (event.event_type === 'alert') {
        alerts.push(transformAlert(event));
      }
    } catch (e) {
      console.error('Error parsing log line:', e);
    }
  }

  // Return last N alerts, reversed (newest first)
  return alerts.slice(-limit).reverse();
}

// Transform Suricata alert to Dashboard format
function transformAlert(event) {
  return {
    id: new Date(event.timestamp).getTime() + Math.random(), // Ensure unique ID
    timestamp: event.timestamp,
    sourceIp: event.src_ip,
    destIp: event.dest_ip,
    sourcePort: event.src_port,
    destPort: event.dest_port,
    protocol: event.proto,
    signature: event.alert.signature,
    category: event.alert.category || 'Unknown',
    severity: mapSeverity(event.alert.severity),
    payload: event.payload_printable || ''
  };
}

function mapSeverity(severity) {
  // Suricata severity: 1 (High) to 3 (Low)
  switch (severity) {
    case 1: return 'CRITICAL';
    case 2: return 'MEDIUM';
    case 3: return 'LOW';
    default: return 'LOW';
  }
}

// API Endpoints

app.get('/suricata/alerts/recent', async (req, res) => {
  const limit = parseInt(req.query.limit) || 50;
  const alerts = await readLogs(limit);
  res.json(alerts);
});

app.get('/suricata/statistics', async (req, res) => {
  const alerts = await readLogs(10000); // Read last 10k for stats

  const stats = {
    totalAlerts: alerts.length,
    criticalAlerts: alerts.filter(a => a.severity === 'CRITICAL').length,
    highAlerts: alerts.filter(a => a.severity === 'HIGH').length,
    mediumAlerts: alerts.filter(a => a.severity === 'MEDIUM').length,
    lowAlerts: alerts.filter(a => a.severity === 'LOW').length,
    alertsLastHour: alerts.filter(a => new Date(a.timestamp) > new Date(Date.now() - 3600000)).length,
    alertsLast24Hours: alerts.filter(a => new Date(a.timestamp) > new Date(Date.now() - 86400000)).length,
    alertsLast7Days: alerts.filter(a => new Date(a.timestamp) > new Date(Date.now() - 7 * 86400000)).length,
    alertsByCategory: {},
    topSourceIps: {},
    topDestIps: {},
    topSignatures: {}
  };

  // Calculate aggregations
  alerts.forEach(alert => {
    // By Category
    stats.alertsByCategory[alert.category] = (stats.alertsByCategory[alert.category] || 0) + 1;

    // Top Source IPs
    stats.topSourceIps[alert.sourceIp] = (stats.topSourceIps[alert.sourceIp] || 0) + 1;

    // Top Dest IPs
    stats.topDestIps[alert.destIp] = (stats.topDestIps[alert.destIp] || 0) + 1;

    // Top Signatures
    stats.topSignatures[alert.signature] = (stats.topSignatures[alert.signature] || 0) + 1;
  });

  res.json(stats);
});

app.get('/suricata/alerts/stream', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  if (!fs.existsSync(LOG_FILE_PATH)) {
    res.write(`event: error\ndata: Log file not found\n\n`);
    // Don't return, keep connection open and retry? Or just wait for file to appear?
    // For now, let's just wait.
  }

  console.log('Client connected to SSE stream');

  // Send heartbeat to keep connection alive
  const heartbeat = setInterval(() => {
    res.write(': keep-alive\n\n');
  }, 15000);

  // Watch file for changes
  let currentSize = 0;
  if (fs.existsSync(LOG_FILE_PATH)) {
    currentSize = fs.statSync(LOG_FILE_PATH).size;
  }

  const watcher = fs.watchFile(LOG_FILE_PATH, { interval: 1000 }, (curr, prev) => {
    if (curr.size > prev.size) {
      const stream = fs.createReadStream(LOG_FILE_PATH, {
        start: prev.size,
        end: curr.size
      });

      const rl = readline.createInterface({
        input: stream,
        crlfDelay: Infinity
      });

      rl.on('line', (line) => {
        try {
          if (!line.trim()) return;
          const event = JSON.parse(line);
          if (event.event_type === 'alert') {
            const alert = transformAlert(event);
            res.write(`event: alert\n`);
            res.write(`data: ${JSON.stringify(alert)}\n\n`);
          }
        } catch (e) {
          console.error('Error parsing stream line:', e);
        }
      });
    }
  });

  req.on('close', () => {
    console.log('Client disconnected from SSE stream');
    clearInterval(heartbeat);
    fs.unwatchFile(LOG_FILE_PATH);
    res.end();
  });
});

app.listen(port, () => {
  console.log(`Real backend listening on http://localhost:${port}`);
  console.log(`Monitoring log file: ${LOG_FILE_PATH}`);
});
