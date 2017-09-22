package net.masonapps.modelviewervr.io;

import android.util.Log;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.modelviewervr.sculpt.SaveData;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bob on 9/19/2017.
 */

public class SaveDataParser {

    private static final Vector3 position = new Vector3();
    private static final Color color = new Color();

    public static SaveData.SaveDataHolder parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    public static SaveData.SaveDataHolder parse(InputStream inputStream) throws IOException {
        DataInputStream stream = new DataInputStream(inputStream);

        final String header = readHeader(stream);
        Log.d(SaveDataParser.class.getSimpleName(), "file header: " + header);
        final String[] lines = header.split("\n");
        String assetName = "none";
        String version = SculptMeshWriter.VERSION_1;
        for (String line : lines) {
            try {
                if (line.startsWith(SculptMeshWriter.VERSION)) {
                    version = line.substring(line.indexOf(' '));
                } else if (line.startsWith(SculptMeshWriter.ASSET)) {
                    assetName = line.substring(line.indexOf(' '));
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        Log.d(SaveDataParser.class.getSimpleName(), "version: " + version);
        Log.d(SaveDataParser.class.getSimpleName(), "original asset: " + assetName);

        final int vertexCount = stream.readInt();
        final SaveData[] saveData = new SaveData[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            saveData[i] = parseSaveData(stream, i);
        }

        stream.close();
        return new SaveData.SaveDataHolder(saveData, assetName);
    }

    private static SaveData parseSaveData(DataInputStream stream, int index) throws IOException {
        position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
        Color.abgr8888ToColor(color, stream.readFloat());
        return new SaveData(position, color, index);
    }

    private static String readHeader(DataInputStream stream) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SculptMeshWriter.HEADER_LENGTH; i++) {
            final char c = stream.readChar();
            if (c != '\0')
                stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
