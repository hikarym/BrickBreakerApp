package br.usp.ime.brickbreakerapp;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class BrickBreakerActivity extends Activity {

    private static final int DIFFICULTY_MIN = 0;
    private static final int DIFFICULTY_MAX = 3;        // inclusive
    private static final int DIFFICULTY_DEFAULT = 1;
    private static int sDifficultyIndex;

    private static boolean sNeverLoseBall;

    private static boolean sSoundEffectsEnabled;


    // The Activity has one View, a GL surface.
    //private GameSurfaceView mGLView;
    private GLSurfaceView glSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		
		// Initialize data that depends on Android resources.
		
		glSurfaceView = new BrickBreakerSurfaceView( this );
        setContentView( glSurfaceView );
        
        glSurfaceView.requestFocus();
        glSurfaceView.setFocusableInTouchMode( true );
		
		
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
