package ObserverDP;

import MVC.donation.Donation;

import java.time.LocalDateTime;
import java.util.Objects;

public record DonationEvent(Type type,
                            Donation donation,
                            Donation.Status previousStatus,
                            LocalDateTime occurredAt) {

    /** Kind of change that happened to a donation (replaces the old DonationEventType enum). */
    public static final class Type {
        public static final Type CREATED        = new Type("CREATED");
        public static final Type UPDATED        = new Type("UPDATED");
        public static final Type STATUS_CHANGED = new Type("STATUS_CHANGED");
        public static final Type REMOVED        = new Type("REMOVED");

        private final String name;

        private Type(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public DonationEvent {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(donation, "donation");
        Objects.requireNonNull(occurredAt, "occurredAt");
        if (type == Type.STATUS_CHANGED && previousStatus == null) {
            throw new IllegalArgumentException("STATUS_CHANGED needs the previous status");
        }
    }

    public static DonationEvent created(Donation donation) {
        return new DonationEvent(Type.CREATED, donation, null, LocalDateTime.now());
    }

    public static DonationEvent updated(Donation donation) {
        return new DonationEvent(Type.UPDATED, donation, null, LocalDateTime.now());
    }

    public static DonationEvent statusChanged(Donation donation, Donation.Status previousStatus) {
        return new DonationEvent(Type.STATUS_CHANGED, donation, previousStatus, LocalDateTime.now());
    }

    public static DonationEvent removed(Donation donation) {
        return new DonationEvent(Type.REMOVED, donation, null, LocalDateTime.now());
    }
}