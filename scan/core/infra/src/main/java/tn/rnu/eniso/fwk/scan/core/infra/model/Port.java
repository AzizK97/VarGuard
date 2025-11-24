package tn.rnu.eniso.fwk.scan.core.infra.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Port {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer portNumber;
    private String protocol;
    private String service;
    private String state;
    private String version;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;
}
