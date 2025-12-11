# VarGuard: Network Security Monitoring System

> AI-powered network monitoring and vulnerability scanning platform with real-time threat detection and intelligent remediation recommendations.

## Overview

This project is a comprehensive network security monitoring application that combines intrusion detection, vulnerability scanning, and AI-driven threat analysis to protect networks from attacks and security breaches.

## Key Features

- **Real-time Network Monitoring** - Continuous surveillance of network traffic and activities
- **Intrusion Detection** - Powered by Suricata IDS for advanced threat detection
- **Vulnerability Scanning** - Integrated Nmap for comprehensive network scanning
- **AI-Powered Analysis** - Machine learning algorithms that learn network patterns and predict potential threats
- **Intelligent Recommendations** - Automated suggestions for security improvements and threat remediation
- **Live Alerts** - Instant notifications via Server-Sent Events (SSE) when threats are detected
- **Pattern Learning** - Adaptive system that learns from network behavior to prevent future attacks

## Technology Stack

- **Backend**: [Your backend framework]
- **IDS Engine**: Suricata
- **Scanner**: Nmap
- **AI/ML**: [Your ML framework]
- **Real-time Communication**: Server-Sent Events (SSE)
- **Database**: [Your database]

## Getting Started

### Prerequisites

```bash
# List prerequisites here
- Python 3.x / Node.js
- Suricata IDS
- Nmap
```

### Installation

```bash
# Clone the repository
[git clone https://github.com/AzizK97/VarGuard.git](https://github.com/AzizK97/VarGuard.git)
cd VarGuard

# How to run the project
docker compose up --build
```

## Project Structure

```
├── container_logs.txt
├── dashboard
│   ├── Dockerfile
│   ├── eslint.config.js
│   ├── index.html
│   ├── nginx.conf
│   ├── package.json
│   ├── package-lock.json
│   ├── public
│   │   └── vite.svg
│   ├── README.md
│   ├── src
│   │   ├── App.css
│   │   ├── App.tsx
│   │   ├── assets
│   │   │   └── react.svg
│   │   ├── components
│   │   │   ├── AlertCard.css
│   │   │   ├── AlertCard.tsx
│   │   │   ├── AlertList.css
│   │   │   ├── AlertList.tsx
│   │   │   ├── NetworkScan.css
│   │   │   ├── NetworkScan.tsx
│   │   │   ├── StatisticsCards.css
│   │   │   └── StatisticsCards.tsx
│   │   ├── index.css
│   │   ├── main.tsx
│   │   ├── services
│   │   │   └── api.ts
│   │   └── types
│   │       └── index.ts
│   ├── tsconfig.app.json
│   ├── tsconfig.json
│   ├── tsconfig.node.json
│   └── vite.config.ts
├── docker
│   ├── Dockerfile.suricata
│   └── suricata
│       └── suricata.yaml
├── docker-compose.yml
├── full_compose_output5.log
├── projet_sem.pdf
├── README.md
├── scan
│   ├── app
│   │   ├── app-jar
│   │   │   ├── pom.xml
│   │   │   └── src
│   │   │       ├── main
│   │   │       │   ├── java
│   │   │       │   │   └── tn
│   │   │       │   │       └── rnu
│   │   │       │   │           └── eniso
│   │   │       │   │               └── fwk
│   │   │       │   │                   └── scan
│   │   │       │   │                       └── ScanApplication.java
│   │   │       │   └── resources
│   │   │       │       └── application.properties
│   │   │       └── test
│   │   │           ├── java
│   │   │           │   └── tn
│   │   │           │       └── rnu
│   │   │           │           └── eniso
│   │   │           │               └── fwk
│   │   │           │                   └── scan
│   │   │           │                       └── ScanApplicationTests.java
│   │   │           └── resources
│   │   │               └── application.properties
│   │   └── pom.xml
│   ├── compose.yaml
│   ├── core
│   │   ├── dal
│   │   │   ├── pom.xml
│   │   │   └── src
│   │   │       └── main
│   │   │           └── java
│   │   │               └── tn
│   │   │                   └── rnu
│   │   │                       └── eniso
│   │   │                           └── fwk
│   │   │                               └── scan
│   │   │                                   └── core
│   │   │                                       └── dal
│   │   │                                           └── repository
│   │   │                                               ├── AlertRepository.java
│   │   │                                               ├── DeviceRepository.java
│   │   │                                               ├── PortRepository.java
│   │   │                                               └── ScanSessionRepository.java
│   │   ├── infra
│   │   │   ├── pom.xml
│   │   │   └── src
│   │   │       └── main
│   │   │           └── java
│   │   │               └── tn
│   │   │                   └── rnu
│   │   │                       └── eniso
│   │   │                           └── fwk
│   │   │                               └── scan
│   │   │                                   └── core
│   │   │                                       └── infra
│   │   │                                           └── model
│   │   │                                               ├── Alert.java
│   │   │                                               ├── AlertSeverity.java
│   │   │                                               ├── AlertStatistics.java
│   │   │                                               ├── Device.java
│   │   │                                               ├── Port.java
│   │   │                                               └── ScanSession.java
│   │   ├── pom.xml
│   │   ├── service-api
│   │   │   ├── pom.xml
│   │   │   └── src
│   │   │       └── main
│   │   │           └── java
│   │   │               └── tn
│   │   │                   └── rnu
│   │   │                       └── eniso
│   │   │                           └── fwk
│   │   │                               └── scan
│   │   │                                   └── core
│   │   │                                       └── service
│   │   │                                           └── api
│   │   │                                               ├── ElasticsearchService.java
│   │   │                                               ├── NmapService.java
│   │   │                                               └── SuricataService.java
│   │   ├── service-impl
│   │   │   ├── pom.xml
│   │   │   └── src
│   │   │       └── main
│   │   │           └── java
│   │   │               └── tn
│   │   │                   └── rnu
│   │   │                       └── eniso
│   │   │                           └── fwk
│   │   │                               └── scan
│   │   │                                   └── core
│   │   │                                       └── service
│   │   │                                           └── impl
│   │   │                                               ├── ElasticsearchServiceImpl.java
│   │   │                                               ├── NmapServiceImpl.java
│   │   │                                               ├── SuricataLogMonitor.java
│   │   │                                               └── SuricataServiceImpl.java
│   │   └── ws-rest
│   │       ├── pom.xml
│   │       └── src
│   │           └── main
│   │               └── java
│   │                   └── tn
│   │                       └── rnu
│   │                           └── eniso
│   │                               └── fwk
│   │                                   └── scan
│   │                                       └── core
│   │                                           └── ws
│   │                                               └── rest
│   │                                                   ├── NmapController.java
│   │                                                   └── SuricataController.java
│   ├── Dockerfile
│   ├── modules
│   │   └── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── suricata-logs
│   ├── eve.json
│   ├── fast.log
│   ├── stats.log
│   └── suricata.log
├── SURICATA_README.md
├── test_advanced_attacks.sh
├── test_attacks.sh
├── test_focused_attacks.sh
└── test_network_attacks.sh

```

## Roadmap

- [x] Basic network monitoring
- [x] Suricata IDS integration
- [x] Nmap scanning integration
- [ ] Advanced AI pattern recognition
- [ ] Automated threat response
- [ ] Dashboard UI
- [ ] Reporting system
- [ ] Multi-network support

## Acknowledgments

- Suricata IDS
- Nmap Project

---

**Note:** This project is under active development. Features and documentation are subject to change.
