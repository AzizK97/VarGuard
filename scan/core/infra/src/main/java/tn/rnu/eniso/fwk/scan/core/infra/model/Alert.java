package tn.rnu.eniso.fwk.scan.core.infra.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_alert_timestamp", columnList = "timestamp"),
        @Index(name = "idx_alert_severity", columnList = "severity"),
        @Index(name = "idx_alert_source_ip", columnList = "sourceIp"),
        @Index(name = "idx_alert_dest_ip", columnList = "destIp")
})
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String sourceIp;

    @Column(nullable = false)
    private String destIp;

    private Integer sourcePort;
    private Integer destPort;

    private String protocol;

    @Column(length = 1000)
    private String signature;

    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertSeverity severity;

    private Long signatureId;
    private Long generatorId;

    @Column(length = 2000)
    private String payload;

    @Column(length = 500)
    private String action;

    // Optional relationship to Device for correlation
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    // Elasticsearch document ID for reference
    private String elasticsearchId;
}
