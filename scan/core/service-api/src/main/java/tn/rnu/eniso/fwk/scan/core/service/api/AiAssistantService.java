package tn.rnu.eniso.fwk.scan.core.service.api;

import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for AI-powered security assistance.
 */
public interface AiAssistantService {

    /**
     * Analyze a specific security alert using AI.
     *
     * @param alert The alert to analyze
     * @return A detailed analysis string
     */
    String analyzeAlert(Alert alert);

    /**
     * Generate remediation steps for a specific alert.
     *
     * @param alertId The ID of the alert to remediate
     * @return A list of remediation steps
     */
    String generateRemediation(Long alertId);

    /**
     * Analyze a batch of alerts.
     *
     * @param alerts List of alerts to analyze
     * @return List of analysis results
     */
    List<String> analyzeAlerts(List<Alert> alerts);

    /**
     * Generate a security summary for a specific time period.
     *
     * @param start Start time
     * @param end   End time
     * @return A security summary
     */
    String generateSecuritySummary(LocalDateTime start, LocalDateTime end);

    /**
     * Check if the AI service is available.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}
