package net.masonapps.modelviewervr.mesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.modelviewervr.sculpt.BVH;

import java.util.Arrays;
import java.util.List;

public class Face {

    public static final int FLAG_UPDATE = 2;
    public static final int FLAG_REFIT_NODE = 4;
    public final Plane plane = new Plane();
    public final Vertex[] vertices;
    public final BoundingBox bounds = new BoundingBox();
    public Edge[] edges;
    public float[] normalWeights;
    //    private final Vector3 tmp = new Vector3();
//    private final Vector3 tmp2 = new Vector3();
    public BVH.Node node = null;
    public int flag = 0;

    public Face(List<Vertex> vertexList) {
        vertices = new Vertex[vertexList.size()];
        vertexList.toArray(vertices);
        init();
    }

    public Face(Vertex... vertices) {
        this.vertices = vertices;
        init();
    }

    private void init() {
        edges = new Edge[vertices.length];
        normalWeights = new float[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            final int j = (i + 1) % vertices.length;

            final Vertex vi = vertices[i];
            final Vertex vj = vertices[j];

            vi.addFace(this);
            normalWeights[i] = 1f;
            edges[i] = new Edge(vi, vj);
        }
        update();
    }

    public boolean intersect(Ray ray) {
        return intersect(ray, null);
    }

    public boolean intersect(Ray ray, Vector3 hitPoint) {
        boolean intersectRayTriangle = false;
        if (Intersector.intersectRayBoundsFast(ray, bounds)) {
            for (int i = 1; i < vertices.length - 1; i++) {
                intersectRayTriangle = Intersector.intersectRayTriangle(ray, vertices[0].position, vertices[i].position, vertices[i + 1].position, hitPoint);
                if (intersectRayTriangle) break;
            }
        }
        return intersectRayTriangle;
    }


    public BoundingBox bounds() {
        return bounds;
    }

    public void update() {
        bounds.inf();
        Arrays.stream(vertices).forEach(vertex -> bounds.ext(vertex.position));
        plane.set(vertices[0].position, vertices[1].position, vertices[2].position);
        clearUpdateFlag();
    }

//    public void updateSlow() {
//        bounds.inf();
//        Arrays.stream(vertices).forEach(vertex -> bounds.ext(vertex.position));
//
//        tmp.set(v2.position).sub(v1.position).nor();
//        tmp2.set(v3.position).sub(v1.position).nor();
//        a1 = (float) Math.acos(tmp.dot(tmp2));
//
//        tmp.set(v3.position).sub(v2.position).nor();
//        tmp2.set(v1.position).sub(v2.position).nor();
//        a2 = (float) Math.acos(tmp.dot(tmp2));
//
//        tmp.set(v1.position).sub(v3.position).nor();
//        tmp2.set(v2.position).sub(v3.position).nor();
//        a3 = (float) Math.acos(tmp.dot(tmp2));
//
//        plane.set(vertices[0].position, vertices[1].position, vertices[2].position);
//
//        clearUpdateFlag();
//    }

    public void refitNode() {
        if (node != null)
            node.refit();
        clearRefitNodeFlag();
    }

    public float getWeight(Vertex vertex) {
        for (int i = 0; i < vertices.length; i++) {
            if (vertex.index == vertices[i].index)
                return normalWeights[i];
        }
        return 0;
    }

    public void flagNeedsUpdate() {
        flag |= FLAG_UPDATE;
    }

    public void flagNeedsRefitNode() {
        flag |= FLAG_REFIT_NODE;
    }

    public void flagNeedsUpdateAndRefit() {
        flagNeedsUpdate();
        flagNeedsRefitNode();
    }

    public void clearUpdateFlag() {
        flag &= ~FLAG_UPDATE;
    }

    public void clearRefitNodeFlag() {
        flag &= ~FLAG_REFIT_NODE;
    }

    public boolean needsUpdate() {
        return (flag & FLAG_UPDATE) == FLAG_UPDATE;
    }

    public boolean shouldRefitNode() {
        return (flag & FLAG_REFIT_NODE) == FLAG_REFIT_NODE;
    }
}