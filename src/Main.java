import controller.menu.MenuController;
import model.Settings;
import model.User;
import model.UserSession;
import util.FileManager;
import view.menu.LoginMenu;
import view.menu.MainMenu;
import view.menu.MenuManager;

public class Main {
    public static void main(String[] args) {
        MenuController controller = new MenuController();
        MenuManager manager = MenuManager.getInstance();

        Settings settings = FileManager.loadSettings();
        if (settings.getAutoLoginUsername() != null) {
            User autoUser = FileManager.getUser(settings.getAutoLoginUsername());
            if (autoUser != null) {
                UserSession.setCurrentUser(autoUser);
                manager.setCurrentMenu(new MainMenu(controller));
            } else {
                manager.setCurrentMenu(new LoginMenu(controller));
            }
        } else {
            manager.setCurrentMenu(new LoginMenu(controller));
        }

        manager.run();
    }
}
// test
