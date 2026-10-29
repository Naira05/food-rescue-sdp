
package config;

public class SystemConfig {

    private static final SystemConfig instance =
            new SystemConfig();

    private SystemConfig() {
    }

    public static SystemConfig getInstance() {
        return instance;
    }

    public String getOrganizationName() {
        return "Surplus Food Rescue Network";
    }
}
