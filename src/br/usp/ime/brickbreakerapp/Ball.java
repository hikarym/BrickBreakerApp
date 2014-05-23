package br.usp.ime.brickbreakerapp;

import android.graphics.Rect;
import android.opengl.GLES20;

import java.nio.ByteBuffer;

/*
 * Ball object
 */
public class Ball extends TexturedAlignedRect {
	private static final String TAG = MainActivity.TAG;
	
	private static final int TEX_SIZE = 64;        // dimension for square texture (power of 2)
	private static final int DATA_FORMAT = GLES20.GL_RGBA;  // 8bpp RGBA
	private static final int BYTES_PER_PIXEL = 4;
	
	// Normalized motion vector.
	private float mMotionX;
	private float mMotionY;
	
	// Speed, expressed in terms of steps per second.  A speed of 60 will move the ball
	// 60 arena-units per second, or 1 unit per frame on a 60Hz device.  This is not the same
	// as 1 *pixel* per frame unless the arena units happen to match up.
	private int mSpeed;
	
	public Ball() {
		setTexture(generateBallTexture(), TEX_SIZE, TEX_SIZE, DATA_FORMAT);
		// Ball diameter is an odd number of pixels.
		setTextureCoords(new Rect(0, 0, TEX_SIZE-1, TEX_SIZE-1));
	}
	
	//---Gets the motion vector X component.
	public float getXDirection() {
		return mMotionX;
	}
	
	//---Gets the motion vector Y component.
	public float getYDirection() {
		return mMotionY;
	}
	
	//---Sets the motion vector.  Input values will be normalized.
	public void setDirection(float deltaX, float deltaY) {
		float mag = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		mMotionX = deltaX / mag;
		mMotionY = deltaY / mag;
	}
	
    /**
     * Gets the speed, in arena-units per second.
     */
    public int getSpeed() {
        return mSpeed;
    }

    /**
     * Sets the speed, in arena-units per second.
     */
    public void setSpeed(int speed) {
        if (speed <= 0) {
            throw new RuntimeException("speed must be positive (" + speed + ")");
        }
        mSpeed = speed;
    }

    /**
     * Gets the ball's radius, in arena units.
     */
    public float getRadius() {
        // The "scale" value indicates diameter.
        return getXScale() / 2.0f;
    }

    /**
     * Generates the ball texture.  This is a simple filled circle in a solid color, with
     * a transparent black background.
     *
     * @return A direct ByteBuffer with pre-multiplied RGBA data.
     */
    private ByteBuffer generateBallTexture() {
        byte[] buf = new byte[TEX_SIZE * TEX_SIZE * BYTES_PER_PIXEL];
        
        int left[] = new int[TEX_SIZE-1];
        int right[] = new int[TEX_SIZE-1];
        computeCircleEdges(TEX_SIZE/2 - 1, left, right);

        // Render the edge list as a filled circle.
        for (int y = 0; y < left.length; y++) {
            int xleft = left[y];
            int xright = right[y];

            for (int x = xleft ; x <= xright; x++) {
                int offset = (y * TEX_SIZE + x) * BYTES_PER_PIXEL;
                buf[offset]   = (byte) 0xff;    // red
                buf[offset+1] = (byte) 0xff;    // green
                buf[offset+2] = (byte) 0xff;    // blue
                buf[offset+3] = (byte) 0xff;    // alpha
            }
        }

        // Create a ByteBuffer, copy the data over, and (very important) reset the position.
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(buf.length);
        byteBuf.put(buf);
        byteBuf.position(0);
        return byteBuf;
    }

    /**
     * Computes the left and right edges of a rasterized circle, using Bresenham's algorithm.
     *
     * @param rad Radius.
     * @param left Left edge index, range [0, rad].  Array must hold (rad*2+1) elements.
     * @param right Right edge index, range [rad, rad*2 + 1].
     */
    private static void computeCircleEdges(int rad, int[] left, int[] right) {
        /* (also available in 6502 assembly) */
        int x, y, d;

        d = 1 - rad;
        x = 0;
        y = rad;

        // Walk through one quadrant, setting the other three as reflections.
        while (x <= y) {
            setCircleValues(rad, x, y, left, right);

            if (d < 0) {
                d = d + (x << 2) + 3;
            } else {
                d = d + ((x - y) << 2) + 5;
                y--;
            }
            x++;
        }
    }

    /**
     * Sets the edge values for four quadrants based on values from the first quadrant.
     */
    private static void setCircleValues(int rad, int x, int y, int[] left, int[] right) {
        left[rad+y] = left[rad-y] = rad - x;
        left[rad+x] = left[rad-x] = rad - y;
        right[rad+y] = right[rad-y] = rad + x;
        right[rad+x] = right[rad-x] = rad + y;
    }


    // Colors for the test texture, in little-endian RGBA.
    public static final int BLACK = 0x00000000;
    public static final int RED = 0x000000ff;
    public static final int GREEN = 0x0000ff00;
    public static final int BLUE = 0x00ff0000;
    public static final int MAGENTA = RED | BLUE;
    public static final int YELLOW = RED | GREEN;
    public static final int CYAN = GREEN | BLUE;
    public static final int WHITE = RED | GREEN | BLUE;
    public static final int OPAQUE = (int) 0xff000000L;
    public static final int HALF = (int) 0x80000000L;
    public static final int LOW = (int) 0x40000000L;
    public static final int TRANSP = 0;

    public static final int GRID[] = new int[] {    // must be 16 elements
        OPAQUE|RED,     OPAQUE|YELLOW,  OPAQUE|GREEN,   OPAQUE|MAGENTA,
        OPAQUE|WHITE,   LOW|RED,        LOW|GREEN,      OPAQUE|YELLOW,
        OPAQUE|MAGENTA, TRANSP|GREEN,   HALF|RED,       OPAQUE|BLACK,
        OPAQUE|CYAN,    OPAQUE|MAGENTA, OPAQUE|CYAN,    OPAQUE|BLUE,
    };

    /**
     * Generates a test texture.  We want to create a 4x4 block pattern with obvious color
     * values in the corners, so that we can confirm orientation and coverage.  We also
     * leave a couple of alpha holes to check that channel.
     *
     * Like most image formats, the pixel data begins with the top-left corner, which is
     * upside-down relative to OpenGL conventions.  The texture coordinates should be flipped
     * vertically.  Using an asymmetric patterns lets us check that we're doing that right.
     *
     * Colors use pre-multiplied alpha (so set glBlendFunc appropriately).
     *
     * @return A direct ByteBuffer with the 8888 RGBA data.
     */
    private ByteBuffer generateTestTexture() {
        byte[] buf = new byte[TEX_SIZE * TEX_SIZE * BYTES_PER_PIXEL];
        final int scale = TEX_SIZE / 4;        // convert 64x64 --> 4x4

        for (int i = 0; i < buf.length; i += BYTES_PER_PIXEL) {
            int texRow = (i / BYTES_PER_PIXEL) / TEX_SIZE;
            int texCol = (i / BYTES_PER_PIXEL) % TEX_SIZE;

            int gridRow = texRow / scale;  // 0-3
            int gridCol = texCol / scale;  // 0-3
            int gridIndex = (gridRow * 4) + gridCol;  // 0-15

            int color = GRID[gridIndex];

            // override the pixels in two corners to check coverage
            if (i == 0) {
                color = OPAQUE | WHITE;
            } else if (i == buf.length - BYTES_PER_PIXEL) {
                color = OPAQUE | WHITE;
            }

            // extract RGBA; use "int" instead of "byte" to get unsigned values
            int red = color & 0xff;
            int green = (color >> 8) & 0xff;
            int blue = (color >> 16) & 0xff;
            int alpha = (color >> 24) & 0xff;

            // pre-multiply colors and store in buffer
            float alphaM = alpha / 255.0f;
            buf[i] = (byte) (red * alphaM);
            buf[i+1] = (byte) (green * alphaM);
            buf[i+2] = (byte) (blue * alphaM);
            buf[i+3] = (byte) alpha;
        }

        ByteBuffer byteBuf = ByteBuffer.allocateDirect(buf.length);
        byteBuf.put(buf);
        byteBuf.position(0);
        return byteBuf;
    }
}
