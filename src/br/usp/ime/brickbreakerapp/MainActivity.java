package br.usp.ime.brickbreakerapp;

import java.io.IOException;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends Activity {
	public static final String TAG = "breakout";
	
	private Fragment mFragment = null;
	private FragmentManager mFragmentManager;

	// Shared preferences file.
    public static final String PREFS_NAME = "PrefsAndScores";
    // Keys for values saved in our preferences file.
    private static final String DIFFICULTY_KEY = "difficulty";
    //private static final String NEVER_LOSE_BALL_KEY = "never-lose-ball";
    private static final String SOUND_EFFECTS_ENABLED_KEY = "sound-effects-enabled";
    public static final String HIGH_SCORE_KEY = "high-score";
    // Highest score seen so far.
    private int mHighScore;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
    	Log.d(TAG, "MainActivity.onCreate");
        super.onCreate( savedInstanceState );
        //Show the menu
        setContentView(R.layout.activity_main);
        
        //Show frament_main layout
        mFragment = (MainFragment) new MainFragment();
    	mFragmentManager = getFragmentManager();
    	
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .addToBackStack(null)
                .commit();
    }


    @Override
    protected void onResume() {
    	Log.d(TAG, "MainActivity.onResume");
        super.onResume();
        restorePreferences();
        updateControls();
    }


    @Override
    protected void onPause() {
    	Log.d(TAG, "MainActivity.onPause");
        super.onPause();
        savePreferences();
    }
    
/***********************************Fragment Main's Buttons***************************************************/
	
	//-----Start the game
	public void onClickPlay(View control){
		BrickBreakerActivity.invalidateSavedGame();
		startGame();
	}
	
	//----Show the levels of game
	public void onClickLevels(View control){
    	mFragment = (LevelsFragment) new LevelsFragment();
		
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .addToBackStack(null)
                .commit();
        
		Log.i(TAG, "onClickLevels");
	}
	
	//----Show a screen with settings(sound, vibration, reset score) 
	public void onClickOption(View control){
    	mFragment = (OptionFragment) new OptionFragment();
		
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .addToBackStack(null)
                .commit();

		Log.i(TAG, "onClickOption");
	}
	
	//---Show a ranking
	public void onClickRanking(View control){
		
	}
	
	//---Exit the game
	public void onClickExit(View control){
		updateControls();
	}
	
	/**
     * Fires an Intent that starts the BrickBreakerActivity.
     */
    private void startGame() {
    	Intent intent = new Intent(this, BrickBreakerActivity.class);
        startActivity(intent);
    
    }
    

/***********************************Fragment Option's Buttons*************************************************/

	//---Get back to Main menu
	public void onClickBack(View control) {
		mFragmentManager.popBackStackImmediate();
	}
	
    /**
     * onClick handler for "sound effects enabled".
     */
    public void clickSoundEffectsEnabled(View view) {
        /*
         * The call to updateControls() isn't really necessary, because changing this value
         * doesn't invalidate the saved game.  In general though it's up to BrickBreakerActivity to
         * decide what does and doesn't spoil a game, and it's possible the behavior could
         * change in the future, so we call it to be safe.
         */

        BrickBreakerActivity.setSoundEffectsEnabled(((CheckBox) view).isChecked());
        updateControls();
      
        
    }

	//---Reset Score
	public void onClickResetScore(View control) {
		
	}

	//---Change Username
	public void onClickChangeUsername(View control) {
		
	}
	
/*************************************************************************************************************/
	
    /**
     * Sets the state of the UI controls to match our internal state.
     */
    private static void updateControls() {
        /*Spinner difficulty = (Spinner) findViewById(R.id.spinner_difficultyLevel);
        difficulty.setSelection(BrickBreakerActivity.getDifficultyIndex());

        Button resume = (Button) findViewById(R.id.button_resumeGame);
        resume.setEnabled(BrickBreakerActivity.canResumeFromSave());

        CheckBox neverLoseBall = (CheckBox) findViewById(R.id.checkbox_neverLoseBall);
        neverLoseBall.setChecked(BrickBreakerActivity.getNeverLoseBall());

        CheckBox soundEffectsEnabled = (CheckBox) findViewById(R.id.checkbox_soundEffectsEnabled);
        soundEffectsEnabled.setChecked(BrickBreakerActivity.getSoundEffectsEnabled());

        TextView highScore = (TextView) findViewById(R.id.text_highScore);
        highScore.setText(String.valueOf(mHighScore));*/
    }
    

    
    
    /**
     * Copies settings to the saved preferences.
     */
    private void savePreferences() {
        /*
         * We could put a version number in the preferences so that, if a future version of the
         * app substantially changes the meaning of the preferences, we have a way to figure
         * out what they mean (or figure out that we can't understand them).  We only have a
         * handful of preferences, and the only interesting one -- the difficulty index -- is
         * trivial to range-check.  We don't need it, so we're not going to build it.  (And
         * if we need it later, the absence of a version number in the prefs is telling, so
         * we're not going to end up in a situation where we can't decipher the prefs file.)
         */

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(DIFFICULTY_KEY, BrickBreakerActivity.getDifficultyIndex());
        //editor.putBoolean(NEVER_LOSE_BALL_KEY, BrickBreakerActivity.getNeverLoseBall());
        editor.putBoolean(SOUND_EFFECTS_ENABLED_KEY, BrickBreakerActivity.getSoundEffectsEnabled());
        editor.commit();
    }

    /**
     * Retrieves settings from the saved preferences.  Also picks up the high score.
     */
    private void restorePreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // If the saved prefs come from a different version of the game, the difficulty level
        // might be out of range.  The code in BrickBreakerActivity will reset it to default.
        BrickBreakerActivity.setDifficultyIndex(prefs.getInt(DIFFICULTY_KEY,
                BrickBreakerActivity.getDefaultDifficultyIndex()));
        //BrickBreakerActivit/y.setNeverLoseBall(prefs.getBoolean(NEVER_LOSE_BALL_KEY, false));
        BrickBreakerActivity.setSoundEffectsEnabled(prefs.getBoolean(SOUND_EFFECTS_ENABLED_KEY, true));

        mHighScore = prefs.getInt(HIGH_SCORE_KEY, 0);
    }
}