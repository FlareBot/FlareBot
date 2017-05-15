package stream.flarebot.flarebot.objects;

import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.Language;

public enum ReportStatus {
    
    OPEN(Language.REPORT_STATUS_OPEN),
    ON_HOLD(Language.REPORT_STATUS_ONHOLD),
    RESOLVED(Language.REPORT_STATUS_RESOLVED),
    CLOSED(Language.REPORT_STATUS_CLOSED),
    UNDER_REVIEW(Language.REPORT_STATUS_UNDERREVIEW),
    DUPLICATE(Language.REPORT_STATUS_DUPLICATE);

    public Language message;

    ReportStatus(Language message) {
        this.message = message;
    }

    public static ReportStatus get(int id) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.ordinal() == id)
                return status;
        }
        return null;
    }

    public String getMessage(String guildId) {
        return this.message.get(guildId);
    }


}
