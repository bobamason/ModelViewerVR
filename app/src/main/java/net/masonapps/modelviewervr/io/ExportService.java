package net.masonapps.modelviewervr.io;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import net.masonapps.modelviewervr.Constants;
import net.masonapps.modelviewervr.io.output.OBJWriter;
import net.masonapps.modelviewervr.io.output.PLYWriter;
import net.masonapps.modelviewervr.io.output.STLWriter;

import java.io.File;
import java.io.IOException;

/**
 * Created by Bob on 8/17/2017.
 */

public class ExportService extends IntentService {

    public ExportService() {
        super(ExportService.class.getName());
        setIntentRedelivery(true);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint({"SetWorldReadable", "SetWorldWritable"})
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;

        final String filePath = intent.getStringExtra(Constants.KEY_FILE_PATH);
        final String fileType = intent.getStringExtra(Constants.KEY_FILE_TYPE);
        final String assetName = intent.getStringExtra(Constants.KEY_ASSET_NAME);
        final Boolean isExternal = intent.getBooleanExtra(Constants.KEY_EXTERNAL, true);
        if (filePath == null || fileType == null)
            return;

        final float[] vertices = intent.getFloatArrayExtra(Constants.KEY_VERTICES);
        final short[] indices = intent.getShortArrayExtra(Constants.KEY_INDICES);
        final short[] symmetryMap = intent.getShortArrayExtra(Constants.KEY_SYMMETRY);
        if (vertices == null || indices == null)
            return;

        String extension;
        if (fileType.equals(Constants.FILE_TYPE_OBJ))
            extension = "zip";
        else
            extension = fileType;
        final File file = new File(filePath + "." + extension);
        if (isExternal) {
            file.setReadable(true, false);
            file.setWritable(true, false);
            file.setExecutable(true, false);
        }
        try {
            switch (fileType) {
                case Constants.FILE_TYPE_OBJ:
                    OBJWriter.writeToZip(file, vertices, indices, 9, false);
                case Constants.FILE_TYPE_PLY:
                    PLYWriter.writeToFile(file, vertices, indices, 9);
                    break;
                case Constants.FILE_TYPE_STL:
                    STLWriter.writeToFile(file, vertices, indices, 9);
                    break;
                case Constants.FILE_TYPE_SCULPT:
                    if (symmetryMap != null)
                        SculptMeshWriter.writeToFile(file, vertices, indices, symmetryMap, assetName == null ? "none" : assetName);
                    break;
            }

            if (isExternal) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                scanIntent.setData(Uri.fromFile(file));
                sendBroadcast(scanIntent);
            }

        } catch (IOException e) {
            Log.e(ExportService.class.getSimpleName(), "export to " + filePath + " failed: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
