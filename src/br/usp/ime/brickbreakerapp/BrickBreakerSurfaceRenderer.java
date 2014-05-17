package br.usp.ime.brickbreakerapp;

//import java.nio.ByteBuffer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.ConditionVariable;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Main game display class.
 * <p>
 * The methods here expect to run on the Renderer thread.  Calling them from other threads
 * must be done through GLSurfaceView.queueEvent().
 */
public class BrickBreakerSurfaceRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = MainActivity.TAG;
    public static final boolean EXTRA_CHECK = true;         // enable additional assertions

    // Orthographic projection matrix.  Must be updated when the available screen area
    // changes (e.g. when the device is rotated).
    static final float mProjectionMatrix[] = new float[16];

    // Size and position of the GL viewport, in screen coordinates.  If the viewport covers the
    // entire screen, the offsets will be zero and the width/height values will match the
    // size of the display.  (This is one of the few places where we deal in actual pixels.)
    private int mViewportWidth, mViewportHeight;
    private int mViewportXoff, mViewportYoff;

    private BrickBreakerSurfaceView mSurfaceView;
    private BrickBreakerState mBrickBreakerState;
    private TextResources.Configuration mTextConfig;
    private Context mContext;
    
    private boolean started=false;

    public void setStarted(boolean value){
    	started=value;
    }
    public boolean getStarted(){
    	return started;
    }

    /**
     * Constructs the Renderer.  We need references to the BrickBreakerState, so we can tell it to
     * update and draw things, and to the SurfaceView, so we can tell it to stop animating
     * when the game is over.
     */
    public BrickBreakerSurfaceRenderer(Context context, BrickBreakerState BrickBreakerState, BrickBreakerSurfaceView surfaceView,
            TextResources.Configuration textConfig) {
    	mContext = context;
        mSurfaceView = surfaceView;
        mBrickBreakerState = BrickBreakerState;
        mTextConfig = textConfig;
    }

    /**
     * Handles initialization when the surface is created.  This generally happens when the
     * activity is started or resumed.  In particular, this is called whenever the device
     * is rotated.
     * <p>
     * All OpenGL state, including programs, must be (re-)generated here.
     */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        if (EXTRA_CHECK) Library.checkGlError("onSurfaceCreated start");

        // Generate programs and data.
        BasicAlignedRect.createProgram();
        TexturedAlignedRect.createProgram();
        TexturedBasicAlignedRect.createProgram();

        // Allocate objects associated with the various graphical elements.
        BrickBreakerState BrickBreakerState = mBrickBreakerState;
        BrickBreakerState.setTextResources(new TextResources(mTextConfig));
        BrickBreakerState.allocBorders();        
        BrickBreakerState.allocBackground(mContext);
        BrickBreakerState.allocBricks(mContext);
        BrickBreakerState.allocPaddle(mContext);
        BrickBreakerState.allocBall();
        BrickBreakerState.allocScore();
        BrickBreakerState.allocMessages();
        BrickBreakerState.allocDebugStuff();

        // Restore game state from static storage.
        BrickBreakerState.restore();

        // Set the background color.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Disable depth testing -- we're 2D only.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Don't need backface culling.  (If you're feeling pedantic, you can turn it on to
        // make sure we're defining our shapes correctly.)
        if (EXTRA_CHECK) {
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        } else {
            GLES20.glDisable(GLES20.GL_CULL_FACE);
        }

        if (EXTRA_CHECK) Library.checkGlError("onSurfaceCreated end");
    }

    /**
     * Updates the configuration when the underlying surface changes.  Happens at least once
     * after every onSurfaceCreated().
     * <p>
     * If we visit the home screen and immediately return, onSurfaceCreated() may not be
     * called, but this method will.
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        /*
         * We want the viewport to be proportional to the arena size.  That way a 10x10
         * object in arena coordinates will look square on the screen, and our round ball
         * will look round.
         *
         * If we wanted to fill the entire screen with our game, we would want to adjust the
         * size of the arena itself, not just stretch it to fit the boundaries.  This can have
         * subtle effects on gameplay, e.g. the time it takes the ball to travel from the top
         * to the bottom of the screen will be different on a device with a 16:9 display than on
         * a 4:3 display.  Other games might address this differently, e.g. a side-scroller
         * could display a bit more of the level on the left and right.
         *
         * We do want to fill as much space as we can, so we should either be pressed up against
         * the left/right edges or top/bottom.
         *
         * Our game plays best in portrait mode.  We could force the app to run in portrait
         * mode (by setting a value in AndroidManifest, or by setting the projection to rotate
         * the world to match the longest screen dimension), but that's annoying, especially
         * on devices that don't rotate easily (e.g. plasma TVs).
         */

        if (EXTRA_CHECK) Library.checkGlError("onSurfaceChanged start");

        float arenaRatio = BrickBreakerState.ARENA_HEIGHT / BrickBreakerState.ARENA_WIDTH;
        int x, y, viewWidth, viewHeight;

        if (height > (int) (width * arenaRatio)) {
            // limited by narrow width; restrict height
            viewWidth = width;
            viewHeight = (int) (width * arenaRatio);
        } else {
            // limited by short height; restrict width
            viewHeight = height;
            viewWidth = (int) (height / arenaRatio);
        }
        x = (width - viewWidth) / 2;
        y = (height - viewHeight) / 2;

        Log.d(TAG, "onSurfaceChanged w=" + width + " h=" + height);
        Log.d(TAG, " --> x=" + x + " y=" + y + " gw=" + viewWidth + " gh=" + viewHeight);

        GLES20.glViewport(x, y, viewWidth, viewHeight);

        mViewportWidth = viewWidth;
        mViewportHeight = viewHeight;
        mViewportXoff = x;
        mViewportYoff = y;

        // Create an orthographic projection that maps the desired arena size to the viewport
        // dimensions.
        //
        // If we reversed {0, ARENA_HEIGHT} to {ARENA_HEIGHT, 0}, we'd have (0,0) in the
        // upper-left corner instead of the bottom left, which is more familiar for 2D
        // graphics work.  It might cause brain ache if we want to mix in 3D elements though.
        Matrix.orthoM(mProjectionMatrix, 0,  0, BrickBreakerState.ARENA_WIDTH,
                0, BrickBreakerState.ARENA_HEIGHT,  -1, 1);

        // Nudge game state after the surface change.
        mBrickBreakerState.surfaceChanged();
        
        

        if (EXTRA_CHECK) Library.checkGlError("onSurfaceChanged end");
    }  
    
    //-----------------------
    /**
     * Advances game state, then draws the new frame.
     */
    @Override
    public void onDrawFrame(GL10 unused) {
    	
    	
        BrickBreakerState BrickBreakerState = mBrickBreakerState;

        if (started)     		
        	BrickBreakerState.calculateNextFrame();

        // Simulate slow game state update, to see impact on animation.
//        try { Thread.sleep(33); }
//        catch (InterruptedException ie) {}

        if (EXTRA_CHECK) Library.checkGlError("onDrawFrame start");

        // Clear entire screen to background color.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Draw the various elements.  These are all BasicAlignedRect.
     // Draw the various elements.  These are all BasicAlignedRect.
        BasicAlignedRect.prepareToDraw();
        BrickBreakerState.drawBorders();
        BasicAlignedRect.finishedDrawing();
        
        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND);
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE /*GL_SRC_ALPHA*/, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        TexturedBasicAlignedRect.prepareToDraw();
        BrickBreakerState.drawBackground();    
        BrickBreakerState.drawPaddle();
        TexturedBasicAlignedRect.finishedDrawing();
        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND);
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE /*GL_SRC_ALPHA*/, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        BasicAlignedRect.prepareToDraw();
        BrickBreakerState.drawBricks();
//        BrickBreakerState.drawPaddle();
        BasicAlignedRect.finishedDrawing();
        
              

        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND);
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE /*GL_SRC_ALPHA*/, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        TexturedAlignedRect.prepareToDraw();
        BrickBreakerState.drawScore();
        BrickBreakerState.drawBall();
        BrickBreakerState.drawMessages();
        
        TexturedAlignedRect.finishedDrawing();

        if (!started) 
    		return;
        
        BrickBreakerState.drawDebugStuff();

        // Turn alpha blending off.
        GLES20.glDisable(GLES20.GL_BLEND);

        if (EXTRA_CHECK) Library.checkGlError("onDrawFrame end");

        // Stop animating at 60fps (or whatever the refresh rate is) if the game is over.  Once
        // we do this, we won't get here again unless something explicitly asks the system to
        // render a new frame.  (As a handy side-effect, this prevents the paddle from actively
        // moving after the game is over.)
        //
        // It's a bit clunky to be polling for this, but it needs to be controlled by BrickBreakerState,
        // and that class doesn't otherwise need to call back into us or have access to the
        // GLSurfaceView.
        if (!BrickBreakerState.isAnimating()) {
            Log.d(TAG, "Game over, stopping animation");
            // While not explicitly documented as such, it appears that setRenderMode() may be
            // called from any thread.  The getRenderMode() function is documented as being
            // available from any thread, and looking at the sources reveals setRenderMode()
            // uses the same synchronization.  If it weren't allowed, we'd need to post an
            // event to the UI thread to do this.
            mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
    }

    /**
     * Handles pausing of the game Activity.  This is called by the View (via queueEvent) at
     * pause time.  It tells BrickBreakerState to save its state.
     *
     * @param syncObj Object to notify when we have finished saving state.
     */
    public void onViewPause(ConditionVariable syncObj) {
        /*
         * We don't explicitly pause the game action, because the main game loop is being driven
         * by the framework's calls to our onDrawFrame() callback.  If we were driving the updates
         * ourselves we'd need to do something more.
         */

        mBrickBreakerState.save();

        syncObj.open();
    }

    /**
     * Updates state after the player touches the screen.  Call through queueEvent().
     */
    public void touchEvent(float x, float y) {
        /*
         * We chiefly care about the 'x' value, which is used to set the paddle position.  We
         * might want to limit based on the 'y' value because it's a little weird to be
         * controlling the paddle from the top half of the screen, but there's no need to
         * do so.
         *
         * We need to re-scale x,y from window coordinates to arena coordinates.  The viewport
         * may not fill the entire device window, so it's possible to get values that are
         * outside the arena range.
         *
         * If we were directly implementing other on-screen controls, we'd need to check for
         * touches here.
         */

        float arenaX = (x - mViewportXoff) * (BrickBreakerState.ARENA_WIDTH / mViewportWidth);
        float arenaY = (y - mViewportYoff) * (BrickBreakerState.ARENA_HEIGHT / mViewportHeight);
        //Log.v(TAG, "touch at x=" + (int) x + " y=" + (int) y + " --> arenaX=" + (int) arenaX);

        mBrickBreakerState.movePaddle(arenaX);
    }
}
