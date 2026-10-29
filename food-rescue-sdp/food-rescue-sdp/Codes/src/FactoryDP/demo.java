package FactoryDP;

public class demo {


    public static void main(String[] args) {

        DonationFactory factory = new DonationFactory();

        DonationProcessor cash =
                factory.createDonation("cash");
        cash.process();

        DonationProcessor food =
                factory.createDonation("food");
        food.process();

        DonationProcessor goods =
                factory.createDonation("goods");
        goods.process();
    }
}

