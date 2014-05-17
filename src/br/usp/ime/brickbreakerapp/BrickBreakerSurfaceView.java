package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.os.ConditionVariable;

/**
 * View object for the GL surface.  Wraps the renderer.
 */
public class BrickBreakerSurfaceView extends GLSurfaceView {
    private static final String TAG = MainActivity.TAG;

    private BrickBreakerSurfaceRenderer mRenderer;
    private final ConditionVariable syncObj = new ConditionVariable();

    /**
     * Prepares the OpenGL context and starts the Renderer thread.
     */
    public BrickBreakerSurfaceView(Context context, BrickBreakerState brickBreakerState,
            TextResources.Configuration textConfig) {
        super(context);

        setEGLContextClientVersion(2);      // Request OpenGL ES 2.0

        // Create our Renderer object, and tell the GLSurfaceView code about it.  This also
        // starts the renderer thread, which will be calling the various callback methods
        // in the BrickBreakerSurfaceRenderer class.
        mRenderer = new BrickBreakerSurfaceRenderer(context, brickBreakerState, this, textConfig);
        setRenderer(mRenderer);
        //onPause();
    }

    @Override
    public void onPause() {
        /*
         * We call a "pause" function in our Renderer class, which tells it to save state and
         * go to sleep.  Because it's running in the Renderer thread, we call it through
         * queueEvent(), which doesn't wait for the code to actually execute.  In theory the
         * application could be killed shortly after we return from here, which would be bad if
         * it happened while the Renderer thread was still saving off important state.  We need
         * to wait for it to finish.
         */

        super.onPause();

        //Log.d(TAG, "asking renderer to pause");
        syncObj.close();
        queueEvent(new Runnable() {
            @Override public void run() {
                mRenderer.onViewPause(syncObj);
            }});
        syncObj.block();

        //Log.d(TAG, "renderer pause complete");
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        /*
         * Forward touch events to the game loop.  We don't want to call Renderer methods
         * directly, because they manipulate state that is "owned" by a different thread.  We
         * use the GLSurfaceView queueEvent() function to execute it there.
         *
         * This increases the latency of our touch response slightly, but it shouldn't be
         * noticeable.
         */
    	if (!mRenderer.getStarted())
    		mRenderer.setStarted(true);

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                final float x, y;
                x = e.getX();
                y = e.getY();
                //Log.d(TAG, "BrickBreakerSurfaceView onTouchEvent x=" + x + " y=" + y);
                queueEvent(new Runnable() {
                    @Override public void run() {
                        mRenderer.touchEvent(x, y);
                    }});
                break;
            case MotionEvent.ACTION_DOWN:
    			x = e.getX();
    			y = e.getY();
    			queueEvent(new Runnable() {
    				@Override public void run() {
    					//mRenderer.actionDownTouchEvent();
    				}});
    			break;
    		default:
    			break;
    		}        

        return true;
    }
}
