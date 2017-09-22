package net.masonapps.modelviewervr.io;

import android.util.Log;
import android.util.SparseIntArray;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.modelviewervr.mesh.Face;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bob on 7/11/2017.
 */

public class PLYConverter {
//    private static final SimpleDateFormat df = new SimpleDateFormat("mm:ss.SSS");

    private static int parseStream(InputStream inputStream, boolean flipV, FloatArray vertices, ShortArray indices) {
//        long t = System.currentTimeMillis();
        String line;
        String[] tokens;
        int xIndex = -1, yIndex = -1, zIndex = -1, nxIndex = -1, nyIndex = -1, nzIndex = -1, uIndex = -1, vIndex = -1;
        boolean hasNormal = false;
        boolean hasUV = false;
        int propertyIndex = 0;
        int vertexCount = 0, faceCount = 0, currentVertex = 0, currentFace = 0, vertexSize = 3;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            while ((line = reader.readLine()) != null) {
                tokens = line.split("\\s+");
                if (tokens.length < 1) break;

                if (tokens[0].length() == 0) {
                    continue;
                }

                if (tokens[0].equals("element")) {
                    if (tokens[1].equals("vertex")) {
                        vertexCount = Integer.parseInt(tokens[2]);
                    } else if (tokens[1].equals("face")) {
                        faceCount = Integer.parseInt(tokens[2]);
                    }

                } else if (tokens[0].equals("property")) {
                    if (tokens[2].equals("x")) {
                        xIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("y")) {
                        yIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("z")) {
                        zIndex = propertyIndex;
                        propertyIndex++;
                    }
                    if (tokens[2].equals("nx")) {
                        nxIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("ny")) {
                        nyIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("nz")) {
                        nzIndex = propertyIndex;
                        propertyIndex++;
                        hasNormal = true;
                    }
                    if (tokens[2].equals("s")) {
                        uIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                    if (tokens[2].equals("t")) {
                        vIndex = propertyIndex;
                        propertyIndex++;
                        hasUV = true;
                    }
                } else if (tokens[0].equals("end_header")) {
                    vertexSize = 3;
                    if (hasNormal) vertexSize += 3;
                    if (hasUV) vertexSize += 2;
                    vertices.ensureCapacity(vertexCount * vertexSize);
                    indices.ensureCapacity(faceCount * 3);
                    break;
                }
            }

            while (currentVertex < vertexCount) {
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                float x = 0, y = 0, z = 0, nx = 0, ny = 0, nz = 0, u = 0, v = 0;
                try {
                    x = Float.parseFloat(tokens[xIndex]);
                    y = Float.parseFloat(tokens[yIndex]);
                    z = Float.parseFloat(tokens[zIndex]);
                    if (hasNormal) {
                        nx = Float.parseFloat(tokens[nxIndex]);
                        ny = Float.parseFloat(tokens[nyIndex]);
                        nz = Float.parseFloat(tokens[nzIndex]);
                    }
                    if (hasUV) {
                        u = Float.parseFloat(tokens[uIndex]);
                        v = Float.parseFloat(tokens[vIndex]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);
                if (hasNormal) {
                    vertices.add(nx);
                    vertices.add(ny);
                    vertices.add(nz);
                }
                if (hasUV) {
                    vertices.add(u);
                    vertices.add(flipV ? 1f - v : v);
                }
                currentVertex++;
            }

            Log.d(PLYConverter.class.getSimpleName(), "vertices " + (vertices.size / vertexSize) + " file count " + vertexCount);

            while (currentFace < faceCount) {
                line = reader.readLine();
                if (line == null) break;
                tokens = line.split("\\s+");
                final int n = Integer.parseInt(tokens[0]);
                for (int i = 0; i < n; i++) {
                    if (i + 1 > tokens.length) break;
                    if (i >= 3) {
                        indices.add(Integer.parseInt(tokens[1]));
                        indices.add(Integer.parseInt(tokens[i]));
                    }
                    indices.add(Integer.parseInt(tokens[i + 1]));
                }
                currentFace++;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(PLYConverter.class.getSimpleName(), "load ply file failed: " + e.getMessage());
            return -1;
        }
        Log.d(PLYConverter.class.getSimpleName(), "faces " + (indices.size / 3));
//        Log.d(PLYLoader.class.getSimpleName(), "parseStream eT: " + df.format(System.currentTimeMillis() - t));
        return vertexSize;
    }

    public static MeshData createMeshData(File file, String assetName) throws FileNotFoundException {
        return createMeshData(new FileInputStream(file), assetName);
    }

    public static MeshData createMeshData(InputStream inputStream, String assetName) {
//        long t = System.currentTimeMillis();
        final FloatArray vertexArray = new FloatArray();
        final ShortArray indexArray = new ShortArray();
        final int vertexSize = parseStream(inputStream, false, vertexArray, indexArray);
        final List<Vertex> vertexList = new ArrayList<>(vertexArray.size);
        final List<Face> faces = new ArrayList<>(indexArray.size / 3);
        final float tolerance = 1e-5f;
        SparseIntArray indexMap = new SparseIntArray();

        float[] vertices = vertexArray.toArray();
        short[] indices = indexArray.toArray();
        int numDuplicates = 0;
        for (int i = 0; i < vertices.length; i += vertexSize) {
            boolean isDouble = false;
            final Vertex vertex = new Vertex();
            final int index = i / vertexSize;
            vertex.position.set(vertices[i], vertices[i + 1], vertices[i + 2]);
            vertex.normal.set(vertices[i + 3], vertices[i + 4], vertices[i + 5]);
            if (vertexSize > 6) {
                final int offset = 6;
                vertex.uv.set(vertices[i + offset], vertices[i + offset + 1]);
            }
            for (int j = 0; j < vertexList.size(); j++) {
                if (vertexList.get(j).position.dst(vertex.position) <= tolerance) {
                    numDuplicates++;
                    indexMap.put(index, j);
                    isDouble = true;
                    break;
                }
            }
            if (!isDouble) {
                vertex.index = vertexList.size();
                indexMap.put(index, vertexList.size());
                vertexList.add(vertex);
            }
        }

        for (int i = 0; i < indices.length; i += 3) {
            int ia = indices[i];
            ia = indexMap.get(ia, ia);

            int ib = indices[i + 1];
            ib = indexMap.get(ib, ib);

            int ic = indices[i + 2];
            ic = indexMap.get(ic, ic);
            final Face face = new Face(vertexList.get(ia), vertexList.get(ib), vertexList.get(ic));
            face.update();
            faces.add(face);
        }
//        Log.d(PLYLoader.class.getSimpleName(), "createTriangleList eT: " + df.format(System.currentTimeMillis() - t));
        Log.d(PLYConverter.class.getSimpleName(), numDuplicates + " duplicate vertices removed");
        final MeshData meshData = new MeshData(vertexList.toArray(new Vertex[vertexList.size()]), faces.toArray(new Face[faces.size()]));
//        SymmetryMapUtils.createSymmetry(sculptMeshData.getVertices(), 1e-5f);
        meshData.setOriginalName(assetName);
        return meshData;
    }

    public static class PLYLoaderParameters extends ModelLoader.ModelParameters {
        public boolean flipV = true;

        public PLYLoaderParameters() {
        }

        public PLYLoaderParameters(boolean flipV) {
            this.flipV = flipV;
        }
    }
}
