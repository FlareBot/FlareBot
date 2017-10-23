package stream.flarebot.flarebot.objects;

public enum ReportStatus {

    OPEN("Open"),
    ON_HOLD("On Hold"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    UNDER_REVIEW("Under Review"),
    DUPLICATE("Duplicate");

    public String message;

    ReportStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

}
