package net.masonapps.modelviewervr.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.masonapps.libgdxgooglevr.gfx.Entity;

/**
 * Created by Bob on 6/6/2017.
 */

public class Grid extends Entity {

    private final GridShader gradientShader;

    public Grid(ModelInstance modelInstance) {
        super(modelInstance);
        setLightingEnabled(false);
        gradientShader = new GridShader();
        setShader(gradientShader);
    }

    public static Grid newInstance(float radius, float spacing, Color color, Color fogColor) {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Material material = new Material(new BlendingAttribute(), FloatAttribute.createAlphaTest(0.5f), new IntAttribute(IntAttribute.CullFace, 0));
        final Model rect = modelBuilder.createRect(
                -radius, -radius, 0f,
                radius, -radius, 0f,
                radius, radius, 0f,
                -radius, radius, 0f,
                0f, 0f, 1f,
                material,
                VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates
        );
        return new Grid(new ModelInstance(rect));
    }

    private static class GridShader extends BaseShader {

        private static String vertexShader = null;
        private static String fragmentShader = null;
        private final int u_projTrans = register(new Uniform("u_projTrans"));
        private final int u_worldTrans = register(new Uniform("u_worldTrans"));
        private final int u_color1 = register(new Uniform("u_color1"));
        private final int u_color2 = register(new Uniform("u_color2"));
        private final int u_fogColor = register(new Uniform("u_fogColor"));
        private final int u_fogRadius = register(new Uniform("u_fogRadius"));
        private final int u_spacing = register(new Uniform("u_spacing"));
        private final int u_thickness = register(new Uniform("u_thickness"));
        private final Color color1 = new Color();
        private final Color color2 = new Color(Color.CLEAR);
        private final Color fogColor = new Color();
        private final ShaderProgram program;
        private float fogRadius = 50f;
        private float spacing = 1f;
        private float thickness = 0.025f;

        public GridShader() {
            program = new ShaderProgram(getVertexShader(), getFragmentShader());

            if (!program.isCompiled())
                throw new GdxRuntimeException("Couldn't compile shader " + program.getLog());
            String log = program.getLog();
            if (log.length() > 0)
                Gdx.app.error(Grid.class.getSimpleName(), "Shader compilation log: " + log);
            init();
        }

        public static String getVertexShader() {
            if (vertexShader == null)
                vertexShader = Gdx.files.internal("shaders/grid.vertex.glsl").readString();
            return vertexShader;
        }

        public static String getFragmentShader() {
            if (fragmentShader == null)
                fragmentShader = Gdx.files.internal("shaders/grid.fragment.glsl").readString();
            return fragmentShader;
        }

        @Override
        public void init() {
            super.init(program, null);
        }

        @Override
        public int compareTo(Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(Renderable instance) {
            return true;
        }

        @Override
        public void begin(Camera camera, RenderContext context) {
            program.begin();
            context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
            context.setDepthMask(true);
            set(u_projTrans, camera.combined);
        }

        @Override
        public void render(Renderable renderable) {
            set(u_worldTrans, renderable.worldTransform);
            set(u_color1, color1);
            set(u_color2, color2);
            set(u_fogColor, fogColor);
            set(u_fogRadius, fogRadius);
            set(u_spacing, spacing);
            set(u_thickness, thickness);
            renderable.meshPart.render(program);
        }

        @Override
        public void end() {
            program.end();
        }

        @Override
        public void dispose() {
            super.dispose();
            program.dispose();
        }

        public void setColor1(Color color) {
            this.color1.set(color);
        }

        public void setColor2(Color color) {
            this.color2.set(color);
        }

        public void setFogColor(Color color) {
            this.fogColor.set(color);
        }

        public void setFogRadius(float fogRadius) {
            this.fogRadius = fogRadius;
        }

        public void setSpacing(float spacing) {
            this.spacing = spacing;
        }

        public void setThickness(float thickness) {
            this.thickness = thickness;
        }
    }
}
