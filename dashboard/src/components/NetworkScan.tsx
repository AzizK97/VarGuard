import React, { useState } from 'react';
import { suricataApi } from '../services/api';
import type { NmapScanResult, Vulnerability } from '../types';
import './NetworkScan.css';

const NetworkScan: React.FC = () => {
    const [target, setTarget] = useState<string>('127.0.0.1');
    const [loading, setLoading] = useState(false);
    const [scanResult, setScanResult] = useState<NmapScanResult | null>(null);
    const [vulns, setVulns] = useState<Vulnerability[] | null>(null);
    const [error, setError] = useState<string | null>(null);

    const runScan = async () => {
        setError(null);
        setLoading(true);
        setScanResult(null);
        setVulns(null);
        try {
            // Trigger scan (backend might perform nmap or return cached results)
            await suricataApi.runNmapScan(target);
            // Fetch results
            const res = await suricataApi.getNmapResults(target);
            setScanResult(res as NmapScanResult);
            // Try to fetch vulnerabilities (optional endpoint)
            try {
                const v = await suricataApi.getVulnerabilities(target);
                setVulns(v as Vulnerability[]);
            } catch (vErr) {
                // not fatal
                console.warn('Vuln fetch failed', vErr);
            }
        } catch (err: any) {
            console.error('Scan error', err);
            setError(err?.message || 'Failed to run scan');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="network-scan-card">
            <div className="network-scan-header">
                <h3>Network Scan</h3>
                <p className="small">Run an nmap scan to discover open ports and potential vulnerabilities</p>
            </div>

            <div className="network-scan-controls">
                <input value={target} onChange={(e) => setTarget(e.target.value)} />
                <button onClick={runScan} disabled={loading}>
                    {loading ? 'Scanning…' : 'Run Scan'}
                </button>
            </div>

            {error && <div className="scan-error">Error: {error}</div>}

            {scanResult && (
                <div className="scan-results">
                    <div className="scan-meta">Scanned: {new Date(scanResult.scannedAt).toLocaleString()}</div>
                    <div className="ports-list">
                        <h4>Discovered Ports</h4>
                        {scanResult.ports.length === 0 && <div className="no-ports">No open ports found</div>}
                        <ul>
                            {scanResult.ports.map((p) => (
                                <li key={`${p.port}/${p.protocol}`} className={p.state === 'open' ? 'port-open' : ''}>
                                    <strong>{p.port}/{p.protocol}</strong> — {p.state}{p.service ? ` — ${p.service}` : ''}
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}

            {vulns && vulns.length > 0 && (
                <div className="vuln-list">
                    <h4>Potential Vulnerabilities</h4>
                    <ul>
                        {vulns.map((v, i) => (
                            <li key={v.id || i} className={`vuln-${v.severity?.toLowerCase()}`}>
                                <div className="vuln-title">{v.name} {v.severity && <span className="vuln-severity">{v.severity}</span>}</div>
                                <div className="vuln-desc">{v.description}</div>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
};

export default NetworkScan;
