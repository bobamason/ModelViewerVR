package net.masonapps.modelviewervr.io;

import android.util.Log;

import net.masonapps.modelviewervr.Constants;
import net.masonapps.modelviewervr.mesh.Face;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Bob on 7/27/2017.
 */

public class SculptMeshWriter {

    public static final int HEADER_LENGTH = 80;
    public static final String VERSION = "version";
    public static final String ASSET = "asset";
    public static final String VERSION_1 = "1";

    public static void writeToFile(File file, MeshData meshData) throws IOException {
        writeToOutputStream(new FileOutputStream(file), meshData);
    }

    public static void writeToFile(File file, float[] vertices, short[] indices, short[] symmetry, String assetName) throws IOException {
        writeToOutputStream(new FileOutputStream(file), vertices, indices, symmetry, assetName);
    }

    public static void writeToOutputStream(OutputStream outputStream, MeshData meshData) throws IOException {
        final DataOutputStream stream = new DataOutputStream(outputStream);
        writeHeader(stream, meshData.getOriginalName());
        stream.writeInt(meshData.vertices.length);
        stream.writeInt(meshData.faces.length);

        for (int i = 0; i < meshData.vertices.length; i++) {
            final Vertex v = meshData.vertices[i];
            stream.writeFloat(v.position.x);
            stream.writeFloat(v.position.y);
            stream.writeFloat(v.position.z);
            stream.writeFloat(v.normal.x);
            stream.writeFloat(v.normal.y);
            stream.writeFloat(v.normal.z);
            stream.writeFloat(v.uv.x);
            stream.writeFloat(v.uv.y);
            stream.writeFloat(v.color.toFloatBits());
        }

        Log.d(SculptMeshWriter.class.getSimpleName(), meshData.vertices.length + " vertices written to file");

        for (int i = 0; i < meshData.faces.length; i++) {
            final Face f = meshData.faces[i];
            for (Vertex v : f.vertices) {
                stream.writeShort(v.index);
            }
        }


        for (int i = 0; i < meshData.vertices.length; i++) {
            final Vertex symmetricVertex = meshData.vertices[i].symmetricPair;
            stream.writeShort(symmetricVertex != null ? symmetricVertex.index : -1);
        }

        Log.d(SculptMeshWriter.class.getSimpleName(), (meshData.faces.length * 3) + " indices written to file");

        stream.flush();
        stream.close();
    }

    public static void writeToOutputStream(OutputStream outputStream, float[] vertices, short[] indices, short[] symmetry, String assetName) throws IOException {
        final DataOutputStream stream = new DataOutputStream(outputStream);
        writeHeader(stream, assetName);
        stream.writeInt(vertices.length / 9);
        stream.writeInt(indices.length / 3);

        for (int i = 0; i < vertices.length; i += 9) {
            for (int j = 0; j < 9; j++) {
                stream.writeFloat(vertices[i + j]);
            }
        }
        Log.d(SculptMeshWriter.class.getSimpleName(), (vertices.length / 9) + " vertices written to file");

        for (int i = 0; i < indices.length; i += 3) {
            for (int j = 0; j < 3; j++) {
                stream.writeShort(indices[i + j]);
            }
        }


        for (int i = 0; i < symmetry.length; i++) {
            stream.writeShort(symmetry[i]);
        }

        Log.d(SculptMeshWriter.class.getSimpleName(), indices.length + " indices written to file");

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
