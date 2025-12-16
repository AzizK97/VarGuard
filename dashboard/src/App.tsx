import { useState, useEffect } from 'react';
import StatisticsCards from './components/StatisticsCards';
import AlertList from './components/AlertList';
import NetworkScan from './components/NetworkScan';
import { suricataApi, type TodayStatistics } from './services/api';
import type { Alert } from './types';
import './App.css';

function App() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [statistics, setStatistics] = useState<TodayStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [connected, setConnected] = useState(false);
  const [lastUpdate, setLastUpdate] = useState<Date>(new Date());

  // Fetch initial data
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [alertsData, statsData] = await Promise.all([
          suricataApi.getRecentAlerts(50),
          suricataApi.getTodayStatistics(),
        ]);
        setAlerts(alertsData);
        setStatistics(statsData);
        setLastUpdate(new Date());
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Setup SSE for real-time alerts
  useEffect(() => {
    const eventSource = suricataApi.createAlertStream(
      (newAlert) => {
        setAlerts((prev) => [newAlert, ...prev].slice(0, 50));
        setLastUpdate(new Date());
        setConnected(true);
      },
      (error) => {
        console.error('SSE error:', error);
        setConnected(false);
      }
    );

    setConnected(true);

    return () => {
      eventSource.close();
      setConnected(false);
    };
  }, []);

  // Refresh statistics periodically
  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const statsData = await suricataApi.getTodayStatistics();
        setStatistics(statsData);
      } catch (error) {
        console.error('Error refreshing statistics:', error);
      }
    }, 30000); // Refresh every 30 seconds

    return () => clearInterval(interval);
  }, []);

  const handleAlertClick = (alert: Alert) => {
    console.log('Alert clicked:', alert);
    // You can implement a modal or detail view here
  };

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-content">
          <div className="header-left">
            <h1>🛡️ Network Security Monitor</h1>
            <p className="subtitle">Real-time Intrusion Detection System</p>
          </div>
          <div className="header-right">
            <div className={`connection-status ${connected ? 'connected' : 'disconnected'}`}>
              <span className="status-dot"></span>
              {connected ? 'Live' : 'Disconnected'}
            </div>
            <div className="last-update">
              Last update: {lastUpdate.toLocaleTimeString()}
            </div>
          </div>
        </div>
      </header>

      <main className="app-main">
        <section className="statistics-section">
          <StatisticsCards statistics={statistics} loading={loading} />
          <NetworkScan />
        </section>

        <section className="alerts-section">
          <AlertList
            alerts={alerts}
            loading={loading}
            onAlertClick={handleAlertClick}
          />
        </section>
      </main>

      <footer className="app-footer">
        <p>Powered by Suricata IDS • Built with React + Vite + TypeScript</p>
      </footer>
    </div>
  );
}

export default App;
