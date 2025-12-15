package tn.rnu.eniso.fwk.scan.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.rnu.eniso.fwk.scan.core.dal.repository.AlertRepository;
import tn.rnu.eniso.fwk.scan.core.dal.repository.DeviceRepository;
import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;
import tn.rnu.eniso.fwk.scan.core.infra.model.AlertSeverity;
import tn.rnu.eniso.fwk.scan.core.infra.model.AlertStatistics;
import tn.rnu.eniso.fwk.scan.core.infra.model.Device;
import tn.rnu.eniso.fwk.scan.core.service.api.ElasticsearchService;
import tn.rnu.eniso.fwk.scan.core.service.api.SuricataService;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuricataServiceImpl implements SuricataService {

    private final AlertRepository alertRepository;
    private final DeviceRepository deviceRepository;
    private final ElasticsearchService elasticsearchService;
    private final ApplicationEventPublisher eventPublisher;
    private final DailyThreatService dailyThreatService;
    private final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public List<Alert> getRecentAlerts(int limit) {
        return alertRepository.findTop100ByOrderByTimestampDesc()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Alert> getAlerts(Pageable pageable) {
        return alertRepository.findByOrderByTimestampDesc(pageable);
    }

    @Override
    public List<Alert> getAlertsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return alertRepository.findByTimestampBetween(start, end);
    }

    @Override
    public List<Alert> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverityOrderByTimestampDesc(severity);
    }

    @Override
    public List<Alert> getAlertsByIp(String ipAddress) {
        return alertRepository.findByIpAddress(ipAddress);
    }

    @Override
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alert not found with id: " + id));
    }

    @Override
    public AlertStatistics getStatistics(LocalDateTime since) {
        AlertStatistics stats = new AlertStatistics();

        // Total alerts
        stats.setTotalAlerts(alertRepository.countByTimestampAfter(since));

        // Alerts by severity
        stats.setCriticalAlerts(alertRepository.countBySeverityAndTimestampAfter(AlertSeverity.CRITICAL, since));
        stats.setHighAlerts(alertRepository.countBySeverityAndTimestampAfter(AlertSeverity.HIGH, since));
        stats.setMediumAlerts(alertRepository.countBySeverityAndTimestampAfter(AlertSeverity.MEDIUM, since));
        stats.setLowAlerts(alertRepository.countBySeverityAndTimestampAfter(AlertSeverity.LOW, since));

        // Alerts by category
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        alertRepository.countByCategory(since).forEach(row -> categoryMap.put((String) row[0], (Long) row[1]));
        stats.setAlertsByCategory(categoryMap);

        // Top source IPs
        Map<String, Long> sourceIpMap = new LinkedHashMap<>();
        alertRepository.countBySourceIp(since).stream().limit(10)
                .forEach(row -> sourceIpMap.put((String) row[0], (Long) row[1]));
        stats.setTopSourceIps(sourceIpMap);

        // Top destination IPs
        Map<String, Long> destIpMap = new LinkedHashMap<>();
        alertRepository.countByDestIp(since).stream().limit(10)
                .forEach(row -> destIpMap.put((String) row[0], (Long) row[1]));
        stats.setTopDestIps(destIpMap);

        // Top signatures
        Map<String, Long> signatureMap = new LinkedHashMap<>();
        alertRepository.countBySignature(since).stream().limit(10)
                .forEach(row -> signatureMap.put((String) row[0], (Long) row[1]));
        stats.setTopSignatures(signatureMap);

        // Time-based counts
        LocalDateTime now = LocalDateTime.now();
        stats.setAlertsLastHour(alertRepository.countByTimestampAfter(now.minusHours(1)));
        stats.setAlertsLast24Hours(alertRepository.countByTimestampAfter(now.minusDays(1)));
        stats.setAlertsLast7Days(alertRepository.countByTimestampAfter(now.minusDays(7)));

        return stats;
    }

    @Override
    @Transactional
    public void processEveLog(String jsonLog) {
        try {
            JsonNode root = objectMapper.readTree(jsonLog);

            // Check if this is an alert event
            String eventType = root.path("event_type").asText();
            if (!"alert".equals(eventType)) {
                return;
            }

            Alert alert = new Alert();

            // Parse timestamp
            String timestampStr = root.path("timestamp").asText();
            if (!timestampStr.isEmpty()) {
                try {
                    ZonedDateTime zdt = ZonedDateTime.parse(timestampStr, DateTimeFormatter.ISO_DATE_TIME);
                    alert.setTimestamp(zdt.toLocalDateTime());
                } catch (Exception e) {
                    alert.setTimestamp(LocalDateTime.now());
                }
            } else {
                alert.setTimestamp(LocalDateTime.now());
            }

            // Parse source and destination
            alert.setSourceIp(root.path("src_ip").asText());
            alert.setDestIp(root.path("dest_ip").asText());
            alert.setSourcePort(root.path("src_port").asInt(0));
            alert.setDestPort(root.path("dest_port").asInt(0));
            alert.setProtocol(root.path("proto").asText());

            // Parse alert details
            JsonNode alertNode = root.path("alert");
            alert.setSignature(alertNode.path("signature").asText());
            alert.setCategory(alertNode.path("category").asText());
            alert.setSignatureId(alertNode.path("signature_id").asLong());
            alert.setGeneratorId(alertNode.path("gid").asLong());
            alert.setAction(alertNode.path("action").asText());

            // Determine severity based on signature or default to MEDIUM
            int severityLevel = alertNode.path("severity").asInt(2);
            alert.setSeverity(mapSeverity(severityLevel));

            // Parse payload if available
            if (root.has("payload")) {
                alert.setPayload(root.path("payload").asText());
            }

            // Try to correlate with existing devices
            Optional<Device> device = deviceRepository.findByIpAddress(alert.getDestIp());
            device.ifPresent(alert::setDevice);

            // Save to database
            Alert savedAlert = alertRepository.save(alert);
            log.info("Saved alert: {} from {} to {}", savedAlert.getSignature(),
                    savedAlert.getSourceIp(), savedAlert.getDestIp());

            // Index in Elasticsearch
            try {
                String esId = elasticsearchService.indexAlert(savedAlert);
                if (esId != null) {
                    savedAlert.setElasticsearchId(esId);
                    alertRepository.save(savedAlert);
                }
            } catch (Exception e) {
                log.warn("Failed to index alert in Elasticsearch", e);
            }

            // Publish event for real-time broadcasting via SSE
            eventPublisher.publishEvent(new AlertEvent(this, savedAlert));
            log.debug("Published AlertEvent for real-time broadcasting");

            // Cache to Redis for AI analysis
            dailyThreatService.cacheAlert(savedAlert);

        } catch (Exception e) {
            log.error("Error processing EVE log: {}", jsonLog, e);
        }
    }

    @Override
    @Transactional
    public void processEveLogs(List<String> jsonLogs) {
        jsonLogs.forEach(this::processEveLog);
    }

    private AlertSeverity mapSeverity(int suricataSeverity) {
        // Suricata severity: 1 = high, 2 = medium, 3 = low
        return switch (suricataSeverity) {
            case 1 -> AlertSeverity.HIGH;
            case 2 -> AlertSeverity.MEDIUM;
            case 3 -> AlertSeverity.LOW;
            default -> AlertSeverity.MEDIUM;
        };
    }
}
