package net.masonapps.modelviewervr.screens;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.controller.Controller;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;

import net.masonapps.modelviewervr.Assets;
import net.masonapps.modelviewervr.Style;
import net.masonapps.modelviewervr.environment.GradientSphere;
import net.masonapps.modelviewervr.ui.VisTableVR;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.Entity;
import org.masonapps.libgdxgooglevr.gfx.VrGame;
import org.masonapps.libgdxgooglevr.gfx.VrWorldScreen;
import org.masonapps.libgdxgooglevr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgooglevr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgooglevr.ui.ImageButtonVR;
import org.masonapps.libgdxgooglevr.ui.VrUiContainer;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Created by Bob on 8/30/2017.
 */

public abstract class ModelSelectionScreen<T> extends VrWorldScreen {

    private static final float SPEED = -360f;
    private static final int MODEL_Z = -3;
    private final List<T> list;
    private final Entity loadingSpinner;
    private final ModelSelectionUI ui;
    private final SpriteBatch spriteBatch;
    private int currentIndex = 0;
    @Nullable
    private Entity currentModel = null;
    private int numModels;
    private CompletableFuture<ModelData> loadModelFuture = null;

    public ModelSelectionScreen(VrGame game, List<T> list) {
        super(game);
        this.list = Collections.synchronizedList(list);
        numModels = list.size();
        getWorld().add(GradientSphere.newInstance(getVrCamera().far - 1f, 32, 16, Color.BLACK, Color.SLATE));
        setBackgroundColor(Color.BLACK);
        getVrCamera().near = 0.1f;
        final Model rect = createRect();
        final Texture texture = new Texture(Assets.LOADING_SPINNER_ASSET);
        manageDisposable(texture);
        rect.materials.get(0).set(new BlendingAttribute(), TextureAttribute.createDiffuse(texture));
        loadingSpinner = getWorld().add(new Entity(new ModelInstance(rect, 0, 0, MODEL_Z)));
        loadingSpinner.setLightingEnabled(false);
        spriteBatch = new SpriteBatch();
        manageDisposable(spriteBatch);
        ui = new ModelSelectionUI();
        loadModel(currentIndex);
    }

    @Override
    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.setColor(Color.WHITE);
        light.setDirection(new Vector3(1, -1, -1).nor());
        lights.add(light);
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

    private void loadModel(int index) {
        if (currentModel != null) {
            world.remove(currentModel);
            currentModel.dispose();
            currentModel = null;
        }
        if (loadModelFuture != null && !loadModelFuture.isDone()) {
            loadModelFuture.cancel(true);
        }
        loadingSpinner.setVisible(true);
        loadModelFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return loadModelData(list.get(index));
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
        loadModelFuture.exceptionally(e -> {
            runOnGLThread(() -> onLoadModelFailed(list.get(index), e));
            return null;
        }).thenAccept(modelData -> runOnGLThread(() -> {
            if (modelData != null) {
                final Model model = new Model(modelData);
                final ModelInstance modelInstance = new ModelInstance(model, 0, 0, -2);
                modelInstance.materials.get(0).set(ColorAttribute.createDiffuse(Color.WHITE));
                currentModel = getWorld().add(new Entity(modelInstance));
                float r = currentModel.getRadius() * 0.5f;
                if (r != 0)
                    currentModel.setScale(1f / r);
                currentModel.setPosition(0, 0.5f, MODEL_Z);
                ui.setVertexCount(currentModel.modelInstance.model.meshes.get(0).getNumVertices());
                ui.setTriangleCount(currentModel.modelInstance.model.meshes.get(0).getNumIndices() / 3);
                loadingSpinner.setVisible(false);
            }
        }));
    }

    protected abstract ModelData loadModelData(T t) throws IOException;

    protected abstract void onLoadModelFailed(T t, Throwable e);

    protected abstract void onModelClicked(int index, T t);

    public List<T> getList() {
        return list;
    }

    @Override
    public void update() {
        super.update();
        ui.act();
        final boolean isLoadingModel = currentModel == null;
        loadingSpinner.setVisible(isLoadingModel);
        if (isLoadingModel)
            loadingSpinner.rotateZ(GdxVr.graphics.getDeltaTime() * SPEED);
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
        if (event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD &&
                event.action == DaydreamButtonEvent.ACTION_DOWN &&
                currentModel != null) {
            if (currentModel.intersectsRayBoundsFast(GdxVr.input.getInputRay()))
                onModelClicked(currentIndex, list.get(currentIndex));
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {
    }

    private class ModelSelectionUI extends VrUiContainer {

        private static final float PADDING = 10f;
        private VisLabel vertexCountLabel;
        private VisLabel triangleCountLabel;
        private NumberFormat nf = NumberFormat.getIntegerInstance();

        public ModelSelectionUI() {
            super();
            initLayout();
        }

        @Override
        public boolean performRayTest(Ray ray) {
            if (super.performRayTest(ray)) return true;
            boolean modelTest = false;
            if (currentModel != null && currentModel.intersectsRayBounds(ray, hitPoint3D)) {
                currentModel.lookAt(hitPoint3D, Vector3.Y);
                hitPoint2DPixels.set(-1, -1);
                modelTest = true;
                isCursorOver = true;
            }
            return modelTest;
        }

        private void initLayout() {
            final VisTableVR infoTable = new VisTableVR(spriteBatch, 240, 120);
            vertexCountLabel = new VisLabel(nf.format(0) + " vertices");
            infoTable.getTable()
                    .add(vertexCountLabel)
                    .padTop(PADDING)
                    .padBottom(PADDING)
                    .padLeft(PADDING)
                    .padRight(PADDING)
                    .growX()
                    .center()
                    .row();
            triangleCountLabel = new VisLabel(nf.format(0) + " triangles");
            infoTable.getTable()
                    .add(triangleCountLabel)
                    .padBottom(PADDING)
                    .padLeft(PADDING)
                    .padRight(PADDING)
                    .growX()
                    .center();
            infoTable.setPosition(0, 1.35f, MODEL_Z);
            infoTable.getTable().setBackground(Style.Drawables.window);
            addProcessor(infoTable);

            final ImageButtonVR leftBtn = new ImageButtonVR(spriteBatch, VisUI.getSkin().newDrawable(Style.Drawables.left_arrow, Style.COLOR_PRIMARY_DARK), VisUI.getSkin().newDrawable(Style.Drawables.left_arrow, Style.COLOR_PRIMARY_LIGHT), null);
            leftBtn.setPosition(-0.75f, 0f, MODEL_Z);
            addProcessor(leftBtn);

            final ImageButtonVR rightBtn = new ImageButtonVR(spriteBatch, VisUI.getSkin().newDrawable(Style.Drawables.right_arrow, Style.COLOR_PRIMARY_DARK), VisUI.getSkin().newDrawable(Style.Drawables.right_arrow, Style.COLOR_PRIMARY_LIGHT), null);
            rightBtn.setPosition(0.75f, 0f, MODEL_Z);
            addProcessor(rightBtn);

            leftBtn.setVisible(currentIndex > 0);
            rightBtn.setVisible(currentIndex < numModels - 1);


            leftBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentIndex > 0) {
                        currentIndex--;
                        loadModel(currentIndex);
                        leftBtn.setVisible(currentIndex > 0);
                        rightBtn.setVisible(true);
                    }
                }
            });

            rightBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (currentIndex < numModels - 1) {
                        currentIndex++;
                        loadModel(currentIndex);
                        leftBtn.setVisible(true);
                        rightBtn.setVisible(currentIndex < numModels - 1);
                    }
                }
            });
        }

        public void setVertexCount(int numVertices) {
            vertexCountLabel.setText(nf.format(numVertices) + " vertices");
        }

        public void setTriangleCount(int numTriangles) {
            triangleCountLabel.setText(nf.format(numTriangles) + " triangles");
        }
    }
}
