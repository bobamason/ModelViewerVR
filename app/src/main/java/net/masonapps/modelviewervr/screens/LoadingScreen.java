package net.masonapps.modelviewervr.screens;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.google.vr.sdk.controller.Controller;

import net.masonapps.modelviewervr.Assets;
import net.masonapps.modelviewervr.environment.GradientSphere;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 12/28/2016.
 */
public class LoadingScreen extends VrWorldScreen {

    private static final float SPEED = -360f;
    private final Entity loadingSpinner;

    public LoadingScreen(VrGame game) {
        super(game);
        getWorld().add(GradientSphere.newInstance(getVrCamera().far - 1f, 32, 16, Color.BLACK, Color.SLATE));
        setBackgroundColor(Color.BLACK);
        getVrCamera().near = 0.1f;
        final Model rect = createRect();
        final Texture texture = new Texture(Assets.LOADING_SPINNER_ASSET);
        manageDisposable(texture);
        rect.materials.get(0).set(new BlendingAttribute(), TextureAttribute.createDiffuse(texture));
        loadingSpinner = getWorld().add(new Entity(new ModelInstance(rect, 0, 0, -6)));
        loadingSpinner.setLightingEnabled(false);
    }

    private Model createRect() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material();
        final float r = 0.25f;
        return modelBuilder.createRect(
                -r, -r, 0,
                r, -r, 0,
                r, r, 0,
                -r, r, 0,
                0, 0, r,
                material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void update() {
        super.update();
        loadingSpinner.rotateZ(GdxVr.graphics.getDeltaTime() * SPEED);
    }

    @Override
    public void onCardboardTrigger() {
        super.onCardboardTrigger();
    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
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
}
