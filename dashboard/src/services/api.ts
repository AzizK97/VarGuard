import axios from 'axios';
import type { Alert, AlertStatistics, AlertSeverity } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const suricataApi = {
    // Get recent alerts
    getRecentAlerts: async (limit: number = 100): Promise<Alert[]> => {
        const response = await api.get<Alert[]>(`/suricata/alerts/recent`, {
            params: { limit },
        });
        return response.data;
    },

    // Get paginated alerts
    getAlerts: async (page: number = 0, size: number = 20) => {
        const response = await api.get(`/suricata/alerts`, {
            params: { page, size },
        });
        return response.data;
    },

    // Get alert by ID
    getAlertById: async (id: number): Promise<Alert> => {
        const response = await api.get<Alert>(`/suricata/alerts/${id}`);
        return response.data;
    },

    // Get alerts by severity
    getAlertsBySeverity: async (severity: AlertSeverity): Promise<Alert[]> => {
        const response = await api.get<Alert[]>(`/suricata/alerts/severity/${severity}`);
        return response.data;
    },

    // Get alerts by IP
    getAlertsByIp: async (ipAddress: string): Promise<Alert[]> => {
        const response = await api.get<Alert[]>(`/suricata/alerts/ip/${ipAddress}`);
        return response.data;
    },

    // Get statistics
    getStatistics: async (since?: string): Promise<AlertStatistics> => {
        const response = await api.get<AlertStatistics>('/suricata/statistics', {
            params: since ? { since } : {},
        });
        return response.data;
    },

    // Create SSE connection for real-time alerts
    createAlertStream: (onAlert: (alert: Alert) => void, onError?: (error: Event) => void) => {
        const eventSource = new EventSource(`${API_BASE_URL}/suricata/alerts/stream`);

        eventSource.addEventListener('alert', (event) => {
            try {
                const alert = JSON.parse(event.data) as Alert;
                onAlert(alert);
            } catch (error) {
                console.error('Error parsing alert:', error);
            }
        });

        eventSource.onerror = (error) => {
            console.error('SSE connection error:', error);
            if (onError) onError(error);
        };

        return eventSource;
    },
    // Nmap scan endpoints (backend must implement these or use mock backend)
    getNmapResults: async (targetIp: string): Promise<any> => {
        const response = await api.get(`/network/nmap/results`, { params: { target: targetIp } });
        return response.data;
    },

    runNmapScan: async (targetIp: string): Promise<any> => {
        const response = await api.post(`/network/nmap/scan`, { target: targetIp });
        return response.data;
    },

    // Get potential vulnerabilities for a target (optional)
    getVulnerabilities: async (targetIp: string): Promise<any> => {
        const response = await api.get(`/network/vulnerabilities`, { params: { target: targetIp } });
        return response.data;
    },
};
