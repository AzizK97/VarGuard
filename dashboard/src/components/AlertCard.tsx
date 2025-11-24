import React from 'react';
import type { Alert } from '../types';
import './AlertCard.css';

interface AlertCardProps {
    alert: Alert;
    onClick?: () => void;
}

const AlertCard: React.FC<AlertCardProps> = ({ alert, onClick }) => {
    const getSeverityClass = (severity: string) => {
        return `severity-${severity.toLowerCase()}`;
    };

    const formatTime = (timestamp: string) => {
        return new Date(timestamp).toLocaleString();
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
                {alert.protocol && (
                    <div className="detail-row">
                        <span className="label">Protocol:</span>
                        <span className="value">{alert.protocol}</span>
                    </div>
                )}
                {alert.category && (
                    <div className="detail-row">
                        <span className="label">Category:</span>
                        <span className="value">{alert.category}</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AlertCard;
