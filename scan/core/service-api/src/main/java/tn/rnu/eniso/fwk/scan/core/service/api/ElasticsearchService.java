package tn.rnu.eniso.fwk.scan.core.service.api;

import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ElasticsearchService {

    /**
     * Index an alert in Elasticsearch
     */
    String indexAlert(Alert alert);

    /**
     * Search alerts with a query string
     */
    List<Alert> searchAlerts(String query, int from, int size);

    /**
     * Get alerts by time range from Elasticsearch
     */
    List<Alert> getAlertsByTimeRange(LocalDateTime start, LocalDateTime end, int size);

    /**
     * Aggregate alerts by a specific field
     */
    Map<String, Long> aggregateByField(String field, LocalDateTime since, int topN);

    /**
     * Check if Elasticsearch is available
     */
    boolean isAvailable();

    /**
     * Create the alerts index if it doesn't exist
     */
    void createIndexIfNotExists();
}
