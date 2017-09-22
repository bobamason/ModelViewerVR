package net.masonapps.modelviewervr.ui;

import android.util.Log;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisTable;

import org.masonapps.libgdxgooglevr.ui.VirtualStage;

/**
 * Created by Bob on 3/31/2017.
 */

public class VisTableVR extends VirtualStage {

    protected final VisTable table;

    public VisTableVR(Batch batch, int tableWidth, int tableHeight) {
        super(batch, tableWidth, tableHeight);
        table = new VisTable();
        table.setFillParent(true);
        addActor(table);
        setActivationMovement(0);
    }

    @Override
    public void setSize(int virtualPixelWidth, int virtualPixelHeight) {
        super.setSize(virtualPixelWidth, virtualPixelHeight);
        if (table != null)
            table.invalidate();
    }

    public Table getTable() {
        return table;
    }

    public void resizeToFitTable() {
        table.setFillParent(false);
        table.layout();
        final int w = Math.round(table.getPrefWidth());
        final int h = Math.round(table.getPrefHeight());
        Log.d(VisTableVR.class.getSimpleName(), "size: " + w + " x " + h);
        setSize(w, h);
    }

    @Override
    public void setTouchable(boolean touchable) {
        super.setTouchable(touchable);
        table.setTouchable(touchable ? Touchable.enabled : Touchable.childrenOnly);
    }
}
