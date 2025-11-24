package tn.rnu.eniso.fwk.scan.core.service.api;

import tn.rnu.eniso.fwk.scan.core.infra.model.Device;
import tn.rnu.eniso.fwk.scan.core.infra.model.ScanSession;

import java.util.List;

public interface NmapService {
    ScanSession scanNetwork(String target);

    List<ScanSession> getAllScans();

    List<Device> getDevices(Long scanId);

    ScanSession getScanById(Long scanId);
}
