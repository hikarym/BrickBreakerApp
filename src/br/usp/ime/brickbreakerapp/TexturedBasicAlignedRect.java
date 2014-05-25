package br.usp.ime.brickbreakerapp;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Represents a two-dimensional axis-aligned solid-color rectangle.
 */
public class TexturedBasicAlignedRect extends BaseRect {
	private static final String TAG = MainActivity.TAG;
	
	/* SHADER Solid
	 * 
	 * This shader is for rendering a colored primitive.
	 * 
	 */
    static final String VERTEX_SHADER_CODE =
            "uniform mat4 u_mvpMatrix;" +
            "attribute vec4 a_position;" +

            "void main() {" +
            "  gl_Position = u_mvpMatrix * a_position;" +
            "}";

    static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "uniform vec4 u_color;" +

            "void main() {" +
            "  gl_FragColor = u_color;" +
            "}";
    /* SHADER Image
	 * 
	 * This shader is for rendering 2D images straight from a texture
	 * No additional effects.
	 * 
	 */
	static final String VERTEX_SHADER_CODE_IMAGE =
		"uniform mat4 u_mvpMatrix;" +
		"attribute vec4 a_position;" +
		"attribute vec2 a_texCoord;" +
		"varying vec2 v_texCoord;" +
	    "void main() {" +
	    "  gl_Position = u_mvpMatrix * a_position;" +
	    "  v_texCoord = a_texCoord;" +
	    "}";
	
	static final String FRAGMENT_SHADER_CODE_IMAGE =
		"precision mediump float;" +
	    "varying vec2 v_texCoord;" +
        "uniform sampler2D u_texture;" +
	    "void main() {" +
	    "  gl_FragColor = texture2D( u_texture, v_texCoord );" +
	    "}"; 

    // Reference to vertex data.
    static FloatBuffer sVertexBuffer = getVertexArray();
	//public ShortBuffer sDrawListBuffer = getIndicesArray();
    static FloatBuffer sUVBuffer = getUVSArray();
    //static FloatBuffer sVertices = getVerticesArray();

    // Handles to the GL program and various components of it.
    // Handles to uniforms and attributes in the shader.
    static int sProgramHandle = -1;
    //static int sColorHandle = -1;
    static int sPositionHandle = -1;
    static int sMVPMatrixHandle = -1;
    static int sTexCoordHandle = -1;
    static int sTextureHandle = -1;
    
    // Texture data for this instance.
    private int mTextureDataHandle = -1;
    int mTextureWidth = -1;
    int mTextureHeight = -1;

    // RGBA color vector.
    float[] mColor = new float[4];
    
    // Sanity check on draw prep.
    private static boolean sDrawPrepared;
    
    static float[] sTempMVP = new float[16];


    /**
     * Creates the GL program and associated references.
     */
    public static void createProgram() {
        sProgramHandle = Library.createProgram(VERTEX_SHADER_CODE_IMAGE,
                FRAGMENT_SHADER_CODE_IMAGE);
        Log.d(TAG, "Created program " + sProgramHandle);
        
        //----RENDER()        
        // get handle to vertex shader's a_position member
        sPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_position");
        Library.checkGlError("glGetAttribLocation");

        // Get handle to texture coordinates location
        sTexCoordHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_texCoord");
        Library.checkGlError("glGetAttribLocation");
        
        // get Handle to textures locations
        sTextureHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_texture");
        Library.checkGlError("glGetUniformLocation");

        // get handle to transformation matrix
        sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");
        Library.checkGlError("glGetUniformLocation");
    }

    /**
     * Sets the color.
     */
    public void setColor(float r, float g, float b) {
        Library.checkGlError("setColor start");
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        mColor[3] = 1.0f;
    }

    /**
     * Returns a four-element array with the RGBA color info.  The caller must not modify
     * the values in the returned array.
     */
    public float[] getColor() {
        return mColor;
    }
    
    /*border*/
    /**
     * Sets the texture data by creating a new texture from a buffer of data.
     */
    public void setTexture(ByteBuffer buf, int width, int height, int format) {
        mTextureDataHandle = Library.createImageTexture(buf, width, height, format);
        mTextureWidth = width;
        mTextureHeight = height;
    }
    
    public void setTexture(Bitmap bmp) {
        mTextureDataHandle = Library.createImageTexture(bmp);
        //mTextureWidth = width;
        //mTextureHeight = height;
    }

    /**
     * Sets the texture data to the specified texture handle.
     *
     * @param handle GL texture handle.
     * @param width Width of the texture (in texels).
     * @param height Height of the texture (in texels).
     */
    public void setTexture(int handle, int width, int height) {
        mTextureDataHandle = handle;
        mTextureWidth = width;
        mTextureHeight = height;
    }

    /**
     * Performs setup common to all BasicAlignedRects.
     */
    public static void prepareToDraw() {

        // Select the program.
        GLES20.glUseProgram(sProgramHandle);
        Library.checkGlError("glUseProgram");

        // Enable the "a_position" vertex attribute.
        GLES20.glEnableVertexAttribArray(sPositionHandle);
        Library.checkGlError("glEnableVertexAttribArray");

        // Connect sVertexBuffer to "a_position".
        GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX,
        		GLES20.GL_FLOAT, false, VERTEX_STRIDE, sVertexBuffer);
        Library.checkGlError("glVertexAttribPointer");
        
        //----------------------------
        // Enable the "a_texCoord" vertex attribute
        GLES20.glEnableVertexAttribArray(sTexCoordHandle);
        Library.checkGlError("glEnableVertexAttribArray");
        // Prepare the texture coordinates
        //Connect sTexCoordHandle to "a_texCoord"
        GLES20.glVertexAttribPointer(sTextureHandle, COORDS_PER_VERTEX, 
        		GLES20.GL_FLOAT, false, VERTEX_STRIDE, sUVBuffer);
        

        sDrawPrepared = true;
    }

    /**
     * Cleans up after drawing.
     */
    public static void finishedDrawing() {
        sDrawPrepared = false;

        // Disable vertex array and program.  Not strictly necessary.
        GLES20.glDisableVertexAttribArray(sPositionHandle);
        GLES20.glDisableVertexAttribArray(sTexCoordHandle);
        GLES20.glUseProgram(0);
    }

    /**
     * Draws the rect.
     */
    public void draw() {
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("draw start");
        if (!sDrawPrepared) {
            throw new RuntimeException("not prepared");
        }

        // Compute model/view/projection matrix.
        float[] mvp = sTempMVP;     // scratch storage
        Matrix.multiplyMM(mvp, 0, BrickBreakerSurfaceRenderer.mProjectionMatrix, 0, mModelView, 0);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(sMVPMatrixHandle, 1, false, mvp, 0);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glUniformMatrix4fv");
        
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(sTextureHandle, 0);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glUniform1i");
        
        //------------------------
        // Set the active texture unit to unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glActiveTexture");
        
        // Bind the texture data to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glBindTexture");
        //------------------------
        
        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glDrawArrays");
    }
}

