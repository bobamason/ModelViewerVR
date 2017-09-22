package net.masonapps.modelviewervr.sculpt;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

import net.masonapps.modelviewervr.mesh.MeshData;
import net.masonapps.modelviewervr.mesh.Vertex;

/**
 * Created by Bob on 9/19/2017.
 */
public class SaveData {
    public Vector3 position = new Vector3();
    public Color color = new Color();
    public int index = -1;

    public SaveData(Vector3 position, Color color) {
        this.position.set(position);
        this.color.set(color);
    }

    public SaveData(Vector3 position, Color color, int index) {
        this.position.set(position);
        this.color.set(color);
        this.index = index;
    }

    public static SaveDataHolder fromSculptMeshData(MeshData meshData) {
        final SaveData[] saveData = new SaveData[meshData.getVertexCount()];
        for (int i = 0; i < saveData.length; i++) {
            final Vertex v = meshData.getVertex(i);
            saveData[i] = new SaveData(v.position, v.color);
        }
        return new SaveDataHolder(saveData, meshData.getOriginalName());
    }

    public static class SaveDataHolder {
        public SaveData[] saveData;
        public String originalAssetName;

        public SaveDataHolder() {
            saveData = new SaveData[0];
            originalAssetName = "";
        }

        public SaveDataHolder(SaveData[] saveData, String originalAssetName) {
            this.saveData = saveData;
            this.originalAssetName = originalAssetName;
        }
    }
}
