package net.masonapps.modelviewervr.mesh;

import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import java.util.Arrays;

/**
 * Created by Bob on 5/11/2017.
 */

public class Vertex {
    public static final int FLAG_UPDATE = 2;
    public static final int FLAG_POSITION_SAVED = 4;
    public static final int FLAG_SKIP_SPHERE_TEST = 6;
    public final Vector3 position = new Vector3();
    public final Vector3 normal = new Vector3();
    public final Vector2 uv = new Vector2();
    public final Vector3 savedPosition = new Vector3();
    public Color color = Color.GRAY.cpy();
    public int index = -1;
    public Face[] faces = new Face[0];
    public Vertex[] adjacentVertices = new Vertex[0];
    public volatile int flag = 0;
    @Nullable
    public Vertex symmetricPair = null;
    private Edge[] edges = new Edge[0];

    public Vertex() {
    }

    private static boolean isDuplicateVertex(Vertex[] vertices, Vertex vertex) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertices[i].index == vertex.index)
                return true;
        }
        return false;
    }

    public void addFace(Face face) {
        final int length = faces.length;
        faces = Arrays.copyOf(faces, length + 1);
        faces[length] = face;
    }

    public Vertex set(Vertex vertex) {
        position.set(vertex.position);
        normal.set(vertex.normal);
        uv.set(vertex.uv);
        color.set(vertex.color);
        index = vertex.index;
        faces = Arrays.copyOf(vertex.faces, vertex.faces.length);
        return this;
    }

    public Vertex lerp(Vertex vertex, float t) {
        position.lerp(vertex.position, t);
        normal.lerp(vertex.normal, t);
        uv.lerp(vertex.uv, t);
        color.lerp(vertex.color, t);
        return this;
    }

    public Vertex recalculateNormal() {
        normal.set(0, 0, 0);
        for (Face face : faces) {
            normal.add(face.plane.normal).scl(face.getWeight(this));
        }
        normal.nor();
        return this;
    }

    public void addEdge(Edge edge) {
        final int length = edges.length;
        edges = Arrays.copyOf(edges, length + 1);
        edges[length] = edge;
        updateAdjacentVertices();
    }

    public Vertex[] getAdjacentVertices() {
        return adjacentVertices;
    }

    private void updateAdjacentVertices() {
        adjacentVertices = new Vertex[0];
        for (Edge edge : edges) {
            final Vertex v1 = edge.v1;
            final Vertex v2 = edge.v2;
            if (v1 != this && !isDuplicateVertex(adjacentVertices, v1)) {
                final int length = adjacentVertices.length;
                adjacentVertices = Arrays.copyOf(adjacentVertices, length + 1);
                adjacentVertices[length] = v1;
            }
            if (v2 != this && !isDuplicateVertex(adjacentVertices, v2)) {
                final int length = adjacentVertices.length;
                adjacentVertices = Arrays.copyOf(adjacentVertices, length + 1);
                adjacentVertices[length] = v2;
            }
        }
    }

    public void savePosition() {
        synchronized (savedPosition) {
            savedPosition.set(position);
        }
        this.flag |= FLAG_POSITION_SAVED;
    }

    public void clearSavedFlag() {
        flag &= ~Vertex.FLAG_POSITION_SAVED;
    }

    public void clearUpdateFlag() {
        flag &= ~Vertex.FLAG_UPDATE;
    }

    public boolean needsUpdate() {
        return (flag & Vertex.FLAG_UPDATE) == Vertex.FLAG_UPDATE;
    }

    public boolean shouldSkipSphereTest() {
        return (flag & Vertex.FLAG_SKIP_SPHERE_TEST) == Vertex.FLAG_SKIP_SPHERE_TEST;
    }

    public boolean isSavedPositionUpdated() {
        return (flag & Vertex.FLAG_POSITION_SAVED) == Vertex.FLAG_POSITION_SAVED;
    }

    public void clearFlagSkipSphereTest() {
        flag &= ~Vertex.FLAG_SKIP_SPHERE_TEST;
    }

    public void flagNeedsUpdate() {
        flag |= Vertex.FLAG_UPDATE;
    }

    public void flagSkipSphereTest() {
        flag |= Vertex.FLAG_SKIP_SPHERE_TEST;
    }
}
