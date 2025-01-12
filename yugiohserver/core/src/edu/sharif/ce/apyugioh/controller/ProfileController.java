package edu.sharif.ce.apyugioh.controller;

import edu.sharif.ce.apyugioh.model.MenuState;
import edu.sharif.ce.apyugioh.model.User;
import edu.sharif.ce.apyugioh.view.ProfileView;
import edu.sharif.ce.apyugioh.view.menu.ProfileMenuView;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProfileController {

    @Getter
    private static ProfileController instance;
    private static ProfileMenuView view;
    private static Logger logger;

    static {
        instance = new ProfileController();
        view = new ProfileMenuView(ProgramController.getGame());
        logger = LogManager.getLogger(ProfileController.class);
    }

    private ProfileController() {
    }

    @Setter
    @Getter
    private User user;

    public void changeNickname(String nickname) {
        if (User.getUserByNickname(nickname) != null) {
            view.showError(ProfileView.ERROR_USER_NICKNAME_ALREADY_TAKEN, nickname);
            return;
        }
        logger.info("{} changed nickname to {}", user.getNickname(), nickname);
        user.setNickname(nickname);
        view.showSuccess(ProfileView.SUCCESS_CHANGE_NICKNAME);
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (!user.isPasswordCorrect(currentPassword)) {
            view.showError(ProfileView.ERROR_USER_PASSWORD_WRONG);
            return;
        }
        if (user.isPasswordCorrect(newPassword)) {
            view.showError(ProfileView.ERROR_USER_PASSWORD_REPEATED);
            return;
        }
        user.setPassword(newPassword);
        view.showSuccess(ProfileView.SUCCESS_CHANGE_PASSWORD);
        logger.info("{} changed password from {} to {}", user.getNickname(), currentPassword, newPassword);
    }

    public void changeUsername(String username) {
        if (User.getUserByUsername(username) != null) {
            view.showError(ProfileView.ERROR_USER_NICKNAME_ALREADY_TAKEN, username);
            return;
        }
        logger.info("{} changed username to {}", user.getUsername(), username);
        user.setUsername(username);
        view.showSuccess(ProfileView.SUCCESS_CHANGE_NICKNAME);
    }

    public void showProfile() {
        if (view != null)
            view.dispose();
        view = new ProfileMenuView(ProgramController.getGame());
        ProgramController.setState(MenuState.PROFILE);
        ProgramController.setCurrentMenu(view);
    }

    public void back() {
//        view.dispose();
        MainMenuController.getInstance().showMainMenu();
    }

}
