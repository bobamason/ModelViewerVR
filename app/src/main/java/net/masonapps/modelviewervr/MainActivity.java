package net.masonapps.modelviewervr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.vr.ndk.base.DaydreamApi;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.GvrView;

import org.masonapps.libgdxgooglevr.vr.VrActivityGVR;

public class MainActivity extends VrActivityGVR {
    private static final int RC_PERMISSIONS = 901;
    private static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private StoragePermissionResultListener listener;
    @Nullable
    private RequestStoragePermissionAction action = null;
    @Nullable
    private DaydreamApi daydreamApi = null;
    private ModelViewerGame vrGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        final File[] files = getFilesDir().listFiles();
//        for (File file : files) {
//            file.delete();
//        }
        vrGame = new ModelViewerGame();
        initialize(vrGame);
    }

    @Override
    protected void initGvrView(GvrView gvrView) {
        super.initGvrView(gvrView);
        gvrView.setMultisampling(2);
        gvrView.setNeckModelEnabled(true);
        gvrView.setNeckModelFactor(1f);
//        gvrView.setStereoModeEnabled(false);
        if (gvrView.setAsyncReprojectionEnabled(true)) {
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        daydreamApi = DaydreamApi.create(this);
    }

    @Override
    protected void onStop() {
        vrGame.saveCurrentProject();
        if (daydreamApi != null)
            daydreamApi.close();
        super.onStop();
    }

    public boolean isReadStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isWriteStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestStoragePermissions(StoragePermissionResultListener listener) {

        this.listener = listener;
        action = new RequestStoragePermissionAction(true, true);
        exitVrAndRequestPermissions();
    }

    public void requestWriteStoragePermissions(StoragePermissionResultListener listener) {
        this.listener = listener;
        action = new RequestStoragePermissionAction(false, true);
        exitVrAndRequestPermissions();
    }

    public void requestReadStoragePermissions(StoragePermissionResultListener listener) {
        this.listener = listener;
        action = new RequestStoragePermissionAction(true, false);
        exitVrAndRequestPermissions();
    }

    private void exitVrAndRequestPermissions() {
        if (daydreamApi != null)
            daydreamApi.exitFromVr(this, RC_PERMISSIONS, null);
        else
            requestPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PERMISSIONS && resultCode == RESULT_OK) {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        if (action != null) {
            if (action.read && action.write)
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, RC_PERMISSIONS);
            else if (action.read)
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, RC_PERMISSIONS);
            else if (action.write)
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE}, RC_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSIONS) {
            if (listener != null) {
                listener.onResult(isReadStoragePermissionGranted(), isWriteStoragePermissionGranted());
            }
        }
    }

    public interface StoragePermissionResultListener {
        void onResult(boolean readGranted, boolean writeGranted);
    }

    private static class RequestStoragePermissionAction {
        boolean read, write;

        public RequestStoragePermissionAction(boolean read, boolean write) {
            this.read = read;
            this.write = write;
        }
    }
}
