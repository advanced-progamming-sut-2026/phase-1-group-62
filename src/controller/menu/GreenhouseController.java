package controller.menu;

import model.greenhouse.Greenhouse;
import view.greenhouse.GreenhouseView;

public class GreenhouseController {
    private final Greenhouse greenhouse;
    private final GreenhouseView view;

    public GreenhouseController(Greenhouse greenhouse, GreenhouseView view) {
        this.greenhouse = greenhouse;
        this.view = view;
    }

    public void showGreenhouse() {
        view.showGreenhouse(greenhouse);
    }
}

