package tn.rnu.eniso.fwk.scan.core.infra.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String macAddress;
    private String hostname;
    private String vendor;
    private String state;

    @ManyToOne
    @JoinColumn(name = "scan_session_id")
    private ScanSession scanSession;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Port> ports;
}
