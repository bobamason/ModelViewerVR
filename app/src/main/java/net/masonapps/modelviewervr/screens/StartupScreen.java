package net.masonapps.modelviewervr.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.modelviewervr.ModelViewerGame;
import net.masonapps.modelviewervr.environment.GradientSphere;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

/**
 * Created by Bob on 8/10/2017.
 */

public class StartupScreen extends VrWorldScreen {


    private final StartupInterface ui;

    public StartupScreen(ModelViewerGame modelViewerGame, StartupScreenListener listener) {
        super(modelViewerGame);
        ui = new StartupInterface(new SpriteBatch(), modelViewerGame.getSkin(), listener);
        getWorld().add(GradientSphere.newInstance(getVrCamera().far - 1f, 32, 16, Color.BLACK, Color.SLATE));
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
    }

    @Override
    public void update() {
        super.update();
        ui.act();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
        ui.draw(camera);
    }

    @Override
    public void show() {
        super.show();
        GdxVr.input.setInputProcessor(ui);
    }

    @Override
    public void hide() {
        super.hide();
        GdxVr.input.setInputProcessor(null);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    public interface StartupScreenListener {
        void onStartClicked();
    }

    private static class StartupInterface extends VrUiContainer {

        private static final float PADDING = 10f;
        private final Batch spriteBatch;
        private final Skin skin;
        private StartupScreenListener listener;

        public StartupInterface(Batch spriteBatch, Skin skin, StartupScreenListener listener) {
            super();
            this.spriteBatch = spriteBatch;
            this.skin = skin;
            this.listener = listener;
            initMainLayout();
        }

        private void initMainLayout() {

        }

        @Override
        public void act() {
            super.act();
        }
    }
}
