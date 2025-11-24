package tn.rnu.eniso.fwk.scan.core.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.rnu.eniso.fwk.scan.core.infra.model.Device;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByScanSessionId(Long scanSessionId);

    Optional<Device> findByIpAddress(String ipAddress);
}
