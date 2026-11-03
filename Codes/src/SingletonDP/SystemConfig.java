
package SingletonDP;

public class SystemConfig {

    private static SystemConfig instance;

    private SystemConfig() {
    }

    public static SystemConfig getInstance() {
        if (instance == null) {
            instance = new SystemConfig();
        }
        return instance;
    }

    public String getOrganizationName() {
        return "Surplus Food Rescue Network";
    }

    /** Share of a cash donation that the donor can deduct from taxes. */
    public double getTaxDeductionRate() {
        return 0.5;
    }
}
