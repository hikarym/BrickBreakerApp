package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * A handful of utility functions.
 */
public class Library {
    private static final String TAG = MainActivity.TAG;
    
    private static int genRandomNumber(int min, int max){
		return min + (int)(Math.random() * ((max - min) + 1));
	}

    /**
     * Creates a texture from raw data.
     *
     * @param data Image data.
     * @param width Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createImageTexture(ByteBuffer data, int width, int height, int format) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        Library.checkGlError("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        Library.checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
                width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        Library.checkGlError("loadImageTexture");

        return textureHandle;
    }
    
    public static int createImageTexture(Bitmap bmp) {
		int[] textureHandles = new int[1];
		int textureHandle;

		GLES20.glGenTextures(1, textureHandles, 0);
		textureHandle = textureHandles[0];
		Library.checkGlError("glGenTextures");

		// Bind the texture handle to the 2D texture target.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

		// Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
		// is smaller or larger than the source image.
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		Library.checkGlError("loadImageTexture");
		
		// Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
		Library.checkGlError("loadImageTexture");
		
		// We are done using the bitmap so we should recycle it.
		//bmp.recycle();

		return textureHandle;
	}

    /**
     * Loads a shader from a string and compiles it.
     *
     * @param type GL shader type, e.g. GLES20.GL_VERTEX_SHADER.
     * @param shaderCode Shader source code.
     * @return Handle to shader.
     */
    public static int loadShader(int type, String shaderCode) {
        int shaderHandle = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shaderHandle, shaderCode);
        GLES20.glCompileShader(shaderHandle);

        // Check for failure.
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] != GLES20.GL_TRUE) {
            // Extract the detailed failure message.
            String msg = GLES20.glGetShaderInfoLog(shaderHandle);
            GLES20.glDeleteProgram(shaderHandle);
            Log.e(TAG, "glCompileShader: " + msg);
            throw new RuntimeException("glCompileShader failed");
        }

        return shaderHandle;
    }

    /**
     * Creates a program, given source code for vertex and fragment shaders.
     *
     * @param vertexShaderCode Source code for vertex shader.
     * @param fragmentShaderCode Source code for fragment shader.
     * @return Handle to program.
     */
    public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        // Load the shaders.
        int vertexShader =
                Library.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader =
                Library.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Build the program.
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShader);
        GLES20.glAttachShader(programHandle, fragmentShader);
        GLES20.glLinkProgram(programHandle);

        // Check for failure.
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            // Extract the detailed failure message.
            String msg = GLES20.glGetProgramInfoLog(programHandle);
            GLES20.glDeleteProgram(programHandle);
            Log.e(TAG, "glLinkProgram: " + msg);
            throw new RuntimeException("glLinkProgram failed");
        }

        return programHandle;
    }


    /**
     * Utility method for checking for OpenGL errors.  
     *
     * @param msg string to display in the error message (usually the name of the last
     *      GL operation)
     */
    public static void checkGlError(String msg) {
        int error, lastError = GLES20.GL_NO_ERROR;

        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, msg + ": glError " + error);
            lastError = error;
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(msg + ": glError " + lastError);
        }
    }
    
    /**
	 * Build a brick configuration of the game
	 * (Each brick must be a value between 0 e 4) 
	 * @param configStr: array[001111100, 001111100, 000232000, 000232000, 001111100, 001111100])
	 * (The array must be BRICK_ROWS elements and 
	 * each string must be have BRICK_COLUMNS characters)
	 * 
	 */
	public static int[][] buildBrickStatesConfig(int rows, int columns, String[] configStr){
		int[][] mBrickStatesConfig = new int[rows][columns];
		for (int i = 0; i < rows; i++) {
			
			for (int j = 0; j < columns; j++) {				
				mBrickStatesConfig[i][j] = Integer.parseInt(
						String.valueOf(configStr[i].charAt(j)));
			}
		}
		Log.v(TAG, "param.mBrickStatesConfig1:"+String.valueOf(mBrickStatesConfig[5][8]));
		return mBrickStatesConfig;
		
	}
	
	/**
	 * Get a bitmap object from a source
	 * @param context
	 * @param src i.e: "drawable/background_3" 
	 * @return a Bitmap object
	 */
	public static Bitmap getBitmapTexture(Context context, String src){
		int id = context.getResources().getIdentifier(src, null, context.getPackageName());		
		// Temporary create a bitmap
		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
		return bmp;
	}
}
