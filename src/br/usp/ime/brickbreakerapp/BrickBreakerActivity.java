package br.usp.ime.brickbreakerapp;

import br.usp.ime.brickbreakerapp.LevelParameters.ParametersConfig;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity for the actual game.  This is largely just a wrapper for our GLSurfaceView.
 */
//---Activity for the game
public class BrickBreakerActivity extends Activity {
    private static final String TAG = MainActivity.TAG;
    
	public static final int DEFAULT_LEVEL = MainActivity.DEFAULT_LEVEL;
	public static final int MIN_LEVEL = LevelsFragment.MIN_LEVEL;
    public static final int MAX_LEVEL = LevelsFragment.MAX_LEVEL;
    
	// Flag to indicate if sounds effects of game are enabled or not
    private static int sLevelGame = DEFAULT_LEVEL;
    
	// Flag to indicate if sounds effects of game are enabled or not
	private static boolean sSfxEnabled;
	
    // The Activity has one View, a GL surface.
    private BrickBreakerSurfaceView mGLView;

    // Live game state.    
    private BrickBreakerState mBrickBreakerState;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "BrickBreakerActivity onCreate");

        // Initialize the sounds
        SoundResources.initialize(this);
        TextResources.Configuration textConfig = TextResources.configure(this);
        
        mBrickBreakerState = new BrickBreakerState();
        configureBrickBreakerState();

        // Create a GLSurfaceView, and set it as the Activity's "content view".  
        mGLView = new BrickBreakerSurfaceView(this, mBrickBreakerState, textConfig);
        setContentView(mGLView);
    }
    
    protected void exitGame() {
    	Log.d(TAG, "BrickBreakerActivity pausing");
        
    	super.onPause();
        mGLView.onPause();
        
        updateHighScore(BrickBreakerState.getFinalScore());
    }
    
    @Override
    protected void onPause() {
		exitGame();
    }

    @Override
    protected void onResume() {
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

	@Override
	public void onBackPressed() {
		Log.d(TAG, "BrickBreakerActivity.onBackPressed");
		
		super.onBackPressed();
		
		overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
		
		// finish();
	}

    /**
     * Configures the BrickBreakerState object with the configuration options set by MainActivity.
     */
    private void configureBrickBreakerState() {
        int rows = BrickBreakerState.BRICK_ROWS;
		int columns = BrickBreakerState.BRICK_COLUMNS;
        int[][] mBrickStatesConfig = new int[rows][columns];

        if(sLevelGame > MAX_LEVEL){            
        	throw new RuntimeException("bad difficulty index " + sLevelGame);
        }
        
        // Configure the level of game
		ParametersConfig param = LevelParameters.configLevelParameters(sLevelGame);				
        
        mBrickStatesConfig = Library.buildBrickStatesConfig(rows, columns, param.configStr);
        
        mBrickBreakerState.setBallSizeMultiplier(param.ballSize);
        mBrickBreakerState.setPaddleSizeMultiplier(param.paddleSize);
        mBrickBreakerState.setScoreMultiplier(param.scoreMultiplier);
        mBrickBreakerState.setMaxLives(param.maxLives);
        mBrickBreakerState.setBallInitialSpeed(param.minSpeed);
        mBrickBreakerState.setBallMaximumSpeed(param.maxSpeed);
        mBrickBreakerState.setGameLevel(sLevelGame);
        mBrickBreakerState.setBrickStatesConfig(mBrickStatesConfig);
        mBrickBreakerState.setBackgroundLevel(param.backgroundTextureImg);
        
        
        
        

        SoundResources.setSoundEffectsEnabled(sSfxEnabled);
    }
	
    //---Returns the default level index
    public static int getLevelIndex() {
        return sLevelGame;
    }
    
    /**
     * Configures various tunable parameters based on the difficulty index.
     * <p>
     * Changing the value will cause a game in progress to reset.
     */
    public static void setLevelIndex(int levelIndex) {
        // This could be coming from preferences set by a different version of the game.  We
        // want to be tolerant of values we don't recognize.
        if (levelIndex < MIN_LEVEL || levelIndex > MAX_LEVEL) {
            Log.w(TAG, "Invalid difficulty index " + levelIndex + ", using default");
            levelIndex = DEFAULT_LEVEL;
        }

        if (sLevelGame != levelIndex) {
            sLevelGame = levelIndex;
            invalidateSavedGame();
        }
    }
    
    //---Returns true if sound effects are enabled, and false otherwise
    public static boolean isSoundEffectsEnabled() {
        return sSfxEnabled;

    }

    //---Enables or disables sound effects
    public static void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
    	sSfxEnabled = soundEffectsEnabled;
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
    
    public void finishActivity(){
    	finish();
    }
}
