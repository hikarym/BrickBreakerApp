package br.usp.ime.brickbreakerapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    
	// Shared preferences file.
    public static final String PREFS_NAME = "PrefsAndScores";
    // Keys for values saved in our preferences file.
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String NEVER_LOSE_BALL_KEY = "never-lose-ball";
    private static final String SOUND_EFFECTS_ENABLED_KEY = "sound-effects-enabled";
    public static final String HIGH_SCORE_KEY = "high-score";
    // Highest score seen so far.
    private int mHighScore;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView(R.layout.fragment_main);
        
    }


    @Override
    protected void onResume() {
        super.onResume();
        //glSurfaceView.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //glSurfaceView.onPause();
    }

	
	//-----Start the game
	public void onClickPlay(View control){
		//cancelSavedGame();
		startGame();
	}
	
	//----Show the levels of game
	public void onClickLevels(View control){
		
	}
	
	//----Show a screen with settings(sound, vibration, reset score) 
	public void onClickOption(View control){
		
	}
	
	//---Show a ranking
	public void onClickRanking(View control){
		
	}
	
	//---Exit the game
	public void onClickExit(View control){
		
	}
	
	/**
     * Fires an Intent that starts the GameActivity.
     */
    private void startGame() {
    	Intent intent = new Intent(this, BrickBreakerActivity.class);
        startActivity(intent);
    
    }
	
    //---cancel saved game
    private void cancelSavedGame(){
    	
    }
}