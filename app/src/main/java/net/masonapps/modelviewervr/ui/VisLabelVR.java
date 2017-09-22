package net.masonapps.modelviewervr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.kotcrab.vis.ui.widget.VisLabel;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

/**
 * Created by Bob on 3/31/2017.
 */

public class VisLabelVR extends VirtualStage {

    protected final VisLabel label;

    public VisLabelVR(CharSequence text, Batch batch) {
        super(batch, 100, 100);
        label = new VisLabel(text);
        init();
    }

    public VisLabelVR(CharSequence text, Batch batch, String fontName, Color color) {
        super(batch, 100, 100);
        label = new VisLabel(text, fontName, color);
        init();
    }

    public VisLabelVR(CharSequence text, Batch batch, String fontName, String colorName) {
        super(batch, 100, 100);
        label = new VisLabel(text, fontName, colorName);
        init();
    }

    public VisLabelVR(CharSequence text, Batch batch, String styleName) {
        super(batch, 100, 100);
        label = new VisLabel(text, styleName);
        init();
    }

    public VisLabelVR(CharSequence text, Batch batch, Label.LabelStyle labelStyle) {
        super(batch, 100, 100);
        label = new VisLabel(text, labelStyle);
        init();
    }

    private void init() {
        setActivationMovement(0);
        setTouchable(false);
        addActor(label);
        getViewport().update((int) label.getWidth(), (int) label.getHeight(), false);
        invalidate();
    }

    public Label getLabel() {
        return label;
    }

    public Label.LabelStyle getStyle() {
        return label.getStyle();
    }

    public void setStyle(Label.LabelStyle style) {
        label.setStyle(style);
    }

    public StringBuilder getText() {
        return label.getText();
    }

    public void setText(CharSequence newText) {
        label.setText(newText);
    }

    public GlyphLayout getGlyphLayout() {
        return label.getGlyphLayout();
    }

    public int getLabelAlign() {
        return label.getLabelAlign();
    }

    public int getLineAlign() {
        return label.getLineAlign();
    }

    public boolean textEquals(CharSequence other) {
        return label.textEquals(other);
    }

    public void setWrap(boolean wrap) {
        label.setWrap(wrap);
    }

    public void setAlignment(int alignment) {
        label.setAlignment(alignment);
    }

    public void setAlignment(int labelAlign, int lineAlign) {
        label.setAlignment(labelAlign, lineAlign);
    }

    public void setEllipsis(String ellipsis) {
        label.setEllipsis(ellipsis);
    }

    public void setEllipsis(boolean ellipsis) {
        label.setEllipsis(ellipsis);
    }
}
