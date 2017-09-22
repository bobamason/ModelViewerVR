package net.masonapps.modelviewervr.ui;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import org.masonapps.libgdxgooglevr.ui.TableVR;

/**
 * Created by Bob on 8/3/2017.
 */

public class ColorPickerGrid extends TableVR {

    public static final int PADDING = 10;
    @Nullable
    private OnColorChangedListener listener = null;
    private Texture colorGridTexture;
    private Texture highlightTexture;
    private Image highlightImage;
    private int[][] colors;

    public ColorPickerGrid(Batch batch, int tableWidth, int tableHeight) {
        super(batch, tableWidth + PADDING * 2, tableHeight + PADDING * 2);
        init(tableWidth, tableHeight);
    }

    public ColorPickerGrid(Batch batch, Skin skin, int tableWidth, int tableHeight) {
        super(batch, skin, tableWidth + PADDING * 2, tableHeight + PADDING * 2);
        init(tableWidth, tableHeight);
    }

    private void init(int tableWidth, int tableHeight) {
        colors = new int[16][16];
        Pixmap pixmap = new Pixmap(tableWidth, tableHeight, Pixmap.Format.RGBA8888);
        final int thickness = 4;
        final int w = pixmap.getWidth() / 16;
        final int h = pixmap.getHeight() / 16;
        final float[] hsv = new float[]{0, 0, 0};
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++) {
                hsv[0] = col == 0 ? 0 : 360 / 15f * (col - 1);
                hsv[1] = col == 0 ? 0 : (row < 5 ? (row + 1f) / 5f : 1);
                hsv[2] = col == 0 ? (15 - row) / 15f : (row < 5 ? 1 : (14f - (row - 4f)) / 14f);
                final int c = android.graphics.Color.HSVToColor(hsv);
                colors[col][15 - row] = c;
                pixmap.setColor((c >> 16 & 0xff) / 255f, (c >> 8 & 0xff) / 255f, (c & 0xff) / 255f, 1f);
                pixmap.fillRectangle(col * w + thickness, row * h + thickness, w - thickness * 2, h - thickness * 2);
            }
        }
        final Pixmap highlightPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        highlightPixmap.setColor(Color.CLEAR);
        highlightPixmap.fill();
        highlightPixmap.setColor(Color.WHITE);
        highlightPixmap.fillRectangle(0, 0, w, thickness);
        highlightPixmap.fillRectangle(0, h - thickness, w, thickness);
        highlightPixmap.fillRectangle(0, 0, thickness, h);
        highlightPixmap.fillRectangle(w - thickness, 0, thickness, h);
        highlightTexture = new Texture(highlightPixmap);
        highlightImage = new Image(highlightTexture);
        addActor(highlightImage);
        colorGridTexture = new Texture(pixmap);
        final Image colorGridImage = new Image(colorGridTexture);
        getTable().add(colorGridImage).pad(PADDING).expand().fill();
        colorGridImage.setTouchable(Touchable.enabled);
        colorGridImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final float w = (getTable().getWidth() - PADDING * 2) / 16f;
                final float h = (getTable().getHeight() - PADDING * 2) / 16f;
                int col = Math.round(x / w - 0.5f);
                int row = Math.round(y / h - 0.5f);
                int c = colors[col][row];
                final Color color = new Color((c >> 16 & 0xff) / 255f, (c >> 8 & 0xff) / 255f, (c & 0xff) / 255f, 1f);
                highlightImage.setPosition(col * w, row * h, Align.center);
                if (listener != null)
                    listener.onColorChanged(color);
            }
        });
    }

    public void setListener(@Nullable OnColorChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void dispose() {
        super.dispose();
        colorGridTexture.dispose();
        highlightTexture.dispose();
    }

    public interface OnColorChangedListener {
        void onColorChanged(Color color);
    }
}
