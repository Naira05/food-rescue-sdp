package ObserverDP;

import MVC.donation.Donation;

import java.time.LocalDateTime;
import java.util.Objects;


public record DonationEvent(DonationEventType type,
                            Donation donation,
                            Donation.Status previousStatus,
                            LocalDateTime occurredAt) {

    public DonationEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(donation, "donation");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (type == DonationEventType.STATUS_CHANGED && previousStatus == null) {
            throw new IllegalArgumentException("STATUS_CHANGED needs the previous status");
        }
    }

    public static DonationEvent created(Donation donation) {
        return new DonationEvent(DonationEventType.CREATED, donation, null, LocalDateTime.now());
    }

    public static DonationEvent updated(Donation donation) {
        return new DonationEvent(DonationEventType.UPDATED, donation, null, LocalDateTime.now());
    }

    public static DonationEvent statusChanged(Donation donation, Donation.Status previousStatus) {
        return new DonationEvent(DonationEventType.STATUS_CHANGED, donation, previousStatus, LocalDateTime.now());
    }

    public static DonationEvent removed(Donation donation) {
        return new DonationEvent(DonationEventType.REMOVED, donation, null, LocalDateTime.now());
    }
}