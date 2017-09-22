package net.masonapps.modelviewervr.io;

import android.util.Log;

import com.badlogic.gdx.graphics.Color;

import net.masonapps.modelviewervr.mesh.Face;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bob on 7/27/2017.
 */

public class SculptMeshParser {

    public static MeshData parse(File file) throws IOException {
        return parse(new FileInputStream(file));
    }

    public static MeshData parse(InputStream inputStream) throws IOException {
        DataInputStream stream = new DataInputStream(inputStream);

        final String header = readHeader(stream);
        Log.d(SculptMeshParser.class.getSimpleName(), "file header: " + header);
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
        Log.d(SculptMeshParser.class.getSimpleName(), "version: " + version);
        Log.d(SculptMeshParser.class.getSimpleName(), "original asset: " + assetName);

        final int vertexCount = stream.readInt();
        final int triangleCount = stream.readInt();
        final Vertex[] vertices = new Vertex[vertexCount];
        final Face[] faces = new Face[triangleCount];

        for (int i = 0; i < vertexCount; i++) {
            vertices[i] = parseVertex(stream, i);
        }

        for (int i = 0; i < triangleCount; i++) {
            faces[i] = parseTriangle(stream, vertices);
        }

        for (int i = 0; i < vertexCount; i++) {
            final short j = stream.readShort();
            if (j >= 0 && j < vertices.length)
                vertices[i].symmetricPair = vertices[j];
        }

        stream.close();
        Log.d(SculptMeshParser.class.getSimpleName(), "vertexCount: " + vertices.length);
        Log.d(SculptMeshParser.class.getSimpleName(), "triangleCount: " + faces.length);
        final MeshData meshData = new MeshData(vertices, faces);
        meshData.setOriginalName(assetName);
        return meshData;
    }

    private static Vertex parseVertex(DataInputStream stream, int index) throws IOException {
        final Vertex vertex = new Vertex();
        vertex.index = index;
        vertex.position.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
        vertex.normal.set(stream.readFloat(), stream.readFloat(), stream.readFloat());
        vertex.uv.set(stream.readFloat(), stream.readFloat());
        Color.abgr8888ToColor(vertex.color, stream.readFloat());
        return vertex;
    }

    private static Face parseTriangle(DataInputStream stream, Vertex[] vertexArray) throws IOException {
        final short ia = stream.readShort();
        final short ib = stream.readShort();
        final short ic = stream.readShort();
        final Face face = new Face(vertexArray[ia], vertexArray[ib], vertexArray[ic]);
        return face;
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
