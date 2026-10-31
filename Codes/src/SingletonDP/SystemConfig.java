
package config;

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
}
