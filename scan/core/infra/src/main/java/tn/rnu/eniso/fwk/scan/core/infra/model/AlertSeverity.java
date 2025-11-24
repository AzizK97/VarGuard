package tn.rnu.eniso.fwk.scan.core.infra.model;

public enum AlertSeverity {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    AlertSeverity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static AlertSeverity fromLevel(int level) {
        for (AlertSeverity severity : values()) {
            if (severity.level == level) {
                return severity;
            }
        }
        return LOW;
    }
}
