package MVC.controller;

public abstract class ControlPanel {
    private DonationController controller;
    public ControlPanel(DonationController controller){
        this.controller = controller;
    }

    public abstract void displayDashboard();

}
