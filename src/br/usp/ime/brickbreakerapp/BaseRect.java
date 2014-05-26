package br.usp.ime.brickbreakerapp;

import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Base class for our graphical objects.
 */
public class BaseRect {
    
	// Model/view matrix for this object
    protected float[] mModelView;

    // Simple square
    private static final float COORDS[] = {
        -0.5f, -0.5f,   // 0 bottom left
         0.5f, -0.5f,   // 1 bottom right
        -0.5f,  0.5f,   // 2 top left
         0.5f,  0.5f,   // 3 top right
    };

    // Texture coordinates. 
    private static final float TEX_COORDS[] = {
        0.0f,   1.0f,   // bottom left
        1.0f,   1.0f,   // bottom right
        0.0f,   0.0f,   // top left
        1.0f,   0.0f,   // top right
    };

    
    //Square, suitable for GL_LINE_LOOP. 
    private static final float OUTLINE_COORDS[] = {
        -0.5f, -0.5f,   // bottom left
         0.5f, -0.5f,   // bottom right
         0.5f,  0.5f,   // top right
        -0.5f,  0.5f,   // top left
    };

    // Common arrays of vertices.
    private static FloatBuffer sVertexArray = BaseRect.createVertexArray(COORDS);
    private static FloatBuffer sTexArray = BaseRect.createVertexArray(TEX_COORDS);
    private static FloatBuffer sOutlineVertexArray = BaseRect.createVertexArray(OUTLINE_COORDS);
	private static FloatBuffer sUVSArray = BaseRect.createVertexArray(TEX_COORDS);


    public static final int COORDS_PER_VERTEX = 2;         // x,y
    public static final int TEX_COORDS_PER_VERTEX = 2;     // s,t
    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per float
    public static final int TEX_VERTEX_STRIDE = TEX_COORDS_PER_VERTEX * 4;

    // vertex count should be the same for both COORDS and TEX_COORDS
    public static final int VERTEX_COUNT = COORDS.length / COORDS_PER_VERTEX;


    protected BaseRect() {
        // Init model/view matrix, which holds position and scale.
        mModelView = new float[16];
        Matrix.setIdentityM(mModelView, 0);
    }

    /**
     * Allocates a direct float buffer, and populates it with the vertex data.
     */
    private static FloatBuffer createVertexArray(float[] coords) {
        // Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
        ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.position(0);
        return fb;
    }

    /**
     * Returns a FloatBuffer with the vertex data for a unit-size square.  
     */
    public static FloatBuffer getVertexArray() {
        return sVertexArray;
    }
    
    /**
	 * Returns a FloatBuffer with the texture coordinate.
	 */ 
	public static FloatBuffer getUVSArray() {
		return sUVSArray;
	}

    /**
     * Returns a FloatBuffer with the texture coordinate data for an image with (0,0) in the
     * top-left corner.
     */
    public static FloatBuffer getTexArray() {
        return sTexArray;
    }

    /**
     * Returns a FloatBuffer with vertex data suitable for an outline rect (which has to
     * specify the vertices in a different order).
     */
    public static FloatBuffer getOutlineVertexArray() {
        return sOutlineVertexArray;
    }


    /**
     * Returns the X position (arena / world coordinates).
     */
    public float getXPosition() {
        return mModelView[12];
    }

    /**
     * Returns the Y position (arena / world coordinates).
     */
    public float getYPosition() {
        return mModelView[13];
    }

    /**
     * Sets the position in the arena.
     */
    public void setPosition(float x, float y) {
        // column-major 4x4 matrix
        mModelView[12] = x;
        mModelView[13] = y;
    }

    /**
     * Sets the position in the arena (X-coord only).
     */
    public void setXPosition(float x) {
        // column-major 4x4 matrix
        mModelView[12] = x;
    }

    /**
     * Gets the scale value in the X dimension.
     */
    public float getXScale() {
        return mModelView[0];
    }

    /**
     * Gets the scale value in the Y dimension.
     */
    public float getYScale() {
        return mModelView[5];
    }

    /**
     * Sets the size of the rectangle.
     */
    public void setScale(float xs, float ys) {
        // column-major 4x4 matrix
        mModelView[0] = xs;
        mModelView[5] = ys;
    }

    @Override
    public String toString() {
        return "[BaseRect x=" + getXPosition() + " y=" + getYPosition()
                + " xs=" + getXScale() + " ys=" + getYScale() + "]";
    }
}
