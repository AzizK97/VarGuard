const express = require('express');
const cors = require('cors');
const app = express();
app.use(cors());
app.use(express.json());

const port = process.env.PORT || 4000;

// Simple in-memory store for results keyed by target
const store = {};

app.post('/network/nmap/scan', (req, res) => {
  const { target } = req.body || req.query || {};
  if (!target) return res.status(400).json({ error: 'target required' });
  // Simulate scan: create a simple result
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
  store[target] = { result, vulns: [
    { id: 'CVE-2020-0001', name: 'Example vuln on port 80', description: 'Sample vulnerability', severity: 'Medium', affectedPort: 80 }
  ] };
  // Simulate asynchronous scan processing
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
  const target = req.query.target;
  if (!target) return res.status(400).json({ error: 'target query param required' });
  const entry = store[target];
  if (!entry) return res.json([]);
  return res.json(entry.vulns || []);
});

app.listen(port, () => {
  console.log(`Mock backend listening on http://localhost:${port}`);
});
