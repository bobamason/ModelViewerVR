package net.masonapps.modelviewervr.sculpt;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

import net.masonapps.modelviewervr.mesh.Face;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Bounding Volume Hierarchy based on AABBs.
 *
 * @author Jesper Öqvist <jesper.oqvist@cs.lth.se>
 */
public class BVH {

    private static final String TAG = BVH.class.getSimpleName();
    /**
     * Indicates whether to use SAH for construction.
     */
    private static final int SPLIT_LIMIT = 6;
    private final Comparator<Face> cmpX = (o1, o2) -> {
        BoundingBox b1 = o1.bounds();
        BoundingBox b2 = o2.bounds();
        float c1 = b1.min.x + (b1.max.x - b1.min.x) / 2;
        float c2 = b2.min.x + (b2.max.x - b2.min.x) / 2;
        return Float.compare(c1, c2);
    };
    private final Selector selectX = (bounds, split) -> {
        double centroid = bounds.min.x + (bounds.max.x - bounds.min.x) / 2;
        return centroid < split;
    };
    private final Comparator<Face> cmpY = (o1, o2) -> {
        BoundingBox b1 = o1.bounds();
        BoundingBox b2 = o2.bounds();
        float c1 = b1.min.y + (b1.max.y - b1.min.y) / 2;
        float c2 = b2.min.y + (b2.max.y - b2.min.y) / 2;
        return Float.compare(c1, c2);
    };
    private final Selector selectY = (bounds, split) -> {
        double centroid = bounds.min.y + (bounds.max.y - bounds.min.y) / 2;
        return centroid < split;
    };
    private final Comparator<Face> cmpZ = (o1, o2) -> {
        BoundingBox b1 = o1.bounds();
        BoundingBox b2 = o2.bounds();
        float c1 = b1.min.z + (b1.max.z - b1.min.z) / 2;
        float c2 = b2.min.z + (b2.max.z - b2.min.z) / 2;
        return Float.compare(c1, c2);
    };
    private final Selector selectZ = (bounds, split) -> {
        double centroid = bounds.min.z + (bounds.max.z - bounds.min.z) / 2;
        return centroid < split;
    };
    public Node root;
    private MeshData meshData;

    /**
     * Construct a new BVH containing the given primitives.
     */
    public BVH(@NonNull MeshData meshData, Method method) {
        this.meshData = meshData;
        final Face[] faceArray = new Face[meshData.getFaceCount()];
        System.arraycopy(meshData.getFaces(), 0, faceArray, 0, faceArray.length);

        final Random random = new Random(420);
        for (int i = faceArray.length; i > 1; i--) {
            final int j = random.nextInt(i);
            Face tmp = faceArray[i - 1];
            faceArray[i - 1] = faceArray[j];
            faceArray[j] = tmp;

        }

        switch (method) {
            case MIDPOINT:
                root = constructMidpointSplit(faceArray);
                break;
            case SAH:
                root = constructSAH(faceArray);
                break;
            case SAH_MA:
                root = constructSAH_MA(faceArray);
                break;
        }
    }

    private static BoundingBox bb(Face[] faces) {

        final BoundingBox boundingBox = new BoundingBox();
        boundingBox.inf();

        for (Face face : faces) {
            BoundingBox bb = face.bounds();
            boundingBox.ext(bb);
        }
        return boundingBox;
    }

    private static float surfaceArea(BoundingBox bb) {
        float x = bb.max.x - bb.min.x;
        float y = bb.max.y - bb.min.y;
        float z = bb.max.z - bb.min.z;
        return 2f * (y * z + x * z * x * y);
    }

    private static boolean intersectSphereBounds(Vector3 center, float radius, BoundingBox bb) {

        float dx = 0;
        if (center.x < bb.min.x) {
            dx = bb.min.x - center.x;
            dx *= dx;
        } else if (center.x > bb.max.x) {
            dx = center.x - bb.max.x;
            dx *= dx;
        }

        float dy = 0;
        if (center.y < bb.min.y) {
            dy = bb.min.y - center.y;
            dy *= dy;
        } else if (center.y > bb.max.y) {
            dy = center.y - bb.max.y;
            dy *= dy;
        }

        float dz = 0;
        if (center.z < bb.min.z) {
            dz = bb.min.z - center.z;
            dz *= dz;
        } else if (center.z > bb.max.z) {
            dz = center.z - bb.max.z;
            dz *= dz;
        }
        final float sqDist = dx + dy + dz;
        return sqDist <= radius * radius;
    }

    /**
     * Simple BVH construction using splitting by major axis.
     *
     * @return root node of constructed BVH
     */
    private Node constructMidpointSplit(Face[] faces) {
        Stack<Node> nodes = new Stack<>();
        Stack<Action> actions = new Stack<>();
        Stack<Face[]> chunks = new Stack<>();
        chunks.push(faces);
        actions.push(Action.PUSH);
        while (!actions.isEmpty()) {
            Action action = actions.pop();
            if (action == Action.MERGE) {
                nodes.push(new Group(nodes.pop(), nodes.pop()));
            } else {
                Face[] chunk = chunks.pop();
                if (chunk.length < SPLIT_LIMIT) {
                    nodes.push(new Leaf(chunk));
                } else {
                    splitMidpointMajorAxis(chunk, actions, chunks);
                }
            }
        }
        return nodes.pop();
    }

    /**
     * Split a chunk on the major axis.
     */
    private void splitMidpointMajorAxis(Face[] chunk, Stack<Action> actions, Stack<Face[]> chunks) {
        BoundingBox bb = bb(chunk);
        float xl = bb.max.x - bb.min.x;
        float yl = bb.max.y - bb.min.y;
        float zl = bb.max.z - bb.min.z;
        float splitPos;
        Selector selector;
        if (xl >= yl && xl >= zl) {
            splitPos = bb.min.x + (bb.max.x - bb.min.x) / 2;
            selector = selectX;
            Arrays.sort(chunk, cmpX);
        } else if (yl >= xl && yl >= zl) {
            splitPos = bb.min.y + (bb.max.y - bb.min.y) / 2;
            selector = selectY;
            Arrays.sort(chunk, cmpY);
        } else {
            splitPos = bb.min.z + (bb.max.z - bb.min.z) / 2;
            selector = selectZ;
            Arrays.sort(chunk, cmpZ);
        }

        int split;
        int end = chunk.length;
        for (split = 1; split < end; ++split) {
            if (!selector.select(chunk[split].bounds(), splitPos)) {
                break;
            }
        }

        actions.push(Action.MERGE);
        Face[] cons = new Face[split];
        System.arraycopy(chunk, 0, cons, 0, split);
        chunks.push(cons);
        actions.push(Action.PUSH);

        cons = new Face[end - split];
        System.arraycopy(chunk, split, cons, 0, end - split);
        chunks.push(cons);
        actions.push(Action.PUSH);
    }

    /**
     * Construct a BVH using Surface Area Heuristic (SAH).
     *
     * @return root node of constructed BVH
     */
    private Node constructSAH(Face[] faces) {
        Stack<Node> nodes = new Stack<>();
        Stack<Action> actions = new Stack<>();
        Stack<Face[]> chunks = new Stack<>();
        chunks.push(faces);
        actions.push(Action.PUSH);
        while (!actions.isEmpty()) {
            Action action = actions.pop();
            if (action == Action.MERGE) {
                nodes.push(new Group(nodes.pop(), nodes.pop()));
            } else {
                Face[] chunk = chunks.pop();
                if (chunk.length < SPLIT_LIMIT) {
                    nodes.push(new Leaf(chunk));
                } else {
                    splitSAH(chunk, actions, chunks);
                }
            }
        }
        return nodes.pop();
    }

    /**
     * Construct a BVH using Surface Area Heuristic (SAH)
     *
     * @return root node of constructed BVH
     */
    private Node constructSAH_MA(Face[] faces) {
        Stack<Node> nodes = new Stack<>();
        Stack<Action> actions = new Stack<>();
        Stack<Face[]> chunks = new Stack<>();
        chunks.push(faces);
        actions.push(Action.PUSH);
        while (!actions.isEmpty()) {
            Action action = actions.pop();
            if (action == Action.MERGE) {
                nodes.push(new Group(nodes.pop(), nodes.pop()));
            } else {
                Face[] chunk = chunks.pop();
                if (chunk.length < SPLIT_LIMIT) {
                    nodes.push(new Leaf(chunk));
                } else {
                    splitSAH_MA(chunk, actions, chunks);
                }
            }
        }
        return nodes.pop();
    }

    /**
     * Split a chunk based on Surface Area Heuristic of all possible splits
     */
    private void splitSAH(Face[] chunk, Stack<Action> actions, Stack<Face[]> chunks) {
        BoundingBox bounds = new BoundingBox();
        bounds.inf();
        float cmin = Float.POSITIVE_INFINITY;
        int split = 0;
        int end = chunk.length;

        float[] sl = new float[end];
        float[] sr = new float[end];

        Comparator<Face> cmp = cmpX;
        Arrays.sort(chunk, cmpX);
        for (int i = 0; i < end - 1; ++i) {
            bounds.ext(chunk[i].bounds());
            sl[i] = surfaceArea(bounds);
        }
        bounds.inf();
        for (int i = end - 1; i > 0; --i) {
            bounds.ext(chunk[i].bounds());
            sr[i - 1] = surfaceArea(bounds);
        }
        for (int i = 0; i < end - 1; ++i) {
            float c = sl[i] * (i + 1) + sr[i] * (end - i - 1);
            if (c < cmin) {
                cmin = c;
                split = i;
            }
        }

        Arrays.sort(chunk, cmpY);
        for (int i = 0; i < end - 1; ++i) {
            bounds.ext(chunk[i].bounds());
            sl[i] = surfaceArea(bounds);
        }
        bounds.inf();
        for (int i = end - 1; i > 0; --i) {
            bounds.ext(chunk[i].bounds());
            sr[i - 1] = surfaceArea(bounds);
        }
        for (int i = 0; i < end - 1; ++i) {
            float c = sl[i] * (i + 1) + sr[i] * (end - i - 1);
            if (c < cmin) {
                cmin = c;
                split = i;
                cmp = cmpY;
            }
        }

        Arrays.sort(chunk, cmpZ);
        for (int i = 0; i < end - 1; ++i) {
            bounds.ext(chunk[i].bounds());
            sl[i] = surfaceArea(bounds);
        }
        bounds.inf();
        for (int i = end - 1; i > 0; --i) {
            bounds.ext(chunk[i].bounds());
            sr[i - 1] = surfaceArea(bounds);
        }
        for (int i = 0; i < end - 1; ++i) {
            float c = sl[i] * (i + 1) + sr[i] * (end - i - 1);
            if (c < cmin) {
                cmin = c;
                split = i;
                cmp = cmpZ;
            }
        }

        if (cmp != cmpZ) {
            Arrays.sort(chunk, cmp);
        }

        split += 1;

        actions.push(Action.MERGE);
        Face[] cons = new Face[split];
        System.arraycopy(chunk, 0, cons, 0, split);
        chunks.push(cons);
        actions.push(Action.PUSH);

        cons = new Face[end - split];
        System.arraycopy(chunk, split, cons, 0, end - split);
        chunks.push(cons);
        actions.push(Action.PUSH);
    }

    /**
     * Split a chunk based on Surface Area Heuristic of all possible splits.
     */
    private void splitSAH_MA(Face[] chunk, Stack<Action> actions, Stack<Face[]> chunks) {
        BoundingBox bb = bb(chunk);
        float xl = bb.max.x - bb.min.x;
        float yl = bb.max.y - bb.min.y;
        float zl = bb.max.z - bb.min.z;
        Comparator<Face> cmp;
        if (xl >= yl && xl >= zl) {
            cmp = cmpX;
            Arrays.sort(chunk, cmpX);
        } else if (yl >= xl && yl >= zl) {
            cmp = cmpY;
        } else {
            cmp = cmpZ;
        }

        BoundingBox bounds = new BoundingBox();
        bounds.inf();
        double cmin = Double.POSITIVE_INFINITY;
        int split = 0;
        int end = chunk.length;

        double[] sl = new double[end];
        double[] sr = new double[end];

        Arrays.sort(chunk, cmp);
        for (int i = 0; i < end - 1; ++i) {
            bounds.ext(chunk[i].bounds());
            sl[i] = surfaceArea(bounds);
        }
        bounds.inf();
        for (int i = end - 1; i > 0; --i) {
            bounds.ext(chunk[i].bounds());
            sr[i - 1] = surfaceArea(bounds);
        }
        for (int i = 0; i < end - 1; ++i) {
            double c = sl[i] * (i + 1) + sr[i] * (end - i - 1);
            if (c < cmin) {
                cmin = c;
                split = i;
            }
        }

        split += 1;

        actions.push(Action.MERGE);
        Face[] cons = new Face[split];
        System.arraycopy(chunk, 0, cons, 0, split);
        chunks.push(cons);
        actions.push(Action.PUSH);

        cons = new Face[end - split];
        System.arraycopy(chunk, split, cons, 0, end - split);
        chunks.push(cons);
        actions.push(Action.PUSH);
    }

    /**
     * Find closest intersection between the ray and any object in the BVH.
     *
     * @return {@code true} if there exists any intersection
     */
    public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
        return Intersector.intersectRayBoundsFast(ray, root.bb) && root.closestIntersection(ray, intersection);
    }

    /**
     * Find any intersection between the ray and any object in the BVH. For simple
     * intersection tests this method is quicker. The closest point search costs a
     * bit more.
     *
     * @return {@code true} if there exists any intersection
     */
    public boolean anyIntersection(Ray ray) {
        return Intersector.intersectRayBoundsFast(ray, root.bb) && root.anyIntersection(ray);
    }

    public void sphereSearch(List<Vertex> vertices, Vector3 center, float radius) {
        vertices.clear();
        Arrays.stream(meshData.vertices)
                .forEach(Vertex::clearFlagSkipSphereTest);
        root.sphereSearch(vertices, center, radius);
    }

    public void sphereSearchAddDistinct(List<Vertex> vertices, Vector3 center, float radius) {
        root.sphereSearch(vertices, center, radius);
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public void setMeshData(MeshData meshData) {
        this.meshData = meshData;
    }

    public enum Method {
        MIDPOINT,
        SAH,
        SAH_MA,
    }

    enum Action {
        PUSH,
        MERGE,
    }

    interface Selector {
        boolean select(BoundingBox bounds, double split);
    }

    public static abstract class Node {
        public final BoundingBox bb;
        public final Face[] faces;
        @Nullable
        protected Node parent = null;

        /**
         * Create a new BVH node.
         */
        public Node(Face[] faces) {
            this.bb = bb(faces);
            this.faces = faces;
        }

        /**
         * Create new BVH node with specific bounds.
         */
        public Node(BoundingBox bb, Face[] faces) {
            this.bb = bb;
            this.faces = faces;
        }

        abstract public boolean closestIntersection(Ray ray, IntersectionInfo intersection);

        abstract public boolean anyIntersection(Ray ray);

        abstract public void sphereSearch(List<Vertex> outVertexList, Vector3 center, float radius);

        abstract public void refit();

        abstract public int size();
    }

    public static class Group extends Node {
        public final Node child1;
        public final Node child2;
        private final int numPrimitives;

        /**
         * Create a new BVH node.
         */
        public Group(Node child1, Node child2) {
            super(new BoundingBox(child1.bb).ext(child2.bb), new Face[0]);
            this.numPrimitives = child1.size() + child2.size();
            this.child1 = child1;
            this.child2 = child2;
            this.child1.parent = this;
            this.child2.parent = this;
        }

        @Override
        public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
            intersection.t = Float.POSITIVE_INFINITY;
            if (!Intersector.intersectRayBoundsFast(ray, bb))
                return false;
            final IntersectionInfo intersection1 = new IntersectionInfo();
            final IntersectionInfo intersection2 = new IntersectionInfo();
            final boolean hit1 = child1.closestIntersection(ray, intersection1);
            final boolean hit2 = child2.closestIntersection(ray, intersection2);
            if (hit1 && hit2) {
//                Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit1 && hit2");
                if (intersection1.t < intersection2.t) {
                    intersection.face = intersection1.face;
                    intersection.hitPoint.set(intersection1.hitPoint);
                    intersection.t = intersection1.t;
                } else {
                    intersection.face = intersection2.face;
                    intersection.hitPoint.set(intersection2.hitPoint);
                    intersection.t = intersection2.t;
                }
                return true;
            } else if (hit1) {
//                Log.d(Group.class.getSimpleName()  + ".closestIntersection", "hit1");
                intersection.face = intersection1.face;
                intersection.hitPoint.set(intersection1.hitPoint);
                intersection.t = intersection1.t;
                return true;
            } else if (hit2) {
//                Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit2");
                intersection.face = intersection2.face;
                intersection.hitPoint.set(intersection2.hitPoint);
                intersection.t = intersection2.t;
                return true;
            }
//            Log.d(Group.class.getSimpleName() + ".closestIntersection", "hit none");
            return false;
        }

        @Override
        public boolean anyIntersection(Ray ray) {
            return (Intersector.intersectRayBoundsFast(ray, child1.bb) && child1.anyIntersection(ray)) || (Intersector.intersectRayBoundsFast(ray, child2.bb) && child2.anyIntersection(ray));
        }

        @Override
        public void sphereSearch(List<Vertex> outVertexList, Vector3 center, float radius) {
            if (intersectSphereBounds(center, radius, bb)) {
                child1.sphereSearch(outVertexList, center, radius);
                child2.sphereSearch(outVertexList, center, radius);
            }
        }

        @Override
        public void refit() {
            bb.inf();
            bb.ext(child1.bb);
            bb.ext(child2.bb);
            if (parent != null) parent.refit();
        }

        @Override
        public int size() {
            return numPrimitives;
        }
    }

    public static class Leaf extends Node {

        public Leaf(Face[] faces) {
            super(faces);
            for (Face face : faces) {
                face.node = this;
            }
        }

        @Override
        public boolean closestIntersection(Ray ray, IntersectionInfo intersection) {
            boolean hit = false;
            intersection.t = Float.POSITIVE_INFINITY;
            final Vector3 hitPoint = new Vector3();
            for (Face face : faces) {
                if (face.intersect(ray, hitPoint)) {
                    float dst = ray.origin.dst(hitPoint);
                    if (dst < intersection.t) {
                        intersection.t = dst;
                        intersection.hitPoint.set(hitPoint);
                        intersection.face = face;
                        hit = true;
                    }
                }
            }
//            Log.d(Leaf.class.getSimpleName() + ".closestIntersection", "hit = " + hit);
            return hit;
        }

        @Override
        public boolean anyIntersection(Ray ray) {
            for (Face face : faces) {
                if (face.intersect(ray)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void sphereSearch(List<Vertex> outVertexList, Vector3 center, float radius) {
            if (intersectSphereBounds(center, radius, bb)) {
                final float r2 = radius * radius;
                for (Face face : faces) {
                    for (Vertex v : face.vertices) {
                        if (!v.shouldSkipSphereTest()) {
                            Vector3 p1 = v.position;
                            if (Vector3.dst2(p1.x, p1.y, p1.z, center.x, center.y, center.z) <= r2) {
                                outVertexList.add(v);
                                v.flagSkipSphereTest();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int size() {
            return faces.length;
        }

        @Override
        public void refit() {
            bb.inf();
            for (Face face : faces) {
                bb.ext(face.bounds());
            }
            if (parent != null) parent.refit();
        }
    }

    public static class IntersectionInfo {
        public float t = -1f;
        public Vector3 hitPoint = new Vector3();
        @Nullable
        public Face face = null;
    }
}