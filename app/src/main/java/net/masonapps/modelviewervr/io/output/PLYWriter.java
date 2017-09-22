package net.masonapps.modelviewervr.io.output;

import android.annotation.SuppressLint;

import com.badlogic.gdx.utils.NumberUtils;

import net.masonapps.modelviewervr.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * Created by Bob on 7/11/2017.
 */

public class PLYWriter {

    public static void writeToFile(File file, float[] vertices, short[] indices, int vertexSize) throws IOException {
        writeToOutputStream(new FileOutputStream(file), vertices, indices, vertexSize);
    }

    @SuppressLint("DefaultLocale")
    public static void writeToOutputStream(OutputStream outputStream, float[] vertices, short[] indices, int vertexSize) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write("ply");
        writer.newLine();
        writer.write("format ascii 1.0");
        writer.newLine();
        writer.write("comment Created by " + Constants.APP_NAME);
        writer.newLine();

        final String vs = "element vertex " + vertices.length;
        vs.replace(",", "");
        writer.write(vs);
        writer.newLine();

        writer.write("property float x");
        writer.newLine();
        writer.write("property float y");
        writer.newLine();
        writer.write("property float z");
        writer.newLine();

        if (vertexSize > 3) {
            writer.write("property float nx");
            writer.newLine();
            writer.write("property float ny");
            writer.newLine();
            writer.write("property float nz");
            writer.newLine();
        }

        if (vertexSize > 8) {
            writer.write("property uchar red");
            writer.newLine();
            writer.write("property uchar green");
            writer.newLine();
            writer.write("property uchar blue");
            writer.newLine();
        }

        writer.write("element face " + (indices.length / 3));
        writer.newLine();
        writer.write("property list uchar uint vertex_indices");
        writer.newLine();
        writer.write("end_header");
        writer.newLine();

        for (int i = 0; i < vertices.length; i += vertexSize) {
            writer.write(String.format(Locale.US, "%f %f %f",
                    vertices[i],
                    vertices[i + 1],
                    vertices[i + 2]));
            if (vertexSize > 3)
                writer.write(String.format(Locale.US, " %f %f %f",
                        vertices[i + 3],
                        vertices[i + 4],
                        vertices[i + 5]));
            if (vertexSize > 8) {
                int c = NumberUtils.floatToIntColor(vertices[i + 8]);
                writer.write(String.format(Locale.US, " %d %d %d",
                        (c & 0x000000ff),
                        ((c & 0x0000ff00) >>> 8),
                        ((c & 0x00ff0000) >>> 16)));
            }
            writer.newLine();
        }

        for (int i = 0; i < indices.length; i += 3) {
            writer.write(String.format(Locale.US, "%d %d %d %d", 3, indices[i], indices[i + 1], indices[i + 2]));
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }
}
