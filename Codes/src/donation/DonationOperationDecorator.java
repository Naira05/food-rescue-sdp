
package donation;

public abstract class DonationOperationDecorator
        implements DonationOperation {

    protected final DonationOperation operation;

    public DonationOperationDecorator(DonationOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException(
                    "Operation cannot be null"
            );
        }

        this.operation = operation;
    }

    @Override
    public Donation execute(Donation donation) {
        return operation.execute(donation);
    }
}
