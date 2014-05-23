package br.usp.ime.brickbreakerapp;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import br.usp.ime.brickbreakerapp.sqlite.BbSQliteHelper;
import br.usp.ime.brickbreakerapp.sqlite.BbScore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String TAG = "breakout";
	
	// SQLiteHelper to handle the app BD
	public static BbSQliteHelper mAppDB;
	
	private Fragment mFragment = null;
	private FragmentManager mFragmentManager;

	// Shared preferences file.
    public static final String PREFS_NAME = "PrefsAndScores";
    // Keys for values saved in our preferences file.
    //private static final String DIFFICULTY_KEY = "difficulty";
    private static final String GAME_LEVEL = "game-level";
    private static final String SOUND_EFFECTS_ENABLED_KEY = "sound-effects-enabled";
    public static final String HIGH_SCORE_KEY = "high-score";
    // Highest score seen so far
    private int mHighScore = 0;
    // Highest score seen so far.
    private String mUser = null;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
    	Log.d(TAG, "MainActivity.onCreate");
        super.onCreate( savedInstanceState );
        //Show the menu
        setContentView(R.layout.activity_main);
        
        mAppDB = new BbSQliteHelper(this);
        
        //Show frament_main layout
        mFragment = (MainFragment) new MainFragment();
    	mFragmentManager = getFragmentManager();
    	
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .commit();
    }

    @Override
    protected void onResume() {
    	Log.d(TAG, "MainActivity.onResume");
        super.onResume();
        
        restorePreferences();
        //updateControls();
    }

    @Override
    protected void onPause() {
    	Log.d(TAG, "MainActivity.onPause");
        super.onPause();
        
        savePreferences();
        //mAppDB.close();
    }

    @Override
    protected void onDestroy() {
    	Log.d(TAG, "MainActivity.onDestroy");
        super.onDestroy();

        savePreferences();
        mAppDB.close();
    }
    
/***********************************Fragment Main's Buttons***************************************************/
	
	//-----Start the game
	public void onClickPlay(View control){
		BrickBreakerActivity.invalidateSavedGame();
		startGame();
	}
	
	//----Show the levels of game
	public void onClickLevels(View control){
		Log.d(TAG, "MainActivity.onClickLevels");
		
    	mFragment = (LevelsFragment) new LevelsFragment();
		
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .addToBackStack(null)
                .commit();
	}
	
	//----Show a screen with settings(sound, vibration, reset score) 
	public void onClickOption(View control){
		Log.d(TAG, "MainActivity.onClickOption");
		
    	mFragment = (OptionFragment) new OptionFragment();
		
        mFragmentManager.beginTransaction()
                .replace(R.id.container, mFragment)
                .addToBackStack(null)
                .commit();
	}
	
	//---Show a ranking
	public void onClickRanking(View control){
		Log.d(TAG, "MainActivity.onClickRanking");
		
		mHighScore = mAppDB.getHighScore();
		
		// If there are no recorded scores, show alert dialog
		if (mHighScore == -1) {
			mHighScore = 0;
			
			AlertDialog.Builder builder = new AlertDialog.Builder(
					new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
			
			builder.setTitle(R.string.title_no_ranking);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			
			//builder.setIcon(R.drawable.ic_dark_action_warning);
			builder.setMessage(R.string.msg_no_scores);
			builder.setCancelable(true);					// implies setCanceledOnTouchOutside
			builder.setPositiveButton(R.string.ok, null);
			builder.show();
			
			return;
		}
		
		// Otherwise, show ranking table
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment prev = mFragmentManager.findFragmentByTag("dialog");
		
		if (prev != null)
			ft.remove(prev);
	    
		ft.addToBackStack(null);
		
		// Create and show the dialog
		DialogFragment newFragment = new RankingFragment();
		newFragment.show(ft, "dialog");
	}
	
	//---Exit the game
	public void onClickExit(View control){
		//updateControls();
		finish();
	}
	
	/**
     * Fires an Intent that starts the BrickBreakerActivity.
     */
    private void startGame() {
    	Intent intent = new Intent(this, BrickBreakerActivity.class);
        startActivity(intent);
        finish();
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
        //updateControls();
      
        
    }

	//---Reset Score
	public void onClickResetScore(View control) {/*
    	final BbSQliteHelper mAppDB;
        mAppDB = new BbSQliteHelper(this);
        
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
		
		builder.setTitle(R.string.title_warning);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		
		builder.setMessage(R.string.msg_reset_scores);
		builder.setCancelable(true);					// implies setCanceledOnTouchOutside
		builder.setNegativeButton(R.string.action_cancel, null);
		
		builder.setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
			@Override
            public void onClick(DialogInterface dialog, int id) {
                // Reset scores
        		mAppDB.dropDBTable();
        		mAppDB.createDBTable();
            }
        });
		
		mAppDB.close();
		*/
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment prev = mFragmentManager.findFragmentByTag("dialog");
		
		if (prev != null)
			ft.remove(prev);
	    
		ft.addToBackStack(null);
		
		// Create and show the dialog
		DialogFragment newFragment = OptionFragment.resetScoresFragment.newInstance(R.string.title_warning);
		newFragment.show(ft, "resetScoresDialog");
	}

	//---Change Username
	public void onClickChangeUsername(View control) {
		
	}
	
/*************************************************************************************************************/
	   
    
    /**
     * Copies settings to the saved preferences.
     */
    private void savePreferences() {

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt(GAME_LEVEL, BrickBreakerActivity.getLevelIndex());
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
        BrickBreakerActivity.setLevelIndex(prefs.getInt(GAME_LEVEL, BrickBreakerActivity.getDefaultLevelIndex()));
        BrickBreakerActivity.setSoundEffectsEnabled(prefs.getBoolean(SOUND_EFFECTS_ENABLED_KEY, true));

        mHighScore = prefs.getInt(HIGH_SCORE_KEY, 0);
    }
}