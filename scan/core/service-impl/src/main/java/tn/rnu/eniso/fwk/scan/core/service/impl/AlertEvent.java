package tn.rnu.eniso.fwk.scan.core.service.impl;

import org.springframework.context.ApplicationEvent;
import tn.rnu.eniso.fwk.scan.core.infra.model.Alert;

/**
 * Spring ApplicationEvent wrapper for Alert objects.
 * Used to decouple the service layer from the controller layer
 * for real-time alert broadcasting via SSE.
 */
public class AlertEvent extends ApplicationEvent {

    private final Alert alert;

    public AlertEvent(Object source, Alert alert) {
        super(source);
        this.alert = alert;
    }

    public Alert getAlert() {
        return alert;
    }
}
