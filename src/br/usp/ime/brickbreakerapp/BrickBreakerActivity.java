package br.usp.ime.brickbreakerapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity for the actual game.  This is largely just a wrapper for our GLSurfaceView.
 */
public class BrickBreakerActivity extends Activity {
    private static final String TAG = MainActivity.TAG;


    private static final int LEVEL_MIN = 1;
    private static final int LEVEL_MAX = 6;        // inclusive
    private static final int LEVEL_DEFAULT = 1;
    //private static int sDifficultyIndex = 1;
    private static int sLevelGame = 1;
        

    //private static boolean sNeverLoseBall;
    
	// Flag to indicate if sounds effects of game are enabled or not
	private static boolean statusSoundEffectsEnabled;
	
    // The Activity has one View, a GL surface.
    private BrickBreakerSurfaceView mGLView;

    // Live game state.    
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
        

        SoundResources.setSoundEffectsEnabled(statusSoundEffectsEnabled);
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
    
    /**
     * Gets the difficulty index, used to configure the game parameters.
     */
    public static int getLevelIndex() {
        return sLevelGame;
    }

    /**
     * Gets the default difficulty index.  This should be used if no preference has been saved.
     */
    public static int getDefaultLevelIndex() {
        return LEVEL_DEFAULT;
    }

    /**
     * Configures various tunable parameters based on the difficulty index.
     * <p>
     * Changing the value will cause a game in progress to reset.
     */
    public static void setLevelIndex(int levelIndex) {
        // This could be coming from preferences set by a different version of the game.  We
        // want to be tolerant of values we don't recognize.
        if (levelIndex < LEVEL_MIN || levelIndex > LEVEL_MAX) {
            Log.w(TAG, "Invalid difficulty index " + levelIndex + ", using default");
            levelIndex = LEVEL_DEFAULT;
        }

        if (sLevelGame != levelIndex) {
            sLevelGame = levelIndex;
            invalidateSavedGame();
        }
    }
    
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
    
    public void finishActivity(){
    	finish();
    }
}
