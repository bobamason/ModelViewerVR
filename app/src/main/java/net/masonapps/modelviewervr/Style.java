package net.masonapps.modelviewervr;

import android.content.Context;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;

import org.masonapps.libgdxgooglevr.GdxVr;

/**
 * Created by Bob on 2/11/2017.
 */

public class Style {

    public static final String FONT_REGION = "Roboto-hdpi";
    public static final String FONT_FILE = "skin/Roboto-hdpi.fnt";
    public static final String ATLAS_FILE = "skin/drawables.pack";
    public static final Color COLOR_UP = new Color(0x3f3f3fff);
    public static final Color COLOR_DOWN = new Color(0x8f8f8fff);
    public static final Color COLOR_OVER = new Color(0x5f5f5fff);
    public static final Color COLOR_UP_2 = new Color(0x3f3f3fff);
    public static final Color COLOR_DOWN_2 = new Color(0x8f8f8fff);
    public static final Color COLOR_OVER_2 = new Color(0x5f5f5fff);
    public static final Color COLOR_DISABLED = new Color(Color.GRAY);
    public static final Color COLOR_WINDOW = new Color(Color.DARK_GRAY);
    public static final String DEFAULT_FONT = "default-font";
    public static final String DEFAULT = "default";
    public static final String TOGGLE = "toggle";
    public static final String LIST_ITEM = "list_item";
    public static final Color COLOR_PRIMARY = new Color(0x0288d1ff);
    public static final Color COLOR_PRIMARY_LIGHT = new Color(0x5eb8ffff);
    public static final Color COLOR_PRIMARY_DARK = new Color(0x005b9fff);
    public static final Color COLOR_ACCENT = new Color(0xffc107ff);

    public static ImageButton.ImageButtonStyle createImageButtonStyle(Skin skin, String name, boolean useBackground) {
        final ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle(skin.get(DEFAULT, ImageButton.ImageButtonStyle.class));
        imageButtonStyle.imageUp = skin.newDrawable(name, Color.WHITE);
        imageButtonStyle.imageDown = skin.newDrawable(name, useBackground ? Color.WHITE : COLOR_DOWN_2);
        imageButtonStyle.imageOver = skin.newDrawable(name, useBackground ? Color.WHITE : COLOR_OVER_2);
        imageButtonStyle.imageDisabled = skin.newDrawable(name, COLOR_DISABLED);
//        imageButtonStyle.up = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_UP) : null;
//        imageButtonStyle.down = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_DOWN) : null;
//        imageButtonStyle.over = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_OVER) : null;
//        imageButtonStyle.disabled = useBackground ? skin.newDrawable(Drawables.round_button, COLOR_UP) : null;
        return imageButtonStyle;
    }

    public static VisImageButton.VisImageButtonStyle createVisImageButtonStyle(String name, Color up, Color down, Color checked) {
        final Skin skin = VisUI.getSkin();
        return new VisImageButton.VisImageButtonStyle(null, null, null, skin.newDrawable(name, up), skin.newDrawable(name, down), skin.newDrawable(name, checked));
    }

    public static ImageTextButton.ImageTextButtonStyle createImageTextButtonStyle(Skin skin, String name) {
        final ImageTextButton.ImageTextButtonStyle imageTextButtonStyle = new ImageTextButton.ImageTextButtonStyle(skin.get(DEFAULT, ImageTextButton.ImageTextButtonStyle.class));
//        imageTextButtonStyle.font = skin.getFont(Style.DEFAULT_FONT);
//        imageTextButtonStyle.up = skin.newDrawable(Style.Drawables.button, Style.COLOR_UP);
//        imageTextButtonStyle.over = skin.newDrawable(Style.Drawables.button, Style.COLOR_OVER);
//        imageTextButtonStyle.down = skin.newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
//        imageTextButtonStyle.checked = null;
//        imageTextButtonStyle.fontColor = Color.WHITE;
        imageTextButtonStyle.imageUp = skin.newDrawable(name);
        return imageTextButtonStyle;
    }

    public static String getStringResource(int res, String defaultValue) {
        final Context context = GdxVr.app.getActivityWeakReference().get();
        if (context != null) {
            try {
                return context.getString(res);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

//    public static Image newBackgroundImage(Skin skin, Color color) {
//        final Image bg = new Image(skin.newDrawable(Style.Drawables.window, color));
//        bg.setFillParent(true);
//        return bg;
//    }

    public static class Drawables {
        public static final String loading_spinner = "loading_spinner";
        public static final String circle = "circle";
        public static final String touch_pad_background = "touch_pad_background";
        public static final String touch_pad_button_down = "touch_pad_button_down";
        public static final String ic_add_white_48dp = "ic_add_white_48dp";
        public static final String left_arrow = "left_arrow";
        public static final String right_arrow = "right_arrow";
        public static final String ic_folder = "ic_folder";
        public static final String ic_pan = "ic_pan";
        public static final String ic_redo = "ic_redo";
        public static final String ic_undo = "ic_undo";
        public static final String ic_rotate = "ic_rotate";
        public static final String ic_zoom = "ic_zoom";
        public static final String ic_more_vert = "ic_more_vert";
        public static final String ic_button_draw = "ic_button_draw";
        public static final String ic_button_pinch = "ic_button_pinch";
        public static final String ic_button_flatten = "ic_button_flatten";
        public static final String ic_button_inflate = "ic_button_inflate";
        public static final String ic_button_smooth = "ic_button_smooth";
        public static final String ic_button_paint = "ic_button_paint";
        public static final String ic_button_brush = "ic_button_brush";
        public static final String window = "window";
        public static final String window_border_bg = "window-border-bg";
        public static final String window_noborder = "window-noborder";
        public static final String new_project = "new_project";
        public static final String open_project = "open_project";
    }
}
