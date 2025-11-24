import React from 'react';
import type { AlertStatistics } from '../types';
import './StatisticsCards.css';

interface StatisticsCardsProps {
    statistics: AlertStatistics | null;
    loading?: boolean;
}

const StatisticsCards: React.FC<StatisticsCardsProps> = ({ statistics, loading }) => {
    if (loading || !statistics) {
        return (
            <div className="statistics-grid">
                {[1, 2, 3, 4].map((i) => (
                    <div key={i} className="stat-card loading">
                        <div className="stat-skeleton"></div>
                    </div>
                ))}
            </div>
        );
    }

    const stats = [
        {
            label: 'Total Alerts',
            value: statistics.totalAlerts,
            color: '#6c757d',
            icon: '📊',
        },
        {
            label: 'Critical',
            value: statistics.criticalAlerts,
            color: '#dc3545',
            icon: '🔴',
        },
        {
            label: 'High',
            value: statistics.highAlerts,
            color: '#fd7e14',
            icon: '🟠',
        },
        {
            label: 'Medium',
            value: statistics.mediumAlerts,
            color: '#ffc107',
            icon: '🟡',
        },
        {
            label: 'Low',
            value: statistics.lowAlerts,
            color: '#28a745',
            icon: '🟢',
        },
        {
            label: 'Last Hour',
            value: statistics.alertsLastHour,
            color: '#17a2b8',
            icon: '⏱️',
        },
        {
            label: 'Last 24h',
            value: statistics.alertsLast24Hours,
            color: '#6610f2',
            icon: '📅',
        },
        {
            label: 'Last 7 Days',
            value: statistics.alertsLast7Days,
            color: '#e83e8c',
            icon: '📆',
        },
    ];

    return (
        <div className="statistics-grid">
            {stats.map((stat) => (
                <div key={stat.label} className="stat-card" style={{ borderTopColor: stat.color }}>
                    <div className="stat-icon">{stat.icon}</div>
                    <div className="stat-content">
                        <div className="stat-value">{stat.value.toLocaleString()}</div>
                        <div className="stat-label">{stat.label}</div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default StatisticsCards;
