package net.masonapps.modelviewervr.mesh;

import android.support.annotation.Nullable;

/**
 * Created by Bob on 7/24/2017.
 */

public class MeshData {
    public final Vertex[] vertices;
    public final Face[] faces;
    @Nullable
    private String originalName = null;

    public MeshData(Vertex[] vertices, Face[] faces) {
        this.vertices = vertices;
        this.faces = faces;
    }

    public Face[] getFaces() {
        return faces;
    }

    public Vertex getVertex(int i) {
        return vertices[i];
    }

    public Vertex[] getVertices() {
        return vertices;
    }

    @Nullable
    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(@Nullable String originalName) {
        this.originalName = originalName;
    }

    public int getVertexCount() {
        return vertices.length;
    }

    public int getFaceCount() {
        return faces.length;
    }
}
