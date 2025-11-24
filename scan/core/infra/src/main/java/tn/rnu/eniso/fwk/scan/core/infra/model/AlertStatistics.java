package tn.rnu.eniso.fwk.scan.core.infra.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertStatistics {
    private long totalAlerts;
    private long criticalAlerts;
    private long highAlerts;
    private long mediumAlerts;
    private long lowAlerts;

    private Map<String, Long> alertsByCategory;
    private Map<String, Long> topSourceIps;
    private Map<String, Long> topDestIps;
    private Map<String, Long> topSignatures;

    private long alertsLastHour;
    private long alertsLast24Hours;
    private long alertsLast7Days;
}
