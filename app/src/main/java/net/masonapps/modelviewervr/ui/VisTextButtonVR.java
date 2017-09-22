package net.masonapps.modelviewervr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.kotcrab.vis.ui.widget.VisTextButton;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

/**
 * Created by Bob on 3/31/2017.
 */

public class VisTextButtonVR extends VirtualStage {

    protected final VisTextButton textButton;

    public VisTextButtonVR(Batch batch, String text) {
        super(batch, 100, 100);
        textButton = new VisTextButton(text);
        init();
    }

    public VisTextButtonVR(Batch batch, String text, String styleName) {
        super(batch, 100, 100);
        textButton = new VisTextButton(text, styleName);
        init();
    }

    public VisTextButtonVR(Batch batch, String text, VisTextButton.VisTextButtonStyle style) {
        super(batch, 100, 100);
        textButton = new VisTextButton(text, style);
        init();
    }

    private void init() {
        setActivationMovement(0);
        setTouchable(true);
        addActor(textButton);
        getViewport().update((int) textButton.getWidth(), (int) textButton.getHeight(), false);
        invalidate();
    }

    public TextButton getTextButton() {
        return textButton;
    }

    public TextButton.TextButtonStyle getStyle() {
        return textButton.getStyle();
    }

    public void setStyle(Button.ButtonStyle style) {
        textButton.setStyle(style);
    }

    public Label getLabel() {
        return textButton.getLabel();
    }

    public Cell getLabelCell() {
        return textButton.getLabelCell();
    }

    public CharSequence getText() {
        return textButton.getText();
    }

    public void setText(String text) {
        textButton.setText(text);
    }

    @Override
    public boolean addListener(EventListener listener) {
        return textButton.addListener(listener);
    }
}
