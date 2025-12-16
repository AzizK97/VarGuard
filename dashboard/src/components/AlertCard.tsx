import React, { useState, useEffect } from 'react';
import type { Alert } from '../types';
import { aiApi } from '../services/aiApi';
import './AlertCard.css';

interface AlertCardProps {
    alert: Alert;
    onClick?: () => void;
}

const CodeBlock = ({ code }: { code: string }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = (e: React.MouseEvent) => {
        e.stopPropagation();
        navigator.clipboard.writeText(code);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="code-block-wrapper">
            <div className="code-header">
                <span className="code-lang">bash</span>
                <button className={`copy-btn ${copied ? 'copied' : ''}`} onClick={handleCopy}>
                    {copied ? '✓ Copied' : 'Copy'}
                </button>
            </div>
            <pre>
                <code>{code}</code>
            </pre>
        </div>
    );
};

const MarkdownRenderer = ({ content }: { content: string }) => {
    const lines = content.split('\n');
    const elements: React.ReactNode[] = [];
    let inCodeBlock = false;
    let codeBlockContent: string[] = [];

    lines.forEach((line, index) => {
        if (line.trim().startsWith('```')) {
            if (inCodeBlock) {
                elements.push(<CodeBlock key={`code-${index}`} code={codeBlockContent.join('\n')} />);
                codeBlockContent = [];
                inCodeBlock = false;
            } else {
                inCodeBlock = true;
            }
            return;
        }

        if (inCodeBlock) {
            codeBlockContent.push(line);
            return;
        }

        if (line.startsWith('### ')) {
            elements.push(<h3 key={`h3-${index}`}>{line.replace('### ', '')}</h3>);
        } else if (line.trim().startsWith('* ') || line.trim().startsWith('- ')) {
            const text = line.trim().substring(2);
            const parts = text.split('**');
            const itemContent = parts.map((part, i) => i % 2 === 1 ? <strong key={i}>{part}</strong> : part);
            elements.push(
                <div key={`li-${index}`} className="list-item">
                    <span className="bullet">•</span>
                    <span>{itemContent}</span>
                </div>
            );
        } else if (line.trim().match(/^\d+\. /)) {
            const match = line.trim().match(/^\d+\./);
            const number = match ? match[0] : '';
            const text = line.trim().replace(/^\d+\. /, '');
            const parts = text.split('**');
            const itemContent = parts.map((part, i) => i % 2 === 1 ? <strong key={i}>{part}</strong> : part);
            elements.push(
                <div key={`nli-${index}`} className="list-item">
                    <span className="number">{number}</span>
                    <span>{itemContent}</span>
                </div>
            );
        } else if (line.trim() !== '') {
            const parts = line.split('**');
            const pContent = parts.map((part, i) => i % 2 === 1 ? <strong key={i}>{part}</strong> : part);
            elements.push(<p key={`p-${index}`}>{pContent}</p>);
        }
    });

    return <div className="markdown-content">{elements}</div>;
};

const AlertCard: React.FC<AlertCardProps> = ({ alert, onClick }) => {
    const [analysis, setAnalysis] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [activeTab, setActiveTab] = useState<'overview' | 'remediation' | 'investigation'>('overview');

    const severityUpper = String(alert.severity).toUpperCase();

    useEffect(() => {
        if ((severityUpper === 'HIGH' || severityUpper === 'CRITICAL') && !analysis) {
            fetchAnalysis();
        }
    }, [alert.id, severityUpper]);

    const fetchAnalysis = async () => {
        setLoading(true);
        try {
            const response = await aiApi.analyzeAlert(alert.id);
            setAnalysis(response.analysis);
        } catch (error) {
            console.error("Failed to fetch AI analysis", error);
            setAnalysis("Failed to generate AI analysis. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    const getSeverityClass = (severity: string) => `severity-${severity.toLowerCase()}`;
    const formatTime = (timestamp: string) => new Date(timestamp).toLocaleString();

    // Parse analysis into sections for tabs
    const getSectionContent = () => {
        if (!analysis) return null;

        const sections = {
            overview: '',
            remediation: '',
            investigation: ''
        };

        const parts = analysis.split('### ');
        parts.forEach(part => {
            if (!part.trim()) return;
            const fullPart = '### ' + part;

            if (part.includes('🛠️') || part.includes('Remediation')) {
                sections.remediation += fullPart;
            } else if (part.includes('🔍') || part.includes('Investigation')) {
                sections.investigation += fullPart;
            } else {
                // Everything else goes to overview (Analysis, Impact, etc.)
                sections.overview += fullPart;
            }
        });

        // Fallback if parsing fails (e.g. simple error message)
        if (!sections.remediation && !sections.investigation) {
            return <MarkdownRenderer content={analysis} />;
        }

        const content = sections[activeTab];
        return content ? <MarkdownRenderer content={content} /> : <div className="empty-tab">No information available for this section.</div>;
    };

    return (
        <div className={`alert-card ${getSeverityClass(alert.severity)}`} onClick={onClick}>
            <div className="alert-header">
                <span className={`severity-badge ${getSeverityClass(alert.severity)}`}>
                    {alert.severity}
                </span>
                <span className="alert-time">{formatTime(alert.timestamp)}</span>
            </div>

            <div className="alert-signature">{alert.signature}</div>

            <div className="alert-details">
                <div className="detail-row">
                    <span className="label">Source:</span>
                    <span className="value">{alert.sourceIp}{alert.sourcePort ? `:${alert.sourcePort}` : ''}</span>
                </div>
                <div className="detail-row">
                    <span className="label">Destination:</span>
                    <span className="value">{alert.destIp}{alert.destPort ? `:${alert.destPort}` : ''}</span>
                </div>
                <div className="detail-row">
                    <span className="label">Protocol:</span>
                    <span className="value">{alert.protocol || 'N/A'}</span>
                </div>
            </div>

            {!analysis && !loading && severityUpper !== 'HIGH' && severityUpper !== 'CRITICAL' && (
                <div style={{ marginTop: '12px' }}>
                    <button
                        onClick={(e) => { e.stopPropagation(); fetchAnalysis(); }}
                        className="ai-btn"
                    >
                        <span>🤖</span> Analyze with AI
                    </button>
                </div>
            )}

            {(analysis || loading) && (
                <div className="ai-analysis-section">
                    <div className="ai-header">
                        <div className="ai-title">
                            <span>🛡️ AI Security Assistant</span>
                            {loading && <span className="loading-spinner"></span>}
                        </div>
                        {analysis && !loading && (
                            <div className="ai-tabs">
                                <button
                                    className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
                                    onClick={(e) => { e.stopPropagation(); setActiveTab('overview'); }}
                                >
                                    Overview
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'remediation' ? 'active' : ''}`}
                                    onClick={(e) => { e.stopPropagation(); setActiveTab('remediation'); }}
                                >
                                    Remediation
                                </button>
                                <button
                                    className={`tab-btn ${activeTab === 'investigation' ? 'active' : ''}`}
                                    onClick={(e) => { e.stopPropagation(); setActiveTab('investigation'); }}
                                >
                                    Investigation
                                </button>
                            </div>
                        )}
                    </div>

                    <div className="ai-body">
                        {loading ? (
                            <div className="loading-text">Analyzing threat patterns...</div>
                        ) : (
                            getSectionContent()
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default AlertCard;
