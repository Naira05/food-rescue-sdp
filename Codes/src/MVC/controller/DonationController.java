package MVC.controller;

import DecoratorDP.BasicDonationOperation;
import DecoratorDP.DonationOperation;
import DecoratorDP.ReceiptDecorator;
import DecoratorDP.TaxDocumentDecorator;
import MVC.database.DonationTypeRepository;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import MVC.donation.DonationView;
import MVC.user.Donor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DonationController {

    private final DonationModel model;
    private final DonationView view;
    private final DonationTypeRepository types;
    private final DonationOperation donationOperation;

    public DonationController(DonationModel donationModel) {
        this(donationModel, new DonationView(), new DonationTypeRepository(), false);
    }
    public DonationController(DonationModel donationModel, DonationView view) {
        this(donationModel, view, new DonationTypeRepository(), true);
    }

    private DonationController(DonationModel donationModel, DonationView view,
                               DonationTypeRepository types, boolean attachView) {
        this.model = donationModel;
        this.view = view;
        this.types = types;

        DonationOperation operation = new BasicDonationOperation(donationModel);
        operation = new ReceiptDecorator(operation);
        operation = new TaxDocumentDecorator(operation);
        this.donationOperation = operation;

        if (attachView) {
            donationModel.attach(view);
        }
    }


    public Donation saveDonation(Donation donation) {
        return donationOperation.execute(donation);
    }

    public Donation createDonation(Donor donor, String type, String description,
                                   double quantity, String unit, LocalDate expiryDate) {
        return createDonation(donor.getUserID(), type, description, quantity, unit, expiryDate);
    }

    public Donation createDonation(int donorId, String type, String description,
                                   double quantity, String unit, LocalDate expiryDate) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Donation type is required");
        }
        if (!types.exists(type.trim())) {
            throw new IllegalArgumentException("Unknown donation type '" + type.trim()
                    + "'. Available types: " + types.findAll());
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit is required (kg, meals, boxes...)");
        }
        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiry date must be today or later");
        }

        Donation donation = new Donation(0, donorId, type.trim(), quantity, unit.trim(),
                expiryDate, LocalDate.now(), Donation.Status.AVAILABLE, description.trim());
        return saveDonation(donation);
    }

    public Optional<Donation> findDonation(int donationId) {
        return model.findDonation(donationId);
    }

    public List<Donation> getDonations() {
        return model.findAll();
    }

    public List<Donation> getDonationsByDonor(int donorId) {
        return model.findByDonor(donorId);
    }

    public List<Donation> getAvailableDonations() {
        return model.findAll().stream()
                .filter(d -> d.getSt() == Donation.Status.AVAILABLE)
                .toList();
    }

    public Donation updateDonation(int donationId, String description, Double quantity, Donation.Status status) {
        Donation donation = requireDonation(donationId);
        if (description != null) {
            if (description.isBlank()) {
                throw new IllegalArgumentException("Description cannot be blank");
            }
            donation.setDescription(description.trim());
        }
        if (quantity != null) {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            donation.setQuantity(quantity);
        }
        if (status != null) {
            donation.setSt(status);
        }
        return model.updateDonation(donation);
    }

    public Donation claimDonation(int donationId) {
        Donation donation = requireDonation(donationId);
        if (donation.getSt() != Donation.Status.AVAILABLE) {
            throw new IllegalArgumentException("Donation #" + donationId + " is not available (it is "
                    + donation.getSt() + ")");
        }
        return updateDonation(donationId, null, null, Donation.Status.CLAIMED);
    }

    public boolean deleteDonation(int donationId) {
        return model.removeDonation(donationId);
    }
    public List<String> getDonationTypes() {
        return types.findAll();
    }

    public boolean addDonationType(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Type name is required");
        }
        return types.add(name.trim());
    }

    public boolean renameDonationType(String oldName, String newName) {
        if (oldName == null || oldName.isBlank() || newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Old and new type names are required");
        }
        return types.rename(oldName.trim(), newName.trim());
    }

    public boolean removeDonationType(String name) {
        return types.remove(name == null ? "" : name.trim());
    }
    public void showDonationForm() {
        view.showDonationForm();
    }

    public void showDonations(List<Donation> donations) {
        view.displayDonations(donations);
    }

    public void showReceipt(int donationId) {
        view.showReceipt(requireDonation(donationId));
    }

    private Donation requireDonation(int donationId) {
        return model.findDonation(donationId)
                .orElseThrow(() -> new IllegalArgumentException("No donation with id " + donationId));
    }
}
