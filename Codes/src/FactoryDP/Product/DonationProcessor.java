package FactoryDP.Product;

import MVC.donation.Donation;

import java.time.LocalDate;

public interface DonationProcessor {

    // expiry date stored for donations that never expire (cash, goods)
    LocalDate NO_EXPIRY = LocalDate.of(9999, 12, 31);

    Donation process();
}
