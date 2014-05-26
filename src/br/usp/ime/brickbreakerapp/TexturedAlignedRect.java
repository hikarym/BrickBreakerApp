package br.usp.ime.brickbreakerapp;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


/**
 * Represents a two-dimensional axis-aligned textured rectangle.
 */
public class TexturedAlignedRect extends BaseRect {
    private static final String TAG = MainActivity.TAG;

    
    // Similar to BasicAlignedRect, but we need to manage texture data as well.
    

    static final String VERTEX_SHADER_CODE =
            "uniform mat4 u_mvpMatrix;" +       // model/view/projection matrix
            "attribute vec4 a_position;" +      // vertex data for us to transform
            "attribute vec2 a_texCoord;" +      // texture coordinate for vertex...
            "varying vec2 v_texCoord;" +        // ...which we forward to the fragment shader

            "void main() {" +
            "  gl_Position = u_mvpMatrix * a_position;" +
            "  v_texCoord = a_texCoord;" +
            "}";

    static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +        // medium is fine for texture maps
            "uniform sampler2D u_texture;" +    // texture data
            "varying vec2 v_texCoord;" +        // linearly interpolated texture coordinate

            "void main() {" +
            "  gl_FragColor = texture2D(u_texture, v_texCoord);" +
            "}";


    // References to vertex data.
    private static FloatBuffer sVertexBuffer = getVertexArray();

    // Handles to uniforms and attributes in the shader.
    private static int sProgramHandle = -1;
    private static int sTexCoordHandle = -1;
    private static int sPositionHandle = -1;
    private static int sMVPMatrixHandle = -1;

    // Texture data for this instance.
    private int mTextureDataHandle = -1;
    private int mTextureWidth = -1;
    private int mTextureHeight = -1;
    private FloatBuffer mTexBuffer;

    // Sanity check on draw prep.
    private static boolean sDrawPrepared;

    private static float[] sTempMVP = new float[16];


    public TexturedAlignedRect() {
        FloatBuffer defaultCoords = getTexArray();

        // Allocate a FloatBuffer to hold our texture coordinate data, and populate it with
        // default values.  These may be overwritten by setTextureCoords().
        ByteBuffer bb = ByteBuffer.allocateDirect(VERTEX_COUNT * TEX_VERTEX_STRIDE);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(defaultCoords);
        defaultCoords.position(0);      // ugh
        fb.position(0);
        mTexBuffer = fb;
    }

    /**
     * Creates the GL program and associated references.
     */
    public static void createProgram() {
        sProgramHandle = Library.createProgram(VERTEX_SHADER_CODE,
                FRAGMENT_SHADER_CODE);
        Log.d(TAG, "Created program " + sProgramHandle);

        // Get handle to vertex shader's a_position member.
        sPositionHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_position");
        Library.checkGlError("glGetAttribLocation");

        // Get handle to vertex shader's a_texCoord member.
        sTexCoordHandle = GLES20.glGetAttribLocation(sProgramHandle, "a_texCoord");
        Library.checkGlError("glGetAttribLocation");

        // Get handle to transformation matrix.
        sMVPMatrixHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_mvpMatrix");
        Library.checkGlError("glGetUniformLocation");

        // Get handle to texture reference.
        int textureUniformHandle = GLES20.glGetUniformLocation(sProgramHandle, "u_texture");
        Library.checkGlError("glGetUniformLocation");

        // Set u_texture to reference texture unit 0.  (We don't change the value, so we can just
        // set it here.)
        GLES20.glUseProgram(sProgramHandle);
        GLES20.glUniform1i(textureUniformHandle, 0);
        Library.checkGlError("glUniform1i");
        GLES20.glUseProgram(0);

        Library.checkGlError("TexturedAlignedRect setup complete");
    }    

    /**
     * Sets the texture data by creating a new texture from a buffer of data.
     */
    public void setTexture(ByteBuffer buf, int width, int height, int format) {
        mTextureDataHandle =
                Library.createImageTexture(buf, width, height, format);
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
     * Specifies the rectangle within the texture map where the texture data is.  
     *
     * @param coords Coordinates within the texture.
     */
    public void setTextureCoords(Rect coords) {
        // Convert integer rect coordinates to [0.0, 1.0].
        float left = (float) coords.left / mTextureWidth;
        float right = (float) coords.right / mTextureWidth;
        float top = (float) coords.top / mTextureHeight;
        float bottom = (float) coords.bottom / mTextureHeight;

        FloatBuffer fb = mTexBuffer;
        fb.put(left);           // bottom left
        fb.put(bottom);
        fb.put(right);          // bottom right
        fb.put(bottom);
        fb.put(left);           // top left
        fb.put(top);
        fb.put(right);          // top right
        fb.put(top);
        fb.position(0);
    }

    /**
     * Performs setup common to all BasicAlignedRects.
     */
    public static void prepareToDraw() {
        // Select our program.
        GLES20.glUseProgram(sProgramHandle);
        Library.checkGlError("glUseProgram");

        // Enable the "a_position" vertex attribute.
        GLES20.glEnableVertexAttribArray(sPositionHandle);
        Library.checkGlError("glEnableVertexAttribArray");

        // Connect sVertexBuffer to "a_position".
        GLES20.glVertexAttribPointer(sPositionHandle, COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false, VERTEX_STRIDE, sVertexBuffer);
        Library.checkGlError("glEnableVertexAttribPointer");

        // Enable the "a_texCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(sTexCoordHandle);
        Library.checkGlError("glEnableVertexAttribArray");

        sDrawPrepared = true;
    }

    /**
     * Cleans up after drawing.
     */
    public static void finishedDrawing() {
        sDrawPrepared = false;

        // Disable vertex array and program.  Not strictly necessary.
        GLES20.glDisableVertexAttribArray(sPositionHandle);
        GLES20.glUseProgram(0);
    }

    /**
     * Draws the textured rect.
     */
    public void draw() {
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("draw start");
        if (!sDrawPrepared) {
            throw new RuntimeException("not prepared");
        }

        // Connect mTexBuffer to "a_texCoord".
        GLES20.glVertexAttribPointer(sTexCoordHandle, TEX_COORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false, TEX_VERTEX_STRIDE, mTexBuffer);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glVertexAttribPointer");

        // Compute model/view/projection matrix.
        float[] mvp = sTempMVP;     // scratch storage
        Matrix.multiplyMM(mvp, 0, BrickBreakerSurfaceRenderer.mProjectionMatrix, 0, mModelView, 0);

        // Copy the model / view / projection matrix over.
        GLES20.glUniformMatrix4fv(sMVPMatrixHandle, 1, false, mvp, 0);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glUniformMatrix4fv");

        // Set the active texture unit to unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glActiveTexture");

        // In OpenGL ES 1.1 you needed to call glEnable(GLES20.GL_TEXTURE_2D).  This is not
        // required in 2.0, and will actually raise a GL_INVALID_ENUM error.

        // Bind the texture data to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glBindTexture");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, VERTEX_COUNT);
        if (BrickBreakerSurfaceRenderer.EXTRA_CHECK) Library.checkGlError("glDrawArrays");
    }
}
