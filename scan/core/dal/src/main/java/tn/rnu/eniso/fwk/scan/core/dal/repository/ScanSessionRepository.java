package tn.rnu.eniso.fwk.scan.core.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.rnu.eniso.fwk.scan.core.infra.model.ScanSession;

@Repository
public interface ScanSessionRepository extends JpaRepository<ScanSession, Long> {
}
