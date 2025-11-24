# Suricata IDS/IPS Integration - Backend

This document provides an overview of the Suricata IDS/IPS integration with the network security monitoring platform.

## Architecture

The integration consists of:

1. **Suricata IDS** - Monitors network traffic and generates alerts
2. **Elasticsearch** - Stores and indexes alerts for fast searching
3. **Spring Boot Backend** - Processes alerts and exposes REST APIs
4. **PostgreSQL** - Stores alert data persistently

## Components

### Data Models

- **Alert** - Main entity for Suricata alerts with fields for IPs, ports, signatures, severity, etc.
- **AlertSeverity** - Enum for severity levels (LOW, MEDIUM, HIGH, CRITICAL)
- **AlertStatistics** - DTO for dashboard metrics and analytics

### Services

- **SuricataService** - Core service for alert processing and retrieval
- **ElasticsearchService** - Handles Elasticsearch indexing and search operations
- **SuricataLogMonitor** - Background service that monitors Suricata's EVE JSON log file

### REST API Endpoints

Base URL: `/api/suricata`

- `GET /alerts` - Get paginated alerts
- `GET /alerts/recent?limit=100` - Get recent alerts
- `GET /alerts/{id}` - Get specific alert
- `GET /alerts/severity/{severity}` - Get alerts by severity
- `GET /alerts/ip/{ipAddress}` - Get alerts for specific IP
- `GET /alerts/timerange?start=...&end=...` - Get alerts in time range
- `GET /statistics?since=...` - Get dashboard statistics
- `GET /alerts/stream` - Server-Sent Events stream for real-time alerts

## Configuration

### Application Properties

```properties
# Elasticsearch
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.index.alerts=suricata-alerts

# Suricata
suricata.log.path=/var/log/suricata/eve.json
suricata.log.monitor.enabled=true
suricata.log.monitor.delay=1000

# CORS for external dashboard
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:5173
```

### Docker Compose

Services included:
- PostgreSQL (database)
- Elasticsearch (alert indexing)
- Suricata (IDS/IPS)
- Spring Boot App (backend)

## Running the System

### Using Docker Compose

```bash
cd /home/turki/Projet_fwk/scan
docker-compose up -d
```

### Building the Application

```bash
cd /home/turki/Projet_fwk/scan
./mvnw clean install
```

### Running Locally

```bash
./mvnw spring-boot:run -pl app/app-jar
```

## Testing

### Generate Test Alerts

```bash
# Trigger test alert
curl http://testmyids.com

# Run port scan to generate alerts
nmap -sS localhost
```

### Check Alerts via API

```bash
# Get recent alerts
curl http://localhost:8080/api/suricata/alerts/recent

# Get statistics
curl http://localhost:8080/api/suricata/statistics

# Stream real-time alerts
curl -N http://localhost:8080/api/suricata/alerts/stream
```

### Check Elasticsearch

```bash
# Check if index exists
curl http://localhost:9200/suricata-alerts

# Search alerts
curl http://localhost:9200/suricata-alerts/_search?pretty
```

## Dashboard Integration

The backend exposes REST APIs and SSE endpoints that can be consumed by any frontend dashboard. CORS is configured to allow connections from common development ports.

### Example SSE Client (JavaScript)

```javascript
const eventSource = new EventSource('http://localhost:8080/api/suricata/alerts/stream');

eventSource.addEventListener('alert', (event) => {
  const alert = JSON.parse(event.data);
  console.log('New alert:', alert);
  // Update UI with new alert
});
```

## Troubleshooting

### Suricata not generating alerts

1. Check if Suricata is running: `docker-compose ps`
2. Check Suricata logs: `docker-compose logs suricata`
3. Verify EVE JSON file: `docker-compose exec suricata tail -f /var/log/suricata/eve.json`

### Elasticsearch connection issues

1. Check if Elasticsearch is running: `curl http://localhost:9200`
2. Check application logs for connection errors
3. Verify network connectivity between containers

### Alerts not appearing in database

1. Check SuricataLogMonitor is enabled in application.properties
2. Verify log file path is correct
3. Check application logs for parsing errors
