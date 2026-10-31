package FactoryDP;

public class DonationFactory {

    public DonationProcessor createDonation(String type) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Donation type cannot be null."
            );
        }

        switch (type.trim().toLowerCase()) {
            case "cash":
                return new CashDonation();

            case "food":
                return new FoodDonation();

            case "goods":
                return new GoodsDonation();

            default:
                throw new IllegalArgumentException(
                        "Invalid donation type: " + type
                );
        }
    }
}

