package br.usp.ime.brickbreakerapp;

import br.usp.ime.brickbreakerapp.sqlite.BbSQliteHelper;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final String TAG = "BrickBreakerApp";
	
	private static BbSQliteHelper mBbScoreDB; // SQLiteHelper to handle the BbScoreBD

	private static SharedPreferences mPrefs; // Helper to handle the user's preferences
	
	// This is done so we won't have to create new fragments all the time
	//private OptionFragment mOptionFragment = null; // Option fragment to help handle the preferences
	//private LevelsFragment mLevelsFragment = null; // Level fragment to help handle the preferences
	
	private FragmentManager mFragmentManager = null;
	private Fragment mFragment = null; // Helper to handle current fragment
	
	private AudioManager mAudioManager = null;
	
	/** Preference keys **/

	// Shared preferences file.

    public static final String PREFS_NAME = "PrefsAndScores";
    // Keys for values saved in our preferences file.
    //private static final String DIFFICULTY_KEY = "difficulty";
    private static final String GAME_LEVEL_KEY = "game-level";
    public static final String SOUND_EFFECTS_ENABLED_KEY = "sound-effects-enabled";
    public static final String HIGH_SCORE_KEY = "high-score";
	public static final String USERNAME_KEY = "username";
	
	public static final String DEFAULT_USERNAME = "Master";
	public static final Boolean DEFAULT_SOUND_EFFECTS_STATUS = true; // DEFAULT_SOUND_EFFECTS_STATUS
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "MainActivity.onCreate");
		
		super.onCreate(savedInstanceState);
		
		// Show developers layout
		setContentView(R.layout.activity_main);
		
		//-----------------------------------------------------------------------------------------------------
		// Do animation here

		mFragmentManager = getFragmentManager();
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		// Start listening for button presses
		//mAudioManager.registerMediaButtonEventReceiver(RemoteControlReceiver);---------------------------------
		
		
		/*
		// Retrieve and cache the system's default "short" animation time.
		mShortAnimationDuration = getResources().getInteger(
				android.R.integer.config_shortAnimTime);
		*/
		// Initialize the fragments
		//mOptionFragment = new OptionFragment();
		//mLevelsFragment = new LevelsFragment();
		
		//mPrefs = PreferenceManager.getDefaultSharedPreferences(MODE_PRIVATE);
		mPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		mBbScoreDB = new BbSQliteHelper(this);
		
		// Show main menu
		mFragment = new MainFragment();
		displayFragment(mFragment);
	}
	
	@Override
	protected void onResume() {
		Log.d(TAG, "MainActivity.onResume");
		
		super.onResume();
		
		restorePreferences();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "MainActivity.onPause");
		
		super.onPause();
		
		savePreferences();
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "MainActivity.onDestroy");
		
		super.onDestroy();
		
		savePreferences();
		
		// Stop listening for button presses
		//mAudioManager.unregisterMediaButtonEventReceiver(RemoteControlReceiver);------------------------------
		
		// Closes the BbSQLiteHelper
		mBbScoreDB.close();
	}
	
	//---Show the view of the fragment without adding it to the back stack of mFragmentManager
	//---This way, the transition can't be undone
	private void displayFragment(Fragment fragment) {
		mFragmentManager.beginTransaction()
				.replace(R.id.container, fragment)
				.commit();
	}
	
	//---Show the view of the fragment and add it to the back stack of mFragmentManager
	private void displayAndAddFragment(Fragment fragment) {
		mFragmentManager.beginTransaction()
				.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
						R.animator.card_flip_left_in, R.animator.card_flip_left_out)
				.replace(R.id.container, fragment)
				.addToBackStack(null)
				.commit();
	}
	
	//---Reload the view of the fragment
	private void reloadFragment(Fragment fragment) {
		/*
		mFragmentManager.popBackStack();
		
		mFragmentManager.beginTransaction()
				.replace(R.id.container, fragment)
				.addToBackStack(null)
				.commit();
		*/
		
		// There's no need to create
		fragment.onResume();
	}
	
	private void displayDialogFragment(DialogFragment fragment) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		Fragment prev = mFragmentManager.findFragmentByTag("dialog");
		
		if (prev != null)
			ft.remove(prev);
		
		ft.addToBackStack(null);
		
		// Show the dialog
		fragment.show(ft, "dialog");
	}
	
	public static BbSQliteHelper getBbSQliteHelper() {
		return mBbScoreDB;
	}
	
	
	
/************************************* Handling saved preferences *********************************************/
	
	public static void putBoolPref(String key, boolean value) {
		SharedPreferences.Editor editor = mPrefs.edit();
		
		editor.putBoolean(key, value);
		
		editor.commit();
	}
	
	public static boolean getBooPref(String key, boolean defaultValue) {
		return mPrefs.getBoolean(key, defaultValue);
	}
	
	public static void putIntPref(String key, int value) {
		SharedPreferences.Editor editor = mPrefs.edit();
		
		editor.putInt(key, value);
		
		editor.commit();
	}

	public static int getIntPref(String key, int defaultValue) {
		return mPrefs.getInt(key, defaultValue);
	}
	
	public static void putStrPref(String key, String value) {
		SharedPreferences.Editor editor = mPrefs.edit();
		
		editor.putString(key, value);
		
		editor.commit();
	}

	public static String getStrPref(String key, String defaultValue) {
		return mPrefs.getString(key, defaultValue);
	}
	
	//---Copies settings to the saved preferences' file
	private void savePreferences() {
		//SharedPreferences prefs = getSharedPreferences(OptionFragment.PREFS_NAME, MODE_PRIVATE);
		//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
		
		//editor.putInt(DIFFICULTY_KEY, BrickBreakerActivity.getDifficultyIndex());
		//editor.putBoolean(NEVER_LOSE_BALL_KEY, BrickBreakerActivity.getNeverLoseBall());
		editor.putBoolean(SOUND_EFFECTS_ENABLED_KEY, BrickBreakerActivity.isSoundEffectsEnabled());
		editor.putString(USERNAME_KEY, OptionFragment.getCurrentUsername());
		editor.putInt(GAME_LEVEL_KEY, BrickBreakerActivity.getLevelIndex());
		editor.commit();
	}
	
	//---Retrieves settings from the saved preferences' file
	private void restorePreferences() {
		//BrickBreakerActivity.setDifficultyIndex(mPrefs.getInt(DIFFICULTY_KEY,
		//		BrickBreakerActivity.getDefaultDifficultyIndex()));
		//BrickBreakerActivity.setNeverLoseBall(mPrefs.getBoolean(NEVER_LOSE_BALL_KEY, false));
		//BrickBreakerActivity.setLevel(mPrefs.getInt(LEVEL_KEY, 1));
		BrickBreakerActivity.setSoundEffectsEnabled(
				mPrefs.getBoolean(SOUND_EFFECTS_ENABLED_KEY, DEFAULT_SOUND_EFFECTS_STATUS));
		
		OptionFragment.setCurrentUsername(mPrefs.getString(USERNAME_KEY, DEFAULT_USERNAME));
	}
	
/************************************* Fragment Main's Buttons *********************************************/
	
	//-----Start the game
	public void onClickPlay(View view) {
		Log.d(TAG, "MainActivity.onClickChangeUsername");
		
		BrickBreakerActivity.invalidateSavedGame();
		startGame();
	}
	
	//----Show the levels of game
	public void onClickLevels(View view) {
		Log.d(TAG, "MainActivity.onClickLevels");
		
		mFragment = new LevelsFragment();
		//displayAndAddFragment(mLevelsFragment);
		displayAndAddFragment(mFragment);
	}
	
	//----Show a screen with settings(sound, vibration, reset score)
	public void onClickOption(View view) {
		Log.d(TAG, "MainActivity.onClickOption");
		/*
		if (mOptionFragment == null)
			mOptionFragment = new OptionFragment();
		
		displayAndAddFragment(mOptionFragment);
		*/
		
		mFragment = new OptionFragment();
		displayAndAddFragment(mFragment);
	}
	
	//---Show the rankings
	public void onClickRanking(View view) {
		Log.d(TAG, "MainActivity.onClickRanking");

		// Create and show the dialog
		mFragment = new RankingFragment();
		displayAndAddFragment(mFragment);
		
		// If there are no recorded scores, show alert dialog
		if (mBbScoreDB.getHighScore() == -1) {
			
			new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
					.setTitle(R.string.title_no_ranking)
					.setIcon(android.R.drawable.ic_dialog_alert)
					//.setIcon(R.drawable.ic_dark_action_warning)
					.setMessage(R.string.msg_no_scores)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mFragmentManager.popBackStackImmediate();
						}
						
					})
					.show();
		}
	}
	
	//---Exit the game
	public void onClickExit(View view) {
		Log.d(TAG, "MainActivity.onClickExit");		
		// This method calls onDestroy
		finish();
	}
	
	//---Starts the BrickBreakerActivity
	private void startGame() {
		Log.d(TAG, "MainActivity.startGame");
		Intent intent = new Intent(this, BrickBreakerActivity.class);
		startActivity(intent);
		
		overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_left);
        //finish();---------------------------------------------------------------------------------------------
	}
	
	
/************************************* Other Fragment's Buttons *********************************************/
    
	//---Get back to main menu
	public void onClickBack(View view) {
		Log.d(TAG, "MainActivity.onClickBack");
		
		mFragmentManager.popBackStackImmediate();
	}
	
	/**
	 * onClick handler for "sound effects enabled".
	 */
	//---onClick handler for "sound effects enabled"
	public void onClickSoundEffectsEnabled(View view) {
		Log.d(TAG, "MainActivity.onClickSoundEffectsEnabled");
		
		BrickBreakerActivity.setSoundEffectsEnabled(((CheckBox) view).isChecked());
		
		savePreferences();
		
		// There's no need to create a new fragment because we know it's running when this method is executed.
		reloadFragment(mFragment);
	}
	
	//---Reset scores
	public void onClickResetScore(View view) {
		Log.d(TAG, "MainActivity.onClickResetScore");
		
		// Create and show the dialog
		DialogFragment dialogFragment = new OptionFragment.resetScoresFragment(); 
		displayDialogFragment(dialogFragment);
	}
	
	//---Change Username
	public void onClickChangeUsername(View view) {
		Log.d(TAG, "MainActivity.onClickChangeUsername");
		
		// Create and show the change username dialog
		final EditText usernameField = new EditText(this);
		
		usernameField.setHint(DEFAULT_USERNAME);
		usernameField.setBackgroundColor(Color.WHITE);
		usernameField.setSoundEffectsEnabled(getBooPref(SOUND_EFFECTS_ENABLED_KEY, DEFAULT_SOUND_EFFECTS_STATUS));
		usernameField.setInputType(InputType.TYPE_CLASS_TEXT);
		usernameField.requestFocus();
		usernameField.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If "enter" is pressed
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					String newUsername = usernameField.getText().toString();

					if (newUsername.trim().isEmpty() || newUsername == "\n")
						newUsername = DEFAULT_USERNAME;
					
					OptionFragment.setCurrentUsername(newUsername);
					
					// Add user name
					MainActivity.getBbSQliteHelper().addUser(newUsername);
					
					return true;
				}
				
				return false;
			}
		});


        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(usernameField, InputMethodManager.SHOW_FORCED);
        
		new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.title_change_username)
				.setView(usernameField)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String newUsername = usernameField.getText().toString();

							if (newUsername.trim().isEmpty() || newUsername == "\n")
								newUsername = DEFAULT_USERNAME;
							
							OptionFragment.setCurrentUsername(newUsername);
							
							// Add user name
							MainActivity.getBbSQliteHelper().addUser(newUsername);
							
							savePreferences();
							
							// There's no need to create a new fragment because we know it's running when this
							// method is executed.
							reloadFragment(mFragment);
						}
				}).show();
	}
	
	
	public class RemoteControlReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
				KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				
				if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
					BrickBreakerActivity.setSoundEffectsEnabled(true);
					savePreferences();
				}
				
				else if (KeyEvent.KEYCODE_MEDIA_STOP == event.getKeyCode()) {
					BrickBreakerActivity.setSoundEffectsEnabled(false);
					// Handle key press.

					savePreferences();
				}
			}
		}
	}
}