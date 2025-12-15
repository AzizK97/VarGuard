package tn.rnu.eniso.fwk.scan.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for caching Suricata alerts in Redis for daily analysis.
 *
 * Each day has its own Redis list with key: daily_threats:{yyyy-MM-dd}.
 * Values are stored as JSON thanks to {@link tn.rnu.eniso.fwk.scan.core.service.impl.RedisConfig}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyThreatService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final String KEY_PREFIX = "daily_threats:";

    private final RedisTemplate<String, Alert> alertRedisTemplate;

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

    private String buildKey(Alert alert) {
        LocalDate date = alert.getTimestamp() != null
                ? alert.getTimestamp().toLocalDate()
                : LocalDate.now();
        return KEY_PREFIX + DATE_FORMATTER.format(date);
    }
}


