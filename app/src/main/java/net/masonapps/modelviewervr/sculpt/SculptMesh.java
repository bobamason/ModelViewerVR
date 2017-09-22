package net.masonapps.modelviewervr.sculpt;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

import net.masonapps.modelviewervr.mesh.Face;
import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

/**
 * Created by Bob on 7/10/2017.
 */

public class SculptMesh extends Mesh {


    private final float[] tempVertices;
    private final int vertexSize;
    private final MeshData meshData;

    public SculptMesh(MeshData meshData) {
        super(false, true, meshData.vertices.length, meshData.faces.length * 3, new VertexAttributes(VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0), VertexAttribute.ColorPacked()));
        this.meshData = meshData;
        final FloatArray verticesArray = new FloatArray();
        final ShortArray indicesArray = new ShortArray(meshData.faces.length * 3);
        for (Face face : meshData.faces) {
            for (int i = 1; i < face.vertices.length - 1; i++) {
                indicesArray.add(face.vertices[0].index);
                indicesArray.add(face.vertices[i].index);
                indicesArray.add(face.vertices[i + 1].index);
            }
        }
        verticesArray.ensureCapacity(meshData.vertices.length * (getVertexSize() / 4));
        for (int i = 0; i < meshData.vertices.length; i++) {
            final Vertex vertex = meshData.vertices[i];
            if (vertex == null) continue;
            verticesArray.add(vertex.position.x);
            verticesArray.add(vertex.position.y);
            verticesArray.add(vertex.position.z);
            verticesArray.add(vertex.normal.x);
            verticesArray.add(vertex.normal.y);
            verticesArray.add(vertex.normal.z);
            verticesArray.add(vertex.uv.x);
            verticesArray.add(vertex.uv.y);
            verticesArray.add(vertex.color.toFloatBits());
        }
        vertexSize = (getVertexSize() / 4);
        tempVertices = verticesArray.toArray();
        setVertices(tempVertices);
        setIndices(indicesArray.toArray());
    }

    public void setVertex(Vertex vertex) {
        if (vertex.index < 0) return;
//        vertices.get(vertex.index).set(vertex);
        int i = vertex.index * vertexSize;

        tempVertices[i] = vertex.position.x;
        tempVertices[i + 1] = vertex.position.y;
        tempVertices[i + 2] = vertex.position.z;

        tempVertices[i + 3] = vertex.normal.x;
        tempVertices[i + 4] = vertex.normal.y;
        tempVertices[i + 5] = vertex.normal.z;

        tempVertices[i + 6] = vertex.uv.x;
        tempVertices[i + 7] = vertex.uv.y;

        tempVertices[i + 8] = vertex.color.toFloatBits();
    }

    public void update() {
        super.setVertices(tempVertices);
    }

    public float[] getTempVertices() {
        return tempVertices;
    }

    public Face[] getTriangles() {
        return meshData.faces;
    }

    public Vertex[] getVertexArray() {
        return meshData.vertices;
    }

    public synchronized Vertex getVertex(int i) {
        return meshData.vertices[i];
    }

    public MeshData getMeshData() {
        return meshData;
    }
}
