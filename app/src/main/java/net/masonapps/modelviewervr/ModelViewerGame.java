package net.masonapps.modelviewervr;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;

import net.masonapps.modelviewervr.io.ExportService;
import net.masonapps.modelviewervr.io.SculptMeshParser;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.screens.LoadingScreen;
import net.masonapps.modelviewervr.screens.StartupScreen;
import net.masonapps.modelviewervr.sculpt.BVH;
import net.masonapps.modelviewervr.sculpt.SculptMesh;

import org.masonapps.libgdxgooglevr.GdxVr;
import org.masonapps.libgdxgooglevr.gfx.VrGame;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Created by Bob on 5/26/2017.
 */

public class ModelViewerGame extends VrGame {

    private boolean isAtlasLoaded = false;
    private LoadingScreen loadingScreen;

    @SuppressLint("SimpleDateFormat")
    private static String generateNewProjectName() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }

    @Override
    public void create() {
        super.create();
        loadingScreen = new LoadingScreen(this);
        setScreen(loadingScreen);
        loadAsset(Style.ATLAS_FILE, TextureAtlas.class);
        VisUI.load(VisUI.SkinScale.X2);
    }

    @Override
    protected void doneLoading(AssetManager assets) {
        if (!isAtlasLoaded) {
            getSkin().addRegions(assets.get(Style.ATLAS_FILE, TextureAtlas.class));
//            setupSkin();

            setScreen(new StartupScreen(this, () -> {
            }));
            isAtlasLoaded = true;
        }
    }

    private void createNewProject(final String asset) {
        setScreen(loadingScreen);
        CompletableFuture.supplyAsync(() -> {
            try {
                final MeshData meshData = SculptMeshParser.parse(Gdx.files.internal(asset).read());
                return new BVH(meshData, BVH.Method.SAH_MA);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            Log.e(ModelViewerGame.class.getSimpleName(), "unable to create a new project: " + e.getLocalizedMessage());
            return null;
        }).thenAccept(bvh -> {
            if (bvh != null) {
                final String projectName = generateNewProjectName();
//                GdxVr.app.postRunnable(() -> setScreen(new SculptingScreen(ModelViewerGame.this, bvh, projectName)));
            } else {
                showError("unable to create a new project");
            }
        });
    }

    private void openProject(final File file) {
        final String fileName = file.getName();
        CompletableFuture.supplyAsync(() -> {
            try {
                return SculptMeshParser.parse(file);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }).exceptionally(e -> {
            Log.e(ModelViewerGame.class.getSimpleName(), "unable to open project" + fileName + ": " + e.getLocalizedMessage());
            return null;
        }).thenAccept(meshData -> {
            if (meshData != null) {
                final BVH bvh = new BVH(meshData, BVH.Method.SAH_MA);
                final String projectName = fileName.substring(0, fileName.lastIndexOf('.'));
//                setScreen(new SculptingScreen(ModelViewerGame.this, bvh, projectName));
            } else {
                showError("unable to open project");
            }
        });
    }

    private void showError(String message) {
        Log.e(ModelViewerGame.class.getSimpleName(), message);
    }

    public void saveCurrentProject() {
//        if (getScreen() instanceof SculptingScreen) {
//            final SculptMesh sculptMesh = ((SculptingScreen) getScreen()).getSculptMesh();
//            Context context = GdxVr.app.getActivityWeakReference().get();
//            if (sculptMesh != null && context != null) {
////                final File file = new File(context.getFilesDir(), ((SculptingScreen) getScreen()).getProjectName() + "." + Constants.FILE_TYPE_SAVE_DATA);
////                new Thread(() -> {
////                    try {
////                        SaveDataWriter.writeToFile(file, sculptMesh.getMeshData());
////                        Log.d(Constants.APP_NAME, "project " + file.getName() + " saved");
////                    } catch (Exception e) {
////                        Log.e(Constants.APP_NAME, "failed to save project");
////                        if (file.exists())
////                            file.delete();
////                    }
////                }).start();
//                final File file = new File(context.getFilesDir(), ((SculptingScreen) getScreen()).getProjectName() + "." + Constants.FILE_TYPE_SCULPT);
//                new Thread(() -> {
//                    try {
//                        SculptMeshWriter.writeToFile(file, sculptMesh.getMeshData());
//                        Log.d(Constants.APP_NAME, "project " + file.getName() + " saved");
//                    } catch (Exception e) {
//                        Log.e(Constants.APP_NAME, "failed to save project");
//                        if (file.exists())
//                            file.delete();
//                    }
//                }).start();
//            }
//        }
    }

    public void exportFile(SculptMesh sculptMesh, String fileType) {
        final Context context = GdxVr.app.getActivityWeakReference().get();
        if (context == null) return;
        final File dir = new File(Environment.getExternalStorageDirectory(), "SculptVR");
        if (fileType.equals(Constants.FILE_TYPE_SCULPT)) {
            saveSculptFile(context, sculptMesh);
        } else {
            final Intent intent = new Intent(context, ExportService.class);
//            File file = new File(dir, ((SculptingScreen) getScreen()).getProjectName());
//            int i = 2;
//            while (file.exists()) {
//                file = new File(dir, ((SculptingScreen) getScreen()).getProjectName() + i++);
//            }
//            intent.putExtra(Constants.KEY_FILE_PATH, file.getAbsolutePath());
            intent.putExtra(Constants.KEY_FILE_TYPE, fileType);
            intent.putExtra(Constants.KEY_EXTERNAL, true);
            intent.putExtra(Constants.KEY_VERTICES, sculptMesh.getTempVertices());
            final short[] indices = new short[sculptMesh.getNumIndices()];
            sculptMesh.getIndices(indices);
            intent.putExtra(Constants.KEY_INDICES, indices);
            context.startService(intent);
        }
    }

    protected void saveSculptFile(Context context, SculptMesh sculptMesh) {
        final Intent intent = new Intent(context, ExportService.class);
//        final File file = new File(context.getFilesDir(), ((SculptingScreen) getScreen()).getProjectName());
//        intent.putExtra(Constants.KEY_FILE_PATH, file.getAbsolutePath());
        intent.putExtra(Constants.KEY_FILE_TYPE, Constants.FILE_TYPE_SCULPT);
        intent.putExtra(Constants.KEY_ASSET_NAME, sculptMesh.getMeshData().getOriginalName());
        intent.putExtra(Constants.KEY_EXTERNAL, false);
        intent.putExtra(Constants.KEY_VERTICES, sculptMesh.getTempVertices());
        final short[] indices = new short[sculptMesh.getNumIndices()];
        sculptMesh.getIndices(indices);
        intent.putExtra(Constants.KEY_INDICES, indices);
        context.startService(intent);
    }

//    private void setupSkin() {
//        addFont();
//        addSliderStyle();
//        addButtonStyle();
//        addLabelStyle();
//    }

//    private void addFont() {
//        getSkin().add(Style.DEFAULT, new BitmapFont(Gdx.files.internal(Style.FONT_FILE), getSkin().getRegion(Style.FONT_REGION)), BitmapFont.class);
//    }
//
//    private void addSliderStyle() {
//        getSkin().add("default-horizontal", new Slider.SliderStyle(getSkin().newDrawable(Style.Drawables.slider, Style.COLOR_UP_2), getSkin().newDrawable(Style.Drawables.slider_knob, Style.COLOR_UP_2)), Slider.SliderStyle.class);
//    }
//
//    private void addButtonStyle() {
//        final TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
//        textButtonStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
//        textButtonStyle.up = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_UP);
//        textButtonStyle.over = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_OVER);
//        textButtonStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
//        textButtonStyle.checked = null;
//        textButtonStyle.fontColor = Color.WHITE;
//        getSkin().add(Style.DEFAULT, textButtonStyle, TextButton.TextButtonStyle.class);
//
//        final TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
//        toggleStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
//        toggleStyle.up = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_UP);
//        toggleStyle.over = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_OVER);
//        toggleStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
//        toggleStyle.checked = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
//        toggleStyle.fontColor = Color.WHITE;
//        getSkin().add(Style.TOGGLE, toggleStyle, TextButton.TextButtonStyle.class);
//
//        final TextButton.TextButtonStyle listBtnStyle = new TextButton.TextButtonStyle();
//        listBtnStyle.font = getSkin().getFont(Style.DEFAULT_FONT);
//        listBtnStyle.up = getSkin().newDrawable(Style.Drawables.button, new Color(0, 0, 0, 0.84706f));
//        listBtnStyle.over = getSkin().newDrawable(Style.Drawables.button, new Color(0.15f, 0.15f, 0.15f, 0.84706f));
//        listBtnStyle.down = getSkin().newDrawable(Style.Drawables.button, Style.COLOR_DOWN);
//        listBtnStyle.checked = null;
//        listBtnStyle.fontColor = Color.WHITE;
//        getSkin().add(Style.LIST_ITEM, listBtnStyle, TextButton.TextButtonStyle.class);
//    }
//
//    private void addLabelStyle() {
//        getSkin().add(Style.DEFAULT, new Label.LabelStyle(getSkin().getFont(Style.DEFAULT), Color.WHITE), Label.LabelStyle.class);
//    }

    public Skin getSkin() {
        return VisUI.getSkin();
    }

    @Override
    public void dispose() {
        super.dispose();
        loadingScreen.dispose();
        VisUI.dispose(true);
    }
}
