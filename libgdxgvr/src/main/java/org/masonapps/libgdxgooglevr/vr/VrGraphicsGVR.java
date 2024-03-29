package org.masonapps.libgdxgooglevr.vr;

import android.graphics.Point;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.AndroidGL20;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureArray;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Array;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.libgdxgooglevr.GdxVr;

import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Bob on 7/20/2017.
 */

public class VrGraphicsGVR implements Graphics, GvrView.Renderer {

    public static final String TAG = VrGraphics.class.getSimpleName();
    private static final String LOG_TAG = VrGraphics.class.getSimpleName();
    private static final int OFFSET_RIGHT = 0;
    private static final int OFFSET_UP = OFFSET_RIGHT + 3;
    private static final int OFFSET_FORWARD = OFFSET_UP + 3;
    private static final int OFFSET_TRANSLATION = OFFSET_FORWARD + 3;
    private static final int OFFSET_EULER = OFFSET_TRANSLATION + 3;
    private static final int OFFSET_QUATERNION = OFFSET_EULER + 3;
    private static final int INDEX_SCENE_BUFFER = 0;
    final Object synch = new Object();
    private final float[] array = new float[3 * 5 + 4 + 16];
    private final Vector3 forward = new Vector3();
    private final Vector3 up = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 headTranslation = new Vector3();
    private final Quaternion headQuaternion = new Quaternion();
    private final Matrix4 headMatrix = new Matrix4();
    private final long predictionOffsetNanos;
    protected VrActivity.VrApplication app;
    protected GL20 gl20;
    protected GL30 gl30;
    protected EGLContext eglContext;
    protected GLVersion glVersion;
    protected String extensions;
    protected long lastFrameTime = System.nanoTime();
    protected float deltaTime = 0;
    protected long frameStart = System.nanoTime();
    protected long frameId = -1;
    protected int frames = 0;
    protected int fps;
    protected WindowedMean mean = new WindowedMean(5);
    protected volatile boolean created = false;
    protected volatile boolean running = false;
    protected volatile boolean pause = false;
    protected volatile boolean resume = false;
    protected volatile boolean destroy = false;
    int[] value = new int[1];
    private float ppiX = 0;
    private float ppiY = 0;
    private float ppcX = 0;
    private float ppcY = 0;
    private float density = 1;
    private Point targetSize = new Point();

    public VrGraphicsGVR(VrActivity.VrApplication application) {
        this.app = application;
        predictionOffsetNanos = TimeUnit.MILLISECONDS.toNanos(50);
    }

    /**
     * Checks GL state for errors and logs a message then throw a RuntimeExecption when one is
     * encountered. Should be called regularly after calls to GL functions to help with debugging.
     *
     * @param tag The tag to use when logging.
     * @param op  The name of the GL function called before calling checkGlError
     */

    public static void checkGlError(String tag, String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(tag, op + ": glError " + error);
//            throw new RuntimeException(op + ": glError " + error);
        }
    }

    /**
     * Checks EGL state for errors and log a message when an error is encountered. Should be
     * called regularly after calls to EGL functions to help with debugging.
     *
     * @param egl An EGL object.
     * @param tag The tag to use when logging.
     * @param op  The name of the EGL function called before calling checkGlError
     */
    public static void checkEGLError(EGL10 egl, String tag, String op) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(tag, op + ": eglError " + error);
        }
    }

    /**
     * Clears all GL errors.
     */
    public static void clearGLErrors() {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
        }
    }

    private void updatePpi() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ppiX = metrics.xdpi;
        ppiY = metrics.ydpi;
        ppcX = metrics.xdpi / 2.54f;
        ppcY = metrics.ydpi / 2.54f;
        density = metrics.density;
        targetSize.x = metrics.widthPixels;
        targetSize.y = metrics.heightPixels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GL20 getGL20() {
        return gl20;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHeight() {
        return targetSize.y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWidth() {
        return targetSize.x;
    }

    @Override
    public int getBackBufferWidth() {
        return targetSize.x;
    }

    @Override
    public int getBackBufferHeight() {
        return targetSize.y;
    }

    private int getAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attrib, int defValue) {
        if (egl.eglGetConfigAttrib(display, config, attrib, value)) {
            return value[0];
        }
        return defValue;
    }

    void resume() {
        synchronized (synch) {
            running = true;
            resume = true;
        }
        final Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.resume();
            }
        }
        app.getApplicationListener().resume();
        Gdx.app.log(LOG_TAG, "resumed");
    }

    void pause() {
        synchronized (synch) {
            if (!running) return;
            running = false;
            pause = true;
        }
        final Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.pause();
            }
        }
        app.getApplicationListener().pause();
        Gdx.app.log(LOG_TAG, "paused");
//        synchronized (synch) {
//            if (!running) return;
//            running = false;
//            pause = true;
//            while (pause) {
//                try {
//                    // TODO: fix deadlock race condition with quick resume/pause.
//                    // Temporary workaround:
//                    // Android ANR time is 5 seconds, so wait up to 4 seconds before assuming
//                    // deadlock and killing process. This can easily be triggered by opening the
//                    // Recent Apps list and then double-tapping the Recent Apps button with
//                    // ~500ms between taps.
//                    synch.wait(4000);
//                    if (pause) {
//                        // pause will never go false if onDrawFrame is never called by the GLThread
//                        // when entering this method, we MUST enforce continuous rendering
//                        Gdx.app.error(LOG_TAG, "waiting for pause synchronization took too long; assuming deadlock and killing");
//                        android.os.Process.killProcess(android.os.Process.myPid());
//                    }
//                } catch (InterruptedException ignored) {
//                    Gdx.app.log(LOG_TAG, "waiting for pause synchronization failed!");
//                }
//            }
//        }
    }

    void destroy() {
        Array<LifecycleListener> listeners = app.getLifecycleListeners();
        synchronized (listeners) {
            for (LifecycleListener listener : listeners) {
                listener.dispose();
            }
        }
        app.getApplicationListener().dispose();
        clearManagedCaches();
    }


    @Override
    public long getFrameId() {
        return frameId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getDeltaTime() {
        return mean.getMean() == 0 ? deltaTime : mean.getMean();
    }

    @Override
    public float getRawDeltaTime() {
        return deltaTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphicsType getType() {
        return GraphicsType.AndroidGL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GLVersion getGLVersion() {
        return glVersion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFramesPerSecond() {
        return fps;
    }

    public void clearManagedCaches() {
        Mesh.clearAllMeshes(app);
        Texture.clearAllTextures(app);
        Cubemap.clearAllCubemaps(app);
        TextureArray.clearAllTextureArrays(app);
        ShaderProgram.clearAllShaderPrograms(app);
        FrameBuffer.clearAllFrameBuffers(app);

        logManagedCachesStatus();
    }

    protected void logManagedCachesStatus() {
        Gdx.app.log(LOG_TAG, Mesh.getManagedStatus());
        Gdx.app.log(LOG_TAG, Texture.getManagedStatus());
        Gdx.app.log(LOG_TAG, Cubemap.getManagedStatus());
        Gdx.app.log(LOG_TAG, ShaderProgram.getManagedStatus());
        Gdx.app.log(LOG_TAG, FrameBuffer.getManagedStatus());
    }

    @Override
    public float getPpiX() {
        return ppiX;
    }

    @Override
    public float getPpiY() {
        return ppiY;
    }

    @Override
    public float getPpcX() {
        return ppcX;
    }

    @Override
    public float getPpcY() {
        return ppcY;
    }

    @Override
    public float getDensity() {
        return density;
    }

    @Override
    public boolean supportsDisplayModeChange() {
        return false;
    }

    @Override
    public boolean setFullscreenMode(DisplayMode displayMode) {
        return false;
    }

    @Override
    public Monitor getPrimaryMonitor() {
        return new AndroidMonitor(0, 0, "Primary Monitor");
    }

    @Override
    public Monitor getMonitor() {
        return getPrimaryMonitor();
    }

    @Override
    public Monitor[] getMonitors() {
        return new Monitor[]{getPrimaryMonitor()};
    }

    @Override
    public DisplayMode[] getDisplayModes(Monitor monitor) {
        return getDisplayModes();
    }

    @Override
    public DisplayMode getDisplayMode(Monitor monitor) {
        return getDisplayMode();
    }

    @Override
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[]{getDisplayMode()};
    }

    @Override
    public boolean setWindowedMode(int width, int height) {
        return false;
    }

    @Override
    public void setTitle(String title) {

    }

    @Override
    public void setUndecorated(boolean undecorated) {
        final int mask = (undecorated) ? 1 : 0;
        app.getApplicationWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, mask);
    }

    @Override
    public void setResizable(boolean resizable) {

    }

    @Override
    public DisplayMode getDisplayMode() {
        DisplayMetrics metrics = new DisplayMetrics();
        app.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return new AndroidDisplayMode(metrics.widthPixels, metrics.heightPixels, 0, 0);
    }

    @Override
    public BufferFormat getBufferFormat() {
        return null;
    }

    @Override
    public void setVSync(boolean vsync) {
    }

    @Override
    public boolean supportsExtension(String extension) {
        if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
        return extensions.contains(extension);
    }

    @Override
    public boolean isContinuousRendering() {
        return true;
    }

    @Override
    public void setContinuousRendering(boolean isContinuous) {
        // not supported
    }

    @Override
    public void requestRendering() {
        // not supported
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public boolean isGL30Available() {
        return gl30 != null;
    }

    @Override
    public GL30 getGL30() {
        return gl30;
    }

    @Override
    public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
        return null;
    }

    @Override
    public void setCursor(Cursor cursor) {
    }

    @Override
    public void setSystemCursor(Cursor.SystemCursor systemCursor) {
    }

    private void handleHeadTransform(HeadTransform headTransform) {
        headTransform.getForwardVector(array, OFFSET_FORWARD);
        forward.set(array[OFFSET_FORWARD], array[OFFSET_FORWARD + 1], array[OFFSET_FORWARD + 2]);
        headTransform.getUpVector(array, OFFSET_UP);
        up.set(array[OFFSET_UP], array[OFFSET_UP + 1], array[OFFSET_UP + 2]);
        headTransform.getRightVector(array, OFFSET_RIGHT);
        right.set(array[OFFSET_RIGHT], array[OFFSET_RIGHT + 1], array[OFFSET_RIGHT + 2]);
        headTransform.getTranslation(array, OFFSET_TRANSLATION);
        headTranslation.set(array[OFFSET_TRANSLATION], array[OFFSET_TRANSLATION + 1], array[OFFSET_TRANSLATION + 2]);
        headTransform.getQuaternion(array, OFFSET_QUATERNION);
        headQuaternion.set(array[OFFSET_QUATERNION], array[OFFSET_QUATERNION + 1], array[OFFSET_QUATERNION + 2], array[OFFSET_QUATERNION + 3]);
        headMatrix.set(headTransform.getHeadView());
    }

    @Override
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        long time = System.nanoTime();
        deltaTime = (time - lastFrameTime) / 1000000000.0f;
        lastFrameTime = time;

        // After pause deltaTime can have somewhat huge value that destabilizes the mean, so let's cut it off
        if (!resume) {
            mean.addValue(deltaTime);
        } else {
            deltaTime = 0;
        }

        boolean lrunning = false;
        boolean lpause = false;
        boolean ldestroy = false;
        boolean lresume = false;

        synchronized (synch) {
            lrunning = running;
            lpause = pause;
            ldestroy = destroy;
            lresume = resume;

            if (resume) {
                resume = false;
            }

            if (pause) {
                pause = false;
                synch.notifyAll();
            }

            if (destroy) {
                destroy = false;
                synch.notifyAll();
            }
        }

//        if (lresume) {
//        }

        if (lrunning) {
            handleHeadTransform(headTransform);

            while (!app.getRunnableQueue().isEmpty()) {
                final Runnable runnable = app.getRunnableQueue().poll();
                try {
                    runnable.run();
                } catch (Exception e) {
                    Log.e(VrGraphicsGVR.class.getSimpleName(), e.getMessage());
                }
            }

            ((VrApplicationAdapter) GdxVr.app.getApplicationListener()).onDrawFrame(headTransform, leftEye, rightEye);
            frameId++;
        }

//        if (lpause) {
//        }

//        if (ldestroy) {
//        }

        if (time - frameStart > 1000000000) {
            fps = frames;
            frames = 0;
            frameStart = time;
        }
        frames++;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
        ((VrApplicationAdapter) GdxVr.app.getApplicationListener()).onFinishFrame(viewport);
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        targetSize.set(i, i1);
        updatePpi();
        if (!created) {
            app.getApplicationListener().create();
            created = true;
            synchronized (this) {
                running = true;
            }
        }
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {

        eglContext = ((EGL10) EGLContext.getEGL()).eglGetCurrentContext();
        gl20 = new AndroidGL20();
        Gdx.gl = gl20;
        GdxVr.gl = gl20;
        Gdx.gl20 = gl20;
        GdxVr.gl20 = gl20;
        String versionString = Gdx.gl.glGetString(GL10.GL_VERSION);
        String vendorString = Gdx.gl.glGetString(GL10.GL_VENDOR);
        String rendererString = Gdx.gl.glGetString(GL10.GL_RENDERER);
        glVersion = new GLVersion(Application.ApplicationType.Android, versionString, vendorString, rendererString);
        updatePpi();

        Mesh.invalidateAllMeshes(app);
        Texture.invalidateAllTextures(app);
        Cubemap.invalidateAllCubemaps(app);
        ShaderProgram.invalidateAllShaderPrograms(app);
        FrameBuffer.invalidateAllFrameBuffers(app);

        logManagedCachesStatus();

        this.mean = new WindowedMean(5);
        this.lastFrameTime = System.nanoTime();
    }

    public void onRendererShutdown() {
        Gdx.app.log(LOG_TAG, "onRendererShutdown");
    }

    public Vector3 getForward() {
        return forward;
    }

    public Vector3 getUp() {
        return up;
    }

    public Vector3 getRight() {
        return right;
    }

    public Vector3 getHeadTranslation() {
        return headTranslation;
    }

    public Matrix4 getHeadMatrix() {
        return headMatrix;
    }

    public Quaternion getHeadQuaternion() {
        return headQuaternion;
    }

    private class AndroidDisplayMode extends DisplayMode {
        protected AndroidDisplayMode(int width, int height, int refreshRate, int bitsPerPixel) {
            super(width, height, refreshRate, bitsPerPixel);
        }
    }

    private class AndroidMonitor extends Monitor {

        public AndroidMonitor(int virtualX, int virtualY, String name) {
            super(virtualX, virtualY, name);
        }

    }
}
