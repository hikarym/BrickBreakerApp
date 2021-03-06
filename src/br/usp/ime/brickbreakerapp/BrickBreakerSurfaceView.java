package br.usp.ime.brickbreakerapp;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.os.ConditionVariable;

/**
 * View object for the GL surface view.  
 */
public class BrickBreakerSurfaceView extends GLSurfaceView {
	private static final String TAG = MainActivity.TAG;
	
	private BrickBreakerSurfaceRenderer mRenderer;
	private final ConditionVariable syncObj = new ConditionVariable();

    //---Prepares the OpenGL context and starts the Renderer thread
	public BrickBreakerSurfaceView(Context context, BrickBreakerState brickBreakerState,
			TextResources.Configuration textConfig) {
		super(context);
		
		// Request OpenGL ES 2.0
		setEGLContextClientVersion(2);
		mRenderer = new BrickBreakerSurfaceRenderer(context, brickBreakerState, this, textConfig);
		setRenderer(mRenderer);
	}
	
	// It tells BrickBreakerState to save its state.
	// for than, we use queueEvent(), which doesn't wait for the code to actually execute.
	@Override
	public void onPause() {		
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
	
	//Forward touch events to the game loop
	//(queueEvent: This can be used to communicate with the Renderer on the rendering thread. 
	//Must not be called before a renderer has been set.)
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (!mRenderer.getStarted())
			mRenderer.setStarted(true);
		
		final float x, y;
		
		switch (e.getAction()) {
			case MotionEvent.ACTION_MOVE:
				//we  verify this action to move the paddle
				x = e.getX();
				y = e.getY();
				
				//Log.d(TAG, "BrickBreakerSurfaceView onTouchEvent x=" + x + " y=" + y);
				queueEvent(new Runnable() {
					@Override public void run() {
						mRenderer.actionMoveTouchEvent(x, y);
					}});
				
				break;
			
			case MotionEvent.ACTION_DOWN:
				// we verify this action to start, continue, restart, 
				// exit of the game or to go next level
				x = e.getX();
				y = e.getY();
				
				Log.d(TAG, "BrickBreakerSurfaceView TOUCH DOWN");
				queueEvent(new Runnable() {
					@Override public void run() {
						mRenderer.actionDownTouchEvent(x, y);
					}});
				break;
			
			default:
				break;
		}
		
		return true;
	}
}
