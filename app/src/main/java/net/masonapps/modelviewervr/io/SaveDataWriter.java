package net.masonapps.modelviewervr.io;

import android.util.Log;

import net.masonapps.modelviewervr.Constants;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.sculpt.SaveData;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Bob on 9/19/2017.
 */

public class SaveDataWriter {

    public static final int HEADER_LENGTH = 80;
    public static final String VERSION = "version";
    public static final String ASSET = "asset";
    public static final String VERSION_1 = "1";

    public static void writeToFile(File file, MeshData meshData) throws IOException {
        final SaveData.SaveDataHolder saveDataHolder = SaveData.fromSculptMeshData(meshData);
        writeToOutputStream(new FileOutputStream(file), saveDataHolder.saveData, saveDataHolder.originalAssetName);
    }

    public static void writeToOutputStream(OutputStream outputStream, SaveData[] saveData, String originalAssetName) throws IOException {
        final DataOutputStream stream = new DataOutputStream(outputStream);
        writeHeader(stream, originalAssetName);
        stream.writeInt(saveData.length);

        for (int i = 0; i < saveData.length; i++) {
            final SaveData s = saveData[i];
            stream.writeFloat(s.position.x);
            stream.writeFloat(s.position.y);
            stream.writeFloat(s.position.z);
            stream.writeFloat(s.color.toFloatBits());
        }

        Log.d(SaveDataWriter.class.getSimpleName(), saveData.length + " vertices written to file");
        stream.flush();
        stream.close();
    }

    private static void writeHeader(DataOutputStream stream, String assetName) throws IOException {
        final char[] chars = new char[HEADER_LENGTH];
        Arrays.fill(chars, '\0');
        final String s = "created by " + Constants.APP_NAME + "\n" +
                VERSION + " " + VERSION_1 + "\n" +
                ASSET + " " + assetName + "\n";
        s.getChars(0, s.length(), chars, 0);
        for (int i = 0; i < chars.length; i++) {
            stream.writeChar(chars[i]);
        }
    }
}
