package net.masonapps.modelviewervr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisImageButton;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

/**
 * Created by Bob on 3/31/2017.
 */

public class VisImageButtonVR extends VirtualStage {

    protected final VisImageButton imageButton;

    public VisImageButtonVR(Batch batch, String styleName) {
        super(batch, 100, 100);
        imageButton = new VisImageButton(styleName);
        init();
    }

    public VisImageButtonVR(Batch batch, Drawable imageUp) {
        super(batch, 100, 100);
        imageButton = new VisImageButton(imageUp);
        init();
    }

    public VisImageButtonVR(Batch batch, Drawable imageUp, Drawable imageDown) {
        super(batch, 100, 100);
        imageButton = new VisImageButton(imageUp, imageDown);
        init();
    }

    public VisImageButtonVR(Batch batch, Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
        super(batch, 100, 100);
        imageButton = new VisImageButton(imageUp, imageDown, imageChecked);
        init();
    }

    public VisImageButtonVR(Batch batch, VisImageButton.VisImageButtonStyle imageButtonStyle) {
        super(batch, 100, 100);
        imageButton = new VisImageButton(imageButtonStyle);
        init();
    }

    private void init() {
        setActivationMovement(0);
        addActor(imageButton);
        setSize((int) imageButton.getWidth(), (int) imageButton.getHeight());
        invalidate();
    }

    public VisImageButton getImageButton() {
        return imageButton;
    }

    public VisImageButton.VisImageButtonStyle getStyle() {
        return imageButton.getStyle();
    }

    public void setStyle(Button.ButtonStyle style) {
        imageButton.setStyle(style);
    }

    public Image getImage() {
        return imageButton.getImage();
    }

    public Cell getImageCell() {
        return imageButton.getImageCell();
    }

    @Override
    public boolean addListener(EventListener listener) {
        return imageButton.addListener(listener);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
