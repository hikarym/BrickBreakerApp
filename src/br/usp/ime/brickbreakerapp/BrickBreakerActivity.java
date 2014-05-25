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
//---Activity for the game
public class BrickBreakerActivity extends Activity {
    private static final String TAG = MainActivity.TAG;
    
	public static final int DEFAULT_LEVEL = MainActivity.DEFAULT_LEVEL;
	public static final int MIN_LEVEL = LevelsFragment.MIN_LEVEL;
    public static final int MAX_LEVEL = LevelsFragment.MAX_LEVEL;
    
	// Flag to indicate if sounds effects of game are enabled or not
    private static int sLevelGame = 1;
    
	// Flag to indicate if sounds effects of game are enabled or not
	private static boolean sSfxEnabled;
	
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
    
    @Override
    protected void onPause() {
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

	@Override
	public void onBackPressed() {
		Log.d(TAG, "BrickBreakerActivity.onBackPressed");
		
		onPause();

        AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog))
				.setTitle(R.string.app_name)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.msg_exit)
				.setNegativeButton(R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				        onResume();
					}
		        })
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
			        	dialog.dismiss();
			        	finish();
			        	overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_right);
					}
		        }).create();
		
        dialog.show();
	}
	
    /**
     * Configures the BrickBreakerState object with the configuration options set by MainActivity.
     */
    private void configureBrickBreakerState() {
        int maxLives, minSpeed, maxSpeed;
        float ballSize, paddleSize, scoreMultiplier;
        int rows = BrickBreakerState.BRICK_ROWS;
		int columns = BrickBreakerState.BRICK_COLUMNS;
        int[][] mBrickStatesConfig = new int[rows][columns];
        String[] configStr = null;
        String mBackgroundTextureImg = "drawable/background_3";

        switch (sLevelGame) {
            case 1:                     // easy
                ballSize = 2.0f;
                paddleSize = 2.0f;
                scoreMultiplier = 0.75f;
                maxLives = 4;
                minSpeed = 200;
                maxSpeed = 500;
                
                // configuration of bricks
                // NIVEL I: NORMAL BRICKS
        		configStr = new String[]{"111111111","111111111", "111111111", "111111111", "111111111", "111111111"};   
        		mBackgroundTextureImg = "drawable/background_3";
                break;
            case 2:                     // normal
                ballSize = 1;
                paddleSize = 1.0f;
                scoreMultiplier = 1.0f;
                maxLives = 3;
                minSpeed = 300;
                maxSpeed = 800;
                
                // configuration of bricks
        		// NIVEL II: Letter I
        		configStr = new String[]{"001111100","001111100", "000232000", "000232000", "001111100", "001111100"};
        		mBackgroundTextureImg = "drawable/background_4";
                break;
            case 3:                     // normal
            	ballSize = 1;
                paddleSize = 1.0f;
                scoreMultiplier = 1.0f;
                maxLives = 3;
                minSpeed = 300;
                maxSpeed = 800;
                
                // configuration of bricks
        		// NIVEL III: FACE
        		configStr = new String[]{"000111000", "111000111", "011111110", "111414111", "101111101", "000101000"};
        		mBackgroundTextureImg = "drawable/background_5";
                break;
            case 4:                     // hard
            	ballSize = 1;
                paddleSize = 1.0f;
                scoreMultiplier = 1.0f;
                maxLives = 3;
                minSpeed = 300;
                maxSpeed = 800;
                		
        		// NIVEL IV: CASTLE
        		configStr = new String[]{"021222120", "021222120", "021222120", "021111120", "222222222", "220222522"};
        		mBackgroundTextureImg = "drawable/background_6";
                break;
                
            case 5:                     // hard
                ballSize = 1.0f;
                paddleSize = 0.8f;
                scoreMultiplier = 1.25f;
                maxLives = 3;
                minSpeed = 600;
                maxSpeed = 1200;
                
                // configuration of bricks
        		// NIVEL V : (SNAKE)
        		configStr = new String[]{"333033303", "202020202", "202020202", "202020202", "202020202", "303330333"};
        		mBackgroundTextureImg = "drawable/background_7";
                break;
                
            case 6:                     // hard
                ballSize = 1.0f;
                paddleSize = 0.8f;
                scoreMultiplier = 1.25f;
                maxLives = 3;
                minSpeed = 600;
                maxSpeed = 1200;
                
                // configuration of bricks    
        		// NIVEL VI : USP
        		configStr = new String[]{"222222222", "111333100", "101003100", "101333111", "101300101", "101333111"};
        		mBackgroundTextureImg = "drawable/background_8";
                break;    
                
            case 7:                     // hard
            	 ballSize = 1.0f;
                 paddleSize = 0.5f;
                 scoreMultiplier = 0.1f;
                 maxLives = 1;
                 minSpeed = 1000;
                 maxSpeed = 100000;
                
                // configuration of bricks    
        		// NIVEL VI : USP
        		configStr = new String[]{"222222222", "111333100", "101003100", "101333111", "101300101", "101333111"};  
        		mBackgroundTextureImg = "drawable/background_8";
                break;    
                
            default:
                throw new RuntimeException("bad difficulty index " + sLevelGame);
        }
        
        mBrickStatesConfig = buildBrickStatesConfig(rows, columns, configStr);

        mBrickBreakerState.setBallSizeMultiplier(ballSize);
        mBrickBreakerState.setPaddleSizeMultiplier(paddleSize);
        mBrickBreakerState.setScoreMultiplier(scoreMultiplier);
        mBrickBreakerState.setMaxLives(maxLives);
        mBrickBreakerState.setBallInitialSpeed(minSpeed);
        mBrickBreakerState.setBallMaximumSpeed(maxSpeed);
        mBrickBreakerState.setGameLevel(sLevelGame);
        mBrickBreakerState.setBrickStatesConfig(mBrickStatesConfig);
        mBrickBreakerState.setBackgroundLevel(mBackgroundTextureImg);
        

        SoundResources.setSoundEffectsEnabled(sSfxEnabled);
    }
    
    /**
	 * Build a brick configuration of the game
	 * (Each brick must be a value between 0 e 4) 
	 * @param configStr: array[001111100, 001111100, 000232000, 000232000, 001111100, 001111100])
	 * (The array must be BRICK_ROWS elements and 
	 * each string must be have BRICK_COLUMNS characters)
	 * 
	 */
	private int[][] buildBrickStatesConfig(int rows, int columns, String[] configStr){
		int[][] mBrickStatesConfig = new int[rows][columns];
		for (int i = 0; i < rows; i++) {
			
			for (int j = 0; j < columns; j++) {				
				mBrickStatesConfig[i][j] = Integer.parseInt(
						String.valueOf(configStr[i].charAt(j)));
			}
		}
		return mBrickStatesConfig;
		
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
        
        if (lastScore >= 0)
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
