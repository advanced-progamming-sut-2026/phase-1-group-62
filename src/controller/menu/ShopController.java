package controller.menu;

import model.shop.Shop;
import view.View;

public class ShopController {
    private final Shop shop;
    private final View view;

    public ShopController(Shop shop, View view) {
        this.shop = shop;
        this.view = view;
    }

    public void showItems() {
        view.showMessage("Shop items: " + shop.getItems().size());
    }
}

