package br.usp.ime.brickbreakerapp;

import br.usp.ime.brickbreakerapp.OptionFragment.resetScoresFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

/**
 * Activity for the actual game.  This is largely just a wrapper for our GLSurfaceView.
 */
public class BrickBreakerActivity extends Activity {
    private static final String TAG = MainActivity.TAG;

    private static final int DIFFICULTY_MIN = 0;
    private static final int DIFFICULTY_MAX = 3;        // inclusive
    private static final int DIFFICULTY_DEFAULT = 1;
    private static int sDifficultyIndex = 1;
    /*
    
    private static final int LEVEL_MIN = 1;
    private static final int LEVEL_MAX = 3;        // inclusive
    private static int sLevelIndex = 1;
     
    */
    
    //private static boolean sNeverLoseBall;
    
	// Flag to indicate if sounds effects of game are enabled or not
	private static boolean statusSoundEffectsEnabled;
	
    // The Activity has one View, a GL surface.
    private BrickBreakerSurfaceView mGLView;

    // Live game state.
    //
    // We could make this static and let it persist across game restarts.  This would avoid
    // some setup time when we leave and re-enter the game, but it also means that the
    // BrickBreakerState will stay in memory even after the game is no longer running.  If BrickBreakerState
    // holds references to other objects, such as this Activity, the GC will be unable to
    // discard those either.
    private BrickBreakerState mBrickBreakerState;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "BrickBreakerActivity onCreate");

        // Initialize data that depends on Android resources.
        SoundResources.initialize(this);
        TextResources.Configuration textConfig = TextResources.configure(this);
        
        /*new AlertDialog.Builder(this)
        .setTitle("Delete entry")
        .setMessage("Are you sure you want to delete this entry?")
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // continue with delete
            }
         })
        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                // do nothing
            }
         })
        .setIcon(android.R.drawable.ic_dialog_alert)
         .show();*/

        mBrickBreakerState = new BrickBreakerState();
        configureBrickBreakerState();

        // Create a GLSurfaceView, and set it as the Activity's "content view".  This will
        // also create a GLSurfaceView.Renderer, which starts the Renderer thread.
        //
        // IMPORTANT: anything we have done up to this point -- notably, configuring BrickBreakerState --
        // will be visible to the new Renderer thread.  However, any accesses to mutual state
        // after this point must be guarded with some form of synchronization.
        mGLView = new BrickBreakerSurfaceView(this, mBrickBreakerState, textConfig);
        setContentView(mGLView);
    }
    
    protected void exitGame() {
    	Log.d(TAG, "BrickBreakerActivity pausing");

        /*
         * We must call the GLView's onPause() function when the framework tells us to pause.
         * We're also expected to deallocate any large OpenGL resources, though presumably
         * that just means our associated Bitmaps and FloatBuffers since the OpenGL goodies
         * themselves (e.g. programs) are discarded by the GLSurfaceView.
         *
         * Our GLSurfaceView's onPause() method will synchronously invoke the BrickBreakerState's save()
         * function on the Renderer thread.  This will record the saved game into the storage
         * we provided when the object was constructed.
         */
    	super.onPause();
        mGLView.onPause();
        //------------------------------------------------------------------------------------------------------
        /*
         * If the game is over, record the new high score.
         *
         * This isn't the ideal place to do this, because if the devices loses power while
         * sitting on the "game over" screen we won't record the score.  In practice the
         * user will either leave the game or the device will go to sleep, pausing the activity,
         * so it's not a real concern.
         *
         * We could improve on this by having BrickBreakerState manage the high score, but since
         * we're using Preferences to hold it, we'd need to pass the Activity through.  This
         * interferes with the idea of keeping BrickBreakerState isolated from the application UI.
         *
         * Note that doing this update in the Preferences code in MainActivity is a
         * bad idea, because that would prevent us from recording a high score until the user
         * hit "back" to return to the initial Activity -- which won't happen if they just
         * hit the "home" button to quit.
         *
         * MainActivity will need to see the updated high score.  The Android lifecycle
         * is defined such that our onPause() will execute before MainActivity's onResume()
         * is called (see "Coordinating Activities" in the developer guide page for Activities),
         * so they'll be able to pick up whatever we do here.
         *
         * We need to do this *after* the call to mGLView.onPause(), because that causes
         * BrickBreakerState to save the game to static storage, and that's what we read the score from.
         */
        updateHighScore(BrickBreakerState.getFinalScore());
    }
    
    @Override
    protected void onPause() {/*
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(this, android.R.style.Theme_Holo_Dialog));
		
		builder.setTitle(R.string.app_name);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setMessage(R.string.msg_exit);
		builder.setNegativeButton(R.string.cancel, null);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        BrickBreakerActivity.exitGame();
			}
        });
		
		builder.show();
		*/
		exitGame();
    }

    @Override
    protected void onResume() {
        /*
         * Complement of onPause().  We're required to call the GLView's onResume().
         *
         * We don't restore the saved game state here, because we want to wait until after the
         * objects have been created (since much of the game state is held within the objects).
         * In any event we need it to run on the Renderer thread, so we let the restore happen
         * in GameSurfaceRenderer's onSurfaceCreated() method.
         */

        Log.d(TAG, "BrickBreakerActivity resuming");
        super.onResume();
        mGLView.onResume();
    }
    /*
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
	}
	
	
	//---Copies settings to the saved preferences' file
	private void savePreferences() {
		SharedPreferences prefs = getSharedPreferences(OptionFragment.PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		
		//editor.putInt(OptionFragment.DIFFICULTY_KEY, getDifficultyIndex());
		//editor.putBoolean(OptionFragment.NEVER_LOSE_BALL_KEY, getNeverLoseBall());
		editor.putBoolean(OptionFragment.SOUND_EFFECTS_ENABLED_KEY, isSoundEffectsEnabled());
		editor.putString(OptionFragment.USERNAME_KEY, OptionFragment.getCurrentUsername());
		//editor.putInt(OptionFragment.LEVEL_KEY, getLevelIndex());
		editor.commit();
	}
	
	//---Retrieves settings from the saved preferences' file
	private void restorePreferences() {
		SharedPreferences prefs = getSharedPreferences(OptionFragment.PREFS_NAME, MODE_PRIVATE);
		//setDifficultyIndex(prefs.getInt(OptionFragment.DIFFICULTY_KEY, DIFFICULTY_DEFAULT));
		//setNeverLoseBall(prefs.getBoolean(OptionFragment.NEVER_LOSE_BALL_KEY, false));
		//setLevel(prefs.getInt(OptionFragment.LEVEL_KEY, 1));
		setSoundEffectsEnabled(prefs.getBoolean(OptionFragment.SOUND_EFFECTS_ENABLED_KEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS));
		setSoundEffectsEnabled(prefs.getBoolean(OptionFragment.USERNAMainActivity.DEFAULT_SOUND_EFFECTS_STATUSEY, MainActivity.DEFAULT_SOUND_EFFECTS_STATUS));
	}
	
    */
    /**
     * Configures the BrickBreakerState object with the configuration options set by MainActivity.
     */
    private void configureBrickBreakerState() {
        int maxLives, minSpeed, maxSpeed;
        float ballSize, paddleSize, scoreMultiplier;

        switch (sDifficultyIndex) {
            case 0:                     // easy
                ballSize = 2.0f;
                paddleSize = 2.0f;
                scoreMultiplier = 0.75f;
                maxLives = 4;
                minSpeed = 200;
                maxSpeed = 500;
                break;
            case 1:                     // normal
                ballSize = 1;
                paddleSize = 1.0f;
                scoreMultiplier = 1.0f;
                maxLives = 3;
                minSpeed = 300;
                maxSpeed = 800;
                break;
            case 2:                     // hard
                ballSize = 1.0f;
                paddleSize = 0.8f;
                scoreMultiplier = 1.25f;
                maxLives = 3;
                minSpeed = 600;
                maxSpeed = 1200;
                break;
            case 3:                     // absurd
                ballSize = 1.0f;
                paddleSize = 0.5f;
                scoreMultiplier = 0.1f;
                maxLives = 1;
                minSpeed = 1000;
                maxSpeed = 100000;
                break;
            default:
                throw new RuntimeException("bad difficulty index " + sDifficultyIndex);
        }

        mBrickBreakerState.setBallSizeMultiplier(ballSize);
        mBrickBreakerState.setPaddleSizeMultiplier(paddleSize);
        mBrickBreakerState.setScoreMultiplier(scoreMultiplier);
        mBrickBreakerState.setMaxLives(maxLives);
        mBrickBreakerState.setBallInitialSpeed(minSpeed);
        mBrickBreakerState.setBallMaximumSpeed(maxSpeed);

        //mBrickBreakerState.setNeverLoseBall(sNeverLoseBall);

        SoundResources.setSoundEffectsEnabled(statusSoundEffectsEnabled);
    }
    /*
    
    public static int getLevelIndex() {
        return sLevelIndex;
    }
    
	public static void setLevelIndex(int levelIndex) {
        // This could be coming from preferences set by a different version of the game.  We
        // want to be tolerant of values we don't recognize.
        if (levelIndex < LEVEL_MIN || levelIndex > LEVEL_MAX) {
            Log.w(TAG, "Invalid LEVEL index " + levelIndex + ", using mininum");
            difficultyIndex = LEVEL_MIN;
        }

        if (sLevelIndex != levelIndex) {
            sLevelIndex = levelIndex;
            invalidateSavedGame();
        }
    }
    
    */
    
    /**
     * Gets the difficulty index, used to configure the game parameters.
     */
    public static int getDifficultyIndex() {
        return sDifficultyIndex;
    }

    /**
     * Gets the default difficulty index.  This should be used if no preference has been saved.
     */
    public static int getDefaultDifficultyIndex() {
        return DIFFICULTY_DEFAULT;
    }

    /**
     * Configures various tunable parameters based on the difficulty index.
     * <p>
     * Changing the value will cause a game in progress to reset.
     */
    public static void setDifficultyIndex(int difficultyIndex) {
        // This could be coming from preferences set by a different version of the game.  We
        // want to be tolerant of values we don't recognize.
        if (difficultyIndex < DIFFICULTY_MIN || difficultyIndex > DIFFICULTY_MAX) {
            Log.w(TAG, "Invalid difficulty index " + difficultyIndex + ", using default");
            difficultyIndex = DIFFICULTY_DEFAULT;
        }

        if (sDifficultyIndex != difficultyIndex) {
            sDifficultyIndex = difficultyIndex;
            invalidateSavedGame();
        }
    }

    /**
     * Gets the "never lose a ball" option.
     */
    /*public static boolean getNeverLoseBall() {
        return sNeverLoseBall;
    }*/

    /**
     * Configures the "never lose a ball" option.  If set, the ball bounces off the bottom
     * (incurring a point deduction) instead of draining out.
     * <p>
     * Changing the value will cause a game in progress to reset.
     */
    /*public static void setNeverLoseBall(boolean neverLoseBall) {
        if (sNeverLoseBall != neverLoseBall) {
            sNeverLoseBall = neverLoseBall;
            invalidateSavedGame();
        }
    }*/
    
    

    //---Returns true if sound effects are enabled, and false otherwise
    public static boolean isSoundEffectsEnabled() {
        return statusSoundEffectsEnabled;
    }

    //---Enables or disables sound effects
    public static void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
    	statusSoundEffectsEnabled = soundEffectsEnabled;
    }

    /**
     * Invalidates the current saved game.
     */
    public static void invalidateSavedGame() {
        BrickBreakerState.invalidateSavedGame();
    }

    /**
     * Determines whether our saved game is for a game in progress.
     */
    public static boolean canResumeFromSave() {
        return BrickBreakerState.canResumeFromSave();
    }

    /**
     * Updates high score.  If the new score is higher than the previous score, the entry
     * is updated.
     *
     * @param lastScore Score from the last completed game.
     */
    private void updateHighScore(int lastScore) {
    	String username = MainActivity.getStrPref(MainActivity.USERNAME_KEY, MainActivity.DEFAULT_USERNAME);
        int highScore = MainActivity.getBbSQliteHelper().getHighScore();

        Log.d(TAG, "final score was " + lastScore);
        
        MainActivity.getBbSQliteHelper().addScore(username, lastScore);
        
        if (lastScore > highScore) {
            Log.d(TAG, "new high score!  (" + highScore + " vs. " + lastScore + ")");
            
            /**************************************************************************************************************************************/
            // put fragment here!!!!!! CONGRATS!!! and the like
        }
    }
    

}
