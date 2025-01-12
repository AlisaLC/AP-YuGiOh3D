package edu.sharif.ce.apyugioh.view;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import edu.sharif.ce.apyugioh.controller.AssetController;

public abstract class ButtonClickListener extends ClickListener {

    public abstract void clickAction();

    @Override
    public void clicked(InputEvent event, float x, float y) {
        AssetController.playSound("click");
        clickAction();
    }
}
