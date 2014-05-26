package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextResources {
    private static final String TAG = MainActivity.TAG;
    
    public static final int PAUSE = -2;
    public static final int NO_MESSAGE = -1;        // used to indicate no message shown
    public static final int READY = 0;
    public static final int GAME_OVER = 1;
    public static final int WINNER = 2;             // YOU'RE WINNER !
    public static final int DIGIT_START = 3;
    private static final int STRING_COUNT = DIGIT_START + 10;
    
    private static final int TEXTURE_SIZE = 512;
    
    private static final int TEXT_SIZE = 70;

    // Fancy text parameters.
    private static final int SHADOW_RADIUS = 8;
    private static final int SHADOW_OFFSET = 5;

    // These identify the location of individual items.
    private Rect[] mTextPositions = new Rect[STRING_COUNT];

    // Handle to the image texture that holds all of the strings.
    public int mTextureHandle = -1;

    /**
     * Text string configuration.  
     */
    public static final class Configuration {
        // Strings to draw.
        private final String[] mTextStrings = new String[STRING_COUNT];

        // RGB colors to use when rendering the text.
        private final int[] mTextColors = new int[STRING_COUNT];

        // Add shadows
        private final boolean[] mTextShadows = new boolean[STRING_COUNT];

        /**
         * Extracts strings from Android resource file and prepares internal text data.  Selects
         * colors for text strings.
         */
        private Configuration(Context context) {
            setString(context, READY, R.string.msg_ready, 0xe15c1b);
            setString(context, GAME_OVER, R.string.msg_game_over, 0xff0000);
            setString(context, WINNER, R.string.msg_winner, 0x00ff00);
            for (int i = 0; i < 10; i++) {
                // Just using Arabic numerals here.  No need to pull the string out of the resource.
                mTextStrings[DIGIT_START + i] = String.valueOf((char)('0' + i));
                mTextColors[DIGIT_START + i] = 0xe0e020;//e0e020 , d9db13
                mTextShadows[DIGIT_START + i] = false;
            }
        }

        /** helper for constructor */
        private void setString(Context context, int index, int res, int color) {
            mTextStrings[index] = context.getString(res);
            mTextColors[index] = color;
            mTextShadows[index] = true;
        }

        public String getTextString(int index) {
            return mTextStrings[index];
        }
        public int getTextColor(int index) {
            return mTextColors[index];
        }
        public boolean getTextShadow(int index) {
            return mTextShadows[index];
        }
    }

    /**
     * Initializes configuration data. 
     */
    public static Configuration configure(Context context) {
        return new Configuration(context);
    }

    /**
     * Generates the texture image from the configuration specified earlier.
     */
    public TextResources(Configuration config) {
        createTexture(config);
    }

    private void createTexture(Configuration config) {

        Bitmap bitmap = createTextBitmap(config);

        // Create texture storage.
        int handles[] = new int[1];
        GLES20.glGenTextures(1, handles, 0);
        // REVISAR 
        //Util.checkGlError("glGenTextures");
        mTextureHandle = handles[0];

        // Bind the texture data to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureHandle);

        // Linear scaling so the text doesn't look chunky.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        // Load the bitmap into a texture using the Android utility function.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // Don't need this anymore.
        bitmap.recycle();
    }

    /**
     * Creates a bitmap with the various strings.
     */
    private Bitmap createTextBitmap(Configuration config) {
        /*
         * In everything that follows we're working in image coordinates, for which (0,0) is
         * at the top left rather than the bottom left.
         */

        Bitmap bitmap = Bitmap.createBitmap(TEXTURE_SIZE, TEXTURE_SIZE, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(0x00000000);      // transparent black background
        //bitmap.eraseColor(0xffffffff);      // debug -- use opaque white background

        Paint textPaint = new Paint();
        Typeface typeface = Typeface.defaultFromStyle(Typeface.BOLD);
        textPaint.setTypeface(typeface);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setAntiAlias(true);

        int startX = 0;
        int startY = 0;
        int lineHeight = 0;
        for (int i = 0; i < STRING_COUNT; i++) {
            // Get text dimensions.
            String str = config.getTextString(i);
            textPaint.setColor(0xff000000 | config.getTextColor(i));
            if (config.getTextShadow(i)) {
                textPaint.setShadowLayer(SHADOW_RADIUS, SHADOW_OFFSET, SHADOW_OFFSET, 0xff000000);
            } else {
                textPaint.setShadowLayer(0, 0, 0, 0);
            }
            Rect boundsRect = new Rect();
            textPaint.getTextBounds(str, 0, str.length(), boundsRect);
            if (config.getTextShadow(i)) {
                boundsRect.right += SHADOW_RADIUS + SHADOW_OFFSET;
                boundsRect.bottom += SHADOW_RADIUS + SHADOW_OFFSET;
            }

            if (boundsRect.width() > TEXTURE_SIZE || boundsRect.height() > TEXTURE_SIZE) {
                Log.w(TAG, "HEY: text string '" + str + "' is too big: " + boundsRect);
            }

            if (startX != 0 && startX + boundsRect.width() > TEXTURE_SIZE) {
                // Ran out of room on this line, move down to next section.
                startX = 0;
                startY += lineHeight;
                lineHeight = 0;

                if (startY >= TEXTURE_SIZE) {
                    Log.w(TAG, "HEY: fell off the bottom of the message texture");
                }
            }

            // Draw the text at an offset that will yield a bounds rect at (startX,startY),
            // and store the bounds in our table.
            canvas.drawText(str, startX - boundsRect.left, startY - boundsRect.top, textPaint);
            boundsRect.offsetTo(startX, startY);
            mTextPositions[i] = boundsRect;
            lineHeight = Math.max(lineHeight, boundsRect.height() + 1);
            startX += boundsRect.width() + 1;
        }

        return bitmap;
    }

    /**
     * Returns the number of strings we know about.
     */
    public static int getNumStrings() {
        return STRING_COUNT;
    }

    /**
     * Returns a handle to the texture.
     */
    public int getTextureHandle() {
        return mTextureHandle;
    }

    /**
     * Texture width, in pixels.
     */
    public int getTextureWidth() {
        return TEXTURE_SIZE;
    }

    /**
     * Texture height, in pixels.
     */
    public int getTextureHeight() {
        return TEXTURE_SIZE;
    }

    /**
     * Returns a Rect that bounds the text with the specified index.
     */
    public Rect getTextureRect(int index) {
        return mTextPositions[index];
    }
}

