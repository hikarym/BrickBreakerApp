package br.usp.ime.brickbreakerapp;

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
        BrickBreakerState.allocBall(mContext);
        BrickBreakerState.allocScore();
        BrickBreakerState.allocMessages();
        BrickBreakerState.allocButtonQuit(mContext);
        BrickBreakerState.allocButtonReloadLevel(mContext);
        BrickBreakerState.allocButtonNextLevel(mContext);
        BrickBreakerState.allocDebugStuff();

        // Restore game state from static storage.
        BrickBreakerState.restore();

        // Set the background color.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Disable depth testing -- we're 2D only.
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
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
     */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {      

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

        // Create an orthographic projection that maps the desired arena  size
        // to the viewport dimensions.
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

        //if (started){     		
        	Log.v(TAG, "CRIANDO NoVO FRAME");
        	BrickBreakerState.calculateNextFrame();
        //}

        // Simulate slow game state update, to see impact on animation.
        // try { Thread.sleep(33); }
        // catch (InterruptedException ie) {}

        if (EXTRA_CHECK) Library.checkGlError("onDrawFrame start");

        // Clear entire screen to background color.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

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
        BrickBreakerState.drawBricks();
        BrickBreakerState.drawPaddle();
        TexturedBasicAlignedRect.finishedDrawing();
        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND);
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE /*GL_SRC_ALPHA*/, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        TexturedAlignedRect.prepareToDraw();
        BrickBreakerState.drawScore();
        BrickBreakerState.drawBall();
        BrickBreakerState.drawMessages();        
        TexturedAlignedRect.finishedDrawing();
        
        // Enable alpha blending.
        GLES20.glEnable(GLES20.GL_BLEND);
        // Blend based on the fragment's alpha value.
        GLES20.glBlendFunc(GLES20.GL_ONE /*GL_SRC_ALPHA*/, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        TexturedBasicAlignedRect.prepareToDraw();
        BrickBreakerState.drawButtons();
        TexturedBasicAlignedRect.finishedDrawing();

        //if (!started) 
    	//	return;
        
        BrickBreakerState.drawDebugStuff();

        // Turn alpha blending off.
        GLES20.glDisable(GLES20.GL_BLEND);

        if (EXTRA_CHECK) Library.checkGlError("onDrawFrame end");

        // Stop animating if the game is over  or if there is a ball lost.  
        if (!BrickBreakerState.isAnimating()) {
        	 Log.d(TAG, "Game over or before start game, stopping animation");
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
    	// Saves game state into static storage
        mBrickBreakerState.save();

        syncObj.open();
    }

    /**
     * Updates state after the player moves touch the screen.  Call through queueEvent().
     */
    public void actionMoveTouchEvent(float x, float y) {        

        float arenaX = (x - mViewportXoff) * (BrickBreakerState.ARENA_WIDTH / mViewportWidth);
        float arenaY = (y - mViewportYoff) * (BrickBreakerState.ARENA_HEIGHT / mViewportHeight);
        //Log.v(TAG, "touch at x=" + (int) x + " y=" + (int) y + " --> arenaX=" + (int) arenaX);

        mBrickBreakerState.movePaddle(arenaX);
    }
    
    /**
     * Restart game after the player touches the screen
     */
    public void actionDownTouchEvent(){
    	//Log.v(TAG, "reanudando o jogodd"+ String.valueOf(mBrickBreakerState.getGamePlayState()));
        
        if (mBrickBreakerState.isGamePaused()) {
        	Log.v(TAG, "reanudando o jogo");
        	mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        	mBrickBreakerState.RestartGame();
        	
        	
		}
    }
}
