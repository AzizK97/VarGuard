import React from 'react';
import type { TodayStatistics } from '../services/api';
import './StatisticsCards.css';

interface StatisticsCardsProps {
    statistics: TodayStatistics | null;
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
            label: 'Total Alerts (All Time)',
            value: statistics.globalTotal,
            color: '#007bff',
            icon: '📉',
        },
        {
            label: 'Today\'s Traffic',
            value: statistics.totalAlerts,
            color: '#6c757d',
            icon: '📊',
        },
        {
            label: 'Critical (Today)',
            value: statistics.criticalAlerts,
            color: '#dc3545',
            icon: '🔴',
        },
        {
            label: 'High (Today)',
            value: statistics.highAlerts,
            color: '#fd7e14',
            icon: '🟠',
        },
        {
            label: 'Medium (Today)',
            value: statistics.mediumAlerts,
            color: '#ffc107',
            icon: '🟡',
        },
        {
            label: 'Low (Today)',
            value: statistics.lowAlerts,
            color: '#28a745',
            icon: '🟢',
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
