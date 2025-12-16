package tn.rnu.eniso.fwk.scan.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;
import tn.rnu.eniso.fwk.scan.core.infra.model.AlertSeverity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service responsible for caching Suricata alerts in Redis for daily analysis.
 *
 * Each day has its own Redis list with key: daily_threats:{yyyy-MM-dd}.
 * Values are stored as JSON thanks to
 * {@link tn.rnu.eniso.fwk.scan.core.service.impl.RedisConfig}.
 */
@Service
public class DailyThreatService {

    private static final Logger log = LoggerFactory.getLogger(DailyThreatService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String KEY_PREFIX = "daily_threats:";

    private final RedisTemplate<String, Alert> alertRedisTemplate;
    private final tn.rnu.eniso.fwk.scan.core.dal.repository.AlertRepository alertRepository;

    public DailyThreatService(RedisTemplate<String, Alert> alertRedisTemplate,
            tn.rnu.eniso.fwk.scan.core.dal.repository.AlertRepository alertRepository) {
        this.alertRedisTemplate = alertRedisTemplate;
        this.alertRepository = alertRepository;
    }

    public void cacheAlert(Alert alert) {
        if (alert == null) {
            return;
        }

        try {
            String key = buildKey(alert);
            alertRedisTemplate.opsForList().rightPush(key, alert);
            log.debug("Cached alert {} into Redis list {}", alert.getId(), key);
        } catch (Exception e) {
            // Redis must not break the main alert-processing pipeline
            log.warn("Failed to cache alert in Redis", e);
        }
    }

    /**
     * Get all alerts for today from Redis
     */
    public List<Alert> getTodayAlerts() {
        try {
            String key = buildTodayKey();
            Long size = alertRedisTemplate.opsForList().size(key);
            if (size == null || size == 0) {
                return List.of();
            }
            return alertRedisTemplate.opsForList().range(key, 0, -1);
        } catch (Exception e) {
            log.error("Failed to get today's alerts from Redis", e);
            return List.of();
        }
    }

    /**
     * Get statistics for today's alerts from Redis
     */
    public TodayStatistics getTodayStatistics() {
        List<Alert> todayAlerts = getTodayAlerts();

        long globalTotal = alertRepository.count();
        long todayTotal = todayAlerts.size();
        long critical = todayAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.CRITICAL)
                .count();
        long high = todayAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.HIGH)
                .count();
        long medium = todayAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.MEDIUM)
                .count();
        long low = todayAlerts.stream()
                .filter(a -> a.getSeverity() == AlertSeverity.LOW)
                .count();

        return new TodayStatistics(globalTotal, todayTotal, critical, high, medium, low);
    }

    private String buildKey(Alert alert) {
        LocalDate date = alert.getTimestamp() != null
                ? alert.getTimestamp().toLocalDate()
                : LocalDate.now();
        return KEY_PREFIX + DATE_FORMATTER.format(date);
    }

    private String buildTodayKey() {
        return KEY_PREFIX + DATE_FORMATTER.format(LocalDate.now());
    }

    /**
     * Statistics for today's alerts
     */
    public record TodayStatistics(
            long globalTotal,
            long totalAlerts,
            long criticalAlerts,
            long highAlerts,
            long mediumAlerts,
            long lowAlerts) {
    }
}
