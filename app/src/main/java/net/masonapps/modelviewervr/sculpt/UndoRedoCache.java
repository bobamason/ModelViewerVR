package net.masonapps.modelviewervr.sculpt;

import android.support.annotation.Nullable;

import net.masonapps.modelviewervr.mesh.Vertex;

import java.util.Arrays;
import java.util.Stack;

/**
 * Created by Bob on 7/21/2017.
 */

public class UndoRedoCache {
    private static final int MAX_UNDO_STACK_COUNT = 10;
    private Stack<SaveData[]> undoStack = new Stack<>();
    private Stack<SaveData[]> redoStack = new Stack<>();

    public static void applySaveData(SculptMesh sculptMesh, @Nullable SaveData[] saveData) {
        if (saveData == null) return;
        if (sculptMesh.getMeshData().getVertices().length != saveData.length)
            throw new IllegalArgumentException("save data array must be same length as mesh vertices array");
        for (int i = 0; i < saveData.length; i++) {
            final Vertex vertex = sculptMesh.getMeshData().vertices[i];
            vertex.flagNeedsUpdate();
            vertex.position.set(saveData[i].position);
            vertex.color.set(saveData[i].color);
        }

        Arrays.stream(sculptMesh.getTriangles())
                .forEach(triangle -> {
                    triangle.update();
                    triangle.refitNode();
                });

        Arrays.stream(sculptMesh.getMeshData().getVertices())
                .parallel()
                .filter(Vertex::needsUpdate)
                .forEach(Vertex::recalculateNormal);

        Arrays.stream(sculptMesh.getMeshData().getVertices())
                .filter(Vertex::needsUpdate)
                .forEach(vertex -> {
                    vertex.clearUpdateFlag();
                    sculptMesh.setVertex(vertex);
                });
        sculptMesh.update();
    }

    @Nullable
    public SaveData[] undo() {
        if (undoStack.empty()) return null;
        final SaveData[] saveData = undoStack.pop();
        redoStack.push(saveData);
        return saveData;
    }

    public int getUndoCount() {
        return undoStack.size();
    }

    @Nullable
    public SaveData[] redo() {
        if (redoStack.empty()) return null;
        final SaveData[] saveData = redoStack.pop();
        undoStack.push(saveData);
        return saveData;
    }

    public int getRedoCount() {
        return redoStack.size();
    }

    public void save(Vertex[] vertices) {
        if (undoStack.size() >= MAX_UNDO_STACK_COUNT)
            undoStack.remove(0);
        redoStack.clear();
        final SaveData[] saveData = new SaveData[vertices.length];
        undoStack.push(saveData);
        for (int i = 0; i < vertices.length; i++) {
            final Vertex vertex = vertices[i];
            saveData[i] = new SaveData(vertex.position, vertex.color);
        }
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

}
