
package config;

public class SingleTone {

    private static final SingleTone instance =
            new SystemConfig();

    private SingleTone() {
    }

    public static SingleTone getInstance() {
        return instance;
    }

    public String getOrganizationName() {
        return "Surplus Food Rescue Network";
    }
}
